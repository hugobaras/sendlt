package com.sendlt.infrastructure.config;

import com.sendlt.application.security.SendltUserDetailsService;
import com.sendlt.infrastructure.security.JwtAccessDeniedHandler;
import com.sendlt.infrastructure.security.JwtAuthenticationEntryPoint;
import com.sendlt.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final SendltUserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/ws/**", "/ws-native/**").permitAll();
                    auth.requestMatchers("/live-demo.html").permitAll();
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/gyms/**", "/api/sectors/**", "/api/boulders/**")
                            .authenticated();
                    auth.requestMatchers(
                                    HttpMethod.POST,
                                    "/api/gyms/**",
                                    "/api/sectors/**",
                                    "/api/boulders/**")
                            .hasRole("SETTER_ADMIN");
                    auth.requestMatchers(
                                    HttpMethod.PUT,
                                    "/api/gyms/**",
                                    "/api/sectors/**",
                                    "/api/boulders/**")
                            .hasRole("SETTER_ADMIN");
                    auth.requestMatchers(
                                    HttpMethod.PATCH,
                                    "/api/gyms/**",
                                    "/api/sectors/**",
                                    "/api/boulders/**")
                            .hasRole("SETTER_ADMIN");
                    auth.requestMatchers(
                                    HttpMethod.DELETE,
                                    "/api/gyms/**",
                                    "/api/sectors/**",
                                    "/api/boulders/**")
                            .hasRole("SETTER_ADMIN");
                    auth.requestMatchers("/api/**").authenticated();
                    auth.anyRequest().permitAll();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
