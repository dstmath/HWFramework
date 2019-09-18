package com.android.server.security.securityprofile;

import android.content.Context;
import android.util.Slog;
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

public class PolicyDatabase {
    public static final String DEFAULT_POLICY = "{\n\"overlay_labels\": {},\"domains\": {},\"rules\": [        {        \"decision\": \"deny\",        \"subject\": \"ANY\",        \"object\": \"Black\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\"        },        {        \"decision\": \"allowif\",        \"subject\": \"ANY\",        \"object\": \"Red\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\",        \"conditions\":  { \"state\": \"NoScreenRecording\" }        },        {        \"decision\": \"allowafter\",        \"subject\": \"ANY\",        \"object\": \"Red\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\",        \"conditions\":  { \"action\": { \"name\": \"StopScreenRecording\" },              \"timeout\": 1000,              \"state\": \"NoScreenRecording\"            }         },        {        \"decision\": \"deny\",        \"subject\": \"ANY\",        \"object\": \"Red\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\"        },        {        \"decision\": \"deny\",        \"subject\": \"ANY\",        \"object\": \"Red\",        \"subsystem\": \"MediaProjection\",        \"operation\": \"Record\"        },        {        \"decision\": \"allow\",        \"subject\": \"ANY\",        \"object\": \"ANY\",        \"subsystem\": \"MediaProjection\",        \"operation\": \"Record\"        },        {        \"decision\": \"allow\",        \"subject\": \"ANY\",        \"object\": \"ANY\",        \"subsystem\": \"Intent\",        \"operation\": \"Send\"        }]}";
    public static final String NEED_POLICY_RECOVER = "need_policy_recover";
    private static final String TAG = "SecurityProfileService";
    private JSONObject mActivePolicy = this.mActiveStorage.readDatabase();
    private PolicyStorage mActiveStorage = new PolicyStorage("/data/system/securityprofile.json", null);
    private Context mContext;
    private PolicyStorage mDatabaseStorage = new PolicyStorage("/data/system/securityprofiledb.json", DEFAULT_POLICY);
    private JSONObject mPolicyDatabase = this.mDatabaseStorage.readDatabase();

    interface DigestHelper {
        boolean matches(ApkDigest apkDigest);
    }

