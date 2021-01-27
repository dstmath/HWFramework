package ohos.agp.render;

import java.util.ArrayList;
import java.util.List;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Rect;

public class PdfGenerator {
    private PdfPage mCurrentPage;
    private long mNativePdfGenerator = nativeCreatePdfGenerator();
    private final List<PageInfo> mPageInfos = new ArrayList();

    private native long nativeBeginDrawing(long j, int i, int i2, int i3, int i4, int i5, int i6);

    private native void nativeClose(long j);

    private native long nativeCreatePdfGenerator();

    private native void nativeEndDrawing(long j);

    public PdfGenerator() {
        MemoryCleanerRegistry.getInstance().register(this, new PdfGeneratorCleaner(this.mNativePdfGenerator));
    }

    public PdfPage beginDrawing(PageInfo pageInfo) {
        long j = this.mNativePdfGenerator;
        if (j == 0 || this.mCurrentPage != null || pageInfo == null) {
            throw new IllegalArgumentException("document cannot start page");
        }
        this.mCurrentPage = new PdfPage(pageInfo, new PdfCanvas(nativeBeginDrawing(j, pageInfo.mWidth, pageInfo.mHeight, pageInfo.mContentRect.left, pageInfo.mContentRect.top, pageInfo.mContentRect.right, pageInfo.mContentRect.bottom)));
        return this.mCurrentPage;
    }

    public void endDrawing(PdfPage pdfPage) {
        throwExceptionIfIsClosed();
        if (pdfPage == null || pdfPage != this.mCurrentPage) {
            throw new IllegalArgumentException("Pdf page is invalid");
        } else if (!pdfPage.isFinished()) {
            this.mPageInfos.add(pdfPage.getPageInfo());
            this.mCurrentPage = null;
            nativeEndDrawing(this.mNativePdfGenerator);
            pdfPage.finish();
        } else {
            throw new IllegalStateException("Pdf page already finished");
        }
    }

    public void close() {
        if (this.mCurrentPage == null) {
            long j = this.mNativePdfGenerator;
            if (j != 0) {
                nativeClose(j);
                this.mNativePdfGenerator = 0;
                return;
            }
            return;
        }
        throw new IllegalStateException("Current pdf page not finished");
    }

    public static final class PageInfo {
        private Rect mContentRect;
        private int mHeight;
        private int mNumber;
        private int mWidth;

        private PageInfo() {
        }

        public static final class Builder {
            private final PageInfo mPageInfo = new PageInfo();

            public Builder(int i, int i2, int i3) {
                if (i <= 0 || i2 <= 0 || i3 < 0) {
                    throw new IllegalArgumentException("Page info param illegal");
                }
                this.mPageInfo.mWidth = i;
                this.mPageInfo.mHeight = i2;
                this.mPageInfo.mNumber = i3;
            }

            public PageInfo create() {
                if (this.mPageInfo.mContentRect == null) {
                    PageInfo pageInfo = this.mPageInfo;
                    pageInfo.mContentRect = new Rect(0, 0, pageInfo.mWidth, this.mPageInfo.mHeight);
                }
                return this.mPageInfo;
            }
        }
    }

    public static final class PdfPage {
        private Canvas mCanvas;
        private final PageInfo mPageInfo;

        private PdfPage(PageInfo pageInfo, Canvas canvas) {
            this.mCanvas = canvas;
            this.mPageInfo = pageInfo;
        }

        public Canvas getCanvas() {
            return this.mCanvas;
        }

        public PageInfo getPageInfo() {
            return this.mPageInfo;
        }

        /* access modifiers changed from: package-private */
        public boolean isFinished() {
            return this.mCanvas == null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void finish() {
            if (this.mCanvas != null) {
                this.mCanvas = null;
            }
        }
    }

    protected static class PdfGeneratorCleaner extends NativeMemoryCleanerHelper {
        private native void nativePdfGeneratorRelease(long j);

        public PdfGeneratorCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePdfGeneratorRelease(j);
            }
        }
    }

    private void throwExceptionIfIsClosed() {
        if (this.mNativePdfGenerator == 0) {
            throw new IllegalStateException("Pdf document is closed");
        }
    }

    private static final class PdfCanvas extends Canvas {
        public PdfCanvas(long j) {
            super(j);
        }
    }
}
