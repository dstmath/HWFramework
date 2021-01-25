package ohos.media.camera.mode;

import ohos.media.image.Image;
import ohos.media.image.common.Size;

public abstract class ActionDataCallback {

    public @interface Type {
        public static final int BURST = 2;
        public static final int TAKE_PICTURE = 1;
    }

    public void onImageAvailable(Mode mode, @Type int i, Image image) {
    }

    public void onRawImageAvailable(Mode mode, @Type int i, Image image) {
    }

    public void onThumbnailAvailable(Mode mode, @Type int i, Size size, byte[] bArr) {
    }
}
