package huawei.android.widget;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HwKeyEventDetector;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.SubTabViewContainer;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.android.widget.plume.HwPlumeManager;

public class SubTabWidget extends LinearLayout {
    private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
    private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
    private static final int INVALID_POSITION = -1;
    private static final String TAG = "SubTabWidget";
    private HwBlurEngine mBlurEngine;
    private int mBlurOverlayColor;
    private HwBlurEngine.BlurType mBlurType;
    private ImageView mFunctionView;
    private HwKeyEventDetector mHwKeyEventDetector;
    private boolean mIsBlurEnable;
    private boolean mIsClickable;
    private boolean mIsGlobalNextTabEnable;
    private boolean mIsNextTabEnable;
    private boolean mIsSetSubTab;
    private int mLastPos;
    private int mLastSubTab;
    private Typeface mMedium;
    private HwOnGlobalNextTabEventListener mOnGlobalNextTabListener;
    private HwOnNextTabEventListener mOnNextTabListener;
    private Typeface mRegular;
    private SubTab mSelectedSubTab;
    private SubTabViewContainer.SlidingTabStrip mSlidingTabStrip;
    private SubTabClickListener mSubTabClickListener;
    private SubTabViewContainer mSubTabContainer;
    public LinearLayout mSubTabContentView;
    private int mSubTabItemMargin;

    public interface SubTabListener {
        void onSubTabReselected(SubTab subTab, FragmentTransaction fragmentTransaction);

        void onSubTabSelected(SubTab subTab, FragmentTransaction fragmentTransaction);

        void onSubTabUnselected(SubTab subTab, FragmentTransaction fragmentTransaction);
    }

    public SubTabWidget(Context context) {
        this(context, null);
    }

