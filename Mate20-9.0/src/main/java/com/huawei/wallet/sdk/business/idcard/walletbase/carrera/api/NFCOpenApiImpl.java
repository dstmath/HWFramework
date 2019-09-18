package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.api;

import android.app.Activity;
import android.content.Context;

public final class NFCOpenApiImpl implements NFCOpenApi {
    private static final String KEY_FIRST_INTO_CARDLIST = "key_first_into_cardlist";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile NFCOpenApiImpl instance;
    private Context mContext;

    public static NFCOpenApi getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new NFCOpenApiImpl(context);
                }
            }
        }
        return instance;
    }

    private NFCOpenApiImpl(Context context) {
        if (context instanceof Activity) {
            this.mContext = context.getApplicationContext();
        } else {
            this.mContext = context;
        }
    }

    public boolean isAutoOpenNFC(Context context) {
        return true;
    }
}
