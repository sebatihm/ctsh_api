package com.ctsh.ctsh_api.Controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ctsh.ctsh_api.Dtos.MailResponseDto;
import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Dtos.Validation.MailRequestDto;
import com.ctsh.ctsh_api.Models.Enum.Role;
import com.ctsh.ctsh_api.Services.MailService;

@WebMvcTest(controllers = MailController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MailControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MailService mailService;

  private MailResponseDto mail() {

    UserResponseDto receiver = new UserResponseDto();
    receiver.setUuid("recipient-uuid");
    receiver.setName("Jane Smith");
    receiver.setEmail("jane1234@gmail.com");
    receiver.setRole(Role.USER);
    receiver.setProfilePicture("test.jpg");

    UserResponseDto sender = new UserResponseDto();
    sender.setUuid("sender-uuid");
    sender.setName("John Doe");
    sender.setEmail("john.doe@example.com");
    sender.setRole(Role.USER);
    sender.setProfilePicture("test.jpg");

    MailResponseDto mail = new MailResponseDto();
    mail.setUuid("uuid-1234");
    mail.setFrom(sender);
    mail.setTo(receiver);
    mail.setMessage("Hello, this is a test email.");
    return mail;
  }



  @Test
  void testGetAllMails() throws Exception {
    when(mailService.getMails()).thenReturn(List.of(mail()));
    
    mockMvc.perform(get("/mail"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0].uuid").value("uuid-1234"))
      .andExpect(jsonPath("$.data[0].from.name").value("John Doe"))
      .andExpect(jsonPath("$.data[0].to.name").value("Jane Smith"))
      .andExpect(jsonPath("$.data[0].message").value("Hello, this is a test email."));
  }

  @Test
  void testGetMailByUuid() throws Exception {
    when(mailService.getMailByUuid("uuid-1234")).thenReturn(mail());

    mockMvc.perform(get("/mail/uuid-1234"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.uuid").value("uuid-1234"))
      .andExpect(jsonPath("$.data.from.name").value("John Doe"))
      .andExpect(jsonPath("$.data.to.name").value("Jane Smith"))
      .andExpect(jsonPath("$.data.message").value("Hello, this is a test email."));

  }

  @Test
  void testCreateMail() throws Exception {
    MailRequestDto requestDto = new MailRequestDto("sender-uuid", "recipient-uuid", "Hello, this is a test email.");
    when(mailService.createMail(requestDto))
      .thenReturn(mail());

    mockMvc.perform(post("/mail")
      .contentType("application/json")
      .content("""
        {
          "fromUuid": "sender-uuid",
          "toUuid": "recipient-uuid",
          "message": "Hello, this is a test email."
        }
        """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.uuid").value("uuid-1234"))
      .andExpect(jsonPath("$.data.from.name").value("John Doe"))
      .andExpect(jsonPath("$.data.to.name").value("Jane Smith"))
      .andExpect(jsonPath("$.data.message").value("Hello, this is a test email."));
  }


  @Test
  void testCreateInvalidMail() throws Exception {

    mockMvc.perform(post("/mail")
      .contentType("application/json")
      .content("""
        {
          "fromUuid": "sender-uuid",
          "toUuid": "recipient-uuid",
        }
        """))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateMail() throws Exception {
    MailRequestDto requestDto = new MailRequestDto(null, null, "Updated message.");
    when(mailService.updateMail("uuid-1234", requestDto))
      .thenReturn(mail());

    mockMvc.perform(put("/mail/uuid-1234")
      .contentType("application/json")
      .content("""
        {
          "message": "Updated message."
        }
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.uuid").value("uuid-1234"))
      .andExpect(jsonPath("$.data.from.name").value("John Doe"))
      .andExpect(jsonPath("$.data.to.name").value("Jane Smith"));

  }

  @Test
  void testDeleteMailByUuid() throws Exception {
    mockMvc.perform(delete("/mail/uuid-1234"))
      .andExpect(status().isNoContent());

    verify(mailService).deleteMail("uuid-1234");
  }
  
}
