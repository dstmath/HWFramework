package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.BitSet;

public class NativeScanResult implements Parcelable {
    private static final int CAPABILITY_SIZE = 16;
    public static final Creator<NativeScanResult> CREATOR = new Creator<NativeScanResult>() {
        public NativeScanResult createFromParcel(Parcel in) {
            boolean z = true;
            NativeScanResult result = new NativeScanResult();
            result.ssid = in.createByteArray();
            result.bssid = in.createByteArray();
            result.infoElement = in.createByteArray();
            result.frequency = in.readInt();
            result.signalMbm = in.readInt();
            result.tsf = in.readLong();
            int capabilityInt = in.readInt();
            result.capability = new BitSet(16);
            for (int i = 0; i < 16; i++) {
                if (((1 << i) & capabilityInt) != 0) {
                    result.capability.set(i);
                }
            }
            if (in.readInt() == 0) {
                z = false;
            }
            result.associated = z;
            return result;
        }

        public NativeScanResult[] newArray(int size) {
            return new NativeScanResult[size];
        }
    };
    public boolean associated;
    public byte[] bssid;
    public BitSet capability;
    public int frequency;
    public byte[] infoElement;
    public int signalMbm;
    public byte[] ssid;
    public long tsf;

    public NativeScanResult(NativeScanResult source) {
        this.ssid = (byte[]) source.ssid.clone();
        this.bssid = (byte[]) source.bssid.clone();
        this.infoElement = (byte[]) source.infoElement.clone();
        this.frequency = source.frequency;
        this.signalMbm = source.signalMbm;
        this.tsf = source.tsf;
        this.capability = (BitSet) source.capability.clone();
        this.associated = source.associated;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 1;
        out.writeByteArray(this.ssid);
        out.writeByteArray(this.bssid);
        out.writeByteArray(this.infoElement);
        out.writeInt(this.frequency);
        out.writeInt(this.signalMbm);
        out.writeLong(this.tsf);
        int capabilityInt = 0;
        for (int i2 = 0; i2 < 16; i2++) {
            if (this.capability.get(i2)) {
                capabilityInt |= 1 << i2;
            }
        }
        out.writeInt(capabilityInt);
        if (!this.associated) {
            i = 0;
        }
        out.writeInt(i);
    }
}
