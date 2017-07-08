package com.android.ims;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ImsConferenceState implements Parcelable {
    public static final Creator<ImsConferenceState> CREATOR = null;
    public static final String DISPLAY_TEXT = "display-text";
    public static final String ENDPOINT = "endpoint";
    public static final String SIP_STATUS_CODE = "sipstatuscode";
    public static final String STATUS = "status";
    public static final String STATUS_ALERTING = "alerting";
    public static final String STATUS_CONNECTED = "connected";
    public static final String STATUS_CONNECT_FAIL = "connect-fail";
    public static final String STATUS_DIALING_IN = "dialing-in";
    public static final String STATUS_DIALING_OUT = "dialing-out";
    public static final String STATUS_DISCONNECTED = "disconnected";
    public static final String STATUS_DISCONNECTING = "disconnecting";
    public static final String STATUS_MUTED_VIA_FOCUS = "muted-via-focus";
    public static final String STATUS_ON_HOLD = "on-hold";
    public static final String STATUS_PENDING = "pending";
    public static final String USER = "user";
    public HashMap<String, Bundle> mParticipants;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsConferenceState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsConferenceState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsConferenceState.<clinit>():void");
    }

    public ImsConferenceState() {
        this.mParticipants = new HashMap();
    }

    public ImsConferenceState(Parcel in) {
        this.mParticipants = new HashMap();
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mParticipants.size());
        if (this.mParticipants.size() > 0) {
            Set<Entry<String, Bundle>> entries = this.mParticipants.entrySet();
            if (entries != null) {
                for (Entry<String, Bundle> entry : entries) {
                    out.writeString((String) entry.getKey());
                    out.writeParcelable((Parcelable) entry.getValue(), 0);
                }
            }
        }
    }

    private void readFromParcel(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            this.mParticipants.put(in.readString(), (Bundle) in.readParcelable(null));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getConnectionStateForStatus(String status) {
        if (status.equals(STATUS_PENDING)) {
            return 0;
        }
        if (status.equals(STATUS_DIALING_IN)) {
            return 2;
        }
        if (status.equals(STATUS_ALERTING) || status.equals(STATUS_DIALING_OUT)) {
            return 3;
        }
        if (status.equals(STATUS_ON_HOLD)) {
            return 5;
        }
        if (status.equals(STATUS_CONNECTED) || status.equals(STATUS_MUTED_VIA_FOCUS) || status.equals(STATUS_DISCONNECTING) || !status.equals(STATUS_DISCONNECTED)) {
            return 4;
        }
        return 6;
    }
}
