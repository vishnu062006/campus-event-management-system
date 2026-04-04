package com.example.campus_events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CampusEventsApplication {

	public static void main(String[] args) {
		System.out.println("Working directory: " + System.getProperty("user.dir"));
		System.out.println("User home: " + System.getProperty("user.home"));
		SpringApplication.run(CampusEventsApplication.class, args);
	}
}