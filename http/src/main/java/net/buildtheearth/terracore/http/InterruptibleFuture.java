package net.buildtheearth.terracore.http;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link CompletableFuture} that can be manually bound to its executor thread,
 * so it can interrupt it when {@link CompletableFuture#cancel(boolean)} is called.
 * <br>
 * This lets the code responsible for completing the future be interrupted during blocking operations,
 * allowing for immediate cancellation of the underlying operation upon calling {@link CompletableFuture#cancel(boolean)}.
 *
 * @param <T> the type of the completed future
 */
class InterruptibleFuture<T> extends CompletableFuture<T> {

    private Thread boundThread;

    /**
     * Binds the current thread to this future, allowing it to be interrupted when {@link CompletableFuture#cancel(boolean)} is called.
     */
    void bindThread() {
        this.boundThread = Thread.currentThread();
    }

    /**
     * Unbinds the future from its bound thread, preventing it from being interrupted.
     * This needs to be called as soon as the code responsible for completing the future is no longer running on the bound thread,
     * so that said thread can be safely reused for other tasks without risking interruption.
     */
    void unbindThread() {
        this.boundThread = null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        var wasCanceled = super.cancel(mayInterruptIfRunning);
        if (!wasCanceled || !mayInterruptIfRunning || this.boundThread == null) {
            return wasCanceled;
        }
        this.boundThread.interrupt();
        return true;
    }

    /**
     * Binds the current thread to this future, allowing it to be interrupted when {@link CompletableFuture#cancel(boolean)} is called,
     * and returns a {@link Closeable} that must be closed as soon as the code responsible for completing the future is no longer running on the bound thread,
     * so that said thread can be safely reused for other tasks without risking interruption.
     * <br><br>
     * This is mostly syntactic sugar to avoid having to call {@link InterruptibleFuture#unbindThread()} manually.
     * <br><br>
     * Example:
     * <pre>{@code
     *   var myFuture = new InterruptibleFuture<Void>();
     *   Executors.newSingleThreadExecutor().run(() -> {
     *       try (var ignored = myFuture.enterInterruptibleScope()) {
     *          doInterruptibleWork(myFuture);
     *       }
     *   });
     * }</pre>
     *
     *
     * @return a closable context that unbinds this future's thread when closed
     */
    InterruptibleContext enterInterruptibleScope() {
        return new InterruptibleContext();
    }

    class InterruptibleContext implements Closeable {
        private InterruptibleContext() {
            InterruptibleFuture.this.bindThread();
        }

        @Override
        public void close() {
            InterruptibleFuture.this.unbindThread();
        }
    }

}
