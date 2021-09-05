package ru.l1ttleO.boyfriend.exceptions;

import org.jetbrains.annotations.NotNull;

public class NumberOverflowException extends Exception {

    public NumberOverflowException(final @NotNull String message) {
        super(message);
    }
}
