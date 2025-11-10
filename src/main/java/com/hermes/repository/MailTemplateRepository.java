package com.hermes.repository;

import com.hermes.entity.MailTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {

    Optional<MailTemplate> findByNameAndGroupKey(String name, String groupKey);

    boolean existsByNameAndGroupKey(String name, String groupKey);

    Page<MailTemplate> findByGroupKey(String groupKey, Pageable pageable);

    Optional<MailTemplate> findByIdAndGroupKey(Long id, String groupKey);

    @Query("SELECT mt FROM MailTemplate mt WHERE mt.groupKey = :groupKey AND (" +
        "LOWER(mt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(mt.subject) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MailTemplate> findByKeywordAndGroupKey(@Param("keyword") String keyword, @Param("groupKey") String groupKey, Pageable pageable);

    @Query("SELECT DISTINCT mt.groupKey FROM MailTemplate mt WHERE mt.groupKey IS NOT NULL AND mt.groupKey <> ''")
    List<String> findDistinctGroupKeys();
}