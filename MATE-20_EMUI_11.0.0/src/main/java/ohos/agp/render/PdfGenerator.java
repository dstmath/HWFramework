package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Rect;

public class PdfGenerator {
    private PdfPage mCurrentPage;
    private long mNativePdfGenerator = nativeCreatePdfGenerator();

    private native long nativeBeginDrawing(long j, int i, int i2, int i3, int i4, int i5, int i6);

    private native long nativeCreatePdfGenerator();

    public PdfGenerator() {
        MemoryCleanerRegistry.getInstance().register(this, new PdfGeneratorCleaner(this.mNativePdfGenerator));
    }

    public PdfPage beginDrawing(PageInfo pageInfo) {
        long j = this.mNativePdfGenerator;
        if (j != 0 && this.mCurrentPage == null && pageInfo != null) {
            return new PdfPage(pageInfo, new PdfCanvas(nativeBeginDrawing(j, pageInfo.mWidth, pageInfo.mHeight, pageInfo.mContentRect.left, pageInfo.mContentRect.top, pageInfo.mContentRect.right, pageInfo.mContentRect.bottom)));
        }
        throw new IllegalArgumentException("document cannot start page");
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

    private static final class PdfCanvas extends Canvas {
        public PdfCanvas(long j) {
            super(j);
        }
    }
}
