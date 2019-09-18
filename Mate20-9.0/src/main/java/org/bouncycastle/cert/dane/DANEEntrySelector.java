package org.bouncycastle.cert.dane;

import org.bouncycastle.util.Selector;

public class DANEEntrySelector implements Selector {
    private final String domainName;

    DANEEntrySelector(String str) {
        this.domainName = str;
    }

    public Object clone() {
        return this;
    }

    public String getDomainName() {
        return this.domainName;
    }

    public boolean match(Object obj) {
        return ((DANEEntry) obj).getDomainName().equals(this.domainName);
    }
}
