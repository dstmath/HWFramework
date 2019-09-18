package com.huawei.wallet.sdk.business.bankcard.util;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.CardAndIssuerInfoCacheApi;
import com.huawei.wallet.sdk.business.bankcard.api.CardInfoManagerApi;
import com.huawei.wallet.sdk.business.bankcard.api.CardLostManagerApi;
import com.huawei.wallet.sdk.business.bankcard.manager.LocalRouter;
import com.huawei.wallet.sdk.business.bankcard.request.RouterRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.api.NFCOpenApi;

public class Router {
    public static CardLostManagerApi getCardLostManagerApi(Context context) {
        return (CardLostManagerApi) LocalRouter.getInstance().invoke(new RouterRequest(context).setDomain("com.huawei.wallet.nfc").setProvider("CardLostManagerProvider").setAction("CardLostManagerCreateAction"), null).getResultParam("CardLostManager");
    }

    public static CardAndIssuerInfoCacheApi getCardAndIssuerInfoCacheApi(Context context) {
        return (CardAndIssuerInfoCacheApi) LocalRouter.getInstance().invoke(new RouterRequest(context).setDomain("com.huawei.wallet.nfc").setProvider("CardAndIssuerInfoCacheProvider").setAction("CardAndIssuerInfoCacheCreateAction"), null).getResultParam("CardAndIssuerInfoCache");
    }

    public static CardInfoManagerApi getCardInfoManagerApi(Context context) {
        return (CardInfoManagerApi) LocalRouter.getInstance().invoke(new RouterRequest(context).setDomain("com.huawei.wallet.nfc").setProvider("CardInfoManagerProvider").setAction("CardInfoManagerCreateAction"), null).getResultParam("CardInfoManager");
    }

    public static NFCOpenApi getNFCOpenApi(Context context) {
        return (NFCOpenApi) LocalRouter.getInstance().invoke(new RouterRequest(context).setDomain("com.huawei.wallet.nfc").setProvider("NFCOpenApiProvider").setAction("NFCOpenApiCreateAction"), null).getResultParam("NFCOpenApi");
    }
}
