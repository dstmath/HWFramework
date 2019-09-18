package org.bouncycastle.pqc.jcajce.provider.xmss;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTKeyPairGenerator;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;
import org.bouncycastle.pqc.jcajce.spec.XMSSMTParameterSpec;

public class XMSSMTKeyPairGeneratorSpi extends KeyPairGenerator {
    private XMSSMTKeyPairGenerator engine = new XMSSMTKeyPairGenerator();
    private boolean initialised = false;
    private XMSSMTKeyGenerationParameters param;
    private SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    private ASN1ObjectIdentifier treeDigest;

    public XMSSMTKeyPairGeneratorSpi() {
        super("XMSSMT");
    }

    public KeyPair generateKeyPair() {
        if (!this.initialised) {
            this.param = new XMSSMTKeyGenerationParameters(new XMSSMTParameters(10, 20, new SHA512Digest()), this.random);
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
        return new KeyPair(new BCXMSSMTPublicKey(this.treeDigest, (XMSSMTPublicKeyParameters) generateKeyPair.getPublic()), new BCXMSSMTPrivateKey(this.treeDigest, (XMSSMTPrivateKeyParameters) generateKeyPair.getPrivate()));
    }

    public void initialize(int i, SecureRandom secureRandom) {
        throw new IllegalArgumentException("use AlgorithmParameterSpec");
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        XMSSMTKeyGenerationParameters xMSSMTKeyGenerationParameters;
        if (algorithmParameterSpec instanceof XMSSMTParameterSpec) {
            XMSSMTParameterSpec xMSSMTParameterSpec = (XMSSMTParameterSpec) algorithmParameterSpec;
            if (xMSSMTParameterSpec.getTreeDigest().equals("SHA256")) {
                this.treeDigest = NISTObjectIdentifiers.id_sha256;
                xMSSMTKeyGenerationParameters = new XMSSMTKeyGenerationParameters(new XMSSMTParameters(xMSSMTParameterSpec.getHeight(), xMSSMTParameterSpec.getLayers(), new SHA256Digest()), secureRandom);
            } else if (xMSSMTParameterSpec.getTreeDigest().equals("SHA512")) {
                this.treeDigest = NISTObjectIdentifiers.id_sha512;
                xMSSMTKeyGenerationParameters = new XMSSMTKeyGenerationParameters(new XMSSMTParameters(xMSSMTParameterSpec.getHeight(), xMSSMTParameterSpec.getLayers(), new SHA512Digest()), secureRandom);
            } else if (xMSSMTParameterSpec.getTreeDigest().equals("SHAKE128")) {
                this.treeDigest = NISTObjectIdentifiers.id_shake128;
                xMSSMTKeyGenerationParameters = new XMSSMTKeyGenerationParameters(new XMSSMTParameters(xMSSMTParameterSpec.getHeight(), xMSSMTParameterSpec.getLayers(), new SHAKEDigest(128)), secureRandom);
            } else {
                if (xMSSMTParameterSpec.getTreeDigest().equals("SHAKE256")) {
                    this.treeDigest = NISTObjectIdentifiers.id_shake256;
                    xMSSMTKeyGenerationParameters = new XMSSMTKeyGenerationParameters(new XMSSMTParameters(xMSSMTParameterSpec.getHeight(), xMSSMTParameterSpec.getLayers(), new SHAKEDigest(256)), secureRandom);
                }
                this.engine.init(this.param);
                this.initialised = true;
                return;
            }
            this.param = xMSSMTKeyGenerationParameters;
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a XMSSMTParameterSpec");
    }
}
