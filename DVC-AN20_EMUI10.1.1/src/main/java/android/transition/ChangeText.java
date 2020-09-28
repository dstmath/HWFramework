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
    private int mChangeBehavior = 0;

    public ChangeText setChangeBehavior(int changeBehavior) {
        if (changeBehavior >= 0 && changeBehavior <= 3) {
            this.mChangeBehavior = changeBehavior;
        }
        return this;
    }

    @Override // android.transition.Transition
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

    @Override // android.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override // android.transition.Transition
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01a3  */
    @Override // android.transition.Transition
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        final int endSelectionEnd;
        final int endSelectionStart;
        int startSelectionStart;
        int startSelectionEnd;
        final int startSelectionEnd2;
        final int startSelectionStart2;
        final int endColor;
        Animator anim;
        int endColor2;
        char c;
        int i;
        ValueAnimator outAnim;
        final int endColor3;
        ValueAnimator inAnim;
        int i2;
        if (startValues == null || endValues == null || !(startValues.view instanceof TextView) || !(endValues.view instanceof TextView)) {
            return null;
        }
        final TextView view = (TextView) endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        final CharSequence endText = "";
        final CharSequence startText = startVals.get(PROPNAME_TEXT) != null ? (CharSequence) startVals.get(PROPNAME_TEXT) : endText;
        if (endVals.get(PROPNAME_TEXT) != null) {
            endText = (CharSequence) endVals.get(PROPNAME_TEXT);
        }
        int endSelectionStart2 = -1;
        if (view instanceof EditText) {
            int startSelectionStart3 = startVals.get(PROPNAME_TEXT_SELECTION_START) != null ? ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_START)).intValue() : -1;
            int startSelectionEnd3 = startVals.get(PROPNAME_TEXT_SELECTION_END) != null ? ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_END)).intValue() : startSelectionStart3;
            if (endVals.get(PROPNAME_TEXT_SELECTION_START) != null) {
                endSelectionStart2 = ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_START)).intValue();
            }
            endSelectionStart = endSelectionStart2;
            endSelectionEnd = endVals.get(PROPNAME_TEXT_SELECTION_END) != null ? ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_END)).intValue() : endSelectionStart2;
            startSelectionStart = startSelectionStart3;
            startSelectionEnd = startSelectionEnd3;
        } else {
            endSelectionEnd = -1;
            startSelectionStart = -1;
            endSelectionStart = -1;
            startSelectionEnd = -1;
        }
        if (startText.equals(endText)) {
            return null;
        }
        if (this.mChangeBehavior != 2) {
            view.setText(startText);
            if (view instanceof EditText) {
                setSelection((EditText) view, startSelectionStart, startSelectionEnd);
            }
        }
        if (this.mChangeBehavior == 0) {
            endColor = 0;
            startSelectionStart2 = startSelectionStart;
            anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            startSelectionEnd2 = startSelectionEnd;
            anim.addListener(new AnimatorListenerAdapter() {
                /* class android.transition.ChangeText.AnonymousClass1 */

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationEnd(Animator animation) {
                    if (startText.equals(view.getText())) {
                        view.setText(endText);
                        TextView textView = view;
                        if (textView instanceof EditText) {
                            ChangeText.this.setSelection((EditText) textView, endSelectionStart, endSelectionEnd);
                        }
                    }
                }
            });
        } else {
            startSelectionEnd2 = startSelectionEnd;
            startSelectionStart2 = startSelectionStart;
            final int startColor = ((Integer) startVals.get(PROPNAME_TEXT_COLOR)).intValue();
            final int endColor4 = ((Integer) endVals.get(PROPNAME_TEXT_COLOR)).intValue();
            int i3 = this.mChangeBehavior;
            if (i3 == 3 || i3 == 1) {
                ValueAnimator outAnim2 = ValueAnimator.ofInt(Color.alpha(startColor), 0);
                outAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class android.transition.ChangeText.AnonymousClass2 */

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (startColor & 16777215));
                    }
                });
                outAnim = outAnim2;
                c = 1;
                i = 3;
                endColor2 = endColor4;
                outAnim.addListener(new AnimatorListenerAdapter() {
                    /* class android.transition.ChangeText.AnonymousClass3 */

                    @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                    public void onAnimationEnd(Animator animation) {
                        if (startText.equals(view.getText())) {
                            view.setText(endText);
                            TextView textView = view;
                            if (textView instanceof EditText) {
                                ChangeText.this.setSelection((EditText) textView, endSelectionStart, endSelectionEnd);
                            }
                        }
                        view.setTextColor(endColor4);
                    }
                });
            } else {
                outAnim = null;
                c = 1;
                endColor2 = endColor4;
                i = 3;
            }
            int i4 = this.mChangeBehavior;
            if (i4 != i) {
                i2 = 2;
                if (i4 != 2) {
                    inAnim = null;
                    endColor3 = endColor2;
                    if (outAnim == null && inAnim != null) {
                        Animator anim2 = new AnimatorSet();
                        Animator[] animatorArr = new Animator[2];
                        animatorArr[0] = outAnim;
                        animatorArr[c] = inAnim;
                        ((AnimatorSet) anim2).playSequentially(animatorArr);
                        endColor = endColor3;
                        anim = anim2;
                    } else if (outAnim == null) {
                        endColor = endColor3;
                        anim = outAnim;
                    } else {
                        endColor = endColor3;
                        anim = inAnim;
                    }
                }
            } else {
                i2 = 2;
            }
            int[] iArr = new int[i2];
            iArr[0] = 0;
            iArr[c] = Color.alpha(endColor2);
            inAnim = ValueAnimator.ofInt(iArr);
            endColor3 = endColor2;
            inAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class android.transition.ChangeText.AnonymousClass4 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (endColor3 & 16777215));
                }
            });
            inAnim.addListener(new AnimatorListenerAdapter() {
                /* class android.transition.ChangeText.AnonymousClass5 */

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationCancel(Animator animation) {
                    view.setTextColor(endColor3);
                }
            });
            if (outAnim == null) {
            }
            if (outAnim == null) {
            }
        }
        addListener(new TransitionListenerAdapter() {
            /* class android.transition.ChangeText.AnonymousClass6 */
            int mPausedColor = 0;

            @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
            public void onTransitionPause(Transition transition) {
                if (ChangeText.this.mChangeBehavior != 2) {
                    view.setText(endText);
                    TextView textView = view;
                    if (textView instanceof EditText) {
                        ChangeText.this.setSelection((EditText) textView, endSelectionStart, endSelectionEnd);
                    }
                }
                if (ChangeText.this.mChangeBehavior > 0) {
                    this.mPausedColor = view.getCurrentTextColor();
                    view.setTextColor(endColor);
                }
            }

            @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
            public void onTransitionResume(Transition transition) {
                if (ChangeText.this.mChangeBehavior != 2) {
                    view.setText(startText);
                    TextView textView = view;
                    if (textView instanceof EditText) {
                        ChangeText.this.setSelection((EditText) textView, startSelectionStart2, startSelectionEnd2);
                    }
                }
                if (ChangeText.this.mChangeBehavior > 0) {
                    view.setTextColor(this.mPausedColor);
                }
            }

            @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
            }
        });
        return anim;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSelection(EditText editText, int start, int end) {
        if (start >= 0 && end >= 0) {
            editText.setSelection(start, end);
        }
    }
}
