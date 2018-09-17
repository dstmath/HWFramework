package android.media.session;

import android.media.AudioAttributes;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ParcelableVolumeInfo implements Parcelable {
    public static final Creator<ParcelableVolumeInfo> CREATOR = new Creator<ParcelableVolumeInfo>() {
        public ParcelableVolumeInfo createFromParcel(Parcel in) {
            return new ParcelableVolumeInfo(in);
        }

        public ParcelableVolumeInfo[] newArray(int size) {
            return new ParcelableVolumeInfo[size];
        }
    };
    public AudioAttributes audioAttrs;
    public int controlType;
    public int currentVolume;
    public int maxVolume;
    public int volumeType;

    public ParcelableVolumeInfo(int volumeType, AudioAttributes audioAttrs, int controlType, int maxVolume, int currentVolume) {
        this.volumeType = volumeType;
        this.audioAttrs = audioAttrs;
        this.controlType = controlType;
        this.maxVolume = maxVolume;
        this.currentVolume = currentVolume;
    }

    public ParcelableVolumeInfo(Parcel from) {
        this.volumeType = from.readInt();
        this.controlType = from.readInt();
        this.maxVolume = from.readInt();
        this.currentVolume = from.readInt();
        this.audioAttrs = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(from);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.volumeType);
        dest.writeInt(this.controlType);
        dest.writeInt(this.maxVolume);
        dest.writeInt(this.currentVolume);
        this.audioAttrs.writeToParcel(dest, flags);
    }
}
