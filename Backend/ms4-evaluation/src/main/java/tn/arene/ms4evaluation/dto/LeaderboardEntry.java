
package tn.arene.ms4evaluation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeaderboardEntry {

    int rank;

    UUID submissionId;

    UUID applicantId;

    double averageScore;

    int evaluationsCount;
}
