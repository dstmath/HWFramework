package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.ActionBarContextView;
import com.huawei.hsm.permission.StubController;
import huawei.android.widget.HwActionMenuPresenter;
import java.lang.ref.WeakReference;

public class HwActionBarContextView extends ActionBarContextView implements View.OnClickListener, ActionModeView {
    private static final String TAG = "HwActionBarContextView";
    private WeakReference<ActionMode> mActionMode;
    private int mBaseEndMargin;
    private ImageView mCancel;
    private ActionMenuItem mCancelMenuItem;
    private boolean mIsLightStyle;
    private ImageView mOK;
    private ActionMenuItem mOKMenuItem;
    private int mResCancel;
    private int mResOK;

    public HwActionBarContextView(Context context) {
        this(context, null);
    }

    public HwActionBarContextView(Context context, AttributeSet attrs) {
        super(context, attrs, 16843668);
        this.mIsLightStyle = HwWidgetFactory.getSuggestionForgroundColorStyle(context) == 0;
        Context context2 = context;
        ActionMenuItem actionMenuItem = new ActionMenuItem(context2, 0, 16909156, 0, 0, context.getString(17039370));
        this.mOKMenuItem = actionMenuItem;
        ActionMenuItem actionMenuItem2 = new ActionMenuItem(context2, 0, 16908798, 0, 0, context.getString(17039360));
        this.mCancelMenuItem = actionMenuItem2;
    }

