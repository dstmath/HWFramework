package android.graphics.pdf;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import libcore.io.IoUtils;

public final class PdfRenderer implements AutoCloseable {
    static final Object sPdfiumLock = new Object();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    @UnsupportedAppUsage
    private Page mCurrentPage;
    private ParcelFileDescriptor mInput;
    private long mNativeDocument;
    private final int mPageCount;
    private final Point mTempPoint = new Point();

    @Retention(RetentionPolicy.SOURCE)
    public @interface RenderMode {
    }

    private static native void nativeClose(long j);

    /* access modifiers changed from: private */
    public static native void nativeClosePage(long j);

    private static native long nativeCreate(int i, long j);

    private static native int nativeGetPageCount(long j);

    /* access modifiers changed from: private */
    public static native long nativeOpenPageAndGetSize(long j, int i, Point point);

    /* access modifiers changed from: private */
    public static native void nativeRenderPage(long j, long j2, long j3, int i, int i2, int i3, int i4, long j4, int i5);

    private static native boolean nativeScaleForPrinting(long j);

    public PdfRenderer(ParcelFileDescriptor input) throws IOException {
        if (input != null) {
            try {
                Os.lseek(input.getFileDescriptor(), 0, OsConstants.SEEK_SET);
                long size = Os.fstat(input.getFileDescriptor()).st_size;
                this.mInput = input;
                synchronized (sPdfiumLock) {
                    this.mNativeDocument = nativeCreate(this.mInput.getFd(), size);
                    try {
                        this.mPageCount = nativeGetPageCount(this.mNativeDocument);
                    } catch (Throwable t) {
                        nativeClose(this.mNativeDocument);
                        this.mNativeDocument = 0;
                        throw t;
                    }
                }
                this.mCloseGuard.open("close");
            } catch (ErrnoException e) {
                throw new IllegalArgumentException("file descriptor not seekable");
            }
        } else {
            throw new NullPointerException("input cannot be null");
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        throwIfClosed();
        throwIfPageOpened();
        doClose();
    }

    public int getPageCount() {
        throwIfClosed();
        return this.mPageCount;
    }

    public boolean shouldScaleForPrinting() {
        boolean nativeScaleForPrinting;
        throwIfClosed();
        synchronized (sPdfiumLock) {
            nativeScaleForPrinting = nativeScaleForPrinting(this.mNativeDocument);
        }
        return nativeScaleForPrinting;
    }

    public Page openPage(int index) {
        throwIfClosed();
        throwIfPageOpened();
        throwIfPageNotInDocument(index);
        this.mCurrentPage = new Page(index);
        return this.mCurrentPage;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            doClose();
        } finally {
            super.finalize();
        }
    }

    @UnsupportedAppUsage
    private void doClose() {
        Page page = this.mCurrentPage;
        if (page != null) {
            page.close();
            this.mCurrentPage = null;
        }
        if (this.mNativeDocument != 0) {
            synchronized (sPdfiumLock) {
                nativeClose(this.mNativeDocument);
            }
            this.mNativeDocument = 0;
        }
        ParcelFileDescriptor parcelFileDescriptor = this.mInput;
        if (parcelFileDescriptor != null) {
            IoUtils.closeQuietly(parcelFileDescriptor);
            this.mInput = null;
        }
        this.mCloseGuard.close();
    }

    private void throwIfClosed() {
        if (this.mInput == null) {
            throw new IllegalStateException("Already closed");
        }
    }

    private void throwIfPageOpened() {
        if (this.mCurrentPage != null) {
            throw new IllegalStateException("Current page not closed");
        }
    }

    private void throwIfPageNotInDocument(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= this.mPageCount) {
            throw new IllegalArgumentException("Invalid page index");
        }
    }

    public final class Page implements AutoCloseable {
        public static final int RENDER_MODE_FOR_DISPLAY = 1;
        public static final int RENDER_MODE_FOR_PRINT = 2;
        private final CloseGuard mCloseGuard;
        private final int mHeight;
        private final int mIndex;
        private long mNativePage;
        private final int mWidth;

        private Page(int index) {
            this.mCloseGuard = CloseGuard.get();
            Point size = PdfRenderer.this.mTempPoint;
            synchronized (PdfRenderer.sPdfiumLock) {
                this.mNativePage = PdfRenderer.nativeOpenPageAndGetSize(PdfRenderer.this.mNativeDocument, index, size);
            }
            this.mIndex = index;
            this.mWidth = size.x;
            this.mHeight = size.y;
            this.mCloseGuard.open("close");
        }

        public int getIndex() {
            return this.mIndex;
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public void render(Bitmap destination, Rect destClip, Matrix transform, int renderMode) {
            int contentRight;
            int contentBottom;
            Matrix transform2;
            if (this.mNativePage != 0) {
                Bitmap destination2 = (Bitmap) Preconditions.checkNotNull(destination, "bitmap null");
                if (destination2.getConfig() != Bitmap.Config.ARGB_8888) {
                    throw new IllegalArgumentException("Unsupported pixel format");
                } else if (destClip != null && (destClip.left < 0 || destClip.top < 0 || destClip.right > destination2.getWidth() || destClip.bottom > destination2.getHeight())) {
                    throw new IllegalArgumentException("destBounds not in destination");
                } else if (transform != null && !transform.isAffine()) {
                    throw new IllegalArgumentException("transform not affine");
                } else if (renderMode != 2 && renderMode != 1) {
                    throw new IllegalArgumentException("Unsupported render mode");
                } else if (renderMode == 2 && renderMode == 1) {
                    throw new IllegalArgumentException("Only single render mode supported");
                } else {
                    int contentTop = 0;
                    int contentLeft = destClip != null ? destClip.left : 0;
                    if (destClip != null) {
                        contentTop = destClip.top;
                    }
                    if (destClip != null) {
                        contentRight = destClip.right;
                    } else {
                        contentRight = destination2.getWidth();
                    }
                    if (destClip != null) {
                        contentBottom = destClip.bottom;
                    } else {
                        contentBottom = destination2.getHeight();
                    }
                    if (transform == null) {
                        Matrix transform3 = new Matrix();
                        transform3.postScale(((float) (contentRight - contentLeft)) / ((float) getWidth()), ((float) (contentBottom - contentTop)) / ((float) getHeight()));
                        transform3.postTranslate((float) contentLeft, (float) contentTop);
                        transform2 = transform3;
                    } else {
                        transform2 = transform;
                    }
                    long transformPtr = transform2.native_instance;
                    synchronized (PdfRenderer.sPdfiumLock) {
                        try {
                            PdfRenderer.nativeRenderPage(PdfRenderer.this.mNativeDocument, this.mNativePage, destination2.getNativeInstance(), contentLeft, contentTop, contentRight, contentBottom, transformPtr, renderMode);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            throwIfClosed();
            doClose();
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            try {
                if (this.mCloseGuard != null) {
                    this.mCloseGuard.warnIfOpen();
                }
                doClose();
            } finally {
                super.finalize();
            }
        }

        private void doClose() {
            if (this.mNativePage != 0) {
                synchronized (PdfRenderer.sPdfiumLock) {
                    PdfRenderer.nativeClosePage(this.mNativePage);
                }
                this.mNativePage = 0;
            }
            this.mCloseGuard.close();
            PdfRenderer.this.mCurrentPage = null;
        }

        private void throwIfClosed() {
            if (this.mNativePage == 0) {
                throw new IllegalStateException("Already closed");
            }
        }
    }
}
