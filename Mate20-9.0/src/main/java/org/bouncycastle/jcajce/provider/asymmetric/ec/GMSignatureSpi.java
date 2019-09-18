package org.bouncycastle.jcajce.provider.asymmetric.ec;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;

public class GMSignatureSpi extends SignatureSpi {
    private AlgorithmParameters engineParams;
    private final JcaJceHelper helper = new BCJcaJceHelper();
    private SM2ParameterSpec paramSpec;
    private final SM2Signer signer;

    public static class sm3WithSM2 extends GMSignatureSpi {
        public sm3WithSM2() {
            super(new SM2Signer());
        }
    }

    GMSignatureSpi(SM2Signer sM2Signer) {
        this.signer = sM2Signer;
    }

    /* access modifiers changed from: protected */
    public Object engineGetParameter(String str) {
        throw new UnsupportedOperationException("engineGetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        if (this.engineParams == null && this.paramSpec != null) {
            try {
                this.engineParams = this.helper.createAlgorithmParameters("PSS");
                this.engineParams.init(this.paramSpec);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
        }
        return this.engineParams;
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        CipherParameters generatePrivateKeyParameter = ECUtil.generatePrivateKeyParameter(privateKey);
        if (this.appRandom != null) {
            generatePrivateKeyParameter = new ParametersWithRandom(generatePrivateKeyParameter, this.appRandom);
        }
        if (this.paramSpec != null) {
            this.signer.init(true, new ParametersWithID(generatePrivateKeyParameter, this.paramSpec.getID()));
        } else {
            this.signer.init(true, generatePrivateKeyParameter);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        CipherParameters generatePublicKeyParameter = ECUtils.generatePublicKeyParameter(publicKey);
        if (this.paramSpec != null) {
            generatePublicKeyParameter = new ParametersWithID(generatePublicKeyParameter, this.paramSpec.getID());
        }
        this.signer.init(false, generatePublicKeyParameter);
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(String str, Object obj) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException {
        if (algorithmParameterSpec instanceof SM2ParameterSpec) {
            this.paramSpec = (SM2ParameterSpec) algorithmParameterSpec;
            return;
        }
        throw new InvalidAlgorithmParameterException("only SM2ParameterSpec supported");
    }

    /* access modifiers changed from: protected */
    public byte[] engineSign() throws SignatureException {
        try {
            return this.signer.generateSignature();
        } catch (CryptoException e) {
            throw new SignatureException("unable to create signature: " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte b) throws SignatureException {
        this.signer.update(b);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] bArr, int i, int i2) throws SignatureException {
        this.signer.update(bArr, i, i2);
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] bArr) throws SignatureException {
        return this.signer.verifySignature(bArr);
    }
}
