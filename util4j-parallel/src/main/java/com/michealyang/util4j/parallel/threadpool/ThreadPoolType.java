package com.michealyang.util4j.parallel.threadpool;

import java.util.concurrent.ExecutorService;

/**
 * @author michealyang
 * @version 1.0
 * @created 17/9/22
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */

/**
 * 不同的操作使用不同的线程池，保证线程池之间的隔离，防止相互影响
 */
public enum ThreadPoolType {
    DEFAULT;

    public static ExecutorService getThreadPool(ThreadPoolType threadPoolType) {
        switch (threadPoolType) {
            default:
                return MDefaultThreadPool.getThreadPool();
        }
    }
}
