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
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.Callback;
import com.android.internal.widget.ActionBarContextView;
import com.huawei.hsm.permission.StubController;
import huawei.android.widget.HwActionMenuPresenter;
import java.lang.ref.WeakReference;

public class HwActionBarContextView extends ActionBarContextView implements OnClickListener, ActionModeView {
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
        boolean z;
        super(context, attrs, 16843668);
        if (HwWidgetFactory.getSuggestionForgroundColorStyle(context) == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsLightStyle = z;
        this.mOKMenuItem = new ActionMenuItem(context, 0, 16909119, 0, 0, context.getString(17039370));
        this.mCancelMenuItem = new ActionMenuItem(context, 0, 16908779, 0, 0, context.getString(17039360));
    }

    public void initForMode(ActionMode mode) {
        this.mActionMode = new WeakReference(mode);
        MenuBuilder menuBuilder = (MenuBuilder) mode.getMenu();
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.dismissPopupMenus();
        }
        this.mActionMenuPresenter = new HwActionMenuPresenter(this.mContext, 34013188, 34013187);
        LayoutParams lp = new LayoutParams(-2, -1);
        if (this.mSplitActionBar) {
            this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
            this.mActionMenuPresenter.setItemLimit(HwSignalStrength.WCDMA_STRENGTH_INVALID);
            lp.width = -2;
            lp.height = -2;
            menuBuilder.addMenuPresenter(this.mActionMenuPresenter);
            this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
            if (this.mSplitView != null) {
                this.mSplitView.addView(this.mMenuView, lp);
            }
        } else {
            menuBuilder.addMenuPresenter(this.mActionMenuPresenter);
            this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
            this.mMenuView.setBackgroundDrawable(null);
            addView(this.mMenuView, lp);
        }
        initTitle();
    }

    protected void initTitle() {
        TextView titleView = getTitleView();
        TextView subTitleView = getSubtitleView();
        LinearLayout titleLayout = getTitleLayout();
        if (titleLayout == null) {
            titleLayout = (LinearLayout) LayoutInflater.from(this.mContext).inflate(34013190, this, false);
            if (titleLayout != null) {
                this.mOK = (ImageView) titleLayout.findViewById(16909119);
                this.mCancel = (ImageView) titleLayout.findViewById(16908779);
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
                titleView = (TextView) titleLayout.findViewById(16908684);
                subTitleView = (TextView) titleLayout.findViewById(16908683);
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
            subTitleView.setVisibility(TextUtils.isEmpty(getSubtitle()) ^ 1 ? 0 : 8);
            subTitleView.setText(getSubtitle());
        }
        initTitleAppearance();
        if (titleLayout != null && titleLayout.getParent() == null) {
            addView(titleLayout);
            setTitleLayout(titleLayout);
        }
    }

    public void killMode() {
        super.killMode();
        this.mActionMode = null;
        this.mActionMenuPresenter = null;
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

    protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        if (child != getTitleLayout()) {
            return super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        }
        invalidateTitleLayout();
        child.measure(MeasureSpec.makeMeasureSpec(availableWidth, StubController.PERMISSION_ACCESS_BROWSER_RECORDS), childSpecHeight);
        availableWidth = (availableWidth - child.getMeasuredWidth()) - spacing;
        if (availableWidth <= 0) {
            availableWidth = 0;
        }
        return availableWidth;
    }

    private void invalidateTitleLayout() {
        int gravity = getTitleGravity();
        TextView title = getTitleView();
        TextView subTitle = getSubtitleView();
        int margin = getResources().getDimensionPixelSize(34472184);
        if (!(gravity == 8388611)) {
            margin = 0;
        }
        HwActionBarView.invalidateTitleLayout(gravity, margin, title, subTitle);
    }

    private int getTitleGravity() {
        boolean okVis = this.mOK != null && this.mOK.getVisibility() == 0;
        boolean cancelVis = this.mCancel != null && this.mCancel.getVisibility() == 0;
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
        boolean okVis = this.mOK != null && this.mOK.getVisibility() == 0;
        boolean cancelVis = this.mCancel != null && this.mCancel.getVisibility() == 0;
        setActionVisible(okVis, cancelVis);
    }

    public void setActionVisible(boolean OKVis, boolean cancelVis) {
        int i = 4;
        if (this.mOK == null || this.mCancel == null) {
            throw new RuntimeException("pls set the correct res for the ok and cancel button.");
        }
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

    public void onClick(View view) {
        Callback callback = null;
        ActionMode actionMode = null;
        if (this.mActionMode.get() instanceof Callback) {
            callback = (Callback) this.mActionMode.get();
        }
        if (this.mActionMode.get() instanceof ActionMode) {
            actionMode = (ActionMode) this.mActionMode.get();
        }
        if (callback != null && actionMode != null) {
            if (view.getId() == 16909119) {
                if (!callback.onMenuItemSelected((MenuBuilder) actionMode.getMenu(), this.mOKMenuItem)) {
                    actionMode.finish();
                }
            } else if (view.getId() == 16908779 && !callback.onMenuItemSelected((MenuBuilder) actionMode.getMenu(), this.mCancelMenuItem)) {
                actionMode.finish();
            }
        }
    }

    protected int getMenuViewHeight(int contentHeight) {
        return -2;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && getParent() != null) {
            ViewGroup vp = (ViewGroup) getParent();
            if (vp != null && vp.getId() != 16909314 && vp.getId() != 16908681) {
                setBackgroundDrawable(new ColorDrawable(HwWidgetFactory.getPrimaryColor(getContext())));
            }
        }
    }

    protected void initTitleAppearance() {
        super.initTitleAppearance();
        HwActionBarView.initTitleAppearance(getContext(), getTitleView(), getSubtitleView());
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

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!(this.mIsLightStyle || this.mActionMode == null)) {
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
