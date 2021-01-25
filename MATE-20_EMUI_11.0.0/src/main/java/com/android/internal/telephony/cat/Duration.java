package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class Duration implements Parcelable {
    public static final Parcelable.Creator<Duration> CREATOR = new Parcelable.Creator<Duration>() {
        /* class com.android.internal.telephony.cat.Duration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Duration createFromParcel(Parcel in) {
            return new Duration(in);
        }

        @Override // android.os.Parcelable.Creator
        public Duration[] newArray(int size) {
            return new Duration[size];
        }
    };
    @UnsupportedAppUsage
    public int timeInterval;
    @UnsupportedAppUsage
    public TimeUnit timeUnit;

    public enum TimeUnit {
        MINUTE(0),
        SECOND(1),
        TENTH_SECOND(2);
        
        private int mValue;

        private TimeUnit(int value) {
            this.mValue = value;
        }

        @UnsupportedAppUsage
        public int value() {
            return this.mValue;
        }
    }

    public Duration(int timeInterval2, TimeUnit timeUnit2) {
        this.timeInterval = timeInterval2;
        this.timeUnit = timeUnit2;
    }

    private Duration(Parcel in) {
        this.timeInterval = in.readInt();
        this.timeUnit = TimeUnit.values()[in.readInt()];
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.timeInterval);
        dest.writeInt(this.timeUnit.ordinal());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
