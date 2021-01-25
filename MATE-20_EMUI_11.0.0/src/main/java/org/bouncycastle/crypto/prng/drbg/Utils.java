package org.bouncycastle.crypto.prng.drbg;

import java.util.Hashtable;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSKeyParameters;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.util.Integers;

/* access modifiers changed from: package-private */
public class Utils {
    static final Hashtable maxSecurityStrengths = new Hashtable();

    static {
        maxSecurityStrengths.put(McElieceCCA2KeyGenParameterSpec.SHA1, Integers.valueOf(128));
        maxSecurityStrengths.put(McElieceCCA2KeyGenParameterSpec.SHA224, Integers.valueOf(CertificateHolderAuthorization.CVCA));
        maxSecurityStrengths.put("SHA-256", Integers.valueOf(256));
        maxSecurityStrengths.put(McElieceCCA2KeyGenParameterSpec.SHA384, Integers.valueOf(256));
        maxSecurityStrengths.put("SHA-512", Integers.valueOf(256));
        maxSecurityStrengths.put("SHA-512/224", Integers.valueOf(CertificateHolderAuthorization.CVCA));
        maxSecurityStrengths.put(SPHINCSKeyParameters.SHA512_256, Integers.valueOf(256));
    }

    Utils() {
    }

    static int getMaxSecurityStrength(Digest digest) {
        return ((Integer) maxSecurityStrengths.get(digest.getAlgorithmName())).intValue();
    }

    static int getMaxSecurityStrength(Mac mac) {
        String algorithmName = mac.getAlgorithmName();
        return ((Integer) maxSecurityStrengths.get(algorithmName.substring(0, algorithmName.indexOf("/")))).intValue();
    }

    static byte[] hash_df(Digest digest, byte[] bArr, int i) {
        byte[] bArr2 = new byte[((i + 7) / 8)];
        int length = bArr2.length / digest.getDigestSize();
        byte[] bArr3 = new byte[digest.getDigestSize()];
        int i2 = 0;
        int i3 = 1;
        for (int i4 = 0; i4 <= length; i4++) {
            digest.update((byte) i3);
            digest.update((byte) (i >> 24));
            digest.update((byte) (i >> 16));
            digest.update((byte) (i >> 8));
            digest.update((byte) i);
            digest.update(bArr, 0, bArr.length);
            digest.doFinal(bArr3, 0);
            System.arraycopy(bArr3, 0, bArr2, bArr3.length * i4, bArr2.length - (bArr3.length * i4) > bArr3.length ? bArr3.length : bArr2.length - (bArr3.length * i4));
            i3++;
        }
        int i5 = i % 8;
        if (i5 != 0) {
            int i6 = 8 - i5;
            int i7 = 0;
            while (i2 != bArr2.length) {
                int i8 = bArr2[i2] & 255;
                bArr2[i2] = (byte) ((i7 << (8 - i6)) | (i8 >>> i6));
                i2++;
                i7 = i8;
            }
        }
        return bArr2;
    }

    static boolean isTooLarge(byte[] bArr, int i) {
        return bArr != null && bArr.length > i;
    }
}
