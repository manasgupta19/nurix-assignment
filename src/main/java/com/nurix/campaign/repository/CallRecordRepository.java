package com.nurix.campaign.repository;

import com.nurix.campaign.entity.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    
    // Pick PENDING calls where the next retry time has arrived or it's a first attempt
    @Query("SELECT r FROM CallRecord r JOIN FETCH r.campaign c " +
       "WHERE r.status = com.nurix.campaign.entity.enums.CallStatus.PENDING " +
       "AND (r.nextRetryAt IS NULL OR r.nextRetryAt <= :now) " +
       "ORDER BY r.nextRetryAt ASC NULLS LAST")
    List<CallRecord> findEligibleCalls(@Param("now") LocalDateTime now);
}