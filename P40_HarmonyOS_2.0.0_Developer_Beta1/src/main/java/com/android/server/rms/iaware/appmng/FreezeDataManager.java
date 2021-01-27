package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareLog;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.util.ArrayList;
import java.util.List;

public class FreezeDataManager {
    private static final int DEFAULT_IM_COUNT = -1;
    private static final Object LOCK = new Object();
    private static final int MAX_IM_COUNT = 5;
    private static final String TAG = "FreezeDataManager";
    private static FreezeDataManager sInstance;

    private FreezeDataManager() {
    }

    public static FreezeDataManager getInstance() {
        FreezeDataManager freezeDataManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new FreezeDataManager();
            }
            freezeDataManager = sInstance;
        }
        return freezeDataManager;
    }

    public List<String> getFrequentIm(int count) {
        return updateHabitImEmail(count);
    }

    private List<String> updateHabitImEmail(int count) {
        List<String> freqTopImList = new ArrayList<>();
        if (count < -1 || count == 0) {
            return freqTopImList;
        }
        int maxCount = count;
        if (maxCount > 5) {
            maxCount = 5;
        }
        if (AwareUserHabit.getInstance() == null || !AwareUserHabit.getInstance().isEnable()) {
            return freqTopImList;
        }
        List<String> freqTopImList2 = AwareUserHabit.getInstance().getMostFreqAppByType(0, maxCount);
        AwareLog.d(TAG, "freqTopImList is " + freqTopImList2);
        return freqTopImList2 == null ? new ArrayList() : freqTopImList2;
    }
}
