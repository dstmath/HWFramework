package com.huawei.android.media;

import android.media.DecoderCapabilities;
import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;

public class DecoderCapabilitiesEx {
    public static boolean isWMAEnabled() {
        for (AudioDecoder decoder : DecoderCapabilities.getAudioDecoders()) {
            if (decoder == AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWMVEnabled() {
        for (VideoDecoder decoder : DecoderCapabilities.getVideoDecoders()) {
            if (decoder == VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }
}
