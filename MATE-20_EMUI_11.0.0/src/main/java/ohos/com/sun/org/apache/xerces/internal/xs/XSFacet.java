package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSFacet extends XSObject {
    XSAnnotation getAnnotation();

    XSObjectList getAnnotations();

    short getFacetKind();

    boolean getFixed();

    String getLexicalFacetValue();
}
