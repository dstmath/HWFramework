package org.apache.xml.serializer.dom3;

import java.io.IOException;
import org.apache.xml.serializer.DOM3Serializer;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.utils.WrappedRuntimeException;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializerFilter;
import org.xml.sax.SAXException;

public final class DOM3SerializerImpl implements DOM3Serializer {
    private DOMErrorHandler fErrorHandler;
    private String fNewLine;
    private SerializationHandler fSerializationHandler;
    private LSSerializerFilter fSerializerFilter;

    public DOM3SerializerImpl(SerializationHandler handler) {
        this.fSerializationHandler = handler;
    }

    public DOMErrorHandler getErrorHandler() {
        return this.fErrorHandler;
    }

    public LSSerializerFilter getNodeFilter() {
        return this.fSerializerFilter;
    }

    public char[] getNewLine() {
        return this.fNewLine != null ? this.fNewLine.toCharArray() : null;
    }

    public void serializeDOM3(Node node) throws IOException {
        try {
            new DOM3TreeWalker(this.fSerializationHandler, this.fErrorHandler, this.fSerializerFilter, this.fNewLine).traverse(node);
        } catch (SAXException se) {
            throw new WrappedRuntimeException(se);
        }
    }

    public void setErrorHandler(DOMErrorHandler handler) {
        this.fErrorHandler = handler;
    }

    public void setNodeFilter(LSSerializerFilter filter) {
        this.fSerializerFilter = filter;
    }

    public void setSerializationHandler(SerializationHandler handler) {
        this.fSerializationHandler = handler;
    }

    public void setNewLine(char[] newLine) {
        String str = null;
        if (newLine != null) {
            str = new String(newLine);
        }
        this.fNewLine = str;
    }
}
