package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.constant.ServerAddressConstant;

public class CtidCardServer extends IdCardServer {
    private static final String TAG = "IDCard:CtidCardServer";

    public CtidCardServer(Context context) {
        super(context);
        this.modelName = ServerAddressConstant.CTID_CARD_MODULE_NAME;
    }
}
