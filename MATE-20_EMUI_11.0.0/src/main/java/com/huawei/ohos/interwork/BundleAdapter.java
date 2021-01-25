package com.huawei.ohos.interwork;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ohos.appexecfwk.utils.AppConstants;
import ohos.bundle.BundleManager;
import ohos.bundle.IBundleInstaller;
import ohos.bundle.InstallParam;
import ohos.bundle.InstallerCallback;
import ohos.rpc.RemoteException;

public class BundleAdapter {
    private static final String APK_SUFFIX = ".apk";
    private static final String ENTRY_APK = "entry.apk";
    public static final int FLAG_INSTALL_DEFAULT = 0;
    private static final String HAP_SUFFIX = ".hap";
    public static final int HARMONY_STATUS_INSTALL_FAILURE = 1;
    public static final int HARMONY_STATUS_INSTALL_FAILURE_ABORTED = 2;
    public static final int HARMONY_STATUS_INSTALL_FAILURE_CONFLICT = 4;
    public static final int HARMONY_STATUS_INSTALL_FAILURE_INCOMPATIBLE = 6;
    public static final int HARMONY_STATUS_INSTALL_FAILURE_INVALID = 3;
    public static final int HARMONY_STATUS_INSTALL_FAILURE_STORAGE = 5;
    public static final int HARMONY_STATUS_UNINSTALL_FAILURE = 7;
    public static final int HARMONY_STATUS_UNINSTALL_FAILURE_ABORTED = 9;
    public static final int HARMONY_STATUS_UNINSTALL_FAILURE_BLOCKED = 8;
    public static final int HARMONY_STATUS_UNINSTALL_FAILURE_CONFLICT = 10;
    private static final int MAX_BUFFER_SIZE = 65536;
    private static final long MAX_FILE_SIZE = 4294967296L;
    private static final String SHELL_APK = "shell.apk";
    public static final int SUCCESS = 0;
    private static final String TAG = "BundleAdapter";

    public interface InstallBundleCallBack {
        void onFinished(int i, String str);
    }

    static {
        try {
            Log.i(TAG, "loadLibrary ipc_core.z begin");
            System.loadLibrary("ipc_core.z");
            Log.i(TAG, "loadLibrary ipc_core.z end");
        } catch (UnsatisfiedLinkError unused) {
            Log.e(TAG, "UnsatisfiedLinkError");
        }
    }

    public static boolean install(ArrayList<String> arrayList, int i, InstallBundleCallBack installBundleCallBack) {
        Log.i(TAG, "install by apk");
        InstallParam installParam = new InstallParam();
        installParam.setInstallFlag(i);
        InstallerCallback innerCallback = getInnerCallback(installBundleCallBack);
        IBundleInstaller bundleInstaller = getBundleInstaller();
        if (bundleInstaller == null) {
            Log.w(TAG, "installer is null");
            return false;
        }
        try {
            return bundleInstaller.install(arrayList, installParam, innerCallback);
        } catch (RemoteException unused) {
            Log.e(TAG, "RemoteException");
            return false;
        }
    }

    public static void uninstall(String str, int i, InstallBundleCallBack installBundleCallBack) {
        Log.i(TAG, "uninstall by apk");
        InstallParam installParam = new InstallParam();
        installParam.setInstallFlag(i);
        InstallerCallback innerCallback = getInnerCallback(installBundleCallBack);
        IBundleInstaller bundleInstaller = getBundleInstaller();
        if (bundleInstaller == null) {
            Log.w(TAG, "installer is null");
            return;
        }
        try {
            bundleInstaller.uninstall(str, installParam, innerCallback);
        } catch (RemoteException unused) {
            Log.e(TAG, "RemoteException");
        }
    }

