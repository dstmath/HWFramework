package ohos.media.image.common;

public enum PixelFormat {
    UNKNOWN(0),
    ARGB_8888(1),
    RGB_565(2),
    RGBA_8888(3),
    BGRA_8888(4);
    
    private final int pixelFormatValue;

    private PixelFormat(int i) {
        this.pixelFormatValue = i;
    }

    public int getValue() {
        return this.pixelFormatValue;
    }
}
