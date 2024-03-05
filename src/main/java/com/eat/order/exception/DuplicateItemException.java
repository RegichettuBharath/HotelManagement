package com.eat.order.exception;

@SuppressWarnings("serial")
public class DuplicateItemException extends RuntimeException {

    public DuplicateItemException(String message) {
        super(message);
    }
}
