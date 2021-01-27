package ohos.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSupport;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class LauncherService {
    private static final int GET_ABILITY_FLAG = 6;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218108160, "LauncherService");
    private static final int USER_ALL = -1;
    private Optional<IBundleManager> bundleManager;
    private BundleMonitor bundleMonitor;
    private Context context;

    public static abstract class BundleStatusCallback {
        public abstract void onBundleAdded(String str, int i);

        public abstract void onBundleRemoved(String str, int i);

        public abstract void onBundleUpdated(String str, int i);
    }

    public LauncherService(Context context2) {
        this.context = context2;
        init();
    }

    private void init() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_ABILITY_ADDED);
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_ABILITY_UPDATED);
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_ABILITY_REMOVED);
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscribeInfo.setUserId(-1);
        this.bundleMonitor = new BundleMonitor(commonEventSubscribeInfo);
        this.bundleManager = getBundleManager();
    }

    public void registerCallback(BundleStatusCallback bundleStatusCallback) {
        AppLog.d(TAG, "registerCallback called", new Object[0]);
        BundleMonitor bundleMonitor2 = this.bundleMonitor;
        if (bundleMonitor2 == null) {
            AppLog.e(TAG, "failed to register callback, bundleMonitor is null", new Object[0]);
        } else {
            bundleMonitor2.subscribe(bundleStatusCallback);
        }
    }

    public void unRegisterCallback() {
        AppLog.d(TAG, "unRegisterCallback called", new Object[0]);
        BundleMonitor bundleMonitor2 = this.bundleMonitor;
        if (bundleMonitor2 == null) {
            AppLog.e(TAG, "failed to unregister callback, bundleMonitor is null", new Object[0]);
        } else {
            bundleMonitor2.unsubscribe();
        }
    }

    private Optional<IBundleManager> getBundleManager() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e(TAG, "context is null", new Object[0]);
            return Optional.empty();
        }
        IBundleManager bundleManager2 = context2.getBundleManager();
        if (bundleManager2 != null) {
            return Optional.of(bundleManager2);
        }
        AppLog.e(TAG, "bundleManager is null", new Object[0]);
        return Optional.empty();
    }

    public Optional<List<LauncherAbilityInfo>> getAbilityList(String str, int i) {
        AppLog.d(TAG, "getAbilityList called", new Object[0]);
        Optional<IBundleManager> bundleManager2 = getBundleManager();
        if (!bundleManager2.isPresent()) {
            AppLog.e(TAG, "getAbilityList get bundleManager failed", new Object[0]);
            return Optional.empty();
        }
        Intent intent = new Intent();
        ElementName elementName = new ElementName();
        elementName.setBundleName(str);
        intent.setElement(elementName);
        intent.setAction(Intent.ACTION_HOME);
        intent.addEntity(Intent.ENTITY_HOME);
        try {
            List<AbilityInfo> queryAbilityByIntent = bundleManager2.get().queryAbilityByIntent(intent, 6, i);
            if (queryAbilityByIntent != null) {
                if (!queryAbilityByIntent.isEmpty()) {
                    ArrayList arrayList = new ArrayList();
                    for (AbilityInfo abilityInfo : queryAbilityByIntent) {
                        arrayList.add(new LauncherAbilityInfo(this.context, abilityInfo, i));
                    }
                    AppLog.d(TAG, "getAbilityList success and size is %{public}d", Integer.valueOf(arrayList.size()));
                    return Optional.of(arrayList);
                }
            }
            AppLog.e(TAG, "getAbilityList query ability by intent result is null or empty", new Object[0]);
            return Optional.empty();
        } catch (RemoteException e) {
            AppLog.e(TAG, "getAbilityList failed, %{public}s", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<LauncherAbilityInfo> getAbilityInfo(Intent intent, int i) {
        AppLog.d(TAG, "getAbilityInfo called", new Object[0]);
        if (intent == null) {
            AppLog.e(TAG, "getAbilityInfo intent is null", new Object[0]);
            return Optional.empty();
        }
        ElementName element = intent.getElement();
        if (element == null || element.getBundleName() == null || element.getAbilityName() == null) {
            AppLog.e(TAG, "getAbilityInfo elementName is null", new Object[0]);
            return Optional.empty();
        }
        Optional<IBundleManager> bundleManager2 = getBundleManager();
        if (!bundleManager2.isPresent()) {
            AppLog.e(TAG, "getAbilityInfo get bundleManager failed", new Object[0]);
            return Optional.empty();
        }
        try {
            List<AbilityInfo> queryAbilityByIntent = bundleManager2.get().queryAbilityByIntent(intent, 6, i);
            if (queryAbilityByIntent != null) {
                if (!queryAbilityByIntent.isEmpty()) {
                    AbilityInfo abilityInfo = queryAbilityByIntent.get(0);
                    if (abilityInfo == null) {
                        AppLog.e(TAG, "getAbilityInfo from bundleManager is null", new Object[0]);
                        return Optional.empty();
                    }
                    LauncherAbilityInfo launcherAbilityInfo = new LauncherAbilityInfo(this.context, abilityInfo, i);
                    AppLog.d(TAG, "getAbilityInfo success", new Object[0]);
                    return Optional.of(launcherAbilityInfo);
                }
            }
            AppLog.e(TAG, "getAbilityInfo query ability by intent result is null", new Object[0]);
            return Optional.empty();
        } catch (RemoteException e) {
            AppLog.e(TAG, "getAbilityInfo failed, %{public}s", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ApplicationInfo> getApplicationInfo(String str, int i, int i2) {
        AppLog.d(TAG, "getApplicationInfo called", new Object[0]);
        Optional<IBundleManager> bundleManager2 = getBundleManager();
        if (!bundleManager2.isPresent()) {
            AppLog.e(TAG, "getApplicationInfo get bundleManager failed", new Object[0]);
            return Optional.empty();
        } else if (str == null) {
            AppLog.e(TAG, "getApplicationInfo bundleName is null", new Object[0]);
            return Optional.empty();
        } else {
            try {
                ApplicationInfo applicationInfo = bundleManager2.get().getApplicationInfo(str, i, i2);
                if (applicationInfo == null) {
                    AppLog.e(TAG, "getApplicationInfo from bundleManager is null", new Object[0]);
                    return Optional.empty();
                }
                AppLog.d(TAG, "getApplicationInfo success", new Object[0]);
                return Optional.of(applicationInfo);
            } catch (RemoteException e) {
                AppLog.e(TAG, "getApplicationInfo failed, %{public}s", e.getMessage());
                return Optional.empty();
            }
        }
    }

    public boolean isBundleEnabled(String str) {
        AppLog.d(TAG, "isBundleEnabled called", new Object[0]);
        Optional<IBundleManager> bundleManager2 = getBundleManager();
        if (!bundleManager2.isPresent()) {
            AppLog.e(TAG, "get bundleManager failed", new Object[0]);
            return false;
        } else if (str == null || str.isEmpty()) {
            AppLog.e(TAG, "bundleName is null or empty", new Object[0]);
            return false;
        } else {
            try {
                return bundleManager2.get().isApplicationEnabled(str);
            } catch (IllegalArgumentException e) {
                AppLog.e(TAG, "illegal argument exception, %{public}s", e.getMessage());
                return false;
            }
        }
    }

    public boolean isAbilityEnabled(AbilityInfo abilityInfo) {
        AppLog.d(TAG, "isAbilityEnabled called", new Object[0]);
        if (abilityInfo == null) {
            AppLog.e(TAG, "abilityInfo is null", new Object[0]);
            return false;
        }
        Optional<IBundleManager> bundleManager2 = getBundleManager();
        if (!bundleManager2.isPresent()) {
            AppLog.e(TAG, "get bundleManager failed", new Object[0]);
            return false;
        }
        try {
            return bundleManager2.get().isAbilityEnabled(abilityInfo);
        } catch (IllegalArgumentException e) {
            AppLog.e(TAG, "illegal argument exception, %{public}s", e.getMessage());
            return false;
        }
    }

    public Optional<List<LauncherShortcutInfo>> getShortcutInfos(String str) {
        AppLog.d(TAG, "getShortcutInfos called", new Object[0]);
        if (str == null || str.isEmpty()) {
            AppLog.e(TAG, "bundleName is null", new Object[0]);
            return Optional.empty();
        } else if (!this.bundleManager.isPresent()) {
            AppLog.e(TAG, "get bundleManager failed", new Object[0]);
            return Optional.empty();
        } else {
            try {
                List<ShortcutInfo> shortcutInfos = this.bundleManager.get().getShortcutInfos(str);
                if (shortcutInfos == null) {
                    AppLog.e(TAG, "shortcutInfo is not exist in system", new Object[0]);
                    return Optional.empty();
                }
                ArrayList arrayList = new ArrayList();
                for (ShortcutInfo shortcutInfo : shortcutInfos) {
                    if (str.equals(shortcutInfo.getBundleName())) {
                        arrayList.add(new LauncherShortcutInfo(this.context, shortcutInfo));
                    }
                }
                return Optional.of(arrayList);
            } catch (RemoteException e) {
                AppLog.e(TAG, "getShortcutInfos failed, exception = %{public}s", e.getMessage());
                return Optional.empty();
            }
        }
    }

    public void startShortcut(String str, String str2) {
        AppLog.d(TAG, "startShortcut called", new Object[0]);
        if (str == null || str.isEmpty() || str2 == null || str2.isEmpty()) {
            AppLog.e(TAG, "shortcutId or bundleName is null", new Object[0]);
        } else if (!this.bundleManager.isPresent()) {
            AppLog.e(TAG, "get bundleManager failed", new Object[0]);
        } else {
            try {
                this.bundleManager.get().startShortcut(str, str2);
            } catch (RemoteException e) {
                AppLog.e(TAG, "startShortcut failed, exception = %{public}s", e.getMessage());
            }
        }
    }
}
