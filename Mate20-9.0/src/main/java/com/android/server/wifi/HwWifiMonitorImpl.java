package com.android.server.wifi;

import android.util.Log;
import com.android.internal.util.StateMachine;

public class HwWifiMonitorImpl implements HwWifiMonitor {
    private static final String HEART_ACK_STR = "HEART-BEAT-ACK";
    public static final int HEART_BEAT_ACK_EVENT = 147506;
    private static final String TAG = "HwWifiMonitorImpl";
    public static final int WAPI_AUTHENTICATION_FAILURE_EVENT = 147474;
    private static final String WAPI_AUTHENTICATION_FAILURE_QCOM_STR = "Unicast Handshake failed -pre-shared key may be incorrect";
    private static final String WAPI_AUTHENTICATION_FAILURE_STR = "authentication failed";
    public static final int WAPI_CERTIFICATION_FAILURE_EVENT = 147475;
    private static final String WAPI_CERTIFICATION_FAILURE_STR = "certificate initialization failed";
    private static final String WAPI_EVENT_PREFIX_STR = "WAPI:";
    private static HwWifiMonitor mInstance = new HwWifiMonitorImpl();

    public static HwWifiMonitor getDefault() {
        return mInstance;
    }

    public void parsingWAPIEvent(String eventStr, StateMachine mStateMachine) {
        if (!eventStr.startsWith(WAPI_EVENT_PREFIX_STR)) {
            return;
        }
        if (eventStr.indexOf(WAPI_CERTIFICATION_FAILURE_STR) > 0) {
            Log.v(TAG, "Got WAPI event [" + eventStr + "]");
            mStateMachine.sendMessage(147475);
        } else if (eventStr.indexOf(WAPI_AUTHENTICATION_FAILURE_STR) > 0 || eventStr.indexOf(WAPI_AUTHENTICATION_FAILURE_QCOM_STR) > 0) {
            Log.v(TAG, "Got WAPI event [" + eventStr + "]");
            mStateMachine.sendMessage(147474);
        }
    }

    public void parsingSupplicantHeartBeatEvent(String eventStr, StateMachine mStateMachine) {
        if (eventStr.startsWith(HEART_ACK_STR)) {
            Log.v(TAG, "parsingSupplicantHeartBeatEvent Got SupplicantHeartBeat event [" + eventStr + "]");
            mStateMachine.sendMessage(147506);
        }
    }
}
