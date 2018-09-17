package com.android.server.security.tsmagent.server.wallet.response;

import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import java.util.ArrayList;

public class QueryDicsResponse extends CardServerBaseResponse {
    public ArrayList<DicItem> dicItems = new ArrayList();
}
