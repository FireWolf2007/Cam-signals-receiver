package ru.wolfa.cam.driver;

import java.util.function.Consumer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.reactive.function.client.WebClient;

public interface Camera {

    WebClient getClient();

    void syncTime(Consumer<ByteArrayResource> callback);

    void callPosition(Consumer<ByteArrayResource> callback, int position);

    void executeShot(Consumer<ByteArrayResource> callback);

    void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis);

    void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis, String CMD_PARAM,
            String CMD_STOP, String queryPath);

    boolean includeInAllCommand();
}