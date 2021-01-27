package com.huawei.coauth.auth.authentity;

import com.huawei.coauth.auth.CoAuthContext;
import java.util.ArrayList;
import java.util.List;

public class CoAuthQueryMethodEntity extends CoAuthRspBaseEntity {
    private static final int INITIAL_CAPACITY = 1;
    private List<CoAuthContext> contextList = new ArrayList(1);

    public List<CoAuthContext> getContextList() {
        return this.contextList;
    }
}
