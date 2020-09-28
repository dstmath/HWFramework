package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.ClipDescription;
import android.mtp.MtpConstants;
import android.os.SystemProperties;
import com.android.internal.widget.MessagingMessage;
import java.util.HashMap;
import java.util.Locale;
import libcore.net.MimeUtils;

public class MediaFile {
    @UnsupportedAppUsage
    @Deprecated
    private static final int FIRST_AUDIO_FILE_TYPE = 1;
    @UnsupportedAppUsage
    @Deprecated
    private static final int LAST_AUDIO_FILE_TYPE = 10;
    @UnsupportedAppUsage
    @Deprecated
    private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap<>();
    @UnsupportedAppUsage
    @Deprecated
    private static final HashMap<String, Integer> sFileTypeToFormatMap = new HashMap<>();
    private static final HashMap<String, String> sFileTypeToMimeTypeMap = new HashMap<>();
    @UnsupportedAppUsage
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = new HashMap<>();
    @UnsupportedAppUsage
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = new HashMap<>();

    @Deprecated
    public static class MediaFileType {
        @UnsupportedAppUsage
        public final int fileType;
        @UnsupportedAppUsage
        public final String mimeType;

        MediaFileType(int fileType2, String mimeType2) {
            this.fileType = fileType2;
            this.mimeType = mimeType2;
        }
    }

