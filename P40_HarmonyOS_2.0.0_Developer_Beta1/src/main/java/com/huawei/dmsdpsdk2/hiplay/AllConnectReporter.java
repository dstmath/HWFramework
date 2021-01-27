package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.dmsdpsdk2.HwLog;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class AllConnectReporter {
    public static final String CONNECT_SOURCE_HIPLAY = "HiPlay";
    public static final int DEVICE_STATE_CONNECTED = 2;
    public static final int DEVICE_STATE_CONNECTING = 1;
    public static final int DEVICE_STATE_DIS_CONNECTING = 3;
    public static final int DEVICE_STATE_IDLE = 0;
    private static final String TAG = "AllConnectReporter";
    private HiPlayDevice mHiPlayDevice;
    private String mReportContent;

    private AllConnectReporter() {
        this.mReportContent = null;
    }

    public Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private static final String DEVICE_TYPE_HW_TV = "09C";
        private static final String DEVICE_TYPE_SOUND_BOX = "00A";
        private static final String KEY_CONNECT_SOURCE = "connectSource";
        private static final String KEY_CONNECT_STATE = "processState";
        private static final String KEY_CURRENT_TIME = "eventTime";
        private static final String KEY_DEVICE_ID = "deviceId";
        private static final String KEY_DEVICE_TYPE = "deviceType";
        private static final String KEY_ERROR_CODE = "errorCode";
        private static final int MSG_AUTH_FAILED = 7006;
        private static final int MSG_CANCEL_CONNECTION_FAILED = 7101;
        private static final int MSG_DEVICE_CONNECTION_FAILED = 7004;
        private static final int MSG_IN_AIRPLANE_MODE = 7011;
        private static final int MSG_IN_DFS_CHANNEL_MODE = 7010;
        private static final int MSG_IN_P2P_CONNECTED_MODE = 7012;
        private static final int MSG_IN_WIFI_HOTSPOT_ENABLE_MODE = 7203;
        private static final int MSG_NOTICE_AP_ENABLE = 7301;
        private static final int MSG_NOT_SUPPORT_PROJECTION_NOW = 7303;
        private static final int MSG_OPEN_WIFI_FAILED_IN_AIRPLANE_MODE = 7204;
        private static final int MSG_OPERATION_FREQUENTLY = 7005;
        private static final int MSG_PEER_BLUETOOTH_ERR = 7008;
        private static final int MSG_PEER_DEVICE_BUSY = 7002;
        private static final int MSG_PEER_DEVICE_CANCEL_CONNECTION = 7202;
        private static final int MSG_PEER_DEVICE_MUTED = 7201;
        private static final int MSG_PEER_DEVICE_NO_RESPONSE = 7003;
        private static final int MSG_PEER_DEVICE_REJECT = 7001;
        private static final int MSG_PEER_NETWORK_ERR = 7007;
        private static final int MSG_REQUIRE_UNLOCK_DEVICE = 7009;
        private static final int MSG_STOP_PROJECTION_AND_RETRY = 7302;
        private static final int MSG_SUCCESS = 0;
        private static final int MSG_WITHOUT_RELATIONSHIP = 7013;
        private HiPlayDevice mHiPlayDevice;
        private JSONObject mObject = new JSONObject();

        public AllConnectReporter build() {
            AllConnectReporter reporter = new AllConnectReporter();
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                reporter.mReportContent = jSONObject.toString();
            }
            reporter.mHiPlayDevice = this.mHiPlayDevice;
            return reporter;
        }

        public Builder setDeviceId(String deviceId) throws JSONException {
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                jSONObject.put(KEY_DEVICE_ID, deviceId);
            }
            return this;
        }

        public Builder setProcessState(int state) throws JSONException {
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                jSONObject.put(KEY_CONNECT_STATE, state);
            }
            return this;
        }

        public Builder setErrorCode(int errorCode) throws JSONException {
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                jSONObject.put(KEY_ERROR_CODE, errorCode);
            }
            return this;
        }

        public Builder setDeviceType(String deviceType) throws JSONException {
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                jSONObject.put(KEY_DEVICE_TYPE, deviceType);
            }
            return this;
        }

        public Builder setConnectSource(String connectSource) throws JSONException {
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                jSONObject.put(KEY_CONNECT_SOURCE, connectSource);
            }
            return this;
        }

        public Builder setCurrentTime(long time) throws JSONException {
            JSONObject jSONObject = this.mObject;
            if (jSONObject != null) {
                jSONObject.put(KEY_CURRENT_TIME, time);
            }
            return this;
        }

        public Builder setHiPlayDevice(HiPlayDevice hiplayDevice) throws JSONException {
            this.mHiPlayDevice = hiplayDevice;
            setDeviceId(hiplayDevice.getDeviceId());
            setDeviceType(covertDeviceType(hiplayDevice.getDeviceType()));
            return this;
        }

        public Builder setHiPlayDeviceState(String state) throws JSONException {
            return setProcessState(covertProcessState(state));
        }

        private int covertProcessState(String state) {
            if (DeviceState.CONTIUITING.equals(state) || DeviceState.STOP_CONTINUTING_FAILED.equals(state)) {
                return 2;
            }
            return 0;
        }

        private String covertDeviceType(int type) {
            if (type == 3) {
                return DEVICE_TYPE_HW_TV;
            }
            if (type == 6 || type == 10) {
                return "00A";
            }
            return null;
        }
    }

    public static void doReport(HiPlayAdapter hiPlayAdapter, AllConnectReporter reporter) {
        String str;
        if (hiPlayAdapter == null) {
            HwLog.e(TAG, "hiPlayAdapter is invalid");
        } else if (reporter == null || (str = reporter.mReportContent) == null) {
            HwLog.e(TAG, "reporter or reportContent is null");
        } else {
            hiPlayAdapter.sendData(reporter.mHiPlayDevice, 39, str.getBytes(StandardCharsets.UTF_8));
        }
    }
}
