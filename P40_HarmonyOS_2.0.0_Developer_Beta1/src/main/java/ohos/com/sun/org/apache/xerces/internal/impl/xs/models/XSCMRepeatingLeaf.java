package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

public final class XSCMRepeatingLeaf extends XSCMLeaf {
    private final int fMaxOccurs;
    private final int fMinOccurs;

    public XSCMRepeatingLeaf(int i, Object obj, int i2, int i3, int i4, int i5) {
        super(i, obj, i4, i5);
        this.fMinOccurs = i2;
        this.fMaxOccurs = i3;
    }

    /* access modifiers changed from: package-private */
    public final int getMinOccurs() {
        return this.fMinOccurs;
    }

    /* access modifiers changed from: package-private */
    public final int getMaxOccurs() {
        return this.fMaxOccurs;
    }
}
