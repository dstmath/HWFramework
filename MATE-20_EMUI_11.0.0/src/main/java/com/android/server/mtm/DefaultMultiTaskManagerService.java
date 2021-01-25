package com.android.server.mtm;

import android.app.mtm.IMultiTaskManagerService;
import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.SceneData;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.rms.iaware.RPolicyData;
import com.huawei.annotation.HwSystemApi;
import java.util.List;

@HwSystemApi
public class DefaultMultiTaskManagerService extends IMultiTaskManagerService.Stub {
    public DefaultMultiTaskManagerService() {
    }

    public DefaultMultiTaskManagerService(Context context) {
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
    }

    public boolean updateCloudPolicy(String filePath) throws RemoteException {
        return false;
    }

    public boolean removeAppStartupSetting(String pkgName) throws RemoteException {
        return false;
    }

    public boolean updateAppStartupSettings(List<HwAppStartupSetting> list, boolean clearFirst) throws RemoteException {
        return false;
    }

    public List<String> retrieveAppStartupPackages(List<String> list, int[] policy, int[] modifier, int[] show) throws RemoteException {
        return null;
    }

    public List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> list, HwAppStartupSettingFilter filter) throws RemoteException {
        return null;
    }

    public boolean reportScene(int featureId, SceneData scene) {
        return false;
    }

    public RPolicyData acquirePolicyData(int featureId, SceneData scene) {
        return null;
    }

    public boolean forcestopApps(int pid) {
        return false;
    }

    public void notifyProcessDiedChange(int pid, int uid) {
    }

    public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) {
    }

    public void notifyProcessGroupChange(int pid, int uid) {
    }

    public boolean killProcess(int pid, boolean restartservice) {
        return false;
    }

    public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
    }
}
