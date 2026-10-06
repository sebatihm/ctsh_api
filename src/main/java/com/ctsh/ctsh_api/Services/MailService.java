package com.ctsh.ctsh_api.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ctsh.ctsh_api.Dtos.MailResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.MailRequestDto;
import com.ctsh.ctsh_api.Exceptions.ResourceNotFoundException;
import com.ctsh.ctsh_api.Mappers.MailMapper;
import com.ctsh.ctsh_api.Models.Mail;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Repositories.MailRepository;
import com.ctsh.ctsh_api.Repositories.UserRepository;

@Service
public class MailService {
  private final MailRepository mailRepository;
  private final UserRepository userRepository;
  private final MailMapper mailMapper;

  public MailService(MailRepository mailRepository, MailMapper mailMapper, UserRepository userRepository) {
    this.mailRepository = mailRepository;
    this.mailMapper = mailMapper;
    this.userRepository = userRepository;
  }

  public MailResponseDto getMailByUuid(String uuid) {
    return mailMapper.toResponseDto(getMail(uuid));
  }

  public List<MailResponseDto> getMails() {
    return mailRepository.findAll()
      .stream()
      .map(mailMapper::toResponseDto)
      .toList();
  }

  public MailResponseDto createMail(MailRequestDto dto) {
    Mail mail = new Mail();
    mail.setFrom(getUser(dto.fromUuid()));
    mail.setTo(getUser(dto.toUuid()));
    mail.setMessage(dto.message());
    return mailMapper.toResponseDto(mailRepository.save(mail));
  }

  public MailResponseDto updateMail(String uuid, MailRequestDto dto) {
    Mail mail = getMail(uuid);

    if (dto.fromUuid() != null) {
      mail.setFrom(getUser(dto.fromUuid()));
    }

    if (dto.toUuid() != null) {
      mail.setTo(getUser(dto.toUuid()));
    }

    if (dto.message() != null) {
      mail.setMessage(dto.message());
    }
    return mailMapper.toResponseDto(mailRepository.save(mail));
  }

  public void deleteMail(String uuid) {
    Mail mail = getMail(uuid);
    mailRepository.delete(mail);
  }

  private User getUser(String uuid) {
    return this.userRepository.findById(uuid)
      .orElseThrow(() -> new ResourceNotFoundException("User with uuid: " + uuid + " not found"));
  }

  private Mail getMail(String uuid) {
    return this.mailRepository.findById(uuid)
      .orElseThrow(() -> new ResourceNotFoundException("Mail not found"));
  }

}
