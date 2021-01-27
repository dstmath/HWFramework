package com.huawei.security.fileprotect;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.hwpartsecurityservices.BuildConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HwdpsPackageInfo {
    private static final int INVALID_USERID = -1;
    private static final String POLICY_KEY_PATH = "path";
    private static final String TAG = "HwdpsPackageInfo";
    private int mAppId;
    private String mProtectPolicys;

    public HwdpsPackageInfo(int appId, String protectPolicys) {
        this.mAppId = appId;
        this.mProtectPolicys = protectPolicys;
    }

    public String getProtectPolicys() {
        return this.mProtectPolicys;
    }

    public static class Builder {
        private static final String ESCAPE_REGEX = "\\\\";
        private int mAppId = -1;
        private List<HwdpsPolicy> mProtectPolicys = new ArrayList();

        public Builder setAppId(int appId) {
            this.mAppId = appId;
            return this;
        }

        public Builder addDefaultPolicy(String dataDir) {
            if (TextUtils.isEmpty(dataDir)) {
                return this;
            }
            this.mProtectPolicys.add(new HwdpsPolicy(dataDir));
            return this;
        }

        public HwdpsPackageInfo build() {
            return new HwdpsPackageInfo(this.mAppId, generatePolicys(this.mProtectPolicys));
        }

        private String generatePolicys(List<HwdpsPolicy> policys) {
            HwdpsPolicy policy;
            if (policys == null || policys.isEmpty() || (policy = policys.get(0)) == null) {
                return null;
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(HwdpsPackageInfo.POLICY_KEY_PATH, policy.mDataDir);
            } catch (JSONException e) {
                Log.w(HwdpsPackageInfo.TAG, "Json constuct falied!");
            }
            return jsonObject.toString().replaceAll(ESCAPE_REGEX, BuildConfig.FLAVOR);
        }
    }

    /* access modifiers changed from: private */
    public static class HwdpsPolicy {
        private String mDataDir;

        private HwdpsPolicy(String dataDir) {
            this.mDataDir = dataDir;
            String str = this.mDataDir;
            if (str != null && !str.endsWith(File.separator)) {
                this.mDataDir += File.separator;
            }
        }
    }
}
