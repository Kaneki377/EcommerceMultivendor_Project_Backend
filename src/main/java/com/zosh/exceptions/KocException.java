package com.zosh.exceptions;

public class KocException extends RuntimeException{
    public KocException(String message) {
        super(message);
    }
    public KocException(String message, Throwable cause) {
        super(message, cause);
    }

    public KocException(Throwable cause) {
        super(cause);
    }
}
