package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

import ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition;

public class KeyRef extends IdentityConstraint {
    protected UniqueOrKey fKey;

    public KeyRef(String str, String str2, String str3, UniqueOrKey uniqueOrKey) {
        super(str, str2, str3);
        this.fKey = uniqueOrKey;
        this.type = 2;
    }

    public UniqueOrKey getKey() {
        return this.fKey;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint, ohos.com.sun.org.apache.xerces.internal.xs.XSIDCDefinition
    public XSIDCDefinition getRefKey() {
        return this.fKey;
    }
}
