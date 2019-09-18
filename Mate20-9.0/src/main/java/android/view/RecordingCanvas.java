package android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.TemporaryBuffer;
import android.text.GraphicsOperations;
import android.text.PrecomputedText;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;

public class RecordingCanvas extends Canvas {
    private static native void nDrawArc(long j, float f, float f2, float f3, float f4, float f5, float f6, boolean z, long j2);

    private static native void nDrawBitmap(long j, Bitmap bitmap, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, long j2, int i, int i2);

    private static native void nDrawBitmap(long j, Bitmap bitmap, float f, float f2, long j2, int i, int i2, int i3);

    private static native void nDrawBitmap(long j, int[] iArr, int i, int i2, float f, float f2, int i3, int i4, boolean z, long j2);

    private static native void nDrawBitmapMatrix(long j, Bitmap bitmap, long j2, long j3);

    private static native void nDrawBitmapMesh(long j, Bitmap bitmap, int i, int i2, float[] fArr, int i3, int[] iArr, int i4, long j2);

    private static native void nDrawCircle(long j, float f, float f2, float f3, long j2);

    private static native void nDrawColor(long j, int i, int i2);

    private static native void nDrawLine(long j, float f, float f2, float f3, float f4, long j2);

    private static native void nDrawLines(long j, float[] fArr, int i, int i2, long j2);

    private static native void nDrawNinePatch(long j, long j2, long j3, float f, float f2, float f3, float f4, long j4, int i, int i2);

    private static native void nDrawOval(long j, float f, float f2, float f3, float f4, long j2);

    private static native void nDrawPaint(long j, long j2);

    private static native void nDrawPath(long j, long j2, long j3);

    private static native void nDrawPoint(long j, float f, float f2, long j2);

    private static native void nDrawPoints(long j, float[] fArr, int i, int i2, long j2);

    private static native void nDrawRect(long j, float f, float f2, float f3, float f4, long j2);

    private static native void nDrawRegion(long j, long j2, long j3);

    private static native void nDrawRoundRect(long j, float f, float f2, float f3, float f4, float f5, float f6, long j2);

    private static native void nDrawText(long j, String str, int i, int i2, float f, float f2, int i3, long j2);

    private static native void nDrawText(long j, char[] cArr, int i, int i2, float f, float f2, int i3, long j2);

    private static native void nDrawTextOnPath(long j, String str, long j2, float f, float f2, int i, long j3);

    private static native void nDrawTextOnPath(long j, char[] cArr, int i, int i2, long j2, float f, float f2, int i3, long j3);

    private static native void nDrawTextRun(long j, String str, int i, int i2, int i3, int i4, float f, float f2, boolean z, long j2);

    private static native void nDrawTextRun(long j, char[] cArr, int i, int i2, int i3, int i4, float f, float f2, boolean z, long j2, long j3);

    private static native void nDrawVertices(long j, int i, int i2, float[] fArr, int i3, float[] fArr2, int i4, int[] iArr, int i5, short[] sArr, int i6, int i7, long j2);

    public RecordingCanvas(long nativeCanvas) {
        super(nativeCanvas);
    }

