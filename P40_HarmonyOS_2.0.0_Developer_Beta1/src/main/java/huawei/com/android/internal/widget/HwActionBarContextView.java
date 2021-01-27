package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.ActionBarContextView;
import com.huawei.android.os.ProcessExt;
import huawei.android.widget.HwActionMenuPresenter;
import huawei.android.widget.utils.ClickEffectEntry;
import huawei.android.widget.utils.HwWidgetUtils;
import huawei.android.widget.utils.ResLoader;
import java.lang.ref.WeakReference;

public class HwActionBarContextView extends ActionBarContextView implements View.OnClickListener, ActionModeView {
    private static final int DOUBLE = 2;
    private static final String TAG = "HwActionBarContextView";
    private WeakReference<ActionMode> mActionMode;
    private int mBaseEndMargin;
    private ImageView mCancel;
    private ActionMenuItem mCancelMenuItem;
    private ClickEffectEntry mClickEffectEntry;
    private ColorStateList mIconColors;
    private boolean mIsLightStyle;
    private ImageView mOk;
    private ActionMenuItem mOkMenuItem;
    private int mResCancel;
    private ResLoader mResLoader;
    private int mResOk;

    public HwActionBarContextView(Context context) {
        this(context, null);
    }

    public HwActionBarContextView(Context context, AttributeSet attrs) {
        super(context, attrs, 16843668);
        int[] hwToolbarStyles;
        this.mClickEffectEntry = null;
        this.mIsLightStyle = HwWidgetFactory.getSuggestionForgroundColorStyle(context) == 0;
        this.mOkMenuItem = new ActionMenuItem(context, 0, 16909228, 0, 0, context.getString(17039370));
        this.mCancelMenuItem = new ActionMenuItem(context, 0, 16908828, 0, 0, context.getString(17039360));
        this.mResLoader = ResLoader.getInstance();
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null && (hwToolbarStyles = this.mResLoader.getIdentifierArray(context, "styleable", "HwToolbar")) != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(attrs, hwToolbarStyles, 16843946, 0);
            this.mIconColors = typedArray.getColorStateList(this.mResLoader.getIdentifier(this.mContext, "styleable", "HwToolbar_hwToolbarIconColor"));
            typedArray.recycle();
            this.mClickEffectEntry = HwWidgetUtils.getCleckEffectEntry(context, 16843946);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: huawei.com.android.internal.widget.HwActionBarContextView */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // huawei.com.android.internal.widget.ActionModeView
    public void initForMode(ActionMode mode) {
        if (mode != null) {
            this.mActionMode = new WeakReference<>(mode);
            if (this.mActionMenuPresenter != null) {
                this.mActionMenuPresenter.dismissPopupMenus();
            }
            this.mActionMenuPresenter = new HwActionMenuPresenter(this.mContext, 34013286, 34013287);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-2, -1);
            if (!this.mSplitActionBar) {
                if (mode.getMenu() instanceof MenuBuilder) {
                    mode.getMenu().addMenuPresenter(this.mActionMenuPresenter);
                }
                if (this.mActionMenuPresenter.getMenuView(this) instanceof ActionMenuView) {
                    this.mMenuView = this.mActionMenuPresenter.getMenuView(this);
                    this.mMenuView.setBackgroundDrawable(null);
                    addView(this.mMenuView, params);
                }
            } else {
                this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                params.width = -2;
                params.height = -2;
                if (mode.getMenu() instanceof MenuBuilder) {
                    mode.getMenu().addMenuPresenter(this.mActionMenuPresenter);
                }
                if (this.mActionMenuPresenter.getMenuView(this) instanceof ActionMenuView) {
                    this.mMenuView = this.mActionMenuPresenter.getMenuView(this);
                    setChildSplitViewGone(params);
                }
            }
            initTitle();
        }
    }

