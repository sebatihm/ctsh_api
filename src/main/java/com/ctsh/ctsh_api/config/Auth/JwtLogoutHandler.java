package com.ctsh.ctsh_api.config.Auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.ctsh.ctsh_api.Dtos.ApiResponse;
import com.ctsh.ctsh_api.Services.CookieService;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtLogoutHandler implements LogoutHandler, LogoutSuccessHandler {

    private final CookieService cookieService;

    private final JsonMapper jsonMapper;

    public JwtLogoutHandler(
        CookieService cookieService,
        JsonMapper jsonMapper
    ) {
        this.cookieService = cookieService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        cookieService.deleteCookie(response);
    }

    @Override
    public void onLogoutSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {

    ApiResponse apiResponse = ApiResponse.of(
        HttpStatus.OK,
        "Logout successful",
        null
    );

    response.setStatus(HttpStatus.OK.value());
    response.setContentType("application/json");
    response.getWriter().write(
        jsonMapper.writeValueAsString(apiResponse)
    );
    }
}