package org.apache.xml.serializer.dom3;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;

public final class DOMErrorImpl implements DOMError {
    private Exception fException = null;
    private DOMLocatorImpl fLocation = new DOMLocatorImpl();
    private String fMessage = null;
    private Object fRelatedData;
    private short fSeverity = (short) 1;
    private String fType;

    DOMErrorImpl() {
    }

    public DOMErrorImpl(short severity, String message, String type) {
        this.fSeverity = severity;
        this.fMessage = message;
        this.fType = type;
    }

    public DOMErrorImpl(short severity, String message, String type, Exception exception) {
        this.fSeverity = severity;
        this.fMessage = message;
        this.fType = type;
        this.fException = exception;
    }

    public DOMErrorImpl(short severity, String message, String type, Exception exception, Object relatedData, DOMLocatorImpl location) {
        this.fSeverity = severity;
        this.fMessage = message;
        this.fType = type;
        this.fException = exception;
        this.fRelatedData = relatedData;
        this.fLocation = location;
    }

    public short getSeverity() {
        return this.fSeverity;
    }

    public String getMessage() {
        return this.fMessage;
    }

    public DOMLocator getLocation() {
        return this.fLocation;
    }

    public Object getRelatedException() {
        return this.fException;
    }

    public String getType() {
        return this.fType;
    }

    public Object getRelatedData() {
        return this.fRelatedData;
    }

    public void reset() {
        this.fSeverity = (short) 1;
        this.fException = null;
        this.fMessage = null;
        this.fType = null;
        this.fRelatedData = null;
        this.fLocation = null;
    }
}
