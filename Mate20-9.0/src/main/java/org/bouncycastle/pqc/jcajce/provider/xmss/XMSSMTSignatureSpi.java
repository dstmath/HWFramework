package org.bouncycastle.pqc.jcajce.provider.xmss;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTSigner;
import org.bouncycastle.pqc.jcajce.interfaces.StateAwareSignature;

public class XMSSMTSignatureSpi extends Signature implements StateAwareSignature {
    private Digest digest;
    private SecureRandom random;
    private XMSSMTSigner signer;
    private ASN1ObjectIdentifier treeDigest;

    public static class withSha256 extends XMSSMTSignatureSpi {
        public withSha256() {
            super("SHA256withXMSSMT", new SHA256Digest(), new XMSSMTSigner());
        }
    }

    public static class withSha512 extends XMSSMTSignatureSpi {
        public withSha512() {
            super("SHA512withXMSSMT", new SHA512Digest(), new XMSSMTSigner());
        }
    }

    public static class withShake128 extends XMSSMTSignatureSpi {
        public withShake128() {
            super("SHAKE128withXMSSMT", new SHAKEDigest(128), new XMSSMTSigner());
        }
    }

    public static class withShake256 extends XMSSMTSignatureSpi {
        public withShake256() {
            super("SHAKE256withXMSSMT", new SHAKEDigest(256), new XMSSMTSigner());
        }
    }

    protected XMSSMTSignatureSpi(String str) {
        super(str);
    }

    protected XMSSMTSignatureSpi(String str, Digest digest2, XMSSMTSigner xMSSMTSigner) {
        super(str);
        this.digest = digest2;
        this.signer = xMSSMTSigner;
    }

    /* access modifiers changed from: protected */
    public Object engineGetParameter(String str) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (privateKey instanceof BCXMSSMTPrivateKey) {
            BCXMSSMTPrivateKey bCXMSSMTPrivateKey = (BCXMSSMTPrivateKey) privateKey;
            CipherParameters keyParams = bCXMSSMTPrivateKey.getKeyParams();
            this.treeDigest = bCXMSSMTPrivateKey.getTreeDigestOID();
            CipherParameters parametersWithRandom = this.random != null ? new ParametersWithRandom(keyParams, this.random) : keyParams;
            this.digest.reset();
            this.signer.init(true, parametersWithRandom);
            return;
        }
        throw new InvalidKeyException("unknown private key passed to XMSSMT");
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey, SecureRandom secureRandom) throws InvalidKeyException {
        this.random = secureRandom;
        engineInitSign(privateKey);
    }

    /* access modifiers changed from: protected */
    public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof BCXMSSMTPublicKey) {
            CipherParameters keyParams = ((BCXMSSMTPublicKey) publicKey).getKeyParams();
            this.treeDigest = null;
            this.digest.reset();
            this.signer.init(false, keyParams);
            return;
        }
        throw new InvalidKeyException("unknown public key passed to XMSSMT");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(String str, Object obj) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(AlgorithmParameterSpec algorithmParameterSpec) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public byte[] engineSign() throws SignatureException {
        try {
            return this.signer.generateSignature(DigestUtil.getDigestResult(this.digest));
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw new SignatureException(e.getMessage());
            }
            throw new SignatureException(e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte b) throws SignatureException {
        this.digest.update(b);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] bArr, int i, int i2) throws SignatureException {
        this.digest.update(bArr, i, i2);
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] bArr) throws SignatureException {
        return this.signer.verifySignature(DigestUtil.getDigestResult(this.digest), bArr);
    }

    public PrivateKey getUpdatedPrivateKey() {
        if (this.treeDigest != null) {
            BCXMSSMTPrivateKey bCXMSSMTPrivateKey = new BCXMSSMTPrivateKey(this.treeDigest, (XMSSMTPrivateKeyParameters) this.signer.getUpdatedPrivateKey());
            this.treeDigest = null;
            return bCXMSSMTPrivateKey;
        }
        throw new IllegalStateException("signature object not in a signing state");
    }

    public boolean isSigningCapable() {
        return this.treeDigest != null;
    }
}
