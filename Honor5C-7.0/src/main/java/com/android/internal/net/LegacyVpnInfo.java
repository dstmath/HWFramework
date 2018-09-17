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
    public static final Creator<LegacyVpnInfo> CREATOR = null;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_FAILED = 5;
    public static final int STATE_INITIALIZING = 1;
    public static final int STATE_TIMEOUT = 4;
    private static final String TAG = "LegacyVpnInfo";
    public PendingIntent intent;
    public String key;
    public int state;

    private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
        if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
            return -android-net-NetworkInfo$DetailedStateSwitchesValues;
        }
        int[] iArr = new int[DetailedState.values().length];
        try {
            iArr[DetailedState.AUTHENTICATING.ordinal()] = STATE_FAILED;
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
            iArr[DetailedState.CONNECTED.ordinal()] = STATE_INITIALIZING;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DetailedState.CONNECTING.ordinal()] = STATE_CONNECTING;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DetailedState.DISCONNECTED.ordinal()] = STATE_CONNECTED;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[DetailedState.DISCONNECTING.ordinal()] = 8;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[DetailedState.FAILED.ordinal()] = STATE_TIMEOUT;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.net.LegacyVpnInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.net.LegacyVpnInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.LegacyVpnInfo.<clinit>():void");
    }

    public LegacyVpnInfo() {
        this.state = -1;
    }

    public int describeContents() {
        return STATE_DISCONNECTED;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.key);
        out.writeInt(this.state);
        out.writeParcelable(this.intent, flags);
    }

    public static int stateFromNetworkInfo(NetworkInfo info) {
        switch (-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[info.getDetailedState().ordinal()]) {
            case STATE_INITIALIZING /*1*/:
                return STATE_CONNECTED;
            case STATE_CONNECTING /*2*/:
                return STATE_CONNECTING;
            case STATE_CONNECTED /*3*/:
                return STATE_DISCONNECTED;
            case STATE_TIMEOUT /*4*/:
                return STATE_FAILED;
            default:
                Log.w(TAG, "Unhandled state " + info.getDetailedState() + " ; treating as disconnected");
                return STATE_DISCONNECTED;
        }
    }
}
