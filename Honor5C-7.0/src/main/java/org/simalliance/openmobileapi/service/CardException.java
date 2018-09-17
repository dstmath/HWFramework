package org.simalliance.openmobileapi.service;

public class CardException extends Exception {
    private static final long mSerialVersionUID = 945149106070548293L;

    public CardException(String message) {
        super(message);
    }

    public CardException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardException(Throwable cause) {
        super(cause);
    }
}
