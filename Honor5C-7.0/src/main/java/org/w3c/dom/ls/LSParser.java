package org.w3c.dom.ls;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface LSParser {
    public static final short ACTION_APPEND_AS_CHILDREN = (short) 1;
    public static final short ACTION_INSERT_AFTER = (short) 4;
    public static final short ACTION_INSERT_BEFORE = (short) 3;
    public static final short ACTION_REPLACE = (short) 5;
    public static final short ACTION_REPLACE_CHILDREN = (short) 2;

    void abort();

    boolean getAsync();

    boolean getBusy();

    DOMConfiguration getDomConfig();

    LSParserFilter getFilter();

    Document parse(LSInput lSInput) throws DOMException, LSException;

    Document parseURI(String str) throws DOMException, LSException;

    Node parseWithContext(LSInput lSInput, Node node, short s) throws DOMException, LSException;

    void setFilter(LSParserFilter lSParserFilter);
}
