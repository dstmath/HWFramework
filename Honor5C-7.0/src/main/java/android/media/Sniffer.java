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
        ;
        
        private String ext;
        private String[] validMimes;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Sniffer.SupportFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Sniffer.SupportFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.Sniffer.SupportFormat.<clinit>():void");
        }

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Sniffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Sniffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Sniffer.<clinit>():void");
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
