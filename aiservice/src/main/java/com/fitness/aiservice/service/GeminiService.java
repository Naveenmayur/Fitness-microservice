package com.fitness.aiservice.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeminiService {

    private final Client client;

    // This constructor will receive the key directly from properties
    public GeminiService(@Value("${gemini.api.key}") String geminiApiKey) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini API key is missing. Please set gemini.api.key in application.properties or application.yml");
        }

        this.client = Client.builder()
                .apiKey(geminiApiKey.trim())
                .build();
    }

    public String getAnswer(String question) {
        try {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3-flash-preview",
                    question,
                    null
            );

            String text = response.text();
            return text != null && !text.isBlank()
                    ? text
                    : "Sorry, I couldn't generate a response at this time.";

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);   // Add @Slf4j if you want logging here
            throw new RuntimeException("Failed to get answer from Gemini: " + e.getMessage(), e);
        }
    }
}