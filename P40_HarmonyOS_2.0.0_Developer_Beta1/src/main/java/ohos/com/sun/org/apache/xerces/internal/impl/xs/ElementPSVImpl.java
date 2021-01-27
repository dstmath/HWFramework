package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class ElementPSVImpl implements ElementPSVI {
    protected Object fActualValue = null;
    protected short fActualValueType = 45;
    protected XSElementDeclaration fDeclaration = null;
    protected String[] fErrorCodes = null;
    protected SchemaGrammar[] fGrammars = null;
    protected ShortList fItemValueTypes = null;
    protected XSSimpleTypeDefinition fMemberType = null;
    protected boolean fNil = false;
    protected String fNormalizedValue = null;
    protected XSNotationDeclaration fNotation = null;
    protected XSModel fSchemaInformation = null;
    protected boolean fSpecified = false;
    protected XSTypeDefinition fTypeDecl = null;
    protected short fValidationAttempted = 0;
    protected String fValidationContext = null;
    protected short fValidity = 0;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public String getSchemaDefault() {
        XSElementDeclaration xSElementDeclaration = this.fDeclaration;
        if (xSElementDeclaration == null) {
            return null;
        }
        return xSElementDeclaration.getConstraintValue();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public String getSchemaNormalizedValue() {
        return this.fNormalizedValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public boolean getIsSchemaSpecified() {
        return this.fSpecified;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public short getValidationAttempted() {
        return this.fValidationAttempted;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public short getValidity() {
        return this.fValidity;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public StringList getErrorCodes() {
        String[] strArr = this.fErrorCodes;
        if (strArr == null) {
            return null;
        }
        return new StringListImpl(strArr, strArr.length);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public String getValidationContext() {
        return this.fValidationContext;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI
    public boolean getNil() {
        return this.fNil;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI
    public XSNotationDeclaration getNotation() {
        return this.fNotation;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public XSTypeDefinition getTypeDefinition() {
        return this.fTypeDecl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public XSSimpleTypeDefinition getMemberTypeDefinition() {
        return this.fMemberType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI
    public XSElementDeclaration getElementDeclaration() {
        return this.fDeclaration;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI
    public synchronized XSModel getSchemaInformation() {
        if (this.fSchemaInformation == null && this.fGrammars != null) {
            this.fSchemaInformation = new XSModelImpl(this.fGrammars);
        }
        return this.fSchemaInformation;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public Object getActualNormalizedValue() {
        return this.fActualValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public short getActualNormalizedValueType() {
        return this.fActualValueType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public ShortList getItemValueTypes() {
        return this.fItemValueTypes;
    }

    public void reset() {
        this.fDeclaration = null;
        this.fTypeDecl = null;
        this.fNil = false;
        this.fSpecified = false;
        this.fNotation = null;
        this.fMemberType = null;
        this.fValidationAttempted = 0;
        this.fValidity = 0;
        this.fErrorCodes = null;
        this.fValidationContext = null;
        this.fNormalizedValue = null;
        this.fActualValue = null;
        this.fActualValueType = 45;
        this.fItemValueTypes = null;
    }
}
