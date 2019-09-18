package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatus;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusChangeEvent;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusChangedCallBack;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusEvent;
import com.huawei.msdp.devicestatus.HwMSDPDeviceStatusServiceConnection;
import java.util.ArrayList;
import java.util.List;

public class PhoneStatusRecong {
    private static final long CALL_BACK_INTERVAL_DEFAULT_VALUE = 86400000000000L;
    private static final int DEBUG_TRUE = 1;
    private static final String DEFAULT_PROPERTIES = "UNDEFINED";
    private static final long MIN_RETRY_TIME = 1800000;
    private static final int MSG_CONNECT_MSDP = 0;
    private static final String[] QCOM_CHIP_TYPE = {"sdm660", "sdm636"};
    private static final String TAG = "PhoneStatusRecong";
    private static PhoneStatusRecong sPhoneStatusRecong;
    private final boolean isQualcommPlatform = isQualComm(SystemProperties.get("ro.board.platform", DEFAULT_PROPERTIES));
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public final List<CurrentStatus> mCurrentStatus = new ArrayList();
    /* access modifiers changed from: private */
    public int mCurrentStatusCMC = 0;
    private long mCurrentStatusEnterTime = SystemClock.elapsedRealtime();
    /* access modifiers changed from: private */
    public long mCurrentStatusEnterTimeCMC = SystemClock.elapsedRealtime();
    private HwMSDPDeviceStatus mDevMSDPDeviceStatus;
    private DevMSDPDeviceStatusChangedCallBack mDevMSDPDeviceStatusChangedCallBack;
    private DevMSDPDeviceStatusServiceConnection mDevMSDPDeviceStatusServiceConnection;
    private DevSchedSensor mDevSchedSensor;
    /* access modifiers changed from: private */
    public boolean mIsConnected = false;
    private boolean mIsDebug = false;
    private boolean mIsStatusValid = true;
    private PhoneStatusHandler mPhoneStatusHandler = new PhoneStatusHandler();
    private long mRecordTime = 0;

    public static class CurrentStatus {
        private int mPhoneStatus;
        private long mTimestamp;

        public CurrentStatus(int phoneStatus, long timestamp) {
            this.mPhoneStatus = phoneStatus;
            this.mTimestamp = timestamp;
        }

        public int getPhoneStatus() {
            return this.mPhoneStatus;
        }

        public void setPhoneStatus(int phoneStatus) {
            this.mPhoneStatus = phoneStatus;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public void setTimestamp(long timestamp) {
            this.mTimestamp = timestamp;
        }

        public String toString() {
            return "CurrentStatus[ mPhoneStatus:" + this.mPhoneStatus + ", mTimestamp:" + this.mTimestamp + "]";
        }
    }

    private static class DevMSDPDeviceStatusChangedCallBack implements HwMSDPDeviceStatusChangedCallBack {
        private DevMSDPDeviceStatusChangedCallBack() {
        }

        public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) {
            AwareLog.d(PhoneStatusRecong.TAG, "onDeviceStatusChanged");
        }
    }

    private class DevMSDPDeviceStatusServiceConnection implements HwMSDPDeviceStatusServiceConnection {
        private DevMSDPDeviceStatusServiceConnection() {
        }

        public void onServiceConnected() {
            AwareLog.d(PhoneStatusRecong.TAG, "onServiceConnected()");
            boolean unused = PhoneStatusRecong.this.mIsConnected = true;
            if (!PhoneStatusRecong.this.enableDeviceStatusEvent(DeviceStatusConstant.MSDP_DEVICESTATUS_TYPE_STILL_STATUS, 1, PhoneStatusRecong.CALL_BACK_INTERVAL_DEFAULT_VALUE)) {
                AwareLog.e(PhoneStatusRecong.TAG, "enable error");
            }
        }

        public void onServiceDisconnected() {
            synchronized (PhoneStatusRecong.this.mCurrentStatus) {
                PhoneStatusRecong.this.mCurrentStatus.clear();
            }
            AwareLog.d(PhoneStatusRecong.TAG, "onServiceDisconnected() and clear cache");
            boolean unused = PhoneStatusRecong.this.mIsConnected = false;
        }
    }

    class DevSchedSensor implements SensorEventListener {
        private static final String TAG = "DevSchedSensor";
        private Sensor mSensor = null;
        private SensorManager mSensorManager = null;
        private float[] mValues = new float[3];

        public DevSchedSensor() {
            cmcSensorInit();
            AwareLog.d(TAG, "DevSchedSensor, init success.");
        }

