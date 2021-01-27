package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class CMLeaf extends CMNode {
    private QName fElement = new QName();
    private int fPosition = -1;

    public CMLeaf(QName qName, int i) {
        super(0);
        this.fElement.setValues(qName);
        this.fPosition = i;
    }

    public CMLeaf(QName qName) {
        super(0);
        this.fElement.setValues(qName);
    }

    /* access modifiers changed from: package-private */
    public final QName getElement() {
        return this.fElement;
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
        StringBuffer stringBuffer = new StringBuffer(this.fElement.toString());
        stringBuffer.append(" (");
        stringBuffer.append(this.fElement.uri);
        stringBuffer.append(',');
        stringBuffer.append(this.fElement.localpart);
        stringBuffer.append(')');
        if (this.fPosition >= 0) {
            stringBuffer.append(" (Pos:" + new Integer(this.fPosition).toString() + ")");
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
