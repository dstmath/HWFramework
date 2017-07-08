package org.apache.xml.serializer.dom3;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;

public final class DOMErrorImpl implements DOMError {
    private Exception fException;
    private DOMLocatorImpl fLocation;
    private String fMessage;
    private Object fRelatedData;
    private short fSeverity;
    private String fType;

    DOMErrorImpl() {
        this.fSeverity = (short) 1;
        this.fMessage = null;
        this.fException = null;
        this.fLocation = new DOMLocatorImpl();
    }

    public DOMErrorImpl(short severity, String message, String type) {
        this.fSeverity = (short) 1;
        this.fMessage = null;
        this.fException = null;
        this.fLocation = new DOMLocatorImpl();
        this.fSeverity = severity;
        this.fMessage = message;
        this.fType = type;
    }

    public DOMErrorImpl(short severity, String message, String type, Exception exception) {
        this.fSeverity = (short) 1;
        this.fMessage = null;
        this.fException = null;
        this.fLocation = new DOMLocatorImpl();
        this.fSeverity = severity;
        this.fMessage = message;
        this.fType = type;
        this.fException = exception;
    }

    public DOMErrorImpl(short severity, String message, String type, Exception exception, Object relatedData, DOMLocatorImpl location) {
        this.fSeverity = (short) 1;
        this.fMessage = null;
        this.fException = null;
        this.fLocation = new DOMLocatorImpl();
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
