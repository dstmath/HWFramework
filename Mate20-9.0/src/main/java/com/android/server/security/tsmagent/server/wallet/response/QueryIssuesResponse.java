package com.android.server.security.tsmagent.server.wallet.response;

import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import java.util.ArrayList;

public class QueryIssuesResponse extends CardServerBaseResponse {
    public ArrayList<IssueItem> issueItems = new ArrayList<>();
}