    private static InstallerCallback getInnerCallback(final InstallBundleCallBack installBundleCallBack) {
        return new InstallerCallback() {
            /* class com.huawei.ohos.interwork.BundleAdapter.AnonymousClass1 */

            @Override // ohos.bundle.InstallerCallback, ohos.bundle.IInstallerCallback
            public void onFinished(int i, String str) {
                InstallBundleCallBack installBundleCallBack = InstallBundleCallBack.this;
                if (installBundleCallBack != null) {
                    installBundleCallBack.onFinished(i, str);
                }
            }
        };
    }

    private static IBundleInstaller getBundleInstaller() {
        try {
            BundleManager instance = BundleManager.getInstance();
            if (instance != null) {
                return instance.getBundleInstaller();
            }
            Log.w(TAG, "getBundleInstaller bundleMgr is null");
            return null;
        } catch (RemoteException unused) {
            Log.e(TAG, "RemoteException");
            return null;
        }
    }

    public static PackageInfo getPackageInfoForHap(Context context, String str, int i) {
        Log.i(TAG, "getPackageInfoForHap begin");
        if (context == null) {
            Log.e(TAG, "getPackageInfoForHap context is null");
            return null;
        } else if (str == null || !str.toLowerCase(Locale.ENGLISH).endsWith(HAP_SUFFIX)) {
            Log.e(TAG, "invalid hap file");
            return null;
        } else {
            File file = new File(str);
            if (file.exists() && file.isFile() && file.canRead() && file.length() != 0) {
                return getPackageInfoForHapInner(context, file, i);
            }
            Log.e(TAG, "getPackageInfoForHap, file not exist or invalid.");
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00e3  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00e8  */
    private static PackageInfo getPackageInfoForHapInner(Context context, File file, int i) {
        FileOutputStream fileOutputStream;
        Throwable th;
        ZipInputStream zipInputStream;
        File file2;
        try {
            String apkPath = getApkPath(file.getCanonicalPath(), SHELL_APK);
            zipInputStream = new ZipInputStream(new FileInputStream(file));
            fileOutputStream = null;
            file2 = null;
            while (true) {
                try {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry != null) {
                        if (!nextEntry.isDirectory()) {
                            if (nextEntry.getName().toLowerCase(Locale.ENGLISH).endsWith(APK_SUFFIX)) {
                                if (fileOutputStream != null && nextEntry.getName().toLowerCase(Locale.ENGLISH).endsWith(ENTRY_APK)) {
                                    Log.w(TAG, "getPackageInfoForHapInner, only parse feature apk");
                                    break;
                                }
                                if (fileOutputStream != null) {
                                    safeCloseStream(fileOutputStream);
                                    safeDeleteFile(file2);
                                }
                                File file3 = new File(apkPath);
                                try {
                                    apkPath = file3.getCanonicalPath();
                                    FileOutputStream fileOutputStream2 = new FileOutputStream(file3);
                                    try {
                                        byte[] bArr = new byte[65536];
                                        long j = 0;
                                        while (true) {
                                            int read = zipInputStream.read(bArr);
                                            if (read == -1) {
                                                fileOutputStream2.flush();
                                                fileOutputStream = fileOutputStream2;
                                                file2 = file3;
                                                break;
                                            }
                                            j += (long) read;
                                            if (j > MAX_FILE_SIZE) {
                                                Log.w(TAG, "getPackageInfoForHapInner, readSizeCount: " + j);
                                                safeDeleteFile(file3);
                                                safeCloseStream(zipInputStream);
                                                safeCloseStream(fileOutputStream2);
                                                return null;
                                            }
                                            fileOutputStream2.write(bArr, 0, read);
                                        }
                                    } catch (IOException unused) {
                                        fileOutputStream = fileOutputStream2;
                                        file2 = file3;
                                        try {
                                            Log.w(TAG, "getPackageInfoForHapInner exception");
                                            safeDeleteFile(file2);
                                            if (zipInputStream != null) {
                                            }
                                            if (fileOutputStream != null) {
                                            }
                                            return null;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            if (zipInputStream != null) {
                                                safeCloseStream(zipInputStream);
                                            }
                                            if (fileOutputStream != null) {
                                                safeCloseStream(fileOutputStream);
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        fileOutputStream = fileOutputStream2;
                                        if (zipInputStream != null) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        throw th;
                                    }
                                } catch (IOException unused2) {
                                    file2 = file3;
                                    Log.w(TAG, "getPackageInfoForHapInner exception");
                                    safeDeleteFile(file2);
                                    if (zipInputStream != null) {
                                        safeCloseStream(zipInputStream);
                                    }
                                    if (fileOutputStream != null) {
                                        safeCloseStream(fileOutputStream);
                                    }
                                    return null;
                                }
                            }
                        }
                    } else {
                        break;
                    }
                } catch (IOException unused3) {
                    Log.w(TAG, "getPackageInfoForHapInner exception");
                    safeDeleteFile(file2);
                    if (zipInputStream != null) {
                    }
                    if (fileOutputStream != null) {
                    }
                    return null;
                }
            }
            safeCloseStream(zipInputStream);
            if (fileOutputStream != null) {
                safeCloseStream(fileOutputStream);
            }
            PackageInfo packageArchiveInfo = getPackageArchiveInfo(context, apkPath, i);
            safeDeleteFile(file2);
            Log.i(TAG, "getPackageInfoForHap end");
            return packageArchiveInfo;
        } catch (IOException unused4) {
            fileOutputStream = null;
            zipInputStream = null;
            file2 = null;
            Log.w(TAG, "getPackageInfoForHapInner exception");
            safeDeleteFile(file2);
            if (zipInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            return null;
        } catch (Throwable th4) {
            th = th4;
            fileOutputStream = null;
            zipInputStream = null;
            if (zipInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            throw th;
        }
    }

    private static String getApkPath(String str, String str2) {
        int lastIndexOf = str.lastIndexOf(47);
        if (lastIndexOf == -1) {
            Log.e(TAG, "getApkPath, index is error");
            return "";
        }
        String str3 = str.substring(0, lastIndexOf + 1) + str2;
        Log.d(TAG, "getApkPath, apkPath: " + str3);
        return str3;
    }

    private static PackageInfo getPackageArchiveInfo(Context context, String str, int i) {
        if (str.isEmpty()) {
            Log.e(TAG, "getPackageArchiveInfo apk path is empty");
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            return packageManager.getPackageArchiveInfo(str, i);
        }
        Log.w(TAG, "getPackageArchiveInfo pm is null");
        return null;
    }

    private static void safeDeleteFile(File file) {
        if (file == null) {
            Log.w(TAG, "safeDeleteFile failed, apkFile is null");
            return;
        }
        try {
            boolean delete = file.delete();
            Log.i(TAG, "safeDeleteFile, delete: " + delete);
        } catch (SecurityException e) {
            Log.e(TAG, "safeDeleteFile failed, exception: " + e.getMessage());
        }
    }

    private static void safeCloseStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                Log.w(TAG, "safeCloseStream failure");
            }
        }
    }

    public static Intent getLaunchIntentForBundle(String str) {
        try {
            BundleManager instance = BundleManager.getInstance();
            if (instance == null) {
                Log.w(TAG, "getLaunchIntentForBundle bundleMgr is null");
                return null;
            }
            ohos.aafwk.content.Intent launchIntentForBundle = instance.getLaunchIntentForBundle(str);
            if (launchIntentForBundle != null) {
                if (launchIntentForBundle.getElement() != null) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.addCategory("android.intent.category.LAUNCHER");
                    intent.setPackage(launchIntentForBundle.getElement().getBundleName());
                    intent.setFlags(268435456);
                    intent.setClassName(launchIntentForBundle.getElement().getBundleName(), launchIntentForBundle.getElement().getAbilityName() + AppConstants.SHELL_ACTIVITY_SUFFIX);
                    return intent;
                }
            }
            Log.w(TAG, "getLaunchIntentForBundle failure");
            return null;
        } catch (RemoteException unused) {
            Log.e(TAG, "RemoteException");
            return null;
        }
    }
}
