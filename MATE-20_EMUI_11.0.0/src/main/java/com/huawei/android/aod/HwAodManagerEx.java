package com.huawei.android.aod;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import huawei.android.aod.AodConfigInfo;
import huawei.android.aod.AodVolumeInfo;
import huawei.android.aod.HwAodManager;

public class HwAodManagerEx {
    public static final String AOD_PERMISSION = "com.huawei.permission.aod.UPDATE_AOD";

    public static void start() {
        HwAodManager.getInstance().start();
    }

    public static void configAndStart(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        HwAodManager.getInstance().configAndStart(aodInfo, fileSize, pfd);
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

    public static void configAndUpdate(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) {
        HwAodManager.getInstance().configAndUpdate(aodInfo, fileSize, pfd);
    }

    public static void endUpdate() {
        HwAodManager.getInstance().endUpdate();
    }

    public static void setAodConfig(AodConfigInfo aodInfo) {
        HwAodManager.getInstance().setAodConfig(aodInfo);
    }

    public static void updateAodVolumeInfo(AodVolumeInfo aodVolumeInfo) {
        HwAodManager.getInstance().updateAodVolumeInfo(aodVolumeInfo);
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

    public static int getAodStatus() {
        return HwAodManager.getInstance().getAodStatus();
    }

    public static Bundle getAodInfo(int infoType) {
        return HwAodManager.getInstance().getAodInfo(infoType);
    }
}
