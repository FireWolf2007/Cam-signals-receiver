package ru.wolfa.cam.signals.receiver;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.util.ExecutorServiceUtil;

public class ThreadingServiceImpl {
    private static final ExecutorService executor = ExecutorServiceUtil.newExecutorService();
    private static final ConcurrentLinkedQueue<Runnable> rejectedTasks = new ConcurrentLinkedQueue<>();

    public void exec(Runnable task) {
        try {
            executor.execute(task);
            log.info("Task executed: {}", task);
        } catch (RejectedExecutionException e) {
            log.info("Task rejected: {}", task);
            rejectedTasks.add(task);
        }
    }

    public void runTasks() {
        while (true) {
            try {
                synchronized (rejectedTasks) {
                    rejectedTasks.wait(5000L);
                    log.info("Checking rejected tasks");
                }
                if (!rejectedTasks.isEmpty()) {
                    Runnable task = rejectedTasks.poll();
                    log.info("New rejected task: {}", task);
                    try {
                        executor.execute(task);
                        log.info("Rejected task executed: {}", task);
                    } catch (RejectedExecutionException e) {
                        log.info("Rejected task rejected: {}", task);
                        rejectedTasks.add(task);
                    }
                }
            } catch (InterruptedException e) {
                log.error("Interrupt", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ThreadingServiceImpl.class);
}
