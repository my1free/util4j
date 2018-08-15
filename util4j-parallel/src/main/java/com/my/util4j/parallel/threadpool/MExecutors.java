package com.my.util4j.parallel.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author michealyang
 * @version 1.0
 * @created 17/9/22
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class MExecutors {

    public static ExecutorService newFixedThreadPool(int nThreads, String threadName) {
        final BlockingQueue blockingQueue = new LinkedBlockingQueue<Runnable>();

        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                blockingQueue,
                new NamedThreadFactory(threadName));
    }
}
