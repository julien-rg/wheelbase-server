package com.jureg.wheelbase_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class WheelbaseServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WheelbaseServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("WheelbaseServerApplication is fully ready and listening!");
    }
}
