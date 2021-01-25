package com.huawei.privacyability;

import huawei.android.security.privacyability.diffprivacy.DiffPrivacyManagerImpl;

public class DiffPrivacyManager {
    private static volatile DiffPrivacyManager sInstance = null;
    private final DiffPrivacyManagerImpl mDiffPrivacyManagerImpl = DiffPrivacyManagerImpl.getInstance();

    private DiffPrivacyManager() {
    }

    public static DiffPrivacyManager getInstance() {
        if (sInstance == null) {
            synchronized (DiffPrivacyManager.class) {
                if (sInstance == null) {
                    sInstance = new DiffPrivacyManager();
                }
            }
        }
        return sInstance;
    }

    public String diffPrivacyBloomfilter(String data, String parameter) {
        return this.mDiffPrivacyManagerImpl.diffPrivacyBloomfilter(data, parameter);
    }

    public String diffPrivacyBitshistogram(int[] data, String parameter) {
        return this.mDiffPrivacyManagerImpl.diffPrivacyBitshistogram(data, parameter);
    }

    public String diffPrivacyCountsketch(String data, String parameter) {
        return this.mDiffPrivacyManagerImpl.diffPrivacyCountsketch(data, parameter);
    }

    public String diffPrivacyWordfilter(String data, String parameter) {
        return this.mDiffPrivacyManagerImpl.diffPrivacyWordfilter(data, parameter);
    }
}
