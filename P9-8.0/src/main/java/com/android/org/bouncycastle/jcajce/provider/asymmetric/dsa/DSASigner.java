package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DSA;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.digests.NullDigest;
import com.android.org.bouncycastle.crypto.params.DSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;

public class DSASigner extends SignatureSpi implements PKCSObjectIdentifiers, X509ObjectIdentifiers {
    private Digest digest;
    private SecureRandom random;
    private DSA signer;

    public static class dsa224 extends DSASigner {
        public dsa224() {
            super(AndroidDigestFactory.getSHA224(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    public static class dsa256 extends DSASigner {
        public dsa256() {
            super(AndroidDigestFactory.getSHA256(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    public static class noneDSA extends DSASigner {
        public noneDSA() {
            super(new NullDigest(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    public static class stdDSA extends DSASigner {
        public stdDSA() {
            super(AndroidDigestFactory.getSHA1(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    protected DSASigner(Digest digest, DSA signer) {
        this.digest = digest;
        this.signer = signer;
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        CipherParameters param = DSAUtil.generatePublicKeyParameter(publicKey);
        this.digest.reset();
        this.signer.init(false, param);
    }

    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        this.random = random;
        engineInitSign(privateKey);
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        CipherParameters param = DSAUtil.generatePrivateKeyParameter(privateKey);
        checkKey(((DSAKeyParameters) param).getParameters());
        if (this.random != null) {
            param = new ParametersWithRandom(param, this.random);
        }
        this.digest.reset();
        this.signer.init(true, param);
    }

    protected void engineUpdate(byte b) throws SignatureException {
        this.digest.update(b);
    }

    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.digest.update(b, off, len);
    }

    protected byte[] engineSign() throws SignatureException {
        byte[] hash = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(hash, 0);
        try {
            BigInteger[] sig = this.signer.generateSignature(hash);
            return derEncode(sig[0], sig[1]);
        } catch (Exception e) {
            throw new SignatureException(e.toString());
        }
    }

    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        byte[] hash = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(hash, 0);
        try {
            BigInteger[] sig = derDecode(sigBytes);
            return this.signer.verifySignature(hash, sig[0], sig[1]);
        } catch (Exception e) {
            throw new SignatureException("error decoding signature bytes.");
        }
    }

    protected void engineSetParameter(AlgorithmParameterSpec params) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    protected void checkKey(DSAParameters params) throws InvalidKeyException {
        int valueL = params.getP().bitLength();
        int valueN = params.getQ().bitLength();
        int digestSize = this.digest.getDigestSize();
        if (valueL < 1024 || valueL > 3072 || valueL % 1024 != 0) {
            throw new InvalidKeyException("valueL values must be between 1024 and 3072 and a multiple of 1024");
        } else if (valueL == 1024 && valueN != 160) {
            throw new InvalidKeyException("valueN must be 160 for valueL = 1024");
        } else if (valueL == 2048 && valueN != 224 && valueN != 256) {
            throw new InvalidKeyException("valueN must be 224 or 256 for valueL = 2048");
        } else if (valueL == 3072 && valueN != 256) {
            throw new InvalidKeyException("valueN must be 256 for valueL = 3072");
        } else if (!(this.digest instanceof NullDigest) && valueN > digestSize * 8) {
            throw new InvalidKeyException("Key is too strong for this signature algorithm");
        }
    }

    protected void engineSetParameter(String param, Object value) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    protected Object engineGetParameter(String param) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    private byte[] derEncode(BigInteger r, BigInteger s) throws IOException {
        return new DERSequence(new ASN1Integer[]{new ASN1Integer(r), new ASN1Integer(s)}).getEncoded(ASN1Encoding.DER);
    }

    private BigInteger[] derDecode(byte[] encoding) throws IOException {
        ASN1Sequence s = (ASN1Sequence) ASN1Primitive.fromByteArray(encoding);
        if (s.size() != 2) {
            throw new IOException("malformed signature");
        } else if (Arrays.areEqual(encoding, s.getEncoded(ASN1Encoding.DER))) {
            return new BigInteger[]{((ASN1Integer) s.getObjectAt(0)).getValue(), ((ASN1Integer) s.getObjectAt(1)).getValue()};
        } else {
            throw new IOException("malformed signature");
        }
    }
}
