package com.android.internal.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Pools;
import com.android.internal.R;
import java.nio.ByteBuffer;
import org.xmlpull.v1.XmlPullParser;

@Deprecated
public class SimpleIconFactory {
    private static final int AMBIENT_SHADOW_ALPHA = 30;
    private static final float BLUR_FACTOR = 0.010416667f;
    private static final float CIRCLE_AREA_BY_RECT = 0.7853982f;
    private static final int DEFAULT_WRAPPER_BACKGROUND = -1;
    private static final int KEY_SHADOW_ALPHA = 61;
    private static final float KEY_SHADOW_DISTANCE = 0.020833334f;
    private static final float LINEAR_SCALE_SLOPE = 0.040449437f;
    private static final float MAX_CIRCLE_AREA_FACTOR = 0.6597222f;
    private static final float MAX_SQUARE_AREA_FACTOR = 0.6510417f;
    private static final int MIN_VISIBLE_ALPHA = 40;
    private static final float SCALE_NOT_INITIALIZED = 0.0f;
    private static final Pools.SynchronizedPool<SimpleIconFactory> sPool = new Pools.SynchronizedPool<>(Runtime.getRuntime().availableProcessors());
    private final Rect mAdaptiveIconBounds;
    private float mAdaptiveIconScale;
    private int mBadgeBitmapSize;
    private final Bitmap mBitmap;
    private Paint mBlurPaint = new Paint(3);
    private final Rect mBounds;
    private Canvas mCanvas;
    private Context mContext;
    private BlurMaskFilter mDefaultBlurMaskFilter;
    private Paint mDrawPaint = new Paint(3);
    private int mFillResIconDpi;
    private int mIconBitmapSize;
    private final float[] mLeftBorder;
    private final int mMaxSize;
    private final Rect mOldBounds = new Rect();
    private final byte[] mPixels;
    private PackageManager mPm;
    private final float[] mRightBorder;
    private final Canvas mScaleCheckCanvas;
    private int mWrapperBackgroundColor;
    private Drawable mWrapperIcon;

    @Deprecated
    public static SimpleIconFactory obtain(Context ctx) {
        SimpleIconFactory instance = sPool.acquire();
        if (instance != null) {
            return instance;
        }
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        int iconDpi = am == null ? 0 : am.getLauncherLargeIconDensity();
        Resources r = ctx.getResources();
        SimpleIconFactory instance2 = new SimpleIconFactory(ctx, iconDpi, r.getDimensionPixelSize(R.dimen.resolver_icon_size), r.getDimensionPixelSize(R.dimen.resolver_badge_size));
        instance2.setWrapperBackgroundColor(-1);
        return instance2;
    }

    @Deprecated
    public void recycle() {
        setWrapperBackgroundColor(-1);
        sPool.release(this);
    }

