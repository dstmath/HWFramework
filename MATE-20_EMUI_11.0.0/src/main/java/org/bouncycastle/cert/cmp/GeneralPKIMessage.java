package org.bouncycastle.cert.cmp;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.cert.CertIOException;

public class GeneralPKIMessage {
    private final PKIMessage pkiMessage;

    public GeneralPKIMessage(PKIMessage pKIMessage) {
        this.pkiMessage = pKIMessage;
    }

    public GeneralPKIMessage(byte[] bArr) throws IOException {
        this(parseBytes(bArr));
    }

    private static PKIMessage parseBytes(byte[] bArr) throws IOException {
        try {
            return PKIMessage.getInstance(ASN1Primitive.fromByteArray(bArr));
        } catch (ClassCastException e) {
            throw new CertIOException("malformed data: " + e.getMessage(), e);
        } catch (IllegalArgumentException e2) {
            throw new CertIOException("malformed data: " + e2.getMessage(), e2);
        }
    }

    public PKIBody getBody() {
        return this.pkiMessage.getBody();
    }

    public PKIHeader getHeader() {
        return this.pkiMessage.getHeader();
    }

    public boolean hasProtection() {
        return this.pkiMessage.getHeader().getProtectionAlg() != null;
    }

    public PKIMessage toASN1Structure() {
        return this.pkiMessage;
    }
}
