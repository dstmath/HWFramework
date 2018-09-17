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
import android.graphics.Point;
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
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidhwext.R;

public class SubTabWidget extends LinearLayout {
    private static final /* synthetic */ int[] -android-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues = null;
    private static final String TAG = "SubTabWidget";
    private boolean mClickable;
    private Context mContext;
    private boolean mIsEmphasizeStyle;
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
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int mSavedPosition;

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

        public SubTab(SubTabWidget this$0) {
            this(null, null, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text) {
            this(text, null, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, SubTabListener callback) {
            this(text, callback, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, Object tag) {
            this(text, null, tag);
        }

        public SubTab(CharSequence text, SubTabListener callback, Object tag) {
            this.mPosition = -1;
            this.mSubTabId = -1;
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
            SubTabWidget.this.selectSubTab(this);
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public SubTab setSubTabListener(SubTabListener callback) {
            this.mCallback = callback;
            return this;
        }

        public SubTab setText(int resId) {
            return setText(SubTabWidget.this.getContext().getResources().getText(resId));
        }

        public SubTab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                SubTabWidget.this.updateSubTab(this.mPosition);
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
        private int mDuration = 150;
        private boolean mIsAnimEnd;
        private int mSecondLayerInsetBottom;
        private int mSecondLayerInsetLeft;
        private int mSecondLayerInsetRight;
        private int mSecondLayerInsetTop;
        private LinearLayout mView;

        SubTabAnim(LinearLayout view, Context context) {
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

        private void anim(final int from, final int to, final int cnt) {
            this.mAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mAnimator.setDuration((long) this.mDuration).start();
            this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    if (value.floatValue() == 0.0f) {
                        SubTabAnim.this.switchTextColor(from, to);
                    }
                    SubTabAnim.this.updateSubTabColor(from, to, (int) (255.0f - (value.floatValue() * 255.0f)), (int) (value.floatValue() * 255.0f), cnt);
                }
            });
            this.mAnimator.addListener(new AnimatorListener() {
                public void onAnimationCancel(Animator animation) {
                    SubTabAnim.this.updateSubTabColor(from, to, 0, 255, cnt);
                }

                public void onAnimationEnd(Animator animation) {
                    SubTabAnim.this.updateSubTabColor(from, to, 0, 255, cnt);
                    SubTabAnim.this.mIsAnimEnd = true;
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationStart(Animator animation) {
                }
            });
        }
    }

    private class SubTabClickListener implements OnClickListener {
        /* synthetic */ SubTabClickListener(SubTabWidget this$0, SubTabClickListener -this1) {
            this();
        }

        private SubTabClickListener() {
        }

        public void onClick(View view) {
            if (SubTabWidget.this.mClickable) {
                if (view instanceof SubTabView) {
                    ((SubTabView) view).getSubTab().select();
                }
                int subTabCount = SubTabWidget.this.mSubTabContentView.getChildCount();
                for (int i = 0; i < subTabCount; i++) {
                    boolean z;
                    View child = SubTabWidget.this.mSubTabContentView.getChildAt(i);
                    if (child == view) {
                        z = true;
                    } else {
                        z = false;
                    }
                    child.setSelected(z);
                    if (child == view) {
                        int lastPos = SubTabWidget.this.mLastPos;
                        if (SubTabWidget.this.mIsSetSubTab && SubTabWidget.this.mLastSubTab != -1) {
                            lastPos = SubTabWidget.this.mLastSubTab;
                        }
                        SubTabWidget.this.mSubTabAnim.startAnim(lastPos, i, subTabCount);
                        SubTabWidget.this.mIsSetSubTab = false;
                        SubTabWidget.this.mLastPos = i;
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
        private boolean mIsEmphasizeStyleInnner;
        private boolean mIsLightStyleInner;
        private SubTab mSubTab;

        public SubTabView(Context context, SubTab subTab, boolean isLightStyle, boolean isEmphasizeStyle) {
            super(context, null, 33620030);
            this.mSubTab = subTab;
            this.mIsLightStyleInner = isLightStyle;
            this.mIsEmphasizeStyleInnner = isEmphasizeStyle;
            update();
        }

        public SubTab getSubTab() {
            return this.mSubTab;
        }

        public void update() {
            CharSequence text = this.mSubTab.getText();
            if (TextUtils.isEmpty(text) ^ 1) {
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
                    colorId = 33882257;
                } else if (this.mIsEmphasizeStyleInnner) {
                    colorId = 33882265;
                } else {
                    colorId = 33882261;
                }
                setTypeface(Typeface.defaultFromStyle(0));
            } else {
                if (this.mIsLightStyleInner) {
                    colorId = 33882254;
                } else if (this.mIsEmphasizeStyleInnner) {
                    colorId = 33882262;
                } else {
                    colorId = 33882258;
                }
                setTypeface(Typeface.defaultFromStyle(1));
            }
            setTextColor(this.mContext.getResources().getColor(colorId));
        }

        private void initTitleAppearance() {
            int colorRes;
            if (this.mIsLightStyleInner) {
                colorRes = 33882428;
            } else if (this.mIsEmphasizeStyleInnner) {
                colorRes = 33882430;
            } else {
                colorRes = 33882429;
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
        this(context, attrs, 33620029);
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
        this.mTextHeightOffset = context.getResources().getDimensionPixelSize(34472106);
        this.mSubTabContentView = (LinearLayout) inflater.inflate(34013265, this, true).findViewById(34603094);
        this.mIsLightStyle = HwWidgetFactory.isHwLightTheme(context);
        this.mIsEmphasizeStyle = HwWidgetFactory.isHwEmphasizeTheme(context);
        this.mContext = context;
        initDividerDrawable();
        this.mSubTabContentView.setDividerPadding(dividerPadding);
        a.recycle();
        updateTabViewContainerWidth(context);
        this.mRegular = Typeface.create("sans-serif", 0);
        this.mMedium = Typeface.create("HwChinese-medium", 0);
        this.mSubTabAnim = new SubTabAnim(this.mSubTabContentView, this.mContext);
        this.mIsInitSubTabColorDone = false;
        this.mSubTabAnim.mSecondLayerInsetLeft = context.getResources().getDimensionPixelSize(34472172);
        this.mSubTabAnim.mSecondLayerInsetTop = context.getResources().getDimensionPixelSize(34472173);
        this.mSubTabAnim.mSecondLayerInsetRight = context.getResources().getDimensionPixelSize(34472174);
        this.mSubTabAnim.mSecondLayerInsetBottom = context.getResources().getDimensionPixelSize(34472175);
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
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point useSize = new Point();
        Point realSize = new Point();
        display.getSize(useSize);
        display.getRealSize(realSize);
        if (realSize.x < realSize.y) {
            this.mWidthPixels = subtabContainerViewWidth;
        } else if (useSize.y >= useSize.x) {
            this.mWidthPixels = subtabContainerViewWidth;
        } else if (useSize.x > useSize.y) {
            this.mWidthPixels = (useSize.x * 8) / 12;
        }
        LayoutParams lp = (LayoutParams) this.mSubTabContentView.getLayoutParams();
        lp.width = this.mWidthPixels;
        this.mSubTabContentView.setLayoutParams(lp);
    }

    private int getDimensForScaleMode() {
        switch (-getandroid-hwcontrol-HwWidgetFactory$DisplayModeSwitchesValues()[HwWidgetFactory.getDisplayMode(this.mContext).ordinal()]) {
            case 1:
                return 34472105;
            case 2:
                return 34472104;
            case 3:
                return 34472103;
            default:
                return 34472058;
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
            int minChildCountWhenFilled = this.mContext.getResources().getInteger(34275330);
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
                    if (measuredWidth > childWidthSize && measuredWidth - childWidthSize > maxDiff) {
                        maxDiff = measuredWidth - childWidthSize;
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
        int dividerRes = this.mIsLightStyle ? 33751342 : this.mIsEmphasizeStyle ? 33751344 : 33751343;
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
        int subTabCount = getSubTabCount();
        for (int pos = 0; pos < subTabCount; pos++) {
            ((SubTabView) this.mSubTabContentView.getChildAt(pos)).setBackgroundStyle(style);
        }
        initDividerDrawable();
        invalidate();
    }

    public void addSubTab(SubTab subTab, int position, boolean setSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LayoutParams lp = new LayoutParams(0, -1, 1.0f);
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
        int subTabCount = getSubTabCount();
        for (int i = 0; i < subTabCount; i++) {
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
        return new SubTab(text, callback, tag);
    }

    public void removeSubTab(SubTab subTab) {
        removeSubTabAt(subTab.getPosition());
    }

    public void removeSubTabAt(int position) {
        int i = 0;
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
                if (position - 1 > 0) {
                    i = position - 1;
                }
                selectSubTab(getSubTabAt(i));
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
        if (!(trans == null || (trans.isEmpty() ^ 1) == 0)) {
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
        return createSubTabView(subTab, this.mIsLightStyle, this.mIsEmphasizeStyle);
    }

    private SubTabView createSubTabView(SubTab subTab, boolean isLightStyle, boolean isEmphasizeStyle) {
        SubTabView subTabView = new SubTabView(getContext(), subTab, isLightStyle, isEmphasizeStyle);
        subTabView.setFocusable(true);
        if (this.mSubTabClickListener == null) {
            this.mSubTabClickListener = new SubTabClickListener(this, null);
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
                if (this.mIsLightStyle || this.mIsEmphasizeStyle) {
                    leftRightRes = 33751351;
                } else {
                    leftRightRes = 33751352;
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
                array[1].setAlpha(255);
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
                array[0] = context.getResources().getDrawable(33751694);
                array[1] = context.getResources().getDrawable(33751690);
            } else {
                array[0] = context.getResources().getDrawable(33751692);
                array[1] = context.getResources().getDrawable(33751688);
            }
        } else if (index != last) {
            array[0] = context.getResources().getDrawable(33751693);
            array[1] = context.getResources().getDrawable(33751689);
        } else if (SystemProperties.getRTLFlag()) {
            array[0] = context.getResources().getDrawable(33751692);
            array[1] = context.getResources().getDrawable(33751688);
        } else {
            array[0] = context.getResources().getDrawable(33751694);
            array[1] = context.getResources().getDrawable(33751690);
        }
        array[0].setTint(context.getResources().getColor(33882449));
        array[1].setTint(context.getResources().getColor(33882450));
    }
}
