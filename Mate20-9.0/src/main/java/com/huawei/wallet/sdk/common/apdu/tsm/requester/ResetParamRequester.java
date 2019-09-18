package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.http.service.CommonService;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class ResetParamRequester extends TSMOperateParamRequester {
    private static final String SIGN_CERT = "RSAWithCert";
    private int mediaType;
    private String randomStr;

    public ResetParamRequester(Context context, String rand, int mediaType2) {
        super(context, "resetOpt", mediaType2);
        this.randomStr = rand;
        this.mediaType = mediaType2;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        CommonService service = new CommonService(this.mContext, AddressNameMgr.MODULE_NAME_WISECLOUDVIRTUALCARD);
        paramQueryRequest.setSignType(SIGN_CERT);
        paramQueryRequest.setTerminal(Build.MODEL);
        paramQueryRequest.setCplc(ESEInfoManager.getInstance(this.mContext).queryCplcByMediaType(this.mediaType));
        paramQueryRequest.setRandomStr(this.randomStr);
        paramQueryRequest.setDeviceId(PhoneDeviceUtil.getDeviceID(this.mContext));
        return service.ssdReset(paramQueryRequest);
    }
}
