package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

/* access modifiers changed from: package-private */
/* compiled from: XSAttributeChecker */
public abstract class Container {
    static final int THRESHOLD = 5;
    int pos = 0;
    OneAttr[] values;

    /* access modifiers changed from: package-private */
    public abstract OneAttr get(String str);

    /* access modifiers changed from: package-private */
    public abstract void put(String str, OneAttr oneAttr);

    Container() {
    }

    static Container getContainer(int i) {
        if (i > 5) {
            return new LargeContainer(i);
        }
        return new SmallContainer(i);
    }
}
