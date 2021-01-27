package ohos.media.image.common;

import java.io.IOException;
import ohos.media.image.ImageSource;

public abstract class Filter {
    public abstract long applyToSource(ImageSource imageSource) throws IOException;

    public abstract Filter restore();
}
