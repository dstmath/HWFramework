package com.huawei.systemmanager.appcontrol.iaware;

import android.os.RemoteException;
import com.huawei.systemmanager.appcontrol.iaware.appmng.AppCleanParamEx;
import com.huawei.systemmanager.appcontrol.iaware.appmng.IAppCleanCallbackEx;
import java.util.List;

public interface IMultiTaskManager {
    boolean removeAppStartupSetting(String str) throws RemoteException;

    void requestAppCleanWithCallback(AppCleanParamEx appCleanParamEx, IAppCleanCallbackEx iAppCleanCallbackEx) throws RemoteException;

    List<HwAppStartupSettingEx> retrieveAppStartupSettings(List<String> list, HwAppStartupSettingFilterEx hwAppStartupSettingFilterEx) throws RemoteException;

    boolean updateAppStartupSettings(List<HwAppStartupSettingEx> list, boolean z) throws RemoteException;

    boolean updateCloudPolicy(String str) throws RemoteException;
}
