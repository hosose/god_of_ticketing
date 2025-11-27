package com.ticket.god_of_ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GodOfTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GodOfTicketingApplication.class, args);
	}

}
