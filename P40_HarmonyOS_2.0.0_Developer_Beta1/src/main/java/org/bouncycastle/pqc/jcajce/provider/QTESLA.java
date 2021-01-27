package org.bouncycastle.pqc.jcajce.provider;

import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.jcajce.provider.qtesla.QTESLAKeyFactorySpi;

public class QTESLA {
    private static final String PREFIX = "org.bouncycastle.pqc.jcajce.provider.qtesla.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        @Override // org.bouncycastle.jcajce.provider.util.AlgorithmProvider
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("KeyFactory.QTESLA", "org.bouncycastle.pqc.jcajce.provider.qtesla.QTESLAKeyFactorySpi");
            configurableProvider.addAlgorithm("KeyPairGenerator.QTESLA", "org.bouncycastle.pqc.jcajce.provider.qtesla.KeyPairGeneratorSpi");
            configurableProvider.addAlgorithm("Signature.QTESLA", "org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi$qTESLA");
            addSignatureAlgorithm(configurableProvider, "QTESLA-P-I", "org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi$PI", PQCObjectIdentifiers.qTESLA_p_I);
            addSignatureAlgorithm(configurableProvider, "QTESLA-P-III", "org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi$PIII", PQCObjectIdentifiers.qTESLA_p_III);
            QTESLAKeyFactorySpi qTESLAKeyFactorySpi = new QTESLAKeyFactorySpi();
            registerOid(configurableProvider, PQCObjectIdentifiers.qTESLA_p_I, "QTESLA-P-I", qTESLAKeyFactorySpi);
            registerOid(configurableProvider, PQCObjectIdentifiers.qTESLA_p_III, "QTESLA-P-III", qTESLAKeyFactorySpi);
        }
    }
}
