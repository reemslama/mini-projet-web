package tn.arene.soumission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SoumissionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoumissionApplication.class, args);
	}

}
