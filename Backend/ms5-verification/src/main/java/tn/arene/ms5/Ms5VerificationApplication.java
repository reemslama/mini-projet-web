package tn.arene.ms5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** MS5 · Vérification — port 8085, 6 endpoints gelés, verdict déterministe. */
@SpringBootApplication
@EnableScheduling
public class Ms5VerificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ms5VerificationApplication.class, args);
	}
}
