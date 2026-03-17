package com.nurix.campaign.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nurix.campaign.entity.enums.CallStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data // Kept for toString, equals, and hashCode
@Table(name = "call_records")
public class CallRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private CallStatus status = CallStatus.PENDING;

    private int retryCount = 0;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime nextRetryAt;

    // --- MANUALLY IMPLEMENTED GETTERS ---
    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public String getPhoneNumber() { return phoneNumber; }
    public CallStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }

    // --- MANUALLY IMPLEMENTED SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setStatus(CallStatus status) { this.status = status; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
}