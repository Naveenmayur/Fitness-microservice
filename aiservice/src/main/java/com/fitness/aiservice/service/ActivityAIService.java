package com.fitness.aiservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiWebClientService geminiWebClientService;

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;   // Spring Boot auto-configures this

    public Recommendation generateRecommendation(Activity activity) throws JsonProcessingException {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);

        Recommendation recommendation = mapAiResponseToRecommendation(aiResponse, activity);

        log.info("Generated AI recommendation for activity {}: {}", activity.getId(), recommendation.getSummary());
        return recommendation;
    }

    private Recommendation mapAiResponseToRecommendation(String aiResponse,
                                                         Activity activity) throws JsonProcessingException {

        // Clean the response in case Gemini adds ```json ... ```
        String cleanJson = cleanJsonResponse(aiResponse);

        Recommendation recommendation = objectMapper.readValue(cleanJson, Recommendation.class);

        // Set additional fields not coming from AI
        recommendation.setUserId(activity.getUserId());
        recommendation.setActivityId(activity.getId());
        recommendation.setActivityType(activity.getType());

        return recommendation;
    }

    /**
     * Removes markdown code blocks if Gemini wraps the JSON
     */
    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";

        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.replace("```", "").trim();
        }

        return cleaned;
    }

    private String createPromptForActivity(Activity activity) {
        return String.format(
                "Analyze the following fitness activity and generate a detailed, user-friendly summary.\n\n" +
                        "Activity Details:\n" +
                        "- Activity Type: %s\n" +
                        "- Duration: %d minutes\n" +
                        "- Calories Burned: %d kcal\n" +
                        "- Start Time: %s\n" +
                        "- Additional Metrics: %s\n\n" +

                        "Instructions:\n" +
                        "1. Provide a concise summary of the activity.\n" +
                        "2. Analyze performance based on available metrics (e.g., distance, speed, heart rate).\n" +
                        "3. Highlight intensity level (low, moderate, high).\n" +
                        "4. Give 2-3 personalized insights.\n" +
                        "5. Provide improvements to enhance performance.\n" +
                        "6. Provide actionable workout suggestions.\n" +
                        "7. Provide safety tips based on the activity.\n" +
                        "8. Keep tone motivational and easy to understand.\n\n" +

                        "IMPORTANT:\n" +
                        "- Respond ONLY in valid JSON.\n" +
                        "- Do NOT include any explanation or extra text outside JSON.\n" +
                        "- Ensure all fields are always present.\n" +
                        "- If data is missing, return empty arrays [] instead of null.\n\n" +

                        "Output JSON format:\n" +
                        "{\n" +
                        "  \"summary\": \"string\",\n" +
                        "  \"performanceAnalysis\": \"string\",\n" +
                        "  \"recommendation\": \"string\",\n" +
                        "  \"intensity\": \"low | moderate | high\",\n" +
                        "  \"insights\": [\"string\", \"string\", \"string\"],\n" +
                        "  \"improvements\": [\"string\", \"string\", \"string\"],\n" +
                        "  \"suggestions\": [\"string\", \"string\", \"string\"],\n" +
                        "  \"safety\": [\"string\", \"string\", \"string\"]\n" +
                        "}\n",

                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getStartTime(),
                activity.getAdditionalMetrics()
        );
    }


}
