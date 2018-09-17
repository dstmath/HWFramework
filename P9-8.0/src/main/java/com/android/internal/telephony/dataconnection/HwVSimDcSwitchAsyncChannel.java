package com.android.internal.telephony.dataconnection;

import android.net.NetworkRequest;
import android.os.Message;
import android.util.LocalLog;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.util.AsyncChannel;

public class HwVSimDcSwitchAsyncChannel extends AsyncChannel {
    private static final int BASE = 278528;
    private static final int CMD_TO_STRING_COUNT = 11;
    static final int EVENT_DATA_ATTACHED = 278535;
    static final int EVENT_DATA_DETACHED = 278536;
    static final int EVENT_EMERGENCY_CALL_ENDED = 278538;
    static final int EVENT_EMERGENCY_CALL_STARTED = 278537;
    private static final String LOG_TAG = "VSimDcSwitchChannel";
    static final int REQ_CONNECT = 278528;
    static final int REQ_DISCONNECT_ALL = 278530;
    static final int REQ_IS_IDLE_OR_DETACHING_STATE = 278533;
    static final int REQ_IS_IDLE_STATE = 278531;
    static final int REQ_RETRY_CONNECT = 278529;
    static final int RSP_IS_IDLE_OR_DETACHING_STATE = 278534;
    static final int RSP_IS_IDLE_STATE = 278532;
    private static String[] sCmdToString = new String[11];
    private HwVSimDcSwitchStateMachine mDcSwitchState;

    public static class RequestInfo {
        boolean executed = false;
        final int priority;
        final NetworkRequest request;
        private final LocalLog requestLog;

        public RequestInfo(NetworkRequest request, int priority, LocalLog l) {
            this.request = request;
            this.priority = priority;
            this.requestLog = l;
        }

        public void log(String str) {
            this.requestLog.log(str);
        }

        public LocalLog getLog() {
            return this.requestLog;
        }

        public NetworkRequest getNetworkRequest() {
            return this.request;
        }

        public String toString() {
            return "[ request=" + this.request + ", executed=" + this.executed + ", priority=" + this.priority + "]";
        }
    }

    static {
        sCmdToString[0] = "REQ_CONNECT";
        sCmdToString[1] = "REQ_RETRY_CONNECT";
        sCmdToString[2] = "REQ_DISCONNECT_ALL";
        sCmdToString[3] = "REQ_IS_IDLE_STATE";
        sCmdToString[4] = "RSP_IS_IDLE_STATE";
        sCmdToString[5] = "REQ_IS_IDLE_OR_DETACHING_STATE";
        sCmdToString[6] = "RSP_IS_IDLE_OR_DETACHING_STATE";
        sCmdToString[7] = "EVENT_DATA_ATTACHED";
        sCmdToString[8] = "EVENT_DATA_DETACHED";
        sCmdToString[9] = "EVENT_EMERGENCY_CALL_STARTED";
        sCmdToString[10] = "EVENT_EMERGENCY_CALL_ENDED";
    }

    protected static String cmdToString(int cmd) {
        cmd -= 278528;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return AsyncChannel.cmdToString(cmd + 278528);
        }
        return sCmdToString[cmd];
    }

    public HwVSimDcSwitchAsyncChannel(HwVSimDcSwitchStateMachine dcSwitchState, int id) {
        this.mDcSwitchState = dcSwitchState;
    }

    public int connect(RequestInfo apnRequest) {
        sendMessage(278528, apnRequest);
        return 1;
    }

    public void retryConnect() {
        sendMessage(REQ_RETRY_CONNECT);
    }

    public int disconnectAll() {
        sendMessage(REQ_DISCONNECT_ALL);
        return 1;
    }

    public void notifyDataAttached() {
        sendMessage(EVENT_DATA_ATTACHED);
    }

    public void notifyDataDetached() {
        sendMessage(EVENT_DATA_DETACHED);
    }

    public void notifyEmergencyCallToggled(int start) {
        if (start != 0) {
            sendMessage(EVENT_EMERGENCY_CALL_STARTED);
        } else {
            sendMessage(EVENT_EMERGENCY_CALL_ENDED);
        }
    }

    private boolean rspIsIdle(Message response) {
        boolean retVal = response.arg1 == 1;
        logd("rspIsIdle=" + retVal);
        return retVal;
    }

    public boolean isIdleSync() {
        Message response = sendMessageSynchronously(REQ_IS_IDLE_STATE);
        if (response != null && response.what == RSP_IS_IDLE_STATE) {
            return rspIsIdle(response);
        }
        logd("rspIsIndle error response=" + response);
        return false;
    }

    public void reqIsIdleOrDetaching() {
        sendMessage(REQ_IS_IDLE_OR_DETACHING_STATE);
        logd("reqIsIdleOrDetaching");
    }

    public boolean rspIsIdleOrDetaching(Message response) {
        boolean retVal = response.arg1 == 1;
        logd("rspIsIdleOrDetaching=" + retVal);
        return retVal;
    }

    public boolean isIdleOrDetachingSync() {
        Message response = sendMessageSynchronously(REQ_IS_IDLE_OR_DETACHING_STATE);
        if (response != null && response.what == RSP_IS_IDLE_OR_DETACHING_STATE) {
            return rspIsIdleOrDetaching(response);
        }
        logd("rspIsIdleOrDetaching error response=" + response);
        return false;
    }

    public String toString() {
        return this.mDcSwitchState.getName();
    }

    private void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }
}
