package ru.wolfa.cam.driver;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class BaseCamera implements Camera {

    /*
     * (non-Javadoc)
     * 
     * @see ru.wolfa.cam.driver.Camera#getClient()
     */
    @Override
    abstract public WebClient getClient();

    /*
     * (non-Javadoc)
     * 
     * @see ru.wolfa.cam.driver.Camera#syncTime(java.util.function.Consumer)
     */
    @Override
    abstract public void syncTime(Consumer<ByteArrayResource> callback);

    /*
     * (non-Javadoc)
     * 
     * @see ru.wolfa.cam.driver.Camera#callPosition(java.util.function.Consumer,
     * int)
     */
    @Override
    abstract public void callPosition(Consumer<ByteArrayResource> callback, int position);

    /*
     * (non-Javadoc)
     * 
     * @see ru.wolfa.cam.driver.Camera#executeShot(java.util.function.Consumer)
     */
    @Override
    abstract public void executeShot(Consumer<ByteArrayResource> callback);

    /*
     * (non-Javadoc)
     * 
     * @see ru.wolfa.cam.driver.Camera#executeCmd(java.util.function.Consumer,
     * java.lang.String, int)
     */
    @Override
    abstract public void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis);

    /*
     * (non-Javadoc)
     * 
     * @see ru.wolfa.cam.driver.Camera#executeCmd(java.util.function.Consumer,
     * java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis, String CMD_PARAM,
            String CMD_STOP, String queryPath) {
        getClient().get().uri(builder -> builder.queryParam(CMD_PARAM, direction).path(queryPath).build()).retrieve()
                .bodyToMono(ByteArrayResource.class).subscribe(c -> {
                    log.trace("MOVE to {}", direction);
                    try {
                        Thread.sleep(1500L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    getClient().get().uri(builder -> builder.queryParam(CMD_PARAM, CMD_STOP).path(queryPath).build())
                            .retrieve().bodyToMono(ByteArrayResource.class).subscribe(d -> {
                                log.trace("STOP cam");
                                callback.accept(d);
                            });
                });
    }

    private static final Logger log = LoggerFactory.getLogger(BaseCamera.class);

}
