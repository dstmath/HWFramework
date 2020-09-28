package android.telephony;

import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.os.SystemProperties;

public abstract class CellSignalStrength {
    protected static final boolean FEATURE_VALIDATEINPUT = SystemProperties.getBoolean("ro.SignalStrength.ValidateInput", false);
    protected static final int INVALID_PHONEID = -1;
    public static final int NUM_SIGNAL_STRENGTH_BINS = 6;
    protected static final int NUM_SIGNAL_STRENGTH_THRESHOLDS = 5;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = {"none", "poor", "moderate", "good", "great"};
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    protected int mPhoneId;

    public abstract CellSignalStrength copy();

    public abstract boolean equals(Object obj);

    public abstract int getAsuLevel();

    public abstract int getDbm();

    public abstract int getLevel();

    public abstract int hashCode();

    public abstract boolean isValid();

    public abstract void setDefaultValues();

    public abstract void updateLevel(PersistableBundle persistableBundle, ServiceState serviceState);

    protected CellSignalStrength() {
    }

    protected static final int getRssiDbmFromAsu(int asu) {
        if (asu > 31 || asu < 0) {
            return Integer.MAX_VALUE;
        }
        return (asu * 2) - 113;
    }

    protected static final int getAsuFromRssiDbm(int dbm) {
        if (dbm == Integer.MAX_VALUE) {
            return 99;
        }
        return (dbm + 113) / 2;
    }

    protected static final int getRscpDbmFromAsu(int asu) {
        if (asu > 96 || asu < 0) {
            return Integer.MAX_VALUE;
        }
        return asu + PackageManager.INSTALL_FAILED_MULTIPACKAGE_INCONSISTENCY;
    }

    protected static final int getAsuFromRscpDbm(int dbm) {
        if (dbm == Integer.MAX_VALUE) {
            return 255;
        }
        return dbm + 120;
    }

    protected static final int getEcNoDbFromAsu(int asu) {
        if (asu > 49 || asu < 0) {
            return Integer.MAX_VALUE;
        }
        return (asu / 2) - 24;
    }

    protected static final int inRangeOrUnavailable(int value, int rangeMin, int rangeMax) {
        return value;
    }

    protected static final int inRangeOrUnavailable(int value, int rangeMin, int rangeMax, int special) {
        return value;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public int getLevelHw() {
        return 0;
    }
}
