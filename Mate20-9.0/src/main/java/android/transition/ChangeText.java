package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
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
    private static final String[] sTransitionProperties = {PROPNAME_TEXT, PROPNAME_TEXT_SELECTION_START, PROPNAME_TEXT_SELECTION_END};
    /* access modifiers changed from: private */
    public int mChangeBehavior = 0;

    public ChangeText setChangeBehavior(int changeBehavior) {
        if (changeBehavior >= 0 && changeBehavior <= 3) {
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
            TextView textview = (TextView) transitionValues.view;
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

    /* JADX WARNING: Removed duplicated region for block: B:62:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01b2  */
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        int endSelectionEnd;
        int endSelectionStart;
        int startSelectionEnd;
        int startSelectionStart;
        int startSelectionStart2;
        int startSelectionEnd2;
        int endColor;
        Animator anim;
        int endColor2;
        char c;
        int i;
        ValueAnimator outAnim;
        ValueAnimator inAnim;
        final int endColor3;
        int i2;
        TransitionValues transitionValues = startValues;
        TransitionValues transitionValues2 = endValues;
        if (transitionValues == null || transitionValues2 == null || !(transitionValues.view instanceof TextView) || !(transitionValues2.view instanceof TextView)) {
            return null;
        }
        final TextView view = (TextView) transitionValues2.view;
        Map<String, Object> startVals = transitionValues.values;
        Map<String, Object> endVals = transitionValues2.values;
        CharSequence startText = startVals.get(PROPNAME_TEXT) != null ? (CharSequence) startVals.get(PROPNAME_TEXT) : "";
        CharSequence endText = endVals.get(PROPNAME_TEXT) != null ? (CharSequence) endVals.get(PROPNAME_TEXT) : "";
        int endSelectionStart2 = -1;
        if (view instanceof EditText) {
            startSelectionStart = startVals.get(PROPNAME_TEXT_SELECTION_START) != null ? ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_START)).intValue() : -1;
            int startSelectionEnd3 = startVals.get(PROPNAME_TEXT_SELECTION_END) != null ? ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_END)).intValue() : startSelectionStart;
            if (endVals.get(PROPNAME_TEXT_SELECTION_START) != null) {
                endSelectionStart2 = ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_START)).intValue();
            }
            endSelectionStart = endSelectionStart2;
            startSelectionEnd = startSelectionEnd3;
            endSelectionEnd = endVals.get(PROPNAME_TEXT_SELECTION_END) != null ? ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_END)).intValue() : endSelectionStart2;
        } else {
            startSelectionStart = -1;
            endSelectionEnd = -1;
            endSelectionStart = -1;
            startSelectionEnd = -1;
        }
        int startSelectionStart3 = startSelectionStart;
        if (startText.equals(endText) == 0) {
            if (this.mChangeBehavior != 2) {
                view.setText(startText);
                if (view instanceof EditText) {
                    setSelection((EditText) view, startSelectionStart3, startSelectionEnd);
                }
            }
            if (this.mChangeBehavior == 0) {
                endColor = 0;
                final CharSequence charSequence = startText;
                final TextView textView = view;
                startSelectionEnd2 = startSelectionEnd;
                AnonymousClass1 r7 = r0;
                final CharSequence charSequence2 = endText;
                Animator anim2 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                final int i3 = endSelectionStart;
                startSelectionStart2 = startSelectionStart3;
                final int startSelectionStart4 = endSelectionEnd;
                AnonymousClass1 r0 = new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        if (charSequence.equals(textView.getText())) {
                            textView.setText(charSequence2);
                            if (textView instanceof EditText) {
                                ChangeText.this.setSelection((EditText) textView, i3, startSelectionStart4);
                            }
                        }
                    }
                };
                anim2.addListener(r7);
                anim = anim2;
                Map<String, Object> map = startVals;
            } else {
                startSelectionStart2 = startSelectionStart3;
                startSelectionEnd2 = startSelectionEnd;
                final int startColor = ((Integer) startVals.get(PROPNAME_TEXT_COLOR)).intValue();
                final int endColor4 = ((Integer) endVals.get(PROPNAME_TEXT_COLOR)).intValue();
                if (this.mChangeBehavior == 3 || this.mChangeBehavior == 1) {
                    ValueAnimator outAnim2 = ValueAnimator.ofInt(new int[]{Color.alpha(startColor), 0});
                    outAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (startColor & 16777215));
                        }
                    });
                    int i4 = startColor;
                    AnonymousClass3 r11 = r0;
                    final CharSequence charSequence3 = startText;
                    outAnim = outAnim2;
                    final TextView textView2 = view;
                    c = 1;
                    final CharSequence charSequence4 = endText;
                    Map<String, Object> map2 = startVals;
                    final int i5 = endSelectionStart;
                    i = 3;
                    final int i6 = endSelectionEnd;
                    endColor2 = endColor4;
                    AnonymousClass3 r02 = new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            if (charSequence3.equals(textView2.getText())) {
                                textView2.setText(charSequence4);
                                if (textView2 instanceof EditText) {
                                    ChangeText.this.setSelection((EditText) textView2, i5, i6);
                                }
                            }
                            textView2.setTextColor(endColor4);
                        }
                    };
                    outAnim.addListener(r11);
                } else {
                    outAnim = null;
                    c = 1;
                    endColor2 = endColor4;
                    int i7 = startColor;
                    Map<String, Object> map3 = startVals;
                    i = 3;
                }
                if (this.mChangeBehavior != i) {
                    i2 = 2;
                    if (this.mChangeBehavior != 2) {
                        inAnim = null;
                        endColor3 = endColor2;
                        if (outAnim == null && inAnim != null) {
                            anim = new AnimatorSet();
                            Animator[] animatorArr = new Animator[2];
                            animatorArr[0] = outAnim;
                            animatorArr[c] = inAnim;
                            anim.playSequentially(animatorArr);
                        } else if (outAnim == null) {
                            anim = outAnim;
                        } else {
                            endColor = endColor3;
                            anim = inAnim;
                        }
                        endColor = endColor3;
                    }
                } else {
                    i2 = 2;
                }
                int[] iArr = new int[i2];
                iArr[0] = 0;
                endColor3 = endColor2;
                iArr[c] = Color.alpha(endColor3);
                ValueAnimator inAnim2 = ValueAnimator.ofInt(iArr);
                inAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (endColor3 & 16777215));
                    }
                });
                inAnim2.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationCancel(Animator animation) {
                        view.setTextColor(endColor3);
                    }
                });
                inAnim = inAnim2;
                if (outAnim == null) {
                }
                if (outAnim == null) {
                }
            }
            Animator anim3 = anim;
            final TextView textView3 = view;
            final CharSequence charSequence5 = endText;
            final int i8 = endSelectionStart;
            final int i9 = endSelectionEnd;
            final int i10 = endColor;
            final CharSequence charSequence6 = startText;
            CharSequence charSequence7 = endText;
            final int i11 = startSelectionStart2;
            CharSequence charSequence8 = startText;
            final int i12 = startSelectionEnd2;
            AnonymousClass6 r03 = new TransitionListenerAdapter() {
                int mPausedColor = 0;

                public void onTransitionPause(Transition transition) {
                    if (ChangeText.this.mChangeBehavior != 2) {
                        textView3.setText(charSequence5);
                        if (textView3 instanceof EditText) {
                            ChangeText.this.setSelection((EditText) textView3, i8, i9);
                        }
                    }
                    if (ChangeText.this.mChangeBehavior > 0) {
                        this.mPausedColor = textView3.getCurrentTextColor();
                        textView3.setTextColor(i10);
                    }
                }

                public void onTransitionResume(Transition transition) {
                    if (ChangeText.this.mChangeBehavior != 2) {
                        textView3.setText(charSequence6);
                        if (textView3 instanceof EditText) {
                            ChangeText.this.setSelection((EditText) textView3, i11, i12);
                        }
                    }
                    if (ChangeText.this.mChangeBehavior > 0) {
                        textView3.setTextColor(this.mPausedColor);
                    }
                }

                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                }
            };
            addListener(r03);
            return anim3;
        }
        int i13 = startSelectionEnd;
        CharSequence charSequence9 = startText;
        Map<String, Object> map4 = startVals;
        CharSequence charSequence10 = endText;
        return null;
    }

    /* access modifiers changed from: private */
    public void setSelection(EditText editText, int start, int end) {
        if (start >= 0 && end >= 0) {
            editText.setSelection(start, end);
        }
    }
}
