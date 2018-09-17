package com.huawei.hsm.permission;

import android.util.Log;
import java.util.ArrayList;

public class ANRFilter {
    private static final String TAG = "ANRFilter";
    private static ANRFilter sANRFilterInstance = null;
    private ArrayList<Integer> mSkipedUidFIFO = new ArrayList();

    private ANRFilter() {
    }

    public static synchronized ANRFilter getInstance() {
        ANRFilter aNRFilter;
        synchronized (ANRFilter.class) {
            if (sANRFilterInstance == null) {
                sANRFilterInstance = new ANRFilter();
            }
            aNRFilter = sANRFilterInstance;
        }
        return aNRFilter;
    }

    public synchronized boolean addUid(int uid) {
        this.mSkipedUidFIFO.add(Integer.valueOf(uid));
        return true;
    }

    public synchronized boolean removeUid(int uid) {
        this.mSkipedUidFIFO.remove(Integer.valueOf(uid));
        return true;
    }

    public synchronized boolean checkUid(int uid) {
        boolean exist;
        exist = this.mSkipedUidFIFO.contains(Integer.valueOf(uid));
        if (exist) {
            Log.d(TAG, "exist uid:" + uid);
        }
        return exist;
    }
}
