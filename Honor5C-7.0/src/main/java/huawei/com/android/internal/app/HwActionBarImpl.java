package huawei.com.android.internal.app;

import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.app.WindowDecorActionBar.TabImpl;
import com.android.internal.widget.ActionBarContainer;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.ActionBarView;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ScrollingTabContainerView;
import com.huawei.hsm.permission.StubController;
import huawei.com.android.internal.app.DefaultActionModeImpl.ActionModeCallback;
import huawei.com.android.internal.widget.ActionModeView;
import huawei.com.android.internal.widget.HwActionBarContainer;
import huawei.com.android.internal.widget.HwActionBarContextView;
import huawei.com.android.internal.widget.HwActionBarOverlayLayout;
import huawei.com.android.internal.widget.HwActionBarView;
import huawei.com.android.internal.widget.HwCustomPanel;
import huawei.com.android.internal.widget.HwScrollingTabContainerView;

public class HwActionBarImpl extends WindowDecorActionBar implements ActionModeCallback {
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final String TAG = "HwActionBarImpl";
    private DefaultActionModeImpl mCurrentActionMode;
    private ActionModeView mCurrentActionModeView;
    private View mCustomDragView;
    HwCustomPanel mCustomPanel;
    private boolean mCustomPanelInflated;
    private ViewStub mCustomPanelStub;

    public class HwTabImpl extends TabImpl {
        private int mTabViewId;

        public HwTabImpl() {
            super(HwActionBarImpl.this);
            this.mTabViewId = -1;
        }

        public void setTabViewId(int id) {
            this.mTabViewId = id;
        }

        public int getTabViewId() {
            return this.mTabViewId;
        }
    }

    public interface InnerOnStageChangedListener {
        void onEnterNextStage();

        void onExitNextStage();
    }

    public HwActionBarImpl(Activity activity) {
        super(activity);
    }

    public HwActionBarImpl(Dialog dialog) {
        super(dialog);
    }

    public void finish() {
        if (this.mCurrentActionMode != null) {
            this.mCurrentActionMode.finish();
            DecorToolbar view = getDecorToolbar();
            if (view instanceof ActionBarView) {
                ((ActionBarView) view).sendAccessibilityEvent(32);
            }
        }
    }

    protected void initContainerView(View decor) {
        View view = decor.findViewById(16909290);
        if (view instanceof HwActionBarContainer) {
            setContainerView((HwActionBarContainer) view);
        }
    }

    protected void initContextView(View decor) {
        View view = decor.findViewById(16909292);
        if (view instanceof HwActionBarContextView) {
            setContextView((HwActionBarContextView) view);
        }
    }

    protected ScrollingTabContainerView initScrollingTabContainerView() {
        return new HwScrollingTabContainerView(getContext());
    }

    public ActionMode startActionMode(Callback callback) {
        if (this.mCurrentActionMode != null) {
            this.mCurrentActionMode.finish();
            if (this.mCurrentActionModeView != null) {
                this.mCurrentActionModeView.cancelVisibilityAnimation();
                this.mCurrentActionModeView.killMode();
            }
        }
        this.mCurrentActionMode = createActionMode(callback);
        this.mCurrentActionMode.setActionModeCallback(this);
        this.mCurrentActionModeView = createActionModeView(callback);
        this.mCurrentActionMode.setActionModeView(this.mCurrentActionModeView);
        if (!this.mCurrentActionMode.dispatchOnCreate()) {
            return null;
        }
        this.mCurrentActionMode.invalidate();
        this.mCurrentActionModeView.initForMode(this.mCurrentActionMode);
        animateToMode(true);
        ActionBarContainer spView = getSplitView();
        ActionBarOverlayLayout over = getOverlayLayout();
        if (!(spView == null || getContextDisplayMode() != CONTEXT_DISPLAY_SPLIT || spView.getVisibility() == 0)) {
            spView.setVisibility(CONTEXT_DISPLAY_NORMAL);
            if (over != null) {
                over.requestFitSystemWindows();
            }
        }
        ((View) this.mCurrentActionModeView).sendAccessibilityEvent(32);
        return this.mCurrentActionMode;
    }

    public void setScrollTabAnimEnable(boolean shouldAnim) {
        ScrollingTabContainerView tabContainer = getTabScrollView();
        if (tabContainer != null) {
            tabContainer.setShouldAnimToTab(shouldAnim);
        }
    }

    public DefaultActionModeImpl createActionMode(Callback callback) {
        return new EditActionModeImpl(getThemedContext(), callback);
    }

    public ActionModeView createActionModeView(Callback callback) {
        return (ActionModeView) getContextView();
    }

    public void onActionModeFinish(ActionMode actionMode) {
        animateToMode(false);
        if (actionMode == this.mCurrentActionMode) {
            this.mCurrentActionMode = null;
            this.mCurrentActionModeView = null;
        }
    }

