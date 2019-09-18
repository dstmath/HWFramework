package org.bouncycastle.pqc.crypto.mceliece;

import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;

public class McElieceCCA2Parameters extends McElieceParameters {
    private final String digest;

    public McElieceCCA2Parameters() {
        this(11, 50, McElieceCCA2KeyGenParameterSpec.SHA256);
    }

    public McElieceCCA2Parameters(int i) {
        this(i, McElieceCCA2KeyGenParameterSpec.SHA256);
    }

    public McElieceCCA2Parameters(int i, int i2) {
        this(i, i2, McElieceCCA2KeyGenParameterSpec.SHA256);
    }

    public McElieceCCA2Parameters(int i, int i2, int i3) {
        this(i, i2, i3, McElieceCCA2KeyGenParameterSpec.SHA256);
    }

    public McElieceCCA2Parameters(int i, int i2, int i3, String str) {
        super(i, i2, i3);
        this.digest = str;
    }

    public McElieceCCA2Parameters(int i, int i2, String str) {
        super(i, i2);
        this.digest = str;
    }

    public McElieceCCA2Parameters(int i, String str) {
        super(i);
        this.digest = str;
    }

    public McElieceCCA2Parameters(String str) {
        this(11, 50, str);
    }

    public String getDigest() {
        return this.digest;
    }
}
