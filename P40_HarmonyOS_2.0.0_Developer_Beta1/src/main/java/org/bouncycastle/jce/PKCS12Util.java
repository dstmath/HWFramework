package org.bouncycastle.jce;

import java.io.IOException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;

public class PKCS12Util {
    private static byte[] calculatePbeMac(ASN1ObjectIdentifier aSN1ObjectIdentifier, byte[] bArr, int i, char[] cArr, byte[] bArr2, String str) throws Exception {
        SecretKeyFactory instance = SecretKeyFactory.getInstance(aSN1ObjectIdentifier.getId(), str);
        PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(bArr, i);
        SecretKey generateSecret = instance.generateSecret(new PBEKeySpec(cArr));
        Mac instance2 = Mac.getInstance(aSN1ObjectIdentifier.getId(), str);
        instance2.init(generateSecret, pBEParameterSpec);
        instance2.update(bArr2);
        return instance2.doFinal();
    }

    public static byte[] convertToDefiniteLength(byte[] bArr) throws IOException {
        return Pfx.getInstance(bArr).getEncoded(ASN1Encoding.DER);
    }

    public static byte[] convertToDefiniteLength(byte[] bArr, char[] cArr, String str) throws IOException {
        Pfx instance = Pfx.getInstance(bArr);
        ContentInfo authSafe = instance.getAuthSafe();
        ContentInfo contentInfo = new ContentInfo(authSafe.getContentType(), new DEROctetString(ASN1Primitive.fromByteArray(ASN1OctetString.getInstance(authSafe.getContent()).getOctets()).getEncoded(ASN1Encoding.DER)));
        MacData macData = instance.getMacData();
        try {
            int intValue = macData.getIterationCount().intValue();
            return new Pfx(contentInfo, new MacData(new DigestInfo(new AlgorithmIdentifier(macData.getMac().getAlgorithmId().getAlgorithm(), DERNull.INSTANCE), calculatePbeMac(macData.getMac().getAlgorithmId().getAlgorithm(), macData.getSalt(), intValue, cArr, ASN1OctetString.getInstance(contentInfo.getContent()).getOctets(), str)), macData.getSalt(), intValue)).getEncoded(ASN1Encoding.DER);
        } catch (Exception e) {
            throw new IOException("error constructing MAC: " + e.toString());
        }
    }
}
