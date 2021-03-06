package ru.wolfa.cam.driver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ru.wolfa.telegram.TelegramSenderServiceImpl;

@Service
public class CameraDriver {
    private final Camera[] cams;
    private final TelegramSenderServiceImpl sender;

    public int getCount() {
        return cams.length;
    }

    public Camera getCamera(int camId) {
        if (camId < 1 || camId > cams.length) {
            return null;
        }
        return cams[camId - 1];
    }

    public boolean isCameraIncludedInAll(int camId) {
        if (camId < 1 || camId > cams.length) {
            return false;
        }
        return cams[camId - 1].includeInAllCommand();
    }

    public void callPosition(int chatId, int camId, int position) {
        final Camera camera = getCamera(camId);
        if (camera == null) {
            return;
        }
        camera.callPosition(c -> {
            try {
                Thread.sleep(1500L);
            } catch (InterruptedException e) {
                log.error("Exception", e);
            }
            executeShot(chatId, camId);
        }, position);
    }

    public void executeCmd(int chatId, int camId, String direction) {
        final Camera camera = getCamera(camId);
        if (camera == null) {
            return;
        }
        camera.executeCmd(e -> {
            executeShot(chatId, camId);
        }, direction, 1500);
    }

    public void executeShot(int chatId, int camId) {
        final Camera camera = getCamera(camId);
        if (camera == null) {
            return;
        }
        camera.executeShot(c -> {
            log.trace("Data from camera: {}", c);
            sender.sendPhoto(chatId, c, "Cam" + camId);
        });
    }

    public void executeShotAlert(int chatId, int camId) {
        final Camera camera = getCamera(camId);
        if (camera == null) {
            return;
        }
        camera.executeShot(c -> {
            log.trace("Data from camera: {}", c);
            sender.sendPhoto(chatId, c, "ALERT Cam" + camId);
        });
    }

    @Scheduled(initialDelay = 4000L, fixedDelay = 21600000L) // once in 6 hours
    public void syncCamsTime() {
        for (int i = 0; i < cams.length; i++) {
            cams[i].syncTime(c -> {
                log.debug("Sync time done");
            });
        }
    }

    public CameraDriver(Environment env, TelegramSenderServiceImpl sender) {
        int camCount = Integer.parseInt(env.getProperty("cam.count"));
        cams = new Camera[camCount];
        for (int i = 0; i < camCount; i++) {
            String camLogin = env.getProperty("cam" + i + ".login");
            String camPassword = env.getProperty("cam" + i + ".password");
            String ip = env.getProperty("cam" + i + ".ip");
            String port = env.getProperty("cam" + i + ".port");
            String camClass = env.getProperty("cam" + i + ".class");
            // get snapshot on /all command
            boolean camAll = Boolean.parseBoolean(env.getProperty("cam" + i + ".all"));
            boolean initDone = false;
            try {
                Class<?> camClazz = Class.forName("ru.wolfa.cam.driver." + camClass);
                @SuppressWarnings("unchecked")
                Constructor<Camera> constructor = (Constructor<Camera>) camClazz.getConstructor(String.class,
                        String.class, String.class, String.class, boolean.class);
                if (constructor != null) {
                    cams[i] = constructor.newInstance(ip, port, camLogin, camPassword, camAll);
                    initDone = true;
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("Exception", e);
            }
            if (!initDone) {
                //throw new IllegalArgumentException("Camera class illegal: " + camClass);
            }
        }
        this.sender = sender;
    }

    private static final Logger log = LoggerFactory.getLogger(CameraDriver.class);

}
