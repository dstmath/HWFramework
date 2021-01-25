package ohos.media.image.common;

public class ImageInfo {
    public AlphaType alphaType;
    public ColorSpace colorSpace;
    public PixelFormat pixelFormat;
    public Size size;

    private void setPixelFormat(int i) {
        PixelFormat[] values = PixelFormat.values();
        for (PixelFormat pixelFormat2 : values) {
            if (pixelFormat2.getValue() == i) {
                this.pixelFormat = pixelFormat2;
                return;
            }
        }
    }

    private void setColorSpace(int i) {
        ColorSpace[] values = ColorSpace.values();
        for (ColorSpace colorSpace2 : values) {
            if (colorSpace2.getValue() == i) {
                this.colorSpace = colorSpace2;
                return;
            }
        }
    }

    private void setAlphaType(int i) {
        AlphaType[] values = AlphaType.values();
        for (AlphaType alphaType2 : values) {
            if (alphaType2.getValue() == i) {
                this.alphaType = alphaType2;
                return;
            }
        }
    }
}
