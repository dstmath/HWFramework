package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import java.io.IOException;
import java.io.StringReader;
import ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.parsers.DOMParser;
import ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;

public class XSAnnotationImpl implements XSAnnotation {
    private String fData = null;
    private SchemaGrammar fGrammar = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 12;
    }

    public XSAnnotationImpl(String str, SchemaGrammar schemaGrammar) {
        this.fData = str;
        this.fGrammar = schemaGrammar;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation
    public boolean writeAnnotation(Object obj, short s) {
        if (s == 1 || s == 3) {
            writeToDOM((Node) obj, s);
            return true;
        } else if (s != 2) {
            return false;
        } else {
            writeToSAX((ContentHandler) obj);
            return true;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation
    public String getAnnotationString() {
        return this.fData;
    }

    private synchronized void writeToSAX(ContentHandler contentHandler) {
        SAXParser sAXParser = this.fGrammar.getSAXParser();
        InputSource inputSource = new InputSource(new StringReader(this.fData));
        sAXParser.setContentHandler(contentHandler);
        try {
            sAXParser.parse(inputSource);
        } catch (IOException | SAXException unused) {
        }
        sAXParser.setContentHandler(null);
    }

    private synchronized void writeToDOM(Node node, short s) {
        Node node2;
        Document ownerDocument = s == 1 ? node.getOwnerDocument() : (Document) node;
        DOMParser dOMParser = this.fGrammar.getDOMParser();
        try {
            dOMParser.parse(new InputSource(new StringReader(this.fData)));
        } catch (IOException | SAXException unused) {
        }
        Document document = dOMParser.getDocument();
        dOMParser.dropDocumentReferences();
        Element documentElement = document.getDocumentElement();
        if (ownerDocument instanceof CoreDocumentImpl) {
            node2 = ownerDocument.adoptNode(documentElement);
            if (node2 == null) {
                node2 = ownerDocument.importNode(documentElement, true);
            }
        } else {
            node2 = ownerDocument.importNode(documentElement, true);
        }
        node.insertBefore(node2, node.getFirstChild());
    }
}
