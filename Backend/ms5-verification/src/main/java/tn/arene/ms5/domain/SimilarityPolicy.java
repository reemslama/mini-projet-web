package tn.arene.ms5.domain;

import java.util.Comparator;
import java.util.List;

/** Politique pure de similarité : cosinus, seuil et gravité, sans IA ni I/O. */
public final class SimilarityPolicy {

	private SimilarityPolicy() {
	}

	public static boolean flagged(double maxSimilarity, double threshold) {
		return maxSimilarity >= threshold;
	}

	public static String severity(double maxSimilarity) {
		return maxSimilarity >= 0.95 ? "HIGH" : "WARN";
	}

	/** Similarité cosinus dans [0,1] ; vecteurs non normalisés acceptés. */
	public static double cosine(double[] left, double[] right) {
		if (left.length != right.length || left.length == 0) {
			return 0;
		}
		double dot = 0, normLeft = 0, normRight = 0;
		for (int i = 0; i < left.length; i++) {
			dot += left[i] * right[i];
			normLeft += left[i] * left[i];
			normRight += right[i] * right[i];
		}
		if (normLeft == 0 || normRight == 0) {
			return 0;
		}
		double value = dot / Math.sqrt(normLeft * normRight);
		return Math.max(0, Math.min(1, value));
	}

	public static double maxOf(List<Double> values) {
		return values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
	}

	public static List<SimilarityMatch> topK(java.util.Map<java.util.UUID, Double> scores, int topK) {
		return scores.entrySet().stream()
				.sorted(Comparator.<java.util.Map.Entry<java.util.UUID, Double>>comparingDouble(
						java.util.Map.Entry::getValue).reversed().thenComparing(entry -> entry.getKey().toString()))
				.limit(topK)
				.map(entry -> new SimilarityMatch(entry.getKey(), entry.getValue()))
				.toList();
	}
}
