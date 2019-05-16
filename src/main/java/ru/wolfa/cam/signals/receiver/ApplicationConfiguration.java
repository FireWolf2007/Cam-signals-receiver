package ru.wolfa.cam.signals.receiver;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@ComponentScan("ru.wolfa")
@PropertySource(value = "classpath:/application.properties")
@EnableScheduling
public class ApplicationConfiguration {

	@Autowired
	Environment env;

	/**
	 * Exposes request mapping
	 * 
	 * @return
	 */
	@Bean
	public RouterFunction<ServerResponse> router(SignalisationHandler handler) {
		return RouterFunctions.route(GET("/cam/alarm.do"), a -> handler.signalisation(a));
	}

}
