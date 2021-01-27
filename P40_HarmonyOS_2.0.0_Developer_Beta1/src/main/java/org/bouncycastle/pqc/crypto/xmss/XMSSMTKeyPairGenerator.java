package org.bouncycastle.pqc.crypto.xmss;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;

public final class XMSSMTKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private XMSSMTParameters params;
    private SecureRandom prng;
    private XMSSParameters xmssParams;

    private XMSSMTPrivateKeyParameters generatePrivateKey(BDSStateMap bDSStateMap) {
        int treeDigestSize = this.params.getTreeDigestSize();
        byte[] bArr = new byte[treeDigestSize];
        this.prng.nextBytes(bArr);
        byte[] bArr2 = new byte[treeDigestSize];
        this.prng.nextBytes(bArr2);
        byte[] bArr3 = new byte[treeDigestSize];
        this.prng.nextBytes(bArr3);
        return new XMSSMTPrivateKeyParameters.Builder(this.params).withSecretKeySeed(bArr).withSecretKeyPRF(bArr2).withPublicSeed(bArr3).withBDSState(bDSStateMap).build();
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        XMSSMTPrivateKeyParameters generatePrivateKey = generatePrivateKey(new XMSSMTPrivateKeyParameters.Builder(this.params).build().getBDSState());
        this.xmssParams.getWOTSPlus().importKeys(new byte[this.params.getTreeDigestSize()], generatePrivateKey.getPublicSeed());
        int layers = this.params.getLayers() - 1;
        BDS bds = new BDS(this.xmssParams, generatePrivateKey.getPublicSeed(), generatePrivateKey.getSecretKeySeed(), (OTSHashAddress) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(layers)).build());
        XMSSNode root = bds.getRoot();
        generatePrivateKey.getBDSState().put(layers, bds);
        XMSSMTPrivateKeyParameters build = new XMSSMTPrivateKeyParameters.Builder(this.params).withSecretKeySeed(generatePrivateKey.getSecretKeySeed()).withSecretKeyPRF(generatePrivateKey.getSecretKeyPRF()).withPublicSeed(generatePrivateKey.getPublicSeed()).withRoot(root.getValue()).withBDSState(generatePrivateKey.getBDSState()).build();
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) new XMSSMTPublicKeyParameters.Builder(this.params).withRoot(root.getValue()).withPublicSeed(build.getPublicSeed()).build(), (AsymmetricKeyParameter) build);
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        XMSSMTKeyGenerationParameters xMSSMTKeyGenerationParameters = (XMSSMTKeyGenerationParameters) keyGenerationParameters;
        this.prng = xMSSMTKeyGenerationParameters.getRandom();
        this.params = xMSSMTKeyGenerationParameters.getParameters();
        this.xmssParams = this.params.getXMSSParameters();
    }
}
