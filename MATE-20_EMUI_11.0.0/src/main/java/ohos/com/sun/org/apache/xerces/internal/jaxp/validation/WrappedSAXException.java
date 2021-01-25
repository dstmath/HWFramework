package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.org.xml.sax.SAXException;

public class WrappedSAXException extends RuntimeException {
    public final SAXException exception;

    WrappedSAXException(SAXException sAXException) {
        this.exception = sAXException;
    }
}
