package android.media;

import java.util.ArrayList;
import java.util.List;

public class EncoderCapabilities {
    private static final String TAG = "EncoderCapabilities";

    public static class AudioEncoderCap {
        public final int mCodec;
        public final int mMaxBitRate;
        public final int mMaxChannels;
        public final int mMaxSampleRate;
        public final int mMinBitRate;
        public final int mMinChannels;
        public final int mMinSampleRate;

        private AudioEncoderCap(int codec, int minBitRate, int maxBitRate, int minSampleRate, int maxSampleRate, int minChannels, int maxChannels) {
            this.mCodec = codec;
            this.mMinBitRate = minBitRate;
            this.mMaxBitRate = maxBitRate;
            this.mMinSampleRate = minSampleRate;
            this.mMaxSampleRate = maxSampleRate;
            this.mMinChannels = minChannels;
            this.mMaxChannels = maxChannels;
        }
    }

    public static class VideoEncoderCap {
        public final int mCodec;
        public final int mMaxBitRate;
        public final int mMaxFrameHeight;
        public final int mMaxFrameRate;
        public final int mMaxFrameWidth;
        public final int mMinBitRate;
        public final int mMinFrameHeight;
        public final int mMinFrameRate;
        public final int mMinFrameWidth;

        private VideoEncoderCap(int codec, int minBitRate, int maxBitRate, int minFrameRate, int maxFrameRate, int minFrameWidth, int maxFrameWidth, int minFrameHeight, int maxFrameHeight) {
            this.mCodec = codec;
            this.mMinBitRate = minBitRate;
            this.mMaxBitRate = maxBitRate;
            this.mMinFrameRate = minFrameRate;
            this.mMaxFrameRate = maxFrameRate;
            this.mMinFrameWidth = minFrameWidth;
            this.mMaxFrameWidth = maxFrameWidth;
            this.mMinFrameHeight = minFrameHeight;
            this.mMaxFrameHeight = maxFrameHeight;
        }
    }

    private static final native AudioEncoderCap native_get_audio_encoder_cap(int i);

    private static final native int native_get_file_format(int i);

    private static final native int native_get_num_audio_encoders();

    private static final native int native_get_num_file_formats();

    private static final native int native_get_num_video_encoders();

    private static final native VideoEncoderCap native_get_video_encoder_cap(int i);

    private static final native void native_init();

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public static int[] getOutputFileFormats() {
        int nFormats = native_get_num_file_formats();
        if (nFormats == 0) {
            return null;
        }
        int[] formats = new int[nFormats];
        for (int i = 0; i < nFormats; i++) {
            formats[i] = native_get_file_format(i);
        }
        return formats;
    }

    public static List<VideoEncoderCap> getVideoEncoders() {
        int nEncoders = native_get_num_video_encoders();
        if (nEncoders == 0) {
            return null;
        }
        List<VideoEncoderCap> encoderList = new ArrayList();
        for (int i = 0; i < nEncoders; i++) {
            encoderList.add(native_get_video_encoder_cap(i));
        }
        return encoderList;
    }

    public static List<AudioEncoderCap> getAudioEncoders() {
        int nEncoders = native_get_num_audio_encoders();
        if (nEncoders == 0) {
            return null;
        }
        List<AudioEncoderCap> encoderList = new ArrayList();
        for (int i = 0; i < nEncoders; i++) {
            encoderList.add(native_get_audio_encoder_cap(i));
        }
        return encoderList;
    }

    private EncoderCapabilities() {
    }
}
