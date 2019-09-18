package android.media.update;

public interface VolumeProvider2Provider {
    int getControlType_impl();

    int getCurrentVolume_impl();

    int getMaxVolume_impl();

    void setCurrentVolume_impl(int i);
}
