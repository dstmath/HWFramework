package com.android.org.bouncycastle.jce.interfaces;

import com.android.org.bouncycastle.math.ec.ECPoint;
import java.security.PublicKey;

public interface ECPublicKey extends ECKey, PublicKey {
    ECPoint getQ();
}
