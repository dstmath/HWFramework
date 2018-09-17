package android.telephony;

import com.android.internal.os.PowerProfile;

public abstract class CellSignalStrength {
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = new String[]{PowerProfile.POWER_NONE, "poor", "moderate", "good", "great"};
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;

    public abstract CellSignalStrength copy();

    public abstract boolean equals(Object obj);

    public abstract int getAsuLevel();

    public abstract int getDbm();

    public abstract int getLevel();

    public abstract int hashCode();

    public abstract void setDefaultValues();

    protected CellSignalStrength() {
    }
}
