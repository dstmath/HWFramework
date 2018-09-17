package com.android.internal.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ProviderProperties implements Parcelable {
    public static final Creator<ProviderProperties> CREATOR = null;
    public final int mAccuracy;
    public final boolean mHasMonetaryCost;
    public final int mPowerRequirement;
    public final boolean mRequiresCell;
    public final boolean mRequiresNetwork;
    public final boolean mRequiresSatellite;
    public final boolean mSupportsAltitude;
    public final boolean mSupportsBearing;
    public final boolean mSupportsSpeed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.location.ProviderProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.location.ProviderProperties.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.location.ProviderProperties.<clinit>():void");
    }

    public ProviderProperties(boolean mRequiresNetwork, boolean mRequiresSatellite, boolean mRequiresCell, boolean mHasMonetaryCost, boolean mSupportsAltitude, boolean mSupportsSpeed, boolean mSupportsBearing, int mPowerRequirement, int mAccuracy) {
        this.mRequiresNetwork = mRequiresNetwork;
        this.mRequiresSatellite = mRequiresSatellite;
        this.mRequiresCell = mRequiresCell;
        this.mHasMonetaryCost = mHasMonetaryCost;
        this.mSupportsAltitude = mSupportsAltitude;
        this.mSupportsSpeed = mSupportsSpeed;
        this.mSupportsBearing = mSupportsBearing;
        this.mPowerRequirement = mPowerRequirement;
        this.mAccuracy = mAccuracy;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        if (this.mRequiresNetwork) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mRequiresSatellite) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mRequiresCell) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mHasMonetaryCost) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mSupportsAltitude) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mSupportsSpeed) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (!this.mSupportsBearing) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        parcel.writeInt(this.mPowerRequirement);
        parcel.writeInt(this.mAccuracy);
    }
}
