package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DESParameters;

public class DESKeyGenerator extends CipherKeyGenerator {
    public void init(KeyGenerationParameters param) {
        super.init(param);
        if (this.strength == 0 || this.strength == 7) {
            this.strength = 8;
        } else if (this.strength != 8) {
            throw new IllegalArgumentException("DES key must be 64 bits long.");
        }
    }

    public byte[] generateKey() {
        byte[] newKey = new byte[8];
        do {
            this.random.nextBytes(newKey);
            DESParameters.setOddParity(newKey);
        } while (DESParameters.isWeakKey(newKey, 0));
        return newKey;
    }
}
