package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class ErrorTipTextLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 200;
    private ClickEffectEntry mClickEffectEntry;
    private EditText mEditText;
    private int mEditTextBgResId;
    private int mErrorLiearEditBg;
    private int mErrorResBgId;
    private int mErrorTextAppearance;
    private TextView mErrorView;
    private boolean mIsErrorEnabled;
    private boolean mIsSpaceOccupied;
    private int mLinearEditBgResId;
    private ResLoader mResLoader;
    private ShapeMode mShapeMode;

    public enum ShapeMode {
        Bubble(0),
        Linear(1);
        
        private final int mId;

        private ShapeMode(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
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
        this.mErrorLiearEditBg = -1;
        this.mClickEffectEntry = null;
        setOrientation(1);
        setAddStatesFromChildren(true);
        this.mResLoader = ResLoader.getInstance();
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null) {
            int[] themeAttrs = this.mResLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "ErrorTipTextLayout");
            int defStyleRes = this.mResLoader.getIdentifier(context, "style", "Widget.Emui.ErrorTipTextLayout");
            int defAttrId = this.mResLoader.getIdentifier(context, "attr", "errorTipTextLayoutStyle");
            this.mClickEffectEntry = HwWidgetUtils.getCleckEffectEntry(this.mContext, defAttrId);
            TypedArray a = theme.obtainStyledAttributes(attrs, themeAttrs, defAttrId, defStyleRes);
            this.mEditTextBgResId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_editTextBg"), 0);
            this.mLinearEditBgResId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_linearEditBg"), 0);
            this.mErrorResBgId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_errorResBg"), 0);
            this.mErrorLiearEditBg = a.getResourceId(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_errorLiearEditBg"), 0);
            this.mErrorTextAppearance = a.getResourceId(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_errorTextAppearance"), 0);
            boolean isErrorEnabled = a.getBoolean(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_errorEnabled"), true);
            this.mIsSpaceOccupied = a.getBoolean(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_spaceOccupied"), false);
            this.mShapeMode = ShapeMode.values()[a.getInt(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_shape_mode"), ShapeMode.Bubble.getId())];
            a.recycle();
            setErrorEnabled(isErrorEnabled);
        }
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setAccessibilityDelegate(new TextInputAccessibilityDelegate());
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
            if (this.mShapeMode == ShapeMode.Linear) {
                LinearLayout layout = new LinearLayout(getContext());
                layout.setGravity(16);
                layout.setMinimumHeight(ResLoaderUtil.getDimensionPixelSize(getContext(), "emui_help_text_layout_edit_height"));
                layout.addView(child, updateEditTextMargin(params));
                super.addView(layout, 0);
                return;
            }
            super.addView(child, 0, updateEditTextMargin(params));
            return;
        }
        super.addView(child, index, params);
    }

    private void setEditText(EditText editText) {
        if (this.mEditText == null) {
            this.mEditText = editText;
            this.mEditText.setImeOptions(33554432 | this.mEditText.getImeOptions());
            if (this.mShapeMode == ShapeMode.Bubble) {
                this.mEditText.setBackgroundResource(this.mEditTextBgResId);
            } else if (this.mShapeMode == ShapeMode.Linear) {
                this.mEditText.setBackgroundResource(this.mLinearEditBgResId);
            }
            resetBackground();
            if (this.mErrorView != null) {
                int paddingTop = ResLoaderUtil.getDimensionPixelSize(this.mContext, "errortiptextlayout_top_padding");
                if (this.mShapeMode == ShapeMode.Linear) {
                    paddingTop = 0;
                }
                this.mErrorView.setPaddingRelative(this.mEditText.getPaddingStart(), paddingTop, this.mEditText.getPaddingEnd(), 0);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    private LinearLayout.LayoutParams updateEditTextMargin(ViewGroup.LayoutParams params) {
        return params instanceof LinearLayout.LayoutParams ? (LinearLayout.LayoutParams) params : new LinearLayout.LayoutParams(params);
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

    public void setErrorEnabled(boolean isEnabled) {
        if (this.mIsErrorEnabled != isEnabled) {
            TextView textView = this.mErrorView;
            if (textView != null) {
                textView.animate().cancel();
            }
            if (isEnabled) {
                this.mErrorView = new TextView(getContext());
                this.mErrorView.setTextAppearance(getContext(), this.mErrorTextAppearance);
                this.mErrorView.setVisibility(this.mIsSpaceOccupied ? 4 : 8);
                this.mErrorView.setTextAlignment(5);
                this.mErrorView.setTextDirection(5);
                addView(this.mErrorView);
                EditText editText = this.mEditText;
                if (editText != null) {
                    this.mErrorView.setPaddingRelative(editText.getPaddingStart(), 0, this.mEditText.getPaddingEnd(), 0);
                }
            } else {
                removeView(this.mErrorView);
                this.mErrorView = null;
            }
            this.mIsErrorEnabled = isEnabled;
        }
    }

    public boolean isErrorEnabled() {
        return this.mIsErrorEnabled;
    }

    public void setError(CharSequence error) {
        if (!this.mIsErrorEnabled) {
            if (!TextUtils.isEmpty(error)) {
                setErrorEnabled(true);
            } else {
                return;
            }
        }
        if (!TextUtils.isEmpty(error)) {
            errorNotEmpty(error);
        } else {
            if (this.mErrorView.getVisibility() == 0) {
                this.mErrorView.animate().alpha(0.0f).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                    /* class huawei.android.widget.ErrorTipTextLayout.AnonymousClass1 */

                    public void onAnimationEnd(Animator animation) {
                        ErrorTipTextLayout.this.mErrorView.setVisibility(ErrorTipTextLayout.this.mIsSpaceOccupied ? 4 : 8);
                    }
                }).start();
            }
            if (this.mShapeMode == ShapeMode.Bubble) {
                this.mEditText.setBackgroundResource(this.mEditTextBgResId);
            } else if (this.mShapeMode == ShapeMode.Linear) {
                this.mEditText.setBackgroundResource(this.mLinearEditBgResId);
            }
        }
        resetBackground();
        sendAccessibilityEvent(2048);
    }

    private void errorNotEmpty(CharSequence error) {
        this.mErrorView.setAlpha(0.0f);
        this.mErrorView.setText(error);
        this.mErrorView.animate().alpha(1.0f).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
            /* class huawei.android.widget.ErrorTipTextLayout.AnonymousClass2 */

            public void onAnimationStart(Animator animation) {
                ErrorTipTextLayout.this.mErrorView.setVisibility(0);
            }
        }).start();
        if (this.mShapeMode == ShapeMode.Bubble) {
            this.mEditText.setBackgroundResource(this.mErrorResBgId);
        } else if (this.mShapeMode == ShapeMode.Linear) {
            this.mEditText.setBackgroundResource(this.mErrorLiearEditBg);
        }
    }

    public CharSequence getError() {
        TextView textView;
        if (!this.mIsErrorEnabled || (textView = this.mErrorView) == null || textView.getVisibility() != 0) {
            return null;
        }
        return this.mErrorView.getText();
    }

    private class TextInputAccessibilityDelegate extends View.AccessibilityDelegate {
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
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(ErrorTipTextLayout.class.getSimpleName());
            if (ErrorTipTextLayout.this.mEditText != null) {
                info.setLabelFor(ErrorTipTextLayout.this.mEditText);
            }
            CharSequence error = ErrorTipTextLayout.this.mErrorView != null ? ErrorTipTextLayout.this.mErrorView.getText() : null;
            if (!TextUtils.isEmpty(error)) {
                info.setContentInvalid(true);
                info.setError(error);
            }
        }
    }

    public void resetBackground() {
    }
}
