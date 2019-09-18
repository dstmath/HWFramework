package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.response.DeleteAppletResponse;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class IdCardServerOperator {
    private static final String TYPE_ID_CARD = "12";
    private String mAid;
    private Context mContext;
    private String mCplc;
    private String mIssuerId;
    private String mSeChipManuFacturer = ProductConfigUtil.geteSEManufacturer();
    private CtidCardServerAccessService mServerAccessService;
    private String sn = PhoneDeviceUtil.getSerialNumber();

    public IdCardServerOperator(Context mContext2, String mIssuerId2, String mAid2) {
        this.mContext = mContext2;
        this.mAid = mAid2;
        this.mIssuerId = mIssuerId2;
        this.mCplc = IdCardServerAccessService.getInstance(mContext2).getCplc();
        this.mServerAccessService = CtidCardServerAccessService.getInstance(mContext2);
    }

    public IdCardServerOperatorResult deleteSSD() {
        ServerAccessDeleteAppletRequest request = new ServerAccessDeleteAppletRequest(this.mIssuerId, this.mCplc, this.mAid, Build.MODEL, this.mSeChipManuFacturer);
        request.setSn(this.sn);
        request.setCardType("12");
        request.setReason("4");
        request.setOnlyDeleteApplet("false");
        DeleteAppletResponse response = this.mServerAccessService.deleteApplet(request);
        IdCardServerOperatorResult result = new IdCardServerOperatorResult();
        int resultCode = response.getResultCode();
        if (resultCode == 0 || resultCode == 5002) {
            result.setResultCode(0);
        } else {
            result.setResultCode(response.getResultCode());
        }
        result.setResultMsg(response.getResultDesc());
        return result;
    }
}
