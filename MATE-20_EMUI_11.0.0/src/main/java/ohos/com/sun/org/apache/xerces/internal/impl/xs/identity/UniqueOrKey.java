package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

public class UniqueOrKey extends IdentityConstraint {
    public UniqueOrKey(String str, String str2, String str3, short s) {
        super(str, str2, str3);
        this.type = s;
    }
}
