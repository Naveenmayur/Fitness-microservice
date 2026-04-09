package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userValidationWebClient;

    public boolean validateUser(String userId) {
        log.info("Validating user with id: {}", userId);
        try {
            return Boolean.TRUE.equals(userValidationWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                     .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("User not found with id: " + userId);
            } else if (e.getStatusCode() == HttpStatus.BAD_GATEWAY) {
                throw new RuntimeException("User service is currently unavailable. Please try again later.");
            } else {
                throw new RuntimeException("An error occurred while validating the user: " + e.getMessage());
            }
        }
    }


}

