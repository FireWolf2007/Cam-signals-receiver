package ru.wolfa.cam.driver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for Sricam SP017 wifi camera.
 *
 * Problems:
 * 1. Takes snapshots over shell script.
 * 2. Clock syncrinization does not working.
 *
 */
public class CameraSricamSp017 implements Camera {

    private final String ip;
    private final String port;
    private final boolean includeInAll;

    private static final String XML_REQ_PREFIX = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"  xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\"  xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
            + "<soap:Body>" + "<tptz:ContinuousMove>" + "<tptz:Velocity>";

    // + "<tt:PanTilt x=\"-1\" y=\"0\"/>"
    private static final String XML_REQ_SUFFIX = "</tptz:Velocity>" + "</tptz:ContinuousMove>" + "</soap:Body>"
            + "</soap:Envelope>";

    public CameraSricamSp017(String ip, String port, String camLogin, String camPassword, boolean all) {
        this.ip = ip;
        this.port = port;
        this.includeInAll = all;
    }

    @Override
    public WebClient getClient() {
        return WebClient.builder().baseUrl("http://" + ip + ":" + port).build();
    }

    @Override
    public void syncTime(Consumer<ByteArrayResource> callback) {
        log.trace("syncTime not implemented");
        ByteArrayResource t = new ByteArrayResource(new byte[] {});
        callback.accept(t);
    }

    @Override
    public void callPosition(Consumer<ByteArrayResource> callback, int position) {
        log.trace("callPosition not implemented");
        ByteArrayResource t = new ByteArrayResource(new byte[] {});
        callback.accept(t);
        /*
          String msg = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">" +
          "<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
          "<GotoPreset xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\">" +
          "<ProfileToken>' . $profileToken . '</ProfileToken>" + 
          "<PresetToken>" + position + "</PresetToken></GotoPreset></s:Body></s:Envelope>";
          String content_type = 'application/soap+xml; charset=utf-8; action="http://www.onvif.org/ver20/ptz/wsdl/GotoPreset"';
        */
    }

    private final static Random rnd = new Random();

    @Override
    public void executeShot(Consumer<ByteArrayResource> callback) {
        String fileName = "/tmp/ONVIF_tmp_" + rnd.nextInt() + ".jpg";
        try {
            Process process = Runtime.getRuntime().exec("./scripts/shot_sricam_sp017 " + ip + " " + fileName);
            process.waitFor();
            File f = new File(fileName);
            ByteArrayResource r = new ByteArrayResource(Files.readAllBytes(f.toPath()));
            f.delete();
            callback.accept(r);
        } catch (InterruptedException | IOException e) {
            log.error("Exception", e);
        }
    }

    @Override
    public void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis) {
        final String command;

        switch (direction) {
        case "left":
            command = "<tt:PanTilt x=\"-1\" y=\"0\"/>";
            break;
        case "right":
            command = "<tt:PanTilt x=\"1\" y=\"0\"/>";
            break;
        case "up":
            command = "<tt:PanTilt x=\"0\" y=\"1\"/>";
            break;
        case "down":
            command = "<tt:PanTilt x=\"0\" y=\"-1\"/>";
            break;
        default:
            // STOP
            command = "<tt:PanTilt x=\"0\" y=\"0\"/>";
        }
        executeCmd(callback, command, timeInMillis, "", "", null);
    }

    @Override
    public void executeCmd(Consumer<ByteArrayResource> callback, String direction, int timeInMillis, String CMD_PARAM,
            String CMD_STOP, String queryPath) {
        getClient().post().body(BodyInserters.fromObject(XML_REQ_PREFIX + direction + XML_REQ_SUFFIX)).retrieve()
                .bodyToMono(ByteArrayResource.class).subscribe(c -> {
                    log.trace("MOVE to {}", direction);
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    callback.accept(c);
                });
    }

    @Override
    public boolean includeInAllCommand() {
        return includeInAll;
    }

    private static final Logger log = LoggerFactory.getLogger(CameraSricamSp017.class);
}
