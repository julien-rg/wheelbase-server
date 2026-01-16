package com.jureg.wheelbase_server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class WheelbaseServerApplication {

	@Value("${spring.profiles.active:}")
	private String activeProfile;

	public static void main(String[] args) {
		SpringApplication.run(WheelbaseServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("WheelbaseServerApplication is fully ready and listening (using profile: " + activeProfile + ")!");
    }
}
