package com.huawei.wallet.sdk.business.bankcard.api;

import com.huawei.wallet.sdk.business.bankcard.request.DeleteOverSeaCardRequest;
import com.huawei.wallet.sdk.business.bankcard.request.QueryAidRequest;
import com.huawei.wallet.sdk.business.bankcard.request.QueryUnionPayPushRequest;
import com.huawei.wallet.sdk.business.bankcard.request.WipeAllBankCardRequest;
import com.huawei.wallet.sdk.business.bankcard.response.CardSwipeResponse;
import com.huawei.wallet.sdk.business.bankcard.response.NullifyCardResponse;
import com.huawei.wallet.sdk.business.bankcard.response.QueryAidResponse;
import com.huawei.wallet.sdk.business.bankcard.response.QueryUnionPayPushResponse;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;

public interface BankCardServerApi {
    ServerAccessApplyAPDUResponse applyApdu(ServerAccessApplyAPDURequest serverAccessApplyAPDURequest);

    NullifyCardResponse deleteOverSeaCard(DeleteOverSeaCardRequest deleteOverSeaCardRequest);

    QueryAidResponse queryAidOnCUP(QueryAidRequest queryAidRequest);

    QueryUnionPayPushResponse queryUnionPayPush(QueryUnionPayPushRequest queryUnionPayPushRequest);

    CardSwipeResponse wipeAllBankCard(WipeAllBankCardRequest wipeAllBankCardRequest);
}
