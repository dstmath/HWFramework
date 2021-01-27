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

public class HwCirclePrompt {
    private static final int ALPHA_RATIO = 255;
    public static final float BG_ALPHA = 0.6f;
    private static final int[] BG_COLORS = {-16777216, 2130706432, 0};
    private static final float[] BG_COLOR_POS = {0.0f, 0.3f, 1.0f};
    private static final int DEFAULT_BG_HEIGJHT = 375;
    private static final int DEFAULT_FONT_HEIGHT = 10;
    private static final int DEFAULT_FONT_RIGHT_PADDING = 50;
    public static final float DENSITY_FHD = 3.0f;
    private static final boolean IS_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int PADDING_DOUBLE = 2;
    private static final float ROTATE_DEGREE_NEG = -90.0f;
    private static final float ROTATE_DEGREE_POS = 90.0f;
    public static final String TAG = "pressure:HwCirclePrompt";
    private static int sBgHeight = DEFAULT_BG_HEIGJHT;
    private Rect mBgRect = new Rect();
    private Shader mBgShader = null;
    private Bitmap mBitmap;
    private Bitmap mBitmapH;
    private Context mContext;
    float mDpi = 1.0f;
    private int mFontHeigh = 10;
    private int mFontRightPadding = 50;
    private int mHeight = 0;
    private boolean mIsPortrait = true;
    private Resources mResources;
    private int mWidth = 0;

    protected HwCirclePrompt(Context context, Resources resources) {
        this.mContext = context;
        this.mResources = resources;
        this.mDpi = resources.getDisplayMetrics().density / ((float) (resources.getDisplayMetrics().densityDpi / 160));
        convDpi();
    }

    private void convDpi() {
        float f = this.mDpi;
        sBgHeight = (int) (((float) sBgHeight) * f);
        this.mFontHeigh = (int) (((float) this.mFontHeigh) * f);
        this.mFontHeigh = (int) (((float) this.mFontHeigh) * f);
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
        paint.setShader(this.mBgShader);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha((int) (255.0f * 0.6f));
        canvas.drawRect(this.mBgRect, paint);
        paint.setShader(paint.getShader());
    }

    private void drawText(Canvas canvas, TextPaint paint) {
        int bitmapheight;
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null || bitmap.isRecycled()) {
            bitmapheight = 0;
        } else {
            bitmapheight = this.mBitmap.getHeight();
        }
        canvas.save();
        paint.setTextSize((float) this.mResources.getDimensionPixelSize(34472107));
        Paint.FontMetrics fm = paint.getFontMetrics();
        float fontHeight = fm.bottom - fm.top;
        paint.setColor(this.mResources.getColor(33882275));
        paint.setTextAlign(Paint.Align.CENTER);
        String str = this.mContext.getString(33685838);
        if (str != null) {
            StaticLayout staticlayout = new StaticLayout(str, paint, this.mWidth - this.mFontRightPadding, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            canvas.translate((float) (this.mWidth / 2), ((float) (this.mHeight - bitmapheight)) - (((float) getTextLines(paint, str, fontHeight, this.mWidth - (this.mFontRightPadding * 2))) * fontHeight));
            staticlayout.draw(canvas);
        }
        canvas.restore();
    }

    private int getTextLines(TextPaint paint, String content, float fontHeigh, int lineWidth) {
        int width = 0;
        int count = content.length();
        if (IS_DEBUG) {
            Log.e(TAG, "getTextWidths count = " + count + "fontHeigh = " + fontHeigh + " lineWidth = " + lineWidth);
        }
        for (int i = 0; i < count; i++) {
            float[] widths = new float[1];
            paint.getTextWidths(String.valueOf(content.charAt(i)), widths);
            width += (int) Math.ceil((double) widths[0]);
            if (IS_DEBUG) {
                Log.e(TAG, "getTextWidths width = " + width + " widths = " + widths[0]);
            }
        }
        int textLines = (width / lineWidth) + 1;
        if (IS_DEBUG) {
            Log.e(TAG, "getTextWidths textLines = " + textLines);
        }
        return textLines;
    }

