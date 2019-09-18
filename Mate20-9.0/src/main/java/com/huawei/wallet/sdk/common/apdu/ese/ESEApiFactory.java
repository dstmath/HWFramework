package com.huawei.wallet.sdk.common.apdu.ese;

import android.content.Context;
import com.huawei.wallet.sdk.common.apdu.ese.api.ESEInfoManagerApi;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;

public class ESEApiFactory {
    public static ESEInfoManagerApi createESEInfoManagerApi(Context context) {
        return ESEInfoManager.getInstance(context);
    }
}
