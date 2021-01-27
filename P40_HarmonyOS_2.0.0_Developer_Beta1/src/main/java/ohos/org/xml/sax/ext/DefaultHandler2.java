package ohos.org.xml.sax.ext;

import java.io.IOException;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.helpers.DefaultHandler;

public class DefaultHandler2 extends DefaultHandler implements LexicalHandler, DeclHandler, EntityResolver2 {
    @Override // ohos.org.xml.sax.ext.DeclHandler
    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void comment(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.DeclHandler
    public void elementDecl(String str, String str2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void endCDATA() throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void endDTD() throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void endEntity(String str) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.DeclHandler
    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.EntityResolver2
    public InputSource getExternalSubset(String str, String str2) throws SAXException, IOException {
        return null;
    }

    @Override // ohos.org.xml.sax.ext.DeclHandler
    public void internalEntityDecl(String str, String str2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.EntityResolver2
    public InputSource resolveEntity(String str, String str2, String str3, String str4) throws SAXException, IOException {
        return null;
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void startCDATA() throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void startDTD(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ext.LexicalHandler
    public void startEntity(String str) throws SAXException {
    }

    @Override // ohos.org.xml.sax.helpers.DefaultHandler, ohos.org.xml.sax.EntityResolver
    public InputSource resolveEntity(String str, String str2) throws SAXException, IOException {
        return resolveEntity(null, str, null, str2);
    }
}
