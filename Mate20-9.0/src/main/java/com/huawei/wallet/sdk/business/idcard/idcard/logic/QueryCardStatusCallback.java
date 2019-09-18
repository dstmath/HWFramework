package com.huawei.wallet.sdk.business.idcard.idcard.logic;

import com.huawei.wallet.sdk.business.idcard.idcard.server.response.IdCardStatusItem;
import java.util.List;

public interface QueryCardStatusCallback {
    void onFail(int i, String str);

    void onSuccess(List<IdCardStatusItem> list);
}
