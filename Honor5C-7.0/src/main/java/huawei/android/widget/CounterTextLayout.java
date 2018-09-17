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
import com.huawei.android.os.UserManagerEx;
import com.huawei.attestation.HwAttestationStatus;
import com.huawei.lcagent.client.MetricConstant;
import huawei.android.widget.DialogContentHelper.Dex;
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.CounterTextLayout.ShapeMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.CounterTextLayout.ShapeMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.CounterTextLayout.ShapeMode.<clinit>():void");
        }
    }

    private class TextInputAccessibilityDelegate extends AccessibilityDelegate {
        final /* synthetic */ CounterTextLayout this$0;

        /* synthetic */ TextInputAccessibilityDelegate(CounterTextLayout this$0, TextInputAccessibilityDelegate textInputAccessibilityDelegate) {
            this(this$0);
        }

        private TextInputAccessibilityDelegate(CounterTextLayout this$0) {
            this.this$0 = this$0;
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setClassName(CounterTextLayout.class.getSimpleName());
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            CharSequence error = null;
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(CounterTextLayout.class.getSimpleName());
            if (this.this$0.mEditText != null) {
                info.setLabelFor(this.this$0.mEditText);
            }
            if (this.this$0.mCounterView != null) {
                error = this.this$0.mCounterView.getText();
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CounterTextLayout, defStyleAttr, 33947850);
        this.mCounterTextAppearance = a.getResourceId(2, 0);
        this.mCounterWarningColor = context.getResources().getColor(33882295);
        this.mSpaceOccupied = a.getBoolean(0, false);
        this.mShapeMode = ShapeMode.values()[a.getInt(1, ShapeMode.Bubble.ordinal())];
        a.recycle();
        this.isHwDarkTheme = HwWidgetFactory.isHwDarkTheme(context);
        if (this.isHwDarkTheme) {
            this.mCounterResBgId = 33751291;
            this.mErrorResBgId = 33751272;
            this.mEditTextBgResId = 33751293;
            this.mLinearEditBgResId = 33751295;
        } else {
            this.mCounterResBgId = 33751290;
            this.mErrorResBgId = 33751271;
            this.mEditTextBgResId = 33751292;
            this.mLinearEditBgResId = 33751294;
        }
        setupCounterView();
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setAccessibilityDelegate(new TextInputAccessibilityDelegate());
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
        this.mEditText.setImeOptions(UserManagerEx.FLAG_HW_HIDDENSPACE | this.mEditText.getImeOptions());
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
                    Animation shake = AnimationUtils.loadAnimation(CounterTextLayout.this.getContext(), 34078770);
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
        sendAccessibilityEvent(HwAttestationStatus.CERT_MAX_LENGTH);
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
            } else if (this.mShapeMode == ShapeMode.Linear) {
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
        sendAccessibilityEvent(HwAttestationStatus.CERT_MAX_LENGTH);
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
        int count = getChildCount();
        boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) == 1073741824 ? MeasureSpec.getMode(heightMeasureSpec) != 1073741824 : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, (child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin);
                maxHeight = Math.max(maxHeight, (child.getMeasuredHeight() + lp.topMargin) + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child);
                }
            }
        }
        setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth, getSuggestedMinimumWidth()), widthMeasureSpec, childState), resolveSizeAndState(Math.max(maxHeight, getSuggestedMinimumHeight()), heightMeasureSpec, childState << 16));
        count = this.mMatchParentChildren.size();
        if (count > 1) {
            for (i = 0; i < count; i++) {
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;
                child = (View) this.mMatchParentChildren.get(i);
                MarginLayoutParams lp2 = (MarginLayoutParams) child.getLayoutParams();
                if (lp2.width == -1) {
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, (getMeasuredWidth() - lp2.leftMargin) - lp2.rightMargin), 1073741824);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, lp2.leftMargin + lp2.rightMargin, lp2.width);
                }
                if (lp2.height == -1) {
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, (getMeasuredHeight() - lp2.topMargin) - lp2.bottomMargin), 1073741824);
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
                int i2;
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
                    case ViewDragHelper.STATE_DRAGGING /*1*/:
                        i2 = lp.leftMargin;
                        childLeft = (((((parentRight + 0) - width) / 2) + 0) + r0) - lp.rightMargin;
                        break;
                    case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                        childLeft = (parentRight - width) - lp.rightMargin;
                        break;
                    default:
                        childLeft = lp.leftMargin + 0;
                        break;
                }
                switch (verticalGravity) {
                    case MetricConstant.LEVEL_B /*16*/:
                        i2 = lp.topMargin;
                        childTop = (((((parentBottom + 0) - height) / 2) + 0) + r0) - lp.bottomMargin;
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
