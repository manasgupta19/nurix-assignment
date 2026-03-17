package com.nurix.campaign.repository;

import com.nurix.campaign.entity.CallRecord;
import com.nurix.campaign.entity.enums.CallStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CallRecordRepositoryTest {

    @Autowired
    private CallRecordRepository repository;

    @Test
    void shouldFindOnlyEligibleCalls() {
        LocalDateTime now = LocalDateTime.now();

        // 1. A call that is ready (nextRetryAt is in the past)
        CallRecord readyCall = new CallRecord();
        readyCall.setPhoneNumber("111");
        readyCall.setStatus(CallStatus.PENDING);
        readyCall.setNextRetryAt(now.minusMinutes(10));
        repository.save(readyCall);

        // 2. A call that is NOT ready (nextRetryAt is in the future)
        CallRecord futureCall = new CallRecord();
        futureCall.setPhoneNumber("222");
        futureCall.setStatus(CallStatus.PENDING);
        futureCall.setNextRetryAt(now.plusMinutes(10));
        repository.save(futureCall);

        List<CallRecord> eligible = repository.findEligibleCalls(now);

        assertEquals(1, eligible.size());
        assertEquals("111", eligible.get(0).getPhoneNumber());
    }
}