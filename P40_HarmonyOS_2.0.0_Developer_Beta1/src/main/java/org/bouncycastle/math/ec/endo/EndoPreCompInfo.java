package org.bouncycastle.math.ec.endo;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.PreCompInfo;

public class EndoPreCompInfo implements PreCompInfo {
    protected ECEndomorphism endomorphism;
    protected ECPoint mappedPoint;

    public ECEndomorphism getEndomorphism() {
        return this.endomorphism;
    }

    public ECPoint getMappedPoint() {
        return this.mappedPoint;
    }

    public void setEndomorphism(ECEndomorphism eCEndomorphism) {
        this.endomorphism = eCEndomorphism;
    }

    public void setMappedPoint(ECPoint eCPoint) {
        this.mappedPoint = eCPoint;
    }
}
