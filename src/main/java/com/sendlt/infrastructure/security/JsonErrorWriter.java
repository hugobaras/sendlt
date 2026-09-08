package com.sendlt.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendlt.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public final class JsonErrorWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private JsonErrorWriter() {}

    public static void write(HttpServletResponse response, int status, ErrorResponse error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getWriter(), error);
    }

    public static void write(HttpServletResponse response, HttpStatus status, String message, String path)
            throws IOException {
        write(response, status.value(), ErrorResponse.of(status, message, path));
    }
}
