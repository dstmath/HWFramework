package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class XSGroupDecl implements XSModelGroupDefinition {
    public XSObjectList fAnnotations = null;
    public XSModelGroupImpl fModelGroup = null;
    public String fName = null;
    private XSNamespaceItem fNamespaceItem = null;
    public String fTargetNamespace = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 6;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return this.fName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition
    public XSModelGroup getModelGroup() {
        return this.fModelGroup;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition
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
