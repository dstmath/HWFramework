package com.huawei.coauth.auth.authentity;

import com.huawei.coauth.auth.authmsg.CoAuthOperationType;

public class CoAuthRspBaseEntity {
    private CoAuthOperationType coAuthOperationType;

    public void setCoAuthOperationType(CoAuthOperationType coAuthOperationType2) {
        this.coAuthOperationType = coAuthOperationType2;
    }

    public CoAuthOperationType getCoAuthOperationType() {
        return this.coAuthOperationType;
    }
}
