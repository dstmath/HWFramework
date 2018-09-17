package android.webkit;

import android.app.LoadedApk;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.SystemService;
import android.os.SystemService.State;
import android.os.ZygoteProcess;
import android.os.ZygoteProcess.ZygoteState;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.util.TimedRemoteCaller;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class WebViewZygote {
    private static final String LOGTAG = "WebViewZygote";
    private static final String WEBVIEW_ZYGOTE_SERVICE_32 = "webview_zygote32";
    private static final String WEBVIEW_ZYGOTE_SERVICE_64 = "webview_zygote64";
    private static final String WEBVIEW_ZYGOTE_SOCKET = "webview_zygote";
    private static final Object sLock = new Object();
    @GuardedBy("sLock")
    private static boolean sMultiprocessEnabled = false;
    @GuardedBy("sLock")
    private static PackageInfo sPackage;
    @GuardedBy("sLock")
    private static String sPackageCacheKey;
    @GuardedBy("sLock")
    private static boolean sStartedService = false;
    @GuardedBy("sLock")
    private static ZygoteProcess sZygote;

    public static ZygoteProcess getProcess() {
        synchronized (sLock) {
            ZygoteProcess zygoteProcess;
            if (sZygote != null) {
                zygoteProcess = sZygote;
                return zygoteProcess;
            }
            waitForServiceStartAndConnect();
            zygoteProcess = sZygote;
            return zygoteProcess;
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
        boolean z = false;
        synchronized (sLock) {
            if (sMultiprocessEnabled && sPackage != null) {
                z = true;
            }
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:13:0x001a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setMultiprocessEnabled(boolean enabled) {
        synchronized (sLock) {
            sMultiprocessEnabled = enabled;
            String serviceName = getServiceNameLocked();
            if (serviceName == null) {
            } else if (!enabled) {
                SystemService.stop(serviceName);
                sStartedService = false;
                sZygote = null;
            } else if (!sStartedService) {
                SystemService.start(serviceName);
                sStartedService = true;
            }
        }
    }

    public static void onWebViewProviderChanged(PackageInfo packageInfo, String cacheKey) {
        synchronized (sLock) {
            sPackage = packageInfo;
            sPackageCacheKey = cacheKey;
            if (sMultiprocessEnabled) {
                String serviceName = getServiceNameLocked();
                sZygote = null;
                if (SystemService.isStopped(serviceName)) {
                    SystemService.start(serviceName);
                } else {
                    SystemService.restart(serviceName);
                }
                sStartedService = true;
                return;
            }
        }
    }

    private static void waitForServiceStartAndConnect() {
        if (sStartedService) {
            String serviceName;
            synchronized (sLock) {
                serviceName = getServiceNameLocked();
            }
            try {
                SystemService.waitForState(serviceName, State.RUNNING, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
                synchronized (sLock) {
                    connectToZygoteIfNeededLocked();
                }
                return;
            } catch (TimeoutException e) {
                Log.e(LOGTAG, "Timed out waiting for " + serviceName);
                return;
            }
        }
        throw new AndroidRuntimeException("Tried waiting for the WebView Zygote Service to start running without first starting the service.");
    }

    @GuardedBy("sLock")
    private static String getServiceNameLocked() {
        if (sPackage == null) {
            return null;
        }
        if (Arrays.asList(Build.SUPPORTED_64_BIT_ABIS).contains(sPackage.applicationInfo.primaryCpuAbi)) {
            return WEBVIEW_ZYGOTE_SERVICE_64;
        }
        return WEBVIEW_ZYGOTE_SERVICE_32;
    }

    @GuardedBy("sLock")
    private static void connectToZygoteIfNeededLocked() {
        if (sZygote == null) {
            if (sPackage == null) {
                Log.e(LOGTAG, "Cannot connect to zygote, no package specified");
                return;
            }
            String serviceName = getServiceNameLocked();
            if (SystemService.isRunning(serviceName)) {
                try {
                    String zip;
                    sZygote = new ZygoteProcess(WEBVIEW_ZYGOTE_SOCKET, null);
                    Iterable zipPaths = new ArrayList(10);
                    Iterable libPaths = new ArrayList(10);
                    LoadedApk.makePaths(null, false, sPackage.applicationInfo, zipPaths, libPaths);
                    String librarySearchPath = TextUtils.join(File.pathSeparator, libPaths);
                    if (zipPaths.size() == 1) {
                        zip = (String) zipPaths.get(0);
                    } else {
                        zip = TextUtils.join(File.pathSeparator, zipPaths);
                    }
                    waitForZygote();
                    Log.d(LOGTAG, "Preloading package " + zip + " " + librarySearchPath);
                    sZygote.preloadPackageForAbi(zip, librarySearchPath, sPackageCacheKey, Build.SUPPORTED_ABIS[0]);
                } catch (Exception e) {
                    Log.e(LOGTAG, "Error connecting to " + serviceName, e);
                    sZygote = null;
                }
                return;
            }
            Log.e(LOGTAG, serviceName + " is not running");
        }
    }

    private static void waitForZygote() {
        while (true) {
            try {
                ZygoteState.connect(WEBVIEW_ZYGOTE_SOCKET).close();
                break;
            } catch (IOException ioe) {
                Log.w(LOGTAG, "Got error connecting to zygote, retrying. msg= " + ioe.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
