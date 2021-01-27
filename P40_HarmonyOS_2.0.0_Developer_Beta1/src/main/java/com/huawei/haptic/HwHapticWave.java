package com.huawei.haptic;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import java.util.ArrayList;

public class HwHapticWave implements Parcelable {
    public static final Parcelable.Creator<HwHapticWave> CREATOR = new Parcelable.Creator<HwHapticWave>() {
        /* class com.huawei.haptic.HwHapticWave.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwHapticWave createFromParcel(Parcel in) {
            return new HwHapticWave(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwHapticWave[] newArray(int size) {
            if (size > 64) {
                return new HwHapticWave[0];
            }
            return new HwHapticWave[size];
        }
    };
    private static final String TAG = "HwHapticWave";
    public ArrayList<HwHapticChannel> mHapticChannels = new ArrayList<>();
    private ArrayMap<String, String> mMetadata = new ArrayMap<>();
    public int mVersion;

    public HwHapticWave() {
    }

    public HwHapticWave(Parcel in) {
        this.mVersion = in.readInt();
        this.mHapticChannels = in.readArrayList(null);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mVersion);
        out.writeList(this.mHapticChannels);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void setMetadata(String key, String value) {
        this.mMetadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.mMetadata.get(key);
    }

    public void addHapticChannel(HwHapticChannel channel) {
        this.mHapticChannels.add(channel);
    }
}
