package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

public class CMUniOp extends CMNode {
    private CMNode fChild;

    public CMUniOp(int i, CMNode cMNode) {
        super(i);
        if (type() == 1 || type() == 2 || type() == 3) {
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
        if (type() == 3) {
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
}
