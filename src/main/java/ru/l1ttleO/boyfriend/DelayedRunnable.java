package ru.l1ttleO.boyfriend;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class DelayedRunnable {
    
    final public Thread thread;
    final public long duration;
    final public long startedAt;

    public DelayedRunnable(@Nullable ThreadGroup threadGroup, @NotNull Consumer<DelayedRunnable> runnable, @Nullable String name, @NotNull long millis, @Nullable Consumer<DelayedRunnable> ifInterrupted) {
        duration = millis;
        final Runnable internalRunnable = () -> {
            try {
                Thread.sleep(duration);
                runnable.accept(this);
            } catch (final InterruptedException e) {
                if (ifInterrupted != null)
                    ifInterrupted.accept(this);
            }
        };
        if (name != null)
            thread = new Thread(threadGroup, internalRunnable, name);
        else
            thread = new Thread(threadGroup, internalRunnable);
        startedAt = System.currentTimeMillis();
        thread.start();
    }
    
    // TODO for Apceniy: DelayedWorker
}
