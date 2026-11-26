package com.notification_service.StackTrace;

import org.springframework.boot.SpringApplication;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StackTraceApplication {

	public static void main(String[] args) {
		// timestamps are stored as zone-less LocalDateTime: always interpret them as UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(StackTraceApplication.class, args);
	}

}
