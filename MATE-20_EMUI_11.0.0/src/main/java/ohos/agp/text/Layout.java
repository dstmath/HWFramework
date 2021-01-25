package ohos.agp.text;

import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public abstract class Layout {
    protected long mNativeLayoutHandle = 0;

    private native void nativeDrawHighlight(long j, long j2, long j3, long j4, int i);

    private native void nativeDrawText(long j, long j2);

    private native int nativeGetAscent(long j, int i);

    private native int nativeGetBaseline(long j, int i);

    private native int nativeGetBeginCharIndex(long j, int i);

    private native int nativeGetBottom(long j, int i);

    private native int nativeGetCharIndexByCoordinate(long j, int i, float f);

    private native float nativeGetCoordinateForVertical(long j, int i);

    private native float nativeGetCoordinateInMainDir(long j, int i);

    private native float nativeGetCoordinateInSecondDir(long j, int i);

    private native int nativeGetDescent(long j, int i);

    private native int nativeGetEndCharIndex(long j, int i);

    private native int nativeGetHeight(long j);

    private native int nativeGetLimitWidth(long j, int i);

    private native int nativeGetLineCount(long j);

    private native float nativeGetLineEnd(long j, int i);

    private native int nativeGetLineIndexByCharIndex(long j, int i);

    private native int nativeGetLineIndexByCoordinate(long j, int i);

    private native float nativeGetLineStart(long j, int i);

    private native float nativeGetNumOfFontHeight(long j);

    private native float nativeGetTextWidth(long j, int i);

    private native int nativeGetTop(long j, int i);

    private native int nativeGetWidth(long j);

    /* access modifiers changed from: protected */
    public void initLayout(long j) {
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new LayoutCleaner(j), j);
    }

    public void drawText(Canvas canvas) {
        nativeDrawText(this.mNativeLayoutHandle, canvas.getNativePtr());
    }

    public void drawHighlight(Canvas canvas, Path path, Paint paint, int i) {
        nativeDrawHighlight(this.mNativeLayoutHandle, canvas.getNativePtr(), path.getNativeHandle(), paint.getNativeHandle(), i);
    }

    public final int getWidth() {
        return nativeGetWidth(this.mNativeLayoutHandle);
    }

    public int getHeight() {
        return nativeGetHeight(this.mNativeLayoutHandle);
    }

    public int getAscent(int i) {
        return nativeGetAscent(this.mNativeLayoutHandle, i);
    }

    public int getDescent(int i) {
        return nativeGetDescent(this.mNativeLayoutHandle, i);
    }

    public int getBaseline(int i) {
        return nativeGetBaseline(this.mNativeLayoutHandle, i);
    }

    public int getTop(int i) {
        return nativeGetTop(this.mNativeLayoutHandle, i);
    }

    public int getBottom(int i) {
        return nativeGetBottom(this.mNativeLayoutHandle, i);
    }

    public int getLineCount() {
        return nativeGetLineCount(this.mNativeLayoutHandle);
    }

    public int getBeginCharIndex(int i) {
        return nativeGetBeginCharIndex(this.mNativeLayoutHandle, i);
    }

    public int getEndCharIndex(int i) {
        return nativeGetEndCharIndex(this.mNativeLayoutHandle, i);
    }

    public int getLineIndexByCharIndex(int i) {
        return nativeGetLineIndexByCharIndex(this.mNativeLayoutHandle, i);
    }

    public float getLineStart(int i) {
        return nativeGetLineStart(this.mNativeLayoutHandle, i);
    }

    public float getLineEnd(int i) {
        return nativeGetLineEnd(this.mNativeLayoutHandle, i);
    }

    public float getTextWidth(int i) {
        return nativeGetTextWidth(this.mNativeLayoutHandle, i);
    }

    public int getLimitWidth(int i) {
        return nativeGetLimitWidth(this.mNativeLayoutHandle, i);
    }

    public int getCharIndexByCoordinate(int i, float f) {
        return nativeGetCharIndexByCoordinate(this.mNativeLayoutHandle, i, f);
    }

    public float getCoordinateInSecondDir(int i) {
        return nativeGetCoordinateInSecondDir(this.mNativeLayoutHandle, i);
    }

    public float getCoordinateInMainDir(int i) {
        return nativeGetCoordinateInMainDir(this.mNativeLayoutHandle, i);
    }

    public float getNumOfFontHeight() {
        return nativeGetNumOfFontHeight(this.mNativeLayoutHandle);
    }

    public float getCoordinateForVertical(int i) {
        return nativeGetCoordinateForVertical(this.mNativeLayoutHandle, i);
    }

    public int getLineIndexByCoordinate(int i) {
        return nativeGetLineIndexByCoordinate(this.mNativeLayoutHandle, i);
    }

    protected static class LayoutCleaner extends NativeMemoryCleanerHelper {
        private native void nativeLayoutRelease(long j);

        public LayoutCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeLayoutRelease(j);
            }
        }
    }
}
