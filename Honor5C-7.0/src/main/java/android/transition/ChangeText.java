package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.text.Spanned;
import android.transition.Transition.TransitionListenerAdapter;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import java.util.Map;

public class ChangeText extends Transition {
    public static final int CHANGE_BEHAVIOR_IN = 2;
    public static final int CHANGE_BEHAVIOR_KEEP = 0;
    public static final int CHANGE_BEHAVIOR_OUT = 1;
    public static final int CHANGE_BEHAVIOR_OUT_IN = 3;
    private static final String LOG_TAG = "TextChange";
    private static final String PROPNAME_TEXT = "android:textchange:text";
    private static final String PROPNAME_TEXT_COLOR = "android:textchange:textColor";
    private static final String PROPNAME_TEXT_SELECTION_END = "android:textchange:textSelectionEnd";
    private static final String PROPNAME_TEXT_SELECTION_START = "android:textchange:textSelectionStart";
    private static final String[] sTransitionProperties = null;
    private int mChangeBehavior;

    /* renamed from: android.transition.ChangeText.1 */
    class AnonymousClass1 extends AnimatorListenerAdapter {
        final /* synthetic */ int val$endSelectionEnd;
        final /* synthetic */ int val$endSelectionStart;
        final /* synthetic */ CharSequence val$endText;
        final /* synthetic */ CharSequence val$startText;
        final /* synthetic */ TextView val$view;

        AnonymousClass1(CharSequence val$startText, TextView val$view, CharSequence val$endText, int val$endSelectionStart, int val$endSelectionEnd) {
            this.val$startText = val$startText;
            this.val$view = val$view;
            this.val$endText = val$endText;
            this.val$endSelectionStart = val$endSelectionStart;
            this.val$endSelectionEnd = val$endSelectionEnd;
        }

        public void onAnimationEnd(Animator animation) {
            if (this.val$startText.equals(this.val$view.getText())) {
                this.val$view.setText(this.val$endText);
                if (this.val$view instanceof EditText) {
                    ChangeText.this.setSelection((EditText) this.val$view, this.val$endSelectionStart, this.val$endSelectionEnd);
                }
            }
        }
    }

    /* renamed from: android.transition.ChangeText.2 */
    class AnonymousClass2 implements AnimatorUpdateListener {
        final /* synthetic */ int val$startColor;
        final /* synthetic */ TextView val$view;

        AnonymousClass2(TextView val$view, int val$startColor) {
            this.val$view = val$view;
            this.val$startColor = val$startColor;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            this.val$view.setTextColor((((((Integer) animation.getAnimatedValue()).intValue() << 24) | (this.val$startColor & Spanned.SPAN_PRIORITY)) | (this.val$startColor & MotionEvent.ACTION_POINTER_INDEX_MASK)) | (this.val$startColor & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE));
        }
    }

    /* renamed from: android.transition.ChangeText.3 */
    class AnonymousClass3 extends AnimatorListenerAdapter {
        final /* synthetic */ int val$endColor;
        final /* synthetic */ int val$endSelectionEnd;
        final /* synthetic */ int val$endSelectionStart;
        final /* synthetic */ CharSequence val$endText;
        final /* synthetic */ CharSequence val$startText;
        final /* synthetic */ TextView val$view;

        AnonymousClass3(CharSequence val$startText, TextView val$view, CharSequence val$endText, int val$endSelectionStart, int val$endSelectionEnd, int val$endColor) {
            this.val$startText = val$startText;
            this.val$view = val$view;
            this.val$endText = val$endText;
            this.val$endSelectionStart = val$endSelectionStart;
            this.val$endSelectionEnd = val$endSelectionEnd;
            this.val$endColor = val$endColor;
        }

        public void onAnimationEnd(Animator animation) {
            if (this.val$startText.equals(this.val$view.getText())) {
                this.val$view.setText(this.val$endText);
                if (this.val$view instanceof EditText) {
                    ChangeText.this.setSelection((EditText) this.val$view, this.val$endSelectionStart, this.val$endSelectionEnd);
                }
            }
            this.val$view.setTextColor(this.val$endColor);
        }
    }

    /* renamed from: android.transition.ChangeText.4 */
    class AnonymousClass4 implements AnimatorUpdateListener {
        final /* synthetic */ int val$endColor;
        final /* synthetic */ TextView val$view;

