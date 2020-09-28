package android.util;

import android.app.ActivityThread;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.LayerDrawable;
import com.android.internal.R;

public final class LauncherIcons {
    private static final int AMBIENT_SHADOW_ALPHA = 30;
    private static final float ICON_SIZE_BLUR_FACTOR = 0.010416667f;
    private static final float ICON_SIZE_KEY_SHADOW_DELTA_FACTOR = 0.020833334f;
    private static final int KEY_SHADOW_ALPHA = 61;
    private final int mIconSize;
    private final Resources mRes;
    private final SparseArray<Bitmap> mShadowCache = new SparseArray<>();

    public LauncherIcons(Context context) {
        this.mRes = context.getResources();
        this.mIconSize = this.mRes.getDimensionPixelSize(17104896);
    }

    public Drawable wrapIconDrawableWithShadow(Drawable drawable) {
        if (!(drawable instanceof AdaptiveIconDrawable)) {
            return drawable;
        }
        return new ShadowDrawable(getShadowBitmap((AdaptiveIconDrawable) drawable), drawable);
    }

    private Bitmap getShadowBitmap(AdaptiveIconDrawable d) {
        int shadowSize = Math.max(this.mIconSize, d.getIntrinsicHeight());
        synchronized (this.mShadowCache) {
            Bitmap shadow = this.mShadowCache.get(shadowSize);
            if (shadow != null) {
                return shadow;
            }
            d.setBounds(0, 0, shadowSize, shadowSize);
            float blur = ((float) shadowSize) * ICON_SIZE_BLUR_FACTOR;
            float keyShadowDistance = ((float) shadowSize) * ICON_SIZE_KEY_SHADOW_DELTA_FACTOR;
            int bitmapSize = (int) (((float) shadowSize) + (blur * 2.0f) + keyShadowDistance);
            Bitmap shadow2 = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(shadow2);
            canvas.translate((keyShadowDistance / 2.0f) + blur, blur);
            Paint paint = new Paint(1);
            paint.setColor(0);
            paint.setShadowLayer(blur, 0.0f, 0.0f, 503316480);
            canvas.drawPath(d.getIconMask(), paint);
            canvas.translate(0.0f, keyShadowDistance);
            paint.setShadowLayer(blur, 0.0f, 0.0f, 1023410176);
            canvas.drawPath(d.getIconMask(), paint);
            canvas.setBitmap(null);
            synchronized (this.mShadowCache) {
                this.mShadowCache.put(shadowSize, shadow2);
            }
            return shadow2;
        }
    }

    public Drawable getBadgeDrawable(int foregroundRes, int backgroundColor) {
        return getBadgedDrawable(null, foregroundRes, backgroundColor);
    }

    public Drawable getBadgedDrawable(Drawable base, int foregroundRes, int backgroundColor) {
        Drawable[] drawables;
        Resources overlayableRes = ActivityThread.currentActivityThread().getApplication().getResources();
        Drawable badgeShadow = overlayableRes.getDrawable(R.drawable.ic_corp_icon_badge_shadow);
        Drawable badgeColor = overlayableRes.getDrawable(R.drawable.ic_corp_icon_badge_color).getConstantState().newDrawable().mutate();
        Drawable badgeForeground = overlayableRes.getDrawable(foregroundRes);
        badgeForeground.setTint(backgroundColor);
        if (base == null) {
            drawables = new Drawable[]{badgeShadow, badgeColor, badgeForeground};
        } else {
            drawables = new Drawable[]{base, badgeShadow, badgeColor, badgeForeground};
        }
        return new LayerDrawable(drawables);
    }

    /* access modifiers changed from: private */
    public static class ShadowDrawable extends DrawableWrapper {
        final MyConstantState mState;

        public ShadowDrawable(Bitmap shadow, Drawable dr) {
            super(dr);
            this.mState = new MyConstantState(shadow, dr.getConstantState());
        }

        ShadowDrawable(MyConstantState state) {
            super(state.mChildState.newDrawable());
            this.mState = state;
        }

        @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
        public Drawable.ConstantState getConstantState() {
            return this.mState;
        }

        @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.drawBitmap(this.mState.mShadow, (Rect) null, bounds, this.mState.mPaint);
            canvas.save();
            canvas.translate(((float) bounds.width()) * 0.9599999f * LauncherIcons.ICON_SIZE_KEY_SHADOW_DELTA_FACTOR, ((float) bounds.height()) * 0.9599999f * LauncherIcons.ICON_SIZE_BLUR_FACTOR);
            canvas.scale(0.9599999f, 0.9599999f);
            super.draw(canvas);
            canvas.restore();
        }

        private static class MyConstantState extends Drawable.ConstantState {
            final Drawable.ConstantState mChildState;
            final Paint mPaint = new Paint(2);
            final Bitmap mShadow;

            MyConstantState(Bitmap shadow, Drawable.ConstantState childState) {
                this.mShadow = shadow;
                this.mChildState = childState;
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public Drawable newDrawable() {
                return new ShadowDrawable(this);
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public int getChangingConfigurations() {
                return this.mChildState.getChangingConfigurations();
            }
        }
    }
}
