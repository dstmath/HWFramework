package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.android.widget.utils.EmuiUtils;

public class CounterTextLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 50;
    private static final int DEFAULT_ERROR_COLOR = -382419;
    private static final float SHOW_TIP_LENGTH_RATE = 0.9f;
    private int mAnimShakeId;
    private int mCounterResBgId;
    private int mCounterTextAppearance;
    private TextView mCounterView;
    private int mCounterWarningColor;
    private EditText mEditText;
    private int mEditTextBgResId;
    private int mErrorResBgId;
    private boolean mIsSpaceOccupied;
    private int mLinearEditBgResId;
    private int mMaxLength;
    private ShapeMode mShapeMode;
    private int mStartShownPos;

    public CounterTextLayout(Context context) {
        this(context, null);
    }

    public CounterTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CounterTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        this.mMaxLength = -1;
        this.mStartShownPos = -1;
        this.mCounterResBgId = -1;
        this.mErrorResBgId = -1;
        this.mEditTextBgResId = -1;
        this.mLinearEditBgResId = -1;
        setOrientation(1);
        ResLoader resLoader = ResLoader.getInstance();
        this.mAnimShakeId = resLoader.getIdentifier(context, "anim", "shake");
        this.mCounterWarningColor = EmuiUtils.getAttrColor(context, 16844099, DEFAULT_ERROR_COLOR);
        Resources.Theme theme = resLoader.getTheme(context);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(attrs, resLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "CounterTextLayout"), resLoader.getIdentifier(context, "attr", "counterTextLayoutStyle"), resLoader.getIdentifier(context, "style", "Widget.Emui.CounterTextLayout"));
            this.mCounterTextAppearance = a.getResourceId(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_counterTextAppearance"), 0);
            this.mIsSpaceOccupied = a.getBoolean(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_spaceOccupied"), false);
            this.mShapeMode = ShapeMode.getShapeMode(a.getInt(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_shape_mode"), ShapeMode.Bubble.getModeValue()));
            this.mLinearEditBgResId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_linearEditBg"), 0);
            this.mCounterResBgId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_counterResBg"), 0);
            this.mErrorResBgId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_errorResBg"), 0);
            this.mEditTextBgResId = a.getResourceId(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_editTextBg"), 0);
            a.recycle();
        }
        setupCounterView();
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

    private LinearLayout.LayoutParams updateEditTextMargin(ViewGroup.LayoutParams params) {
        return params instanceof LinearLayout.LayoutParams ? (LinearLayout.LayoutParams) params : new LinearLayout.LayoutParams(params);
    }

    public EditText getEditText() {
        return this.mEditText;
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
            this.mEditText.addTextChangedListener(new ChangeWatcher());
            return;
        }
        throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    public CharSequence getHint() {
        return this.mEditText.getHint();
    }

    public void setHint(CharSequence hint) {
        this.mEditText.setHint(hint);
        sendAccessibilityEvent(2048);
    }

    public int getMaxLength() {
        return this.mMaxLength;
    }

    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
        this.mStartShownPos = (int) (((float) this.mMaxLength) * SHOW_TIP_LENGTH_RATE);
    }

    private void setupCounterView() {
        this.mCounterView = new TextView(getContext());
        this.mCounterView.setTextAppearance(getContext(), this.mCounterTextAppearance);
        this.mCounterView.setVisibility(this.mIsSpaceOccupied ? 4 : 8);
        this.mCounterView.setGravity(17);
        addCounterView();
    }

    private void addCounterView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = 8388693;
        addView(this.mCounterView, params);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        EditText editText;
        super.onLayout(changed, l, t, r, b);
        if (this.mShapeMode == ShapeMode.Bubble && (editText = this.mEditText) != null) {
            ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutParams;
                int bottom = ((((this.mEditText.getTop() + getHeight()) - getPaddingBottom()) - getPaddingTop()) - params.bottomMargin) - params.topMargin;
                EditText editText2 = this.mEditText;
                editText2.layout(editText2.getLeft(), this.mEditText.getTop(), this.mEditText.getRight(), bottom);
            }
            int padding = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");
            this.mCounterView.layout((this.mEditText.getRight() - this.mCounterView.getWidth()) - this.mEditText.getTotalPaddingEnd(), (this.mEditText.getBottom() - this.mCounterView.getHeight()) - padding, this.mEditText.getRight() - this.mEditText.getTotalPaddingEnd(), this.mEditText.getBottom() - padding);
        }
    }

    public CharSequence getError() {
        TextView textView = this.mCounterView;
        if (textView == null || textView.getVisibility() != 0) {
            return null;
        }
        return this.mCounterView.getText();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setError(CharSequence error) {
        if (!TextUtils.isEmpty(error)) {
            this.mCounterView.setAlpha(0.0f);
            this.mCounterView.setText(error);
            this.mCounterView.animate().alpha(1.0f).setDuration(50).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.CounterTextLayout.AnonymousClass1 */

                public void onAnimationStart(Animator animation) {
                    CounterTextLayout.this.mCounterView.setVisibility(0);
                }
            }).start();
            if (this.mShapeMode == ShapeMode.Bubble) {
                this.mEditText.setBackgroundResource(this.mCounterResBgId);
            }
        } else if (this.mCounterView.getVisibility() == 0) {
            this.mCounterView.animate().alpha(0.0f).setDuration(50).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                /* class huawei.android.widget.CounterTextLayout.AnonymousClass2 */

                public void onAnimationEnd(Animator animation) {
                    CounterTextLayout.this.mCounterView.setVisibility(CounterTextLayout.this.mIsSpaceOccupied ? 4 : 8);
                }
            }).start();
            updateTextLayoutBackground(this.mEditTextBgResId, null);
        }
        sendAccessibilityEvent(2048);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTextLayoutBackground(int bubbleBackgroundId, ColorStateList linearTint) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            this.mEditText.setBackgroundResource(bubbleBackgroundId);
        } else if (this.mShapeMode == ShapeMode.Linear) {
            this.mEditText.setBackgroundTintList(linearTint);
        }
    }

    public enum ShapeMode {
        Bubble(0),
        Linear(1);
        
        private final int mModeValue;

        private ShapeMode(int value) {
            this.mModeValue = value;
        }

        static ShapeMode getShapeMode(int modeValue) {
            ShapeMode shapeMode = null;
            int size = values().length;
            for (int i = 0; i < size; i++) {
                shapeMode = values()[i];
                if (shapeMode.getModeValue() == modeValue) {
                    return shapeMode;
                }
            }
            return shapeMode;
        }

        /* access modifiers changed from: package-private */
        public int getModeValue() {
            return this.mModeValue;
        }
    }

    /* access modifiers changed from: private */
    public class ChangeWatcher implements TextWatcher {
        private final Animation mShake;

        private ChangeWatcher() {
            this.mShake = AnimationUtils.loadAnimation(CounterTextLayout.this.getContext(), CounterTextLayout.this.mAnimShakeId);
            this.mShake.setAnimationListener(new Animation.AnimationListener(CounterTextLayout.this) {
                /* class huawei.android.widget.CounterTextLayout.ChangeWatcher.AnonymousClass1 */

                public void onAnimationStart(Animation animation) {
                    CounterTextLayout.this.mCounterView.setTextColor(CounterTextLayout.this.mCounterWarningColor);
                    CounterTextLayout.this.updateTextLayoutBackground(CounterTextLayout.this.mErrorResBgId, ColorStateList.valueOf(CounterTextLayout.this.mCounterView.getCurrentTextColor()));
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    CounterTextLayout.this.updateTextLayoutBackground(CounterTextLayout.this.mCounterResBgId, null);
                    CounterTextLayout.this.mCounterView.setTextAppearance(CounterTextLayout.this.getContext(), CounterTextLayout.this.mCounterTextAppearance);
                }
            });
        }

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence text, int start, int before, int count) {
            if (CounterTextLayout.this.mMaxLength == -1 || CounterTextLayout.this.mStartShownPos == -1) {
                CounterTextLayout.this.setError(null);
            } else if (text instanceof Editable) {
                Editable editable = (Editable) text;
                int length = editable.length();
                if (length > CounterTextLayout.this.mMaxLength) {
                    int selectionEnd = CounterTextLayout.this.mEditText.getSelectionEnd();
                    editable.delete(CounterTextLayout.this.mMaxLength, editable.length());
                    CounterTextLayout.this.mEditText.setSelection(selectionEnd > CounterTextLayout.this.mMaxLength ? CounterTextLayout.this.mMaxLength : selectionEnd);
                    CounterTextLayout.this.mEditText.startAnimation(this.mShake);
                } else if (length > CounterTextLayout.this.mStartShownPos) {
                    CounterTextLayout counterTextLayout = CounterTextLayout.this;
                    counterTextLayout.setError(length + " / " + CounterTextLayout.this.mMaxLength);
                } else {
                    CounterTextLayout.this.setError(null);
                }
            }
        }
    }

    private class TextInputAccessibilityDelegate extends View.AccessibilityDelegate {
        private TextInputAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setClassName(CounterTextLayout.class.getSimpleName());
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(CounterTextLayout.class.getSimpleName());
            if (CounterTextLayout.this.mEditText != null) {
                info.setLabelFor(CounterTextLayout.this.mEditText);
            }
            CharSequence error = CounterTextLayout.this.mCounterView != null ? CounterTextLayout.this.mCounterView.getText() : null;
            if (!TextUtils.isEmpty(error)) {
                info.setContentInvalid(true);
                info.setError(error);
            }
        }
    }
}
