package com.android.org.bouncycastle.jcajce.spec;

import com.android.org.bouncycastle.util.Arrays;
import javax.crypto.spec.IvParameterSpec;

public class AEADParameterSpec extends IvParameterSpec {
    private final byte[] associatedData;
    private final int macSizeInBits;

    public AEADParameterSpec(byte[] nonce, int macSizeInBits2) {
        this(nonce, macSizeInBits2, null);
    }

    public AEADParameterSpec(byte[] nonce, int macSizeInBits2, byte[] associatedData2) {
        super(nonce);
        this.macSizeInBits = macSizeInBits2;
        this.associatedData = Arrays.clone(associatedData2);
    }

    public int getMacSizeInBits() {
        return this.macSizeInBits;
    }

    public byte[] getAssociatedData() {
        return Arrays.clone(this.associatedData);
    }

    public byte[] getNonce() {
        return getIV();
    }
}
