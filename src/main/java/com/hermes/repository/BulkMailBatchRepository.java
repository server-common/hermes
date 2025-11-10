package com.hermes.repository;

import com.hermes.entity.BulkMailBatch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkMailBatchRepository extends JpaRepository<BulkMailBatch, Long> {

    Optional<BulkMailBatch> findByBatchIdAndGroupKey(String batchId, String groupKey);
}