package com.my.util4j.parallel;

import java.util.concurrent.CountDownLatch;

/**
 * @author michealyang
 * @version 1.0
 * @created 17/9/18
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */

/**
 *
 * @param <T>  任务返回值类型
 */
public abstract class MWorker<T> implements Runnable {
    private CountDownLatch countDownLatch;
    private MResult<T> result;

    @Override
    public void run() {
        result = new MResult<>();
        try {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            result.setData(execute());
            result.setSuccess(true);
        } catch (InterruptedException e) {
            result.setSuccess(false);
            result.setMsg("thread is interrupted");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMsg(e.getMessage());
        } finally {
            countDownLatch.countDown();
            setResult(result);
        }
    }

    public abstract T execute();

    public void setResult(MResult<T> result) {
        this.result = result;
    }

    public MResult<T> getResult() {
        return result;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
