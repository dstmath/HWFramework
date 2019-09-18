package com.android.org.bouncycastle.math.ec.endo;

import java.math.BigInteger;

public interface GLVEndomorphism extends ECEndomorphism {
    BigInteger[] decomposeScalar(BigInteger bigInteger);
}
