package android.location;

public final class GnssStatus {
    public static final int CONSTELLATION_BEIDOU = 5;
    public static final int CONSTELLATION_GALILEO = 6;
    public static final int CONSTELLATION_GLONASS = 3;
    public static final int CONSTELLATION_GPS = 1;
    public static final int CONSTELLATION_QZSS = 4;
    public static final int CONSTELLATION_SBAS = 2;
    public static final int CONSTELLATION_TYPE_MASK = 15;
    public static final int CONSTELLATION_TYPE_SHIFT_WIDTH = 3;
    public static final int CONSTELLATION_UNKNOWN = 0;
    public static final int GNSS_SV_FLAGS_HAS_ALMANAC_DATA = 2;
    public static final int GNSS_SV_FLAGS_HAS_EPHEMERIS_DATA = 1;
    public static final int GNSS_SV_FLAGS_NONE = 0;
    public static final int GNSS_SV_FLAGS_USED_IN_FIX = 4;
    public static final int SVID_SHIFT_WIDTH = 7;
    float[] mAzimuths;
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

    GnssStatus(int svCount, int[] svidWithFlags, float[] cn0s, float[] elevations, float[] azimuths) {
        this.mSvCount = svCount;
        this.mSvidWithFlags = svidWithFlags;
        this.mCn0DbHz = cn0s;
        this.mElevations = elevations;
        this.mAzimuths = azimuths;
    }

    public int getNumSatellites() {
        return getSatelliteCount();
    }

    public int getSatelliteCount() {
        return this.mSvCount;
    }

    public int getConstellationType(int satIndex) {
        return (this.mSvidWithFlags[satIndex] >> CONSTELLATION_TYPE_SHIFT_WIDTH) & CONSTELLATION_TYPE_MASK;
    }

    public int getSvid(int satIndex) {
        return this.mSvidWithFlags[satIndex] >> SVID_SHIFT_WIDTH;
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

    public boolean hasEphemeris(int satIndex) {
        return hasEphemerisData(satIndex);
    }

    public boolean hasEphemerisData(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & GNSS_SV_FLAGS_HAS_EPHEMERIS_DATA) != 0;
    }

    public boolean hasAlmanac(int satIndex) {
        return hasAlmanacData(satIndex);
    }

    public boolean hasAlmanacData(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & GNSS_SV_FLAGS_HAS_ALMANAC_DATA) != 0;
    }

    public boolean usedInFix(int satIndex) {
        return (this.mSvidWithFlags[satIndex] & GNSS_SV_FLAGS_USED_IN_FIX) != 0;
    }
}