    public SubTabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "subTabBarStyle"));
    }

    public SubTabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsClickable = true;
        this.mIsSetSubTab = false;
        this.mLastSubTab = -1;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.mIsBlurEnable = false;
        this.mBlurOverlayColor = INVALID_BLUR_OVERLAY_COLOR;
        this.mBlurType = HwBlurEngine.BlurType.LightBlurWithGray;
        this.mIsNextTabEnable = true;
        this.mIsGlobalNextTabEnable = true;
        this.mHwKeyEventDetector = null;
        this.mOnNextTabListener = new HwOnNextTabEventListener() {
            /* class huawei.android.widget.SubTabWidget.AnonymousClass1 */

            public boolean onNextTab(int action, KeyEvent event) {
                if (action == 1) {
                    SubTabWidget.this.changeToNextProc();
                }
                return true;
            }
        };
        this.mOnGlobalNextTabListener = new HwOnGlobalNextTabEventListener() {
            /* class huawei.android.widget.SubTabWidget.AnonymousClass2 */

            public boolean onGlobalNextTab(int action, KeyEvent event) {
                if (action != 0) {
                    return true;
                }
                SubTabWidget.this.changeToNextProc();
                return true;
            }
        };
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        int layoutResId = ResLoaderUtil.getLayoutId(context, "sub_tab_content");
        if (layoutResId != 0) {
            View view = inflater.inflate(layoutResId, (ViewGroup) this, true);
            this.mSubTabContainer = (SubTabViewContainer) view.findViewById(ResLoaderUtil.getViewId(context, "subTabViewContainer"));
            this.mFunctionView = (ImageView) view.findViewById(ResLoaderUtil.getViewId(context, "function_icon"));
            this.mSlidingTabStrip = this.mSubTabContainer.getmTabStrip();
            this.mSubTabContentView = this.mSlidingTabStrip;
            setOrientation(0);
            this.mMedium = Typeface.create("HwChinese-medium", 0);
            this.mRegular = Typeface.create("sans-serif", 0);
            this.mSlidingTabStrip.setSelectedIndicatorMargin(ResLoaderUtil.getDimensionPixelSize(context, "sub_tab_indicator_margin"));
            Resources.Theme theme = ResLoader.getInstance().getTheme(context);
            int[] themeAttrs = ResLoader.getInstance().getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, TAG);
            if (theme != null && themeAttrs != null) {
                TypedArray array = theme.obtainStyledAttributes(attrs, themeAttrs, defStyle, 0);
                this.mSlidingTabStrip.setSelectedIndicatorHeight(array.getDimensionPixelOffset(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_insetBottom"), 0));
                this.mSlidingTabStrip.setSelectedIndicatorColor(array.getColor(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_borderColor"), 0));
                this.mSubTabItemMargin = array.getDimensionPixelOffset(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_middleWidth"), 0);
                this.mSubTabContainer.setSubTabItemMargin(this.mSubTabItemMargin);
                HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(array.getInteger(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_subTabBlurType"), DEFAULT_BLUR_TYPE));
                if (blurType != null) {
                    this.mBlurType = blurType;
                }
                this.mBlurOverlayColor = array.getColor(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_subTabBlurColor"), INVALID_BLUR_OVERLAY_COLOR);
                array.recycle();
            }
        }
        this.mHwKeyEventDetector = HwWidgetFactory.getKeyEventDetector(context);
        setValueFromPlume();
    }

    private void setValueFromPlume() {
        if (HwPlumeManager.isPlumeUsed(this.mContext)) {
            setExtendedNextTabEnabled(true, HwPlumeManager.getInstance(this.mContext).getDefault(this, "switchTabEnabled", true));
            setExtendedNextTabEnabled(false, HwPlumeManager.getInstance(this.mContext).getDefault(this, "switchTabWhenFocusedEnabled", true));
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setBackgroundStyle(int style) {
    }

    public void addSubTab(SubTab subTab, int position, boolean isSetSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -1);
        layoutParams.setMarginStart(this.mSubTabItemMargin);
        layoutParams.setMarginEnd(this.mSubTabItemMargin);
        subTabView.setLayoutParams(layoutParams);
        this.mSlidingTabStrip.addView(subTabView, position, layoutParams);
        subTab.setPosition(position);
        updateSubTabPosition(position, getSubTabCount(), true);
        if (isSetSelected) {
            subTab.select();
            subTabView.setSelected(true);
            this.mLastPos = position;
        }
    }

    public void addSubTab(SubTab subTab, boolean isSetSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -1);
        layoutParams.setMarginStart(this.mSubTabItemMargin);
        layoutParams.setMarginEnd(this.mSubTabItemMargin);
        subTabView.setLayoutParams(layoutParams);
        this.mSlidingTabStrip.addView(subTabView, layoutParams);
        subTab.setPosition(getSubTabCount() - 1);
        if (isSetSelected) {
            subTab.select();
            subTabView.setSelected(true);
            this.mLastPos = getSubTabCount() - 1;
        }
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            return super.onSaveInstanceState();
        }
        int position = getSelectedSubTabPostion();
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mSavedPosition = position;
        return savedState;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            super.onRestoreInstanceState(state);
        } else if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            int position = savedState.mSavedPosition;
            if (position >= 0 && position < getSubTabCount()) {
                SubTab subTab = getSubTabAt(position);
                if (subTab != null) {
                    subTab.select();
                }
                View view = this.mSlidingTabStrip.getChildAt(position);
                SubTabView subTabView = null;
                if (view instanceof SubTabView) {
                    subTabView = (SubTabView) view;
                }
                if (subTabView != null) {
                    subTabView.setSelected(true);
                }
                this.mLastPos = position;
            }
        }
    }

    @Override // android.view.View
    public void setClickable(boolean isClickable) {
        this.mIsClickable = isClickable;
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
        SubTab subTab = getSubTabAt(position);
        SubTab subTab2 = this.mSelectedSubTab;
        if (!((subTab2 != null && subTab2.getPosition() != -1) || subTab == null || subTab.getPosition() == -1)) {
            this.mSubTabContainer.setScrollPosition(subTab.getPosition(), 0.0f);
        }
        this.mSelectedSubTab = subTab;
        setSubTabSelectedInner(position);
        int i = this.mLastPos;
        if (i != position) {
            this.mLastSubTab = i;
            this.mIsSetSubTab = true;
        }
        this.mLastPos = position;
    }

    public void updateSubTabPosition(int start, int end, boolean isAdd) {
        if (isAdd) {
            for (int i = start + 1; i < end; i++) {
                SubTab child = getSubTabAt(i);
                if (child != null) {
                    child.setPosition(i);
                }
            }
            return;
        }
        for (int j = start; j < end; j++) {
            SubTab child2 = getSubTabAt(j);
            if (child2 != null) {
                child2.setPosition(j);
            }
        }
    }

    private void setSubTabSelectedInner(int position) {
        int subTabCount = this.mSlidingTabStrip.getChildCount();
        if (HwWidgetFactory.isEmuiSuperLite()) {
            this.mSubTabContainer.setScrollPosition(position, 0.0f);
            Log.i(TAG, "SHOW: SubTab Indicator with no animation");
        }
        int i = 0;
        while (i < subTabCount) {
            View view = this.mSlidingTabStrip.getChildAt(i);
            if (view instanceof TextView) {
                TextView child = (TextView) view;
                boolean isSelected = i == position;
                if (i == position) {
                    child.setTypeface(this.mMedium);
                } else {
                    child.setTypeface(this.mRegular);
                }
                child.setSelected(isSelected);
            }
            i++;
        }
    }

    public SubTab getSubTabAt(int position) {
        View view = this.mSlidingTabStrip.getChildAt(position);
        if (view instanceof SubTabView) {
            return ((SubTabView) view).getSubTab();
        }
        return null;
    }

    public int getSubTabCount() {
        return this.mSlidingTabStrip.getChildCount();
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
        if (this.mSlidingTabStrip != null) {
            SubTab subTab = getSubTabAt(position);
            if (subTab != null) {
                subTab.setPosition(-1);
            }
            this.mSlidingTabStrip.removeViewAt(position);
            if (getSubTabCount() == 0) {
                this.mSelectedSubTab = null;
            }
            int i = 0;
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
        SubTabViewContainer.SlidingTabStrip slidingTabStrip = this.mSlidingTabStrip;
        if (slidingTabStrip != null) {
            slidingTabStrip.removeAllViews();
            this.mSelectedSubTab = null;
        }
    }

    public class SubTab {
        public static final int INVALID_POSITION = -1;
        private SubTabListener mCallback;
        private int mPosition;
        private int mSubTabId;
        private Object mTag;
        private CharSequence mText;

        public SubTab(SubTabWidget this$02) {
            this(null, null, null);
        }

        public SubTab(SubTabWidget this$02, CharSequence text) {
            this(text, null, null);
        }

        public SubTab(SubTabWidget this$02, CharSequence text, SubTabListener callback) {
            this(text, callback, null);
        }

        public SubTab(SubTabWidget this$02, CharSequence text, Object tag) {
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
            int i = this.mPosition;
            if (i >= 0) {
                SubTabWidget.this.updateSubTab(i);
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

    public void selectSubTab(SubTab subTab) {
        FragmentTransaction trans = null;
        if (this.mContext instanceof Activity) {
            trans = ((Activity) this.mContext).getFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        SubTab subTab2 = this.mSelectedSubTab;
        int i = -1;
        if (!((subTab2 != null && subTab2.getPosition() != -1) || subTab == null || subTab.getPosition() == -1)) {
            this.mSubTabContainer.setScrollPosition(subTab.getPosition(), 0.0f);
        }
        SubTab subTab3 = this.mSelectedSubTab;
        if (subTab3 != subTab) {
            if (subTab != null) {
                i = subTab.getPosition();
            }
            setSubTabSelectedInner(i);
            SubTab subTab4 = this.mSelectedSubTab;
            if (subTab4 != null) {
                subTab4.getCallback().onSubTabUnselected(this.mSelectedSubTab, trans);
            }
            this.mSelectedSubTab = subTab;
            SubTab subTab5 = this.mSelectedSubTab;
            if (subTab5 != null) {
                subTab5.getCallback().onSubTabSelected(this.mSelectedSubTab, trans);
            }
        } else if (subTab3 != null) {
            subTab3.getCallback().onSubTabReselected(this.mSelectedSubTab, trans);
        }
        if (trans != null && !trans.isEmpty()) {
            trans.commit();
        }
    }

    /* access modifiers changed from: private */
    public class SubTabView extends TextView {
        private SubTab mSubTab;

        SubTabView(Context context, SubTab subTab) {
            super(context, null, ResLoader.getInstance().getIdentifier(context, "attr", "subTabViewStyle"));
            this.mSubTab = subTab;
            updateInternal();
            setClickable(true);
            setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, ResLoader.getInstance().getIdentifier(context, "attr", "subTabViewStyle")));
        }

        public SubTab getSubTab() {
            return this.mSubTab;
        }

        private void updateInternal() {
            CharSequence text = this.mSubTab.getText();
            if (!TextUtils.isEmpty(text)) {
                setText(text);
                setVisibility(0);
            } else {
                setVisibility(8);
                setText((CharSequence) null);
            }
            if (this.mSubTab.getSubTabId() != -1) {
                setId(this.mSubTab.getSubTabId());
            }
        }

        public void update() {
            updateInternal();
        }

        @Override // android.widget.TextView, android.view.KeyEvent.Callback, android.view.View
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (SubTabWidget.this.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override // android.widget.TextView, android.view.KeyEvent.Callback, android.view.View
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (SubTabWidget.this.onKeyUp(keyCode, event)) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    public void updateSubTab(int position) {
        View view = this.mSlidingTabStrip.getChildAt(position);
        if (view instanceof SubTabView) {
            ((SubTabView) view).update();
        }
    }

    private SubTabView createSubTabView(SubTab subTab) {
        SubTabView subTabView = new SubTabView(getContext(), subTab);
        subTabView.setFocusable(true);
        if (this.mSubTabClickListener == null) {
            this.mSubTabClickListener = new SubTabClickListener();
        }
        subTabView.setOnClickListener(this.mSubTabClickListener);
        return subTabView;
    }

    /* access modifiers changed from: private */
    public class SubTabClickListener implements View.OnClickListener {
        private SubTabClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (SubTabWidget.this.mIsClickable) {
                int subTabCount = SubTabWidget.this.mSlidingTabStrip.getChildCount();
                for (int i = 0; i < subTabCount; i++) {
                    View child = SubTabWidget.this.mSlidingTabStrip.getChildAt(i);
                    child.setSelected(child == view);
                    if (child == view) {
                        int i2 = SubTabWidget.this.mLastPos;
                        if (SubTabWidget.this.mIsSetSubTab && SubTabWidget.this.mLastSubTab != -1) {
                            int lastPos = SubTabWidget.this.mLastSubTab;
                        }
                        SubTabWidget.this.mIsSetSubTab = false;
                        SubTabWidget.this.mLastPos = i;
                        SubTabWidget.this.mSubTabContainer.animateToTab(i);
                    }
                }
                if (view instanceof SubTabView) {
                    ((SubTabView) view).getSubTab().select();
                }
            }
        }
    }

    public void setSubTabScrollingOffsets(int position, float offset) {
        if (!HwWidgetFactory.isEmuiSuperLite()) {
            this.mSubTabContainer.setScrollPosition(position, offset);
            Log.i(TAG, "SHOW: SubTab Indicator with animation");
        }
    }

    private int getSubTabViewCount() {
        return this.mSubTabContentView.getChildCount();
    }

    /* access modifiers changed from: private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class huawei.android.widget.SubTabWidget.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private int mSavedPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            this.mSavedPosition = source.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mSavedPosition);
        }
    }

    public void addFunctionMenu(int iconRes, View.OnClickListener clickListener) {
        ImageView imageView = this.mFunctionView;
        if (imageView != null) {
            imageView.setVisibility(0);
            this.mFunctionView.setImageResource(iconRes);
            this.mFunctionView.setOnClickListener(clickListener);
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.mBlurEngine.isShowHwBlur(this)) {
            this.mBlurEngine.draw(canvas, this);
            super.dispatchDraw(canvas);
            return;
        }
        super.draw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            this.mBlurEngine.addBlurTargetView(this, this.mBlurType);
            this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
            int i = this.mBlurOverlayColor;
            if (i != INVALID_BLUR_OVERLAY_COLOR) {
                this.mBlurEngine.setTargetViewOverlayColor(this, i);
                return;
            }
            return;
        }
        this.mBlurEngine.removeBlurTargetView(this);
    }

    public boolean isBlurEnable() {
        return this.mIsBlurEnable;
    }

    public void setBlurEnable(boolean isBlurEnable) {
        this.mIsBlurEnable = isBlurEnable;
        this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
    }

    private void setBlurColor(int blurColor) {
        this.mBlurOverlayColor = blurColor;
    }

    private void setBlurType(int blurTypeId) {
        HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId);
        if (blurType != null) {
            this.mBlurType = blurType;
        }
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null || !hwKeyEventDetector.onKeyEvent(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null || !hwKeyEventDetector.onKeyEvent(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean changeToNextProc() {
        if (this.mSubTabContentView == null) {
            return false;
        }
        int pos = getSelectedSubTabPostion();
        int count = getSubTabCount();
        if (count <= 0) {
            return false;
        }
        View view = this.mSubTabContentView.getChildAt((pos + 1) % count);
        if (view == null) {
            return true;
        }
        view.requestFocus();
        view.performClick();
        return true;
    }

    public void setExtendedNextTabEnabled(boolean isGlobal, boolean isEnabled) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            if (isGlobal) {
                if (isEnabled) {
                    hwKeyEventDetector.setOnGlobalNextTabListener(this, this.mOnGlobalNextTabListener);
                } else {
                    hwKeyEventDetector.setOnGlobalNextTabListener(this, (HwOnGlobalNextTabEventListener) null);
                }
                this.mIsGlobalNextTabEnable = isEnabled;
                return;
            }
            if (isEnabled) {
                hwKeyEventDetector.setOnNextTabListener(this.mOnNextTabListener);
            } else {
                hwKeyEventDetector.setOnNextTabListener((HwOnNextTabEventListener) null);
            }
            this.mIsNextTabEnable = isEnabled;
        }
    }

    public boolean isExtendedNextTabEnabled(boolean isGlobal) {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null) {
            return false;
        }
        if (isGlobal) {
            if (hwKeyEventDetector.getOnGlobalNextTabListener() != null) {
                return true;
            }
            return false;
        } else if (hwKeyEventDetector.getOnNextTabListener() != null) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            hwKeyEventDetector.onDetachedFromWindow();
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            if (this.mIsNextTabEnable) {
                hwKeyEventDetector.setOnNextTabListener(this.mOnNextTabListener);
            }
            if (this.mIsGlobalNextTabEnable) {
                this.mHwKeyEventDetector.setOnGlobalNextTabListener(this, this.mOnGlobalNextTabListener);
                return;
            }
            return;
        }
        Log.w(TAG, "onAttachedToWindow: mHwKeyEventDetector is null");
    }
}
