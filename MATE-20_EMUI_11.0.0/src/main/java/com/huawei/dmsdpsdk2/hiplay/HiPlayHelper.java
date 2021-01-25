package com.huawei.dmsdpsdk2.hiplay;

import android.os.Handler;
import android.os.Message;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.android.net.wifi.WifiManagerCommonEx;
import com.huawei.dmsdp.devicevirtualization.Capability;
import com.huawei.dmsdpsdk2.DMSDPConfig;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.common.Utils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class HiPlayHelper {
    public static final String ACCESS_VERSION = "accessVersion";
    public static final String ACCESS_VERSION_AP = "2";
    private static final String DEVICE_CONN_BUSY = "busy";
    private static final String DISCOVER_HICOM = "HICOM";
    private static final String DISCOVER_HILINK = "HILINK";
    private static final int HASHCODE_17 = 17;
    private static final int HASHCODE_31 = 31;
    private static final Object HIPLAY_ADAPTER_LOCK = new Object();
    private static final Object HIPLAY_DISCOVERY_LOCK = new Object();
    private static final int HIPLAY_DISC_TIME_MS = 3000;
    private static final int HIPLAY_SERVICE_FILTER = 511;
    private static final int INT_TWO = 2;
    private static final String KEY_DEVICE_CONN_STATUS = "status";
    private static final int MAX_RETRY_TIMES = 2;
    private static final int OPERATE_TIMEOUT_AP = 10000;
    private static final int OPERATE_TIMEOUT_ROUTER = 5000;
    private static final int OPERATE_TIMEOUT_SOFTBUS = 20000;
    private static final int RETRY_TIMES_SOFTBUS = 1;
    public static final String STEREO_MODE = "stereoMode";
    public static final String STEREO_MODE_SINGLE = "0";
    private static final String TAG = "HiPlayHelper";
    private static final int WORKER_POOL_SIZE = 1;
    private DeviceOperationParams mActiveOptParams = null;
    private DeviceOperationHandler mDeviceOptHandler = new DeviceOperationHandler();
    private boolean mDisableOptFinish = true;
    private final Runnable mDisableTimeout = new Runnable() {
        /* class com.huawei.dmsdpsdk2.hiplay.HiPlayHelper.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            if (HiPlayHelper.this.mDisableOptFinish) {
                HwLog.w(HiPlayHelper.TAG, "opertaion finish, no need report timeout");
                return;
            }
            HiPlayHelper.this.mDisableOptFinish = true;
            if (HiPlayHelper.this.mActiveOptParams == null || HiPlayHelper.this.mHiPlayListener == null) {
                HwLog.e(HiPlayHelper.TAG, "connection param or hiplaylistener is null, can not report timeout");
                return;
            }
            HwLog.i(HiPlayHelper.TAG, "Disable timeout");
            Iterator it = HiPlayHelper.this.mActiveOptParams.getCapability().iterator();
            while (it.hasNext()) {
                HiPlayHelper.this.mHiPlayListener.onVirtualDeviceFailed(HiPlayHelper.this.mActiveOptParams.getDevice(), (Capability) it.next(), DMSDPConfig.DISADBLE_FAILED);
            }
            HiPlayHelper hiPlayHelper = HiPlayHelper.this;
            hiPlayHelper.reportHiPlayDeviceState(hiPlayHelper.mActiveOptParams.getDeviceId(), null, DeviceState.STOP_CONTINUTING_FAILED);
        }
    };
    private DiscoveryTask mDiscThread;
    private final Runnable mDiscoverTimeout = new Runnable() {
        /* class com.huawei.dmsdpsdk2.hiplay.HiPlayHelper.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            HwLog.w(HiPlayHelper.TAG, "Discover task timeout, task retry times: " + HiPlayHelper.this.mActiveOptParams.getRetryTimes());
            if (HiPlayHelper.this.mActiveOptParams.getRetryTimes() < 2) {
                HwLog.w(HiPlayHelper.TAG, "retry enable task");
                HiPlayHelper.this.stopDiscover();
                HiPlayHelper.this.mDeviceOptHandler.removeCallbacks(HiPlayHelper.this.mEnableTimeout);
                HiPlayHelper hiPlayHelper = HiPlayHelper.this;
                HiPlayTask retryTask = new HiPlayTask(hiPlayHelper.mActiveOptParams.getDevice(), HiPlayHelper.this.mActiveOptParams.getCapability(), HiPlayHelper.this.mActiveOptParams.getJsonString(), HiPlayHelper.this.mActiveOptParams.getMode(), HiPlayHelper.this.mActiveOptParams.getRetryTimes() + 1);
                HiPlayHelper.this.mWorkQueue.onTaskFinishOnly();
                HiPlayHelper.this.addRetryTask(retryTask);
                return;
            }
            synchronized (HiPlayHelper.HIPLAY_DISCOVERY_LOCK) {
                HwLog.e(HiPlayHelper.TAG, "discover timeout, report it");
                HieventHiplayError.reportError(1);
                HiPlayHelper.HIPLAY_DISCOVERY_LOCK.notifyAll();
                HiPlayHelper.this.reportEnableOptFailed();
            }
        }
    };
    private boolean mEnableOptFinish = true;
    private final Runnable mEnableTimeout = new Runnable() {
        /* class com.huawei.dmsdpsdk2.hiplay.HiPlayHelper.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (HiPlayHelper.this.mEnableOptFinish) {
                HwLog.w(HiPlayHelper.TAG, "opertaion finish, no need report timeout");
            } else if (HiPlayHelper.this.mActiveOptParams == null) {
                HwLog.e(HiPlayHelper.TAG, "connection param or hiplaylistener is null, can not report timeout");
            } else {
                HwLog.i(HiPlayHelper.TAG, "Enable timeout, task retry times: " + HiPlayHelper.this.mActiveOptParams.getRetryTimes());
                if (HiPlayHelper.this.mActiveOptParams.getRetryTimes() < 2) {
                    HwLog.w(HiPlayHelper.TAG, "retry enable task");
                    HiPlayHelper hiPlayHelper = HiPlayHelper.this;
                    HiPlayTask retryTask = new HiPlayTask(hiPlayHelper.mActiveOptParams.getDevice(), HiPlayHelper.this.mActiveOptParams.getCapability(), HiPlayHelper.this.mActiveOptParams.getJsonString(), HiPlayHelper.this.mActiveOptParams.getMode(), HiPlayHelper.this.mActiveOptParams.getRetryTimes() + 1);
                    HiPlayHelper.this.mWorkQueue.onTaskFinishOnly();
                    HiPlayHelper.this.addRetryTask(retryTask);
                    return;
                }
                HwLog.e(HiPlayHelper.TAG, "task reach MAX retry times, report failed");
                HieventHiplayError.reportError(3);
                HiPlayHelper.this.mEnableOptFinish = true;
                if (HiPlayHelper.this.mHiPlayListener == null) {
                    HwLog.e(HiPlayHelper.TAG, "hiplaylistener is null, can not report timeout");
                    return;
                }
                Iterator it = HiPlayHelper.this.mActiveOptParams.getCapability().iterator();
                while (it.hasNext()) {
                    HiPlayHelper.this.mHiPlayListener.onVirtualDeviceFailed(HiPlayHelper.this.mActiveOptParams.getDevice(), (Capability) it.next(), DMSDPConfig.CONNECT_DEVICE_TIMEOUT);
                }
                HiPlayHelper hiPlayHelper2 = HiPlayHelper.this;
                hiPlayHelper2.reportHiPlayDeviceState(hiPlayHelper2.mActiveOptParams.getDeviceId(), null, DeviceState.CONTINUTING_FAILED);
            }
        }
    };
    private HiPlayAdapter mHiPlayAdapter = null;
    private HiPlayListener mHiPlayListener;
    private DeviceServiceHandler mHiPlayRetHandler = new DeviceServiceHandler();
    private WorkQueue mWorkQueue = null;

    public enum AccessVersion {
        ACCESS_UNKONWN,
        ACCESS_ROUTER,
        ACCESS_AP
    }

    public static class ConnectStat {
        public static final int CONNECTED = 1;
        public static final int CONNECTING = 0;
        public static final int DISCONNECT = -1;
        public static final int DISCONNECTING = -2;
    }

    public static class HiPlayOptEvent {
        public static final int HIPLAY_DEVICE_OPERATION_MSG_DISABLE = 3;
        public static final int HIPLAY_DEVICE_OPERATION_MSG_ENABLE = 2;
        public static final int HIPLAY_DEVICE_OPERATION_MSG_FOUND_NEW_DEVICE = 1;
    }

    public static class HiPlayOptResult {
        public static final int SERVICE_FAIL = 2;
        public static final int SERVICE_SUCCESS = 1;
    }

    public enum OptMode {
        ENABLE,
        DISABLE
    }

    public Boolean isHaveRunningTask() {
        if (this.mWorkQueue == null || this.mEnableOptFinish) {
            return false;
        }
        return true;
    }

    public List<String> reportDeviceStatus(String dataStr) {
        HwLog.i(TAG, "reportDeviceStatus");
        List<String> deviceIdList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(dataStr);
            if (jsonObject.has(KEY_DEVICE_CONN_STATUS)) {
                if (!BuildConfig.FLAVOR.equals(jsonObject.getString(KEY_DEVICE_CONN_STATUS))) {
                    if (!jsonObject.getString(KEY_DEVICE_CONN_STATUS).equals(DEVICE_CONN_BUSY)) {
                        return deviceIdList;
                    }
                    HwLog.e(TAG, "remote device is busy, report it");
                    Iterator it = this.mActiveOptParams.getCapability().iterator();
                    while (it.hasNext()) {
                        Capability cap = (Capability) it.next();
                        this.mHiPlayListener.onVirtualDeviceFailed(this.mActiveOptParams.getDevice(), cap, DMSDPConfig.DEVICE_BUSY);
                        this.mHiPlayListener.onDeviceStateChange(new DeviceState(this.mActiveOptParams.getDeviceId(), cap, DeviceState.CONTINUTING_FAILED));
                        deviceIdList.add(this.mActiveOptParams.getDeviceId());
                    }
                    clearEnableTimeoutTimer(true);
                    if (this.mWorkQueue != null) {
                        this.mWorkQueue.onTaskFinish();
                    }
                    return deviceIdList;
                }
            }
            HwLog.e(TAG, "device's status is invalid" + dataStr);
            return deviceIdList;
        } catch (JSONException e) {
            HwLog.e(TAG, "JSON parse fail.");
            return deviceIdList;
        }
    }

    public void reportEnableOptFailed() {
        this.mEnableOptFinish = true;
        this.mDeviceOptHandler.removeCallbacks(this.mEnableTimeout);
        StringBuilder sb = new StringBuilder();
        sb.append("mWorkQueue is null: ");
        String str = "true";
        sb.append(this.mWorkQueue == null ? str : "false");
        sb.append(", mEnableOptFinish: ");
        sb.append(this.mEnableOptFinish ? str : "false");
        sb.append(", mDisableOptFinish: ");
        if (!this.mDisableOptFinish) {
            str = "false";
        }
        sb.append(str);
        HwLog.i(TAG, sb.toString());
        WorkQueue workQueue = this.mWorkQueue;
        if (workQueue != null && this.mEnableOptFinish && this.mDisableOptFinish) {
            workQueue.onTaskFinish();
        }
        DeviceOperationParams deviceOperationParams = this.mActiveOptParams;
        if (deviceOperationParams == null || this.mHiPlayListener == null) {
            HwLog.w(TAG, "opt params is null, can not report");
            return;
        }
        Iterator it = deviceOperationParams.getCapability().iterator();
        while (it.hasNext()) {
            this.mHiPlayListener.onVirtualDeviceFailed(this.mActiveOptParams.getDevice(), (Capability) it.next(), DMSDPConfig.CONNECT_DEVICE_FAILED);
        }
        reportHiPlayDeviceState(this.mActiveOptParams.getDeviceId(), null, DeviceState.CONTINUTING_FAILED);
    }

    public void reportDisableOptFailed() {
        this.mDisableOptFinish = true;
        this.mDeviceOptHandler.removeCallbacks(this.mDisableTimeout);
        StringBuilder sb = new StringBuilder();
        sb.append("mWorkQueue is null: ");
        String str = "true";
        sb.append(this.mWorkQueue == null ? str : "false");
        sb.append(", mEnableOptFinish: ");
        sb.append(this.mEnableOptFinish ? str : "false");
        sb.append(", mDisableOptFinish: ");
        if (!this.mDisableOptFinish) {
            str = "false";
        }
        sb.append(str);
        HwLog.i(TAG, sb.toString());
        WorkQueue workQueue = this.mWorkQueue;
        if (workQueue != null && this.mEnableOptFinish && this.mDisableOptFinish) {
            workQueue.onTaskFinish();
        }
        DeviceOperationParams deviceOperationParams = this.mActiveOptParams;
        if (deviceOperationParams == null || this.mHiPlayListener == null) {
            HwLog.w(TAG, "opt params or hiplay listener is null, can not report");
            return;
        }
        Iterator it = deviceOperationParams.getCapability().iterator();
        while (it.hasNext()) {
            this.mHiPlayListener.onVirtualDeviceFailed(this.mActiveOptParams.getDevice(), (Capability) it.next(), DMSDPConfig.DISADBLE_FAILED);
        }
        reportHiPlayDeviceState(this.mActiveOptParams.getDeviceId(), null, DeviceState.STOP_CONTINUTING_FAILED);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0072: APUT  (r2v3 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r4v1 java.lang.String) */
    public void reportHiPlayDeviceState(String deviceId, Capability capability, String stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("mWorkQueue is null: ");
        String str = "true";
        sb.append(this.mWorkQueue == null ? str : "false");
        sb.append(", mEnableOptFinish: ");
        sb.append(this.mEnableOptFinish ? str : "false");
        sb.append(", mDisableOptFinish: ");
        if (!this.mDisableOptFinish) {
            str = "false";
        }
        sb.append(str);
        HwLog.i(TAG, sb.toString());
        WorkQueue workQueue = this.mWorkQueue;
        if (workQueue != null && this.mEnableOptFinish && this.mDisableOptFinish) {
            workQueue.onTaskFinish();
        }
        if (this.mHiPlayListener == null) {
            HwLog.i(TAG, "HiPlayListener is null, can not report to hiplay");
        } else if (deviceId == null || stat == null) {
            HwLog.e(TAG, "param is null for report device stat");
        } else {
            Locale locale = Locale.ENGLISH;
            Object[] objArr = new Object[3];
            objArr[0] = transSensiInfo(deviceId);
            objArr[1] = capability != null ? capability.getProfileDesc() : BuildConfig.FLAVOR;
            objArr[2] = stat;
            HwLog.i(TAG, String.format(locale, "device id: %s, cap: %s, stat: %s", objArr));
            this.mHiPlayListener.onDeviceStateChange(new DeviceState(deviceId, capability, stat));
        }
    }

    public void reportHiPlayDeviceState(String deviceId, String stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("mWorkQueue is null: ");
        String str = "true";
        sb.append(this.mWorkQueue == null ? str : "false");
        sb.append(", mEnableOptFinish: ");
        sb.append(this.mEnableOptFinish ? str : "false");
        sb.append(", mDisableOptFinish: ");
        if (!this.mDisableOptFinish) {
            str = "false";
        }
        sb.append(str);
        HwLog.i(TAG, sb.toString());
        if (deviceId == null || stat == null) {
            HwLog.e(TAG, "param is null for report device stat");
            return;
        }
        if (!this.mEnableOptFinish && stat.equals(DeviceState.CONTINUTING_FAILED)) {
            clearEnableTimeoutTimer(true);
        }
        WorkQueue workQueue = this.mWorkQueue;
        if (workQueue != null && this.mEnableOptFinish && this.mDisableOptFinish) {
            workQueue.onTaskFinish();
        }
        if (this.mHiPlayListener == null) {
            HwLog.i(TAG, "HiPlayListener is null, can not report to hiplay");
            return;
        }
        HwLog.i(TAG, String.format(Locale.ENGLISH, "device id: %s, stat: %s", transSensiInfo(deviceId), stat));
        this.mHiPlayListener.onDeviceStateChange(new DeviceState(deviceId, null, stat));
    }

    public void stopDiscover() {
        if (this.mDiscThread != null) {
            HwLog.d(TAG, "start to stop discover thread");
            if (this.mDiscThread.isAlive()) {
                HwLog.i(TAG, "Stop discovery thread");
                this.mDiscThread.stopDiscTask();
                try {
                    this.mDiscThread.join(10000);
                } catch (InterruptedException e) {
                    HwLog.e(TAG, "stop discover thread failed");
                }
            }
            this.mDiscThread = null;
            HwLog.d(TAG, "start to stop discover end");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearEnableTimeoutTimer(boolean isTaskFinish) {
        this.mEnableOptFinish = isTaskFinish;
        this.mDeviceOptHandler.removeCallbacks(this.mEnableTimeout);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearDisableTimeoutTimer(boolean isTaskFinish) {
        this.mDisableOptFinish = isTaskFinish;
        this.mDeviceOptHandler.removeCallbacks(this.mDisableTimeout);
    }

    public static String transSensiInfo(String info) {
        return transSensiInfo(info, 2, 2, true);
    }

    private static String transSensiInfo(String info, int left, int right, boolean basedOnLeft) {
        if (info == null || info.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sbText = new StringBuilder(1);
        int length = info.length();
        if ((length - left) - right > 0) {
            String prefix = info.substring(0, left);
            String suffix = info.substring(length - right);
            sbText.append(prefix);
            sbText.append("****");
            sbText.append(suffix);
        } else if (basedOnLeft) {
            if (length <= left || left <= 0) {
                sbText.append(info.substring(0, 1));
                sbText.append("****");
            } else {
                sbText.append(info.substring(0, left));
                sbText.append("****");
            }
        } else if (length <= right || right <= 0) {
            sbText.append("****");
            sbText.append(info.substring(info.length() - 1));
        } else {
            sbText.append("****");
            sbText.append(info.substring(length - right));
        }
        return sbText.toString();
    }

    public void sendMessageToHiPlayRetHandler(int what, int arg1, int arg2, Object obj) {
        this.mHiPlayRetHandler.sendMessage(this.mHiPlayRetHandler.obtainMessage(what, arg1, arg2, obj));
    }

    public void sendMessageToDeviceOptHandler(int what, int arg1, int arg2, Object obj) {
        this.mDeviceOptHandler.sendMessage(this.mDeviceOptHandler.obtainMessage(what, arg1, arg2, obj));
    }

    public void sendMessageToDeviceOptHandler(int what, Object obj) {
        this.mDeviceOptHandler.sendMessage(this.mDeviceOptHandler.obtainMessage(what, obj));
    }

    /* access modifiers changed from: protected */
    public void notifyDeviceFound() {
        synchronized (HIPLAY_DISCOVERY_LOCK) {
            HwLog.i(TAG, "notify found device, stop discover");
            HIPLAY_DISCOVERY_LOCK.notifyAll();
            this.mDeviceOptHandler.removeCallbacks(this.mDiscoverTimeout);
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasEnableTaskRunning() {
        return !this.mEnableOptFinish;
    }

    /* access modifiers changed from: private */
    public class DiscoveryTask extends Thread {
        public DiscoveryTask() {
        }

        public void stopDiscTask() {
            synchronized (HiPlayHelper.HIPLAY_DISCOVERY_LOCK) {
                HiPlayHelper.HIPLAY_DISCOVERY_LOCK.notifyAll();
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            synchronized (HiPlayHelper.HIPLAY_DISCOVERY_LOCK) {
                boolean holdOn = true;
                while (holdOn) {
                    HwLog.i(HiPlayHelper.TAG, "DiscoveryTask run");
                    if (HiPlayHelper.this.mHiPlayAdapter == null) {
                        HwLog.e(HiPlayHelper.TAG, "mHiPlayAdapter is null");
                        return;
                    }
                    int ret = HiPlayHelper.this.mHiPlayAdapter.startDiscover(HiPlayHelper.this.mActiveOptParams.getDiscoverProc(), HiPlayHelper.this.mActiveOptParams.getDeviceFilterType(), 511);
                    if (ret != 0) {
                        HwLog.e(HiPlayHelper.TAG, "start discover device failed, error " + ret);
                        HiPlayHelper.this.reportEnableOptFailed();
                        return;
                    }
                    HiPlayHelper.this.mDeviceOptHandler.postDelayed(HiPlayHelper.this.mDiscoverTimeout, 3000);
                    try {
                        HiPlayHelper.HIPLAY_DISCOVERY_LOCK.wait();
                    } catch (InterruptedException e) {
                        HwLog.e(HiPlayHelper.TAG, "Wait on Discover lock failed");
                    }
                    holdOn = false;
                    HiPlayHelper.this.stopDiscoverAfterFoundDevice();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDiscoverAfterFoundDevice() {
        HiPlayAdapter hiPlayAdapter = this.mHiPlayAdapter;
        if (hiPlayAdapter != null) {
            int ret = hiPlayAdapter.stopDiscover(this.mActiveOptParams.getDiscoverProc());
            HwLog.d(TAG, "stop discover device, protocol: " + this.mActiveOptParams.getDiscoverProc() + ", ret " + ret);
        }
    }

    public static class DeviceOperationParams {
        private AccessVersion accessVersion;
        private EnumSet<Capability> capability;
        private HiPlayDevice device;
        private int deviceFilterType;
        private String deviceId;
        private int deviceType;
        private int discoverProc;
        private String jsonString;
        private OptMode mode;
        private int retryTimes;
        private Set<Integer> serviceTypes;
        private String stereoMode;

        public DeviceOperationParams(String deviceId2, EnumSet<Capability> capability2, String connJson, OptMode mode2) {
            this.deviceId = deviceId2;
            this.capability = capability2;
            this.jsonString = connJson;
            this.mode = mode2;
            this.serviceTypes = new HashSet();
            this.retryTimes = 0;
            this.accessVersion = AccessVersion.ACCESS_ROUTER;
            this.stereoMode = HiPlayHelper.STEREO_MODE_SINGLE;
            Iterator it = capability2.iterator();
            while (it.hasNext()) {
                if (HiPlayUtils.isHiPlayNeedCapability((Capability) it.next())) {
                    this.serviceTypes.add(4);
                }
            }
        }

        public DeviceOperationParams(String deviceId2, EnumSet<Capability> capability2, String connJson, OptMode mode2, int retryTimes2) {
            this.deviceId = deviceId2;
            this.capability = capability2;
            this.jsonString = connJson;
            this.mode = mode2;
            this.serviceTypes = new HashSet();
            this.retryTimes = retryTimes2;
            this.accessVersion = AccessVersion.ACCESS_ROUTER;
            this.stereoMode = HiPlayHelper.STEREO_MODE_SINGLE;
            Iterator it = capability2.iterator();
            while (it.hasNext()) {
                if (HiPlayUtils.isHiPlayNeedCapability((Capability) it.next())) {
                    this.serviceTypes.add(4);
                }
            }
        }

        public void init(HiPlayDevice device2) {
            String accessVer;
            this.device = device2;
            this.deviceType = device2.getDeviceType();
            this.discoverProc = 0;
            if (OptMode.ENABLE.equals(this.mode)) {
                try {
                    JSONObject jsonObject = new JSONObject(this.jsonString);
                    if (jsonObject.has(HiPlayUtils.DISCOVER_MODE)) {
                        String disMode = jsonObject.getString(HiPlayUtils.DISCOVER_MODE);
                        if (HiPlayHelper.DISCOVER_HICOM.equals(disMode)) {
                            this.discoverProc = 64;
                        }
                        if (HiPlayHelper.DISCOVER_HILINK.equals(disMode)) {
                            this.discoverProc = 32;
                        }
                    }
                    if (jsonObject.has(HiPlayHelper.ACCESS_VERSION) && (accessVer = jsonObject.getString(HiPlayHelper.ACCESS_VERSION)) != null && accessVer.equals(HiPlayHelper.ACCESS_VERSION_AP)) {
                        this.accessVersion = AccessVersion.ACCESS_AP;
                    }
                    if (jsonObject.has(HiPlayHelper.STEREO_MODE)) {
                        this.stereoMode = jsonObject.getString(HiPlayHelper.STEREO_MODE);
                        if (this.stereoMode == null) {
                            this.stereoMode = HiPlayHelper.STEREO_MODE_SINGLE;
                        }
                    }
                    this.accessVersion = checkAccessVersion();
                    if (this.accessVersion == AccessVersion.ACCESS_AP) {
                        this.deviceType = 10;
                    }
                } catch (JSONException e) {
                    HwLog.e(HiPlayHelper.TAG, "Parse conn json failed");
                }
            }
            if (this.deviceType == 3) {
                this.deviceFilterType = 4;
            }
            if (this.deviceType == 6) {
                this.deviceFilterType = 32;
            }
        }

        public HiPlayDevice getDevice() {
            return this.device;
        }

        public void setDevice(HiPlayDevice device2) {
            this.device = device2;
        }

        public String getDeviceId() {
            return this.deviceId;
        }

        public EnumSet<Capability> getCapability() {
            return this.capability;
        }

        public String getJsonString() {
            return this.jsonString;
        }

        public Set<Integer> getServiceTypes() {
            return this.serviceTypes;
        }

        public int getDeviceType() {
            return this.deviceType;
        }

        public void setDeviceType(int deviceType2) {
            this.deviceType = deviceType2;
        }

        public int getDiscoverProc() {
            return this.discoverProc;
        }

        public void setDiscoverProc(int discoverProc2) {
            this.discoverProc = discoverProc2;
        }

        public int getDeviceFilterType() {
            return this.deviceFilterType;
        }

        public void setDeviceFilterType(int deviceFilterType2) {
            this.deviceFilterType = deviceFilterType2;
        }

        public OptMode getMode() {
            return this.mode;
        }

        public void setMode(OptMode mode2) {
            this.mode = mode2;
        }

        public int getRetryTimes() {
            return this.retryTimes;
        }

        public void setRetryTimes(int retryTimes2) {
            this.retryTimes = retryTimes2;
        }

        public AccessVersion getAccessVersion() {
            return this.accessVersion;
        }

        public String getDeviceName() {
            HiPlayDevice hiPlayDevice = this.device;
            if (hiPlayDevice != null) {
                return hiPlayDevice.getDeviceName();
            }
            return BuildConfig.FLAVOR;
        }

        private AccessVersion checkAccessVersion() {
            AccessVersion accessVer = AccessVersion.ACCESS_ROUTER;
            if (this.accessVersion != AccessVersion.ACCESS_AP) {
                return accessVer;
            }
            boolean isRsdbSupported = isRsdbSupport();
            HwLog.i(HiPlayHelper.TAG, "check RsdbSupport is " + isRsdbSupported);
            if (this.stereoMode.equals(HiPlayHelper.STEREO_MODE_SINGLE) && !isRsdbSupported) {
                return AccessVersion.ACCESS_ROUTER;
            }
            if (this.stereoMode.equals(HiPlayHelper.STEREO_MODE_SINGLE) || isRsdbSupported) {
                return AccessVersion.ACCESS_AP;
            }
            return AccessVersion.ACCESS_UNKONWN;
        }

        private boolean isRsdbSupport() {
            return WifiManagerCommonEx.isRSDBSupported();
        }
    }

    public static class DevSvrId {
        private String deviceId;
        private String serviceId;
        private int serviceType;

        public DevSvrId(String deviceId2, String serviceId2, int serviceType2) {
            this.deviceId = deviceId2;
            this.serviceId = serviceId2;
            this.serviceType = serviceType2;
        }

        public String getDeviceId() {
            return this.deviceId;
        }

        public void setDeviceId(String deviceId2) {
            this.deviceId = deviceId2;
        }

        public String getServiceId() {
            return this.serviceId;
        }

        public void setServiceId(String serviceId2) {
            this.serviceId = serviceId2;
        }

        public int getServiceType() {
            return this.serviceType;
        }

        public void setServiceType(int serviceType2) {
            this.serviceType = serviceType2;
        }

        public boolean equals(Object obj) {
            String str;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof DevSvrId)) {
                return false;
            }
            DevSvrId newObj = (DevSvrId) obj;
            String str2 = this.deviceId;
            if (str2 == null || !str2.equals(newObj.getDeviceId()) || (str = this.serviceId) == null || !str.equals(newObj.getServiceId()) || this.serviceType != newObj.getServiceType()) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (((((17 * HiPlayHelper.HASHCODE_31) + getDeviceId().hashCode()) * HiPlayHelper.HASHCODE_31) + getServiceId().hashCode()) * HiPlayHelper.HASHCODE_31) + getServiceType();
        }
    }

    public void stop() {
        WorkQueue workQueue = this.mWorkQueue;
        if (workQueue == null) {
            HwLog.w(TAG, "no worker threads, no need stop");
        } else {
            workQueue.stop();
        }
    }

    public void clearHiPlayAdapter() {
        synchronized (HIPLAY_ADAPTER_LOCK) {
            HwLog.i(TAG, "clear HiPlayAdapter in HiPlayHelper");
            this.mHiPlayAdapter = null;
        }
    }

    public class HiPlayTask implements Runnable {
        private EnumSet<Capability> capability;
        private String connJson;
        private HiPlayDevice device;
        private OptMode mode;
        private int retryTimes;

        public HiPlayTask(HiPlayDevice device2, EnumSet<Capability> capability2, String connJson2, OptMode mode2) {
            this.device = device2;
            this.capability = capability2;
            this.connJson = connJson2;
            this.mode = mode2;
            this.retryTimes = 0;
        }

        public HiPlayTask(HiPlayDevice device2, EnumSet<Capability> capability2, String connJson2, OptMode mode2, int retryTimes2) {
            this.device = device2;
            this.capability = capability2;
            this.connJson = connJson2;
            this.mode = mode2;
            this.retryTimes = retryTimes2;
        }

        public HiPlayDevice getDevice() {
            return this.device;
        }

        public void setDevice(HiPlayDevice device2) {
            this.device = device2;
        }

        public EnumSet<Capability> getCapability() {
            return this.capability;
        }

        public void setCapability(EnumSet<Capability> capability2) {
            this.capability = capability2;
        }

        public String getConnJson() {
            return this.connJson;
        }

        public void setConnJson(String connJson2) {
            this.connJson = connJson2;
        }

        public OptMode getMode() {
            return this.mode;
        }

        public void setMode(OptMode mode2) {
            this.mode = mode2;
        }

        public int getRetryTimes() {
            return this.retryTimes;
        }

        public void setRetryTimes(int retryTimes2) {
            this.retryTimes = retryTimes2;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (OptMode.ENABLE.equals(getMode())) {
                HwLog.i(HiPlayHelper.TAG, String.format(Locale.ENGLISH, "enable virtual device, device id: %s, capability: %s, conn json: %s", HiPlayHelper.transSensiInfo(this.device.getDeviceId()), Utils.getEnumSetString(this.capability), this.connJson));
                HiPlayHelper.this.mEnableOptFinish = false;
                DeviceOperationParams params = new DeviceOperationParams(this.device.getDeviceId(), this.capability, this.connJson, OptMode.ENABLE, this.retryTimes);
                params.init(this.device);
                HiPlayHelper.this.mActiveOptParams = params;
                HiPlayHelper.this.mDeviceOptHandler.sendMessage(HiPlayHelper.this.mDeviceOptHandler.obtainMessage(2, params));
                long timeout = 5000;
                if (params.accessVersion == AccessVersion.ACCESS_AP) {
                    timeout = 10000;
                }
                if (HiPlayUtils.checkJsonEmpty(this.connJson) && (HiPlayUtils.isUpperVersion(this.connJson) || HiPlayUtils.isDiffAccount(this.connJson))) {
                    timeout = 20000;
                    HiPlayHelper.this.mActiveOptParams.setRetryTimes(1);
                }
                HwLog.d(HiPlayHelper.TAG, "enable timeout config " + timeout + "ms");
                HiPlayHelper.this.mDeviceOptHandler.postDelayed(HiPlayHelper.this.mEnableTimeout, timeout);
            }
            if (OptMode.DISABLE.equals(getMode())) {
                HwLog.i(HiPlayHelper.TAG, String.format(Locale.ENGLISH, "disable virtual device, device id: %s, capability : %s", HiPlayHelper.transSensiInfo(this.device.getDeviceId()), Utils.getEnumSetString(this.capability)));
                HiPlayHelper.this.mDisableOptFinish = false;
                DeviceOperationParams params2 = new DeviceOperationParams(this.device.getDeviceId(), this.capability, BuildConfig.FLAVOR, OptMode.DISABLE);
                params2.init(this.device);
                HiPlayHelper.this.mActiveOptParams = params2;
                HiPlayHelper.this.mDeviceOptHandler.sendMessage(HiPlayHelper.this.mDeviceOptHandler.obtainMessage(3, params2));
                HiPlayHelper.this.mDeviceOptHandler.postDelayed(HiPlayHelper.this.mDisableTimeout, 5000);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class WorkQueue {
        private boolean hasRunningTask = false;
        private boolean needExit = false;
        private final LinkedList queue = new LinkedList();
        private final PoolWorker[] threads;

        public WorkQueue(int nThreads) {
            this.threads = new PoolWorker[nThreads];
            for (int i = 0; i < nThreads; i++) {
                this.threads[i] = new PoolWorker();
                this.threads[i].start();
            }
        }

        public void execute(Runnable task) {
            synchronized (this.queue) {
                HwLog.i(HiPlayHelper.TAG, "before add task num: " + this.queue.size());
                this.queue.addLast(task);
                HwLog.i(HiPlayHelper.TAG, "after add task num: " + this.queue.size());
                this.queue.notifyAll();
                HwLog.i(HiPlayHelper.TAG, "after add task, notify all");
            }
        }

        public void executeRetryTask(Runnable task) {
            synchronized (this.queue) {
                HwLog.i(HiPlayHelper.TAG, "before add retry task num: " + this.queue.size());
                this.queue.addFirst(task);
                HwLog.i(HiPlayHelper.TAG, "after add retry task num: " + this.queue.size());
                this.queue.notifyAll();
                HwLog.i(HiPlayHelper.TAG, "after add retry task, notify all");
            }
        }

        public void onTaskFinish() {
            synchronized (this.queue) {
                HwLog.i(HiPlayHelper.TAG, "running task finish, task num: " + this.queue.size());
                this.hasRunningTask = false;
                this.queue.notifyAll();
                HwLog.i(HiPlayHelper.TAG, "after finish task, notify all");
            }
        }

        public void onTaskFinishOnly() {
            synchronized (this.queue) {
                HwLog.i(HiPlayHelper.TAG, "running task finish, task num: " + this.queue.size() + ", set running task false");
                this.hasRunningTask = false;
            }
        }

        public void stop() {
            synchronized (this.queue) {
                this.needExit = true;
                this.queue.notifyAll();
            }
        }

        private class PoolWorker extends Thread {
            private PoolWorker() {
            }

            /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
                r1 = (java.lang.Runnable) r4.this$0.queue.removeFirst();
                r4.this$0.hasRunningTask = true;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x003c, code lost:
                if (r4.this$0.needExit != false) goto L_0x0042;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:12:0x003e, code lost:
                r1.run();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
                com.huawei.dmsdpsdk2.HwLog.i(com.huawei.dmsdpsdk2.hiplay.HiPlayHelper.TAG, "Receive exit cmd, drop task");
             */
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                while (!WorkQueue.this.needExit) {
                    synchronized (WorkQueue.this.queue) {
                        while (true) {
                            if (WorkQueue.this.queue.isEmpty() || WorkQueue.this.hasRunningTask) {
                                try {
                                    WorkQueue.this.queue.wait();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("queue is empty: ");
                                    sb.append(WorkQueue.this.queue.isEmpty() ? "true" : "false");
                                    sb.append(", hasRunningTask: ");
                                    sb.append(WorkQueue.this.hasRunningTask ? "true" : "false");
                                    HwLog.i(HiPlayHelper.TAG, sb.toString());
                                } catch (InterruptedException e) {
                                    HwLog.e(HiPlayHelper.TAG, "task queue wait exception");
                                }
                            } else {
                                try {
                                    break;
                                } catch (NoSuchElementException e2) {
                                    HwLog.e(HiPlayHelper.TAG, "Can not find waiting task");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class DeviceOperationHandler extends Handler {
        public DeviceOperationHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.obj instanceof DeviceOperationParams) {
                int i = msg.what;
                if (i == 1) {
                    synchronized (HiPlayHelper.HIPLAY_ADAPTER_LOCK) {
                        if (HiPlayHelper.this.mHiPlayAdapter != null) {
                            HiPlayHelper.this.mHiPlayAdapter.handleNewDevice((DeviceOperationParams) msg.obj);
                        }
                    }
                } else if (i == 2) {
                    synchronized (HiPlayHelper.HIPLAY_ADAPTER_LOCK) {
                        if (HiPlayHelper.this.mHiPlayAdapter != null) {
                            HiPlayHelper.this.mHiPlayAdapter.handleDeviceEnable((DeviceOperationParams) msg.obj);
                        }
                    }
                } else if (i == 3) {
                    synchronized (HiPlayHelper.HIPLAY_ADAPTER_LOCK) {
                        if (HiPlayHelper.this.mHiPlayAdapter != null) {
                            HiPlayHelper.this.mHiPlayAdapter.handleDeviceDisable((DeviceOperationParams) msg.obj);
                        }
                    }
                }
            } else {
                HwLog.e(HiPlayHelper.TAG, "not hiplay enable param object instance");
            }
        }
    }

    /* access modifiers changed from: private */
    public class DeviceServiceHandler extends Handler {
        private DeviceServiceHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (HiPlayHelper.this.mHiPlayListener == null) {
                HwLog.e(HiPlayHelper.TAG, "hiplayListener is null");
            } else if (!(msg.obj instanceof HiPlayDevice)) {
                HwLog.e(HiPlayHelper.TAG, "not HiPlayDevice instance");
            } else {
                HiPlayDevice hiPlayDevice = (HiPlayDevice) msg.obj;
                Capability cap = Utils.convertServiceTypeToCapability(msg.arg1);
                if (cap == null) {
                    HwLog.e(HiPlayHelper.TAG, "Unknown capability, service type: " + msg.arg1);
                    return;
                }
                int i = msg.what;
                if (i == 1) {
                    if (msg.arg2 == 204) {
                        onServiceEnableSuccess(hiPlayDevice, cap);
                    }
                    if (msg.arg2 == 205) {
                        onServiceDisableSuccess(hiPlayDevice, cap);
                    }
                } else if (i == 2) {
                    onServiceAbnormal(hiPlayDevice, cap);
                }
            }
        }

        private void onServiceEnableSuccess(HiPlayDevice hiPlayDevice, Capability cap) {
            HwLog.i(HiPlayHelper.TAG, String.format(Locale.ENGLISH, "hiplay enable success, device id: %s, capability: %s", HiPlayHelper.transSensiInfo(hiPlayDevice.getDeviceId()), cap.getProfileDesc()));
            HiPlayHelper.this.mHiPlayListener.onVirtualDeviceSuccess(hiPlayDevice, cap, 204);
            HiPlayHelper.this.reportHiPlayDeviceState(hiPlayDevice.getDeviceId(), cap, DeviceState.CONTIUITING);
            HiPlayHelper.this.clearEnableTimeoutTimer(true);
            if (HiPlayHelper.this.mWorkQueue != null) {
                HiPlayHelper.this.mWorkQueue.onTaskFinish();
            }
        }

        private void onServiceDisableSuccess(HiPlayDevice hiPlayDevice, Capability cap) {
            HwLog.i(HiPlayHelper.TAG, String.format(Locale.ENGLISH, "hiplay disable success, device id: %s, capability: %s", HiPlayHelper.transSensiInfo(hiPlayDevice.getDeviceId()), cap.getProfileDesc()));
            HiPlayHelper.this.mHiPlayListener.onVirtualDeviceSuccess(hiPlayDevice, cap, 205);
            HiPlayHelper.this.reportHiPlayDeviceState(hiPlayDevice.getDeviceId(), cap, DeviceState.NO_CONTINUTING);
            synchronized (HiPlayHelper.HIPLAY_ADAPTER_LOCK) {
                if (HiPlayHelper.this.mHiPlayAdapter != null) {
                    HiPlayHelper.this.mHiPlayAdapter.getAllHiPlayDevices().remove(hiPlayDevice.getDeviceId());
                    HiPlayHelper.this.mHiPlayAdapter.getAllDMSDPDevices().remove(hiPlayDevice.getDeviceId());
                }
            }
            HiPlayHelper.this.clearDisableTimeoutTimer(true);
            if (HiPlayHelper.this.mWorkQueue != null) {
                HiPlayHelper.this.mWorkQueue.onTaskFinish();
            }
        }

        private void onServiceAbnormal(HiPlayDevice hiPlayDevice, Capability cap) {
            HwLog.i(HiPlayHelper.TAG, String.format(Locale.ENGLISH, "hiplay enable or disable failed, device id: %s, capability: %s", HiPlayHelper.transSensiInfo(hiPlayDevice.getDeviceId()), cap.getProfileDesc()));
            HiPlayHelper.this.mHiPlayListener.onVirtualDeviceFailed(hiPlayDevice, cap, DMSDPConfig.CONNECT_DEVICE_FAILED);
            HiPlayHelper.this.reportHiPlayDeviceState(hiPlayDevice.getDeviceId(), cap, DeviceState.NO_CONTINUTING);
            if (HiPlayHelper.this.mActiveOptParams.getMode().equals(OptMode.ENABLE)) {
                HiPlayHelper.this.clearEnableTimeoutTimer(true);
            } else {
                HiPlayHelper.this.clearDisableTimeoutTimer(true);
            }
            if (HiPlayHelper.this.mWorkQueue != null) {
                HiPlayHelper.this.mWorkQueue.onTaskFinish();
            }
        }
    }

    public DeviceServiceHandler getHiPlayRetHandler() {
        return this.mHiPlayRetHandler;
    }

    public DeviceOperationHandler getDeviceOptHandler() {
        return this.mDeviceOptHandler;
    }

    public HiPlayListener getHiPlayListener() {
        return this.mHiPlayListener;
    }

    public synchronized void addTask(HiPlayTask task) {
        if (this.mWorkQueue == null) {
            this.mWorkQueue = new WorkQueue(1);
        }
        HwLog.i(TAG, "add one task");
        this.mWorkQueue.execute(task);
    }

    public synchronized void addRetryTask(HiPlayTask task) {
        if (this.mWorkQueue == null) {
            this.mWorkQueue = new WorkQueue(1);
        }
        HwLog.i(TAG, "add one task");
        this.mWorkQueue.executeRetryTask(task);
    }

    public void registerHiPlayAdapter(HiPlayAdapter adapter) {
        synchronized (HIPLAY_ADAPTER_LOCK) {
            if (!(this.mHiPlayAdapter == null || this.mHiPlayAdapter == adapter)) {
                HwLog.w(TAG, "The HiPlayAdapter has not unregister, may be error");
            }
            HwLog.i(TAG, "register hiplay adapter, addr: " + adapter);
            this.mHiPlayAdapter = adapter;
        }
    }

    public void unregisterHiPlayAdapter(HiPlayAdapter adapter) {
        synchronized (HIPLAY_ADAPTER_LOCK) {
            if (!(this.mHiPlayAdapter == null || this.mHiPlayAdapter == adapter)) {
                HwLog.w(TAG, "The HiPlayAdapter cached not match the input one, may be unregister error");
            }
            HwLog.i(TAG, "Unregister HiPlayAdapter");
            this.mHiPlayAdapter = null;
        }
    }

    public void registerHiPlayListener(HiPlayListener listener) {
        HiPlayListener hiPlayListener = this.mHiPlayListener;
        if (!(hiPlayListener == null || hiPlayListener == listener)) {
            HwLog.w(TAG, "The HiPlayListener has not unregister, may be error");
        }
        this.mHiPlayListener = listener;
    }

    public void unRegisterHiPlayListener() {
        HwLog.i(TAG, "Unregister HiPlayListener");
        this.mHiPlayListener = null;
    }

    public void startDiscover() {
        this.mDiscThread = new DiscoveryTask();
        this.mDiscThread.start();
    }

    public DeviceOperationParams getActiveOptParam() {
        return this.mActiveOptParams;
    }

    public static Capability convertSericeTypeToCapability(int serviceType) {
        if (serviceType == 2) {
            return Capability.MIC;
        }
        if (serviceType == 4) {
            return Capability.SPEAKER;
        }
        if (serviceType == 8) {
            return Capability.DISPLAY;
        }
        if (serviceType == 2048) {
            return Capability.SENSOR;
        }
        if (serviceType == 4096) {
            return Capability.VIBRATE;
        }
        if (serviceType != 8192) {
            return null;
        }
        return Capability.NOTIFICATION;
    }
}
