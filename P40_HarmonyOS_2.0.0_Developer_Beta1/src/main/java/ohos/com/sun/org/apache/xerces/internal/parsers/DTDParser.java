package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;

public abstract class DTDParser extends XMLGrammarParser implements XMLDTDHandler, XMLDTDContentModelHandler {
    protected XMLDTDScanner fDTDScanner;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
    }

    public void childrenElement(String str) throws XNIException {
    }

    public void childrenEndGroup() throws XNIException {
    }

    public void childrenOccurrence(short s) throws XNIException {
    }

    public void childrenSeparator(short s) throws XNIException {
    }

    public void childrenStartGroup() throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endAttlist(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endConditional(Augmentations augmentations) throws XNIException {
    }

    public void endContentModel() throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endDTD(Augmentations augmentations) throws XNIException {
    }

    public void endEntity(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endExternalSubset(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
    }

    public DTDGrammar getDTDGrammar() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
    }

    public void mixedElement(String str) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startAttlist(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startConditional(short s, Augmentations augmentations) throws XNIException {
    }

    public void startContentModel(String str, short s) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
    }

    public void startEntity(String str, String str2, String str3, String str4) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
    }

    public void textDecl(String str, String str2) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
    }

    public DTDParser(SymbolTable symbolTable) {
        super(symbolTable);
    }
}
