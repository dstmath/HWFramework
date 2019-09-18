package com.huawei.android.media;

import com.huawei.android.audio.HwAudioServiceManager;

public abstract class IAudioModeCallback {
    private HwAudioServiceManager.AudioModeCallback mAudioModeCallback = new HwAudioServiceManager.AudioModeCallback() {
        public void onAudioModeChanged(int audioMode) {
            IAudioModeCallback.this.onAudioModeChanged(audioMode);
        }
    };

    public abstract void onAudioModeChanged(int i);

    public HwAudioServiceManager.AudioModeCallback getAudioModeCb() {
        return this.mAudioModeCallback;
    }
}
