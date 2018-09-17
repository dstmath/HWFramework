package android.media;

import java.util.ArrayList;
import java.util.List;

public class DecoderCapabilities {

    public enum AudioDecoder {
        AUDIO_DECODER_WMA
    }

    public enum VideoDecoder {
        VIDEO_DECODER_WMV
    }

    private static final native int native_get_audio_decoder_type(int i);

    private static final native int native_get_num_audio_decoders();

    private static final native int native_get_num_video_decoders();

    private static final native int native_get_video_decoder_type(int i);

    private static final native void native_init();

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public static List<VideoDecoder> getVideoDecoders() {
        List<VideoDecoder> decoderList = new ArrayList();
        int nDecoders = native_get_num_video_decoders();
        for (int i = 0; i < nDecoders; i++) {
            decoderList.add(VideoDecoder.values()[native_get_video_decoder_type(i)]);
        }
        return decoderList;
    }

    public static List<AudioDecoder> getAudioDecoders() {
        List<AudioDecoder> decoderList = new ArrayList();
        int nDecoders = native_get_num_audio_decoders();
        for (int i = 0; i < nDecoders; i++) {
            decoderList.add(AudioDecoder.values()[native_get_audio_decoder_type(i)]);
        }
        return decoderList;
    }

    private DecoderCapabilities() {
    }
}
