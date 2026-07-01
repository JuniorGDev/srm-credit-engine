package br.com.creditengine.exceptions;

public class CurrencyAlreadyExistsException extends RuntimeException {
    public CurrencyAlreadyExistsException(String message) {
        super("Currency already exists with code: " + message);
    }
}
