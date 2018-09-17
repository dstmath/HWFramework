package com.android.server.security.securityprofile;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.projection.IMediaProjectionManager.Stub;
import android.media.projection.MediaProjectionInfo;
import android.media.projection.MediaProjectionManager;
import android.media.projection.MediaProjectionManager.Callback;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Slog;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import huawei.android.security.ISecurityProfileService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class SecurityProfileService implements IHwSecurityPlugin {
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            return new SecurityProfileService(context);
        }

        public String getPluginPermission() {
            return SecurityProfileService.MANAGE_SECURITYPROFILE;
        }
    };
    private static final String MANAGE_SECURITYPROFILE = "com.huawei.permission.MANAGE_SECURITYPROFILE";
    private static final int MAX_TASK = 10;
    private static final String TAG = "SecurityProfileService";
    private static final int TOAST_LONG_DELAY = 3500;
    private static Object mAppCategoryLock = new Object();
    private long blackToastTime = 0;
    private CategoryTableHelper categoryTableHelper;
    private final Context mContext;
    private final Handler mHandler = new Handler();
    MediaProjectionStopper mMediaProjectionStopper = null;
    private DatabaseHelper mOpenHelper;
    private Hashtable<String, Integer> mPackageCategory = null;
    private ScreenshotProtector screenshotProtector = new ScreenshotProtector();

    private final class LocalServiceImpl implements SecurityProfileInternal {
        /* synthetic */ LocalServiceImpl(SecurityProfileService this$0, LocalServiceImpl -this1) {
            this();
        }

        private LocalServiceImpl() {
        }

        public boolean shouldPreventInteraction(int type, String targetPackage, int callerUid, int callerPid, String callerPackage, int userId) {
            return SecurityProfileService.this.shouldPreventInteraction(type, targetPackage, callerUid, callerPid, callerPackage, userId);
        }

        public boolean shouldPreventMediaProjection(int uid) {
            return SecurityProfileService.this.shouldPreventMediaProjection(uid);
        }

        public void handleActivityResuming(String packageName) {
            if (packageName != null) {
                SecurityProfileService.this.stopMediaProjectionIfNeeded(packageName);
            }
        }

        public void registerScreenshotProtector(ScreenshotProtectorCallback callback) {
            SecurityProfileService.this.screenshotProtector.register(callback);
        }

        public void unregisterScreenshotProtector(ScreenshotProtectorCallback callback) {
            SecurityProfileService.this.screenshotProtector.unregister(callback);
        }
    }

    private class MediaProjectionStopper {
        private MediaProjectionInfo mActiveProjection = null;
        private MediaProjectionManager mProjectionService = null;

        private class MediaProjectionCallback extends Callback {
            /* synthetic */ MediaProjectionCallback(MediaProjectionStopper this$1, MediaProjectionCallback -this1) {
                this();
            }

            private MediaProjectionCallback() {
            }

            public void onStart(MediaProjectionInfo info) {
                synchronized (MediaProjectionStopper.this) {
                    MediaProjectionStopper.this.mActiveProjection = info;
                }
            }

            public void onStop(MediaProjectionInfo info) {
                synchronized (MediaProjectionStopper.this) {
                    MediaProjectionStopper.this.mActiveProjection = null;
                }
            }
        }

        public MediaProjectionStopper() {
            installCallback();
        }

        private void installCallback() {
            long token = Binder.clearCallingIdentity();
            try {
                if (Stub.asInterface(ServiceManager.getService("media_projection")) != null) {
                    this.mProjectionService = (MediaProjectionManager) SecurityProfileService.this.mContext.getSystemService("media_projection");
                    if (this.mProjectionService != null) {
                        this.mProjectionService.addCallback(new MediaProjectionCallback(this, null), null);
                        synchronized (this) {
                            this.mActiveProjection = this.mProjectionService.getActiveProjectionInfo();
                        }
                    }
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }

        private MediaProjectionInfo getActiveProjectionInfo() {
            MediaProjectionInfo mediaProjectionInfo;
            synchronized (this) {
                mediaProjectionInfo = this.mActiveProjection;
            }
            return mediaProjectionInfo;
        }

        public void stopMediaProjectionIfNeeded(String packageName) {
            try {
                MediaProjectionInfo mpi = getActiveProjectionInfo();
                if (mpi != null) {
                    ScreenshotProtectorCallback callback = SecurityProfileService.this.screenshotProtector.getRegister(packageName);
                    if (callback != null) {
                        String projectionPack = mpi.getPackageName();
                        if (projectionPack != null && !projectionPack.equals(packageName) && !SecurityProfileService.this.isSystemApp(projectionPack)) {
                            SecurityProfileService.this.stopPackages(new String[]{projectionPack});
                            SecurityProfileService.this.screenshotProtector.notifyInfo(callback, projectionPack);
                            this.mProjectionService.stopActiveProjection();
                            Slog.d(SecurityProfileService.TAG, "MediaProjection is stopped by SecurityProfileService.");
                        }
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e2) {
                Slog.e(SecurityProfileService.TAG, "failed to stop MediaProjection.");
            }
        }
    }

    private static class PackageStopper implements Runnable {
        private String[] packageList = null;

        public PackageStopper(String[] packages) {
            this.packageList = packages;
        }

        public void run() {
            if (this.packageList != null) {
                HwActivityManagerService am = HwActivityManagerService.self();
                if (am != null) {
                    for (String packageName : this.packageList) {
                        Slog.d(SecurityProfileService.TAG, packageName + " is stopped by SecurityProfileService.");
                        am.forceStopPackage(packageName, -1);
                    }
                }
            }
        }
    }

    private static class ScreenshotProtector {
        private final Object mLock;
        private List<ScreenshotProtectorCallback> screenshotList;

        /* synthetic */ ScreenshotProtector(ScreenshotProtector -this0) {
            this();
        }

        private ScreenshotProtector() {
            this.screenshotList = new ArrayList();
            this.mLock = new Object();
        }

        public ScreenshotProtectorCallback getRegister(String app) {
            synchronized (this.mLock) {
                for (ScreenshotProtectorCallback callback : this.screenshotList) {
                    if (callback.isProtectedApp(app)) {
                        return callback;
                    }
                }
                return null;
            }
        }

        public void notifyInfo(ScreenshotProtectorCallback callback, String projectionApp) {
            synchronized (this.mLock) {
                if (callback.isActive()) {
                    callback.notifyInfo(projectionApp);
                }
            }
        }

        public void register(ScreenshotProtectorCallback callback) {
            if (callback != null) {
                synchronized (this.mLock) {
                    callback.setActiveStatus(true);
                    this.screenshotList.add(callback);
                }
            }
        }

        public void unregister(ScreenshotProtectorCallback callback) {
            if (callback != null) {
                synchronized (this.mLock) {
                    callback.setActiveStatus(false);
                    this.screenshotList.remove(callback);
                }
            }
        }
    }

    private class SecurityProfileServiceImpl extends ISecurityProfileService.Stub {
        /* synthetic */ SecurityProfileServiceImpl(SecurityProfileService this$0, SecurityProfileServiceImpl -this1) {
            this();
        }

        private SecurityProfileServiceImpl() {
        }

        public void updateBlackApp(List packageName, int action) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            switch (action) {
                case 1:
                    SecurityProfileService.this.updateBlackApp(packageName);
                    return;
                case 2:
                    SecurityProfileService.this.addBlackApp(packageName);
                    return;
                case 3:
                    SecurityProfileService.this.removeBlackApp(packageName);
                    return;
                default:
                    return;
            }
        }

        public boolean isBlackApp(String packageName) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            return SecurityProfileService.this.isBlackApp(packageName);
        }
    }

    public SecurityProfileService(Context context) {
        this.mContext = context;
    }

    public SecurityProfileInternal getLocalServiceImpl() {
        return new LocalServiceImpl(this, null);
    }

    public void onStart() {
        this.mOpenHelper = new DatabaseHelper(this.mContext);
        this.categoryTableHelper = new CategoryTableHelper(this.mOpenHelper);
        this.mPackageCategory = this.categoryTableHelper.readDatabase();
        if (this.mMediaProjectionStopper == null) {
            this.mMediaProjectionStopper = new MediaProjectionStopper();
        }
        LocalServices.addService(SecurityProfileInternal.class, getLocalServiceImpl());
    }

    public void onStop() {
    }

    public IBinder asBinder() {
        return new SecurityProfileServiceImpl(this, null);
    }

    private int determinePackageCategory(String packageName) {
        Integer category = (Integer) this.mPackageCategory.get(packageName);
        if (category != null) {
            return category.intValue();
        }
        return 0;
    }

    public void addBlackApp(List<String> packageList) {
        if (packageList != null && this.categoryTableHelper != null) {
            synchronized (mAppCategoryLock) {
                for (String packageName : packageList) {
                    this.mPackageCategory.put(packageName, Integer.valueOf(1));
                }
                this.categoryTableHelper.storeCategoryToDatabase(packageList, 1);
            }
        }
    }

    public void removeBlackApp(List<String> packageList) {
        if (packageList != null && this.categoryTableHelper != null) {
            synchronized (mAppCategoryLock) {
                for (String packageName : packageList) {
                    if (isBlackApp(packageName)) {
                        this.mPackageCategory.remove(packageName);
                    }
                }
                this.categoryTableHelper.removeCategoryFromDatabase(packageList, 1);
            }
        }
    }

    public void updateBlackApp(List<String> packageList) {
        if (packageList != null && this.categoryTableHelper != null) {
            synchronized (mAppCategoryLock) {
                this.mPackageCategory.clear();
                for (String packageName : packageList) {
                    this.mPackageCategory.put(packageName, Integer.valueOf(1));
                }
                this.categoryTableHelper.eraseBlacklistedFromDatabase(1);
                this.categoryTableHelper.storeCategoryToDatabase(packageList, 1);
            }
        }
    }

    public boolean isBlackApp(String packageName) {
        if (determinePackageCategory(packageName) == 1) {
            return true;
        }
        return false;
    }

    public boolean shouldPreventMediaProjection(int uid) {
        if (uid < 10000) {
            return false;
        }
        String[] projectionPackages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (projectionPackages == null || projectionPackages.length == 0) {
            return false;
        }
        Set<String> projectionSet = new HashSet();
        for (String pack : projectionPackages) {
            projectionSet.add(pack);
        }
        HwActivityManagerService hwAm = HwActivityManagerService.self();
        if (hwAm == null) {
            return false;
        }
        for (RunningTaskInfo taskInfo : hwAm.getTasks(10, 0)) {
            String foreground = taskInfo.topActivity == null ? null : taskInfo.topActivity.getPackageName();
            if (!(foreground == null || projectionSet.contains(foreground) || !hwAm.isTaskVisible(taskInfo.id))) {
                ScreenshotProtectorCallback callback = this.screenshotProtector.getRegister(foreground);
                if (callback != null) {
                    stopPackages(projectionPackages);
                    this.screenshotProtector.notifyInfo(callback, projectionPackages[0]);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldPreventInteraction(int type, String targetPackage, int callerUid, int callerPid, String callerPackage, int userId) {
        if (targetPackage == null) {
            return false;
        }
        if (isBlackApp(targetPackage)) {
            if (type == 0 && isLauncherApp(callerPackage)) {
                long curTime = System.currentTimeMillis();
                if (curTime < this.blackToastTime || curTime - this.blackToastTime > 3500) {
                    this.blackToastTime = curTime;
                    String text = this.mContext.getResources().getString(33686072);
                    if (text != null) {
                        showToast(text);
                    }
                }
            }
            Slog.d(TAG, targetPackage + " is not allowed to start.");
            return true;
        }
        if (type == 0) {
            stopMediaProjectionIfNeeded(targetPackage);
        }
        return false;
    }

    private boolean isLauncherApp(String target) {
        PackageManager manager = this.mContext.getPackageManager();
        if (manager == null) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfo = manager.resolveActivity(intent, 65536);
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return false;
        }
        String launcher = resolveInfo.activityInfo.packageName;
        if (launcher == null || (launcher.equals(target) ^ 1) != 0) {
            return false;
        }
        return true;
    }

    public void stopMediaProjectionIfNeeded(String packageName) {
        this.mMediaProjectionStopper.stopMediaProjectionIfNeeded(packageName);
    }

    private void showToast(final String text) {
        UiThread.getHandler().post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(SecurityProfileService.this.mContext, text, 1);
                LayoutParams windowParams = toast.getWindowParams();
                windowParams.privateFlags |= 16;
                toast.show();
            }
        });
    }

    private void stopPackages(String[] packageNames) {
        this.mHandler.post(new PackageStopper(packageNames));
    }

    private boolean isSystemApp(String pack) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(pack, 0);
            if (info == null) {
                return false;
            }
            if (info.uid < 10000 || (info.flags & 1) != 0) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "can not check if " + pack + " is a system app");
        }
    }
}
