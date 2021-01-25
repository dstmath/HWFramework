package ohos.abilityshell;

import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.HiViewUtil;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ApplicationInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.BundleManager;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class BundleMgrBridge {
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static IBundleManager bundleMgrProxy = null;
    private static SysAbilityFactory sysAbilityFactory = new DefaultSysAbilityFactory();

    public static void setSysAbilityFactory(SysAbilityFactory sysAbilityFactory2) {
        sysAbilityFactory = sysAbilityFactory2;
    }

    public static void setBundleMgrProxy(IBundleManager iBundleManager) {
        bundleMgrProxy = iBundleManager;
    }

    public static SysAbilityFactory getSysAbilityFactory() {
        SysAbilityFactory sysAbilityFactory2 = sysAbilityFactory;
        return sysAbilityFactory2 == null ? new DefaultSysAbilityFactory() : sysAbilityFactory2;
    }

    private boolean initBundleMgrProxy() {
        synchronized (BundleMgrBridge.class) {
            if (bundleMgrProxy == null) {
                IRemoteObject sysAbility = getSysAbilityFactory().getSysAbility(401);
                if (sysAbility == null) {
                    AppLog.e(SHELL_LABEL, "BundleMgrBridge::initBundleMgrProxy BMS not available", new Object[0]);
                    HiViewUtil.sendBmsEvent();
                    return false;
                }
                bundleMgrProxy = new BundleManager(sysAbility);
                sysAbility.addDeathRecipient($$Lambda$BundleMgrBridge$LD1X_B8XmofjzcY9Pe_ID4z3o70.INSTANCE, 0);
            }
            return true;
        }
    }

    static /* synthetic */ void lambda$initBundleMgrProxy$0() {
        AppLog.w(SHELL_LABEL, "BundleMgrBridge::initBundleMgrProxy receive death notify", new Object[0]);
        bundleMgrProxy = null;
    }

    public List<AbilityInfo> queryAbilityByIntent(Intent intent) {
        if (intent == null) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::queryAbilityByIntent intent param invalid", new Object[0]);
            return null;
        } else if (!initBundleMgrProxy()) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::queryAbilityByIntent get proxy failed", new Object[0]);
            return null;
        } else {
            try {
                return bundleMgrProxy.queryAbilityByIntent(intent);
            } catch (RemoteException e) {
                AppLog.e(SHELL_LABEL, "BundleMgrBridge::queryAbilityByIntent get data failed:%{public}s", e.getMessage());
                return null;
            }
        }
    }

    public BundleInfo getBundleInfo(String str, int i) {
        if (!initBundleMgrProxy()) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getBundleInfo get proxy failed", new Object[0]);
            return null;
        }
        try {
            return bundleMgrProxy.getBundleInfo(str, i);
        } catch (RemoteException e) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getBundleInfo get data failed:%{public}s", e.getMessage());
            return null;
        }
    }

    public List<String> getModuleSourceDirs(String str, int i) {
        BundleInfo bundleInfo = getBundleInfo(str, i);
        if (bundleInfo == null) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getModuleSourceDirs failed, bundleInfo is null", new Object[0]);
            return null;
        }
        ApplicationInfo appInfo = bundleInfo.getAppInfo();
        if (appInfo != null) {
            return appInfo.getModuleSourceDirs();
        }
        AppLog.e(SHELL_LABEL, "HarmonyApplication::setResources failed, applicationInfo is null", new Object[0]);
        return null;
    }

    public AbilityInfo getAbilityInfo(String str, String str2) {
        if (str == null || str2 == null) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getAbilityInfo param invalid", new Object[0]);
            return null;
        } else if (!initBundleMgrProxy()) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getAbilityInfo get proxy failed", new Object[0]);
            return null;
        } else {
            try {
                AbilityInfo abilityInfo = bundleMgrProxy.getAbilityInfo(str, str2);
                if (abilityInfo != null) {
                    AppLog.i(SHELL_LABEL, "BundleMgrBridge::getAbilityInfo bundleName: %{public}s, className: %{public}s", abilityInfo.getBundleName(), abilityInfo.getClassName());
                }
                return abilityInfo;
            } catch (RemoteException e) {
                AppLog.e(SHELL_LABEL, "BundleMgrBridge::getAbilityInfo get data failed:%{public}s", e.getMessage());
                return null;
            }
        }
    }

    public ApplicationInfo getApplicationInfo(String str, int i, int i2) {
        if (str == null) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getApplicationInfo param invalid", new Object[0]);
            return null;
        } else if (!initBundleMgrProxy()) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::getApplicationInfo get proxy failed", new Object[0]);
            return null;
        } else {
            try {
                return bundleMgrProxy.getApplicationInfo(str, i, i2);
            } catch (RemoteException unused) {
                AppLog.e(SHELL_LABEL, "BundleMgrBridge::getAbilityInfo get data failed", new Object[0]);
                return null;
            }
        }
    }

    public BundleInfo attachApplication(String str, IRemoteObject iRemoteObject) {
        if (str == null || str.isEmpty()) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::attachApplication param invalid", new Object[0]);
            return null;
        } else if (!initBundleMgrProxy()) {
            AppLog.e(SHELL_LABEL, "BundleMgrBridge::attachApplication get proxy failed", new Object[0]);
            return null;
        } else {
            try {
                return bundleMgrProxy.attachApplication(str, iRemoteObject);
            } catch (RemoteException unused) {
                AppLog.e(SHELL_LABEL, "BundleMgrBridge::attachApplication get data failed", new Object[0]);
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class DefaultSysAbilityFactory implements SysAbilityFactory {
        private DefaultSysAbilityFactory() {
        }

        @Override // ohos.abilityshell.SysAbilityFactory
        public IRemoteObject getSysAbility(int i) {
            return SysAbilityManager.getSysAbility(i);
        }
    }
}
