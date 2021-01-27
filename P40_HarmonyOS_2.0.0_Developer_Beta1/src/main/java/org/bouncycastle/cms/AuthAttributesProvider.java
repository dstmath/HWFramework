package org.bouncycastle.cms;

import org.bouncycastle.asn1.ASN1Set;

/* access modifiers changed from: package-private */
public interface AuthAttributesProvider {
    ASN1Set getAuthAttributes();

    boolean isAead();
}
