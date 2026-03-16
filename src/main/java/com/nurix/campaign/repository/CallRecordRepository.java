package com.nurix.campaign.repository;

import com.nurix.campaign.entity.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    
    // Pick PENDING calls where the next retry time has arrived or it's a first attempt
    @Query("SELECT c FROM CallRecord c WHERE c.status = com.nurix.campaign.entity.enums.CallStatus.PENDING " +
       "AND (c.nextRetryAt IS NULL OR c.nextRetryAt <= :now)")
List<CallRecord> findEligibleCalls(@Param("now") LocalDateTime now);
}