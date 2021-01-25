package org.bouncycastle.jcajce.provider.symmetric;

import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AlgorithmProvider;

abstract class SymmetricAlgorithmProvider extends AlgorithmProvider {
    SymmetricAlgorithmProvider() {
    }

    /* access modifiers changed from: protected */
    public void addCMacAlgorithm(ConfigurableProvider configurableProvider, String str, String str2, String str3) {
        configurableProvider.addAlgorithm("Mac." + str + "-CMAC", str2);
        configurableProvider.addAlgorithm("Alg.Alias.Mac." + str + "CMAC", str + "-CMAC");
        configurableProvider.addAlgorithm("KeyGenerator." + str + "-CMAC", str3);
        configurableProvider.addAlgorithm("Alg.Alias.KeyGenerator." + str + "CMAC", str + "-CMAC");
    }

    /* access modifiers changed from: protected */
    public void addGMacAlgorithm(ConfigurableProvider configurableProvider, String str, String str2, String str3) {
        configurableProvider.addAlgorithm("Mac." + str + "-GMAC", str2);
        configurableProvider.addAlgorithm("Alg.Alias.Mac." + str + "GMAC", str + "-GMAC");
        configurableProvider.addAlgorithm("KeyGenerator." + str + "-GMAC", str3);
        configurableProvider.addAlgorithm("Alg.Alias.KeyGenerator." + str + "GMAC", str + "-GMAC");
    }

    /* access modifiers changed from: protected */
    public void addPoly1305Algorithm(ConfigurableProvider configurableProvider, String str, String str2, String str3) {
        configurableProvider.addAlgorithm("Mac.POLY1305-" + str, str2);
        configurableProvider.addAlgorithm("Alg.Alias.Mac.POLY1305" + str, "POLY1305-" + str);
        configurableProvider.addAlgorithm("KeyGenerator.POLY1305-" + str, str3);
        configurableProvider.addAlgorithm("Alg.Alias.KeyGenerator.POLY1305" + str, "POLY1305-" + str);
    }
}
