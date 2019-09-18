package org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class PKCS7TypedStream extends CMSTypedStream {
    private final ASN1Encodable content;

    public PKCS7TypedStream(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) throws IOException {
        super(aSN1ObjectIdentifier);
        this.content = aSN1Encodable;
    }

    private InputStream getContentStream(ASN1Encodable aSN1Encodable) throws IOException {
        byte[] encoded = aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER);
        int i = 1;
        while ((encoded[i] & 255) > Byte.MAX_VALUE) {
            i++;
        }
        int i2 = i + 1;
        return new ByteArrayInputStream(encoded, i2, encoded.length - i2);
    }

    public void drain() throws IOException {
        getContentStream(this.content);
    }

    public ASN1Encodable getContent() {
        return this.content;
    }

    public InputStream getContentStream() {
        try {
            return getContentStream(this.content);
        } catch (IOException e) {
            throw new CMSRuntimeException("unable to convert content to stream: " + e.getMessage(), e);
        }
    }
}
