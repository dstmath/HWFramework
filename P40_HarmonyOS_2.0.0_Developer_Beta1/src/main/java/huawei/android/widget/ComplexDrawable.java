package huawei.android.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.animation.AnimationUtils;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class ComplexDrawable extends Drawable {
    private static final float MAX_SCALE = 1.42f;
    private static final int STATE_INVALID_INDEX = -1;
    private int mAnimationDuration;
    private ValueAnimator.AnimatorUpdateListener mAnimatorListener;
    private ValueAnimator mCheckAnim;
    private Path mClipPath;
    private Context mContext;
    private Rect mDrawableRect;
    private Drawable mDstDrawable;
    private int mIconActiveColor;
    private int mIconBounds;
    private int mIconDefaultColor;
    private int mRadius;
    private Drawable mSrcDrawable;
    private ValueAnimator mUnCheckAnim;

    public ComplexDrawable(Context context, Drawable srcDrawable) {
        this(context, srcDrawable, 0);
    }

    public ComplexDrawable(Context context, Drawable srcDrawable, int iconBounds) {
        this.mRadius = 0;
        this.mContext = context;
        initialise(srcDrawable, iconBounds);
    }

    private void initialise(Drawable srcDrawable, int iconBounds) {
        this.mAnimationDuration = ResLoaderUtil.getResources(this.mContext).getInteger(ResLoader.getInstance().getIdentifier(this.mContext, "integer", "bottomnav_icon_anim_duration"));
        if (iconBounds == 0) {
            this.mIconBounds = ResLoaderUtil.getDimensionPixelSize(this.mContext, "bottomnav_item_icon_size");
        } else {
            this.mIconBounds = iconBounds;
        }
        int i = this.mIconBounds;
        this.mDrawableRect = new Rect(0, 0, i, i);
        setSrcDrawable(srcDrawable);
        this.mAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.ComplexDrawable.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (valueAnimator != null) {
                    Object animatedValue = valueAnimator.getAnimatedValue();
                    if (animatedValue instanceof Integer) {
                        ComplexDrawable.this.setRadius(((Integer) animatedValue).intValue());
                    }
                }
            }
        };
        this.mClipPath = new Path();
        initAnim();
    }

    private void initAnim() {
        this.mCheckAnim = ValueAnimator.ofInt(0, (int) (((float) this.mIconBounds) * MAX_SCALE));
        this.mCheckAnim.setDuration((long) this.mAnimationDuration);
        this.mCheckAnim.addUpdateListener(this.mAnimatorListener);
        this.mCheckAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, ResLoader.getInstance().getIdentifier(this.mContext, "interpolator", "cubic_bezier_interpolator_type_20_80")));
        this.mUnCheckAnim = ValueAnimator.ofInt((int) (((float) this.mIconBounds) * MAX_SCALE), 0);
        this.mUnCheckAnim.setDuration((long) this.mAnimationDuration);
        this.mUnCheckAnim.addUpdateListener(this.mAnimatorListener);
        Context context = this.mContext;
        this.mUnCheckAnim.setInterpolator(AnimationUtils.loadInterpolator(context, context.getResources().getIdentifier("fast_out_slow_in", "interpolator", "android")));
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.mClipPath.reset();
        this.mClipPath.addCircle((float) (getLayoutDirection() == 1 ? this.mIconBounds : this.mDrawableRect.left), (float) this.mDrawableRect.bottom, (float) this.mRadius, Path.Direction.CCW);
        canvas.save();
        canvas.clipOutPath(this.mClipPath);
        this.mSrcDrawable.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.clipPath(this.mClipPath);
        this.mDstDrawable.draw(canvas);
        canvas.restore();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        Drawable drawable = this.mSrcDrawable;
        if (drawable != null) {
            drawable.setAlpha(alpha);
        }
        Drawable drawable2 = this.mDstDrawable;
        if (drawable2 != null) {
            drawable2.setAlpha(alpha);
        }
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        Drawable drawable = this.mSrcDrawable;
        if (drawable != null) {
            drawable.setColorFilter(colorFilter);
        }
        Drawable drawable2 = this.mDstDrawable;
        if (drawable2 != null) {
            drawable2.setColorFilter(colorFilter);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        Drawable drawable = this.mSrcDrawable;
        if (drawable != null) {
            return drawable.getOpacity();
        }
        return -3;
    }

    /* access modifiers changed from: package-private */
    public void setDrawableSize(int size) {
        this.mIconBounds = size;
        this.mDrawableRect.set(0, 0, size, size);
        this.mCheckAnim.setIntValues(0, (int) (((float) this.mIconBounds) * MAX_SCALE));
        this.mUnCheckAnim.setIntValues((int) (((float) this.mIconBounds) * MAX_SCALE), 0);
        Drawable drawable = this.mSrcDrawable;
        if (drawable != null) {
            drawable.setBounds(this.mDrawableRect);
        }
        Drawable drawable2 = this.mDstDrawable;
        if (drawable2 != null) {
            drawable2.setBounds(this.mDrawableRect);
        }
        invalidateSelf();
    }

    /* access modifiers changed from: package-private */
    public void setSrcDrawable(int drawableRes) {
        setSrcDrawable(this.mContext.getResources().getDrawable(drawableRes));
    }

    /* access modifiers changed from: package-private */
    public void setSrcDrawable(Drawable drawable) {
        Drawable.ConstantState constantState;
        if (drawable instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) drawable;
            int attr = this.mContext.getResources().getIdentifier("state_selected", "attr", "android");
            int[] emptySet = new int[0];
            int[] selectedSet = {attr};
            Drawable srcDrawable = null;
            Drawable dstDrawable = null;
            int index = stateListDrawable.findStateDrawableIndex(new int[]{~attr});
            if (index != -1) {
                srcDrawable = stateListDrawable.getStateDrawable(index);
            }
            int index2 = stateListDrawable.findStateDrawableIndex(selectedSet);
            if (index2 != -1) {
                dstDrawable = stateListDrawable.getStateDrawable(index2);
            }
            int index3 = stateListDrawable.findStateDrawableIndex(emptySet);
            if (srcDrawable == null && dstDrawable == null) {
                setSrcAndDst(drawable, drawable.getConstantState().newDrawable().mutate());
            } else if (srcDrawable != null && dstDrawable != null) {
                setSrcAndDst(srcDrawable, dstDrawable);
            } else if (index3 != -1) {
                setSrcAndDst(srcDrawable == null ? stateListDrawable.getStateDrawable(index3) : srcDrawable, dstDrawable == null ? stateListDrawable.getStateDrawable(index3) : dstDrawable);
            } else {
                throw new IllegalArgumentException("no resource available to provide");
            }
        } else if (drawable != null && (constantState = drawable.getConstantState()) != null) {
            setSrcAndDst(drawable, constantState.newDrawable().mutate());
        }
    }

    private void setSrcAndDst(Drawable srcDrawable, Drawable dstDrawable) {
        if (srcDrawable != null && dstDrawable != null) {
            this.mSrcDrawable = srcDrawable;
            this.mSrcDrawable.setBounds(this.mDrawableRect);
            this.mDstDrawable = dstDrawable;
            this.mDstDrawable.setBounds(this.mDrawableRect);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRadius(int radius) {
        this.mRadius = radius;
        invalidateSelf();
    }

    /* access modifiers changed from: package-private */
    public void setActiveColor(int iconActiveColor) {
        if (this.mIconActiveColor != iconActiveColor) {
            this.mIconActiveColor = iconActiveColor;
            Drawable drawable = this.mDstDrawable;
            if (drawable != null) {
                drawable.setTint(this.mIconActiveColor);
            }
            invalidateSelf();
        }
    }

    /* access modifiers changed from: package-private */
    public void setDefaultColor(int iconDefaultColor) {
        if (this.mIconDefaultColor != iconDefaultColor) {
            this.mIconDefaultColor = iconDefaultColor;
            Drawable drawable = this.mSrcDrawable;
            if (drawable != null) {
                drawable.setTint(this.mIconDefaultColor);
            }
            invalidateSelf();
        }
    }

    private void startAnim(boolean isCheckedState) {
        ValueAnimator outdatedAnim = isCheckedState ? this.mUnCheckAnim : this.mCheckAnim;
        ValueAnimator currentAnim = isCheckedState ? this.mCheckAnim : this.mUnCheckAnim;
        if (outdatedAnim.isRunning()) {
            outdatedAnim.reverse();
        } else {
            currentAnim.start();
        }
    }

    /* access modifiers changed from: package-private */
    public void setState(boolean isChecked, boolean isUseAnim) {
        if (isUseAnim) {
            startAnim(isChecked);
        } else {
            setRadius(isChecked ? (int) (((float) this.mIconBounds) * MAX_SCALE) : 0);
        }
    }
}
