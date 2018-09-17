package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;

class ReasonsMask {
    static final ReasonsMask allReasons = new ReasonsMask(33023);
    private int _reasons;

    ReasonsMask(ReasonFlags reasons) {
        this._reasons = reasons.intValue();
    }

    private ReasonsMask(int reasons) {
        this._reasons = reasons;
    }

    ReasonsMask() {
        this(0);
    }

    void addReasons(ReasonsMask mask) {
        this._reasons |= mask.getReasons();
    }

    boolean isAllReasons() {
        return this._reasons == allReasons._reasons;
    }

    ReasonsMask intersect(ReasonsMask mask) {
        ReasonsMask _mask = new ReasonsMask();
        _mask.addReasons(new ReasonsMask(this._reasons & mask.getReasons()));
        return _mask;
    }

    boolean hasNewReasons(ReasonsMask mask) {
        return (this._reasons | (mask.getReasons() ^ this._reasons)) != 0;
    }

    int getReasons() {
        return this._reasons;
    }
}
