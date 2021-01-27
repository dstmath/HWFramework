package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

public class CMAny extends CMNode {
    private int fPosition = -1;
    private int fType;
    private String fURI;

    public CMAny(int i, String str, int i2) {
        super(i);
        this.fType = i;
        this.fURI = str;
        this.fPosition = i2;
    }

    /* access modifiers changed from: package-private */
    public final int getType() {
        return this.fType;
    }

    /* access modifiers changed from: package-private */
    public final String getURI() {
        return this.fURI;
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
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("(");
        stringBuffer.append("##any:uri=");
        stringBuffer.append(this.fURI);
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