    public void setStartIcon(boolean icon1Visible, Drawable icon1, OnClickListener listener1) {
        DecorToolbar view = getDecorToolbar();
        HwActionBarView acview = null;
        if (view instanceof HwActionBarView) {
            acview = (HwActionBarView) view;
        }
        if (acview != null) {
            acview.setStartIconVisible(icon1Visible);
            acview.setStartIconImage(icon1);
            acview.setStartIconListener(listener1);
        }
    }

    public void setEndIcon(boolean icon2Visible, Drawable icon2, OnClickListener listener2) {
        DecorToolbar view = getDecorToolbar();
        HwActionBarView acview = null;
        if (view instanceof HwActionBarView) {
            acview = (HwActionBarView) view;
        }
        if (acview != null) {
            acview.setEndIconVisible(icon2Visible);
            acview.setEndIconImage(icon2);
            acview.setEndIconListener(listener2);
        }
    }

    public void setCustomTitle(View view) {
        DecorToolbar v = getDecorToolbar();
        HwActionBarView acview = null;
        if (v instanceof HwActionBarView) {
            acview = (HwActionBarView) v;
        }
        if (acview != null) {
            acview.setCustomTitle(view);
        }
    }

    public void setStartContentDescription(CharSequence contentDescription) {
        DecorToolbar view = getDecorToolbar();
        HwActionBarView acview = null;
        if (view instanceof HwActionBarView) {
            acview = (HwActionBarView) view;
        }
        if (acview != null) {
            acview.setStartContentDescription(contentDescription);
        }
    }

    public void setEndContentDescription(CharSequence contentDescription) {
        DecorToolbar view = getDecorToolbar();
        HwActionBarView acview = null;
        if (view instanceof HwActionBarView) {
            acview = (HwActionBarView) view;
        }
        if (acview != null) {
            acview.setEndContentDescription(contentDescription);
        }
    }

