package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSParticle extends XSObject {
    XSObjectList getAnnotations();

    int getMaxOccurs();

    boolean getMaxOccursUnbounded();

    int getMinOccurs();

    XSTerm getTerm();
}
