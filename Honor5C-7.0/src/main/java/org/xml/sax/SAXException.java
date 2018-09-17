package org.xml.sax;

public class SAXException extends Exception {
    private Exception exception;

    public SAXException() {
        this.exception = null;
    }

    public SAXException(String message) {
        super(message);
        this.exception = null;
    }

    public SAXException(Exception e) {
        this.exception = e;
    }

    public SAXException(String message, Exception e) {
        super(message);
        this.exception = e;
    }

    public String getMessage() {
        String message = super.getMessage();
        if (message != null || this.exception == null) {
            return message;
        }
        return this.exception.getMessage();
    }

    public Exception getException() {
        return this.exception;
    }

    public String toString() {
        if (this.exception != null) {
            return this.exception.toString();
        }
        return super.toString();
    }
}
