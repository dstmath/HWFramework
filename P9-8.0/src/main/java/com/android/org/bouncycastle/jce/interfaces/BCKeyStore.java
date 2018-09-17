package com.android.org.bouncycastle.jce.interfaces;

import java.security.SecureRandom;

public interface BCKeyStore {
    void setRandom(SecureRandom secureRandom);
}
