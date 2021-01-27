package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.huawei.msdp.devicestatus.DeviceStatusConstantAdapter;
import com.huawei.msdp.devicestatus.HwMsdpDeviceStatusAdapter;
import com.huawei.msdp.devicestatus.HwMsdpDeviceStatusChangeEventAdapter;
import com.huawei.msdp.devicestatus.HwMsdpDeviceStatusChangedCallBackAdapter;
import com.huawei.msdp.devicestatus.HwMsdpDeviceStatusEventAdapter;
import com.huawei.msdp.devicestatus.HwMsdpDeviceStatusServiceConnectionAdapter;
import java.util.ArrayList;
import java.util.List;

public class PhoneStatusRecong {
    private static final long CALL_BACK_INTERVAL_DEFAULT_VALUE = 86400000000000L;
    private static final int DEBUG_TRUE = 1;
    private static final Object LOCK = new Object();
    private static final long MIN_RETRY_TIME = 1800000;
    private static final int MSG_CONNECT_MSDP = 0;
    private static final String TAG = "PhoneStatusRecong";
    private static PhoneStatusRecong sPhoneStatusRecong;
    private Context mContext = null;
    private final List<CurrentStatus> mCurrentStatus = new ArrayList();
    private long mCurrentStatusEnterTime = SystemClock.elapsedRealtime();
    private HwMsdpDeviceStatusAdapter mDevMsdpDeviceStatus;
    private DevMsdpDeviceStatusChangedCallBack mDevMsdpDeviceStatusChangedCallBack;
    private DevMsdpDeviceStatusServiceConnection mDevMsdpDeviceStatusServiceConnection;
    private boolean mIsConnected = false;
    private boolean mIsStatusValid = true;
    private PhoneStatusHandler mPhoneStatusHandler = new PhoneStatusHandler();
    private long mRecordTime = 0;

    public static PhoneStatusRecong getInstance() {
        PhoneStatusRecong phoneStatusRecong;
        synchronized (LOCK) {
            if (sPhoneStatusRecong == null) {
                sPhoneStatusRecong = new PhoneStatusRecong();
            }
            phoneStatusRecong = sPhoneStatusRecong;
        }
        return phoneStatusRecong;
    }

