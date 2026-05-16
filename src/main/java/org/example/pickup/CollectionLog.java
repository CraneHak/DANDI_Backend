package org.example.pickup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.example.entity.LostItem;

import java.time.OffsetDateTime;

@Entity
@Table(name = "collection_log")
public class CollectionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItem lostItem;

    @Column(name = "pickup_pass_id")
    private Long pickupPassId;

    @Column(name = "requester_uid", nullable = false, length = 128)
    private String requesterUid;

    @Column(name = "requester_email", nullable = false, length = 255)
    private String requesterEmail;

    @Column(name = "manager_uid", length = 128)
    private String managerUid;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    @Column(name = "otp_token", nullable = false, length = 120)
    private String otpToken;

    @Column(name = "otp_expires_at", nullable = false)
    private OffsetDateTime otpExpiresAt;

    @Column(name = "collected_at")
    private OffsetDateTime collectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private CollectionAction action;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public LostItem getLostItem() {
        return lostItem;
    }

    public void setLostItem(LostItem lostItem) {
        this.lostItem = lostItem;
    }

    public Long getPickupPassId() {
        return pickupPassId;
    }

    public void setPickupPassId(Long pickupPassId) {
        this.pickupPassId = pickupPassId;
    }

    public String getRequesterUid() {
        return requesterUid;
    }

    public void setRequesterUid(String requesterUid) {
        this.requesterUid = requesterUid;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getManagerUid() {
        return managerUid;
    }

    public void setManagerUid(String managerUid) {
        this.managerUid = managerUid;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getOtpToken() {
        return otpToken;
    }

    public void setOtpToken(String otpToken) {
        this.otpToken = otpToken;
    }

    public OffsetDateTime getOtpExpiresAt() {
        return otpExpiresAt;
    }

    public void setOtpExpiresAt(OffsetDateTime otpExpiresAt) {
        this.otpExpiresAt = otpExpiresAt;
    }

    public OffsetDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(OffsetDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    public CollectionAction getAction() {
        return action;
    }

    public void setAction(CollectionAction action) {
        this.action = action;
    }
}
