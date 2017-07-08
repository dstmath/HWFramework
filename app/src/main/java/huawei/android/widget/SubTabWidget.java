package huawei.android.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.hwcontrol.HwWidgetFactory.DisplayMode;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidhwext.R;
import com.huawei.internal.telephony.uicc.IccConstantsEx;

public class SubTabWidget extends LinearLayout {
    private static final /* synthetic */ int[] -android-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues = null;
    private static final String TAG = "SubTabWidget";
    private boolean mClickable;
    private Context mContext;
    private boolean mIsInitSubTabColorDone;
    private boolean mIsLightStyle;
    private boolean mIsSetSubTab;
    private int mLastOritation;
    private int mLastPos;
    private int mLastSubTab;
    private Typeface mMedium;
    private Typeface mRegular;
    private SubTab mSelectedSubTab;
    private SubTabAnim mSubTabAnim;
    private SubTabClickListener mSubTabClickListener;
    public LinearLayout mSubTabContentView;
    private int mTextHeightOffset;
    private int mWidthPixels;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        public int mSavedPosition;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.SubTabWidget.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.SubTabWidget.SavedState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.SubTabWidget.SavedState.<clinit>():void");
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            this.mSavedPosition = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mSavedPosition);
        }
    }

    public class SubTab {
        public static final int INVALID_POSITION = -1;
        private SubTabListener mCallback;
        private int mPosition;
        private int mSubTabId;
        private Object mTag;
        private CharSequence mText;
        final /* synthetic */ SubTabWidget this$0;

        public SubTab(SubTabWidget this$0) {
            this(this$0, null, null, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text) {
            this(this$0, text, null, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, SubTabListener callback) {
            this(this$0, text, callback, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, Object tag) {
            this(this$0, text, null, tag);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, SubTabListener callback, Object tag) {
            this.this$0 = this$0;
            this.mPosition = INVALID_POSITION;
            this.mSubTabId = INVALID_POSITION;
            this.mText = text;
            this.mCallback = callback;
            this.mTag = tag;
        }

        public SubTabListener getCallback() {
            return this.mCallback;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public void select() {
            this.this$0.selectSubTab(this);
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public SubTab setSubTabListener(SubTabListener callback) {
            this.mCallback = callback;
            return this;
        }

        public SubTab setText(int resId) {
            return setText(this.this$0.getContext().getResources().getText(resId));
        }

        public SubTab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                this.this$0.updateSubTab(this.mPosition);
            }
            return this;
        }

        public SubTab setTag(Object obj) {
            this.mTag = obj;
            return this;
        }

        public Object getTag() {
            return this.mTag;
        }

        public void setSubTabId(int id) {
            this.mSubTabId = id;
        }

        public int getSubTabId() {
            return this.mSubTabId;
        }
    }

    private static class SubTabAnim {
        private ValueAnimator mAnimator;
        private Context mContext;
        private int mDuration;
        private boolean mIsAnimEnd;
        private int mSecondLayerInsetBottom;
        private int mSecondLayerInsetLeft;
        private int mSecondLayerInsetRight;
        private int mSecondLayerInsetTop;
        private LinearLayout mView;

        /* renamed from: huawei.android.widget.SubTabWidget.SubTabAnim.1 */
        class AnonymousClass1 implements AnimatorUpdateListener {
            final /* synthetic */ SubTabAnim this$1;
            final /* synthetic */ int val$cnt;
            final /* synthetic */ int val$from;
            final /* synthetic */ int val$to;

            AnonymousClass1(SubTabAnim this$1, int val$from, int val$to, int val$cnt) {
                this.this$1 = this$1;
                this.val$from = val$from;
                this.val$to = val$to;
                this.val$cnt = val$cnt;
            }

            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                if (value.floatValue() == 0.0f) {
                    this.this$1.switchTextColor(this.val$from, this.val$to);
                }
                this.this$1.updateSubTabColor(this.val$from, this.val$to, (int) (255.0f - (value.floatValue() * 255.0f)), (int) (value.floatValue() * 255.0f), this.val$cnt);
            }
        }

        /* renamed from: huawei.android.widget.SubTabWidget.SubTabAnim.2 */
        class AnonymousClass2 implements AnimatorListener {
            final /* synthetic */ SubTabAnim this$1;
            final /* synthetic */ int val$cnt;
            final /* synthetic */ int val$from;
            final /* synthetic */ int val$to;

            AnonymousClass2(SubTabAnim this$1, int val$from, int val$to, int val$cnt) {
                this.this$1 = this$1;
                this.val$from = val$from;
                this.val$to = val$to;
                this.val$cnt = val$cnt;
            }

            public void onAnimationCancel(Animator animation) {
                this.this$1.updateSubTabColor(this.val$from, this.val$to, 0, IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN, this.val$cnt);
            }

            public void onAnimationEnd(Animator animation) {
                this.this$1.updateSubTabColor(this.val$from, this.val$to, 0, IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN, this.val$cnt);
                this.this$1.mIsAnimEnd = true;
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationStart(Animator animation) {
            }
        }

        SubTabAnim(LinearLayout view, Context context) {
            this.mDuration = 150;
            this.mView = view;
            this.mContext = context;
            this.mIsAnimEnd = true;
        }

        public void startAnim(int from, int to, int cnt) {
            if (from != to) {
                this.mIsAnimEnd = false;
                anim(from, to, cnt);
            }
        }

        public boolean isAnimEnd() {
            return this.mIsAnimEnd;
        }

        private void switchTextColor(int from, int to) {
            SubTabView child = (SubTabView) this.mView.getChildAt(from);
            if (child != null) {
                child.updateTitleAppearance(0);
            } else {
                Log.w(SubTabWidget.TAG, "the child is null and switch from " + from);
            }
            child = (SubTabView) this.mView.getChildAt(to);
            if (child != null) {
                child.updateTitleAppearance(1);
            } else {
                Log.w(SubTabWidget.TAG, "the child is null and switch to " + to);
            }
        }

        private void updateSubTabColor(int from, int to, int fromAlpha, int toAlpha, int cnt) {
            array = new Drawable[2];
            int[] changeArray = new int[]{from, to};
            for (int i = 0; i < changeArray.length; i++) {
                View child = this.mView.getChildAt(changeArray[i]);
                SubTabWidget.updateSubTabColorRes(this.mContext, array, changeArray[i], 0, cnt - 1);
                if (changeArray[i] == from) {
                    array[1].setAlpha(fromAlpha);
                } else if (changeArray[i] == to) {
                    array[1].setAlpha(toAlpha);
                }
                LayerDrawable ld = new LayerDrawable(array);
                ld.setLayerInset(1, -this.mSecondLayerInsetLeft, -this.mSecondLayerInsetTop, -this.mSecondLayerInsetRight, -this.mSecondLayerInsetBottom);
                child.setBackgroundDrawable(ld);
            }
        }

        private void anim(int from, int to, int cnt) {
            this.mAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mAnimator.setDuration((long) this.mDuration).start();
            this.mAnimator.addUpdateListener(new AnonymousClass1(this, from, to, cnt));
            this.mAnimator.addListener(new AnonymousClass2(this, from, to, cnt));
        }
    }

    private class SubTabClickListener implements OnClickListener {
        final /* synthetic */ SubTabWidget this$0;

        /* synthetic */ SubTabClickListener(SubTabWidget this$0, SubTabClickListener subTabClickListener) {
            this(this$0);
        }

        private SubTabClickListener(SubTabWidget this$0) {
            this.this$0 = this$0;
        }

        public void onClick(View view) {
            if (this.this$0.mClickable) {
                if (view instanceof SubTabView) {
                    ((SubTabView) view).getSubTab().select();
                }
                int subTabCount = this.this$0.mSubTabContentView.getChildCount();
                for (int i = 0; i < subTabCount; i++) {
                    boolean z;
                    View child = this.this$0.mSubTabContentView.getChildAt(i);
                    if (child == view) {
                        z = true;
                    } else {
                        z = false;
                    }
                    child.setSelected(z);
                    if (child == view) {
                        int lastPos = this.this$0.mLastPos;
                        if (this.this$0.mIsSetSubTab && this.this$0.mLastSubTab != -1) {
                            lastPos = this.this$0.mLastSubTab;
                        }
                        this.this$0.mSubTabAnim.startAnim(lastPos, i, subTabCount);
                        this.this$0.mIsSetSubTab = false;
                        this.this$0.mLastPos = i;
                    }
                }
            }
        }
    }

    public interface SubTabListener {
        void onSubTabReselected(SubTab subTab, FragmentTransaction fragmentTransaction);

        void onSubTabSelected(SubTab subTab, FragmentTransaction fragmentTransaction);

        void onSubTabUnselected(SubTab subTab, FragmentTransaction fragmentTransaction);
    }

    private static class SubTabView extends TextView {
        private boolean mIsLightStyleInner;
        private SubTab mSubTab;

        public SubTabView(Context context, SubTab subTab, boolean isLightStyle) {
            super(context, null, 34799638);
            this.mSubTab = subTab;
            this.mIsLightStyleInner = isLightStyle;
            update();
        }

        public SubTab getSubTab() {
            return this.mSubTab;
        }

        public void update() {
            CharSequence text = this.mSubTab.getText();
            if (!TextUtils.isEmpty(text)) {
                setText(text);
                setVisibility(0);
            } else {
                setVisibility(8);
                setText(null);
            }
            initTitleAppearance();
            if (this.mSubTab.getSubTabId() != -1) {
                setId(this.mSubTab.getSubTabId());
            }
        }

        public void updateTitleAppearance(int type) {
            int colorId;
            if (type == 0) {
                if (this.mIsLightStyleInner) {
                    colorId = 33882249;
                } else {
                    colorId = 33882253;
                }
                setTypeface(Typeface.defaultFromStyle(0));
            } else {
                if (this.mIsLightStyleInner) {
                    colorId = 33882246;
                } else {
                    colorId = 33882250;
                }
                setTypeface(Typeface.defaultFromStyle(1));
            }
            setTextColor(this.mContext.getResources().getColor(colorId));
        }

        private void initTitleAppearance() {
            int colorRes;
            if (this.mIsLightStyleInner) {
                colorRes = 33882411;
            } else {
                colorRes = 33882412;
            }
            setTextColor(this.mContext.getResources().getColorStateList(colorRes));
        }

        public void setBackgroundStyle(int style) {
            boolean z = false;
            if (style == 0) {
                z = true;
            }
            this.mIsLightStyleInner = z;
            initTitleAppearance();
        }
    }

    private static /* synthetic */ int[] -getandroid-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues() {
        if (-android-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues != null) {
            return -android-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues;
        }
        int[] iArr = new int[DisplayMode.values().length];
        try {
            iArr[DisplayMode.Large.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DisplayMode.Medium.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DisplayMode.Normal.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DisplayMode.Small.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        -android-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues = iArr;
        return iArr;
    }

    public SubTabWidget(Context context) {
        this(context, null);
    }

    public SubTabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 34799637);
    }

    public SubTabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mClickable = true;
        this.mWidthPixels = 0;
        this.mIsSetSubTab = false;
        this.mLastSubTab = -1;
        this.mTextHeightOffset = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SubTabWidget, defStyle, 0);
        int dividerPadding = a.getDimensionPixelSize(1, 0);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mTextHeightOffset = context.getResources().getDimensionPixelSize(34472115);
        this.mSubTabContentView = (LinearLayout) inflater.inflate(34013265, this, true).findViewById(34603189);
        this.mIsLightStyle = HwWidgetFactory.isHwLightTheme(context);
        this.mContext = context;
        initDividerDrawable();
        this.mSubTabContentView.setDividerPadding(dividerPadding);
        a.recycle();
        updateTabViewContainerWidth(context);
        this.mRegular = Typeface.create("sans-serif", 0);
        this.mMedium = Typeface.create("HwChinese-medium", 0);
        this.mSubTabAnim = new SubTabAnim(this.mSubTabContentView, this.mContext);
        this.mIsInitSubTabColorDone = false;
        this.mSubTabAnim.mSecondLayerInsetLeft = context.getResources().getDimensionPixelSize(34472175);
        this.mSubTabAnim.mSecondLayerInsetTop = context.getResources().getDimensionPixelSize(34472176);
        this.mSubTabAnim.mSecondLayerInsetRight = context.getResources().getDimensionPixelSize(34472177);
        this.mSubTabAnim.mSecondLayerInsetBottom = context.getResources().getDimensionPixelSize(34472178);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mLastOritation != newConfig.orientation) {
            this.mLastOritation = newConfig.orientation;
            updateTabViewContainerWidth(this.mContext);
        }
    }

    private void updateTabViewContainerWidth(Context context) {
        int subtabContainerViewWidth = context.getResources().getDimensionPixelOffset(getDimensForScaleMode());
        boolean isLandscape = context.getResources().getConfiguration().orientation == 2;
        if (subtabContainerViewWidth != 0) {
            this.mWidthPixels = subtabContainerViewWidth;
        } else {
            DisplayMetrics dp = context.getResources().getDisplayMetrics();
            if (isLandscape) {
                this.mWidthPixels = (Math.max(dp.widthPixels, dp.heightPixels) * 8) / 12;
            } else {
                this.mWidthPixels = Math.min(dp.widthPixels, dp.heightPixels);
            }
        }
        LayoutParams lp = (LayoutParams) this.mSubTabContentView.getLayoutParams();
        lp.width = this.mWidthPixels;
        this.mSubTabContentView.setLayoutParams(lp);
    }

    private int getDimensForScaleMode() {
        switch (-getandroid-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues()[HwWidgetFactory.getDisplayMode(this.mContext).ordinal()]) {
            case ViewDragHelper.STATE_DRAGGING /*1*/:
                return 34472114;
            case ViewDragHelper.STATE_SETTLING /*2*/:
                return 34472113;
            case ViewDragHelper.DIRECTION_ALL /*3*/:
                return 34472112;
            default:
                return 34472067;
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int textviewPaddingHeight = 0;
        int textviewLineHeight = 0;
        int measuredHeight = 0;
        int childCount = this.mSubTabContentView.getChildCount();
        if (childCount > 0) {
            int j;
            int padding;
            int totalW = this.mWidthPixels;
            int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
            if (sizeWidth < totalW && sizeWidth > 0) {
                totalW = sizeWidth;
            }
            int cellSizeRemaining = 0;
            int avgCellSizeRemaining = 0;
            int minChildCountWhenFilled = this.mContext.getResources().getInteger(34537483);
            if (childCount > 1 && childCount < minChildCountWhenFilled) {
                cellSizeRemaining = totalW - ((totalW * childCount) / minChildCountWhenFilled);
                avgCellSizeRemaining = cellSizeRemaining / childCount;
                totalW = (totalW * childCount) / minChildCountWhenFilled;
            }
            int childWidthSize = totalW / childCount;
            boolean hasLongText = false;
            int maxDiff = 0;
            for (j = 0; j < childCount; j++) {
                TextView child = (SubTabView) this.mSubTabContentView.getChildAt(j);
                if (child != null) {
                    TextView tv = child;
                    padding = child.getPaddingStart() + child.getPaddingEnd();
                    textviewPaddingHeight = child.getPaddingTop() + child.getPaddingBottom();
                    if (getSelectedSubTab() == child.getSubTab()) {
                        child.setTypeface(this.mMedium);
                    } else {
                        child.setTypeface(this.mRegular);
                    }
                    child.setSingleLine(true);
                    child.setMaxLines(1);
                    child.measure(0, 0);
                    int measuredWidth = child.getMeasuredWidth() + padding;
                    measuredHeight = child.getMeasuredHeight();
                    if (measuredWidth > childWidthSize) {
                        maxDiff = Math.max(measuredWidth - childWidthSize, maxDiff);
                    }
                }
            }
            if (maxDiff > 0 && cellSizeRemaining > 0) {
                if (maxDiff <= avgCellSizeRemaining) {
                    totalW += childCount * maxDiff;
                } else {
                    totalW += childCount * avgCellSizeRemaining;
                }
                childWidthSize = totalW / childCount;
                for (j = 0; j < childCount; j++) {
                    SubTabView child2 = (SubTabView) this.mSubTabContentView.getChildAt(j);
                    if (child2 != null) {
                        SubTabView subTabView = child2;
                        padding = child2.getPaddingStart() + child2.getPaddingEnd();
                        child2.measure(0, 0);
                        textviewLineHeight = child2.getLayout().getHeight() + this.mTextHeightOffset;
                        if (child2.getMeasuredWidth() + padding > childWidthSize) {
                            child2.setSingleLine(false);
                            child2.setMaxLines(2);
                            hasLongText = true;
                            textviewLineHeight *= 2;
                        }
                    }
                }
            }
            LayoutParams lp = (LayoutParams) this.mSubTabContentView.getLayoutParams();
            lp.width = totalW;
            if (hasLongText && textviewLineHeight > measuredHeight) {
                lp.height = textviewLineHeight + textviewPaddingHeight;
            }
            this.mSubTabContentView.setLayoutParams(lp);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initDividerDrawable() {
        int dividerRes;
        if (this.mIsLightStyle) {
            dividerRes = 33751575;
        } else {
            dividerRes = 33751576;
        }
        Drawable divider = null;
        if (!(dividerRes == 0 || this.mContext == null)) {
            divider = this.mContext.getResources().getDrawable(dividerRes);
        }
        if (divider != null) {
            this.mSubTabContentView.setShowDividers(2);
            this.mSubTabContentView.setDividerDrawable(divider);
        }
    }

    public void setBackgroundStyle(int style) {
        boolean z = false;
        if (style == 0) {
            z = true;
        }
        this.mIsLightStyle = z;
        for (int pos = 0; pos < getSubTabCount(); pos++) {
            ((SubTabView) this.mSubTabContentView.getChildAt(pos)).setBackgroundStyle(style);
        }
        initDividerDrawable();
        invalidate();
    }

    public void addSubTab(SubTab subTab, int position, boolean setSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LayoutParams lp = new LayoutParams(0, -1, 1.0f);
        if (SystemProperties.getRTLFlag() && position == 0) {
            lp.setMarginStart(3);
        }
        subTabView.setLayoutParams(lp);
        this.mSubTabContentView.addView(subTabView, position, lp);
        subTab.setPosition(position);
        updateSubTabPosition(position, getSubTabCount(), true);
        if (setSelected) {
            subTab.select();
            subTabView.setSelected(true);
            this.mLastPos = position;
        }
    }

    public void addSubTab(SubTab subTab, boolean setSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LayoutParams lp = new LayoutParams(0, -1, 1.0f);
        if (SystemProperties.getRTLFlag() && getSubTabCount() == 0) {
            lp.setMarginStart(3);
        }
        subTabView.setLayoutParams(lp);
        this.mSubTabContentView.addView(subTabView, lp);
        subTab.setPosition(getSubTabCount() - 1);
        if (setSelected) {
            subTab.select();
            subTabView.setSelected(true);
            this.mLastPos = getSubTabCount() - 1;
        }
    }

    public Parcelable onSaveInstanceState() {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            return super.onSaveInstanceState();
        }
        int position = getSelectedSubTabPostion();
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mSavedPosition = position;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int position = ss.mSavedPosition;
        if (position >= 0 && position < getSubTabCount()) {
            getSubTabAt(position).select();
            ((SubTabView) this.mSubTabContentView.getChildAt(position)).setSelected(true);
            this.mLastPos = position;
        }
    }

    public void setClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    public SubTab getSelectedSubTab() {
        return this.mSelectedSubTab;
    }

    public int getSelectedSubTabPostion() {
        for (int i = 0; i < getSubTabCount(); i++) {
            if (this.mSelectedSubTab == getSubTabAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public void setSubTabSelected(int position) {
        this.mSelectedSubTab = getSubTabAt(position);
        setSubTabSelectedInner(position);
        if (this.mLastPos != position) {
            this.mLastSubTab = this.mLastPos;
            this.mIsSetSubTab = true;
        }
        this.mLastPos = position;
        requestLayout();
    }

    public void updateSubTabPosition(int start, int end, boolean isAdd) {
        SubTab child;
        if (isAdd) {
            for (int i = start + 1; i < end; i++) {
                child = getSubTabAt(i);
                if (child != null) {
                    child.setPosition(i);
                }
            }
            return;
        }
        for (int j = start; j < end; j++) {
            child = getSubTabAt(j);
            if (child != null) {
                child.setPosition(j);
            }
        }
    }

    private void setSubTabSelectedInner(int position) {
        int subTabCount = this.mSubTabContentView.getChildCount();
        initSubTabColor(position, subTabCount);
        int i = 0;
        while (i < subTabCount) {
            this.mSubTabContentView.getChildAt(i).setSelected(i == position);
            i++;
        }
    }

    public SubTab getSubTabAt(int position) {
        View view = this.mSubTabContentView.getChildAt(position);
        if (view != null) {
            return ((SubTabView) view).getSubTab();
        }
        return null;
    }

    public int getSubTabCount() {
        return this.mSubTabContentView.getChildCount();
    }

    public SubTab newSubTab() {
        return new SubTab(this);
    }

    public SubTab newSubTab(CharSequence text) {
        return new SubTab(this, text);
    }

    public SubTab newSubTab(CharSequence text, SubTabListener callback, Object tag) {
        return new SubTab(this, text, callback, tag);
    }

    public void removeSubTab(SubTab subTab) {
        removeSubTabAt(subTab.getPosition());
    }

    public void removeSubTabAt(int position) {
        if (this.mSubTabContentView != null) {
            SubTab subTab = getSubTabAt(position);
            if (subTab != null) {
                subTab.setPosition(-1);
            }
            this.mSubTabContentView.removeViewAt(position);
            if (getSubTabCount() == 0) {
                this.mSelectedSubTab = null;
            }
            updateSubTabPosition(position, getSubTabCount(), false);
            if (subTab == this.mSelectedSubTab) {
                selectSubTab(getSubTabAt(Math.max(0, position - 1)));
            }
        }
    }

    public void removeAllSubTabs() {
        if (this.mSubTabContentView != null) {
            this.mSubTabContentView.removeAllViews();
            this.mSelectedSubTab = null;
        }
    }

    public void selectSubTab(SubTab subTab) {
        boolean changed = false;
        FragmentTransaction trans = null;
        if (this.mContext instanceof Activity) {
            trans = ((Activity) this.mContext).getFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        if (this.mSelectedSubTab != subTab) {
            int position;
            changed = true;
            if (subTab != null) {
                position = subTab.getPosition();
            } else {
                position = -1;
            }
            setSubTabSelectedInner(position);
            if (this.mSelectedSubTab != null) {
                this.mSelectedSubTab.getCallback().onSubTabUnselected(this.mSelectedSubTab, trans);
            }
            this.mSelectedSubTab = subTab;
            if (this.mSelectedSubTab != null) {
                this.mSelectedSubTab.getCallback().onSubTabSelected(this.mSelectedSubTab, trans);
            }
        } else if (this.mSelectedSubTab != null) {
            this.mSelectedSubTab.getCallback().onSubTabReselected(this.mSelectedSubTab, trans);
        }
        if (!(trans == null || trans.isEmpty())) {
            trans.commit();
        }
        if (changed) {
            requestLayout();
        }
    }

    public void updateSubTab(int position) {
        SubTabView subTabView = (SubTabView) this.mSubTabContentView.getChildAt(position);
        if (subTabView != null) {
            subTabView.update();
        }
    }

    private SubTabView createSubTabView(SubTab subTab) {
        return createSubTabView(subTab, this.mIsLightStyle);
    }

    private SubTabView createSubTabView(SubTab subTab, boolean isLightStyle) {
        SubTabView subTabView = new SubTabView(getContext(), subTab, isLightStyle);
        subTabView.setFocusable(true);
        if (this.mSubTabClickListener == null) {
            this.mSubTabClickListener = new SubTabClickListener();
        }
        subTabView.setOnClickListener(this.mSubTabClickListener);
        return subTabView;
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mSubTabAnim.isAnimEnd()) {
            int childCount = this.mSubTabContentView.getChildCount();
            if (childCount == 1) {
                int leftRightRes;
                if (this.mIsLightStyle) {
                    leftRightRes = 33751584;
                } else {
                    leftRightRes = 33751585;
                }
                this.mSubTabContentView.getChildAt(0).setBackgroundResource(leftRightRes);
            } else if (!this.mIsInitSubTabColorDone) {
                initSubTabColor(this.mLastPos, childCount);
                this.mIsInitSubTabColorDone = true;
            }
        }
    }

    private void initSubTabColor(int selected, int cnt) {
        Drawable[] array = new Drawable[2];
        for (int i = 0; i < cnt; i++) {
            SubTabView child = (SubTabView) this.mSubTabContentView.getChildAt(i);
            updateSubTabColorRes(this.mContext, array, i, 0, cnt - 1);
            if (selected == i) {
                array[1].setAlpha(IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN);
                child.updateTitleAppearance(1);
            } else {
                array[1].setAlpha(0);
                child.updateTitleAppearance(0);
            }
            LayerDrawable ld = new LayerDrawable(array);
            ld.setLayerInset(1, -this.mSubTabAnim.mSecondLayerInsetLeft, -this.mSubTabAnim.mSecondLayerInsetTop, -this.mSubTabAnim.mSecondLayerInsetRight, -this.mSubTabAnim.mSecondLayerInsetBottom);
            child.setBackgroundDrawable(ld);
        }
    }

    private static void updateSubTabColorRes(Context context, Drawable[] array, int index, int first, int last) {
        if (index == first) {
            if (SystemProperties.getRTLFlag()) {
                array[0] = context.getResources().getDrawable(33751588);
                array[1] = context.getResources().getDrawable(33751574);
            } else {
                array[0] = context.getResources().getDrawable(33751586);
                array[1] = context.getResources().getDrawable(33751572);
            }
        } else if (index != last) {
            array[0] = context.getResources().getDrawable(33751587);
            array[1] = context.getResources().getDrawable(33751573);
        } else if (SystemProperties.getRTLFlag()) {
            array[0] = context.getResources().getDrawable(33751586);
            array[1] = context.getResources().getDrawable(33751572);
        } else {
            array[0] = context.getResources().getDrawable(33751588);
            array[1] = context.getResources().getDrawable(33751574);
        }
        array[0].setTint(context.getResources().getColor(33882246));
        array[1].setTint(context.getResources().getColor(33882246));
    }
}
