package com.nurix.campaign.scheduler;

import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.enums.CallStatus;
import com.nurix.campaign.repository.CallRecordRepository;
import com.nurix.campaign.util.BusinessHourValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CallDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CallDispatcher.class);

    private final CallRecordRepository callRecordRepository;
    private final Counter callAttemptsCounter;

    public CallDispatcher(CallRecordRepository repository, MeterRegistry registry) {
        this.callRecordRepository = repository;
        this.callAttemptsCounter = Counter.builder("campaign.calls.dispatched")
                .description("Total number of calls attempted")
                .register(registry);
    }

    @Scheduled(fixedDelay = 5000)
    public void dispatchCalls() {
        LocalDateTime now = LocalDateTime.now();
        List<CallRecord> eligibleCalls = callRecordRepository.findEligibleCalls(now);

        if (eligibleCalls.isEmpty()) {
            return;
        }

        log.info("Found {} eligible calls for dispatching.", eligibleCalls.size());

        // Group by campaign to respect per-campaign concurrency
        Map<Long, List<CallRecord>> callsByCampaign = eligibleCalls.stream()
                .collect(Collectors.groupingBy(record -> record.getCampaign().getId()));

        callsByCampaign.forEach((campaignId, records) -> {
            var campaign = records.get(0).getCampaign();

            // 1. Business Hour Validation
            if (!BusinessHourValidator.isWithinBusinessHours(campaign, now)) {
                log.debug("Campaign '{}' is currently outside business hours. Skipping.", campaign.getName());
                return;
            }

            // 2. Concurrency Control
            int limit = campaign.getMaxConcurrency();
            List<CallRecord> callsToProcess = records.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("Dispatching {} calls for campaign '{}' (Limit: {}).", 
                     callsToProcess.size(), campaign.getName(), limit);

            callsToProcess.forEach(this::processCall);
        });
    }

    private void processCall(CallRecord record) {
        log.info(">>> [DISPATCH] Attempt {} for phone: {}", 
                 record.getRetryCount() + 1, record.getPhoneNumber());
        
        callAttemptsCounter.increment();
        
        record.setStatus(CallStatus.IN_PROGRESS);
        record.setLastAttemptAt(LocalDateTime.now());
        callRecordRepository.saveAndFlush(record);

        // Simulate Telephony Outcome
        CallStatus outcome = mockTelephonyCall();
        handleCallOutcome(record, outcome);
    }

    private CallStatus mockTelephonyCall() {
        return Math.random() < 0.7 ? CallStatus.COMPLETED : CallStatus.FAILED;
    }

    private void handleCallOutcome(CallRecord record, CallStatus outcome) {
        if (outcome == CallStatus.COMPLETED) {
            log.info("<<< [SUCCESS] Call to {} completed successfully.", record.getPhoneNumber());
            record.setStatus(CallStatus.COMPLETED);
        } else {
            int currentRetries = record.getRetryCount();
            int maxAllowed = record.getCampaign().getMaxRetries();

            if (currentRetries < maxAllowed) {
                long delay = record.getCampaign().getRetryDelaySeconds();
                record.setRetryCount(currentRetries + 1);
                record.setNextRetryAt(LocalDateTime.now().plusSeconds(delay));
                record.setStatus(CallStatus.PENDING);
                
                log.warn("!!! [RETRY] Call to {} failed. Scheduled retry #{} in {}s", 
                         record.getPhoneNumber(), record.getRetryCount(), delay);
            } else {
                log.error("XXX [FAILED] Max retries ({}) reached for {}. Marking as FAILED.", 
                          maxAllowed, record.getPhoneNumber());
                record.setStatus(CallStatus.FAILED);
            }
        }
        callRecordRepository.save(record);
    }
}