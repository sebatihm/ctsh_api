package com.ctsh.ctsh_api.Services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ctsh.ctsh_api.Exceptions.BadRequestException;

@Service 
public class FileService {

  private static final String PROFILES_DIR = "profiles";
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
  private static final Set<String> ALLOWED_EXTENSIONS =
      Set.of("jpg", "jpeg", "png", "webp", "gif");

  private final Path storageDir;
  private final String publicBaseUrl;
  private final String contextPath;
  private final String uploadsUrlPath;

  public FileService(
      @Value("${app.uploads.dir}") String uploadsDir,
      @Value("${app.uploads.url-path}") String uploadsUrlPath,
      @Value("${app.url.backend}") String publicBaseUrl,
      @Value("${server.servlet.context-path:}") String contextPath) {
    this.storageDir = Path.of(uploadsDir, PROFILES_DIR);
    this.uploadsUrlPath = uploadsUrlPath;
    this.publicBaseUrl = publicBaseUrl;
    this.contextPath = contextPath;
  }

  public String getPublicUrl(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return null;
    }
    return publicBaseUrl + contextPath + uploadsUrlPath + "/" + PROFILES_DIR + "/" + fileName;
  }

  public void validateFile(MultipartFile fileData) {

    if (fileData.getSize() > MAX_FILE_SIZE) {
        throw new BadRequestException("File size exceeds the 5 MB limit");
    }

    String contentType = fileData.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        throw new BadRequestException("File type must be an image");
    }
  }

  public String saveFile(MultipartFile file) {
    try {
      
      Files.createDirectories(storageDir);
      String ext = extractExtension(file.getOriginalFilename());
      String filename = UUID.randomUUID() + "." + ext;      
      Path target = storageDir.resolve(filename).normalize();
      if (!target.startsWith(storageDir.normalize())) {
        throw new BadRequestException("Invalid file name");
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
        throw new BadRequestException("Invalid file name");
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
    if (originalFilename == null || originalFilename.isBlank()) {
      throw new BadRequestException(
          "File must have a valid extension"
      );
    }
  
    Path fileNameOnly = Path.of(originalFilename).getFileName();
    String name = fileNameOnly == null ? "" : fileNameOnly.toString();
    int dot = name.lastIndexOf('.');
    if (dot <= 0 || dot == name.length() - 1) {
      throw new BadRequestException("File must have a valid extension");
    }
    String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
    if (!ALLOWED_EXTENSIONS.contains(ext)) {
      throw new BadRequestException("Invalid file extension: " + ext);
    }
    return ext;
  }
}
