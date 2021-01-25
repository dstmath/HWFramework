package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import ohos.com.sun.org.apache.xerces.internal.jaxp.validation.WrappedSAXException;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;

public class SAX2XNI implements ContentHandler, XMLDocumentSource {
    private XMLDocumentHandler fCore;
    private Locator locator;
    private final NamespaceSupport nsContext = new NamespaceSupport();
    private final SymbolTable symbolTable = new SymbolTable();
    private final XMLAttributes xa = new XMLAttributesImpl();

    public void skippedEntity(String str) {
    }

    public SAX2XNI(XMLDocumentHandler xMLDocumentHandler) {
        this.fCore = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fCore = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public XMLDocumentHandler getDocumentHandler() {
        return this.fCore;
    }

    public void startDocument() throws SAXException {
        XMLLocator xMLLocator;
        try {
            this.nsContext.reset();
            if (this.locator == null) {
                xMLLocator = new SimpleLocator(null, null, -1, -1);
            } else {
                xMLLocator = new LocatorWrapper(this.locator);
            }
            this.fCore.startDocument(xMLLocator, null, this.nsContext, null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void endDocument() throws SAXException {
        try {
            this.fCore.endDocument(null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        try {
            this.fCore.startElement(createQName(str, str2, str3), createAttributes(attributes), null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        try {
            this.fCore.endElement(createQName(str, str2, str3), null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        try {
            this.fCore.characters(new XMLString(cArr, i, i2), null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        try {
            this.fCore.ignorableWhitespace(new XMLString(cArr, i, i2), null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void startPrefixMapping(String str, String str2) {
        this.nsContext.pushContext();
        this.nsContext.declarePrefix(str, str2);
    }

    public void endPrefixMapping(String str) {
        this.nsContext.popContext();
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        try {
            this.fCore.processingInstruction(symbolize(str), createXMLString(str2), null);
        } catch (WrappedSAXException e) {
            throw e.exception;
        }
    }

    public void setDocumentLocator(Locator locator2) {
        this.locator = locator2;
    }

    private QName createQName(String str, String str2, String str3) {
        String str4;
        int indexOf = str3.indexOf(58);
        String str5 = "";
        if (str2.length() != 0) {
            str5 = str;
        } else if (indexOf < 0) {
            str2 = str3;
        } else {
            str2 = str3.substring(indexOf + 1);
        }
        String str6 = null;
        if (indexOf < 0) {
            str4 = null;
        } else {
            str4 = str3.substring(0, indexOf);
        }
        if (str5 == null || str5.length() != 0) {
            str6 = str5;
        }
        return new QName(symbolize(str4), symbolize(str2), symbolize(str3), symbolize(str6));
    }

    private String symbolize(String str) {
        if (str == null) {
            return null;
        }
        return this.symbolTable.addSymbol(str);
    }

    private XMLString createXMLString(String str) {
        return new XMLString(str.toCharArray(), 0, str.length());
    }

    private XMLAttributes createAttributes(Attributes attributes) {
        this.xa.removeAllAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            this.xa.addAttribute(createQName(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i)), attributes.getType(i), attributes.getValue(i));
        }
        return this.xa;
    }
}
