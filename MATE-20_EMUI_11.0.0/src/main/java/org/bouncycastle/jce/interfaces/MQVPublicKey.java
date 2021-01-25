package org.bouncycastle.jce.interfaces;

import java.security.PublicKey;

public interface MQVPublicKey extends PublicKey {
    PublicKey getEphemeralKey();

    PublicKey getStaticKey();
}
