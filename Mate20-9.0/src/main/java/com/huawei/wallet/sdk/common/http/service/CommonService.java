package com.huawei.wallet.sdk.common.http.service;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.request.WipeAllBankCardRequest;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.clearssd.request.RandomRequest;
import com.huawei.wallet.sdk.business.clearssd.response.RandomResponse;
import com.huawei.wallet.sdk.business.clearssd.task.RandomTask;
import com.huawei.wallet.sdk.business.diploma.request.DiplomaUploadRequest;
import com.huawei.wallet.sdk.business.diploma.response.DiplomaUploadResponse;
import com.huawei.wallet.sdk.business.diploma.task.DiplomaUploadTask;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.CardStatusQueryRequest;
import com.huawei.wallet.sdk.common.apdu.request.QueryCardProductInfoRequest;
import com.huawei.wallet.sdk.common.apdu.request.QueryIssuerInfoRequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.CardStatusQueryResponse;
import com.huawei.wallet.sdk.common.apdu.response.QueryCardProductInfoResponse;
import com.huawei.wallet.sdk.common.apdu.response.QueryIssuerInfoResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.CardProductInfoQueryTask;
import com.huawei.wallet.sdk.common.apdu.task.IssuerInfoQueryTask;
import com.huawei.wallet.sdk.common.apdu.task.ServerAccessApplyAPDUTask;
import com.huawei.wallet.sdk.common.apdu.task.ServerAccessDeleteAppletTask;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessApplyOrderRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderResultRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessTransferOutRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessApplyOrderResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResultResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessTransferOutResponse;
import com.huawei.wallet.sdk.common.buscard.task.CardStatusQueryTask;
import com.huawei.wallet.sdk.common.buscard.task.ServerAccessApplyOrderTask;
import com.huawei.wallet.sdk.common.buscard.task.ServerAccessQueryOrderResuletTask;
import com.huawei.wallet.sdk.common.buscard.task.ServerAccessQueryOrderTask;
import com.huawei.wallet.sdk.common.buscard.task.ServerAccessTransferOutTask;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.PropertyUtils;

public class CommonService implements CommonServiceImp {
    private Context mContext;
    private String mModule;
    private String serverTotalUrl;

    public CommonService(Context mContext2) {
        this.mContext = mContext2;
        int versionCode = PackageUtil.getVersionCode(mContext2);
        this.serverTotalUrl = ServiceConfig.getCardInfoManageServerUrl() + "?clientVersion=" + versionCode;
    }

    public CommonService(Context context, String module) {
        this.mContext = context;
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        this.serverTotalUrl = ServiceConfig.getCardInfoManageServerUrl() + "?clientVersion=" + versionCode;
        this.mModule = module;
        if (module != null && !module.isEmpty()) {
            setModule(module);
        }
    }

    public RandomResponse ssdGetRandom(RandomRequest request) {
        return (RandomResponse) new RandomTask(this.mContext, getSSDWalletAddress() + "?clientVersion=" + AddressNameMgr.VERSIONCODES).processTask(request);
    }

    public TsmParamQueryResponse ssdReset(TsmParamQueryRequest request) {
        return (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, getSSDWalletAddress() + "?clientVersion=" + AddressNameMgr.VERSIONCODES, "nfc.se.reset").processTask(request);
    }

    private String getSSDWalletAddress() {
        String countryCode = PropertyUtils.getProperty(AddressNameMgr.PROP_NAME_LOCALE_REGION, "CN");
        LogC.i("CommonService|getSSDWalletAddress|countryCode: " + countryCode, false);
        if ("CN".equalsIgnoreCase(countryCode)) {
            return AddressNameMgr.WISECLOUDVIRTUALCARD_SERVER_URL;
        }
        return "https://vcardmgt-dre.wallet.hicloud.com/WiseCloudVirtualCardMgmtService/app/gateway";
    }

    public void cleanAllCupCard(WipeAllBankCardRequest request) {
    }

    public ServerAccessDeleteAppletResponse deleteApplet(ServerAccessDeleteAppletRequest request) {
        return (ServerAccessDeleteAppletResponse) new ServerAccessDeleteAppletTask(this.mContext, getServerAddress("delete.app", getModule())).processTask(request);
    }

    public ServerAccessApplyAPDUResponse applyApdu(ServerAccessApplyAPDURequest request) {
        return (ServerAccessApplyAPDUResponse) new ServerAccessApplyAPDUTask(this.mContext, getServerAddress("get.apdu", getModule())).processTask(request);
    }

    public TsmParamQueryResponse queryInfoInitTsmParam(TsmParamQueryRequest paramQueryRequest, String command) {
        return (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, getServerAddress("get.apdu", getModule()), command).processTask(paramQueryRequest);
    }

