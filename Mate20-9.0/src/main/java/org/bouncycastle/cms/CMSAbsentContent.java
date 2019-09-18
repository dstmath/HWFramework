package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;

public class CMSAbsentContent implements CMSTypedData, CMSReadable {
    private final ASN1ObjectIdentifier type;

    public CMSAbsentContent() {
        this(CMSObjectIdentifiers.data);
    }

    public CMSAbsentContent(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.type = aSN1ObjectIdentifier;
    }

    public Object getContent() {
        return null;
    }

    public ASN1ObjectIdentifier getContentType() {
        return this.type;
    }

    public InputStream getInputStream() {
        return null;
    }

    public void write(OutputStream outputStream) throws IOException, CMSException {
    }
}
