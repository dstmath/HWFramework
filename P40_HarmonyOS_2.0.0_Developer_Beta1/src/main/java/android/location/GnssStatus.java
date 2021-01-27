package android.location;

import com.android.internal.telephony.IccCardConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class GnssStatus {
    public static final int CONSTELLATION_BEIDOU = 5;
    public static final int CONSTELLATION_COUNT = 8;
    public static final int CONSTELLATION_GALILEO = 6;
    public static final int CONSTELLATION_GLONASS = 3;
    public static final int CONSTELLATION_GPS = 1;
    public static final int CONSTELLATION_IRNSS = 7;
    public static final int CONSTELLATION_QZSS = 4;
    public static final int CONSTELLATION_SBAS = 2;
    public static final int CONSTELLATION_TYPE_MASK = 15;
    public static final int CONSTELLATION_TYPE_SHIFT_WIDTH = 4;
    public static final int CONSTELLATION_UNKNOWN = 0;
    public static final int GNSS_SV_FLAGS_HAS_ALMANAC_DATA = 2;
    public static final int GNSS_SV_FLAGS_HAS_CARRIER_FREQUENCY = 8;
    public static final int GNSS_SV_FLAGS_HAS_EPHEMERIS_DATA = 1;
    public static final int GNSS_SV_FLAGS_NONE = 0;
    public static final int GNSS_SV_FLAGS_USED_IN_FIX = 4;
    public static final int SVID_SHIFT_WIDTH = 8;
    final float[] mAzimuths;
    final float[] mCarrierFrequencies;
    final float[] mCn0DbHz;
    final float[] mElevations;
    final int mSvCount;
    final int[] mSvidWithFlags;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConstellationType {
    }

    public static abstract class Callback {
        public void onStarted() {
        }

        public void onStopped() {
        }

        public void onFirstFix(int ttffMillis) {
        }

        public void onSatelliteStatusChanged(GnssStatus status) {
        }
    }

    public GnssStatus(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFrequencies) {
        this.mSvCount = svCount;
        this.mSvidWithFlags = svidWithFlags;
        this.mCn0DbHz = cn0s;
        this.mElevations = elevations;
        this.mAzimuths = azimuths;
        this.mCarrierFrequencies = carrierFrequencies;
    }

    public int getSatelliteCount() {
        return this.mSvCount;
    }

    public int getConstellationType(int satIndex) {
        return (this.mSvidWithFlags[satIndex] >> 4) & 15;
    }

    public int getSvid(int satIndex) {
        return this.mSvidWithFlags[satIndex] >> 8;
    }

    public float getCn0DbHz(int satIndex) {
        return this.mCn0DbHz[satIndex];
    }

    public float getElevationDegrees(int satIndex) {
        return this.mElevations[satIndex];
    }

    public float getAzimuthDegrees(int satIndex) {
        return this.mAzimuths[satIndex];
    }

    public boolean hasEphemerisData(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & 1) != 0;
    }

    public boolean hasAlmanacData(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & 2) != 0;
    }

    public boolean usedInFix(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & 4) != 0;
    }

    public boolean hasCarrierFrequencyHz(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & 8) != 0;
    }

    public float getCarrierFrequencyHz(int satIndex) {
        return this.mCarrierFrequencies[satIndex];
    }

    public static String constellationTypeToString(int constellationType) {
        switch (constellationType) {
            case 0:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            case 1:
                return "GPS";
            case 2:
                return "SBAS";
            case 3:
                return "GLONASS";
            case 4:
                return "QZSS";
            case 5:
                return "BEIDOU";
            case 6:
                return "GALILEO";
            case 7:
                return "IRNSS";
            default:
                return Integer.toString(constellationType);
        }
    }
}
