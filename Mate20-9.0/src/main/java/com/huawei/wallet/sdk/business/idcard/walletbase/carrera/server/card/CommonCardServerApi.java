package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.server.card;

import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;

public interface CommonCardServerApi {
    ServerAccessApplyAPDUResponse applyAPDU(ServerAccessApplyAPDURequest serverAccessApplyAPDURequest);

    ServerAccessDeleteAppletResponse deleteApplet(ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest);
}
