package android.location;

import android.content.pm.PackageManager;
import android.net.LinkQualityInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.WorkSource;
import android.preference.Preference;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.TimeUtils;

public final class LocationRequest implements Parcelable {
    public static final int ACCURACY_BLOCK = 102;
    public static final int ACCURACY_CITY = 104;
    public static final int ACCURACY_FINE = 100;
    public static final Creator<LocationRequest> CREATOR = null;
    private static final double FASTEST_INTERVAL_FACTOR = 6.0d;
    public static final int POWER_HIGH = 203;
    public static final int POWER_LOW = 201;
    public static final int POWER_NONE = 200;
    private long mExpireAt;
    private boolean mExplicitFastestInterval;
    private long mFastestInterval;
    private boolean mHideFromAppOps;
    private long mInterval;
    private int mNumUpdates;
    private String mProvider;
    private int mQuality;
    private float mSmallestDisplacement;
    private WorkSource mWorkSource;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.LocationRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.LocationRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.LocationRequest.<clinit>():void");
    }

    public static LocationRequest create() {
        return new LocationRequest();
    }

    public static LocationRequest createFromDeprecatedProvider(String provider, long minTime, float minDistance, boolean singleShot) {
        int quality;
        if (minTime < 0) {
            minTime = 0;
        }
        if (minDistance < 0.0f) {
            minDistance = 0.0f;
        }
        if (LocationManager.PASSIVE_PROVIDER.equals(provider)) {
            quality = POWER_NONE;
        } else if (LocationManager.GPS_PROVIDER.equals(provider)) {
            quality = ACCURACY_FINE;
        } else {
            quality = POWER_LOW;
        }
        LocationRequest request = new LocationRequest().setProvider(provider).setQuality(quality).setInterval(minTime).setFastestInterval(minTime).setSmallestDisplacement(minDistance);
        if (singleShot) {
            request.setNumUpdates(1);
        }
        return request;
    }

    public static LocationRequest createFromDeprecatedCriteria(Criteria criteria, long minTime, float minDistance, boolean singleShot) {
        int quality;
        if (minTime < 0) {
            minTime = 0;
        }
        if (minDistance < 0.0f) {
            minDistance = 0.0f;
        }
        switch (criteria.getAccuracy()) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                quality = ACCURACY_FINE;
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                quality = ACCURACY_BLOCK;
                break;
            default:
                switch (criteria.getPowerRequirement()) {
                    case Engine.DEFAULT_STREAM /*3*/:
                        quality = POWER_HIGH;
                        break;
                    default:
                        quality = POWER_LOW;
                        break;
                }
        }
        LocationRequest request = new LocationRequest().setQuality(quality).setInterval(minTime).setFastestInterval(minTime).setSmallestDisplacement(minDistance);
        if (singleShot) {
            request.setNumUpdates(1);
        }
        return request;
    }

    public LocationRequest() {
        this.mQuality = POWER_LOW;
        this.mInterval = PackageManager.MAXIMUM_VERIFICATION_TIMEOUT;
        this.mFastestInterval = (long) (((double) this.mInterval) / FASTEST_INTERVAL_FACTOR);
        this.mExplicitFastestInterval = false;
        this.mExpireAt = LinkQualityInfo.UNKNOWN_LONG;
        this.mNumUpdates = Preference.DEFAULT_ORDER;
        this.mSmallestDisplacement = 0.0f;
        this.mWorkSource = null;
        this.mHideFromAppOps = false;
        this.mProvider = LocationManager.FUSED_PROVIDER;
    }

    public LocationRequest(LocationRequest src) {
        this.mQuality = POWER_LOW;
        this.mInterval = PackageManager.MAXIMUM_VERIFICATION_TIMEOUT;
        this.mFastestInterval = (long) (((double) this.mInterval) / FASTEST_INTERVAL_FACTOR);
        this.mExplicitFastestInterval = false;
        this.mExpireAt = LinkQualityInfo.UNKNOWN_LONG;
        this.mNumUpdates = Preference.DEFAULT_ORDER;
        this.mSmallestDisplacement = 0.0f;
        this.mWorkSource = null;
        this.mHideFromAppOps = false;
        this.mProvider = LocationManager.FUSED_PROVIDER;
        this.mQuality = src.mQuality;
        this.mInterval = src.mInterval;
        this.mFastestInterval = src.mFastestInterval;
        this.mExplicitFastestInterval = src.mExplicitFastestInterval;
        this.mExpireAt = src.mExpireAt;
        this.mNumUpdates = src.mNumUpdates;
        this.mSmallestDisplacement = src.mSmallestDisplacement;
        this.mProvider = src.mProvider;
        this.mWorkSource = src.mWorkSource;
        this.mHideFromAppOps = src.mHideFromAppOps;
    }

    public LocationRequest setQuality(int quality) {
        checkQuality(quality);
        this.mQuality = quality;
        return this;
    }

    public int getQuality() {
        return this.mQuality;
    }

    public LocationRequest setInterval(long millis) {
        checkInterval(millis);
        this.mInterval = millis;
        if (!this.mExplicitFastestInterval) {
            this.mFastestInterval = (long) (((double) this.mInterval) / FASTEST_INTERVAL_FACTOR);
        }
        return this;
    }

    public long getInterval() {
        return this.mInterval;
    }

    public LocationRequest setFastestInterval(long millis) {
        checkInterval(millis);
        this.mExplicitFastestInterval = true;
        this.mFastestInterval = millis;
        return this;
    }

    public long getFastestInterval() {
        return this.mFastestInterval;
    }

    public LocationRequest setExpireIn(long millis) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (millis > LinkQualityInfo.UNKNOWN_LONG - elapsedRealtime) {
            this.mExpireAt = LinkQualityInfo.UNKNOWN_LONG;
        } else {
            this.mExpireAt = millis + elapsedRealtime;
        }
        if (this.mExpireAt < 0) {
            this.mExpireAt = 0;
        }
        return this;
    }

    public LocationRequest setExpireAt(long millis) {
        this.mExpireAt = millis;
        if (this.mExpireAt < 0) {
            this.mExpireAt = 0;
        }
        return this;
    }

    public long getExpireAt() {
        return this.mExpireAt;
    }

    public LocationRequest setNumUpdates(int numUpdates) {
        if (numUpdates <= 0) {
            throw new IllegalArgumentException("invalid numUpdates: " + numUpdates);
        }
        this.mNumUpdates = numUpdates;
        return this;
    }

    public int getNumUpdates() {
        return this.mNumUpdates;
    }

    public void decrementNumUpdates() {
        if (this.mNumUpdates != Preference.DEFAULT_ORDER) {
            this.mNumUpdates--;
        }
        if (this.mNumUpdates < 0) {
            this.mNumUpdates = 0;
        }
    }

    public LocationRequest setProvider(String provider) {
        checkProvider(provider);
        this.mProvider = provider;
        return this;
    }

    public String getProvider() {
        return this.mProvider;
    }

    public LocationRequest setSmallestDisplacement(float meters) {
        checkDisplacement(meters);
        this.mSmallestDisplacement = meters;
        return this;
    }

    public float getSmallestDisplacement() {
        return this.mSmallestDisplacement;
    }

    public void setWorkSource(WorkSource workSource) {
        this.mWorkSource = workSource;
    }

    public WorkSource getWorkSource() {
        return this.mWorkSource;
    }

    public void setHideFromAppOps(boolean hideFromAppOps) {
        this.mHideFromAppOps = hideFromAppOps;
    }

    public boolean getHideFromAppOps() {
        return this.mHideFromAppOps;
    }

    private static void checkInterval(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("invalid interval: " + millis);
        }
    }

    private static void checkQuality(int quality) {
        switch (quality) {
            case ACCURACY_FINE /*100*/:
            case ACCURACY_BLOCK /*102*/:
            case ACCURACY_CITY /*104*/:
            case POWER_NONE /*200*/:
            case POWER_LOW /*201*/:
            case POWER_HIGH /*203*/:
            default:
                throw new IllegalArgumentException("invalid quality: " + quality);
        }
    }

    private static void checkDisplacement(float meters) {
        if (meters < 0.0f) {
            throw new IllegalArgumentException("invalid displacement: " + meters);
        }
    }

    private static void checkProvider(String name) {
        if (name == null) {
            throw new IllegalArgumentException("invalid provider: " + name);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        parcel.writeInt(this.mQuality);
        parcel.writeLong(this.mFastestInterval);
        parcel.writeLong(this.mInterval);
        parcel.writeLong(this.mExpireAt);
        parcel.writeInt(this.mNumUpdates);
        parcel.writeFloat(this.mSmallestDisplacement);
        if (this.mHideFromAppOps) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        parcel.writeString(this.mProvider);
        parcel.writeParcelable(this.mWorkSource, 0);
    }

    public static String qualityToString(int quality) {
        switch (quality) {
            case ACCURACY_FINE /*100*/:
                return "ACCURACY_FINE";
            case ACCURACY_BLOCK /*102*/:
                return "ACCURACY_BLOCK";
            case ACCURACY_CITY /*104*/:
                return "ACCURACY_CITY";
            case POWER_NONE /*200*/:
                return "POWER_NONE";
            case POWER_LOW /*201*/:
                return "POWER_LOW";
            case POWER_HIGH /*203*/:
                return "POWER_HIGH";
            default:
                return "???";
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Request[").append(qualityToString(this.mQuality));
        if (this.mProvider != null) {
            s.append(' ').append(this.mProvider);
        }
        if (this.mQuality != POWER_NONE) {
            s.append(" requested=");
            TimeUtils.formatDuration(this.mInterval, s);
        }
        s.append(" fastest=");
        TimeUtils.formatDuration(this.mFastestInterval, s);
        if (this.mExpireAt != LinkQualityInfo.UNKNOWN_LONG) {
            long expireIn = this.mExpireAt - SystemClock.elapsedRealtime();
            s.append(" expireIn=");
            TimeUtils.formatDuration(expireIn, s);
        }
        if (this.mNumUpdates != Preference.DEFAULT_ORDER) {
            s.append(" num=").append(this.mNumUpdates);
        }
        s.append(']');
        return s.toString();
    }
}
