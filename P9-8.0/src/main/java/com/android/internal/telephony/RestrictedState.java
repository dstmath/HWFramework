package com.android.internal.telephony;

public class RestrictedState {
    private boolean mCsEmergencyRestricted;
    private boolean mCsNormalRestricted;
    private boolean mPsRestricted;

    public RestrictedState() {
        setPsRestricted(false);
        setCsNormalRestricted(false);
        setCsEmergencyRestricted(false);
    }

    public void setCsEmergencyRestricted(boolean csEmergencyRestricted) {
        this.mCsEmergencyRestricted = csEmergencyRestricted;
    }

    public boolean isCsEmergencyRestricted() {
        return this.mCsEmergencyRestricted;
    }

    public void setCsNormalRestricted(boolean csNormalRestricted) {
        this.mCsNormalRestricted = csNormalRestricted;
    }

    public boolean isCsNormalRestricted() {
        return this.mCsNormalRestricted;
    }

    public void setPsRestricted(boolean psRestricted) {
        this.mPsRestricted = psRestricted;
    }

    public boolean isPsRestricted() {
        return this.mPsRestricted;
    }

    public boolean isCsRestricted() {
        return this.mCsNormalRestricted ? this.mCsEmergencyRestricted : false;
    }

    public boolean isAnyCsRestricted() {
        return !this.mCsNormalRestricted ? this.mCsEmergencyRestricted : true;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            RestrictedState s = (RestrictedState) o;
            if (o == null) {
                return false;
            }
            if (this.mPsRestricted == s.mPsRestricted && this.mCsNormalRestricted == s.mCsNormalRestricted && this.mCsEmergencyRestricted == s.mCsEmergencyRestricted) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String csString = "none";
        if (this.mCsEmergencyRestricted && this.mCsNormalRestricted) {
            csString = "all";
        } else if (this.mCsEmergencyRestricted && (this.mCsNormalRestricted ^ 1) != 0) {
            csString = "emergency";
        } else if (!this.mCsEmergencyRestricted && this.mCsNormalRestricted) {
            csString = "normal call";
        }
        return "Restricted State CS: " + csString + " PS:" + this.mPsRestricted;
    }
}
