package com.ctsh.ctsh_api.Services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.ctsh.ctsh_api.Exceptions.BadRequestException;


public class FileServiceTest {
  @TempDir
  Path tempDir;

  private FileService fileService;

  @BeforeEach
  void setUp() {
      fileService = new FileService(
          tempDir.toString(),
          "/uploads",
          "http://localhost:8080",
          "/api"
      );
  }

  @Test
  void testGeneratePublicUrl() {

      String result = fileService.getPublicUrl("foto.jpg");

      assertEquals(
          "http://localhost:8080/api/uploads/profiles/foto.jpg",
          result
      );
  }
  
  @Test
  void testReturnNullWhenFileNameIsNull() {

    String result = fileService.getPublicUrl(null);
    assertNull(result);
  }

  @Test
  void testReturnNullWhenFileNameIsBlank() {

      String result = fileService.getPublicUrl("   ");
      assertNull(result);
  }

  @Test
  void testAcceptValidImage() {

      MockMultipartFile file = new MockMultipartFile(
          "profilePicture",
          "photo.jpg",
          "image/jpeg",
          "fake image".getBytes()
      );

      assertDoesNotThrow(
          () -> fileService.validateFile(file)
      );
  }

  @Test
  void testRejectFileLargerThan5MB() {

    byte[] content = new byte[5 * 1024 * 1024 + 1];

    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        "photo.jpg",
        "image/jpeg",
        content
    );

    BadRequestException exception = assertThrows(
        BadRequestException.class,
        () -> fileService.validateFile(file)
    );

    assertEquals(
        "File size exceeds the 5 MB limit",
        exception.getMessage()
    );
  }

  @Test
  void testRejectNonImageFile() {

      MockMultipartFile file = new MockMultipartFile(
          "profilePicture",
          "document.pdf",
          "application/pdf",
          "fake pdf".getBytes()
      );

      assertThrows(
          BadRequestException.class,
          () -> fileService.validateFile(file)
      );
  }

  @Test
  void testRejectFileWithoutContentType() {

      MockMultipartFile file = new MockMultipartFile(
          "profilePicture",
          "photo.jpg",
          null,
          "fake image".getBytes()
      );

      assertThrows(
          BadRequestException.class,
          () -> fileService.validateFile(file)
      );
  }

  @Test
  void testSaveFile() throws IOException {

    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        "photo.jpg",
        "image/jpeg",
        "image content".getBytes()
    );

    String fileName = fileService.saveFile(file);

    assertNotNull(fileName);
    assertTrue(fileName.endsWith(".jpg"));

    Path savedFile =
        tempDir.resolve("profiles").resolve(fileName);

    assertTrue(Files.exists(savedFile));

    assertEquals(
        "image content",
        Files.readString(savedFile)
    );
  }

  @Test
  void testRejectInvalidExtension() {

    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        "malware.exe",
        "application/octet-stream",
        "test".getBytes()
    );

    BadRequestException exception = assertThrows(
        BadRequestException.class,
        () -> fileService.saveFile(file)
    );

    assertTrue(
        exception.getMessage()
            .contains("Invalid file extension")
    );
  }

  @Test
  void testRejectFileWithoutExtension() {

    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        "photo",
        "image/jpeg",
        "test".getBytes()
    );

    assertThrows(
        BadRequestException.class,
        () -> fileService.saveFile(file)
    );
  }

  @Test
  void testDeleteFile() throws IOException {

    Path profilesDir = tempDir.resolve("profiles");
    Files.createDirectories(profilesDir);
    Path file = profilesDir.resolve("photo.jpg");

    Files.writeString(file, "hello");

    fileService.deleteFile("photo.jpg");
    assertFalse(Files.exists(file));
  }

  @Test
  void testNotFailWhenFileDoesNotExist() {

    assertDoesNotThrow(
        () -> fileService.deleteFile("does-not-exist.jpg")
    );
  }

  @Test
  void testRejectPathTraversal() {

    assertThrows(
        BadRequestException.class,
        () -> fileService.deleteFile("../../secret.txt")
    );
  }

  @Test
  void testUpdateFile() throws IOException {

    Path profilesDir = tempDir.resolve("profiles");
    Files.createDirectories(profilesDir);
    Path oldFile = profilesDir.resolve("old.jpg");
    Files.writeString(oldFile, "old content");

    MockMultipartFile newFile = new MockMultipartFile(
        "profilePicture",
        "new.jpg",
        "image/jpeg",
        "new content".getBytes()
    );

    String newFileName =
        fileService.updateFile(newFile, "old.jpg");

    assertNotNull(newFileName);
    assertTrue(newFileName.endsWith(".jpg"));

    assertFalse(Files.exists(oldFile));

    Path savedFile =
        profilesDir.resolve(newFileName);

    assertTrue(Files.exists(savedFile));

    assertEquals(
        "new content",
        Files.readString(savedFile)
    );
  }

  @Test
  void testRejectFileWithNullFilename() {

    MockMultipartFile file = new MockMultipartFile(
        "profilePicture",
        null,
        "image/jpeg",
        "test".getBytes()
    );

    assertThrows(
        BadRequestException.class,
        () -> fileService.saveFile(file)
    );
  }
}
