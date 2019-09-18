package huawei.android.security.securityprofile;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import huawei.android.security.ISecurityProfileService;
import huawei.android.security.securityprofile.PolicyExtractor;
import java.util.Arrays;
import java.util.List;

public class HwSignedInfo {
    public static final String BUNDLE_KEY_ADD_DOMAIN_POLICY = "addDomainPolicy";
    public static final String BUNDLE_KEY_APKPATH = "apkPath";
    public static final String BUNDLE_KEY_CONTENT_TYPE = "ContentType";
    public static final String BUNDLE_KEY_DIGEST_ALGORITHM = "digestAlgorithm";
    public static final String BUNDLE_KEY_DIGEST_BASE64DIGEST = "base64Digest";
    public static final String BUNDLE_KEY_DIGEST_SCHEME = "apkSignatureScheme";
    public static final String BUNDLE_KEY_LABEL = "pureAndroidLabel";
    public static final String BUNDLE_KEY_PACKAGENAME = "packageName";
    public static final String CONTENT_TYPE_APK_DIGEST_ONLY = "ApkDigestOnly";
    public static final List<String> HUAWEI_INSTALLERS = Arrays.asList(new String[]{"com.huawei.appmarket", "com.huawei.gamebox"});
    public static final String POLICY_LEBALS_GREEN = "GREEN";
    public static final String POLICY_LEBALS_NORMAL = "NORMAL";
    public static final int POLICY_OK = 0;
    public static final int POLICY_VERIFICATION_FAILED = 1;
    public static final String PREFIX_RESULT = "RESULT_";
    public static final String TAG = "HwSignedInfo";

    public static final String getPureAndroidLabelFromLabelList(List<String> labelsList) {
        if (labelsList == null || labelsList.size() <= 0) {
            return null;
        }
        if (labelsList.contains(POLICY_LEBALS_GREEN)) {
            return POLICY_LEBALS_GREEN;
        }
        if (labelsList.contains(POLICY_LEBALS_NORMAL)) {
            return POLICY_LEBALS_NORMAL;
        }
        return null;
    }

    public static final Bundle resolveHwSignedBundleInfo(String packageName, List<String> labelsList, ApkDigest apkDigest) {
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        bundle.putString(BUNDLE_KEY_LABEL, getPureAndroidLabelFromLabelList(labelsList));
        if (apkDigest != null) {
            bundle.putString(BUNDLE_KEY_DIGEST_SCHEME, apkDigest.apkSignatureScheme);
            bundle.putString(BUNDLE_KEY_DIGEST_ALGORITHM, apkDigest.digestAlgorithm);
            bundle.putString(BUNDLE_KEY_DIGEST_BASE64DIGEST, apkDigest.base64Digest);
        }
        return bundle;
    }

