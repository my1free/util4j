package com.my.util4j.parallel.threadpool;

import java.util.concurrent.ExecutorService;

/**
 * @author michealyang
 * @version 1.0
 * @created 17/9/18
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class MDefaultThreadPool {
    private MDefaultThreadPool() {
    }

    public static ExecutorService getThreadPool() {
        return InnerClass.threadPool;
    }

    /**
     * 单例用的内部静态类
     */
    static class InnerClass {
        public static ExecutorService threadPool = MExecutors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, "M-DEFAULT");
    }

}
