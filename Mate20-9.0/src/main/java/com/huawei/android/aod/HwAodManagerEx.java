package com.huawei.android.aod;

import android.os.ParcelFileDescriptor;
import huawei.android.aod.AodConfigInfo;
import huawei.android.aod.HwAodManager;
import huawei.android.aod.IAodCallback;

public class HwAodManagerEx {
    public static final String AOD_PERMISSION = "com.huawei.permission.aod.UPDATE_AOD";

    public static void registerAodCallback(IAodCallback listener) {
        HwAodManager.getInstance().registerAodCallback(listener);
    }

    public static void unRegisterAodCallback(IAodCallback listener) {
        HwAodManager.getInstance().unRegisterAodCallback(listener);
    }

    public static void start() {
        HwAodManager.getInstance().start();
    }

    public static void stop() {
        HwAodManager.getInstance().stop();
    }

    public static void pause() {
        HwAodManager.getInstance().pause();
    }

    public static void resume() {
        HwAodManager.getInstance().resume();
    }

    public static void beginUpdate() {
        HwAodManager.getInstance().beginUpdate();
    }

    public static void endUpdate() {
        HwAodManager.getInstance().endUpdate();
    }

    public static void setAodConfig(AodConfigInfo aodInfo) {
        HwAodManager.getInstance().setAodConfig(aodInfo);
    }

    public static void setBitmapByMemoryFile(int fileSize, ParcelFileDescriptor pfd) {
        HwAodManager.getInstance().setBitmapByMemoryFile(fileSize, pfd);
    }

    public static int getDeviceNodeFD() {
        return HwAodManager.getInstance().getDeviceNodeFD();
    }

    public static void setPowerState(int state) {
        HwAodManager.getInstance().setPowerState(state);
    }

    public static void restoreBacklightBrightnessByLcdFile(int brightness, int normalizedMaxBrightness) {
        HwAodManager.getInstance().restoreBacklightBrightnessByLcdFile(brightness, normalizedMaxBrightness);
    }
}
