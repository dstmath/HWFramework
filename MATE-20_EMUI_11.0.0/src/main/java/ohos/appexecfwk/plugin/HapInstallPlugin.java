package ohos.appexecfwk.plugin;

import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import ohos.aafwk.ability.Ability;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.BundleInfo;
import ohos.bundle.IBundleInstaller;
import ohos.bundle.IBundleManager;
import ohos.bundle.InstallerCallback;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public final class HapInstallPlugin implements ModuleGroup.ModuleGroupHandler {
    private static final int COMMON_ERROR_CODE = 200;
    private static final HiLogLabel HAP_INSTALL_PLUGIN = new HiLogLabel(3, 218108160, "HapInstallPlugin");
    private static final String TAG = "HapInstallPlugin#";
    private static HapInstallPlugin instance;
    private Ability ability;
    private InstallerCallback installerCallback = new InstallerCallback() {
        /* class ohos.appexecfwk.plugin.HapInstallPlugin.AnonymousClass1 */

        @Override // ohos.bundle.InstallerCallback, ohos.bundle.IInstallerCallback
        public void onFinished(int i, String str) {
            if (i == 0) {
                HapInstallPlugin.this.moduleResult.success(true);
            } else {
                HapInstallPlugin.this.moduleResult.success(false);
            }
        }
    };
    private Result moduleResult;

    public static String getJsCode() {
        return "var pkg = {\n    module: null,\n    onInit: function onInit() {\n        if (pkg.module == null) {\n            pkg.module = ModuleGroup.getGroup(\"AceModuleGroup/package\");\n        }\n    },\n    hasInstalled: async function hasInstalled(param) {\n        console.error('hasInstalled called');\n        if (typeof param.bundleName !== 'string') {\n            commonCallback(param.fail, 'fail', 'value is not an available number',202);\n            commonCallback(param.complete, 'complete');\n            return;\n        }\n        pkg.onInit();\n        return await catching(pkg.module.callNative(\"hasInstalled\", param.bundleName), param);\n    },\n};\nglobal.systemplugin.package = {\n    hasInstalled: pkg.hasInstalled,\n};";
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        Ability ability2 = this.ability;
        if (ability2 == null) {
            result.error(200, "ability is unavailable");
            return;
        }
        IBundleManager bundleManager = ability2.getBundleManager();
        if (bundleManager == null) {
            result.error(200, "bundleManager is unavailable");
            return;
        }
        this.moduleResult = result;
        if (function.name.equals("hasInstalled")) {
            AppLog.d(HAP_INSTALL_PLUGIN, "hasInstalled called", new Object[0]);
            if (function.arguments.get(0) instanceof String) {
                checkPackageInstall(bundleManager, (String) function.arguments.get(0));
            } else {
                result.error(200, "arguments is not an available String");
            }
        } else {
            result.notExistFunction();
        }
    }

    public static void register(Context context) {
        instance = new HapInstallPlugin();
        instance.onRegister(context);
        ModuleGroup.registerModuleGroup("AceModuleGroup/package", instance, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static void deregister(Context context) {
        ModuleGroup.registerModuleGroup("AceModuleGroup/package", null, context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/package");
        hashSet.add("AceEventGroup/package");
        return hashSet;
    }

    private void checkPackageInstall(IBundleManager iBundleManager, String str) {
        try {
            BundleInfo bundleInfo = iBundleManager.getBundleInfo(str, 0);
            if (bundleInfo == null) {
                this.moduleResult.success(false);
            } else if (bundleInfo.getName().equals(str)) {
                this.moduleResult.success(true);
            } else {
                this.moduleResult.error(200, "bundleName is not exist");
            }
        } catch (RemoteException unused) {
            this.moduleResult.error(200, "failed to obtain the bundle manager service");
        } catch (SecurityException unused2) {
            this.moduleResult.error(200, "application does not have required permissions");
        }
    }

    private void installPackage(IBundleManager iBundleManager, String str) {
        try {
            IBundleInstaller bundleInstaller = iBundleManager.getBundleInstaller();
            if (bundleInstaller == null) {
                this.moduleResult.error(200, "get bundle install failed");
                return;
            }
            ArrayList arrayList = new ArrayList();
            arrayList.add(str);
            bundleInstaller.install(arrayList, null, this.installerCallback);
        } catch (RemoteException unused) {
            this.moduleResult.error(200, "bundle install remote exception");
        }
    }

    private void onRegister(Context context) {
        if (context instanceof Ability) {
            this.ability = (Ability) context;
        }
    }
}
