package com.android.server.security.tsmagent.server.card.impl;

import android.content.Context;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import com.android.server.security.tsmagent.server.card.request.TsmParamQueryRequest;
import com.android.server.security.tsmagent.server.card.response.TsmParamQueryResponse;
import com.android.server.security.tsmagent.server.wallet.impl.DicsQueryTask;
import com.android.server.security.tsmagent.server.wallet.impl.IssuersQueryTask;
import com.android.server.security.tsmagent.server.wallet.request.QueryDicsRequset;
import com.android.server.security.tsmagent.server.wallet.request.QueryIssuesRequset;
import com.android.server.security.tsmagent.server.wallet.response.QueryDicsResponse;
import com.android.server.security.tsmagent.server.wallet.response.QueryIssuesResponse;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.PackageUtil;

public class CardServer {
    private static volatile CardServer sInstance;
    private static String serverTotalUrl;
    private static int versionCode = 27;
    private final Context mContext;

    public static CardServer getInstance(Context context) {
        if (sInstance == null) {
            synchronized (CardServer.class) {
                if (sInstance == null) {
                    sInstance = new CardServer(context);
                }
            }
        }
        serverTotalUrl = ServiceConfig.getCardUrl() + "?clientVersion=" + versionCode;
        return sInstance;
    }

    public CardServer(Context context) {
        this.mContext = context;
        versionCode = PackageUtil.getVersionCode(this.mContext);
        serverTotalUrl = ServiceConfig.getCardUrl() + "?clientVersion=" + versionCode;
    }

    public TsmParamQueryResponse queryDeleteSSDTsmParam(TsmParamQueryRequest request) {
        HwLog.i("queryDeleteSSDTsmParam begin.");
        CardServerBaseResponse response = new TsmParamQueryTask(this.mContext, serverTotalUrl, "nfc.get.del.SSD").processTask(request);
        HwLog.i("queryDeleteSSDTsmParam end.");
        if (response instanceof TsmParamQueryResponse) {
            return (TsmParamQueryResponse) response;
        }
        return null;
    }

    public QueryDicsResponse queryDics(QueryDicsRequset request) {
        HwLog.i("queryDics begin.");
        CardServerBaseResponse response = new DicsQueryTask(this.mContext, serverTotalUrl).processTask(request);
        if (response instanceof QueryDicsResponse) {
            return (QueryDicsResponse) response;
        }
        HwLog.i("queryDics end.");
        return null;
    }

    public QueryIssuesResponse queryUkeyIssues() {
        HwLog.i("queryUkeyIssues begin.");
        CardServerBaseResponse response = new IssuersQueryTask(this.mContext, serverTotalUrl).processTask(new QueryIssuesRequset());
        if (response instanceof QueryIssuesResponse) {
            return (QueryIssuesResponse) response;
        }
        HwLog.i("queryUkeyIssues end.");
        return null;
    }

    public TsmParamQueryResponse queryCreateSSDTsmParam(TsmParamQueryRequest request) {
        HwLog.i("queryCreateSSDTsmParam begin.");
        CardServerBaseResponse response = new TsmParamQueryTask(this.mContext, serverTotalUrl, "nfc.get.create.SSD").processTask(request);
        HwLog.i("queryCreateSSDTsmParam end.");
        if (response instanceof TsmParamQueryResponse) {
            return (TsmParamQueryResponse) response;
        }
        return null;
    }

    public TsmParamQueryResponse queryInfoInitTsmParam(TsmParamQueryRequest request) {
        HwLog.i("queryInfoInitTsmParam begin.");
        CardServerBaseResponse response = new TsmParamQueryTask(this.mContext, serverTotalUrl, "nfc.get.NotifyEseInfoSync").processTask(request);
        HwLog.i("queryInfoInitTsmParam end.");
        if (response instanceof TsmParamQueryResponse) {
            return (TsmParamQueryResponse) response;
        }
        return null;
    }
}
