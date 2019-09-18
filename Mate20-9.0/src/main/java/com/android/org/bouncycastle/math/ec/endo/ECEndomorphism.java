package com.android.org.bouncycastle.math.ec.endo;

import com.android.org.bouncycastle.math.ec.ECPointMap;

public interface ECEndomorphism {
    ECPointMap getPointMap();

    boolean hasEfficientPointMap();
}
