package org.bouncycastle.pqc.crypto.sphincs;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.sphincs.Tree;

public class SPHINCS256KeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private SecureRandom random;
    private Digest treeDigest;

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        Tree.leafaddr leafaddr = new Tree.leafaddr();
        byte[] bArr = new byte[1088];
        this.random.nextBytes(bArr);
        byte[] bArr2 = new byte[1056];
        System.arraycopy(bArr, 32, bArr2, 0, 1024);
        leafaddr.level = 11;
        leafaddr.subtree = 0;
        leafaddr.subleaf = 0;
        Tree.treehash(new HashFunctions(this.treeDigest), bArr2, 1024, 5, bArr, leafaddr, bArr2, 0);
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) new SPHINCSPublicKeyParameters(bArr2, this.treeDigest.getAlgorithmName()), (AsymmetricKeyParameter) new SPHINCSPrivateKeyParameters(bArr, this.treeDigest.getAlgorithmName()));
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.random = keyGenerationParameters.getRandom();
        this.treeDigest = ((SPHINCS256KeyGenerationParameters) keyGenerationParameters).getTreeDigest();
    }
}
