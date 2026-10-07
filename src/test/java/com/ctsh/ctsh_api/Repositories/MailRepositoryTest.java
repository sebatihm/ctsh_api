package com.ctsh.ctsh_api.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.ctsh.ctsh_api.Models.Mail;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;

@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MailRepositoryTest {
  @Autowired 
  private MailRepository mailRepository;

  @Autowired
  private UserRepository userRepository;

  private void createTestEmailAndUsers() {
    // Create and save a sender
    User user = new User();
    user.setName("John Doe");
    user.setEmail("john.doe@example.com");
    user.setProfilePicture("test.jpg");
    user.setPassword("password");
    user.setRole(Role.USER);
    userRepository.save(user);

    // Create and save a receiver
    User recipient = new User();
    recipient.setName("Jane Smith");
    recipient.setEmail("jane.smith@example.com");
    recipient.setProfilePicture("test2.jpg");
    recipient.setPassword("password2");
    recipient.setRole(Role.USER);
    userRepository.save(recipient);

    // Create and save a mail
    Mail mail = new Mail();
    mail.setFrom(user);
    mail.setTo(recipient);
    mail.setMessage("Hello, this is a test email.");
    mailRepository.save(mail);
  }

  @Test
  void testSaveMail() {
    createTestEmailAndUsers();

    Mail mail = mailRepository.findAll().get(0);
    assertTrue(mail != null);
    assertEquals("Hello, this is a test email.", mail.getMessage());
    assertEquals("John Doe", mail.getFrom().getName());
  }

  @Test 
  void testFindById() {
    createTestEmailAndUsers();

    Mail mail = mailRepository.findAll().get(0);

    Mail foundMail = mailRepository.findById(mail.getUuid()).orElse(null);
    assertTrue(foundMail != null);
  }



  @Test
  void updateMailMessage() {
    createTestEmailAndUsers();

    Mail mail = mailRepository.findAll().get(0);
    mail.setMessage("This is an updated test email.");
    mailRepository.save(mail);

    Mail updatedMail = mailRepository.findById(mail.getUuid()).orElse(null);
    assertTrue(updatedMail != null);
    assertEquals("This is an updated test email.", updatedMail.getMessage());
  }

  @Test 
  void deleteMail() {
    createTestEmailAndUsers();

    Mail mail = mailRepository.findAll().get(0);
    mailRepository.delete(mail);

    Mail deletedMail = mailRepository.findById(mail.getUuid()).orElse(null);
    assertTrue(deletedMail == null);
  }
}