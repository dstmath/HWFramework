package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DSA;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.digests.NullDigest;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.crypto.signers.ECDSASigner;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.DSABase;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.DSAEncoder;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class SignatureSpi extends DSABase {

    private static class StdDSAEncoder implements DSAEncoder {
        /* synthetic */ StdDSAEncoder(StdDSAEncoder -this0) {
            this();
        }

        private StdDSAEncoder() {
        }

        public byte[] encode(BigInteger r, BigInteger s) throws IOException {
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1Integer(r));
            v.add(new ASN1Integer(s));
            return new DERSequence(v).getEncoded(ASN1Encoding.DER);
        }

        public BigInteger[] decode(byte[] encoding) throws IOException {
            ASN1Sequence s = (ASN1Sequence) ASN1Primitive.fromByteArray(encoding);
            if (s.size() != 2) {
                throw new IOException("malformed signature");
            } else if (Arrays.areEqual(encoding, s.getEncoded(ASN1Encoding.DER))) {
                return new BigInteger[]{ASN1Integer.getInstance(s.getObjectAt(0)).getValue(), ASN1Integer.getInstance(s.getObjectAt(1)).getValue()};
            } else {
                throw new IOException("malformed signature");
            }
        }
    }

    public static class ecDSA224 extends SignatureSpi {
        public ecDSA224() {
            super(AndroidDigestFactory.getSHA224(), new ECDSASigner(), new StdDSAEncoder());
        }
    }

    public static class ecDSA256 extends SignatureSpi {
        public ecDSA256() {
            super(AndroidDigestFactory.getSHA256(), new ECDSASigner(), new StdDSAEncoder());
        }
    }

    public static class ecDSA384 extends SignatureSpi {
        public ecDSA384() {
            super(AndroidDigestFactory.getSHA384(), new ECDSASigner(), new StdDSAEncoder());
        }
    }

    public static class ecDSA512 extends SignatureSpi {
        public ecDSA512() {
            super(AndroidDigestFactory.getSHA512(), new ECDSASigner(), new StdDSAEncoder());
        }
    }

    public static class ecDSA extends SignatureSpi {
        public ecDSA() {
            super(AndroidDigestFactory.getSHA1(), new ECDSASigner(), new StdDSAEncoder());
        }
    }

    public static class ecDSAnone extends SignatureSpi {
        public ecDSAnone() {
            super(new NullDigest(), new ECDSASigner(), new StdDSAEncoder());
        }
    }

    SignatureSpi(Digest digest, DSA signer, DSAEncoder encoder) {
        super(digest, signer, encoder);
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        CipherParameters param = ECUtils.generatePublicKeyParameter(publicKey);
        this.digest.reset();
        this.signer.init(false, param);
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        CipherParameters param = ECUtil.generatePrivateKeyParameter(privateKey);
        this.digest.reset();
        if (this.appRandom != null) {
            this.signer.init(true, new ParametersWithRandom(param, this.appRandom));
        } else {
            this.signer.init(true, param);
        }
    }
}
