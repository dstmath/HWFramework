package com.android.server.wifi.util;

public class KalmanFilter {
    public Matrix mF;
    public Matrix mH;
    public Matrix mP;
    public Matrix mQ;
    public Matrix mR;
    public Matrix mx;

    public void predict() {
        this.mx = this.mF.dot(this.mx);
        this.mP = this.mF.dot(this.mP).dotTranspose(this.mF).plus(this.mQ);
    }

    public void update(Matrix z) {
        Matrix y = z.minus(this.mH.dot(this.mx));
        Matrix tK = this.mP.dotTranspose(this.mH).dot(this.mH.dot(this.mP).dotTranspose(this.mH).plus(this.mR).inverse());
        this.mx = this.mx.plus(tK.dot(y));
        this.mP = this.mP.minus(tK.dot(this.mH).dot(this.mP));
    }

    public String toString() {
        return "{F: " + this.mF + " Q: " + this.mQ + " H: " + this.mH + " R: " + this.mR + " P: " + this.mP + " x: " + this.mx + "}";
    }
}
