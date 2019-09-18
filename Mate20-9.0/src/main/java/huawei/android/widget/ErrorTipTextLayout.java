package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import huawei.android.graphics.drawable.HwAnimatedGradientDrawable;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class ErrorTipTextLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 200;
    private ClickEffectEntry mClickEffectEntry;
    /* access modifiers changed from: private */
    public EditText mEditText;
    private int mEditTextBgResId;
    private boolean mErrorEnabled;
    private int mErrorLiearEditBg;
    private int mErrorResBgId;
    private int mErrorTextAppearance;
    /* access modifiers changed from: private */
    public TextView mErrorView;
    private int mLinearEditBgResId;
    private ResLoader mResLoader;
    private ShapeMode mShapeMode;
    /* access modifiers changed from: private */
    public boolean mSpaceOccupied;

    public enum ShapeMode {
        Bubble,
        Linear
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
            boolean errorEnabled = a.getBoolean(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_errorEnabled"), true);
            this.mSpaceOccupied = a.getBoolean(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_spaceOccupied"), false);
            this.mShapeMode = ShapeMode.values()[a.getInt(ResLoaderUtil.getStyleableId(context, "ErrorTipTextLayout_shape_mode"), ShapeMode.Bubble.ordinal())];
            a.recycle();
            setErrorEnabled(errorEnabled);
        }
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setAccessibilityDelegate(new TextInputAccessibilityDelegate());
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
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
                this.mErrorView.setPaddingRelative(this.mEditText.getPaddingStart(), ResLoaderUtil.getDimensionPixelSize(this.mContext, "errortiptextlayout_top_padding"), this.mEditText.getPaddingEnd(), 0);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    private LinearLayout.LayoutParams updateEditTextMargin(ViewGroup.LayoutParams lp) {
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
                this.mErrorView.setTextAlignment(5);
                this.mErrorView.setTextDirection(5);
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
                this.mEditText.setBackgroundResource(this.mErrorLiearEditBg);
            }
        } else {
            if (this.mErrorView.getVisibility() == 0) {
                this.mErrorView.animate().alpha(0.0f).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        int i;
                        TextView access$100 = ErrorTipTextLayout.this.mErrorView;
                        if (ErrorTipTextLayout.this.mSpaceOccupied) {
                            i = 4;
                        } else {
                            i = 8;
                        }
                        access$100.setVisibility(i);
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

    public CharSequence getError() {
        if (!this.mErrorEnabled || this.mErrorView == null || this.mErrorView.getVisibility() != 0) {
            return null;
        }
        return this.mErrorView.getText();
    }

    public void resetBackground() {
        Drawable bg = this.mEditText.getBackground();
        HwAnimatedGradientDrawable drawable = HwWidgetUtils.getHwAnimatedGradientDrawable(getContext(), this.mClickEffectEntry);
        if (bg != null) {
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable, bg});
            layerDrawable.setPaddingMode(1);
            this.mEditText.setBackground(layerDrawable);
            return;
        }
        this.mEditText.setBackground(drawable);
    }
}
