package br.com.creditengine.exceptions;

public class ExchangeRateAlreadyException extends RuntimeException {
    public ExchangeRateAlreadyException(String message) {
        super("Exchange already exists with code: " + message);
    }
}
