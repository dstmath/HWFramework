package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableVolumeInfo implements Parcelable {
    public static final Parcelable.Creator<ParcelableVolumeInfo> CREATOR = new Parcelable.Creator<ParcelableVolumeInfo>() {
        public ParcelableVolumeInfo createFromParcel(Parcel in) {
            return new ParcelableVolumeInfo(in);
        }

        public ParcelableVolumeInfo[] newArray(int size) {
            return new ParcelableVolumeInfo[size];
        }
    };
    public int audioStream;
    public int controlType;
    public int currentVolume;
    public int maxVolume;
    public int volumeType;

    public ParcelableVolumeInfo(int volumeType2, int audioStream2, int controlType2, int maxVolume2, int currentVolume2) {
        this.volumeType = volumeType2;
        this.audioStream = audioStream2;
        this.controlType = controlType2;
        this.maxVolume = maxVolume2;
        this.currentVolume = currentVolume2;
    }

    public ParcelableVolumeInfo(Parcel from) {
        this.volumeType = from.readInt();
        this.controlType = from.readInt();
        this.maxVolume = from.readInt();
        this.currentVolume = from.readInt();
        this.audioStream = from.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.volumeType);
        dest.writeInt(this.controlType);
        dest.writeInt(this.maxVolume);
        dest.writeInt(this.currentVolume);
        dest.writeInt(this.audioStream);
    }
}