    public final void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        nDrawArc(this.mNativeCanvasWrapper, left, top, right, bottom, startAngle, sweepAngle, useCenter, paint.getNativeInstance());
    }

    public final void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        drawArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, useCenter, paint);
    }

    public final void drawARGB(int a, int r, int g, int b) {
        drawColor(Color.argb(a, r, g, b));
    }

    public final void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        throwIfCannotDraw(bitmap);
        nDrawBitmap(this.mNativeCanvasWrapper, bitmap, left, top, paint != null ? paint.getNativeInstance() : 0, this.mDensity, this.mScreenDensity, bitmap.mDensity);
    }

    public final void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        nDrawBitmapMatrix(this.mNativeCanvasWrapper, bitmap, matrix.ni(), paint != null ? paint.getNativeInstance() : 0);
    }

    public final void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        int left;
        int right;
        int top;
        int bottom;
        Rect rect = src;
        Rect rect2 = dst;
        if (rect2 != null) {
            throwIfCannotDraw(bitmap);
            long nativePaint = paint == null ? 0 : paint.getNativeInstance();
            if (rect == null) {
                left = 0;
                top = 0;
                right = bitmap.getWidth();
                bottom = bitmap.getHeight();
            } else {
                left = rect.left;
                right = rect.right;
                top = rect.top;
                bottom = rect.bottom;
            }
            int right2 = right;
            int bottom2 = bottom;
            float f = (float) rect2.left;
            int i = this.mScreenDensity;
            Bitmap bitmap2 = bitmap;
            int i2 = left;
            int i3 = bottom2;
            int i4 = right2;
            nDrawBitmap(this.mNativeCanvasWrapper, bitmap2, (float) left, (float) top, (float) right2, (float) bottom2, f, (float) rect2.top, (float) rect2.right, (float) rect2.bottom, nativePaint, i, bitmap2.mDensity);
            return;
        }
        Bitmap bitmap3 = bitmap;
        throw new NullPointerException();
    }

    public final void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        float left;
        float bottom;
        float right;
        float top;
        Rect rect = src;
        RectF rectF = dst;
        if (rectF != null) {
            throwIfCannotDraw(bitmap);
            long nativePaint = paint == null ? 0 : paint.getNativeInstance();
            if (rect == null) {
                left = 0.0f;
                top = 0.0f;
                bottom = (float) bitmap.getHeight();
                right = (float) bitmap.getWidth();
            } else {
                left = (float) rect.left;
                bottom = (float) rect.bottom;
                right = (float) rect.right;
                top = (float) rect.top;
            }
            float bottom2 = bottom;
            Bitmap bitmap2 = bitmap;
            nDrawBitmap(this.mNativeCanvasWrapper, bitmap2, left, top, right, bottom2, rectF.left, rectF.top, rectF.right, rectF.bottom, nativePaint, this.mScreenDensity, bitmap2.mDensity);
            return;
        }
        throw new NullPointerException();
    }

    @Deprecated
    public final void drawBitmap(int[] colors, int offset, int stride, float x, float y, int width, int height, boolean hasAlpha, Paint paint) {
        int i = width;
        if (i < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        } else if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        } else if (Math.abs(stride) >= i) {
            int lastScanline = offset + ((height - 1) * stride);
            int[] iArr = colors;
            int length = iArr.length;
            if (offset < 0 || offset + i > length || lastScanline < 0 || lastScanline + i > length) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (i == 0) {
            } else if (height == 0) {
                int i2 = length;
            } else {
                int i3 = length;
                nDrawBitmap(this.mNativeCanvasWrapper, iArr, offset, stride, x, y, i, height, hasAlpha, paint != null ? paint.getNativeInstance() : 0);
            }
        } else {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
    }

    @Deprecated
    public final void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint) {
        drawBitmap(colors, offset, stride, (float) x, (float) y, width, height, hasAlpha, paint);
    }

    public final void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight, float[] verts, int vertOffset, int[] colors, int colorOffset, Paint paint) {
        int i = vertOffset;
        int[] iArr = colors;
        int i2 = colorOffset;
        if ((meshWidth | meshHeight | i | i2) < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (meshWidth != 0 && meshHeight != 0) {
            int count = (meshWidth + 1) * (meshHeight + 1);
            float[] fArr = verts;
            checkRange(fArr.length, i, count * 2);
            if (iArr != null) {
                checkRange(iArr.length, i2, count);
            }
            int i3 = count;
            nDrawBitmapMesh(this.mNativeCanvasWrapper, bitmap, meshWidth, meshHeight, fArr, i, iArr, i2, paint != null ? paint.getNativeInstance() : 0);
        }
    }

    public final void drawCircle(float cx, float cy, float radius, Paint paint) {
        nDrawCircle(this.mNativeCanvasWrapper, cx, cy, radius, paint.getNativeInstance());
    }

    public final void drawColor(int color) {
        nDrawColor(this.mNativeCanvasWrapper, color, PorterDuff.Mode.SRC_OVER.nativeInt);
    }

    public final void drawColor(int color, PorterDuff.Mode mode) {
        nDrawColor(this.mNativeCanvasWrapper, color, mode.nativeInt);
    }

    public final void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        nDrawLine(this.mNativeCanvasWrapper, startX, startY, stopX, stopY, paint.getNativeInstance());
    }

    public final void drawLines(float[] pts, int offset, int count, Paint paint) {
        nDrawLines(this.mNativeCanvasWrapper, pts, offset, count, paint.getNativeInstance());
    }

    public final void drawLines(float[] pts, Paint paint) {
        drawLines(pts, 0, pts.length, paint);
    }

    public final void drawOval(float left, float top, float right, float bottom, Paint paint) {
        nDrawOval(this.mNativeCanvasWrapper, left, top, right, bottom, paint.getNativeInstance());
    }

    public final void drawOval(RectF oval, Paint paint) {
        if (oval != null) {
            drawOval(oval.left, oval.top, oval.right, oval.bottom, paint);
            return;
        }
        throw new NullPointerException();
    }

    public final void drawPaint(Paint paint) {
        nDrawPaint(this.mNativeCanvasWrapper, paint.getNativeInstance());
    }

    public final void drawPatch(NinePatch patch, Rect dst, Paint paint) {
        Rect rect = dst;
        Bitmap bitmap = patch.getBitmap();
        throwIfCannotDraw(bitmap);
        nDrawNinePatch(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), patch.mNativeChunk, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, paint == null ? 0 : paint.getNativeInstance(), this.mDensity, patch.getDensity());
    }

    public final void drawPatch(NinePatch patch, RectF dst, Paint paint) {
        RectF rectF = dst;
        Bitmap bitmap = patch.getBitmap();
        throwIfCannotDraw(bitmap);
        nDrawNinePatch(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), patch.mNativeChunk, rectF.left, rectF.top, rectF.right, rectF.bottom, paint == null ? 0 : paint.getNativeInstance(), this.mDensity, patch.getDensity());
    }

    public final void drawPath(Path path, Paint paint) {
        if (!path.isSimplePath || path.rects == null) {
            nDrawPath(this.mNativeCanvasWrapper, path.readOnlyNI(), paint.getNativeInstance());
        } else {
            nDrawRegion(this.mNativeCanvasWrapper, path.rects.mNativeRegion, paint.getNativeInstance());
        }
    }

    public final void drawPicture(Picture picture) {
        picture.endRecording();
        int restoreCount = save();
        picture.draw(this);
        restoreToCount(restoreCount);
    }

    public final void drawPicture(Picture picture, Rect dst) {
        save();
        translate((float) dst.left, (float) dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(((float) dst.width()) / ((float) picture.getWidth()), ((float) dst.height()) / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    public final void drawPicture(Picture picture, RectF dst) {
        save();
        translate(dst.left, dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(dst.width() / ((float) picture.getWidth()), dst.height() / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    public final void drawPoint(float x, float y, Paint paint) {
        nDrawPoint(this.mNativeCanvasWrapper, x, y, paint.getNativeInstance());
    }

    public final void drawPoints(float[] pts, int offset, int count, Paint paint) {
        nDrawPoints(this.mNativeCanvasWrapper, pts, offset, count, paint.getNativeInstance());
    }

    public final void drawPoints(float[] pts, Paint paint) {
        drawPoints(pts, 0, pts.length, paint);
    }

    @Deprecated
    public final void drawPosText(char[] text, int index, int count, float[] pos, Paint paint) {
        if (index < 0 || index + count > text.length || count * 2 > pos.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < count; i++) {
            char[] cArr = text;
            drawText(cArr, index + i, 1, pos[i * 2], pos[(i * 2) + 1], paint);
        }
    }

    @Deprecated
    public final void drawPosText(String text, float[] pos, Paint paint) {
        drawPosText(text.toCharArray(), 0, text.length(), pos, paint);
    }

    public final void drawRect(float left, float top, float right, float bottom, Paint paint) {
        nDrawRect(this.mNativeCanvasWrapper, left, top, right, bottom, paint.getNativeInstance());
    }

    public final void drawRect(Rect r, Paint paint) {
        drawRect((float) r.left, (float) r.top, (float) r.right, (float) r.bottom, paint);
    }

    public final void drawRect(RectF rect, Paint paint) {
        nDrawRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, paint.getNativeInstance());
    }

    public final void drawRGB(int r, int g, int b) {
        drawColor(Color.rgb(r, g, b));
    }

    public final void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
        nDrawRoundRect(this.mNativeCanvasWrapper, left, top, right, bottom, rx, ry, paint.getNativeInstance());
    }

    public final void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint);
    }

    public final void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
        char[] cArr = text;
        if ((index | count | (index + count) | ((cArr.length - index) - count)) >= 0) {
            nDrawText(this.mNativeCanvasWrapper, cArr, index, count, x, y, paint.mBidiFlags, paint.getNativeInstance());
            return;
        }
        Paint paint2 = paint;
        throw new IndexOutOfBoundsException();
    }

    public final void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
        CharSequence charSequence = text;
        int i = start;
        int i2 = end;
        Paint paint2 = paint;
        if ((i | i2 | (i2 - i) | (text.length() - i2)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if ((charSequence instanceof String) || (charSequence instanceof SpannedString) || (charSequence instanceof SpannableString)) {
            long j = this.mNativeCanvasWrapper;
            String charSequence2 = text.toString();
            int i3 = paint2.mBidiFlags;
            nDrawText(j, charSequence2, i, i2, x, y, i3, paint.getNativeInstance());
        } else if (charSequence instanceof GraphicsOperations) {
            ((GraphicsOperations) charSequence).drawText(this, i, i2, x, y, paint2);
        } else {
            char[] buf = TemporaryBuffer.obtain(i2 - i);
            TextUtils.getChars(charSequence, i, i2, buf, 0);
            long j2 = this.mNativeCanvasWrapper;
            nDrawText(j2, buf, 0, i2 - i, x, y, paint2.mBidiFlags, paint.getNativeInstance());
            TemporaryBuffer.recycle(buf);
        }
    }

    public final void drawText(String text, float x, float y, Paint paint) {
        nDrawText(this.mNativeCanvasWrapper, text, 0, text.length(), x, y, paint.mBidiFlags, paint.getNativeInstance());
    }

    public final void drawText(String text, int start, int end, float x, float y, Paint paint) {
        if ((start | end | (end - start) | (text.length() - end)) >= 0) {
            nDrawText(this.mNativeCanvasWrapper, text, start, end, x, y, paint.mBidiFlags, paint.getNativeInstance());
            return;
        }
        Paint paint2 = paint;
        throw new IndexOutOfBoundsException();
    }

    public final void drawTextOnPath(char[] text, int index, int count, Path path, float hOffset, float vOffset, Paint paint) {
        if (index >= 0) {
            char[] cArr = text;
            if (index + count <= cArr.length) {
                nDrawTextOnPath(this.mNativeCanvasWrapper, cArr, index, count, path.readOnlyNI(), hOffset, vOffset, paint.mBidiFlags, paint.getNativeInstance());
                return;
            }
        } else {
            char[] cArr2 = text;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public final void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
        if (text.length() > 0) {
            nDrawTextOnPath(this.mNativeCanvasWrapper, text, path.readOnlyNI(), hOffset, vOffset, paint.mBidiFlags, paint.getNativeInstance());
            return;
        }
        Paint paint2 = paint;
    }

    public final void drawTextRun(char[] text, int index, int count, int contextIndex, int contextCount, float x, float y, boolean isRtl, Paint paint) {
        char[] cArr = text;
        if (cArr == null) {
            throw new NullPointerException("text is null");
        } else if (paint == null) {
            throw new NullPointerException("paint is null");
        } else if ((index | count | contextIndex | contextCount | (index - contextIndex) | ((contextIndex + contextCount) - (index + count)) | (cArr.length - (contextIndex + contextCount))) >= 0) {
            nDrawTextRun(this.mNativeCanvasWrapper, cArr, index, count, contextIndex, contextCount, x, y, isRtl, paint.getNativeInstance(), 0);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public final void drawTextRun(CharSequence text, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {
        CharSequence charSequence = text;
        int i = start;
        int i2 = end;
        int i3 = contextStart;
        int i4 = contextEnd;
        if (charSequence == null) {
            throw new NullPointerException("text is null");
        } else if (paint == null) {
            throw new NullPointerException("paint is null");
        } else if ((i | i2 | i3 | i4 | (i - i3) | (i2 - i) | (i4 - i2) | (text.length() - i4)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if ((charSequence instanceof String) || (charSequence instanceof SpannedString) || (charSequence instanceof SpannableString)) {
            long j = this.mNativeCanvasWrapper;
            nDrawTextRun(j, text.toString(), start, end, contextStart, contextEnd, x, y, isRtl, paint.getNativeInstance());
        } else if (charSequence instanceof GraphicsOperations) {
            ((GraphicsOperations) charSequence).drawTextRun(this, i, i2, i3, i4, x, y, isRtl, paint);
        } else {
            int contextLen = i4 - i3;
            int len = i2 - i;
            char[] buf = TemporaryBuffer.obtain(contextLen);
            TextUtils.getChars(charSequence, i3, i4, buf, 0);
            long measuredTextPtr = 0;
            if (charSequence instanceof PrecomputedText) {
                PrecomputedText mt = (PrecomputedText) charSequence;
                int paraIndex = mt.findParaIndex(i);
                if (i2 <= mt.getParagraphEnd(paraIndex)) {
                    measuredTextPtr = mt.getMeasuredParagraph(paraIndex).getNativePtr();
                }
            }
            nDrawTextRun(this.mNativeCanvasWrapper, buf, i - i3, len, 0, contextLen, x, y, isRtl, paint.getNativeInstance(), measuredTextPtr);
            TemporaryBuffer.recycle(buf);
        }
    }

    public final void drawVertices(Canvas.VertexMode mode, int vertexCount, float[] verts, int vertOffset, float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices, int indexOffset, int indexCount, Paint paint) {
        int i;
        int i2 = vertexCount;
        float[] fArr = texs;
        int[] iArr = colors;
        short[] sArr = indices;
        float[] fArr2 = verts;
        int i3 = vertOffset;
        checkRange(fArr2.length, i3, i2);
        if (!isHardwareAccelerated()) {
            if (fArr != null) {
                i = texOffset;
                checkRange(fArr.length, i, i2);
            } else {
                i = texOffset;
            }
            if (iArr != null) {
                checkRange(iArr.length, colorOffset, i2 / 2);
            } else {
                int i4 = colorOffset;
            }
            if (sArr != null) {
                checkRange(sArr.length, indexOffset, indexCount);
            } else {
                int i5 = indexOffset;
                int i6 = indexCount;
            }
            nDrawVertices(this.mNativeCanvasWrapper, mode.nativeInt, i2, fArr2, i3, fArr, i, iArr, colorOffset, sArr, indexOffset, indexCount, paint.getNativeInstance());
        }
    }
}
