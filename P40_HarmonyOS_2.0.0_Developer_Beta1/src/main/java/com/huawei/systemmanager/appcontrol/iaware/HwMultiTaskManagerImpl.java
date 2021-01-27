package com.huawei.systemmanager.appcontrol.iaware;

import android.app.mtm.IMultiTaskManagerService;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.appcontrol.iaware.appmng.AppCleanParamEx;
import com.huawei.systemmanager.appcontrol.iaware.appmng.IAppCleanCallbackEx;
import java.util.ArrayList;
import java.util.List;

public class HwMultiTaskManagerImpl implements IMultiTaskManager {
    private static final String TAG = "HwMultiTaskManagerServiceEx";
    private static volatile IMultiTaskManager mInstance = null;
    private IMultiTaskManagerService mImpl;

    public static synchronized IMultiTaskManager getInstance() {
        synchronized (HwMultiTaskManagerImpl.class) {
            HwMultiTaskManagerImpl tmp = new HwMultiTaskManagerImpl();
            if (tmp.mImpl == null) {
                return null;
            }
            return tmp;
        }
    }

    private HwMultiTaskManagerImpl() {
        this.mImpl = null;
        this.mImpl = getService();
    }

    private IMultiTaskManagerService getService() {
        IBinder binder = ServiceManager.getService("multi_task");
        if (binder != null) {
            return IMultiTaskManagerService.Stub.asInterface(binder);
        }
        return null;
    }

    @Override // com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager
    public void requestAppCleanWithCallback(AppCleanParamEx appCleanParam, IAppCleanCallbackEx appCleanCallback) throws RemoteException {
        IMultiTaskManagerService iMultiTaskManagerService = this.mImpl;
        if (iMultiTaskManagerService != null) {
            iMultiTaskManagerService.requestAppCleanWithCallback(appCleanParam.getInnerAppCleanParam(), appCleanCallback.getIAppCleanCallback());
        }
    }

    @Override // com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager
    public List<HwAppStartupSettingEx> retrieveAppStartupSettings(List<String> list, HwAppStartupSettingFilterEx filter) throws RemoteException {
        List<HwAppStartupSetting> tempList;
        IMultiTaskManagerService iMultiTaskManagerService = this.mImpl;
        if (iMultiTaskManagerService == null || (tempList = iMultiTaskManagerService.retrieveAppStartupSettings(list, filter.getHwAppStartupSettingFilter())) == null) {
            return null;
        }
        List<HwAppStartupSettingEx> appStartupSettings = new ArrayList<>();
        for (HwAppStartupSetting setting : tempList) {
            appStartupSettings.add(new HwAppStartupSettingEx(setting));
        }
        return appStartupSettings;
    }

    @Override // com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager
    public boolean removeAppStartupSetting(String str) throws RemoteException {
        IMultiTaskManagerService iMultiTaskManagerService = this.mImpl;
        if (iMultiTaskManagerService != null) {
            return iMultiTaskManagerService.removeAppStartupSetting(str);
        }
        return false;
    }

    @Override // com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager
    public boolean updateAppStartupSettings(List<HwAppStartupSettingEx> list, boolean isTrue) throws RemoteException {
        if (this.mImpl == null) {
            return false;
        }
        List<HwAppStartupSetting> appStartupSettings = new ArrayList<>();
        for (HwAppStartupSettingEx setting : list) {
            if (setting != null) {
                appStartupSettings.add(setting.getHwAppStartupSetting());
            }
        }
        return this.mImpl.updateAppStartupSettings(appStartupSettings, isTrue);
    }

    @Override // com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager
    public boolean updateCloudPolicy(String str) throws RemoteException {
        IMultiTaskManagerService iMultiTaskManagerService = this.mImpl;
        if (iMultiTaskManagerService != null) {
            return iMultiTaskManagerService.updateCloudPolicy(str);
        }
        return false;
    }
}
