package ohos.com.sun.org.apache.xerces.internal.xpointer;

import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

class ShortHandPointer implements XPointerPart {
    private boolean fIsFragmentResolved = false;
    int fMatchingChildCount = 0;
    private String fShortHandPointer;
    private SymbolTable fSymbolTable;

    public String getChildrenSchemaDeterminedID(XMLAttributes xMLAttributes, int i) throws XNIException {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public String getSchemeData() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public void setSchemeData(String str) {
    }

    public ShortHandPointer() {
    }

    public ShortHandPointer(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public void parseXPointer(String str) throws XNIException {
        this.fShortHandPointer = str;
        this.fIsFragmentResolved = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public boolean resolveXPointer(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations, int i) throws XNIException {
        if (this.fMatchingChildCount == 0) {
            this.fIsFragmentResolved = false;
        }
        if (i == 0) {
            if (this.fMatchingChildCount == 0) {
                this.fIsFragmentResolved = hasMatchingIdentifier(qName, xMLAttributes, augmentations, i);
            }
            if (this.fIsFragmentResolved) {
                this.fMatchingChildCount++;
            }
        } else if (i == 2) {
            if (this.fMatchingChildCount == 0) {
                this.fIsFragmentResolved = hasMatchingIdentifier(qName, xMLAttributes, augmentations, i);
            }
        } else if (this.fIsFragmentResolved) {
            this.fMatchingChildCount--;
        }
        return this.fIsFragmentResolved;
    }

    private boolean hasMatchingIdentifier(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations, int i) throws XNIException {
        String str = null;
        if (xMLAttributes != null) {
            int i2 = 0;
            while (i2 < xMLAttributes.getLength() && (str = getSchemaDeterminedID(xMLAttributes, i2)) == null && (str = getChildrenSchemaDeterminedID(xMLAttributes, i2)) == null && (str = getDTDDeterminedID(xMLAttributes, i2)) == null) {
                i2++;
            }
        }
        if (str == null || !str.equals(this.fShortHandPointer)) {
            return false;
        }
        return true;
    }

    public String getDTDDeterminedID(XMLAttributes xMLAttributes, int i) throws XNIException {
        if (xMLAttributes.getType(i).equals(SchemaSymbols.ATTVAL_ID)) {
            return xMLAttributes.getValue(i);
        }
        return null;
    }

    public String getSchemaDeterminedID(XMLAttributes xMLAttributes, int i) throws XNIException {
        AttributePSVI attributePSVI = (AttributePSVI) xMLAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
        if (attributePSVI == null) {
            return null;
        }
        XSTypeDefinition memberTypeDefinition = attributePSVI.getMemberTypeDefinition();
        if (memberTypeDefinition != null) {
            memberTypeDefinition = attributePSVI.getTypeDefinition();
        }
        if (memberTypeDefinition == null || !((XSSimpleType) memberTypeDefinition).isIDType()) {
            return null;
        }
        return attributePSVI.getSchemaNormalizedValue();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public boolean isFragmentResolved() {
        return this.fIsFragmentResolved;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public boolean isChildFragmentResolved() {
        return (this.fMatchingChildCount > 0) & this.fIsFragmentResolved;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public String getSchemeName() {
        return this.fShortHandPointer;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public void setSchemeName(String str) {
        this.fShortHandPointer = str;
    }
}
