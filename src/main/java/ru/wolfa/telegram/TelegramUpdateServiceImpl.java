package ru.wolfa.telegram;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.wolfa.cam.driver.CameraDriver;
import ru.wolfa.cam.signals.receiver.Answ;
import ru.wolfa.cam.signals.receiver.Message;
import ru.wolfa.cam.signals.receiver.Result;
import ru.wolfa.cam.signals.receiver.SettingsService;
import ru.wolfa.parser.CommandParser;
import static ru.wolfa.cam.signals.receiver.ApplicationConstants.*;

@Component
public class TelegramUpdateServiceImpl implements InitializingBean, DisposableBean {

    private final WebClient client;
    private final TelegramSenderServiceImpl sender;
    private final CommandParser parser;
    private final ObjectMapper om = new ObjectMapper();

    private volatile boolean quit = false;

    public TelegramUpdateServiceImpl(Environment env, CameraDriver cameraDriver, TelegramSenderServiceImpl sender,
            SettingsService settings) {
        this.sender = sender;
        this.parser = new CommandParser(cameraDriver, sender, settings);

        String botId = env.getProperty(CONFIG_TELEGRAM_BOT_ID);
        String botToken = env.getProperty(CONFIG_TELEGRAM_BOT_TOKEN);
        client = WebClient.builder().baseUrl(TELEGRAM_BOT_URL + botId + ":" + botToken).build();
    }

    @Override
    public void afterPropertiesSet() {
        try {
            new Thread(this::launch).start();
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void destroy() throws Exception {
        quit = true;
    }

    public void launch() {
        while (!quit) {
            try {
                update();
            } catch (Exception e) {
                // ignore
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error("launch break", e);
                Thread.currentThread().interrupt();
                quit = true;
                break;
            }
            log.error("launch state: quit={}", quit);
        }
    }

    /**
     * Last updateId from telegram service
     */
    private Integer updateId = null;

    public void update() {
        log.debug("Requesting UPDATE from telegram");
        ByteArrayResource res = client.get().uri(b -> {
            b.path("/getUpdates").queryParam(TELEGRAM_UPDATE_REQUEST_TIMEOUT_PARAM, "49");
            if (updateId != null) {
                b.queryParam(TELEGRAM_UPDATE_REQUEST_OFFSET_PARAM, Integer.toString(updateId + 1));
            }
            return b.build();
        }).retrieve().bodyToMono(ByteArrayResource.class).doFinally(e -> {
            log.error("relaunch getUpdate, previous result: {}", e);
        }).block();
        if (res != null) {
            log.trace("getUpdate done");
            try {
                byte[] updateResponse = res.getByteArray();
                Answ result = om.readValue(updateResponse, Answ.class);
                if (Boolean.TRUE.equals(result.getOk())) {
                    for (Result r : result.getResult()) {
                        log.trace("OK updateId={}", r.getUpdateId());
                        if (updateId != null && r.getUpdateId() <= updateId) {
                            log.trace("SKIP updateId={} our={}", r.getUpdateId(), updateId);
                            continue;
                        }
                        updateId = r.getUpdateId();
                        Message msg = r.getMessage();
                        if (msg == null) {
                            continue;
                        }
                        long id = msg.getMessageId();
                        int chatId = msg.getChat().getId();
                        log.trace("OK id={} chat={} text={}", id, chatId, msg.getText());
                        Path f = Paths.get("processed/id." + id);
                        if (!Files.exists(f, LinkOption.NOFOLLOW_LINKS)) {
                            log.error("NEW {}", id);
                            if (sender.isAllowedChat(chatId)) {
                                log.trace("Allowed chat id {}", chatId);
                                parser.process(msg.getText(), chatId);
                            } else {
                                log.trace("Not allowed chat id {} with text {}", chatId, msg.getText());
                                sender.sendMessage(chatId, "Not allowed");
                            }
                            try (FileOutputStream fos = new FileOutputStream(f.toFile())) {
                                fos.write("Done".getBytes());
                            }
                        } else {
                            log.trace("PROCESSED {}", id);
                        }

                    }
                } else {
                    log.error("Reqesting update failed");
                }
            } catch (IOException e) {
                log.error("IOException", e);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(TelegramUpdateServiceImpl.class);
}
