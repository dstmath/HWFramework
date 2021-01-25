package ohos.com.sun.org.apache.xml.internal.serializer;

import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

interface ExtendedLexicalHandler extends LexicalHandler {
    void comment(String str) throws SAXException;
}
