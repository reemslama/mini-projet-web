package tn.arene.ms5.application.port;

import java.util.UUID;

/** Port IA optionnel, injecté uniquement dans SummarizeWithAI (jamais dans un worker). */
public interface AiPort {

	record Summary(String text, String model) {
	}

	Summary summarize(UUID submissionId);
}
