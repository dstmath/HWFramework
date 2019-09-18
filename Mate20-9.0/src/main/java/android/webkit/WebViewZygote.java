package android.webkit;

import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ChildZygoteProcess;
import android.os.Process;
import android.os.ZygoteProcess;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WebViewZygote {
    private static final String LOGTAG = "WebViewZygote";
    private static final Object sLock = new Object();
    @GuardedBy("sLock")
    private static boolean sMultiprocessEnabled = false;
    @GuardedBy("sLock")
    private static PackageInfo sPackage;
    @GuardedBy("sLock")
    private static ApplicationInfo sPackageOriginalAppInfo;
    @GuardedBy("sLock")
    private static ChildZygoteProcess sZygote;

    public static ZygoteProcess getProcess() {
        synchronized (sLock) {
            if (sZygote != null) {
                ChildZygoteProcess childZygoteProcess = sZygote;
                return childZygoteProcess;
            }
            connectToZygoteIfNeededLocked();
            ChildZygoteProcess childZygoteProcess2 = sZygote;
            return childZygoteProcess2;
        }
    }

    public static String getPackageName() {
        synchronized (sLock) {
            if (sPackage == null) {
                Log.e(LOGTAG, "sPackage is NULL, return null");
                return null;
            }
            String str = sPackage.packageName;
            return str;
        }
    }

    public static boolean isMultiprocessEnabled() {
        boolean z;
        synchronized (sLock) {
            z = sMultiprocessEnabled && sPackage != null;
        }
        return z;
    }

    public static void setMultiprocessEnabled(boolean enabled) {
        synchronized (sLock) {
            sMultiprocessEnabled = enabled;
            if (enabled) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute($$Lambda$xYTrYQCPf1HcdlWzDof3mq93ihs.INSTANCE);
            } else {
                stopZygoteLocked();
            }
        }
    }

    public static void onWebViewProviderChanged(PackageInfo packageInfo, ApplicationInfo originalAppInfo) {
        synchronized (sLock) {
            sPackage = packageInfo;
            sPackageOriginalAppInfo = originalAppInfo;
            if (sMultiprocessEnabled) {
                stopZygoteLocked();
            }
        }
    }

    @GuardedBy("sLock")
    private static void stopZygoteLocked() {
        if (sZygote != null) {
            sZygote.close();
            Process.killProcess(sZygote.getPid());
            sZygote = null;
        }
    }

    @GuardedBy("sLock")
    private static void connectToZygoteIfNeededLocked() {
        String str;
        String cacheKey;
        if (sZygote == null) {
            if (sPackage == null) {
                Log.e(LOGTAG, "Cannot connect to zygote, no package specified");
                return;
            }
            try {
                sZygote = Process.zygoteProcess.startChildZygote("com.android.internal.os.WebViewZygoteInit", "webview_zygote", Process.WEBVIEW_ZYGOTE_UID, Process.WEBVIEW_ZYGOTE_UID, null, 0, "webview_zygote", sPackage.applicationInfo.primaryCpuAbi, null);
                List<String> zipPaths = new ArrayList<>(10);
                List<String> libPaths = new ArrayList<>(10);
                LoadedApk.makePaths(null, false, sPackage.applicationInfo, zipPaths, libPaths);
                String librarySearchPath = TextUtils.join((CharSequence) File.pathSeparator, (Iterable) libPaths);
                if (zipPaths.size() == 1) {
                    str = zipPaths.get(0);
                } else {
                    str = TextUtils.join((CharSequence) File.pathSeparator, (Iterable) zipPaths);
                }
                String zip = str;
                String libFileName = WebViewFactory.getWebViewLibrary(sPackage.applicationInfo);
                LoadedApk.makePaths(null, false, sPackageOriginalAppInfo, zipPaths, null);
                if (zipPaths.size() == 1) {
                    cacheKey = zipPaths.get(0);
                } else {
                    cacheKey = TextUtils.join((CharSequence) File.pathSeparator, (Iterable) zipPaths);
                }
                ZygoteProcess.waitForConnectionToZygote(sZygote.getPrimarySocketAddress());
                Log.d(LOGTAG, "Preloading package " + zip + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + librarySearchPath);
                String str2 = zip;
                String str3 = librarySearchPath;
                sZygote.preloadPackageForAbi(str2, str3, libFileName, cacheKey, Build.SUPPORTED_ABIS[0]);
            } catch (Exception e) {
                Log.e(LOGTAG, "Error connecting to webview zygote", e);
                stopZygoteLocked();
            }
        }
    }
}
