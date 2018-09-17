package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidhwext.R;

public class ErrorTipTextLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 200;
    private boolean isHwDarkTheme;
    private EditText mEditText;
    private int mEditTextBgResId;
    private boolean mErrorEnabled;
    private int mErrorResBgId;
    private int mErrorTextAppearance;
    private TextView mErrorView;
    private int mLinearEditBgResId;
    private ShapeMode mShapeMode;
    private boolean mSpaceOccupied;

    public enum ShapeMode {
        Bubble,
        Linear
    }

    private class TextInputAccessibilityDelegate extends AccessibilityDelegate {
        /* synthetic */ TextInputAccessibilityDelegate(ErrorTipTextLayout this$0, TextInputAccessibilityDelegate -this1) {
            this();
        }

        private TextInputAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setClassName(ErrorTipTextLayout.class.getSimpleName());
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            CharSequence error;
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(ErrorTipTextLayout.class.getSimpleName());
            if (ErrorTipTextLayout.this.mEditText != null) {
                info.setLabelFor(ErrorTipTextLayout.this.mEditText);
            }
            if (ErrorTipTextLayout.this.mErrorView != null) {
                error = ErrorTipTextLayout.this.mErrorView.getText();
            } else {
                error = null;
            }
            if (!TextUtils.isEmpty(error)) {
                info.setContentInvalid(true);
                info.setError(error);
            }
        }
    }

    public ErrorTipTextLayout(Context context) {
        this(context, null);
    }

    public ErrorTipTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ErrorTipTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        this.mErrorResBgId = -1;
        this.mEditTextBgResId = -1;
        this.mLinearEditBgResId = -1;
        setOrientation(1);
        setAddStatesFromChildren(true);
        this.isHwDarkTheme = HwWidgetFactory.isHwDarkTheme(context);
        if (this.isHwDarkTheme) {
            this.mEditTextBgResId = 33751209;
            this.mLinearEditBgResId = 33751212;
            this.mErrorResBgId = 33751719;
        } else {
            this.mEditTextBgResId = 33751208;
            this.mLinearEditBgResId = 33751211;
            this.mErrorResBgId = 33751718;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ErrorTipTextLayout, defStyleAttr, 33947988);
        this.mErrorTextAppearance = a.getResourceId(1, 0);
        boolean errorEnabled = a.getBoolean(0, true);
        this.mSpaceOccupied = a.getBoolean(2, false);
        this.mShapeMode = ShapeMode.values()[a.getInt(3, ShapeMode.Bubble.ordinal())];
        a.recycle();
        setErrorEnabled(errorEnabled);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setAccessibilityDelegate(new TextInputAccessibilityDelegate(this, null));
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
            super.addView(child, 0, updateEditTextMargin(params));
            return;
        }
        super.addView(child, index, params);
    }

    private void setEditText(EditText editText) {
        if (this.mEditText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }
        this.mEditText = editText;
        this.mEditText.setImeOptions(33554432 | this.mEditText.getImeOptions());
        if (this.mShapeMode == ShapeMode.Bubble) {
            this.mEditText.setBackgroundResource(this.mEditTextBgResId);
        } else if (this.mShapeMode == ShapeMode.Linear) {
            this.mEditText.setBackgroundResource(this.mLinearEditBgResId);
        }
        if (this.mErrorView != null) {
            this.mErrorView.setPaddingRelative(this.mEditText.getPaddingStart(), 0, this.mEditText.getPaddingEnd(), 0);
        }
    }

    private LinearLayout.LayoutParams updateEditTextMargin(LayoutParams lp) {
        if (lp instanceof LinearLayout.LayoutParams) {
            return (LinearLayout.LayoutParams) lp;
        }
        return new LinearLayout.LayoutParams(lp);
    }

    public EditText getEditText() {
        return this.mEditText;
    }

    public void setHint(CharSequence hint) {
        this.mEditText.setHint(hint);
        sendAccessibilityEvent(2048);
    }

    public CharSequence getHint() {
        return this.mEditText.getHint();
    }

    public void setErrorEnabled(boolean enabled) {
        if (this.mErrorEnabled != enabled) {
            if (this.mErrorView != null) {
                this.mErrorView.animate().cancel();
            }
            if (enabled) {
                this.mErrorView = new TextView(getContext());
                this.mErrorView.setTextAppearance(getContext(), this.mErrorTextAppearance);
                this.mErrorView.setVisibility(this.mSpaceOccupied ? 4 : 8);
                addView(this.mErrorView);
                if (this.mEditText != null) {
                    this.mErrorView.setPaddingRelative(this.mEditText.getPaddingStart(), 0, this.mEditText.getPaddingEnd(), 0);
                }
            } else {
                removeView(this.mErrorView);
                this.mErrorView = null;
            }
            this.mErrorEnabled = enabled;
        }
    }

    public boolean isErrorEnabled() {
        return this.mErrorEnabled;
    }

    public void setError(CharSequence error) {
        if (!this.mErrorEnabled) {
            if (!TextUtils.isEmpty(error)) {
                setErrorEnabled(true);
            } else {
                return;
            }
        }
        if (!TextUtils.isEmpty(error)) {
            this.mErrorView.setAlpha(0.0f);
            this.mErrorView.setText(error);
            this.mErrorView.animate().alpha(1.0f).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    ErrorTipTextLayout.this.mErrorView.setVisibility(0);
                }
            }).start();
            if (this.mShapeMode == ShapeMode.Bubble) {
                this.mEditText.setBackgroundResource(this.mErrorResBgId);
            } else if (this.mShapeMode == ShapeMode.Linear) {
                this.mEditText.setBackgroundTintList(ColorStateList.valueOf(this.mErrorView.getCurrentTextColor()));
            }
        } else if (this.mErrorView.getVisibility() == 0) {
            this.mErrorView.animate().alpha(0.0f).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    int i;
                    TextView -get1 = ErrorTipTextLayout.this.mErrorView;
                    if (ErrorTipTextLayout.this.mSpaceOccupied) {
                        i = 4;
                    } else {
                        i = 8;
                    }
                    -get1.setVisibility(i);
                }
            }).start();
            if (this.mShapeMode == ShapeMode.Bubble) {
                this.mEditText.setBackgroundResource(this.mEditTextBgResId);
            } else if (this.mShapeMode == ShapeMode.Linear) {
                this.mEditText.setBackgroundTintList(null);
            }
        }
        sendAccessibilityEvent(2048);
    }

    public CharSequence getError() {
        if (this.mErrorEnabled && this.mErrorView != null && this.mErrorView.getVisibility() == 0) {
            return this.mErrorView.getText();
        }
        return null;
    }
}
