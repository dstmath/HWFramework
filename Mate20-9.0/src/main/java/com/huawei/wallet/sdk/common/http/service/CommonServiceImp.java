package com.huawei.wallet.sdk.common.http.service;

import com.huawei.wallet.sdk.business.bankcard.request.WipeAllBankCardRequest;
import com.huawei.wallet.sdk.business.clearssd.request.RandomRequest;
import com.huawei.wallet.sdk.business.clearssd.response.RandomResponse;
import com.huawei.wallet.sdk.business.diploma.request.DiplomaUploadRequest;
import com.huawei.wallet.sdk.business.diploma.response.DiplomaUploadResponse;
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
import com.huawei.wallet.sdk.common.buscard.PollTimeOutException;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessApplyOrderRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderResultRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessTransferOutRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessApplyOrderResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResultResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessTransferOutResponse;

public interface CommonServiceImp {
    ServerAccessApplyAPDUResponse applyApdu(ServerAccessApplyAPDURequest serverAccessApplyAPDURequest);

    ServerAccessApplyOrderResponse applyOrder(ServerAccessApplyOrderRequest serverAccessApplyOrderRequest);

    void cleanAllCupCard(WipeAllBankCardRequest wipeAllBankCardRequest);

    ServerAccessTransferOutResponse cloudTransferOut(ServerAccessTransferOutRequest serverAccessTransferOutRequest) throws PollTimeOutException;

    ServerAccessDeleteAppletResponse deleteApplet(ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest);

    QueryCardProductInfoResponse queryCardProductInfoList(QueryCardProductInfoRequest queryCardProductInfoRequest);

    CardStatusQueryResponse queryCardStatus(CardStatusQueryRequest cardStatusQueryRequest);

    TsmParamQueryResponse queryInfoInitTsmParam(TsmParamQueryRequest tsmParamQueryRequest, String str);

    QueryIssuerInfoResponse queryIssuerInfo(QueryIssuerInfoRequest queryIssuerInfoRequest);

    ServerAccessQueryOrderResponse queryOrder(ServerAccessQueryOrderRequest serverAccessQueryOrderRequest);

    ServerAccessQueryOrderResultResponse queryOrderResult(ServerAccessQueryOrderResultRequest serverAccessQueryOrderResultRequest) throws PollTimeOutException;

    RandomResponse ssdGetRandom(RandomRequest randomRequest);

    TsmParamQueryResponse ssdReset(TsmParamQueryRequest tsmParamQueryRequest);

    ServerAccessTransferOutResponse transferOut(ServerAccessTransferOutRequest serverAccessTransferOutRequest);

    DiplomaUploadResponse uploadDiploma(DiplomaUploadRequest diplomaUploadRequest);
}
