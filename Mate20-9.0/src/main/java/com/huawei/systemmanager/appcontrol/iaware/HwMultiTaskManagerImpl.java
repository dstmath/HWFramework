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
        IBinder b = ServiceManager.getService("multi_task");
        if (b != null) {
            return IMultiTaskManagerService.Stub.asInterface(b);
        }
        return null;
    }

    public void requestAppCleanWithCallback(AppCleanParamEx appCleanParam, IAppCleanCallbackEx appCleanCallback) throws RemoteException {
        if (this.mImpl != null) {
            this.mImpl.requestAppCleanWithCallback(appCleanParam.getInnerAppCleanParam(), appCleanCallback.getIAppCleanCallback());
        }
    }

    public List<HwAppStartupSettingEx> retrieveAppStartupSettings(List<String> list, HwAppStartupSettingFilterEx filter) throws RemoteException {
        if (this.mImpl == null) {
            return null;
        }
        List<HwAppStartupSetting> tempList = this.mImpl.retrieveAppStartupSettings(list, filter.getHwAppStartupSettingFilter());
        if (tempList == null) {
            return null;
        }
        List<HwAppStartupSettingEx> appStartupSettings = new ArrayList<>();
        for (HwAppStartupSetting setting : tempList) {
            appStartupSettings.add(new HwAppStartupSettingEx(setting));
        }
        return appStartupSettings;
    }

    public boolean removeAppStartupSetting(String str) throws RemoteException {
        if (this.mImpl != null) {
            return this.mImpl.removeAppStartupSetting(str);
        }
        return false;
    }

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

    public boolean updateCloudPolicy(String str) throws RemoteException {
        if (this.mImpl != null) {
            return this.mImpl.updateCloudPolicy(str);
        }
        return false;
    }
}
