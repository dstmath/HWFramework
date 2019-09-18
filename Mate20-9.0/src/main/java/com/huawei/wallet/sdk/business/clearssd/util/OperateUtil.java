package com.huawei.wallet.sdk.business.clearssd.util;

import android.content.Context;
import com.huawei.wallet.sdk.business.clearssd.request.RandomRequest;
import com.huawei.wallet.sdk.business.clearssd.response.RandomResponse;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperateResponse;
import com.huawei.wallet.sdk.common.apdu.tsm.TSMOperator;
import com.huawei.wallet.sdk.common.http.service.CommonService;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.PhoneFeatureAdaptUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class OperateUtil {
    /* access modifiers changed from: private */
    public static int currType;
    /* access modifiers changed from: private */
    public static List<Integer> eseStr = new ArrayList();
    private static boolean isTried = false;
    /* access modifiers changed from: private */
    public static Context mContext;
    /* access modifiers changed from: private */
    public static ClearSSDInterface operateInterface;
    /* access modifiers changed from: private */
    public static boolean ssdFlag = false;

    public static boolean isSSD() {
        return ssdFlag;
    }

    public static void delSSD(Context context, ClearSSDInterface listener) {
        mContext = context;
        operateInterface = listener;
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            public void run() {
                LogC.i("OperateUtil|delSSD|start", false);
                List unused = OperateUtil.eseStr = PhoneFeatureAdaptUtil.getEseDeviceList();
                if (OperateUtil.eseStr.size() > 0) {
                    int i = 0;
                    while (i < OperateUtil.eseStr.size()) {
                        int unused2 = OperateUtil.currType = ((Integer) OperateUtil.eseStr.get(i)).intValue();
                        CommonService service = new CommonService(OperateUtil.mContext, AddressNameMgr.MODULE_NAME_WISECLOUDVIRTUALCARD);
                        RandomRequest randomRequest = new RandomRequest();
                        randomRequest.setCplc(ESEInfoManager.getInstance(OperateUtil.mContext).queryCplcByMediaType(OperateUtil.currType));
                        randomRequest.setDeviceId(PhoneDeviceUtil.getDeviceID(OperateUtil.mContext));
                        RandomResponse randomResponse = service.ssdGetRandom(randomRequest);
                        if (randomResponse.returnCode == 0) {
                            if (!StringUtil.isEmpty(randomResponse.getRand(), true)) {
                                boolean unused3 = OperateUtil.ssdFlag = true;
                                TSMOperateResponse response = TSMOperator.getInstance(OperateUtil.mContext).resetOpt(randomResponse.getRand(), OperateUtil.currType);
                                boolean unused4 = OperateUtil.ssdFlag = false;
                                if (response.getResultCode() == 100000) {
                                    LogC.i("OperateUtil|dealData|resetOpt，i:" + i + "  currType:" + OperateUtil.currType, false);
                                    if (OperateUtil.operateInterface != null && i == OperateUtil.eseStr.size() - 1) {
                                        LogC.i("OperateUtil|dealData|resetOpt，success", false);
                                        OperateUtil.operateInterface.onEventBack(0, "reset success");
                                    }
                                } else {
                                    LogC.i("OperateUtil|dealData|resetOpt，fail ResultCode=" + response.getResultCode(), false);
                                    OperateUtil.judgeTryOrFinish(OperateUtil.mContext);
                                    return;
                                }
                            }
                            i++;
                        } else {
                            LogC.i("OperateUtil|dealData|resetOpt，fail ResultCode=" + randomResponse.getResultCode(), false);
                            OperateUtil.judgeTryOrFinish(OperateUtil.mContext);
                            return;
                        }
                    }
                    return;
                }
                LogC.i("OperateUtil|delSSD|EVENT_ID_DEL_SSD_FAIL", false);
                OperateUtil.judgeTryOrFinish(OperateUtil.mContext);
            }
        });
    }

    /* access modifiers changed from: private */
    public static void judgeTryOrFinish(Context context) {
        if (!isTried) {
            isTried = true;
            LogC.i("OperateUtil|judgeTryOrFinish|isTried:" + isTried, false);
            delSSD(context, operateInterface);
            return;
        }
        isTried = false;
        LogC.i("OperateUtil|judgeTryOrFinish|Finish", false);
        if (operateInterface != null) {
            operateInterface.onEventBack(1, "reset fail");
        }
    }
}
