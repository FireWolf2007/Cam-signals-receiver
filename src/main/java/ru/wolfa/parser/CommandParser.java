package ru.wolfa.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.wolfa.cam.driver.CameraDriver;
import ru.wolfa.cam.signals.receiver.SettingsService;
import ru.wolfa.telegram.TelegramSenderServiceImpl;

public class CommandParser {
    private final CameraDriver cameraDriver;
    private final TelegramSenderServiceImpl sender;
    private final String botName;
    private final SettingsService settings;

    public void process(String msg, int chatId) {
        String cmd = msg;
        if (cmd != null) {
            cmd = cmd.trim().toLowerCase();
            if (cmd.charAt(0) != '/') {
                return;
            }
        } else {
            return;
        }
        int botNameIndex = cmd.indexOf(botName);
        if (botNameIndex > 0) {
            cmd = cmd.substring(0, botNameIndex);
        }
        if ("/help".equals(cmd) || "/start".equals(cmd)) {
            log.trace("send help");
            sender.sendMessage(chatId, "Список команд:\n"
                    + "/s1 - камера 1\n"
                    + "/s2 - камера 2\n"
                    + "/s3 - камера 3\n"
                    + "/l1 - камеру 1 налево\n"
                    + "/r1 - камеру 1 направо\n"
                    + "/d1 - камеру 1 вниз\n"
                    + "/u1 - камеру 1 вверх\n"
                    + "/p1 1 - камеру 1 в предустановленную позицию 1\n"
                    + "/disable 1 - отключить уведомления на камере 1\n"
                    + "/enable 1 - включить уведомления на камере 1\n"
                    + "/reset - вернуть все уведомления в исходное состояние\n");
        } else if ("/all".equals(cmd)) {
            for (int i = 1;i < cameraDriver.getCount() + 1;i++) {
                log.trace("Make shot from cam{}", i);
                cameraDriver.executeShot(chatId, i);
            }
        } else {
            boolean processed = true;
            char cmdKey = cmd.charAt(1);
            int camId = -1;
            try{
                try {
                    camId = Integer.parseInt(cmd.substring(2));
                } catch (NumberFormatException e) {
                    // ignore
                }
                if (cmdKey == 's') {
                    log.trace("Make shot from cam{}", camId);
                    cameraDriver.executeShot(chatId, camId);
                } else if (cmdKey == 'e') {
                    if (cmd.startsWith("/enable ")) {
                        try {
                            camId = Integer.parseInt(cmd.substring(8));
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        log.trace("Enable notifications from cam{}", camId);
                        settings.enableNotifications(camId);
                        sender.sendMessage(chatId, "Enabled notifications from camera " + camId);
                    } else {
                        processed = false;
                    }
                } else if (cmdKey == 'l') {
                    log.trace("Left cam{}", camId);
                    cameraDriver.executeCmd(chatId, camId, "left");
                } else if (cmdKey == 'r') {
                    if (cmd.startsWith("/reset")) {
                        log.trace("Restore settings");
                        settings.restoreSettings();
                        sender.sendMessage(chatId, "Enabled all notifications");
                    } else {
                        log.trace("Right cam{}", camId);
                        cameraDriver.executeCmd(chatId, camId, "right");
                    }
                } else if (cmdKey == 'u') {
                    log.trace("Up cam{}", camId);
                    cameraDriver.executeCmd(chatId, camId, "up");
                } else if (cmdKey == 'd') {
                    if (cmd.startsWith("/disable ")) {
                        try {
                            camId = Integer.parseInt(cmd.substring(9));
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        log.trace("Disable notifications from cam{}", camId);
                        settings.disableNotifications(camId);
                        sender.sendMessage(chatId, "Disabled notifications from camera " + camId);
                    } else {
                        log.trace("Down cam{}", camId);
                        cameraDriver.executeCmd(chatId, camId, "down");
                    }
                } else if (cmdKey == 'p') {
                    String[] cmdParts = cmd.split(" ");
                    if (cmdParts.length > 1) {
                        int position = -1;
                        camId = Integer.parseInt(cmdParts[0].substring(2));
                        position = Integer.parseInt(cmdParts[1]);
                        log.trace("Call posion for cam{} to {}", camId, position);
                        cameraDriver.callPosition(chatId, camId, position);
                    } else {
                        processed = false;
                    }
                } else {
                    processed = false;
                }
            } catch (NumberFormatException e) {
                log.debug("Exception", e);
            }
            if (!processed) {
                sender.sendMessage(chatId, "Команда не обработана.\nПомощь: /help");
            }
        }
    }

    public CommandParser(CameraDriver cameraDriver, TelegramSenderServiceImpl sender,
            SettingsService settings) {
        this.cameraDriver = cameraDriver;
        this.sender = sender;
        this.botName = "@" + sender.getBotName().toLowerCase();
        this.settings = settings;
    }

    private static final Logger log = LoggerFactory.getLogger(CommandParser.class);

}
