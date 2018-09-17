package com.leisen.wallet.sdk.util;

import com.google.gson.Gson;
import com.leisen.wallet.sdk.AppConfig;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.bean.OperAppletReqParams;
import com.leisen.wallet.sdk.business.ActivateAppletBusinessForReq;
import com.leisen.wallet.sdk.business.ApduResBean;
import com.leisen.wallet.sdk.business.AppletOperBusinessForReq;
import com.leisen.wallet.sdk.business.BaseBusinessForReq;
import com.leisen.wallet.sdk.business.BaseBusinessForReqNext;
import com.leisen.wallet.sdk.business.BaseRequest;
import com.leisen.wallet.sdk.business.GPACOperBusinessForReq;
import com.leisen.wallet.sdk.business.SSDOperBusinessForReq;

public class AppJsonUtil {
    public static String getOperSSDJsonResult(CommonRequestParams params, int businessType, int operType, String ssdAid, int taskIndex) {
        Object request = new BaseRequest();
        fillBaseData(request, params);
        SSDOperBusinessForReq business = new SSDOperBusinessForReq();
        business.setType(businessType);
        business.setSsdAid(ssdAid);
        business.setOperType(operType);
        business.setTaskIndex(taskIndex);
        request.setBusiness(business);
        return new Gson().toJson(request);
    }

    public static String getOperAppletJsonResult(CommonRequestParams params, int businessType, int operType, OperAppletReqParams appletParams, int taskIndex) {
        Object request = new BaseRequest();
        fillBaseData(request, params);
        AppletOperBusinessForReq business = new AppletOperBusinessForReq();
        business.setType(businessType);
        business.setAppAid(appletParams.getAppletAid());
        business.setAppletVersion(appletParams.getAppletVersion());
        business.setOperType(operType);
        business.setTaskIndex(taskIndex);
        request.setBusiness(business);
        return new Gson().toJson(request);
    }

    public static String getOperGPACJsonResult(CommonRequestParams params, int businessType, int operType, String appletAid, int taskIndex) {
        Object request = new BaseRequest();
        fillBaseData(request, params);
        GPACOperBusinessForReq business = new GPACOperBusinessForReq();
        business.setType(businessType);
        business.setAppAid(appletAid);
        business.setOperType(operType);
        business.setTaskIndex(taskIndex);
        request.setBusiness(business);
        return new Gson().toJson(request);
    }

    public static String getActivateAppletJsonResult(CommonRequestParams params, int businessType, String appletAid, int taskIndex) {
        Object request = new BaseRequest();
        fillBaseData(request, params);
        ActivateAppletBusinessForReq business = new ActivateAppletBusinessForReq();
        business.setType(businessType);
        business.setAppAid(appletAid);
        business.setTaskIndex(taskIndex);
        request.setBusiness(business);
        return new Gson().toJson(request);
    }

    public static String getReqNextJsonResult(CommonRequestParams params, int businessType, ApduResBean rapduList, int result, int taskIndex) {
        Object request = new BaseRequest();
        fillBaseData(request, params);
        BaseBusinessForReqNext business = new BaseBusinessForReqNext();
        business.setType(businessType);
        business.setRapduList(rapduList);
        business.setResult(result);
        business.setTaskIndex(taskIndex);
        request.setBusiness(business);
        return new Gson().toJson(request);
    }

    public static String getBaseReqJsonResult(CommonRequestParams params, int businessType, int taskIndex) {
        Object request = new BaseRequest();
        fillBaseData(request, params);
        BaseBusinessForReq business = new BaseBusinessForReq();
        business.setType(businessType);
        business.setTaskIndex(taskIndex);
        request.setBusiness(business);
        return new Gson().toJson(request);
    }

    private static void fillBaseData(BaseRequest<?> request, CommonRequestParams params) {
        request.setClientVersion(AppConfig.CLIENTVERSION);
        request.setImei(AppConfig.IMEI);
        request.setMobileType(AppConfig.MOBILETYPE);
        request.setVersion("1.0");
        request.setServiceId(params.getServiceId());
        request.setFunctionCallId(params.getFunCallId());
        request.setSeid(params.getSeid());
    }
}
