package huawei.android.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toolbar;
import androidhwext.R;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ToolbarWidgetWrapper;
import huawei.android.graphics.drawable.HwEventBadge;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.com.android.internal.widget.HwToolbarWidgetWrapper;
import java.lang.annotation.RCUnownedThisRef;

public class HwToolbar extends Toolbar {
    private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
    private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
    private static final boolean IS_DEBUG = false;
    private static final int MAX_LINES = 2;
    private static final String RES_TYPE_DIMEN = "dimen";
    private static final String RES_TYPE_ID = "id";
    private static final String RES_TYPE_INTEGER = "integer";
    private static final String RES_TYPE_LAYOUT = "layout";
    private static final String TAG = "HwToolbar";
    private static final int TITLE_MAX_LINE = 2;
    private HwActionMenuPresenter mActionMenuPresenter;
    private MenuPresenter.Callback mActionMenuPresenterCallback;
    private ActivityInfo mActivityInfo;
    private int mBadgeMargin;
    private int mBgCornerDimen;
    private HwBlurEngine mBlurEngine;
    private int mBlurOverlayColor;
    private HwBlurEngine.BlurType mBlurType;
    private ClickEffectEntry mClickEffectEntry;
    private float mColumnDensity;
    private int mColumnHeight;
    private int mColumnWidth;
    private Context mContext;
    private View mCustomTitleView;
    private MenuPresenter mExpandMenuPresenter;
    private HwEventBadge mHwEventBadge;
    private int mHwTitleMarginEnd;
    private int mHwTitleMarginStart;
    private HwWidgetSafeInsets mHwWidgetSafeInsets;
    private ImageView mIcon1View;
    private ImageView mIcon2View;
    private ColorStateList mIconColor;
    private ColorStateList mIconColorList;
    private View mIconLayout;
    private boolean mIsBlurEnable;
    private boolean mIsColumnEnabled;
    private boolean mIsDynamicSplitMenu;
    private boolean mIsForceSplit;
    private boolean mIsIcon1Visible;
    private boolean mIsIcon2Visible;
    private boolean mIsSetDynamicSplitMenu;
    private boolean mIsSplitActionBar;
    private boolean mIsUserApp;
    private ImageView mLogoView;
    private MenuBuilder mMenu;
    private MenuBuilder.Callback mMenuBuilderCallback;
    private int mMenuItemLimit;
    private HwToolbarMenuView mMenuView;
    private final ActionMenuView.OnMenuItemClickListener mMenuViewItemClickListener;
    private int mNoIconEndMargin;
    private int mNoIconStartMargin;
    private Toolbar.OnMenuItemClickListener mOnMenuItemClickListener;
    private TextView mParentClassSubTitleTextView;
    private TextView mParentClassTitleTextView;
    private int mPopupEndLocation;
    private int mPopupStartLocation;
    private int mResCancel;
    private ResLoader mResLoader;
    private int mResOk;
    private Spinner mSpinner;
    private SpinnerAdapter mSpinnerAdapter;
    private int mSpinnerVisible;
    private HwToolBarMenuContainer mSplitView;
    private int mStartIconHeight;
    private int mStartIconInsetPadding;
    private int mStartIconWidth;
    private int mSubTitleMarginBottom;
    private int mSubTitleMarginTop;
    private int mSubTitleMinSize;
    private int mSubTitleNormalSize;
    private HwTextView mSubTitleView;
    private int mSubTitleViewVisible;
    private CharSequence mSubtitleText;
    private ColorStateList mTitleColor;
    private LinearLayout mTitleContainer;
    private int mTitleMarginTop;
    private int mTitleMinSize;
    private int mTitleNormalSize;
    private int mTitleNormalSizeLast;
    private int mTitleSizeStep;
    private CharSequence mTitleText;
    private HwTextView mTitleView;
    private WindowInsets mWindowInsets;
    private int mWithIconEndMargin;
    private int mWithIconStarMargin;
    private ToolbarWidgetWrapper mWrapper;

    public HwToolbar(Context context) {
        this(context, null);
    }

