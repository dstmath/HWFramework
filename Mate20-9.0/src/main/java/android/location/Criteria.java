package android.location;

import android.os.Parcel;
import android.os.Parcelable;

public class Criteria implements Parcelable {
    public static final int ACCURACY_COARSE = 2;
    public static final int ACCURACY_FINE = 1;
    public static final int ACCURACY_HIGH = 3;
    public static final int ACCURACY_LOW = 1;
    public static final int ACCURACY_MEDIUM = 2;
    public static final Parcelable.Creator<Criteria> CREATOR = new Parcelable.Creator<Criteria>() {
        public Criteria createFromParcel(Parcel in) {
            Criteria c = new Criteria();
            int unused = c.mHorizontalAccuracy = in.readInt();
            int unused2 = c.mVerticalAccuracy = in.readInt();
            int unused3 = c.mSpeedAccuracy = in.readInt();
            int unused4 = c.mBearingAccuracy = in.readInt();
            int unused5 = c.mPowerRequirement = in.readInt();
            boolean z = false;
            boolean unused6 = c.mAltitudeRequired = in.readInt() != 0;
            boolean unused7 = c.mBearingRequired = in.readInt() != 0;
            boolean unused8 = c.mSpeedRequired = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            }
            boolean unused9 = c.mCostAllowed = z;
            return c;
        }

        public Criteria[] newArray(int size) {
            return new Criteria[size];
        }
    };
    public static final int NO_REQUIREMENT = 0;
    public static final int POWER_HIGH = 3;
    public static final int POWER_LOW = 1;
    public static final int POWER_MEDIUM = 2;
    /* access modifiers changed from: private */
    public boolean mAltitudeRequired = false;
    /* access modifiers changed from: private */
    public int mBearingAccuracy = 0;
    /* access modifiers changed from: private */
    public boolean mBearingRequired = false;
    /* access modifiers changed from: private */
    public boolean mCostAllowed = false;
    /* access modifiers changed from: private */
    public int mHorizontalAccuracy = 0;
    /* access modifiers changed from: private */
    public int mPowerRequirement = 0;
    /* access modifiers changed from: private */
    public int mSpeedAccuracy = 0;
    /* access modifiers changed from: private */
    public boolean mSpeedRequired = false;
    /* access modifiers changed from: private */
    public int mVerticalAccuracy = 0;

    public Criteria() {
    }

    public Criteria(Criteria criteria) {
        this.mHorizontalAccuracy = criteria.mHorizontalAccuracy;
        this.mVerticalAccuracy = criteria.mVerticalAccuracy;
        this.mSpeedAccuracy = criteria.mSpeedAccuracy;
        this.mBearingAccuracy = criteria.mBearingAccuracy;
        this.mPowerRequirement = criteria.mPowerRequirement;
        this.mAltitudeRequired = criteria.mAltitudeRequired;
        this.mBearingRequired = criteria.mBearingRequired;
        this.mSpeedRequired = criteria.mSpeedRequired;
        this.mCostAllowed = criteria.mCostAllowed;
    }

    public void setHorizontalAccuracy(int accuracy) {
        if (accuracy < 0 || accuracy > 3) {
            throw new IllegalArgumentException("accuracy=" + accuracy);
        }
        this.mHorizontalAccuracy = accuracy;
    }

    public int getHorizontalAccuracy() {
        return this.mHorizontalAccuracy;
    }

    public void setVerticalAccuracy(int accuracy) {
        if (accuracy < 0 || accuracy > 3) {
            throw new IllegalArgumentException("accuracy=" + accuracy);
        }
        this.mVerticalAccuracy = accuracy;
    }

    public int getVerticalAccuracy() {
        return this.mVerticalAccuracy;
    }

    public void setSpeedAccuracy(int accuracy) {
        if (accuracy < 0 || accuracy > 3) {
            throw new IllegalArgumentException("accuracy=" + accuracy);
        }
        this.mSpeedAccuracy = accuracy;
    }

    public int getSpeedAccuracy() {
        return this.mSpeedAccuracy;
    }

    public void setBearingAccuracy(int accuracy) {
        if (accuracy < 0 || accuracy > 3) {
            throw new IllegalArgumentException("accuracy=" + accuracy);
        }
        this.mBearingAccuracy = accuracy;
    }

    public int getBearingAccuracy() {
        return this.mBearingAccuracy;
    }

    public void setAccuracy(int accuracy) {
        if (accuracy < 0 || accuracy > 2) {
            throw new IllegalArgumentException("accuracy=" + accuracy);
        } else if (accuracy == 1) {
            this.mHorizontalAccuracy = 3;
        } else {
            this.mHorizontalAccuracy = 1;
        }
    }

    public int getAccuracy() {
        if (this.mHorizontalAccuracy >= 3) {
            return 1;
        }
        return 2;
    }

    public void setPowerRequirement(int level) {
        if (level < 0 || level > 3) {
            throw new IllegalArgumentException("level=" + level);
        }
        this.mPowerRequirement = level;
    }

    public int getPowerRequirement() {
        return this.mPowerRequirement;
    }

    public void setCostAllowed(boolean costAllowed) {
        this.mCostAllowed = costAllowed;
    }

    public boolean isCostAllowed() {
        return this.mCostAllowed;
    }

    public void setAltitudeRequired(boolean altitudeRequired) {
        this.mAltitudeRequired = altitudeRequired;
    }

    public boolean isAltitudeRequired() {
        return this.mAltitudeRequired;
    }

    public void setSpeedRequired(boolean speedRequired) {
        this.mSpeedRequired = speedRequired;
    }

    public boolean isSpeedRequired() {
        return this.mSpeedRequired;
    }

    public void setBearingRequired(boolean bearingRequired) {
        this.mBearingRequired = bearingRequired;
    }

    public boolean isBearingRequired() {
        return this.mBearingRequired;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mHorizontalAccuracy);
        parcel.writeInt(this.mVerticalAccuracy);
        parcel.writeInt(this.mSpeedAccuracy);
        parcel.writeInt(this.mBearingAccuracy);
        parcel.writeInt(this.mPowerRequirement);
        parcel.writeInt(this.mAltitudeRequired ? 1 : 0);
        parcel.writeInt(this.mBearingRequired ? 1 : 0);
        parcel.writeInt(this.mSpeedRequired ? 1 : 0);
        parcel.writeInt(this.mCostAllowed ? 1 : 0);
    }

    private static String powerToString(int power) {
        switch (power) {
            case 0:
                return "NO_REQ";
            case 1:
                return "LOW";
            case 2:
                return "MEDIUM";
            case 3:
                return "HIGH";
            default:
                return "???";
        }
    }

    private static String accuracyToString(int accuracy) {
        switch (accuracy) {
            case 0:
                return "---";
            case 1:
                return "LOW";
            case 2:
                return "MEDIUM";
            case 3:
                return "HIGH";
            default:
                return "???";
        }
    }

    public String toString() {
        return "Criteria[power=" + powerToString(this.mPowerRequirement) + " acc=" + accuracyToString(this.mHorizontalAccuracy) + ']';
    }
}
