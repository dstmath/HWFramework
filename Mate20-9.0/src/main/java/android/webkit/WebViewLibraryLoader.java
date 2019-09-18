package android.webkit;

import android.app.ActivityManagerInternal;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebViewFactory;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@VisibleForTesting
public class WebViewLibraryLoader {
    private static final long CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES = 104857600;
    private static final String CHROMIUM_WEBVIEW_NATIVE_RELRO_32 = "/data/misc/shared_relro/libwebviewchromium32.relro";
    private static final String CHROMIUM_WEBVIEW_NATIVE_RELRO_64 = "/data/misc/shared_relro/libwebviewchromium64.relro";
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final String LOGTAG = WebViewLibraryLoader.class.getSimpleName();
    /* access modifiers changed from: private */
    public static boolean sAddressSpaceReserved = false;

    private static class RelroFileCreator {
        private RelroFileCreator() {
        }

        public static void main(String[] args) {
            String str;
            String str2;
            String str3;
            boolean is64Bit = VMRuntime.getRuntime().is64Bit();
            try {
                if (args.length == 1) {
                    if (args[0] != null) {
                        String access$000 = WebViewLibraryLoader.LOGTAG;
                        Log.v(access$000, "RelroFileCreator (64bit = " + is64Bit + "), lib: " + args[0]);
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
                        String str4 = args[0];
                        if (is64Bit) {
                            str3 = WebViewLibraryLoader.CHROMIUM_WEBVIEW_NATIVE_RELRO_64;
                        } else {
                            str3 = WebViewLibraryLoader.CHROMIUM_WEBVIEW_NATIVE_RELRO_32;
                        }
                        boolean result = WebViewLibraryLoader.nativeCreateRelroFile(str4, str3);
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
                String access$0002 = WebViewLibraryLoader.LOGTAG;
                Log.e(access$0002, "Invalid RelroFileCreator args: " + Arrays.toString(args));
            } finally {
                try {
                    WebViewFactory.getUpdateServiceUnchecked().notifyRelroCreationCompleted();
                } catch (RemoteException e3) {
                    str2 = "error notifying update service";
                    Log.e(WebViewLibraryLoader.LOGTAG, str2, e3);
                }
                if (0 == 0) {
                    str = "failed to create relro file";
                    Log.e(WebViewLibraryLoader.LOGTAG, str);
                }
                System.exit(0);
            }
        }
    }

    @VisibleForTesting
    public static class WebViewNativeLibrary {
        public final String path;
        public final long size;

        WebViewNativeLibrary(String path2, long size2) {
            this.path = path2;
            this.size = size2;
        }
    }

    static native boolean nativeCreateRelroFile(String str, String str2);

    static native int nativeLoadWithRelroFile(String str, String str2, ClassLoader classLoader);

    static native boolean nativeReserveAddressSpace(long j);

    static void createRelroFile(boolean is64Bit, WebViewNativeLibrary nativeLib) {
        final String abi = is64Bit ? Build.SUPPORTED_64_BIT_ABIS[0] : Build.SUPPORTED_32_BIT_ABIS[0];
        Runnable crashHandler = new Runnable() {
            public void run() {
                try {
                    String access$000 = WebViewLibraryLoader.LOGTAG;
                    Log.e(access$000, "relro file creator for " + abi + " crashed. Proceeding without");
                    WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                } catch (RemoteException e) {
                    String access$0002 = WebViewLibraryLoader.LOGTAG;
                    Log.e(access$0002, "Cannot reach WebViewUpdateService. " + e.getMessage());
                }
            }
        };
        if (nativeLib != null) {
            try {
                if (nativeLib.path != null) {
                    String name = RelroFileCreator.class.getName();
                    String[] strArr = {nativeLib.path};
                    if (!((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).startIsolatedProcess(name, strArr, "WebViewLoader-" + abi, abi, 1037, crashHandler)) {
                        throw new Exception("Failed to start the relro file creator process");
                    }
                    return;
                }
            } catch (Throwable t) {
                String str = LOGTAG;
                Log.e(str, "error starting relro file creator for abi " + abi, t);
                crashHandler.run();
                return;
            }
        }
        throw new IllegalArgumentException("Native library paths to the WebView RelRo process must not be null!");
    }

    static int prepareNativeLibraries(PackageInfo webviewPackageInfo) throws WebViewFactory.MissingWebViewPackageException {
        WebViewNativeLibrary nativeLib32bit = getWebViewNativeLibrary(webviewPackageInfo, false);
        WebViewNativeLibrary nativeLib64bit = getWebViewNativeLibrary(webviewPackageInfo, true);
        updateWebViewZygoteVmSize(nativeLib32bit, nativeLib64bit);
        return createRelros(nativeLib32bit, nativeLib64bit);
    }

    private static int createRelros(WebViewNativeLibrary nativeLib32bit, WebViewNativeLibrary nativeLib64bit) {
        int numRelros = 0;
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            if (nativeLib32bit == null) {
                Log.e(LOGTAG, "No 32-bit WebView library path, skipping relro creation.");
            } else {
                createRelroFile(false, nativeLib32bit);
                numRelros = 0 + 1;
            }
        }
        if (Build.SUPPORTED_64_BIT_ABIS.length <= 0) {
            return numRelros;
        }
        if (nativeLib64bit == null) {
            Log.e(LOGTAG, "No 64-bit WebView library path, skipping relro creation.");
            return numRelros;
        }
        createRelroFile(true, nativeLib64bit);
        return numRelros + 1;
    }

    private static void updateWebViewZygoteVmSize(WebViewNativeLibrary nativeLib32bit, WebViewNativeLibrary nativeLib64bit) throws WebViewFactory.MissingWebViewPackageException {
        long newVmSize = 0;
        if (nativeLib32bit != null) {
            newVmSize = Math.max(0, nativeLib32bit.size);
        }
        if (nativeLib64bit != null) {
            newVmSize = Math.max(newVmSize, nativeLib64bit.size);
        }
        long newVmSize2 = Math.max(2 * newVmSize, CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES);
        String str = LOGTAG;
        Log.d(str, "Setting new address space to " + newVmSize2);
        setWebViewZygoteVmSize(newVmSize2);
    }

    static void reserveAddressSpaceInZygote() {
        System.loadLibrary("webviewchromium_loader");
        long addressSpaceToReserve = SystemProperties.getLong(WebViewFactory.CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY, CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES);
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

    @VisibleForTesting
    public static WebViewNativeLibrary getWebViewNativeLibrary(PackageInfo packageInfo, boolean is64bit) throws WebViewFactory.MissingWebViewPackageException {
        ApplicationInfo ai = packageInfo.applicationInfo;
        return findNativeLibrary(ai, WebViewFactory.getWebViewLibrary(ai), is64bit ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS, getWebViewNativeLibraryDirectory(ai, is64bit));
    }

    @VisibleForTesting
    public static String getWebViewNativeLibraryDirectory(ApplicationInfo ai, boolean is64bit) {
        if (is64bit == VMRuntime.is64BitAbi(ai.primaryCpuAbi)) {
            return ai.nativeLibraryDir;
        }
        if (!TextUtils.isEmpty(ai.secondaryCpuAbi)) {
            return ai.secondaryNativeLibraryDir;
        }
        return "";
    }

    private static WebViewNativeLibrary findNativeLibrary(ApplicationInfo ai, String nativeLibFileName, String[] abiList, String libDirectory) throws WebViewFactory.MissingWebViewPackageException {
        if (TextUtils.isEmpty(libDirectory)) {
            return null;
        }
        String libPath = libDirectory + "/" + nativeLibFileName;
        File f = new File(libPath);
        if (f.exists()) {
            return new WebViewNativeLibrary(libPath, f.length());
        }
        return getLoadFromApkPath(ai.sourceDir, abiList, nativeLibFileName);
    }

    private static WebViewNativeLibrary getLoadFromApkPath(String apkPath, String[] abiList, String nativeLibFileName) throws WebViewFactory.MissingWebViewPackageException {
        ZipFile z;
        try {
            z = new ZipFile(apkPath);
            int length = abiList.length;
            int i = 0;
            while (i < length) {
                String abi = abiList[i];
                ZipEntry e = z.getEntry("lib/" + abi + "/" + nativeLibFileName);
                if (e == null || e.getMethod() != 0) {
                    i++;
                } else {
                    WebViewNativeLibrary webViewNativeLibrary = new WebViewNativeLibrary(apkPath + "!/" + entry, e.getSize());
                    z.close();
                    return webViewNativeLibrary;
                }
            }
            z.close();
            return null;
        } catch (IOException e2) {
            throw new WebViewFactory.MissingWebViewPackageException((Exception) e2);
        } catch (Throwable th) {
            r1.addSuppressed(th);
        }
        throw th;
    }

    private static void setWebViewZygoteVmSize(long vmSize) {
        SystemProperties.set(WebViewFactory.CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY, Long.toString(vmSize));
    }
}
