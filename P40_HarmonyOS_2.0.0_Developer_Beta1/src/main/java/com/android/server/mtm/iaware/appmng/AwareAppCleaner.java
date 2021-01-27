package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;

public abstract class AwareAppCleaner {
    protected Context mContext;

    public abstract int execute(AwareAppMngSortPolicy awareAppMngSortPolicy, Bundle bundle);

    protected AwareAppCleaner(Context context) {
        this.mContext = context;
    }
}
