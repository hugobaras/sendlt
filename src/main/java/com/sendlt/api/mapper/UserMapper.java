package com.sendlt.api.mapper;

import com.sendlt.api.dto.auth.UserResponse;
import com.sendlt.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
    }
}
