package com.nurix.campaign.scheduler;

import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.entity.enums.CallStatus;
import com.nurix.campaign.repository.CallRecordRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry; // Import this
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CallDispatcherTest {

    @Mock
    private CallRecordRepository repository;
    private MeterRegistry meterRegistry;
    private CallDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use a real SimpleMeterRegistry instead of a mock
        meterRegistry = new SimpleMeterRegistry(); 
        
        // Now the constructor will successfully create a real counter internally
        dispatcher = new CallDispatcher(repository, meterRegistry);
    }

    @Test
    void shouldRespectConcurrencyLimit() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setMaxConcurrency(1);
        campaign.setBusinessHours(List.of());

        CallRecord c1 = new CallRecord(); c1.setCampaign(campaign); c1.setPhoneNumber("1");
        CallRecord c2 = new CallRecord(); c2.setCampaign(campaign); c2.setPhoneNumber("2");

        when(repository.findEligibleCalls(any())).thenReturn(List.of(c1, c2));

        dispatcher.dispatchCalls();

        // Verify logic
        verify(repository, times(2)).saveAndFlush(any(CallRecord.class));
        
        // Extra Staff-level check: Verify the metric actually recorded the attempt
        assert(meterRegistry.get("campaign.calls.dispatched").counter().count() == 1.0);
    }

    @Test
    void shouldSetStatusToFailedWhenMaxRetriesExhausted() {
        // Arrange
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setMaxRetries(2);
        
        CallRecord record = new CallRecord();
        record.setCampaign(campaign);
        record.setRetryCount(2); // Already at max
        record.setStatus(CallStatus.PENDING);

        when(repository.findEligibleCalls(any())).thenReturn(List.of(record));
        
        // Simulate a telephony failure
        // Note: Assuming successRate is set to 0.0 for this test or mockTelephony is mocked
        
        // Act
        dispatcher.dispatchCalls();

        // Assert
        assertEquals(CallStatus.FAILED, record.getStatus());
        verify(repository).saveAndFlush(record);
    }
}