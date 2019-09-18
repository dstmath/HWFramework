package com.huawei.wallet.sdk.common.apdu.response;

import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoServerItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryIssuerInfoResponse extends CardServerBaseResponse {
    public List<IssuerInfoServerItem> issueInfos = new ArrayList();
    public Map<String, IssuerInfoServerItem> issueInfosMap = new HashMap();
}
