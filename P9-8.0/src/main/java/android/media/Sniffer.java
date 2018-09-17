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

        private SupportFormat(String suffix, String... validMimes) {
            this.ext = suffix;
            this.validMimes = validMimes;
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

    private native String _getAlbum();

    private native String _getArtist();

    private native long _getDuration();

    private native String _getFileMime();

    private native String _getTitle();

    private native void _release();

    private native boolean _setDataSource(String str);

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    public native boolean isTagValid();

    public native void reset();

    static {
        System.loadLibrary("musicsniffer");
        native_init();
    }

    public Sniffer() {
        native_setup();
    }

    public static String getRealExt(String path) {
        Sniffer sniffer = new Sniffer();
        sniffer.setDataSource(path);
        String mime = sniffer.getFileMime();
        boolean isValid = sniffer.isTagValid();
        sniffer.release();
        if (isValid) {
            return SupportFormat.getRealExt(mime);
        }
        return null;
    }

    public String getFileMime() {
        return _getFileMime();
    }

    public boolean setDataSource(String path) {
        if (new File(path).exists()) {
            return _setDataSource(path);
        }
        Log.e(TAG, "setDataSource file does not exist!");
        return false;
    }

    public String getTitle() {
        return _getTitle();
    }

    public String getArtist() {
        return _getArtist();
    }

    public String getAlbum() {
        return _getAlbum();
    }

    public long getDuration() {
        return _getDuration();
    }

    public void release() {
        Log.i(TAG, "release!");
        _release();
    }

    protected void finalize() {
        native_finalize();
    }
}
