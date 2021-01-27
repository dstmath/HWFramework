package ohos.media.photokit.metadata;

import java.io.FileDescriptor;
import java.util.List;
import java.util.Map;
import ohos.app.Context;
import ohos.media.image.PixelMap;
import ohos.media.photokit.common.PixelMapConfigs;
import ohos.media.photokit.photokitfwk.AVMetadataHelperImpl;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.net.Uri;

public class AVMetadataHelper implements AutoCloseable {
    public static final int AV_KEY_ALBUM = 1;
    public static final int AV_KEY_ALBUMARTIST = 13;
    public static final int AV_KEY_ARTIST = 2;
    public static final int AV_KEY_AUTHOR = 3;
    public static final int AV_KEY_BITRATE = 20;
    public static final int AV_KEY_BITS_PER_SAMPLE = 39;
    public static final int AV_KEY_CAPTURE_FRAMERATE = 25;
    public static final int AV_KEY_CD_TRACK_NUMBER = 0;
    public static final int AV_KEY_COLOR_RANGE = 37;
    public static final int AV_KEY_COLOR_STANDARD = 35;
    public static final int AV_KEY_COLOR_TRANSFER = 36;
    public static final int AV_KEY_COMPILATION = 15;
    public static final int AV_KEY_COMPOSER = 4;
    public static final int AV_KEY_DATE = 5;
    public static final int AV_KEY_DISC_NUMBER = 14;
    public static final int AV_KEY_DURATION = 9;
    public static final int AV_KEY_EXIF_LENGTH = 34;
    public static final int AV_KEY_EXIF_OFFSET = 33;
    public static final int AV_KEY_GENRE = 6;
    public static final int AV_KEY_HAS_AUDIO = 16;
    public static final int AV_KEY_HAS_IMAGE = 26;
    public static final int AV_KEY_HAS_VIDEO = 17;
    public static final int AV_KEY_IMAGE_COUNT = 27;
    public static final int AV_KEY_IMAGE_HEIGHT = 30;
    public static final int AV_KEY_IMAGE_PRIMARY = 28;
    public static final int AV_KEY_IMAGE_ROTATION = 31;
    public static final int AV_KEY_IMAGE_WIDTH = 29;
    public static final int AV_KEY_IS_DRM = 22;
    public static final int AV_KEY_LOCATION = 23;
    public static final int AV_KEY_MIMETYPE = 12;
    public static final int AV_KEY_NUM_TRACKS = 10;
    public static final int AV_KEY_SAMPLERATE = 38;
    public static final int AV_KEY_TIMED_TEXT_LANGUAGES = 21;
    public static final int AV_KEY_TITLE = 7;
    public static final int AV_KEY_VIDEO_FRAME_COUNT = 32;
    public static final int AV_KEY_VIDEO_HEIGHT = 19;
    public static final int AV_KEY_VIDEO_ROTATION = 24;
    public static final int AV_KEY_VIDEO_WIDTH = 18;
    public static final int AV_KEY_WRITER = 11;
    public static final int AV_KEY_YEAR = 8;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVMetadataHelper.class);
    public static final int QUERY_OPTION_ARGB8888 = 5;
    public static final int QUERY_OPTION_CLOSEST = 3;
    public static final int QUERY_OPTION_CLOSEST_SYNC = 2;
    public static final int QUERY_OPTION_NEXT_SYNC = 1;
    public static final int QUERY_OPTION_PREVIOUS_SYNC = 0;
    private final AVMetadataHelperImpl avMetadataHelperImpl = new AVMetadataHelperImpl();

    public boolean setSource(String str) {
        if (str == null) {
            LOGGER.warn("public setSource by path error: path is null", new Object[0]);
            return false;
        }
        try {
            return this.avMetadataHelperImpl.setSource(str);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public setSource by path catch IllegalArgumentException", new Object[0]);
            return false;
        }
    }

    public boolean setSource(FileDescriptor fileDescriptor) {
        if (fileDescriptor == null) {
            LOGGER.warn("public setSource by fd error: fd is null", new Object[0]);
            return false;
        }
        try {
            return this.avMetadataHelperImpl.setSource(fileDescriptor);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public setSource by fd catch IllegalArgumentException", new Object[0]);
            return false;
        }
    }

    public boolean setSource(FileDescriptor fileDescriptor, long j, long j2) {
        if (fileDescriptor == null) {
            LOGGER.warn("public setSource by fd, offset and length error: fd is null", new Object[0]);
            return false;
        }
        try {
            return this.avMetadataHelperImpl.setSource(fileDescriptor, j, j2);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public setSource by fd, offset and length catch IllegalArgumentException", new Object[0]);
            return false;
        }
    }

    public boolean setSource(String str, Map<String, String> map) {
        if (str == null || map == null) {
            LOGGER.warn("public setSource by uri and headers error: uri or headers is null", new Object[0]);
            return false;
        }
        try {
            return this.avMetadataHelperImpl.setSource(str, map);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public setSource by uri and headers catch IllegalArgumentException", new Object[0]);
            return false;
        }
    }

    public boolean setSource(Context context, Uri uri) {
        if (uri == null || context == null) {
            LOGGER.warn("public setSource by uri and context error: uri or context is null", new Object[0]);
            return false;
        }
        try {
            return this.avMetadataHelperImpl.setSource(context, uri);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public setSource by context and uri catch IllegalArgumentException", new Object[0]);
            return false;
        } catch (SecurityException unused2) {
            LOGGER.warn("public setSource by context and uri catch SecurityException", new Object[0]);
            return false;
        }
    }

    public String resolveMetadata(int i) {
        return this.avMetadataHelperImpl.resolveMetadata(i);
    }

    public PixelMap fetchVideoScaledPixelMapByTime(long j, int i, int i2, int i3) {
        if (i < 0 || i > 3) {
            LOGGER.warn("public fetchVideoScaledPixelMapByTime error: option is illegal: %{public}d", Integer.valueOf(i));
            return null;
        } else if (i2 <= 0) {
            LOGGER.warn("public fetchVideoScaledPixelMapByTime error: width is illegal: %{public}d", Integer.valueOf(i2));
            return null;
        } else if (i3 <= 0) {
            LOGGER.warn("public fetchVideoScaledPixelMapByTime error: height is illegal: %{public}d", Integer.valueOf(i3));
            return null;
        } else {
            try {
                return this.avMetadataHelperImpl.fetchVideoScaledPixelMapByTime(j, i, i2, i3);
            } catch (IllegalArgumentException unused) {
                LOGGER.warn("public fetchVideoScaledPixelMapByTime catch IllegalArgumentException", new Object[0]);
                return null;
            }
        }
    }

    public PixelMap fetchVideoPixelMapByTime(long j, int i) {
        if (i < 1 || i > 5) {
            LOGGER.warn("public fetchVideoPixelMapByTime error: option is illegal: %{public}d", Integer.valueOf(i));
            return null;
        }
        try {
            return this.avMetadataHelperImpl.fetchVideoPixelMapByTime(j, i);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public fetchVideoPixelMapByTime by timeUs and option catch IllegalArgumentException", new Object[0]);
            return null;
        }
    }

    public PixelMap fetchVideoPixelMapByTime(long j) {
        try {
            return this.avMetadataHelperImpl.fetchVideoPixelMapByTime(j);
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public fetchVideoPixelMapByTime by timeUs catch IllegalArgumentException", new Object[0]);
            return null;
        }
    }

    public PixelMap fetchVideoPixelMapByTime() {
        try {
            return this.avMetadataHelperImpl.fetchVideoPixelMapByTime();
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("public fetchVideoPixelMapByTime catch IllegalArgumentException", new Object[0]);
            return null;
        }
    }

    public byte[] resolveImage() {
        return this.avMetadataHelperImpl.resolveImage();
    }

    public PixelMap fetchVideoPixelMapByIndex(int i, PixelMapConfigs pixelMapConfigs) {
        return this.avMetadataHelperImpl.fetchVideoPixelMapByIndex(i, pixelMapConfigs);
    }

    public PixelMap fetchVideoPixelMapByIndex(int i) {
        return this.avMetadataHelperImpl.fetchVideoPixelMapByIndex(i);
    }

    public List<PixelMap> fetchVideoPixelMapsByIndex(int i, int i2, PixelMapConfigs pixelMapConfigs) {
        return this.avMetadataHelperImpl.fetchVideoPixelMapsByIndex(i, i2, pixelMapConfigs);
    }

    public List<PixelMap> fetchVideoPixelMapsByIndex(int i, int i2) {
        return this.avMetadataHelperImpl.fetchVideoPixelMapsByIndex(i, i2);
    }

    public PixelMap fetchImagePixelMapByIndex(int i, PixelMapConfigs pixelMapConfigs) {
        return this.avMetadataHelperImpl.fetchImagePixelMapByIndex(i, pixelMapConfigs);
    }

    public PixelMap fetchImagePixelMapByIndex(int i) {
        return this.avMetadataHelperImpl.fetchImagePixelMapByIndex(i);
    }

    public PixelMap fetchImagePrimaryPixelMap(PixelMapConfigs pixelMapConfigs) {
        return this.avMetadataHelperImpl.fetchImagePrimaryPixelMap(pixelMapConfigs);
    }

    public PixelMap fetchImagePrimaryPixelMap() {
        return this.avMetadataHelperImpl.fetchImagePrimaryPixelMap();
    }

    public void release() {
        this.avMetadataHelperImpl.release();
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        release();
    }
}
