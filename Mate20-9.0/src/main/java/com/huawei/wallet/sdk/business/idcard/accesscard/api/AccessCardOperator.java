package com.huawei.wallet.sdk.business.idcard.accesscard.api;

import com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception.AccessCardOperatorException;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.HandleNullifyResultHandler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.InitAccessCardResultHandler;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import java.util.List;

public interface AccessCardOperator {
    void init(List<TACardInfo> list, InitAccessCardResultHandler initAccessCardResultHandler) throws AccessCardOperatorException;

    void uninstallAccessCard(String str, String str2, boolean z, HandleNullifyResultHandler handleNullifyResultHandler) throws AccessCardOperatorException;
}
