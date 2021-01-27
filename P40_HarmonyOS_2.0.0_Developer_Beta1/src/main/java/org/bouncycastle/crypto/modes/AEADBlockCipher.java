package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;

public interface AEADBlockCipher extends AEADCipher {
    BlockCipher getUnderlyingCipher();
}
