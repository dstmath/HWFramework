package com.huawei.wallet.sdk.common.buscard.request;

import com.huawei.wallet.sdk.common.apdu.request.ServerAccessBaseRequest;

public class ServerAccessQueryOrderResultRequest extends ServerAccessBaseRequest {
    public ServerAccessQueryOrderResultRequest(String cplc, String seChipManuFacturer, String deviceMode, String appletAid, String issuerId) {
        setCplc(cplc);
        setSeChipManuFacturer(seChipManuFacturer);
        setDeviceModel(deviceMode);
        setAppletAid(appletAid);
        setIssuerId(issuerId);
    }
}
