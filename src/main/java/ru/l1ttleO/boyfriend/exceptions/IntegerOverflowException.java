package ru.l1ttleO.boyfriend.exceptions;

public class IntegerOverflowException extends Exception {

    public IntegerOverflowException() {
        super("Введена слишком большая продолжительность, из-за чего она стала отрицательной");
    }
}
