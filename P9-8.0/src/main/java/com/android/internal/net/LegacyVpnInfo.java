package com.android.internal.net;

import android.app.PendingIntent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class LegacyVpnInfo implements Parcelable {
    private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
    public static final Creator<LegacyVpnInfo> CREATOR = new Creator<LegacyVpnInfo>() {
        public LegacyVpnInfo createFromParcel(Parcel in) {
            LegacyVpnInfo info = new LegacyVpnInfo();
            info.key = in.readString();
            info.state = in.readInt();
            info.intent = (PendingIntent) in.readParcelable(null);
            return info;
        }

        public LegacyVpnInfo[] newArray(int size) {
            return new LegacyVpnInfo[size];
        }
    };
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_FAILED = 5;
    public static final int STATE_INITIALIZING = 1;
    public static final int STATE_TIMEOUT = 4;
    private static final String TAG = "LegacyVpnInfo";
    public PendingIntent intent;
    public String key;
    public int state = -1;

    private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
        if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
            return -android-net-NetworkInfo$DetailedStateSwitchesValues;
        }
        int[] iArr = new int[DetailedState.values().length];
        try {
            iArr[DetailedState.AUTHENTICATING.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DetailedState.BLOCKED.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DetailedState.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DetailedState.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DetailedState.DISCONNECTED.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[DetailedState.DISCONNECTING.ordinal()] = 8;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[DetailedState.FAILED.ordinal()] = 4;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[DetailedState.IDLE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[DetailedState.SCANNING.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[DetailedState.SUSPENDED.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
        return iArr;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.key);
        out.writeInt(this.state);
        out.writeParcelable(this.intent, flags);
    }

    public static int stateFromNetworkInfo(NetworkInfo info) {
        switch (-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[info.getDetailedState().ordinal()]) {
            case 1:
                return 3;
            case 2:
                return 2;
            case 3:
                return 0;
            case 4:
                return 5;
            default:
                Log.w(TAG, "Unhandled state " + info.getDetailedState() + " ; treating as disconnected");
                return 0;
        }
    }
}
