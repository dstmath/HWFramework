package com.android.org.bouncycastle.crypto.engines;

public class AESWrapEngine extends RFC3394WrapEngine {
    public AESWrapEngine() {
        super(new AESEngine());
    }

    public AESWrapEngine(boolean useReverseDirection) {
        super(new AESEngine(), useReverseDirection);
    }
}
