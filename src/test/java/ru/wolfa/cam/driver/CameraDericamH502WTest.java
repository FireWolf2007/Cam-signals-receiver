package ru.wolfa.cam.driver;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CameraDericamH502WTest {

    private CameraDericamH502W driver;

    private MockWebServer server;

    private WebClient webClient;

    @AfterEach
    public void shutdown() throws Exception {
        this.server.shutdown();
    }

    @BeforeEach
    public void initTest() throws IOException {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("1"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("2"));
        String baseUrl = server.url("/").toString();
        webClient = WebClient.create(baseUrl);

        driver = new CameraDericamH502W("1.1.1.1", "89", "ladmin", "lpwd", true) {
            public WebClient getClient() {
                return webClient;
            }
        };
    }

    @Test
    public void syncTimerTest() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.syncTime(c -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        });
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        int i = req.indexOf("?");
        req = req.substring(0, i);
        assertEquals("/setntp.xml", req);
        assertEquals(1, server.getRequestCount());
        assertEquals(1, a.get());

    }

    @Test
    public void testExecuteShot() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.executeShot(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        });
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        assertEquals("/snap.jpg", req);
        assertEquals(1, server.getRequestCount());
        assertEquals(1, a.get());
    }

    @Test
    public void testExecuteLeft() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.executeCmd(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        }, "left", 1);
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        assertEquals(1, a.get());
        assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        assertEquals("/moveptz.xml?dir=left", req);

        request = server.takeRequest();
        req = request.getPath();
        assertEquals("/moveptz.xml?dir=stop", req);
    }

    @Test
    public void testExecuteRight() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.executeCmd(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        }, "right", 1);
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        assertEquals(1, a.get());
        assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        assertEquals("/moveptz.xml?dir=right", req);

        request = server.takeRequest();
        req = request.getPath();
        assertEquals("/moveptz.xml?dir=stop", req);
    }

    @Test
    public void testExecuteUp() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.executeCmd(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        }, "up", 1);
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        assertEquals(1, a.get());
        assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        assertEquals("/moveptz.xml?dir=up", req);

        request = server.takeRequest();
        req = request.getPath();
        assertEquals("/moveptz.xml?dir=stop", req);
    }

    @Test
    public void testExecuteDown() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.executeCmd(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        }, "down", 1);
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        assertEquals(1, a.get());
        assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        assertEquals("/moveptz.xml?dir=down", req);

        request = server.takeRequest();
        req = request.getPath();
        assertEquals("/moveptz.xml?dir=stop", req);
    }

}
