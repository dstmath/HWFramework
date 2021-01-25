package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.HashMap;
import java.util.Map;

/* compiled from: XSAttributeChecker */
class LargeContainer extends Container {
    Map items;

    LargeContainer(int i) {
        this.items = new HashMap((i * 2) + 1);
        this.values = new OneAttr[i];
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.Container
    public void put(String str, OneAttr oneAttr) {
        this.items.put(str, oneAttr);
        OneAttr[] oneAttrArr = this.values;
        int i = this.pos;
        this.pos = i + 1;
        oneAttrArr[i] = oneAttr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.Container
    public OneAttr get(String str) {
        return (OneAttr) this.items.get(str);
    }
}
