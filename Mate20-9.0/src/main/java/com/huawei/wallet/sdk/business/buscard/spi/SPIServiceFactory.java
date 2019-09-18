package com.huawei.wallet.sdk.business.buscard.spi;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.spi.ServerAccessService;
import com.huawei.wallet.sdk.common.apdu.spi.impl.ServerAccessServiceImpl;

public class SPIServiceFactory {
    public static ServerAccessService createServerAccessService(Context context) {
        return ServerAccessServiceImpl.getInstance(context, AddressNameMgr.MODULE_NAME_TRANSPORTATIONCARD);
    }
}
