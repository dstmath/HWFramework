package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class PSVIElementNSImpl extends ElementNSImpl implements ElementPSVI {
    static final long serialVersionUID = 6815489624636016068L;
    protected Object fActualValue = null;
    protected short fActualValueType = 45;
    protected XSElementDeclaration fDeclaration = null;
    protected StringList fErrorCodes = null;
    protected ShortList fItemValueTypes = null;
    protected XSSimpleTypeDefinition fMemberType = null;
    protected boolean fNil = false;
    protected String fNormalizedValue = null;
    protected XSNotationDeclaration fNotation = null;
    protected XSModel fSchemaInformation = null;
    protected boolean fSpecified = true;
    protected XSTypeDefinition fTypeDecl = null;
    protected short fValidationAttempted = 0;
    protected String fValidationContext = null;
    protected short fValidity = 0;

    public PSVIElementNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) {
        super(coreDocumentImpl, str, str2, str3);
    }

    public PSVIElementNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2) {
        super(coreDocumentImpl, str, str2);
    }

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
        return this.fErrorCodes;
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
    public XSModel getSchemaInformation() {
        return this.fSchemaInformation;
    }

    public void setPSVI(ElementPSVI elementPSVI) {
        this.fDeclaration = elementPSVI.getElementDeclaration();
        this.fNotation = elementPSVI.getNotation();
        this.fValidationContext = elementPSVI.getValidationContext();
        this.fTypeDecl = elementPSVI.getTypeDefinition();
        this.fSchemaInformation = elementPSVI.getSchemaInformation();
        this.fValidity = elementPSVI.getValidity();
        this.fValidationAttempted = elementPSVI.getValidationAttempted();
        this.fErrorCodes = elementPSVI.getErrorCodes();
        this.fNormalizedValue = elementPSVI.getSchemaNormalizedValue();
        this.fActualValue = elementPSVI.getActualNormalizedValue();
        this.fActualValueType = elementPSVI.getActualNormalizedValueType();
        this.fItemValueTypes = elementPSVI.getItemValueTypes();
        this.fMemberType = elementPSVI.getMemberTypeDefinition();
        this.fSpecified = elementPSVI.getIsSchemaSpecified();
        this.fNil = elementPSVI.getNil();
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

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        throw new NotSerializableException(getClass().getName());
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        throw new NotSerializableException(getClass().getName());
    }
}
