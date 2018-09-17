package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PresSubscriptionState implements Parcelable {
    public static final Creator<PresSubscriptionState> CREATOR = null;
    public static final int UCE_PRES_SUBSCRIPTION_STATE_ACTIVE = 0;
    public static final int UCE_PRES_SUBSCRIPTION_STATE_PENDING = 1;
    public static final int UCE_PRES_SUBSCRIPTION_STATE_TERMINATED = 2;
    public static final int UCE_PRES_SUBSCRIPTION_STATE_UNKNOWN = 3;
    private int mPresSubscriptionState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.presence.PresSubscriptionState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.presence.PresSubscriptionState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.presence.PresSubscriptionState.<clinit>():void");
    }

    public int describeContents() {
        return UCE_PRES_SUBSCRIPTION_STATE_ACTIVE;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPresSubscriptionState);
    }

    private PresSubscriptionState(Parcel source) {
        this.mPresSubscriptionState = UCE_PRES_SUBSCRIPTION_STATE_UNKNOWN;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mPresSubscriptionState = source.readInt();
    }

    public PresSubscriptionState() {
        this.mPresSubscriptionState = UCE_PRES_SUBSCRIPTION_STATE_UNKNOWN;
    }

    public int getPresSubscriptionStateValue() {
        return this.mPresSubscriptionState;
    }

    public void setPresSubscriptionState(int nPresSubscriptionState) {
        this.mPresSubscriptionState = nPresSubscriptionState;
    }
}
