package com.android.server.hidata.wavemapping.util;

import android.os.Binder;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.iaware.IAwareSdkEx;

public class CheckTempertureUtil {
    private static final int MAX_TEMPERATURE_LEVEL = 1;
    public static CheckTempertureUtil mInstance = null;
    /* access modifiers changed from: private */
    public int curTempertureLevel = 0;
    private ThermalCallback mCallback;
    /* access modifiers changed from: private */
    public Handler mHandler;

    public class ThermalCallback extends Binder {
        private static final String DESCRIPTOR = "com.huawei.iaware.sdk.ThermalCallback";
        private static final int TRANSACTION_ASYNC_THERMAL_REPORT_CALLBACK = 1;

        public ThermalCallback() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1 || data == null || reply == null) {
                return super.onTransact(code, data, reply, flags);
            }
            data.enforceInterface(DESCRIPTOR);
            reportThermalData(data.readInt());
            reply.writeNoException();
            return true;
        }

        private void reportThermalData(int level) {
            LogUtil.d("Thermal notify level : " + level);
            int unused = CheckTempertureUtil.this.curTempertureLevel = level;
            if (CheckTempertureUtil.this.curTempertureLevel >= 1 && CheckTempertureUtil.this.mHandler != null) {
                CheckTempertureUtil.this.mHandler.sendEmptyMessage(220);
            }
        }
    }

    private CheckTempertureUtil(Handler handler) {
        this.mHandler = handler;
        this.mCallback = new ThermalCallback();
        IAwareSdkEx.registerCallback(3034, "com.android.server.hidata.wavemapping", this.mCallback);
    }

    public static CheckTempertureUtil getInstance(Handler handler) {
        if (mInstance == null) {
            mInstance = new CheckTempertureUtil(handler);
        }
        return mInstance;
    }

    public static CheckTempertureUtil getInstance() {
        return mInstance;
    }

    public boolean exceedMaxTemperture() {
        if (this.curTempertureLevel > 1) {
            return true;
        }
        return false;
    }
}
