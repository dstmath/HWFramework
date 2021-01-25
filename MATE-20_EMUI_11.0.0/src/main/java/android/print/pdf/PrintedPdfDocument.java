package android.print.pdf;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.print.PrintAttributes;

public class PrintedPdfDocument extends PdfDocument {
    private static final int MILS_PER_INCH = 1000;
    private static final int POINTS_IN_INCH = 72;
    private final Rect mContentRect;
    private final int mPageHeight;
    private final int mPageWidth;

    public PrintedPdfDocument(Context context, PrintAttributes attributes) {
        PrintAttributes.MediaSize mediaSize = attributes.getMediaSize();
        this.mPageWidth = (int) ((((float) mediaSize.getWidthMils()) / 1000.0f) * 72.0f);
        this.mPageHeight = (int) ((((float) mediaSize.getHeightMils()) / 1000.0f) * 72.0f);
        PrintAttributes.Margins minMargins = attributes.getMinMargins();
        int marginBottom = (int) ((((float) minMargins.getBottomMils()) / 1000.0f) * 72.0f);
        this.mContentRect = new Rect((int) ((((float) minMargins.getLeftMils()) / 1000.0f) * 72.0f), (int) ((((float) minMargins.getTopMils()) / 1000.0f) * 72.0f), this.mPageWidth - ((int) ((((float) minMargins.getRightMils()) / 1000.0f) * 72.0f)), this.mPageHeight - marginBottom);
    }

    public PdfDocument.Page startPage(int pageNumber) {
        return startPage(new PdfDocument.PageInfo.Builder(this.mPageWidth, this.mPageHeight, pageNumber).setContentRect(this.mContentRect).create());
    }

    public int getPageWidth() {
        return this.mPageWidth;
    }

    public int getPageHeight() {
        return this.mPageHeight;
    }

    public Rect getPageContentRect() {
        return this.mContentRect;
    }
}
