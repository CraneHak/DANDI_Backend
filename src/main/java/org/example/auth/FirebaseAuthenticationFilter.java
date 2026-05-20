package org.example.auth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final FirebaseProperties firebaseProperties;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final UserProfileRepository userProfileRepository;

    public FirebaseAuthenticationFilter(
            FirebaseProperties firebaseProperties,
            FirebaseTokenVerifier firebaseTokenVerifier,
            UserProfileRepository userProfileRepository
    ) {
        this.firebaseProperties = firebaseProperties;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
            return;
        }

        String idToken = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (idToken.isEmpty()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing Firebase ID token.");
            return;
        }

        try {
            DecodedFirebaseUser decodedUser = firebaseTokenVerifier.verify(idToken);
            String email = decodedUser.email();
            if (email == null || !decodedUser.emailVerified()) {
                writeError(response, HttpStatus.FORBIDDEN, "Verified email is required.");
                return;
            }

            String allowedDomain = firebaseProperties.getAllowedDomain();
            if (!email.toLowerCase().endsWith("@" + allowedDomain.toLowerCase())) {
                writeError(response, HttpStatus.FORBIDDEN, "Only @" + allowedDomain + " email is allowed.");
                return;
            }

            boolean admin = isAdmin(decodedUser.uid(), email);
            FirebaseAuthenticationToken authentication =
                    new FirebaseAuthenticationToken(decodedUser.uid(), email, admin);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (InvalidFirebaseTokenException ex) {
            writeError(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (!PATH_MATCHER.match("/api/**", path)) {
            return true;
        }
        return PATH_MATCHER.match("/api/public/**", path);
    }

    private boolean isAdmin(String uid, String email) {
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            boolean emailAdmin = firebaseProperties.getAdminEmails().stream()
                    .map(value -> value.trim().toLowerCase())
                    .anyMatch(normalized::equals);
            if (emailAdmin) {
                return true;
            }
        }
        if (uid == null || uid.isBlank()) {
            return false;
        }
        if (firebaseProperties.getAdminUids().contains(uid)) {
            return true;
        }
        return userProfileRepository.findByFirebaseUid(uid)
                .map(UserProfile::getRole)
                .map(UserRole.ROLE_ADMIN::equals)
                .orElse(false);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
