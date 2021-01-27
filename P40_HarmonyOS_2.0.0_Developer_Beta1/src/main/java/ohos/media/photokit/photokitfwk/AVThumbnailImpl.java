package ohos.media.photokit.photokitfwk;

import java.io.File;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.media.photokit.adapter.AVThumbnailAdapter;

public class AVThumbnailImpl {
    public static PixelMap createVideoThumbnail(File file, Size size) {
        return AVThumbnailAdapter.createVideoThumbnail(file, size);
    }

    public static PixelMap createImageThumbnail(File file, Size size) {
        return AVThumbnailAdapter.createImageThumbnail(file, size);
    }
}
