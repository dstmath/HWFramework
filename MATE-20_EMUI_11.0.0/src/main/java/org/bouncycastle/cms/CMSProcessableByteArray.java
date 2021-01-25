package org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.util.Arrays;

public class CMSProcessableByteArray implements CMSTypedData, CMSReadable {
    private final byte[] bytes;
    private final ASN1ObjectIdentifier type;

    public CMSProcessableByteArray(ASN1ObjectIdentifier aSN1ObjectIdentifier, byte[] bArr) {
        this.type = aSN1ObjectIdentifier;
        this.bytes = bArr;
    }

    public CMSProcessableByteArray(byte[] bArr) {
        this(CMSObjectIdentifiers.data, bArr);
    }

    @Override // org.bouncycastle.cms.CMSProcessable
    public Object getContent() {
        return Arrays.clone(this.bytes);
    }

    @Override // org.bouncycastle.cms.CMSTypedData
    public ASN1ObjectIdentifier getContentType() {
        return this.type;
    }

    @Override // org.bouncycastle.cms.CMSReadable
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    @Override // org.bouncycastle.cms.CMSProcessable
    public void write(OutputStream outputStream) throws IOException, CMSException {
        outputStream.write(this.bytes);
    }
}
