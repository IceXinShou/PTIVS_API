package com.test;

public class ErrorException extends Exception{
    public ErrorException() {
        super();
    }
    public ErrorException(String reason) {
        super(reason);
    }
}
