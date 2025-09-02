package com.rivals.rivals_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.rivals")
public class RivalsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RivalsApiApplication.class, args);
	}

}
