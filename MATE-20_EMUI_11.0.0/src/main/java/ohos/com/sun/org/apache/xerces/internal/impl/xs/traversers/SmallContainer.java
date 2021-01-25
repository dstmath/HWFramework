package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

/* compiled from: XSAttributeChecker */
class SmallContainer extends Container {
    String[] keys;

    SmallContainer(int i) {
        this.keys = new String[i];
        this.values = new OneAttr[i];
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.Container
    public void put(String str, OneAttr oneAttr) {
        this.keys[this.pos] = str;
        OneAttr[] oneAttrArr = this.values;
        int i = this.pos;
        this.pos = i + 1;
        oneAttrArr[i] = oneAttr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.Container
    public OneAttr get(String str) {
        for (int i = 0; i < this.pos; i++) {
            if (this.keys[i].equals(str)) {
                return this.values[i];
            }
        }
        return null;
    }
}
