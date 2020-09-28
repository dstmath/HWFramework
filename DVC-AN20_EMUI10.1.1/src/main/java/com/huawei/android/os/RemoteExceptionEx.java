package com.huawei.android.os;

import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class RemoteExceptionEx {
    private RemoteExceptionEx() {
    }

    public static RuntimeException rethrowFromSystemServer(RemoteException ex) {
        return ex.rethrowFromSystemServer();
    }
}
