package com.google.android.mms;

import android.os.SystemProperties;
import com.google.android.mms.pdu.CharacterSets;
import java.util.ArrayList;

public class ContentType {
    public static final String APP_DRM_CONTENT = "application/vnd.oma.drm.content";
    public static final String APP_DRM_MESSAGE = "application/vnd.oma.drm.message";
    public static final String APP_SMIL = "application/smil";
    public static final String APP_WAP_XHTML = "application/vnd.wap.xhtml+xml";
    public static final String APP_XHTML = "application/xhtml+xml";
    public static final String AUDIO_3GPP = "audio/3gpp";
    public static final String AUDIO_AAC = "audio/aac";
    public static final String AUDIO_AAC_MP4 = "audio/aac_mp4";
    public static final String AUDIO_AMR = "audio/amr";
    public static final String AUDIO_AMR_WB = "audio/amr-wb";
    public static final String AUDIO_EVRC = "audio/evrc";
    public static final String AUDIO_IMELODY = "audio/imelody";
    public static final String AUDIO_MID = "audio/mid";
    public static final String AUDIO_MIDI = "audio/midi";
    public static final String AUDIO_MP3 = "audio/mp3";
    public static final String AUDIO_MP4 = "audio/mp4";
    public static final String AUDIO_MPEG = "audio/mpeg";
    public static final String AUDIO_MPEG3 = "audio/mpeg3";
    public static final String AUDIO_MPG = "audio/mpg";
    public static final String AUDIO_OGG = "application/ogg";
    public static final String AUDIO_QCELP = "audio/qcelp";
    public static final String AUDIO_UNSPECIFIED = "audio/*";
    public static final String AUDIO_WMA = "audio/x-ms-wma";
    public static final String AUDIO_X_MID = "audio/x-mid";
    public static final String AUDIO_X_MIDI = "audio/x-midi";
    public static final String AUDIO_X_MP3 = "audio/x-mp3";
    public static final String AUDIO_X_MPEG = "audio/x-mpeg";
    public static final String AUDIO_X_MPEG3 = "audio/x-mpeg3";
    public static final String AUDIO_X_MPG = "audio/x-mpg";
    public static final String AUDIO_X_WAV = "audio/x-wav";
    public static final String IMAGE_BMP = "image/bmp";
    public static final String IMAGE_GIF = "image/gif";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_JPG = "image/jpg";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_UNSPECIFIED = "image/*";
    public static final String IMAGE_WBMP = "image/vnd.wap.wbmp";
    public static final String IMAGE_X_MS_BMP = "image/x-ms-bmp";
    public static final String MMS_GENERIC = "application/vnd.wap.mms-generic";
    public static final String MMS_MESSAGE = "application/vnd.wap.mms-message";
    public static final String MULTIPART_ALTERNATIVE = "application/vnd.wap.multipart.alternative";
    public static final String MULTIPART_MIXED = "application/vnd.wap.multipart.mixed";
    public static final String MULTIPART_RELATED = "application/vnd.wap.multipart.related";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_VCALENDAR = "text/x-vCalendar";
    public static final String TEXT_VCARD = "text/x-vCard";
    public static final String VIDEO_3G2 = "video/3gpp2";
    public static final String VIDEO_3GPP = "video/3gpp";
    public static final String VIDEO_H263 = "video/h263";
    public static final String VIDEO_MP4 = "video/mp4";
    public static final String VIDEO_UNSPECIFIED = "video/*";
    private static final ArrayList<String> sSupportedAudioTypes = new ArrayList();
    private static final ArrayList<String> sSupportedContentTypes = new ArrayList();
    private static final ArrayList<String> sSupportedImageTypes = new ArrayList();
    private static final ArrayList<String> sSupportedVideoTypes = new ArrayList();

