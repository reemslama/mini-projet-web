package com.enlice.evaluation.events;

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
    public Consumer<EventEnvelope> onChallengePublished(EvaluationEventConsumer consumer) {
        return consumer::onChallengePublished;
    }

    @Bean
    public Consumer<EventEnvelope> onSubmissionSubmitted(EvaluationEventConsumer consumer) {
        return consumer::onSubmissionSubmitted;
    }

    @Bean
    public Consumer<EventEnvelope> onVerificationCompleted(EvaluationEventConsumer consumer) {
        return consumer::onVerificationCompleted;
    }

    @Bean
    public Consumer<EventEnvelope> onSimilarityFlagged(EvaluationEventConsumer consumer) {
        return consumer::onSimilarityFlagged;
    }

    @Bean
    public Consumer<EventEnvelope> onJudgingCompleted(EvaluationEventConsumer consumer) {
        return consumer::onJudgingCompleted;
    }

    @Bean
    public Function<EventEnvelope, EventEnvelope> judgingCompleted() {
        return envelope -> envelope;
    }

    @Bean
    public Function<EventEnvelope, EventEnvelope> winnersDeclared() {
        return envelope -> envelope;
    }
}
