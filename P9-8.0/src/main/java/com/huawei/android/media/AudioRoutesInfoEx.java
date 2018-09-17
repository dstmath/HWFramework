package com.huawei.android.media;

import android.media.AudioRoutesInfo;

public class AudioRoutesInfoEx {
    private AudioRoutesInfo mAudioRoutesInfo;

    public AudioRoutesInfoEx(AudioRoutesInfo audioRoutesInfo) {
        this.mAudioRoutesInfo = audioRoutesInfo;
    }

    public String toString() {
        if (this.mAudioRoutesInfo != null) {
            return this.mAudioRoutesInfo.toString();
        }
        return null;
    }
}
