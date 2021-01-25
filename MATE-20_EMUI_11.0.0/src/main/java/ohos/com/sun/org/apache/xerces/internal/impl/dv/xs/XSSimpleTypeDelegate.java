package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeFacetException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class XSSimpleTypeDelegate implements XSSimpleType {
    protected final XSSimpleType type;

    public XSSimpleTypeDelegate(XSSimpleType xSSimpleType) {
        if (xSSimpleType != null) {
            this.type = xSSimpleType;
            return;
        }
        throw new NullPointerException();
    }

    public XSSimpleType getWrappedXSSimpleType() {
        return this.type;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getAnnotations() {
        return this.type.getAnnotations();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean getBounded() {
        return this.type.getBounded();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getBuiltInKind() {
        return this.type.getBuiltInKind();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getDefinedFacets() {
        return this.type.getDefinedFacets();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getFacets() {
        return this.type.getFacets();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean getFinite() {
        return this.type.getFinite();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getFixedFacets() {
        return this.type.getFixedFacets();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSSimpleTypeDefinition getItemType() {
        return this.type.getItemType();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public StringList getLexicalEnumeration() {
        return this.type.getLexicalEnumeration();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public String getLexicalFacetValue(short s) {
        return this.type.getLexicalFacetValue(s);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public StringList getLexicalPattern() {
        return this.type.getLexicalPattern();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getMemberTypes() {
        return this.type.getMemberTypes();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getMultiValueFacets() {
        return this.type.getMultiValueFacets();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean getNumeric() {
        return this.type.getNumeric();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getOrdered() {
        return this.type.getOrdered();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSSimpleTypeDefinition getPrimitiveType() {
        return this.type.getPrimitiveType();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getVariety() {
        return this.type.getVariety();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean isDefinedFacet(short s) {
        return this.type.isDefinedFacet(s);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean isFixedFacet(short s) {
        return this.type.isFixedFacet(s);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean derivedFrom(String str, String str2, short s) {
        return this.type.derivedFrom(str, str2, s);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean derivedFromType(XSTypeDefinition xSTypeDefinition, short s) {
        return this.type.derivedFromType(xSTypeDefinition, s);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean getAnonymous() {
        return this.type.getAnonymous();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public XSTypeDefinition getBaseType() {
        return this.type.getBaseType();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public short getFinal() {
        return this.type.getFinal();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public short getTypeCategory() {
        return this.type.getTypeCategory();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean isFinal(short s) {
        return this.type.isFinal(s);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.type.getName();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.type.getNamespace();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return this.type.getNamespaceItem();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return this.type.getType();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public void applyFacets(XSFacets xSFacets, short s, short s2, ValidationContext validationContext) throws InvalidDatatypeFacetException {
        this.type.applyFacets(xSFacets, s, s2, validationContext);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public short getPrimitiveKind() {
        return this.type.getPrimitiveKind();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public short getWhitespace() throws DatatypeException {
        return this.type.getWhitespace();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public boolean isEqual(Object obj, Object obj2) {
        return this.type.isEqual(obj, obj2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public boolean isIDType() {
        return this.type.isIDType();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public void validate(ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        this.type.validate(validationContext, validatedInfo);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public Object validate(String str, ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        return this.type.validate(str, validationContext, validatedInfo);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public Object validate(Object obj, ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        return this.type.validate(obj, validationContext, validatedInfo);
    }

    public String toString() {
        return this.type.toString();
    }
}
