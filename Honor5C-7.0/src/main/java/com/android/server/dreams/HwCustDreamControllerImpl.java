package com.android.server.dreams;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerGlobal;
import com.android.server.dreams.DreamController.DreamRecord;

public class HwCustDreamControllerImpl extends HwCustDreamController {
    private static final int DREAM_CONNECTION_TIMEOUT = 5000;
    private static final int DREAM_FINISH_TIMEOUT = 5000;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustDreamCtlImpl";
    private static final String TAG_FLOW = "HwCustDreamCtlImpl_FLOW";
    private Context mContext;
    private DreamController mDreamController;
    private final Handler mHandler;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : HWFLOW : HWLOGW_E;
        HWFLOW = isLoggable;
    }

    public HwCustDreamControllerImpl(DreamController dreamController, Context context, Handler handler) {
        super(dreamController, context, handler);
        this.mDreamController = null;
        this.mContext = null;
        this.mDreamController = dreamController;
        this.mContext = context;
        this.mHandler = handler;
    }

    public void startChargingAlbumDream(Binder token, ComponentName name, boolean isTest, int userId) {
        if (HwCustDreamManagerServiceImpl.mChargingAlbumSupported) {
            this.mDreamController.stopDream(HWLOGW_E);
            Intent shadeIntent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            shadeIntent.putExtra("reason", "dream");
            this.mContext.sendBroadcastAsUser(shadeIntent, UserHandle.ALL);
            if (HWFLOW) {
                Slog.i(TAG_FLOW, "Starting charging album dream: name=" + name + ", isTest=" + isTest + ", userId=" + userId);
            }
            DreamController dreamController = this.mDreamController;
            DreamController dreamController2 = this.mDreamController;
            dreamController2.getClass();
            dreamController.mCurrentDream = new DreamRecord(dreamController2, token, name, isTest, HWFLOW, userId);
            try {
                WindowManagerGlobal.getWindowManagerService().addWindowToken(token, 2102);
                Intent intent = new Intent("android.service.dreams.DreamService");
                intent.setComponent(name);
                intent.addFlags(8388608);
                try {
                    if (this.mContext.bindServiceAsUser(intent, this.mDreamController.mCurrentDream, 1, new UserHandle(userId))) {
                        this.mDreamController.mCurrentDream.mBound = HWLOGW_E;
                        this.mHandler.postDelayed(this.mDreamController.mStopUnconnectedDreamRunnable, 5000);
                        return;
                    }
                    Slog.e(TAG, "Unable to bind dream service: " + intent);
                    this.mDreamController.stopDream(HWLOGW_E);
                } catch (SecurityException ex) {
                    Slog.e(TAG, "Unable to bind dream service: " + intent, ex);
                    this.mDreamController.stopDream(HWLOGW_E);
                }
            } catch (RemoteException ex2) {
                Slog.e(TAG, "Unable to add window token for dream.", ex2);
                this.mDreamController.stopDream(HWLOGW_E);
            }
        }
    }
}