    public HwToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 16843946);
    }

    public HwToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBlurOverlayColor = INVALID_BLUR_OVERLAY_COLOR;
        this.mBlurType = HwBlurEngine.BlurType.LightBlurWithGray;
        this.mIsSetDynamicSplitMenu = IS_DEBUG;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.mMenuViewItemClickListener = new ActionMenuView.OnMenuItemClickListener() {
            /* class huawei.android.widget.HwToolbar.AnonymousClass1 */

            @Override // android.widget.ActionMenuView.OnMenuItemClickListener
            @RCUnownedThisRef
            public boolean onMenuItemClick(MenuItem item) {
                if (HwToolbar.this.mOnMenuItemClickListener != null) {
                    return HwToolbar.this.mOnMenuItemClickListener.onMenuItemClick(item);
                }
                return HwToolbar.IS_DEBUG;
            }
        };
        this.mIsBlurEnable = IS_DEBUG;
        this.mClickEffectEntry = null;
        this.mActivityInfo = null;
        this.mSubTitleViewVisible = -1;
        this.mHwTitleMarginStart = -1;
        this.mHwTitleMarginEnd = -1;
        this.mIsColumnEnabled = IS_DEBUG;
        this.mSpinnerVisible = -1;
        this.mContext = context;
        this.mHwWidgetSafeInsets = new HwWidgetSafeInsets(this);
        this.mHwWidgetSafeInsets.setDealTop(true);
        this.mHwWidgetSafeInsets.parseHwDisplayCutout(context, attrs);
        init(attrs, defStyleAttr, defStyleRes);
    }

    public void setStartIcon(boolean isIcon1Visible, Drawable icon, View.OnClickListener listener) {
        if (this.mIconLayout == null) {
            initIconLayout();
        }
        if (this.mIcon1View == null) {
            initStartAndEndIconLayout();
        }
        if (this.mIcon1View != null && this.mIcon2View != null) {
            setTitle(this.mTitleText);
            setSubtitle(this.mSubtitleText);
            setStartIconVisible(isIcon1Visible);
            setStartIconImage(icon);
            setStartIconListener(listener);
        }
    }

    public void setEndIcon(boolean isIcon2Visible, Drawable icon, View.OnClickListener listener) {
        if (this.mIconLayout == null) {
            initIconLayout();
        }
        if (this.mIcon2View == null) {
            initStartAndEndIconLayout();
        }
        if (this.mIcon1View != null && this.mIcon2View != null) {
            setTitle(this.mTitleText);
            setSubtitle(this.mSubtitleText);
            setEndIconVisible(isIcon2Visible);
            setEndIconImage(icon);
            setEndIconListener(listener);
        }
    }

    public void setStartContentDescription(CharSequence contentDescription) {
        if (this.mIcon1View == null) {
            initStartAndEndIconLayout();
        }
        ImageView imageView = this.mIcon1View;
        if (imageView != null) {
            imageView.setContentDescription(contentDescription);
        }
    }

    public void setEndContentDescription(CharSequence contentDescription) {
        if (this.mIcon2View == null) {
            initStartAndEndIconLayout();
        }
        ImageView imageView = this.mIcon2View;
        if (imageView != null) {
            imageView.setContentDescription(contentDescription);
        }
    }

    public void setDynamicSplitMenu(boolean isSplitMenu) {
        this.mIsDynamicSplitMenu = isSplitMenu;
        this.mIsSetDynamicSplitMenu = true;
        requestLayout();
    }

    public View getIconLayout() {
        return this.mIconLayout;
    }

    public void setSplitBackgroundDrawable(Drawable drawable) {
        if (this.mSplitView == null) {
            ensureSplitView();
        }
        HwToolBarMenuContainer hwToolBarMenuContainer = this.mSplitView;
        if (hwToolBarMenuContainer != null) {
            hwToolBarMenuContainer.setSplitBackground(drawable);
        }
    }

    public void setSmartColor(ColorStateList iconColor, ColorStateList titleColor) {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView != null) {
            hwToolbarMenuView.onSetSmartColor(iconColor, titleColor);
        }
        this.mIsUserApp = true;
        this.mIconColor = iconColor;
        this.mTitleColor = titleColor;
    }

    /* JADX INFO: Multiple debug info for r1v13 huawei.android.widget.HwTextView: [D('titleViewVp' android.view.ViewParent), D('subTitleView' android.view.View)] */
    @Override // android.widget.Toolbar
    public void setTitle(CharSequence title) {
        View view;
        LinearLayout linearLayout;
        if (this.mIconLayout == null && this.mResLoader != null) {
            initIconLayout();
        }
        this.mTitleText = title;
        if (!(this.mTitleContainer == null || (view = this.mCustomTitleView) == null || view.getParent() != (linearLayout = this.mTitleContainer))) {
            linearLayout.removeView(this.mCustomTitleView);
            View titleView = this.mTitleView;
            if (titleView != null) {
                ViewParent titleViewVp = titleView.getParent();
                if (titleViewVp != null && (titleViewVp instanceof ViewGroup)) {
                    ((ViewGroup) titleViewVp).removeView(titleView);
                }
                this.mTitleContainer.addView(titleView);
            }
            View subTitleView = this.mSubTitleView;
            if (subTitleView != null) {
                ViewParent subTitleViewVp = subTitleView.getParent();
                if (subTitleViewVp != null && (subTitleViewVp instanceof ViewGroup)) {
                    ((ViewGroup) subTitleViewVp).removeView(subTitleView);
                }
                this.mTitleContainer.addView(subTitleView);
            }
        }
        if (shouldLayout(this.mIconLayout)) {
            HwTextView hwTextView = this.mTitleView;
            if (hwTextView != null) {
                hwTextView.setText(title);
            }
            super.setTitle((CharSequence) null);
        } else {
            super.setTitle(title);
        }
        if (this.mParentClassTitleTextView == null) {
            Object object = ReflectUtil.getObject(this, "mTitleTextView", Toolbar.class);
            if (object instanceof TextView) {
                this.mParentClassTitleTextView = (TextView) object;
                this.mParentClassTitleTextView.setSingleLine(IS_DEBUG);
                this.mParentClassTitleTextView.setMaxLines(2);
                if (this.mParentClassTitleTextView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) this.mParentClassTitleTextView.getLayoutParams();
                    params.topMargin = this.mTitleMarginTop;
                    this.mParentClassTitleTextView.setLayoutParams(params);
                }
            }
        }
    }

    @Override // android.widget.Toolbar
    public void setSubtitle(CharSequence subtitle) {
        if (this.mIconLayout == null && this.mResLoader != null) {
            initIconLayout();
        }
        this.mSubtitleText = subtitle;
        View view = this.mIconLayout;
        if (view == null || view.getParent() != this) {
            super.setSubtitle(subtitle);
        } else {
            HwTextView hwTextView = this.mSubTitleView;
            if (hwTextView != null) {
                hwTextView.setText(subtitle);
            }
            super.setSubtitle((CharSequence) null);
        }
        HwTextView hwTextView2 = this.mSubTitleView;
        if (hwTextView2 != null) {
            hwTextView2.setVisibility(subtitle != null ? 0 : 8);
        }
        if (this.mParentClassSubTitleTextView == null) {
            Object object = ReflectUtil.getObject(this, "mSubtitleTextView", Toolbar.class);
            if (object instanceof TextView) {
                this.mParentClassSubTitleTextView = (TextView) object;
                this.mParentClassSubTitleTextView.setSingleLine(true);
                if (this.mParentClassSubTitleTextView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) this.mParentClassSubTitleTextView.getLayoutParams();
                    params.topMargin = this.mSubTitleMarginTop;
                    params.bottomMargin = this.mSubTitleMarginBottom;
                    this.mParentClassSubTitleTextView.setLayoutParams(params);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Toolbar, android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mHwWidgetSafeInsets.updateOriginPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override // android.view.View
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        this.mHwWidgetSafeInsets.updateOriginPadding(left, top, right, bottom);
    }

    public void setCustomTitle(View view) {
        LinearLayout linearLayout;
        LinearLayout linearLayout2;
        if (this.mTitleContainer == null) {
            initIconLayout();
        }
        if (view != null && this.mIconLayout != null && (linearLayout = this.mTitleContainer) != null) {
            linearLayout.removeView(this.mTitleView);
            this.mTitleContainer.removeView(this.mSubTitleView);
            View view2 = this.mCustomTitleView;
            if (view2 != null && view2.getParent() == (linearLayout2 = this.mTitleContainer)) {
                linearLayout2.removeView(this.mCustomTitleView);
            }
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(view);
            }
            ViewGroup.LayoutParams layoutParams = this.mTitleContainer.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
                marginParams.setMarginStart(this.mWithIconStarMargin);
                marginParams.setMarginEnd(this.mWithIconEndMargin);
            }
            this.mTitleContainer.addView(view);
            this.mCustomTitleView = view;
        }
    }

    public void setDisplaySpinner(int contentId, AdapterView.OnItemSelectedListener listener) {
        if (this.mIconLayout == null) {
            initIconLayout();
        }
        if (this.mSpinner == null) {
            initSpinnerLayout();
        }
        if (((this.mIconLayout == null || this.mTitleView == null || this.mSpinner == null) ? false : true) && this.mSubTitleView != null) {
            this.mTitleView.setVisibility(8);
            this.mSubTitleView.setVisibility(8);
            this.mSpinner.setVisibility(0);
            ensureSpinnerAdapter(contentId);
            this.mSpinner.setAdapter(this.mSpinnerAdapter);
            this.mSpinner.setOnItemSelectedListener(listener);
        }
    }

    public SpinnerAdapter getSpinnerAdapter() {
        return this.mSpinnerAdapter;
    }

    public int getDropdownSelectedPosition() {
        Spinner spinner = this.mSpinner;
        if (spinner != null) {
            return spinner.getSelectedItemPosition();
        }
        return 0;
    }

    public int getDropdownItemCount() {
        Spinner spinner = this.mSpinner;
        if (spinner != null) {
            return spinner.getCount();
        }
        return 0;
    }

    public void setSplitViewLocation(int start, int end) {
        HwToolBarMenuContainer hwToolBarMenuContainer = this.mSplitView;
        if (hwToolBarMenuContainer != null) {
            hwToolBarMenuContainer.setSplitViewLocation(start, end);
        }
        this.mPopupStartLocation = start;
        this.mPopupEndLocation = end;
        updateSplitLocation();
    }

    public void setSplitToolbarForce(boolean isForceSplit) {
        this.mIsForceSplit = isForceSplit;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Toolbar, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        HwTextView hwTextView;
        View collapseButtonView;
        updateViews();
        Object object = ReflectUtil.getObject(this, "mCollapseButtonView", Toolbar.class);
        if (!(!(object instanceof View) || (collapseButtonView = (View) object) == null || collapseButtonView.getVisibility() == 8)) {
            collapseButtonView.setVisibility(8);
        }
        boolean isParentSubTitleShouldLayout = shouldLayout(this.mParentClassSubTitleTextView) && !shouldLayout(this.mIconLayout);
        boolean isParentTitleShouldLayout = shouldLayout(this.mParentClassTitleTextView) && !shouldLayout(this.mIconLayout);
        if (isParentTitleShouldLayout) {
            this.mParentClassTitleTextView.setTextSize(1, (float) this.mTitleNormalSize);
            this.mParentClassTitleTextView.setSingleLine(IS_DEBUG);
            this.mParentClassTitleTextView.setMaxLines(2);
            if (isParentSubTitleShouldLayout) {
                this.mParentClassTitleTextView.setSingleLine(true);
            }
        }
        if (isParentSubTitleShouldLayout) {
            this.mParentClassSubTitleTextView.setTextSize(1, (float) this.mSubTitleNormalSize);
        }
        if (!(!shouldLayout(this.mIconLayout) || (hwTextView = this.mTitleView) == null || hwTextView.getVisibility() == 8)) {
            int currentVisible = 8;
            HwTextView hwTextView2 = this.mSubTitleView;
            if (hwTextView2 != null) {
                currentVisible = hwTextView2.getVisibility();
            }
            boolean isShouldUpdateTitle = this.mTitleNormalSizeLast != this.mTitleNormalSize;
            if (this.mSubTitleViewVisible != currentVisible || isShouldUpdateTitle) {
                this.mTitleView.setAutoTextSize(1, (float) this.mTitleNormalSize);
                this.mTitleView.setAutoTextInfo(this.mTitleMinSize, this.mTitleSizeStep, 1);
                if (this.mSubTitleView == null || currentVisible == 8) {
                    this.mTitleView.setSingleLine(IS_DEBUG);
                    this.mTitleView.setMaxLines(2);
                } else {
                    this.mTitleView.setSingleLine(true);
                }
                this.mSubTitleViewVisible = currentVisible;
                this.mTitleNormalSizeLast = this.mTitleNormalSize;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        autoTitleSize(heightMeasureSpec, widthMeasureSpec, isParentSubTitleShouldLayout, isParentTitleShouldLayout);
    }

    private void autoTitleSize(int heightMeasureSpec, int widthMeasureSpec, boolean isParentSubTitleShouldLayout, boolean isParentTitleShouldLayout) {
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (isParentSubTitleShouldLayout) {
            AutoTextUtils.autoText(this.mSubTitleNormalSize, this.mSubTitleMinSize, this.mTitleSizeStep, 1, this.mParentClassSubTitleTextView.getMeasuredWidth(), height, this.mParentClassSubTitleTextView);
        }
        if (isParentTitleShouldLayout) {
            AutoTextUtils.autoText(this.mTitleNormalSize, this.mTitleMinSize, this.mTitleSizeStep, 1, this.mParentClassTitleTextView.getMeasuredWidth(), height, this.mParentClassTitleTextView);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        int defaultHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();
        if (getMeasuredHeight() > defaultHeight) {
            defaultHeight = getMeasuredHeight();
        }
        setMeasuredDimension(getMeasuredWidth(), defaultHeight);
    }

    private boolean shouldLayout(View view) {
        if (view == null || view.getParent() != this || view.getVisibility() == 8) {
            return IS_DEBUG;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Toolbar, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        this.mHwWidgetSafeInsets.applyDisplaySafeInsets(true);
        if (shouldLayout(this.mParentClassTitleTextView)) {
            int titleTextViewTop = this.mParentClassTitleTextView.getTop();
            boolean isHasSubTitle = shouldLayout(this.mParentClassSubTitleTextView);
            int titleHeight = this.mParentClassTitleTextView.getMeasuredHeight();
            int height = getMeasuredHeight();
            if (isHasSubTitle) {
                titleHeight += this.mSubTitleMarginTop + this.mParentClassSubTitleTextView.getMeasuredHeight();
            }
            int realTop = (height - titleHeight) >> 1;
            if (realTop > titleTextViewTop) {
                int titleTextViewLeft = this.mParentClassTitleTextView.getLeft();
                int textViewRight = this.mParentClassTitleTextView.getRight();
                int textViewBottom = this.mParentClassTitleTextView.getMeasuredHeight() + realTop;
                this.mParentClassTitleTextView.layout(titleTextViewLeft, realTop, textViewRight, textViewBottom);
                if (isHasSubTitle) {
                    int subtitleLeft = this.mParentClassSubTitleTextView.getLeft();
                    int subtitleRight = this.mParentClassSubTitleTextView.getRight();
                    int subtitleTop = this.mSubTitleMarginTop + textViewBottom;
                    this.mParentClassSubTitleTextView.layout(subtitleLeft, subtitleTop, subtitleRight, this.mParentClassSubTitleTextView.getMeasuredHeight() + subtitleTop);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HwActionMenuPresenter hwActionMenuPresenter = this.mActionMenuPresenter;
        if (hwActionMenuPresenter != null) {
            hwActionMenuPresenter.onConfigurationChanged(newConfig);
        }
        if (this.mSplitView == null && getSplitStatus()) {
            ensureSplitView();
        }
        initTitleTextAndIconSize(this.mResLoader, this.mContext.getResources());
        handleStartIconView();
        HwTextView hwTextView = this.mSubTitleView;
        if (hwTextView != null) {
            hwTextView.setAutoTextSize(1, (float) this.mSubTitleNormalSize);
        }
        setMinimumHeight(this.mContext.getResources().getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_height")));
    }

    @Override // android.widget.Toolbar
    public Menu getMenu() {
        ensureHwMenu();
        updateSplitLocation();
        return this.mMenu;
    }

    public void setMenu(MenuBuilder menu, ActionMenuPresenter outerPresenter) {
        if (outerPresenter != null) {
            MenuPresenter.Callback callback = outerPresenter.getCallback();
            ensureHwMenuView();
            initHwActionMenuPresenter();
            this.mActionMenuPresenter.setCallback(callback);
            this.mActionMenuPresenter.setId(16908729);
            initExpandMenuPresenter();
            setMenuPresenterStatus(getSplitStatus());
            updateSplitLocation();
            super.setMenu(menu, this.mActionMenuPresenter);
        }
    }

    public void setMenuCallbacks(MenuPresenter.Callback callback, MenuBuilder.Callback builderCallback) {
        super.setMenuCallbacks(callback, builderCallback);
        this.mActionMenuPresenterCallback = callback;
        this.mMenuBuilderCallback = builderCallback;
    }

    @Override // android.widget.Toolbar
    public void setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener listener) {
        super.setOnMenuItemClickListener(listener);
        this.mOnMenuItemClickListener = listener;
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwWidgetSafeInsets.updateWindowInsets(insets);
        this.mWindowInsets = insets;
        return super.onApplyWindowInsets(insets);
    }

    public DecorToolbar getWrapper() {
        if (this.mWrapper == null) {
            this.mWrapper = new HwToolbarWidgetWrapper(this, IS_DEBUG);
        }
        return this.mWrapper;
    }

    @Override // android.widget.Toolbar
    public CharSequence getTitle() {
        return this.mTitleText;
    }

    @Override // android.widget.Toolbar
    public CharSequence getSubtitle() {
        return this.mSubtitleText;
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mResLoader = ResLoader.getInstance();
        Resources res = this.mResLoader.getResources(this.mContext);
        this.mMenuItemLimit = res.getInteger(this.mResLoader.getIdentifier(this.mContext, RES_TYPE_INTEGER, "hwtoolbar_split_menu_itemlimit"));
        initTitleTextAndIconSize(this.mResLoader, res);
        this.mSubTitleMarginTop = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_subtitle_margin_top"));
        this.mSubTitleMarginBottom = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_title_margin_bottom"));
        this.mTitleMarginTop = res.getDimensionPixelOffset(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_title_margin_top"));
        this.mWithIconStarMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_title_margin_start_with_icon"));
        this.mWithIconEndMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_title_margin_end_with_icon"));
        this.mBadgeMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "margin_m"));
        this.mBgCornerDimen = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "emui_corner_radius_clicked"));
        initStyle(attrs, defStyleAttr, defStyleRes);
        this.mClickEffectEntry = HwWidgetUtils.getCleckEffectEntry(this.mContext, defStyleAttr);
    }

    private void initStyle(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs != null) {
            TypedArray array = this.mContext.obtainStyledAttributes(attrs, R.styleable.HwToolbar, defStyleAttr, defStyleRes);
            int count = array.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attrResId = array.getIndex(i);
                if (attrResId == 0) {
                    this.mIconColorList = array.getColorStateList(i);
                } else if (attrResId == 4) {
                    this.mBlurOverlayColor = array.getColor(i, INVALID_BLUR_OVERLAY_COLOR);
                } else if (attrResId == 6) {
                    HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(array.getInt(i, DEFAULT_BLUR_TYPE));
                    if (blurType != null) {
                        this.mBlurType = blurType;
                    }
                } else if (attrResId == 8) {
                    this.mIsColumnEnabled = array.getBoolean(i, IS_DEBUG);
                }
            }
            array.recycle();
        }
    }

    private void initTitleTextAndIconSize(ResLoader resLoader, Resources res) {
        if (this.mResLoader != null && res != null) {
            this.mTitleNormalSize = res.getInteger(resLoader.getIdentifier(this.mContext, RES_TYPE_INTEGER, "hwtoolbar_title_normal_textsize"));
            this.mTitleMinSize = res.getInteger(resLoader.getIdentifier(this.mContext, RES_TYPE_INTEGER, "hwtoolbar_title_min_textsize"));
            this.mSubTitleNormalSize = res.getInteger(resLoader.getIdentifier(this.mContext, RES_TYPE_INTEGER, "hwtoolbar_subtitle_normal_textsize"));
            this.mSubTitleMinSize = res.getInteger(resLoader.getIdentifier(this.mContext, RES_TYPE_INTEGER, "hwtoolbar_subtitle_min_textsize"));
            this.mTitleSizeStep = res.getInteger(resLoader.getIdentifier(this.mContext, RES_TYPE_INTEGER, "hwtoolbar_title_textsize_step"));
            this.mStartIconHeight = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_start_icon_height"));
            this.mStartIconWidth = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_icon_size"));
            this.mStartIconInsetPadding = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_start_icon_inset_padding"));
        }
    }

    private void updateSplitLocation() {
        HwActionMenuPresenter hwActionMenuPresenter = this.mActionMenuPresenter;
        if (hwActionMenuPresenter != null) {
            hwActionMenuPresenter.setPopupLocation(this.mPopupStartLocation, this.mPopupEndLocation);
        }
    }

    private void initExpandMenuPresenter() {
        if (this.mExpandMenuPresenter == null) {
            Object expandPresenter = ReflectUtil.createPrivateInnerInstance(ReflectUtil.getPrivateClass("android.widget.Toolbar$ExpandedActionViewMenuPresenter"), Toolbar.class, this, null, null);
            if ((expandPresenter instanceof MenuPresenter) && this.mMenu != null) {
                this.mExpandMenuPresenter = (MenuPresenter) expandPresenter;
                ReflectUtil.setObject("mExpandedMenuPresenter", this, this.mExpandMenuPresenter, Toolbar.class);
            }
        }
    }

    private void setSplitToolbar(boolean isSplitActionBar) {
        if (this.mSplitView == null && isSplitActionBar) {
            ensureSplitView();
        }
        if (this.mMenuView == null) {
            ensureHwMenuView();
        }
        if (this.mIsSplitActionBar != isSplitActionBar) {
            ViewParent parent = null;
            HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
            if (hwToolbarMenuView != null) {
                parent = hwToolbarMenuView.getParent();
            }
            View view = this.mMenuView;
            int i = 0;
            if (view != null && (parent instanceof ViewGroup)) {
                ((ViewGroup) parent).removeView(view);
                if (isSplitActionBar) {
                    HwToolBarMenuContainer hwToolBarMenuContainer = this.mSplitView;
                    if (hwToolBarMenuContainer != null) {
                        hwToolBarMenuContainer.addView(this.mMenuView);
                        this.mIsSplitActionBar = true;
                    }
                    this.mMenuView.getLayoutParams().width = -1;
                } else {
                    addMenuViewForSystemView();
                    this.mIsSplitActionBar = IS_DEBUG;
                }
                this.mMenuView.requestLayout();
            }
            HwToolBarMenuContainer hwToolBarMenuContainer2 = this.mSplitView;
            if (hwToolBarMenuContainer2 != null) {
                if (!isSplitActionBar) {
                    i = 8;
                }
                hwToolBarMenuContainer2.setVisibility(i);
            }
            setMenuPresenterStatus(isSplitActionBar);
        }
    }

    private void setMenuPresenterStatus(boolean isSplitActionBar) {
        HwActionMenuPresenter hwActionMenuPresenter = this.mActionMenuPresenter;
        if (hwActionMenuPresenter != null) {
            if (!isSplitActionBar) {
                hwActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(17891336));
                return;
            }
            hwActionMenuPresenter.setExpandedActionViewsExclusive((boolean) IS_DEBUG);
            this.mActionMenuPresenter.setWidthLimit(this.mContext.getResources().getDisplayMetrics().widthPixels, true);
            this.mActionMenuPresenter.setItemLimit(this.mMenuItemLimit);
        }
    }

    private void ensureHwMenu() {
        ensureHwMenuView();
        if (this.mMenu == null || this.mMenuView.peekMenu() == null) {
            this.mMenu = new MenuBuilder(this.mContext);
            this.mMenu.setCallback(new MenuBuilderCallback());
            initHwActionMenuPresenter();
            this.mActionMenuPresenter.setReserveOverflow(true);
            HwActionMenuPresenter hwActionMenuPresenter = this.mActionMenuPresenter;
            ActionMenuPresenterCallback actionMenuPresenterCallback = this.mActionMenuPresenterCallback;
            if (actionMenuPresenterCallback == null) {
                actionMenuPresenterCallback = new ActionMenuPresenterCallback();
            }
            hwActionMenuPresenter.setCallback(actionMenuPresenterCallback);
            setMenuPresenterStatus(getSplitStatus());
            this.mMenu.addMenuPresenter(this.mActionMenuPresenter, this.mContext);
            this.mMenuView.setPresenter(this.mActionMenuPresenter);
            initExpandMenuPresenter();
            this.mMenu.addMenuPresenter(this.mExpandMenuPresenter, this.mContext);
        }
    }

    private void initHwActionMenuPresenter() {
        if (this.mActionMenuPresenter == null) {
            this.mActionMenuPresenter = new HwActionMenuPresenter(this.mContext, this.mResLoader.getIdentifier(this.mContext, "layout", "hwtoolbar_menu_layout"), this.mResLoader.getIdentifier(this.mContext, "layout", "hwtoolbar_menu_item_layout"));
        }
    }

    private void ensureHwMenuView() {
        if (this.mMenuView == null) {
            reflectMenuViewObject();
            HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
            if (hwToolbarMenuView != null) {
                hwToolbarMenuView.setColumnEnabled(this.mIsColumnEnabled);
                this.mMenuView.setPopupTheme(getPopupTheme());
                this.mMenuView.setOnMenuItemClickListener(this.mMenuViewItemClickListener);
                this.mMenuView.setMenuCallbacks(this.mActionMenuPresenterCallback, this.mMenuBuilderCallback);
                if (this.mIsUserApp) {
                    this.mMenuView.onSetSmartColor(this.mIconColor, this.mTitleColor);
                }
                addMenuViewForSystemView();
            }
        }
    }

    private void addMenuViewForSystemView() {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView != null && hwToolbarMenuView.getParent() != this) {
            Toolbar.LayoutParams params = generateDefaultLayoutParams();
            params.gravity = 8388613;
            params.width = -2;
            this.mMenuView.setLayoutParams(params);
            ReflectUtil.callMethod(this, "addSystemView", new Class[]{View.class, Boolean.TYPE}, new Object[]{this.mMenuView, Boolean.valueOf((boolean) IS_DEBUG)}, Toolbar.class);
        }
    }

    private void updateViews() {
        setSplitToolbar(getSplitStatus());
        Resources res = this.mResLoader.getResources(this.mContext);
        this.mNoIconStartMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "emui_dimens_max_start"));
        this.mNoIconEndMargin = res.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "emui_dimens_max_end"));
        int titleMarginStart = hasViewsStart() ? this.mWithIconStarMargin : this.mNoIconStartMargin;
        int titleMarginEnd = !hasViewsEnd() ? this.mNoIconEndMargin : this.mWithIconEndMargin;
        int currentTitleMarginStart = getTitleMarginStart();
        int currentTitleMarginEnd = getTitleMarginEnd();
        if (!(currentTitleMarginStart == titleMarginStart && currentTitleMarginEnd == titleMarginEnd)) {
            setTitleMarginStart(titleMarginStart);
            setTitleMarginEnd(titleMarginEnd);
        }
        handleNavigationView();
        updateLogoView(res);
        measureChild(res);
    }

    private void measureChild(Resources res) {
        int gravity = getTitleGravity();
        boolean isNeedInvalidateLayout = true;
        boolean isMarginStartZero = hasViewsStart() || this.mIsIcon1Visible;
        boolean isMarginEndZero = hasViewsEnd();
        int marginStart = isMarginStartZero ? this.mWithIconStarMargin : this.mNoIconStartMargin;
        int marginEnd = isMarginEndZero ? this.mWithIconEndMargin : this.mNoIconEndMargin;
        HwTextView hwTextView = this.mSubTitleView;
        boolean isSubTitleVisibleChanged = (hwTextView == null || hwTextView.getVisibility() == this.mSubTitleViewVisible) ? false : true;
        Spinner spinner = this.mSpinner;
        boolean isSpinnerVisible = (spinner == null || spinner.getVisibility() == this.mSpinnerVisible) ? false : true;
        if (this.mHwTitleMarginStart == marginStart && this.mHwTitleMarginEnd == marginEnd && !isSubTitleVisibleChanged) {
            isNeedInvalidateLayout = false;
        }
        if (isNeedInvalidateLayout || isSpinnerVisible) {
            invalidateTitleLayout(gravity, marginStart, marginEnd, this.mTitleView, this.mSubTitleView);
            this.mHwTitleMarginStart = marginStart;
            this.mHwTitleMarginEnd = marginEnd;
        }
        triggerIconsVisible(this.mIsIcon1Visible, this.mIsIcon2Visible);
    }

    private void updateLogoView(Resources resources) {
        Object object = ReflectUtil.getObject(this, "mLogoView", Toolbar.class);
        if (this.mLogoView == null && (object instanceof ImageView)) {
            this.mLogoView = (ImageView) object;
        }
        ImageView imageView = this.mLogoView;
        if (imageView != null && (imageView.getLayoutParams() instanceof ActionBar.LayoutParams)) {
            ActionBar.LayoutParams params = (ActionBar.LayoutParams) this.mLogoView.getLayoutParams();
            int logoStartMarginId = this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_logo_margin_start");
            int logoEndMarginId = this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_logo_margin_end");
            int logoSize = resources.getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "hwtoolbar_logo_size"));
            params.setMarginStart(resources.getDimensionPixelSize(logoStartMarginId));
            params.setMarginEnd(resources.getDimensionPixelSize(logoEndMarginId));
            params.width = logoSize;
            params.height = logoSize;
            this.mLogoView.setLayoutParams(params);
        }
    }

    private void reflectMenuViewObject() {
        View view = LayoutInflater.from(this.mResLoader.getContext(this.mContext)).inflate(this.mResLoader.getIdentifier(this.mContext, "layout", "hwtoolbar_menu_layout"), (ViewGroup) null);
        if (view instanceof HwToolbarMenuView) {
            this.mMenuView = (HwToolbarMenuView) view;
            ReflectUtil.setObject("mMenuView", this, this.mMenuView, Toolbar.class);
        }
    }

    private void ensureSpinnerAdapter(int contentId) {
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this.mContext, contentId, this.mResLoader.getIdentifier(this.mContext, "layout", "hwtoolbar_spinner_layout"));
        arrayAdapter.setDropDownViewResource(17367049);
        this.mSpinnerAdapter = arrayAdapter;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v5, resolved type: android.view.ViewGroup */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v0, types: [android.view.View, huawei.android.widget.HwToolBarMenuContainer] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void ensureSplitView() {
        if (this.mSplitView == null) {
            Activity activity = null;
            Context context = this.mContext;
            if (context instanceof Activity) {
                activity = (Activity) context;
            }
            if (activity != null) {
                View decor = activity.getWindow().getDecorView();
                View splitView = decor.findViewById(16909435);
                if (splitView instanceof HwToolBarMenuContainer) {
                    this.mSplitView = (HwToolBarMenuContainer) splitView;
                    return;
                }
                this.mSplitView = new HwToolBarMenuContainer(this.mContext);
                WindowInsets windowInsets = this.mWindowInsets;
                if (windowInsets != null) {
                    this.mSplitView.dispatchApplyWindowInsets(windowInsets);
                }
                this.mSplitView.setVisibility(0);
                this.mSplitView.setId(16909435);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -2);
                params.gravity = 80;
                View view = decor.findViewById(16908290);
                if (view instanceof ViewGroup) {
                    ((ViewGroup) view).addView((View) this.mSplitView, params);
                }
            }
        }
    }

    private boolean hasViewsStart() {
        View navView = getNavigationView();
        boolean isHaveNavView = navView != null && navView.getVisibility() == 0 && navView.getParent() == this;
        ImageView imageView = this.mLogoView;
        boolean isHaveLogo = imageView != null && imageView.getVisibility() == 0 && this.mLogoView.getParent() == this;
        if (isHaveNavView || isHaveLogo) {
            return true;
        }
        return IS_DEBUG;
    }

    public void initIconLayout() {
        this.mIconLayout = LayoutInflater.from(this.mResLoader.getContext(this.mContext)).inflate(this.mResLoader.getIdentifier(this.mContext, "layout", "hwtoolbar_title_item_layout"), (ViewGroup) null);
        this.mTitleContainer = (LinearLayout) this.mIconLayout.findViewById(this.mResLoader.getIdentifier(this.mContext, "id", "titleContainer"));
        this.mTitleView = (HwTextView) this.mIconLayout.findViewById(16908725);
        this.mSubTitleView = (HwTextView) this.mIconLayout.findViewById(16908724);
        this.mTitleView.setAutoTextInfo(this.mTitleMinSize, this.mTitleSizeStep, 1);
        this.mSubTitleView.setAutoTextInfo(this.mSubTitleMinSize, this.mTitleSizeStep, 1);
        int titleTextAppearance = 0;
        Object titleRes = ReflectUtil.getObject(this, "mTitleTextAppearance", Toolbar.class);
        if (titleRes instanceof Integer) {
            titleTextAppearance = ((Integer) titleRes).intValue();
        }
        int subTitleTextAppearance = 0;
        Object subTitleRes = ReflectUtil.getObject(this, "mSubtitleTextAppearance", Toolbar.class);
        if (subTitleRes instanceof Integer) {
            subTitleTextAppearance = ((Integer) subTitleRes).intValue();
        }
        if (titleTextAppearance != 0) {
            this.mTitleView.setTextAppearance(titleTextAppearance);
        }
        if (subTitleTextAppearance != 0) {
            this.mSubTitleView.setTextAppearance(subTitleTextAppearance);
        }
        this.mSubTitleView.setAutoTextSize(1, (float) this.mSubTitleNormalSize);
    }

    private void initSpinnerLayout() {
        ResLoader resLoader;
        if (this.mIconLayout == null) {
            initIconLayout();
        }
        if (this.mIconLayout != null && (resLoader = this.mResLoader) != null) {
            View view = ((ViewStub) this.mIconLayout.findViewById(resLoader.getIdentifier(this.mContext, "id", "spinnerContainer"))).inflate();
            if (view instanceof Spinner) {
                this.mSpinner = (Spinner) view;
            }
        }
    }

    private void initStartAndEndIconLayout() {
        ResLoader resLoader;
        if (this.mIconLayout == null) {
            initIconLayout();
        }
        if (this.mIconLayout != null && (resLoader = this.mResLoader) != null) {
            this.mIcon1View = (ImageView) ((ViewStub) this.mIconLayout.findViewById(resLoader.getIdentifier(this.mContext, "id", "hwtoolbar_icon1"))).inflate().findViewById(16908295);
            setViewClickEffectBackground(this.mIcon1View, true);
            this.mIcon2View = (ImageView) ((ViewStub) this.mIconLayout.findViewById(this.mResLoader.getIdentifier(this.mContext, "id", "hwtoolbar_icon2"))).inflate().findViewById(16908296);
            setViewClickEffectBackground(this.mIcon2View, true);
            this.mResOk = this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.DRAWABLE, "ic_public_ok");
            this.mResCancel = this.mResLoader.getIdentifier(this.mContext, ResLoaderUtil.DRAWABLE, "ic_public_cancel");
            initIconsColor();
        }
    }

    private void setViewClickEffectBackground(View view, boolean isNeedInset) {
        if (view != null && this.mClickEffectEntry != null) {
            Drawable iconBgDrawable = this.mContext.getDrawable(33751847);
            if (isNeedInset) {
                int i = this.mStartIconInsetPadding;
                view.setBackground(new InsetDrawable(iconBgDrawable, 0, i, 0, i));
                return;
            }
            view.setBackground(iconBgDrawable);
        }
    }

    private void setStartIconVisible(boolean isIcon1Visible) {
        this.mIsIcon1Visible = isIcon1Visible;
        triggerIconsVisible(this.mIsIcon1Visible, this.mIsIcon2Visible);
    }

    private void setEndIconVisible(boolean isIcon2Visible) {
        this.mIsIcon2Visible = isIcon2Visible;
        triggerIconsVisible(this.mIsIcon1Visible, this.mIsIcon2Visible);
    }

    private void triggerIconsVisible(boolean isIcon1Visible, boolean isIcon2Visible) {
        ImageView imageView = this.mIcon1View;
        if (imageView != null && this.mIcon2View != null) {
            int i = 8;
            if (isIcon1Visible) {
                imageView.setVisibility(0);
            } else {
                this.mIcon1View.setVisibility(getTitleGravity() == 8388611 ? 8 : 4);
            }
            if (isIcon2Visible) {
                this.mIcon2View.setVisibility(0);
                return;
            }
            ImageView imageView2 = this.mIcon2View;
            if (isIcon1Visible) {
                i = 4;
            }
            imageView2.setVisibility(i);
        }
    }

    private void setStartIconImage(Drawable icon) {
        ImageView imageView = this.mIcon1View;
        if (imageView != null) {
            if (icon != null) {
                imageView.setImageDrawable(icon);
                return;
            }
            int i = this.mResCancel;
            if (i != 0) {
                imageView.setImageResource(i);
            }
        }
    }

    private void setEndIconImage(Drawable icon) {
        ImageView imageView = this.mIcon2View;
        if (imageView != null) {
            if (icon != null) {
                imageView.setImageDrawable(icon);
                return;
            }
            int i = this.mResOk;
            if (i != 0) {
                imageView.setImageResource(i);
            }
        }
    }

    private void setStartIconListener(View.OnClickListener listener) {
        ImageView imageView = this.mIcon1View;
        if (imageView != null) {
            imageView.setOnClickListener(listener);
        }
    }

    private void setEndIconListener(View.OnClickListener listener) {
        ImageView imageView = this.mIcon2View;
        if (imageView != null) {
            imageView.setOnClickListener(listener);
        }
    }

    private boolean hasViewsEnd() {
        ImageView imageView;
        int menuCount = this.mMenuView.getChildCount();
        boolean isChildVisible = IS_DEBUG;
        int i = 0;
        while (true) {
            if (i >= menuCount) {
                break;
            } else if (this.mMenuView.getChildAt(i).getVisibility() != 8) {
                isChildVisible = true;
                break;
            } else {
                i++;
            }
        }
        if ((shouldLayout(this.mMenuView) && isChildVisible) || (shouldLayout(this.mIconLayout) && (imageView = this.mIcon2View) != null && imageView.getVisibility() != 8)) {
            return true;
        }
        return IS_DEBUG;
    }

    private void invalidateTitleLayout(int gravity, int marginStart, int marginEnd, TextView title, TextView subTitle) {
        if (title != null && (title.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) title.getLayoutParams();
            params.gravity = gravity;
            params.setMarginStart(marginStart);
            params.setMarginEnd(marginEnd);
            title.setLayoutParams(params);
        }
        if (subTitle != null && subTitle.getVisibility() == 0 && (subTitle.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) subTitle.getLayoutParams();
            params2.gravity = gravity;
            params2.setMarginStart(marginStart);
            params2.setMarginEnd(marginEnd);
            subTitle.setLayoutParams(params2);
        }
        Spinner spinner = this.mSpinner;
        if (spinner != null && spinner.getVisibility() == 0 && (this.mSpinner.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) this.mSpinner.getLayoutParams();
            params3.setMarginStart(marginStart);
            params3.setMarginEnd(marginEnd);
            this.mSpinner.setLayoutParams(params3);
            this.mSpinnerVisible = 0;
        }
    }

    private int getTitleGravity() {
        return 8388611;
    }

    private void initIconsColor() {
        ImageView imageView;
        ImageView imageView2;
        int i = this.mResCancel;
        if (!(i == 0 || (imageView2 = this.mIcon1View) == null)) {
            imageView2.setImageResource(i);
            ColorStateList colorStateList = this.mIconColorList;
            if (colorStateList != null) {
                this.mIcon1View.setImageTintList(colorStateList);
            }
        }
        int i2 = this.mResOk;
        if (i2 != 0 && (imageView = this.mIcon2View) != null) {
            imageView.setImageResource(i2);
            ColorStateList colorStateList2 = this.mIconColorList;
            if (colorStateList2 != null) {
                this.mIcon2View.setImageTintList(colorStateList2);
            }
        }
    }

    private void setNavButtonColor() {
        ColorStateList colorStateList;
        View navView = getNavigationView();
        if (navView instanceof ImageButton) {
            ImageButton navButton = (ImageButton) navView;
            if (this.mIconColorList != null && navButton.getImageTintList() != (colorStateList = this.mIconColorList)) {
                navButton.setImageTintList(colorStateList);
            }
        }
    }

    public boolean getSplitStatus() {
        Activity activity = null;
        Context context = this.mContext;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        boolean isSplitActionBar = IS_DEBUG;
        if (activity == null) {
            Log.w(TAG, "can not get the Activity of toolbar in getSplitStatus()");
            return IS_DEBUG;
        }
        if (this.mActivityInfo == null) {
            try {
                this.mActivityInfo = activity.getPackageManager().getActivityInfo(activity.getComponentName(), 1);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "activity.getComponentName not found");
                return IS_DEBUG;
            }
        }
        ActivityInfo activityInfo = this.mActivityInfo;
        if (activityInfo != null) {
            if (!((activityInfo.uiOptions & 1) != 0)) {
                return IS_DEBUG;
            }
            if (this.mIsSetDynamicSplitMenu) {
                return this.mIsDynamicSplitMenu;
            }
            boolean isMultiWindow = activity.isInMultiWindowMode();
            boolean isPortrait = this.mContext.getResources().getConfiguration().orientation == 1;
            if (this.mIsForceSplit || isMultiWindow || isPortrait) {
                isSplitActionBar = true;
            }
            return isSplitActionBar;
        }
        Log.w(TAG, "can not get the uiOptions in getSplitStatus()");
        return IS_DEBUG;
    }

    private void splitToolbar() {
        setSplitToolbar(getSplitStatus());
    }

    /* access modifiers changed from: private */
    public static class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private ActionMenuPresenterCallback() {
        }

        public void onCloseMenu(MenuBuilder menu, boolean isAllMenusAreClosing) {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            return HwToolbar.IS_DEBUG;
        }
    }

    /* access modifiers changed from: private */
    public class MenuBuilderCallback implements MenuBuilder.Callback {
        private MenuBuilderCallback() {
        }

        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (HwToolbar.this.mMenuViewItemClickListener == null || !HwToolbar.this.mMenuViewItemClickListener.onMenuItemClick(item)) {
                return HwToolbar.IS_DEBUG;
            }
            return true;
        }

        public void onMenuModeChange(MenuBuilder menu) {
            if (HwToolbar.this.mMenuBuilderCallback != null) {
                HwToolbar.this.mMenuBuilderCallback.onMenuModeChange(menu);
            }
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

    public HwToolBarMenuContainer getSplieView() {
        if (this.mSplitView == null) {
            ensureSplitView();
        }
        return this.mSplitView;
    }

    public void setBlurColor(int blurColor) {
        this.mBlurOverlayColor = blurColor;
    }

    public void setBlurType(int blurTypeId) {
        HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId);
        if (blurType != null) {
            this.mBlurType = blurType;
        }
    }

    public void setPositiveEnabled(boolean isEnabled) {
        ImageView imageView = this.mIcon2View;
        if (imageView != null) {
            imageView.setEnabled(isEnabled);
        }
    }

    public void setBubbleCount(int count) {
        if (this.mHwEventBadge == null) {
            this.mHwEventBadge = new HwEventBadge(getContext());
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[]{16843829});
            this.mHwEventBadge.setBackgoundColor(typedArray.getColor(0, 0));
            typedArray.recycle();
        }
        this.mHwEventBadge.setBadgeCount(count);
        setBadgeDrawable(this.mParentClassTitleTextView, count);
        setBadgeDrawable(this.mTitleView, count);
    }

    private void setBadgeDrawable(TextView textView, int count) {
        if (textView != null) {
            if (count <= 0) {
                textView.setCompoundDrawablesRelative(null, null, null, null);
                textView.setCompoundDrawablePadding(0);
                return;
            }
            textView.setCompoundDrawablesRelative(null, null, this.mHwEventBadge, null);
            textView.setCompoundDrawablePadding(this.mBadgeMargin);
        }
    }

    public void setColumnEnabled(boolean isEnabled) {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView != null) {
            this.mIsColumnEnabled = isEnabled;
            hwToolbarMenuView.setColumnEnabled(isEnabled);
            this.mMenuView.requestLayout();
            this.mMenuView.invalidate();
        }
    }

    public boolean isColumnEnabled() {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView == null) {
            return IS_DEBUG;
        }
        return hwToolbarMenuView.isColumnEnabled();
    }

    public void configureColumn(int width, int height, float density) {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView != null) {
            this.mColumnWidth = width;
            this.mColumnHeight = height;
            this.mColumnDensity = density;
            hwToolbarMenuView.configureColumn(width, height, density);
            if (isColumnEnabled()) {
                this.mMenuView.requestLayout();
                this.mMenuView.invalidate();
            }
        }
    }

    public void setInnerAlpha(float alpha) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.setAlpha(alpha);
            }
        }
    }

    private void handleStartIconView() {
        handleCancelStartIconView();
        handleNavigationView();
    }

    private void handleCancelStartIconView() {
        View view = this.mIcon1View;
        if (view != null) {
            setViewClickEffectBackground(view, true);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams == null) {
                return;
            }
            if (layoutParams.height != this.mStartIconHeight || layoutParams.width != this.mStartIconWidth) {
                layoutParams.height = this.mStartIconHeight;
                layoutParams.width = this.mStartIconWidth;
                view.setLayoutParams(layoutParams);
            }
        }
    }

    private void handleNavigationView() {
        View view = getNavigationView();
        if (view != null) {
            setViewClickEffectBackground(view, true);
            if (view.getLayoutParams() instanceof Toolbar.LayoutParams) {
                Toolbar.LayoutParams params = (Toolbar.LayoutParams) view.getLayoutParams();
                int currentMarginStart = params.getMarginStart();
                int marginStart = this.mContext.getResources().getDimensionPixelSize(this.mResLoader.getIdentifier(this.mContext, "dimen", "emui_dimens_default_start"));
                boolean isChanged = IS_DEBUG;
                if (currentMarginStart != marginStart) {
                    params.setMarginStart(marginStart);
                    isChanged = true;
                }
                if (!(params.height == this.mStartIconHeight && params.width == this.mStartIconWidth)) {
                    params.height = this.mStartIconHeight;
                    params.width = this.mStartIconWidth;
                    isChanged = true;
                }
                if (params.gravity != 16) {
                    params.gravity = 16;
                    isChanged = true;
                }
                if (isChanged) {
                    view.setLayoutParams(params);
                }
                setNavButtonColor();
            }
        }
    }

    public ColorStateList getSmartIconColor() {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView == null) {
            return null;
        }
        return hwToolbarMenuView.getSmartIconColor();
    }

    public ColorStateList getSmartTitleColor() {
        HwToolbarMenuView hwToolbarMenuView = this.mMenuView;
        if (hwToolbarMenuView == null) {
            return null;
        }
        return hwToolbarMenuView.getSmartTitleColor();
    }
}
