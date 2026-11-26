package tn.arene.ms5.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import tn.arene.ms5.application.port.AiPort;

/** Seule porte d'entrée LLM : aucun worker n'injecte ce use case. */
@Service
public class SummarizeWithAI {

	public record Summary(UUID submissionId, String summary, String model, LocalDateTime generatedAt) {
	}

	private final AiPort ai;

	public SummarizeWithAI(AiPort ai) {
		this.ai = ai;
	}

	public Summary summarize(UUID submissionId) {
		AiPort.Summary result = ai.summarize(submissionId);
		return new Summary(submissionId, result.text(), result.model(), LocalDateTime.now());
	}
}
