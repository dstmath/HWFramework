package ohos.media.photokit.adapter;

import android.media.ThumbnailUtils;
import java.io.File;
import java.io.IOException;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVThumbnailAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVThumbnailAdapter.class);

    public static PixelMap createVideoThumbnail(File file, Size size) {
        try {
            return ImageDoubleFwConverter.createShellPixelMap(ThumbnailUtils.createVideoThumbnail(file, new android.util.Size(size.width, size.height), null));
        } catch (IOException unused) {
            LOGGER.warn("AVThumbnailAdapter create Video Thumbnail occurred IOException", new Object[0]);
            return null;
        }
    }

    public static PixelMap createImageThumbnail(File file, Size size) {
        try {
            return ImageDoubleFwConverter.createShellPixelMap(ThumbnailUtils.createImageThumbnail(file, new android.util.Size(size.width, size.height), null));
        } catch (IOException unused) {
            LOGGER.warn("AVThumbnailAdapter create Image Thumbnail occurred IOException", new Object[0]);
            return null;
        }
    }
}
