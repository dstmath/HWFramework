package android.hwsystemmanager;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.permission.IHsmMaliAppInfoListener;
import com.huawei.permission.MaliInfoBean;
import com.huawei.securitycenter.MaliciousHelper;
import com.huawei.securitycenter.SecCenterServiceHolder;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSystemManagerPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HsmSecurityProxy {
    private static final int HWSYSTEMMANAGER_PLUGIN_ID = 13;
    private static final int LISTENER_PRIORITY_LOW = 2;
    public static final int MALICIOUS_FLAGS_ALL = -1;
    private static final int MALICIOUS_FLAGS_NONE = 0;
    public static final int MALICIOUS_FLAGS_RESTRICTED = 1;
    private static final int NOT_RESTRICTED = 1;
    private static final int RESTRICTED = 0;
    private static final int RET_FAIL = 1;
    private static final int RET_SUCCESS = 0;
    private static final int RISK_NONE = 0;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HsmSecurityProxy";
    private static volatile String[] sForbiddenPerms;
    private static volatile SecCenterServiceHolder.ServiceDieListener sHoldServiceDieListener;
    private static volatile HsmSecurityProxy sInstance;
    private static final Map<MaliAppInfoListener, IHsmMaliAppInfoListener> sListenerMap = new HashMap();
    private IHwSecurityService mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));

    public interface MaliAppInfoListener {
        void onMaliAppInfoChanged(MaliciousAppInfo maliciousAppInfo);
    }

    public static class MaliciousAppInfo {
        public String[] forbiddenPermissions;
        public boolean isMalicious;
        public boolean isRestricted;
        public String packageName;
    }

    private HsmSecurityProxy() {
        if (this.mSecurityService == null) {
            Log.e(TAG, "error, securityservice was null");
        }
    }

    public static HsmSecurityProxy getInstance() {
        if (sInstance == null) {
            synchronized (HsmSecurityProxy.class) {
                if (sInstance == null) {
                    sInstance = new HsmSecurityProxy();
                }
            }
        }
        return sInstance;
    }

    private IHwSystemManagerPlugin getHwSystemManagerPlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    IHwSystemManagerPlugin hwSystemManagerService = IHwSystemManagerPlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(13));
                    if (hwSystemManagerService == null) {
                        Log.e(TAG, "error, getHwSystemManagerPlugin is null");
                    }
                    return hwSystemManagerService;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwSystemManagerPlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    public int updateAddViewData(Bundle data, int operation) {
        if (data == null) {
            Log.e(TAG, " update data error, dirty data");
            return 1;
        }
        IHwSystemManagerPlugin plugin = getHwSystemManagerPlugin();
        if (plugin == null) {
            return 1;
        }
        try {
            return plugin.updateAddViewData(data, operation);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when updateAddViewData is invoked");
            return 1;
        }
    }

    public static MaliciousAppInfo getMaliciousAppInfo(String packageName, int flags) {
        if (packageName == null || flags == 0) {
            return null;
        }
        checkServiceDie();
        return mapToMaliciousAppInfo(MaliciousHelper.queryMaliAppInfoByPkg(packageName, flags));
    }

    public static List<MaliciousAppInfo> getMaliciousAppInfos(int flags) {
        if (flags == 0) {
            return new ArrayList();
        }
        checkServiceDie();
        return mapToMaliciousAppInfoShort(MaliciousHelper.queryMaliAppInfoShort(flags));
    }

    public static void registMaliAppInfoListener(MaliAppInfoListener listener, int flags) {
        if (listener != null && flags != 0) {
            checkServiceDie();
            MaliciousHelper.registMaliAppInfoListener(addMaliAppInfoListener(listener), flags, 2);
        }
    }

    public static void unregistMaliAppInfoListener(MaliAppInfoListener listener) {
        if (listener != null) {
            checkServiceDie();
            IHsmMaliAppInfoListener maliciousListener = removeMaliAppInfoListener(listener);
            if (maliciousListener != null) {
                MaliciousHelper.unregistMaliAppInfoListener(maliciousListener);
            }
        }
    }

    public static void setRestrictStatus(String packageName, boolean isRestricted) {
        if (packageName != null) {
            checkServiceDie();
            MaliciousHelper.setRestrictStatus(packageName, isRestricted);
        }
    }

    public void setStartComponetBlackList(List<String> pkgs) {
        IHwSystemManagerPlugin plugin = getHwSystemManagerPlugin();
        if (plugin != null) {
            try {
                plugin.setStartComponetBlackList(pkgs);
            } catch (RemoteException e) {
                Log.e(TAG, "setStartComponetBlackList : ", e);
            }
        }
    }

    private static IHsmMaliAppInfoListener addMaliAppInfoListener(final MaliAppInfoListener listener) {
        IHsmMaliAppInfoListener iHsmMaliAppInfoListener;
        synchronized (sListenerMap) {
            if (!sListenerMap.containsKey(listener)) {
                sListenerMap.put(listener, new IHsmMaliAppInfoListener.Stub() {
                    /* class android.hwsystemmanager.HsmSecurityProxy.AnonymousClass1 */

                    public void onMaliAppInfoChanged(String pkgName) {
                        listener.onMaliAppInfoChanged(HsmSecurityProxy.mapToMaliciousAppInfo(MaliciousHelper.queryMaliAppInfoByPkg(pkgName, -1)));
                    }

                    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: android.hwsystemmanager.HsmSecurityProxy$1 */
                    /* JADX WARN: Multi-variable type inference failed */
                    public IBinder asBinder() {
                        return this;
                    }
                });
            }
            iHsmMaliAppInfoListener = sListenerMap.get(listener);
        }
        return iHsmMaliAppInfoListener;
    }

    private static IHsmMaliAppInfoListener removeMaliAppInfoListener(MaliAppInfoListener listener) {
        IHsmMaliAppInfoListener remove;
        synchronized (sListenerMap) {
            remove = sListenerMap.remove(listener);
        }
        return remove;
    }

    /* access modifiers changed from: private */
    public static MaliciousAppInfo mapToMaliciousAppInfo(List<MaliInfoBean> beans) {
        if (beans == null || beans.isEmpty()) {
            return null;
        }
        MaliciousAppInfo info = new MaliciousAppInfo();
        boolean z = false;
        MaliInfoBean bean = beans.get(0);
        info.packageName = bean.appId;
        info.isMalicious = bean.riskLevel != 0;
        if (bean.restrictStatus == 0) {
            z = true;
        }
        info.isRestricted = z;
        if (sForbiddenPerms == null) {
            sForbiddenPerms = MaliciousHelper.getAlwaysForbiddenPerms();
        }
        info.forbiddenPermissions = (String[]) sForbiddenPerms.clone();
        return info;
    }

    private static List<MaliciousAppInfo> mapToMaliciousAppInfoShort(List<MaliInfoBean> beans) {
        List<MaliciousAppInfo> infos = new ArrayList<>();
        if (beans == null || beans.isEmpty()) {
            return infos;
        }
        int size = beans.size();
        for (int i = 0; i < size; i++) {
            MaliInfoBean bean = beans.get(i);
            if (bean != null) {
                MaliciousAppInfo info = new MaliciousAppInfo();
                info.packageName = bean.appId;
                boolean z = false;
                info.isMalicious = bean.riskLevel != 0;
                if (bean.restrictStatus == 0) {
                    z = true;
                }
                info.isRestricted = z;
                if (sForbiddenPerms == null) {
                    sForbiddenPerms = MaliciousHelper.getAlwaysForbiddenPerms();
                }
                info.forbiddenPermissions = (String[]) sForbiddenPerms.clone();
                infos.add(info);
            }
        }
        return infos;
    }

    private static void checkServiceDie() {
        synchronized (sListenerMap) {
            if (sHoldServiceDieListener == null) {
                sHoldServiceDieListener = new SecCenterServiceHolder.ServiceDieListener() {
                    /* class android.hwsystemmanager.HsmSecurityProxy.AnonymousClass2 */

                    @Override // com.huawei.securitycenter.SecCenterServiceHolder.ServiceDieListener
                    public void notifyServiceDie() {
                        synchronized (HsmSecurityProxy.sListenerMap) {
                            SecCenterServiceHolder.ServiceDieListener unused = HsmSecurityProxy.sHoldServiceDieListener = null;
                            HsmSecurityProxy.sListenerMap.clear();
                        }
                    }
                };
                SecCenterServiceHolder.ServiceDieListener dieListener = sHoldServiceDieListener;
                if (dieListener != null) {
                    SecCenterServiceHolder.addServiceDieListener(dieListener, SecCenterServiceHolder.SERVICE_TYPE_HW_SEC);
                }
            }
        }
    }
}
