package org.bouncycastle.jcajce.spec;

import java.security.spec.KeySpec;
import org.bouncycastle.util.Arrays;

public class TLSKeyMaterialSpec implements KeySpec {
    public static final String KEY_EXPANSION = "key expansion";
    public static final String MASTER_SECRET = "master secret";
    private final String label;
    private final int length;
    private final byte[] secret;
    private final byte[] seed;

    public TLSKeyMaterialSpec(byte[] bArr, String str, int i, byte[]... bArr2) {
        this.secret = Arrays.clone(bArr);
        this.label = str;
        this.length = i;
        this.seed = Arrays.concatenate(bArr2);
    }

    public String getLabel() {
        return this.label;
    }

    public int getLength() {
        return this.length;
    }

    public byte[] getSecret() {
        return Arrays.clone(this.secret);
    }

    public byte[] getSeed() {
        return Arrays.clone(this.seed);
    }
}
