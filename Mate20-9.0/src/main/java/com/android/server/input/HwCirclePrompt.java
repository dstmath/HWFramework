package com.android.server.input;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.mplink.HwMpLinkWifiImpl;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwCirclePrompt {
    public static final float BG_ALPHA = 0.6f;
    private static final int[] BG_COLORS = {-16777216, 2130706432, 0};
    private static final float[] BG_COLOR_POS = {GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.3f, 1.0f};
    private static int BG_HEIGHT = MemoryConstant.MSG_RCC_AVAIL_TARGET;
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
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

    protected HwCirclePrompt(Context context, Resources resources) {
        this.mContext = context;
        this.mResources = resources;
        this.mDpi = resources.getDisplayMetrics().density / ((float) (resources.getDisplayMetrics().densityDpi / HwMpLinkWifiImpl.BAND_WIDTH_160MHZ));
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
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha((int) (255.0f * 0.6f));
        canvas.drawRect(this.mBgRect, paint);
        paint.setShader(s);
    }

    private void drawText(Canvas canvas, TextPaint paint) {
        Canvas canvas2 = canvas;
        TextPaint textPaint = paint;
        int bitmapheight = 0;
        if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
            bitmapheight = this.mBitmap.getHeight();
        }
        int bitmapheight2 = bitmapheight;
        canvas.save();
        textPaint.setTextSize((float) this.mResources.getDimensionPixelSize(34472107));
        Paint.FontMetrics fm = paint.getFontMetrics();
        float Fontheight = fm.bottom - fm.top;
        textPaint.setColor(this.mResources.getColor(33882275));
        textPaint.setTextAlign(Paint.Align.CENTER);
        String str = this.mContext.getString(33685838);
        if (str != null) {
            StaticLayout staticlayout = new StaticLayout(str, textPaint, this.mWidth - this.fontRightPadding, Layout.Alignment.ALIGN_NORMAL, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, false);
            canvas2.translate((float) (this.mWidth / 2), ((float) (this.mHeight - bitmapheight2)) - (((float) getTextLines(textPaint, str, Fontheight, this.mWidth - (this.fontRightPadding * 2))) * Fontheight));
            staticlayout.draw(canvas2);
        }
        canvas.restore();
    }

    private int getTextLines(TextPaint paint, String content, float fontHeigh2, int lineWidth) {
        int count = content.length();
        if (DEBUG) {
            Log.e(TAG, "getTextWidths count = " + count + "fontHeigh = " + fontHeigh2 + "  lineWidth  = " + lineWidth);
        }
        int w = 0;
        for (int i = 0; i < count; i++) {
            float[] widths = new float[1];
            paint.getTextWidths(String.valueOf(content.charAt(i)), widths);
            w += (int) Math.ceil((double) widths[0]);
            if (DEBUG) {
                Log.e(TAG, "getTextWidths w = " + w + " widths   = " + widths[0]);
            }
        }
        int textLines = (w / lineWidth) + 1;
        if (DEBUG != 0) {
            Log.e(TAG, "getTextWidths textLines = " + textLines);
        }
        return textLines;
    }

    private void drawTextH(Canvas canvas, TextPaint paint) {
        Canvas canvas2 = canvas;
        TextPaint textPaint = paint;
        int bitmapwidth = 0;
        if (this.mBitmapH != null && !this.mBitmapH.isRecycled()) {
            bitmapwidth = this.mBitmapH.getWidth();
        }
        int bitmapwidth2 = bitmapwidth;
        textPaint.setTextSize((float) this.mResources.getDimensionPixelSize(34472107));
        Paint.FontMetrics fm = paint.getFontMetrics();
        float Fontheight = fm.bottom - fm.top;
        textPaint.setColor(this.mResources.getColor(33882275));
        textPaint.setTextAlign(Paint.Align.CENTER);
        String str = this.mContext.getString(33685838);
        StaticLayout staticlayout = new StaticLayout(str, textPaint, this.mHeight - this.fontRightPadding, Layout.Alignment.ALIGN_NORMAL, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, false);
        int lines = getTextLines(textPaint, str, Fontheight, this.mHeight - (this.fontRightPadding * 2));
        canvas.save();
        canvas2.translate(((float) (this.mWidth - bitmapwidth2)) - (((float) lines) * Fontheight), (float) (this.mHeight / 2));
        canvas2.rotate(-90.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        staticlayout.draw(canvas2);
        canvas2.rotate(90.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        canvas.restore();
    }

    private void drawImage(Canvas canvas, Paint paint) {
        int bitmapheight = 0;
        int bitmapwidth = 0;
        if (this.mBitmap == null || this.mBitmap.isRecycled()) {
            loadBitmap();
            if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
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
        if (this.mBitmapH == null || this.mBitmapH.isRecycled()) {
            loadBitmap();
            if (this.mBitmapH != null && !this.mBitmapH.isRecycled()) {
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
            LinearGradient linearGradient = new LinearGradient(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (float) height, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (float) (height - BG_HEIGHT), BG_COLORS, BG_COLOR_POS, Shader.TileMode.CLAMP);
            this.mBgShader = linearGradient;
            this.mBgRect.set(0, height - BG_HEIGHT, width, height);
            return;
        }
        LinearGradient linearGradient2 = new LinearGradient((float) width, (float) height, (float) (width - BG_HEIGHT), (float) height, BG_COLORS, BG_COLOR_POS, Shader.TileMode.CLAMP);
        this.mBgShader = linearGradient2;
        this.mBgRect.set(width - BG_HEIGHT, 0, width, height);
    }

    private void loadBitmap() {
        if (DEBUG) {
            Log.e(TAG, "load bitmap begin");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (this.mIsPortrait) {
            this.mBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 33751648, options);
        } else {
            this.mBitmapH = BitmapFactory.decodeResource(this.mContext.getResources(), 33751649, options);
        }
        if (DEBUG != 0) {
            Log.e(TAG, "load bitmap end");
        }
    }

    public void release() {
        if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        if (this.mBitmapH != null && !this.mBitmapH.isRecycled()) {
            this.mBitmapH.recycle();
            this.mBitmapH = null;
        }
    }
}
