package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.ChannelPipelineConfigurer;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final WebClient userValidationWebClient;

    public Mono<Boolean> validateUser(String userId) {
        log.info("Validating user with id: {}", userId);
        return userValidationWebClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.warn("User not found with id: {}", userId);
                        return Mono.just(false);
                    } else if (e.getStatusCode() == HttpStatus.BAD_GATEWAY) {
                        log.error("User service is currently unavailable. Please try again later.");
                        return Mono.error(new RuntimeException("User service is currently unavailable. Please try again later."));
                    } else {
                        log.error("An error occurred while validating the user: {}", e.getMessage());
                        return Mono.error(new RuntimeException("An error occurred while validating the user: " + e.getMessage()));
                    }
                });
    }

    public Mono<UserResponse> registerUser(RegisterRequest registerRequest) {
        return userValidationWebClient.post()
                .uri("/api/users/register")
                .bodyValue(registerRequest)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Invalid registration request: " + e.getResponseBodyAsString()));
                    } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        return Mono.error(new RuntimeException("User service is currently unavailable. Please try again later."));
                    } else {
                        return Mono.error(new RuntimeException("An error occurred while registering the user: " + e.getMessage()));
                    }
                });
    }
}

