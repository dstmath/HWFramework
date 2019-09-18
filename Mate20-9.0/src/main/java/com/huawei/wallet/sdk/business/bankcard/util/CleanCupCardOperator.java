package com.huawei.wallet.sdk.business.bankcard.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.business.bankcard.api.HandleDeleteLocalCardsCallback;
import com.huawei.wallet.sdk.business.bankcard.task.CleanAllLocalBankCardsTask;
import com.huawei.wallet.sdk.common.log.LogC;

public class CleanCupCardOperator {
    private static CleanCupCardOperator instance = new CleanCupCardOperator();
    private static Handler taskHandler;

    public static void startCleanBankCard(Context context, HandleDeleteLocalCardsCallback mCallback, boolean canRetry) {
        LogC.i("CleanCupCardOperator|startCleanBankCard.", false);
        HandlerThread thread = new HandlerThread("SDK_MainActivity", 10);
        thread.start();
        taskHandler = new Handler(thread.getLooper());
        taskHandler.post(new CleanAllLocalBankCardsTask(context, mCallback, canRetry));
    }
}
