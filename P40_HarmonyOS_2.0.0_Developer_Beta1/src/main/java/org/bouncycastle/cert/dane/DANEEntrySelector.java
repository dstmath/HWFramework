package org.bouncycastle.cert.dane;

import org.bouncycastle.util.Selector;

public class DANEEntrySelector implements Selector {
    private final String domainName;

    DANEEntrySelector(String str) {
        this.domainName = str;
    }

    @Override // org.bouncycastle.util.Selector, java.lang.Object
    public Object clone() {
        return this;
    }

    public String getDomainName() {
        return this.domainName;
    }

    @Override // org.bouncycastle.util.Selector
    public boolean match(Object obj) {
        return ((DANEEntry) obj).getDomainName().equals(this.domainName);
    }
}
