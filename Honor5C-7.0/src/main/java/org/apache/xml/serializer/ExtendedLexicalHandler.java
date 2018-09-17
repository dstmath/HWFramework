package org.apache.xml.serializer;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public interface ExtendedLexicalHandler extends LexicalHandler {
    void comment(String str) throws SAXException;
}
