package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

//    Client client = new Client();
//
//    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
//        try {
//            this.client = Client.builder()
//                    .apiKey(apiKey)
//                    .build();
//        } catch (Exception e) {
//            e.printStackTrace(); // <-- you'll see actual cause
//            throw e;
//        }
//    }
//
//    public String getAnswer(String question){
//        GenerateContentResponse response =
//                client.models.generateContent("gemini-3-flash-preview", question, null);
//
//        return response.text();
//    }

    private final WebClient  webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;


    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String question) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", question)
                                )
                        )
                )
        );

        String response = webClient.post()
                .uri(geminiApiUrl)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;
    }
}
