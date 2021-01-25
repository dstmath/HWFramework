package org.bouncycastle.jcajce.spec;

import java.security.spec.KeySpec;
import org.bouncycastle.util.Arrays;

public class ScryptKeySpec implements KeySpec {
    private final int blockSize;
    private final int costParameter;
    private final int keySize;
    private final int parallelizationParameter;
    private final char[] password;
    private final byte[] salt;

    public ScryptKeySpec(char[] cArr, byte[] bArr, int i, int i2, int i3, int i4) {
        this.password = cArr;
        this.salt = Arrays.clone(bArr);
        this.costParameter = i;
        this.blockSize = i2;
        this.parallelizationParameter = i3;
        this.keySize = i4;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getCostParameter() {
        return this.costParameter;
    }

    public int getKeyLength() {
        return this.keySize;
    }

    public int getParallelizationParameter() {
        return this.parallelizationParameter;
    }

    public char[] getPassword() {
        return this.password;
    }

    public byte[] getSalt() {
        return Arrays.clone(this.salt);
    }
}
