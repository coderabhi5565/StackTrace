package com.stacktrace.comment_service;

import org.springframework.boot.SpringApplication;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class CommentServiceApplication {

	public static void main(String[] args) {
		// timestamps are stored as zone-less LocalDateTime: always interpret them as UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(CommentServiceApplication.class, args);
	}

}