    public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(d);
        getContainerView().setForcedPrimaryBackground(true);
    }

    public void setStackedBackgroundDrawable(Drawable d) {
        super.setStackedBackgroundDrawable(d);
        getContainerView().setForcedStackedBackground(true);
    }

    public void setSplitBackgroundDrawable(Drawable d) {
        super.setSplitBackgroundDrawable(d);
        getSplitView().setForcedSplitBackground(true);
    }

    protected void initCustomPanel(View decor) {
        this.mCustomPanelStub = (ViewStub) decor.findViewById(34603204);
    }

    protected void inflateCustomPanel() {
        if (this.mCustomPanelStub != null) {
            this.mCustomPanel = (HwCustomPanel) this.mCustomPanelStub.inflate();
            this.mCustomPanelInflated = true;
        }
        if (this.mCustomPanel != null) {
            int[] ATTRS = new int[CONTEXT_DISPLAY_SPLIT];
            ATTRS[CONTEXT_DISPLAY_NORMAL] = 16843499;
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(ATTRS);
            this.mCustomPanel.setClipY(-ta.getDimensionPixelSize(CONTEXT_DISPLAY_NORMAL, CONTEXT_DISPLAY_NORMAL));
            ta.recycle();
        }
    }

    public void setCustomDragView(View view) {
        if (!this.mCustomPanelInflated) {
            inflateCustomPanel();
        }
        if (!(this.mCustomDragView == null || this.mCustomPanel == null)) {
            this.mCustomPanel.removeAllViews();
        }
        this.mCustomDragView = view;
        if (this.mCustomDragView != null && this.mCustomPanel != null) {
            if (this.mCustomDragView.getParent() != null) {
                Log.w(TAG, "CustomDragView already has a parent,you must call removeView() on the child's parent first.");
                return;
            }
            this.mCustomPanel.addView(this.mCustomDragView, new LayoutParams(-1, -1));
            this.mCustomDragView.setAlpha(0.0f);
        }
    }

    public void setCustomDragView(View view, View secondView) {
        if (!this.mCustomPanelInflated) {
            inflateCustomPanel();
        }
        if (!(this.mCustomDragView == null || this.mCustomPanel == null)) {
            this.mCustomPanel.removeAllViews();
        }
        this.mCustomDragView = view;
        if (this.mCustomDragView == null || this.mCustomPanel == null) {
            Log.w(TAG, "The first CustomDragView is null!");
            return;
        }
        if (this.mCustomDragView.getParent() != null) {
            Log.w(TAG, "CustomDragView already has a parent,you must call removeView() on the child's parent first.");
        } else {
            this.mCustomPanel.addView(this.mCustomDragView, new LayoutParams(-1, -2));
            this.mCustomDragView.setAlpha(0.0f);
        }
        if (secondView == null) {
            return;
        }
        if (secondView.getParent() != null) {
            Log.w(TAG, "The secondView already has a parent,you must call removeView() on the child's parent first.");
            return;
        }
        this.mCustomPanel.addView(secondView, new LayoutParams(-1, -2));
        secondView.setAlpha(0.0f);
    }

    public void setStillView(View view, boolean isStill) {
        HwActionBarOverlayLayout overlayLayout = (HwActionBarOverlayLayout) getOverlayLayout();
        if (overlayLayout != null) {
            overlayLayout.setStillView(view, isStill);
        }
    }

    public HwCustomPanel getCustomPanel() {
        return this.mCustomPanel;
    }

    public void startStageAnimation(int stage, boolean isScrollDown) {
        HwActionBarOverlayLayout overlayLayout = (HwActionBarOverlayLayout) getOverlayLayout();
        if (overlayLayout != null) {
            overlayLayout.startStageAnimation(stage, isScrollDown);
        }
    }

    public int getDragAnimationStage() {
        HwActionBarOverlayLayout overlayLayout = (HwActionBarOverlayLayout) getOverlayLayout();
        if (overlayLayout != null) {
            return overlayLayout.getDragAnimationStage();
        }
        return CONTEXT_DISPLAY_NORMAL;
    }

    public void setCanDragFromContent(boolean canDragFromContent) {
        HwActionBarOverlayLayout overlayLayout = (HwActionBarOverlayLayout) getOverlayLayout();
        if (overlayLayout != null) {
            overlayLayout.setCanDragFromContent(canDragFromContent);
        }
    }

    public void setLazyMode(boolean isLazyMode) {
        HwActionBarOverlayLayout overlayLayout = (HwActionBarOverlayLayout) getOverlayLayout();
        if (overlayLayout != null) {
            overlayLayout.setLazyMode(isLazyMode);
        }
    }

    public void setActionBarDraggable(boolean isDraggable) {
        HwActionBarOverlayLayout overlayLayout = (HwActionBarOverlayLayout) getOverlayLayout();
        if (overlayLayout != null) {
            overlayLayout.setActionBarDraggable(isDraggable);
        }
    }

    protected void initActionBarOverlayLayout(View decor) {
        boolean isLand;
        boolean z = false;
        HwActionBarOverlayLayout hwOverlayLayout = (HwActionBarOverlayLayout) decor.findViewById(16909289);
        setOverlayLayout(hwOverlayLayout);
        if (getContext().getResources().getConfiguration().orientation == 2) {
            isLand = true;
        } else {
            isLand = false;
        }
        if (!isLand) {
            z = true;
        }
        hwOverlayLayout.setAnimationEnable(z);
    }

    public void setStageChangedCallBack(InnerOnStageChangedListener callback) {
        ((HwActionBarOverlayLayout) getOverlayLayout()).setCallback(callback);
    }

    public void setStartStageChangedCallBack(InnerOnStageChangedListener callback) {
        ((HwActionBarOverlayLayout) getOverlayLayout()).setStartStageCallback(callback);
    }

    public void resetDragAnimation() {
        ((HwActionBarOverlayLayout) getOverlayLayout()).resetDragAnimation();
    }

    public void setTabScrollingOffsets(int index, float offset) {
        ScrollingTabContainerView tabContainer = getTabScrollView();
        HwScrollingTabContainerView hwTabContainer = null;
        if (tabContainer instanceof HwScrollingTabContainerView) {
            hwTabContainer = (HwScrollingTabContainerView) tabContainer;
        }
        if (hwTabContainer != null) {
            hwTabContainer.setTabScrollingOffsets(index, offset);
        }
    }

    public void hide() {
        super.hide();
        if (this.mCustomPanel != null) {
            this.mCustomPanel.setVisibility(4);
        }
    }

    public void show() {
        super.show();
        if (this.mCustomPanel != null) {
            this.mCustomPanel.setVisibility(CONTEXT_DISPLAY_NORMAL);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DecorToolbar view = getDecorToolbar();
        if (view instanceof ActionBarView) {
            int contextDisplayMode;
            if (((HwActionBarView) view).isSplit()) {
                contextDisplayMode = CONTEXT_DISPLAY_SPLIT;
            } else {
                contextDisplayMode = CONTEXT_DISPLAY_NORMAL;
            }
            setContextDisplayMode(contextDisplayMode);
        }
        if (getContext() != null && HwWidgetFactory.getSuggestionForgroundColorStyle(getContext()) == CONTEXT_DISPLAY_SPLIT && getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public void setDisplayOptions(int options) {
        if ((StubController.PERMISSION_CALLLOG_WRITE & options) == 0) {
            super.setDisplayOptions(options);
        } else if (getContainerView() instanceof HwActionBarContainer) {
            ((HwActionBarContainer) getContainerView()).setDisplayNoSplitLine(true);
        }
    }

    public Drawable getBackgroundDrawable() {
        ActionBarContainer abc = getContainerView();
        if (abc instanceof HwActionBarContainer) {
            return ((HwActionBarContainer) abc).getBackgroundDrawable();
        }
        return null;
    }

    public Tab newTab() {
        return new HwTabImpl();
    }

    public void setSplitViewLocation(int start, int end) {
        if (start >= 0 && start < end) {
            getSplitView().setSplitViewLocation(start, end);
            getDecorToolbar().setSplitViewLocation(start, end);
        }
    }
}
