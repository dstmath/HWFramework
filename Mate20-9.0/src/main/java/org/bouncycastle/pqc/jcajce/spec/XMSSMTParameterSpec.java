package org.bouncycastle.pqc.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;

public class XMSSMTParameterSpec implements AlgorithmParameterSpec {
    public static final String SHA256 = "SHA256";
    public static final String SHA512 = "SHA512";
    public static final String SHAKE128 = "SHAKE128";
    public static final String SHAKE256 = "SHAKE256";
    private final int height;
    private final int layers;
    private final String treeDigest;

    public XMSSMTParameterSpec(int i, int i2, String str) {
        this.height = i;
        this.layers = i2;
        this.treeDigest = str;
    }

    public int getHeight() {
        return this.height;
    }

    public int getLayers() {
        return this.layers;
    }

    public String getTreeDigest() {
        return this.treeDigest;
    }
}
