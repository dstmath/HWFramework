package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.util.Arrays;

public class SPHINCSPrivateKeyParameters extends SPHINCSKeyParameters {
    private final byte[] keyData;

    public SPHINCSPrivateKeyParameters(byte[] bArr) {
        super(true, null);
        this.keyData = Arrays.clone(bArr);
    }

    public SPHINCSPrivateKeyParameters(byte[] bArr, String str) {
        super(true, str);
        this.keyData = Arrays.clone(bArr);
    }

    public byte[] getKeyData() {
        return Arrays.clone(this.keyData);
    }
}