    private static boolean isInstalledByHuaweiAppMarket(ISecurityProfileService service, String packageName) {
        try {
            String installer = service.getInstallerPackageName(packageName);
            Log.d(TAG, packageName + ",installer:" + installer);
            if (HUAWEI_INSTALLERS.contains(installer)) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, packageName + ",check isInstalledByHuaweiAppMarket get RemoteException:" + e.getMessage());
        }
        return false;
    }

    private static boolean isInstalledAppCanGetHwSignedInfo(ISecurityProfileService sSecurityProfileService, String packageName) {
        try {
            if (sSecurityProfileService.isPackageSigned(packageName)) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, packageName + ",check isInstalledByHuaweiAppMarket get RemoteException:" + e.getMessage());
        }
        if (!DigestMatcher.CALCULATE_APKDIGEST || !isInstalledByHuaweiAppMarket(sSecurityProfileService, packageName)) {
            return false;
        }
        return true;
    }

    private static Bundle getInstalledHwSignedInfo(ISecurityProfileService sSecurityProfileService, String packageName) {
        try {
            List<String> labelsList = sSecurityProfileService.getLabels(packageName, null);
            Log.d(TAG, packageName + " labelsList = " + labelsList);
            return resolveHwSignedBundleInfo(packageName, labelsList, null);
        } catch (Exception e) {
            Log.e(TAG, "getInstalledApkHwSignedInfo [" + packageName + "] occurs Exception:" + e.getMessage());
            return resolveHwSignedBundleInfo(packageName, null, null);
        }
    }

    private static Bundle getInstalledHwSignedApkDigest(ISecurityProfileService sSecurityProfileService, String packageName) {
        ApkDigest apkDigest;
        try {
            String apkPath = getInstalledApkPath(packageName);
            try {
                apkDigest = PolicyExtractor.getDigest(packageName, PolicyExtractor.getPolicy(apkPath));
            } catch (PolicyExtractor.PolicyNotFoundException e) {
                apkDigest = DigestMatcher.getApkDigest(apkPath);
            }
            return resolveHwSignedBundleInfo(packageName, null, apkDigest);
        } catch (Exception e2) {
            Log.e(TAG, "getInstalledHwSignedApkDigest [" + packageName + "] occurs Exception:" + e2.getMessage());
            return resolveHwSignedBundleInfo(packageName, null, null);
        }
    }

    private static String getInstalledApkPath(String packageName) {
        IUserManager userManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (userManager == null || packageManager == null) {
            Slog.w(TAG, "getInstalledApkPath getService null");
            return null;
        }
        try {
            List<UserInfo> userInfoList = userManager.getUsers(true);
            if (userInfoList != null) {
                if (userInfoList.size() != 0) {
                    for (UserInfo userInfo : userInfoList) {
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0, userInfo.id);
                        if (applicationInfo != null) {
                            String apkPath = applicationInfo.sourceDir;
                            Slog.d(TAG, "getInstalledApkPath: " + packageName + ", from userId: " + userInfo.id);
                            return apkPath;
                        }
                    }
                    return null;
                }
            }
            Slog.e(TAG, "impossible: users num zero");
            return null;
        } catch (RemoteException e) {
            Slog.d(TAG, "getInstalledApkPath Remote Exception: " + e.getMessage());
        }
    }

    private static Bundle getUnInstalledHwSignedApkDigest(ISecurityProfileService sSecurityProfileService, String packageName, String apkPath) {
        try {
            byte[] policyBlock = PolicyExtractor.getPolicy(apkPath);
            ApkDigest apkDigest = PolicyExtractor.getDigest(packageName, policyBlock);
            if (apkDigest == null) {
                Log.e(TAG, "get apkDigest is null!");
                return resolveHwSignedBundleInfo(packageName, null, null);
            }
            Log.d(TAG, "digest.base64Digest: " + apkDigest.base64Digest);
            Log.d(TAG, "digest.digestAlgorithm: " + apkDigest.digestAlgorithm);
            Log.d(TAG, "digest.apkSignatureScheme: " + apkDigest.apkSignatureScheme);
            if (!DigestMatcher.packageMatchesDigest(apkPath, apkDigest)) {
                Log.e(TAG, packageName + " Package digest did not match policy digest:" + apkDigest.base64Digest + ", apkPath:" + apkPath);
                return resolveHwSignedBundleInfo(packageName, null, null);
            }
            int result = sSecurityProfileService.addDomainPolicy(policyBlock);
            if (result != 0) {
                Log.e(TAG, packageName + " addDomainPolicy err, result = " + result);
                return resolveHwSignedBundleInfo(packageName, null, null);
            }
            List<String> labelsList = sSecurityProfileService.getLabels(packageName, apkDigest);
            Log.d(TAG, packageName + " labelsList = " + labelsList);
            return resolveHwSignedBundleInfo(packageName, labelsList, apkDigest);
        } catch (Exception e) {
            Log.e(TAG, "getUnInstalledHwSignedApkDigest [" + packageName + "] occurs Exception:" + e.getMessage());
            return resolveHwSignedBundleInfo(packageName, null, null);
        }
    }

    public static Bundle getHwSignedInfo(ISecurityProfileService sSecurityProfileService, String packageName, Bundle extraParams) {
        if (packageName == null) {
            Log.e(TAG, "getHwSignedInfo err, packageName is null");
            return null;
        } else if (sSecurityProfileService == null) {
            return null;
        } else {
            String apkPath = null;
            String contentType = null;
            if (extraParams != null) {
                apkPath = extraParams.getString(BUNDLE_KEY_APKPATH, null);
                contentType = extraParams.getString(BUNDLE_KEY_CONTENT_TYPE, null);
            }
            if (apkPath != null) {
                return getUnInstalledHwSignedApkDigest(sSecurityProfileService, packageName, apkPath);
            }
            if (!isInstalledAppCanGetHwSignedInfo(sSecurityProfileService, packageName)) {
                return resolveHwSignedBundleInfo(packageName, null, null);
            }
            if (CONTENT_TYPE_APK_DIGEST_ONLY.equals(contentType)) {
                return getInstalledHwSignedApkDigest(sSecurityProfileService, packageName);
            }
            return getInstalledHwSignedInfo(sSecurityProfileService, packageName);
        }
    }

    public static Bundle setHwSignedInfoToSEAPP(ISecurityProfileService sSecurityProfileService, Bundle params) {
        if (params == null) {
            Log.e(TAG, "setHwSignedInfoToSEAPP params is null");
            return null;
        } else if (sSecurityProfileService == null) {
            Log.e(TAG, "setHwSignedInfoToSEAPP sSecurityProfileService is null");
            return null;
        } else {
            try {
                Bundle bundle = new Bundle();
                byte[] domainPolicy = params.getByteArray(BUNDLE_KEY_ADD_DOMAIN_POLICY);
                if (domainPolicy != null) {
                    bundle.putInt("RESULT_addDomainPolicy", sSecurityProfileService.addDomainPolicy(domainPolicy));
                }
                return bundle;
            } catch (Exception e) {
                Log.e(TAG, "setHwSignedInfoToSEAPP occurs Exception" + e.getMessage());
                return null;
            }
        }
    }
}
