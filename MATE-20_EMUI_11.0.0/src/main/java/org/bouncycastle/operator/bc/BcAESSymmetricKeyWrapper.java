package org.bouncycastle.operator.bc;

import org.bouncycastle.crypto.engines.AESWrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;

public class BcAESSymmetricKeyWrapper extends BcSymmetricKeyWrapper {
    public BcAESSymmetricKeyWrapper(KeyParameter keyParameter) {
        super(AESUtil.determineKeyEncAlg(keyParameter), new AESWrapEngine(), keyParameter);
    }
}
