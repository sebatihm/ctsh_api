package com.ctsh.ctsh_api.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctsh.ctsh_api.Models.Entry;
import com.ctsh.ctsh_api.Models.User;

public interface EntryRepository extends JpaRepository<Entry, String> {
  List<Entry> findByUser(User user);
}
