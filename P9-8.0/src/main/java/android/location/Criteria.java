package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Criteria implements Parcelable {
    public static final int ACCURACY_COARSE = 2;
    public static final int ACCURACY_FINE = 1;
    public static final int ACCURACY_HIGH = 3;
    public static final int ACCURACY_LOW = 1;
    public static final int ACCURACY_MEDIUM = 2;
    public static final Creator<Criteria> CREATOR = new Creator<Criteria>() {
        public Criteria createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            Criteria c = new Criteria();
            c.mHorizontalAccuracy = in.readInt();
            c.mVerticalAccuracy = in.readInt();
            c.mSpeedAccuracy = in.readInt();
            c.mBearingAccuracy = in.readInt();
            c.mPowerRequirement = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            c.mAltitudeRequired = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            c.mBearingRequired = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            c.mSpeedRequired = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            c.mCostAllowed = z2;
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
    private boolean mAltitudeRequired = false;
    private int mBearingAccuracy = 0;
    private boolean mBearingRequired = false;
    private boolean mCostAllowed = false;
    private int mHorizontalAccuracy = 0;
    private int mPowerRequirement = 0;
    private int mSpeedAccuracy = 0;
    private boolean mSpeedRequired = false;
    private int mVerticalAccuracy = 0;

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
        int i;
        int i2 = 1;
        parcel.writeInt(this.mHorizontalAccuracy);
        parcel.writeInt(this.mVerticalAccuracy);
        parcel.writeInt(this.mSpeedAccuracy);
        parcel.writeInt(this.mBearingAccuracy);
        parcel.writeInt(this.mPowerRequirement);
        if (this.mAltitudeRequired) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mBearingRequired) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mSpeedRequired) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (!this.mCostAllowed) {
            i2 = 0;
        }
        parcel.writeInt(i2);
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
        StringBuilder s = new StringBuilder();
        s.append("Criteria[power=").append(powerToString(this.mPowerRequirement));
        s.append(" acc=").append(accuracyToString(this.mHorizontalAccuracy));
        s.append(']');
        return s.toString();
    }
}
