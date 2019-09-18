package com.huawei.wallet.sdk;

import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.common.log.LogC;

public class ResetRoundOffThread extends HandlerThread {
    private static Handler mHandler;
    private static ResetRoundOffThread mInstance;

    public ResetRoundOffThread() {
        super("ThreadName", 0);
    }

    public static void init() {
        if (mInstance == null) {
            mInstance = new ResetRoundOffThread();
            mInstance.start();
            mHandler = new Handler(mInstance.getLooper());
        }
        LogC.i("WalletFactory|ResetRoundOffThread|init.", false);
    }

    public static void postDelayed(Runnable runnable, long delayTime) {
        mHandler.postDelayed(runnable, delayTime);
        LogC.i("WalletFactory|ResetRoundOffThread|post delay runnable.", false);
    }

    public static void destory() {
        LogC.i("WalletFactory|ResetRoundOffThread|destory.", false);
        if (mInstance != null) {
            mInstance.quit();
            mInstance = null;
            mHandler = null;
        }
    }
}
