package com.android.server.input;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

public class HwCirclePrompt {
    public static final float BG_ALPHA = 0.6f;
    private static final int[] BG_COLORS = new int[]{-16777216, 2130706432, 0};
    private static final float[] BG_COLOR_POS = new float[]{0.0f, 0.3f, 1.0f};
    private static int BG_HEIGHT = 375;
    private static final boolean DEBUG;
    public static final float DENSITY_FHD = 3.0f;
    public static final String TAG = "pressure:HwCirclePrompt";
    private static HwCirclePrompt mhwCirclePrompt = null;
    private int fontHeigh = 10;
    private int fontRightPadding = 50;
    private Rect mBgRect = new Rect();
    private Shader mBgShader = null;
    private Bitmap mBitmap;
    private Bitmap mBitmapH;
    private Context mContext;
    float mDpi = 1.0f;
    private int mHeight = 0;
    private boolean mIsPortrait = true;
    private Resources mResources;
    private int mWidth = 0;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    protected HwCirclePrompt(Context context, Resources resources) {
        this.mContext = context;
        this.mResources = resources;
        this.mDpi = resources.getDisplayMetrics().density / ((float) (resources.getDisplayMetrics().densityDpi / 160));
        convDpi();
    }

    private void convDpi() {
        BG_HEIGHT = (int) (((float) BG_HEIGHT) * this.mDpi);
        this.fontHeigh = (int) (((float) this.fontHeigh) * this.mDpi);
        this.fontHeigh = (int) (((float) this.fontHeigh) * this.mDpi);
    }

    public void draw(Canvas canvas) {
        Log.e(TAG, "draw");
        Paint paint = new Paint();
        TextPaint txPaint = new TextPaint();
        if (this.mIsPortrait) {
            drawBackground(canvas, paint);
            drawImage(canvas, paint);
            drawText(canvas, txPaint);
            return;
        }
        drawBackground(canvas, paint);
        drawImageH(canvas, paint);
        drawTextH(canvas, txPaint);
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        Shader s = paint.getShader();
        paint.setShader(this.mBgShader);
        paint.setStyle(Style.FILL);
        paint.setAlpha((int) 153.0f);
        canvas.drawRect(this.mBgRect, paint);
        paint.setShader(s);
    }

