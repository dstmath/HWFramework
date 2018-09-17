package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.util.LogException;
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
    private static final String[] sTransitionProperties = new String[]{PROPNAME_TEXT, PROPNAME_TEXT_SELECTION_START, PROPNAME_TEXT_SELECTION_END};
    private int mChangeBehavior = 0;

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
        if (startValues == null || endValues == null || ((startValues.view instanceof TextView) ^ 1) != 0 || ((endValues.view instanceof TextView) ^ 1) != 0) {
            return null;
        }
        CharSequence startText;
        CharSequence endText;
        int startSelectionStart;
        int startSelectionEnd;
        int endSelectionStart;
        int endSelectionEnd;
        final TextView view = endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        if (startVals.get(PROPNAME_TEXT) != null) {
            startText = (CharSequence) startVals.get(PROPNAME_TEXT);
        } else {
            startText = LogException.NO_VALUE;
        }
        if (endVals.get(PROPNAME_TEXT) != null) {
            endText = (CharSequence) endVals.get(PROPNAME_TEXT);
        } else {
            endText = LogException.NO_VALUE;
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
        if (this.mChangeBehavior != 2) {
            view.setText(startText);
            if (view instanceof EditText) {
                setSelection((EditText) view, startSelectionStart, startSelectionEnd);
            }
        }
        if (this.mChangeBehavior == 0) {
            endColor = 0;
            anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (startText.equals(view.getText())) {
                        view.setText(endText);
                        if (view instanceof EditText) {
                            ChangeText.this.setSelection((EditText) view, endSelectionStart, endSelectionEnd);
                        }
                    }
                }
            });
        } else {
            int startColor = ((Integer) startVals.get(PROPNAME_TEXT_COLOR)).intValue();
            endColor = ((Integer) endVals.get(PROPNAME_TEXT_COLOR)).intValue();
            Animator outAnim = null;
            Animator inAnim = null;
            if (this.mChangeBehavior == 3 || this.mChangeBehavior == 1) {
                outAnim = ValueAnimator.ofInt(new int[]{Color.alpha(startColor), 0});
                final int i = startColor;
                outAnim.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (i & 16777215));
                    }
                });
                outAnim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        if (startText.equals(view.getText())) {
                            view.setText(endText);
                            if (view instanceof EditText) {
                                ChangeText.this.setSelection((EditText) view, endSelectionStart, endSelectionEnd);
                            }
                        }
                        view.setTextColor(endColor);
                    }
                });
            }
            if (this.mChangeBehavior == 3 || this.mChangeBehavior == 2) {
                inAnim = ValueAnimator.ofInt(new int[]{0, Color.alpha(endColor)});
                inAnim.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (endColor & 16777215));
                    }
                });
                inAnim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationCancel(Animator animation) {
                        view.setTextColor(endColor);
                    }
                });
            }
            if (outAnim != null && inAnim != null) {
                anim = new AnimatorSet();
                ((AnimatorSet) anim).playSequentially(new Animator[]{outAnim, inAnim});
            } else if (outAnim != null) {
                anim = outAnim;
            } else {
                anim = inAnim;
            }
        }
        final TextView textView = view;
        final CharSequence charSequence = endText;
        final int i2 = endSelectionStart;
        final int i3 = endSelectionEnd;
        final int i4 = endColor;
        final CharSequence charSequence2 = startText;
        addListener(new TransitionListenerAdapter() {
            int mPausedColor = 0;

            public void onTransitionPause(Transition transition) {
                if (ChangeText.this.mChangeBehavior != 2) {
                    textView.setText(charSequence);
                    if (textView instanceof EditText) {
                        ChangeText.this.setSelection((EditText) textView, i2, i3);
                    }
                }
                if (ChangeText.this.mChangeBehavior > 0) {
                    this.mPausedColor = textView.getCurrentTextColor();
                    textView.setTextColor(i4);
                }
            }

            public void onTransitionResume(Transition transition) {
                if (ChangeText.this.mChangeBehavior != 2) {
                    textView.setText(charSequence2);
                    if (textView instanceof EditText) {
                        ChangeText.this.setSelection((EditText) textView, startSelectionStart, startSelectionEnd);
                    }
                }
                if (ChangeText.this.mChangeBehavior > 0) {
                    textView.setTextColor(this.mPausedColor);
                }
            }

            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
            }
        });
        return anim;
    }

    private void setSelection(EditText editText, int start, int end) {
        if (start >= 0 && end >= 0) {
            editText.setSelection(start, end);
        }
    }
}
