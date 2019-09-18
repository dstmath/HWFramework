package com.huawei.nb.client.diffprivacy;

import android.content.Context;
import android.os.IBinder;
import com.huawei.nb.client.Proxy;
import com.huawei.nb.notification.LocalObservable;
import com.huawei.nb.service.IDiffPrivacyServiceCall;

public class DiffPrivacyAgent extends Proxy<IDiffPrivacyServiceCall> {
    private static final String SERVICE_ACTION = "com.huawei.nb.service.DiffPrivacyService.START";
    private static final String SERVICE_NAME = "DiffPrivacyService";
    private static final String TAG = "DiffPrivacyAgent";

    public DiffPrivacyAgent(Context context) {
        super(context, SERVICE_NAME, SERVICE_ACTION);
    }

    /* access modifiers changed from: protected */
    public IDiffPrivacyServiceCall asInterface(IBinder binder) {
        return IDiffPrivacyServiceCall.Stub.asInterface(binder);
    }

    /* access modifiers changed from: protected */
    public LocalObservable<?, ?, IDiffPrivacyServiceCall> newLocalObservable() {
        return null;
    }

    public String diffPrivacy(String taskName, String filter, String param, String data) {
        return "";
    }
}
