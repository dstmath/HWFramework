package huawei.com.android.internal.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.View;
import com.android.internal.app.WindowDecorActionBar;
import huawei.com.android.internal.app.DefaultActionModeImpl;
import huawei.com.android.internal.widget.ActionModeView;
import huawei.com.android.internal.widget.HwCustomPanel;

public class HwActionBarImpl extends WindowDecorActionBar implements DefaultActionModeImpl.ActionModeCallback {

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
    }

    /* access modifiers changed from: protected */
    public void initContainerView(View decor) {
    }

    /* access modifiers changed from: protected */
    public void initContextView(View decor) {
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return null;
    }

    public void setScrollTabAnimEnable(boolean shouldAnim) {
    }

    public DefaultActionModeImpl createActionMode(ActionMode.Callback callback) {
        return new EditActionModeImpl(getThemedContext(), callback);
    }

    public ActionModeView createActionModeView(ActionMode.Callback callback) {
        return getContextView();
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl.ActionModeCallback
    public void onActionModeFinish(ActionMode actionMode) {
    }

    public void setStartIcon(boolean icon1Visible, Drawable icon1, View.OnClickListener listener1) {
    }

    public void setEndIcon(boolean icon2Visible, Drawable icon2, View.OnClickListener listener2) {
    }

    public void setCustomTitle(View view) {
    }

    public void setStartContentDescription(CharSequence contentDescription) {
    }

    public void setEndContentDescription(CharSequence contentDescription) {
    }

    public void setBackgroundDrawable(Drawable d) {
    }

    public void setStackedBackgroundDrawable(Drawable d) {
    }

    public void setSplitBackgroundDrawable(Drawable d) {
    }

    /* access modifiers changed from: protected */
    public void initCustomPanel(View decor) {
    }

    /* access modifiers changed from: protected */
    public void inflateCustomPanel() {
    }

    public void setCustomDragView(View view) {
    }

    public void setCustomDragView(View view, View secondView) {
    }

    public void setStillView(View view, boolean isStill) {
    }

    public HwCustomPanel getCustomPanel() {
        return null;
    }

    public void startStageAnimation(int stage, boolean isScrollDown) {
    }

    public int getDragAnimationStage() {
        return 0;
    }

    public void setCanDragFromContent(boolean canDragFromContent) {
    }

    public void setLazyMode(boolean isLazyMode) {
    }

    public void setActionBarDraggable(boolean isDraggable) {
    }

    /* access modifiers changed from: protected */
    public void initActionBarOverlayLayout(View decor) {
    }

    public void setStageChangedCallBack(InnerOnStageChangedListener callback) {
    }

    public void setStartStageChangedCallBack(InnerOnStageChangedListener callback) {
    }

    public void resetDragAnimation() {
    }

    public void setTabScrollingOffsets(int index, float offset) {
    }

    public void hide() {
        HwActionBarImpl.super.hide();
    }

    public void show() {
        HwActionBarImpl.super.show();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        HwActionBarImpl.super.onConfigurationChanged(newConfig);
    }

    public void setDisplayOptions(int options) {
        HwActionBarImpl.super.setDisplayOptions(options);
    }

    public Drawable getBackgroundDrawable() {
        return null;
    }

    public class HwTabImpl extends WindowDecorActionBar.TabImpl {
        private int mTabViewId = -1;

        public HwTabImpl() {
            super(HwActionBarImpl.this);
        }

        public void setTabViewId(int id) {
            this.mTabViewId = id;
        }

        public int getTabViewId() {
            return this.mTabViewId;
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.app.ActionBar$Tab, huawei.com.android.internal.app.HwActionBarImpl$HwTabImpl] */
    public ActionBar.Tab newTab() {
        return new HwTabImpl();
    }

    public void setSplitViewLocation(int start, int end) {
    }

    public void setSmartColor(ColorStateList iconColor, ColorStateList titleColor) {
    }
}
