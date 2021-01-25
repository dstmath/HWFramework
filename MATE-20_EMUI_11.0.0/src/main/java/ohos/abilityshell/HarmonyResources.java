package ohos.abilityshell;

import android.content.res.AssetManager;
import android.content.res.Resources;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.BundleInfo;
import ohos.bundle.ModuleInfo;
import ohos.global.innerkit.asset.Package;
import ohos.global.resource.ResourcePath;
import ohos.hiviewdfx.HiLogLabel;

public class HarmonyResources {
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static int resourceState = 0;

    public static Package getPackage(BundleInfo bundleInfo) {
        Package r0 = null;
        if (bundleInfo == null || bundleInfo.getAppInfo() == null) {
            AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage failed, bundleInfo is null", new Object[0]);
            return null;
        }
        List<String> moduleSourceDirs = bundleInfo.getAppInfo().getModuleSourceDirs();
        if (moduleSourceDirs.isEmpty()) {
            AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage failed, moduleSourceDirs is null", new Object[0]);
            return null;
        }
        try {
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            Method method = assetManager.getClass().getMethod("addAssetPath", String.class);
            for (String str : moduleSourceDirs) {
                if (str != null) {
                    method.invoke(assetManager, str);
                    AppLog.d(SHELL_LABEL, "HarmonyResource::getPackage assetPath %{private}s", str);
                }
            }
            Resources resources = new Resources(assetManager, null, null);
            Package r2 = new Package();
            try {
                r2.setResource(resources);
                AppLog.i(SHELL_LABEL, "HarmonyResource::getPackage success", new Object[0]);
                return r2;
            } catch (IllegalAccessException unused) {
                r0 = r2;
                AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage IllegalAccessException", new Object[0]);
                return r0;
            } catch (InstantiationException unused2) {
                r0 = r2;
                AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage InstantiationException", new Object[0]);
                return r0;
            } catch (NoSuchMethodException unused3) {
                r0 = r2;
                AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage NoSuchMethodException", new Object[0]);
                return r0;
            } catch (InvocationTargetException unused4) {
                r0 = r2;
                AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage InvocationTargetException", new Object[0]);
                return r0;
            }
        } catch (IllegalAccessException unused5) {
            AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage IllegalAccessException", new Object[0]);
            return r0;
        } catch (InstantiationException unused6) {
            AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage InstantiationException", new Object[0]);
            return r0;
        } catch (NoSuchMethodException unused7) {
            AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage NoSuchMethodException", new Object[0]);
            return r0;
        } catch (InvocationTargetException unused8) {
            AppLog.e(SHELL_LABEL, "HarmonyResource::getPackage InvocationTargetException", new Object[0]);
            return r0;
        }
    }

    public static ResourcePath[] getResourcePath(BundleInfo bundleInfo) {
        ResourcePath[] resourcePathArr = new ResourcePath[0];
        if (bundleInfo == null || bundleInfo.getAppInfo() == null) {
            AppLog.e("HarmonyResource::getResourcePath failed, bundleInfo is empty", new Object[0]);
            return resourcePathArr;
        }
        List<ModuleInfo> moduleInfos = bundleInfo.getAppInfo().getModuleInfos();
        if (moduleInfos == null || moduleInfos.isEmpty()) {
            AppLog.e("HarmonyResource::getResourcePath failed, moduleInfo is empty", new Object[0]);
            return resourcePathArr;
        }
        int size = moduleInfos.size();
        ResourcePath[] resourcePathArr2 = new ResourcePath[size];
        for (int i = 0; i < size; i++) {
            ModuleInfo moduleInfo = moduleInfos.get(i);
            if (moduleInfo == null || moduleInfo.getModuleSourceDir() == null || moduleInfo.getModuleName() == null) {
                AppLog.i("HarmonyResource::getResourcePath moduleSourceDir or moduleName is null", new Object[0]);
            } else {
                AppLog.d("HarmonyResource::getResourcePath moduleSourceDirs: %{private}s, moduleName: %{private}s", moduleInfo.getModuleSourceDir(), moduleInfo.getModuleName());
                ResourcePath resourcePath = new ResourcePath();
                resourcePath.setResourcePath(moduleInfo.getModuleSourceDir(), moduleInfo.getModuleName());
                resourcePathArr2[i] = resourcePath;
            }
        }
        return resourcePathArr2;
    }

    public static void setNewResourceState() {
        resourceState++;
    }

    public static int getNewResourceState() {
        return resourceState;
    }
}
