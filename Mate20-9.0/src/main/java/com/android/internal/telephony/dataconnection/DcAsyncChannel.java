package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.ProxyInfo;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.dataconnection.DataConnection;
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
    DataConnection.ConnectionParams mLastConnectionParams;
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
        int cmd2 = cmd - 266240;
        if (cmd2 < 0 || cmd2 >= sCmdToString.length) {
            return AsyncChannel.cmdToString(266240 + cmd2);
        }
        return sCmdToString[cmd2];
    }

    public DcAsyncChannel(DataConnection dc, String logTag) {
        this.mDc = dc;
        this.mLogTag = logTag;
    }

    public void reqIsInactive() {
        sendMessage(266240);
    }

    public boolean rspIsInactive(Message response) {
        boolean retVal = true;
        if (response.arg1 != 1) {
            retVal = false;
        }
        return retVal;
    }

    public boolean isInactiveSync() {
        boolean value;
        if (!isCallerOnDifferentThread()) {
            return this.mDc.isInactive();
        }
        Message response = sendMessageSynchronously(266240);
        if (response == null || response.what != 266241) {
            log("rspIsInactive error response=" + response);
            value = false;
        } else {
            value = rspIsInactive(response);
        }
        return value;
    }

    public boolean rspCheckApnContext(Message response) {
        boolean retVal = true;
        if (response.arg1 != 1) {
            retVal = false;
        }
        return retVal;
    }

    public boolean checkApnContextSync(ApnContext apnContext) {
        boolean value;
        if (!isCallerOnDifferentThread()) {
            return this.mDc.checkApnContext(apnContext);
        }
        Message response = sendMessageSynchronously(REQ_CHECK_APNCONTEXT, apnContext);
        if (response == null || response.what != 266255) {
            log("rspCheckApnContext error response=" + response);
            value = false;
        } else {
            value = rspCheckApnContext(response);
        }
        return value;
    }

    public void reqCid() {
        sendMessage(REQ_GET_CID);
    }

    public int rspCid(Message response) {
        return response.arg1;
    }

    public int getCidSync() {
        int value;
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getCid();
        }
        Message response = sendMessageSynchronously(REQ_GET_CID);
        if (response == null || response.what != 266243) {
            log("rspCid error response=" + response);
            value = -1;
        } else {
            value = rspCid(response);
        }
        return value;
    }

    public void reqApnSetting() {
        sendMessage(REQ_GET_APNSETTING);
    }

    public ApnSetting rspApnSetting(Message response) {
        return (ApnSetting) response.obj;
    }

    public ApnSetting getApnSettingSync() {
        ApnSetting value;
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getApnSetting();
        }
        Message response = sendMessageSynchronously(REQ_GET_APNSETTING);
        if (response == null || response.what != 266245) {
            log("getApnSetting error response=" + response);
            value = null;
        } else {
            value = rspApnSetting(response);
        }
        return value;
    }

    public void reqLinkProperties() {
        sendMessage(REQ_GET_LINK_PROPERTIES);
    }

    public LinkProperties rspLinkProperties(Message response) {
        return (LinkProperties) response.obj;
    }

    public LinkProperties getLinkPropertiesSync() {
        LinkProperties value;
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getCopyLinkProperties();
        }
        Message response = sendMessageSynchronously(REQ_GET_LINK_PROPERTIES);
        if (response == null || response.what != 266247) {
            log("getLinkProperties error response=" + response);
            value = null;
        } else {
            value = rspLinkProperties(response);
        }
        return value;
    }

    public void reqSetLinkPropertiesHttpProxy(ProxyInfo proxy) {
        sendMessage(REQ_SET_LINK_PROPERTIES_HTTP_PROXY, proxy);
    }

    public void setLinkPropertiesHttpProxySync(ProxyInfo proxy) {
        if (isCallerOnDifferentThread()) {
            Message response = sendMessageSynchronously(REQ_SET_LINK_PROPERTIES_HTTP_PROXY, proxy);
            if (response == null || response.what != 266249) {
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
        return (NetworkCapabilities) response.obj;
    }

    public NetworkCapabilities getNetworkCapabilitiesSync() {
        NetworkCapabilities value;
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getNetworkCapabilities();
        }
        Message response = sendMessageSynchronously(REQ_GET_NETWORK_CAPABILITIES);
        if (response == null || response.what != 266251) {
            value = null;
        } else {
            value = rspNetworkCapabilities(response);
        }
        return value;
    }

    public void reqReset() {
        sendMessage(REQ_RESET);
    }

    public void bringUp(ApnContext apnContext, int profileId, int rilRadioTechnology, boolean unmeteredUseOnly, Message onCompletedMsg, int connectionGeneration) {
        DataConnection.ConnectionParams connectionParams = new DataConnection.ConnectionParams(apnContext, profileId, rilRadioTechnology, unmeteredUseOnly, onCompletedMsg, connectionGeneration);
        this.mLastConnectionParams = connectionParams;
        sendMessage(InboundSmsTracker.DEST_PORT_FLAG_3GPP2, this.mLastConnectionParams);
    }

    public void tearDown(ApnContext apnContext, String reason, Message onCompletedMsg) {
        sendMessage(262148, new DataConnection.DisconnectParams(apnContext, reason, onCompletedMsg));
    }

    public void clearLink(ApnContext apnContext, String reason, Message onCompletedMsg) {
        log("clearLink: apnContext=" + apnContext + " reason=" + reason + " onCompletedMsg=" + onCompletedMsg);
        sendMessage(262168, new DataConnection.DisconnectParams(apnContext, reason, onCompletedMsg));
    }

    public void resumeLink(ApnContext apnContext, String reason, Message onCompletedMsg) {
        log("resumeLink: apnContext=" + apnContext + " reason=" + reason + " onCompletedMsg=" + onCompletedMsg);
        sendMessage(262169, new DataConnection.DisconnectParams(apnContext, reason, onCompletedMsg));
    }

    public void tearDownAll(String reason, Message onCompletedMsg) {
        sendMessage(262150, new DataConnection.DisconnectParams(null, reason, onCompletedMsg));
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
        String str = this.mLogTag;
        Rlog.d(str, "DataConnectionAc " + s);
    }

    public String[] getPcscfAddr() {
        return this.mDc.mPcscfAddr;
    }
}
