package com.android.server.policy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.TableMaskFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public final class IconUtilities {
    private static final String TAG = "IconUtilities";
    private static final int[] sColors = null;
    private final Paint mBlurPaint;
    private final Canvas mCanvas;
    private int mColorIndex;
    private final DisplayMetrics mDisplayMetrics;
    private final Paint mGlowColorFocusedPaint;
    private final Paint mGlowColorPressedPaint;
    private int mIconHeight;
    private int mIconTextureHeight;
    private int mIconTextureWidth;
    private int mIconWidth;
    private final Rect mOldBounds;
    private final Paint mPaint;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.IconUtilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.IconUtilities.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.IconUtilities.<clinit>():void");
    }

    public IconUtilities(Context context) {
        this.mIconWidth = -1;
        this.mIconHeight = -1;
        this.mIconTextureWidth = -1;
        this.mIconTextureHeight = -1;
        this.mPaint = new Paint();
        this.mBlurPaint = new Paint();
        this.mGlowColorPressedPaint = new Paint();
        this.mGlowColorFocusedPaint = new Paint();
        this.mOldBounds = new Rect();
        this.mCanvas = new Canvas();
        this.mColorIndex = 0;
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        this.mDisplayMetrics = metrics;
        float blurPx = 5.0f * metrics.density;
        int dimension = (int) resources.getDimension(17104896);
        this.mIconHeight = dimension;
        this.mIconWidth = dimension;
        dimension = this.mIconWidth + ((int) (2.0f * blurPx));
        this.mIconTextureHeight = dimension;
        this.mIconTextureWidth = dimension;
        this.mBlurPaint.setMaskFilter(new BlurMaskFilter(blurPx, Blur.NORMAL));
        TypedValue value = new TypedValue();
        this.mGlowColorPressedPaint.setColor(context.getTheme().resolveAttribute(16843661, value, true) ? value.data : -15616);
        this.mGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        this.mGlowColorFocusedPaint.setColor(context.getTheme().resolveAttribute(16843663, value, true) ? value.data : -29184);
        this.mGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        new ColorMatrix().setSaturation(0.2f);
        this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
    }

    public Drawable createIconDrawable(Drawable src) {
        Bitmap scaled = createIconBitmap(src);
        StateListDrawable result = new StateListDrawable();
        result.addState(new int[]{16842908}, new BitmapDrawable(createSelectedBitmap(scaled, false)));
        result.addState(new int[]{16842919}, new BitmapDrawable(createSelectedBitmap(scaled, true)));
        result.addState(new int[0], new BitmapDrawable(scaled));
        result.setBounds(0, 0, this.mIconTextureWidth, this.mIconTextureHeight);
        return result;
    }

    public Bitmap createIconBitmap(Drawable icon) {
        int width = this.mIconWidth;
        int height = this.mIconHeight;
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            if (bitmapDrawable.getBitmap().getDensity() == 0) {
                bitmapDrawable.setTargetDensity(this.mDisplayMetrics);
            }
        }
        int sourceWidth = icon.getIntrinsicWidth();
        int sourceHeight = icon.getIntrinsicHeight();
        if (sourceWidth > 0 && sourceHeight > 0) {
            if (width < sourceWidth || height < sourceHeight) {
                float ratio = ((float) sourceWidth) / ((float) sourceHeight);
                if (sourceWidth > sourceHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (((float) height) * ratio);
                }
            } else if (sourceWidth < width && sourceHeight < height) {
                width = sourceWidth;
                height = sourceHeight;
            }
        }
        int textureWidth = this.mIconTextureWidth;
        int textureHeight = this.mIconTextureHeight;
        Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Config.ARGB_8888);
        Canvas canvas = this.mCanvas;
        canvas.setBitmap(bitmap);
        int left = (textureWidth - width) / 2;
        int top = (textureHeight - height) / 2;
        this.mOldBounds.set(icon.getBounds());
        icon.setBounds(left, top, left + width, top + height);
        icon.draw(canvas);
        icon.setBounds(this.mOldBounds);
        return bitmap;
    }

    private Bitmap createSelectedBitmap(Bitmap src, boolean pressed) {
        Bitmap result = Bitmap.createBitmap(this.mIconTextureWidth, this.mIconTextureHeight, Config.ARGB_8888);
        Canvas dest = new Canvas(result);
        dest.drawColor(0, Mode.CLEAR);
        int[] xy = new int[2];
        Bitmap mask = src.extractAlpha(this.mBlurPaint, xy);
        dest.drawBitmap(mask, (float) xy[0], (float) xy[1], pressed ? this.mGlowColorPressedPaint : this.mGlowColorFocusedPaint);
        mask.recycle();
        dest.drawBitmap(src, 0.0f, 0.0f, this.mPaint);
        dest.setBitmap(null);
        return result;
    }
}
