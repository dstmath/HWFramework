package huawei.android.aod;

import android.os.Parcel;
import android.os.Parcelable;

public final class AodVolumeInfo implements Parcelable {
    public static final Parcelable.Creator<AodVolumeInfo> CREATOR = new Parcelable.Creator<AodVolumeInfo>() {
        /* class huawei.android.aod.AodVolumeInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AodVolumeInfo createFromParcel(Parcel in) {
            return new AodVolumeInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AodVolumeInfo[] newArray(int size) {
            return new AodVolumeInfo[size];
        }
    };
    public int currentVolume;
    public int maxVolume;
    public int minVolume;
    public int protectVolume;
    public int volumeType;

    public AodVolumeInfo() {
    }

    public AodVolumeInfo(Parcel in) {
        this.volumeType = in.readInt();
        this.maxVolume = in.readInt();
        this.minVolume = in.readInt();
        this.currentVolume = in.readInt();
        this.protectVolume = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flag) {
        if (dest != null) {
            dest.writeInt(this.volumeType);
            dest.writeInt(this.maxVolume);
            dest.writeInt(this.minVolume);
            dest.writeInt(this.currentVolume);
            dest.writeInt(this.protectVolume);
        }
    }
}
