package com.huawei.android.media;

import android.media.AudioAttributes;

public final class AudioFocusInfoEx {
    private final AudioAttributes mAttributes;
    private final String mClientId;
    private final int mFocusType;

    public AudioFocusInfoEx(AudioAttributes audioAttributes, String clientId, int focusType) {
        this.mAttributes = audioAttributes == null ? new AudioAttributes.Builder().build() : audioAttributes;
        this.mClientId = clientId;
        this.mFocusType = focusType;
    }

    public AudioAttributes getAttributes() {
        return this.mAttributes;
    }

    public String getClientId() {
        return this.mClientId;
    }

    public int getFocusType() {
        return this.mFocusType;
    }
}
