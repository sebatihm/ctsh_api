package com.ctsh.ctsh_api.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctsh.ctsh_api.Models.Mail;

public interface MailRepository extends JpaRepository<Mail, String> {
  
}
