package org.apache.xml.dtm;

import org.w3c.dom.DOMException;

public class DTMDOMException extends DOMException {
    static final long serialVersionUID = 1895654266613192414L;

    public DTMDOMException(short code, String message) {
        super(code, message);
    }

    public DTMDOMException(short code) {
        super(code, "");
    }
}
