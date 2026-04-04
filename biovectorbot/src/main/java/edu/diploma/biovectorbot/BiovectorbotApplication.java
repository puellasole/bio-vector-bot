package edu.diploma.biovectorbot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import edu.diploma.biovectorbot.client.BioVectorBotClient;

@SpringBootApplication
public class BiovectorbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiovectorbotApplication.class, args);
	}
	
}
