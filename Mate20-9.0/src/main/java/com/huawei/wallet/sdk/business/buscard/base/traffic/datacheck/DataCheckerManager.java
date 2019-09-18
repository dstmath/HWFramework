package com.huawei.wallet.sdk.business.buscard.base.traffic.datacheck;

import java.util.HashMap;
import java.util.Map;

public final class DataCheckerManager {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile DataCheckerManager sInstance;
    private Map<String, DataChecker> dataCheckers = new HashMap();

    private DataCheckerManager() {
        this.dataCheckers.put("9156000014010001", new BJCardDataChecker());
    }

    public static DataCheckerManager getInstance() {
        if (sInstance == null) {
            synchronized (SYNC_LOCK) {
                if (sInstance == null) {
                    sInstance = new DataCheckerManager();
                }
            }
        }
        return sInstance;
    }

    public DataChecker getDataCheckerForCard(String aid) {
        return this.dataCheckers.get(aid);
    }
}
