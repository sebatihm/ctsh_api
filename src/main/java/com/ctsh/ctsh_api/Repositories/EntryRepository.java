package com.ctsh.ctsh_api.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctsh.ctsh_api.Models.Entry;

public interface EntryRepository extends JpaRepository<Entry, String> {}
