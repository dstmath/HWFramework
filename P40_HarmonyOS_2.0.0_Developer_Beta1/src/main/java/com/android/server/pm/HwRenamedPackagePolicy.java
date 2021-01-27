package com.android.server.pm;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.util.Map;

public class HwRenamedPackagePolicy {
    public static final int EXCLUSIVE_INSTALL = 4;
    private static final boolean IS_DEBUG = SystemProperties.get("ro.dbg.pms_log", "0").equals("on");
    private static final int KEY_PAIRS_LEN = 2;
    public static final int MIGRATE_CE_DIR = 2;
    public static final int MIGRATE_DE_DIR = 1;
    private static final Map<String, Integer> POLICY_MAP = new ArrayMap();
    private static final String TAG = "HwRenamedPackagePolicy";
    private int mAppId;
    private String mNewPackageName;
    private String mOriginalPackageName;
    private IHwPackageManagerInner mPmsInner = null;
    private int mPolicyFlags;

    static {
        POLICY_MAP.put("migrate_de", 1);
        POLICY_MAP.put("migrate_ce", 2);
        POLICY_MAP.put("exclusive_install", 4);
    }

    public HwRenamedPackagePolicy(String newPackageName, String strPolicy) {
        this.mPolicyFlags = 0;
        this.mAppId = 0;
        this.mNewPackageName = newPackageName;
        if (!TextUtils.isEmpty(strPolicy)) {
            for (String section : strPolicy.split(",")) {
                parseSection(section);
            }
        }
    }

    public String getOriginalPackageName() {
        return this.mOriginalPackageName;
    }

    public String getNewPackageName() {
        return this.mNewPackageName;
    }

    public void setAppId(int appId) {
        this.mAppId = appId;
    }

    public int getAppId() {
        return this.mAppId;
    }

    public int getPolicyFlags() {
        return this.mPolicyFlags;
    }

    private void parseSection(String section) {
        String[] keyPairs = section.split(":");
        if (keyPairs != null && keyPairs.length == 2) {
            String key = keyPairs[0];
            String value = keyPairs[1];
            if ("other-package-name".equals(key)) {
                this.mOriginalPackageName = value;
                Log.i(TAG, this.mNewPackageName + " declared old package name is:" + this.mOriginalPackageName);
            } else if ("policy".equals(key)) {
                String[] policies = value.split("\\|");
                for (String subPolicy : policies) {
                    Log.i(TAG, this.mNewPackageName + " declared policy:" + subPolicy);
                    if (POLICY_MAP.containsKey(subPolicy)) {
                        this.mPolicyFlags |= POLICY_MAP.get(subPolicy).intValue();
                    }
                }
            }
        }
    }

    public boolean checkFlags(int flags) {
        Log.d(TAG, "checkFlags mPolicyFlags=" + Integer.toHexString(this.mPolicyFlags) + ", flags=" + Integer.toHexString(flags));
        return (this.mPolicyFlags & flags) != 0;
    }
}
