package com.huawei.hsm.permission;

import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;

public class CameraPermission {
    private static final String TAG = "CameraPermission";
    public boolean isCameraBlocked;
    private Context mContext;
    private int mPid;
    private int mUid;

    public CameraPermission() {
        this.mContext = null;
        this.isCameraBlocked = false;
        this.mContext = ActivityThread.currentApplication();
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
    }

    public CameraPermission(Context context) {
        this.mContext = null;
        this.isCameraBlocked = false;
        this.mContext = context;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
    }

    public void remind() {
        if (StubController.checkPrecondition(this.mUid)) {
            if ((this.mContext == null || StubController.isGlobalSwitchOn(this.mContext, 1024)) && 2 == StubController.holdForGetPermissionSelection(1024, this.mUid, this.mPid, null)) {
                this.isCameraBlocked = true;
            }
        }
    }

    public static boolean blockStartActivity(Context context, Intent intent) {
        String action = intent.getAction();
        if (!"android.media.action.IMAGE_CAPTURE".equals(action) && !"android.media.action.VIDEO_CAPTURE".equals(action)) {
            return false;
        }
        CameraPermission cameraPermission = new CameraPermission(context);
        cameraPermission.remind();
        return cameraPermission.isCameraBlocked;
    }
}
