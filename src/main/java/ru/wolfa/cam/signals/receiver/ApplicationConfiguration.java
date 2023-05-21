package ru.wolfa.cam.signals.receiver;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@ComponentScan("ru.wolfa")
@PropertySource(value = "classpath:/application.properties")
@EnableScheduling
@EnableAsync
public class ApplicationConfiguration {

    /**
     * Exposes request mapping
     * 
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> router(SignalizationHandler handler) {
        return RouterFunctions.route(GET("/cam/alarm.do"), a -> handler.signalisation(a));
    }

}
