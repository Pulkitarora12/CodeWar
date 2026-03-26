package com.project.CodeWar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableScheduling
public class CodeWarApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeWarApplication.class, args);
	}

    public String test() {
        return "Hello World";
    }
}
