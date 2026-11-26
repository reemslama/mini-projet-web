package tn.arene.soumission.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Résultat interne d'un test exécuté par MS5 (objet de valeur). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {

	private String testName;
	private String outcome;
	private long durationMs;
	private String logs;

	public boolean isPassed() {
		return "PASS".equalsIgnoreCase(outcome) || "PASSED".equalsIgnoreCase(outcome);
	}
}
