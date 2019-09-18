package com.leisen.wallet.sdk.tsm;

import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.bean.OperAppletReqParams;

public interface ITSMOperator {
    int activateApplet(CommonRequestParams commonRequestParams, String str);

    int addGPAC(CommonRequestParams commonRequestParams, String str);

    int commonExecute(CommonRequestParams commonRequestParams);

    int createSSD(CommonRequestParams commonRequestParams, String str);

    int deleteApplet(CommonRequestParams commonRequestParams, OperAppletReqParams operAppletReqParams);

    int deleteGPAC(CommonRequestParams commonRequestParams, String str);

    int deleteSSD(CommonRequestParams commonRequestParams, String str);

    void getCIN(String str);

    void getCPLC(String str);

    void getIIN(String str);

    int installApplet(CommonRequestParams commonRequestParams, OperAppletReqParams operAppletReqParams);

    int lockApplet(CommonRequestParams commonRequestParams, OperAppletReqParams operAppletReqParams);

    int lockSSD(CommonRequestParams commonRequestParams, String str);

    int notifyEseInfoSync(CommonRequestParams commonRequestParams);

    int notifyInfoInit(CommonRequestParams commonRequestParams);

    int unlockApplet(CommonRequestParams commonRequestParams, OperAppletReqParams operAppletReqParams);

    int unlockSSD(CommonRequestParams commonRequestParams, String str);
}
