package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import java.util.Map;

public class Crossfade extends Transition {
    public static final int FADE_BEHAVIOR_CROSSFADE = 0;
    public static final int FADE_BEHAVIOR_OUT_IN = 2;
    public static final int FADE_BEHAVIOR_REVEAL = 1;
    private static final String LOG_TAG = "Crossfade";
    private static final String PROPNAME_BITMAP = "android:crossfade:bitmap";
    private static final String PROPNAME_BOUNDS = "android:crossfade:bounds";
    private static final String PROPNAME_DRAWABLE = "android:crossfade:drawable";
    public static final int RESIZE_BEHAVIOR_NONE = 0;
    public static final int RESIZE_BEHAVIOR_SCALE = 1;
    private static RectEvaluator sRectEvaluator = new RectEvaluator();
    private int mFadeBehavior = 1;
    private int mResizeBehavior = 1;

    public Crossfade setFadeBehavior(int fadeBehavior) {
        if (fadeBehavior >= 0 && fadeBehavior <= 2) {
            this.mFadeBehavior = fadeBehavior;
        }
        return this;
    }

    public int getFadeBehavior() {
        return this.mFadeBehavior;
    }

    public Crossfade setResizeBehavior(int resizeBehavior) {
        if (resizeBehavior >= 0 && resizeBehavior <= 1) {
            this.mResizeBehavior = resizeBehavior;
        }
        return this;
    }

    public int getResizeBehavior() {
        return this.mResizeBehavior;
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        final boolean useParentOverlay = this.mFadeBehavior != 1;
        final View view = endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        Rect startBounds = (Rect) startVals.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endVals.get(PROPNAME_BOUNDS);
        Bitmap startBitmap = (Bitmap) startVals.get(PROPNAME_BITMAP);
        Bitmap endBitmap = (Bitmap) endVals.get(PROPNAME_BITMAP);
        final BitmapDrawable startDrawable = (BitmapDrawable) startVals.get(PROPNAME_DRAWABLE);
        final BitmapDrawable endDrawable = (BitmapDrawable) endVals.get(PROPNAME_DRAWABLE);
        if (startDrawable == null || endDrawable == null || (startBitmap.sameAs(endBitmap) ^ 1) == 0) {
            return null;
        }
        ObjectAnimator anim;
        ViewOverlay overlay = useParentOverlay ? ((ViewGroup) view.getParent()).getOverlay() : view.getOverlay();
        if (this.mFadeBehavior == 1) {
            overlay.add(endDrawable);
        }
        overlay.add(startDrawable);
        if (this.mFadeBehavior == 2) {
            anim = ObjectAnimator.ofInt(startDrawable, "alpha", new int[]{255, 0, 0});
        } else {
            anim = ObjectAnimator.ofInt(startDrawable, "alpha", new int[]{0});
        }
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                view.invalidate(startDrawable.getBounds());
            }
        });
        ObjectAnimator anim1 = null;
        if (this.mFadeBehavior == 2) {
            anim1 = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 0.0f, 1.0f});
        } else if (this.mFadeBehavior == 0) {
            anim1 = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 1.0f});
        }
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ViewOverlay overlay = useParentOverlay ? ((ViewGroup) view.getParent()).getOverlay() : view.getOverlay();
                overlay.remove(startDrawable);
                if (Crossfade.this.mFadeBehavior == 1) {
                    overlay.remove(endDrawable);
                }
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playTogether(new Animator[]{anim});
        if (anim1 != null) {
            set.playTogether(new Animator[]{anim1});
        }
        if (this.mResizeBehavior == 1 && (startBounds.equals(endBounds) ^ 1) != 0) {
            Animator anim2 = ObjectAnimator.ofObject(startDrawable, "bounds", sRectEvaluator, new Object[]{startBounds, endBounds});
            set.playTogether(new Animator[]{anim2});
            if (this.mResizeBehavior == 1) {
                Animator anim3 = ObjectAnimator.ofObject(endDrawable, "bounds", sRectEvaluator, new Object[]{startBounds, endBounds});
                set.playTogether(new Animator[]{anim3});
            }
        }
        return set;
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        Rect bounds = new Rect(0, 0, view.getWidth(), view.getHeight());
        if (this.mFadeBehavior != 1) {
            bounds.offset(view.getLeft(), view.getTop());
        }
        transitionValues.values.put(PROPNAME_BOUNDS, bounds);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        if (view instanceof TextureView) {
            bitmap = ((TextureView) view).getBitmap();
        } else {
            view.draw(new Canvas(bitmap));
        }
        transitionValues.values.put(PROPNAME_BITMAP, bitmap);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        drawable.setBounds(bounds);
        transitionValues.values.put(PROPNAME_DRAWABLE, drawable);
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
}
