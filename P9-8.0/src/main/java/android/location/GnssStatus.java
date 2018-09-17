package android.location;

import android.util.Log;

public final class GnssStatus {
    public static final int CONSTELLATION_BEIDOU = 5;
    public static final int CONSTELLATION_GALILEO = 6;
    public static final int CONSTELLATION_GLONASS = 3;
    public static final int CONSTELLATION_GPS = 1;
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
    float[] mAzimuths;
    float[] mCarrierFrequencies;
    float[] mCn0DbHz;
    float[] mElevations;
    int mSvCount;
    int[] mSvidWithFlags;

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

    GnssStatus(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFrequencies) {
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

    public static boolean checkGnssData(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths) {
        if (cn0s == null) {
            Log.i("GnssStatus", "checkGnssData() cn0s is null");
        }
        if (elevations == null) {
            Log.i("GnssStatus", "checkGnssData() elevations is null");
        }
        if (azimuths == null) {
            Log.i("GnssStatus", "checkGnssData() azimuths is null");
        }
        if (cn0s != null && elevations != null && azimuths != null) {
            return true;
        }
        Log.i("GnssStatus", "checkGnssData() svCount=" + svCount + " cn0s:" + Integer.toHexString(System.identityHashCode(cn0s)) + " elevations:" + Integer.toHexString(System.identityHashCode(elevations)) + " azimuths:" + Integer.toHexString(System.identityHashCode(azimuths)));
        return false;
    }
}
