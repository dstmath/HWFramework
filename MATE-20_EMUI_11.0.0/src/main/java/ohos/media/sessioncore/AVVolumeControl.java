package ohos.media.sessioncore;

import ohos.media.sessioncore.adapter.AVVolumeControlAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public abstract class AVVolumeControl {
    public static final int AV_VOLUME_ABSOLUTE = 2;
    public static final int AV_VOLUME_FIXED = 0;
    public static final int AV_VOLUME_RELATIVE = 1;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVVolumeControl.class);
    private final AVVolumeControlAdapter adapter;
    private AVVolumeControlCallback callback;

    public static abstract class AVVolumeControlCallback {
        public abstract void onAVPlaybackVolumeChanged(AVVolumeControl aVVolumeControl);
    }

    public void onAdjustAVPlaybackVolume(int i) {
    }

    public void onSetAVPlaybackVolumeTo(int i) {
    }

    public AVVolumeControl(int i, int i2, int i3) {
        if (i == 0 || i == 1 || i == 2) {
            this.adapter = new AVVolumeControlAdapter(this, i, i2, i3);
        } else {
            LOGGER.error("invalid volume type:%{public}d", Integer.valueOf(i));
            throw new IllegalArgumentException("invalid volume type");
        }
    }

    public final int getAVPlaybackVolumeType() {
        return this.adapter.getAVPlaybackVolumeType();
    }

    public final int getAVPlaybackMaxVolume() {
        return this.adapter.getAVPlaybackMaxVolume();
    }

    public final int getAVPlaybackCurrentVolume() {
        return this.adapter.getAVPlaybackCurrentVolume();
    }

    public final void setAVPlaybackCurrentVolume(int i) {
        this.adapter.setAVPlaybackCurrentVolume(i);
        AVVolumeControlCallback aVVolumeControlCallback = this.callback;
        if (aVVolumeControlCallback != null) {
            aVVolumeControlCallback.onAVPlaybackVolumeChanged(this);
        }
    }

    public final Object getHostVolumeControl() {
        return this.adapter.getHostVolumeControl();
    }

    public void setAVVolumeControlCallback(AVVolumeControlCallback aVVolumeControlCallback) {
        this.callback = aVVolumeControlCallback;
    }
}
