package com.huawei.android.media;

import android.media.AudioAttributes;
import android.media.AudioFocusChangeCallback;

public abstract class IAudioFocusChangeCallback {
    private static final String TAG = "IAudioFocusChangeCallback";
    private AudioFocusChangeCallback mCb = new AudioFocusChangeCallback() {
        /* class com.huawei.android.media.IAudioFocusChangeCallback.AnonymousClass1 */

        public void onAudioFocusChange(AudioAttributes mAttributes, String mClientId, int mFocusType, boolean action) {
            IAudioFocusChangeCallback.this.onAudioFocusChange(new AudioFocusInfoEx(mAttributes, mClientId, mFocusType), action);
        }
    };

    public abstract void onAudioFocusChange(AudioFocusInfoEx audioFocusInfoEx, boolean z);

    public AudioFocusChangeCallback getAudioFocusChangeCallback() {
        return this.mCb;
    }
}
