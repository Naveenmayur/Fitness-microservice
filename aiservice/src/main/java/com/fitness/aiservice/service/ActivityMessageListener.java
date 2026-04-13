package com.fitness.aiservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityMessageListener {

    private final ActivityAIService activityAIService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void processActivity(Activity activity) throws JsonProcessingException {
        log.info("Received activity message: {}", activity.getId());
        log.info("Generated Recommendation: {}", activityAIService.generateRecommendation(activity));
    }
}
