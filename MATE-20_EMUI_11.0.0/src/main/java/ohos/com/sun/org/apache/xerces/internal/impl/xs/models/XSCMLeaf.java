package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMStateSet;

public class XSCMLeaf extends CMNode {
    private Object fLeaf = null;
    private int fParticleId = -1;
    private int fPosition = -1;

    public XSCMLeaf(int i, Object obj, int i2, int i3) {
        super(i);
        this.fLeaf = obj;
        this.fParticleId = i2;
        this.fPosition = i3;
    }

    /* access modifiers changed from: package-private */
    public final Object getLeaf() {
        return this.fLeaf;
    }

    /* access modifiers changed from: package-private */
    public final int getParticleId() {
        return this.fParticleId;
    }

    /* access modifiers changed from: package-private */
    public final int getPosition() {
        return this.fPosition;
    }

    /* access modifiers changed from: package-private */
    public final void setPosition(int i) {
        this.fPosition = i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public boolean isNullable() {
        return this.fPosition == -1;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer(this.fLeaf.toString());
        if (this.fPosition >= 0) {
            stringBuffer.append(" (Pos:" + Integer.toString(this.fPosition) + ")");
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public void calcFirstPos(CMStateSet cMStateSet) {
        int i = this.fPosition;
        if (i == -1) {
            cMStateSet.zeroBits();
        } else {
            cMStateSet.setBit(i);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode
    public void calcLastPos(CMStateSet cMStateSet) {
        int i = this.fPosition;
        if (i == -1) {
            cMStateSet.zeroBits();
        } else {
            cMStateSet.setBit(i);
        }
    }
}
