package com.huawei.server.multiwindowtip;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public class HwMultiWindowTips {
    private static final String DOCK_BAR_ACTION = "huawei.intent.action.MULTIWINDOW_TIPS";
    private static final String DOCK_TIP_TYPE = "tiptype";
    private static final boolean ISDEBUG;
    private static final int MSG_NOTIFY_FLOATING_WINDOW = 3;
    private static final int MSG_NOTIFY_SPLIT_WINDOW_LEFT_RIGHT = 2;
    private static final int MSG_NOTIFY_SPLIT_WINDOW_UP_DOWN = 1;
    private static final int MSG_SET_FLOATING_WINDOW_STARTCOUNT = 4;
    private static final Object M_LOCK = new Object();
    private static final int NOTIFY_DOCK_BAR_DELAY = 3000;
    private static final int NOTIFY_FLOATWIN_TIP_THRESHOLD = SystemProperties.getInt("ro.config.float_win_threshold", 1);
    private static final int NOTIFY_FLOATWIN_TIP_THRESHOLD_DFT = 1;
    private static final int SHOWN_FLOATING_WIN_TIP = 256;
    private static final int SHOWN_SPLIT_WIN_TIP_LEFTRIGHT = 1;
    private static final int SHOWN_SPLIT_WIN_TIP_UP_DOWN = 16;
    private static final String TAG = "HwMultiWindowTips";
    private static final int TIP_TYPE_FLOATWIN = 1;
    private static final int TIP_TYPE_SPLITWIN_LEFT_RIGHT = 2;
    private static final int TIP_TYPE_SPLITWIN_UP_DOWN = 3;
    private static final int UPDATE_COUNT_DELAY = 50;
    private static volatile HwMultiWindowTips singleInstance = null;
    private Context mContext;
    private int mDockTipNotifyType;
    private Handler mHandler = new DockTipsHandler();
    private ContentObserver mMultiWinObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.server.multiwindowtip.HwMultiWindowTips.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isChange) {
            HwMultiWindowTips.this.updateMultiWinTipStat();
        }
    };
    private ContentResolver mResolver;
    private int mStartFloatingWinCount;

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        ISDEBUG = z;
    }

    private final class DockTipsHandler extends Handler {
        DockTipsHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwMultiWindowTips.this.handleNotifyDockBar(3);
            } else if (i == 2) {
                HwMultiWindowTips.this.handleNotifyDockBar(2);
            } else if (i == 3) {
                HwMultiWindowTips.this.handleNotifyDockBar(1);
            } else if (i == 4) {
                HwMultiWindowTips.this.updateFloatingWinCount();
            }
        }
    }

    private HwMultiWindowTips(Context context) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }

    public void onSystemReady() {
        registerMultiWinStat();
        registerUserSwtichObserver();
        updateMultiWinTipStat();
    }

    public static HwMultiWindowTips getInstance(Context context) {
        if (singleInstance == null) {
            synchronized (M_LOCK) {
                if (singleInstance == null) {
                    singleInstance = new HwMultiWindowTips(context);
                }
            }
        }
        return singleInstance;
    }

    public void processFloatWinDockTip(boolean isCfgChange, int stackId) {
        if (!isNeedNotifyDockBar(1)) {
            if (ISDEBUG) {
                Log.i(TAG, "do not need process float mDockTipNotifyType: " + this.mDockTipNotifyType);
            }
        } else if (this.mStartFloatingWinCount < NOTIFY_FLOATWIN_TIP_THRESHOLD) {
            if (ISDEBUG) {
                Log.i(TAG, "mCount: " + this.mStartFloatingWinCount + " isCfgChange " + isCfgChange + " id " + stackId);
            }
            if (!isCfgChange) {
                handleUpdateFloatingWinCount(stackId);
            }
        } else if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 3000);
        } else if (ISDEBUG) {
            Log.i(TAG, "message not processed");
        }
    }

    public void processSplitWinDockTip(boolean isLeftRight) {
        Message msg;
        int msgType = isLeftRight ? 2 : 1;
        if (!isNeedNotifyDockBar(isLeftRight ? 2 : 3)) {
            if (ISDEBUG) {
                Log.i(TAG, "do not need process isLeft: " + isLeftRight + " mDockTipNotifyType: " + this.mDockTipNotifyType);
            }
        } else if (!this.mHandler.hasMessages(msgType)) {
            if (isLeftRight) {
                msg = this.mHandler.obtainMessage(2);
            } else {
                msg = this.mHandler.obtainMessage(1);
            }
            this.mHandler.sendMessageDelayed(msg, 3000);
        } else if (ISDEBUG) {
            Log.i(TAG, "message not processed: " + msgType);
        }
    }

    public void saveMultiWindowTipState(String tipKey, int state) {
        long identity = Binder.clearCallingIdentity();
        try {
            Log.i(TAG, "saveMultiWindowTipState: " + tipKey + ": " + state);
            Settings.Secure.putIntForUser(this.mResolver, tipKey, state, ActivityManager.getCurrentUser());
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void registerUserSwtichObserver() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                /* class com.huawei.server.multiwindowtip.HwMultiWindowTips.AnonymousClass2 */

                public void onUserSwitching(int newUserId) {
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    Log.d(HwMultiWindowTips.TAG, "onUserSwitching newUserId = " + newUserId);
                    HwMultiWindowTips.this.updateMultiWinTipStat();
                }
            }, TAG);
        } catch (RemoteException e) {
            Log.e(TAG, "registerUserSwitchObserver fail");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFloatingWinCount() {
        this.mStartFloatingWinCount++;
        setFloatWinStartCount();
    }

    private void handleUpdateFloatingWinCount(int stackId) {
        if (this.mHandler.hasMessages(4, Integer.valueOf(stackId))) {
            Log.d(TAG, "repreat count stackId = " + stackId);
            return;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, Integer.valueOf(stackId)), 50);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyDockBar(int type) {
        notifyDockBar(type);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMultiWinTipStat() {
        this.mDockTipNotifyType = Settings.Secure.getIntForUser(this.mResolver, "dock_tip_notify_type", 0, ActivityManager.getCurrentUser());
        this.mStartFloatingWinCount = Settings.Secure.getIntForUser(this.mResolver, "start_floatwin_boots_count", 0, ActivityManager.getCurrentUser());
        if (ISDEBUG) {
            Log.i(TAG, "update dockTipNotifyType:" + this.mDockTipNotifyType + " count: " + this.mStartFloatingWinCount);
        }
    }

    private void registerMultiWinStat() {
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("dock_tip_notify_type"), false, this.mMultiWinObserver, -1);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("start_floatwin_boots_count"), false, this.mMultiWinObserver, -1);
    }

    private void setFloatWinStartCount() {
        if (ISDEBUG) {
            Log.i(TAG, "mStartFloatingWinCount: " + this.mStartFloatingWinCount);
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Settings.Secure.putIntForUser(this.mResolver, "start_floatwin_boots_count", this.mStartFloatingWinCount, ActivityManager.getCurrentUser());
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void setDockTipValue(int type) {
        int value;
        if (type == 2) {
            value = 1 | this.mDockTipNotifyType;
        } else if (type == 3) {
            value = this.mDockTipNotifyType | 16;
        } else if (type == 1) {
            value = this.mDockTipNotifyType | 256;
        } else {
            return;
        }
        if (ISDEBUG) {
            Log.i(TAG, "type: " + type + " value: " + value + " mDockTipNotifyType: " + this.mDockTipNotifyType);
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Settings.Secure.putIntForUser(this.mResolver, "dock_tip_notify_type", value, ActivityManager.getCurrentUser());
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean isNeedNotifyDockBar(int type) {
        if (type == 2 && (this.mDockTipNotifyType & 1) == 0) {
            return true;
        }
        if (type == 3 && (this.mDockTipNotifyType & 16) == 0) {
            return true;
        }
        if (type == 1 && (this.mDockTipNotifyType & 256) == 0) {
            return true;
        }
        return false;
    }

    private void notifyDockBar(int type) {
        if (type == 1) {
        }
        Intent intent = new Intent(DOCK_BAR_ACTION);
        Log.i(TAG, "notify type: " + type);
        intent.putExtra(DOCK_TIP_TYPE, type);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getFreeformGuideCount() {
        return this.mStartFloatingWinCount;
    }
}
