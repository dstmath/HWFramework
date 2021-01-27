package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMStateSet;

public class XSCMUniOp extends CMNode {
    private CMNode fChild;

    public XSCMUniOp(int i, CMNode cMNode) {
        super(i);
        if (type() == 5 || type() == 4 || type() == 6) {
            this.fChild = cMNode;
            return;
        }
        throw new RuntimeException("ImplementationMessages.VAL_UST");
    }

    /* access modifiers changed from: package-private */
    public final CMNode getChild() {
        return this.fChild;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public boolean isNullable() {
        if (type() == 6) {
            return this.fChild.isNullable();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public void calcFirstPos(CMStateSet cMStateSet) {
        cMStateSet.setTo(this.fChild.firstPos());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public void calcLastPos(CMStateSet cMStateSet) {
        cMStateSet.setTo(this.fChild.lastPos());
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public void setUserData(Object obj) {
        super.setUserData(obj);
        this.fChild.setUserData(obj);
    }
}
