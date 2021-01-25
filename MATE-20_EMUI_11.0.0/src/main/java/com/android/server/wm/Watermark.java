package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;

/* access modifiers changed from: package-private */
public class Watermark {
    private final int mDeltaX;
    private final int mDeltaY;
    private final Display mDisplay;
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl;
    private final String mText;
    private final int mTextHeight;
    private final Paint mTextPaint;
    private final int mTextWidth;
    private final String[] mTokens;

    Watermark(DisplayContent dc, DisplayMetrics dm, String[] tokens) {
        int c1;
        int c2;
        this.mDisplay = dc.getDisplay();
        this.mTokens = tokens;
        StringBuilder builder = new StringBuilder(32);
        int len = this.mTokens[0].length() & -2;
        for (int i = 0; i < len; i += 2) {
            int c12 = this.mTokens[0].charAt(i);
            int c22 = this.mTokens[0].charAt(i + 1);
            if (c12 >= 97 && c12 <= 102) {
                c1 = (c12 - 97) + 10;
            } else if (c12 < 65 || c12 > 70) {
                c1 = c12 - 48;
            } else {
                c1 = (c12 - 65) + 10;
            }
            if (c22 >= 97 && c22 <= 102) {
                c2 = (c22 - 97) + 10;
            } else if (c22 < 65 || c22 > 70) {
                c2 = c22 - 48;
            } else {
                c2 = (c22 - 65) + 10;
            }
            builder.append((char) (255 - ((c1 * 16) + c2)));
        }
        this.mText = builder.toString();
        int fontSize = WindowManagerService.getPropertyInt(tokens, 1, 1, 20, dm);
        this.mTextPaint = new Paint(1);
        this.mTextPaint.setTextSize((float) fontSize);
        this.mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, 1));
        Paint.FontMetricsInt fm = this.mTextPaint.getFontMetricsInt();
        this.mTextWidth = (int) this.mTextPaint.measureText(this.mText);
        this.mTextHeight = fm.descent - fm.ascent;
        this.mDeltaX = WindowManagerService.getPropertyInt(tokens, 2, 0, this.mTextWidth * 2, dm);
        this.mDeltaY = WindowManagerService.getPropertyInt(tokens, 3, 0, this.mTextHeight * 3, dm);
        int shadowColor = WindowManagerService.getPropertyInt(tokens, 4, 0, -1342177280, dm);
        int color = WindowManagerService.getPropertyInt(tokens, 5, 0, 1627389951, dm);
        int shadowRadius = WindowManagerService.getPropertyInt(tokens, 6, 0, 7, dm);
        int shadowDx = WindowManagerService.getPropertyInt(tokens, 8, 0, 0, dm);
        int shadowDy = WindowManagerService.getPropertyInt(tokens, 9, 0, 0, dm);
        this.mTextPaint.setColor(color);
        this.mTextPaint.setShadowLayer((float) shadowRadius, (float) shadowDx, (float) shadowDy, shadowColor);
        SurfaceControl ctrl = null;
        try {
            ctrl = dc.makeOverlay().setName("WatermarkSurface").setBufferSize(1, 1).setFormat(-3).build();
            ctrl.setLayerStack(this.mDisplay.getLayerStack());
            ctrl.setLayer(1000000);
            ctrl.setPosition(0.0f, 0.0f);
            ctrl.show();
            this.mSurface.copyFrom(ctrl);
        } catch (Surface.OutOfResourcesException e) {
        }
        this.mSurfaceControl = ctrl;
    }

    /* access modifiers changed from: package-private */
    public void positionSurface(int dw, int dh) {
        if (this.mLastDW != dw || this.mLastDH != dh) {
            this.mLastDW = dw;
            this.mLastDH = dh;
            this.mSurfaceControl.setBufferSize(dw, dh);
            this.mDrawNeeded = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void drawIfNeeded() {
        if (this.mDrawNeeded) {
            int dw = this.mLastDW;
            int dh = this.mLastDH;
            this.mDrawNeeded = false;
            Canvas c = null;
            try {
                c = this.mSurface.lockCanvas(new Rect(0, 0, dw, dh));
            } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
            }
            if (c != null) {
                c.drawColor(0, PorterDuff.Mode.CLEAR);
                int deltaX = this.mDeltaX;
                int deltaY = this.mDeltaY;
                int i = this.mTextWidth;
                int rem = (i + dw) - (((dw + i) / deltaX) * deltaX);
                int qdelta = deltaX / 4;
                if (rem < qdelta || rem > deltaX - qdelta) {
                    deltaX += deltaX / 3;
                }
                int y = -this.mTextHeight;
                int x = -this.mTextWidth;
                while (y < this.mTextHeight + dh) {
                    c.drawText(this.mText, (float) x, (float) y, this.mTextPaint);
                    x += deltaX;
                    if (x >= dw) {
                        x -= this.mTextWidth + dw;
                        y += deltaY;
                    }
                }
                this.mSurface.unlockCanvasAndPost(c);
            }
        }
    }
}
