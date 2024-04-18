package com.smvcsh.system.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.smvcsh.proxy.**", "com.smvcsh.system.config", "com.thunisoft.tools.tunnel.jenkins.agent"})
@EnableScheduling
public class MainServer {
    public static void main(String[] args) {
        SpringApplication.run(MainServer.class, args);
    }
}
