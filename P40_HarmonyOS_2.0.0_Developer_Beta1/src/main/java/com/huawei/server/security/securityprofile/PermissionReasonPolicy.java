package com.huawei.server.security.securityprofile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.FileUtils;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Xml;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public final class PermissionReasonPolicy {
    private static final List<String> ABROAD_FIXED_PACKAGES = Arrays.asList("com.huawei.hwid");
    private static final int ADD = 1;
    private static final String DATA_SYSTEM_DIR = "/data/system/";
    private static final String DEFAULT_POLICY = "SUPPORTED";
    private static final List<String> FIXED_PACKAGES = Arrays.asList("com.huawei.wear.guard", "com.huawei.parentcontrol.parent", "com.huawei.bone", "com.huawei.hag.assistant", "com.huawei.cloud", "com.huawei.allianceapp", "com.huawei.welinknow", "com.huawei.cloudtwopizza.storm.digixtalk");
    private static final String FLAG_XML_CONFIG_STRING = "string";
    private static final String FLAG_XML_CONFIG_VALUE = "values";
    private static final PermissionReasonPolicy INSTANCE = new PermissionReasonPolicy();
    private static final boolean IS_CHINA_AREA = SecurityProfileUtils.IS_CHINA_AREA;
    private static final boolean IS_DEFAULT_FEATURE_ENABLE = true;
    private static final boolean IS_DEFAULT_PERMITTED = false;
    public static final int PERMISSION_FLAGS_UNKNOWN = -1;
    private static final int PERMISSION_FLAG_REASON_ENABLE = 2;
    private static final int PERMISSION_FLAG_REASON_PERMITTED = 1;
    private static final String PERMISSION_SETTINGS_FILE = "securityPermissionSettings.json";
    private static final String POLICY_FORCED_RESTRICTED = "FORCED_RESTRICTED";
    private static final String POLICY_NO_RESTRICTED = "NO_RESTRICTED";
    private static final String POLICY_SEPARATOR = ";";
    private static final String POLICY_SUPPORT = "SUPPORTED";
    private static final String PREFERENCE_KEY = "permission_reason_policy";
    private static final String PRELOAD_TAG = "preload";
    private static final int REMOVE = 0;
    private static final String TAG = "SecurityProfile_Permission";
    private static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    private Context mContext;
    private String mCurrentPolicy = "SUPPORTED";
    private boolean mIsDefaultPermitted = false;
    private boolean mIsFeatureEnable = true;
    private final Object mMemoryLock = new Object();
    private PolicyEngine mPolicyEngine;
    private WeakReference<Map<String, Boolean>> mSecuritySettings = new WeakReference<>(new HashMap());

    private PermissionReasonPolicy() {
    }

    static PermissionReasonPolicy getInstance() {
        return INSTANCE;
    }

    private static byte[] readFully(File file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[HwSecDiagnoseConstant.BIT_RPROC];
            while (true) {
                int count = fis.read(buffer);
                if (count != -1) {
                    out.write(buffer, 0, count);
                } else {
                    byte[] byteArray = out.toByteArray();
                    FileUtils.closeQuietly(out);
                    FileUtils.closeQuietly(fis);
                    return byteArray;
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to get permissionReasonPriority!");
        } catch (IOException e2) {
            Log.e(TAG, "Failed to read permissionReasonPriority: " + e2.getMessage());
        } catch (Throwable th) {
            FileUtils.closeQuietly(out);
            FileUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        FileUtils.closeQuietly(out);
        FileUtils.closeQuietly((AutoCloseable) null);
        return new byte[0];
    }

    /* access modifiers changed from: package-private */
    public void updateSecuritySettings(byte[] policyBlock) {
        Log.i(TAG, "update security settings immediately");
        synchronized (this.mMemoryLock) {
            if (initSecuritySettingsLocked(policyBlock, true)) {
                persistSecuritySettings(policyBlock);
            }
            recoverPolicyAnywaySyncLocked();
            if ("SUPPORTED".equals(this.mCurrentPolicy)) {
                if (this.mSecuritySettings.get() == null) {
                    initSecuritySettingsFromPersistence(true);
                }
                for (String packageName : (this.mSecuritySettings.get() != null ? this.mSecuritySettings.get() : new HashMap<>()).keySet()) {
                    updatePackagePolicy(packageName);
                }
            }
        }
    }

    private void persistSecuritySettings(byte[] policyBlock) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(new File(DATA_SYSTEM_DIR, PERMISSION_SETTINGS_FILE));
            for (byte b : policyBlock) {
                out.write(b);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to get target file!");
        } catch (IOException e2) {
            Log.e(TAG, "Failed to output to file: " + e2.getMessage());
        } catch (Throwable th) {
            FileUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        FileUtils.closeQuietly(out);
    }

    private boolean parseFeatureEnableOrDefault(JSONObject policy) {
        String text = policy.optString("isPermissionReasonEnable");
        if (!TextUtils.isEmpty(text)) {
            return Boolean.parseBoolean(text);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void init(Context context, PolicyEngine policyEngine) {
        this.mContext = context;
        this.mPolicyEngine = policyEngine;
        initSecuritySettingsFromPersistence(true);
    }

    /* access modifiers changed from: package-private */
    public void recoverPolicyIfNeeded() {
        SecurityProfileUtils.getWorkerThreadPool().execute(new Runnable() {
            /* class com.huawei.server.security.securityprofile.PermissionReasonPolicy.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (PermissionReasonPolicy.this.mMemoryLock) {
                    if (!PermissionReasonPolicy.this.getPolicyPreference().startsWith(PermissionReasonPolicy.this.mCurrentPolicy)) {
                        PermissionReasonPolicy.this.updatePolicyTo(PermissionReasonPolicy.this.mCurrentPolicy);
                        if ("SUPPORTED".equals(PermissionReasonPolicy.this.mCurrentPolicy)) {
                            PermissionReasonPolicy.this.recoverPolicyImpl();
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void recoverPolicyAnyway() {
        SecurityProfileUtils.getWorkerThreadPool().execute(new Runnable() {
            /* class com.huawei.server.security.securityprofile.PermissionReasonPolicy.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (PermissionReasonPolicy.this.mMemoryLock) {
                    PermissionReasonPolicy.this.recoverPolicyAnywaySyncLocked();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recoverPolicyAnywaySyncLocked() {
        if (!getPolicyPreference().startsWith(this.mCurrentPolicy)) {
            updatePolicyTo(this.mCurrentPolicy);
        }
        if ("SUPPORTED".equals(this.mCurrentPolicy)) {
            Log.i(TAG, "Recover all package policy on boot completed.");
            recoverPolicyImpl();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recoverPolicyImpl() {
        for (String packageName : SecurityProfileUtils.getInstalledPackages(this.mContext)) {
            updatePackagePolicy(packageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePolicyTo(String policy) {
        if (SecurityProfileUtils.DEBUG) {
            Log.d(TAG, "Write policy: " + policy);
        }
        if (!Settings.Secure.putString(this.mContext.getContentResolver(), PREFERENCE_KEY, policy)) {
            Log.w(TAG, "Update policy failed.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @NonNull
    private String getPolicyPreference() {
        String policy = Settings.Secure.getString(this.mContext.getContentResolver(), PREFERENCE_KEY);
        return policy == null ? BuildConfig.FLAVOR : policy;
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageSupported(String packageName) {
        String policy = getPolicyPreference();
        return POLICY_NO_RESTRICTED.equals(policy) || Arrays.asList(policy.split(POLICY_SEPARATOR)).contains(packageName);
    }

    /* access modifiers changed from: package-private */
    public void updatePackagePolicy(String packageName) {
        synchronized (this.mMemoryLock) {
            if (!isPermitted(packageName) || !hasPermissionReason(packageName)) {
                updatePackagePolicy(packageName, 0);
            } else {
                updatePackagePolicy(packageName, 1);
            }
        }
    }

    private boolean isPermitted(String packageName) {
        if (!this.mIsFeatureEnable) {
            Log.d(TAG, "Feature show policy not enable.");
            return false;
        } else if (IS_CHINA_AREA || ABROAD_FIXED_PACKAGES.contains(packageName)) {
            try {
                PackageManager pm = this.mContext.getPackageManager();
                if (pm != null) {
                    PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);
                    if (pkgInfo.applicationInfo.uid < 10000 || (pkgInfo.applicationInfo.flags & 1) != 0 || (pkgInfo.applicationInfo.flags & 128) != 0 || (pkgInfo.applicationInfo.flags & HwSecDiagnoseConstant.BIT_SYS_PROPS) != 0 || (pkgInfo.applicationInfo.flags & 2) != 0 || pm.checkSignatures(packageName, "android") >= 0 || FIXED_PACKAGES.contains(packageName) || isBeenUninstalled(packageName)) {
                        Log.d(TAG, "Permission reason is unchecked app type.");
                        return true;
                    }
                }
                return isConfigPermittedOrDefault(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Failed to find package when get permission reason status!");
                return false;
            }
        } else {
            Log.d(TAG, "Abroad area do not show policy.");
            return false;
        }
    }

    private boolean isConfigPermittedOrDefault(String packageName) {
        int permissionFlags = this.mPolicyEngine.getActivePermissionFlags(packageName);
        if (SecurityProfileUtils.DEBUG) {
            Log.d(TAG, "app gallery permission flags: " + permissionFlags);
        }
        if (this.mSecuritySettings.get() == null) {
            initSecuritySettingsFromPersistence(false);
        }
        Map<String, Boolean> securitySettings = this.mSecuritySettings.get() != null ? this.mSecuritySettings.get() : new ArrayMap<>();
        if (securitySettings.containsKey(packageName)) {
            Boolean raw = securitySettings.getOrDefault(packageName, Boolean.FALSE);
            boolean isSecurityPermitted = raw != null ? raw.booleanValue() : false;
            Log.i(TAG, packageName + " security permission settings: " + isSecurityPermitted);
            if (permissionFlags == -1) {
                return isSecurityPermitted;
            }
            if (!isSecurityPermitted || !isAppGalleryPermitted(permissionFlags)) {
                return false;
            }
            return true;
        } else if (permissionFlags != -1) {
            return isAppGalleryPermitted(permissionFlags);
        } else {
            return this.mIsDefaultPermitted;
        }
    }

    private boolean isAppGalleryPermitted(int permissionFlags) {
        return (permissionFlags & 2) == 0 && (permissionFlags & 1) != 0;
    }

    private void initSecuritySettingsFromPersistence(boolean isFullyInit) {
        File settingsFile = new File("/data/system/securityPermissionSettings.json");
        if (!settingsFile.exists()) {
            Log.d(TAG, "SecurityPermissionSettings not ready.");
            return;
        }
        byte[] content = readFully(settingsFile);
        synchronized (this.mMemoryLock) {
            initSecuritySettingsLocked(content, isFullyInit);
        }
    }

    private boolean initSecuritySettingsLocked(byte[] policyBlock, boolean isFullyInit) {
        try {
            long begin = System.currentTimeMillis();
            JSONObject policy = new JSONObject(new String(policyBlock, StandardCharsets.UTF_8));
            if (isFullyInit) {
                Log.i(TAG, "Init fully.");
                this.mIsFeatureEnable = parseFeatureEnableOrDefault(policy);
                this.mIsDefaultPermitted = parseDefaultPermitted(policy);
                this.mCurrentPolicy = this.mIsFeatureEnable ? this.mIsDefaultPermitted ? POLICY_NO_RESTRICTED : "SUPPORTED" : POLICY_FORCED_RESTRICTED;
            }
            if (!this.mIsFeatureEnable) {
                return true;
            }
            JSONObject packagePermissionFlags = policy.getJSONObject("packageAdditionals");
            Map<String, Boolean> securitySettings = new HashMap<>();
            Iterator<String> it = packagePermissionFlags.keys();
            while (it.hasNext()) {
                String packageName = it.next();
                try {
                    String permissionFlagsText = packagePermissionFlags.getJSONObject(packageName).getString("permissionFlags");
                    if (SecurityProfileUtils.DEBUG) {
                        Log.d(TAG, packageName + ", permission flags: 0x" + permissionFlagsText);
                    }
                    int permissionFlags = Integer.parseInt(permissionFlagsText, 16);
                    if ((permissionFlags & 2) != 0) {
                        if ((permissionFlags & 1) != 0) {
                            securitySettings.put(packageName, Boolean.TRUE);
                        } else {
                            securitySettings.put(packageName, Boolean.FALSE);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to get priority for " + packageName + " : " + e.getMessage());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Bad number format for " + packageName + " : " + e2.getMessage());
                }
            }
            this.mSecuritySettings = new WeakReference<>(securitySettings);
            if (SecurityProfileUtils.DEBUG) {
                Log.d(TAG, "init settings cost " + (System.currentTimeMillis() - begin) + " ms");
            }
            return true;
        } catch (JSONException e3) {
            Log.e(TAG, "Failed to parse permission settings:" + e3.getMessage());
            return false;
        }
    }

    private boolean parseDefaultPermitted(JSONObject policy) {
        String text = policy.optString("isDefaultPermitted");
        if (!TextUtils.isEmpty(text)) {
            return Boolean.parseBoolean(text);
        }
        return false;
    }

    private boolean hasPermissionReason(String packageName) {
        try {
            ApplicationInfo applicationInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 128);
            if (applicationInfo.metaData == null) {
                Log.w(TAG, "No meta in package: " + packageName);
                return false;
            }
            for (String key : applicationInfo.metaData.keySet()) {
                if (key != null && key.startsWith("permission.reason.")) {
                    return true;
                }
            }
            Log.i(TAG, packageName + " not adapted.");
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Package not found");
        }
    }

    private void updatePackagePolicy(String packageName, int action) {
        if (!TextUtils.isEmpty(packageName)) {
            String policyStr = getPolicyPreference();
            if (!policyStr.startsWith("SUPPORTED")) {
                Log.i(TAG, "Policy is not SUPPORTED.");
                recoverPolicyIfNeeded();
                return;
            }
            List<String> policy = new ArrayList<>(Arrays.asList(policyStr.split(POLICY_SEPARATOR)));
            if (action != 0) {
                if (action != 1) {
                    Log.w(TAG, "Unsupported action: " + action);
                } else if (!policy.contains(packageName)) {
                    policy.add(packageName);
                    updatePolicyTo(String.join(POLICY_SEPARATOR, (CharSequence[]) policy.toArray(new String[0])));
                }
            } else if (policy.contains(packageName)) {
                policy.remove(packageName);
                updatePolicyTo(String.join(POLICY_SEPARATOR, (CharSequence[]) policy.toArray(new String[0])));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0086, code lost:
        r6 = r0.getAttributeValue(null, "codePath");
        r13 = java.lang.System.currentTimeMillis();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0092, code lost:
        if (com.huawei.server.security.securityprofile.SecurityProfileUtils.DEBUG == false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0094, code lost:
        android.util.Log.d(com.huawei.server.security.securityprofile.PermissionReasonPolicy.TAG, "isBeenUninstalled execution time: " + (r13 - r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r8 = isSystemApp(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ba, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00fa, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x010f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0162  */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A[RETURN, SYNTHETIC] */
    private boolean isBeenUninstalled(String packageName) {
        Throwable th;
        int outerDepth;
        long startTime = System.currentTimeMillis();
        try {
            FileInputStream stream = new FileInputStream(new File(DATA_SYSTEM_DIR, UNINSTALLED_DELAPP_FILE));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                int type = parser.next();
                while (type != 1 && type != 2) {
                    type = parser.next();
                }
                String tag = parser.getName();
                if (FLAG_XML_CONFIG_VALUE.equals(tag)) {
                    parser.next();
                    int outerDepth2 = parser.getDepth();
                    while (true) {
                        int type2 = parser.next();
                        if (type2 == 1) {
                            break;
                        }
                        if (type2 == 3) {
                            if (parser.getDepth() <= outerDepth2) {
                                break;
                            }
                        }
                        if (type2 == 3) {
                            outerDepth = outerDepth2;
                        } else if (type2 == 4) {
                            outerDepth = outerDepth2;
                        } else if (FLAG_XML_CONFIG_STRING.equals(parser.getName())) {
                            String pkgName = parser.getAttributeValue(null, "name");
                            if (!TextUtils.isEmpty(pkgName)) {
                                try {
                                    if (pkgName.equals(packageName)) {
                                        break;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    try {
                                        throw th;
                                    } catch (Throwable th3) {
                                        th.addSuppressed(th3);
                                    }
                                }
                            }
                            outerDepth = outerDepth2;
                        } else {
                            outerDepth = outerDepth2;
                        }
                        outerDepth2 = outerDepth;
                    }
                    stream.close();
                    long endTime = System.currentTimeMillis();
                    if (!SecurityProfileUtils.DEBUG) {
                        return false;
                    }
                    Log.d(TAG, "isBeenUninstalled get exception execution time: " + (endTime - startTime));
                    return false;
                }
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
            throw th;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Get XmlParser failed with FileNotFoundException.");
            long endTime2 = System.currentTimeMillis();
            if (!SecurityProfileUtils.DEBUG) {
            }
        } catch (XmlPullParserException e2) {
            Log.w(TAG, "Get XmlParser failed with XmlPullParserException.");
            long endTime22 = System.currentTimeMillis();
            if (!SecurityProfileUtils.DEBUG) {
            }
        } catch (IOException e3) {
            Log.w(TAG, "Get XmlParser failed with IOException.");
            long endTime222 = System.currentTimeMillis();
            if (!SecurityProfileUtils.DEBUG) {
            }
        } catch (Exception e4) {
            Exception e5 = e4;
            Log.w(TAG, "Get XmlParser failed with unexpected Exception: " + e5.getClass().getSimpleName());
            long endTime2222 = System.currentTimeMillis();
            if (!SecurityProfileUtils.DEBUG) {
            }
        }
    }

    private boolean isSystemApp(String codePath) {
        if (SecurityProfileUtils.DEBUG) {
            Log.d(TAG, "isSystemApp query:" + codePath);
        }
        if (TextUtils.isEmpty(codePath) || codePath.indexOf(PRELOAD_TAG) > 0) {
            return false;
        }
        return true;
    }
}
