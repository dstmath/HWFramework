package android.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.TimeUtils;

@SystemApi
public final class LocationRequest implements Parcelable {
    public static final int ACCURACY_BLOCK = 102;
    public static final int ACCURACY_CITY = 104;
    public static final int ACCURACY_FINE = 100;
    public static final Parcelable.Creator<LocationRequest> CREATOR = new Parcelable.Creator<LocationRequest>() {
        public LocationRequest createFromParcel(Parcel in) {
            LocationRequest request = new LocationRequest();
            request.setQuality(in.readInt());
            request.setFastestInterval(in.readLong());
            request.setInterval(in.readLong());
            request.setExpireAt(in.readLong());
            request.setNumUpdates(in.readInt());
            request.setSmallestDisplacement(in.readFloat());
            boolean z = false;
            request.setHideFromAppOps(in.readInt() != 0);
            if (in.readInt() != 0) {
                z = true;
            }
            request.setLowPowerMode(z);
            String provider = in.readString();
            if (provider != null) {
                request.setProvider(provider);
            }
            WorkSource workSource = (WorkSource) in.readParcelable(null);
            if (workSource != null) {
                request.setWorkSource(workSource);
            }
            return request;
        }

        public LocationRequest[] newArray(int size) {
            return new LocationRequest[size];
        }
    };
    private static final double FASTEST_INTERVAL_FACTOR = 6.0d;
    public static final int POWER_HIGH = 203;
    public static final int POWER_LOW = 201;
    public static final int POWER_NONE = 200;
    private long mExpireAt = Long.MAX_VALUE;
    private boolean mExplicitFastestInterval = false;
    private long mFastestInterval = ((long) (((double) this.mInterval) / FASTEST_INTERVAL_FACTOR));
    private boolean mHideFromAppOps = false;
    private long mInterval = 3600000;
    private boolean mLowPowerMode = false;
    private int mNumUpdates = Integer.MAX_VALUE;
    private String mProvider = LocationManager.FUSED_PROVIDER;
    private int mQuality = 201;
    private float mSmallestDisplacement = 0.0f;
    private WorkSource mWorkSource = null;

    public static LocationRequest create() {
        return new LocationRequest();
    }

    @SystemApi
    public static LocationRequest createFromDeprecatedProvider(String provider, long minTime, float minDistance, boolean singleShot) {
        int quality;
        if (minTime < 0) {
            minTime = 0;
        }
        if (minDistance < 0.0f) {
            minDistance = 0.0f;
        }
        if (LocationManager.PASSIVE_PROVIDER.equals(provider)) {
            quality = 200;
        } else if (LocationManager.GPS_PROVIDER.equals(provider)) {
            quality = 100;
        } else {
            quality = 201;
        }
        LocationRequest request = new LocationRequest().setProvider(provider).setQuality(quality).setInterval(minTime).setFastestInterval(minTime).setSmallestDisplacement(minDistance);
        if (singleShot) {
            request.setNumUpdates(1);
        }
        return request;
    }