    /* JADX WARNING: type inference failed for: r6v0, types: [huawei.com.android.internal.widget.HwActionBarContextView, android.view.ViewGroup] */
    public void initForMode(ActionMode mode) {
        this.mActionMode = new WeakReference<>(mode);
        MenuBuilder menuBuilder = mode.getMenu();
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.dismissPopupMenus();
        }
        this.mActionMenuPresenter = new HwActionMenuPresenter(this.mContext, 34013286, 34013287);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(-2, -1);
        if (!this.mSplitActionBar) {
            menuBuilder.addMenuPresenter(this.mActionMenuPresenter);
            this.mMenuView = this.mActionMenuPresenter.getMenuView(this);
            this.mMenuView.setBackgroundDrawable(null);
            addView(this.mMenuView, lp);
        } else {
            this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
            this.mActionMenuPresenter.setItemLimit(HwSignalStrength.WCDMA_STRENGTH_INVALID);
            lp.width = -2;
            lp.height = -2;
            menuBuilder.addMenuPresenter(this.mActionMenuPresenter);
            this.mMenuView = this.mActionMenuPresenter.getMenuView(this);
            if (this.mSplitView != null) {
                int childCount = this.mSplitView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    this.mSplitView.getChildAt(i).setVisibility(8);
                }
                this.mSplitView.addView(this.mMenuView, lp);
            }
        }
        initTitle();
    }

    /* JADX WARNING: type inference failed for: r5v1, types: [android.view.View] */
    /* JADX WARNING: type inference failed for: r6v3, types: [android.view.View] */
    /* JADX WARNING: type inference failed for: r6v5, types: [android.view.View] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public void initTitle() {
        TextView titleView = getTitleView();
        TextView subTitleView = getSubtitleView();
        LinearLayout titleLayout = getTitleLayout();
        int i = 0;
        if (titleLayout == null) {
            titleLayout = LayoutInflater.from(this.mContext).inflate(34013190, this, false);
            if (titleLayout != null) {
                this.mOK = (ImageView) titleLayout.findViewById(16909156);
                this.mCancel = (ImageView) titleLayout.findViewById(16908798);
                this.mResOK = 33751080;
                this.mResCancel = 33751079;
                ColorStateList color = getImmersionTint(this.mContext);
                if (this.mResCancel != 0) {
                    this.mCancel.setImageResource(this.mResCancel);
                    this.mCancel.setImageTintList(color);
                }
                if (this.mResOK != 0) {
                    this.mOK.setImageResource(this.mResOK);
                    this.mOK.setImageTintList(color);
                }
                titleView = titleLayout.findViewById(16908696);
                subTitleView = titleLayout.findViewById(16908695);
                if (this.mOK != null) {
                    this.mOK.setOnClickListener(this);
                    this.mBaseEndMargin = ((LinearLayout.LayoutParams) this.mOK.getLayoutParams()).getMarginEnd();
                }
                if (this.mCancel != null) {
                    this.mCancel.setOnClickListener(this);
                }
                if (titleView != null) {
                    setTitleView(titleView);
                }
                if (titleView != null) {
                    setSubtitleView(subTitleView);
                }
            }
        }
        if (titleView != null) {
            titleView.setText(getTitle());
        }
        if (subTitleView != null) {
            if (!(!TextUtils.isEmpty(getSubtitle()))) {
                i = 8;
            }
            subTitleView.setVisibility(i);
            subTitleView.setText(getSubtitle());
        }
        initTitleAppearance();
        if (titleLayout != null && titleLayout.getParent() == null) {
            addView(titleLayout);
            setTitleLayout(titleLayout);
        }
    }

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

    public void closeMode() {
        killMode();
    }

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
        child.measure(View.MeasureSpec.makeMeasureSpec(availableWidth, StubController.PERMISSION_ACCESS_BROWSER_RECORDS), childSpecHeight);
        int availableWidth2 = (availableWidth - child.getMeasuredWidth()) - spacing;
        return availableWidth2 > 0 ? availableWidth2 : 0;
    }

    private void invalidateTitleLayout() {
        int gravity = getTitleGravity();
        TextView title = getTitleView();
        TextView subTitle = getSubtitleView();
        int margin = getResources().getDimensionPixelSize(34472184);
        int margin2 = 0;
        if (gravity == 8388611) {
            margin2 = margin;
        }
        invalidateTitleLayout(gravity, margin2, title, subTitle);
    }

    private void invalidateTitleLayout(int gravity, int margin, TextView title, TextView subTitle) {
        if (title != null) {
            LinearLayout.LayoutParams lpTitle = (LinearLayout.LayoutParams) title.getLayoutParams();
            lpTitle.gravity = gravity;
            lpTitle.setMarginStart(margin);
            title.setLayoutParams(lpTitle);
        }
        if (subTitle != null && subTitle.getVisibility() == 0) {
            LinearLayout.LayoutParams lpSubTitle = (LinearLayout.LayoutParams) subTitle.getLayoutParams();
            lpSubTitle.gravity = gravity;
            subTitle.setLayoutParams(lpSubTitle);
        }
    }

    private int getTitleGravity() {
        boolean cancelVis = false;
        boolean okVis = this.mOK != null && this.mOK.getVisibility() == 0;
        if (this.mCancel != null && this.mCancel.getVisibility() == 0) {
            cancelVis = true;
        }
        return getTitleGravity(okVis, cancelVis);
    }

    private int getTitleGravity(boolean okVis, boolean cancelVis) {
        boolean noMenu = getMenuItemCountInActionBarView() == 0;
        if (this.mOK != null) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mOK.getLayoutParams();
            if (noMenu) {
                lp.setMarginEnd(this.mBaseEndMargin);
            } else {
                lp.setMarginEnd(this.mBaseEndMargin * 2);
            }
            this.mOK.setLayoutParams(lp);
        }
        if (cancelVis) {
            return 1;
        }
        return 8388611;
    }

    private int getMenuItemCountInActionBarView() {
        if (this.mMenuView == null || this.mMenuView.getParent() != this) {
            return 0;
        }
        return this.mMenuView.getChildCount();
    }

    public void setImageResource(int resIdOK, int resIdCancel) {
        ColorStateList color = getImmersionTint(getContext());
        if (resIdOK > 0) {
            this.mOK.setImageResource(resIdOK);
            this.mOK.setImageTintList(color);
        } else if (this.mResOK != 0) {
            this.mOK.setImageResource(this.mResOK);
        }
        if (resIdCancel > 0) {
            this.mCancel.setImageResource(resIdCancel);
            this.mCancel.setImageTintList(color);
        } else if (this.mResCancel != 0) {
            this.mCancel.setImageResource(this.mResCancel);
        }
    }

    private void setActionVisible() {
        boolean cancelVis = false;
        boolean okVis = this.mOK != null && this.mOK.getVisibility() == 0;
        if (this.mCancel != null && this.mCancel.getVisibility() == 0) {
            cancelVis = true;
        }
        setActionVisible(okVis, cancelVis);
    }

    public void setActionVisible(boolean OKVis, boolean cancelVis) {
        if (this.mOK == null || this.mCancel == null) {
            throw new RuntimeException("pls set the correct res for the ok and cancel button.");
        }
        int i = 4;
        if (cancelVis) {
            this.mCancel.setVisibility(0);
        } else {
            this.mCancel.setVisibility(getTitleGravity(OKVis, cancelVis) == 8388611 ? 8 : 4);
        }
        if (OKVis) {
            this.mOK.setVisibility(0);
            return;
        }
        ImageView imageView = this.mOK;
        if (!cancelVis) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    public void setContentDescription(CharSequence okContentDescription, CharSequence cancelContentDescription) {
        if (!(this.mOK == null || okContentDescription == null)) {
            this.mOK.setContentDescription(okContentDescription);
        }
        if (this.mCancel != null && cancelContentDescription != null) {
            this.mCancel.setContentDescription(cancelContentDescription);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.view.ActionMode} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v17, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.android.internal.view.menu.MenuBuilder$Callback} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onClick(View view) {
        MenuBuilder.Callback menuCallback = null;
        ActionMode mode = null;
        if (this.mActionMode.get() instanceof MenuBuilder.Callback) {
            menuCallback = this.mActionMode.get();
        }
        if (this.mActionMode.get() instanceof ActionMode) {
            mode = this.mActionMode.get();
        }
        if (menuCallback != null && mode != null) {
            if (view.getId() == 16909156) {
                if (!menuCallback.onMenuItemSelected(mode.getMenu(), this.mOKMenuItem)) {
                    mode.finish();
                }
            } else if (view.getId() == 16908798 && !menuCallback.onMenuItemSelected(mode.getMenu(), this.mCancelMenuItem)) {
                mode.finish();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getMenuViewHeight(int contentHeight) {
        return -2;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        HwActionBarContextView.super.onLayout(changed, l, t, r, b);
        if (changed && getParent() != null) {
            ViewGroup vp = (ViewGroup) getParent();
            if (vp != null && vp.getId() != 16909362 && vp.getId() != 16908693) {
                setBackgroundDrawable(new ColorDrawable(HwWidgetFactory.getPrimaryColor(getContext())));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initTitleAppearance() {
        HwActionBarContextView.super.initTitleAppearance();
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

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        HwActionBarContextView.super.onConfigurationChanged(newConfig);
        if (!this.mIsLightStyle && this.mActionMode != null) {
            ActionMode ac = (ActionMode) this.mActionMode.get();
            if (ac != null) {
                ac.invalidate();
            }
        }
        if (this.mOK != null && this.mCancel != null) {
            setActionVisible();
        }
    }
}
