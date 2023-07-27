package rip.snake.antivpn.core.function;


import rip.snake.antivpn.core.utils.Callback;
import rip.snake.antivpn.core.utils.Console;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Object lock = new Lock();
    private final String uid;

    // Callback function to call when the function is called
    private Callback<T> callback;
    // If the function has been called
    private Boolean called = false;
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
        this.callback = callback;
        return this;
    }

    // Wait for the function to be called with a timeout
    public void await(long timeoutMillis) throws InterruptedException {
        synchronized (this.lock) {
            if (!called) {
                // Wait for the function to be called
                this.lock.wait(timeoutMillis);

                // Remove the function from the waiting responses if it has not been called
                if (!called) waitingResponses.remove(this.uid);
            }
        }
    }

    /**
     * Wait for the function to be called with a timeout of 5000ms
     *
     * @throws InterruptedException If the thread is interrupted
     */
    public void await() throws InterruptedException {
        await(5000L);
    }

    // Call the function
    public void call(T calling) throws Exception {
        call();

        // Call the callback function if it exists
        if (callback != null) callback.call(calling);
    }

    // Call the function and notify all threads
    private void call() {
        // Set the function as called
        this.called = true;

        // Notify all threads to unlock them
        synchronized (lock) {
            lock.notifyAll();
        }

        // Remove the function from the waiting responses
        waitingResponses.remove(this.uid);
    }

    // Lock object
    private static final class Lock {
    }

}
