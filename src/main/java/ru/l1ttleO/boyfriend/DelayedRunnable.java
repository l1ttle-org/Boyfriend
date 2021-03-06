package ru.l1ttleO.boyfriend;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DelayedRunnable {

    public final @NotNull Thread thread;
    public final long duration;
    public final long startedAt;

    public DelayedRunnable(@Nullable final ThreadGroup threadGroup, final @NotNull Consumer<DelayedRunnable> runnable, @Nullable final String name, final long millis, @Nullable final Consumer<DelayedRunnable> ifInterrupted) {
        this.duration = millis;
        final Runnable internalRunnable = () -> {
            try {
                Thread.sleep(this.duration);
                runnable.accept(this);
            } catch (final InterruptedException e) {
                if (ifInterrupted != null)
                    ifInterrupted.accept(this);
            }
        };
        if (name != null)
            this.thread = new Thread(threadGroup, internalRunnable, name);
        else
            this.thread = new Thread(threadGroup, internalRunnable);
        this.startedAt = System.currentTimeMillis();
        this.thread.start();
    }

    // TODO for Apceniy: DelayedWorker
}
