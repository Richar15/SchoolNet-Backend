package com.RichardDev.SchoolNet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SchoolNetApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchoolNetApplication.class, args);
	}

}
