package com.huawei.wallet.sdk.business.buscard.api;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;

public class QueryCplcResultTask implements Runnable {
    private Handler handler;
    private Context mContext;
    private String mCplc;

    private class Task implements Runnable {
        private Task() {
        }

        public void run() {
        }
    }

    public QueryCplcResultTask(Context context, Handler operateResultHandler) {
        this.mContext = context;
        this.handler = operateResultHandler;
    }

    public void run() {
        this.mCplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        if (TextUtils.isEmpty(this.mCplc)) {
            this.mCplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        }
        this.handler.post(new Task());
    }
}
