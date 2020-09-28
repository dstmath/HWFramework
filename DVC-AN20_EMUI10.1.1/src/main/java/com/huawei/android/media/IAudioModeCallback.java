package com.huawei.android.media;

import com.huawei.android.audio.HwAudioServiceManager;

public abstract class IAudioModeCallback {
    private HwAudioServiceManager.AudioModeCallback mAudioModeCallback = new HwAudioServiceManager.AudioModeCallback() {
        /* class com.huawei.android.media.IAudioModeCallback.AnonymousClass1 */

        public void onAudioModeChanged(int audioMode) {
            IAudioModeCallback.this.onAudioModeChanged(audioMode);
        }
    };

    public abstract void onAudioModeChanged(int i);

    public HwAudioServiceManager.AudioModeCallback getAudioModeCb() {
        return this.mAudioModeCallback;
    }
}
