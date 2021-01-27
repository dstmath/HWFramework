package com.huawei.hiai.awareness.client;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class FenceState implements Parcelable {
    public static final Parcelable.Creator<FenceState> CREATOR = new Parcelable.Creator<FenceState>() {
        /* class com.huawei.hiai.awareness.client.FenceState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FenceState createFromParcel(Parcel in) {
            return new FenceState(in);
        }

        @Override // android.os.Parcelable.Creator
        public FenceState[] newArray(int size) {
            return new FenceState[size];
        }
    };
    public static final String MESSAGE_TYPE = "awareness_fence_state";
    public static final int NOK = -1;
    public static final int OK = 1;
    public static final int UNKNOWN = 0;
    private int currentState;
    private Bundle extras;
    private long lastUpdateTimestamp;
    private int previousState;

    public FenceState() {
        this.currentState = 0;
        this.previousState = 0;
        this.lastUpdateTimestamp = 0;
    }

    private FenceState(Parcel in) {
        this.currentState = in.readInt();
        this.previousState = in.readInt();
        this.lastUpdateTimestamp = in.readLong();
        this.extras = in.readBundle();
    }

    public int getCurrentState() {
        return this.currentState;
    }

    public int getPreviousState() {
        return this.previousState;
    }

    public long getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }

    public Bundle getExtras() {
        Bundle bundle = this.extras;
        if (bundle != null) {
            bundle.setClassLoader(FenceState.class.getClassLoader());
        }
        Bundle bundle2 = this.extras;
        if (bundle2 != null) {
            return new Bundle(bundle2);
        }
        return null;
    }

    public void setCurrentState(int currentState2) {
        this.currentState = currentState2;
    }

    public void setPreviousState(int previousState2) {
        this.previousState = previousState2;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp2) {
    }

    public void setExtras(Bundle extras2) {
        this.extras = extras2 != null ? new Bundle(extras2) : null;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.currentState);
        dest.writeInt(this.previousState);
        dest.writeLong(this.lastUpdateTimestamp);
        dest.writeBundle(this.extras);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "FenceState(%d)", Integer.valueOf(this.currentState));
    }
}
