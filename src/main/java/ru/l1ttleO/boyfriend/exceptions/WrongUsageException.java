package ru.l1ttleO.boyfriend.exceptions;

import org.jetbrains.annotations.NotNull;

public class WrongUsageException extends Exception {

    public WrongUsageException(final @NotNull String message) {
        super(message);
    }
}
