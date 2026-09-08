package com.sendlt.support;

import com.sendlt.domain.entity.User;
import com.sendlt.domain.enums.UserRole;
import com.sendlt.domain.repository.UserRepository;
import org.springframework.test.web.servlet.client.RestTestClient;

public final class TestAdminSupport {

    private TestAdminSupport() {}

    public static String registerSetterAdmin(
            RestTestClient client, UserRepository userRepository, String email, String displayName) {
        TestApiClient.register(client, email, displayName);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        user.setRole(UserRole.SETTER_ADMIN);
        userRepository.saveAndFlush(user);
        return TestApiClient.login(client, email);
    }
}
