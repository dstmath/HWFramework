package com.android.server.am;

import android.app.ActivityManager.RunningServiceInfo;
import com.android.server.am.ActiveServices.ServiceMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HwActiveServices extends ActiveServices {
    public static final boolean DEBUG = false;
    static final String EXCLUDE_PROCESS = "com.huawei.android.pushagent.PushService";
    static final String TAG = "HwActiveServices";

    public HwActiveServices(ActivityManagerService service) {
        super(service);
    }

    protected List<RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags) {
        List<RunningServiceInfo> res = super.getRunningServiceInfoLocked(maxNum, flags);
        for (int i = res.size() - 1; i >= 0; i--) {
            if (EXCLUDE_PROCESS.equals(((RunningServiceInfo) res.get(i)).process)) {
                res.remove(i);
                break;
            }
        }
        return res;
    }

    protected boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean killProcess, boolean doit) {
        if (2147383647 != userId && userId != 0) {
            return super.bringDownDisabledPackageServicesLocked(packageName, filterByClasses, userId, evenPersistent, killProcess, doit);
        }
        List<ServiceMap> list = new ArrayList();
        list.add((ServiceMap) this.mServiceMap.get(2147383647));
        if (userId == 0) {
            list.add((ServiceMap) this.mServiceMap.get(0));
        }
        boolean didSomething = DEBUG;
        if (this.mTmpCollectionResults != null) {
            this.mTmpCollectionResults.clear();
        }
        for (ServiceMap smap : list) {
            if (smap != null) {
                didSomething = collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, smap.mServicesByName);
            }
        }
        if (this.mTmpCollectionResults != null) {
            for (int i = this.mTmpCollectionResults.size() - 1; i >= 0; i--) {
                bringDownServiceLocked((ServiceRecord) this.mTmpCollectionResults.get(i));
            }
            this.mTmpCollectionResults.clear();
        }
        return didSomething;
    }
}
