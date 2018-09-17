package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.R;
import java.util.Random;

public class LayoutAnimationController {
    public static final int ORDER_NORMAL = 0;
    public static final int ORDER_RANDOM = 2;
    public static final int ORDER_REVERSE = 1;
    protected Animation mAnimation;
    private float mDelay;
    private long mDuration;
    protected Interpolator mInterpolator;
    private long mMaxDelay;
    private int mOrder;
    protected Random mRandomizer;

    public static class AnimationParameters {
        public int count;
        public int index;
    }

    public LayoutAnimationController(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LayoutAnimation);
        this.mDelay = Description.parseValue(a.peekValue(1)).value;
        this.mOrder = a.getInt(3, 0);
        int resource = a.getResourceId(2, 0);
        if (resource > 0) {
            setAnimation(context, resource);
        }
        resource = a.getResourceId(0, 0);
        if (resource > 0) {
            setInterpolator(context, resource);
        }
        a.recycle();
    }

    public LayoutAnimationController(Animation animation) {
        this(animation, 0.5f);
    }

    public LayoutAnimationController(Animation animation, float delay) {
        this.mDelay = delay;
        setAnimation(animation);
    }

    public int getOrder() {
        return this.mOrder;
    }

    public void setOrder(int order) {
        this.mOrder = order;
    }

    public void setAnimation(Context context, int resourceID) {
        setAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    public void setAnimation(Animation animation) {
        this.mAnimation = animation;
        this.mAnimation.setFillBefore(true);
    }

    public Animation getAnimation() {
        return this.mAnimation;
    }

    public void setInterpolator(Context context, int resourceID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resourceID));
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public float getDelay() {
        return this.mDelay;
    }

    public void setDelay(float delay) {
        this.mDelay = delay;
    }

    public boolean willOverlap() {
        return this.mDelay < 1.0f;
    }

    public void start() {
        this.mDuration = this.mAnimation.getDuration();
        this.mMaxDelay = Long.MIN_VALUE;
        this.mAnimation.setStartTime(-1);
    }

    public final Animation getAnimationForView(View view) {
        long delay = getDelayForView(view) + this.mAnimation.getStartOffset();
        this.mMaxDelay = Math.max(this.mMaxDelay, delay);
        try {
            Animation animation = this.mAnimation.clone();
            animation.setStartOffset(delay);
            return animation;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean isDone() {
        return AnimationUtils.currentAnimationTimeMillis() > (this.mAnimation.getStartTime() + this.mMaxDelay) + this.mDuration;
    }

    protected long getDelayForView(View view) {
        AnimationParameters params = view.getLayoutParams().layoutAnimationParameters;
        if (params == null) {
            return 0;
        }
        float delay = this.mDelay * ((float) this.mAnimation.getDuration());
        long viewDelay = (long) (((float) getTransformedIndex(params)) * delay);
        float totalDelay = delay * ((float) params.count);
        if (this.mInterpolator == null) {
            this.mInterpolator = new LinearInterpolator();
        }
        return (long) (this.mInterpolator.getInterpolation(((float) viewDelay) / totalDelay) * totalDelay);
    }

    protected int getTransformedIndex(AnimationParameters params) {
        switch (getOrder()) {
            case 1:
                return (params.count - 1) - params.index;
            case 2:
                if (this.mRandomizer == null) {
                    this.mRandomizer = new Random();
                }
                return (int) (((float) params.count) * this.mRandomizer.nextFloat());
            default:
                return params.index;
        }
    }
}
