package com.huawei.payments;

import android.content.Context;
import android.os.Binder;
import android.text.TextUtils;

public class WalletFactory {
    private WalletFactory() {
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Wallet getWalletFactory(Context context, String walletType) {
        if (context == null || TextUtils.isEmpty(walletType) || context.checkPermission("com.huawei.permission.WALLET_FACTORY_ACCESS", Binder.getCallingPid(), Binder.getCallingUid()) != 0 || !walletType.equalsIgnoreCase("Paytm")) {
            return null;
        }
        return new Paytm(context);
    }
}
