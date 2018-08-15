package com.my.util4j.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author michealyang
 * @version 1.0
 * @created 17/9/18
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class MThreadPool {
    private static ExecutorService threadPool = Executors.newFixedThreadPool(20);
    private MThreadPool() {
    }

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

}
