package com.hermes.repository;

import com.hermes.entity.MailTemplate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {

    Optional<MailTemplate> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT mt FROM MailTemplate mt WHERE " +
        "LOWER(mt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(mt.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MailTemplate> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}