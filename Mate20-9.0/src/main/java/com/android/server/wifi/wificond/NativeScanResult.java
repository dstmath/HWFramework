package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.BitSet;

public class NativeScanResult implements Parcelable {
    private static final int CAPABILITY_SIZE = 16;
    public static final Parcelable.Creator<NativeScanResult> CREATOR = new Parcelable.Creator<NativeScanResult>() {
        public NativeScanResult createFromParcel(Parcel in) {
            NativeScanResult result = new NativeScanResult();
            result.ssid = in.createByteArray();
            result.bssid = in.createByteArray();
            result.infoElement = in.createByteArray();
            result.frequency = in.readInt();
            result.signalMbm = in.readInt();
            result.tsf = in.readLong();
            int capabilityInt = in.readInt();
            result.capability = new BitSet(16);
            boolean z = false;
            for (int i = 0; i < 16; i++) {
                if (((1 << i) & capabilityInt) != 0) {
                    result.capability.set(i);
                }
            }
            if (in.readInt() != 0) {
                z = true;
            }
            result.associated = z;
            result.radioChainInfos = new ArrayList<>();
            in.readTypedList(result.radioChainInfos, RadioChainInfo.CREATOR);
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
    public ArrayList<RadioChainInfo> radioChainInfos;
    public int signalMbm;
    public byte[] ssid;
    public long tsf;

    public NativeScanResult() {
    }

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
        out.writeByteArray(this.ssid);
        out.writeByteArray(this.bssid);
        out.writeByteArray(this.infoElement);
        out.writeInt(this.frequency);
        out.writeInt(this.signalMbm);
        out.writeLong(this.tsf);
        int capabilityInt = 0;
        for (int i = 0; i < 16; i++) {
            if (this.capability.get(i)) {
                capabilityInt |= 1 << i;
            }
        }
        out.writeInt(capabilityInt);
        out.writeInt(this.associated ? 1 : 0);
        out.writeTypedList(this.radioChainInfos);
    }
}
