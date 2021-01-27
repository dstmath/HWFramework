package com.android.server.hidata.wavemapping.util;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.swing.HwSwingMotionGestureConstant;
import com.huawei.android.iaware.IAwareSdkEx;

public class CheckTemperatureUtil {
    private static final String IAWARE_SDK_SERVICE_NAME = "IAwareSdkService";
    private static final int MAX_TEMPERATURE_LEVEL = 1;
    private static final int MSG_CHECK_SDK_SERVICE = 101;
    private static final String REGISTER_MODULE_NAME = "com.android.server.hidata.wavemapping";
    private static final int RETRY_INTERVAL = 5000;
    private static final int RETRY_TIMES = 3;
    private static CheckTemperatureUtil mInstance = null;
    private ThermalCallback mCallback = null;
    private int mCurTemperatureLevel = 0;
    private SdkServiceHandler mHandler = new SdkServiceHandler();
    private Handler mStateMachineHandler = null;

    private CheckTemperatureUtil(Handler handler) {
        this.mStateMachineHandler = handler;
        this.mCallback = new ThermalCallback();
        IAwareSdkEx.registerCallback(3034, REGISTER_MODULE_NAME, this.mCallback);
    }

    public static synchronized CheckTemperatureUtil getInstance(Handler handler) {
        CheckTemperatureUtil checkTemperatureUtil;
        synchronized (CheckTemperatureUtil.class) {
            if (mInstance == null) {
                mInstance = new CheckTemperatureUtil(handler);
            }
            checkTemperatureUtil = mInstance;
        }
        return checkTemperatureUtil;
    }

    public boolean isExceedMaxTemperature() {
        if (this.mCurTemperatureLevel > 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkSdkService(int count) {
        if (count > 3) {
            LogUtil.e(false, "failed to get IAwareSdkService, Out of count.", new Object[0]);
            return;
        }
        LogUtil.d(false, "checkSdkService retry time: %{public}d", Integer.valueOf(count));
        if (ServiceManager.getService(IAWARE_SDK_SERVICE_NAME) != null) {
            LogUtil.i(false, "IAwareSdkService on", new Object[0]);
            registerThermalCallback();
            return;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101, count + 1, 0), HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD);
    }

    public void registerThermalCallback() {
        if (this.mCallback == null) {
            this.mCallback = new ThermalCallback();
        }
        this.mCallback.linkToSdkService();
        IAwareSdkEx.registerCallback(3034, REGISTER_MODULE_NAME, this.mCallback);
    }

    /* access modifiers changed from: private */
    public class ThermalCallback extends Binder implements IBinder.DeathRecipient {
        private static final int BEGIN_CHECK_SDK_SERVICE_COUNT = 1;
        private static final String DESCRIPTOR = "com.huawei.iaware.sdk.ThermalCallback";
        private static final int TRANSACT_ASYNC_THERMAL_REPORT_CALLBACK = 1;
        private IBinder sdkService;

        private ThermalCallback() {
            this.sdkService = null;
        }

        public void linkToSdkService() {
            if (this.sdkService == null) {
                try {
                    this.sdkService = ServiceManager.getService(CheckTemperatureUtil.IAWARE_SDK_SERVICE_NAME);
                    if (this.sdkService != null) {
                        this.sdkService.linkToDeath(this, 0);
                    } else {
                        LogUtil.e(false, "failed to get IAwareSdkService.", new Object[0]);
                    }
                } catch (RemoteException e) {
                    LogUtil.e(false, "RemoteException", new Object[0]);
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.sdkService = null;
            LogUtil.i(false, "IAwareSdkService died.", new Object[0]);
            if (CheckTemperatureUtil.this.mHandler != null) {
                CheckTemperatureUtil.this.mHandler.sendMessageDelayed(CheckTemperatureUtil.this.mHandler.obtainMessage(101, 1, 0), HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD);
            }
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                LogUtil.d(false, "onTransact default code not care", new Object[0]);
                return super.onTransact(code, data, reply, flags);
            } else if (data == null || reply == null) {
                return false;
            } else {
                data.enforceInterface(DESCRIPTOR);
                reportThermalData(data.readInt());
                reply.writeNoException();
                return true;
            }
        }

        private void reportThermalData(int level) {
            LogUtil.d(false, "Thermal notify level : %{public}d", Integer.valueOf(level));
            CheckTemperatureUtil.this.mCurTemperatureLevel = level;
            if (CheckTemperatureUtil.this.mCurTemperatureLevel >= 1 && CheckTemperatureUtil.this.mStateMachineHandler != null) {
                CheckTemperatureUtil.this.mStateMachineHandler.sendEmptyMessage(WMStateCons.MSG_HIGH_TEMPERATURE);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SdkServiceHandler extends Handler {
        private SdkServiceHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                if (msg.what != 101) {
                    LogUtil.e(false, "unknown msg: %{public}d", Integer.valueOf(msg.what));
                } else {
                    CheckTemperatureUtil.this.checkSdkService(msg.arg1);
                }
            }
        }
    }
}
