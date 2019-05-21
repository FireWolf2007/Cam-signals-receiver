package ru.wolfa.cam.driver;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class CameraApexisJ011WSTest {
    private CameraApexisJ011WS driver;

    private MockWebServer server;

    private WebClient webClient;

    @After
    public void shutdown() throws Exception {
        this.server.shutdown();
    }

    @Before
    public void initTest() throws IOException {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("1"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("2"));
        String baseUrl = server.url("/").toString();
        webClient = WebClient.create(baseUrl);

        driver = new CameraApexisJ011WS("1.1.1.1", "89", "ladmin", "lpwd", true) {
            public WebClient getClient() {
                return webClient;
            }
        };
    }

    @Test
    public void testSyncTime() throws InterruptedException, BrokenBarrierException {
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
        Assert.assertEquals("/set_datetime.cgi", req);
        Assert.assertEquals(1, server.getRequestCount());
        Assert.assertEquals(1, a.get());
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
        Assert.assertEquals("/snapshot.cgi", req);
        Assert.assertEquals(1, server.getRequestCount());
        Assert.assertEquals(1, a.get());
    }

    @Test
    public void testCallPositionOutOfRange() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        // 0 position
        driver.callPosition(v -> a.incrementAndGet(), 0);
        // 16 position
        driver.callPosition(v -> a.incrementAndGet(), 16);
        Assert.assertEquals(0, a.get());
    }

    @Test
    public void testCallPosition1() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.callPosition(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        }, 1);
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=31", req);
        Assert.assertEquals(1, server.getRequestCount());
        Assert.assertEquals(1, a.get());
    }

    @Test
    public void testCallPosition15() throws InterruptedException {
        AtomicInteger a = new AtomicInteger(0);
        driver.callPosition(v -> {
            a.incrementAndGet();
            synchronized (a) {
                a.notifyAll();
            }
        }, 15);
        // Wait for callback
        synchronized (a) {
            a.wait(10000L);
        }
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=59", req);
        Assert.assertEquals(1, server.getRequestCount());
        Assert.assertEquals(1, a.get());
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
        Assert.assertEquals(1, a.get());
        Assert.assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=4", req);

        request = server.takeRequest();
        req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=1", req);
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
        Assert.assertEquals(1, a.get());
        Assert.assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=6", req);

        request = server.takeRequest();
        req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=1", req);
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
        Assert.assertEquals(1, a.get());
        Assert.assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=0", req);

        request = server.takeRequest();
        req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=1", req);
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
        Assert.assertEquals(1, a.get());
        Assert.assertEquals(2, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        String req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=2", req);

        request = server.takeRequest();
        req = request.getPath();
        Assert.assertEquals("/decoder_control.cgi?command=1", req);
    }

}
