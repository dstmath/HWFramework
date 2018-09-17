package android.media;

public abstract class VolumeProvider {
    public static final int VOLUME_CONTROL_ABSOLUTE = 2;
    public static final int VOLUME_CONTROL_FIXED = 0;
    public static final int VOLUME_CONTROL_RELATIVE = 1;
    private Callback mCallback;
    private final int mControlType;
    private int mCurrentVolume;
    private final int mMaxVolume;

    public static abstract class Callback {
        public abstract void onVolumeChanged(VolumeProvider volumeProvider);
    }

    public VolumeProvider(int volumeControl, int maxVolume, int currentVolume) {
        this.mControlType = volumeControl;
        this.mMaxVolume = maxVolume;
        this.mCurrentVolume = currentVolume;
    }

    public final int getVolumeControl() {
        return this.mControlType;
    }

    public final int getMaxVolume() {
        return this.mMaxVolume;
    }

    public final int getCurrentVolume() {
        return this.mCurrentVolume;
    }

    public final void setCurrentVolume(int currentVolume) {
        this.mCurrentVolume = currentVolume;
        if (this.mCallback != null) {
            this.mCallback.onVolumeChanged(this);
        }
    }

    public void onSetVolumeTo(int volume) {
    }

    public void onAdjustVolume(int direction) {
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
