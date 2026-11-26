package tn.arene.ms5.application.port;

/** Embedder local déterministe, versionné, sans appel réseau externe. */
public interface EmbeddingPort {

	double[] embed(String source);

	String modelVersion();
}
