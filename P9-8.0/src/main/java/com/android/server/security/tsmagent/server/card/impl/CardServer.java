package com.android.server.security.tsmagent.server.card.impl;

import android.content.Context;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import com.android.server.security.tsmagent.server.card.request.TsmParamQueryRequest;
import com.android.server.security.tsmagent.server.card.response.TsmParamQueryResponse;
import com.android.server.security.tsmagent.server.wallet.impl.DicsQueryTask;
import com.android.server.security.tsmagent.server.wallet.request.QueryDicsRequset;
import com.android.server.security.tsmagent.server.wallet.response.QueryDicsResponse;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.PackageUtil;

public class CardServer {
    private static volatile CardServer sInstance;
    private final Context mContext;
    private final String serverTotalUrl = (ServiceConfig.CARD_INFO_MANAGE_SERVER_URL + "?clientVersion=" + PackageUtil.getVersionCode(this.mContext));

    public static CardServer getInstance(Context context) {
        if (sInstance == null) {
            synchronized (CardServer.class) {
                if (sInstance == null) {
                    sInstance = new CardServer(context);
                }
            }
        }
        return sInstance;
    }

    public CardServer(Context context) {
        this.mContext = context;
    }

    public TsmParamQueryResponse queryDeleteSSDTsmParam(TsmParamQueryRequest request) {
        HwLog.i("queryDeleteSSDTsmParam begin.");
        CardServerBaseResponse response = new TsmParamQueryTask(this.mContext, this.serverTotalUrl, "nfc.get.del.SSD").processTask(request);
        HwLog.i("queryDeleteSSDTsmParam end.");
        if (response instanceof TsmParamQueryResponse) {
            return (TsmParamQueryResponse) response;
        }
        return null;
    }

    public QueryDicsResponse queryDics(QueryDicsRequset request) {
        HwLog.i("queryDics begin.");
        CardServerBaseResponse response = new DicsQueryTask(this.mContext, this.serverTotalUrl).processTask(request);
        if (response instanceof QueryDicsResponse) {
            return (QueryDicsResponse) response;
        }
        HwLog.i("queryDics end.");
        return null;
    }

    public TsmParamQueryResponse queryCreateSSDTsmParam(TsmParamQueryRequest request) {
        HwLog.i("queryCreateSSDTsmParam begin.");
        CardServerBaseResponse response = new TsmParamQueryTask(this.mContext, this.serverTotalUrl, "nfc.get.create.SSD").processTask(request);
        HwLog.i("queryCreateSSDTsmParam end.");
        if (response instanceof TsmParamQueryResponse) {
            return (TsmParamQueryResponse) response;
        }
        return null;
    }

    public TsmParamQueryResponse queryInfoInitTsmParam(TsmParamQueryRequest request) {
        HwLog.i("queryInfoInitTsmParam begin.");
        CardServerBaseResponse response = new TsmParamQueryTask(this.mContext, this.serverTotalUrl, "nfc.get.NotifyEseInfoSync").processTask(request);
        HwLog.i("queryInfoInitTsmParam end.");
        if (response instanceof TsmParamQueryResponse) {
            return (TsmParamQueryResponse) response;
        }
        return null;
    }
}
