package com.nurix.campaign.scheduler;

import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.repository.CallRecordRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry; // Import this
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

class CallDispatcherTest {

    private CallRecordRepository repository;
    private MeterRegistry meterRegistry;
    private CallDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        repository = mock(CallRecordRepository.class);
        // Use a real SimpleMeterRegistry instead of a mock
        meterRegistry = new SimpleMeterRegistry(); 
        
        // Now the constructor will successfully create a real counter internally
        dispatcher = new CallDispatcher(repository, meterRegistry);
    }

    @Test
    void shouldRespectConcurrencyLimit() {
        Campaign campaign = new Campaign();
        campaign.setMaxConcurrency(1);
        campaign.setBusinessHours(List.of());

        CallRecord c1 = new CallRecord(); c1.setCampaign(campaign); c1.setPhoneNumber("1");
        CallRecord c2 = new CallRecord(); c2.setCampaign(campaign); c2.setPhoneNumber("2");

        when(repository.findEligibleCalls(any())).thenReturn(List.of(c1, c2));

        dispatcher.dispatchCalls();

        // Verify logic
        verify(repository, times(1)).save(any(CallRecord.class));
        
        // Extra Staff-level check: Verify the metric actually recorded the attempt
        assert(meterRegistry.get("campaign.calls.dispatched").counter().count() == 1.0);
    }
}