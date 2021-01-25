package ohos.agp.image;

import java.io.File;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

public class PixelMapFactory {
    public static PixelMap createFromPath(String str) {
        if (!new File(str).exists()) {
            return null;
        }
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/png";
        ImageSource create = ImageSource.create(str, sourceOptions);
        if (create == null) {
            return null;
        }
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        return create.createPixelmap(decodingOptions);
    }
}
