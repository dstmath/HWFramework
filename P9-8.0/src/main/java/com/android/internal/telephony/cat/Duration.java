package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Duration implements Parcelable {
    public static final Creator<Duration> CREATOR = new Creator<Duration>() {
        public Duration createFromParcel(Parcel in) {
            return new Duration(in, null);
        }

        public Duration[] newArray(int size) {
            return new Duration[size];
        }
    };
    public int timeInterval;
    public TimeUnit timeUnit;

    public enum TimeUnit {
        MINUTE(0),
        SECOND(1),
        TENTH_SECOND(2);
        
        private int mValue;

        private TimeUnit(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }
    }

    public Duration(int timeInterval, TimeUnit timeUnit) {
        this.timeInterval = timeInterval;
        this.timeUnit = timeUnit;
    }

    private Duration(Parcel in) {
        this.timeInterval = in.readInt();
        this.timeUnit = TimeUnit.values()[in.readInt()];
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.timeInterval);
        dest.writeInt(this.timeUnit.ordinal());
    }

    public int describeContents() {
        return 0;
    }
}
