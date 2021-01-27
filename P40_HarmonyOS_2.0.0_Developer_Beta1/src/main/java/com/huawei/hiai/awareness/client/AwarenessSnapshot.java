package com.huawei.hiai.awareness.client;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class AwarenessSnapshot implements Parcelable {
    public static final Parcelable.Creator<AwarenessSnapshot> CREATOR = new Parcelable.Creator<AwarenessSnapshot>() {
        /* class com.huawei.hiai.awareness.client.AwarenessSnapshot.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AwarenessSnapshot createFromParcel(Parcel in) {
            return new AwarenessSnapshot(in);
        }

        @Override // android.os.Parcelable.Creator
        public AwarenessSnapshot[] newArray(int size) {
            return new AwarenessSnapshot[size];
        }
    };
    public static final String MESSAGE_TYPE = "context_awareness_snapshot";
    private Bundle args;
    private int type;

    private AwarenessSnapshot(Parcel in) {
        this.type = in.readInt();
        this.args = in.readBundle();
    }

    private AwarenessSnapshot(int type2) {
        this.type = type2;
    }

    public static AwarenessSnapshot create(int type2) {
        return new AwarenessSnapshot(type2);
    }

    public int getType() {
        return this.type;
    }

    public Bundle getArgs() {
        Bundle bundle = this.args;
        if (bundle != null) {
            return new Bundle(bundle);
        }
        return null;
    }

    public AwarenessSnapshot putArg(String name, int value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putInt(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, long value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putLong(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, boolean value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putBoolean(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, String value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putString(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, float value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putFloat(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, double value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putDouble(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, String[] value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putStringArray(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, byte[] value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putByteArray(name, value);
        return this;
    }

    public AwarenessSnapshot putArg(String name, byte value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putByte(name, value);
        return this;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeBundle(this.args);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "AwarenessSnapshot(%d)", Integer.valueOf(this.type));
    }
}
