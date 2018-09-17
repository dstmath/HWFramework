package com.android.ims;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.Rlog;

public class ImsExternalCallState implements Parcelable {
    public static final int CALL_STATE_CONFIRMED = 1;
    public static final int CALL_STATE_TERMINATED = 2;
    public static final Creator<ImsExternalCallState> CREATOR = null;
    private static final String TAG = "ImsExternalCallState";
    private Uri mAddress;
    private int mCallId;
    private int mCallState;
    private int mCallType;
    private boolean mIsHeld;
    private boolean mIsPullable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsExternalCallState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsExternalCallState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsExternalCallState.<clinit>():void");
    }

    public ImsExternalCallState(int callId, Uri address, boolean isPullable, int callState, int callType, boolean isCallheld) {
        this.mCallId = callId;
        this.mAddress = address;
        this.mIsPullable = isPullable;
        this.mCallState = callState;
        this.mCallType = callType;
        this.mIsHeld = isCallheld;
        Rlog.d(TAG, "ImsExternalCallState = " + this);
    }

    public ImsExternalCallState(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mCallId = in.readInt();
        this.mAddress = (Uri) in.readParcelable(ImsExternalCallState.class.getClassLoader());
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsPullable = z;
        this.mCallState = in.readInt();
        this.mCallType = in.readInt();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mIsHeld = z2;
        Rlog.d(TAG, "ImsExternalCallState const = " + this);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = CALL_STATE_CONFIRMED;
        out.writeInt(this.mCallId);
        out.writeParcelable(this.mAddress, 0);
        if (this.mIsPullable) {
            i = CALL_STATE_CONFIRMED;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeInt(this.mCallState);
        out.writeInt(this.mCallType);
        if (!this.mIsHeld) {
            i2 = 0;
        }
        out.writeInt(i2);
        Rlog.d(TAG, "ImsExternalCallState writeToParcel = " + out.toString());
    }

    public int getCallId() {
        return this.mCallId;
    }

    public Uri getAddress() {
        return this.mAddress;
    }

    public boolean isCallPullable() {
        return this.mIsPullable;
    }

    public int getCallState() {
        return this.mCallState;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public boolean isCallHeld() {
        return this.mIsHeld;
    }

    public String toString() {
        return "ImsExternalCallState { mCallId = " + this.mCallId + ", mAddress = " + this.mAddress + ", mIsPullable = " + this.mIsPullable + ", mCallState = " + this.mCallState + ", mCallType = " + this.mCallType + ", mIsHeld = " + this.mIsHeld + "}";
    }
}
