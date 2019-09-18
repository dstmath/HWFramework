package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.rms.AppAssociate;
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
    /* access modifiers changed from: private */
    public int mFadeBehavior = 1;
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
        ViewOverlay overlay;
        ObjectAnimator anim;
        BitmapDrawable endDrawable;
        TransitionValues transitionValues = startValues;
        TransitionValues transitionValues2 = endValues;
        if (transitionValues == null || transitionValues2 == null) {
            return null;
        }
        boolean useParentOverlay = this.mFadeBehavior != 1;
        final View view = transitionValues2.view;
        Map<String, Object> startVals = transitionValues.values;
        Map<String, Object> endVals = transitionValues2.values;
        Rect startBounds = (Rect) startVals.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endVals.get(PROPNAME_BOUNDS);
        Bitmap startBitmap = (Bitmap) startVals.get(PROPNAME_BITMAP);
        Bitmap endBitmap = (Bitmap) endVals.get(PROPNAME_BITMAP);
        final BitmapDrawable startDrawable = (BitmapDrawable) startVals.get(PROPNAME_DRAWABLE);
        BitmapDrawable endDrawable2 = (BitmapDrawable) endVals.get(PROPNAME_DRAWABLE);
        if (startDrawable == null || endDrawable2 == null || startBitmap.sameAs(endBitmap)) {
            Bitmap bitmap = endBitmap;
            Bitmap bitmap2 = startBitmap;
            Rect rect = endBounds;
            BitmapDrawable bitmapDrawable = endDrawable2;
            return null;
        }
        ViewOverlay overlay2 = useParentOverlay ? ((ViewGroup) view.getParent()).getOverlay() : view.getOverlay();
        if (this.mFadeBehavior == 1) {
            overlay2.add(endDrawable2);
        }
        overlay2.add(startDrawable);
        if (this.mFadeBehavior == 2) {
            overlay = overlay2;
            anim = ObjectAnimator.ofInt(startDrawable, AppAssociate.ASSOC_WINDOW_ALPHA, new int[]{255, 0, 0});
        } else {
            overlay = overlay2;
            anim = ObjectAnimator.ofInt(startDrawable, AppAssociate.ASSOC_WINDOW_ALPHA, new int[]{0});
        }
        ObjectAnimator anim2 = anim;
        anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                view.invalidate(startDrawable.getBounds());
            }
        });
        ObjectAnimator anim1 = null;
        if (this.mFadeBehavior == 2) {
            anim1 = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 0.0f, 1.0f});
            endDrawable = endDrawable2;
        } else if (this.mFadeBehavior == 0) {
            endDrawable = endDrawable2;
            anim1 = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 1.0f});
        } else {
            endDrawable = endDrawable2;
        }
        ViewOverlay viewOverlay = overlay;
        BitmapDrawable endDrawable3 = endDrawable;
        BitmapDrawable startDrawable2 = startDrawable;
        final boolean z = useParentOverlay;
        Bitmap bitmap3 = endBitmap;
        final View view2 = view;
        Bitmap bitmap4 = startBitmap;
        final BitmapDrawable bitmapDrawable2 = startDrawable2;
        Rect endBounds2 = endBounds;
        final BitmapDrawable bitmapDrawable3 = endDrawable3;
        AnonymousClass2 r0 = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ViewOverlay overlay = z ? ((ViewGroup) view2.getParent()).getOverlay() : view2.getOverlay();
                overlay.remove(bitmapDrawable2);
                if (Crossfade.this.mFadeBehavior == 1) {
                    overlay.remove(bitmapDrawable3);
                }
            }
        };
        anim2.addListener(r0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(new Animator[]{anim2});
        if (anim1 != null) {
            set.playTogether(new Animator[]{anim1});
        }
        if (this.mResizeBehavior != 1 || startBounds.equals(endBounds2)) {
            BitmapDrawable bitmapDrawable4 = startDrawable2;
        } else {
            set.playTogether(new Animator[]{ObjectAnimator.ofObject(startDrawable2, "bounds", sRectEvaluator, new Object[]{startBounds, endBounds2})});
            if (this.mResizeBehavior == 1) {
                set.playTogether(new Animator[]{ObjectAnimator.ofObject(endDrawable3, "bounds", sRectEvaluator, new Object[]{startBounds, endBounds2})});
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
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
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