    private void setChildSplitViewGone(ViewGroup.LayoutParams params) {
        if (this.mSplitView != null) {
            int childCount = this.mSplitView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mSplitView.getChildAt(i).setVisibility(8);
            }
            this.mSplitView.addView(this.mMenuView, params);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: huawei.com.android.internal.widget.HwActionBarContextView */
    /* JADX WARN: Multi-variable type inference failed */
    private LinearLayout initTitleLayout() {
        View view = LayoutInflater.from(this.mContext).inflate(34013190, (ViewGroup) this, false);
        if (!(view instanceof LinearLayout)) {
            return null;
        }
        LinearLayout titleLayout = (LinearLayout) view;
        this.mOk = (ImageView) titleLayout.findViewById(16909228);
        this.mCancel = (ImageView) titleLayout.findViewById(16908828);
        this.mResOk = 33751080;
        this.mResCancel = 33751079;
        int i = this.mResCancel;
        if (i != 0) {
            this.mCancel.setImageResource(i);
            this.mCancel.setImageTintList(this.mIconColors);
            this.mCancel.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, this.mClickEffectEntry));
        }
        int i2 = this.mResOk;
        if (i2 != 0) {
            this.mOk.setImageResource(i2);
            this.mOk.setImageTintList(this.mIconColors);
            this.mOk.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, this.mClickEffectEntry));
        }
        ImageView imageView = this.mOk;
        if (imageView != null) {
            imageView.setOnClickListener(this);
            if (this.mOk.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                this.mBaseEndMargin = ((LinearLayout.LayoutParams) this.mOk.getLayoutParams()).getMarginEnd();
            }
        }
        ImageView imageView2 = this.mCancel;
        if (imageView2 != null) {
            imageView2.setOnClickListener(this);
        }
        return titleLayout;
    }

    /* access modifiers changed from: protected */
    public void initTitle() {
        TextView titleView = getTitleView();
        TextView subTitleView = getSubtitleView();
        LinearLayout titleLayout = getTitleLayout();
        if (titleLayout == null && (titleLayout = initTitleLayout()) != null) {
            titleView = (TextView) titleLayout.findViewById(16908725);
            subTitleView = (TextView) titleLayout.findViewById(16908724);
            if (titleView != null) {
                setTitleView(titleView);
            }
            if (!(titleView == null || subTitleView == null)) {
                setSubtitleView(subTitleView);
            }
        }
        if (titleView != null) {
            titleView.setText(getTitle());
        }
        if (subTitleView != null) {
            subTitleView.setVisibility(TextUtils.isEmpty(getSubtitle()) ^ true ? 0 : 8);
            subTitleView.setText(getSubtitle());
        }
        initTitleAppearance();
        if (titleLayout != null && titleLayout.getParent() == null) {
            addView(titleLayout);
            setTitleLayout(titleLayout);
        }
    }

    @Override // huawei.com.android.internal.widget.ActionModeView
    public void killMode() {
        HwActionBarContextView.super.killMode();
        this.mActionMode = null;
        this.mActionMenuPresenter = null;
        if (this.mSplitView != null) {
            int childCount = this.mSplitView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = this.mSplitView.getChildAt(i);
                if (!(view == null || view == this.mMenuView)) {
                    view.setVisibility(0);
                }
            }
        }
    }

    @Override // huawei.com.android.internal.widget.ActionModeView
    public void closeMode() {
        killMode();
    }

    @Override // huawei.com.android.internal.widget.ActionModeView
    public void cancelVisibilityAnimation() {
        if (this.mVisibilityAnim != null) {
            this.mVisibilityAnim.cancel();
            this.mVisibilityAnim = null;
        }
    }

    /* access modifiers changed from: protected */
    public int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        if (child != getTitleLayout()) {
            return HwActionBarContextView.super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        }
        invalidateTitleLayout();
        child.measure(View.MeasureSpec.makeMeasureSpec(availableWidth, ProcessExt.SCHED_RESET_ON_FORK), childSpecHeight);
        int realWidth = (availableWidth - child.getMeasuredWidth()) - spacing;
        if (realWidth > 0) {
            return realWidth;
        }
        return 0;
    }

    private void invalidateTitleLayout() {
        int gravity = getTitleGravity();
        TextView title = getTitleView();
        TextView subTitle = getSubtitleView();
        int margin = getResources().getDimensionPixelSize(34472184);
        ImageView imageView = this.mCancel;
        int margin2 = 0;
        if (!(imageView != null && imageView.getVisibility() == 0)) {
            margin2 = margin;
        }
        invalidateTitleLayout(gravity, margin2, title, subTitle);
    }

    private void invalidateTitleLayout(int gravity, int margin, TextView title, TextView subTitle) {
        if (title != null && (title.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) title.getLayoutParams();
            titleParams.gravity = gravity;
            titleParams.setMarginStart(margin);
            title.setLayoutParams(titleParams);
        }
        if (subTitle != null && subTitle.getVisibility() == 0 && (subTitle.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            LinearLayout.LayoutParams subTitleParams = (LinearLayout.LayoutParams) subTitle.getLayoutParams();
            subTitleParams.gravity = gravity;
            subTitle.setLayoutParams(subTitleParams);
        }
    }

    private int getTitleGravity() {
        ImageView imageView = this.mOk;
        boolean isCancelVisible = true;
        boolean isOkVisible = imageView != null && imageView.getVisibility() == 0;
        ImageView imageView2 = this.mCancel;
        if (imageView2 == null || imageView2.getVisibility() != 0) {
            isCancelVisible = false;
        }
        return getTitleGravity(isOkVisible, isCancelVisible);
    }

    private int getTitleGravity(boolean isOkVisible, boolean isCancelVisible) {
        boolean isMenuEmpty = getMenuItemCountInActionBarView() == 0;
        ImageView imageView = this.mOk;
        if (imageView == null || !(imageView.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            return 8388611;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.mOk.getLayoutParams();
        if (isMenuEmpty) {
            params.setMarginEnd(this.mBaseEndMargin);
        } else {
            params.setMarginEnd(this.mBaseEndMargin * 2);
        }
        this.mOk.setLayoutParams(params);
        return 8388611;
    }

    private int getMenuItemCountInActionBarView() {
        if (this.mMenuView == null || this.mMenuView.getParent() != this) {
            return 0;
        }
        return this.mMenuView.getChildCount();
    }

    public void setImageResource(int resIdOk, int resIdCancel) {
        ImageView imageView;
        ImageView imageView2;
        if (resIdOk > 0) {
            ImageView imageView3 = this.mOk;
            if (imageView3 != null) {
                imageView3.setImageResource(resIdOk);
                this.mOk.setImageTintList(this.mIconColors);
            }
        } else {
            int i = this.mResOk;
            if (!(i == 0 || (imageView2 = this.mOk) == null)) {
                imageView2.setImageResource(i);
            }
        }
        if (resIdCancel > 0) {
            ImageView imageView4 = this.mCancel;
            if (imageView4 != null) {
                imageView4.setImageResource(resIdCancel);
                this.mCancel.setImageTintList(this.mIconColors);
                return;
            }
            return;
        }
        int i2 = this.mResCancel;
        if (i2 != 0 && (imageView = this.mCancel) != null) {
            imageView.setImageResource(i2);
        }
    }

    private void setActionVisible() {
        ImageView imageView = this.mOk;
        boolean isCancelVisible = true;
        boolean isOkVisible = imageView != null && imageView.getVisibility() == 0;
        ImageView imageView2 = this.mCancel;
        if (imageView2 == null || imageView2.getVisibility() != 0) {
            isCancelVisible = false;
        }
        setActionVisible(isOkVisible, isCancelVisible);
    }

    public void setActionVisible(boolean isOkVisible, boolean isCancelVisible) {
        ImageView imageView;
        if (this.mOk == null || (imageView = this.mCancel) == null) {
            throw new IllegalArgumentException("pls set the correct res for the ok and cancel button.");
        }
        int i = 8;
        if (isCancelVisible) {
            imageView.setVisibility(0);
        } else {
            this.mCancel.setVisibility(getTitleGravity(isOkVisible, isCancelVisible) == 8388611 ? 8 : 4);
        }
        if (isOkVisible) {
            this.mOk.setVisibility(0);
            return;
        }
        ImageView imageView2 = this.mOk;
        if (isCancelVisible) {
            i = 4;
        }
        imageView2.setVisibility(i);
    }

    public void setContentDescription(CharSequence okContentDescription, CharSequence cancelContentDescription) {
        ImageView imageView = this.mOk;
        if (!(imageView == null || okContentDescription == null)) {
            imageView.setContentDescription(okContentDescription);
        }
        ImageView imageView2 = this.mCancel;
        if (imageView2 != null && cancelContentDescription != null) {
            imageView2.setContentDescription(cancelContentDescription);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        MenuBuilder.Callback menuCallback = null;
        ActionMode mode = null;
        if (this.mActionMode.get() instanceof MenuBuilder.Callback) {
            menuCallback = this.mActionMode.get();
        }
        if (this.mActionMode.get() instanceof ActionMode) {
            mode = this.mActionMode.get();
        }
        if (menuCallback != null && mode != null && view != null) {
            if (view.getId() == 16909228) {
                if ((mode.getMenu() instanceof MenuBuilder) && !menuCallback.onMenuItemSelected(mode.getMenu(), this.mOkMenuItem)) {
                    mode.finish();
                }
            } else if (view.getId() != 16908828) {
                Log.e(TAG, "invalid view id");
            } else if ((mode.getMenu() instanceof MenuBuilder) && !menuCallback.onMenuItemSelected(mode.getMenu(), this.mCancelMenuItem)) {
                mode.finish();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getMenuViewHeight(int contentHeight) {
        return -2;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        ViewGroup viewGroup;
        HwActionBarContextView.super.onLayout(isChanged, left, top, right, bottom);
        if (isChanged && getParent() != null && (getParent() instanceof ViewGroup) && (viewGroup = (ViewGroup) getParent()) != null && viewGroup.getId() != 16909435 && viewGroup.getId() != 16908722) {
            setBackgroundDrawable(new ColorDrawable(HwWidgetFactory.getPrimaryColor(getContext())));
        }
    }

    /* access modifiers changed from: protected */
    public void initTitleAppearance() {
        HwActionBarContextView.super.initTitleAppearance();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        WeakReference<ActionMode> weakReference;
        ActionMode actionMode;
        HwActionBarContextView.super.onConfigurationChanged(newConfig);
        if (!(this.mIsLightStyle || (weakReference = this.mActionMode) == null || (actionMode = weakReference.get()) == null)) {
            actionMode.invalidate();
        }
        if (this.mOk != null && this.mCancel != null) {
            setActionVisible();
        }
    }

    public void setPositiveEnabled(boolean isEnabled) {
        ImageView imageView = this.mOk;
        if (imageView != null) {
            imageView.setEnabled(isEnabled);
        }
    }
}
