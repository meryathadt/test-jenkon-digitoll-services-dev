package com.digitoll.commons.exception;

public class ExpiredAuthTokenException extends Exception {

    public ExpiredAuthTokenException(String message) {
        super(message);
    }
}
