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

public class HarmonyLoader implements Runnable {
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
        AppLog.i(SHELL_LABEL, "waitForLoadHarmony begin", new Object[0]);
        if (!isLoadDone) {
            try {
                LOADER_LATCH.await();
            } catch (InterruptedException unused) {
                AppLog.e(SHELL_LABEL, "waitForLoadHarmony InterruptedException occur", new Object[0]);
            }
        }
        if (!isLoadDone) {
            AppLog.w(SHELL_LABEL, "load harmony application failed!", new Object[0]);
        }
        AppLog.i(SHELL_LABEL, "waitForLoadHarmony end", new Object[0]);
    }

    @Override // java.lang.Runnable
    public void run() {
        tryLoadHarmony(this.applicationContext);
    }

    private void tryLoadHarmony(Context context) throws IllegalStateException {
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
            AppLog.w(SHELL_LABEL, "tryLoadHarmony application context or bundleInfo is null", new Object[0]);
            return;
        }
        String moduleDir = bundleInfo.getModuleDir(bundleInfo.getEntryModuleName());
        if (moduleDir.isEmpty()) {
            AppLog.w(SHELL_LABEL, "entry hapSourceDir is empty", new Object[0]);
        } else {
            loadBegin(bundleInfo, moduleDir, bundleInfo.getEntryModuleName(), true);
        }
    }

    public void loadFeature(String str) {
        BundleInfo bundleInfo = HarmonyApplication.getInstance().getBundleInfo();
        if (this.applicationContext == null || bundleInfo == null) {
            AppLog.w(SHELL_LABEL, "loadFeature application context or bundleInfo is null", new Object[0]);
        } else if (str == null || !str.equals(bundleInfo.getEntryModuleName())) {
            String moduleDir = bundleInfo.getModuleDir(str);
            if (moduleDir.isEmpty()) {
                AppLog.w(SHELL_LABEL, "hapSourceDir or hapModuleInfo is empty", new Object[0]);
            } else {
                loadBegin(bundleInfo, moduleDir, str, false);
            }
        } else {
            AppLog.w(SHELL_LABEL, "this module is entry hap", new Object[0]);
        }
    }

    private void loadBegin(BundleInfo bundleInfo, String str, String str2, boolean z) {
        String cpuAbi = bundleInfo.getCpuAbi();
        boolean z2 = !bundleInfo.getCompressNativeLibs() && cpuAbi != null && !cpuAbi.isEmpty();
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
                return;
            }
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