        private void cmcSensorInit() {
            if (PhoneStatusRecong.this.mContext != null) {
                Context appContext = PhoneStatusRecong.this.mContext.getApplicationContext();
                if (appContext == null) {
                    AwareLog.i(TAG, "DevSchedSensor.init, appContext is null.");
                    return;
                }
                this.mSensorManager = (SensorManager) appContext.getSystemService("sensor");
                if (this.mSensorManager == null) {
                    AwareLog.i(TAG, "DevSchedSensor.init, mSensorManager is null.");
                    return;
                }
                this.mSensor = this.mSensorManager.getDefaultSensor(33171012);
                this.mSensorManager.registerListener(this, this.mSensor, 2);
                AwareLog.i(TAG, "DevSchedSensor init success. mSensor:" + this.mSensor);
            }
        }

        public final void onSensorChanged(SensorEvent event) {
            if (event == null) {
                AwareLog.e(TAG, "onSensorChanged, event is null");
                return;
            }
            synchronized (this.mValues) {
                this.mValues[0] = event.values[0];
                this.mValues[1] = event.values[1];
                this.mValues[2] = event.values[2];
            }
            int unused = PhoneStatusRecong.this.mCurrentStatusCMC = (int) event.values[0];
            long unused2 = PhoneStatusRecong.this.mCurrentStatusEnterTimeCMC = SystemClock.elapsedRealtime();
            AwareLog.d(TAG, "onSensorChanged, mValues, 0:" + this.mValues[0] + ", 1:" + this.mValues[1] + ", 2:" + this.mValues[2] + ", mCurrentStatusCMC:" + PhoneStatusRecong.this.mCurrentStatusCMC + ", mCurrentStatusEnterTimeCMC:" + PhoneStatusRecong.this.mCurrentStatusEnterTimeCMC);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            AwareLog.d(TAG, "onAccuracyChanged, sensor:" + sensor + ", accuracy:" + accuracy);
        }
    }

    private class PhoneStatusHandler extends Handler {
        private PhoneStatusHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.d(PhoneStatusRecong.TAG, "msg is null, error");
                return;
            }
            if (msg.what == 0) {
                PhoneStatusRecong.this.connectService();
            }
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

    public void init(Context context) {
        if (context != null) {
            this.mContext = context;
            initDevSchedSensor();
            this.mDevMSDPDeviceStatus = new HwMSDPDeviceStatus(context);
            this.mDevMSDPDeviceStatusChangedCallBack = new DevMSDPDeviceStatusChangedCallBack();
            this.mDevMSDPDeviceStatusServiceConnection = new DevMSDPDeviceStatusServiceConnection();
        }
    }

    private PhoneStatusRecong() {
    }

    private boolean isQualComm(String platformValue) {
        if (platformValue == null) {
            return false;
        }
        for (String chip : QCOM_CHIP_TYPE) {
            if (chip != null && platformValue.startsWith(chip)) {
                return true;
            }
        }
        return false;
    }

    private void initDevSchedSensor() {
        if (this.isQualcommPlatform) {
            this.mDevSchedSensor = new DevSchedSensor();
        }
    }

    public void connectService() {
        if (this.mContext != null && this.mDevMSDPDeviceStatusChangedCallBack != null && this.mDevMSDPDeviceStatusServiceConnection != null && this.mDevMSDPDeviceStatus != null) {
            AwareLog.d(TAG, "connectService");
            this.mDevMSDPDeviceStatus.connectService(this.mDevMSDPDeviceStatusChangedCallBack, this.mDevMSDPDeviceStatusServiceConnection);
        }
    }

    public void disconnectService() {
        if (this.mDevMSDPDeviceStatus != null && this.mIsConnected) {
            disableDeviceStatusEvent(DeviceStatusConstant.MSDP_DEVICESTATUS_TYPE_STILL_STATUS, 1);
            this.mDevMSDPDeviceStatus.disconnectService();
        }
    }

