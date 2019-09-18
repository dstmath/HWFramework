package huawei.android.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class ComplexDrawable extends Drawable {
    private static final float MAX_SCALE = 1.42f;
    private int mAnimationDuration;
    private ValueAnimator.AnimatorUpdateListener mAnimatorListener;
    private int mCenterColor;
    private ValueAnimator mCheckAnim;
    private Path mClipPath;
    private Context mContext;
    private Rect mDrawableRect;
    private Bitmap mDstBitmap;
    private Drawable mDstDrawable;
    private int mIconActiveColor;
    private int mIconBounds;
    private int mIconDefaultColor;
    private boolean mIsGradientEnable;
    private int mRadius = 0;
    private Drawable mSrcDrawable;
    private int mStartColor;
    private ValueAnimator mUnCheckAnim;

    public ComplexDrawable(Context context, Drawable srcDrawable) {
        this.mContext = context;
        this.mAnimationDuration = ResLoaderUtil.getResources(context).getInteger(ResLoader.getInstance().getIdentifier(context, "integer", "bottomnav_icon_anim_duration"));
        this.mIconBounds = ResLoaderUtil.getDimensionPixelSize(context, "bottomnav_item_icon_size");
        this.mDrawableRect = new Rect(0, 0, this.mIconBounds, this.mIconBounds);
        setSrcDrawable(srcDrawable);
        this.mAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ComplexDrawable.this.setRadius(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        };
        this.mClipPath = new Path();
        initAnim();
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(new int[]{33620134, 33620131, 33620132});
            this.mIsGradientEnable = a.getBoolean(0, false);
            if (this.mIsGradientEnable) {
                this.mStartColor = a.getColor(1, this.mStartColor);
                this.mCenterColor = a.getColor(2, this.mCenterColor);
            }
            a.recycle();
        }
    }

    private Bitmap convertToBitmap(Drawable drawable, int startColor, int centerColor) {
        Bitmap bitmap = Bitmap.createBitmap(this.mIconBounds, this.mIconBounds, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        LinearGradient shader = new LinearGradient((float) this.mDrawableRect.centerX(), (float) this.mDrawableRect.bottom, (float) this.mDrawableRect.centerX(), 0.0f, startColor, centerColor, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(this.mDrawableRect, paint);
        return bitmap;
    }

    private void initAnim() {
        Interpolator INTERPOLATOR_40_80 = AnimationUtils.loadInterpolator(this.mContext, this.mContext.getResources().getIdentifier("fast_out_slow_in", "interpolator", "android"));
        Interpolator INTERPOLATOR_20_80 = AnimationUtils.loadInterpolator(this.mContext, ResLoader.getInstance().getIdentifier(this.mContext, "interpolator", "cubic_bezier_interpolator_type_20_80"));
        this.mCheckAnim = ValueAnimator.ofInt(new int[]{0, (int) (((float) this.mIconBounds) * MAX_SCALE)});
        this.mCheckAnim.setDuration((long) this.mAnimationDuration);
        this.mCheckAnim.addUpdateListener(this.mAnimatorListener);
        this.mCheckAnim.setInterpolator(INTERPOLATOR_20_80);
        this.mUnCheckAnim = ValueAnimator.ofInt(new int[]{(int) (((float) this.mIconBounds) * MAX_SCALE), 0});
        this.mUnCheckAnim.setDuration((long) this.mAnimationDuration);
        this.mUnCheckAnim.addUpdateListener(this.mAnimatorListener);
        this.mUnCheckAnim.setInterpolator(INTERPOLATOR_40_80);
    }

    public void draw(Canvas canvas) {
        this.mClipPath.reset();
        this.mClipPath.addCircle((float) (getLayoutDirection() == 1 ? this.mIconBounds : this.mDrawableRect.left), (float) this.mDrawableRect.bottom, (float) this.mRadius, Path.Direction.CCW);
        canvas.save();
        canvas.clipOutPath(this.mClipPath);
        this.mSrcDrawable.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.clipPath(this.mClipPath);
        if (this.mIsGradientEnable) {
            if (this.mDstBitmap == null) {
                this.mDstBitmap = convertToBitmap(this.mDstDrawable, this.mStartColor, this.mCenterColor);
            }
            canvas.drawBitmap(this.mDstBitmap, 0.0f, 0.0f, null);
        } else {
            this.mDstDrawable.draw(canvas);
        }
        canvas.restore();
    }

    public void setAlpha(int alpha) {
        if (this.mSrcDrawable != null) {
            this.mSrcDrawable.setAlpha(alpha);
        }
    }

    public void setColorFilter(ColorFilter colorFilter) {
        if (this.mSrcDrawable != null) {
            this.mSrcDrawable.setColorFilter(colorFilter);
        }
    }

    public int getOpacity() {
        if (this.mSrcDrawable != null) {
            return this.mSrcDrawable.getOpacity();
        }
        return -3;
    }

    /* access modifiers changed from: package-private */
    public void setSrcDrawable(int drawableRes) {
        setSrcDrawable(this.mContext.getResources().getDrawable(drawableRes));
    }

    /* access modifiers changed from: package-private */
    public void setSrcDrawable(Drawable drawable) {
        Drawable drawable2;
        Drawable drawable3;
        if (drawable instanceof StateListDrawable) {
            StateListDrawable sld = (StateListDrawable) drawable;
            int attr = this.mContext.getResources().getIdentifier("state_selected", "attr", "android");
            int[] empty_set = new int[0];
            int[] selected_set = {attr};
            int[] unselected_set = {~attr};
            Drawable srcDrawable = null;
            Drawable dstDrawable = null;
            int stateDrawableIndex = sld.getStateDrawableIndex(unselected_set);
            int index = stateDrawableIndex;
            if (stateDrawableIndex != -1) {
                srcDrawable = sld.getStateDrawable(index);
            }
            int stateDrawableIndex2 = sld.getStateDrawableIndex(selected_set);
            int index2 = stateDrawableIndex2;
            if (stateDrawableIndex2 != -1) {
                dstDrawable = sld.getStateDrawable(index2);
            }
            if (srcDrawable == null && dstDrawable == null) {
                setSrcAndDst(drawable, drawable.getConstantState().newDrawable().mutate());
            } else if (srcDrawable == null || dstDrawable == null) {
                int stateDrawableIndex3 = sld.getStateDrawableIndex(empty_set);
                int index3 = stateDrawableIndex3;
                if (stateDrawableIndex3 != -1) {
                    if (srcDrawable == null) {
                        drawable2 = sld.getStateDrawable(index3);
                    } else {
                        drawable2 = srcDrawable;
                    }
                    if (dstDrawable == null) {
                        drawable3 = sld.getStateDrawable(index3);
                    } else {
                        drawable3 = dstDrawable;
                    }
                    setSrcAndDst(drawable2, drawable3);
                    return;
                }
                throw new IllegalArgumentException("no resource available to provide");
            } else {
                setSrcAndDst(srcDrawable, dstDrawable);
            }
        } else {
            setSrcAndDst(drawable, drawable.getConstantState().newDrawable().mutate());
        }
    }

    private void setSrcAndDst(Drawable srcDrawable, Drawable dstDrawable) {
        if (srcDrawable != null && dstDrawable != null) {
            this.mSrcDrawable = srcDrawable;
            this.mSrcDrawable.setTint(this.mIconDefaultColor);
            this.mSrcDrawable.setBounds(this.mDrawableRect);
            this.mDstDrawable = dstDrawable;
            this.mDstDrawable.setTint(this.mIconActiveColor);
            this.mDstDrawable.setBounds(this.mDrawableRect);
        }
    }

    /* access modifiers changed from: private */
    public void setRadius(int mRadius2) {
        this.mRadius = mRadius2;
        invalidateSelf();
    }

    /* access modifiers changed from: package-private */
    public void setActiveColor(int iconActiveColor) {
        if (this.mIconActiveColor != iconActiveColor) {
            this.mIconActiveColor = iconActiveColor;
            if (this.mDstDrawable != null) {
                this.mDstDrawable.setTint(this.mIconActiveColor);
            }
            invalidateSelf();
        }
    }

    /* access modifiers changed from: package-private */
    public void setDefaultColor(int iconDefaultColor) {
        if (this.mIconDefaultColor != iconDefaultColor) {
            this.mIconDefaultColor = iconDefaultColor;
            if (this.mSrcDrawable != null) {
                this.mSrcDrawable.setTint(this.mIconDefaultColor);
            }
            invalidateSelf();
        }
    }

    private void startAnim(boolean checkedState) {
        ValueAnimator outdatedAnim = checkedState ? this.mUnCheckAnim : this.mCheckAnim;
        ValueAnimator currentAnim = checkedState ? this.mCheckAnim : this.mUnCheckAnim;
        if (outdatedAnim.isRunning()) {
            outdatedAnim.reverse();
        } else {
            currentAnim.start();
        }
    }

    /* access modifiers changed from: package-private */
    public void setState(boolean checked, boolean useAnim) {
        if (useAnim) {
            startAnim(checked);
        } else {
            setRadius(checked ? (int) (((float) this.mIconBounds) * MAX_SCALE) : 0);
        }
    }
}
