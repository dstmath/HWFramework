package org.bouncycastle.crypto.params;

import java.security.SecureRandom;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class X25519KeyGenerationParameters extends KeyGenerationParameters {
    public X25519KeyGenerationParameters(SecureRandom secureRandom) {
        super(secureRandom, GF2Field.MASK);
    }
}
