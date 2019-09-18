package android.location;

import android.annotation.SystemApi;
import android.bluetooth.BluetoothHidDevice;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Printer;
import android.util.TimeUtils;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public class Location implements Parcelable {
    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            Location l = new Location(in.readString());
            long unused = l.mTime = in.readLong();
            long unused2 = l.mElapsedRealtimeNanos = in.readLong();
            byte unused3 = l.mFieldsMask = in.readByte();
            double unused4 = l.mLatitude = in.readDouble();
            double unused5 = l.mLongitude = in.readDouble();
            double unused6 = l.mAltitude = in.readDouble();
            float unused7 = l.mSpeed = in.readFloat();
            float unused8 = l.mBearing = in.readFloat();
            float unused9 = l.mHorizontalAccuracyMeters = in.readFloat();
            float unused10 = l.mVerticalAccuracyMeters = in.readFloat();
            float unused11 = l.mSpeedAccuracyMetersPerSecond = in.readFloat();
            float unused12 = l.mBearingAccuracyDegrees = in.readFloat();
            Bundle unused13 = l.mExtras = Bundle.setDefusable(in.readBundle(), true);
            return l;
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
    public static final String EXTRA_COARSE_LOCATION = "coarseLocation";
    public static final String EXTRA_NO_GPS_LOCATION = "noGPSLocation";
    public static final int FORMAT_DEGREES = 0;
    public static final int FORMAT_MINUTES = 1;
    public static final int FORMAT_SECONDS = 2;
    private static final int HAS_ALTITUDE_MASK = 1;
    private static final int HAS_BEARING_ACCURACY_MASK = 128;
    private static final int HAS_BEARING_MASK = 4;
    private static final int HAS_HORIZONTAL_ACCURACY_MASK = 8;
    private static final int HAS_MOCK_PROVIDER_MASK = 16;
    private static final int HAS_SPEED_ACCURACY_MASK = 64;
    private static final int HAS_SPEED_MASK = 2;
    private static final int HAS_VERTICAL_ACCURACY_MASK = 32;
    private static ThreadLocal<BearingDistanceCache> sBearingDistanceCache = new ThreadLocal<BearingDistanceCache>() {
        /* access modifiers changed from: protected */
        public BearingDistanceCache initialValue() {
            return new BearingDistanceCache();
        }
    };
    /* access modifiers changed from: private */
    public double mAltitude = 0.0d;
    /* access modifiers changed from: private */
    public float mBearing = 0.0f;
    /* access modifiers changed from: private */
    public float mBearingAccuracyDegrees = 0.0f;
    /* access modifiers changed from: private */
    public long mElapsedRealtimeNanos = 0;
    /* access modifiers changed from: private */
    public Bundle mExtras = null;
    /* access modifiers changed from: private */
    public byte mFieldsMask = 0;
    /* access modifiers changed from: private */
    public float mHorizontalAccuracyMeters = 0.0f;
    /* access modifiers changed from: private */
    public double mLatitude = 0.0d;
    /* access modifiers changed from: private */
    public double mLongitude = 0.0d;
    private String mProvider;
    /* access modifiers changed from: private */
    public float mSpeed = 0.0f;
    /* access modifiers changed from: private */
    public float mSpeedAccuracyMetersPerSecond = 0.0f;
    /* access modifiers changed from: private */
    public long mTime = 0;
    /* access modifiers changed from: private */
    public float mVerticalAccuracyMeters = 0.0f;

    private static class BearingDistanceCache {
        /* access modifiers changed from: private */
        public float mDistance;
        /* access modifiers changed from: private */
        public float mFinalBearing;
        /* access modifiers changed from: private */
        public float mInitialBearing;
        /* access modifiers changed from: private */
        public double mLat1;
        /* access modifiers changed from: private */
        public double mLat2;
        /* access modifiers changed from: private */
        public double mLon1;
        /* access modifiers changed from: private */
        public double mLon2;

        private BearingDistanceCache() {
            this.mLat1 = 0.0d;
            this.mLon1 = 0.0d;
            this.mLat2 = 0.0d;
            this.mLon2 = 0.0d;
            this.mDistance = 0.0f;
            this.mInitialBearing = 0.0f;
            this.mFinalBearing = 0.0f;
        }
    }

    public Location(String provider) {
        this.mProvider = provider;
    }

    public Location(Location l) {
        set(l);
    }

    public void set(Location l) {
        this.mProvider = l.mProvider;
        this.mTime = l.mTime;
        this.mElapsedRealtimeNanos = l.mElapsedRealtimeNanos;
        this.mFieldsMask = l.mFieldsMask;
        this.mLatitude = l.mLatitude;
        this.mLongitude = l.mLongitude;
        this.mAltitude = l.mAltitude;
        this.mSpeed = l.mSpeed;
        this.mBearing = l.mBearing;
        this.mHorizontalAccuracyMeters = l.mHorizontalAccuracyMeters;
        this.mVerticalAccuracyMeters = l.mVerticalAccuracyMeters;
        this.mSpeedAccuracyMetersPerSecond = l.mSpeedAccuracyMetersPerSecond;
        this.mBearingAccuracyDegrees = l.mBearingAccuracyDegrees;
        this.mExtras = l.mExtras == null ? null : new Bundle(l.mExtras);
    }

    public void reset() {
        this.mProvider = null;
        this.mTime = 0;
        this.mElapsedRealtimeNanos = 0;
        this.mFieldsMask = 0;
        this.mLatitude = 0.0d;
        this.mLongitude = 0.0d;
        this.mAltitude = 0.0d;
        this.mSpeed = 0.0f;
        this.mBearing = 0.0f;
        this.mHorizontalAccuracyMeters = 0.0f;
        this.mVerticalAccuracyMeters = 0.0f;
        this.mSpeedAccuracyMetersPerSecond = 0.0f;
        this.mBearingAccuracyDegrees = 0.0f;
        this.mExtras = null;
    }

    public static String convert(double coordinate, int outputType) {
        if (coordinate < -180.0d || coordinate > 180.0d || Double.isNaN(coordinate)) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        } else if (outputType == 0 || outputType == 1 || outputType == 2) {
            StringBuilder sb = new StringBuilder();
            if (coordinate < 0.0d) {
                sb.append('-');
                coordinate = -coordinate;
            }
            DecimalFormat df = new DecimalFormat("###.#####");
            if (outputType == 1 || outputType == 2) {
                int degrees = (int) Math.floor(coordinate);
                sb.append(degrees);
                sb.append(':');
                coordinate = (coordinate - ((double) degrees)) * 60.0d;
                if (outputType == 2) {
                    int minutes = (int) Math.floor(coordinate);
                    sb.append(minutes);
                    sb.append(':');
                    coordinate = (coordinate - ((double) minutes)) * 60.0d;
                }
            }
            sb.append(df.format(coordinate));
            return sb.toString();
        } else {
            throw new IllegalArgumentException("outputType=" + outputType);
        }
    }

    public static double convert(String coordinate) {
        double min;
        String coordinate2 = coordinate;
        if (coordinate2 != null) {
            boolean negative = false;
            if (coordinate2.charAt(0) == '-') {
                coordinate2 = coordinate2.substring(1);
                negative = true;
            }
            boolean negative2 = negative;
            String coordinate3 = coordinate2;
            StringTokenizer st = new StringTokenizer(coordinate3, ":");
            int tokens = st.countTokens();
            if (tokens >= 1) {
                try {
                    String degrees = st.nextToken();
                    if (tokens == 1) {
                        try {
                            double val = Double.parseDouble(degrees);
                            return negative2 ? -val : val;
                        } catch (NumberFormatException e) {
                            StringTokenizer stringTokenizer = st;
                            throw new IllegalArgumentException("coordinate=" + coordinate3);
                        }
                    } else {
                        String minutes = st.nextToken();
                        int deg = Integer.parseInt(degrees);
                        double sec = 0.0d;
                        boolean secPresent = false;
                        if (st.hasMoreTokens()) {
                            min = (double) Integer.parseInt(minutes);
                            sec = Double.parseDouble(st.nextToken());
                            secPresent = true;
                        } else {
                            min = Double.parseDouble(minutes);
                        }
                        boolean isNegative180 = negative2 && deg == 180 && min == 0.0d && sec == 0.0d;
                        StringTokenizer stringTokenizer2 = st;
                        if (((double) deg) < 0.0d || (deg > 179 && !isNegative180)) {
                            throw new IllegalArgumentException("coordinate=" + coordinate3);
                        } else if (min < 0.0d || min >= 60.0d || (secPresent && min > 59.0d)) {
                            throw new IllegalArgumentException("coordinate=" + coordinate3);
                        } else if (sec < 0.0d || sec >= 60.0d) {
                            try {
                                throw new IllegalArgumentException("coordinate=" + coordinate3);
                            } catch (NumberFormatException e2) {
                                throw new IllegalArgumentException("coordinate=" + coordinate3);
                            }
                        } else {
                            double val2 = (((((double) deg) * 3600.0d) + (60.0d * min)) + sec) / 3600.0d;
                            return negative2 ? -val2 : val2;
                        }
                    }
                } catch (NumberFormatException e3) {
                    StringTokenizer stringTokenizer3 = st;
                    throw new IllegalArgumentException("coordinate=" + coordinate3);
                }
            } else {
                throw new IllegalArgumentException("coordinate=" + coordinate3);
            }
        } else {
            throw new NullPointerException("coordinate");
        }
    }

    private static void computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2, BearingDistanceCache results) {
        double lat22;
        BearingDistanceCache bearingDistanceCache = results;
        double lat12 = lat1 * 0.017453292519943295d;
        double lat23 = lat2 * 0.017453292519943295d;
        double lon12 = lon1 * 0.017453292519943295d;
        double lon22 = 0.017453292519943295d * lon2;
        double f = (6378137.0d - 6356752.3142d) / 6378137.0d;
        double aSqMinusBSqOverBSq = ((6378137.0d * 6378137.0d) - (6356752.3142d * 6356752.3142d)) / (6356752.3142d * 6356752.3142d);
        double L = lon22 - lon12;
        double A = 0.0d;
        double cosSigma = Math.atan((1.0d - f) * Math.tan(lat12));
        double lon23 = lon22;
        double U2 = Math.atan((1.0d - f) * Math.tan(lat23));
        double cosU1 = Math.cos(cosSigma);
        double cosU2 = Math.cos(U2);
        double lon13 = lon12;
        double sinU1 = Math.sin(cosSigma);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;
        double sigma = 0.0d;
        double deltaSigma = 0.0d;
        double cosSigma2 = 0.0d;
        double sinSigma = 0.0d;
        double cosLambda = 0.0d;
        double sinLambda = 0.0d;
        int iter = 0;
        double lambda = L;
        while (true) {
            double U22 = U2;
            int iter2 = iter;
            if (iter2 >= 20) {
                lat22 = lat23;
                double d = cosSigma;
                double d2 = lambda;
                double U1 = cosSigma2;
                double d3 = sinSigma;
                break;
            }
            double lambdaOrig = lambda;
            double U12 = cosSigma;
            double U13 = lambda;
            cosLambda = Math.cos(U13);
            sinLambda = Math.sin(U13);
            double t1 = cosU2 * sinLambda;
            double t2 = (cosU1 * sinU2) - ((sinU1 * cosU2) * cosLambda);
            double d4 = U13;
            double sinSqSigma = (t1 * t1) + (t2 * t2);
            lat22 = lat23;
            double sinSigma2 = Math.sqrt(sinSqSigma);
            double d5 = sinSqSigma;
            double cosSigma3 = sinU1sinU2 + (cosU1cosU2 * cosLambda);
            sigma = Math.atan2(sinSigma2, cosSigma3);
            double d6 = 0.0d;
            double sinAlpha = sinSigma2 == 0.0d ? 0.0d : (cosU1cosU2 * sinLambda) / sinSigma2;
            double cosSqAlpha = 1.0d - (sinAlpha * sinAlpha);
            if (cosSqAlpha != 0.0d) {
                d6 = cosSigma3 - ((2.0d * sinU1sinU2) / cosSqAlpha);
            }
            double cos2SM = d6;
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq;
            A = 1.0d + ((uSquared / 16384.0d) * (4096.0d + ((-768.0d + ((320.0d - (175.0d * uSquared)) * uSquared)) * uSquared)));
            double B = (uSquared / 1024.0d) * (256.0d + ((-128.0d + ((74.0d - (47.0d * uSquared)) * uSquared)) * uSquared));
            double C = (f / 16.0d) * cosSqAlpha * (4.0d + ((4.0d - (3.0d * cosSqAlpha)) * f));
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma2 * (cos2SM + ((B / 4.0d) * (((-1.0d + (2.0d * cos2SMSq)) * cosSigma3) - ((((B / 6.0d) * cos2SM) * (-3.0d + ((4.0d * sinSigma2) * sinSigma2))) * (-3.0d + (4.0d * cos2SMSq))))));
            double lambda2 = L + ((1.0d - C) * f * sinAlpha * (sigma + (C * sinSigma2 * (cos2SM + (C * cosSigma3 * (-1.0d + (2.0d * cos2SM * cos2SM)))))));
            double sinSigma3 = sinSigma2;
            if (Math.abs((lambda2 - lambdaOrig) / lambda2) < 1.0E-12d) {
                double d7 = lambda2;
                break;
            }
            iter = iter2 + 1;
            cosSigma2 = cosSigma3;
            U2 = U22;
            cosSigma = U12;
            lambda = lambda2;
            lat23 = lat22;
            sinSigma = sinSigma3;
        }
        float distance = (float) (6356752.3142d * A * (sigma - deltaSigma));
        float unused = bearingDistanceCache.mDistance = distance;
        float f2 = distance;
        float initialBearing = (float) (((double) ((float) Math.atan2(cosU2 * sinLambda, (cosU1 * sinU2) - ((sinU1 * cosU2) * cosLambda)))) * 57.29577951308232d);
        float unused2 = bearingDistanceCache.mInitialBearing = initialBearing;
        float finalBearing = (float) (((double) ((float) Math.atan2(cosU1 * sinLambda, ((-sinU1) * cosU2) + (cosU1 * sinU2 * cosLambda)))) * 57.29577951308232d);
        float unused3 = bearingDistanceCache.mFinalBearing = finalBearing;
        double unused4 = bearingDistanceCache.mLat1 = lat12;
        double unused5 = bearingDistanceCache.mLat2 = lat22;
        float f3 = initialBearing;
        float f4 = finalBearing;
        double lon14 = lon13;
        double unused6 = bearingDistanceCache.mLon1 = lon14;
        double d8 = lon14;
        double unused7 = bearingDistanceCache.mLon2 = lon23;
    }

    public static void distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results) {
        float[] fArr = results;
        if (fArr == null || fArr.length < 1) {
            throw new IllegalArgumentException("results is null or has length < 1");
        }
        BearingDistanceCache cache = sBearingDistanceCache.get();
        computeDistanceAndBearing(startLatitude, startLongitude, endLatitude, endLongitude, cache);
        fArr[0] = cache.mDistance;
        if (fArr.length > 1) {
            fArr[1] = cache.mInitialBearing;
            if (fArr.length > 2) {
                fArr[2] = cache.mFinalBearing;
            }
        }
    }

    public float distanceTo(Location dest) {
        BearingDistanceCache cache = sBearingDistanceCache.get();
        if (!(this.mLatitude == cache.mLat1 && this.mLongitude == cache.mLon1 && dest.mLatitude == cache.mLat2 && dest.mLongitude == cache.mLon2)) {
            computeDistanceAndBearing(this.mLatitude, this.mLongitude, dest.mLatitude, dest.mLongitude, cache);
        }
        return cache.mDistance;
    }

    public float bearingTo(Location dest) {
        BearingDistanceCache cache = sBearingDistanceCache.get();
        if (!(this.mLatitude == cache.mLat1 && this.mLongitude == cache.mLon1 && dest.mLatitude == cache.mLat2 && dest.mLongitude == cache.mLon2)) {
            computeDistanceAndBearing(this.mLatitude, this.mLongitude, dest.mLatitude, dest.mLongitude, cache);
        }
        return cache.mInitialBearing;
    }

    public String getProvider() {
        return this.mProvider;
    }

    public void setProvider(String provider) {
        this.mProvider = provider;
    }

    public long getTime() {
        return this.mTime;
    }

    public void setTime(long time) {
        this.mTime = time;
    }

    public long getElapsedRealtimeNanos() {
        return this.mElapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(long time) {
        this.mElapsedRealtimeNanos = time;
    }

    public double getLatitude() {
        return this.mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public boolean hasAltitude() {
        return (this.mFieldsMask & 1) != 0;
    }

    public double getAltitude() {
        return this.mAltitude;
    }

    public void setAltitude(double altitude) {
        this.mAltitude = altitude;
        this.mFieldsMask = (byte) (this.mFieldsMask | 1);
    }

    @Deprecated
    public void removeAltitude() {
        this.mAltitude = 0.0d;
        this.mFieldsMask = (byte) (this.mFieldsMask & -2);
    }

    public boolean hasSpeed() {
        return (this.mFieldsMask & 2) != 0;
    }

    public float getSpeed() {
        return this.mSpeed;
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed;
        this.mFieldsMask = (byte) (this.mFieldsMask | 2);
    }

    @Deprecated
    public void removeSpeed() {
        this.mSpeed = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -3);
    }

    public boolean hasBearing() {
        return (this.mFieldsMask & 4) != 0;
    }

    public float getBearing() {
        return this.mBearing;
    }

    public void setBearing(float bearing) {
        while (bearing < 0.0f) {
            bearing += 360.0f;
        }
        while (bearing >= 360.0f) {
            bearing -= 360.0f;
        }
        this.mBearing = bearing;
        this.mFieldsMask = (byte) (this.mFieldsMask | 4);
    }

    @Deprecated
    public void removeBearing() {
        this.mBearing = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -5);
    }

    public boolean hasAccuracy() {
        return (this.mFieldsMask & 8) != 0;
    }

    public float getAccuracy() {
        return this.mHorizontalAccuracyMeters;
    }

    public void setAccuracy(float horizontalAccuracy) {
        this.mHorizontalAccuracyMeters = horizontalAccuracy;
        this.mFieldsMask = (byte) (this.mFieldsMask | 8);
    }

    @Deprecated
    public void removeAccuracy() {
        this.mHorizontalAccuracyMeters = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -9);
    }

    public boolean hasVerticalAccuracy() {
        return (this.mFieldsMask & 32) != 0;
    }

    public float getVerticalAccuracyMeters() {
        return this.mVerticalAccuracyMeters;
    }

    public void setVerticalAccuracyMeters(float verticalAccuracyMeters) {
        this.mVerticalAccuracyMeters = verticalAccuracyMeters;
        this.mFieldsMask = (byte) (this.mFieldsMask | 32);
    }

    @Deprecated
    public void removeVerticalAccuracy() {
        this.mVerticalAccuracyMeters = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -33);
    }

    public boolean hasSpeedAccuracy() {
        return (this.mFieldsMask & BluetoothHidDevice.SUBCLASS1_KEYBOARD) != 0;
    }

    public float getSpeedAccuracyMetersPerSecond() {
        return this.mSpeedAccuracyMetersPerSecond;
    }

    public void setSpeedAccuracyMetersPerSecond(float speedAccuracyMeterPerSecond) {
        this.mSpeedAccuracyMetersPerSecond = speedAccuracyMeterPerSecond;
        this.mFieldsMask = (byte) (this.mFieldsMask | BluetoothHidDevice.SUBCLASS1_KEYBOARD);
    }

    @Deprecated
    public void removeSpeedAccuracy() {
        this.mSpeedAccuracyMetersPerSecond = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -65);
    }

    public boolean hasBearingAccuracy() {
        return (this.mFieldsMask & BluetoothHidDevice.SUBCLASS1_MOUSE) != 0;
    }

    public float getBearingAccuracyDegrees() {
        return this.mBearingAccuracyDegrees;
    }

    public void setBearingAccuracyDegrees(float bearingAccuracyDegrees) {
        this.mBearingAccuracyDegrees = bearingAccuracyDegrees;
        this.mFieldsMask = (byte) (this.mFieldsMask | BluetoothHidDevice.SUBCLASS1_MOUSE);
    }

    @Deprecated
    public void removeBearingAccuracy() {
        this.mBearingAccuracyDegrees = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -129);
    }

    @SystemApi
    public boolean isComplete() {
        if (this.mProvider == null || !hasAccuracy() || this.mTime == 0 || this.mElapsedRealtimeNanos == 0) {
            return false;
        }
        return true;
    }

    @SystemApi
    public void makeComplete() {
        if (this.mProvider == null) {
            this.mProvider = "?";
        }
        if (!hasAccuracy()) {
            this.mFieldsMask = (byte) (this.mFieldsMask | 8);
            this.mHorizontalAccuracyMeters = 100.0f;
        }
        if (this.mTime == 0) {
            this.mTime = System.currentTimeMillis();
        }
        if (this.mElapsedRealtimeNanos == 0) {
            this.mElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        }
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public void setExtras(Bundle extras) {
        this.mExtras = extras == null ? null : new Bundle(extras);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Location[");
        s.append(this.mProvider);
        s.append(String.format(" %.0f******,%.0f******", new Object[]{Double.valueOf(this.mLatitude), Double.valueOf(this.mLongitude)}));
        if (hasAccuracy()) {
            s.append(String.format(" hAcc=%.0f", new Object[]{Float.valueOf(this.mHorizontalAccuracyMeters)}));
        } else {
            s.append(" hAcc=???");
        }
        if (this.mTime == 0) {
            s.append(" t=?!?");
        }
        if (this.mElapsedRealtimeNanos == 0) {
            s.append(" et=?!?");
        } else {
            s.append(" et=");
            TimeUtils.formatDuration(this.mElapsedRealtimeNanos / 1000000, s);
        }
        if (hasAltitude()) {
            s.append(" alt=");
            s.append(this.mAltitude);
        }
        if (hasSpeed()) {
            s.append(" vel=");
            s.append(this.mSpeed);
        }
        if (hasBearing()) {
            s.append(" bear=");
            s.append(this.mBearing);
        }
        if (hasVerticalAccuracy()) {
            s.append(String.format(" vAcc=%.0f", new Object[]{Float.valueOf(this.mVerticalAccuracyMeters)}));
        } else {
            s.append(" vAcc=???");
        }
        if (hasSpeedAccuracy()) {
            s.append(String.format(" sAcc=%.0f", new Object[]{Float.valueOf(this.mSpeedAccuracyMetersPerSecond)}));
        } else {
            s.append(" sAcc=???");
        }
        if (hasBearingAccuracy()) {
            s.append(String.format(" bAcc=%.0f", new Object[]{Float.valueOf(this.mBearingAccuracyDegrees)}));
        } else {
            s.append(" bAcc=???");
        }
        if (isFromMockProvider()) {
            s.append(" mock");
        }
        s.append(']');
        return s.toString();
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + toString());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mProvider);
        parcel.writeLong(this.mTime);
        parcel.writeLong(this.mElapsedRealtimeNanos);
        parcel.writeByte(this.mFieldsMask);
        parcel.writeDouble(this.mLatitude);
        parcel.writeDouble(this.mLongitude);
        parcel.writeDouble(this.mAltitude);
        parcel.writeFloat(this.mSpeed);
        parcel.writeFloat(this.mBearing);
        parcel.writeFloat(this.mHorizontalAccuracyMeters);
        parcel.writeFloat(this.mVerticalAccuracyMeters);
        parcel.writeFloat(this.mSpeedAccuracyMetersPerSecond);
        parcel.writeFloat(this.mBearingAccuracyDegrees);
        parcel.writeBundle(this.mExtras);
    }

    public Location getExtraLocation(String key) {
        if (this.mExtras != null) {
            Parcelable value = this.mExtras.getParcelable(key);
            if (value instanceof Location) {
                return (Location) value;
            }
        }
        return null;
    }

    public void setExtraLocation(String key, Location value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putParcelable(key, value);
    }

    public boolean isFromMockProvider() {
        return (this.mFieldsMask & 16) != 0;
    }

    @SystemApi
    public void setIsFromMockProvider(boolean isFromMockProvider) {
        if (isFromMockProvider) {
            this.mFieldsMask = (byte) (this.mFieldsMask | 16);
        } else {
            this.mFieldsMask = (byte) (this.mFieldsMask & -17);
        }
    }
}
