package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.transition.Transition.TransitionListenerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;

public class Fade extends Visibility {
    private static boolean DBG = false;
    public static final int IN = 1;
    private static final String LOG_TAG = "Fade";
    public static final int OUT = 2;
    static final String PROPNAME_TRANSITION_ALPHA = "android:fade:transitionAlpha";

    /* renamed from: android.transition.Fade.1 */
    class AnonymousClass1 extends TransitionListenerAdapter {
        final /* synthetic */ View val$view;

        AnonymousClass1(View val$view) {
            this.val$view = val$view;
        }

        public void onTransitionEnd(Transition transition) {
            this.val$view.setTransitionAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }
    }

    private static class FadeAnimatorListener extends AnimatorListenerAdapter {
        private boolean mLayerTypeChanged;
        private final View mView;

        public FadeAnimatorListener(View view) {
            this.mLayerTypeChanged = false;
            this.mView = view;
        }

        public void onAnimationStart(Animator animator) {
            if (this.mView.hasOverlappingRendering() && this.mView.getLayerType() == 0) {
                this.mLayerTypeChanged = true;
                this.mView.setLayerType(Fade.OUT, null);
            }
        }

        public void onAnimationEnd(Animator animator) {
            this.mView.setTransitionAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            if (this.mLayerTypeChanged) {
                this.mView.setLayerType(0, null);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.Fade.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.Fade.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.Fade.<clinit>():void");
    }

    public Fade(int fadingMode) {
        setMode(fadingMode);
    }

    public Fade(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMode(context.obtainStyledAttributes(attrs, R.styleable.Fade).getInt(0, getMode()));
    }

    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        transitionValues.values.put(PROPNAME_TRANSITION_ALPHA, Float.valueOf(transitionValues.view.getTransitionAlpha()));
    }

    private Animator createAnimation(View view, float startAlpha, float endAlpha) {
        if (startAlpha == endAlpha) {
            return null;
        }
        view.setTransitionAlpha(startAlpha);
        float[] fArr = new float[IN];
        fArr[0] = endAlpha;
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "transitionAlpha", fArr);
        if (DBG) {
            Log.d(LOG_TAG, "Created animator " + anim);
        }
        anim.addListener(new FadeAnimatorListener(view));
        addListener(new AnonymousClass1(view));
        return anim;
    }

    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (DBG) {
            Log.d(LOG_TAG, "Fade.onAppear: startView, startVis, endView, endVis = " + (startValues != null ? startValues.view : null) + ", " + view);
        }
        float startAlpha = getStartAlpha(startValues, 0.0f);
        if (startAlpha == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            startAlpha = 0.0f;
        }
        return createAnimation(view, startAlpha, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createAnimation(view, getStartAlpha(startValues, LayoutParams.BRIGHTNESS_OVERRIDE_FULL), 0.0f);
    }

    private static float getStartAlpha(TransitionValues startValues, float fallbackValue) {
        float startAlpha = fallbackValue;
        if (startValues == null) {
            return startAlpha;
        }
        Float startAlphaFloat = (Float) startValues.values.get(PROPNAME_TRANSITION_ALPHA);
        if (startAlphaFloat != null) {
            return startAlphaFloat.floatValue();
        }
        return startAlpha;
    }
}
