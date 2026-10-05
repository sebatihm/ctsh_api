package com.ctsh.ctsh_api.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Models.Enum.Role;

@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
  
  @Autowired 
  private UserRepository userRepository;


  @Test
  void testFindByEmailShouldReturnUser() {

    User user = new User();

    user.setName("Sebastian");
    user.setEmail("sebastian@test.com");
    user.setPassword("hashed-password");
    user.setProfilePicture("image.png");
    user.setRole(Role.USER);

    userRepository.save(user);

    Optional<User> result =
            userRepository.findByEmail("sebastian@test.com");

    assertTrue(result.isPresent());
    assertEquals("sebastian@test.com", result.get().getEmail());
  }

  @Test
  void testFindByEmailShouldReturnEmptyOptional() {
    Optional<User> result =
            userRepository.findByEmail("nonexistent@test.com");

    assertTrue(result.isEmpty());
  }

  @Test
  void testSaveAssignUuid(){
    
    User user = new User();

    user.setPassword("hashed-password");
    user.setProfilePicture("image.png");
    user.setRole(Role.USER);

    userRepository.save(user);

    Optional<User> result = userRepository.findById(user.getUuid());
    assertTrue(result.isPresent());
    assertEquals(user.getUuid(), result.get().getUuid());
  }

  @Test
  void testSaveRejectsDuplicateEmail(){
    User user = new User();

    user.setName("Sebastian");
    user.setEmail("sebastian@test.com");
    user.setPassword("hashed-password");
    user.setProfilePicture("image.png");
    user.setRole(Role.USER);

    userRepository.saveAndFlush(user);

    User user2 = new User();
    user2.setName("Alice");
    user2.setEmail("sebastian@test.com");
    user2.setPassword("hashed-password");
    user2.setProfilePicture("image2.png");
    user2.setRole(Role.USER);


    assertThrows(
        DataIntegrityViolationException.class,
        () -> userRepository.saveAndFlush(user2)
    );

  }

  @Test
  void testFindByIdReturnsEmptyForUnknownId(){
    
    Optional<User> result = userRepository.findById("unknown-uuid");
    assertTrue(result.isEmpty());
  }

  @Test
  void testDeleteRemovesUser(){
    User user = new User();

    user.setName("Sebastian");
    user.setEmail("sebastian@test.com");
    user.setPassword("hashed-password");
    user.setProfilePicture("image.png");
    user.setRole(Role.USER);

    userRepository.saveAndFlush(user);

    userRepository.delete(user);

    Optional<User> result = userRepository.findById(user.getUuid());
    assertTrue(result.isEmpty());
  }

}