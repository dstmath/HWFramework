package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.crypto.DerivationFunction;
import com.android.org.bouncycastle.crypto.params.DESParameters;
import com.android.org.bouncycastle.crypto.params.KDFParameters;
import com.android.org.bouncycastle.util.Strings;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public abstract class BaseAgreementSpi extends KeyAgreementSpi {
    private static final Map<String, ASN1ObjectIdentifier> defaultOids = null;
    private static final Hashtable des = null;
    private static final Map<String, Integer> keySizes = null;
    private static final Map<String, String> nameTable = null;
    private static final Hashtable oids = null;
    private final String kaAlgorithm;
    private final DerivationFunction kdf;
    protected BigInteger result;
    protected byte[] ukmParameters;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi.<clinit>():void");
    }

    protected abstract byte[] bigIntToBytes(BigInteger bigInteger);

    public BaseAgreementSpi(String kaAlgorithm, DerivationFunction kdf) {
        this.kaAlgorithm = kaAlgorithm;
        this.kdf = kdf;
    }

    protected static String getAlgorithm(String algDetails) {
        if (algDetails.indexOf(91) > 0) {
            return algDetails.substring(0, algDetails.indexOf(91));
        }
        if (algDetails.startsWith(NISTObjectIdentifiers.aes.getId())) {
            return "AES";
        }
        String name = (String) nameTable.get(Strings.toUpperCase(algDetails));
        if (name != null) {
            return name;
        }
        return algDetails;
    }

    protected static int getKeySize(String algDetails) {
        if (algDetails.indexOf(91) > 0) {
            return (Integer.parseInt(algDetails.substring(algDetails.indexOf(91) + 1, algDetails.indexOf(93))) + 7) / 8;
        }
        String algKey = Strings.toUpperCase(algDetails);
        if (keySizes.containsKey(algKey)) {
            return ((Integer) keySizes.get(algKey)).intValue();
        }
        return -1;
    }

    protected static byte[] trimZeroes(byte[] secret) {
        if (secret[0] != null) {
            return secret;
        }
        int ind = 0;
        while (ind < secret.length && secret[ind] == null) {
            ind++;
        }
        byte[] rv = new byte[(secret.length - ind)];
        System.arraycopy(secret, ind, rv, 0, rv.length);
        return rv;
    }

    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if (this.kdf == null) {
            return bigIntToBytes(this.result);
        }
        throw new UnsupportedOperationException("KDF can only be used when algorithm is known");
    }

    protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
        byte[] secret = engineGenerateSecret();
        if (sharedSecret.length - offset < secret.length) {
            throw new ShortBufferException(this.kaAlgorithm + " key agreement: need " + secret.length + " bytes");
        }
        System.arraycopy(secret, 0, sharedSecret, offset, secret.length);
        return secret.length;
    }

    protected SecretKey engineGenerateSecret(String algorithm) throws NoSuchAlgorithmException {
        byte[] secret = bigIntToBytes(this.result);
        String algKey = Strings.toUpperCase(algorithm);
        String oidAlgorithm = algorithm;
        if (oids.containsKey(algKey)) {
            oidAlgorithm = ((ASN1ObjectIdentifier) oids.get(algKey)).getId();
        }
        int keySize = getKeySize(oidAlgorithm);
        byte[] keyBytes;
        if (this.kdf != null) {
            if (keySize < 0) {
                throw new NoSuchAlgorithmException("unknown algorithm encountered: " + oidAlgorithm);
            }
            keyBytes = new byte[(keySize / 8)];
            this.kdf.init(new KDFParameters(secret, this.ukmParameters));
            this.kdf.generateBytes(keyBytes, 0, keyBytes.length);
            secret = keyBytes;
        } else if (keySize > 0) {
            keyBytes = new byte[(keySize / 8)];
            System.arraycopy(secret, 0, keyBytes, 0, keyBytes.length);
            secret = keyBytes;
        }
        if (des.containsKey(oidAlgorithm)) {
            DESParameters.setOddParity(secret);
        }
        return new SecretKeySpec(secret, getAlgorithm(algorithm));
    }
}
