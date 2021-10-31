/*
    This file is part of Boyfriend
    Copyright (C) 2021  l1ttleO

    Boyfriend is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Boyfriend is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Boyfriend.  If not, see <https://www.gnu.org/licenses/>.
*/

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
