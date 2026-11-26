package tn.arene.ms5.adapter.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiSummaryResponse(UUID submissionId, String summary, String model, LocalDateTime generatedAt) {
}
