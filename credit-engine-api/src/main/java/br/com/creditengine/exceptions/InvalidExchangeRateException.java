package br.com.creditengine.exceptions;

public class InvalidExchangeRateException extends RuntimeException {
    public InvalidExchangeRateException() {
        super("From and to currencies cannot be the same");
    }
}
