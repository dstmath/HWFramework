package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessCancelEidRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.CancelEIDResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.response.DeleteAppletResponse;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class EIdCardServerOperator {
    private static final String TYPE_ID_CARD = "12";
    private String mAid;
    private Context mContext;
    private String mCplc;
    private String mIssuerId;
    private String mSeChipManuFacturer = ProductConfigUtil.geteSEManufacturer();
    private String sn = PhoneDeviceUtil.getSerialNumber();

    public EIdCardServerOperator(Context mContext2, String mIssuerId2, String mAid2) {
        this.mContext = mContext2;
        this.mIssuerId = mIssuerId2;
        this.mAid = mAid2;
    }

    public void cancel() {
        IdCardServerAccessService.getInstance(this.mContext).cancel();
        EIdCardServerAccessService.getInstance(this.mContext).cancel();
    }

    public EIdCardServerOperatorResult deleteSSD() {
        getCplc();
        ServerAccessDeleteAppletRequest request = new ServerAccessDeleteAppletRequest(this.mIssuerId, this.mCplc, this.mAid, Build.MODEL, this.mSeChipManuFacturer);
        request.setSn(this.sn);
        request.setCardType("12");
        request.setOnlyDeleteApplet("false");
        request.setReason("4");
        DeleteAppletResponse response = IdCardServerAccessService.getInstance(this.mContext).deleteApplet(request);
        EIdCardServerOperatorResult result = new EIdCardServerOperatorResult();
        int resultCode = response.getResultCode();
        if (resultCode == 0 || resultCode == 5002) {
            result.setResultCode(0);
        } else {
            result.setResultCode(response.getResultCode());
            result.setErrorCodeInfo(response.getErrorInfo());
        }
        result.setResultMsg(response.getResultDesc());
        return result;
    }

    public EIdCardServerOperatorResult cancelEID() {
        ServerAccessCancelEidRequest request = new ServerAccessCancelEidRequest();
        request.setIsNeedServiceTokenAuth(true);
        getCplc();
        request.setCplc(this.mCplc);
        CancelEIDResponse response = EIdCardServerAccessService.getInstance(this.mContext).cancelEID(request);
        EIdCardServerOperatorResult result = new EIdCardServerOperatorResult();
        if (response.getResultCode() != 0) {
            result.setResultCode(response.getResultCode());
            result.setErrorCodeInfo(response.getErrorInfo());
        } else {
            result.setResultCode(0);
        }
        result.setResultMsg(response.getResultDesc());
        return result;
    }

    private void getCplc() {
        if (this.mCplc == null) {
            this.mCplc = IdCardServerAccessService.getInstance(this.mContext).getCplc();
        }
    }
}
