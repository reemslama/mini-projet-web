package tn.arene.ms5.adapter.ai;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import tn.arene.ms5.application.AiUnavailableException;
import tn.arene.ms5.application.port.AiPort;

/** Mode par défaut : aucun LLM dans le chemin critique, réponse 503 rapide pour l'endpoint IA. */
@Component
@ConditionalOnProperty(name = "ms5.ai.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAiPort implements AiPort {

	@Override
	public Summary summarize(UUID submissionId) {
		throw new AiUnavailableException("Service IA indisponible (MS5_AI_ENABLED=false)");
	}
}
