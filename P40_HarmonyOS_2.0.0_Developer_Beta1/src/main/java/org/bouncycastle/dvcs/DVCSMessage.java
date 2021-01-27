package org.bouncycastle.dvcs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.ContentInfo;

public abstract class DVCSMessage {
    private final ContentInfo contentInfo;

    protected DVCSMessage(ContentInfo contentInfo2) {
        this.contentInfo = contentInfo2;
    }

    public abstract ASN1Encodable getContent();

    public ASN1ObjectIdentifier getContentType() {
        return this.contentInfo.getContentType();
    }
}
