package com.huawei.wallet.sdk.business.bankcard.api;

import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;

public interface TsmOperationService {
    TsmParamQueryResponse queryCreateSSDTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryDeleteAppletTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryDeleteSSDTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryInfoInitTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryInfoSynTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryInstallTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryLockAppletTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryUnLockEseTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryUnockAppletTsmParam(TsmParamQueryRequest tsmParamQueryRequest);

    TsmParamQueryResponse queryUpdateTsmParam(TsmParamQueryRequest tsmParamQueryRequest);
}