    public DiplomaUploadResponse uploadDiploma(DiplomaUploadRequest request) {
        return (DiplomaUploadResponse) new DiplomaUploadTask(this.mContext, getSSDWalletAddress() + "?clientVersion=" + AddressNameMgr.VERSIONCODES, request).processTask(request);
    }

    public ServerAccessQueryOrderResponse queryOrder(ServerAccessQueryOrderRequest request) {
        LogX.i("CardServer queryOrder begin");
        return (ServerAccessQueryOrderResponse) new ServerAccessQueryOrderTask(this.mContext, getServerAddress(ServerCmdConstant.QUERY_ORDER, getModule())).processTask(request);
    }

    public ServerAccessApplyOrderResponse applyOrder(ServerAccessApplyOrderRequest request) {
        LogX.i("CardServer applyOrder begin");
        return (ServerAccessApplyOrderResponse) new ServerAccessApplyOrderTask(this.mContext, getServerAddress(ServerCmdConstant.CREATE_ORDER, getModule())).processTask(request);
    }

    public ServerAccessTransferOutResponse checkCloudTransferOut(ServerAccessTransferOutRequest request) {
        LogX.i("checkCloudTransferOut begin.");
        ServerAccessTransferOutTask task = new ServerAccessTransferOutTask(this.mContext, getServerAddress("nfc.transcard.remove.check", getModule()));
        task.setHeadCommander("nfc.transcard.remove.check");
        return (ServerAccessTransferOutResponse) task.processTask(request);
    }

    public ServerAccessTransferOutResponse cloudTransferOut(ServerAccessTransferOutRequest request) {
        LogX.i("cloudTransferOut begin.");
        ServerAccessTransferOutTask task = new ServerAccessTransferOutTask(this.mContext, getServerAddress("nfc.transcard.backup", getModule()));
        task.setHeadCommander("nfc.transcard.backup");
        return (ServerAccessTransferOutResponse) task.processTask(request);
    }

    public ServerAccessTransferOutResponse transferOut(ServerAccessTransferOutRequest request) {
        LogX.i("transferOut begin.");
        return (ServerAccessTransferOutResponse) new ServerAccessTransferOutTask(this.mContext, getServerAddress(ServerCmdConstant.CARDMOVE_OUT, getModule())).processTask(request);
    }

    public ServerAccessQueryOrderResultResponse queryOrderResult(ServerAccessQueryOrderResultRequest request) {
        LogX.i("CardServer queryOrderResult begin", false);
        return (ServerAccessQueryOrderResultResponse) new ServerAccessQueryOrderResuletTask(this.mContext, getServerAddress(ServerCmdConstant.QUERY_ORDER_RESULT, getModule())).processTask(request);
    }

    public CardStatusQueryResponse queryCardStatus(CardStatusQueryRequest request) {
        LogX.i("queryCardStatus begin.");
        return (CardStatusQueryResponse) new CardStatusQueryTask(this.mContext, this.serverTotalUrl).processTask(request);
    }

    public QueryIssuerInfoResponse queryIssuerInfo(QueryIssuerInfoRequest request) {
        LogX.i("queryIssuerInfo begin.");
        IssuerInfoQueryTask task = new IssuerInfoQueryTask(this.mContext, getServerAddress("nfc.get.issuers", getModule()));
        request.setIsNeedServiceTokenAuth(true);
        QueryIssuerInfoResponse response = (QueryIssuerInfoResponse) task.processTask(request);
        LogX.i("queryIssuerInfo end.");
        return response;
    }

    public QueryCardProductInfoResponse queryCardProductInfoList(QueryCardProductInfoRequest request) {
        LogX.i("queryCardProductInfoList begin.");
        CardProductInfoQueryTask task = new CardProductInfoQueryTask(this.mContext, getServerAddress("nfc.get.issuers", getModule()));
        request.setIsNeedServiceTokenAuth(true);
        QueryCardProductInfoResponse response = (QueryCardProductInfoResponse) task.processTask(request);
        LogX.i("queryCardProductInfoList end.");
        return response;
    }

    /* access modifiers changed from: protected */
    public void setModule(String module) {
        this.mModule = module;
    }

    /* access modifiers changed from: protected */
    public String getModule() {
        return this.mModule;
    }

    /* access modifiers changed from: protected */
    public String getServerAddress(String commander, String module) {
        if (module == null || module.isEmpty()) {
            return this.serverTotalUrl;
        }
        return AddressNameMgr.getInstance().getAddress(commander, module, null, this.mContext);
    }
}