    PolicyDatabase(Context context) {
        this.mContext = context;
        if (this.mActivePolicy == null) {
            rebuildPolicy();
            setPolicyRecoverFlagInner(true);
            importOldBlackListDatabase();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNeedPolicyRecover() {
        return this.mActivePolicy.optBoolean(NEED_POLICY_RECOVER, false);
    }

    private void writePolicyDatabase() {
        this.mDatabaseStorage.writeDatabase(this.mPolicyDatabase);
    }

    private void writeActivePolicy() {
        this.mActiveStorage.writeDatabase(this.mActivePolicy);
    }

    private boolean samePolicyTarget(JSONObject p1, JSONObject p2) {
        if (p1 == null && p2 == null) {
            return true;
        }
        if (p1 == null || p2 == null) {
            return false;
        }
        if (p1.optJSONObject("apk_digest").optString("digest").equals(p2.optJSONObject("apk_digest").optString("digest"))) {
            return true;
        }
        return false;
    }

    public void addPolicy(JSONObject policy) {
        JSONObject jSONObject = policy;
        if (jSONObject.optInt("policy_language_version", 1) == 1) {
            Set<String> updatedDomains = new HashSet<>();
            JSONObject domains = jSONObject.optJSONObject("domains");
            try {
                Iterator<String> iter = domains.keys();
                while (iter.hasNext()) {
                    String domain = iter.next();
                    JSONArray domainPolicies = domains.optJSONArray(domain);
                    JSONArray existingDomainPolicies = this.mPolicyDatabase.optJSONObject("domains").optJSONArray(domain);
                    if (existingDomainPolicies == null) {
                        existingDomainPolicies = new JSONArray();
                        this.mPolicyDatabase.optJSONObject("domains").put(domain, existingDomainPolicies);
                    }
                    int i = 0;
                    int i2 = 0;
                    while (i2 < domainPolicies.length()) {
                        JSONObject domainPolicy = domainPolicies.optJSONObject(i2);
                        boolean matchedOldPolicy = false;
                        int j = i;
                        while (true) {
                            if (j >= existingDomainPolicies.length()) {
                                break;
                            }
                            JSONObject existingDomainPolicy = existingDomainPolicies.optJSONObject(j);
                            if (samePolicyTarget(domainPolicy, existingDomainPolicy)) {
                                matchedOldPolicy = true;
                                if (domainPolicy.optInt("policy_version", i) > existingDomainPolicy.optInt("policy_version", i)) {
                                    existingDomainPolicies.put(j, domainPolicy);
                                    updatedDomains.add(domain);
                                } else {
                                    Slog.i(TAG, "this policy_version is old than exit policy do not need update! domain:" + domain);
                                    Slog.i(TAG, "domainPolicy:" + domainPolicy);
                                    Slog.i(TAG, "existingDomainPolicy:" + existingDomainPolicy);
                                }
                            } else {
                                j++;
                                i = 0;
                            }
                        }
                        if (!matchedOldPolicy) {
                            existingDomainPolicies.put(domainPolicy);
                            updatedDomains.add(domain);
                        }
                        i2++;
                        i = 0;
                    }
                }
                writePolicyDatabase();
                updateDomainPolicies(new ArrayList(updatedDomains));
            } catch (JSONException e) {
                Slog.e(TAG, e.getMessage());
            }
        }
    }

    public JSONObject getPolicy() {
        return this.mActivePolicy;
    }

    private JSONObject deepCopyJSONObject(JSONObject o) {
        try {
            return new JSONObject(o.toString());
        } catch (JSONException e) {
            Slog.e(TAG, e.getMessage());
            return new JSONObject();
        }
    }

    private JSONObject emptyDomainPolicy() {
        try {
            JSONObject result = new JSONObject();
            result.put("labels", new JSONArray());
            return result;
        } catch (JSONException e) {
            Slog.e(TAG, e.getMessage());
            return new JSONObject();
        }
    }

    /* access modifiers changed from: package-private */
    public int indexOfJSONArray(JSONArray array, String value) {
        for (int i = 0; i < array.length(); i++) {
            if (value.equals(array.optString(i))) {
                return i;
            }
        }
        return -1;
    }

    public void removeLabel(String label) {
        try {
            List<String> packageList = new ArrayList<>();
            JSONObject overlayLabels = this.mPolicyDatabase.getJSONObject("overlay_labels");
            if (overlayLabels == null) {
                Slog.e(TAG, "overlayLabels is null, removeLabel must return,label:" + label);
                return;
            }
            Iterator<String> iter = overlayLabels.keys();
            while (iter.hasNext()) {
                packageList.add(iter.next());
            }
            Slog.i(TAG, "removeLabel all packageList:" + packageList);
            removeLabel(packageList, label);
        } catch (Exception e) {
            Slog.e(TAG, "removeLabel all err:" + e.getMessage());
        }
    }

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
            Slog.e(TAG, e.getMessage());
        }
    }

    public void removeLabel(List<String> packageList, String label) {
        try {
            JSONObject overlayLabels = this.mPolicyDatabase.getJSONObject("overlay_labels");
            for (String packageName : packageList) {
                Slog.d(TAG, "removeLabel:" + packageName);
                JSONArray packageOverlayLabels = overlayLabels.optJSONArray(packageName);
                if (packageOverlayLabels != null) {
                    int idx = indexOfJSONArray(packageOverlayLabels, label);
                    if (idx != -1) {
                        Slog.d(TAG, "removeLabel:" + packageName + ",label:" + label);
                        packageOverlayLabels.remove(idx);
                        if (packageOverlayLabels.length() == 0) {
                            Slog.d(TAG, "removeLabel:" + packageName + ",has no overlay_labels left,remove package name from overlay_labels");
                            overlayLabels.remove(packageName);
                        }
                    }
                }
            }
            writePolicyDatabase();
            updateDomainPolicies(packageList);
        } catch (JSONException e) {
            Slog.e(TAG, e.getMessage());
        }
    }

    public void updatePackageInformation(String packageName) {
        Slog.d(TAG, "updatePackageInformation,packageName:" + packageName);
        updateDomainPolicies(Arrays.asList(new String[]{packageName}));
        updatePackageSigningInfo(packageName);
        writeActivePolicy();
    }

