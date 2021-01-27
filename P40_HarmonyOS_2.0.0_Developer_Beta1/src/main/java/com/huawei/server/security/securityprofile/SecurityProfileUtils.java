package com.huawei.server.security.securityprofile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.android.widget.ToastEx;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.UiThreadEx;
import com.huawei.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.util.LogEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public final class SecurityProfileUtils {
    static final boolean DEBUG = LogEx.getLogHWInfo();
    private static final int DEFAULT_PACKAGES_MULTI_USERS_CAPACITY = 128;
    private static final int DEFAULT_USERS_CAPACITY = 8;
    static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    private static final Object LOCK = new Object();
    private static final String TAG = "SecurityProfileUtils";
    private static volatile ThreadPoolExecutor sWatcherThreadPool;
    private static volatile ThreadPoolExecutor sWorkerThreadPool;

    SecurityProfileUtils() {
    }

    @NonNull
    static List<Integer> getUserIdListOnPhone(@NonNull Context context) {
        List<Integer> userIdList = new ArrayList<>(8);
        UserManager userManager = (UserManager) context.getSystemService("user");
        if (userManager == null) {
            Log.w(TAG, "get user manager null, I must use default user id");
            userIdList.add(0);
            return userIdList;
        }
        for (UserInfoExt userInfo : UserManagerExt.getUsers(userManager, false)) {
            userIdList.add(Integer.valueOf(userInfo.getUserId()));
        }
        if (userIdList.isEmpty()) {
            Log.w(TAG, "get user id list size 0, I must use default user id");
            userIdList.add(0);
        }
        return userIdList;
    }

    @NonNull
    static List<String> getInstalledPackages(@NonNull Context context) {
        Set<String> tempSet = new HashSet<>(128);
        List<Integer> userIdList = getUserIdListOnPhone(context);
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "get package manager null, when get installed packages");
            return Collections.emptyList();
        }
        for (Integer num : userIdList) {
            for (PackageInfo info : PackageManagerExt.getInstalledPackagesAsUser(pm, 0, num.intValue())) {
                tempSet.add(info.packageName);
            }
        }
        return new ArrayList(tempSet);
    }

    @Nullable
    static String getInstalledApkPath(@NonNull String packageName, @NonNull Context context) {
        List<Integer> userIdList = getUserIdListOnPhone(context);
        PackageManager pm = context.getPackageManager();
        String str = null;
        if (pm == null) {
            Log.w(TAG, "get package manager null, when get installed Apk path");
            return null;
        }
        int len = userIdList.size();
        for (int i = 0; i < len; i++) {
            try {
                return PackageManagerExt.getApplicationInfoAsUser(pm, packageName, 0, userIdList.get(i).intValue()).sourceDir;
            } catch (PackageManager.NameNotFoundException e) {
                if (DEBUG) {
                    Log.d(TAG, "getInstalledApkPath name not found, packageName: " + packageName + ", index: " + i + " in length: " + len);
                }
            }
        }
        return str;
    }

    static boolean isAccessibilitySelectToSpeakActive(@NonNull Context context) {
        return isAccessibilitySelectToSpeakActive(context, getCurrentActiveUserId());
    }

    static boolean isAccessibilitySelectToSpeakActive(@NonNull Context context, int userId) {
        String enabledServicesSetting = SettingsEx.Secure.getStringForUser(context.getContentResolver(), "enabled_accessibility_services", userId);
        if (enabledServicesSetting == null || !enabledServicesSetting.contains("SelectToSpeakService")) {
            return false;
        }
        return true;
    }

    static int getCurrentActiveUserId() {
        return ActivityManagerEx.getCurrentUser();
    }

    static boolean isSystemApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (appInfo == null) {
                return false;
            }
            if (appInfo.uid < 10000 || (appInfo.flags & 1) != 0) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "can not check if " + packageName + " is a system app");
        }
    }

    static boolean isLauncherApp(Context context, String target) {
        String launcher;
        PackageManager manager = context.getPackageManager();
        if (manager == null) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfo = manager.resolveActivity(intent, 65536);
        if (resolveInfo == null || resolveInfo.activityInfo == null || (launcher = resolveInfo.activityInfo.packageName) == null || !launcher.equals(target)) {
            return false;
        }
        return true;
    }

    static void showToast(final Context context, final String text) {
        UiThreadEx.getHandler().post(new Runnable() {
            /* class com.huawei.server.security.securityprofile.SecurityProfileUtils.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Toast toast = Toast.makeText(context, text, 1);
                ToastEx.setWindowParamsPrivateFlags(toast, WindowManagerEx.LayoutParamsEx.getPrivateFlags(ToastEx.getWindowParams(toast)) | WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
                toast.show();
            }
        });
    }

    static String replaceLineSeparator(String outerPackageName) {
        return outerPackageName.replaceAll(System.lineSeparator(), BuildConfig.FLAVOR);
    }

    @NonNull
    static ThreadPoolExecutor getWorkerThreadPool() {
        if (sWorkerThreadPool == null) {
            synchronized (LOCK) {
                if (sWorkerThreadPool == null) {
                    sWorkerThreadPool = getThreadPoolImpl();
                }
            }
        }
        return sWorkerThreadPool;
    }

    private static ThreadPoolExecutor getThreadPoolImpl() {
        return new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() + 8, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>((int) HwSecDiagnoseConstant.BIT_SETIDS), new RejectedExecutionHandler() {
            /* class com.huawei.server.security.securityprofile.SecurityProfileUtils.AnonymousClass2 */

            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                Log.w(SecurityProfileUtils.TAG, "Thread pool and queue is full, dropped #runnable: " + runnable.toString());
            }
        });
    }

    static ThreadPoolExecutor createSingleWatcherThreadPool() {
        return new ThreadPoolExecutor(0, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new RejectedExecutionHandler() {
            /* class com.huawei.server.security.securityprofile.SecurityProfileUtils.AnonymousClass3 */

            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                Log.i(SecurityProfileUtils.TAG, "Single watcher thread pool and queue is full.");
            }
        });
    }
}
