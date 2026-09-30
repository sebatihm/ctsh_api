package com.ctsh.ctsh_api.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ctsh.ctsh_api.Models.Mood;

import jakarta.transaction.Transactional;


public interface MoodRepository extends JpaRepository<Mood, String> {
  Optional<Mood> findByName(String name);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query(value = "INSERT INTO mood (uuid, name, count) VALUES (UUID(), :name, 1) "
              + "ON DUPLICATE KEY UPDATE count = count + 1", nativeQuery = true)
  void upsertIncrement(@Param("name") String name);
}
