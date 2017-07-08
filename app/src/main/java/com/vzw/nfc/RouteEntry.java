package com.vzw.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class RouteEntry implements Parcelable {
    public static final Creator<RouteEntry> CREATOR = null;
    byte[] mAid;
    boolean mAllowed;
    int mLocation;
    int mPowerState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.vzw.nfc.RouteEntry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.vzw.nfc.RouteEntry.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.vzw.nfc.RouteEntry.<clinit>():void");
    }

    public RouteEntry(byte[] Aid, int PowerState, int Location, boolean allowed) {
        this.mAid = Aid;
        this.mPowerState = PowerState;
        this.mLocation = Location;
        this.mAllowed = allowed;
        Log.d("RouteEntry", "constructor mPowerState" + PowerState);
        Log.d("RouteEntry", "constructor mPowerState" + this.mPowerState);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAllowed ? 1 : 0);
        dest.writeInt(this.mLocation);
        dest.writeInt(this.mPowerState);
        dest.writeInt(this.mAid.length);
        dest.writeByteArray(this.mAid);
    }

    public byte[] getAid() {
        return this.mAid;
    }

    public int getPowerState() {
        return this.mPowerState;
    }

    public int getLocation() {
        return this.mLocation;
    }

    public boolean isAllowed() {
        return this.mAllowed;
    }
}
