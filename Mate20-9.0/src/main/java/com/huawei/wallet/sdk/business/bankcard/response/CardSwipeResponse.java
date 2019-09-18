package com.huawei.wallet.sdk.business.bankcard.response;

import com.huawei.wallet.sdk.business.bankcard.modle.SwipeCardInfo;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import java.util.ArrayList;

public class CardSwipeResponse extends CardServerBaseResponse {
    private ArrayList<SwipeCardInfo> mList;

    public ArrayList<SwipeCardInfo> getSwipeCardInfoList() {
        return this.mList;
    }

    public void setSwipeCardInfoList(ArrayList<SwipeCardInfo> mList2) {
        this.mList = mList2;
    }
}
