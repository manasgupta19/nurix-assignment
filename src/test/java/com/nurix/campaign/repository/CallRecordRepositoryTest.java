package com.nurix.campaign.repository;

import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.Campaign;
import com.nurix.campaign.entity.enums.CallStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CallRecordRepositoryTest {

    @Autowired
    private CallRecordRepository repository;

    @Autowired
    private TestEntityManager entityManager; // FIX: Use this to persist prerequisites

    @Test
    void shouldFindOnlyEligibleCalls() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Create and persist a Campaign first
        Campaign campaign = new Campaign();
        campaign.setName("Test Campaign");
        campaign.setMaxConcurrency(5);
        campaign.setBusinessHours(java.util.Collections.emptyList());
        entityManager.persist(campaign);

        // 2. A call that is ready
        CallRecord readyCall = new CallRecord();
        readyCall.setPhoneNumber("111");
        readyCall.setStatus(CallStatus.PENDING);
        readyCall.setNextRetryAt(now.minusMinutes(10));
        readyCall.setCampaign(campaign); // FIX: Attach campaign
        entityManager.persist(readyCall);

        // 3. A call that is NOT ready
        CallRecord futureCall = new CallRecord();
        futureCall.setPhoneNumber("222");
        futureCall.setStatus(CallStatus.PENDING);
        futureCall.setNextRetryAt(now.plusMinutes(10));
        futureCall.setCampaign(campaign); // FIX: Attach campaign
        entityManager.persist(futureCall);
        
        entityManager.flush();

        List<CallRecord> eligible = repository.findEligibleCalls(now);

        assertEquals(1, eligible.size());
        assertEquals("111", eligible.get(0).getPhoneNumber());
    }
}