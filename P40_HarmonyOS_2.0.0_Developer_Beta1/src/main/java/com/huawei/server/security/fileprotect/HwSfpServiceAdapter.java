package com.huawei.server.security.fileprotect;

public class HwSfpServiceAdapter extends DefaultHwSfpService {
    private static final Object LOCK = new Object();
    private static HwSfpService sHwSfpService;
    private static volatile HwSfpServiceAdapter sInstance;

    private HwSfpServiceAdapter() {
        sHwSfpService = HwSfpService.getInstance();
    }

    public static HwSfpServiceAdapter getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwSfpServiceAdapter();
                }
            }
        }
        return sInstance;
    }

    public void notifyUnlockScreen(int userId) {
        HwSfpService hwSfpService = sHwSfpService;
        if (hwSfpService != null) {
            hwSfpService.notifyUnlockScreen(userId);
        }
    }
}
