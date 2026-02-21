package com.henrique.picpaysimplified.exceptions;

public class CredentialException extends RuntimeException {
    public CredentialException(String message) {
        super(message);
    }
    public CredentialException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
