package org.w3c.dom.ls;

import org.w3c.dom.DOMException;

public interface DOMImplementationLS {
    public static final short MODE_ASYNCHRONOUS = (short) 2;
    public static final short MODE_SYNCHRONOUS = (short) 1;

    LSInput createLSInput();

    LSOutput createLSOutput();

    LSParser createLSParser(short s, String str) throws DOMException;

    LSSerializer createLSSerializer();
}
