package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareLog;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.util.ArrayList;
import java.util.List;

public class FreezeDataManager {
    private static final int DEFAULT_IM_COUNT = -1;
    private static final int MAX_IM_COUNT = 5;
    private static final String TAG = "FreezeDataManager";
    private static FreezeDataManager mInstance;
    private static final Object mLock = new Object();

    private FreezeDataManager() {
    }

    public static FreezeDataManager getInstance() {
        FreezeDataManager freezeDataManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new FreezeDataManager();
            }
            freezeDataManager = mInstance;
        }
        return freezeDataManager;
    }

    public List<String> getFrequentIM(int count) {
        return updateHabitImEmail(count);
    }

    private List<String> updateHabitImEmail(int count) {
        if (count < -1 || count == 0) {
            return null;
        }
        int maxCount = count;
        if (count > 5) {
            maxCount = 5;
        }
        if (AwareUserHabit.getInstance() == null || (AwareUserHabit.getInstance().isEnable() ^ 1) != 0) {
            return null;
        }
        List<String> mostFreqAppByType = AwareUserHabit.getInstance().getMostFreqAppByType(0, maxCount);
        AwareLog.d(TAG, "freqTopIMList is " + mostFreqAppByType);
        if (mostFreqAppByType == null) {
            mostFreqAppByType = new ArrayList();
        }
        return mostFreqAppByType;
    }
}
