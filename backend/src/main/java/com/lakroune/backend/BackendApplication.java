package com.lakroune.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		Dotenv.configure()
			.filename(".env")
			.ignoreIfMalformed()
			.ignoreIfMissing()
			.systemProperties()
			.load();
		SpringApplication.run(BackendApplication.class, args);
	}

}
