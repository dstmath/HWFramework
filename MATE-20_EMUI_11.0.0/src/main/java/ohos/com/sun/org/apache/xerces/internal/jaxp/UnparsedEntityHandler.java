package ohos.com.sun.org.apache.xerces.internal.jaxp;

import java.util.HashMap;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;

/* access modifiers changed from: package-private */
public final class UnparsedEntityHandler implements XMLDTDFilter, EntityState {
    private XMLDTDHandler fDTDHandler;
    private XMLDTDSource fDTDSource;
    private HashMap fUnparsedEntities = null;
    private final ValidationManager fValidationManager;

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityDeclared(String str) {
        return false;
    }

    UnparsedEntityHandler(ValidationManager validationManager) {
        this.fValidationManager = validationManager;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
        this.fValidationManager.setEntityState(this);
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startDTD(xMLLocator, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startParameterEntity(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.textDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endParameterEntity(String str, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endParameterEntity(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startExternalSubset(xMLResourceIdentifier, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endExternalSubset(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endExternalSubset(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.comment(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.processingInstruction(str, xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.elementDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startAttlist(String str, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startAttlist(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.attributeDecl(str, str2, str3, strArr, str4, xMLString, xMLString2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endAttlist(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endAttlist(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.internalEntityDecl(str, xMLString, xMLString2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.externalEntityDecl(str, xMLResourceIdentifier, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        if (this.fUnparsedEntities == null) {
            this.fUnparsedEntities = new HashMap();
        }
        this.fUnparsedEntities.put(str, str);
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.unparsedEntityDecl(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.notationDecl(str, xMLResourceIdentifier, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startConditional(short s, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startConditional(s, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.ignoredCharacters(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endConditional(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endConditional(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endDTD(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endDTD(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void setDTDSource(XMLDTDSource xMLDTDSource) {
        this.fDTDSource = xMLDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public XMLDTDSource getDTDSource() {
        return this.fDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource
    public void setDTDHandler(XMLDTDHandler xMLDTDHandler) {
        this.fDTDHandler = xMLDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource
    public XMLDTDHandler getDTDHandler() {
        return this.fDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityUnparsed(String str) {
        HashMap hashMap = this.fUnparsedEntities;
        if (hashMap != null) {
            return hashMap.containsKey(str);
        }
        return false;
    }

    public void reset() {
        HashMap hashMap = this.fUnparsedEntities;
        if (hashMap != null && !hashMap.isEmpty()) {
            this.fUnparsedEntities.clear();
        }
    }
}
