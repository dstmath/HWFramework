package android.media;

public abstract class AudioManagerInternal {

    public interface RingerModeDelegate {
        boolean canVolumeDownEnterSilent();

        int getRingerModeAffectedStreams(int i);

        int onSetRingerModeExternal(int i, int i2, String str, int i3, VolumePolicy volumePolicy);

        int onSetRingerModeInternal(int i, int i2, String str, int i3, VolumePolicy volumePolicy);
    }

    public abstract void adjustStreamVolumeForUid(int i, int i2, int i3, String str, int i4);

    public abstract void adjustSuggestedStreamVolumeForUid(int i, int i2, int i3, String str, int i4);

    public abstract int getRingerModeInternal();

    public abstract int getVolumeControllerUid();

    public abstract void setRingerModeDelegate(RingerModeDelegate ringerModeDelegate);

    public abstract void setRingerModeInternal(int i, String str);

    public abstract void setStreamVolumeForUid(int i, int i2, int i3, String str, int i4);

    public abstract void updateRingerModeAffectedStreamsInternal();
}
