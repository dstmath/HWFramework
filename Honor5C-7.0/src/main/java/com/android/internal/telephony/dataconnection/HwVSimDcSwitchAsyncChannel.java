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
    private static String[] sCmdToString;
    private HwVSimDcSwitchStateMachine mDcSwitchState;

    public static class RequestInfo {
        boolean executed;
        final int priority;
        final NetworkRequest request;
        private final LocalLog requestLog;

        public RequestInfo(NetworkRequest request, int priority, LocalLog l) {
            this.request = request;
            this.priority = priority;
            this.requestLog = l;
            this.executed = false;
        }

        public void log(String str) {
            this.requestLog.log(str);
        }

        public LocalLog getLog() {
            return this.requestLog;
        }

        public String toString() {
            return "[ request=" + this.request + ", executed=" + this.executed + ", priority=" + this.priority + "]";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.HwVSimDcSwitchAsyncChannel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.HwVSimDcSwitchAsyncChannel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.HwVSimDcSwitchAsyncChannel.<clinit>():void");
    }

    protected static String cmdToString(int cmd) {
        cmd -= REQ_CONNECT;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return AsyncChannel.cmdToString(cmd + REQ_CONNECT);
        }
        return sCmdToString[cmd];
    }

    public HwVSimDcSwitchAsyncChannel(HwVSimDcSwitchStateMachine dcSwitchState, int id) {
        this.mDcSwitchState = dcSwitchState;
    }

    public int connect(RequestInfo apnRequest) {
        sendMessage(REQ_CONNECT, apnRequest);
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
