package com.huawei.privacyability;

import huawei.android.security.privacyability.IDAnonymizationManagerImpl;

public class IDAnonymizationManager {
    private static volatile IDAnonymizationManager sInstance = null;
    private final IDAnonymizationManagerImpl mIDAnonymizationManagerImpl = IDAnonymizationManagerImpl.getInstance();

    private IDAnonymizationManager() {
    }

    public static IDAnonymizationManager getInstance() {
        if (sInstance == null) {
            synchronized (IDAnonymizationManager.class) {
                if (sInstance == null) {
                    sInstance = new IDAnonymizationManager();
                }
            }
        }
        return sInstance;
    }

    public String getCUID() {
        return this.mIDAnonymizationManagerImpl.getCUID();
    }

    public String getCFID(String containerID, String contentProviderTag) {
        return this.mIDAnonymizationManagerImpl.getCFID(containerID, contentProviderTag);
    }

    public int resetCUID() {
        return this.mIDAnonymizationManagerImpl.resetCUID();
    }
}
