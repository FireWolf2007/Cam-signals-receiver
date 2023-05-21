package ru.wolfa.cam.signals.receiver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import ru.wolfa.cam.driver.CameraDriver;
import static ru.wolfa.cam.signals.receiver.ApplicationConstants.*;

@Service
public class SignalizationHandler {
    private final CameraDriver cameraDriver;
    private final SettingsService settings;
    private final ThreadingServiceImpl executor = new ThreadingServiceImpl();

    private final int alertChatId;

    /**
     * 
     * @return
     * @throws IOException
     */
    public Mono<ServerResponse> signalisation(ServerRequest request) {
        log.trace("Incoming ALERT");
        // On parse error first cam will be used.
        int camId = 1;
        try {
            String camIdParam = request.queryParam(APP_REQUEST_CAM_ID_PARAM).orElse("1");
            camId = Integer.parseInt(camIdParam);
        } catch (NumberFormatException e) {
            // ignore
        }
        if (settings.isNotificationsEnabled(camId)) {
            final int camIdExec = camId;
            executor.exec(() -> cameraDriver.executeShotAlert(alertChatId, camIdExec));
        } else {
            log.trace("Ignore ALERT for cam{}", camId);
        }
        return Mono.empty();
    }

    public SignalizationHandler(Environment env, CameraDriver cameraDriver, SettingsService settings) {
        if (!"true".equals(System.getProperty("java.net.preferIPv6Stack"))) {
            log.error("Switch on IPv6 prefer: -Djava.net.preferIPv6Stack=true. Current state: {}",
                    System.getProperty("java.net.preferIPv6Stack"));
        }

        alertChatId = Integer.parseInt(env.getProperty(CONFIG_TELEGRAM_ALERT_CHAT_ID));

        this.cameraDriver = cameraDriver;
        this.settings = settings;
    }

    private static final Logger log = LoggerFactory.getLogger(SignalizationHandler.class);
}
