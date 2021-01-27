package com.huawei.server.security.securityprofile;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.security.securityprofile.IntentCaller;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.securityprofile.PolicyEngine;
import com.huawei.server.security.securityprofile.PolicyVerifier;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.security.ISecurityProfileService;
import huawei.android.security.securityprofile.ApkDigest;
import huawei.android.security.securityprofile.ApkSigningBlockUtils;
import huawei.android.security.securityprofile.HwSignedInfo;
import huawei.android.security.securityprofile.PolicyExtractor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class SecurityProfileService implements IHwSecurityPlugin {
    private static final int ACTION_UPDATE_ALL = 1;
    private static final boolean CALCULATE_APKDIGEST = "true".equalsIgnoreCase(SystemPropertiesEx.get("ro.config.iseapp_calculate_apkdigest", "true"));
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.securityprofile.SecurityProfileService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            return new SecurityProfileService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return SecurityProfileService.MANAGE_SECURITYPROFILE;
        }
    };
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final List<String> HUAWEI_INSTALLERS = Arrays.asList("com.huawei.appmarket", "com.huawei.gamebox");
    private static final String HW_SIGNATURE_OR_SYSTEM = "com.huawei.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final boolean IS_CHINA_AREA = SecurityProfileUtils.IS_CHINA_AREA;
    private static final String KEY_POLICY_BLOCK = "policyBlock";
    private static final String KEY_POLICY_TYPE = "policyType";
    public static final String MANAGE_SECURITYPROFILE = "com.huawei.permission.MANAGE_SECURITYPROFILE";
    public static final int ON_POLICY_UPDATED = 1;
    private static final String POLICY_TYPE_PERMISSION_SETTINGS = "PermissionSettings";
    private static final String POLICY_TYPE_SECURITY_PROFILE = "SecurityProfile";
    private static final boolean SUPPORT_HW_SEAPP = "true".equalsIgnoreCase(SystemPropertiesEx.get("ro.config.support_iseapp", "false"));
    private static final String TAG = "SecurityProfileService";
    private static final int TOAST_LONG_DELAY = 3500;
    private ActivityManager mActivityManager;
    private long mBlackToastTime = 0;
    private final Context mContext;
    private BroadcastReceiver mDefaultUserBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.securityprofile.SecurityProfileService.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if (SecurityProfileService.DEBUG) {
                    Log.i(SecurityProfileService.TAG, "receive Intent.ACTION_BOOT_COMPLETED");
                }
                SecurityProfileService.this.mPermissionReasonPolicy.recoverPolicyAnyway();
                if (SecurityProfileService.this.mPolicyEngine.isNeedPolicyRecover()) {
                    new Thread(new VerifyInstalledPackagesRunnable(SecurityProfileUtils.getInstalledPackages(context)), "PolicyRecoverWorkerThread").start();
                } else if (SecurityProfileService.DEBUG) {
                    Log.i(SecurityProfileService.TAG, "not need to recover policy, do not verify all installed package");
                }
            }
        }
    };
    private final Handler mHandler = new Handler() {
        /* class com.huawei.server.security.securityprofile.SecurityProfileService.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(@NonNull Message msg) {
            if (msg.what != 1) {
                Log.w(SecurityProfileService.TAG, "Unknown message: " + msg.what);
            } else if (msg.obj instanceof List) {
                final List<String> packageList = (List) msg.obj;
                if (SecurityProfileService.DEBUG) {
                    Log.d(SecurityProfileService.TAG, "policy updated: " + packageList);
                }
                SecurityProfileUtils.getWorkerThreadPool().execute(new Runnable() {
                    /* class com.huawei.server.security.securityprofile.SecurityProfileService.AnonymousClass2.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        for (String packageName : packageList) {
                            SecurityProfileService.this.mPermissionReasonPolicy.updatePackagePolicy(packageName);
                        }
                    }
                });
            }
        }
    };
    private final HwCertXmlHandler mHwCertXmlHandler;
    private BroadcastReceiver mMultiUserBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.securityprofile.SecurityProfileService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(final Context context, Intent intent) {
            Uri uri;
            final String action = intent.getAction();
            if (("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) && (uri = intent.getData()) != null) {
                String outerPackageName = uri.getSchemeSpecificPart();
                if (outerPackageName == null) {
                    Log.w(SecurityProfileService.TAG, "onReceive outerPackageName null");
                    return;
                }
                final String packageName = SecurityProfileUtils.replaceLineSeparator(outerPackageName);
                SecurityProfileUtils.getWorkerThreadPool().execute(new Runnable() {
                    /* class com.huawei.server.security.securityprofile.SecurityProfileService.AnonymousClass3.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                                SecurityProfileService.this.verifyPackage(packageName, SecurityProfileUtils.getInstalledApkPath(packageName, context));
                            }
                            SecurityProfileService.this.mPolicyEngine.updatePackageInformation(packageName);
                        } catch (Exception e) {
                            Log.e(SecurityProfileService.TAG, "Failed to update policy into database!");
                        }
                        if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                            InstallerDataBase.getInstance().setInstallerPackageName(context, packageName);
                        }
                    }
                });
            }
        }
    };
    private PermissionReasonPolicy mPermissionReasonPolicy;
    private PolicyEngine mPolicyEngine = null;

    @VisibleForTesting(otherwise = 2)
    public SecurityProfileService(Context context) {
        this.mContext = context;
        this.mHwCertXmlHandler = new HwCertXmlHandler();
    }

    private SecurityProfileInternal getLocalServiceImpl() {
        return new LocalServiceImpl();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        long beginTime = 0;
        if (DEBUG) {
            beginTime = System.currentTimeMillis();
            Log.d(TAG, "[SEAPP_TimeUsage]SecurityProfileService onStart begin: " + beginTime);
        }
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPolicyEngine = new PolicyEngine(this.mContext, this.mHandler);
        this.mPolicyEngine.start();
        this.mPermissionReasonPolicy = PermissionReasonPolicy.getInstance();
        this.mPermissionReasonPolicy.init(this.mContext, this.mPolicyEngine);
        LocalServicesExt.addService(SecurityProfileInternal.class, getLocalServiceImpl());
        if (SUPPORT_HW_SEAPP) {
            IntentFilter packageChangedFilter = new IntentFilter();
            packageChangedFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageChangedFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageChangedFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageChangedFilter.addDataScheme("package");
            ContextEx.registerReceiverAsUser(this.mContext, this.mMultiUserBroadcastReceiver, UserHandleEx.ALL, packageChangedFilter, (String) null, (Handler) null);
            IntentFilter bootCompletedFilter = new IntentFilter();
            bootCompletedFilter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mDefaultUserBroadcastReceiver, bootCompletedFilter);
        }
        if (DEBUG) {
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "[SEAPP_TimeUsage]SecurityProfileService onStart end: " + endTime + " Total usage: " + (endTime - beginTime) + "ms");
        }
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        Log.e(TAG, "SecurityProfileService stopped");
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.server.security.securityprofile.SecurityProfileService$SecurityProfileServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return new SecurityProfileServiceImpl();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean verifyPackage(String packageName, String apkPath) {
        try {
            if (DEBUG) {
                Log.d(TAG, "verifyPackage apkPath: " + apkPath);
            }
            JSONObject policy = PolicyVerifier.getValidPolicyFromApkPath(packageName, apkPath);
            this.mPolicyEngine.setPackageSigned(packageName, true);
            this.mPolicyEngine.addPolicy(policy);
            return true;
        } catch (PolicyExtractor.PolicyNotFoundException e) {
            Log.w(TAG, "verifyPackage must return for huawei policy not found: " + e.getMessage() + ", apkPath: " + apkPath);
            return true;
        } catch (PolicyVerifier.PolicyVerifyFailedException e2) {
            Log.w(TAG, "verifyPackage must return for policy verify failed: " + e2.getMessage() + ", apkPath: " + apkPath);
            return false;
        } catch (Exception e3) {
            Log.e(TAG, "Failed to verify policy!");
            return false;
        }
    }

    private boolean isInstalledByHuaweiAppMarket(String packageName) {
        if (HUAWEI_INSTALLERS.contains(InstallerDataBase.getInstance().getInstallerPackageName(this.mContext, packageName))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInstalledAppCanGetHwSignedInfo(String packageName) {
        if (this.mPolicyEngine.isPackageSigned(packageName)) {
            return true;
        }
        if (!CALCULATE_APKDIGEST || !isInstalledByHuaweiAppMarket(packageName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HwSignedInfo getActiveHwSignedInfo(String packageName, HwSignedInfo hwSignedInfo, int flags) {
        if ((flags & 2) != 0) {
            try {
                hwSignedInfo.labelsList = this.mPolicyEngine.getLabels(packageName, null);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get signed info for " + packageName + ", flags: " + flags + ", error!");
            }
        }
        if ((flags & 1) != 0) {
            hwSignedInfo.apkDigest = getActiveHwSignedDigest(packageName);
        }
        return hwSignedInfo;
    }

    private ApkDigest getActiveHwSignedDigest(String packageName) {
        String apkPath = SecurityProfileUtils.getInstalledApkPath(packageName, this.mContext);
        try {
            return PolicyExtractor.getApkDigestFromPolicyBlock(packageName, PolicyExtractor.getPolicyBlock(apkPath));
        } catch (PolicyExtractor.PolicyNotFoundException e) {
            return ApkSigningBlockUtils.calculateApkDigest(apkPath);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldPreventInteraction(int type, String targetPackage, IntentCaller caller, int userId) {
        if (targetPackage == null || this.mPolicyEngine.requestAccessWithExtraLabel(caller.packageName, new PolicyEngine.PolicyObject(targetPackage, null), new PolicyEngine.PolicyAdverbial("Intent", "Send", 0))) {
            return false;
        }
        if (type == 0 && SecurityProfileUtils.isLauncherApp(this.mContext, caller.packageName)) {
            long curTime = System.currentTimeMillis();
            if (isBlackToastNotExist(curTime)) {
                this.mBlackToastTime = curTime;
                SecurityProfileUtils.showToast(this.mContext, this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("toast_security")));
            }
        }
        Log.w(TAG, targetPackage + " is not allowed to start.");
        return true;
    }

    private boolean isBlackToastNotExist(long curTime) {
        long j = this.mBlackToastTime;
        return curTime < j || curTime - j > 3500;
    }

    private class VerifyInstalledPackagesRunnable implements Runnable {
        List<String> mPackageNameList;

        VerifyInstalledPackagesRunnable(List<String> inPackageNameList) {
            this.mPackageNameList = inPackageNameList;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mPackageNameList != null) {
                long beginTime = 0;
                if (SecurityProfileService.DEBUG) {
                    beginTime = System.currentTimeMillis();
                    Log.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]VerifyInstalledPackages thread begin: " + beginTime);
                }
                for (String packageName : this.mPackageNameList) {
                    SecurityProfileService securityProfileService = SecurityProfileService.this;
                    securityProfileService.verifyPackage(packageName, SecurityProfileUtils.getInstalledApkPath(packageName, securityProfileService.mContext));
                }
                SecurityProfileService.this.mPolicyEngine.setPolicyRecoverFlag(false);
                if (SecurityProfileService.DEBUG) {
                    long endTime = System.currentTimeMillis();
                    Log.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]VerifyInstalledPackages thread end: " + endTime + " Total usage: " + (endTime - beginTime) + "ms");
                }
            }
        }
    }

    private class SecurityProfileServiceImpl extends ISecurityProfileService.Stub {
        private static final int RESULT_CODE_ADD_POLICY_FAIL = 1;
        private static final int RESULT_CODE_ADD_POLICY_SUCC = 0;

        private SecurityProfileServiceImpl() {
        }

        public void updateBlackApp(List<String> packages, int action) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            long token = Binder.clearCallingIdentity();
            if (action == 1) {
                SecurityProfileService.this.mPolicyEngine.updateBlackApp(packages);
                SecurityProfileService.this.killBlackApps(packages);
            } else if (action == 2) {
                SecurityProfileService.this.mPolicyEngine.addBlackApp(packages);
                SecurityProfileService.this.killBlackApps(packages);
            } else if (action == 3) {
                try {
                    SecurityProfileService.this.mPolicyEngine.removeBlackApp(packages);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(token);
        }

        public boolean updateMdmCertBlacklist(List<String> blacklist, int action) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            if (action == 1) {
                return SecurityProfileService.this.mHwCertXmlHandler.updateMdmCertBlacklist(blacklist);
            }
            return false;
        }

        public boolean isBlackApp(String packageName) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            return SecurityProfileService.this.mPolicyEngine.isBlackApp(packageName);
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x005e A[Catch:{ PolicyVerifyFailedException -> 0x00af, JSONException -> 0x0095, Exception -> 0x008e }] */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0083 A[Catch:{ PolicyVerifyFailedException -> 0x00af, JSONException -> 0x0095, Exception -> 0x008e }] */
        public int addDomainPolicy(byte[] policyWrapper) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            if (policyWrapper == null) {
                try {
                    Log.e(SecurityProfileService.TAG, "Invalid wrapped policy.");
                    return 1;
                } catch (PolicyVerifier.PolicyVerifyFailedException e) {
                    Log.w(SecurityProfileService.TAG, "Failed to verify when add policy: " + e.getMessage());
                    return 1;
                } catch (JSONException e2) {
                    Log.w(SecurityProfileService.TAG, "Failed to unwrap policy when add policy: " + e2.getMessage());
                    return 1;
                } catch (Exception e3) {
                    Log.e(SecurityProfileService.TAG, "Failed to add policy!");
                    return 1;
                }
            } else {
                JSONObject wrapper = new JSONObject(new String(policyWrapper, StandardCharsets.UTF_8));
                String policyType = wrapper.optString(SecurityProfileService.KEY_POLICY_TYPE, SecurityProfileService.POLICY_TYPE_SECURITY_PROFILE);
                byte[] policyBlock = Base64.getDecoder().decode(wrapper.getString(SecurityProfileService.KEY_POLICY_BLOCK));
                char c = 65535;
                int hashCode = policyType.hashCode();
                if (hashCode != 414104105) {
                    if (hashCode == 1074225970 && policyType.equals(SecurityProfileService.POLICY_TYPE_PERMISSION_SETTINGS)) {
                        c = 0;
                        if (c == 0) {
                            SecurityProfileService.this.mPermissionReasonPolicy.updateSecuritySettings(policyBlock);
                        } else if (c != 1) {
                            Log.w(SecurityProfileService.TAG, "Unsupported policy type: " + policyType);
                            return 1;
                        } else {
                            SecurityProfileService.this.mPolicyEngine.addPolicy(PolicyVerifier.verifyAndDecodePolicy(policyBlock));
                        }
                        return 0;
                    }
                } else if (policyType.equals(SecurityProfileService.POLICY_TYPE_SECURITY_PROFILE)) {
                    c = 1;
                    if (c == 0) {
                    }
                    return 0;
                }
                if (c == 0) {
                }
                return 0;
            }
        }

        @NonNull
        public List<String> getLabels(String packageName, ApkDigest digest) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            if (packageName == null) {
                return Collections.emptyList();
            }
            return SecurityProfileService.this.mPolicyEngine.getLabels(packageName, digest);
        }

        public HwSignedInfo getActiveHwSignedInfo(String packageName, int flags) {
            SecurityProfileService.this.mContext.enforceCallingOrSelfPermission(SecurityProfileService.MANAGE_SECURITYPROFILE, "Must have com.huawei.permission.MANAGE_SECURITYPROFILE permission.");
            if (packageName == null) {
                return null;
            }
            HwSignedInfo hwSignedInfo = new HwSignedInfo(packageName);
            if ((flags & 4) != 0) {
                hwSignedInfo.permissionFlags = SecurityProfileService.this.mPermissionReasonPolicy.isPackageSupported(packageName) ? 1 : 0;
            }
            if (SecurityProfileService.this.isInstalledAppCanGetHwSignedInfo(packageName)) {
                return SecurityProfileService.this.getActiveHwSignedInfo(packageName, hwSignedInfo, flags);
            }
            Log.d(SecurityProfileService.TAG, "App not from reliable source.");
            return hwSignedInfo;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void killBlackApps(@Nullable List<String> packages) {
        if (!(packages == null || packages.size() == 0)) {
            for (String packageName : packages) {
                ActivityManagerEx.forceStopPackageAsUser(this.mActivityManager, packageName, -1);
                Log.w(TAG, packageName + " is stopped by SecurityProfileService, for black apps.");
            }
        }
    }

    @VisibleForTesting(otherwise = 2)
    public final class LocalServiceImpl implements SecurityProfileInternal {
        public LocalServiceImpl() {
        }

        @Override // com.huawei.server.security.securityprofile.SecurityProfileInternal
        public boolean shouldPreventInteraction(int type, String targetPackage, IntentCaller caller, int userId) {
            return SecurityProfileService.this.shouldPreventInteraction(type, targetPackage, caller, userId);
        }

        @Override // com.huawei.server.security.securityprofile.SecurityProfileInternal
        public boolean shouldPreventMediaProjection(int uid) {
            return false;
        }

        @Override // com.huawei.server.security.securityprofile.SecurityProfileInternal
        public void handleActivityResuming(String packageName) {
        }

        @Override // com.huawei.server.security.securityprofile.SecurityProfileInternal
        public void registerScreenshotProtector(ScreenshotProtectorCallback callback) {
        }

        @Override // com.huawei.server.security.securityprofile.SecurityProfileInternal
        public void unregisterScreenshotProtector(ScreenshotProtectorCallback callback) {
        }

        @Override // com.huawei.server.security.securityprofile.SecurityProfileInternal
        public boolean verifyPackage(String packageName, File path) {
            long beginTime = 0;
            if (SecurityProfileService.DEBUG) {
                beginTime = System.currentTimeMillis();
                Log.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]verifyPackage begin: " + beginTime);
            }
            if (packageName == null || path == null) {
                Log.e(SecurityProfileService.TAG, "verifyPackageSecurityPolicy illegal params");
                return false;
            }
            try {
                boolean result = SecurityProfileService.this.verifyPackage(packageName, path.getCanonicalPath() + "/base.apk");
                if (SecurityProfileService.DEBUG) {
                    long endTime = System.currentTimeMillis();
                    Log.d(SecurityProfileService.TAG, "[SEAPP_TimeUsage]verifyPackage end: " + endTime + ", Total usage: " + (endTime - beginTime) + "ms");
                }
                return result;
            } catch (IOException e) {
                Log.e(SecurityProfileService.TAG, "Failed to get path from apk file: " + e.getMessage());
                return false;
            }
        }
    }
}
