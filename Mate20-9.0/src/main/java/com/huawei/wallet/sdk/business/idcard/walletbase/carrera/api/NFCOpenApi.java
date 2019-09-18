package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.api;

import android.content.Context;

public interface NFCOpenApi {
    public static final String AUTO_ENABLE_NFC = "auto_enable_nfc";

    boolean isAutoOpenNFC(Context context);
}
