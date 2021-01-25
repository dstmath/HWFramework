package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSNotationDeclaration extends XSObject {
    XSAnnotation getAnnotation();

    XSObjectList getAnnotations();

    String getPublicId();

    String getSystemId();
}
