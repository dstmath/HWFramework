package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.hwcontrol.HwWidgetFactory;
import android.telephony.HwCarrierConfigManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidhwext.R;
import java.util.ArrayList;

public class CounterTextLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 50;
    private boolean isHwDarkTheme;
    private int mCounterResBgId;
    private int mCounterTextAppearance;
    private TextView mCounterView;
    private int mCounterWarningColor;
    private EditText mEditText;
    private int mEditTextBgResId;
    private int mErrorResBgId;
    private int mLinearEditBgResId;
    private final ArrayList<View> mMatchParentChildren;
    private int mMaxLength;
    private ShapeMode mShapeMode;
    private boolean mSpaceOccupied;
    private int mStartShownPos;

    public enum ShapeMode {
        Bubble,
        Linear
    }

    private class TextInputAccessibilityDelegate extends AccessibilityDelegate {
        /* synthetic */ TextInputAccessibilityDelegate(CounterTextLayout this$0, TextInputAccessibilityDelegate -this1) {
            this();
        }

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
            CharSequence error;
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(CounterTextLayout.class.getSimpleName());
            if (CounterTextLayout.this.mEditText != null) {
                info.setLabelFor(CounterTextLayout.this.mEditText);
            }
            if (CounterTextLayout.this.mCounterView != null) {
                error = CounterTextLayout.this.mCounterView.getText();
            } else {
                error = null;
            }
            if (!TextUtils.isEmpty(error)) {
                info.setContentInvalid(true);
                info.setError(error);
            }
        }
    }

    public CounterTextLayout(Context context) {
        this(context, null);
    }

    public CounterTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CounterTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        this.mMatchParentChildren = new ArrayList(1);
        this.mMaxLength = -1;
        this.mStartShownPos = -1;
        this.mCounterResBgId = -1;
        this.mErrorResBgId = -1;
        this.mEditTextBgResId = -1;
        this.mLinearEditBgResId = -1;
        setOrientation(1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CounterTextLayout, defStyleAttr, 33947939);
        this.mCounterTextAppearance = a.getResourceId(2, 0);
        this.mCounterWarningColor = context.getResources().getColor(33882310);
        this.mSpaceOccupied = a.getBoolean(0, false);
        this.mShapeMode = ShapeMode.values()[a.getInt(1, ShapeMode.Bubble.ordinal())];
        a.recycle();
        this.isHwDarkTheme = HwWidgetFactory.isHwDarkTheme(context);
        if (this.isHwDarkTheme) {
            this.mCounterResBgId = 33751207;
            this.mErrorResBgId = 33751631;
            this.mEditTextBgResId = 33751209;
            this.mLinearEditBgResId = 33751212;
        } else {
            this.mCounterResBgId = 33751206;
            this.mErrorResBgId = 33751630;
            this.mEditTextBgResId = 33751208;
            this.mLinearEditBgResId = 33751211;
        }
        setupCounterView();
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
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (-1 == CounterTextLayout.this.mMaxLength || -1 == CounterTextLayout.this.mStartShownPos) {
                    CounterTextLayout.this.setError(null);
                    return;
                }
                Editable editable = CounterTextLayout.this.mEditText.getText();
                int len = editable.length();
                if (len > CounterTextLayout.this.mMaxLength) {
                    int selEndIndex = Selection.getSelectionEnd(editable);
                    CounterTextLayout.this.mEditText.setText(editable.toString().substring(0, CounterTextLayout.this.mMaxLength));
                    editable = CounterTextLayout.this.mEditText.getText();
                    if (selEndIndex > editable.length()) {
                        selEndIndex = editable.length();
                    }
                    Selection.setSelection(editable, selEndIndex);
                    Animation shake = AnimationUtils.loadAnimation(CounterTextLayout.this.getContext(), 34209796);
                    shake.setAnimationListener(new AnimationListener() {
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
                    CounterTextLayout.this.mEditText.startAnimation(shake);
                } else if (len > CounterTextLayout.this.mStartShownPos) {
                    CounterTextLayout.this.setError(len + " / " + CounterTextLayout.this.mMaxLength);
                } else {
                    CounterTextLayout.this.setError(null);
                }
            }
        });
        if (this.mCounterView != null) {
            this.mCounterView.setPaddingRelative(this.mEditText.getPaddingStart(), 0, this.mEditText.getPaddingEnd(), 0);
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

    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
        this.mStartShownPos = (this.mMaxLength * 9) / 10;
    }

    public int getMaxLength() {
        return this.mMaxLength;
    }

    private void setupCounterView() {
        this.mCounterView = new TextView(getContext());
        this.mCounterView.setTextAppearance(getContext(), this.mCounterTextAppearance);
        this.mCounterView.setVisibility(this.mSpaceOccupied ? 4 : 8);
        addCounterView();
        if (this.mEditText != null) {
            this.mCounterView.setPaddingRelative(this.mEditText.getPaddingStart(), 0, this.mEditText.getPaddingEnd(), 0);
        }
    }

    private void addCounterView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = 8388693;
        addView(this.mCounterView, params);
    }

    private void setError(CharSequence error) {
        if (!TextUtils.isEmpty(error)) {
            this.mCounterView.setAlpha(0.0f);
            this.mCounterView.setText(error);
            this.mCounterView.animate().alpha(1.0f).setDuration(50).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    CounterTextLayout.this.mCounterView.setVisibility(0);
                }
            }).start();
            if (this.mShapeMode == ShapeMode.Bubble) {
                this.mEditText.setBackgroundResource(this.mCounterResBgId);
            } else {
                ShapeMode shapeMode = this.mShapeMode;
                ShapeMode shapeMode2 = ShapeMode.Linear;
            }
        } else if (this.mCounterView.getVisibility() == 0) {
            this.mCounterView.animate().alpha(0.0f).setDuration(50).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    int i;
                    TextView -get2 = CounterTextLayout.this.mCounterView;
                    if (CounterTextLayout.this.mSpaceOccupied) {
                        i = 4;
                    } else {
                        i = 8;
                    }
                    -get2.setVisibility(i);
                }
            }).start();
            updateTextLayoutBackground(this.mEditTextBgResId, null);
        }
        sendAccessibilityEvent(2048);
    }

    public CharSequence getError() {
        if (this.mCounterView == null || this.mCounterView.getVisibility() != 0) {
            return null;
        }
        return this.mCounterView.getText();
    }

    private void updateTextLayoutBackground(int bubbleBackgroundID, ColorStateList linearTint) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            this.mEditText.setBackgroundResource(bubbleBackgroundID);
        } else if (this.mShapeMode == ShapeMode.Linear) {
            this.mEditText.setBackgroundTintList(linearTint);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            measureBubble(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureBubble(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View child;
        int count = getChildCount();
        boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) == 1073741824 ? MeasureSpec.getMode(heightMeasureSpec) != 1073741824 : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                int widthWithMargin = (child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin;
                if (widthWithMargin > maxWidth) {
                    maxWidth = widthWithMargin;
                }
                int heightWithMargin = (child.getMeasuredHeight() + lp.topMargin) + lp.bottomMargin;
                if (heightWithMargin > maxHeight) {
                    maxHeight = heightWithMargin;
                }
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child);
                }
            }
        }
        int minHeight = getSuggestedMinimumHeight();
        int minWidth = getSuggestedMinimumWidth();
        if (maxHeight <= minHeight) {
            maxHeight = minHeight;
        }
        if (maxWidth <= minWidth) {
            maxWidth = minWidth;
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState), resolveSizeAndState(maxHeight, heightMeasureSpec, childState << 16));
        count = this.mMatchParentChildren.size();
        if (count > 1) {
            for (i = 0; i < count; i++) {
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;
                child = (View) this.mMatchParentChildren.get(i);
                MarginLayoutParams lp2 = (MarginLayoutParams) child.getLayoutParams();
                if (lp2.width == -1) {
                    int widthNoMargin = (getMeasuredWidth() - lp2.leftMargin) - lp2.rightMargin;
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthNoMargin > 0 ? widthNoMargin : 0, 1073741824);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, lp2.leftMargin + lp2.rightMargin, lp2.width);
                }
                if (lp2.height == -1) {
                    int heightNoMargin = (getMeasuredHeight() - lp2.topMargin) - lp2.bottomMargin;
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightNoMargin > 0 ? heightNoMargin : 0, 1073741824);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, lp2.topMargin + lp2.bottomMargin, lp2.height);
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            layoutBubble(left, top, right, bottom);
            this.mCounterView.offsetTopAndBottom(-9);
            return;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private void layoutBubble(int left, int top, int right, int bottom) {
        int count = getChildCount();
        int parentRight = right - left;
        int parentBottom = bottom - top;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childLeft;
                int childTop;
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = 8388693;
                }
                int verticalGravity = gravity & 112;
                switch (Gravity.getAbsoluteGravity(gravity, getLayoutDirection()) & 7) {
                    case 1:
                        childLeft = (((((parentRight + 0) - width) / 2) + 0) + lp.leftMargin) - lp.rightMargin;
                        break;
                    case 5:
                        childLeft = (parentRight - width) - lp.rightMargin;
                        break;
                    default:
                        childLeft = lp.leftMargin + 0;
                        break;
                }
                switch (verticalGravity) {
                    case 16:
                        childTop = (((((parentBottom + 0) - height) / 2) + 0) + lp.topMargin) - lp.bottomMargin;
                        break;
                    case HwCarrierConfigManager.HD_ICON_MASK_STATUS_BAR /*48*/:
                        childTop = lp.topMargin + 0;
                        break;
                    case 80:
                        childTop = (parentBottom - height) - lp.bottomMargin;
                        break;
                    default:
                        childTop = lp.topMargin + 0;
                        break;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }
}
