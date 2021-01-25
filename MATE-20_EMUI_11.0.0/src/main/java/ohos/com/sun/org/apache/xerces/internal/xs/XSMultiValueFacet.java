package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSMultiValueFacet extends XSObject {
    XSObjectList getAnnotations();

    short getFacetKind();

    StringList getLexicalFacetValues();
}
