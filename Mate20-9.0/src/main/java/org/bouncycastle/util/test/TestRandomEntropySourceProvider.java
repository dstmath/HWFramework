package org.bouncycastle.util.test;

import java.security.SecureRandom;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.crypto.prng.EntropySourceProvider;

public class TestRandomEntropySourceProvider implements EntropySourceProvider {
    /* access modifiers changed from: private */
    public final boolean _predictionResistant;
    /* access modifiers changed from: private */
    public final SecureRandom _sr = new SecureRandom();

    public TestRandomEntropySourceProvider(boolean z) {
        this._predictionResistant = z;
    }

    public EntropySource get(final int i) {
        return new EntropySource() {
            public int entropySize() {
                return i;
            }

            public byte[] getEntropy() {
                byte[] bArr = new byte[((i + 7) / 8)];
                TestRandomEntropySourceProvider.this._sr.nextBytes(bArr);
                return bArr;
            }

            public boolean isPredictionResistant() {
                return TestRandomEntropySourceProvider.this._predictionResistant;
            }
        };
    }
}
