package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.ProxyInfo;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.dataconnection.DataConnection.ConnectionParams;
import com.android.internal.telephony.dataconnection.DataConnection.DisconnectParams;
import com.android.internal.util.AsyncChannel;

public class DcAsyncChannel extends AsyncChannel {
    public static final int BASE = 266240;
    private static final int CMD_TO_STRING_COUNT = 16;
    private static final boolean DBG = false;
    public static final int REQ_CHECK_APNCONTEXT = 266254;
    public static final int REQ_GET_APNSETTING = 266244;
    public static final int REQ_GET_CID = 266242;
    public static final int REQ_GET_LINK_PROPERTIES = 266246;
    public static final int REQ_GET_NETWORK_CAPABILITIES = 266250;
    public static final int REQ_IS_INACTIVE = 266240;
    public static final int REQ_RESET = 266252;
    public static final int REQ_SET_LINK_PROPERTIES_HTTP_PROXY = 266248;
    public static final int RSP_CHECK_APNCONTEXT = 266255;
    public static final int RSP_GET_APNSETTING = 266245;
    public static final int RSP_GET_CID = 266243;
    public static final int RSP_GET_LINK_PROPERTIES = 266247;
    public static final int RSP_GET_NETWORK_CAPABILITIES = 266251;
    public static final int RSP_IS_INACTIVE = 266241;
    public static final int RSP_RESET = 266253;
    public static final int RSP_SET_LINK_PROPERTIES_HTTP_PROXY = 266249;
    private static String[] sCmdToString = new String[16];
    private DataConnection mDc;
    private long mDcThreadId = this.mDc.getHandler().getLooper().getThread().getId();
    private String mLogTag;

    public enum LinkPropertyChangeAction {
        NONE,
        CHANGED,
        RESET;

        public static LinkPropertyChangeAction fromInt(int value) {
            if (value == NONE.ordinal()) {
                return NONE;
            }
            if (value == CHANGED.ordinal()) {
                return CHANGED;
            }
            if (value == RESET.ordinal()) {
                return RESET;
            }
            throw new RuntimeException("LinkPropertyChangeAction.fromInt: bad value=" + value);
        }
    }

    static {
        sCmdToString[0] = "REQ_IS_INACTIVE";
        sCmdToString[1] = "RSP_IS_INACTIVE";
        sCmdToString[2] = "REQ_GET_CID";
        sCmdToString[3] = "RSP_GET_CID";
        sCmdToString[4] = "REQ_GET_APNSETTING";
        sCmdToString[5] = "RSP_GET_APNSETTING";
        sCmdToString[6] = "REQ_GET_LINK_PROPERTIES";
        sCmdToString[7] = "RSP_GET_LINK_PROPERTIES";
        sCmdToString[8] = "REQ_SET_LINK_PROPERTIES_HTTP_PROXY";
        sCmdToString[9] = "RSP_SET_LINK_PROPERTIES_HTTP_PROXY";
        sCmdToString[10] = "REQ_GET_NETWORK_CAPABILITIES";
        sCmdToString[11] = "RSP_GET_NETWORK_CAPABILITIES";
        sCmdToString[12] = "REQ_RESET";
        sCmdToString[13] = "RSP_RESET";
        sCmdToString[14] = "REQ_CHECK_APNCONTEXT";
        sCmdToString[15] = "RSP_CHECK_APNCONTEXT";
    }

