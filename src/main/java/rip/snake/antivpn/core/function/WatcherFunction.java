package rip.snake.antivpn.core.function;


import rip.snake.antivpn.core.utils.Callback;

/**
 * Function that can be awaited
 *
 * @param <T> The type of the function
 */
public class WatcherFunction<T> {

    // Lock object
    private final Object lock = new Lock();
    // Callback function to call when the function is called
    private Callback<T> callback;

    // If the function has been called
    private Boolean called = false;

    // Called when the function is called
    public WatcherFunction<T> then(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    // Wait for the function to be called
    public void await() throws InterruptedException {
        synchronized (this.lock) {
            while (!called) {
                this.lock.wait();
            }
        }
    }

    // Call the function
    public void call(T calling) throws Exception {
        call();
        if (callback != null) callback.call(calling);
    }

    // Call the function and notify all threads
    private void call() {
        synchronized (lock) {
            lock.notifyAll();
        }
        this.called = true;
    }

    // Lock object
    private static final class Lock {
    }

}
