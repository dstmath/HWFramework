package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import android.content.Context;

public class AppletInfoApiFactory {
    public static AppletCardInfoReadApi createAppletCardInfoReader(Context context) {
        return new AppletCardInfoReader(context);
    }
}
