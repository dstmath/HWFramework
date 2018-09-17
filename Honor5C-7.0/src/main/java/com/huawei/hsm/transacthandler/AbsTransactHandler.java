package com.huawei.hsm.transacthandler;

import android.os.Parcel;

public abstract class AbsTransactHandler {
    public abstract void handleTransactCode(int i, Parcel parcel, Parcel parcel2);
}
