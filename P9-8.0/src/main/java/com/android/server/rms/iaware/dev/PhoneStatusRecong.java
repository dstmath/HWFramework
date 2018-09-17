package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatus;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusChangeEvent;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusChangedCallBack;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusEvent;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusServiceConnection;

public class PhoneStatusRecong {
    private static final int DEBUG_TRUE = 1;
    private static final String TAG = "PhoneStatusRecong";
    private static PhoneStatusRecong sPhoneStatusRecong;
    private int mCurrentStatus = 0;
    private long mCurrentStatusEnterTime = SystemClock.elapsedRealtime();
    private HwMSDPDeviceStatus mDevMSDPDeviceStatus;
    private boolean mIsConnected = false;
    private boolean mIsDebug = false;
    private boolean mIsStatusValid = true;

    private static class DevMSDPDeviceStatusChangedCallBack implements HwMSDPDeviceStatusChangedCallBack {
        /* synthetic */ DevMSDPDeviceStatusChangedCallBack(DevMSDPDeviceStatusChangedCallBack -this0) {
            this();
        }

        private DevMSDPDeviceStatusChangedCallBack() {
        }

        public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) {
            AwareLog.d(PhoneStatusRecong.TAG, "onDeviceStatusChanged");
        }
    }

    private class DevMSDPDeviceStatusServiceConnection implements HwMSDPDeviceStatusServiceConnection {
        /* synthetic */ DevMSDPDeviceStatusServiceConnection(PhoneStatusRecong this$0, DevMSDPDeviceStatusServiceConnection -this1) {
            this();
        }

        private DevMSDPDeviceStatusServiceConnection() {
        }

        public void onServiceConnected() {
            AwareLog.d(PhoneStatusRecong.TAG, "onServiceConnected()");
            PhoneStatusRecong.this.mIsConnected = PhoneStatusRecong.this.getSupportedDeviceStatus();
            if (PhoneStatusRecong.this.mIsConnected) {
                boolean flagAbsStatic = PhoneStatusRecong.this.enableDeviceStatusEvent(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_STILL_OF_ABSOLUTE_LEVEL_ONE, 1, 0);
                boolean flagRelativeStatic = PhoneStatusRecong.this.enableDeviceStatusEvent(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_LEVEL_ONE, 1, 0);
                if (!(flagAbsStatic && (flagRelativeStatic ^ 1) == 0)) {
                    AwareLog.e(PhoneStatusRecong.TAG, "enable error");
                    PhoneStatusRecong.this.mIsConnected = false;
                }
            }
        }

        public void onServiceDisconnected() {
            AwareLog.d(PhoneStatusRecong.TAG, "onServiceDisconnected()");
            if (PhoneStatusRecong.this.mIsConnected) {
                PhoneStatusRecong.this.disableDeviceStatusEvent(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_STILL_OF_ABSOLUTE_LEVEL_ONE, 1);
                PhoneStatusRecong.this.disableDeviceStatusEvent(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_LEVEL_ONE, 1);
            }
            PhoneStatusRecong.this.mIsConnected = false;
        }
    }

    public static synchronized PhoneStatusRecong getInstance() {
        PhoneStatusRecong phoneStatusRecong;
        synchronized (PhoneStatusRecong.class) {
            if (sPhoneStatusRecong == null) {
                sPhoneStatusRecong = new PhoneStatusRecong();
            }
            phoneStatusRecong = sPhoneStatusRecong;
        }
        return phoneStatusRecong;
    }

    public void connectService(Context context) {
        if (context != null) {
            this.mDevMSDPDeviceStatus = new HwMSDPDeviceStatus(context);
            AwareLog.d(TAG, "connectService");
            this.mDevMSDPDeviceStatus.connectService(new DevMSDPDeviceStatusChangedCallBack(), new DevMSDPDeviceStatusServiceConnection(this, null));
        }
    }

    public void disconnectService() {
        if (this.mDevMSDPDeviceStatus != null && this.mIsConnected) {
            this.mDevMSDPDeviceStatus.disconnectService();
        }
    }

    private boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        if (this.mDevMSDPDeviceStatus != null) {
            return this.mDevMSDPDeviceStatus.enableDeviceStatusEvent(deviceStatus, eventType, reportLatencyNs);
        }
        return false;
    }

    private boolean disableDeviceStatusEvent(String deviceStatus, int eventType) {
        if (this.mDevMSDPDeviceStatus != null) {
            return this.mDevMSDPDeviceStatus.disableDeviceStatusEvent(deviceStatus, eventType);
        }
        return false;
    }

    private HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        if (this.mIsConnected && this.mDevMSDPDeviceStatus != null) {
            return this.mDevMSDPDeviceStatus.getCurrentDeviceStatus();
        }
        return null;
    }

    public void getDeviceStatus() {
        HwMSDPDeviceStatusChangeEvent status = getCurrentDeviceStatus();
        if (status == null) {
            this.mIsStatusValid = false;
            return;
        }
        Iterable<HwMSDPDeviceStatusEvent> statusList = status.getDeviceStatusRecognitionEvents();
        if (statusList == null) {
            this.mIsStatusValid = false;
            return;
        }
        this.mIsStatusValid = true;
        for (HwMSDPDeviceStatusEvent event : statusList) {
            this.mCurrentStatusEnterTime = event.getmTimestampNs();
            String currentStatus = event.getmDeviceStatus();
            setPhoneStatus(currentStatus);
            AwareLog.d(TAG, "Current status is " + currentStatus + " current time is " + this.mCurrentStatusEnterTime);
        }
    }

    private void setPhoneStatus(String currentStatus) {
        if (currentStatus != null) {
            if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_STILL_OF_ABSOLUTE_LEVEL_ONE)) {
                this.mCurrentStatus = 1;
            } else if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_LEVEL_ONE)) {
                this.mCurrentStatus = 2;
            } else if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICESTATUS_TYPE_UNKNOWN)) {
                this.mCurrentStatus = 0;
            } else {
                this.mCurrentStatus = 0;
            }
        }
    }

    public int getCurrentStatus() {
        if (this.mIsDebug) {
            AwareLog.d(TAG, "getCurrentStatus debug is " + this.mCurrentStatus);
            return this.mCurrentStatus;
        }
        if (!this.mIsStatusValid) {
            this.mCurrentStatus = 0;
        }
        return this.mCurrentStatus;
    }

    public long getCurrentStatusEnterTime() {
        if (this.mIsDebug) {
            AwareLog.d(TAG, "getCurrentStatusEnterTime debug is " + this.mCurrentStatusEnterTime);
        }
        return this.mCurrentStatusEnterTime;
    }

    public final boolean doDumpsys(String[] args) {
        this.mIsDebug = parseInt(args[1]) == 1;
        this.mCurrentStatus = parseInt(args[2]);
        this.mCurrentStatusEnterTime = parseLong(args[3]).longValue();
        AwareLog.d(TAG, "set mCurrentStatus " + this.mCurrentStatus + " set mCurrentStatusEnterTime " + this.mCurrentStatusEnterTime);
        return true;
    }

    private int parseInt(String str) {
        int value = 0;
        if (str == null || str.length() == 0) {
            return value;
        }
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseInt NumberFormatException");
        }
        return value;
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

    public boolean getSupportedDeviceStatus() {
        if (this.mDevMSDPDeviceStatus != null) {
            String[] status = this.mDevMSDPDeviceStatus.getSupportedDeviceStatus();
            if (status != null && status.length > 0) {
                AwareLog.d(TAG, "getSupportedDeviceStatus ok");
                return true;
            }
        }
        return false;
    }
}
