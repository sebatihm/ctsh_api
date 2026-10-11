package com.ctsh.ctsh_api.Mappers;

import org.springframework.stereotype.Component;

import com.ctsh.ctsh_api.Dtos.UserResponseDto;
import com.ctsh.ctsh_api.Models.User;
import com.ctsh.ctsh_api.Services.FileService;

@Component
public class UserMapper {

    private final FileService fileService;

    public UserMapper(FileService fileService) {
        this.fileService = fileService;
    }

    public UserResponseDto toResponseDto(User user) {

        UserResponseDto dto = new UserResponseDto();

        dto.setUuid(user.getUuid());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setProfilePicture(
            fileService.getPublicUrl(user.getProfilePicture())
        );
        dto.setRole(user.getRole());

        return dto;
    }
}