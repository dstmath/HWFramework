package com.huawei.wallet.sdk.business.idcard.accesscard.api;

import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.InitAccessCardOperatorCallback;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.NullifyCardResultCallback;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import java.util.List;

public interface AccessCardOperateLogicApi {
    void initAccessCard(List<TACardInfo> list, InitAccessCardOperatorCallback initAccessCardOperatorCallback);

    void uninstallAccessCard(String str, boolean z, String str2, NullifyCardResultCallback nullifyCardResultCallback);
}