    static {
        sSupportedContentTypes.add(TEXT_PLAIN);
        sSupportedContentTypes.add(TEXT_HTML);
        sSupportedContentTypes.add(TEXT_VCALENDAR);
        sSupportedContentTypes.add(TEXT_VCARD);
        sSupportedContentTypes.add(IMAGE_JPEG);
        sSupportedContentTypes.add(IMAGE_GIF);
        sSupportedContentTypes.add(IMAGE_WBMP);
        sSupportedContentTypes.add(IMAGE_PNG);
        sSupportedContentTypes.add(IMAGE_JPG);
        sSupportedContentTypes.add(IMAGE_X_MS_BMP);
        sSupportedContentTypes.add(IMAGE_BMP);
        sSupportedContentTypes.add(AUDIO_AAC);
        sSupportedContentTypes.add(AUDIO_AAC_MP4);
        sSupportedContentTypes.add(AUDIO_QCELP);
        sSupportedContentTypes.add(AUDIO_EVRC);
        sSupportedContentTypes.add(AUDIO_AMR);
        sSupportedContentTypes.add(AUDIO_AMR_WB);
        sSupportedContentTypes.add(AUDIO_IMELODY);
        sSupportedContentTypes.add(AUDIO_MID);
        sSupportedContentTypes.add(AUDIO_MIDI);
        sSupportedContentTypes.add(AUDIO_MP3);
        sSupportedContentTypes.add(AUDIO_MP4);
        sSupportedContentTypes.add(AUDIO_MPEG3);
        sSupportedContentTypes.add(AUDIO_MPEG);
        sSupportedContentTypes.add(AUDIO_MPG);
        sSupportedContentTypes.add(AUDIO_X_MID);
        sSupportedContentTypes.add(AUDIO_X_MIDI);
        sSupportedContentTypes.add(AUDIO_X_MP3);
        sSupportedContentTypes.add(AUDIO_X_MPEG3);
        sSupportedContentTypes.add(AUDIO_X_MPEG);
        sSupportedContentTypes.add(AUDIO_X_MPG);
        sSupportedContentTypes.add(AUDIO_X_WAV);
        sSupportedContentTypes.add(AUDIO_3GPP);
        sSupportedContentTypes.add(AUDIO_OGG);
        if (isHelixPlayerEnabled()) {
            sSupportedContentTypes.add(AUDIO_WMA);
        }
        sSupportedContentTypes.add(VIDEO_3GPP);
        sSupportedContentTypes.add(VIDEO_3G2);
        sSupportedContentTypes.add(VIDEO_H263);
        sSupportedContentTypes.add(VIDEO_MP4);
        sSupportedContentTypes.add(APP_SMIL);
        sSupportedContentTypes.add(APP_WAP_XHTML);
        sSupportedContentTypes.add(APP_XHTML);
        sSupportedContentTypes.add(APP_DRM_CONTENT);
        sSupportedContentTypes.add("application/vnd.oma.drm.message");
        sSupportedImageTypes.add(IMAGE_JPEG);
        sSupportedImageTypes.add(IMAGE_GIF);
        sSupportedImageTypes.add(IMAGE_WBMP);
        sSupportedImageTypes.add(IMAGE_PNG);
        sSupportedImageTypes.add(IMAGE_JPG);
        sSupportedImageTypes.add(IMAGE_X_MS_BMP);
        sSupportedImageTypes.add(IMAGE_BMP);
        sSupportedAudioTypes.add(AUDIO_AAC);
        sSupportedAudioTypes.add(AUDIO_AAC_MP4);
        sSupportedAudioTypes.add(AUDIO_QCELP);
        sSupportedAudioTypes.add(AUDIO_EVRC);
        sSupportedAudioTypes.add(AUDIO_AMR);
        sSupportedAudioTypes.add(AUDIO_AMR_WB);
        sSupportedAudioTypes.add(AUDIO_IMELODY);
        sSupportedAudioTypes.add(AUDIO_MID);
        sSupportedAudioTypes.add(AUDIO_MIDI);
        sSupportedAudioTypes.add(AUDIO_MP3);
        sSupportedAudioTypes.add(AUDIO_MPEG3);
        sSupportedAudioTypes.add(AUDIO_MPEG);
        sSupportedAudioTypes.add(AUDIO_MPG);
        sSupportedAudioTypes.add(AUDIO_MP4);
        sSupportedAudioTypes.add(AUDIO_X_MID);
        sSupportedAudioTypes.add(AUDIO_X_MIDI);
        sSupportedAudioTypes.add(AUDIO_X_MP3);
        sSupportedAudioTypes.add(AUDIO_X_MPEG3);
        sSupportedAudioTypes.add(AUDIO_X_MPEG);
        sSupportedAudioTypes.add(AUDIO_X_MPG);
        sSupportedAudioTypes.add(AUDIO_X_WAV);
        sSupportedAudioTypes.add(AUDIO_3GPP);
        sSupportedAudioTypes.add(AUDIO_OGG);
        if (isHelixPlayerEnabled()) {
            sSupportedAudioTypes.add(AUDIO_WMA);
        }
        sSupportedVideoTypes.add(VIDEO_3GPP);
        sSupportedVideoTypes.add(VIDEO_3G2);
        sSupportedVideoTypes.add(VIDEO_H263);
        sSupportedVideoTypes.add(VIDEO_MP4);
    }

    private ContentType() {
    }

    private static boolean isHelixPlayerEnabled() {
        return SystemProperties.getBoolean("ro.config.helix_enable", false);
    }

    public static boolean isSupportedType(String contentType) {
        return contentType != null ? sSupportedContentTypes.contains(contentType) : false;
    }

    public static boolean isSupportedImageType(String contentType) {
        return isImageType(contentType) ? isSupportedType(contentType) : false;
    }

    public static boolean isSupportedAudioType(String contentType) {
        return isAudioType(contentType) ? isSupportedType(contentType) : false;
    }

    public static boolean isSupportedVideoType(String contentType) {
        return isVideoType(contentType) ? isSupportedType(contentType) : false;
    }

    public static boolean isTextType(String contentType) {
        return contentType != null ? contentType.startsWith("text/") : false;
    }

    public static boolean isImageType(String contentType) {
        return contentType != null ? contentType.startsWith("image/") : false;
    }

    public static boolean isAudioType(String contentType) {
        return contentType != null ? contentType.startsWith("audio/") : false;
    }

    public static boolean isVideoType(String contentType) {
        return contentType != null ? contentType.startsWith("video/") : false;
    }

    public static boolean isDrmType(String contentType) {
        if (contentType == null) {
            return false;
        }
        if (contentType.equals(APP_DRM_CONTENT)) {
            return true;
        }
        return contentType.equals("application/vnd.oma.drm.message");
    }

    public static boolean isUnspecified(String contentType) {
        return contentType != null ? contentType.endsWith(CharacterSets.MIMENAME_ANY_CHARSET) : false;
    }

    public static ArrayList<String> getImageTypes() {
        return (ArrayList) sSupportedImageTypes.clone();
    }

    public static ArrayList<String> getAudioTypes() {
        return (ArrayList) sSupportedAudioTypes.clone();
    }

    public static ArrayList<String> getVideoTypes() {
        return (ArrayList) sSupportedVideoTypes.clone();
    }

    public static ArrayList<String> getSupportedTypes() {
        return (ArrayList) sSupportedContentTypes.clone();
    }
}
