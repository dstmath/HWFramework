package ohos.media.common.sessioncore;

import ohos.media.common.AudioStreamProperty;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AVPlaybackInfo implements Sequenceable {
    public static final int AVPLAYBACK_TYPE_CUSTOM = 2;
    public static final int AVPLAYBACK_TYPE_SYSTEM = 1;
    public static Sequenceable.Producer<AVPlaybackInfo> CREATOR = $$Lambda$AVPlaybackInfo$HcVCegnxiPWzXa53U1WG1t2reM.INSTANCE;
    private int currentVolume;
    private int maxVolume;
    private AudioStreamProperty streamProperty;
    private int volumeControl;
    private int volumeType;

    static /* synthetic */ AVPlaybackInfo lambda$static$0(Parcel parcel) {
        return new AVPlaybackInfo(parcel);
    }

    public AVPlaybackInfo(int i, int i2, int i3, int i4, AudioStreamProperty audioStreamProperty) {
        this.volumeType = i;
        this.volumeControl = i2;
        this.maxVolume = i3;
        this.currentVolume = i4;
        this.streamProperty = audioStreamProperty;
    }

    AVPlaybackInfo(Parcel parcel) {
        this.volumeType = parcel.readInt();
        this.volumeControl = parcel.readInt();
        this.maxVolume = parcel.readInt();
        this.currentVolume = parcel.readInt();
        this.streamProperty = new AudioStreamProperty.Builder().build();
        parcel.readSequenceable(this.streamProperty);
    }

    public int getAVPlaybackVolumeHandle() {
        return this.volumeType;
    }

    public int getAVPlaybackVolumeControl() {
        return this.volumeControl;
    }

    public int getAVPlaybackMaxVolume() {
        return this.maxVolume;
    }

    public int getAVPlaybackCurrentVolume() {
        return this.currentVolume;
    }

    public AudioStreamProperty getAudioStreamProperty() {
        return this.streamProperty;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.volumeType);
        parcel.writeInt(this.volumeControl);
        parcel.writeInt(this.maxVolume);
        parcel.writeInt(this.currentVolume);
        parcel.writeSequenceable(this.streamProperty);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.volumeType = parcel.readInt();
        this.volumeControl = parcel.readInt();
        this.maxVolume = parcel.readInt();
        this.currentVolume = parcel.readInt();
        this.streamProperty = new AudioStreamProperty.Builder().build();
        parcel.readSequenceable(this.streamProperty);
        return true;
    }
}
