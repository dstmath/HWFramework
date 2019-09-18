package com.unionpay.tsmservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import com.unionpay.tsmservice.ITsmActivityCallback;

public final class a extends ITsmActivityCallback.Stub {
    private Context a;

    public a(Context context) {
        this.a = context;
    }

    public final void startActivity(String str, String str2, int i, Bundle bundle) throws RemoteException {
        ComponentName componentName = new ComponentName(str, str2);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        if (i != -1) {
            intent.setFlags(i);
        }
        intent.setComponent(componentName);
        this.a.startActivity(intent);
    }
}
