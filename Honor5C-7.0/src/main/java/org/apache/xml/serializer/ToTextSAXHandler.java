package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class ToTextSAXHandler extends ToSAXHandler {
    public void endElement(String elemName) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEndElem(elemName);
        }
    }

    public void endElement(String arg0, String arg1, String arg2) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEndElem(arg2);
        }
    }

    public ToTextSAXHandler(ContentHandler hdlr, LexicalHandler lex, String encoding) {
        super(hdlr, lex, encoding);
    }

    public ToTextSAXHandler(ContentHandler handler, String encoding) {
        super(handler, encoding);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (this.m_tracer != null) {
            super.fireCommentEvent(ch, start, length);
        }
    }

    public void comment(String data) throws SAXException {
        int length = data.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        data.getChars(0, length, this.m_charsBuff, 0);
        comment(this.m_charsBuff, 0, length);
    }

    public Properties getOutputFormat() {
        return null;
    }

    public OutputStream getOutputStream() {
        return null;
    }

    public Writer getWriter() {
        return null;
    }

    public void indent(int n) throws SAXException {
    }

    public boolean reset() {
        return false;
    }

    public void serialize(Node node) throws IOException {
    }

    public boolean setEscaping(boolean escape) {
        return false;
    }

    public void setIndent(boolean indent) {
    }

    public void setOutputFormat(Properties format) {
    }

    public void setOutputStream(OutputStream output) {
    }

    public void setWriter(Writer writer) {
    }

    public void addAttribute(String uri, String localName, String rawName, String type, String value, boolean XSLAttribute) {
    }

    public void attributeDecl(String arg0, String arg1, String arg2, String arg3, String arg4) throws SAXException {
    }

    public void elementDecl(String arg0, String arg1) throws SAXException {
    }

    public void externalEntityDecl(String arg0, String arg1, String arg2) throws SAXException {
    }

    public void internalEntityDecl(String arg0, String arg1) throws SAXException {
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEscapingEvent(arg0, arg1);
        }
    }

    public void setDocumentLocator(Locator arg0) {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
        flushPending();
        super.startElement(arg0, arg1, arg2, arg3);
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startEntity(String arg0) throws SAXException {
    }

    public void startElement(String elementNamespaceURI, String elementLocalName, String elementName) throws SAXException {
        super.startElement(elementNamespaceURI, elementLocalName, elementName);
    }

    public void startElement(String elementName) throws SAXException {
        super.startElement(elementName);
    }

    public void endDocument() throws SAXException {
        flushPending();
        this.m_saxHandler.endDocument();
        if (this.m_tracer != null) {
            super.fireEndDoc();
        }
    }

    public void characters(String characters) throws SAXException {
        int length = characters.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        characters.getChars(0, length, this.m_charsBuff, 0);
        this.m_saxHandler.characters(this.m_charsBuff, 0, length);
    }

    public void characters(char[] characters, int offset, int length) throws SAXException {
        this.m_saxHandler.characters(characters, offset, length);
        if (this.m_tracer != null) {
            super.fireCharEvent(characters, offset, length);
        }
    }

    public void addAttribute(String name, String value) {
    }

    public boolean startPrefixMapping(String prefix, String uri, boolean shouldFlush) throws SAXException {
        return false;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void namespaceAfterStartElement(String prefix, String uri) throws SAXException {
    }
}
