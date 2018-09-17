package android.transition;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import huawei.cust.HwCfgFilePolicy;

public class Slide extends Visibility {
    private static final String PROPNAME_SCREEN_POSITION = "android:slide:screenPosition";
    private static final String TAG = "Slide";
    private static final TimeInterpolator sAccelerate = null;
    private static final CalculateSlide sCalculateBottom = null;
    private static final CalculateSlide sCalculateEnd = null;
    private static final CalculateSlide sCalculateLeft = null;
    private static final CalculateSlide sCalculateRight = null;
    private static final CalculateSlide sCalculateStart = null;
    private static final CalculateSlide sCalculateTop = null;
    private static final TimeInterpolator sDecelerate = null;
    private CalculateSlide mSlideCalculator;
    private int mSlideEdge;
    private float mSlideFraction;

    private interface CalculateSlide {
        float getGoneX(ViewGroup viewGroup, View view, float f);

        float getGoneY(ViewGroup viewGroup, View view, float f);
    }

    private static abstract class CalculateSlideHorizontal implements CalculateSlide {
        private CalculateSlideHorizontal() {
        }

        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY();
        }
    }

    private static abstract class CalculateSlideVertical implements CalculateSlide {
        private CalculateSlideVertical() {
        }

        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.Slide.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.Slide.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.Slide.<clinit>():void");
    }

    public Slide() {
        this.mSlideCalculator = sCalculateBottom;
        this.mSlideEdge = 80;
        this.mSlideFraction = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        setSlideEdge(80);
    }

    public Slide(int slideEdge) {
        this.mSlideCalculator = sCalculateBottom;
        this.mSlideEdge = 80;
        this.mSlideFraction = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        setSlideEdge(slideEdge);
    }

    public Slide(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSlideCalculator = sCalculateBottom;
        this.mSlideEdge = 80;
        this.mSlideFraction = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Slide);
        int edge = a.getInt(0, 80);
        a.recycle();
        setSlideEdge(edge);
    }

    private void captureValues(TransitionValues transitionValues) {
        int[] position = new int[2];
        transitionValues.view.getLocationOnScreen(position);
        transitionValues.values.put(PROPNAME_SCREEN_POSITION, position);
    }

    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        captureValues(transitionValues);
    }

    public void setSlideEdge(int slideEdge) {
        switch (slideEdge) {
            case HwCfgFilePolicy.BASE /*3*/:
                this.mSlideCalculator = sCalculateLeft;
                break;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                this.mSlideCalculator = sCalculateRight;
                break;
            case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                this.mSlideCalculator = sCalculateTop;
                break;
            case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                this.mSlideCalculator = sCalculateBottom;
                break;
            case Gravity.START /*8388611*/:
                this.mSlideCalculator = sCalculateStart;
                break;
            case Gravity.END /*8388613*/:
                this.mSlideCalculator = sCalculateEnd;
                break;
            default:
                throw new IllegalArgumentException("Invalid slide direction");
        }
        this.mSlideEdge = slideEdge;
        SidePropagation propagation = new SidePropagation();
        propagation.setSide(slideEdge);
        setPropagation(propagation);
    }

    public int getSlideEdge() {
        return this.mSlideEdge;
    }

    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }
        int[] position = (int[]) endValues.values.get(PROPNAME_SCREEN_POSITION);
        float endX = view.getTranslationX();
        float endY = view.getTranslationY();
        return TranslationAnimationCreator.createAnimation(view, endValues, position[0], position[1], this.mSlideCalculator.getGoneX(sceneRoot, view, this.mSlideFraction), this.mSlideCalculator.getGoneY(sceneRoot, view, this.mSlideFraction), endX, endY, sDecelerate, this);
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null) {
            return null;
        }
        int[] position = (int[]) startValues.values.get(PROPNAME_SCREEN_POSITION);
        return TranslationAnimationCreator.createAnimation(view, startValues, position[0], position[1], view.getTranslationX(), view.getTranslationY(), this.mSlideCalculator.getGoneX(sceneRoot, view, this.mSlideFraction), this.mSlideCalculator.getGoneY(sceneRoot, view, this.mSlideFraction), sAccelerate, this);
    }

    public void setSlideFraction(float slideFraction) {
        this.mSlideFraction = slideFraction;
    }
}
