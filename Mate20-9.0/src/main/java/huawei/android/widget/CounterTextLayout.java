package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
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
import java.util.ArrayList;

public class CounterTextLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 50;
    /* access modifiers changed from: private */
    public int mAnimShakeId;
    /* access modifiers changed from: private */
    public int mCounterResBgId;
    /* access modifiers changed from: private */
    public int mCounterTextAppearance;
    /* access modifiers changed from: private */
    public TextView mCounterView;
    /* access modifiers changed from: private */
    public int mCounterWarningColor;
    /* access modifiers changed from: private */
    public EditText mEditText;
    private int mEditTextBgResId;
    /* access modifiers changed from: private */
    public int mErrorResBgId;
    private int mLinearEditBgResId;
    private final ArrayList<View> mMatchParentChildren;
    /* access modifiers changed from: private */
    public int mMaxLength;
    private ResLoader mResLoader;
    private ShapeMode mShapeMode;
    /* access modifiers changed from: private */
    public boolean mSpaceOccupied;
    /* access modifiers changed from: private */
    public int mStartShownPos;

    public enum ShapeMode {
        Bubble,
        Linear
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

    public CounterTextLayout(Context context) {
        this(context, null);
    }

    public CounterTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CounterTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        this.mMatchParentChildren = new ArrayList<>(1);
        this.mMaxLength = -1;
        this.mStartShownPos = -1;
        this.mCounterResBgId = -1;
        this.mErrorResBgId = -1;
        this.mEditTextBgResId = -1;
        this.mLinearEditBgResId = -1;
        setOrientation(1);
        this.mResLoader = ResLoader.getInstance();
        int textInputErrorColor = this.mResLoader.getIdentifier(context, ResLoaderUtil.COLOR, "design_textinput_error_color");
        this.mAnimShakeId = this.mResLoader.getIdentifier(context, "anim", "shake");
        this.mCounterWarningColor = context.getResources().getColor(textInputErrorColor);
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(attrs, this.mResLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "CounterTextLayout"), this.mResLoader.getIdentifier(context, "attr", "counterTextLayoutStyle"), this.mResLoader.getIdentifier(context, "style", "Widget.Emui.CounterTextLayout"));
            this.mCounterTextAppearance = a.getResourceId(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_counterTextAppearance"), 0);
            this.mSpaceOccupied = a.getBoolean(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_spaceOccupied"), false);
            this.mShapeMode = ShapeMode.values()[a.getInt(ResLoaderUtil.getStyleableId(context, "CounterTextLayout_shape_mode"), ShapeMode.Bubble.ordinal())];
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
                        Editable editable2 = CounterTextLayout.this.mEditText.getText();
                        if (selEndIndex > editable2.length()) {
                            selEndIndex = editable2.length();
                        }
                        Selection.setSelection(editable2, selEndIndex);
                        Animation shake = AnimationUtils.loadAnimation(CounterTextLayout.this.getContext(), CounterTextLayout.this.mAnimShakeId);
                        shake.setAnimationListener(new Animation.AnimationListener() {
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
                        CounterTextLayout counterTextLayout = CounterTextLayout.this;
                        counterTextLayout.setError(len + " / " + CounterTextLayout.this.mMaxLength);
                    } else {
                        CounterTextLayout.this.setError(null);
                    }
                }
            });
            if (this.mCounterView != null) {
                this.mCounterView.setPaddingRelative(this.mEditText.getPaddingStart(), 0, this.mEditText.getPaddingEnd(), 0);
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

    /* access modifiers changed from: private */
    public void setError(CharSequence error) {
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
                    TextView access$700 = CounterTextLayout.this.mCounterView;
                    if (CounterTextLayout.this.mSpaceOccupied) {
                        i = 4;
                    } else {
                        i = 8;
                    }
                    access$700.setVisibility(i);
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

    /* access modifiers changed from: private */
    public void updateTextLayoutBackground(int bubbleBackgroundID, ColorStateList linearTint) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            this.mEditText.setBackgroundResource(bubbleBackgroundID);
        } else if (this.mShapeMode == ShapeMode.Linear) {
            this.mEditText.setBackgroundTintList(linearTint);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            measureBubble(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureBubble(int widthMeasureSpec, int heightMeasureSpec) {
        int widthNoMargin;
        int minHeight;
        int heightNoMargin;
        int i;
        int i2 = widthMeasureSpec;
        int i3 = heightMeasureSpec;
        int count = getChildCount();
        boolean measureMatchParentChildren = (View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && View.MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        int maxHeight2 = 0;
        while (true) {
            int i4 = maxHeight2;
            if (i4 >= count) {
                break;
            }
            View child = getChildAt(i4);
            if (child.getVisibility() != 8) {
                View child2 = child;
                i = i4;
                int childState2 = childState;
                measureChildWithMargins(child, i2, 0, i3, 0);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child2.getLayoutParams();
                int widthWithMargin = child2.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                int maxWidth2 = widthWithMargin > maxWidth ? widthWithMargin : maxWidth;
                int heightWithMargin = child2.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                int maxHeight3 = heightWithMargin > maxHeight ? heightWithMargin : maxHeight;
                childState = combineMeasuredStates(childState2, child2.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child2);
                }
                maxWidth = maxWidth2;
                maxHeight = maxHeight3;
            } else {
                i = i4;
                int i5 = childState;
            }
            maxHeight2 = i + 1;
        }
        int i6 = -1;
        int childState3 = childState;
        int minHeight2 = getSuggestedMinimumHeight();
        int minWidth = getSuggestedMinimumWidth();
        setMeasuredDimension(resolveSizeAndState(maxWidth > minWidth ? maxWidth : minWidth, i2, childState3), resolveSizeAndState(maxHeight > minHeight2 ? maxHeight : minHeight2, i3, childState3 << 16));
        int count2 = this.mMatchParentChildren.size();
        if (count2 > 1) {
            int i7 = 0;
            while (i7 < count2) {
                View child3 = this.mMatchParentChildren.get(i7);
                ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams) child3.getLayoutParams();
                if (lp2.width == i6) {
                    int widthNoMargin2 = (getMeasuredWidth() - lp2.leftMargin) - lp2.rightMargin;
                    widthNoMargin = View.MeasureSpec.makeMeasureSpec(widthNoMargin2 > 0 ? widthNoMargin2 : 0, 1073741824);
                } else {
                    widthNoMargin = getChildMeasureSpec(i2, lp2.leftMargin + lp2.rightMargin, lp2.width);
                }
                int childWidthMeasureSpec = widthNoMargin;
                if (lp2.height == -1) {
                    int heightNoMargin2 = (getMeasuredHeight() - lp2.topMargin) - lp2.bottomMargin;
                    minHeight = minHeight2;
                    heightNoMargin = View.MeasureSpec.makeMeasureSpec(heightNoMargin2 > 0 ? heightNoMargin2 : 0, 1073741824);
                } else {
                    minHeight = minHeight2;
                    heightNoMargin = getChildMeasureSpec(i3, lp2.topMargin + lp2.bottomMargin, lp2.height);
                }
                child3.measure(childWidthMeasureSpec, heightNoMargin);
                i7++;
                minHeight2 = minHeight;
                i6 = -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mShapeMode == ShapeMode.Bubble) {
            layoutBubble(left, top, right, bottom);
            this.mCounterView.offsetTopAndBottom(-9);
            return;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private void layoutBubble(int left, int top, int right, int bottom) {
        int parentLeft;
        int count;
        int childLeft;
        int childTop;
        int count2 = getChildCount();
        int parentLeft2 = false;
        int parentRight = right - left;
        int parentBottom = bottom - top;
        int i = 0;
        while (i < count2) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                count = count2;
                if (gravity == -1) {
                    gravity = 8388693;
                }
                int layoutDirection = getLayoutDirection();
                int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                int i2 = layoutDirection;
                int layoutDirection2 = gravity & 112;
                parentLeft = parentLeft2;
                int parentLeft3 = absoluteGravity & 7;
                if (parentLeft3 == 1) {
                    childLeft = (((((parentRight + 0) - width) / 2) + 0) + lp.leftMargin) - lp.rightMargin;
                } else if (parentLeft3 != 5) {
                    childLeft = lp.leftMargin + 0;
                } else {
                    childLeft = (parentRight - width) - lp.rightMargin;
                }
                if (layoutDirection2 == 16) {
                    int verticalGravity = layoutDirection2;
                    childTop = (((((parentBottom + 0) - height) / 2) + 0) + lp.topMargin) - lp.bottomMargin;
                } else if (layoutDirection2 == 48) {
                    int verticalGravity2 = layoutDirection2;
                    childTop = 0 + lp.topMargin;
                } else if (layoutDirection2 != 80) {
                    childTop = lp.topMargin + 0;
                    int i3 = layoutDirection2;
                } else {
                    int i4 = layoutDirection2;
                    childTop = (parentBottom - height) - lp.bottomMargin;
                }
                int childTop2 = childTop;
                child.layout(childLeft, childTop2, childLeft + width, childTop2 + height);
            } else {
                count = count2;
                parentLeft = parentLeft2;
            }
            i++;
            count2 = count;
            parentLeft2 = parentLeft;
        }
        int i5 = count2;
        int i6 = parentLeft2;
    }
}
