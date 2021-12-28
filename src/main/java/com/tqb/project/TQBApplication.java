package com.tqb.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class TQBApplication {

	public static void main(String[] args) {
		SpringApplication.run(TQBApplication.class, args);
	}

}
