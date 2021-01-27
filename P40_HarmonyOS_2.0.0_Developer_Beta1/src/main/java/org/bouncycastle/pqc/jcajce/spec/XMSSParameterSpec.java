package org.bouncycastle.pqc.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;

public class XMSSParameterSpec implements AlgorithmParameterSpec {
    public static final String SHA256 = "SHA256";
    public static final XMSSParameterSpec SHA2_10_256 = new XMSSParameterSpec(10, "SHA256");
    public static final XMSSParameterSpec SHA2_10_512 = new XMSSParameterSpec(10, "SHA512");
    public static final XMSSParameterSpec SHA2_16_256 = new XMSSParameterSpec(16, "SHA256");
    public static final XMSSParameterSpec SHA2_16_512 = new XMSSParameterSpec(16, "SHA512");
    public static final XMSSParameterSpec SHA2_20_256 = new XMSSParameterSpec(20, "SHA256");
    public static final XMSSParameterSpec SHA2_20_512 = new XMSSParameterSpec(20, "SHA512");
    public static final String SHA512 = "SHA512";
    public static final String SHAKE128 = "SHAKE128";
    public static final String SHAKE256 = "SHAKE256";
    public static final XMSSParameterSpec SHAKE_10_256 = new XMSSParameterSpec(10, "SHAKE128");
    public static final XMSSParameterSpec SHAKE_10_512 = new XMSSParameterSpec(10, "SHAKE256");
    public static final XMSSParameterSpec SHAKE_16_256 = new XMSSParameterSpec(16, "SHAKE128");
    public static final XMSSParameterSpec SHAKE_16_512 = new XMSSParameterSpec(16, "SHAKE256");
    public static final XMSSParameterSpec SHAKE_20_256 = new XMSSParameterSpec(20, "SHAKE128");
    public static final XMSSParameterSpec SHAKE_20_512 = new XMSSParameterSpec(20, "SHAKE256");
    private final int height;
    private final String treeDigest;

    public XMSSParameterSpec(int i, String str) {
        this.height = i;
        this.treeDigest = str;
    }

    public int getHeight() {
        return this.height;
    }

    public String getTreeDigest() {
        return this.treeDigest;
    }
}
