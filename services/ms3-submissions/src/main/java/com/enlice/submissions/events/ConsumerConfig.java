package com.enlice.submissions.events;

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
    public Consumer<EventEnvelope> onSubmissionSubmitted(SubmissionEventConsumer consumer) {
        return consumer::onSubmissionSubmitted;
    }

    @Bean
    public Consumer<EventEnvelope> onVerificationCompleted(SubmissionEventConsumer consumer) {
        return consumer::onVerificationCompleted;
    }

    @Bean
    public Consumer<EventEnvelope> onSubmissionRejected(SubmissionEventConsumer consumer) {
        return consumer::onSubmissionRejected;
    }

    @Bean
    public Consumer<EventEnvelope> onSimilarityFlagged(SubmissionEventConsumer consumer) {
        return consumer::onSimilarityFlagged;
    }

    @Bean
    public Consumer<EventEnvelope> onChallengePublished(SubmissionEventConsumer consumer) {
        return consumer::onChallengePublished;
    }

    @Bean
    public Function<EventEnvelope, EventEnvelope> submissionSubmitted() {
        return envelope -> envelope;
    }
}