    public void setPolicyRecoverFlag(boolean need) {
        setPolicyRecoverFlagInner(need);
    }

    private void setPolicyRecoverFlagInner(boolean need) {
        if (need) {
            try {
                this.mActivePolicy.put(NEED_POLICY_RECOVER, need);
            } catch (JSONException e) {
                Slog.w(TAG, "setPolicyRecoverFlag: " + need + " fail");
                return;
            }
        } else {
            this.mActivePolicy.remove(NEED_POLICY_RECOVER);
        }
        writeActivePolicy();
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d6 A[LOOP:1: B:34:0x00d6->B:36:0x00dc, LOOP_START, PHI: r2 
      PHI: (r2v1 'i' int) = (r2v0 'i' int), (r2v2 'i' int) binds: [B:33:0x00d3, B:36:0x00dc] A[DONT_GENERATE, DONT_INLINE]] */
    private JSONObject resolveActiveDomainPolicy(String packageName, DigestHelper digestHelper) {
        JSONObject selectedPolicy;
        JSONArray domainOverlayLabels;
        JSONObject domains = this.mPolicyDatabase.optJSONObject("domains");
        if (domains == null) {
            try {
                this.mPolicyDatabase.put("domains", new JSONObject());
            } catch (JSONException e) {
                Slog.e(TAG, e.getMessage());
                return emptyDomainPolicy();
            }
        }
        if (domains != null) {
            try {
                if (domains.has(packageName)) {
                    JSONObject latestMatch = null;
                    JSONArray domainPolicies = domains.optJSONArray(packageName);
                    Slog.d(TAG, "resolveActiveDomainPolicy has packageName:" + packageName + ",potential policies:" + String.valueOf(domainPolicies.length()));
                    int latestVersion = 0;
                    for (int i = 0; i < domainPolicies.length(); i++) {
                        JSONObject domainPolicy = domainPolicies.optJSONObject(i);
                        JSONObject digest = domainPolicy.optJSONObject("apk_digest");
                        if (digest == null || digestHelper.matches(jsonToApkDigest(digest))) {
                            Slog.d(TAG, "resolveActiveDomainPolicy match");
                            int version = domainPolicy.optInt("policy_version", 0);
                            if (version > latestVersion) {
                                latestMatch = domainPolicy;
                                latestVersion = version;
                            }
                        }
                    }
                    selectedPolicy = latestMatch != null ? deepCopyJSONObject(latestMatch) : emptyDomainPolicy();
                    domainOverlayLabels = this.mPolicyDatabase.optJSONObject("overlay_labels").optJSONArray(packageName);
                    if (domainOverlayLabels != null) {
                        for (int i2 = 0; i2 < domainOverlayLabels.length(); i2++) {
                            selectedPolicy.optJSONArray("labels").put(domainOverlayLabels.optString(i2));
                        }
                    }
                    return selectedPolicy;
                }
            } catch (Exception e2) {
                Slog.e(TAG, "rules err:" + e2.getMessage());
                selectedPolicy = emptyDomainPolicy();
            }
        }
        selectedPolicy = emptyDomainPolicy();
        domainOverlayLabels = this.mPolicyDatabase.optJSONObject("overlay_labels").optJSONArray(packageName);
        if (domainOverlayLabels != null) {
        }
        return selectedPolicy;
    }

    private boolean isEmptyDomainPolicy(JSONObject policy) {
        if (policy != null) {
            return policy.optJSONArray("labels").length() == 0 && policy.length() == 1;
        }
        return true;
    }

    private void resolveActiveDomainPolicies(List<String> packages, JSONObject resultDomains) throws JSONException {
        for (String packageName : packages) {
            try {
                String packagePath = SecurityProfileUtils.getInstalledApkPath(packageName, this.mContext);
                if (packagePath == null) {
                    Slog.w(TAG, "resolveActiveDomainPolicies packagePath is null, packageName:" + packageName);
                    resultDomains.put(packageName, null);
                } else {
                    JSONObject selectedPolicy = resolveActiveDomainPolicy(packageName, new DigestHelper(packagePath) {
                        private final /* synthetic */ String f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final boolean matches(ApkDigest apkDigest) {
                            return DigestMatcher.packageMatchesDigest(this.f$0, apkDigest);
                        }
                    });
                    if (!isEmptyDomainPolicy(selectedPolicy)) {
                        Slog.d(TAG, "resolveActiveDomainPolicies packageName:" + packageName + ", selectedPolicy:" + selectedPolicy);
                        resultDomains.put(packageName, selectedPolicy);
                    } else {
                        resultDomains.put(packageName, null);
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, "resolveActiveDomainPolicies err:" + e.getMessage());
            }
        }
    }

    private ApkDigest jsonToApkDigest(JSONObject json) {
        return new ApkDigest(json.optString("signature_scheme", "v2"), json.optString("digest_algorithm", "SHA-256"), json.optString("digest"));
    }

    private JSONObject getActiveDomainPolicy(String packageName) {
        JSONObject activeDomainPolicy = this.mActivePolicy.optJSONObject("domains").optJSONObject(packageName);
        if (activeDomainPolicy == null) {
            return emptyDomainPolicy();
        }
        return activeDomainPolicy;
    }

    public List<String> getLabels(String packageName, ApkDigest digest) {
        JSONObject activeDomainPolicy;
        if (digest != null) {
            activeDomainPolicy = resolveActiveDomainPolicy(packageName, new DigestHelper(digest) {
                private final /* synthetic */ ApkDigest f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean matches(ApkDigest apkDigest) {
                    return apkDigest.base64Digest.equals(this.f$0.base64Digest);
                }
            });
        } else {
            activeDomainPolicy = getActiveDomainPolicy(packageName);
        }
        JSONArray labels = activeDomainPolicy.optJSONArray("labels");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < labels.length(); i++) {
            result.add(labels.optString(i));
        }
        return result;
    }

    public void updateDomainPolicies(List<String> packageList) {
        try {
            resolveActiveDomainPolicies(packageList, this.mActivePolicy.optJSONObject("domains"));
            writeActivePolicy();
        } catch (JSONException e) {
            Slog.e(TAG, e.getMessage());
        }
    }

    private void importOldBlackListDatabase() {
        addLabel(B200DatabaseImporter.getBlackListAndDeleteDatabase(this.mContext), "Black");
    }

    public boolean isPackageSigned(String packageName) {
        try {
            return this.mActivePolicy.optJSONObject("signedPackages").optBoolean(packageName, false);
        } catch (Exception e) {
            Slog.e(TAG, "isPackageSigned err:" + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void setPackageSigned(String packageName, boolean isPackageSigned) {
        if (isPackageSigned) {
            try {
                this.mActivePolicy.optJSONObject("signedPackages").put(packageName, isPackageSigned);
            } catch (JSONException e) {
                Slog.e(TAG, "setPackageSigned json not found err:" + e.getMessage());
            }
        } else {
            this.mActivePolicy.optJSONObject("signedPackages").remove(packageName);
        }
    }

    private void updatePackageSigningInfo(String packageName) {
        try {
            String packagePath = SecurityProfileUtils.getInstalledApkPath(packageName, this.mContext);
            if (packagePath == null) {
                this.mActivePolicy.optJSONObject("signedPackages").remove(packageName);
                Slog.i(TAG, "update PackageSigned Info Action:remove, packageName:" + packageName);
                return;
            }
            boolean hasValidPolicy = PolicyVerifier.packageHasValidPolicy(packageName, packagePath);
            if (hasValidPolicy) {
                this.mActivePolicy.optJSONObject("signedPackages").put(packageName, hasValidPolicy);
            } else {
                this.mActivePolicy.optJSONObject("signedPackages").remove(packageName);
            }
            Slog.i(TAG, "update PackageSigned Info Action:put, packageName:" + packageName + ",hasValidPolicy:" + hasValidPolicy);
        } catch (JSONException e) {
            Slog.e(TAG, "update PackageSigned Info json not found err:" + e.getMessage());
        } catch (Exception e2) {
            Slog.e(TAG, "update PackageSigned Info err:" + e2.getMessage());
        }
    }

    private void rebuildPolicy() {
        try {
            Slog.d(TAG, "rebuildPolicy");
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
            Slog.e(TAG, e.getMessage());
        }
    }
}
