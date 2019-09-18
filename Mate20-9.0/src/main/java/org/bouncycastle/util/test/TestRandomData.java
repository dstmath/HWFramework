package org.bouncycastle.util.test;

import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.test.FixedSecureRandom;

public class TestRandomData extends FixedSecureRandom {
    public TestRandomData(String str) {
        super(new FixedSecureRandom.Source[]{new FixedSecureRandom.Data(Hex.decode(str))});
    }

    public TestRandomData(byte[] bArr) {
        super(new FixedSecureRandom.Source[]{new FixedSecureRandom.Data(bArr)});
    }
}
