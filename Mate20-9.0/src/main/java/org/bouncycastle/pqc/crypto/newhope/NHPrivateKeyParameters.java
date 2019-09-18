package org.bouncycastle.pqc.crypto.newhope;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.Arrays;

public class NHPrivateKeyParameters extends AsymmetricKeyParameter {
    final short[] secData;

    public NHPrivateKeyParameters(short[] sArr) {
        super(true);
        this.secData = Arrays.clone(sArr);
    }

    public short[] getSecData() {
        return Arrays.clone(this.secData);
    }
}
