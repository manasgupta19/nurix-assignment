package com.nurix.campaign.entity;

import com.nurix.campaign.entity.enums.CampaignStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;

@Entity
@Data // Still kept for toString, equals, and hashCode
@Table(name = "campaigns")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status = CampaignStatus.PENDING;

    private int maxConcurrency;
    private int maxRetries;
    private long retryDelaySeconds;
    private String timeZone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<BusinessWindow> businessHours;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CallRecord> calls;

    // --- MANUALLY IMPLEMENTED GETTERS ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public CampaignStatus getStatus() { return status; }
    public int getMaxConcurrency() { return maxConcurrency; }
    public int getMaxRetries() { return maxRetries; }
    public long getRetryDelaySeconds() { return retryDelaySeconds; }
    public String getTimeZone() { return timeZone; }
    public List<BusinessWindow> getBusinessHours() { return businessHours; }
    public List<CallRecord> getCalls() { return calls; }

    // --- MANUALLY IMPLEMENTED SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setStatus(CampaignStatus status) { this.status = status; }
    public void setMaxConcurrency(int maxConcurrency) { this.maxConcurrency = maxConcurrency; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public void setRetryDelaySeconds(long retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public void setBusinessHours(List<BusinessWindow> businessHours) { this.businessHours = businessHours; }
    public void setCalls(List<CallRecord> calls) { this.calls = calls; }

    /**
     * Nested static class for JSONB mapping of Business Hours
     */
    @Data
    public static class BusinessWindow {
        private String dayOfWeek;
        private String startTime;
        private String endTime;

        // --- NESTED GETTERS ---
        public String getDayOfWeek() { return dayOfWeek; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }

        // --- NESTED SETTERS ---
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }
}