    private void drawText(Canvas canvas, TextPaint paint) {
        int bitmapheight = 0;
        if (!(this.mBitmap == null || (this.mBitmap.isRecycled() ^ 1) == 0)) {
            bitmapheight = this.mBitmap.getHeight();
        }
        canvas.save();
        paint.setTextSize((float) this.mResources.getDimensionPixelSize(34472107));
        FontMetrics fm = paint.getFontMetrics();
        float Fontheight = fm.bottom - fm.top;
        paint.setColor(this.mResources.getColor(33882275));
        paint.setTextAlign(Align.CENTER);
        String str = this.mContext.getString(33685838);
        if (str != null) {
            StaticLayout staticlayout = new StaticLayout(str, paint, this.mWidth - this.fontRightPadding, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int lines = getTextLines(paint, str, Fontheight, this.mWidth - (this.fontRightPadding * 2));
            canvas.translate((float) (this.mWidth / 2), ((float) (this.mHeight - bitmapheight)) - (((float) lines) * Fontheight));
            staticlayout.draw(canvas);
        }
        canvas.restore();
    }

    private int getTextLines(TextPaint paint, String content, float fontHeigh, int lineWidth) {
        int w = 0;
        int count = content.length();
        if (DEBUG) {
            Log.e(TAG, "getTextWidths count = " + count + "fontHeigh = " + fontHeigh + "  lineWidth  = " + lineWidth);
        }
        for (int i = 0; i < count; i++) {
            float[] widths = new float[1];
            paint.getTextWidths(String.valueOf(content.charAt(i)), widths);
            w += (int) Math.ceil((double) widths[0]);
            if (DEBUG) {
                Log.e(TAG, "getTextWidths w = " + w + " widths   = " + widths[0]);
            }
        }
        int textLines = (w / lineWidth) + 1;
        if (DEBUG) {
            Log.e(TAG, "getTextWidths textLines = " + textLines);
        }
        return textLines;
    }

    private void drawTextH(Canvas canvas, TextPaint paint) {
        int bitmapwidth = 0;
        if (!(this.mBitmapH == null || (this.mBitmapH.isRecycled() ^ 1) == 0)) {
            bitmapwidth = this.mBitmapH.getWidth();
        }
        paint.setTextSize((float) this.mResources.getDimensionPixelSize(34472107));
        FontMetrics fm = paint.getFontMetrics();
        float Fontheight = fm.bottom - fm.top;
        paint.setColor(this.mResources.getColor(33882275));
        paint.setTextAlign(Align.CENTER);
        String str = this.mContext.getString(33685838);
        StaticLayout staticlayout = new StaticLayout(str, paint, this.mHeight - this.fontRightPadding, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int lines = getTextLines(paint, str, Fontheight, this.mHeight - (this.fontRightPadding * 2));
        canvas.save();
        canvas.translate(((float) (this.mWidth - bitmapwidth)) - (((float) lines) * Fontheight), (float) (this.mHeight / 2));
        canvas.rotate(-90.0f, 0.0f, 0.0f);
        staticlayout.draw(canvas);
        canvas.rotate(90.0f, 0.0f, 0.0f);
        canvas.restore();
    }

    private void drawImage(Canvas canvas, Paint paint) {
        int bitmapheight = 0;
        int bitmapwidth = 0;
        if (this.mBitmap == null || (this.mBitmap.isRecycled() ^ 1) == 0) {
            loadBitmap();
            if (!(this.mBitmap == null || (this.mBitmap.isRecycled() ^ 1) == 0)) {
                bitmapwidth = this.mBitmap.getWidth();
                bitmapheight = this.mBitmap.getHeight();
            }
        } else {
            bitmapwidth = this.mBitmap.getWidth();
            bitmapheight = this.mBitmap.getHeight();
        }
        Log.e(TAG, "drawImage bitmapwidth = " + bitmapwidth + "  bitmapheight = " + bitmapheight);
        if (this.mIsPortrait) {
            Rect src = new Rect();
            Rect dst = new Rect();
            src.left = 0;
            src.top = 0;
            src.right = bitmapwidth;
            src.bottom = bitmapheight;
            dst.left = 0;
            dst.top = this.mHeight - ((this.mWidth / bitmapwidth) * bitmapheight);
            dst.right = this.mWidth;
            dst.bottom = this.mHeight;
            canvas.drawBitmap(this.mBitmap, src, dst, paint);
        }
    }

    private void drawImageH(Canvas canvas, Paint paint) {
        int bitmapwidth = 0;
        int bitmapheight = 0;
        if (this.mBitmapH == null || (this.mBitmapH.isRecycled() ^ 1) == 0) {
            loadBitmap();
            if (!(this.mBitmapH == null || (this.mBitmapH.isRecycled() ^ 1) == 0)) {
                bitmapwidth = this.mBitmapH.getWidth();
                bitmapheight = this.mBitmapH.getHeight();
            }
        } else {
            bitmapwidth = this.mBitmapH.getWidth();
            bitmapheight = this.mBitmapH.getHeight();
        }
        Log.e(TAG, "drawImage bitmapwidth = " + bitmapwidth + "  bitmapheight = " + bitmapheight);
        Rect src = new Rect();
        Rect dst = new Rect();
        src.left = 0;
        src.top = 0;
        src.right = bitmapwidth;
        src.bottom = bitmapheight;
        dst.left = this.mWidth - bitmapwidth;
        dst.top = 0;
        dst.right = this.mWidth;
        dst.bottom = this.mHeight;
        if (this.mBitmapH != null) {
            canvas.drawBitmap(this.mBitmapH, src, dst, paint);
        }
    }

    public void setViewSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (this.mHeight > this.mWidth) {
            this.mIsPortrait = true;
        } else {
            this.mIsPortrait = false;
        }
        if (this.mIsPortrait) {
            this.mBgShader = new LinearGradient(0.0f, (float) height, 0.0f, (float) (height - BG_HEIGHT), BG_COLORS, BG_COLOR_POS, TileMode.CLAMP);
            this.mBgRect.set(0, height - BG_HEIGHT, width, height);
            return;
        }
        this.mBgShader = new LinearGradient((float) width, (float) height, (float) (width - BG_HEIGHT), (float) height, BG_COLORS, BG_COLOR_POS, TileMode.CLAMP);
        this.mBgRect.set(width - BG_HEIGHT, 0, width, height);
    }

    private void loadBitmap() {
        if (DEBUG) {
            Log.e(TAG, "load bitmap begin");
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (this.mIsPortrait) {
            this.mBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 33751648, options);
        } else {
            this.mBitmapH = BitmapFactory.decodeResource(this.mContext.getResources(), 33751649, options);
        }
        if (DEBUG) {
            Log.e(TAG, "load bitmap end");
        }
    }

    public void release() {
        if (!(this.mBitmap == null || (this.mBitmap.isRecycled() ^ 1) == 0)) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        if (this.mBitmapH != null && (this.mBitmapH.isRecycled() ^ 1) != 0) {
            this.mBitmapH.recycle();
            this.mBitmapH = null;
        }
    }
}
