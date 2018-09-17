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
    private static final int[] BG_COLORS = null;
    private static final float[] BG_COLOR_POS = null;
    private static int BG_HEIGHT = 0;
    private static final boolean DEBUG = false;
    public static final float DENSITY_FHD = 3.0f;
    public static final String TAG = "pressure:HwCirclePrompt";
    private static HwCirclePrompt mhwCirclePrompt;
    private int fontHeigh;
    private int fontRightPadding;
    private Rect mBgRect;
    private Shader mBgShader;
    private Bitmap mBitmap;
    private Bitmap mBitmapH;
    private Context mContext;
    float mDpi;
    private int mHeight;
    private boolean mIsPortrait;
    private Resources mResources;
    private int mWidth;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.input.HwCirclePrompt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.input.HwCirclePrompt.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.input.HwCirclePrompt.<clinit>():void");
    }

    protected HwCirclePrompt(Context context, Resources resources) {
        this.mBgShader = null;
        this.mBgRect = new Rect();
        this.mWidth = 0;
        this.mHeight = 0;
        this.fontHeigh = 10;
        this.fontRightPadding = 50;
        this.mIsPortrait = true;
        this.mDpi = HwCircleAnimation.SMALL_ALPHA;
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
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            bitmapheight = this.mBitmap.getHeight();
        }
        canvas.save();
        paint.setTextSize((float) this.mResources.getDimensionPixelSize(34472116));
        FontMetrics fm = paint.getFontMetrics();
        float Fontheight = fm.bottom - fm.top;
        paint.setColor(this.mResources.getColor(33882263));
        paint.setTextAlign(Align.CENTER);
        String str = this.mContext.getString(33685831);
        if (str != null) {
            StaticLayout staticlayout = new StaticLayout(str, paint, this.mWidth - this.fontRightPadding, Alignment.ALIGN_NORMAL, HwCircleAnimation.SMALL_ALPHA, 0.0f, DEBUG);
            Canvas canvas2 = canvas;
            canvas2.translate((float) (this.mWidth / 2), ((float) (this.mHeight - bitmapheight)) - (((float) getTextLines(paint, str, Fontheight, this.mWidth - (this.fontRightPadding * 2))) * Fontheight));
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
        if (!(this.mBitmapH == null || this.mBitmapH.isRecycled())) {
            bitmapwidth = this.mBitmapH.getWidth();
        }
        paint.setTextSize((float) this.mResources.getDimensionPixelSize(34472116));
        FontMetrics fm = paint.getFontMetrics();
        float Fontheight = fm.bottom - fm.top;
        paint.setColor(this.mResources.getColor(33882263));
        paint.setTextAlign(Align.CENTER);
        String str = this.mContext.getString(33685831);
        StaticLayout staticlayout = new StaticLayout(str, paint, this.mHeight - this.fontRightPadding, Alignment.ALIGN_NORMAL, HwCircleAnimation.SMALL_ALPHA, 0.0f, DEBUG);
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
        if (this.mBitmap == null || this.mBitmap.isRecycled()) {
            loadBitmap();
            if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
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
            if (!(this.mBitmapH == null || this.mBitmapH.isRecycled())) {
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
            this.mIsPortrait = DEBUG;
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
        options.inJustDecodeBounds = DEBUG;
        options.inSampleSize = 1;
        if (this.mIsPortrait) {
            this.mBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 33751427, options);
        } else {
            this.mBitmapH = BitmapFactory.decodeResource(this.mContext.getResources(), 33751428, options);
        }
        if (DEBUG) {
            Log.e(TAG, "load bitmap end");
        }
    }

    public void release() {
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        if (this.mBitmapH != null && !this.mBitmapH.isRecycled()) {
            this.mBitmapH.recycle();
            this.mBitmapH = null;
        }
    }
}
