package org.bouncycastle.jce.interfaces;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface IESKey extends Key {
    PrivateKey getPrivate();

    PublicKey getPublic();
}
