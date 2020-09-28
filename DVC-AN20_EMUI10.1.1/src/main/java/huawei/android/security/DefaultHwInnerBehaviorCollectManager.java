package huawei.android.security;

import android.content.pm.ApplicationInfo;
import com.huawei.annotation.HwSystemApi;
import huawei.android.security.IHwBehaviorCollectManager;

@HwSystemApi
public class DefaultHwInnerBehaviorCollectManager implements IHwBehaviorCollectManager {
    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
    }

    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void sendBehavior(int bid) {
    }

    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
    }

    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void sendBehavior(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid) {
    }

    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void sendBehavior(int uid, int pid, IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
    }

    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void sendEvent(int event, int uid, int pid, String packageName, String installer) {
    }

    @Override // huawei.android.security.IHwBehaviorCollectManager
    public void regUntrustedAppToMonitorService(ApplicationInfo appInfo) {
    }
}
