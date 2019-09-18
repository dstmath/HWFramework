package com.huawei.wallet.sdk.common.apdu;

import android.content.Context;
import com.huawei.wallet.sdk.common.log.LogC;

public final class BaseCommonContext {
    private static BaseCommonContext instance = new BaseCommonContext();
    private Context applicationContext = null;

    private BaseCommonContext() {
    }

    public static BaseCommonContext getInstance() {
        return instance;
    }

    public void initContext(Context context) {
        if (this.applicationContext != null) {
            LogC.d("initBackGround applicationContext init not null!", false);
        } else if (context != null) {
            this.applicationContext = context.getApplicationContext();
        } else {
            LogC.e("initBackGround applicationContext init failed! context==null", false);
        }
    }

    public Context getApplicationContext() {
        return this.applicationContext;
    }
}
