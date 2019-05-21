package ru.wolfa.cam.driver;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.Calendar;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Client for Apexis J011WS wifi camera.
 *
 * Problems:
 * 1. Clock synchronization with GMT+3 hardcoded.
 *
 */
public class CameraApexisJ011WS extends BaseCamera {
    public static final String CMD_PARAM = "command";
    public static final String CMD_STOP = "1";
    private final String ip;
    private final String port;
    private final ExchangeFilterFunction authFilter;
    private final boolean includeInAll;

    public CameraApexisJ011WS(String ip, String port, String login, String password, boolean all) {
        this.port = port;
        this.ip = ip;
        authFilter = basicAuthentication(login, password);
        this.includeInAll = all;
    }

    @Override
    public void executeShot(Consumer<ByteArrayResource> callback) {
        getClient().get().uri(b -> b.path("/snapshot.cgi").build()).retrieve().bodyToMono(ByteArrayResource.class)
                .doFinally(onFinally -> {
                    log.info("SNAPSHOT FINALLY: {}", onFinally);
                }).subscribe(callback);
    }

    @Override
    public void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis) {
        final String command;

        switch (direction) {
        case "left":
            command = "4";
            break;
        case "right":
            command = "6";
            break;
        case "up":
            command = "0";
            break;
        case "down":
            command = "2";
            break;
        default:
            // STOP
            command = CMD_STOP;
        }
        executeCmd(callback, command, timeInMillis, CMD_PARAM, CMD_STOP, "/decoder_control.cgi");
    }

    @Override
    public void callPosition(Consumer<ByteArrayResource> callback, int position) {
        if (position < 1 || position > 15) {
            return;
        }
        // 29 + X*2
        String value = "" + (29 + position * 2);
        getClient().get().uri(builder -> builder.queryParam(CMD_PARAM, value).path("/decoder_control.cgi").build())
                .retrieve().bodyToMono(ByteArrayResource.class).subscribe(c -> {
                    log.trace("CALL PERSET POSTITION to {}", position);
                    try {
                        Thread.sleep(1500L);
                    } catch (InterruptedException e) {
                        log.error("Exception", e);
                    }
                    callback.accept(c);
                });
    }

    @Override
    public WebClient getClient() {
        return WebClient.builder().baseUrl("http://" + ip + ":" + port).filters(f -> f.add(authFilter)).build();
    }

    @Override
    public void syncTime(Consumer<ByteArrayResource> callback) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("next_url", "datetime.htm");
        map.add("ntp_svr", "82.199.107.209");
        // FIXME TimeZone
        map.add("tz", "-10800");
        map.add("ntp_enable", "0");
        Calendar i = Calendar.getInstance();
        long now = i.getTime().getTime() / 1000;
        log.trace("NOW: {}", now);
        map.add("now", "" + now + ".000");
        Mono<ByteArrayResource> m = getClient().get()
                .uri(builder -> builder.queryParams(map).path("/set_datetime.cgi").build()).retrieve()
                .bodyToMono(ByteArrayResource.class);

        m.subscribe(callback);
    }

    @Override
    public boolean includeInAllCommand() {
        return includeInAll;
    }

    private static final Logger log = LoggerFactory.getLogger(CameraApexisJ011WS.class);
}
