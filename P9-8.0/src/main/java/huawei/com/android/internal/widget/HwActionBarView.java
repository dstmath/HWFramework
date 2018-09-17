package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.rms.iaware.AppTypeInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.widget.ActionMenuPresenter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.widget.ActionBarView;
import com.android.internal.widget.ActionBarView.HomeView;
import com.huawei.hsm.permission.StubController;
import huawei.android.widget.HwActionMenuPresenter;

public class HwActionBarView extends ActionBarView {
    private static final String TAG = "HwActionBarView";
    private int mBaseEndMargin;
    private Drawable mIcon1Drawable;
    private ImageView mIcon1View;
    private boolean mIcon1Visible;
    private Drawable mIcon2Drawable;
    private ImageView mIcon2View;
    private boolean mIcon2Visible;
    private final Configuration mLastConfiguration = new Configuration();
    private OnClickListener mListener1;
    private OnClickListener mListener2;
    private int mPopupEndLocation;
    private int mPopupStartLocation;
    private int mResCancel;
    private int mResOK;
    private ButtonState mSavedState;
    private int mSplitViewMaxSize;
    private LinearLayout mTitleContainer;

    private static class ButtonState {
        public float mAlpha1;
        public float mAlpha2;
        public boolean mEnabled1;
        public boolean mEnabled2;
        public boolean mUsed1;
        public boolean mUsed2;

        /* synthetic */ ButtonState(ButtonState -this0) {
            this();
        }

        private ButtonState() {
        }
    }

    public static class HwHomeView extends HomeView {
        public HwHomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setShowUp(boolean isUp) {
            super.setShowUp(isUp);
            ColorStateList color = HwActionBarView.getImmersionTint(getContext());
            if (this.mUpView != null) {
                this.mUpView.setImageTintList(color);
            }
        }

        public void setShowIcon(boolean showIcon) {
            super.setShowIcon(false);
        }

        public int getStartOffset() {
            return 0;
        }

        protected void layoutUpView(View view, int upLeft, int upTop, int upRight, int upBottom, int leftMargin, int upOffset) {
            if (isRtlLocale()) {
                view.layout(upLeft - leftMargin, upTop, upRight - leftMargin, upBottom);
            } else {
                view.layout(upLeft + leftMargin, upTop, upOffset, upBottom);
            }
        }
    }

