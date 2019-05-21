package ru.wolfa.cam.driver;

import java.util.Calendar;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Client for Dericam H502W wifi camera.
 *
 * Problems:
 * 1. Clock synchronization with GMT+3 hardcoded.
 *
 */
public class CameraDericamH502W extends BaseCamera {
    public static final String CMD_PARAM = "dir";
    public static final String CMD_STOP = "stop";
    private final String cookie;
    private final String ip;
    private final String port;
    private final boolean includeInAll;

    public CameraDericamH502W(String ip, String port, String login, String password, boolean all) {
        cookie = "bRememberMe=0; userLastLogin=; passwordLastLogin=; bShowMenu=1; usrLevel=0; user=" + login
                + "; password=" + password + "; usr=" + login + "; pwd=" + password;

        this.port = port;
        this.ip = ip;
        this.includeInAll = all;
    }

    @Override
    public void executeShot(Consumer<ByteArrayResource> callback) {
        getClient().get().uri(b -> b.path("/snap.jpg").build()).retrieve().bodyToMono(ByteArrayResource.class)
                .doFinally(onFinally -> {
                    log.info("SNAPSHOT FINALLY: {}", onFinally);
                }).subscribe(callback);
    }

    @Override
    public void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis) {
        executeCmd(callback, direction, timeInMillis, CMD_PARAM, CMD_STOP, "/moveptz.xml");
    }

    @Override
    public WebClient getClient() {
        return WebClient.builder().baseUrl("http://" + ip + ":" + port).defaultHeader("Cookie", cookie).build();
    }

    @Override
    public void callPosition(Consumer<ByteArrayResource> callback, int position) {
        // NO Camera API
    }

    @Override
    public void syncTime(Consumer<ByteArrayResource> callback) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        // FIXME TimeZone
        map.add("tz", "180");
        map.add("enable", "0");
        map.add("syncpc", "1");
        map.add("svr", "time.nuri.net");
        map.add("dst", "0");
        Calendar i = Calendar.getInstance();
        String date = i.get(Calendar.DAY_OF_MONTH) + "-" + (i.get(Calendar.MONTH) + 1) + "-" + i.get(Calendar.YEAR);
        map.add("date", date);
        String time = i.get(Calendar.HOUR_OF_DAY) + ":" + i.get(Calendar.MINUTE) + ":" + i.get(Calendar.SECOND);
        map.add("time", time);
        Mono<ByteArrayResource> m = getClient().get()
                .uri(builder -> builder.queryParams(map).path("/setntp.xml").build()).retrieve()
                .bodyToMono(ByteArrayResource.class);
        m.subscribe(callback);
    }

    @Override
    public boolean includeInAllCommand() {
        return includeInAll;
    }

    private static final Logger log = LoggerFactory.getLogger(CameraDericamH502W.class);
}
