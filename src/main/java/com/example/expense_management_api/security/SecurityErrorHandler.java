package com.example.expense_management_api.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.expense_management_api.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        writeError(response, 401, "Authentication required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        writeError(response, 403, "Access denied");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ErrorResponse(status, message, null, LocalDateTime.now()));
    }
}
