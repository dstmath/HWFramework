package android.media;

import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.opengl.GLES11;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MediaFile {
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_AIF = 13;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_APE = 14;
    public static final int FILE_TYPE_ARW = 304;
    public static final int FILE_TYPE_ASF = 26;
    public static final int FILE_TYPE_AU = 12;
    public static final int FILE_TYPE_AVI = 29;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_BMP = 37;
    public static final int FILE_TYPE_CR2 = 301;
    public static final int FILE_TYPE_DNG = 300;
    public static final int FILE_TYPE_FL = 51;
    public static final int FILE_TYPE_FLAC = 10;
    public static final int FILE_TYPE_GIF = 35;
    public static final int FILE_TYPE_HTML = 101;
    public static final int FILE_TYPE_HTTPLIVE = 47;
    public static final int FILE_TYPE_IMY = 17;
    public static final int FILE_TYPE_JPEG = 34;
    public static final int FILE_TYPE_M3U = 44;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_M4V = 22;
    public static final int FILE_TYPE_MID = 15;
    public static final int FILE_TYPE_MKA = 9;
    public static final int FILE_TYPE_MKV = 27;
    public static final int FILE_TYPE_MP2PS = 200;
    public static final int FILE_TYPE_MP2TS = 28;
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_MP4 = 21;
    public static final int FILE_TYPE_MS_EXCEL = 105;
    public static final int FILE_TYPE_MS_POWERPOINT = 106;
    public static final int FILE_TYPE_MS_WORD = 104;
    public static final int FILE_TYPE_NEF = 302;
    public static final int FILE_TYPE_NRW = 303;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_ORF = 306;
    public static final int FILE_TYPE_PDF = 102;
    public static final int FILE_TYPE_PEF = 308;
    public static final int FILE_TYPE_PLS = 45;
    public static final int FILE_TYPE_PNG = 36;
    public static final int FILE_TYPE_RA = 11;
    public static final int FILE_TYPE_RAF = 307;
    public static final int FILE_TYPE_RM = 30;
    public static final int FILE_TYPE_RMHD = 19;
    public static final int FILE_TYPE_RMVB = 20;
    public static final int FILE_TYPE_RV = 31;
    public static final int FILE_TYPE_RW2 = 305;
    public static final int FILE_TYPE_SMF = 16;
    public static final int FILE_TYPE_SRW = 309;
    public static final int FILE_TYPE_TEXT = 100;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_WBMP = 38;
    public static final int FILE_TYPE_WEBM = 32;
    public static final int FILE_TYPE_WEBP = 39;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_WMV = 25;
    public static final int FILE_TYPE_WPL = 46;
    public static final int FILE_TYPE_XML = 103;
    public static final int FILE_TYPE_ZIP = 107;
    private static final int FIRST_AUDIO_FILE_TYPE = 1;
    private static final int FIRST_DRM_FILE_TYPE = 51;
    private static final int FIRST_IMAGE_FILE_TYPE = 34;
    private static final int FIRST_MIDI_FILE_TYPE = 15;
    private static final int FIRST_PLAYLIST_FILE_TYPE = 44;
    private static final int FIRST_RAW_IMAGE_FILE_TYPE = 300;
    private static final int FIRST_VIDEO_FILE_TYPE = 19;
    private static final int FIRST_VIDEO_FILE_TYPE2 = 200;
    private static final int LAST_AUDIO_FILE_TYPE = 14;
    private static final int LAST_DRM_FILE_TYPE = 51;
    private static final int LAST_IMAGE_FILE_TYPE = 39;
    private static final int LAST_MIDI_FILE_TYPE = 17;
    private static final int LAST_PLAYLIST_FILE_TYPE = 47;
    private static final int LAST_RAW_IMAGE_FILE_TYPE = 309;
    private static final int LAST_VIDEO_FILE_TYPE = 32;
    private static final int LAST_VIDEO_FILE_TYPE2 = 200;
    private static final HashMap<String, MediaFileType> sFileTypeMap = null;
    private static final HashMap<String, Integer> sFileTypeToFormatMap = null;
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = null;
    private static final HashMap<String, Integer> sMimeTypeMap = null;
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = null;

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaFile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaFile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaFile.<clinit>():void");
    }

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    static void addFileType(String extension, int fileType, String mimeType, int mtpFormatCode) {
        addFileType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, Integer.valueOf(mtpFormatCode));
        sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        sFormatToMimeTypeMap.put(Integer.valueOf(mtpFormatCode), mimeType);
    }

    private static boolean isWMAEnabled() {
        List<AudioDecoder> decoders = DecoderCapabilities.getAudioDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i += FIRST_AUDIO_FILE_TYPE) {
            if (((AudioDecoder) decoders.get(i)) == AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWMVEnabled() {
        List<VideoDecoder> decoders = DecoderCapabilities.getVideoDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i += FIRST_AUDIO_FILE_TYPE) {
            if (((VideoDecoder) decoders.get(i)) == VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudioFileType(int fileType) {
        if (fileType >= FIRST_AUDIO_FILE_TYPE && fileType <= LAST_AUDIO_FILE_TYPE) {
            return true;
        }
        if (fileType < FIRST_MIDI_FILE_TYPE) {
            return false;
        }
        if (fileType > LAST_MIDI_FILE_TYPE) {
            return false;
        }
        return true;
    }

    public static boolean isVideoFileType(int fileType) {
        if (fileType >= FIRST_VIDEO_FILE_TYPE && fileType <= LAST_VIDEO_FILE_TYPE) {
            return true;
        }
        if (fileType < LAST_VIDEO_FILE_TYPE2) {
            return false;
        }
        if (fileType > LAST_VIDEO_FILE_TYPE2) {
            return false;
        }
        return true;
    }

    public static boolean isImageFileType(int fileType) {
        if (fileType >= FIRST_IMAGE_FILE_TYPE && fileType <= LAST_IMAGE_FILE_TYPE) {
            return true;
        }
        if (fileType < FIRST_RAW_IMAGE_FILE_TYPE) {
            return false;
        }
        if (fileType > LAST_RAW_IMAGE_FILE_TYPE) {
            return false;
        }
        return true;
    }

    public static boolean isRawImageFileType(int fileType) {
        if (fileType < FIRST_RAW_IMAGE_FILE_TYPE || fileType > LAST_RAW_IMAGE_FILE_TYPE) {
            return false;
        }
        return true;
    }

    public static boolean isPlayListFileType(int fileType) {
        if (fileType < FIRST_PLAYLIST_FILE_TYPE || fileType > LAST_PLAYLIST_FILE_TYPE) {
            return false;
        }
        return true;
    }

    public static boolean isDrmFileType(int fileType) {
        if (fileType < LAST_DRM_FILE_TYPE || fileType > LAST_DRM_FILE_TYPE) {
            return false;
        }
        return true;
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(FILE_TYPE_WPL);
        if (lastDot < 0) {
            return null;
        }
        return (MediaFileType) sFileTypeMap.get(path.substring(lastDot + FIRST_AUDIO_FILE_TYPE).toUpperCase(Locale.US));
    }

    public static boolean isMimeTypeMedia(String mimeType) {
        int fileType = getFileTypeForMimeType(mimeType);
        if (isAudioFileType(fileType) || isVideoFileType(fileType) || isImageFileType(fileType)) {
            return true;
        }
        return isPlayListFileType(fileType);
    }

    public static String getFileTitle(String path) {
        int lastSlash = path.lastIndexOf(LAST_PLAYLIST_FILE_TYPE);
        if (lastSlash >= 0) {
            lastSlash += FIRST_AUDIO_FILE_TYPE;
            if (lastSlash < path.length()) {
                path = path.substring(lastSlash);
            }
        }
        int lastDot = path.lastIndexOf(FILE_TYPE_WPL);
        if (lastDot > 0) {
            return path.substring(0, lastDot);
        }
        return path;
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = (Integer) sMimeTypeMap.get(mimeType);
        return value == null ? 0 : value.intValue();
    }

    public static String getMimeTypeForFile(String path) {
        MediaFileType mediaFileType = getFileType(path);
        if (mediaFileType == null) {
            return null;
        }
        return mediaFileType.mimeType;
    }

    public static int getFormatCode(String fileName, String mimeType) {
        Integer value;
        if (mimeType != null) {
            value = (Integer) sMimeTypeToFormatMap.get(mimeType);
            if (value != null) {
                return value.intValue();
            }
        }
        int lastDot = fileName.lastIndexOf(FILE_TYPE_WPL);
        if (lastDot > 0) {
            value = (Integer) sFileTypeToFormatMap.get(fileName.substring(lastDot + FIRST_AUDIO_FILE_TYPE).toUpperCase(Locale.ROOT));
            if (value != null) {
                return value.intValue();
            }
        }
        return GLES11.GL_CLIP_PLANE0;
    }

    public static String getMimeTypeForFormatCode(int formatCode) {
        return (String) sFormatToMimeTypeMap.get(Integer.valueOf(formatCode));
    }

    private static void addFileTypeExt() {
        addFileType("MP3", FIRST_AUDIO_FILE_TYPE, "audio/mp3");
        addFileType("M4A", FILE_TYPE_M4A, "audio/m4a");
        addFileType("BMP", FILE_TYPE_BMP, "image/bmp");
    }

    public static void hwAddFileType(String extension, int fileType, String mimeType) {
        addFileType(extension, fileType, mimeType);
    }
}
