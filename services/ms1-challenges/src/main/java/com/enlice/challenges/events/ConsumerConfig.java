package com.enlice.challenges.events;

import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Déclarations fonctionnelles Spring Cloud Stream.
 * Les noms de beans correspondent exactement à {@code spring.cloud.function.definition}.
 */
@Configuration
public class ConsumerConfig {

    @Bean
    public Consumer<EventEnvelope> onApplicationRegistered(ChallengeEventConsumer consumer) {
        return consumer::onApplicationRegistered;
    }

    @Bean
    public Consumer<EventEnvelope> onJudgingCompleted(ChallengeEventConsumer consumer) {
        return consumer::onJudgingCompleted;
    }

    @Bean
    public Consumer<EventEnvelope> onWinnersDeclared(ChallengeEventConsumer consumer) {
        return consumer::onWinnersDeclared;
    }
}
