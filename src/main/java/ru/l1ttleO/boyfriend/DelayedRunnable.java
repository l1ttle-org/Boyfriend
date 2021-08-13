package ru.l1ttleO.boyfriend;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class DelayedRunnable {

    public final Thread thread;
    public final long duration;
    public final long startedAt;

    public DelayedRunnable(final @Nullable ThreadGroup threadGroup, final @NotNull Consumer<DelayedRunnable> runnable, final @Nullable String name, final @NotNull long millis, final @Nullable Consumer<DelayedRunnable> ifInterrupted) {
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
