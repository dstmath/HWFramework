package org.bouncycastle.pqc.jcajce.provider;

import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;

public class McEliece {
    private static final String PREFIX = "org.bouncycastle.pqc.jcajce.provider.mceliece.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("KeyPairGenerator.McElieceKobaraImai", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyPairGeneratorSpi");
            configurableProvider.addAlgorithm("KeyPairGenerator.McEliecePointcheval", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyPairGeneratorSpi");
            configurableProvider.addAlgorithm("KeyPairGenerator.McElieceFujisaki", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyPairGeneratorSpi");
            configurableProvider.addAlgorithm("KeyPairGenerator.McEliece", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceKeyPairGeneratorSpi");
            configurableProvider.addAlgorithm("KeyPairGenerator.McEliece-CCA2", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyPairGeneratorSpi");
            configurableProvider.addAlgorithm("KeyFactory.McElieceKobaraImai", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi");
            configurableProvider.addAlgorithm("KeyFactory.McEliecePointcheval", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi");
            configurableProvider.addAlgorithm("KeyFactory.McElieceFujisaki", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi");
            configurableProvider.addAlgorithm("KeyFactory.McEliece", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceKeyFactorySpi");
            configurableProvider.addAlgorithm("KeyFactory.McEliece-CCA2", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi");
            configurableProvider.addAlgorithm("KeyFactory." + PQCObjectIdentifiers.mcElieceCca2, "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi");
            configurableProvider.addAlgorithm("KeyFactory." + PQCObjectIdentifiers.mcEliece, "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceKeyFactorySpi");
            configurableProvider.addAlgorithm("Cipher.McEliece", "org.bouncycastle.pqc.jcajce.provider.mceliece.McEliecePKCSCipherSpi$McEliecePKCS");
            configurableProvider.addAlgorithm("Cipher.McEliecePointcheval", "org.bouncycastle.pqc.jcajce.provider.mceliece.McEliecePointchevalCipherSpi$McEliecePointcheval");
            configurableProvider.addAlgorithm("Cipher.McElieceKobaraImai", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceKobaraImaiCipherSpi$McElieceKobaraImai");
            configurableProvider.addAlgorithm("Cipher.McElieceFujisaki", "org.bouncycastle.pqc.jcajce.provider.mceliece.McElieceFujisakiCipherSpi$McElieceFujisaki");
        }
    }
}
