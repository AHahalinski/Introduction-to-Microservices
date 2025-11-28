package com.epam.microservices.resourceservice.exception;

public class SongServiceCommunicationException extends RuntimeException {
    public SongServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}