    public HwActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        if (!isTitleLayout(child)) {
            return super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        }
        invalidateTitleLayout();
        int maxSize = availableWidth;
        if (getUpGoerFive() == null || getUpGoerFive().getChildCount() != 0) {
            TextView title = getTitleView();
            if (title != null) {
                title.setTextSize(2, 18.0f);
                title.setSingleLine(true);
                title.setMaxLines(1);
            }
            child.measure(MeasureSpec.makeMeasureSpec(availableWidth, StubController.PERMISSION_ACCESS_BROWSER_RECORDS), childSpecHeight);
            if (title != null) {
                int normalWidth = title.getMeasuredWidth();
                int titleHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(childSpecHeight), AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                title.measure(0, titleHeightSpec);
                if (title.getMeasuredWidth() > normalWidth) {
                    title.setTextSize(2, 15.0f);
                    title.measure(0, titleHeightSpec);
                    if (title.getMeasuredWidth() > normalWidth) {
                        title.setTextSize(2, 12.0f);
                        title.measure(0, titleHeightSpec);
                        if (title.getMeasuredWidth() > normalWidth) {
                            title.setSingleLine(false);
                            title.setMaxLines(2);
                        }
                    }
                    child.measure(MeasureSpec.makeMeasureSpec(availableWidth, StubController.PERMISSION_ACCESS_BROWSER_RECORDS), childSpecHeight);
                }
            }
        } else {
            child.measure(MeasureSpec.makeMeasureSpec(availableWidth, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), childSpecHeight);
        }
        availableWidth = (availableWidth - child.getMeasuredWidth()) - spacing;
        if (availableWidth <= 0) {
            availableWidth = 0;
        }
        return availableWidth;
    }

    private boolean isTitleLayout(View child) {
        return child == getUpGoerFive() && getNavigationMode() == 0;
    }

    private int getMenuItemCountInActionBarView() {
        if (this.mMenuView == null || this.mMenuView.getParent() != this) {
            return 0;
        }
        return this.mMenuView.getChildCount() + 0;
    }

    protected LinearLayout initTitleLayout() {
        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(34013186, this, false);
        this.mTitleContainer = (LinearLayout) titleLayout.findViewById(34603090);
        this.mIcon1View = null;
        this.mIcon2View = null;
        this.mResOK = 33751080;
        this.mResCancel = 33751079;
        if (this.mIcon1Visible || this.mIcon2Visible) {
            initTitleIcons(titleLayout);
        }
        return titleLayout;
    }

    protected void initTitleIcons() {
        initTitleIcons(this.mTitleLayout);
    }

    protected void initTitleIcons(LinearLayout titleLayout) {
        if (titleLayout != null) {
            this.mIcon1View = (ImageView) ((ViewStub) titleLayout.findViewById(34603091)).inflate().findViewById(16908295);
            this.mIcon2View = (ImageView) ((ViewStub) titleLayout.findViewById(34603092)).inflate().findViewById(16908296);
            initIconsColor();
            if (this.mIcon2View != null) {
                this.mBaseEndMargin = ((LayoutParams) this.mIcon2View.getLayoutParams()).getMarginEnd();
            }
            triggerIconsVisible(this.mIcon1Visible, this.mIcon2Visible);
            setStartIconImage(this.mIcon1Drawable);
            setEndIconImage(this.mIcon2Drawable);
            setStartIconListener(this.mListener1);
            setEndIconListener(this.mListener2);
        }
    }

    private void initIconsColor() {
        ColorStateList color = getImmersionTint(getContext());
        if (!(this.mResCancel == 0 || this.mIcon1View == null)) {
            this.mIcon1View.setImageResource(this.mResCancel);
            this.mIcon1View.setImageTintList(color);
        }
        if (this.mResOK != 0 && this.mIcon2View != null) {
            this.mIcon2View.setImageResource(this.mResOK);
            this.mIcon2View.setImageTintList(color);
        }
    }

    private void invalidateTitleLayout() {
        int marginNotZero = !((getDisplayOptions() & 4) != 0) ? this.mIcon1Visible ^ 1 : 0;
        int margin = getResources().getDimensionPixelSize(34472184);
        if (marginNotZero == 0) {
            margin = 0;
        }
        invalidateTitleLayout(getTitleGravity(), margin, getTitleView(), getSubTitleView());
        triggerIconsVisible(this.mIcon1Visible, this.mIcon2Visible);
    }

    private int getTitleGravity() {
        boolean noMenu = getMenuItemCountInActionBarView() == 0;
        if (this.mIcon2View != null) {
            LayoutParams lp = (LayoutParams) this.mIcon2View.getLayoutParams();
            if (noMenu) {
                lp.setMarginEnd(this.mBaseEndMargin);
            } else {
                lp.setMarginEnd(this.mBaseEndMargin * 2);
            }
            this.mIcon2View.setLayoutParams(lp);
        }
        if (this.mIcon1Visible) {
            return 1;
        }
        return 8388611;
    }

    public void setTitle(CharSequence title) {
        if (!(this.mTitleContainer == null || this.mCustTitle == null || this.mCustTitle.getParent() != this.mTitleContainer)) {
            this.mTitleContainer.removeView(this.mCustTitle);
            View titleView = getTitleView();
            if (titleView != null) {
                ViewParent titleViewVp = titleView.getParent();
                if (titleViewVp != null && (titleViewVp instanceof ViewGroup)) {
                    ((ViewGroup) titleViewVp).removeView(titleView);
                }
            }
            View subTitleView = getSubTitleView();
            if (subTitleView != null) {
                ViewParent subTitleViewVp = subTitleView.getParent();
                if (subTitleViewVp != null && (subTitleViewVp instanceof ViewGroup)) {
                    ((ViewGroup) subTitleViewVp).removeView(subTitleView);
                }
            }
            this.mTitleContainer.addView(titleView);
            this.mTitleContainer.addView(subTitleView);
        }
        super.setTitle(title);
    }

    public void setCustomTitle(View view) {
        if (this.mTitleContainer != null && view != null) {
            this.mTitleContainer.removeView(getTitleView());
            this.mTitleContainer.removeView(getSubTitleView());
            if (this.mCustTitle != null && this.mCustTitle.getParent() == this.mTitleContainer) {
                this.mTitleContainer.removeView(this.mCustTitle);
            }
            ViewParent vp = view.getParent();
            if (vp != null && (vp instanceof ViewGroup)) {
                ((ViewGroup) vp).removeView(view);
            }
            this.mTitleContainer.addView(view);
            this.mCustTitle = view;
        }
    }

    public void setStartIconVisible(boolean icon1Visible) {
        this.mIcon1Visible = icon1Visible;
        triggerIconsVisible(this.mIcon1Visible, this.mIcon2Visible);
    }

    public void setEndIconVisible(boolean icon2Visible) {
        this.mIcon2Visible = icon2Visible;
        triggerIconsVisible(this.mIcon1Visible, this.mIcon2Visible);
    }

    public void setStartContentDescription(CharSequence contentDescription) {
        if (this.mIcon1View == null) {
            initTitleIcons();
        }
        if (this.mIcon1View != null) {
            this.mIcon1View.setContentDescription(contentDescription);
        }
    }

    public void setEndContentDescription(CharSequence contentDescription) {
        if (this.mIcon2View == null) {
            initTitleIcons();
        }
        if (this.mIcon2View != null) {
            this.mIcon2View.setContentDescription(contentDescription);
        }
    }

    public void triggerIconsVisible(boolean icon1Visible, boolean icon2Visible) {
        int i = 4;
        if (this.mIcon2View == null || this.mIcon1View == null) {
            initTitleIcons();
        }
        if (this.mIcon2View != null && this.mIcon1View != null) {
            if (icon1Visible) {
                this.mIcon1View.setVisibility(0);
            } else {
                this.mIcon1View.setVisibility(getTitleGravity() == 8388611 ? 8 : 4);
            }
            if (icon2Visible) {
                this.mIcon2View.setVisibility(0);
            } else {
                ImageView imageView = this.mIcon2View;
                if (!icon1Visible) {
                    i = 8;
                }
                imageView.setVisibility(i);
            }
        }
    }

    public void setStartIconImage(Drawable icon1) {
        if (this.mIcon1View == null) {
            initTitleIcons();
        }
        if (icon1 != null) {
            this.mIcon1Drawable = icon1;
            this.mIcon1View.setImageDrawable(icon1);
            return;
        }
        this.mIcon1Drawable = null;
        if (this.mResCancel != 0) {
            this.mIcon1View.setImageResource(this.mResCancel);
        }
    }

    public void setEndIconImage(Drawable icon2) {
        if (this.mIcon2View == null) {
            initTitleIcons();
        }
        if (icon2 != null) {
            this.mIcon2Drawable = icon2;
            this.mIcon2View.setImageDrawable(icon2);
            return;
        }
        this.mIcon2Drawable = null;
        if (this.mResOK != 0) {
            this.mIcon2View.setImageResource(this.mResOK);
        }
    }

    public void setStartIconListener(OnClickListener listener1) {
        if (this.mIcon1View == null) {
            initTitleIcons();
        }
        this.mListener1 = listener1;
        this.mIcon1View.setOnClickListener(listener1);
    }

    public void setEndIconListener(OnClickListener listener2) {
        if (this.mIcon2View == null) {
            initTitleIcons();
        }
        this.mListener2 = listener2;
        this.mIcon2View.setOnClickListener(listener2);
    }

    protected ActionMenuPresenter initActionMenuPresenter(Context context) {
        return new HwActionMenuPresenter(context, 34013188, 34013187);
    }

    protected void deleteExpandedHomeIfNeed() {
        getUpGoerFive().removeView(getExpandedHomeLayout());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mLastConfiguration.diff(newConfig) != 0) {
            this.mLastConfiguration.setTo(newConfig);
            if (!(this.mIcon1View == null && this.mIcon2View == null)) {
                this.mSavedState = new ButtonState();
                saveState(this.mSavedState);
            }
            super.onConfigurationChanged(newConfig);
            if (!(this.mSavedState == null || (this.mIcon1View == null && this.mIcon2View == null))) {
                restoreState(this.mSavedState);
                this.mSavedState = null;
            }
        }
    }

    public void saveState(ButtonState state) {
        float alpha;
        boolean isEnabled;
        boolean z = false;
        float f = 0.0f;
        boolean used1 = this.mIcon1View != null;
        state.mUsed1 = used1;
        if (used1) {
            alpha = this.mIcon1View.getAlpha();
        } else {
            alpha = 0.0f;
        }
        state.mAlpha1 = alpha;
        if (used1) {
            isEnabled = this.mIcon1View.isEnabled();
        } else {
            isEnabled = false;
        }
        state.mEnabled1 = isEnabled;
        boolean used2 = this.mIcon2View != null;
        state.mUsed2 = used2;
        if (used2) {
            f = this.mIcon2View.getAlpha();
        }
        state.mAlpha2 = f;
        if (used2) {
            z = this.mIcon2View.isEnabled();
        }
        state.mEnabled2 = z;
    }

    public void restoreState(ButtonState saved) {
        if (saved.mUsed1 && this.mIcon1View != null) {
            this.mIcon1View.setAlpha(saved.mAlpha1);
            this.mIcon1View.setEnabled(saved.mEnabled1);
        }
        if (saved.mUsed2 && this.mIcon2View != null) {
            this.mIcon2View.setAlpha(saved.mAlpha2);
            this.mIcon2View.setEnabled(saved.mEnabled2);
        }
    }

    protected void initTitleAppearance() {
        initTitleAppearance(getContext(), getTitleView(), getSubTitleView());
    }

    private static ColorStateList getImmersionTint(Context context) {
        int resTint = HwWidgetFactory.getImmersionResource(context, 33882140, 0, 33882388, true);
        if (HwWidgetFactory.isHwEmphasizeTheme(context)) {
            resTint = 33882402;
        }
        if (HwWidgetFactory.isBlackActionBar(context)) {
            resTint = 33882455;
        }
        return context.getColorStateList(resTint);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        getUpGoerFive().setOnClickListener(null);
        OnClickListener upClickListener = getUpClickListener();
        HomeView homeView = getHomeLayout();
        homeView.setOnClickListener(upClickListener);
        homeView.setClickable(true);
        homeView.setFocusable(true);
        homeView.setContentDescription(getContext().getResources().getText(17039534));
        setNavigationContentDescription("");
    }

    public void invalidateAllViews() {
        int options = getDisplayOptions();
        setDisplayOptions(options & -9);
        setDisplayOptions(options | 8);
        initTitleAppearance();
        boolean setUp = (options & 4) != 0;
        HomeView hv = getHomeLayout();
        if (hv != null) {
            hv.setShowUp(setUp);
        }
        initIconsColor();
        if (this.mMenuView != null && this.mMenuView.getParent() == this) {
            removeView(this.mMenuView);
            addView(this.mMenuView);
        }
    }

    public void setSplitViewLocation(int start, int end) {
        this.mSplitViewMaxSize = end - start;
        this.mPopupStartLocation = start;
        this.mPopupEndLocation = end;
        updateSplitLocation();
    }

    protected void updateSplitLocation() {
        if (this.mMenuView != null) {
            this.mMenuView.setSplitViewMaxSize(this.mSplitViewMaxSize);
        }
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.setPopupLocation(this.mPopupStartLocation, this.mPopupEndLocation);
        }
    }

    public static void invalidateTitleLayout(int gravity, int margin, TextView title, TextView subTitle) {
        if (title != null) {
            LayoutParams lpTitle = (LayoutParams) title.getLayoutParams();
            lpTitle.gravity = gravity;
            lpTitle.setMarginStart(margin);
            title.setLayoutParams(lpTitle);
        }
        if (subTitle != null && subTitle.getVisibility() == 0) {
            LayoutParams lpSubTitle = (LayoutParams) subTitle.getLayoutParams();
            lpSubTitle.gravity = gravity;
            subTitle.setLayoutParams(lpSubTitle);
        }
    }

    public static void initTitleAppearance(Context context, TextView title, TextView subTitle) {
        if (title != null) {
            HwWidgetFactory.setImmersionStyle(context, title, 33882238, 33882237, 0, false);
        }
        if (subTitle != null) {
            HwWidgetFactory.setImmersionStyle(context, subTitle, 33882238, 33882237, 0, false);
        }
    }
}
