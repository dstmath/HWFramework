package android.hardware.display;

public final class DisplayedContentSamplingAttributes {
    private int mComponentMask;
    private int mDataspace;
    private int mPixelFormat;

    public DisplayedContentSamplingAttributes(int format, int ds, int componentMask) {
        this.mPixelFormat = format;
        this.mDataspace = ds;
        this.mComponentMask = componentMask;
    }

    public int getPixelFormat() {
        return this.mPixelFormat;
    }

    public int getDataspace() {
        return this.mDataspace;
    }

    public int getComponentMask() {
        return this.mComponentMask;
    }
}