    static {
        addFileType(12297, MediaFormat.MIMETYPE_AUDIO_MPEG);
        addFileType(12296, "audio/x-wav");
        addFileType((int) MtpConstants.FORMAT_WMA, "audio/x-ms-wma");
        addFileType((int) MtpConstants.FORMAT_OGG, "audio/ogg");
        addFileType((int) MtpConstants.FORMAT_AAC, "audio/aac");
        addFileType((int) MtpConstants.FORMAT_FLAC, MediaFormat.MIMETYPE_AUDIO_FLAC);
        addFileType(12295, "audio/x-aiff");
        addFileType((int) MtpConstants.FORMAT_MP2, MediaFormat.MIMETYPE_AUDIO_MPEG);
        addFileType(12299, "video/mpeg");
        addFileType((int) MtpConstants.FORMAT_MP4_CONTAINER, "video/mp4");
        addFileType((int) MtpConstants.FORMAT_3GP_CONTAINER, MediaFormat.MIMETYPE_VIDEO_H263);
        addFileType((int) MtpConstants.FORMAT_3GP_CONTAINER, "video/3gpp2");
        addFileType(12298, "video/avi");
        addFileType((int) MtpConstants.FORMAT_WMV, "video/x-ms-wmv");
        addFileType(12300, "video/x-ms-asf");
        addFileType((int) MtpConstants.FORMAT_EXIF_JPEG, "image/jpeg");
        addFileType((int) MtpConstants.FORMAT_GIF, "image/gif");
        addFileType((int) MtpConstants.FORMAT_PNG, "image/png");
        addFileType((int) MtpConstants.FORMAT_BMP, "image/x-ms-bmp");
        addFileType((int) MtpConstants.FORMAT_HEIF, "image/heif");
        addFileType((int) MtpConstants.FORMAT_DNG, "image/x-adobe-dng");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/tiff");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-canon-cr2");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-nikon-nrw");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-sony-arw");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-panasonic-rw2");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-olympus-orf");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-pentax-pef");
        addFileType((int) MtpConstants.FORMAT_TIFF, "image/x-samsung-srw");
        addFileType((int) MtpConstants.FORMAT_TIFF_EP, "image/tiff");
        addFileType((int) MtpConstants.FORMAT_TIFF_EP, "image/x-nikon-nef");
        addFileType((int) MtpConstants.FORMAT_JP2, "image/jp2");
        addFileType((int) MtpConstants.FORMAT_JPX, "image/jpx");
        addFileType((int) MtpConstants.FORMAT_M3U_PLAYLIST, "audio/x-mpegurl");
        addFileType((int) MtpConstants.FORMAT_PLS_PLAYLIST, "audio/x-scpls");
        addFileType((int) MtpConstants.FORMAT_WPL_PLAYLIST, "application/vnd.ms-wpl");
        addFileType((int) MtpConstants.FORMAT_ASX_PLAYLIST, "video/x-ms-asf");
        addFileType(12292, ClipDescription.MIMETYPE_TEXT_PLAIN);
        addFileType(12293, ClipDescription.MIMETYPE_TEXT_HTML);
        addFileType((int) MtpConstants.FORMAT_XML_DOCUMENT, "text/xml");
        addFileType((int) MtpConstants.FORMAT_MS_WORD_DOCUMENT, "application/msword");
        addFileType((int) MtpConstants.FORMAT_MS_WORD_DOCUMENT, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        addFileType((int) MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET, "application/vnd.ms-excel");
        addFileType((int) MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        addFileType((int) MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION, "application/vnd.ms-powerpoint");
        addFileType((int) MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        addFileType("APE", "audio/ffmpeg");
        addFileType("ISMA", "audio/mp4");
        addFileType("F4V", "video/mp4");
        if ("true".equalsIgnoreCase(SystemProperties.get("ro.config.dolby_ddp", "false"))) {
            addFileType("EC3", "audio/ec3");
        }
        addFileType("AC3", MediaFormat.MIMETYPE_AUDIO_AC3);
        addFileType("AC4", MediaFormat.MIMETYPE_AUDIO_AC4);
        addFileType("ISMV", "video/mp4");
        addFileType("RMHD", "video/x-pn-realvideo");
        addFileType("RMVB", "video/x-pn-realvideo");
        addFileType("DIVX", "video/quicktime");
        addFileType("RV", "video/x-pn-realvideo");
        addFileType("WPNG", "image/x-up-wpng");
        addFileType("DCF", "application/vnd.oma.drm.content");
    }

    @UnsupportedAppUsage
    @Deprecated
    static void addFileType(String extension, int fileType, String mimeType) {
    }

    private static void addFileType(int mtpFormatCode, String mimeType) {
        if (!sMimeTypeToFormatMap.containsKey(mimeType)) {
            sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        }
        if (!sFormatToMimeTypeMap.containsKey(Integer.valueOf(mtpFormatCode))) {
            sFormatToMimeTypeMap.put(Integer.valueOf(mtpFormatCode), mimeType);
        }
    }

    private static void addFileType(String extension, String mimeType) {
        sFileTypeToMimeTypeMap.put(extension, mimeType);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isAudioFileType(int fileType) {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isVideoFileType(int fileType) {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isImageFileType(int fileType) {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isPlayListFileType(int fileType) {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isDrmFileType(int fileType) {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static MediaFileType getFileType(String path) {
        return null;
    }

    public static boolean isExifMimeType(String mimeType) {
        return isImageMimeType(mimeType);
    }

    public static boolean isAudioMimeType(String mimeType) {
        return normalizeMimeType(mimeType).startsWith("audio/");
    }

    public static boolean isVideoMimeType(String mimeType) {
        return normalizeMimeType(mimeType).startsWith("video/");
    }

    public static boolean isImageMimeType(String mimeType) {
        return normalizeMimeType(mimeType).startsWith(MessagingMessage.IMAGE_MIME_TYPE_PREFIX);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static boolean isPlayListMimeType(String mimeType) {
        char c;
        String normalizeMimeType = normalizeMimeType(mimeType);
        switch (normalizeMimeType.hashCode()) {
            case -1165508903:
                if (normalizeMimeType.equals("audio/x-scpls")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -979095690:
                if (normalizeMimeType.equals("application/x-mpegurl")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -622808459:
                if (normalizeMimeType.equals("application/vnd.apple.mpegurl")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -432766831:
                if (normalizeMimeType.equals("audio/mpegurl")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 264230524:
                if (normalizeMimeType.equals("audio/x-mpegurl")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1872259501:
                if (normalizeMimeType.equals("application/vnd.ms-wpl")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        return c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5;
    }

    public static boolean isDrmMimeType(String mimeType) {
        return normalizeMimeType(mimeType).equals("application/x-android-drm-fl") || normalizeMimeType(mimeType).equals("application/vnd.oma.drm.content");
    }

    @UnsupportedAppUsage
    public static String getFileTitle(String path) {
        int lastSlash;
        int lastSlash2 = path.lastIndexOf(47);
        if (lastSlash2 >= 0 && (lastSlash = lastSlash2 + 1) < path.length()) {
            path = path.substring(lastSlash);
        }
        int lastDot = path.lastIndexOf(46);
        if (lastDot > 0) {
            return path.substring(0, lastDot);
        }
        return path;
    }

    public static String getFileExtension(String path) {
        int lastDot;
        if (path != null && (lastDot = path.lastIndexOf(46)) >= 0) {
            return path.substring(lastDot + 1);
        }
        return null;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static int getFileTypeForMimeType(String mimeType) {
        return 0;
    }

    public static String getMimeType(String path, int formatCode) {
        String mimeType = getMimeTypeForFile(path);
        if (!"application/octet-stream".equals(mimeType)) {
            return mimeType;
        }
        return getMimeTypeForFormatCode(formatCode);
    }

    @UnsupportedAppUsage
    public static String getMimeTypeForFile(String path) {
        String mimeType = MimeUtils.guessMimeTypeFromExtension(getFileExtension(path));
        if (mimeType != null) {
            return mimeType;
        }
        if (getFileExtension(path) != null) {
            mimeType = sFileTypeToMimeTypeMap.get(getFileExtension(path).toUpperCase(Locale.US));
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    public static String getMimeTypeForFormatCode(int formatCode) {
        String mimeType = sFormatToMimeTypeMap.get(Integer.valueOf(formatCode));
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    public static int getFormatCode(String path, String mimeType) {
        int formatCode = getFormatCodeForMimeType(mimeType);
        if (formatCode != 12288) {
            return formatCode;
        }
        return getFormatCodeForFile(path);
    }

    public static int getFormatCodeForFile(String path) {
        return getFormatCodeForMimeType(getMimeTypeForFile(path));
    }

    public static int getFormatCodeForMimeType(String mimeType) {
        if (mimeType == null) {
            return 12288;
        }
        Integer value = sMimeTypeToFormatMap.get(mimeType);
        if (value != null) {
            return value.intValue();
        }
        String mimeType2 = normalizeMimeType(mimeType);
        Integer value2 = sMimeTypeToFormatMap.get(mimeType2);
        if (value2 != null) {
            return value2.intValue();
        }
        if (mimeType2.startsWith("audio/")) {
            return MtpConstants.FORMAT_UNDEFINED_AUDIO;
        }
        if (mimeType2.startsWith("video/")) {
            return MtpConstants.FORMAT_UNDEFINED_VIDEO;
        }
        if (mimeType2.startsWith(MessagingMessage.IMAGE_MIME_TYPE_PREFIX)) {
            return 14336;
        }
        return 12288;
    }

    private static String normalizeMimeType(String mimeType) {
        String extensionMimeType;
        String extension = MimeUtils.guessExtensionFromMimeType(mimeType);
        if (extension == null || (extensionMimeType = MimeUtils.guessMimeTypeFromExtension(extension)) == null) {
            return mimeType != null ? mimeType : "application/octet-stream";
        }
        return extensionMimeType;
    }
}
