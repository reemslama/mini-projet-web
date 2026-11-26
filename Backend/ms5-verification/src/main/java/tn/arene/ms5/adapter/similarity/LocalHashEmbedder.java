package tn.arene.ms5.adapter.similarity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import tn.arene.ms5.application.port.EmbeddingPort;

/**
 * Embedder local déterministe (S5). L'embedder ONNX versionné le remplace en S8 derrière
 * le même port ; le vecteur reste reproductible et aucune donnée ne sort du service.
 */
@Component
@ConditionalOnProperty(name = "ms5.similarity.embedder", havingValue = "local-hash", matchIfMissing = true)
public class LocalHashEmbedder implements EmbeddingPort {

	private final int dimension;
	private final String version;

	public LocalHashEmbedder(
			@Value("${ms5.similarity.dim:384}") int dimension,
			@Value("${ms5.similarity.model-version:local-code-minilm-v1.0.0-384d}") String version) {
		this.dimension = dimension;
		this.version = version;
	}

	@Override
	public String modelVersion() {
		return version;
	}

	@Override
	public double[] embed(String source) {
		double[] vector = new double[dimension];
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			for (int i = 0; i < dimension; i++) {
				byte[] hash = digest.digest((source + "\u001f" + i).getBytes(StandardCharsets.UTF_8));
				int bits = ((hash[0] & 0xff) << 24) | ((hash[1] & 0xff) << 16)
						| ((hash[2] & 0xff) << 8) | (hash[3] & 0xff);
				vector[i] = ((bits & 0xffffL) / 32767.5) - 1;
			}
			return vector;
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 indisponible", exception);
		}
	}
}
