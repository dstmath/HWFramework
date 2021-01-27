package com.huawei.server.magicwin;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import com.android.server.wm.HwMagicContainer;
import com.android.server.wm.HwMagicWinManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;

public class GestureHomeAnimatorObserver extends ContentObserver {
    private static final int GESTURE_ANIMATION_END_HOME = 12;
    private static final int GESTURE_ANIMATION_END_LASTTASK = 15;
    private static final int GESTURE_ANIMATION_END_NEWTASK = 14;
    private static final int GESTURE_ANIMATION_END_NULL = 16;
    private static final int GESTURE_ANIMATION_END_RECENTS = 13;
    private static final int GESTURE_ANIMATION_START = 11;
    public static final String GESTURE_HOME_ANIMATOR = "gesture_home_animator";
    private static final int GESTURE_STARTNEWTASK_FAIL = 19;
    private static final int GESTURE_STARTNEWTASK_START = 17;
    private static final int GESTURE_STARTNEWTASK_SUCCESS = 18;
    public static final int HOME_ANIMATION_END = 0;
    public static final int HOME_ANIMATION_START = 1;
    public static final int RECENT_ANIMATION_CANCEL = -1;
    private static final String TAG = "HWMW_GestureHomeAnimatorObserver";
    private HwMagicContainer mContainer = null;
    private Context mContext = null;
    private HwMagicWindowUI mHwMagicWindowUI = null;
    private HwMagicWinManager mMwManager = null;

    private GestureHomeAnimatorObserver(Handler handler) {
        super(handler);
    }

    public GestureHomeAnimatorObserver(Handler handler, HwMagicWindowUI hwMagicWindowUI, Context context, HwMagicWinManager manager) {
        super(handler);
        this.mHwMagicWindowUI = hwMagicWindowUI;
        this.mContext = context;
        this.mMwManager = manager;
        this.mContainer = hwMagicWindowUI.getContainer();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        boolean isTopAppInMwMode = this.mMwManager.getAmsPolicy().isStackInHwMagicWindowMode(this.mContainer);
        int animStatus = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), GESTURE_HOME_ANIMATOR, 0, ActivityManagerEx.getCurrentUser());
        if (!isTopAppInMwMode) {
            SlogEx.i(TAG, "GESTURE_HOME_ANIMATOR onChange return, isTopAppInMwMode = " + isTopAppInMwMode + " animStatus = " + animStatus);
            return;
        }
        handleGestureHomeAnimatorChange(animStatus);
    }

    private void handleGestureHomeAnimatorChange(int animStatus) {
        SlogEx.i(TAG, "handleGestureHomeAnimatorChanged animStatus = " + animStatus);
        if (animStatus != 1) {
            switch (animStatus) {
                case 11:
                case 12:
                case 13:
                    break;
                case 14:
                case 16:
                default:
                    return;
                case 15:
                case 17:
                    if (!this.mMwManager.getAmsPolicy().isFullscreenWindow(this.mContainer)) {
                        this.mHwMagicWindowUI.updateMagicWallpaperByGesture(true, false, true, false);
                        return;
                    }
                    return;
            }
        }
        this.mHwMagicWindowUI.updateMagicWallpaperByGesture(false, true, true, false);
    }
}
