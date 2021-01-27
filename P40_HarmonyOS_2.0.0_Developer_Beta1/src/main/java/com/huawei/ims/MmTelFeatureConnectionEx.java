package com.huawei.ims;

import android.content.Context;
import android.os.IBinder;
import com.android.ims.MmTelFeatureConnection;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MmTelFeatureConnectionEx {
    private MmTelFeatureConnection mMmTelFeatureConnection;

    public MmTelFeatureConnectionEx(Context context, int subId) {
        this.mMmTelFeatureConnection = new MmTelFeatureConnection(context, subId);
    }

    public MmTelFeatureConnection getMmTelFeatureConnection() {
        return this.mMmTelFeatureConnection;
    }

    public IImsServiceFeatureCallback getListener() {
        return this.mMmTelFeatureConnection.getListener();
    }

    public void setBinder(IBinder binder) {
        this.mMmTelFeatureConnection.setBinder(binder);
    }

    public int getFeatureState() {
        return this.mMmTelFeatureConnection.getFeatureState();
    }
}
