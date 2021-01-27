package com.huawei.haptic;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public final class HwHapticAttributes implements Parcelable {
    public static final Parcelable.Creator<HwHapticAttributes> CREATOR = new Parcelable.Creator<HwHapticAttributes>() {
        /* class com.huawei.haptic.HwHapticAttributes.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwHapticAttributes createFromParcel(Parcel in) {
            return new HwHapticAttributes(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwHapticAttributes[] newArray(int size) {
            if (size > 64) {
                return new HwHapticAttributes[0];
            }
            return new HwHapticAttributes[size];
        }
    };
    public static final int USAGE_ALARM = 2;
    public static final int USAGE_GAME = 5;
    public static final int USAGE_MEDIA = 1;
    public static final int USAGE_NOTIFICATION = 3;
    public static final int USAGE_RINGTONE = 4;
    public static final int USAGE_UNKNOWN = 0;
    private Bundle mBundle;
    int mFlags;
    int mUsage;

    private HwHapticAttributes(Parcel in) {
        this.mUsage = 0;
        this.mFlags = 0;
        this.mUsage = in.readInt();
        this.mFlags = in.readInt();
        this.mBundle = in.readBundle();
    }

    public HwHapticAttributes() {
        this.mUsage = 0;
        this.mFlags = 0;
    }

    public int getUsage() {
        return this.mUsage;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public Bundle getBundle() {
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            return null;
        }
        return new Bundle(bundle);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUsage);
        dest.writeInt(this.mFlags);
        dest.writeBundle(this.mBundle);
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("HwHapticAttributes: usage=");
        sb.append(this.mUsage);
        sb.append(" flags=0x");
        sb.append(Integer.toHexString(this.mFlags).toUpperCase(Locale.ROOT));
        sb.append(" bundle=");
        Bundle bundle = this.mBundle;
        if (bundle == null) {
            str = "null";
        } else {
            str = bundle.toString();
        }
        sb.append(str);
        return new String(sb.toString());
    }
}