    private void drawTextH(Canvas canvas, TextPaint paint) {
        int bitmapwidth;
        Bitmap bitmap = this.mBitmapH;
        if (bitmap == null || bitmap.isRecycled()) {
            bitmapwidth = 0;
        } else {
            bitmapwidth = this.mBitmapH.getWidth();
        }
        paint.setTextSize((float) this.mResources.getDimensionPixelSize(34472107));
        paint.setColor(this.mResources.getColor(33882275));
        paint.setTextAlign(Paint.Align.CENTER);
        String str = this.mContext.getString(33685838);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float fontHeight = fm.bottom - fm.top;
        StaticLayout staticlayout = new StaticLayout(str, paint, this.mHeight - this.mFontRightPadding, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int lines = getTextLines(paint, str, fontHeight, this.mHeight - (this.mFontRightPadding * 2));
        canvas.save();
        canvas.translate(((float) (this.mWidth - bitmapwidth)) - (((float) lines) * fontHeight), (float) (this.mHeight / 2));
        canvas.rotate(ROTATE_DEGREE_NEG, 0.0f, 0.0f);
        staticlayout.draw(canvas);
        canvas.rotate(ROTATE_DEGREE_POS, 0.0f, 0.0f);
        canvas.restore();
    }

    private void drawImage(Canvas canvas, Paint paint) {
        int bitmapheight = 0;
        int bitmapwidth = 0;
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null || bitmap.isRecycled()) {
            loadBitmap();
            Bitmap bitmap2 = this.mBitmap;
            if (bitmap2 != null && !bitmap2.isRecycled()) {
                bitmapwidth = this.mBitmap.getWidth();
                bitmapheight = this.mBitmap.getHeight();
            }
        } else {
            bitmapwidth = this.mBitmap.getWidth();
            bitmapheight = this.mBitmap.getHeight();
        }
        Log.e(TAG, "drawImage bitmapwidth = " + bitmapwidth + " bitmapheight = " + bitmapheight);
        if (this.mIsPortrait) {
            Rect src = new Rect();
            src.left = 0;
            src.top = 0;
            src.right = bitmapwidth;
            src.bottom = bitmapheight;
            Rect dst = new Rect();
            dst.left = 0;
            int i = this.mHeight;
            int i2 = this.mWidth;
            dst.top = i - ((i2 / bitmapwidth) * bitmapheight);
            dst.right = i2;
            dst.bottom = i;
            canvas.drawBitmap(this.mBitmap, src, dst, paint);
        }
    }

    private void drawImageH(Canvas canvas, Paint paint) {
        int bitmapwidth = 0;
        int bitmapheight = 0;
        Bitmap bitmap = this.mBitmapH;
        if (bitmap == null || bitmap.isRecycled()) {
            loadBitmap();
            Bitmap bitmap2 = this.mBitmapH;
            if (bitmap2 != null && !bitmap2.isRecycled()) {
                bitmapwidth = this.mBitmapH.getWidth();
                bitmapheight = this.mBitmapH.getHeight();
            }
        } else {
            bitmapwidth = this.mBitmapH.getWidth();
            bitmapheight = this.mBitmapH.getHeight();
        }
        Log.e(TAG, "drawImage bitmapwidth = " + bitmapwidth + " bitmapheight = " + bitmapheight);
        Rect src = new Rect();
        src.left = 0;
        src.top = 0;
        src.right = bitmapwidth;
        src.bottom = bitmapheight;
        Rect dst = new Rect();
        int i = this.mWidth;
        dst.left = i - bitmapwidth;
        dst.top = 0;
        dst.right = i;
        dst.bottom = this.mHeight;
        Bitmap bitmap3 = this.mBitmapH;
        if (bitmap3 != null) {
            canvas.drawBitmap(bitmap3, src, dst, paint);
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
            this.mBgShader = new LinearGradient(0.0f, (float) height, 0.0f, (float) (height - sBgHeight), BG_COLORS, BG_COLOR_POS, Shader.TileMode.CLAMP);
            this.mBgRect.set(0, height - sBgHeight, width, height);
            return;
        }
        this.mBgShader = new LinearGradient((float) width, (float) height, (float) (width - sBgHeight), (float) height, BG_COLORS, BG_COLOR_POS, Shader.TileMode.CLAMP);
        this.mBgRect.set(width - sBgHeight, 0, width, height);
    }

    private void loadBitmap() {
        if (IS_DEBUG) {
            Log.e(TAG, "load bitmap begin");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (this.mIsPortrait) {
            this.mBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 33751648, options);
        } else {
            this.mBitmapH = BitmapFactory.decodeResource(this.mContext.getResources(), 33751649, options);
        }
        if (IS_DEBUG) {
            Log.e(TAG, "load bitmap end");
        }
    }

    public void release() {
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        Bitmap bitmap2 = this.mBitmapH;
        if (bitmap2 != null && !bitmap2.isRecycled()) {
            this.mBitmapH.recycle();
            this.mBitmapH = null;
        }
    }
}
