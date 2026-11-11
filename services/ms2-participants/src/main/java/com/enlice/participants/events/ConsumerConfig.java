package com.enlice.participants.events;

import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Déclarations fonctionnelles Spring Cloud Stream.
 * Les noms de beans correspondent exactement à {@code spring.cloud.function.definition}.
 */
@Configuration
public class ConsumerConfig {

    @Bean
    public Consumer<EventEnvelope> onChallengePublished(ApplicationEventConsumer consumer) {
        return consumer::onChallengePublished;
    }

    @Bean
    public Consumer<EventEnvelope> onApplicationRegistered(ApplicationEventConsumer consumer) {
        return consumer::onApplicationRegistered;
    }

    @Bean
    public Consumer<EventEnvelope> onVerificationCompleted(ApplicationEventConsumer consumer) {
        return consumer::onVerificationCompleted;
    }

    @Bean
    public Consumer<EventEnvelope> onSubmissionRejected(ApplicationEventConsumer consumer) {
        return consumer::onSubmissionRejected;
    }

    @Bean
    public Consumer<EventEnvelope> onJudgingCompleted(ApplicationEventConsumer consumer) {
        return consumer::onJudgingCompleted;
    }

    @Bean
    public Consumer<EventEnvelope> onWinnersDeclared(ApplicationEventConsumer consumer) {
        return consumer::onWinnersDeclared;
    }

    @Bean
    public Function<EventEnvelope, EventEnvelope> applicationRegistered() {
        return envelope -> envelope;
    }
}