    protected static String cmdToString(int cmd) {
        cmd -= 266240;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return AsyncChannel.cmdToString(cmd + 266240);
        }
        return sCmdToString[cmd];
    }

    public DcAsyncChannel(DataConnection dc, String logTag) {
        this.mDc = dc;
        this.mLogTag = logTag;
    }

    public void reqIsInactive() {
        sendMessage(266240);
    }

    public boolean rspIsInactive(Message response) {
        return response.arg1 == 1;
    }

    public boolean isInactiveSync() {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getIsInactive();
        }
        Message response = sendMessageSynchronously(266240);
        if (response != null && response.what == RSP_IS_INACTIVE) {
            return rspIsInactive(response);
        }
        log("rspIsInactive error response=" + response);
        return false;
    }

    public boolean rspCheckApnContext(Message response) {
        return response.arg1 == 1;
    }

    public boolean checkApnContextSync(ApnContext apnContext) {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.checkApnContext(apnContext);
        }
        Message response = sendMessageSynchronously(REQ_CHECK_APNCONTEXT, apnContext);
        if (response != null && response.what == RSP_CHECK_APNCONTEXT) {
            return rspCheckApnContext(response);
        }
        log("rspCheckApnContext error response=" + response);
        return false;
    }

    public void reqCid() {
        sendMessage(REQ_GET_CID);
    }

    public int rspCid(Message response) {
        return response.arg1;
    }

    public int getCidSync() {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getCid();
        }
        Message response = sendMessageSynchronously(REQ_GET_CID);
        if (response != null && response.what == RSP_GET_CID) {
            return rspCid(response);
        }
        log("rspCid error response=" + response);
        return -1;
    }

    public void reqApnSetting() {
        sendMessage(REQ_GET_APNSETTING);
    }

    public ApnSetting rspApnSetting(Message response) {
        return response.obj;
    }

    public ApnSetting getApnSettingSync() {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getApnSetting();
        }
        Message response = sendMessageSynchronously(REQ_GET_APNSETTING);
        if (response != null && response.what == RSP_GET_APNSETTING) {
            return rspApnSetting(response);
        }
        log("getApnSetting error response=" + response);
        return null;
    }

    public void reqLinkProperties() {
        sendMessage(REQ_GET_LINK_PROPERTIES);
    }

    public LinkProperties rspLinkProperties(Message response) {
        return response.obj;
    }

    public LinkProperties getLinkPropertiesSync() {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getCopyLinkProperties();
        }
        Message response = sendMessageSynchronously(REQ_GET_LINK_PROPERTIES);
        if (response != null && response.what == RSP_GET_LINK_PROPERTIES) {
            return rspLinkProperties(response);
        }
        log("getLinkProperties error response=" + response);
        return null;
    }

    public void reqSetLinkPropertiesHttpProxy(ProxyInfo proxy) {
        sendMessage(REQ_SET_LINK_PROPERTIES_HTTP_PROXY, proxy);
    }

    public void setLinkPropertiesHttpProxySync(ProxyInfo proxy) {
        if (isCallerOnDifferentThread()) {
            Message response = sendMessageSynchronously(REQ_SET_LINK_PROPERTIES_HTTP_PROXY, proxy);
            if (response == null || response.what != RSP_SET_LINK_PROPERTIES_HTTP_PROXY) {
                log("setLinkPropertiesHttpPoxy error response=" + response);
                return;
            }
            return;
        }
        this.mDc.setLinkPropertiesHttpProxy(proxy);
    }

    public void reqNetworkCapabilities() {
        sendMessage(REQ_GET_NETWORK_CAPABILITIES);
    }

    public NetworkCapabilities rspNetworkCapabilities(Message response) {
        return response.obj;
    }

    public NetworkCapabilities getNetworkCapabilitiesSync() {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getNetworkCapabilities();
        }
        Message response = sendMessageSynchronously(REQ_GET_NETWORK_CAPABILITIES);
        if (response == null || response.what != RSP_GET_NETWORK_CAPABILITIES) {
            return null;
        }
        return rspNetworkCapabilities(response);
    }

    public void reqReset() {
        sendMessage(REQ_RESET);
    }

    public void bringUp(ApnContext apnContext, int profileId, int rilRadioTechnology, boolean unmeteredUseOnly, Message onCompletedMsg, int connectionGeneration) {
        sendMessage(InboundSmsTracker.DEST_PORT_FLAG_3GPP2, new ConnectionParams(apnContext, profileId, rilRadioTechnology, unmeteredUseOnly, onCompletedMsg, connectionGeneration));
    }

    public void tearDown(ApnContext apnContext, String reason, Message onCompletedMsg) {
        DisconnectParams disconnectParams = new DisconnectParams(apnContext, reason, onCompletedMsg);
        if (this.mDc != null) {
            this.mDc.toDisconnectParams = disconnectParams;
        }
        sendMessage(262148, disconnectParams);
    }

    public void clearLink(ApnContext apnContext, String reason, Message onCompletedMsg) {
        log("clearLink: apnContext=" + apnContext + " reason=" + reason + " onCompletedMsg=" + onCompletedMsg);
        sendMessage(262161, new DisconnectParams(apnContext, reason, onCompletedMsg));
    }

    public void resumeLink(ApnContext apnContext, String reason, Message onCompletedMsg) {
        log("resumeLink: apnContext=" + apnContext + " reason=" + reason + " onCompletedMsg=" + onCompletedMsg);
        sendMessage(262162, new DisconnectParams(apnContext, reason, onCompletedMsg));
    }

    public void tearDownAll(String reason, Message onCompletedMsg) {
        sendMessage(262150, new DisconnectParams(null, reason, onCompletedMsg));
    }

    public int getDataConnectionIdSync() {
        return this.mDc.getDataConnectionId();
    }

    public String toString() {
        return this.mDc.getName();
    }

    private boolean isCallerOnDifferentThread() {
        return this.mDcThreadId != Thread.currentThread().getId();
    }

    private void log(String s) {
        Rlog.d(this.mLogTag, "DataConnectionAc " + s);
    }

    public String[] getPcscfAddr() {
        return this.mDc.mPcscfAddr;
    }
}
