package org.bouncycastle.jce.interfaces;

import java.security.PublicKey;
import org.bouncycastle.math.ec.ECPoint;

public interface ECPublicKey extends ECKey, PublicKey {
    ECPoint getQ();
}
