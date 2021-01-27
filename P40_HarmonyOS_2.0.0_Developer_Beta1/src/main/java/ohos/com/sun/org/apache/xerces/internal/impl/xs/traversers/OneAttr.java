package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

/* access modifiers changed from: package-private */
/* compiled from: XSAttributeChecker */
public class OneAttr {
    public Object dfltValue;
    public int dvIndex;
    public String name;
    public int valueIndex;

    public OneAttr(String str, int i, int i2, Object obj) {
        this.name = str;
        this.dvIndex = i;
        this.valueIndex = i2;
        this.dfltValue = obj;
    }
}
