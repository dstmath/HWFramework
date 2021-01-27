package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSAttributeGroupDefinition extends XSObject {
    XSAnnotation getAnnotation();

    XSObjectList getAnnotations();

    XSObjectList getAttributeUses();

    XSWildcard getAttributeWildcard();
}
