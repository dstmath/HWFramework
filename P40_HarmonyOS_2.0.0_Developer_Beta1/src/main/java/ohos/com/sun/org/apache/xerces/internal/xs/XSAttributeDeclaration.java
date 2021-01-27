package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSAttributeDeclaration extends XSObject {
    Object getActualVC() throws XSException;

    short getActualVCType() throws XSException;

    XSAnnotation getAnnotation();

    XSObjectList getAnnotations();

    short getConstraintType();

    String getConstraintValue();

    XSComplexTypeDefinition getEnclosingCTDefinition();

    ShortList getItemValueTypes() throws XSException;

    short getScope();

    XSSimpleTypeDefinition getTypeDefinition();
}
