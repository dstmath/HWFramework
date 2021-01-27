package com.vzw.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class RouteEntry implements Parcelable {
    private static final int AID_MAX_LENGTH = 16;
    public static final Parcelable.Creator<RouteEntry> CREATOR = new Parcelable.Creator<RouteEntry>() {
        /* class com.vzw.nfc.RouteEntry.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RouteEntry createFromParcel(Parcel in) {
            boolean allowed = true;
            if (((byte) in.readInt()) != 1) {
                allowed = false;
            }
            int location = in.readInt();
            int powerState = in.readInt();
            int aidLength = in.readInt();
            if (aidLength > 16) {
                Log.e("RouteEntry", "aidLength exceed limit, truncate it to 16. current is " + aidLength);
                aidLength = 16;
            }
            byte[] aid = new byte[aidLength];
            in.readByteArray(aid);
            return new RouteEntry(aid, powerState, location, allowed);
        }

        @Override // android.os.Parcelable.Creator
        public RouteEntry[] newArray(int size) {
            return new RouteEntry[size];
        }
    };
    byte[] mAid;
    boolean mAllowed;
    int mLocation;
    int mPowerState;

    public RouteEntry() {
    }

    public RouteEntry(byte[] Aid, int PowerState, int Location, boolean allowed) {
        this.mAid = Aid;
        this.mPowerState = PowerState;
        this.mLocation = Location;
        this.mAllowed = allowed;
        Log.d("RouteEntry", "constructor mPowerState" + PowerState);
        Log.d("RouteEntry", "constructor mPowerState" + this.mPowerState);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        if (true != this.mAllowed) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mLocation);
        dest.writeInt(this.mPowerState);
        int i2 = 16;
        if (this.mAid.length > 16) {
            Log.e("RouteEntry", "writeToParcel, current aid length is " + this.mAid.length);
        }
        byte[] bArr = this.mAid;
        if (bArr.length <= 16) {
            i2 = bArr.length;
        }
        dest.writeInt(i2);
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
