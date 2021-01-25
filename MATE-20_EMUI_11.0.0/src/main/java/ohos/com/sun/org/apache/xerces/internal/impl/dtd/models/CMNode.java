package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

public abstract class CMNode {
    private CMStateSet fFirstPos = null;
    private CMStateSet fFollowPos = null;
    private CMStateSet fLastPos = null;
    private int fMaxStates = -1;
    private int fType;
    private Object fUserData = null;

    /* access modifiers changed from: protected */
    public abstract void calcFirstPos(CMStateSet cMStateSet);

    /* access modifiers changed from: protected */
    public abstract void calcLastPos(CMStateSet cMStateSet);

    public abstract boolean isNullable();

    public CMNode(int i) {
        this.fType = i;
    }

    public final int type() {
        return this.fType;
    }

    public final CMStateSet firstPos() {
        if (this.fFirstPos == null) {
            this.fFirstPos = new CMStateSet(this.fMaxStates);
            calcFirstPos(this.fFirstPos);
        }
        return this.fFirstPos;
    }

    public final CMStateSet lastPos() {
        if (this.fLastPos == null) {
            this.fLastPos = new CMStateSet(this.fMaxStates);
            calcLastPos(this.fLastPos);
        }
        return this.fLastPos;
    }

    /* access modifiers changed from: package-private */
    public final void setFollowPos(CMStateSet cMStateSet) {
        this.fFollowPos = cMStateSet;
    }

    public final void setMaxStates(int i) {
        this.fMaxStates = i;
    }

    public void setUserData(Object obj) {
        this.fUserData = obj;
    }

    public Object getUserData() {
        return this.fUserData;
    }
}
