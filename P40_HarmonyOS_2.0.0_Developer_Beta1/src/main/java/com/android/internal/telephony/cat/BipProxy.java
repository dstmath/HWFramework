package com.android.internal.telephony.cat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.BearerDescription;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.InterfaceTransportLevel;
import com.android.internal.telephony.dataconnection.TransportManager;
import huawei.cust.HwCustUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BipProxy extends Handler {
    static final int CHANNEL_CLOSED = 2;
    static final int CHANNEL_IDENTIFIER_NOT_VALID = 3;
    static final int CHANNEL_STATUS_AVAILABLE = 32768;
    static final int CH_STATUS_LINK_DROP = 5;
    static final int CMD_QUAL_AUTO_RECONN = 2;
    static final int CMD_QUAL_BACKGROUND = 4;
    static final int CMD_QUAL_IMMEDIATE_LINK_ESTABLISH = 1;
    private static final int DEFAULT_DC_TIMEOUT = 180000;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT = 150000;
    private static final int DNS_SERVER_ADDRESS_REQUESTED = 8;
    static final int EVENT_DC_TIMEOUT = 100;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 99;
    private static final boolean IS_VZ = ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
    static final int MAX_CHANNEL_NUM = 7;
    static final int MAX_LEN_OF_CHANNEL_DATA = 237;
    static final int MAX_TCP_SERVER_CLIENT_CHANNEL_NUM = 15;
    static final int MSG_ID_DATA_STATE_CHANGED = 12;
    static final int MSG_ID_GET_DATA_STATE = 13;
    static final int MSG_ID_SETUP_DATA_CALL = 10;
    static final int MSG_ID_TEARDOWN_DATA_CALL = 11;
    static final int NO_CHANNEL_AVAILABLE = 1;
    static final int NO_SPECIFIC_CAUSE_CAN_BE_GIVEN = 0;
    static final int REQUESTED_BUFFER_SIZE_NOT_AVAILABLE = 4;
    static final int REQUESTED_SIM_ME_INTERFACE_TRANSPORT_LEVEL_NOT_AVAILABLE = 6;
    static final int SECURITY_ERROR = 5;
    static final int TCP_CHANNEL_BUFFER_SIZE = 16384;
    static final int UDP_CHANNEL_BUFFER_SIZE = 1500;
    private BipChannel[] mBipChannels = new BipChannel[7];
    private ChannelApnInfo[] mChannelApnInfo = new ChannelApnInfo[7];
    private Context mContext;
    private DefaultBearerStateReceiver mDefaultBearerStateReceiver;
    private HwCustBipProxy mHwCustBipProxy = null;
    boolean mImmediateLinkEstablish = true;
    protected boolean[] mIsWifiConnected = new boolean[7];
    private CatService mStkService = null;
    PowerManager.WakeLock mWakeLock;
    final int mWakeLockTimeout;
    CatCmdMessage openChCmdMsg;

    /* access modifiers changed from: package-private */
    public interface BipChannel {
        void close(CatCmdMessage catCmdMessage);

        int getStatus();

        void onSessionEnd();

        boolean open(CatCmdMessage catCmdMessage);

        boolean preProcessOpen(CatCmdMessage catCmdMessage);

        void receive(CatCmdMessage catCmdMessage);

        void send(CatCmdMessage catCmdMessage);

        void setStatus(int i);
    }

    public BipProxy(CatService stkService, CommandsInterface cmdIf, Context context) {
        this.mStkService = stkService;
        this.mContext = context;
        this.mDefaultBearerStateReceiver = new DefaultBearerStateReceiver(context);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "BipProxy");
        this.mWakeLock.setReferenceCounted(false);
        this.mWakeLockTimeout = DEFAULT_WAKE_LOCK_TIMEOUT;
        this.mHwCustBipProxy = (HwCustBipProxy) HwCustUtils.createObj(HwCustBipProxy.class, new Object[]{this.mContext});
    }

    public boolean canHandleNewChannel() {
        int i = 0;
        while (true) {
            BipChannel[] bipChannelArr = this.mBipChannels;
            if (i < bipChannelArr.length) {
                if (this.mChannelApnInfo[i] == null) {
                    bipChannelArr[i] = null;
                }
                if (this.mBipChannels[i] == null) {
                    CatLog.d(this, "channel index " + i + " found. new channel can be handled");
                    return true;
                }
                i++;
            } else {
                CatLog.d(this, "new channel can't be handled");
                return false;
            }
        }
    }

    private boolean isBipOverWlanAllowed() {
        return SystemProperties.getBoolean("ro.config.bip_over_wlan", false);
    }

    private boolean isDefaultBearerDescriptionType(CatCmdMessage cmdMsg) {
        if (cmdMsg == null || AppInterface.CommandType.OPEN_CHANNEL != cmdMsg.getCmdType()) {
            return false;
        }
        if (BearerDescription.BearerType.DEFAULT_BEARER == cmdMsg.getChannelSettings().bearerDescription.type) {
            return true;
        }
        return false;
    }

    private boolean isIPV6Address(CatCmdMessage cmdMsg) {
        CatCmdMessage.ChannelSettings newChannel;
        if (cmdMsg == null || AppInterface.CommandType.OPEN_CHANNEL != cmdMsg.getCmdType() || (newChannel = cmdMsg.getChannelSettings()) == null || newChannel.destinationAddress == null || 4 == newChannel.destinationAddress.length) {
            return false;
        }
        return true;
    }

    private boolean isDnsServerAddressRequested(CatCmdMessage cmdMsg) {
        if (cmdMsg != null && AppInterface.CommandType.OPEN_CHANNEL == cmdMsg.getCmdType() && 8 == (cmdMsg.getCommandQualifier() & 8)) {
            return true;
        }
        return false;
    }

    private void updateWifiAvailableFlag(CatCmdMessage cmdMsg) {
        if (cmdMsg == null) {
            CatLog.d(this, "updateWifiAvailableFlag, input param invalid!");
        } else if (!cmdMsg.getWifiConnectedFlag()) {
            CatLog.d(this, "updateWifiAvailableFlag, getWifiConnectedFlag is false, just return!");
        } else if (AppInterface.CommandType.OPEN_CHANNEL != cmdMsg.getCmdType()) {
            CatLog.d(this, "updateWifiAvailableFlag to false, cmdType is " + cmdMsg.getCmdType());
            cmdMsg.setWifiConnectedFlag(false);
        } else if (isDnsServerAddressRequested(cmdMsg)) {
            CatLog.d(this, "updateWifiAvailableFlag to false, DNS server address(es) requested!");
            cmdMsg.setWifiConnectedFlag(false);
        } else if (!isBipOverWlanAllowed()) {
            CatLog.d(this, "updateWifiAvailableFlag to false, isBipOverWlanAllowed is false");
            cmdMsg.setWifiConnectedFlag(false);
        } else if (!isDefaultBearerDescriptionType(cmdMsg)) {
            CatLog.d(this, "updateWifiAvailableFlag to false, not default_bearer!");
            cmdMsg.setWifiConnectedFlag(false);
        } else if (isIPV6Address(cmdMsg)) {
            CatLog.d(this, "updateWifiAvailableFlag to false, IPV6!");
            cmdMsg.setWifiConnectedFlag(false);
        }
    }

    public void handleBipCommand(CatCmdMessage cmdMsg) {
        if (cmdMsg == null) {
            CatLog.d(this, "handleBipCommand null cmdMsg");
            int i = 0;
            while (true) {
                BipChannel[] bipChannelArr = this.mBipChannels;
                if (i < bipChannelArr.length) {
                    if (bipChannelArr[i] != null) {
                        CatLog.d(this, "handleBipCommand handle channel " + i + " session end");
                        this.mBipChannels[i].onSessionEnd();
                    }
                    i++;
                } else {
                    return;
                }
            }
        } else {
            AppInterface.CommandType curCmdType = cmdMsg.getCmdType();
            CatLog.d(this, "handleBipCommand curCmdType: " + curCmdType + ", channelSettings: " + cmdMsg.getChannelSettings() + ", cmd_qual: " + cmdMsg.getCommandQualifier() + ", dataSettings: " + cmdMsg.getDataSettings());
            updateWifiAvailableFlag(cmdMsg);
            int i2 = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[curCmdType.ordinal()];
            if (i2 == 1) {
                CatCmdMessage.ChannelSettings channelSettings = cmdMsg.getChannelSettings();
                if (channelSettings != null) {
                    acquireWakeLock();
                    if (allChannelsClosed()) {
                        this.mDefaultBearerStateReceiver.startListening();
                    }
                    int i3 = 0;
                    while (true) {
                        BipChannel[] bipChannelArr2 = this.mBipChannels;
                        if (i3 >= bipChannelArr2.length) {
                            break;
                        } else if (bipChannelArr2[i3] == null) {
                            CatLog.d(this, "mBipChannels " + i3 + " is available");
                            channelSettings.channel = i3 + 1;
                            break;
                        } else {
                            i3++;
                        }
                    }
                    if (channelSettings.channel == 0) {
                        this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 1, null);
                        return;
                    }
                    int i4 = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol[channelSettings.protocol.ordinal()];
                    if (i4 != 1) {
                        if (i4 == 2 || i4 == 3) {
                            this.mBipChannels[channelSettings.channel - 1] = new TcpClientChannel();
                        } else if (i4 == 4 || i4 == 5) {
                            this.mBipChannels[channelSettings.channel - 1] = new UdpClientChannel();
                        } else {
                            CatLog.d(this, "invalid protocol found");
                            this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                            return;
                        }
                        if ((cmdMsg.getCommandQualifier() & 1) == 0) {
                            this.mImmediateLinkEstablish = false;
                            this.openChCmdMsg = cmdMsg;
                            this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                            return;
                        }
                        this.mImmediateLinkEstablish = true;
                        if (setupDataConnection(cmdMsg)) {
                            CatLog.d(this, "Continue processing open channel");
                            this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                            if (!this.mBipChannels[channelSettings.channel - 1].open(cmdMsg)) {
                                CatLog.d(this, "open channel failed");
                                cleanupBipChannel(channelSettings.channel);
                                return;
                            }
                            return;
                        }
                        CatLog.d(this, "handleBipCommand :setupDataConnection returned");
                        return;
                    }
                    this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                    return;
                }
            } else if (i2 == 2 || i2 == 3 || i2 == 4) {
                if (cmdMsg.getDataSettings() != null) {
                    try {
                        BipChannel curChannel = this.mBipChannels[cmdMsg.getDataSettings().channel - 1];
                        if (curChannel == null) {
                            this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                            CatLog.d(this, "handleBipCommand, There is not open channel");
                            return;
                        } else if (AppInterface.CommandType.SEND_DATA == curCmdType) {
                            curChannel.send(cmdMsg);
                            return;
                        } else if (AppInterface.CommandType.RECEIVE_DATA == curCmdType) {
                            curChannel.receive(cmdMsg);
                            return;
                        } else if (AppInterface.CommandType.CLOSE_CHANNEL == curCmdType) {
                            clearWakeLock();
                            curChannel.close(cmdMsg);
                            cleanupBipChannel(cmdMsg.getDataSettings().channel);
                            return;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                        CatLog.d(this, "handleBipCommand error");
                        return;
                    }
                }
            } else if (i2 == 5) {
                int[] status = new int[7];
                for (int i5 = 0; i5 < 7; i5++) {
                    BipChannel[] bipChannelArr3 = this.mBipChannels;
                    if (bipChannelArr3[i5] != null) {
                        status[i5] = bipChannelArr3[i5].getStatus();
                    } else {
                        status[i5] = 0;
                    }
                    CatLog.d(this, "get channel status = " + status[i5]);
                }
                this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.OK, false, 0, new ChannelStatusResponseData(status));
                return;
            }
            this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.cat.BipProxy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType = new int[AppInterface.CommandType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol = new int[InterfaceTransportLevel.TransportProtocol.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.OPEN_CHANNEL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_DATA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.RECEIVE_DATA.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.CLOSE_CHANNEL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.GET_CHANNEL_STATUS.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol[InterfaceTransportLevel.TransportProtocol.TCP_SERVER.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol[InterfaceTransportLevel.TransportProtocol.TCP_CLIENT_REMOTE.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol[InterfaceTransportLevel.TransportProtocol.TCP_CLIENT_LOCAL.ordinal()] = 3;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol[InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_REMOTE.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$InterfaceTransportLevel$TransportProtocol[InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_LOCAL.ordinal()] = 5;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    private boolean allChannelsClosed() {
        for (BipChannel channel : this.mBipChannels) {
            if (channel != null) {
                CatLog.d(this, "not all Channels Closed");
                return false;
            }
        }
        CatLog.d(this, "all Channels Closed");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanupBipChannel(int channel) {
        this.mBipChannels[channel - 1] = null;
        if (allChannelsClosed()) {
            this.mDefaultBearerStateReceiver.stopListening();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendChannelStatusEvent(int channelStatus) {
        byte[] additionalInfo = {-72, 2, 0, 0};
        additionalInfo[2] = (byte) ((channelStatus >> 8) & 255);
        additionalInfo[3] = (byte) (channelStatus & 255);
        CatLog.d(this, "sendChannelStatusEvent channelStatus = " + channelStatus);
        this.mStkService.onEventDownload(new CatEventMessage(EventCode.CHANNEL_STATUS.value(), additionalInfo, true));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDataAvailableEvent(int channelStatus, int dataAvailable) {
        byte[] additionalInfo = {-72, 2, 0, 0, -73, 1, 0};
        additionalInfo[2] = (byte) ((channelStatus >> 8) & 255);
        additionalInfo[3] = (byte) (channelStatus & 255);
        additionalInfo[6] = (byte) (dataAvailable & 255);
        CatLog.d(this, "sendDataAvailableEvent channelStatus = " + channelStatus + " dataAvailable = " + dataAvailable);
        this.mStkService.onEventDownload(new CatEventMessage(EventCode.DATA_AVAILABLE.value(), additionalInfo, true));
    }

    /* access modifiers changed from: private */
    public static class ConnectionSetupFailedException extends IOException {
        public ConnectionSetupFailedException(String message) {
            super(message);
            CatLog.d(this, "ConnectionSetupFailedException: " + message);
        }
    }

    private boolean checkExistingCsCallInNetworkClass2G() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        int networkType = tm.getNetworkType();
        int networkClass = TelephonyManager.getNetworkClass(networkType);
        if ((1 != networkClass && networkClass != 0) || tm.getCallState() == 0) {
            return false;
        }
        CatLog.d(this, "Bearer not setup, busy on voice call, networkClass = " + networkClass + " networkType = " + networkType);
        return true;
    }

    private String getLguPlusOtaApn() {
        return SystemProperties.get("ro.config.lgu_plus_ota_apn", "ota.lguplus.co.kr");
    }

    private boolean isLguPlusOtaEnable() {
        return SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false);
    }

    private String formatDefaultApn(CatCmdMessage cmdMsg) {
        CatCmdMessage.ChannelSettings newChannel = cmdMsg.getChannelSettings();
        CatLog.d(this, "formatDefaultApn, mStkService.OTA_TYPE:" + this.mStkService.OTA_TYPE);
        if (!isLguPlusOtaEnable()) {
            return TransportManager.IWLAN_OPERATION_MODE_DEFAULT;
        }
        if (newChannel.networkAccessName == null) {
            CatLog.d(this, "formatDefaultApn, newChannel.networkAccessName is null");
            newChannel.networkAccessName = getLguPlusOtaApn();
        }
        if (newChannel.userLogin == null) {
            newChannel.userLogin = PhoneConfigurationManager.SSSS;
        }
        if (newChannel.userPassword == null) {
            newChannel.userPassword = PhoneConfigurationManager.SSSS;
        }
        String apnString = "bipapn, " + newChannel.networkAccessName + ", ," + String.valueOf(newChannel.port) + ", " + newChannel.userLogin + ", " + newChannel.userPassword + ", , , , , , ,3 , " + this.mChannelApnInfo[newChannel.channel - 1].type;
        CatLog.d(this, "formatDefaultApn, apnString:" + apnString);
        return apnString;
    }

    private boolean setupDefaultDataConnection(CatCmdMessage cmdMsg) throws ConnectionSetupFailedException {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        CatCmdMessage.ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (checkExistingCsCallInNetworkClass2G()) {
            this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 2, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
            throw new ConnectionSetupFailedException("Busy on voice call");
        } else if (this.mChannelApnInfo[newChannel.channel - 1] == null || this.mChannelApnInfo[newChannel.channel - 1].networkCallback == null) {
            this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
            this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
            String apnString = formatDefaultApn(cmdMsg);
            CatLog.d(this, "IS_vz = " + IS_VZ);
            if (true == IS_VZ) {
                SystemProperties.set("gsm.bip.apn", (String) null);
            } else {
                SystemProperties.set("gsm.bip.apn", apnString);
            }
            CatLog.d(this, "setupDefaultDataConnection");
            NetworkRequest request = new NetworkRequest.Builder().addTransportType(0).addCapability(getBipCapability(this.mChannelApnInfo[newChannel.channel - 1].feature)).build();
            this.mChannelApnInfo[newChannel.channel - 1].networkCallback = new BipNetworkCallback(this.mChannelApnInfo[newChannel.channel - 1].networkType, cmdMsg);
            cm.requestNetwork(request, this.mChannelApnInfo[newChannel.channel - 1].networkCallback);
            startDataConnectionTimer(cmdMsg);
            return false;
        } else {
            CatLog.d(this, "Data connection has requested!");
            return false;
        }
    }

    private boolean setupSpecificPdpConnection(CatCmdMessage cmdMsg) throws ConnectionSetupFailedException {
        String apnstring;
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        CatCmdMessage.ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (newChannel.networkAccessName == null) {
            CatLog.d(this, "no accessname for PS bearer req");
            return setupDefaultDataConnection(cmdMsg);
        } else if (checkExistingCsCallInNetworkClass2G()) {
            this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 2, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
            throw new ConnectionSetupFailedException("Busy on voice call");
        } else if (this.mChannelApnInfo[newChannel.channel - 1] == null || this.mChannelApnInfo[newChannel.channel - 1].networkCallback == null) {
            CatLog.d(this, "Detected new data connection parameters");
            HwCustBipProxy hwCustBipProxy = this.mHwCustBipProxy;
            boolean userbipSetting = hwCustBipProxy != null && hwCustBipProxy.kddiBipOtaEnable();
            String str = "au";
            if (newChannel.userLogin == null) {
                newChannel.userLogin = userbipSetting ? str : PhoneConfigurationManager.SSSS;
            }
            if (newChannel.userPassword == null) {
                if (!userbipSetting) {
                    str = PhoneConfigurationManager.SSSS;
                }
                newChannel.userPassword = str;
            }
            this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
            this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
            if (userbipSetting) {
                apnstring = this.mHwCustBipProxy.getApnString(newChannel, this.mChannelApnInfo[newChannel.channel - 1].type);
            } else {
                apnstring = getApnString(newChannel);
            }
            SystemProperties.set("gsm.bip.apn", apnstring);
            CatLog.d(this, "setupSpecificPdpConnection");
            NetworkRequest request = new NetworkRequest.Builder().addTransportType(0).addCapability(getBipCapability(this.mChannelApnInfo[newChannel.channel - 1].feature)).build();
            this.mChannelApnInfo[newChannel.channel - 1].networkCallback = new BipNetworkCallback(this.mChannelApnInfo[newChannel.channel - 1].networkType, cmdMsg);
            cm.requestNetwork(request, this.mChannelApnInfo[newChannel.channel - 1].networkCallback);
            startDataConnectionTimer(cmdMsg);
            return false;
        } else {
            CatLog.d(this, "Data connection has requested!");
            return false;
        }
    }

    public String getApnString(CatCmdMessage.ChannelSettings newChannel) {
        return "bipapn, " + newChannel.networkAccessName + ", ," + String.valueOf(newChannel.port) + ", " + newChannel.userLogin + ", " + newChannel.userPassword + ", , , , , , ,3 , " + this.mChannelApnInfo[newChannel.channel - 1].type;
    }

    private int getBipCapability(String feature) {
        CatLog.d(this, "feature: " + feature);
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP0.equals(feature)) {
            return 25;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP1.equals(feature)) {
            return 26;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP2.equals(feature)) {
            return 27;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP3.equals(feature)) {
            return 28;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP4.equals(feature)) {
            return 29;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP5.equals(feature)) {
            return 30;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP6.equals(feature)) {
            return 31;
        }
        return 25;
    }

    private int getChannelId(CatCmdMessage cmdMsg) {
        int channel = 0;
        if (cmdMsg.getCmdType() == AppInterface.CommandType.OPEN_CHANNEL) {
            channel = cmdMsg.getChannelSettings().channel;
        } else if (cmdMsg.getCmdType() == AppInterface.CommandType.CLOSE_CHANNEL || cmdMsg.getCmdType() == AppInterface.CommandType.RECEIVE_DATA || cmdMsg.getCmdType() == AppInterface.CommandType.SEND_DATA) {
            channel = cmdMsg.getDataSettings().channel;
        }
        CatLog.d(this, "getChannelId:" + channel);
        return channel;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void checkSetStatusOrNot(CatCmdMessage cmdMsg) {
        int channel = getChannelId(cmdMsg);
        int index = channel - 1;
        BipChannel tempChannel = this.mBipChannels[index];
        if (channel > 0) {
            if (channel <= 7) {
                if (tempChannel == null) {
                    CatLog.d(this, "checkSetStatusOrNot, mBipChannel[" + index + "] is null, just return");
                    return;
                } else if (5 == (tempChannel.getStatus() & 255)) {
                    CatLog.d(this, "checkSetStatusOrNot, mBipChannel[" + index + "] already link droped, just return");
                    return;
                } else {
                    CatLog.d(this, "checkSetStatusOrNot, mBipChannel[" + index + "] CH_STATUS_LINK_DROP");
                    tempChannel.setStatus(5);
                    return;
                }
            }
        }
        CatLog.d(this, "checkSetStatusOrNot, channel_id" + index + "is invalid, just return");
    }

    /* access modifiers changed from: package-private */
    public class BipNetworkCallback extends ConnectivityManager.NetworkCallback {
        CatCmdMessage mCmdMsg;
        Network mCurrentNetwork = null;
        private int mNetworkType;

        BipNetworkCallback(int networkType, CatCmdMessage cmdMsg) {
            this.mNetworkType = networkType;
            this.mCmdMsg = cmdMsg;
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            this.mCurrentNetwork = network;
            CatLog.d(this, "onAvailable got Network: " + network);
            CatLog.d(this, "MSG_ID_SETUP_DATA_CALL");
            if (BipProxy.this.mChannelApnInfo[this.mNetworkType - 38] != null) {
                BipProxy bipProxy = BipProxy.this;
                Message msg = bipProxy.obtainMessage(10, bipProxy.mChannelApnInfo[this.mNetworkType - 38].bakCmdMsg);
                AsyncResult.forMessage(msg, (Object) null, (Throwable) null);
                msg.sendToTarget();
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            CatLog.d(this, "onLost Network: " + network + ", mCurrentNetwork: " + this.mCurrentNetwork);
            if (network.equals(this.mCurrentNetwork)) {
                this.mCurrentNetwork = null;
                if (BipProxy.this.mChannelApnInfo[this.mNetworkType - 38] != null) {
                    CatCmdMessage cmdMsg = BipProxy.this.mChannelApnInfo[this.mNetworkType - 38].bakCmdMsg;
                    BipProxy.this.teardownDataConnection(cmdMsg);
                    BipProxy.this.checkSetStatusOrNot(cmdMsg);
                }
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onUnavailable() {
            CatLog.d(this, "onUnavailable");
            sendTerminalResponseHw(ResultCode.BEYOND_TERMINAL_CAPABILITY);
        }

        private void sendTerminalResponseHw(ResultCode rc) {
            if (this.mCmdMsg != null) {
                BipProxy.this.mStkService.sendTerminalResponseHw(this.mCmdMsg.mCmdDet, rc, false, 0, null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setupDataConnection(CatCmdMessage cmdMsg) {
        boolean result;
        CatCmdMessage.ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (newChannel.protocol == InterfaceTransportLevel.TransportProtocol.TCP_CLIENT_REMOTE || newChannel.protocol == InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_REMOTE) {
            BearerDescription bd = newChannel.bearerDescription;
            CatLog.d(this, "bd.type = " + bd.type + ", isWifiConnectedFlag = " + cmdMsg.getWifiConnectedFlag());
            if (cmdMsg.getWifiConnectedFlag()) {
                this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
                this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
                return true;
            }
            try {
                if (BearerDescription.BearerType.DEFAULT_BEARER == bd.type) {
                    int slotId = cmdMsg.getSlotId();
                    if (this.mHwCustBipProxy == null || (!this.mHwCustBipProxy.kddiBipOtaEnable() && !this.mHwCustBipProxy.isBipOtaEnable(slotId))) {
                        result = setupDefaultDataConnection(cmdMsg);
                    } else {
                        result = setupSpecificPdpConnection(cmdMsg);
                    }
                    return result;
                }
                if (!(BearerDescription.BearerType.MOBILE_PS == bd.type || BearerDescription.BearerType.MOBILE_PS_EXTENDED_QOS == bd.type)) {
                    if (BearerDescription.BearerType.E_UTRAN != bd.type) {
                        CatLog.d(this, "Unsupported bearer type");
                        this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                        return false;
                    }
                }
                return setupSpecificPdpConnection(cmdMsg);
            } catch (ConnectionSetupFailedException csfe) {
                CatLog.d(this, "setupDataConnection failed: " + csfe.getMessage());
                this.mBipChannels[newChannel.channel - 1] = null;
                cleanupBipChannel(newChannel.channel);
                return false;
            }
        } else {
            CatLog.d(this, "No data connection needed for this channel");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean teardownDataConnection(CatCmdMessage cmdMsg) {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        int channel = getChannelId(cmdMsg);
        if (channel > 0) {
            if (channel <= 7) {
                CatLog.d(this, "teardownDataConnection channel = " + channel);
                if (this.mChannelApnInfo[channel - 1] == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("teardownDataConnection mChannelApnInfo[");
                    sb.append(channel - 1);
                    sb.append("] is null");
                    CatLog.d(this, sb.toString());
                    return false;
                }
                if (!cmdMsg.getWifiConnectedFlag()) {
                    CatLog.d(this, "teardownDataConnection begin");
                    if (!(this.mChannelApnInfo[channel - 1] == null || this.mChannelApnInfo[channel - 1].networkCallback == null)) {
                        cm.unregisterNetworkCallback(this.mChannelApnInfo[channel - 1].networkCallback);
                        this.mChannelApnInfo[channel - 1].networkCallback = null;
                        CatLog.d(this, "unregisterNetworkCallback");
                    }
                    CatLog.d(this, "teardownDataConnection end");
                }
                cleanChannelApnInfo(channel);
                return true;
            }
        }
        CatLog.d(this, "teardownDataConnection, channel_id" + channel + "is invalid, just return");
        return false;
    }

    private void onSetupConnectionCompleted(AsyncResult ar) {
        if (ar == null) {
            CatLog.d(this, "onSetupConnectionCompleted ar null");
            return;
        }
        CatCmdMessage cmdMsg = (CatCmdMessage) ar.userObj;
        if (ar.exception != null) {
            CatLog.d(this, "Failed to setup data connection for channel: " + cmdMsg.getChannelSettings().channel);
            this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS, false, 0, new OpenChannelResponseData(cmdMsg.getChannelSettings().bufSize, null, cmdMsg.getChannelSettings().bearerDescription));
            cleanupBipChannel(cmdMsg.getChannelSettings().channel);
            cleanChannelApnInfo(cmdMsg.getChannelSettings().channel);
            return;
        }
        CatLog.d(this, "setup data connection for channel: " + cmdMsg.getChannelSettings().channel);
        if (this.mChannelApnInfo[cmdMsg.getChannelSettings().channel - 1] == null || this.mChannelApnInfo[cmdMsg.getChannelSettings().channel - 1].networkType == 0) {
            CatLog.d(this, "Succeeded to setup data connection for channel - Default bearer");
        }
        if (this.mBipChannels[cmdMsg.getChannelSettings().channel - 1] != null) {
            CatLog.d(this, "Continue processing open channel");
            this.mBipChannels[cmdMsg.getChannelSettings().channel - 1].preProcessOpen(cmdMsg);
            if (!this.mBipChannels[cmdMsg.getChannelSettings().channel - 1].open(cmdMsg)) {
                CatLog.d(this, "fail to open channel");
                cleanupBipChannel(cmdMsg.getChannelSettings().channel);
            }
        }
    }

    private void onTeardownConnectionCompleted(AsyncResult ar) {
        int channel;
        if (ar == null) {
            CatLog.d(this, "onTeardownConnectionCompleted ar null");
            return;
        }
        CatCmdMessage cmdMsg = (CatCmdMessage) ar.userObj;
        if (cmdMsg.getCmdType() == AppInterface.CommandType.OPEN_CHANNEL) {
            channel = cmdMsg.getChannelSettings().channel;
        } else if (cmdMsg.getCmdType() == AppInterface.CommandType.CLOSE_CHANNEL) {
            channel = cmdMsg.getDataSettings().channel;
        } else {
            return;
        }
        if (ar.exception != null) {
            CatLog.d(this, "Failed to teardown data connection for channel " + channel + ": " + ar.exception.getMessage());
        } else {
            CatLog.d(this, "Succedded to teardown data connection for channel: " + channel);
            for (int i = 0; i < 7; i++) {
                if (this.mBipChannels[i] != null) {
                    CatLog.d(this, "channel " + i + " link drop");
                    this.mBipChannels[i].setStatus(5);
                }
            }
        }
        cleanupBipChannel(channel);
    }

    private void startDataConnectionTimer(CatCmdMessage cmdMsg) {
        cancelDataConnectionTimer();
        CatLog.d(this, "startDataConnectionTimer.");
        sendMessageDelayed(obtainMessage(100, cmdMsg), 180000);
    }

    private void cancelDataConnectionTimer() {
        CatLog.d(this, "cancelDataConnectionTimer.");
        removeMessages(100);
    }

    private void acquireWakeLock() {
        synchronized (this.mWakeLock) {
            CatLog.d(this, "acquireWakeLock.");
            this.mWakeLock.acquire();
            removeMessages(99);
            sendMessageDelayed(obtainMessage(99), (long) this.mWakeLockTimeout);
        }
    }

    private void clearWakeLock() {
        synchronized (this.mWakeLock) {
            if (this.mWakeLock.isHeld()) {
                CatLog.d(this, "clearWakeLock.");
                this.mWakeLock.release();
                removeMessages(99);
            }
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        CatLog.d(this, "handleMessage: " + msg.what);
        int i = msg.what;
        if (i == 10) {
            cancelDataConnectionTimer();
            if (msg.obj != null) {
                onSetupConnectionCompleted((AsyncResult) msg.obj);
            }
        } else if (i != 11) {
            if (i == 99) {
                clearWakeLock();
            } else if (i == 100) {
                CatCmdMessage cmdMsg = (CatCmdMessage) msg.obj;
                CatLog.d(this, "EVENT_DC_TIMEOUT teardownDataConnection");
                teardownDataConnection(cmdMsg);
                this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
            } else {
                throw new AssertionError("Unrecognized message: " + msg.what);
            }
        } else if (msg.obj != null) {
            onTeardownConnectionCompleted((AsyncResult) msg.obj);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ChannelApnInfo {
        CatCmdMessage bakCmdMsg;
        String feature;
        BipNetworkCallback networkCallback;
        int networkType;
        String type;

        public ChannelApnInfo(int channel, CatCmdMessage cmdMsg) {
            switch (channel) {
                case 1:
                    this.networkType = 38;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP0;
                    this.type = "bip0";
                    break;
                case 2:
                    this.networkType = 39;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP1;
                    this.type = "bip1";
                    break;
                case 3:
                    this.networkType = 40;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP2;
                    this.type = "bip2";
                    break;
                case 4:
                    this.networkType = 41;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP3;
                    this.type = "bip3";
                    break;
                case 5:
                    this.networkType = 42;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP4;
                    this.type = "bip4";
                    break;
                case 6:
                    this.networkType = 43;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP5;
                    this.type = "bip5";
                    break;
                case 7:
                    this.networkType = 44;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP6;
                    this.type = "bip6";
                    break;
            }
            this.bakCmdMsg = cmdMsg;
        }
    }

    public void cleanChannelApnInfo(int channel) {
        CatLog.d(this, "cleanChannelApnInfo, channel: " + channel);
        this.mChannelApnInfo[channel + -1] = null;
    }

    /* access modifiers changed from: package-private */
    public class TcpClientChannel implements BipChannel {
        CatCmdMessage catCmdMsg = null;
        CatCmdMessage.ChannelSettings mChannelSettings = null;
        int mChannelStatus = 0;
        byte[] mRxBuf = new byte[16384];
        int mRxLen = 0;
        int mRxPos = 0;
        TcpClientSendThread mSendThread = null;
        Socket mSocket;
        TcpClientThread mThread = null;
        byte[] mTxBuf = new byte[16384];
        int mTxLen = 0;
        int mTxPos = 0;
        ResultCode result = ResultCode.OK;
        private Object token = new Object();

        TcpClientChannel() {
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << 8;
            this.catCmdMsg = cmdMsg;
            CatLog.d(this, "preProcessOpen bufSize = " + this.mChannelSettings.bufSize + " mImmediateLinkEstablish = " + BipProxy.this.mImmediateLinkEstablish);
            if (this.mChannelSettings.bufSize > 16384) {
                this.result = ResultCode.PRFRMD_WITH_MODIFICATION;
                this.mChannelSettings.bufSize = 16384;
            } else {
                this.mRxBuf = new byte[this.mChannelSettings.bufSize];
                this.mTxBuf = new byte[this.mChannelSettings.bufSize];
            }
            if (BipProxy.this.mImmediateLinkEstablish) {
                return true;
            }
            BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.OK, false, 0, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
            return true;
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public boolean open(CatCmdMessage cmdMsg) {
            this.mThread = new TcpClientThread();
            this.mThread.start();
            return true;
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void close(CatCmdMessage cmdMsg) {
            if (this.mChannelSettings != null) {
                CatLog.d(this, "Update channel status to closed before close socket");
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            CatLog.d(this, "mSocket = " + this.mSocket);
            Socket socket = this.mSocket;
            if (socket != null && !socket.isClosed()) {
                try {
                    this.mSocket.close();
                } catch (IOException e) {
                }
            }
            this.mSocket = null;
            this.mRxPos = 0;
            this.mRxLen = 0;
            this.mTxPos = 0;
            this.mTxLen = 0;
            CatCmdMessage.ChannelSettings channelSettings = this.mChannelSettings;
            if (channelSettings == null) {
                CatLog.d(this, "TcpClientChannel close BIP_ERROR");
                BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                return;
            }
            this.mChannelStatus = channelSettings.channel << 8;
            BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.OK, false, 0, null);
            BipProxy.this.mStkService.sendBroadcastToOtaUI(BipProxy.this.mStkService.OTA_TYPE, true);
            if (BipProxy.this.mChannelApnInfo[this.mChannelSettings.channel - 1] != null) {
                BipProxy.this.teardownDataConnection(cmdMsg);
            }
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == 1) {
                BipProxy bipProxy = BipProxy.this;
                if (bipProxy.setupDataConnection(bipProxy.openChCmdMsg)) {
                    CatLog.d(this, "Continue processing open channel");
                    if (!BipProxy.this.mBipChannels[cmdMsg.getDataSettings().channel - 1].open(BipProxy.this.openChCmdMsg)) {
                        BipProxy.this.cleanupBipChannel(cmdMsg.getDataSettings().channel);
                        return;
                    }
                    this.mSendThread = new TcpClientSendThread(cmdMsg);
                    this.mSendThread.start();
                    return;
                }
            }
            this.mSendThread = new TcpClientSendThread(cmdMsg);
            this.mSendThread.start();
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void receive(CatCmdMessage cmdMsg) {
            int available;
            byte[] data;
            ResultCode result2 = ResultCode.OK;
            CatLog.d(this, "RECEIVE_DATA on channel no: " + cmdMsg.getDataSettings().channel);
            int requested = cmdMsg.getDataSettings().length;
            if (requested > BipProxy.MAX_LEN_OF_CHANNEL_DATA) {
                requested = BipProxy.MAX_LEN_OF_CHANNEL_DATA;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result2 = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            if (this.mRxLen < 255) {
                available = this.mRxLen;
            } else {
                available = 255;
            }
            if (requested > 0) {
                byte[] data2 = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, data2, 0, requested);
                this.mRxPos += requested;
                data = data2;
            } else {
                data = null;
            }
            BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, result2, false, 0, new ReceiveDataResponseData(data, available));
            CatLog.d(this, "Receive Data, available data is: " + available);
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public int getStatus() {
            CatCmdMessage.ChannelSettings channelSettings = this.mChannelSettings;
            if (channelSettings == null) {
                this.mChannelStatus = 0;
            } else if (channelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            return this.mChannelStatus;
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void setStatus(int status) {
            if (5 == status) {
                int i = this.mChannelStatus;
                if ((i & 32768) == 32768) {
                    this.mChannelStatus = (i & 32512) | status;
                    BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
                }
            }
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void onSessionEnd() {
            TcpClientThread tcpClientThread = this.mThread;
            if (tcpClientThread == null || !tcpClientThread.isAlive()) {
                this.mThread = new TcpClientThread();
                this.mThread.start();
            }
        }

        class TcpClientThread extends Thread {
            TcpClientThread() {
            }

            private void processSocketException() {
                TcpClientChannel tcpClientChannel = TcpClientChannel.this;
                tcpClientChannel.mSocket = null;
                tcpClientChannel.mRxBuf = new byte[tcpClientChannel.mChannelSettings.bufSize];
                TcpClientChannel tcpClientChannel2 = TcpClientChannel.this;
                tcpClientChannel2.mTxBuf = new byte[tcpClientChannel2.mChannelSettings.bufSize];
                TcpClientChannel tcpClientChannel3 = TcpClientChannel.this;
                tcpClientChannel3.mRxPos = 0;
                tcpClientChannel3.mRxLen = 0;
                tcpClientChannel3.mTxPos = 0;
                tcpClientChannel3.mTxLen = 0;
                if (BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1] != null) {
                    CatLog.d(this, "TcpClientThread Exception happened");
                    BipProxy.this.teardownDataConnection(TcpClientChannel.this.catCmdMsg);
                    BipProxy.this.checkSetStatusOrNot(TcpClientChannel.this.catCmdMsg);
                }
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                InetAddress addr;
                CatLog.d(this, "Client thread start on channel no: " + TcpClientChannel.this.mChannelSettings.channel);
                try {
                    if (InterfaceTransportLevel.TransportProtocol.TCP_CLIENT_REMOTE == TcpClientChannel.this.mChannelSettings.protocol) {
                        addr = InetAddress.getByAddress(TcpClientChannel.this.mChannelSettings.destinationAddress);
                    } else {
                        addr = InetAddress.getLocalHost();
                    }
                    TcpClientChannel.this.mSocket = new Socket();
                    CatLog.d(this, "TcpClientThread bindSocket");
                    ChannelApnInfo curInfo = BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1];
                    if (!(curInfo == null || curInfo.networkCallback == null || curInfo.networkCallback.mCurrentNetwork == null)) {
                        curInfo.networkCallback.mCurrentNetwork.bindSocket(TcpClientChannel.this.mSocket);
                        TcpClientChannel.this.mSocket.connect(new InetSocketAddress(addr, TcpClientChannel.this.mChannelSettings.port));
                        CatLog.d(this, "TcpClientThread mSocket.connect");
                    }
                    CatLog.d(this, "Connected TCP client socket for channel " + TcpClientChannel.this.mChannelSettings.channel);
                    TcpClientChannel.this.mChannelStatus = (TcpClientChannel.this.mChannelSettings.channel << 8) + 32768;
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponseHw(TcpClientChannel.this.catCmdMsg.mCmdDet, TcpClientChannel.this.result, false, 0, new OpenChannelResponseData(TcpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpClientChannel.this.mChannelStatus), TcpClientChannel.this.mChannelSettings.bearerDescription));
                    } else {
                        BipProxy.this.mImmediateLinkEstablish = true;
                    }
                    while (TcpClientChannel.this.mSocket != null) {
                        try {
                            TcpClientChannel.this.mRxLen = TcpClientChannel.this.mSocket.getInputStream().read(TcpClientChannel.this.mRxBuf);
                        } catch (IOException e) {
                            CatLog.d(this, "Read on No: " + TcpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                            processSocketException();
                        }
                        synchronized (TcpClientChannel.this.token) {
                            if (TcpClientChannel.this.mRxLen > 0) {
                                CatLog.d(this, "BipLog, " + TcpClientChannel.this.mRxLen + " data read.");
                                TcpClientChannel.this.mRxPos = 0;
                                int available = 255;
                                if (TcpClientChannel.this.mRxLen < 255) {
                                    available = TcpClientChannel.this.mRxLen;
                                }
                                BipProxy.this.sendDataAvailableEvent(TcpClientChannel.this.mChannelStatus, (byte) (available & 255));
                                try {
                                    TcpClientChannel.this.token.wait();
                                } catch (InterruptedException e2) {
                                    CatLog.d(this, "InterruptedException " + e2.getMessage());
                                }
                            } else if (TcpClientChannel.this.mRxLen < 0) {
                                CatLog.d(this, "BipLog invalid len: " + TcpClientChannel.this.mRxLen + " data read.");
                                return;
                            } else {
                                CatLog.d(this, "No data read. ");
                            }
                        }
                    }
                    CatLog.d(this, "Client thread end on channel no: " + TcpClientChannel.this.mChannelSettings.channel);
                } catch (IOException e3) {
                    CatLog.d(this, "OPEN_CHANNEL - Client connection failed: " + e3.getMessage());
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponseHw(TcpClientChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new OpenChannelResponseData(TcpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpClientChannel.this.mChannelStatus), TcpClientChannel.this.mChannelSettings.bearerDescription));
                    } else {
                        BipProxy.this.mImmediateLinkEstablish = true;
                    }
                    if (BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1] != null) {
                        BipProxy.this.teardownDataConnection(TcpClientChannel.this.catCmdMsg);
                    }
                    BipProxy.this.mStkService.sendBroadcastToOtaUI(BipProxy.this.mStkService.OTA_TYPE, false);
                }
            }
        }

        class TcpClientSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public TcpClientSendThread(CatCmdMessage cmdMsg2) {
                this.cmdMsg = cmdMsg2;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                CatCmdMessage.DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d(this, "SEND_DATA on channel no: " + dataSettings.channel + " Transfer data into tx buffer");
                for (int i = 0; i < dataSettings.data.length && TcpClientChannel.this.mTxPos < TcpClientChannel.this.mTxBuf.length; i++) {
                    byte[] bArr = TcpClientChannel.this.mTxBuf;
                    TcpClientChannel tcpClientChannel = TcpClientChannel.this;
                    int i2 = tcpClientChannel.mTxPos;
                    tcpClientChannel.mTxPos = i2 + 1;
                    bArr[i2] = dataSettings.data[i];
                }
                TcpClientChannel.this.mTxLen += dataSettings.data.length;
                CatLog.d(this, "Tx buffer now contains " + TcpClientChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == 1) {
                    TcpClientChannel tcpClientChannel2 = TcpClientChannel.this;
                    tcpClientChannel2.mTxPos = 0;
                    int len = tcpClientChannel2.mTxLen;
                    TcpClientChannel.this.mTxLen = 0;
                    CatLog.d(this, "Sent data to socket " + len + " bytes.");
                    if (TcpClientChannel.this.mSocket == null) {
                        CatLog.d(this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                    try {
                        TcpClientChannel.this.mSocket.getOutputStream().write(TcpClientChannel.this.mTxBuf, 0, len);
                        CatLog.d(this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e) {
                        CatLog.d(this, "IOException " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                }
                int avail = 238;
                if (TcpClientChannel.this.mChannelSettings != null && (avail = TcpClientChannel.this.mChannelSettings.bufSize - TcpClientChannel.this.mTxLen) > 255) {
                    avail = 255;
                }
                CatLog.d(this, "TR with " + avail + " bytes available in Tx Buffer on channel " + dataSettings.channel);
                BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.OK, false, 0, new SendDataResponseData(avail));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class UdpClientChannel implements BipChannel {
        CatCmdMessage catCmdMsg = null;
        CatCmdMessage.ChannelSettings mChannelSettings = null;
        int mChannelStatus = 0;
        DatagramSocket mDatagramSocket;
        byte[] mRxBuf = new byte[BipProxy.UDP_CHANNEL_BUFFER_SIZE];
        int mRxLen = 0;
        int mRxPos = 0;
        UdpClientSendThread mSendThread = null;
        UdpClientThread mThread = null;
        byte[] mTxBuf = new byte[BipProxy.UDP_CHANNEL_BUFFER_SIZE];
        int mTxLen = 0;
        int mTxPos = 0;
        ResultCode result = ResultCode.OK;
        private Object token = new Object();

        UdpClientChannel() {
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << 8;
            this.catCmdMsg = cmdMsg;
            CatLog.d(this, "preProcessOpen bufSize = " + this.mChannelSettings.bufSize + ", mImmediateLinkEstablish = " + BipProxy.this.mImmediateLinkEstablish);
            if (this.mChannelSettings.bufSize > BipProxy.UDP_CHANNEL_BUFFER_SIZE) {
                this.result = ResultCode.PRFRMD_WITH_MODIFICATION;
                this.mChannelSettings.bufSize = BipProxy.UDP_CHANNEL_BUFFER_SIZE;
            } else if (this.mChannelSettings.bufSize > 0) {
                this.mRxBuf = new byte[this.mChannelSettings.bufSize];
                this.mTxBuf = new byte[this.mChannelSettings.bufSize];
            } else {
                this.mChannelSettings.bufSize = BipProxy.UDP_CHANNEL_BUFFER_SIZE;
            }
            if (BipProxy.this.mImmediateLinkEstablish) {
                return true;
            }
            BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.OK, false, 0, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
            return true;
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public boolean open(CatCmdMessage cmdMsg) {
            this.mThread = new UdpClientThread();
            this.mThread.start();
            return true;
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void close(CatCmdMessage cmdMsg) {
            if (this.mChannelSettings != null) {
                CatLog.d(this, "Update channel status to closed before close socket");
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            CatLog.d(this, "mDatagramSocket = " + this.mDatagramSocket + " mChannelSettings = " + this.mChannelSettings);
            DatagramSocket datagramSocket = this.mDatagramSocket;
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                this.mDatagramSocket.close();
            }
            this.mDatagramSocket = null;
            this.mRxPos = 0;
            this.mRxLen = 0;
            this.mTxPos = 0;
            this.mTxLen = 0;
            CatCmdMessage.ChannelSettings channelSettings = this.mChannelSettings;
            if (channelSettings == null) {
                CatLog.d(this, "UdpClientChannel close BIP_ERROR");
                BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                return;
            }
            this.mChannelStatus = channelSettings.channel << 8;
            BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, ResultCode.OK, false, 0, null);
            BipProxy.this.mStkService.sendBroadcastToOtaUI(BipProxy.this.mStkService.OTA_TYPE, true);
            if (BipProxy.this.mChannelApnInfo[this.mChannelSettings.channel - 1] != null) {
                CatLog.d(this, "UdpClientChannel close");
                BipProxy.this.teardownDataConnection(cmdMsg);
            }
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == 1) {
                BipProxy bipProxy = BipProxy.this;
                if (bipProxy.setupDataConnection(bipProxy.openChCmdMsg)) {
                    CatLog.d(this, "Continue processing open channel");
                    if (!BipProxy.this.mBipChannels[cmdMsg.getDataSettings().channel - 1].open(BipProxy.this.openChCmdMsg)) {
                        BipProxy.this.cleanupBipChannel(cmdMsg.getDataSettings().channel);
                        return;
                    }
                    this.mSendThread = new UdpClientSendThread(cmdMsg);
                    this.mSendThread.start();
                    return;
                }
            }
            this.mSendThread = new UdpClientSendThread(cmdMsg);
            this.mSendThread.start();
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void receive(CatCmdMessage cmdMsg) {
            int available;
            byte[] data;
            ResultCode result2 = ResultCode.OK;
            CatLog.d(this, "RECEIVE_DATA on channel no: " + cmdMsg.getDataSettings().channel);
            int requested = cmdMsg.getDataSettings().length;
            if (requested > BipProxy.MAX_LEN_OF_CHANNEL_DATA) {
                requested = BipProxy.MAX_LEN_OF_CHANNEL_DATA;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result2 = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            if (this.mRxLen < 255) {
                available = this.mRxLen;
            } else {
                available = 255;
            }
            if (requested > 0) {
                byte[] data2 = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, data2, 0, requested);
                this.mRxPos += requested;
                data = data2;
            } else {
                data = null;
            }
            BipProxy.this.mStkService.sendTerminalResponseHw(cmdMsg.mCmdDet, result2, false, 0, new ReceiveDataResponseData(data, available));
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public int getStatus() {
            CatCmdMessage.ChannelSettings channelSettings = this.mChannelSettings;
            if (channelSettings == null) {
                this.mChannelStatus = 0;
            } else if (channelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            return this.mChannelStatus;
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void setStatus(int status) {
            if (5 == status) {
                int i = this.mChannelStatus;
                if ((i & 32768) == 32768) {
                    this.mChannelStatus = (i & 32512) | status;
                    BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
                }
            }
        }

        @Override // com.android.internal.telephony.cat.BipProxy.BipChannel
        public void onSessionEnd() {
            UdpClientThread udpClientThread = this.mThread;
            if (udpClientThread == null || !udpClientThread.isAlive()) {
                this.mThread = new UdpClientThread();
                this.mThread.start();
            }
        }

        class UdpClientThread extends Thread {
            UdpClientThread() {
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Creating ");
                    sb.append(InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_REMOTE == UdpClientChannel.this.mChannelSettings.protocol ? "remote" : "local");
                    sb.append(" client socket for channel ");
                    sb.append(UdpClientChannel.this.mChannelSettings.channel);
                    CatLog.d(this, sb.toString());
                    UdpClientChannel.this.mDatagramSocket = new DatagramSocket();
                    CatLog.d(this, "UdpClientThread bindSocket");
                    ChannelApnInfo curInfo = BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1];
                    if (!(curInfo == null || curInfo.networkCallback == null || curInfo.networkCallback.mCurrentNetwork == null)) {
                        curInfo.networkCallback.mCurrentNetwork.bindSocket(UdpClientChannel.this.mDatagramSocket);
                    }
                    CatLog.d(this, "Connected UDP client socket for channel " + UdpClientChannel.this.mChannelSettings.channel);
                    UdpClientChannel.this.mChannelStatus = (UdpClientChannel.this.mChannelSettings.channel << 8) + 32768;
                    BipProxy.this.mStkService.sendTerminalResponseHw(UdpClientChannel.this.catCmdMsg.mCmdDet, UdpClientChannel.this.result, false, 0, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                    while (UdpClientChannel.this.mDatagramSocket != null) {
                        DatagramPacket packet = null;
                        boolean success = false;
                        try {
                            CatLog.d(this, "UDP Client listening on port: " + UdpClientChannel.this.mDatagramSocket.getLocalPort());
                            packet = new DatagramPacket(UdpClientChannel.this.mRxBuf, UdpClientChannel.this.mRxBuf.length);
                            UdpClientChannel.this.mDatagramSocket.receive(packet);
                            success = true;
                        } catch (IOException e) {
                            CatLog.d(this, "Read on No: " + UdpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                        } catch (IllegalArgumentException e2) {
                            CatLog.d(this, "IllegalArgumentException: " + e2.getMessage());
                        }
                        if (success) {
                            UdpClientChannel.this.mRxLen = packet.getLength();
                        } else {
                            UdpClientChannel udpClientChannel = UdpClientChannel.this;
                            udpClientChannel.mDatagramSocket = null;
                            udpClientChannel.mRxBuf = new byte[udpClientChannel.mChannelSettings.bufSize];
                            UdpClientChannel udpClientChannel2 = UdpClientChannel.this;
                            udpClientChannel2.mTxBuf = new byte[udpClientChannel2.mChannelSettings.bufSize];
                            UdpClientChannel udpClientChannel3 = UdpClientChannel.this;
                            udpClientChannel3.mRxPos = 0;
                            udpClientChannel3.mRxLen = 0;
                            udpClientChannel3.mTxPos = 0;
                            udpClientChannel3.mTxLen = 0;
                            if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                                CatLog.d(this, "UdpClientThread Exception happened");
                                BipProxy.this.teardownDataConnection(UdpClientChannel.this.catCmdMsg);
                                BipProxy.this.checkSetStatusOrNot(UdpClientChannel.this.catCmdMsg);
                            }
                        }
                        synchronized (UdpClientChannel.this.token) {
                            if (UdpClientChannel.this.mRxLen <= 0) {
                                CatLog.d(this, "No data read. " + UdpClientChannel.this.mRxLen);
                            } else {
                                CatLog.d(this, "BipLog, " + UdpClientChannel.this.mRxLen + " data read.");
                                UdpClientChannel.this.mRxPos = 0;
                                int available = 255;
                                if (UdpClientChannel.this.mRxLen < 255) {
                                    available = UdpClientChannel.this.mRxLen;
                                }
                                BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & 255));
                                try {
                                    UdpClientChannel.this.token.wait();
                                } catch (InterruptedException e3) {
                                    CatLog.d(this, "BipLog, InterruptedException catched.");
                                }
                            }
                        }
                    }
                    CatLog.d(this, "UDP Client thread end on channel no: " + UdpClientChannel.this.mChannelSettings.channel);
                } catch (IOException e4) {
                    CatLog.d(this, "OPEN_CHANNEL - UDP Client connection failed: " + e4.getMessage());
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponseHw(UdpClientChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                    } else {
                        BipProxy.this.mImmediateLinkEstablish = true;
                    }
                    if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                        BipProxy.this.teardownDataConnection(UdpClientChannel.this.catCmdMsg);
                    }
                    BipProxy.this.mStkService.sendBroadcastToOtaUI(BipProxy.this.mStkService.OTA_TYPE, false);
                }
            }
        }

        class UdpClientSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public UdpClientSendThread(CatCmdMessage cmdMsg2) {
                this.cmdMsg = cmdMsg2;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                CatCmdMessage.DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d(this, "SEND_DATA on channel no: " + dataSettings.channel);
                CatLog.d(this, "Transfer data into tx buffer");
                for (int i = 0; i < dataSettings.data.length && UdpClientChannel.this.mTxPos < UdpClientChannel.this.mTxBuf.length; i++) {
                    byte[] bArr = UdpClientChannel.this.mTxBuf;
                    UdpClientChannel udpClientChannel = UdpClientChannel.this;
                    int i2 = udpClientChannel.mTxPos;
                    udpClientChannel.mTxPos = i2 + 1;
                    bArr[i2] = dataSettings.data[i];
                }
                UdpClientChannel.this.mTxLen += dataSettings.data.length;
                CatLog.d(this, "Tx buffer now contains " + UdpClientChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == 1) {
                    UdpClientChannel udpClientChannel2 = UdpClientChannel.this;
                    udpClientChannel2.mTxPos = 0;
                    int len = udpClientChannel2.mTxLen;
                    UdpClientChannel.this.mTxLen = 0;
                    CatLog.d(this, "Sent data to socket " + len + " bytes.");
                    if (UdpClientChannel.this.mDatagramSocket == null) {
                        CatLog.d(this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                    InetAddress addr = null;
                    try {
                        addr = InterfaceTransportLevel.TransportProtocol.UDP_CLIENT_REMOTE == UdpClientChannel.this.mChannelSettings.protocol ? InetAddress.getByAddress(UdpClientChannel.this.mChannelSettings.destinationAddress) : InetAddress.getLocalHost();
                    } catch (IOException e) {
                        CatLog.d(this, "OPEN_CHANNEL - UDP Client connection failed: " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                        if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                            BipProxy.this.teardownDataConnection(this.cmdMsg);
                        }
                    }
                    try {
                        UdpClientChannel.this.mDatagramSocket.send(new DatagramPacket(UdpClientChannel.this.mTxBuf, len, addr, UdpClientChannel.this.mChannelSettings.port));
                        CatLog.d(this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e2) {
                        CatLog.d(this, "IOException " + e2.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    } catch (IllegalArgumentException e3) {
                        CatLog.d(this, "IllegalArgumentException " + e3.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                }
                int avail = 238;
                if (UdpClientChannel.this.mChannelSettings != null && (avail = UdpClientChannel.this.mChannelSettings.bufSize - UdpClientChannel.this.mTxLen) > 255) {
                    avail = 255;
                }
                CatLog.d(this, "TR with " + avail + " bytes available in Tx Buffer on channel " + dataSettings.channel);
                BipProxy.this.mStkService.sendTerminalResponseHw(this.cmdMsg.mCmdDet, ResultCode.OK, false, 0, new SendDataResponseData(avail));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class DefaultBearerStateReceiver extends BroadcastReceiver {
        Context mContext;
        IntentFilter mFilter = new IntentFilter();
        boolean mIsRegistered;

        public DefaultBearerStateReceiver(Context context) {
            this.mContext = context;
            this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mIsRegistered = false;
        }

        public void startListening() {
            if (this.mIsRegistered) {
                CatLog.d(this, "already registered");
                return;
            }
            this.mContext.registerReceiver(this, this.mFilter);
            this.mIsRegistered = true;
        }

        public void stopListening() {
            if (!this.mIsRegistered) {
                CatLog.d(this, "not registered or already de-registered");
                return;
            }
            this.mContext.unregisterReceiver(this);
            this.mIsRegistered = false;
        }

        public void handleWifiDisconnectedMsg(boolean isWifiConnected) {
            if (!isWifiConnected) {
                for (int i = 0; i < 7; i++) {
                    if (BipProxy.this.mBipChannels[i] != null) {
                        CatLog.d(this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] Status is " + (BipProxy.this.mBipChannels[i].getStatus() & 255));
                        if (5 == (BipProxy.this.mBipChannels[i].getStatus() & 255)) {
                            CatLog.d(this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] already link droped");
                        } else if (BipProxy.this.mIsWifiConnected[i]) {
                            BipProxy.this.mBipChannels[i].setStatus(5);
                            CatLog.d(this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] CH_STATUS_LINK_DROP");
                            BipProxy.this.cleanChannelApnInfo(i + 1);
                        } else {
                            CatLog.d(this, "handleWifiDisconnectedMsg: mIsWifiConnected[" + i + "] is false");
                        }
                    } else {
                        CatLog.d(this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] is null");
                    }
                }
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                CatLog.d(this, "Received broadcast: intent is null");
            } else if (intent.getAction() == null) {
                CatLog.d(this, "Received broadcast: Action is null");
            } else {
                CatLog.d(this, "onReceive, action: " + intent.getAction());
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    boolean isWifiConnected = networkInfo != null && networkInfo.isConnected();
                    CatLog.d(this, "WifiManager.NETWORK_STATE_CHANGED_ACTION: IsWifiConnected = " + isWifiConnected);
                    handleWifiDisconnectedMsg(isWifiConnected);
                }
            }
        }
    }
}