        AnonymousClass4(TextView val$view, int val$endColor) {
            this.val$view = val$view;
            this.val$endColor = val$endColor;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            this.val$view.setTextColor((((((Integer) animation.getAnimatedValue()).intValue() << 24) | (Color.red(this.val$endColor) << 16)) | (Color.green(this.val$endColor) << 8)) | Color.red(this.val$endColor));
        }
    }

    /* renamed from: android.transition.ChangeText.5 */
    class AnonymousClass5 extends AnimatorListenerAdapter {
        final /* synthetic */ int val$endColor;
        final /* synthetic */ TextView val$view;

        AnonymousClass5(TextView val$view, int val$endColor) {
            this.val$view = val$view;
            this.val$endColor = val$endColor;
        }

        public void onAnimationCancel(Animator animation) {
            this.val$view.setTextColor(this.val$endColor);
        }
    }

    /* renamed from: android.transition.ChangeText.6 */
    class AnonymousClass6 extends TransitionListenerAdapter {
        int mPausedColor;
        final /* synthetic */ int val$endColor;
        final /* synthetic */ int val$endSelectionEnd;
        final /* synthetic */ int val$endSelectionStart;
        final /* synthetic */ CharSequence val$endText;
        final /* synthetic */ int val$startSelectionEnd;
        final /* synthetic */ int val$startSelectionStart;
        final /* synthetic */ CharSequence val$startText;
        final /* synthetic */ TextView val$view;

        AnonymousClass6(TextView val$view, CharSequence val$endText, int val$endSelectionStart, int val$endSelectionEnd, int val$endColor, CharSequence val$startText, int val$startSelectionStart, int val$startSelectionEnd) {
            this.val$view = val$view;
            this.val$endText = val$endText;
            this.val$endSelectionStart = val$endSelectionStart;
            this.val$endSelectionEnd = val$endSelectionEnd;
            this.val$endColor = val$endColor;
            this.val$startText = val$startText;
            this.val$startSelectionStart = val$startSelectionStart;
            this.val$startSelectionEnd = val$startSelectionEnd;
            this.mPausedColor = ChangeText.CHANGE_BEHAVIOR_KEEP;
        }

        public void onTransitionPause(Transition transition) {
            if (ChangeText.this.mChangeBehavior != ChangeText.CHANGE_BEHAVIOR_IN) {
                this.val$view.setText(this.val$endText);
                if (this.val$view instanceof EditText) {
                    ChangeText.this.setSelection((EditText) this.val$view, this.val$endSelectionStart, this.val$endSelectionEnd);
                }
            }
            if (ChangeText.this.mChangeBehavior > 0) {
                this.mPausedColor = this.val$view.getCurrentTextColor();
                this.val$view.setTextColor(this.val$endColor);
            }
        }

