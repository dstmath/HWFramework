package ohos.media.photokit.metadata;

import java.io.File;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.photokit.photokitfwk.AVThumbnailImpl;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVThumbnailUtils {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVThumbnailUtils.class);

    public static PixelMap createVideoThumbnail(File file, Size size) {
        if (file == null) {
            LOGGER.error("AVThumbnailUtils createVideoThumbnail, file is null", new Object[0]);
            return null;
        } else if (size != null) {
            return AVThumbnailImpl.createVideoThumbnail(file, size);
        } else {
            LOGGER.error("AVThumbnailUtils createVideoThumbnail, size is null", new Object[0]);
            return null;
        }
    }

    public static PixelMap createImageThumbnail(File file, Size size) {
        if (file == null) {
            LOGGER.error("AVThumbnailUtils createImageThumbnail, file is null", new Object[0]);
            return null;
        } else if (size != null) {
            return AVThumbnailImpl.createImageThumbnail(file, size);
        } else {
            LOGGER.error("AVThumbnailUtils createImageThumbnail, size is null", new Object[0]);
            return null;
        }
    }
}
