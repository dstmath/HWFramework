package org.bouncycastle.pqc.jcajce.interfaces;

import java.security.PrivateKey;

public interface LMSPrivateKey extends LMSKey, PrivateKey {
    LMSPrivateKey extractKeyShard(int i);

    long getIndex();

    long getUsagesRemaining();
}