    @Deprecated
    private SimpleIconFactory(Context context, int fillResIconDpi, int iconBitmapSize, int badgeBitmapSize) {
        this.mContext = context.getApplicationContext();
        this.mPm = this.mContext.getPackageManager();
        this.mIconBitmapSize = iconBitmapSize;
        this.mBadgeBitmapSize = badgeBitmapSize;
        this.mFillResIconDpi = fillResIconDpi;
        this.mCanvas = new Canvas();
        this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        this.mMaxSize = iconBitmapSize * 2;
        int i = this.mMaxSize;
        this.mBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ALPHA_8);
        this.mScaleCheckCanvas = new Canvas(this.mBitmap);
        int i2 = this.mMaxSize;
        this.mPixels = new byte[(i2 * i2)];
        this.mLeftBorder = new float[i2];
        this.mRightBorder = new float[i2];
        this.mBounds = new Rect();
        this.mAdaptiveIconBounds = new Rect();
        this.mAdaptiveIconScale = 0.0f;
        this.mDefaultBlurMaskFilter = new BlurMaskFilter(((float) iconBitmapSize) * BLUR_FACTOR, BlurMaskFilter.Blur.NORMAL);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void setWrapperBackgroundColor(int color) {
        this.mWrapperBackgroundColor = Color.alpha(color) < 255 ? -1 : color;
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public Bitmap createUserBadgedIconBitmap(Drawable icon, UserHandle user) {
        float[] scale = new float[1];
        if (icon == null) {
            icon = getFullResDefaultActivityIcon(this.mFillResIconDpi);
        }
        Drawable icon2 = normalizeAndWrapToAdaptiveIcon(icon, null, scale);
        Bitmap bitmap = createIconBitmap(icon2, scale[0]);
        if (icon2 instanceof AdaptiveIconDrawable) {
            this.mCanvas.setBitmap(bitmap);
            recreateIcon(Bitmap.createBitmap(bitmap), this.mCanvas);
            this.mCanvas.setBitmap(null);
        }
        if (user == null) {
            return bitmap;
        }
        Drawable badged = this.mPm.getUserBadgedIcon(new FixedSizeBitmapDrawable(bitmap), user);
        if (badged instanceof BitmapDrawable) {
            return ((BitmapDrawable) badged).getBitmap();
        }
        return createIconBitmap(badged, 1.0f);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public Bitmap createAppBadgedIconBitmap(Drawable icon, Bitmap renderedAppIcon) {
        if (icon == null) {
            icon = getFullResDefaultActivityIcon(this.mFillResIconDpi);
        }
        int w = icon.getIntrinsicWidth();
        int h = icon.getIntrinsicHeight();
        float scale = 1.0f;
        if (h > w && w > 0) {
            scale = ((float) h) / ((float) w);
        } else if (w > h && h > 0) {
            scale = ((float) w) / ((float) h);
        }
        Drawable icon2 = new BitmapDrawable(this.mContext.getResources(), maskBitmapToCircle(createIconBitmap(icon, scale)));
        Bitmap bitmap = createIconBitmap(icon2, getScale(icon2, null));
        this.mCanvas.setBitmap(bitmap);
        recreateIcon(Bitmap.createBitmap(bitmap), this.mCanvas);
        if (renderedAppIcon != null) {
            int i = this.mBadgeBitmapSize;
            Bitmap renderedAppIcon2 = Bitmap.createScaledBitmap(renderedAppIcon, i, i, false);
            Canvas canvas = this.mCanvas;
            int i2 = this.mIconBitmapSize;
            int i3 = this.mBadgeBitmapSize;
            canvas.drawBitmap(renderedAppIcon2, (float) (i2 - i3), (float) (i2 - i3), (Paint) null);
        }
        this.mCanvas.setBitmap(null);
        return bitmap;
    }

    private Bitmap maskBitmapToCircle(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(-1);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f, (((float) bitmap.getWidth()) / 2.0f) - 1.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private static Drawable getFullResDefaultActivityIcon(int iconDpi) {
        return Resources.getSystem().getDrawableForDensity(17629184, iconDpi);
    }

    private Bitmap createIconBitmap(Drawable icon, float scale) {
        return createIconBitmap(icon, scale, this.mIconBitmapSize);
    }

    /* JADX INFO: Multiple debug info for r1v6 int: [D('bitmapDrawable' android.graphics.drawable.BitmapDrawable), D('width' int)] */
    private Bitmap createIconBitmap(Drawable icon, float scale, int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(bitmap);
        this.mOldBounds.set(icon.getBounds());
        if (icon instanceof AdaptiveIconDrawable) {
            int offset = Math.max((int) Math.ceil((double) (((float) size) * BLUR_FACTOR)), Math.round((((float) size) * (1.0f - scale)) / 2.0f));
            icon.setBounds(offset, offset, size - offset, size - offset);
            icon.draw(this.mCanvas);
        } else {
            if (icon instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap b = bitmapDrawable.getBitmap();
                if (bitmap != null && b.getDensity() == 0) {
                    bitmapDrawable.setTargetDensity(this.mContext.getResources().getDisplayMetrics());
                }
            }
            int width = size;
            int height = size;
            int intrinsicWidth = icon.getIntrinsicWidth();
            int intrinsicHeight = icon.getIntrinsicHeight();
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                float ratio = ((float) intrinsicWidth) / ((float) intrinsicHeight);
                if (intrinsicWidth > intrinsicHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (intrinsicHeight > intrinsicWidth) {
                    width = (int) (((float) height) * ratio);
                }
            }
            int left = (size - width) / 2;
            int top = (size - height) / 2;
            icon.setBounds(left, top, left + width, top + height);
            this.mCanvas.save();
            this.mCanvas.scale(scale, scale, (float) (size / 2), (float) (size / 2));
            icon.draw(this.mCanvas);
            this.mCanvas.restore();
        }
        icon.setBounds(this.mOldBounds);
        this.mCanvas.setBitmap(null);
        return bitmap;
    }

    private Drawable normalizeAndWrapToAdaptiveIcon(Drawable icon, RectF outIconBounds, float[] outScale) {
        if (this.mWrapperIcon == null) {
            this.mWrapperIcon = this.mContext.getDrawable(R.drawable.iconfactory_adaptive_icon_drawable_wrapper).mutate();
        }
        AdaptiveIconDrawable dr = (AdaptiveIconDrawable) this.mWrapperIcon;
        dr.setBounds(0, 0, 1, 1);
        float scale = getScale(icon, outIconBounds);
        if (!(icon instanceof AdaptiveIconDrawable)) {
            FixedScaleDrawable fsd = (FixedScaleDrawable) dr.getForeground();
            fsd.setDrawable(icon);
            fsd.setScale(scale);
            icon = dr;
            scale = getScale(icon, outIconBounds);
            ((ColorDrawable) dr.getBackground()).setColor(this.mWrapperBackgroundColor);
        }
        outScale[0] = scale;
        return icon;
    }

    private synchronized float getScale(Drawable d, RectF outBounds) {
        float scaleRequired;
        if (!(d instanceof AdaptiveIconDrawable) || this.mAdaptiveIconScale == 0.0f) {
            int width = d.getIntrinsicWidth();
            int height = d.getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                width = (width <= 0 || width > this.mMaxSize) ? this.mMaxSize : width;
                height = (height <= 0 || height > this.mMaxSize) ? this.mMaxSize : height;
            } else if (width > this.mMaxSize || height > this.mMaxSize) {
                int max = Math.max(width, height);
                width = (this.mMaxSize * width) / max;
                height = (this.mMaxSize * height) / max;
            }
            this.mBitmap.eraseColor(0);
            d.setBounds(0, 0, width, height);
            d.draw(this.mScaleCheckCanvas);
            ByteBuffer buffer = ByteBuffer.wrap(this.mPixels);
            buffer.rewind();
            this.mBitmap.copyPixelsToBuffer(buffer);
            int topY = -1;
            int bottomY = -1;
            int leftX = this.mMaxSize + 1;
            int rightX = -1;
            int x = 0;
            int rowSizeDiff = this.mMaxSize - width;
            int y = 0;
            while (y < height) {
                int lastX = -1;
                int firstX = -1;
                int index = x;
                int x2 = 0;
                while (x2 < width) {
                    if ((this.mPixels[index] & 255) > 40) {
                        if (firstX == -1) {
                            firstX = x2;
                        }
                        lastX = x2;
                    }
                    index++;
                    x2++;
                    buffer = buffer;
                }
                x = index + rowSizeDiff;
                this.mLeftBorder[y] = (float) firstX;
                this.mRightBorder[y] = (float) lastX;
                if (firstX != -1) {
                    bottomY = y;
                    if (topY == -1) {
                        topY = y;
                    }
                    int leftX2 = Math.min(leftX, firstX);
                    rightX = Math.max(rightX, lastX);
                    leftX = leftX2;
                }
                y++;
                buffer = buffer;
            }
            if (topY != -1) {
                if (rightX != -1) {
                    convertToConvexArray(this.mLeftBorder, 1, topY, bottomY);
                    convertToConvexArray(this.mRightBorder, -1, topY, bottomY);
                    float area = 0.0f;
                    for (int y2 = 0; y2 < height; y2++) {
                        if (this.mLeftBorder[y2] > -1.0f) {
                            area += (this.mRightBorder[y2] - this.mLeftBorder[y2]) + 1.0f;
                        }
                    }
                    float hullByRect = area / ((float) (((bottomY + 1) - topY) * ((rightX + 1) - leftX)));
                    if (hullByRect < CIRCLE_AREA_BY_RECT) {
                        scaleRequired = MAX_CIRCLE_AREA_FACTOR;
                    } else {
                        scaleRequired = ((1.0f - hullByRect) * LINEAR_SCALE_SLOPE) + MAX_SQUARE_AREA_FACTOR;
                    }
                    this.mBounds.left = leftX;
                    this.mBounds.right = rightX;
                    this.mBounds.top = topY;
                    this.mBounds.bottom = bottomY;
                    if (outBounds != null) {
                        outBounds.set(((float) this.mBounds.left) / ((float) width), ((float) this.mBounds.top) / ((float) height), 1.0f - (((float) this.mBounds.right) / ((float) width)), 1.0f - (((float) this.mBounds.bottom) / ((float) height)));
                    }
                    float areaScale = area / ((float) (width * height));
                    float scale = areaScale > scaleRequired ? (float) Math.sqrt((double) (scaleRequired / areaScale)) : 1.0f;
                    if ((d instanceof AdaptiveIconDrawable) && this.mAdaptiveIconScale == 0.0f) {
                        this.mAdaptiveIconScale = scale;
                        this.mAdaptiveIconBounds.set(this.mBounds);
                    }
                    return scale;
                }
            }
            return 1.0f;
        }
        if (outBounds != null) {
            outBounds.set(this.mAdaptiveIconBounds);
        }
        return this.mAdaptiveIconScale;
    }

    private static void convertToConvexArray(float[] xCoordinates, int direction, int topY, int bottomY) {
        int start;
        float[] angles = new float[(xCoordinates.length - 1)];
        int last = -1;
        float lastAngle = Float.MAX_VALUE;
        for (int i = topY + 1; i <= bottomY; i++) {
            if (xCoordinates[i] > -1.0f) {
                if (lastAngle == Float.MAX_VALUE) {
                    start = topY;
                } else if ((((xCoordinates[i] - xCoordinates[last]) / ((float) (i - last))) - lastAngle) * ((float) direction) < 0.0f) {
                    start = last;
                    while (start > topY) {
                        start--;
                        if ((((xCoordinates[i] - xCoordinates[start]) / ((float) (i - start))) - angles[start]) * ((float) direction) >= 0.0f) {
                            break;
                        }
                    }
                } else {
                    start = last;
                }
                float lastAngle2 = (xCoordinates[i] - xCoordinates[start]) / ((float) (i - start));
                for (int j = start; j < i; j++) {
                    angles[j] = lastAngle2;
                    xCoordinates[j] = xCoordinates[start] + (((float) (j - start)) * lastAngle2);
                }
                last = i;
                lastAngle = lastAngle2;
            }
        }
    }

    private synchronized void recreateIcon(Bitmap icon, Canvas out) {
        recreateIcon(icon, this.mDefaultBlurMaskFilter, 30, 61, out);
    }

    private synchronized void recreateIcon(Bitmap icon, BlurMaskFilter blurMaskFilter, int ambientAlpha, int keyAlpha, Canvas out) {
        int[] offset = new int[2];
        this.mBlurPaint.setMaskFilter(blurMaskFilter);
        Bitmap shadow = icon.extractAlpha(this.mBlurPaint, offset);
        this.mDrawPaint.setAlpha(ambientAlpha);
        out.drawBitmap(shadow, (float) offset[0], (float) offset[1], this.mDrawPaint);
        this.mDrawPaint.setAlpha(keyAlpha);
        out.drawBitmap(shadow, (float) offset[0], ((float) offset[1]) + (((float) this.mIconBitmapSize) * KEY_SHADOW_DISTANCE), this.mDrawPaint);
        this.mDrawPaint.setAlpha(255);
        out.drawBitmap(icon, 0.0f, 0.0f, this.mDrawPaint);
    }

    public static class FixedScaleDrawable extends DrawableWrapper {
        private static final float LEGACY_ICON_SCALE = 0.46669f;
        private float mScaleX = LEGACY_ICON_SCALE;
        private float mScaleY = LEGACY_ICON_SCALE;

        public FixedScaleDrawable() {
            super(new ColorDrawable());
        }

        @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int saveCount = canvas.save();
            canvas.scale(this.mScaleX, this.mScaleY, getBounds().exactCenterX(), getBounds().exactCenterY());
            super.draw(canvas);
            canvas.restoreToCount(saveCount);
        }

        @Override // android.graphics.drawable.Drawable
        public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) {
        }

        @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
        public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) {
        }

        public void setScale(float scale) {
            float h = (float) getIntrinsicHeight();
            float w = (float) getIntrinsicWidth();
            this.mScaleX = scale * LEGACY_ICON_SCALE;
            this.mScaleY = LEGACY_ICON_SCALE * scale;
            if (h > w && w > 0.0f) {
                this.mScaleX *= w / h;
            } else if (w > h && h > 0.0f) {
                this.mScaleY *= h / w;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class FixedSizeBitmapDrawable extends BitmapDrawable {
        FixedSizeBitmapDrawable(Bitmap bitmap) {
            super((Resources) null, bitmap);
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override // android.graphics.drawable.BitmapDrawable, android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }
}
