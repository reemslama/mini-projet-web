package tn.arene.ms1challenges.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeCreateRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 140, message = "Le titre ne doit pas dépasser 140 caractères")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "L'enterpriseId est obligatoire")
    private UUID enterpriseId;

    @NotNull(message = "La date limite (deadline) est obligatoire")
    private LocalDateTime deadline;

    private List<String> tags;
}
