package org.bouncycastle.pqc.jcajce.provider.sphincs;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512tDigest;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCS256KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCS256KeyPairGenerator;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.bouncycastle.pqc.jcajce.spec.SPHINCS256KeyGenParameterSpec;

public class Sphincs256KeyPairGeneratorSpi extends KeyPairGenerator {
    SPHINCS256KeyPairGenerator engine = new SPHINCS256KeyPairGenerator();
    boolean initialised = false;
    SPHINCS256KeyGenerationParameters param;
    SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    ASN1ObjectIdentifier treeDigest = NISTObjectIdentifiers.id_sha512_256;

    public Sphincs256KeyPairGeneratorSpi() {
        super("SPHINCS256");
    }

    public KeyPair generateKeyPair() {
        if (!this.initialised) {
            this.param = new SPHINCS256KeyGenerationParameters(this.random, new SHA512tDigest(256));
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
        return new KeyPair(new BCSphincs256PublicKey(this.treeDigest, (SPHINCSPublicKeyParameters) generateKeyPair.getPublic()), new BCSphincs256PrivateKey(this.treeDigest, (SPHINCSPrivateKeyParameters) generateKeyPair.getPrivate()));
    }

    public void initialize(int i, SecureRandom secureRandom) {
        throw new IllegalArgumentException("use AlgorithmParameterSpec");
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        SPHINCS256KeyGenerationParameters sPHINCS256KeyGenerationParameters;
        if (algorithmParameterSpec instanceof SPHINCS256KeyGenParameterSpec) {
            SPHINCS256KeyGenParameterSpec sPHINCS256KeyGenParameterSpec = (SPHINCS256KeyGenParameterSpec) algorithmParameterSpec;
            if (sPHINCS256KeyGenParameterSpec.getTreeDigest().equals(SPHINCS256KeyGenParameterSpec.SHA512_256)) {
                this.treeDigest = NISTObjectIdentifiers.id_sha512_256;
                sPHINCS256KeyGenerationParameters = new SPHINCS256KeyGenerationParameters(secureRandom, new SHA512tDigest(256));
            } else {
                if (sPHINCS256KeyGenParameterSpec.getTreeDigest().equals(SPHINCS256KeyGenParameterSpec.SHA3_256)) {
                    this.treeDigest = NISTObjectIdentifiers.id_sha3_256;
                    sPHINCS256KeyGenerationParameters = new SPHINCS256KeyGenerationParameters(secureRandom, new SHA3Digest(256));
                }
                this.engine.init(this.param);
                this.initialised = true;
                return;
            }
            this.param = sPHINCS256KeyGenerationParameters;
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a SPHINCS256KeyGenParameterSpec");
    }
}
