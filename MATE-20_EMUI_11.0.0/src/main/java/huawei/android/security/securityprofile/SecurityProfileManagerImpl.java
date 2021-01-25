package huawei.android.security.securityprofile;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.ISecurityProfileService;
import huawei.android.security.securityprofile.PolicyExtractor;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class SecurityProfileManagerImpl {
    public static final String CONTENT_TYPE_APK_DIGEST_ONLY = "ApkDigestOnly";
    private static final boolean DEBUG;
    public static final int FLAG_INSTALLED_PACKAGE_DIGEST = 1;
    public static final int FLAG_INSTALLED_PACKAGE_GREEN_LABEL = 2;
    public static final int FLAG_INSTALLED_PACKAGE_PERMISSION_REASON_STATUS = 4;
    public static final int FLAG_UNINSTALLED_PACKAGE_EXTRACT = 8;
    private static final String KEY_ADD_DOMAIN_POLICY = "addDomainPolicy";
    public static final String KEY_APK_PATH = "apkPath";
    public static final String KEY_CONTENT_TYPE = "ContentType";
    private static final String KEY_DIGEST_ALGORITHM = "digestAlgorithm";
    private static final String KEY_DIGEST_BASE64DIGEST = "base64Digest";
    private static final String KEY_DIGEST_SCHEME = "apkSignatureScheme";
    private static final String KEY_LABEL = "pureAndroidLabel";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_PERMISSION_REASON_STATUS = "permissionReasonStatus";
    private static final String KEY_POLICY_BLOCK = "policyBlock";
    private static final String KEY_POLICY_TYPE = "policyType";
    public static final String KEY_SEAPP_FLAGS = "seappFlags";
    private static final Object LOCK = new Object();
    private static final int PERMISSION_REASON_NOT_PERMITTED = 0;
    private static final int PERMISSION_REASON_PERMITTED = 1;
    private static final String POLICY_LABELS_GREEN = "GREEN";
    private static final String POLICY_LABELS_NORMAL = "NORMAL";
    private static final int POLICY_OK = 0;
    private static final String POLICY_TYPE_PERMISSION_SETTINGS = "PermissionSettings";
    private static final String POLICY_TYPE_SECURITY_PROFILE = "SecurityProfile";
    private static final int POLICY_VERIFICATION_FAILED = 1;
    private static final String PREFIX_RESULT = "RESULT_";
    private static final int SECURITY_PROFILE_PLUGIN_ID = 8;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "SecurityProfileManager";
    private static SecurityProfileManagerImpl sSelf = null;
    private static ISecurityProfileService sService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SeappFlags {
    }

    static {
        boolean z = false;
        if (SystemPropertiesEx.getBoolean("ro.debuggable", false) || SystemPropertiesEx.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = true;
        }
        DEBUG = z;
    }

    private SecurityProfileManagerImpl() {
    }

    public static SecurityProfileManagerImpl getDefault() {
        SecurityProfileManagerImpl securityProfileManagerImpl;
        synchronized (SecurityProfileManagerImpl.class) {
            if (sSelf == null) {
                sSelf = new SecurityProfileManagerImpl();
            }
            securityProfileManagerImpl = sSelf;
        }
        return securityProfileManagerImpl;
    }

    @Nullable
    private static ISecurityProfileService getService() {
        synchronized (LOCK) {
            if (sService != null) {
                return sService;
            }
            try {
                IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
                if (secService != null) {
                    IBinder secPlugin = secService.querySecurityInterface(8);
                    sService = ISecurityProfileService.Stub.asInterface(secPlugin);
                    if (secPlugin != null) {
                        secPlugin.linkToDeath(new IBinder.DeathRecipient() {
                            /* class huawei.android.security.securityprofile.SecurityProfileManagerImpl.AnonymousClass1 */

                            @Override // android.os.IBinder.DeathRecipient
                            public void binderDied() {
                                synchronized (SecurityProfileManagerImpl.LOCK) {
                                    ISecurityProfileService unused = SecurityProfileManagerImpl.sService = null;
                                    Log.e(SecurityProfileManagerImpl.TAG, "secPlugin is died.");
                                }
                            }
                        }, 0);
                    } else {
                        Log.i(TAG, "secPlugin is null.");
                    }
                }
                return sService;
            } catch (RemoteException e) {
                Log.e(TAG, "getService occurs RemoteException");
                return null;
            } catch (Exception e2) {
                Log.e(TAG, "getService occurs Exception");
                return null;
            } catch (Error e3) {
                Log.e(TAG, "getService occurs Error");
                return null;
            }
        }
    }

    private static int getCompatibleFlags(@Nullable Bundle extraParams) {
        if (extraParams == null || extraParams.isEmpty()) {
            return 2;
        }
        int seappFlags = extraParams.getInt(KEY_SEAPP_FLAGS, 0);
        if (seappFlags != 0) {
            return seappFlags;
        }
        String apkPath = extraParams.getString(KEY_APK_PATH, null);
        String contentType = extraParams.getString(KEY_CONTENT_TYPE, null);
        if (apkPath != null || !CONTENT_TYPE_APK_DIGEST_ONLY.equals(contentType)) {
            return 8;
        }
        return 1;
    }

    private String getPureAndroidLabelFromLabelList(List<String> labelsList) {
        if (labelsList.size() <= 0) {
            return null;
        }
        if (labelsList.contains(POLICY_LABELS_GREEN)) {
            return POLICY_LABELS_GREEN;
        }
        if (labelsList.contains(POLICY_LABELS_NORMAL)) {
            return POLICY_LABELS_NORMAL;
        }
        return null;
    }

    private Bundle resolveHwSignedBundleInfo(String packageName, @Nullable HwSignedInfo hwSignedInfo, int seappFlags) {
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        if (hwSignedInfo == null) {
            Log.w(TAG, "Bundle null bundle for " + packageName);
            return bundle;
        }
        if (hwSignedInfo.labelsList != null) {
            bundle.putString(KEY_LABEL, getPureAndroidLabelFromLabelList(hwSignedInfo.labelsList));
        }
        ApkDigest apkDigest = hwSignedInfo.apkDigest;
        if (apkDigest != null) {
            bundle.putString(KEY_DIGEST_SCHEME, apkDigest.apkSignatureScheme);
            bundle.putString(KEY_DIGEST_ALGORITHM, apkDigest.digestAlgorithm);
            bundle.putString(KEY_DIGEST_BASE64DIGEST, apkDigest.base64Digest);
        }
        if ((seappFlags & 4) != 0) {
            bundle.putInt(KEY_PERMISSION_REASON_STATUS, hwSignedInfo.permissionFlags);
        }
        Log.d(TAG, "Bundle completed.");
        return bundle;
    }

    public void updateBlackApp(List<String> blackListedPackages, int action) {
        if (getService() != null) {
            try {
                sService.updateBlackApp(blackListedPackages, action);
            } catch (RemoteException e) {
                Log.e(TAG, "updateBlackApp occurs RemoteException");
            } catch (Exception e2) {
                Log.e(TAG, "updateBlackApp occurs Exception");
            }
        }
    }

    public boolean updateMdmCertBlacklist(List<String> blacklist, int action) {
        if (getService() == null) {
            return false;
        }
        try {
            return sService.updateMdmCertBlacklist(blacklist, action);
        } catch (RemoteException e) {
            Log.e(TAG, "updateMdmCertBlacklist RemoteException");
            return false;
        }
    }

    public boolean isBlackApp(String packageName) {
        if (getService() == null) {
            return false;
        }
        try {
            return sService.isBlackApp(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "isBlackApp occurs RemoteException");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "updateBlackApp occurs Exception");
            return false;
        }
    }

    public Bundle getHwSignedInfo(String packageName, Bundle extraParams) {
        Log.d(TAG, "Fetch seapp begin.");
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "getHwSignedInfo err: packageName empty!");
            return null;
        } else if (getService() == null) {
            Log.e(TAG, "getHwSignedInfo err: getService is null");
            return null;
        } else {
            int seappFlags = getCompatibleFlags(extraParams);
            if ((seappFlags & 8) != 0) {
                return resolveHwSignedBundleInfo(packageName, getUnInstalledHwSignedInfo(packageName, extraParams), seappFlags);
            }
            int policyFlags = getPolicyFlags(seappFlags);
            HwSignedInfo policy = null;
            if (policyFlags > 0) {
                policy = getActiveHwSignedInfo(packageName, policyFlags);
            }
            return resolveHwSignedBundleInfo(packageName, policy, seappFlags);
        }
    }

    private int getPolicyFlags(int seappFlags) {
        int flag = 0;
        if ((seappFlags & 2) != 0) {
            flag = 0 | 2;
        }
        if ((seappFlags & 1) != 0) {
            flag |= 1;
        }
        if ((seappFlags & 4) != 0) {
            return flag | 4;
        }
        return flag;
    }

    private HwSignedInfo getActiveHwSignedInfo(String packageName, int flags) {
        try {
            return sService.getActiveHwSignedInfo(packageName, flags);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwSignedInfo occurs RemoteException: " + e.getMessage());
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "getHwSignedInfo occurs Exception");
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a A[Catch:{ RemoteException -> 0x00d2, ArrayIndexOutOfBoundsException -> 0x00ba, IndexOutOfBoundsException -> 0x00a2, JSONException -> 0x0088, Exception -> 0x0081 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0068 A[Catch:{ RemoteException -> 0x00d2, ArrayIndexOutOfBoundsException -> 0x00ba, IndexOutOfBoundsException -> 0x00a2, JSONException -> 0x0088, Exception -> 0x0081 }] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0071 A[Catch:{ RemoteException -> 0x00d2, ArrayIndexOutOfBoundsException -> 0x00ba, IndexOutOfBoundsException -> 0x00a2, JSONException -> 0x0088, Exception -> 0x0081 }] */
    public Bundle setHwSignedInfoToSEAPP(Bundle params) {
        byte[] policyBlock;
        if (params == null) {
            Log.e(TAG, "setHwSignedInfoToSEAPP params is null");
            return null;
        } else if (getService() == null) {
            Log.e(TAG, "setHwSignedInfoToSEAPP err: getService is null");
            return null;
        } else {
            Bundle bundle = new Bundle();
            try {
                String policyType = params.getString(KEY_POLICY_TYPE, POLICY_TYPE_SECURITY_PROFILE);
                char c = 65535;
                int hashCode = policyType.hashCode();
                if (hashCode != 414104105) {
                    if (hashCode == 1074225970 && policyType.equals(POLICY_TYPE_PERMISSION_SETTINGS)) {
                        c = 0;
                        if (c == 0) {
                            policyBlock = params.getByteArray(KEY_POLICY_BLOCK);
                        } else if (c != 1) {
                            Log.w(TAG, "Unsupported policy type: " + policyType);
                            return bundle;
                        } else {
                            policyBlock = params.getByteArray(KEY_ADD_DOMAIN_POLICY);
                        }
                        if (policyBlock != null) {
                            bundle.putInt("RESULT_addDomainPolicy", sService.addDomainPolicy(wrapPolicy(policyType, policyBlock)));
                        }
                        return bundle;
                    }
                } else if (policyType.equals(POLICY_TYPE_SECURITY_PROFILE)) {
                    c = 1;
                    if (c == 0) {
                    }
                    if (policyBlock != null) {
                    }
                    return bundle;
                }
                if (c == 0) {
                }
                if (policyBlock != null) {
                }
                return bundle;
            } catch (RemoteException e) {
                Log.e(TAG, "Update HwSignedInfo, failed to connect binder: " + e.getMessage());
                return bundle;
            } catch (ArrayIndexOutOfBoundsException e2) {
                Log.e(TAG, "Failed to get policy from extras: " + e2.getMessage());
                return bundle;
            } catch (IndexOutOfBoundsException e3) {
                Log.e(TAG, "Failed to get policy from extras: " + e3.getMessage());
                return bundle;
            } catch (JSONException e4) {
                Log.e(TAG, "Failed to wrap policy: " + e4.getMessage());
                return bundle;
            } catch (Exception e5) {
                Log.e(TAG, "Failed to update HwSignedInfo: unknown exception occurred!");
                return bundle;
            }
        }
    }

    private byte[] wrapPolicy(String policyType, byte[] policyBlock) throws JSONException {
        JSONObject wrapper = new JSONObject();
        wrapper.put(KEY_POLICY_TYPE, policyType);
        wrapper.put(KEY_POLICY_BLOCK, new String(Base64.getEncoder().encode(policyBlock), StandardCharsets.UTF_8));
        return wrapper.toString().getBytes(StandardCharsets.UTF_8);
    }

    private HwSignedInfo getUnInstalledHwSignedInfo(String packageName, Bundle extraParams) {
        try {
            String apkPath = extraParams.getString(KEY_APK_PATH, null);
            byte[] policyBlock = PolicyExtractor.getPolicyBlock(apkPath);
            ApkDigest apkDigest = PolicyExtractor.getApkDigestFromPolicyBlock(packageName, policyBlock);
            if (!DigestMatcher.packageMatchesDigest(apkPath, apkDigest)) {
                Log.e(TAG, packageName + " Package digest did not match policy digest: " + apkDigest.base64Digest + ", apkPath: " + apkPath);
                return null;
            }
            if (DEBUG) {
                Log.d(TAG, "get ApkDigest: " + apkDigest.toString());
            }
            HwSignedInfo hwSignedInfo = new HwSignedInfo(packageName);
            int result = sService.addDomainPolicy(wrapPolicy(POLICY_TYPE_SECURITY_PROFILE, policyBlock));
            if (result != 0) {
                Log.e(TAG, packageName + " addDomainPolicy err, result = " + result);
                return null;
            }
            hwSignedInfo.apkDigest = apkDigest;
            List<String> labelsList = sService.getLabels(packageName, apkDigest);
            if (DEBUG) {
                Log.d(TAG, packageName + " labelsList = " + labelsList);
            }
            hwSignedInfo.labelsList = labelsList;
            return hwSignedInfo;
        } catch (PolicyExtractor.PolicyNotFoundException e) {
            Log.w(TAG, "Policy Not Found from uninstalled Apk: " + e.getMessage());
            return null;
        } catch (RemoteException e2) {
            Log.e(TAG, "Get uninstalled Apk hw signedInfo, failed to connect binder");
            return null;
        } catch (Exception e3) {
            Log.e(TAG, "Failed to get policy from uninstalled Apk!");
            return null;
        }
    }
}