    /* access modifiers changed from: private */
    public boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
        if (r9.mDevMSDPDeviceStatus == null) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0039, code lost:
        return r9.mDevMSDPDeviceStatus.getCurrentDeviceStatus();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003a, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002d, code lost:
        return null;
     */
    private HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        synchronized (this.mCurrentStatus) {
            if (!this.mIsConnected) {
                this.mCurrentStatus.clear();
                long curTime = SystemClock.elapsedRealtime();
                if (curTime - this.mRecordTime >= 1800000) {
                    this.mRecordTime = curTime;
                    Message msg = this.mPhoneStatusHandler.obtainMessage();
                    msg.what = 0;
                    this.mPhoneStatusHandler.sendMessage(msg);
                }
            }
        }
    }

    public void getDeviceStatus() {
        if (!this.isQualcommPlatform || 1 != this.mCurrentStatusCMC) {
            HwMSDPDeviceStatusChangeEvent status = getCurrentDeviceStatus();
            if (status == null) {
                AwareLog.e(TAG, "status is null, error! mIsConnected is " + this.mIsConnected);
                this.mIsStatusValid = false;
                return;
            }
            Iterable<HwMSDPDeviceStatusEvent> statusList = status.getDeviceStatusRecognitionEvents();
            if (statusList == null) {
                this.mIsStatusValid = false;
                return;
            }
            this.mIsStatusValid = true;
            synchronized (this.mCurrentStatus) {
                this.mCurrentStatus.clear();
                for (HwMSDPDeviceStatusEvent event : statusList) {
                    if (event != null) {
                        long currentStatusEnterTime = event.getmTimestampNs();
                        String currentStatus = event.getmDeviceStatus();
                        setPhoneStatus(currentStatus, currentStatusEnterTime);
                        AwareLog.d(TAG, "Current status :" + currentStatus + ", current time :" + currentStatusEnterTime);
                    }
                }
            }
            return;
        }
        AwareLog.d(TAG, "getDeviceStatus, isQualcommPlatform:" + this.isQualcommPlatform + ", mCurrentStatusCMC:" + this.mCurrentStatusCMC);
        synchronized (this.mCurrentStatus) {
            this.mCurrentStatus.clear();
            this.mCurrentStatus.add(new CurrentStatus(1, this.mCurrentStatusEnterTimeCMC));
        }
        this.mIsStatusValid = true;
    }

    private void setPhoneStatus(String currentStatus, long currentStatusEnterTime) {
        if (currentStatus != null) {
            CurrentStatus curState = new CurrentStatus(0, currentStatusEnterTime);
            char c = 65535;
            switch (currentStatus.hashCode()) {
                case -1773012506:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICESTATUS_TYPE_UNKNOWN)) {
                        c = 6;
                        break;
                    }
                    break;
                case -734839525:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_HIGH_STILL)) {
                        c = 0;
                        break;
                    }
                    break;
                case -703279893:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_FAST_WALKING)) {
                        c = 4;
                        break;
                    }
                    break;
                case -662314917:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_OTHER)) {
                        c = 5;
                        break;
                    }
                    break;
                case 300619368:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_COARSE_STILL)) {
                        c = 1;
                        break;
                    }
                    break;
                case 605414195:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_FINE_STILL)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1435500548:
                    if (currentStatus.equals(DeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_WALKING)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    curState.setPhoneStatus(1);
                    break;
                case 1:
                    curState.setPhoneStatus(2);
                    break;
                case 2:
                    curState.setPhoneStatus(3);
                    break;
                case 3:
                    curState.setPhoneStatus(4);
                    break;
                case 4:
                    curState.setPhoneStatus(5);
                    break;
                case 5:
                    curState.setPhoneStatus(6);
                    break;
                case 6:
                    curState.setPhoneStatus(0);
                    break;
                default:
                    curState.setPhoneStatus(0);
                    break;
            }
            this.mCurrentStatus.add(curState);
        }
    }

    public List<CurrentStatus> getCurrentStatus() {
        synchronized (this.mCurrentStatus) {
            if (this.mIsDebug) {
                AwareLog.d(TAG, "getCurrentStatus debug is " + this.mCurrentStatus);
                List<CurrentStatus> list = this.mCurrentStatus;
                return list;
            }
            if (!this.mIsStatusValid && this.mCurrentStatus.size() == 0) {
                this.mCurrentStatus.add(new CurrentStatus(0, SystemClock.elapsedRealtime()));
            }
            ArrayList arrayList = new ArrayList(this.mCurrentStatus);
            return arrayList;
        }
    }

    public final boolean doDumpsys(String[] args) {
        if (args == null) {
            AwareLog.e(TAG, "args is null ,error!");
            return false;
        }
        try {
            synchronized (this.mCurrentStatus) {
                this.mCurrentStatus.clear();
                for (int i = 2; i < args.length; i += 2) {
                    this.mCurrentStatus.add(new CurrentStatus(parseInt(args[i]), parseLong(args[i + 1]).longValue()));
                }
                AwareLog.d(TAG, "set mCurrentStatus " + this.mCurrentStatus);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            AwareLog.e(TAG, "ArrayIndexOutOfBoundsException, args.length : " + args.length);
            return false;
        }
    }

    private int parseInt(String str) {
        int value = 0;
        if (str == null || str.length() == 0) {
            return 0;
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
}
