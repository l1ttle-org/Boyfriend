package ru.l1ttleO.boyfriend.exceptions;

// used for errors that have a low chance of occuring

public class ImprobableException extends RuntimeException {
    public ImprobableException(final String message) {
        super(message);
    }
}
