package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.math.ec.ECPoint;

public class WTauNafPreCompInfo implements PreCompInfo {
    protected ECPoint.AbstractF2m[] preComp = null;

    public ECPoint.AbstractF2m[] getPreComp() {
        return this.preComp;
    }

    public void setPreComp(ECPoint.AbstractF2m[] preComp2) {
        this.preComp = preComp2;
    }
}
