package rip.snake.antivpn.core.function;


import rip.snake.antivpn.core.utils.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Function that can be awaited
 *
 * @param <T> The type of the function
 */
public class WatcherFunction<T> {
    /**
     * Map of all the functions that are waiting for a response
     */
    public static final ConcurrentHashMap<String, WatcherFunction<?>> waitingResponses = new ConcurrentHashMap<>();

    // Lock object
    private final Lock concurrencyLock = new ReentrantLock();
    private final Object notifyLock = new NotifyLock();
    private final String uid;

    // Callback function to call when the function is called
    private final List<Callback<T>> callbacks = new ArrayList<>();
    private T data;
    // If the function has been called
    private final AtomicBoolean called = new AtomicBoolean(false);

    // Unique identifier of the function

    /**
     * Create a new function
     *
     * @param uid The uid of the function
     */
    public WatcherFunction(String uid) {
        this.uid = uid;
    }

    @SuppressWarnings("unchecked")
    public static <T> WatcherFunction<T> getWatcherFunction(String key) {
        return (WatcherFunction<T>) WatcherFunction.waitingResponses.getOrDefault(key, null);
    }

    public static <T> WatcherFunction<T> createFunction(String uid) {
        WatcherFunction<T> function = new WatcherFunction<>(uid);
        waitingResponses.put(uid, function);
        return function;
    }

    // Called when the function is called
    public WatcherFunction<T> then(Callback<T> callback) {
        this.concurrencyLock.lock();
        if (this.called.get()) {
            try {
                callback.call(this.data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            this.callbacks.add(callback);
        }
        this.concurrencyLock.unlock();
        return this;
    }

    // Wait for the function to be called with a timeout
    public T await(long timeoutMillis) throws InterruptedException {
        synchronized (this.notifyLock) {
            if (this.called.get()) {
                return this.data;
            }
            this.notifyLock.wait(timeoutMillis);
            if (!this.called.get()) {
                waitingResponses.remove(this.uid);
            }
            return this.data;
        }
    }

    /**
     * Wait for the function to be called with a timeout of 5000ms
     *
     * @throws InterruptedException If the thread is interrupted
     */
    public T await() throws InterruptedException {
        return await(5000L);
    }

    // Call the function
    public void call(T calling) {
        if (this.called.getAndSet(true)) {
            return;
        }
        synchronized (this.notifyLock) {
            this.notifyLock.notifyAll();
        }
        this.data = calling;
        // Call the callback function if it exists
        this.concurrencyLock.lock();
        this.callbacks.forEach(tCallback -> {
            try {
                tCallback.call(calling);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.concurrencyLock.unlock();
    }

    public static class NotifyLock {
    }
}