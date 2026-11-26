package tn.arene.ms5.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SimilarityPolicyTest {

	private static final UUID A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Test
	void cosineIsBoundedAndDeterministic() {
		double[] v = { 1, 2, 3 };
		assertThat(SimilarityPolicy.cosine(v, v)).isEqualTo(1);
		assertThat(SimilarityPolicy.cosine(v, new double[] { 3, -1, 0 })).isBetween(0.0, 1.0);
		assertThat(SimilarityPolicy.cosine(v, new double[] { 0, 0, 0 })).isZero();
		assertThat(SimilarityPolicy.cosine(new double[] { 1 }, new double[] { 1, 2 })).isZero();
	}

	@Test
	void thresholdAndSeverityArePure() {
		assertThat(SimilarityPolicy.flagged(0.85, 0.85)).isTrue();
		assertThat(SimilarityPolicy.flagged(0.849, 0.85)).isFalse();
		assertThat(SimilarityPolicy.severity(0.96)).isEqualTo("HIGH");
		assertThat(SimilarityPolicy.severity(0.86)).isEqualTo("WARN");
	}

	@Test
	void topKIsSortedBySimilarityThenId() {
		Map<UUID, Double> scores = new LinkedHashMap<>();
		scores.put(A, 0.4);
		scores.put(B, 0.9);
		List<SimilarityMatch> matches = SimilarityPolicy.topK(scores, 10);
		assertThat(matches).hasSize(2);
		assertThat(matches.get(0).otherSubmissionId()).isEqualTo(B);
		assertThat(matches.get(0).similarity()).isEqualTo(0.9);
	}
}
