package com.google.android.startop.iorap;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SystemServiceEvent implements Parcelable {
    public static final Parcelable.Creator<SystemServiceEvent> CREATOR = new Parcelable.Creator<SystemServiceEvent>() {
        /* class com.google.android.startop.iorap.SystemServiceEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SystemServiceEvent createFromParcel(Parcel in) {
            return new SystemServiceEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public SystemServiceEvent[] newArray(int size) {
            return new SystemServiceEvent[size];
        }
    };
    public static final int TYPE_BOOT_PHASE = 0;
    private static final int TYPE_MAX = 1;
    public static final int TYPE_START = 1;
    public final int type;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public SystemServiceEvent(int type2) {
        this.type = type2;
        checkConstructorArguments();
    }

    private void checkConstructorArguments() {
        CheckHelpers.checkTypeInRange(this.type, 1);
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format("{type: %d}", Integer.valueOf(this.type));
    }

    @Override // java.lang.Object
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof SystemServiceEvent) {
            return equals((SystemServiceEvent) other);
        }
        return false;
    }

    private boolean equals(SystemServiceEvent other) {
        return this.type == other.type;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.type);
    }

    private SystemServiceEvent(Parcel in) {
        this.type = in.readInt();
        checkConstructorArguments();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
