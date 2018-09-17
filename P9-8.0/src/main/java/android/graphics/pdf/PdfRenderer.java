package android.graphics.pdf;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.OsConstants;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.IOException;
import libcore.io.Libcore;

public final class PdfRenderer implements AutoCloseable {
    static final Object sPdfiumLock = new Object();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private Page mCurrentPage;
    private ParcelFileDescriptor mInput;
    private final long mNativeDocument;
    private final int mPageCount;
    private final Point mTempPoint = new Point();

    public final class Page implements AutoCloseable {
        public static final int RENDER_MODE_FOR_DISPLAY = 1;
        public static final int RENDER_MODE_FOR_PRINT = 2;
        private final CloseGuard mCloseGuard;
        private final int mHeight;
        private final int mIndex;
        private long mNativePage;
        private final int mWidth;

        /* synthetic */ Page(PdfRenderer this$0, int index, Page -this2) {
            this(index);
        }

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
            if (this.mNativePage == 0) {
                throw new NullPointerException();
            }
            destination = (Bitmap) Preconditions.checkNotNull(destination, "bitmap null");
            if (destination.getConfig() != Config.ARGB_8888) {
                throw new IllegalArgumentException("Unsupported pixel format");
            } else if (destClip != null && (destClip.left < 0 || destClip.top < 0 || destClip.right > destination.getWidth() || destClip.bottom > destination.getHeight())) {
                throw new IllegalArgumentException("destBounds not in destination");
            } else if (transform != null && (transform.isAffine() ^ 1) != 0) {
                throw new IllegalArgumentException("transform not affine");
            } else if (renderMode != 2 && renderMode != 1) {
                throw new IllegalArgumentException("Unsupported render mode");
            } else if (renderMode == 2 && renderMode == 1) {
                throw new IllegalArgumentException("Only single render mode supported");
            } else {
                int contentRight;
                int contentBottom;
                int contentLeft = destClip != null ? destClip.left : 0;
                int contentTop = destClip != null ? destClip.top : 0;
                if (destClip != null) {
                    contentRight = destClip.right;
                } else {
                    contentRight = destination.getWidth();
                }
                if (destClip != null) {
                    contentBottom = destClip.bottom;
                } else {
                    contentBottom = destination.getHeight();
                }
                if (transform == null) {
                    int clipWidth = contentRight - contentLeft;
                    int clipHeight = contentBottom - contentTop;
                    transform = new Matrix();
                    transform.postScale(((float) clipWidth) / ((float) getWidth()), ((float) clipHeight) / ((float) getHeight()));
                    transform.postTranslate((float) contentLeft, (float) contentTop);
                }
                long transformPtr = transform.native_instance;
                synchronized (PdfRenderer.sPdfiumLock) {
                    PdfRenderer.nativeRenderPage(PdfRenderer.this.mNativeDocument, this.mNativePage, destination, contentLeft, contentTop, contentRight, contentBottom, transformPtr, renderMode);
                }
            }
        }

        public void close() {
            throwIfClosed();
            doClose();
        }

        protected void finalize() throws Throwable {
            try {
                this.mCloseGuard.warnIfOpen();
                if (this.mNativePage != 0) {
                    doClose();
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }

        private void doClose() {
            synchronized (PdfRenderer.sPdfiumLock) {
                PdfRenderer.nativeClosePage(this.mNativePage);
            }
            this.mNativePage = 0;
            this.mCloseGuard.close();
            PdfRenderer.this.mCurrentPage = null;
        }

        private void throwIfClosed() {
            if (this.mNativePage == 0) {
                throw new IllegalStateException("Already closed");
            }
        }
    }

    private static native void nativeClose(long j);

    private static native void nativeClosePage(long j);

    private static native long nativeCreate(int i, long j);

    private static native int nativeGetPageCount(long j);

    private static native long nativeOpenPageAndGetSize(long j, int i, Point point);

    private static native void nativeRenderPage(long j, long j2, Bitmap bitmap, int i, int i2, int i3, int i4, long j3, int i5);

    private static native boolean nativeScaleForPrinting(long j);

    public PdfRenderer(ParcelFileDescriptor input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        }
        try {
            Libcore.os.lseek(input.getFileDescriptor(), 0, OsConstants.SEEK_SET);
            long size = Libcore.os.fstat(input.getFileDescriptor()).st_size;
            this.mInput = input;
            synchronized (sPdfiumLock) {
                this.mNativeDocument = nativeCreate(this.mInput.getFd(), size);
                try {
                    this.mPageCount = nativeGetPageCount(this.mNativeDocument);
                } catch (Throwable th) {
                    nativeClose(this.mNativeDocument);
                }
            }
            this.mCloseGuard.open("close");
        } catch (ErrnoException e) {
            throw new IllegalArgumentException("file descriptor not seekable");
        }
    }

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
        this.mCurrentPage = new Page(this, index, null);
        return this.mCurrentPage;
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
        if (this.mCurrentPage != null) {
            this.mCurrentPage.close();
        }
        synchronized (sPdfiumLock) {
            nativeClose(this.mNativeDocument);
        }
        try {
            this.mInput.close();
        } catch (IOException e) {
        }
        this.mInput = null;
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
}
