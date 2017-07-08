package android.location;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.util.Printer;
import android.util.TimeUtils;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public class Location implements Parcelable {
    public static final Creator<Location> CREATOR = null;
    public static final String EXTRA_COARSE_LOCATION = "coarseLocation";
    public static final String EXTRA_NO_GPS_LOCATION = "noGPSLocation";
    public static final int FORMAT_DEGREES = 0;
    public static final int FORMAT_MINUTES = 1;
    public static final int FORMAT_SECONDS = 2;
    private static final byte HAS_ACCURACY_MASK = (byte) 8;
    private static final byte HAS_ALTITUDE_MASK = (byte) 1;
    private static final byte HAS_BEARING_MASK = (byte) 4;
    private static final byte HAS_MOCK_PROVIDER_MASK = (byte) 16;
    private static final byte HAS_SPEED_MASK = (byte) 2;
    private static ThreadLocal<BearingDistanceCache> sBearingDistanceCache;
    private float mAccuracy;
    private double mAltitude;
    private float mBearing;
    private long mElapsedRealtimeNanos;
    private Bundle mExtras;
    private byte mFieldsMask;
    private double mLatitude;
    private double mLongitude;
    private String mProvider;
    private float mSpeed;
    private long mTime;

    private static class BearingDistanceCache {
        private float mDistance;
        private float mFinalBearing;
        private float mInitialBearing;
        private double mLat1;
        private double mLat2;
        private double mLon1;
        private double mLon2;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.Location.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.Location.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.Location.<clinit>():void");
    }

    public Location(String provider) {
        this.mTime = 0;
        this.mElapsedRealtimeNanos = 0;
        this.mLatitude = 0.0d;
        this.mLongitude = 0.0d;
        this.mAltitude = 0.0d;
        this.mSpeed = 0.0f;
        this.mBearing = 0.0f;
        this.mAccuracy = 0.0f;
        this.mExtras = null;
        this.mFieldsMask = (byte) 0;
        this.mProvider = provider;
    }

    public Location(Location l) {
        this.mTime = 0;
        this.mElapsedRealtimeNanos = 0;
        this.mLatitude = 0.0d;
        this.mLongitude = 0.0d;
        this.mAltitude = 0.0d;
        this.mSpeed = 0.0f;
        this.mBearing = 0.0f;
        this.mAccuracy = 0.0f;
        this.mExtras = null;
        this.mFieldsMask = (byte) 0;
        set(l);
    }

    public void set(Location l) {
        Bundle bundle = null;
        this.mProvider = l.mProvider;
        this.mTime = l.mTime;
        this.mElapsedRealtimeNanos = l.mElapsedRealtimeNanos;
        this.mFieldsMask = l.mFieldsMask;
        this.mLatitude = l.mLatitude;
        this.mLongitude = l.mLongitude;
        this.mAltitude = l.mAltitude;
        this.mSpeed = l.mSpeed;
        this.mBearing = l.mBearing;
        this.mAccuracy = l.mAccuracy;
        if (l.mExtras != null) {
            bundle = new Bundle(l.mExtras);
        }
        this.mExtras = bundle;
    }

    public void reset() {
        this.mProvider = null;
        this.mTime = 0;
        this.mElapsedRealtimeNanos = 0;
        this.mFieldsMask = (byte) 0;
        this.mLatitude = 0.0d;
        this.mLongitude = 0.0d;
        this.mAltitude = 0.0d;
        this.mSpeed = 0.0f;
        this.mBearing = 0.0f;
        this.mAccuracy = 0.0f;
        this.mExtras = null;
    }

    public static String convert(double coordinate, int outputType) {
        if (coordinate < -180.0d || coordinate > 180.0d || Double.isNaN(coordinate)) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        } else if (outputType == 0 || outputType == FORMAT_MINUTES || outputType == FORMAT_SECONDS) {
            StringBuilder sb = new StringBuilder();
            if (coordinate < 0.0d) {
                sb.append('-');
                coordinate = -coordinate;
            }
            DecimalFormat df = new DecimalFormat("###.#####");
            if (outputType == FORMAT_MINUTES || outputType == FORMAT_SECONDS) {
                int degrees = (int) Math.floor(coordinate);
                sb.append(degrees);
                sb.append(':');
                coordinate = (coordinate - ((double) degrees)) * 60.0d;
                if (outputType == FORMAT_SECONDS) {
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
        if (coordinate == null) {
            throw new NullPointerException("coordinate");
        }
        boolean negative = false;
        if (coordinate.charAt(FORMAT_DEGREES) == '-') {
            coordinate = coordinate.substring(FORMAT_MINUTES);
            negative = true;
        }
        StringTokenizer st = new StringTokenizer(coordinate, ":");
        int tokens = st.countTokens();
        if (tokens < FORMAT_MINUTES) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        }
        try {
            String degrees = st.nextToken();
            double val;
            if (tokens == FORMAT_MINUTES) {
                val = Double.parseDouble(degrees);
                if (negative) {
                    val = -val;
                }
                return val;
            }
            double min;
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
            boolean isNegative180 = (negative && deg == 180 && min == 0.0d) ? sec == 0.0d : false;
            if (((double) deg) < 0.0d || (deg > 179 && !isNegative180)) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            } else if (min < 0.0d || min >= 60.0d || (secPresent && min > 59.0d)) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            } else if (sec < 0.0d || sec >= 60.0d) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            } else {
                val = (((((double) deg) * 3600.0d) + (60.0d * min)) + sec) / 3600.0d;
                if (negative) {
                    val = -val;
                }
                return val;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        }
    }

    private static void computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2, BearingDistanceCache results) {
        lat1 *= 0.017453292519943295d;
        lat2 *= 0.017453292519943295d;
        lon1 *= 0.017453292519943295d;
        lon2 *= 0.017453292519943295d;
        double f = 21384.685800000094d / 6378137.0d;
        double aSqMinusBSqOverBSq = (4.0680631590769E13d - 4.0408299984087055E13d) / 4.0408299984087055E13d;
        double L = lon2 - lon1;
        double A = 0.0d;
        double U1 = Math.atan((1.0d - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0d - f) * Math.tan(lat2));
        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;
        double sigma = 0.0d;
        double deltaSigma = 0.0d;
        double cosLambda = 0.0d;
        double sinLambda = 0.0d;
        double lambda = L;
        for (int iter = FORMAT_DEGREES; iter < 20; iter += FORMAT_MINUTES) {
            double sinAlpha;
            double cos2SM;
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = (cosU1 * sinU2) - ((sinU1 * cosU2) * cosLambda);
            double sinSigma = Math.sqrt((t1 * t1) + (t2 * t2));
            double cosSigma = sinU1sinU2 + (cosU1cosU2 * cosLambda);
            sigma = Math.atan2(sinSigma, cosSigma);
            if (sinSigma == 0.0d) {
                sinAlpha = 0.0d;
            } else {
                sinAlpha = (cosU1cosU2 * sinLambda) / sinSigma;
            }
            double cosSqAlpha = 1.0d - (sinAlpha * sinAlpha);
            if (cosSqAlpha == 0.0d) {
                cos2SM = 0.0d;
            } else {
                cos2SM = cosSigma - ((2.0d * sinU1sinU2) / cosSqAlpha);
            }
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq;
            A = 1.0d + ((uSquared / 16384.0d) * (((((320.0d - (175.0d * uSquared)) * uSquared) - 0.005859375d) * uSquared) + 4096.0d));
            double B = (uSquared / 1024.0d) * (((((74.0d - (47.0d * uSquared)) * uSquared) - 0.03125d) * uSquared) + 256.0d);
            double C = ((f / 16.0d) * cosSqAlpha) * (((4.0d - (3.0d * cosSqAlpha)) * f) + 4.0d);
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = (B * sinSigma) * (((B / 4.0d) * ((((2.0d * cos2SMSq) - 4.0d) * cosSigma) - ((((B / 6.0d) * cos2SM) * (((4.0d * sinSigma) * sinSigma) - 1.5d)) * ((4.0d * cos2SMSq) - 1.5d)))) + cos2SM);
            lambda = L + ((((1.0d - C) * f) * sinAlpha) * (((C * sinSigma) * (((C * cosSigma) * (((2.0d * cos2SM) * cos2SM) - 4.0d)) + cos2SM)) + sigma));
            if (Math.abs((lambda - lambdaOrig) / lambda) < 1.0E-12d) {
                break;
            }
        }
        results.mDistance = (float) ((6356752.3142d * A) * (sigma - deltaSigma));
        results.mInitialBearing = (float) (((double) ((float) Math.atan2(cosU2 * sinLambda, (cosU1 * sinU2) - ((sinU1 * cosU2) * cosLambda)))) * 57.29577951308232d);
        results.mFinalBearing = (float) (((double) ((float) Math.atan2(cosU1 * sinLambda, ((-sinU1) * cosU2) + ((cosU1 * sinU2) * cosLambda)))) * 57.29577951308232d);
        results.mLat1 = lat1;
        results.mLat2 = lat2;
        results.mLon1 = lon1;
        results.mLon2 = lon2;
    }

    public static void distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results) {
        if (results == null || results.length < FORMAT_MINUTES) {
            throw new IllegalArgumentException("results is null or has length < 1");
        }
        BearingDistanceCache cache = (BearingDistanceCache) sBearingDistanceCache.get();
        computeDistanceAndBearing(startLatitude, startLongitude, endLatitude, endLongitude, cache);
        results[FORMAT_DEGREES] = cache.mDistance;
        if (results.length > FORMAT_MINUTES) {
            results[FORMAT_MINUTES] = cache.mInitialBearing;
            if (results.length > FORMAT_SECONDS) {
                results[FORMAT_SECONDS] = cache.mFinalBearing;
            }
        }
    }

    public float distanceTo(Location dest) {
        BearingDistanceCache cache = (BearingDistanceCache) sBearingDistanceCache.get();
        if (this.mLatitude == cache.mLat1 && this.mLongitude == cache.mLon1 && dest.mLatitude == cache.mLat2) {
            if (dest.mLongitude != cache.mLon2) {
            }
            return cache.mDistance;
        }
        computeDistanceAndBearing(this.mLatitude, this.mLongitude, dest.mLatitude, dest.mLongitude, cache);
        return cache.mDistance;
    }

    public float bearingTo(Location dest) {
        BearingDistanceCache cache = (BearingDistanceCache) sBearingDistanceCache.get();
        if (this.mLatitude == cache.mLat1 && this.mLongitude == cache.mLon1 && dest.mLatitude == cache.mLat2) {
            if (dest.mLongitude != cache.mLon2) {
            }
            return cache.mInitialBearing;
        }
        computeDistanceAndBearing(this.mLatitude, this.mLongitude, dest.mLatitude, dest.mLongitude, cache);
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
        return (this.mFieldsMask & FORMAT_MINUTES) != 0;
    }

    public double getAltitude() {
        return this.mAltitude;
    }

    public void setAltitude(double altitude) {
        this.mAltitude = altitude;
        this.mFieldsMask = (byte) (this.mFieldsMask | FORMAT_MINUTES);
    }

    public void removeAltitude() {
        this.mAltitude = 0.0d;
        this.mFieldsMask = (byte) (this.mFieldsMask & -2);
    }

    public boolean hasSpeed() {
        return (this.mFieldsMask & FORMAT_SECONDS) != 0;
    }

    public float getSpeed() {
        return this.mSpeed;
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed;
        this.mFieldsMask = (byte) (this.mFieldsMask | FORMAT_SECONDS);
    }

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

    public void removeBearing() {
        this.mBearing = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -5);
    }

    public boolean hasAccuracy() {
        return (this.mFieldsMask & 8) != 0;
    }

    public float getAccuracy() {
        return this.mAccuracy;
    }

    public void setAccuracy(float accuracy) {
        this.mAccuracy = accuracy;
        this.mFieldsMask = (byte) (this.mFieldsMask | 8);
    }

    public void removeAccuracy() {
        this.mAccuracy = 0.0f;
        this.mFieldsMask = (byte) (this.mFieldsMask & -9);
    }

    public boolean isComplete() {
        if (this.mProvider == null || !hasAccuracy() || this.mTime == 0 || this.mElapsedRealtimeNanos == 0) {
            return false;
        }
        return true;
    }

    public void makeComplete() {
        if (this.mProvider == null) {
            this.mProvider = "?";
        }
        if (!hasAccuracy()) {
            this.mFieldsMask = (byte) (this.mFieldsMask | 8);
            this.mAccuracy = SensorManager.LIGHT_CLOUDY;
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
        Bundle bundle = null;
        if (extras != null) {
            bundle = new Bundle(extras);
        }
        this.mExtras = bundle;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Location[");
        s.append(this.mProvider);
        Object[] objArr = new Object[FORMAT_SECONDS];
        objArr[FORMAT_DEGREES] = Double.valueOf(this.mLatitude);
        objArr[FORMAT_MINUTES] = Double.valueOf(this.mLongitude);
        s.append(String.format(" %.0f******,%.0f******", objArr));
        if (hasAccuracy()) {
            objArr = new Object[FORMAT_MINUTES];
            objArr[FORMAT_DEGREES] = Float.valueOf(this.mAccuracy);
            s.append(String.format(" acc=%.0f", objArr));
        } else {
            s.append(" acc=???");
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
            s.append(" alt=").append(this.mAltitude);
        }
        if (hasSpeed()) {
            s.append(" vel=").append(this.mSpeed);
        }
        if (hasBearing()) {
            s.append(" bear=").append(this.mBearing);
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
        return FORMAT_DEGREES;
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
        parcel.writeFloat(this.mAccuracy);
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

    public void setIsFromMockProvider(boolean isFromMockProvider) {
        if (isFromMockProvider) {
            this.mFieldsMask = (byte) (this.mFieldsMask | 16);
        } else {
            this.mFieldsMask = (byte) (this.mFieldsMask & -17);
        }
    }
}
