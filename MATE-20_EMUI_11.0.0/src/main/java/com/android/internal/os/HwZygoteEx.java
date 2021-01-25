package com.android.internal.os;

import android.app.ActivityThread;
import android.app.ApplicationLoaders;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.res.CompatibilityInfo;
import android.net.LocalSocket;
import android.os.Parcel;
import android.os.Process;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import libcore.io.IoUtils;

public class HwZygoteEx implements IHwZygoteEx {
    private static final String APACHE_HTTP_JAR_STR = "/system/framework/org.apache.http.legacy.jar";
    private static final String APACHE_HTTP_STR = "org.apache.http.legacy";
    private static final int ERROR_NUM = -1;
    private static final String NAME_USAP = "USAP";
    private static HwZygoteEx sInstance;

    public static synchronized HwZygoteEx getDefault() {
        HwZygoteEx hwZygoteEx;
        synchronized (HwZygoteEx.class) {
            if (sInstance == null) {
                sInstance = new HwZygoteEx();
            }
            hwZygoteEx = sInstance;
        }
        return hwZygoteEx;
    }

    private boolean isApacheUsedByApp(List<SharedLibraryInfo> sharedLibraries) {
        if (sharedLibraries == null) {
            return false;
        }
        for (SharedLibraryInfo info : sharedLibraries) {
            if (APACHE_HTTP_STR.equals(info.getName()) && info.isBuiltin() && info.getLongVersion() == -1) {
                return true;
            }
        }
        return false;
    }

    private void cacheLibrariesClassLoaders(ApplicationInfo applicationInfo) {
        if (applicationInfo != null && isApacheUsedByApp(applicationInfo.sharedLibraryInfos)) {
            ApplicationLoaders.getDefault().addAndCacheNonBootclasspathSystemClassLoaders(new SharedLibraryInfo(APACHE_HTTP_JAR_STR, null, null, null, 0, 0, null, null, null));
        }
    }

    private static ApplicationInfo decodeApplicationInfo(String rawAppInfo) {
        byte[] rawParcelData = Base64.getDecoder().decode(rawAppInfo);
        Parcel appInfoParcel = Parcel.obtain();
        appInfoParcel.unmarshall(rawParcelData, 0, rawParcelData.length);
        appInfoParcel.setDataPosition(0);
        ApplicationInfo appInfo = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(appInfoParcel);
        appInfoParcel.recycle();
        return appInfo;
    }

    public ZygoteArguments preloadApplication(String rawAppInfo, BufferedReader usapReader, DataOutputStream usapOutputStream, LocalSocket sessionSocket) {
        int pid = Process.myPid();
        ApplicationInfo appInfo = decodeApplicationInfo(rawAppInfo);
        ZygoteArguments preLoadArgs = null;
        Trace.setTracingEnabled(true, 0);
        if (appInfo == null) {
            return null;
        }
        try {
            usapOutputStream.writeInt(pid);
            cacheLibrariesClassLoaders(appInfo);
            String[] argStrings = Zygote.readArgumentList(usapReader);
            if (argStrings != null) {
                preLoadArgs = new ZygoteArguments(argStrings);
                if (preLoadArgs.mPreloadExit) {
                    IoUtils.closeQuietly(sessionSocket);
                    Log.i(NAME_USAP, "Usap exit!");
                    System.exit(-1);
                }
            } else {
                Log.i(NAME_USAP, "Truncated command received.");
                IoUtils.closeQuietly(sessionSocket);
                System.exit(-1);
            }
        } catch (IOException ioEx) {
            Log.e(NAME_USAP, "Failed to write response to session socket: " + ioEx.getMessage());
            System.exit(-1);
        }
        return preLoadArgs;
    }

    public void handlePreloadApp(String rawAppInfo) {
        final ApplicationInfo appInfo = decodeApplicationInfo(rawAppInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(appInfo.seInfo);
        sb.append(TextUtils.isEmpty(appInfo.seInfoUser) ? StorageManagerExt.INVALID_KEY_DESC : appInfo.seInfoUser);
        sb.toString();
        new Thread(new Runnable() {
            /* class com.android.internal.os.HwZygoteEx.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Trace.traceBegin(64, "handlePreload");
                new LoadedApk((ActivityThread) null, appInfo, (CompatibilityInfo) null, (ClassLoader) null, false, true, false).getClassLoader();
                Trace.traceEnd(64);
            }
        }).start();
    }
}
