package com.huawei.server.security.securityprofile;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import huawei.android.security.securityprofile.ApkDigest;
import huawei.android.security.securityprofile.DigestMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public class PolicyDatabase {
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final int DEFAULT_PACKAGES_WITH_OVERLAY_LABELS_CAPACITY = 16;
    private static final int DEFAULT_PACKAGE_LABELS_CAPACITY = 16;
    private static final String DEFAULT_POLICY = ("{" + System.lineSeparator() + "\"overlay_labels\": {},\"domains\": {},\"rules\": [        {        \"decision\": \"deny\",        \"subject\": \"ANY\",        \"object\": \"Black\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\"        },        {        \"decision\": \"allow\",        \"subject\": \"ANY\",        \"object\": \"ANY\",        \"subsystem\": \"MediaProjection\",        \"operation\": \"Record\"        },        {        \"decision\": \"allow\",        \"subject\": \"ANY\",        \"object\": \"ANY\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\"        }]}");
    private static final int DEFAULT_UPDATING_DOMAINS_POLICY_CAPACITY = 32;
    private static final String NEED_POLICY_RECOVER = "need_policy_recover";
    private static final String TAG = "SecurityProfileService";
    private boolean isNewVersionFirstBoot = false;
    private JSONObject mActivePolicy;
    private PolicyStorage mActiveStorage;
    private Context mContext;
    private PolicyStorage mDatabaseStorage;
    private Handler mHandler;
    private JSONObject mPolicyDatabase;

    /* access modifiers changed from: package-private */
    public interface DigestHelper {
        boolean matches(ApkDigest apkDigest);
    }

    PolicyDatabase(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mDatabaseStorage = new PolicyStorage("/data/system/securityprofiledb.json", DEFAULT_POLICY);
        this.mPolicyDatabase = this.mDatabaseStorage.readDatabase();
        this.mActiveStorage = new PolicyStorage("/data/system/securityprofile.json", null);
        this.mActivePolicy = this.mActiveStorage.readDatabase();
        if (this.mPolicyDatabase != null && this.mActivePolicy == null) {
            rebuildPolicy();
            setPolicyRecoverFlagInner(true);
            importOldBlackListDatabase();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedPolicyRecover() {
        return this.mActivePolicy.optBoolean(NEED_POLICY_RECOVER, false);
    }

    private void writePolicyDatabase() {
        this.mDatabaseStorage.writeDatabase(this.mPolicyDatabase);
    }

    private void writeActivePolicy() {
        this.mActiveStorage.writeDatabase(this.mActivePolicy);
    }

    private boolean samePolicyTarget(JSONObject jsonObj1, JSONObject jsonObj2) throws JSONException {
        if (jsonObj1 == null && jsonObj2 == null) {
            return true;
        }
        if (jsonObj1 == null || jsonObj2 == null) {
            return false;
        }
        if (jsonObj1.getJSONObject("apk_digest").getString("digest").equals(jsonObj2.getJSONObject("apk_digest").getString("digest"))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void addPolicy(JSONObject policy) {
        if (!isPolicyLanguageVersionInvalid(policy)) {
            Set<String> updatedDomains = new HashSet<>(32);
            try {
                JSONObject domains = policy.getJSONObject("domains");
                Iterator<String> iterator = domains.keys();
                while (iterator.hasNext()) {
                    String domain = iterator.next();
                    JSONArray domainPolicies = domains.getJSONArray(domain);
                    JSONArray existingDomainPolicies = getExistingDomainPolicies(domain);
                    int updatingsLen = domainPolicies.length();
                    for (int i = 0; i < updatingsLen; i++) {
                        JSONObject domainPolicy = domainPolicies.getJSONObject(i);
                        boolean matchedOldPolicy = false;
                        int j = 0;
                        int existingsLen = existingDomainPolicies.length();
                        while (true) {
                            if (j >= existingsLen) {
                                break;
                            }
                            JSONObject existingDomainPolicy = existingDomainPolicies.getJSONObject(j);
                            if (!samePolicyTarget(domainPolicy, existingDomainPolicy)) {
                                j++;
                            } else {
                                matchedOldPolicy = true;
                                if (isDomainPolicyVersionForward(domainPolicy, existingDomainPolicy)) {
                                    existingDomainPolicies.put(j, domainPolicy);
                                    updatedDomains.add(domain);
                                }
                            }
                        }
                        if (!matchedOldPolicy) {
                            existingDomainPolicies.put(domainPolicy);
                            updatedDomains.add(domain);
                        }
                    }
                }
                writePolicyDatabase();
                updateDomainPolicies(new ArrayList(updatedDomains));
            } catch (JSONException e) {
                Log.e(TAG, "addPolicy JSONException: " + e.getMessage());
            }
        }
    }

    @NonNull
    private JSONArray getExistingDomainPolicies(String domain) throws JSONException {
        JSONArray existingDomainPolicies = this.mPolicyDatabase.getJSONObject("domains").optJSONArray(domain);
        if (existingDomainPolicies != null) {
            return existingDomainPolicies;
        }
        JSONArray existingDomainPolicies2 = new JSONArray();
        this.mPolicyDatabase.getJSONObject("domains").put(domain, existingDomainPolicies2);
        return existingDomainPolicies2;
    }

    private boolean isDomainPolicyVersionForward(JSONObject domainPolicy, JSONObject existingDomainPolicy) {
        boolean isForward = false;
        if (domainPolicy.optInt("policy_version", 0) > existingDomainPolicy.optInt("policy_version", 0)) {
            isForward = true;
        }
        if (DEBUG && !isForward) {
            Log.d(TAG, "this policy_version is older than exist policy, domainPolicy: " + domainPolicy + ", existingDomainPolicy: " + existingDomainPolicy);
        }
        return isForward;
    }

    private boolean isPolicyLanguageVersionInvalid(JSONObject policy) {
        return policy.optInt("policy_language_version", 1) != 1;
    }

    /* access modifiers changed from: package-private */
    public JSONObject getPolicy() {
        return this.mActivePolicy;
    }

    @NonNull
    private JSONObject deepCopyJSONObject(@Nullable JSONObject jsonObj) {
        if (jsonObj != null) {
            return new JSONObject(jsonObj.toString());
        }
        try {
            Log.w(TAG, "deepCopyJSONObject arg null!");
            return new JSONObject();
        } catch (JSONException e) {
            Log.e(TAG, "deepCopyJSONObject " + e.getMessage());
            return new JSONObject();
        }
    }

    private JSONObject emptyDomainPolicy() {
        try {
            JSONObject result = new JSONObject();
            result.put("labels", new JSONArray());
            result.put("rules", new JSONArray());
            return result;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return new JSONObject();
        }
    }

    private int indexOfJSONArray(JSONArray array, String value) {
        int len = array.length();
        for (int i = 0; i < len; i++) {
            if (value.equals(array.optString(i))) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void removeLabel(String label) {
        try {
            List<String> packageList = new ArrayList<>(16);
            JSONObject overlayLabels = this.mPolicyDatabase.getJSONObject("overlay_labels");
            if (overlayLabels == null) {
                Log.e(TAG, "overlayLabels is null, removeLabel must return, label: " + label);
                return;
            }
            Iterator<String> iterator = overlayLabels.keys();
            while (iterator.hasNext()) {
                packageList.add(iterator.next());
            }
            removeLabel(packageList, label);
        } catch (JSONException e) {
            Log.e(TAG, "removeLabel all json exception: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void addLabel(List<String> packageList, String label) {
        try {
            for (String packageName : packageList) {
                JSONArray packageOverlayLabels = this.mPolicyDatabase.getJSONObject("overlay_labels").optJSONArray(packageName);
                if (packageOverlayLabels == null) {
                    packageOverlayLabels = new JSONArray();
                    this.mPolicyDatabase.getJSONObject("overlay_labels").put(packageName, packageOverlayLabels);
                }
                if (indexOfJSONArray(packageOverlayLabels, label) == -1) {
                    packageOverlayLabels.put(label);
                }
            }
            writePolicyDatabase();
            updateDomainPolicies(packageList);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void removeLabel(List<String> packageList, String label) {
        try {
            JSONObject overlayLabels = this.mPolicyDatabase.getJSONObject("overlay_labels");
            for (String packageName : packageList) {
                JSONArray packageOverlayLabels = overlayLabels.optJSONArray(packageName);
                if (packageOverlayLabels != null) {
                    int index = indexOfJSONArray(packageOverlayLabels, label);
                    if (index != -1) {
                        if (DEBUG) {
                            Log.d(TAG, "removeLabel: " + packageName + ", label: " + label);
                        }
                        packageOverlayLabels.remove(index);
                        if (packageOverlayLabels.length() == 0) {
                            if (DEBUG) {
                                Log.d(TAG, "removeLabel: " + packageName + ", has no overlay_labels left, remove package name from overlay_labels");
                            }
                            overlayLabels.remove(packageName);
                        }
                    }
                }
            }
            writePolicyDatabase();
            updateDomainPolicies(packageList);
        } catch (JSONException e) {
            Log.e(TAG, "removeLabel failed: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePackageInformation(String packageName) {
        updateDomainPolicies(Arrays.asList(packageName));
        updatePackageSigningInfo(packageName);
        writeActivePolicy();
    }

    /* access modifiers changed from: package-private */
    public void setPolicyRecoverFlag(boolean need) {
        setPolicyRecoverFlagInner(need);
    }

    private void setPolicyRecoverFlagInner(boolean need) {
        if (need) {
            try {
                this.mActivePolicy.put(NEED_POLICY_RECOVER, true);
            } catch (JSONException e) {
                Log.w(TAG, "setPolicyRecoverFlag: fail");
                return;
            }
        } else {
            this.mActivePolicy.remove(NEED_POLICY_RECOVER);
        }
        writeActivePolicy();
    }

    private JSONObject resolveActiveDomainPolicy(String packageName, DigestHelper digestHelper) throws JSONException {
        JSONObject selectedPolicy;
        int version;
        JSONObject domains = this.mPolicyDatabase.optJSONObject("domains");
        if (domains == null) {
            try {
                this.mPolicyDatabase.put("domains", new JSONObject());
            } catch (JSONException e) {
                Log.e(TAG, "Failed to add domains field for policy database: " + e.getMessage());
                return emptyDomainPolicy();
            }
        }
        if (domains == null || !domains.has(packageName)) {
            selectedPolicy = emptyDomainPolicy();
        } else {
            int latestVersion = 0;
            JSONObject latestMatch = null;
            JSONArray domainPolicies = domains.getJSONArray(packageName);
            int domainPoliciesLen = domainPolicies.length();
            for (int i = 0; i < domainPoliciesLen; i++) {
                JSONObject domainPolicy = domainPolicies.getJSONObject(i);
                JSONObject digest = domainPolicy.optJSONObject("apk_digest");
                if ((digest == null || digestHelper.matches(jsonToApkDigest(digest))) && (version = domainPolicy.optInt("policy_version", 0)) > latestVersion) {
                    latestMatch = domainPolicy;
                    latestVersion = version;
                }
            }
            selectedPolicy = latestMatch != null ? deepCopyJSONObject(latestMatch) : emptyDomainPolicy();
        }
        JSONArray domainOverlayLabels = this.mPolicyDatabase.getJSONObject("overlay_labels").optJSONArray(packageName);
        if (domainOverlayLabels != null) {
            int labelsLen = domainOverlayLabels.length();
            for (int i2 = 0; i2 < labelsLen; i2++) {
                selectedPolicy.getJSONArray("labels").put(domainOverlayLabels.optString(i2));
            }
        }
        return selectedPolicy;
    }

    private boolean isEmptyDomainPolicy(JSONObject policy) {
        if (policy != null) {
            return policy.optJSONArray("labels").length() == 0 && policy.length() == 1;
        }
        return true;
    }

    private void resolveActiveDomainPolicies(List<String> packages, JSONObject resultDomains) {
        for (String packageName : packages) {
            try {
                final String packagePath = SecurityProfileUtils.getInstalledApkPath(packageName, this.mContext);
                if (packagePath == null) {
                    Log.w(TAG, "resolve active domains but apkPath is null, packageName: " + packageName);
                    resultDomains.put(packageName, (Object) null);
                } else {
                    JSONObject selectedPolicy = resolveActiveDomainPolicy(packageName, new DigestHelper() {
                        /* class com.huawei.server.security.securityprofile.PolicyDatabase.AnonymousClass1 */

                        @Override // com.huawei.server.security.securityprofile.PolicyDatabase.DigestHelper
                        public boolean matches(ApkDigest digest) {
                            return DigestMatcher.packageMatchesDigest(packagePath, digest);
                        }
                    });
                    if (!isEmptyDomainPolicy(selectedPolicy)) {
                        if (DEBUG) {
                            Log.d(TAG, "resolve active domains packageName: " + packageName + ", selected active domains: " + selectedPolicy);
                        }
                        resultDomains.put(packageName, selectedPolicy);
                    } else {
                        resultDomains.put(packageName, (Object) null);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse active domains json policy: " + e.getMessage());
            } catch (Exception e2) {
                Log.e(TAG, "Failed to match package digest!");
            }
        }
    }

    private ApkDigest jsonToApkDigest(JSONObject json) {
        return new ApkDigest(json.optString("signature_scheme", "v2"), json.optString("digest_algorithm", "SHA-256"), json.optString("digest"));
    }

    private JSONObject getActiveDomainPolicy(String packageName) throws JSONException {
        JSONObject activeDomainPolicy = this.mActivePolicy.getJSONObject("domains").optJSONObject(packageName);
        if (activeDomainPolicy == null) {
            return emptyDomainPolicy();
        }
        return activeDomainPolicy;
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public List<String> getLabels(String packageName, final ApkDigest digest) {
        JSONObject activeDomainPolicy;
        List<String> result = new ArrayList<>(16);
        if (digest != null) {
            try {
                activeDomainPolicy = resolveActiveDomainPolicy(packageName, new DigestHelper() {
                    /* class com.huawei.server.security.securityprofile.PolicyDatabase.AnonymousClass2 */

                    @Override // com.huawei.server.security.securityprofile.PolicyDatabase.DigestHelper
                    public boolean matches(ApkDigest apkDigest) {
                        return apkDigest.base64Digest.equals(digest.base64Digest);
                    }
                });
            } catch (JSONException e) {
                Log.w(TAG, "Failed to get label: " + e.getMessage());
            }
        } else {
            activeDomainPolicy = getActiveDomainPolicy(packageName);
        }
        JSONArray labels = activeDomainPolicy.getJSONArray("labels");
        int labelsLen = labels.length();
        for (int i = 0; i < labelsLen; i++) {
            result.add(labels.optString(i));
        }
        return result;
    }

    private void updateDomainPolicies(List<String> packageList) {
        if (packageList != null && packageList.size() != 0) {
            resolveActiveDomainPolicies(packageList, this.mActivePolicy.optJSONObject("domains"));
            writeActivePolicy();
            Message message = Message.obtain();
            message.what = 1;
            message.obj = packageList;
            this.mHandler.sendMessage(message);
        }
    }

    private void importOldBlackListDatabase() {
        addLabel(B200DatabaseImporter.getBlackListAndDeleteDatabase(this.mContext), "Black");
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageSigned(String packageName) {
        try {
            return this.mActivePolicy.getJSONObject("signedPackages").optBoolean(packageName, false);
        } catch (JSONException e) {
            Log.w(TAG, "isPackageSigned failed find field signedPackages!");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPackageSigned(String packageName, boolean isPackageSigned) {
        if (isPackageSigned) {
            try {
                this.mActivePolicy.getJSONObject("signedPackages").put(packageName, true);
            } catch (JSONException e) {
                Log.e(TAG, "setPackageSigned json not found err: " + e.getMessage());
            }
        } else {
            this.mActivePolicy.getJSONObject("signedPackages").remove(packageName);
        }
    }

    private void updatePackageSigningInfo(String packageName) {
        String packagePath = SecurityProfileUtils.getInstalledApkPath(packageName, this.mContext);
        if (packagePath == null) {
            setPackageSigned(packageName, false);
            Log.w(TAG, "update PackageSigned Info Action: remove, packageName: " + packageName);
            return;
        }
        boolean hasValidPolicy = PolicyVerifier.packageHasValidPolicy(packageName, packagePath);
        if (hasValidPolicy) {
            setPackageSigned(packageName, true);
        } else {
            setPackageSigned(packageName, false);
        }
        if (DEBUG) {
            Log.i(TAG, "update PackageSigned Info Action: put, packageName: " + packageName + ", hasValidPolicy: " + hasValidPolicy);
        }
    }

    private void rebuildPolicy() {
        if (DEBUG) {
            Log.d(TAG, "rebuildPolicy");
        }
        try {
            List<String> installedPackages = SecurityProfileUtils.getInstalledPackages(this.mContext);
            JSONObject result = deepCopyJSONObject(this.mPolicyDatabase);
            JSONObject resultDomains = new JSONObject();
            result.put("domains", resultDomains);
            resolveActiveDomainPolicies(installedPackages, resultDomains);
            result.put("signedPackages", new JSONObject());
            this.mActivePolicy = result;
            for (String packageName : installedPackages) {
                updatePackageSigningInfo(packageName);
            }
            writeActivePolicy();
        } catch (JSONException e) {
            Log.e(TAG, "rebuildPolicy failed: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public int getActivePermissionFlags(String packageName) {
        try {
            JSONObject pkgPolicy = this.mActivePolicy.getJSONObject("domains").optJSONObject(packageName);
            if (pkgPolicy != null) {
                int flags = pkgPolicy.optInt("permissionFlags", -1);
                if (flags != -1) {
                    return flags;
                }
                Log.w(TAG, "PermissionFlags is missing.");
            } else {
                Log.w(TAG, "Package is missing");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get domains field: " + e.getMessage());
        }
        return -1;
    }
}
