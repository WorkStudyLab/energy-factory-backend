package com.energyfactory.energy_factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class EnergyFactoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnergyFactoryApplication.class, args);
	}

}
