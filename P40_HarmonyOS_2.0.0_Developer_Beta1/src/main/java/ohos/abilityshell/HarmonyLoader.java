package ohos.abilityshell;

import android.content.Context;
import dalvik.system.PathClassLoader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.BundleInfo;
import ohos.bundle.ShellApplicationCallback;
import ohos.hiviewdfx.HiLogLabel;
import ohos.tools.Bytrace;

public class HarmonyLoader {
    private static final String LIBS = "!/libs/";
    private static final CountDownLatch LOADER_LATCH = new CountDownLatch(1);
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static volatile BundleMgrBridge bundleMgrImpl;
    private static boolean isLoadDone = false;
    private Context applicationContext;
    private ApplicationChangeReceive receive;

    HarmonyLoader(Context context) {
        this.applicationContext = context;
    }

    private static void setLoaderDone(boolean z) {
        isLoadDone = z;
    }

    public static void waitForLoadHarmony() {
        AppLog.d(SHELL_LABEL, "waitForLoadHarmony begin", new Object[0]);
        if (!isLoadDone) {
            try {
                LOADER_LATCH.await();
            } catch (InterruptedException unused) {
                AppLog.e(SHELL_LABEL, "waitForLoadHarmony InterruptedException occur", new Object[0]);
            }
        }
        if (!isLoadDone) {
            AppLog.d(SHELL_LABEL, "load harmony application failed!", new Object[0]);
        }
        AppLog.d(SHELL_LABEL, "waitForLoadHarmony end", new Object[0]);
    }

    public void tryLoadHarmony(Context context) throws IllegalStateException {
        AppLog.i(SHELL_LABEL, "tryLoadHarmony start", new Object[0]);
        Bytrace.startTrace(2147483648L, "tryLoadHarmony");
        if (context != null) {
            if (bundleMgrImpl == null) {
                bundleMgrImpl = new BundleMgrBridge();
            }
            if (this.receive == null) {
                Bytrace.startTrace(2147483648L, "attach application");
                this.receive = new ApplicationChangeReceive();
                BundleInfo attachApplication = bundleMgrImpl.attachApplication(context.getPackageName(), this.receive);
                Bytrace.finishTrace(2147483648L, "attach application");
                if (attachApplication != null) {
                    HarmonyApplication.getInstance().setBundleInfo(attachApplication);
                    loadHarmony(attachApplication);
                    setLoaderDone(true);
                    LOADER_LATCH.countDown();
                } else {
                    throw new IllegalStateException("failed to attach Application");
                }
            }
            Bytrace.finishTrace(2147483648L, "tryLoadHarmony");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadHarmony(BundleInfo bundleInfo) {
        if (this.applicationContext == null || bundleInfo == null) {
            AppLog.i(SHELL_LABEL, "tryLoadHarmony application context or bundleInfo is null", new Object[0]);
            return;
        }
        String moduleDir = bundleInfo.getModuleDir(bundleInfo.getEntryModuleName());
        if (moduleDir.isEmpty()) {
            AppLog.i(SHELL_LABEL, "entry hapSourceDir is empty %{public}b", Boolean.valueOf(moduleDir.isEmpty()));
        } else {
            loadBegin(bundleInfo, moduleDir, bundleInfo.getEntryModuleName(), true);
        }
    }

    public void loadFeature(String str) {
        BundleInfo bundleInfo = HarmonyApplication.getInstance().getBundleInfo();
        if (this.applicationContext == null || bundleInfo == null) {
            AppLog.i(SHELL_LABEL, "tryLoadHarmony application context or bundleInfo is null", new Object[0]);
        } else if (str == null || !str.equals(bundleInfo.getEntryModuleName())) {
            String moduleDir = bundleInfo.getModuleDir(str);
            if (moduleDir.isEmpty()) {
                AppLog.i(SHELL_LABEL, "hapSourceDir or hapModuleInfo is empty %{public}b", Boolean.valueOf(moduleDir.isEmpty()));
            } else {
                loadBegin(bundleInfo, moduleDir, str, false);
            }
        } else {
            AppLog.i(SHELL_LABEL, "this module is entry hap", new Object[0]);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0016: APUT  (r3v0 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v0 java.lang.String) */
    private void loadBegin(BundleInfo bundleInfo, String str, String str2, boolean z) {
        boolean z2;
        String cpuAbi = bundleInfo.getCpuAbi();
        boolean compressNativeLibs = bundleInfo.getCompressNativeLibs();
        HiLogLabel hiLogLabel = SHELL_LABEL;
        Object[] objArr = new Object[1];
        objArr[0] = compressNativeLibs ? "true" : "false";
        AppLog.d(hiLogLabel, "tryLoadHarmony get compressNativeLibs result:%{private}s", objArr);
        if (compressNativeLibs || cpuAbi == null || cpuAbi.isEmpty()) {
            z2 = false;
        } else {
            AppLog.d(SHELL_LABEL, "tryLoadHarmony get current device cpuAbi: %{private}s", cpuAbi);
            z2 = true;
        }
        ClassLoader classLoader = this.applicationContext.getClassLoader();
        if (classLoader instanceof PathClassLoader) {
            PathClassLoader pathClassLoader = (PathClassLoader) classLoader;
            pathClassLoader.addDexPath(str);
            if (z2) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(str + LIBS + cpuAbi);
                pathClassLoader.addNativePath(arrayList);
            }
            if (!z) {
                HarmonyApplication.getInstance().loadClass(str2);
            }
            AppLog.d(SHELL_LABEL, "tryLoadHarmony add path %{private}s to classloader success", str);
            return;
        }
        throw new IllegalStateException("class loader is not a PathClassLoader");
    }

    /* access modifiers changed from: private */
    public class ApplicationChangeReceive extends ShellApplicationCallback {
        private ApplicationChangeReceive() {
        }

        @Override // ohos.bundle.ShellApplicationCallback, ohos.bundle.IShellApplication
        public void onBundleUpdated(BundleInfo bundleInfo) {
            AppLog.i(HarmonyLoader.SHELL_LABEL, "tryLoadHarmony add path %{private}s to classloader success", bundleInfo);
            HarmonyApplication.getInstance().setBundleInfo(bundleInfo);
            HarmonyApplication.getInstance().getApplication().setBundleInfo(bundleInfo);
            HarmonyLoader.this.loadHarmony(bundleInfo);
            for (String str : HarmonyApplication.getInstance().getLoadedHapMap().keySet()) {
                HarmonyLoader.this.loadFeature(str);
            }
            HarmonyResources.setNewResourceState();
        }
    }
}