    public void init(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mDevMsdpDeviceStatus = new HwMsdpDeviceStatusAdapter(context);
            this.mDevMsdpDeviceStatusChangedCallBack = new DevMsdpDeviceStatusChangedCallBack();
            this.mDevMsdpDeviceStatusServiceConnection = new DevMsdpDeviceStatusServiceConnection();
        }
    }

    /* access modifiers changed from: private */
    public class PhoneStatusHandler extends Handler {
        private PhoneStatusHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.d(PhoneStatusRecong.TAG, "msg is null, error");
            } else if (msg.what == 0) {
                PhoneStatusRecong.this.connectService();
            }
        }
    }

    public void connectService() {
        if (this.mContext != null && this.mDevMsdpDeviceStatus != null && this.mDevMsdpDeviceStatusChangedCallBack != null && this.mDevMsdpDeviceStatusServiceConnection != null) {
            AwareLog.d(TAG, "connectService");
            this.mDevMsdpDeviceStatus.connectService(this.mDevMsdpDeviceStatusChangedCallBack, this.mDevMsdpDeviceStatusServiceConnection);
        }
    }

    public void disconnectService() {
        if (this.mDevMsdpDeviceStatus != null && this.mIsConnected) {
            disableDeviceStatusEvent(DeviceStatusConstantAdapter.MSDP_DEVICESTATUS_TYPE_STILL_STATUS, DeviceStatusConstantAdapter.EVENT_TYPE_ENTER);
            this.mDevMsdpDeviceStatus.disconnectService();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        HwMsdpDeviceStatusAdapter hwMsdpDeviceStatusAdapter = this.mDevMsdpDeviceStatus;
        if (hwMsdpDeviceStatusAdapter != null) {
            return hwMsdpDeviceStatusAdapter.enableDeviceStatusEvent(deviceStatus, eventType, reportLatencyNs);
        }
        return false;
    }

    private boolean disableDeviceStatusEvent(String deviceStatus, int eventType) {
        HwMsdpDeviceStatusAdapter hwMsdpDeviceStatusAdapter = this.mDevMsdpDeviceStatus;
        if (hwMsdpDeviceStatusAdapter != null) {
            return hwMsdpDeviceStatusAdapter.disableDeviceStatusEvent(deviceStatus, eventType);
        }
        return false;
    }

    private HwMsdpDeviceStatusChangeEventAdapter getCurrentDeviceStatus() {
        synchronized (this.mCurrentStatus) {
            if (!this.mIsConnected) {
                this.mCurrentStatus.clear();
                long curTime = SystemClock.elapsedRealtime();
                if (curTime - this.mRecordTime >= MIN_RETRY_TIME) {
                    this.mRecordTime = curTime;
                    Message msg = this.mPhoneStatusHandler.obtainMessage();
                    msg.what = 0;
                    this.mPhoneStatusHandler.sendMessage(msg);
                }
                return null;
            }
        }
        HwMsdpDeviceStatusAdapter hwMsdpDeviceStatusAdapter = this.mDevMsdpDeviceStatus;
        if (hwMsdpDeviceStatusAdapter != null) {
            return hwMsdpDeviceStatusAdapter.getCurrentDeviceStatus();
        }
        return null;
    }

    public void getDeviceStatus() {
        HwMsdpDeviceStatusChangeEventAdapter status = getCurrentDeviceStatus();
        if (status == null) {
            AwareLog.e(TAG, "status is null, error! mIsConnected is " + this.mIsConnected);
            this.mIsStatusValid = false;
            return;
        }
        Iterable<HwMsdpDeviceStatusEventAdapter> statusList = status.getDeviceStatusRecognitionEvents();
        if (statusList == null) {
            this.mIsStatusValid = false;
            return;
        }
        this.mIsStatusValid = true;
        synchronized (this.mCurrentStatus) {
            this.mCurrentStatus.clear();
            for (HwMsdpDeviceStatusEventAdapter event : statusList) {
                if (event != null) {
                    long currentStatusEnterTime = event.getmTimestampNs();
                    String currentStatus = event.getmDeviceStatus();
                    setPhoneStatus(currentStatus, currentStatusEnterTime);
                    AwareLog.d(TAG, "Current status :" + currentStatus + ", current time :" + currentStatusEnterTime);
                }
            }
        }
    }

    private void setPhoneStatus(String currentStatus, long currentStatusEnterTime) {
        if (currentStatus != null) {
            CurrentStatus curState = new CurrentStatus(0, currentStatusEnterTime);
            if (DeviceStatusConstantAdapter.MSDP_DEVICETSTATUS_TYPE_HIGH_STILL.equals(currentStatus)) {
                curState.setPhoneStatus(1);
            } else if (DeviceStatusConstantAdapter.MSDP_DEVICETSTATUS_TYPE_COARSE_STILL.equals(currentStatus)) {
                curState.setPhoneStatus(2);
            } else if (DeviceStatusConstantAdapter.MSDP_DEVICETSTATUS_TYPE_FINE_STILL.equals(currentStatus)) {
                curState.setPhoneStatus(3);
            } else if (DeviceStatusConstantAdapter.MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_WALKING.equals(currentStatus)) {
                curState.setPhoneStatus(4);
            } else if (DeviceStatusConstantAdapter.MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_FAST_WALKING.equals(currentStatus)) {
                curState.setPhoneStatus(5);
            } else if (DeviceStatusConstantAdapter.MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_OTHER.equals(currentStatus)) {
                curState.setPhoneStatus(6);
            } else if (DeviceStatusConstantAdapter.MSDP_DEVICESTATUS_TYPE_UNKNOWN.equals(currentStatus)) {
                curState.setPhoneStatus(0);
            } else {
                curState.setPhoneStatus(0);
            }
            this.mCurrentStatus.add(curState);
        }
    }

    public List<CurrentStatus> getCurrentStatus() {
        ArrayList arrayList;
        synchronized (this.mCurrentStatus) {
            if (!this.mIsStatusValid && this.mCurrentStatus.size() == 0) {
                this.mCurrentStatus.add(new CurrentStatus(0, SystemClock.elapsedRealtime()));
            }
            arrayList = new ArrayList(this.mCurrentStatus);
        }
        return arrayList;
    }

    private int parseInt(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseInt NumberFormatException");
            return 0;
        }
    }

    private Long parseLong(String str) {
        long value = this.mCurrentStatusEnterTime;
        if (str == null || str.length() == 0) {
            return Long.valueOf(value);
        }
        try {
            value = Long.parseLong(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseLong NumberFormatException");
        }
        return Long.valueOf(value);
    }

    /* access modifiers changed from: private */
    public class DevMsdpDeviceStatusServiceConnection extends HwMsdpDeviceStatusServiceConnectionAdapter {
        private DevMsdpDeviceStatusServiceConnection() {
        }

        public void onServiceConnected() {
            AwareLog.d(PhoneStatusRecong.TAG, "onServiceConnected()");
            PhoneStatusRecong.this.mIsConnected = true;
            if (!PhoneStatusRecong.this.enableDeviceStatusEvent(DeviceStatusConstantAdapter.MSDP_DEVICESTATUS_TYPE_STILL_STATUS, DeviceStatusConstantAdapter.EVENT_TYPE_ENTER, PhoneStatusRecong.CALL_BACK_INTERVAL_DEFAULT_VALUE)) {
                AwareLog.e(PhoneStatusRecong.TAG, "enable error");
            }
        }

        public void onServiceDisconnected() {
            synchronized (PhoneStatusRecong.this.mCurrentStatus) {
                PhoneStatusRecong.this.mCurrentStatus.clear();
            }
            AwareLog.d(PhoneStatusRecong.TAG, "onServiceDisconnected() and clear cache");
            PhoneStatusRecong.this.mIsConnected = false;
        }
    }

    /* access modifiers changed from: private */
    public static class DevMsdpDeviceStatusChangedCallBack extends HwMsdpDeviceStatusChangedCallBackAdapter {
        private DevMsdpDeviceStatusChangedCallBack() {
        }

        public void onDeviceStatusChanged(HwMsdpDeviceStatusChangeEventAdapter hwMSDPDeviceStatusChangeEvent) {
            AwareLog.d(PhoneStatusRecong.TAG, "onDeviceStatusChanged");
        }
    }

    public static class CurrentStatus {
        private int mPhoneStatus;
        private long mTimeStamp;

        public CurrentStatus(int phoneStatus, long timeStamp) {
            this.mPhoneStatus = phoneStatus;
            this.mTimeStamp = timeStamp;
        }

        public int getPhoneStatus() {
            return this.mPhoneStatus;
        }

        public void setPhoneStatus(int phoneStatus) {
            this.mPhoneStatus = phoneStatus;
        }

        public long getTimeStamp() {
            return this.mTimeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.mTimeStamp = timeStamp;
        }

        public String toString() {
            return "CurrentStatus[ mPhoneStatus:" + this.mPhoneStatus + ", mTimeStamp:" + this.mTimeStamp + "]";
        }
    }
}
