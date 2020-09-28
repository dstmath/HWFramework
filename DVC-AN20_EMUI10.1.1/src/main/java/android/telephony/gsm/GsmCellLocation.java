package android.telephony.gsm;

import android.annotation.UnsupportedAppUsage;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.CellLocation;

public class GsmCellLocation extends CellLocation {
    private int mCid;
    private int mLac;
    private long mNci;
    private int mPsc;

    public GsmCellLocation() {
        this.mLac = -1;
        this.mCid = -1;
        this.mPsc = -1;
        this.mNci = -1;
    }

    public GsmCellLocation(Bundle bundle) {
        this.mLac = bundle.getInt(Telephony.CellBroadcasts.LAC, -1);
        this.mCid = bundle.getInt("cid", -1);
        this.mPsc = bundle.getInt("psc", -1);
        this.mNci = bundle.getLong("nci", -1);
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int getPsc() {
        return this.mPsc;
    }

    @Override // android.telephony.CellLocation
    public void setStateInvalid() {
        this.mLac = -1;
        this.mCid = -1;
        this.mPsc = -1;
        this.mNci = -1;
    }

    public void setLacAndCid(int lac, int cid) {
        this.mLac = lac;
        this.mCid = cid;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void setPsc(int psc) {
        this.mPsc = psc;
    }

    public void setNrLacAndCid(int lac, long nci) {
        this.mLac = lac;
        this.mNci = nci;
    }

    public long getHwCid() {
        int i = this.mCid;
        if (i != -1) {
            return (long) i;
        }
        return this.mNci;
    }

    public int hashCode() {
        return this.mLac ^ this.mCid;
    }

    public boolean equals(Object o) {
        try {
            GsmCellLocation s = (GsmCellLocation) o;
            if (o != null && equalsHandlesNulls(Integer.valueOf(this.mLac), Integer.valueOf(s.mLac)) && equalsHandlesNulls(Integer.valueOf(this.mCid), Integer.valueOf(s.mCid)) && equalsHandlesNulls(Integer.valueOf(this.mPsc), Integer.valueOf(s.mPsc)) && equalsHandlesNulls(Long.valueOf(this.mNci), Long.valueOf(s.mNci))) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "GsmCellLocation ****";
    }

    private static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    @Override // android.telephony.CellLocation
    public void fillInNotifierBundle(Bundle m) {
        m.putInt(Telephony.CellBroadcasts.LAC, this.mLac);
        m.putInt("cid", this.mCid);
        m.putInt("psc", this.mPsc);
        m.putLong("nci", this.mNci);
    }

    @Override // android.telephony.CellLocation
    public boolean isEmpty() {
        return this.mLac == -1 && this.mCid == -1 && this.mPsc == -1 && this.mNci == -1;
    }

    public boolean isNotLacEquals(GsmCellLocation mCellLoc) {
        if (mCellLoc == null || this.mLac != mCellLoc.mLac) {
            return true;
        }
        return false;
    }
}
