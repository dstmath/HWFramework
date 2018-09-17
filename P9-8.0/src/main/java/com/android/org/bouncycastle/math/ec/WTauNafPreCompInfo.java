package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.math.ec.ECPoint.AbstractF2m;

public class WTauNafPreCompInfo implements PreCompInfo {
    protected AbstractF2m[] preComp = null;

    public AbstractF2m[] getPreComp() {
        return this.preComp;
    }

    public void setPreComp(AbstractF2m[] preComp) {
        this.preComp = preComp;
    }
}
