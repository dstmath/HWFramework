package com.android.server.security.securityprofile;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.projection.IMediaProjectionManager;
import android.media.projection.MediaProjectionInfo;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.securityprofile.PolicyEngine;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.HwActivityManager;
import huawei.android.security.ISecurityProfileService;
import huawei.android.security.securityprofile.ApkDigest;
import huawei.android.security.securityprofile.DigestMatcher;
import huawei.android.security.securityprofile.PolicyExtractor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

public class SecurityProfileService implements IHwSecurityPlugin {
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            return new SecurityProfileService(context);
        }

        public String getPluginPermission() {
            return SecurityProfileService.MANAGE_SECURITYPROFILE;
        }
    };
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final String MANAGE_SECURITYPROFILE = "com.huawei.permission.MANAGE_SECURITYPROFILE";
    private static final int MAX_TASK = 10;
    private static final boolean SUPPORT_HW_SEAPP = "true".equalsIgnoreCase(SystemProperties.get("ro.config.support_iseapp", "false"));
    private static final String TAG = "SecurityProfileService";
    private static final int TOAST_LONG_DELAY = 3500;
    /* access modifiers changed from: private */
    public ActivityManager activityManager;
    private long blackToastTime = 0;
    /* access modifiers changed from: private */
    public final Context mContext;
    BroadcastReceiver mDefaultUserBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Slog.i(SecurityProfileService.TAG, "receive Intent.ACTION_BOOT_COMPLETED");
                if (!SecurityProfileService.this.mPolicyEngine.isNeedPolicyRecover()) {
                    Slog.i(SecurityProfileService.TAG, "not need to recover policy,do not verify all installed package");
                } else {
                    new Thread(new VerifyInstalledPackagesRunnable(context, SecurityProfileUtils.getInstalledPackages(context))).start();
                }
            }
        }
    };
    private final Handler mHandler = new Handler();
    MediaProjectionStopper mMediaProjectionStopper = null;
    BroadcastReceiver mMultiUserBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
                Uri uri = intent.getData();
                if (uri != null) {
                    String packageName = uri.getSchemeSpecificPart();
                    SecurityProfileService.this.mPolicyEngine.updatePackageInformation(packageName);
                    if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                        InstallerDataBase.getInstance().setInstallerPackageName(context, packageName);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public PolicyEngine mPolicyEngine = null;
    /* access modifiers changed from: private */
    public ScreenshotProtector screenshotProtector = new ScreenshotProtector();

    private final class LocalServiceImpl implements SecurityProfileInternal {
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
                if (!SecurityProfileService.this.mPolicyEngine.requestAccessWithExtraLabel("", packageName, SecurityProfileService.this.getExtraObjectLabel(packageName), "MediaProjection", "Record", 0)) {
                    Slog.w(SecurityProfileService.TAG, "handleActivityResuming packageName:" + packageName + " is Media protect app,need stop Media Projection");
                    SecurityProfileService.this.mMediaProjectionStopper.stopMediaProjection();
                }
            }
        }

        public void registerScreenshotProtector(ScreenshotProtectorCallback callback) {
            SecurityProfileService.this.screenshotProtector.register(callback);
        }

        public void unregisterScreenshotProtector(ScreenshotProtectorCallback callback) {
            SecurityProfileService.this.screenshotProtector.unregister(callback);
        }

        public boolean verifyPackage(String packageName, String path) {
            long beginTime = System.currentTimeMillis();
            Slog.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]verifyPackage begin:" + beginTime);
            boolean result = SecurityProfileService.this.verifyPackage(packageName, path);
            long endTime = System.currentTimeMillis();
            Slog.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]verifyPackage end:" + endTime + ", Total usage:" + (endTime - beginTime) + "ms");
            return result;
        }
    }

    private class MediaProjectionStopper {
        public MediaProjectionInfo mActiveProjection = null;
        private MediaProjectionManager mProjectionService = null;
        public String mProtectedPackage = null;

        private class MediaProjectionCallback extends MediaProjectionManager.Callback {
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
                if (IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection")) != null) {
                    this.mProjectionService = (MediaProjectionManager) SecurityProfileService.this.mContext.getSystemService("media_projection");
                    if (this.mProjectionService != null) {
                        this.mProjectionService.addCallback(new MediaProjectionCallback(), null);
                        synchronized (this) {
                            this.mActiveProjection = this.mProjectionService.getActiveProjectionInfo();
                        }
                    }
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        private MediaProjectionInfo getActiveProjectionInfo() {
            MediaProjectionInfo mediaProjectionInfo;
            synchronized (this) {
                mediaProjectionInfo = this.mActiveProjection;
            }
            return mediaProjectionInfo;
        }

        public void stopMediaProjection() {
            try {
                MediaProjectionInfo mpi = getActiveProjectionInfo();
                if (mpi != null) {
                    String projectionPack = mpi.getPackageName();
                    if (projectionPack == null) {
                        Slog.w(SecurityProfileService.TAG, "stopMediaProjection, but get MediaProjectionInfo without packageName");
                    } else if (SecurityProfileService.this.isSystemApp(projectionPack)) {
                        Slog.d(SecurityProfileService.TAG, "not stopMediaProjection system App: " + projectionPack);
                    } else {
                        SecurityProfileService.this.stopPackages(new String[]{projectionPack});
                        if (this.mProtectedPackage != null) {
                            ScreenshotProtectorCallback callback = SecurityProfileService.this.screenshotProtector.getRegister(this.mProtectedPackage);
                            if (callback != null) {
                                SecurityProfileService.this.screenshotProtector.notifyInfo(callback, projectionPack);
                            }
                            this.mProtectedPackage = null;
                        }
                        this.mProjectionService.stopActiveProjection();
                        Slog.d(SecurityProfileService.TAG, "MediaProjection is stopped by SecurityProfileService.");
                        this.mActiveProjection = null;
                        this.mProjectionService.stopActiveProjection();
                    }
                }
            } catch (RuntimeException runtimeException) {
                Slog.e(SecurityProfileService.TAG, "RuntimeException failed to stop MediaProjection: " + runtimeException.getMessage());
                throw runtimeException;
            } catch (Exception ignore) {
                Slog.e(SecurityProfileService.TAG, "failed to stop MediaProjection: " + ignore.getMessage());
            }
        }
    }

    private class PackageStopper implements Runnable {
        private String[] packageList = null;

        public PackageStopper(String[] packages) {
            this.packageList = packages;
        }

        public void run() {
            if (!(this.packageList == null || SecurityProfileService.this.activityManager == null)) {
                for (String packageName : this.packageList) {
                    Slog.d(SecurityProfileService.TAG, packageName + " is stopped by SecurityProfileService.");
                    SecurityProfileService.this.activityManager.forceStopPackageAsUser(packageName, -1);
                }
            }
        }
    }

    private static class ScreenshotProtector {
        private final Object mLock;
        private List<ScreenshotProtectorCallback> screenshotList;

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
        private static final int RESULT_CODE_ADD_POLICY_FAIL = 1;
        private static final int RESULT_CODE_ADD_POLICY_SUCC = 0;

        private SecurityProfileServiceImpl() {
        }

        public void updateBlackApp(List packageName, int action) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            switch (action) {
                case 1:
                    SecurityProfileService.this.mPolicyEngine.updateBlackApp(packageName);
                    return;
                case 2:
                    SecurityProfileService.this.mPolicyEngine.addBlackApp(packageName);
                    return;
                case 3:
                    SecurityProfileService.this.mPolicyEngine.removeBlackApp(packageName);
                    return;
                default:
                    return;
            }
        }

        public boolean isBlackApp(String packageName) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            return SecurityProfileService.this.mPolicyEngine.isBlackApp(packageName);
        }

        public int addDomainPolicy(byte[] domainPolicy) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            JSONObject policy = PolicyVerifier.verifyAndDecodePolicy(domainPolicy);
            if (policy == null) {
                return 1;
            }
            SecurityProfileService.this.mPolicyEngine.addPolicy(policy);
            return 0;
        }

        public boolean requestAccess(String subject, String object, String subsystem, List<String> list, String operation, int timeout) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            return SecurityProfileService.this.mPolicyEngine.requestAccess(subject, object, subsystem, operation, timeout);
        }

        public boolean checkAccess(String subject, String object, String subsystem, List<String> list, String operation) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            return SecurityProfileService.this.mPolicyEngine.checkAccess(subject, object, subsystem, operation);
        }

        public List<String> getLabels(String packageName, ApkDigest digest) {
            return SecurityProfileService.this.mPolicyEngine.getLabels(packageName, digest);
        }

        public boolean isPackageSigned(String packageName) {
            return SecurityProfileService.this.mPolicyEngine.isPackageSigned(packageName);
        }

        public String getInstallerPackageName(String packageName) {
            return InstallerDataBase.getInstance().getInstaller(SecurityProfileService.this.mContext, packageName);
        }
    }

    private class VerifyInstalledPackagesRunnable implements Runnable {
        Context context;
        List<String> packageNameList;

        public VerifyInstalledPackagesRunnable(Context context2, List<String> inPackageNameList) {
            this.packageNameList = inPackageNameList;
            this.context = context2;
        }

        public void run() {
            if (this.packageNameList != null) {
                long beginTime = System.currentTimeMillis();
                Slog.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]VerifyInstalledPackages  thread begin:" + beginTime);
                for (String packageName : this.packageNameList) {
                    try {
                        SecurityProfileService.this.verifyPackage(packageName, SecurityProfileUtils.getInstalledApkPath(packageName, this.context));
                    } catch (Exception e) {
                        Slog.e(SecurityProfileService.TAG, "VerifyInstalledPackagesRunnable Exception " + e.getMessage());
                    }
                }
                SecurityProfileService.this.mPolicyEngine.setPolicyRecoverFlag(false);
                long endTime = System.currentTimeMillis();
                Slog.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]VerifyInstalledPackages  thread end:" + endTime + " Total usage:" + (endTime - beginTime) + "ms");
            }
        }
    }

    public SecurityProfileService(Context context) {
        this.mContext = context;
    }

    public SecurityProfileInternal getLocalServiceImpl() {
        return new LocalServiceImpl();
    }

    /* access modifiers changed from: private */
    public boolean isNoScreenRecording() {
        if (this.mMediaProjectionStopper.mActiveProjection == null) {
            return true;
        }
        String projectionPackageName = this.mMediaProjectionStopper.mActiveProjection.getPackageName();
        if (projectionPackageName == null) {
            Slog.w(TAG, "mActiveProjection is not null, but we can not get projectionPackageName from it!");
            return true;
        }
        if (isSystemApp(projectionPackageName)) {
            if (SecurityProfileUtils.isAccessibilitySelectToSpeakActive(this.mContext)) {
                return true;
            }
            Slog.d(TAG, projectionPackageName + " is systemApp and it is recording");
        }
        return false;
    }

    public void onStart() {
        long beginTime = System.currentTimeMillis();
        Slog.d(TAG, "[SEAPP_TimeUsage]SecurityProfileService onStart begin:" + beginTime);
        this.activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (this.mMediaProjectionStopper == null) {
            this.mMediaProjectionStopper = new MediaProjectionStopper();
        }
        this.mPolicyEngine = new PolicyEngine(this.mContext);
        this.mPolicyEngine.addAction("StopScreenRecording", new PolicyEngine.Action() {
            public final void execute(int i) {
                SecurityProfileService.this.mMediaProjectionStopper.stopMediaProjection();
            }
        });
        this.mPolicyEngine.addState("NoScreenRecording", new PolicyEngine.State() {
            public final boolean evaluate() {
                return SecurityProfileService.this.isNoScreenRecording();
            }
        });
        this.mPolicyEngine.start();
        LocalServices.addService(SecurityProfileInternal.class, getLocalServiceImpl());
        if (SUPPORT_HW_SEAPP && IS_CHINA_AREA) {
            IntentFilter packageChangedFilter = new IntentFilter();
            packageChangedFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageChangedFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageChangedFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageChangedFilter.addDataScheme("package");
            this.mContext.registerReceiverAsUser(this.mMultiUserBroadcastReceiver, UserHandle.ALL, packageChangedFilter, null, null);
            IntentFilter bootCompletedFilter = new IntentFilter();
            bootCompletedFilter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mDefaultUserBroadcastReceiver, bootCompletedFilter);
        }
        long endTime = System.currentTimeMillis();
        Slog.d(TAG, "[SEAPP_TimeUsage]SecurityProfileService onStart end:" + endTime + " Total usage:" + (endTime - beginTime) + "ms");
    }

    public void onStop() {
        Slog.e(TAG, "SecurityProfileService stoped");
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.securityprofile.SecurityProfileService$SecurityProfileServiceImpl, android.os.IBinder] */
    public IBinder asBinder() {
        return new SecurityProfileServiceImpl();
    }

    public boolean verifyPackage(String packageName, String apkPath) {
        try {
            Slog.d(TAG, "verifyPackage apkPath:" + apkPath);
            byte[] jws = PolicyExtractor.getPolicy(apkPath);
            ApkDigest apkDigest = PolicyExtractor.getDigest(packageName, jws);
            JSONObject policy = PolicyVerifier.verifyAndDecodePolicy(jws);
            if (policy == null) {
                Slog.e(TAG, "Policy verification failed");
                return false;
            } else if (!DigestMatcher.packageMatchesDigest(apkPath, apkDigest)) {
                Slog.e(TAG, "Package digest did not match policy digest");
                return false;
            } else {
                this.mPolicyEngine.setPackageSigned(packageName, true);
                this.mPolicyEngine.addPolicy(policy);
                return true;
            }
        } catch (PolicyExtractor.PolicyNotFoundException e) {
            Slog.e(TAG, "Not found Exception " + e.getMessage());
            return true;
        } catch (IOException e2) {
            Slog.e(TAG, "verifyPackage IOException " + e2.getMessage());
            return false;
        } catch (Exception e3) {
            Slog.e(TAG, "verifyPackage Exception " + e3.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public String getExtraObjectLabel(String packageName) {
        if (this.screenshotProtector.getRegister(packageName) == null) {
            return null;
        }
        Slog.d(TAG, "getExtraObjectLabel: " + packageName + ", label: Red");
        return "Red";
    }

    public boolean shouldPreventMediaProjection(int uid) {
        if (uid < 10000) {
            Slog.d(TAG, "not PreventMediaProjection: system Uid " + uid);
            return false;
        }
        String[] projectionPackages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (projectionPackages == null || projectionPackages.length == 0) {
            Slog.d(TAG, "not PreventMediaProjection: get no packages for Uid" + uid);
            return false;
        }
        Set<String> projectionSet = new HashSet<>();
        for (String pack : projectionPackages) {
            projectionSet.add(pack);
        }
        if (this.activityManager == null) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo taskInfo : this.activityManager.getRunningTasks(10)) {
            String foreground = taskInfo.topActivity == null ? null : taskInfo.topActivity.getPackageName();
            if (foreground != null && !projectionSet.contains(foreground) && HwActivityManager.isTaskVisible(taskInfo.id)) {
                if (this.mPolicyEngine.requestAccessWithExtraLabel((String) projectionSet.toArray()[0], foreground, getExtraObjectLabel(foreground), "MediaProjection", "Record", 0)) {
                    continue;
                } else {
                    ScreenshotProtectorCallback callback = this.screenshotProtector.getRegister(foreground);
                    if (callback != null) {
                        Slog.d(TAG, "package: " + foreground + "is protected, try to stop projection");
                        stopPackages(projectionPackages);
                        this.screenshotProtector.notifyInfo(callback, projectionPackages[0]);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean shouldPreventInteraction(int type, String targetPackage, int callerUid, int callerPid, String callerPackage, int userId) {
        String str = targetPackage;
        long t = System.nanoTime();
        if (str == null) {
            return false;
        }
        String extraObjectLabel = null;
        if (type == 0) {
            extraObjectLabel = getExtraObjectLabel(str);
            if (extraObjectLabel != null) {
                this.mMediaProjectionStopper.mProtectedPackage = str;
            }
        }
        if (!this.mPolicyEngine.requestAccessWithExtraLabel(callerPackage, str, extraObjectLabel, "Intent", "Send", 0)) {
            if (type != 0) {
                String str2 = callerPackage;
            } else if (isLauncherApp(callerPackage)) {
                long curTime = System.currentTimeMillis();
                if (curTime < this.blackToastTime || curTime - this.blackToastTime > 3500) {
                    this.blackToastTime = curTime;
                    String text = this.mContext.getResources().getString(33686249);
                    if (text != null) {
                        showToast(text);
                    }
                }
            }
            Slog.d(TAG, str + " is not allowed to start.");
            Slog.d(TAG, "shouldPreventInteraction " + String.valueOf(System.nanoTime() - t));
            return true;
        }
        String str3 = callerPackage;
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
        if (launcher == null || !launcher.equals(target)) {
            return false;
        }
        return true;
    }

    private void showToast(final String text) {
        UiThread.getHandler().post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(SecurityProfileService.this.mContext, text, 1);
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void stopPackages(String[] packageNames) {
        this.mHandler.post(new PackageStopper(packageNames));
    }

    /* access modifiers changed from: private */
    public boolean isSystemApp(String pack) {
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
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "can not check if " + pack + " is a system app");
        }
    }
}
