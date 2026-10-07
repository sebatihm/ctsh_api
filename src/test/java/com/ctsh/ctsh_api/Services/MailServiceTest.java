package com.ctsh.ctsh_api.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ctsh.ctsh_api.Dtos.MailResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.MailRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.MailMapper;
import com.ctsh.ctsh_api.Mappers.UserMapper;
import com.ctsh.ctsh_api.Models.Mail;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Repositories.MailRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

  @Mock 
  private MailRepository mailRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FileService fileService;

  @InjectMocks
  private MailService mailService;

  private User createTestUser(String name, String email) {
    User user = new User();
    user.setUuid(UUID.randomUUID().toString());
    user.setName(name);
    user.setProfilePicture("profile.jpg");
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole(Role.USER);
    return user;
  }


  private Mail createTestMail(String from, String to, String body) {
    Mail mail = new Mail();
    mail.setFrom(createTestUser(from, "senderemail@example.com"));
    mail.setTo(createTestUser(to, "receiveremail@example.com"));
    mail.setMessage(body);
    return mail;
  }

  @BeforeEach
  private void setUp() {
    UserMapper userMapper = new UserMapper(fileService);
    MailMapper mailMapper = new MailMapper (userMapper); 
    mailService = new MailService(mailRepository, mailMapper, userRepository);
  }

  @Test
  void testGetAllMails() {
    when(mailRepository.findAll()).thenReturn(List.of(createTestMail("Subject 1", "Subject 1.1", "Body 1"), createTestMail("Subject 2", "Subject 2.1", "Body 2")));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    List<MailResponseDto> result = mailService.getMails();
    assertEquals(2, result.size());
    assertEquals("Subject 1", result.get(0).getFrom().getName());
    assertEquals("Subject 1.1", result.get(0).getTo().getName());
    assertEquals("Body 1", result.get(0).getMessage());
    assertEquals("Subject 2", result.get(1).getFrom().getName());
    assertEquals("Subject 2.1", result.get(1).getTo().getName());
    assertEquals("Body 2", result.get(1).getMessage());
  }

  @Test
  void testGetMailByUuid() {
    Mail testMail = createTestMail("Subject 1", "Subject 1.1", "Body 1");
    when(mailRepository.findById(testMail.getUuid())).thenReturn(Optional.of(testMail));
    when(fileService.getPublicUrl("profile.jpg")).thenReturn("profile.jpg");

    MailResponseDto result = mailService.getMailByUuid(testMail.getUuid());
    assertEquals("Subject 1", result.getFrom().getName());
    assertEquals("Subject 1.1", result.getTo().getName());
    assertEquals("Body 1", result.getMessage());
  }

  @Test
  void testGetMailByUuidNotFound() {
    String nonExistentUuid = UUID.randomUUID().toString();
    when(mailRepository.findById(nonExistentUuid)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      mailService.getMailByUuid(nonExistentUuid);
    });
  }

  @Test
  void testCreateMail() {
    User fromUser = createTestUser("Sender", "senderemail@example.com");
    User toUser = createTestUser("Receiver", "receiveremail@example.com");

    MailRequestDto mailRequest = new MailRequestDto(
      fromUser.getUuid(),
      toUser.getUuid(),
      "Test message"
    );

    when(userRepository.findById(fromUser.getUuid())).thenReturn(Optional.of(fromUser));
    when(userRepository.findById(toUser.getUuid())).thenReturn(Optional.of(toUser));
    when(mailRepository.save(any(Mail.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MailResponseDto result = mailService.createMail(mailRequest);

    assertEquals("Sender", result.getFrom().getName());
    assertEquals("Receiver", result.getTo().getName());
    assertEquals("Test message", result.getMessage());
  }

  @Test
  void testCreateMailUserNotFound() {
    String nonExistentUuid = UUID.randomUUID().toString();
    MailRequestDto mailRequest = new MailRequestDto(
      nonExistentUuid,
      nonExistentUuid,
      "Test message"
    );

    when(userRepository.findById(nonExistentUuid)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      mailService.createMail(mailRequest);
    });
  }

  @Test
  void testUpdateMail() {
    Mail existingMail = createTestMail("Subject 1", "Subject 1.1", "Body 1");
    User newFromUser = createTestUser("New Sender", "newsenderemail@example.com");
    User newToUser = createTestUser("New Receiver", "newreceiveremail@example.com");

    MailRequestDto mailUpdate = new MailRequestDto(
      newFromUser.getUuid(),
      newToUser.getUuid(),
      "Updated message"
    );

    when(mailRepository.findById(existingMail.getUuid())).thenReturn(Optional.of(existingMail));
    when(userRepository.findById(newFromUser.getUuid())).thenReturn(Optional.of(newFromUser));
    when(userRepository.findById(newToUser.getUuid())).thenReturn(Optional.of(newToUser));
    when(mailRepository.save(any(Mail.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MailResponseDto result = mailService.updateMail(existingMail.getUuid(), mailUpdate);

    assertEquals("New Sender", result.getFrom().getName());
    assertEquals("New Receiver", result.getTo().getName());
    assertEquals("Updated message", result.getMessage());
  }

  @Test
  void testUpdateMailNotFound() {
    String nonExistentUuid = UUID.randomUUID().toString();
    MailRequestDto mailUpdate = new MailRequestDto(
      nonExistentUuid,
      nonExistentUuid,
      "Updated message"
    );

    when(mailRepository.findById(nonExistentUuid)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      mailService.updateMail(nonExistentUuid, mailUpdate);
    });
  }

  @Test
  void testUpdatePartialMail() {
    Mail existingMail = createTestMail("Subject 1", "Subject 1.1", "Body 1");
    User newFromUser = createTestUser("New Sender", "newsenderemail@example.com");

    MailRequestDto mailUpdate = new MailRequestDto(
      newFromUser.getUuid(),
      null,
      null
    );

    when(mailRepository.findById(existingMail.getUuid())).thenReturn(Optional.of(existingMail));
    when(userRepository.findById(newFromUser.getUuid())).thenReturn(Optional.of(newFromUser));
    when(mailRepository.save(any(Mail.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MailResponseDto result = mailService.updateMail(existingMail.getUuid(), mailUpdate);

    assertEquals("New Sender", result.getFrom().getName());
  }

  @Test 
  void deleteMail() {
    Mail existingMail = createTestMail("Subject 1", "Subject 1.1", "Body 1");

    when(mailRepository.findById(existingMail.getUuid())).thenReturn(Optional.of(existingMail));
    mailService.deleteMail(existingMail.getUuid());

    verify(mailRepository).delete(existingMail);
  }

  @Test
  void deleteMailNotFound() {
    String nonExistentUuid = UUID.randomUUID().toString();
    when(mailRepository.findById(nonExistentUuid)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      mailService.deleteMail(nonExistentUuid);
    });
  }

}