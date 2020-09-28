package com.huawei.payments;

import android.content.Intent;

public interface Wallet {
    Intent getIntent();

    String getPackageName();

    String getWalletName();

    boolean isVaildSignature();
}
