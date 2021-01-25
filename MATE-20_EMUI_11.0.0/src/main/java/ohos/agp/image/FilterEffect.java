package ohos.agp.image;

import ohos.agp.components.Component;

public class FilterEffect {
    protected long mNativeFilterPtr = 0;

    public static class AlgorithmType {
        public static final int BOX_BLUR = 1;
        public static final int NONE = 0;
    }

    public static class ColorType {
        public static final int DARK_BLUR = 1;
        public static final int LIGHT_BLUR = 2;
        public static final int NONE = 0;
    }

    private native void nativeFilterRelease(long j);

    private native long nativeGetFilterHandle(int i, int i2);

    private native void nativeSetBoxBlurParameter(long j, int i);

    private native void nativeSetFilterPicture(long j, String str, long j2);

    private native void nativeSetFilterView(long j, long j2);

    public static class BoxBlurParam {
        private int radius;

        public void setRadius(int i) {
            this.radius = i;
        }
    }

    public FilterEffect(int i, int i2) {
        this.mNativeFilterPtr = nativeGetFilterHandle(i, i2);
    }

    public long getNativeFilterPtr() {
        return this.mNativeFilterPtr;
    }

    /* access modifiers changed from: protected */
    public void checkReleaseStatus() {
        if (this.mNativeFilterPtr == 0) {
            throw new IllegalStateException("mNativeFilterPtr has already been released.");
        }
    }

    public void setFilterComponent(Component component) {
        checkReleaseStatus();
        nativeSetFilterView(this.mNativeFilterPtr, component.getNativeViewPtr());
    }

    public void setFilterPicture(String str, Component component) {
        checkReleaseStatus();
        nativeSetFilterPicture(this.mNativeFilterPtr, str, component.getNativeViewPtr());
    }

    public void setBoxBlurParameter(BoxBlurParam boxBlurParam) {
        checkReleaseStatus();
        nativeSetBoxBlurParameter(this.mNativeFilterPtr, boxBlurParam.radius);
    }

    /* access modifiers changed from: protected */
    public void releaseNativeRes() {
        nativeFilterRelease(this.mNativeFilterPtr);
    }
}
