package com.sendlt.api.controller;

import com.sendlt.api.dto.auth.UserResponse;
import com.sendlt.api.mapper.UserMapper;
import com.sendlt.application.exception.ResourceNotFoundException;
import com.sendlt.application.security.SecurityUtils;
import com.sendlt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse me() {
        return userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", SecurityUtils.getCurrentUserId()));
    }
}
