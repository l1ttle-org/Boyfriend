package ru.l1ttleO.boyfriend.exceptions;

public class InvalidAuthorException extends Exception {

    public InvalidAuthorException() {
        super("Автор является null");
    }
}
