package com.huawei.server.security.fileprotect;

public class DefaultHwSfpService {
    private static final Object LOCK = new Object();
    private static volatile DefaultHwSfpService sInstance;

    public static DefaultHwSfpService getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DefaultHwSfpService();
                }
            }
        }
        return sInstance;
    }

    public void notifyUnlockScreen(int userId) {
    }
}
