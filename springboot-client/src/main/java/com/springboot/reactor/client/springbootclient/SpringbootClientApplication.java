package com.springboot.reactor.client.springbootclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class SpringbootClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootClientApplication.class, args);
	}

}
