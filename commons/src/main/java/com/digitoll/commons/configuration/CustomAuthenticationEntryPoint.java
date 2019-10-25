package com.digitoll.commons.configuration;

import com.digitoll.commons.dto.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

@Component
public final class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();
 
    @Override
    public void commence(
        HttpServletRequest request, 
        HttpServletResponse response, 
        AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponseDTO responseDTO = new ErrorResponseDTO(
            HttpStatus.valueOf(HttpServletResponse.SC_UNAUTHORIZED),
            "Unauthorized",
            Collections.emptyList()
        );

        PrintWriter out = response.getWriter();
        out.append(objectMapper.writeValueAsString(responseDTO));
        out.close();
    }
}
