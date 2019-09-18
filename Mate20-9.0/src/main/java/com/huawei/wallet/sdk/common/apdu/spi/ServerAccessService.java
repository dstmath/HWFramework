package com.huawei.wallet.sdk.common.apdu.spi;

import com.huawei.wallet.sdk.business.buscard.base.spi.request.ApplyOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.QueryOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.TransferOutRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.ApplyOrderResponse;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.QueryOrderResponse;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.TransferOutResponse;
import com.huawei.wallet.sdk.common.apdu.request.DeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.DeleteAppletResponse;

public interface ServerAccessService {
    ApplyOrderResponse applyOrder(ApplyOrderRequest applyOrderRequest);

    TransferOutResponse checkCloudTransferOut(TransferOutRequest transferOutRequest);

    TransferOutResponse cloudTransferOut(TransferOutRequest transferOutRequest);

    TransferOutResponse cloudTransferOut(TransferOutRequest transferOutRequest, int i);

    DeleteAppletResponse deleteApplet(DeleteAppletRequest deleteAppletRequest);

    DeleteAppletResponse deleteApplet(DeleteAppletRequest deleteAppletRequest, int i);

    QueryOrderResponse queryOrder(QueryOrderRequest queryOrderRequest);

    TransferOutResponse transferOut(TransferOutRequest transferOutRequest);

    TransferOutResponse transferOut(TransferOutRequest transferOutRequest, int i);
}
