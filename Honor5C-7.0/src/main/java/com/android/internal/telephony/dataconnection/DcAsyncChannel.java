package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.ProxyInfo;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
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
    private static String[] sCmdToString;
    private DataConnection mDc;
    private long mDcThreadId;
    private String mLogTag;

    public enum LinkPropertyChangeAction {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcAsyncChannel.LinkPropertyChangeAction.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcAsyncChannel.LinkPropertyChangeAction.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcAsyncChannel.LinkPropertyChangeAction.<clinit>():void");
        }

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcAsyncChannel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcAsyncChannel.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcAsyncChannel.<clinit>():void");
    }

    protected static String cmdToString(int cmd) {
        cmd -= REQ_IS_INACTIVE;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return AsyncChannel.cmdToString(cmd + REQ_IS_INACTIVE);
        }
        return sCmdToString[cmd];
    }

    public DcAsyncChannel(DataConnection dc, String logTag) {
        this.mDc = dc;
        this.mDcThreadId = this.mDc.getHandler().getLooper().getThread().getId();
        this.mLogTag = logTag;
    }

    public void reqIsInactive() {
        sendMessage(REQ_IS_INACTIVE);
    }

    public boolean rspIsInactive(Message response) {
        return response.arg1 == 1 ? true : DBG;
    }

    public boolean isInactiveSync() {
        if (!isCallerOnDifferentThread()) {
            return this.mDc.getIsInactive();
        }
        Message response = sendMessageSynchronously(REQ_IS_INACTIVE);
        if (response != null && response.what == RSP_IS_INACTIVE) {
            return rspIsInactive(response);
        }
        log("rspIsInactive error response=" + response);
        return DBG;
    }

    public boolean rspCheckApnContext(Message response) {
        return response.arg1 == 1 ? true : DBG;
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
        return DBG;
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
            return this.mDc.getCopyNetworkCapabilities();
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

    public void bringUp(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration) {
        sendMessage(SmsEnvelope.TELESERVICE_MWI, new ConnectionParams(apnContext, profileId, rilRadioTechnology, onCompletedMsg, connectionGeneration));
    }

    public void tearDown(ApnContext apnContext, String reason, Message onCompletedMsg) {
        sendMessage(262148, new DisconnectParams(apnContext, reason, onCompletedMsg));
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
        return this.mDcThreadId != Thread.currentThread().getId() ? true : DBG;
    }

    private void log(String s) {
        Rlog.d(this.mLogTag, "DataConnectionAc " + s);
    }

    public String[] getPcscfAddr() {
        return this.mDc.mPcscfAddr;
    }
}