        public void onTransitionResume(Transition transition) {
            if (ChangeText.this.mChangeBehavior != ChangeText.CHANGE_BEHAVIOR_IN) {
                this.val$view.setText(this.val$startText);
                if (this.val$view instanceof EditText) {
                    ChangeText.this.setSelection((EditText) this.val$view, this.val$startSelectionStart, this.val$startSelectionEnd);
                }
            }
            if (ChangeText.this.mChangeBehavior > 0) {
                this.val$view.setTextColor(this.mPausedColor);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.ChangeText.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.ChangeText.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeText.<clinit>():void");
    }

    public ChangeText() {
        this.mChangeBehavior = CHANGE_BEHAVIOR_KEEP;
    }

    public ChangeText setChangeBehavior(int changeBehavior) {
        if (changeBehavior >= 0 && changeBehavior <= CHANGE_BEHAVIOR_OUT_IN) {
            this.mChangeBehavior = changeBehavior;
        }
        return this;
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public int getChangeBehavior() {
        return this.mChangeBehavior;
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof TextView) {
            TextView textview = transitionValues.view;
            transitionValues.values.put(PROPNAME_TEXT, textview.getText());
            if (textview instanceof EditText) {
                transitionValues.values.put(PROPNAME_TEXT_SELECTION_START, Integer.valueOf(textview.getSelectionStart()));
                transitionValues.values.put(PROPNAME_TEXT_SELECTION_END, Integer.valueOf(textview.getSelectionEnd()));
            }
            if (this.mChangeBehavior > 0) {
                transitionValues.values.put(PROPNAME_TEXT_COLOR, Integer.valueOf(textview.getCurrentTextColor()));
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null || !(startValues.view instanceof TextView) || !(endValues.view instanceof TextView)) {
            return null;
        }
        CharSequence startText;
        CharSequence endText;
        int startSelectionStart;
        int startSelectionEnd;
        int endSelectionStart;
        int endSelectionEnd;
        TextView view = endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        if (startVals.get(PROPNAME_TEXT) != null) {
            startText = (CharSequence) startVals.get(PROPNAME_TEXT);
        } else {
            startText = "";
        }
        if (endVals.get(PROPNAME_TEXT) != null) {
            endText = (CharSequence) endVals.get(PROPNAME_TEXT);
        } else {
            endText = "";
        }
        if (view instanceof EditText) {
            if (startVals.get(PROPNAME_TEXT_SELECTION_START) != null) {
                startSelectionStart = ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_START)).intValue();
            } else {
                startSelectionStart = -1;
            }
            if (startVals.get(PROPNAME_TEXT_SELECTION_END) != null) {
                startSelectionEnd = ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_END)).intValue();
            } else {
                startSelectionEnd = startSelectionStart;
            }
            if (endVals.get(PROPNAME_TEXT_SELECTION_START) != null) {
                endSelectionStart = ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_START)).intValue();
            } else {
                endSelectionStart = -1;
            }
            if (endVals.get(PROPNAME_TEXT_SELECTION_END) != null) {
                endSelectionEnd = ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_END)).intValue();
            } else {
                endSelectionEnd = endSelectionStart;
            }
        } else {
            endSelectionEnd = -1;
            endSelectionStart = -1;
            startSelectionEnd = -1;
            startSelectionStart = -1;
        }
        if (startText.equals(endText)) {
            return null;
        }
        int endColor;
        Animator anim;
        if (this.mChangeBehavior != CHANGE_BEHAVIOR_IN) {
            view.setText(startText);
            if (view instanceof EditText) {
                setSelection((EditText) view, startSelectionStart, startSelectionEnd);
            }
        }
        if (this.mChangeBehavior == 0) {
            endColor = CHANGE_BEHAVIOR_KEEP;
            float[] fArr = new float[CHANGE_BEHAVIOR_IN];
            fArr[CHANGE_BEHAVIOR_KEEP] = 0.0f;
            fArr[CHANGE_BEHAVIOR_OUT] = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            anim = ValueAnimator.ofFloat(fArr);
            anim.addListener(new AnonymousClass1(startText, view, endText, endSelectionStart, endSelectionEnd));
        } else {
            int startColor = ((Integer) startVals.get(PROPNAME_TEXT_COLOR)).intValue();
            endColor = ((Integer) endVals.get(PROPNAME_TEXT_COLOR)).intValue();
            Animator animator = null;
            Animator animator2 = null;
            if (this.mChangeBehavior == CHANGE_BEHAVIOR_OUT_IN || this.mChangeBehavior == CHANGE_BEHAVIOR_OUT) {
                animator = ValueAnimator.ofInt(new int[]{MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, CHANGE_BEHAVIOR_KEEP});
                animator.addUpdateListener(new AnonymousClass2(view, startColor));
                animator.addListener(new AnonymousClass3(startText, view, endText, endSelectionStart, endSelectionEnd, endColor));
            }
            if (this.mChangeBehavior == CHANGE_BEHAVIOR_OUT_IN || this.mChangeBehavior == CHANGE_BEHAVIOR_IN) {
                animator2 = ValueAnimator.ofInt(new int[]{CHANGE_BEHAVIOR_KEEP, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE});
                animator2.addUpdateListener(new AnonymousClass4(view, endColor));
                animator2.addListener(new AnonymousClass5(view, endColor));
            }
            if (animator != null && animator2 != null) {
                anim = new AnimatorSet();
                AnimatorSet animatorSet = (AnimatorSet) anim;
                Animator[] animatorArr = new Animator[CHANGE_BEHAVIOR_IN];
                animatorArr[CHANGE_BEHAVIOR_KEEP] = animator;
                animatorArr[CHANGE_BEHAVIOR_OUT] = animator2;
                animatorSet.playSequentially(animatorArr);
            } else if (animator != null) {
                anim = animator;
            } else {
                anim = animator2;
            }
        }
        addListener(new AnonymousClass6(view, endText, endSelectionStart, endSelectionEnd, endColor, startText, startSelectionStart, startSelectionEnd));
        return anim;
    }

    private void setSelection(EditText editText, int start, int end) {
        if (start >= 0 && end >= 0) {
            editText.setSelection(start, end);
        }
    }
}
