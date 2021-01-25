package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSAttributeUse extends XSObject {
    Object getActualVC() throws XSException;

    short getActualVCType() throws XSException;

    XSObjectList getAnnotations();

    XSAttributeDeclaration getAttrDeclaration();

    short getConstraintType();

    String getConstraintValue();

    ShortList getItemValueTypes() throws XSException;

    boolean getRequired();
}
