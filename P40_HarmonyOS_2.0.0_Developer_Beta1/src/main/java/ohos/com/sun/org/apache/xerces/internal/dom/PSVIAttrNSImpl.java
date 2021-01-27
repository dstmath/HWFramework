package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class PSVIAttrNSImpl extends AttrNSImpl implements AttributePSVI {
    static final long serialVersionUID = -3241738699421018889L;
    protected Object fActualValue = null;
    protected short fActualValueType = 45;
    protected XSAttributeDeclaration fDeclaration = null;
    protected StringList fErrorCodes = null;
    protected ShortList fItemValueTypes = null;
    protected XSSimpleTypeDefinition fMemberType = null;
    protected String fNormalizedValue = null;
    protected boolean fSpecified = true;
    protected XSTypeDefinition fTypeDecl = null;
    protected short fValidationAttempted = 0;
    protected String fValidationContext = null;
    protected short fValidity = 0;

    public PSVIAttrNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) {
        super(coreDocumentImpl, str, str2, str3);
    }

    public PSVIAttrNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2) {
        super(coreDocumentImpl, str, str2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public String getSchemaDefault() {
        XSAttributeDeclaration xSAttributeDeclaration = this.fDeclaration;
        if (xSAttributeDeclaration == null) {
            return null;
        }
        return xSAttributeDeclaration.getConstraintValue();
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

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public XSTypeDefinition getTypeDefinition() {
        return this.fTypeDecl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI
    public XSSimpleTypeDefinition getMemberTypeDefinition() {
        return this.fMemberType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI
    public XSAttributeDeclaration getAttributeDeclaration() {
        return this.fDeclaration;
    }

    public void setPSVI(AttributePSVI attributePSVI) {
        this.fDeclaration = attributePSVI.getAttributeDeclaration();
        this.fValidationContext = attributePSVI.getValidationContext();
        this.fValidity = attributePSVI.getValidity();
        this.fValidationAttempted = attributePSVI.getValidationAttempted();
        this.fErrorCodes = attributePSVI.getErrorCodes();
        this.fNormalizedValue = attributePSVI.getSchemaNormalizedValue();
        this.fActualValue = attributePSVI.getActualNormalizedValue();
        this.fActualValueType = attributePSVI.getActualNormalizedValueType();
        this.fItemValueTypes = attributePSVI.getItemValueTypes();
        this.fTypeDecl = attributePSVI.getTypeDefinition();
        this.fMemberType = attributePSVI.getMemberTypeDefinition();
        this.fSpecified = attributePSVI.getIsSchemaSpecified();
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
