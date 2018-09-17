package android.location;

@Deprecated
public final class GpsSatellite {
    float mAzimuth;
    float mElevation;
    boolean mHasAlmanac;
    boolean mHasEphemeris;
    int mPrn;
    float mSnr;
    boolean mUsedInFix;
    boolean mValid;

    GpsSatellite(int prn) {
        this.mPrn = prn;
    }

    void setStatus(GpsSatellite satellite) {
        if (satellite == null) {
            this.mValid = false;
            return;
        }
        this.mValid = satellite.mValid;
        this.mHasEphemeris = satellite.mHasEphemeris;
        this.mHasAlmanac = satellite.mHasAlmanac;
        this.mUsedInFix = satellite.mUsedInFix;
        this.mSnr = satellite.mSnr;
        this.mElevation = satellite.mElevation;
        this.mAzimuth = satellite.mAzimuth;
    }

    public int getPrn() {
        return this.mPrn;
    }

    public float getSnr() {
        return this.mSnr;
    }

    public float getElevation() {
        return this.mElevation;
    }

    public float getAzimuth() {
        return this.mAzimuth;
    }

    public boolean hasEphemeris() {
        return this.mHasEphemeris;
    }

    public boolean hasAlmanac() {
        return this.mHasAlmanac;
    }

    public boolean usedInFix() {
        return this.mUsedInFix;
    }
}
