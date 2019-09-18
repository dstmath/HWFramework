package android.media;

import android.util.Log;
import java.io.File;

public class Sniffer {
    public static final String FILE_EXT_AUDIO_AAC_ADTS = "aac";
    public static final String FILE_EXT_AUDIO_APE = "ape";
    public static final String FILE_EXT_AUDIO_FLAC = "flac";
    public static final String FILE_EXT_AUDIO_M4A = "m4a";
    public static final String FILE_EXT_AUDIO_MPEG = "mp3";
    public static final String MEDIA_MIMETYPE_AUDIO_AAC_ADTS = "audio/aac-adts";
    public static final String MEDIA_MIMETYPE_AUDIO_APE = "audio/ape";
    public static final String MEDIA_MIMETYPE_AUDIO_FLAC = "audio/flac";
    public static final String MEDIA_MIMETYPE_AUDIO_M4A = "audio/mp4";
    public static final String MEDIA_MIMETYPE_AUDIO_MPEG = "audio/mpeg";
    private static final String TAG = "Sniffer_Java";
    private long mNativeContext;

    public enum SupportFormat {
        AAC(Sniffer.FILE_EXT_AUDIO_AAC_ADTS, Sniffer.MEDIA_MIMETYPE_AUDIO_AAC_ADTS),
        APE(Sniffer.FILE_EXT_AUDIO_APE, Sniffer.MEDIA_MIMETYPE_AUDIO_APE),
        FLAC(Sniffer.FILE_EXT_AUDIO_FLAC, Sniffer.MEDIA_MIMETYPE_AUDIO_FLAC),
        MP3(Sniffer.FILE_EXT_AUDIO_MPEG, Sniffer.MEDIA_MIMETYPE_AUDIO_MPEG),
        M4A(Sniffer.FILE_EXT_AUDIO_M4A, Sniffer.MEDIA_MIMETYPE_AUDIO_M4A);
        
        private String ext;
        private String[] validMimes;

        private SupportFormat(String suffix, String... validMimes2) {
            this.ext = suffix;
            this.validMimes = validMimes2;
        }

        public static String getRealExt(String mime) {
            for (SupportFormat format : values()) {
                for (String validmime : format.validMimes) {
                    if (validmime.equals(mime)) {
                        return format.ext;
                    }
                }
            }
            return null;
        }

        public static boolean isSupportExt(String inputExt) {
            for (SupportFormat format : values()) {
                if (format.ext.equals(inputExt)) {
                    return true;
                }
            }
            return false;
        }
    }

    private native void nativeFinalize();

    private native String nativeGetAlbum();

    private native String nativeGetArtist();

    private native long nativeGetDuration();

    private native String nativeGetFileMime();

    private native String nativeGetTitle();

    private static native void nativeInit();

    private native void nativeRelease();

    private native boolean nativeSetDataSource(String str);

    private native void nativeSetup();

    public native boolean isTagValid();

    public native void reset();

    static {
        System.loadLibrary("musicsniffer");
        nativeInit();
    }

    public Sniffer() {
        nativeSetup();
    }

    public static String getRealExt(String path) {
        Sniffer sniffer = new Sniffer();
        sniffer.setDataSource(path);
        String mime = sniffer.getFileMime();
        boolean isValid = sniffer.isTagValid();
        sniffer.release();
        if (!isValid) {
            return null;
        }
        return SupportFormat.getRealExt(mime);
    }

    public String getFileMime() {
        return nativeGetFileMime();
    }

    public boolean setDataSource(String path) {
        if (new File(path).exists()) {
            return nativeSetDataSource(path);
        }
        Log.e(TAG, "setDataSource file does not exist!");
        return false;
    }

    public String getTitle() {
        return nativeGetTitle();
    }

    public String getArtist() {
        return nativeGetArtist();
    }

    public String getAlbum() {
        return nativeGetAlbum();
    }

    public long getDuration() {
        return nativeGetDuration();
    }

    public void release() {
        Log.i(TAG, "release!");
        nativeRelease();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0008, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000c, code lost:
        super.finalize();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000f, code lost:
        throw r0;
     */
    public void finalize() throws Throwable {
        nativeFinalize();
        super.finalize();
    }
}
