package ohos.com.sun.org.apache.xerces.internal.xni;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;

public interface XMLDTDHandler {
    public static final short CONDITIONAL_IGNORE = 1;
    public static final short CONDITIONAL_INCLUDE = 0;

    void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException;

    void comment(XMLString xMLString, Augmentations augmentations) throws XNIException;

    void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException;

    void endAttlist(Augmentations augmentations) throws XNIException;

    void endConditional(Augmentations augmentations) throws XNIException;

    void endDTD(Augmentations augmentations) throws XNIException;

    void endExternalSubset(Augmentations augmentations) throws XNIException;

    void endParameterEntity(String str, Augmentations augmentations) throws XNIException;

    void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException;

    XMLDTDSource getDTDSource();

    void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException;

    void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException;

    void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException;

    void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException;

    void setDTDSource(XMLDTDSource xMLDTDSource);

    void startAttlist(String str, Augmentations augmentations) throws XNIException;

    void startConditional(short s, Augmentations augmentations) throws XNIException;

    void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException;

    void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException;

    void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException;

    void textDecl(String str, String str2, Augmentations augmentations) throws XNIException;

    void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException;
}
