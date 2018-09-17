package android.app;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.FreezeScreenScene;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.hsm.permission.StubController;
import huawei.android.widget.HwChangeButtonWindow;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HwChangeButtonWindowCtrl implements IHwChangeButtonWindowCtrl {
    private static final String HW_SCREEN_ASPECT = SystemProperties.get("ro.config.hw_screen_aspect", "");
    private static Set<String> mBlankActivities = new HashSet();
    private static HashMap<Integer, HwChangeButtonWindowCtrl> mInstanceMap = new HashMap();
    private Activity mActivity;
    private HwChangeButtonWindow mHwChangeButtonWindow;

    static {
        mBlankActivities.add("com.vlocker.settings.DismissActivity");
        mBlankActivities.add("com.kingsoft.activitys.LockScreenActivity");
    }

    private HwChangeButtonWindowCtrl(Activity a) {
        this.mActivity = a;
        while (this.mActivity.getParent() != null) {
            this.mActivity = this.mActivity.getParent();
        }
    }

    public static IHwChangeButtonWindowCtrl getdefault(Activity a) {
        HwChangeButtonWindowCtrl instance = (HwChangeButtonWindowCtrl) mInstanceMap.get(Integer.valueOf(a.hashCode()));
        if (instance != null) {
            return instance;
        }
        instance = new HwChangeButtonWindowCtrl(a);
        mInstanceMap.put(Integer.valueOf(a.hashCode()), instance);
        return instance;
    }

    public boolean notShowWhenDialogActivity() {
        if (this.mActivity.getWindow().isFloating()) {
            return true;
        }
        return false;
    }

    public boolean isLongScreenPhone() {
        boolean z = true;
        if (this.mActivity == null) {
            return false;
        }
        Display display = ((WindowManager) this.mActivity.getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);
        if (realSize.x == 0) {
            return false;
        }
        float size1 = ((float) realSize.x) / ((float) realSize.y);
        if (((float) realSize.y) / ((float) realSize.x) < 2.0f && size1 < 2.0f) {
            z = false;
        }
        return z;
    }

    public boolean hasView() {
        if (this.mHwChangeButtonWindow == null) {
            return false;
        }
        return true;
    }

    public boolean hasViewAdd() {
        if (hasView()) {
            return this.mHwChangeButtonWindow.isAddView();
        }
        return false;
    }

    public void addView(WindowManager wm, Rect appBounds) {
        if (hasView() && !notShowWhenDialogActivity()) {
            this.mHwChangeButtonWindow.addView(wm, appBounds);
        }
    }

    public void removeView(WindowManager wm) {
        if (hasView()) {
            this.mHwChangeButtonWindow.removeView(wm);
        }
    }

    public void setViewHide(boolean hide) {
        if (hasView() && !notShowWhenDialogActivity()) {
            if (hide || !toHideButtonWindow()) {
                this.mHwChangeButtonWindow.setViewHide(hide);
            }
        }
    }

    public boolean isViewHide() {
        if (hasView()) {
            return this.mHwChangeButtonWindow.isHide();
        }
        return false;
    }

    public void ceateView() {
        if (!hasView() || (toHideButtonWindow() ^ 1) != 0 || notShowWhenDialogActivity()) {
            this.mHwChangeButtonWindow = new HwChangeButtonWindow(this.mActivity);
        }
    }

    public void showChangeButtonWindow(Rect appBounds) {
        if (hasView() && !toHideButtonWindow() && !notShowWhenDialogActivity()) {
            boolean isNavBarChanged = updateNavBarState();
            if (hasViewAdd()) {
                if (isNavBarChanged) {
                    this.mHwChangeButtonWindow.updateView(this.mActivity.getWindowManager(), appBounds);
                }
                if (isViewHide()) {
                    this.mHwChangeButtonWindow.setViewHide(false);
                }
            } else {
                this.mHwChangeButtonWindow.addView(this.mActivity.getWindowManager(), appBounds);
            }
        }
    }

    public void destoryView() {
        if (hasViewAdd()) {
            removeView(this.mActivity.getWindowManager());
        }
        this.mHwChangeButtonWindow = null;
        int hasCode = this.mActivity.hashCode();
        this.mActivity = null;
        mInstanceMap.remove(Integer.valueOf(hasCode));
    }

    public boolean isToOtherApp() {
        return this.mHwChangeButtonWindow.isToOtherApp();
    }

    /* JADX WARNING: Missing block: B:6:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateView(Rect appBounds) {
        if (hasView() && !toHideButtonWindow() && !notShowWhenDialogActivity() && hasViewAdd()) {
            this.mHwChangeButtonWindow.updateView(this.mActivity.getWindowManager(), appBounds);
        }
    }

    private boolean toHideButtonWindow() {
        if (this.mActivity == null || mBlankActivities.contains(this.mActivity.getClass().getName())) {
            return true;
        }
        return (StubController.PERMISSION_SMSLOG_DELETE & this.mActivity.getWindow().getAttributes().flags) != 0 && ((KeyguardManager) this.mActivity.getSystemService("keyguard")).isKeyguardLocked();
    }

    private boolean updateNavBarState() {
        if (HwChangeButtonWindow.IS_FRONT_NAV) {
            boolean currentNavBarState = System.getInt(this.mActivity.getContentResolver(), "enable_navbar", 0) != 0;
            if (!(this.mHwChangeButtonWindow == null || this.mHwChangeButtonWindow.getNavBarState() == currentNavBarState)) {
                this.mHwChangeButtonWindow.setNavBarState(currentNavBarState);
                return true;
            }
        }
        return false;
    }
}
