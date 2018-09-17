package com.android.contacts.hap.numbermark.hwtoms.api;

import android.content.Context;
import android.text.TextUtils;
import com.android.contacts.external.separated.ISeparatedResourceUtils;
import com.android.contacts.external.separated.SeparatedResourceUtils;
import com.android.contacts.hap.numbermark.CapabilityInfo;
import com.android.contacts.hap.numbermark.base.ISDKCallApi;
import com.android.contacts.hap.numbermark.hwtoms.model.AccessPath;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestTelForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseTelForHW;
import com.android.contacts.hap.service.NumberMarkInfo;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class TomsSDKCallApi implements ISDKCallApi {
    private static String CALL_LOG_DETAIL_EXTRA_INFO_TITLE_LOCATION = null;
    private static String CALL_LOG_DETAIL_EXTRA_INFO_TITLE_URL = null;
    private static String CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIBO = null;
    private static String CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIXIN = null;
    private static final String CORRECTION_APP_KEY = "68ff1cc3c477517e";
    private static final String EXTRA_INFO_NO_ADDRESS = "";
    private static final String EXTRA_INFO_NO_POSITION = "";
    private static final String NORMAL_APP_KEY = "e00e96b9ffa3c862";
    private static final String NUMBER_MARK_INFO_NO_ATTRIBUTE = "";
    private static final String SUPPLIER_TOMS_SERVER_OPTION = "server 1";
    private static final boolean TOMS_DEFAULT_CLOUD_MARK = true;
    private static final int TOMS_DEFAULT_MARK_COUNT = -1;
    private static final String TYPE_BRAND_MERCHANTS = "0";
    private static final String TYPE_NORMAL_MERCHANTS = "1";
    private static IApiManager apiManager;
    private static TomsSDKCallApi callApi = new TomsSDKCallApi();

    private TomsSDKCallApi() {
    }

    public static TomsSDKCallApi getInstance(Context context) {
        ISeparatedResourceUtils aISeparatedResourceUtils = new SeparatedResourceUtils();
        if (CALL_LOG_DETAIL_EXTRA_INFO_TITLE_URL == null) {
            CALL_LOG_DETAIL_EXTRA_INFO_TITLE_URL = aISeparatedResourceUtils.getString(context, 1);
        }
        if (CALL_LOG_DETAIL_EXTRA_INFO_TITLE_LOCATION == null) {
            CALL_LOG_DETAIL_EXTRA_INFO_TITLE_LOCATION = aISeparatedResourceUtils.getString(context, 2);
        }
        if (CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIBO == null) {
            CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIBO = aISeparatedResourceUtils.getString(context, 3);
        }
        if (CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIXIN == null) {
            CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIXIN = aISeparatedResourceUtils.getString(context, 4);
        }
        IApiManager am = TomsApiManager.getInstance();
        TomsInvocationHandler mTomsInvocationHandler = new TomsInvocationHandler(context, am);
        apiManager = (IApiManager) Proxy.newProxyInstance(mTomsInvocationHandler.getClass().getClassLoader(), am.getClass().getInterfaces(), mTomsInvocationHandler);
        return callApi;
    }

    public NumberMarkInfo getInfoByNum(String subjectNum, String callType) {
        TomsRequestTelForHW request = new TomsRequestTelForHW();
        request.setSubjectNum(subjectNum);
        request.setTimestamp("" + System.currentTimeMillis());
        TomsResponseTelForHW telResponse = apiManager.telForHuawei(request, NORMAL_APP_KEY);
        if (telResponse == null) {
            return null;
        }
        if (telResponse.getErrorCode().equals(AccessPath.CONNECTION_TIMEOUT)) {
            return new NumberMarkInfo(AccessPath.CONNECTION_TIMEOUT);
        }
        return new NumberMarkInfo(subjectNum, "", telResponse.getName(), TYPE_BRAND_MERCHANTS.equals(telResponse.getType()) ? "brand" : "normal", TOMS_DEFAULT_CLOUD_MARK, TOMS_DEFAULT_MARK_COUNT, "toms");
    }

    public List<NumberMarkInfo> getInfoByName(String name) {
        TomsRequestInfoForHW request = new TomsRequestInfoForHW();
        request.setKeyword(name);
        return revertToNMIList(apiManager.infoForHuawei(request, NORMAL_APP_KEY));
    }

    public List<NumberMarkInfo> revertToNMIList(List<TomsResponseInfoForHW> list) {
        if (list == null) {
            return null;
        }
        List<NumberMarkInfo> nmiList = new ArrayList();
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            TomsResponseInfoForHW infoResponse = (TomsResponseInfoForHW) list.get(i);
            nmiList.add(new NumberMarkInfo(infoResponse.getTel(), infoResponse.getRegionName() + infoResponse.getAddress(), infoResponse.getCustName(), TYPE_BRAND_MERCHANTS.equals(infoResponse.getType()) ? "brand" : "normal", TOMS_DEFAULT_CLOUD_MARK, TOMS_DEFAULT_MARK_COUNT, "toms"));
        }
        return nmiList;
    }

    public List<CapabilityInfo> getExtraInfoByNum(String number) {
        List<CapabilityInfo> list = new ArrayList();
        TomsRequestDetailForHW request = new TomsRequestDetailForHW();
        request.setId(number);
        List<TomsResponseDetailForHW> responseDetailList = apiManager.detailForHuawei(request, NORMAL_APP_KEY);
        if (responseDetailList == null) {
            return null;
        }
        if (responseDetailList.size() == 0) {
            return list;
        }
        TomsResponseDetailForHW detailResponse = (TomsResponseDetailForHW) responseDetailList.get(0);
        if (detailResponse == null) {
            return null;
        }
        if (!TextUtils.isEmpty(detailResponse.getUrl())) {
            list.add(new CapabilityInfo("website", CALL_LOG_DETAIL_EXTRA_INFO_TITLE_URL, detailResponse.getUrl(), detailResponse.getTel(), detailResponse.getUrl(), ""));
        }
        String type = detailResponse.getType();
        String regionName = detailResponse.getRegionName();
        String address = detailResponse.getAddress();
        if (!(!TYPE_NORMAL_MERCHANTS.equals(type) || TextUtils.isEmpty(regionName) || TextUtils.isEmpty(address))) {
            list.add(new CapabilityInfo("address", CALL_LOG_DETAIL_EXTRA_INFO_TITLE_LOCATION, regionName + address, detailResponse.getTel(), "", regionName + address));
        }
        if (!TextUtils.isEmpty(detailResponse.getWeibo())) {
            list.add(new CapabilityInfo("weibo", CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIBO, detailResponse.getWeibo(), detailResponse.getTel(), "", ""));
        }
        if (TextUtils.isEmpty(detailResponse.getWeixin())) {
            return list;
        }
        list.add(new CapabilityInfo("weixin", CALL_LOG_DETAIL_EXTRA_INFO_TITLE_WEIXIN, detailResponse.getWeixin(), detailResponse.getTel(), "", ""));
        return list;
    }

    public String correction(NumberMarkInfo info) {
        if (info == null) {
            return null;
        }
        TomsRequestCorrection request = new TomsRequestCorrection();
        request.setNewAddr(info.getAttribute());
        request.setNewName(info.getName());
        request.setNewTel(info.getNumber());
        TomsResponseCorrection correction = apiManager.correction(request, CORRECTION_APP_KEY);
        if (correction != null) {
            return correction.getErrorCode();
        }
        return null;
    }

    public NumberMarkInfo getInfoFromPresetDB(String num) {
        return null;
    }

    public String toString() {
        return SUPPLIER_TOMS_SERVER_OPTION;
    }
}
