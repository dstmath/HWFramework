package com.huawei.android.media;

import android.media.DecoderCapabilities;

public class DecoderCapabilitiesEx {
    public static boolean isWMAEnabled() {
        for (DecoderCapabilities.AudioDecoder decoder : DecoderCapabilities.getAudioDecoders()) {
            if (decoder == DecoderCapabilities.AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWMVEnabled() {
        for (DecoderCapabilities.VideoDecoder decoder : DecoderCapabilities.getVideoDecoders()) {
            if (decoder == DecoderCapabilities.VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }
}
