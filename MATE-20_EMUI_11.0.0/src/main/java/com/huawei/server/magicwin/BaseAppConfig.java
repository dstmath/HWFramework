package com.huawei.server.magicwin;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class BaseAppConfig {
    private static final String TAG = "BaseAppConfig";
    boolean mIsDefaultSetting;
    boolean mIsDragToFullscreen;
    boolean mIsDragable;
    boolean mIsNotchAdapted;
    boolean mIsScaleEnabled;
    List<String> mMainActivities = new ArrayList();
    int mMode;
    boolean mNeedRelaunch;
    String mPackageName;
    boolean mSupportCameraPreview;
    boolean mSupportLeftResume;
    boolean mSupportVideoFScreen;

    public int getWindowMode() {
        return this.mMode;
    }

    public boolean isLeftResume() {
        return this.mSupportLeftResume;
    }

    public boolean isVideoFullscreen() {
        return this.mSupportVideoFScreen;
    }

    public boolean isCameraPreview() {
        return this.mSupportCameraPreview;
    }

    public boolean isNotchModeEnabled() {
        return this.mIsNotchAdapted;
    }

    public boolean isScaleEnabled() {
        return this.mIsScaleEnabled;
    }

    public boolean isDefaultSetting() {
        return this.mIsDefaultSetting;
    }

    public boolean isDragable() {
        return this.mIsDragable;
    }

    public boolean isDragToFullscreen() {
        return this.mIsDragToFullscreen;
    }

    public boolean needRelaunch() {
        return this.mNeedRelaunch;
    }

    public void split(String strSeq) {
        if (!TextUtils.isEmpty(strSeq)) {
            String[] strArray = strSeq.split(",");
            for (int i = 0; i < strArray.length; i++) {
                if (!"".equals(strArray[i])) {
                    this.mMainActivities.add(strArray[i]);
                }
            }
        }
    }
}
