package ohos.agp.components.element;

import ohos.agp.render.Canvas;
import ohos.agp.render.ColorMatrix;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.Rect;
import ohos.app.Context;
import ohos.global.resource.solidxml.Node;

public abstract class Element {
    public static final int ALPHA_DEFAULT = 255;
    public static final int ALPHA_MAX = 255;
    public static final int ALPHA_MIN = 0;
    public static final Rect DEFAULT_BOUNDS_RECT = new Rect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    protected ColorMatrix mColorMatrix;
    public long mNativeElementPtr = 0;
    protected OnChangeListener mOnChangeListener = null;

    public static abstract class ConstantState {
        public long mNativePtr = 0;
    }

    public interface OnChangeListener {
        void onChange(Element element);
    }

    private native void nativeElementBind(long j);

    private native int nativeGetAlpha(long j);

    private native int[] nativeGetBounds(long j);

    private native int nativeGetHeight(long j);

    private native boolean nativeGetVisible(long j);

    private native int nativeGetWidth(long j);

    private native void nativeSetAlpha(long j, int i);

    private native void nativeSetBounds(long j, int[] iArr);

    private native void nativeSetCallback(long j, OnChangeListener onChangeListener);

    private native void nativeSetColorMatrix(long j, float[] fArr);

    private native boolean nativeSetVisible(long j, boolean z, boolean z2);

    public abstract void createNativePtr();

    public Element getCurrentElement() {
        return this;
    }

    public boolean isStateful() {
        return false;
    }

    public void parseXMLNode(Context context, Node node) {
    }

    public void release() {
    }

    public void skipAnimation() {
    }

    public long getNativeElementPtr() {
        return this.mNativeElementPtr;
    }

    public static class ElementCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeDrawableRelease(long j);

        ElementCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeDrawableRelease(j);
                this.mNativePtr = 0;
            }
        }
    }

    public Element() {
        createNativePtr();
        registerCleaner();
        bind();
    }

    private void registerCleaner() {
        if (this.mNativeElementPtr != 0) {
            MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new ElementCleaner(this.mNativeElementPtr), this.mNativeElementPtr);
        }
    }

    public void setAlpha(int i) {
        nativeSetAlpha(this.mNativeElementPtr, Math.max(0, Math.min(255, i)));
    }

    public int getAlpha() {
        return nativeGetAlpha(this.mNativeElementPtr);
    }

    public void drawToCanvas(Canvas canvas) {
        if (canvas != null) {
            canvas.drawElement(this);
        }
    }

    public boolean setVisible(boolean z, boolean z2) {
        return nativeSetVisible(this.mNativeElementPtr, z, z2);
    }

    public boolean getVisible() {
        return nativeGetVisible(this.mNativeElementPtr);
    }

    public void setBounds(int i, int i2, int i3, int i4) {
        setBounds(new Rect(i, i2, i3, i4));
    }

    public void setBounds(Rect rect) {
        if (rect == null) {
            rect = DEFAULT_BOUNDS_RECT;
        }
        nativeSetBounds(this.mNativeElementPtr, new int[]{rect.left, rect.top, rect.right, rect.bottom});
    }

    public final Rect getBounds() {
        int[] nativeGetBounds = nativeGetBounds(this.mNativeElementPtr);
        return new Rect(nativeGetBounds[0], nativeGetBounds[1], nativeGetBounds[2], nativeGetBounds[3]);
    }

    public void bind() {
        long j = this.mNativeElementPtr;
        if (j != 0) {
            nativeElementBind(j);
        }
    }

    public int getWidth() {
        return nativeGetWidth(this.mNativeElementPtr);
    }

    public int getHeight() {
        return nativeGetHeight(this.mNativeElementPtr);
    }

    public int getMinWidth() {
        return Math.max(getWidth(), 0);
    }

    public int getMinHeight() {
        return Math.max(getHeight(), 0);
    }

    public void setColorMatrix(ColorMatrix colorMatrix) {
        if (colorMatrix != null) {
            nativeSetColorMatrix(this.mNativeElementPtr, colorMatrix.getMatrix());
            this.mColorMatrix = colorMatrix;
            return;
        }
        nativeSetColorMatrix(this.mNativeElementPtr, null);
        this.mColorMatrix = null;
    }

    public ColorMatrix getColorMatrix() {
        return this.mColorMatrix;
    }

    public void clearColorMatrix() {
        setColorMatrix(null);
    }

    public void setCallback(OnChangeListener onChangeListener) {
        this.mOnChangeListener = onChangeListener;
        nativeSetCallback(this.mNativeElementPtr, this.mOnChangeListener);
    }
}
