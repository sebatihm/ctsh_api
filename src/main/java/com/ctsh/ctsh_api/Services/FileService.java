package com.ctsh.ctsh_api.Services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service 
public class FileService {

  private Path storageDir = Path.of("uploads/profiles");

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");


  public void validateFile(MultipartFile fileData) {
    long MAX_FILE_SIZE = 5 * 1024 * 1024; 

    if (fileData.getSize() > MAX_FILE_SIZE) {
        throw new IllegalArgumentException("File size exceeds the maximum limit");
    }

    String contentType = fileData.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        throw new IllegalArgumentException("Invalid file type. Only image files are allowed.");
    }
  }

  public String saveFile(MultipartFile file) {
    try {
      
      Files.createDirectories(storageDir);
      String ext = extractExtension(file.getOriginalFilename());
      String filename = UUID.randomUUID() + "." + ext;      
      Path target = storageDir.resolve(filename).normalize();
      if (!target.startsWith(storageDir.normalize())) {
        throw new IllegalArgumentException("Invalid file name");
      }
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return filename;                    

    } catch (IOException e) {
      throw new RuntimeException("Failed to save file: " + file.getOriginalFilename(), e);
    }

  }

  public void deleteFile(String fileName) {
     try {
      Path target = storageDir.resolve(fileName).normalize();
      if (!target.startsWith(storageDir.normalize())) {
        throw new IllegalArgumentException("Invalid file name");
      }
      Files.deleteIfExists(target);
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete file: " + fileName, e);
    }
  
  }

  public String updateFile(MultipartFile file, String oldFileName) {
    String newFileName = saveFile(file);
      if (oldFileName != null && !oldFileName.isBlank()) {
        deleteFile(oldFileName);
      }
      return newFileName;
  }

  private String extractExtension(String originalFilename) {
    Path fileNameOnly = Path.of(originalFilename).getFileName();
    String name = fileNameOnly == null ? "" : fileNameOnly.toString();
    int dot = name.lastIndexOf('.');
    if (dot <= 0 || dot == name.length() - 1) {
      throw new IllegalArgumentException("File must have a valid extension");
    }
    String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
    if (!ALLOWED_EXTENSIONS.contains(ext)) {
      throw new IllegalArgumentException("Invalid file extension: " + ext);
    }
    return ext;
  }
}
