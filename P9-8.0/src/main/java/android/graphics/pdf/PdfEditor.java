package android.graphics.pdf;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.OsConstants;
import dalvik.system.CloseGuard;
import java.io.IOException;
import libcore.io.IoUtils;
import libcore.io.Libcore;

public final class PdfEditor {
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private ParcelFileDescriptor mInput;
    private final long mNativeDocument;
    private int mPageCount;

    private static native void nativeClose(long j);

    private static native int nativeGetPageCount(long j);

    private static native boolean nativeGetPageCropBox(long j, int i, Rect rect);

    private static native boolean nativeGetPageMediaBox(long j, int i, Rect rect);

    private static native void nativeGetPageSize(long j, int i, Point point);

    private static native long nativeOpen(int i, long j);

    private static native int nativeRemovePage(long j, int i);

    private static native boolean nativeScaleForPrinting(long j);

    private static native void nativeSetPageCropBox(long j, int i, Rect rect);

    private static native void nativeSetPageMediaBox(long j, int i, Rect rect);

    private static native void nativeSetTransformAndClip(long j, int i, long j2, int i2, int i3, int i4, int i5);

    private static native void nativeWrite(long j, int i);

    public PdfEditor(ParcelFileDescriptor input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        }
        try {
            Libcore.os.lseek(input.getFileDescriptor(), 0, OsConstants.SEEK_SET);
            long size = Libcore.os.fstat(input.getFileDescriptor()).st_size;
            this.mInput = input;
            synchronized (PdfRenderer.sPdfiumLock) {
                this.mNativeDocument = nativeOpen(this.mInput.getFd(), size);
                this.mPageCount = nativeGetPageCount(this.mNativeDocument);
            }
            this.mCloseGuard.open("close");
        } catch (ErrnoException e) {
            throw new IllegalArgumentException("file descriptor not seekable");
        }
    }

    public int getPageCount() {
        throwIfClosed();
        return this.mPageCount;
    }

    public void removePage(int pageIndex) {
        throwIfClosed();
        throwIfPageNotInDocument(pageIndex);
        synchronized (PdfRenderer.sPdfiumLock) {
            this.mPageCount = nativeRemovePage(this.mNativeDocument, pageIndex);
        }
    }

    public void setTransformAndClip(int pageIndex, Matrix transform, Rect clip) {
        throwIfClosed();
        throwIfPageNotInDocument(pageIndex);
        throwIfNotNullAndNotAfine(transform);
        if (transform == null) {
            transform = Matrix.IDENTITY_MATRIX;
        }
        Object obj;
        if (clip == null) {
            Point size = new Point();
            getPageSize(pageIndex, size);
            obj = PdfRenderer.sPdfiumLock;
            synchronized (obj) {
                nativeSetTransformAndClip(this.mNativeDocument, pageIndex, transform.native_instance, 0, 0, size.x, size.y);
            }
        } else {
            obj = PdfRenderer.sPdfiumLock;
            synchronized (obj) {
                nativeSetTransformAndClip(this.mNativeDocument, pageIndex, transform.native_instance, clip.left, clip.top, clip.right, clip.bottom);
            }
        }
    }

    public void getPageSize(int pageIndex, Point outSize) {
        throwIfClosed();
        throwIfOutSizeNull(outSize);
        throwIfPageNotInDocument(pageIndex);
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeGetPageSize(this.mNativeDocument, pageIndex, outSize);
        }
    }

    public boolean getPageMediaBox(int pageIndex, Rect outMediaBox) {
        boolean nativeGetPageMediaBox;
        throwIfClosed();
        throwIfOutMediaBoxNull(outMediaBox);
        throwIfPageNotInDocument(pageIndex);
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeGetPageMediaBox = nativeGetPageMediaBox(this.mNativeDocument, pageIndex, outMediaBox);
        }
        return nativeGetPageMediaBox;
    }

    public void setPageMediaBox(int pageIndex, Rect mediaBox) {
        throwIfClosed();
        throwIfMediaBoxNull(mediaBox);
        throwIfPageNotInDocument(pageIndex);
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeSetPageMediaBox(this.mNativeDocument, pageIndex, mediaBox);
        }
    }

    public boolean getPageCropBox(int pageIndex, Rect outCropBox) {
        boolean nativeGetPageCropBox;
        throwIfClosed();
        throwIfOutCropBoxNull(outCropBox);
        throwIfPageNotInDocument(pageIndex);
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeGetPageCropBox = nativeGetPageCropBox(this.mNativeDocument, pageIndex, outCropBox);
        }
        return nativeGetPageCropBox;
    }

    public void setPageCropBox(int pageIndex, Rect cropBox) {
        throwIfClosed();
        throwIfCropBoxNull(cropBox);
        throwIfPageNotInDocument(pageIndex);
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeSetPageCropBox(this.mNativeDocument, pageIndex, cropBox);
        }
    }

    public boolean shouldScaleForPrinting() {
        boolean nativeScaleForPrinting;
        throwIfClosed();
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeScaleForPrinting = nativeScaleForPrinting(this.mNativeDocument);
        }
        return nativeScaleForPrinting;
    }

    public void write(ParcelFileDescriptor output) throws IOException {
        try {
            throwIfClosed();
            synchronized (PdfRenderer.sPdfiumLock) {
                nativeWrite(this.mNativeDocument, output.getFd());
            }
        } finally {
            IoUtils.closeQuietly(output);
        }
    }

    public void close() {
        throwIfClosed();
        doClose();
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            if (this.mInput != null) {
                doClose();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void doClose() {
        synchronized (PdfRenderer.sPdfiumLock) {
            nativeClose(this.mNativeDocument);
        }
        IoUtils.closeQuietly(this.mInput);
        this.mInput = null;
        this.mCloseGuard.close();
    }

    private void throwIfClosed() {
        if (this.mInput == null) {
            throw new IllegalStateException("Already closed");
        }
    }

    private void throwIfPageNotInDocument(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= this.mPageCount) {
            throw new IllegalArgumentException("Invalid page index");
        }
    }

    private void throwIfNotNullAndNotAfine(Matrix matrix) {
        if (matrix != null && (matrix.isAffine() ^ 1) != 0) {
            throw new IllegalStateException("Matrix must be afine");
        }
    }

    private void throwIfOutSizeNull(Point outSize) {
        if (outSize == null) {
            throw new NullPointerException("outSize cannot be null");
        }
    }

    private void throwIfOutMediaBoxNull(Rect outMediaBox) {
        if (outMediaBox == null) {
            throw new NullPointerException("outMediaBox cannot be null");
        }
    }

    private void throwIfMediaBoxNull(Rect mediaBox) {
        if (mediaBox == null) {
            throw new NullPointerException("mediaBox cannot be null");
        }
    }

    private void throwIfOutCropBoxNull(Rect outCropBox) {
        if (outCropBox == null) {
            throw new NullPointerException("outCropBox cannot be null");
        }
    }

    private void throwIfCropBoxNull(Rect cropBox) {
        if (cropBox == null) {
            throw new NullPointerException("cropBox cannot be null");
        }
    }
}
