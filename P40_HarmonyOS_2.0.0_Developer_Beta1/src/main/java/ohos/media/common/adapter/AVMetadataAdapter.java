package ohos.media.common.adapter;

import android.graphics.Bitmap;
import android.media.MediaMetadata;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import ohos.media.common.AVMetadata;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVMetadataAdapter {
    private static final Map<String, String> AV_METADATA_AZ_MAPPER;
    @AVMetadata.AVLongKey
    private static final String[] AV_METADATA_LONG_KEY = {AVMetadata.AVLongKey.DURATION, AVMetadata.AVLongKey.YEAR, AVMetadata.AVLongKey.TRACK, AVMetadata.AVLongKey.TOTAL_TRACKS, AVMetadata.AVLongKey.DISC};
    @AVMetadata.AVPixelMapKey
    private static final String[] AV_METADATA_PIXELMAP_KEY = {AVMetadata.AVPixelMapKey.ICON};
    @AVMetadata.AVTextKey
    private static final String[] AV_METADATA_TEXT_KEY = {AVMetadata.AVTextKey.TITLE, AVMetadata.AVTextKey.AUTHOR, AVMetadata.AVTextKey.ARTIST, AVMetadata.AVTextKey.ALBUM, AVMetadata.AVTextKey.WRITER, AVMetadata.AVTextKey.COMPILATION, AVMetadata.AVTextKey.COMPOSER, AVMetadata.AVTextKey.DATE, AVMetadata.AVTextKey.GENRE, AVMetadata.AVTextKey.ALBUM_ARTIST, AVMetadata.AVTextKey.SUBTITLE, AVMetadata.AVTextKey.ICON_URI, AVMetadata.AVTextKey.META_ID, AVMetadata.AVTextKey.META_URI};
    private static final Logger LOGGER = LoggerFactory.getImageLogger(AVMetadataAdapter.class);

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(AVMetadata.AVTextKey.TITLE, "android.media.metadata.TITLE");
        hashMap.put(AVMetadata.AVTextKey.ARTIST, "android.media.metadata.ARTIST");
        hashMap.put(AVMetadata.AVLongKey.DURATION, "android.media.metadata.DURATION");
        hashMap.put(AVMetadata.AVTextKey.ALBUM, "android.media.metadata.ALBUM");
        hashMap.put(AVMetadata.AVTextKey.AUTHOR, "android.media.metadata.AUTHOR");
        hashMap.put(AVMetadata.AVTextKey.WRITER, "android.media.metadata.WRITER");
        hashMap.put(AVMetadata.AVTextKey.COMPOSER, "android.media.metadata.COMPOSER");
        hashMap.put(AVMetadata.AVTextKey.COMPILATION, "android.media.metadata.COMPILATION");
        hashMap.put(AVMetadata.AVTextKey.DATE, "android.media.metadata.DATE");
        hashMap.put(AVMetadata.AVLongKey.YEAR, "android.media.metadata.YEAR");
        hashMap.put(AVMetadata.AVTextKey.GENRE, "android.media.metadata.GENRE");
        hashMap.put(AVMetadata.AVLongKey.TRACK, "android.media.metadata.TRACK_NUMBER");
        hashMap.put(AVMetadata.AVLongKey.TOTAL_TRACKS, "android.media.metadata.NUM_TRACKS");
        hashMap.put(AVMetadata.AVLongKey.DISC, "android.media.metadata.DISC_NUMBER");
        hashMap.put(AVMetadata.AVTextKey.ALBUM_ARTIST, "android.media.metadata.ALBUM_ARTIST");
        hashMap.put(AVMetadata.AVTextKey.SUBTITLE, "android.media.metadata.DISPLAY_SUBTITLE");
        hashMap.put(AVMetadata.AVPixelMapKey.ICON, "android.media.metadata.DISPLAY_ICON");
        hashMap.put(AVMetadata.AVTextKey.ICON_URI, "android.media.metadata.DISPLAY_ICON_URI");
        hashMap.put(AVMetadata.AVTextKey.META_ID, "android.media.metadata.MEDIA_ID");
        hashMap.put(AVMetadata.AVTextKey.META_URI, "android.media.metadata.MEDIA_URI");
        AV_METADATA_AZ_MAPPER = Collections.unmodifiableMap(hashMap);
    }

    public static MediaMetadata getMediaMetadata(AVMetadata aVMetadata) {
        if (aVMetadata != null) {
            MediaMetadata.Builder builder = new MediaMetadata.Builder();
            Set<String> keysSet = aVMetadata.getKeysSet();
            if (keysSet != null) {
                String[] strArr = AV_METADATA_LONG_KEY;
                for (String str : strArr) {
                    if (aVMetadata.hasKey(str)) {
                        builder.putLong(AV_METADATA_AZ_MAPPER.get(str), aVMetadata.getLong(str));
                        keysSet.remove(str);
                    }
                }
                String[] strArr2 = AV_METADATA_PIXELMAP_KEY;
                for (String str2 : strArr2) {
                    if (aVMetadata.hasKey(str2)) {
                        builder.putBitmap(AV_METADATA_AZ_MAPPER.get(str2), ImageDoubleFwConverter.createShadowBitmap(aVMetadata.getPixelMap(str2)));
                        keysSet.remove(str2);
                    }
                }
                String[] strArr3 = AV_METADATA_TEXT_KEY;
                for (String str3 : strArr3) {
                    if (aVMetadata.hasKey(str3)) {
                        String string = aVMetadata.getString(str3);
                        if (string != null) {
                            builder.putString(AV_METADATA_AZ_MAPPER.get(str3), string);
                        }
                        CharSequence text = aVMetadata.getText(str3);
                        if (text != null) {
                            builder.putText(AV_METADATA_AZ_MAPPER.get(str3), text);
                        }
                        keysSet.remove(str3);
                    }
                }
                handleExtraKey(aVMetadata, keysSet, builder);
                return builder.build();
            }
            throw new IllegalArgumentException("avMetadata keySets is null");
        }
        throw new IllegalArgumentException("avMetadataBuilder is null");
    }

    public static AVMetadata getAVMetadata(MediaMetadata mediaMetadata) {
        if (mediaMetadata != null) {
            Set<String> keySet = mediaMetadata.keySet();
            if (keySet != null) {
                AVMetadata.Builder builder = new AVMetadata.Builder();
                String[] strArr = AV_METADATA_LONG_KEY;
                for (String str : strArr) {
                    long j = mediaMetadata.getLong(AV_METADATA_AZ_MAPPER.get(str));
                    if (j != 0) {
                        builder.setLong(str, j);
                    }
                    keySet.remove(AV_METADATA_AZ_MAPPER.get(str));
                }
                String[] strArr2 = AV_METADATA_PIXELMAP_KEY;
                for (String str2 : strArr2) {
                    Bitmap bitmap = mediaMetadata.getBitmap(AV_METADATA_AZ_MAPPER.get(str2));
                    if (bitmap != null) {
                        builder.setPixelMap(str2, ImageDoubleFwConverter.createShellPixelMap(bitmap));
                    }
                    keySet.remove(AV_METADATA_AZ_MAPPER.get(str2));
                }
                String[] strArr3 = AV_METADATA_TEXT_KEY;
                for (String str3 : strArr3) {
                    String string = mediaMetadata.getString(AV_METADATA_AZ_MAPPER.get(str3));
                    if (string != null) {
                        builder.setString(str3, string);
                    }
                    CharSequence text = mediaMetadata.getText(AV_METADATA_AZ_MAPPER.get(str3));
                    if (text != null) {
                        builder.setText(str3, text);
                    }
                    keySet.remove(AV_METADATA_AZ_MAPPER.get(str3));
                }
                handleExtraKey(mediaMetadata, keySet, builder);
                return builder.build();
            }
            throw new IllegalArgumentException("mediaMetadata keySets is null");
        }
        throw new IllegalArgumentException("mediaMetadata is null");
    }

    private static void handleExtraKey(MediaMetadata mediaMetadata, Set<String> set, AVMetadata.Builder builder) {
        if (!(mediaMetadata == null || set == null || set.isEmpty())) {
            for (String str : set) {
                String string = mediaMetadata.getString(str);
                if (string != null) {
                    builder.setString(str, string);
                } else {
                    long j = mediaMetadata.getLong(str);
                    if (j != 0) {
                        builder.setLong(str, j);
                    } else {
                        CharSequence text = mediaMetadata.getText(str);
                        if (text != null) {
                            builder.setText(str, text);
                        } else {
                            try {
                                if (mediaMetadata.getBitmap(str) != null) {
                                    builder.setPixelMap(str, ImageDoubleFwConverter.createShellPixelMap(mediaMetadata.getBitmap(str)));
                                }
                            } catch (Exception e) {
                                LOGGER.warn("handleExtraKey Bitmap occur exception, key is %{public}s, ex: %{public}s", str, e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleExtraKey(AVMetadata aVMetadata, Set<String> set, MediaMetadata.Builder builder) {
        if (!(aVMetadata == null || set == null || set.isEmpty())) {
            for (String str : set) {
                Object object = aVMetadata.getObject(str);
                if (object == null) {
                    LOGGER.warn("getMediaMetadata handleExtraKey key value is null, key: %{public}s", str);
                } else if (object instanceof Long) {
                    builder.putLong(str, ((Long) object).longValue());
                } else if (object instanceof PixelMap) {
                    builder.putBitmap(str, ImageDoubleFwConverter.createShadowBitmap((PixelMap) object));
                } else if (object instanceof String) {
                    builder.putString(str, (String) object);
                } else {
                    LOGGER.warn("getMediaMetadata handleExtraKey key value invalid type, key: %{public}s", str);
                }
            }
        }
    }
}
