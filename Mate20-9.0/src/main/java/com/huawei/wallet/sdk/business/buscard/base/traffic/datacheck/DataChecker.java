package com.huawei.wallet.sdk.business.buscard.base.traffic.datacheck;

import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;

public interface DataChecker {
    void checkAmount(CardInfo cardInfo) throws AppletCardException;

    void checkDate(CardInfo cardInfo) throws AppletCardException;
}
