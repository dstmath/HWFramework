package android.webkit;

import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.pm.PackageInfo;
import android.content.res.CompatibilityInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import dalvik.system.VMRuntime;
import java.util.Arrays;

@VisibleForTesting
public class WebViewLibraryLoader {
    private static final String CHROMIUM_WEBVIEW_NATIVE_RELRO_32 = "/data/misc/shared_relro/libwebviewchromium32.relro";
    private static final String CHROMIUM_WEBVIEW_NATIVE_RELRO_64 = "/data/misc/shared_relro/libwebviewchromium64.relro";
    private static final boolean DEBUG = false;
    private static final String LOGTAG = WebViewLibraryLoader.class.getSimpleName();
    private static boolean sAddressSpaceReserved = false;

    static native boolean nativeCreateRelroFile(String str, String str2, ClassLoader classLoader);

    static native int nativeLoadWithRelroFile(String str, String str2, ClassLoader classLoader);

    static native boolean nativeReserveAddressSpace(long j);

    /* access modifiers changed from: private */
    public static class RelroFileCreator {
        private RelroFileCreator() {
        }

        public static void main(String[] args) {
            String str;
            boolean is64Bit = VMRuntime.getRuntime().is64Bit();
            try {
                if (args.length == 2 && args[0] != null) {
                    if (args[1] != null) {
                        String packageName = args[0];
                        String libraryFileName = args[1];
                        String str2 = WebViewLibraryLoader.LOGTAG;
                        Log.v(str2, "RelroFileCreator (64bit = " + is64Bit + "), package: " + packageName + " library: " + libraryFileName);
                        if (!WebViewLibraryLoader.sAddressSpaceReserved) {
                            Log.e(WebViewLibraryLoader.LOGTAG, "can't create relro file; address space not reserved");
                            try {
                                WebViewFactory.getUpdateServiceUnchecked().notifyRelroCreationCompleted();
                            } catch (RemoteException e) {
                                Log.e(WebViewLibraryLoader.LOGTAG, "error notifying update service", e);
                            }
                            if (0 == 0) {
                                Log.e(WebViewLibraryLoader.LOGTAG, "failed to create relro file");
                            }
                            System.exit(0);
                            return;
                        }
                        LoadedApk apk = ActivityThread.currentActivityThread().getPackageInfo(packageName, (CompatibilityInfo) null, 3);
                        if (is64Bit) {
                            str = WebViewLibraryLoader.CHROMIUM_WEBVIEW_NATIVE_RELRO_64;
                        } else {
                            str = WebViewLibraryLoader.CHROMIUM_WEBVIEW_NATIVE_RELRO_32;
                        }
                        boolean result = WebViewLibraryLoader.nativeCreateRelroFile(libraryFileName, str, apk.getClassLoader());
                        try {
                            WebViewFactory.getUpdateServiceUnchecked().notifyRelroCreationCompleted();
                        } catch (RemoteException e2) {
                            Log.e(WebViewLibraryLoader.LOGTAG, "error notifying update service", e2);
                        }
                        if (!result) {
                            Log.e(WebViewLibraryLoader.LOGTAG, "failed to create relro file");
                        }
                        System.exit(0);
                        return;
                    }
                }
                String str3 = WebViewLibraryLoader.LOGTAG;
                Log.e(str3, "Invalid RelroFileCreator args: " + Arrays.toString(args));
            } finally {
                try {
                    WebViewFactory.getUpdateServiceUnchecked().notifyRelroCreationCompleted();
                } catch (RemoteException e3) {
                    Log.e(WebViewLibraryLoader.LOGTAG, "error notifying update service", e3);
                }
                if (0 == 0) {
                    Log.e(WebViewLibraryLoader.LOGTAG, "failed to create relro file");
                }
                System.exit(0);
            }
        }
    }

    static void createRelroFile(boolean is64Bit, String packageName, String libraryFileName) {
        final String abi = is64Bit ? Build.SUPPORTED_64_BIT_ABIS[0] : Build.SUPPORTED_32_BIT_ABIS[0];
        Runnable crashHandler = new Runnable() {
            /* class android.webkit.WebViewLibraryLoader.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    String str = WebViewLibraryLoader.LOGTAG;
                    Log.e(str, "relro file creator for " + abi + " crashed. Proceeding without");
                    WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                } catch (RemoteException e) {
                    String str2 = WebViewLibraryLoader.LOGTAG;
                    Log.e(str2, "Cannot reach WebViewUpdateService. " + e.getMessage());
                }
            }
        };
        try {
            String name = RelroFileCreator.class.getName();
            String[] strArr = {packageName, libraryFileName};
            if (!((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).startIsolatedProcess(name, strArr, "WebViewLoader-" + abi, abi, 1037, crashHandler)) {
                throw new Exception("Failed to start the relro file creator process");
            }
        } catch (Throwable t) {
            String str = LOGTAG;
            Log.e(str, "error starting relro file creator for abi " + abi, t);
            crashHandler.run();
        }
    }

    static int prepareNativeLibraries(PackageInfo webViewPackageInfo) {
        String libraryFileName = WebViewFactory.getWebViewLibrary(webViewPackageInfo.applicationInfo);
        if (libraryFileName == null) {
            return 0;
        }
        return createRelros(webViewPackageInfo.packageName, libraryFileName);
    }

    private static int createRelros(String packageName, String libraryFileName) {
        int numRelros = 0;
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            createRelroFile(false, packageName, libraryFileName);
            numRelros = 0 + 1;
        }
        if (Build.SUPPORTED_64_BIT_ABIS.length <= 0) {
            return numRelros;
        }
        createRelroFile(true, packageName, libraryFileName);
        return numRelros + 1;
    }

    static void reserveAddressSpaceInZygote() {
        System.loadLibrary("webviewchromium_loader");
        long addressSpaceToReserve = VMRuntime.getRuntime().is64Bit() ? TrafficStats.GB_IN_BYTES : 136314880;
        sAddressSpaceReserved = nativeReserveAddressSpace(addressSpaceToReserve);
        if (!sAddressSpaceReserved) {
            String str = LOGTAG;
            Log.e(str, "reserving " + addressSpaceToReserve + " bytes of address space failed");
        }
    }

    public static int loadNativeLibrary(ClassLoader clazzLoader, String libraryFileName) {
        String relroPath;
        if (!sAddressSpaceReserved) {
            Log.e(LOGTAG, "can't load with relro file; address space not reserved");
            return 2;
        }
        if (VMRuntime.getRuntime().is64Bit()) {
            relroPath = CHROMIUM_WEBVIEW_NATIVE_RELRO_64;
        } else {
            relroPath = CHROMIUM_WEBVIEW_NATIVE_RELRO_32;
        }
        int result = nativeLoadWithRelroFile(libraryFileName, relroPath, clazzLoader);
        if (result != 0) {
            Log.w(LOGTAG, "failed to load with relro file, proceeding without");
        }
        return result;
    }
}
