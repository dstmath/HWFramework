package com.huawei.wallet.sdk.business.buscard.impl;

import android.content.Context;
import android.os.Handler;
import android.util.SparseArray;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess.ServerAccessImp;

public class SPIOperatorManager {
    private final Object lock = new Object();
    private final SparseArray<TrafficCardOperator> mTrafficCardOperators = new SparseArray<>();

    public SPIOperatorManager(Context context, Handler handler) {
        this.mTrafficCardOperators.put(20, new ServerAccessImp(context));
    }

    public TrafficCardOperator getTrafficCardOpertor(int mode) {
        TrafficCardOperator trafficCardOperator;
        synchronized (this.lock) {
            if (mode == 22) {
                mode = 20;
            }
            trafficCardOperator = this.mTrafficCardOperators.get(mode);
        }
        return trafficCardOperator;
    }
}