    @SystemApi
    public static LocationRequest createFromDeprecatedCriteria(Criteria criteria, long minTime, float minDistance, boolean singleShot) {
        int quality;
        if (minTime < 0) {
            minTime = 0;
        }
        if (minDistance < 0.0f) {
            minDistance = 0.0f;
        }
        switch (criteria.getAccuracy()) {
            case 1:
                quality = 100;
                break;
            case 2:
                quality = 102;
                break;
            default:
                if (criteria.getPowerRequirement() == 3) {
                    quality = 203;
                    break;
                } else {
                    quality = 201;
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
    }

    public LocationRequest(LocationRequest src) {
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
        this.mLowPowerMode = src.mLowPowerMode;
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

    @SystemApi
    public LocationRequest setLowPowerMode(boolean enabled) {
        this.mLowPowerMode = enabled;
        return this;
    }

    @SystemApi
    public boolean isLowPowerMode() {
        return this.mLowPowerMode;
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
        if (millis > Long.MAX_VALUE - elapsedRealtime) {
            this.mExpireAt = Long.MAX_VALUE;
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
        if (numUpdates > 0) {
            this.mNumUpdates = numUpdates;
            return this;
        }
        throw new IllegalArgumentException("invalid numUpdates: " + numUpdates);
    }

    public int getNumUpdates() {
        return this.mNumUpdates;
    }

    public void decrementNumUpdates() {
        if (this.mNumUpdates != Integer.MAX_VALUE) {
            this.mNumUpdates--;
        }
        if (this.mNumUpdates < 0) {
            this.mNumUpdates = 0;
        }
    }

    @SystemApi
    public LocationRequest setProvider(String provider) {
        checkProvider(provider);
        this.mProvider = provider;
        return this;
    }

    @SystemApi
    public String getProvider() {
        return this.mProvider;
    }

    @SystemApi
    public LocationRequest setSmallestDisplacement(float meters) {
        checkDisplacement(meters);
        this.mSmallestDisplacement = meters;
        return this;
    }

    @SystemApi
    public float getSmallestDisplacement() {
        return this.mSmallestDisplacement;
    }

    @SystemApi
    public void setWorkSource(WorkSource workSource) {
        this.mWorkSource = workSource;
    }

    @SystemApi
    public WorkSource getWorkSource() {
        return this.mWorkSource;
    }

    @SystemApi
    public void setHideFromAppOps(boolean hideFromAppOps) {
        this.mHideFromAppOps = hideFromAppOps;
    }

    @SystemApi
    public boolean getHideFromAppOps() {
        return this.mHideFromAppOps;
    }

    private static void checkInterval(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("invalid interval: " + millis);
        }
    }

    private static void checkQuality(int quality) {
        if (quality != 100 && quality != 102 && quality != 104 && quality != 203) {
            switch (quality) {
                case 200:
                case 201:
                    return;
                default:
                    throw new IllegalArgumentException("invalid quality: " + quality);
            }
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
        parcel.writeInt(this.mQuality);
        parcel.writeLong(this.mFastestInterval);
        parcel.writeLong(this.mInterval);
        parcel.writeLong(this.mExpireAt);
        parcel.writeInt(this.mNumUpdates);
        parcel.writeFloat(this.mSmallestDisplacement);
        parcel.writeInt(this.mHideFromAppOps ? 1 : 0);
        parcel.writeInt(this.mLowPowerMode ? 1 : 0);
        parcel.writeString(this.mProvider);
        parcel.writeParcelable(this.mWorkSource, 0);
    }

    public static String qualityToString(int quality) {
        if (quality == 100) {
            return "ACCURACY_FINE";
        }
        if (quality == 102) {
            return "ACCURACY_BLOCK";
        }
        if (quality == 104) {
            return "ACCURACY_CITY";
        }
        if (quality == 203) {
            return "POWER_HIGH";
        }
        switch (quality) {
            case 200:
                return "POWER_NONE";
            case 201:
                return "POWER_LOW";
            default:
                return "???";
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Request[");
        s.append(qualityToString(this.mQuality));
        if (this.mProvider != null) {
            s.append(' ');
            s.append(this.mProvider);
        }
        if (this.mQuality != 200) {
            s.append(" requested=");
            TimeUtils.formatDuration(this.mInterval, s);
        }
        s.append(" fastest=");
        TimeUtils.formatDuration(this.mFastestInterval, s);
        if (this.mExpireAt != Long.MAX_VALUE) {
            s.append(" expireIn=");
            TimeUtils.formatDuration(this.mExpireAt - SystemClock.elapsedRealtime(), s);
        }
        if (this.mNumUpdates != Integer.MAX_VALUE) {
            s.append(" num=");
            s.append(this.mNumUpdates);
        }
        s.append(" lowPowerMode=");
        s.append(this.mLowPowerMode);
        s.append(']');
        return s.toString();
    }
}
