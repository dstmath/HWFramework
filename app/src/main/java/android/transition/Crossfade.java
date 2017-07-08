package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Property;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.view.WindowManager.LayoutParams;
import com.android.internal.logging.MetricsProto.MetricsEvent;
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
    private static RectEvaluator sRectEvaluator;
    private int mFadeBehavior;
    private int mResizeBehavior;

    /* renamed from: android.transition.Crossfade.1 */
    class AnonymousClass1 implements AnimatorUpdateListener {
        final /* synthetic */ BitmapDrawable val$startDrawable;
        final /* synthetic */ View val$view;

        AnonymousClass1(View val$view, BitmapDrawable val$startDrawable) {
            this.val$view = val$view;
            this.val$startDrawable = val$startDrawable;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            this.val$view.invalidate(this.val$startDrawable.getBounds());
        }
    }

    /* renamed from: android.transition.Crossfade.2 */
    class AnonymousClass2 extends AnimatorListenerAdapter {
        final /* synthetic */ BitmapDrawable val$endDrawable;
        final /* synthetic */ BitmapDrawable val$startDrawable;
        final /* synthetic */ boolean val$useParentOverlay;
        final /* synthetic */ View val$view;

        AnonymousClass2(boolean val$useParentOverlay, View val$view, BitmapDrawable val$startDrawable, BitmapDrawable val$endDrawable) {
            this.val$useParentOverlay = val$useParentOverlay;
            this.val$view = val$view;
            this.val$startDrawable = val$startDrawable;
            this.val$endDrawable = val$endDrawable;
        }

        public void onAnimationEnd(Animator animation) {
            ViewOverlay overlay = this.val$useParentOverlay ? ((ViewGroup) this.val$view.getParent()).getOverlay() : this.val$view.getOverlay();
            overlay.remove(this.val$startDrawable);
            if (Crossfade.this.mFadeBehavior == Crossfade.RESIZE_BEHAVIOR_SCALE) {
                overlay.remove(this.val$endDrawable);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.Crossfade.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.Crossfade.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.transition.Crossfade.<clinit>():void");
    }

    public Crossfade() {
        this.mFadeBehavior = RESIZE_BEHAVIOR_SCALE;
        this.mResizeBehavior = RESIZE_BEHAVIOR_SCALE;
    }

    public Crossfade setFadeBehavior(int fadeBehavior) {
        if (fadeBehavior >= 0 && fadeBehavior <= FADE_BEHAVIOR_OUT_IN) {
            this.mFadeBehavior = fadeBehavior;
        }
        return this;
    }

    public int getFadeBehavior() {
        return this.mFadeBehavior;
    }

    public Crossfade setResizeBehavior(int resizeBehavior) {
        if (resizeBehavior >= 0 && resizeBehavior <= RESIZE_BEHAVIOR_SCALE) {
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
        boolean useParentOverlay = this.mFadeBehavior != RESIZE_BEHAVIOR_SCALE;
        View view = endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        Rect startBounds = (Rect) startVals.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endVals.get(PROPNAME_BOUNDS);
        Bitmap startBitmap = (Bitmap) startVals.get(PROPNAME_BITMAP);
        Bitmap endBitmap = (Bitmap) endVals.get(PROPNAME_BITMAP);
        BitmapDrawable startDrawable = (BitmapDrawable) startVals.get(PROPNAME_DRAWABLE);
        BitmapDrawable endDrawable = (BitmapDrawable) endVals.get(PROPNAME_DRAWABLE);
        if (startDrawable == null || endDrawable == null || startBitmap.sameAs(endBitmap)) {
            return null;
        }
        ObjectAnimator anim;
        ViewOverlay overlay = useParentOverlay ? ((ViewGroup) view.getParent()).getOverlay() : view.getOverlay();
        if (this.mFadeBehavior == RESIZE_BEHAVIOR_SCALE) {
            overlay.add(endDrawable);
        }
        overlay.add(startDrawable);
        if (this.mFadeBehavior == FADE_BEHAVIOR_OUT_IN) {
            anim = ObjectAnimator.ofInt(startDrawable, "alpha", new int[]{MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, RESIZE_BEHAVIOR_NONE, RESIZE_BEHAVIOR_NONE});
        } else {
            int[] iArr = new int[RESIZE_BEHAVIOR_SCALE];
            iArr[RESIZE_BEHAVIOR_NONE] = RESIZE_BEHAVIOR_NONE;
            anim = ObjectAnimator.ofInt(startDrawable, "alpha", iArr);
        }
        anim.addUpdateListener(new AnonymousClass1(view, startDrawable));
        ObjectAnimator anim1 = null;
        if (this.mFadeBehavior == FADE_BEHAVIOR_OUT_IN) {
            anim1 = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 0.0f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
        } else if (this.mFadeBehavior == 0) {
            Property property = View.ALPHA;
            float[] fArr = new float[FADE_BEHAVIOR_OUT_IN];
            fArr[RESIZE_BEHAVIOR_NONE] = 0.0f;
            fArr[RESIZE_BEHAVIOR_SCALE] = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            anim1 = ObjectAnimator.ofFloat(view, property, fArr);
        }
        anim.addListener(new AnonymousClass2(useParentOverlay, view, startDrawable, endDrawable));
        AnimatorSet set = new AnimatorSet();
        Animator[] animatorArr = new Animator[RESIZE_BEHAVIOR_SCALE];
        animatorArr[RESIZE_BEHAVIOR_NONE] = anim;
        set.playTogether(animatorArr);
        if (anim1 != null) {
            animatorArr = new Animator[RESIZE_BEHAVIOR_SCALE];
            animatorArr[RESIZE_BEHAVIOR_NONE] = anim1;
            set.playTogether(animatorArr);
        }
        if (this.mResizeBehavior == RESIZE_BEHAVIOR_SCALE && !startBounds.equals(endBounds)) {
            TypeEvaluator typeEvaluator = sRectEvaluator;
            Rect[] rectArr = new Object[FADE_BEHAVIOR_OUT_IN];
            rectArr[RESIZE_BEHAVIOR_NONE] = startBounds;
            rectArr[RESIZE_BEHAVIOR_SCALE] = endBounds;
            animatorArr = new Animator[RESIZE_BEHAVIOR_SCALE];
            animatorArr[RESIZE_BEHAVIOR_NONE] = ObjectAnimator.ofObject(startDrawable, "bounds", typeEvaluator, rectArr);
            set.playTogether(animatorArr);
            if (this.mResizeBehavior == RESIZE_BEHAVIOR_SCALE) {
                typeEvaluator = sRectEvaluator;
                rectArr = new Object[FADE_BEHAVIOR_OUT_IN];
                rectArr[RESIZE_BEHAVIOR_NONE] = startBounds;
                rectArr[RESIZE_BEHAVIOR_SCALE] = endBounds;
                animatorArr = new Animator[RESIZE_BEHAVIOR_SCALE];
                animatorArr[RESIZE_BEHAVIOR_NONE] = ObjectAnimator.ofObject(endDrawable, "bounds", typeEvaluator, rectArr);
                set.playTogether(animatorArr);
            }
        }
        return set;
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        Rect bounds = new Rect(RESIZE_BEHAVIOR_NONE, RESIZE_BEHAVIOR_NONE, view.getWidth(), view.getHeight());
        if (this.mFadeBehavior != RESIZE_BEHAVIOR_SCALE) {
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
