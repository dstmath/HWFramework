package com.android.server.autofill;

import android.os.Bundle;
import android.os.UserManagerInternal.UserRestrictionsListener;

final /* synthetic */ class -$Lambda$vJuxjgWyqc7YDAVrm5huZJMbjMg implements UserRestrictionsListener {
    private final /* synthetic */ Object -$f0;

    private final /* synthetic */ void $m$0(int arg0, Bundle arg1, Bundle arg2) {
        ((AutofillManagerService) this.-$f0).lambda$-com_android_server_autofill_AutofillManagerService_5890(arg0, arg1, arg2);
    }

    public /* synthetic */ -$Lambda$vJuxjgWyqc7YDAVrm5huZJMbjMg(Object obj) {
        this.-$f0 = obj;
    }

    public final void onUserRestrictionsChanged(int i, Bundle bundle, Bundle bundle2) {
        $m$0(i, bundle, bundle2);
    }
}
