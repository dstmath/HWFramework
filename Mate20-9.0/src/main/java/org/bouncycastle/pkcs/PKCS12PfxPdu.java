package org.bouncycastle.pkcs;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Arrays;

public class PKCS12PfxPdu {
    private Pfx pfx;

    public PKCS12PfxPdu(Pfx pfx2) {
        this.pfx = pfx2;
    }

    public PKCS12PfxPdu(byte[] bArr) throws IOException {
        this(parseBytes(bArr));
    }

    private static Pfx parseBytes(byte[] bArr) throws IOException {
        try {
            return Pfx.getInstance(ASN1Primitive.fromByteArray(bArr));
        } catch (ClassCastException e) {
            throw new PKCSIOException("malformed data: " + e.getMessage(), e);
        } catch (IllegalArgumentException e2) {
            throw new PKCSIOException("malformed data: " + e2.getMessage(), e2);
        }
    }

    public ContentInfo[] getContentInfos() {
        ASN1Sequence instance = ASN1Sequence.getInstance(ASN1OctetString.getInstance(this.pfx.getAuthSafe().getContent()).getOctets());
        ContentInfo[] contentInfoArr = new ContentInfo[instance.size()];
        for (int i = 0; i != instance.size(); i++) {
            contentInfoArr[i] = ContentInfo.getInstance(instance.getObjectAt(i));
        }
        return contentInfoArr;
    }

    public byte[] getEncoded() throws IOException {
        return toASN1Structure().getEncoded();
    }

    public byte[] getEncoded(String str) throws IOException {
        return toASN1Structure().getEncoded(str);
    }

    public AlgorithmIdentifier getMacAlgorithmID() {
        MacData macData = this.pfx.getMacData();
        if (macData != null) {
            return macData.getMac().getAlgorithmId();
        }
        return null;
    }

    public boolean hasMac() {
        return this.pfx.getMacData() != null;
    }

    public boolean isMacValid(PKCS12MacCalculatorBuilderProvider pKCS12MacCalculatorBuilderProvider, char[] cArr) throws PKCSException {
        if (hasMac()) {
            MacData macData = this.pfx.getMacData();
            try {
                return Arrays.constantTimeAreEqual(new MacDataGenerator(pKCS12MacCalculatorBuilderProvider.get(new AlgorithmIdentifier(macData.getMac().getAlgorithmId().getAlgorithm(), new PKCS12PBEParams(macData.getSalt(), macData.getIterationCount().intValue())))).build(cArr, ASN1OctetString.getInstance(this.pfx.getAuthSafe().getContent()).getOctets()).getEncoded(), this.pfx.getMacData().getEncoded());
            } catch (IOException e) {
                throw new PKCSException("unable to process AuthSafe: " + e.getMessage());
            }
        } else {
            throw new IllegalStateException("no MAC present on PFX");
        }
    }

    public Pfx toASN1Structure() {
        return this.pfx;
    }
}
