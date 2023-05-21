package ru.wolfa.cam.signals.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        //ReactorDebugAgent.init();
        SpringApplication.run(Application.class, args);
    }

}
