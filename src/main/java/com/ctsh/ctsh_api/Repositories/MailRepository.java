package com.ctsh.ctsh_api.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctsh.ctsh_api.Models.Mail;

public interface MailRepository extends JpaRepository<Mail, String> {
  List<Mail> findByFrom_Uuid(String userUuid);

  List<Mail> findByTo_Uuid(String userUuid);
}
