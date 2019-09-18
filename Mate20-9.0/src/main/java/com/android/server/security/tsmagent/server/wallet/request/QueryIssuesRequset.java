package com.android.server.security.tsmagent.server.wallet.request;

import com.android.server.security.tsmagent.server.CardServerBaseRequest;

public class QueryIssuesRequset extends CardServerBaseRequest {
    public long timestamp = 0;
    public int type = 2;
}
