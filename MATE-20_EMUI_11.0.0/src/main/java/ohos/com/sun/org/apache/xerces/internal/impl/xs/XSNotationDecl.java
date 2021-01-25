package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class XSNotationDecl implements XSNotationDeclaration {
    public XSObjectList fAnnotations = null;
    public String fName = null;
    private XSNamespaceItem fNamespaceItem = null;
    public String fPublicId = null;
    public String fSystemId = null;
    public String fTargetNamespace = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 11;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.fName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration
    public String getSystemId() {
        return this.fSystemId;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration
    public String getPublicId() {
        return this.fPublicId;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return this.fNamespaceItem;
    }

    /* access modifiers changed from: package-private */
    public void setNamespaceItem(XSNamespaceItem xSNamespaceItem) {
        this.fNamespaceItem = xSNamespaceItem;
    }
}
