package com.android.internal.telephony.cat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.cat.BearerDescription.BearerType;
import com.android.internal.telephony.cat.CatCmdMessage.ChannelSettings;
import com.android.internal.telephony.cat.CatCmdMessage.DataSettings;
import com.android.internal.telephony.cat.InterfaceTransportLevel.TransportProtocol;
import huawei.cust.HwCustUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BipProxy extends Handler {
    private static final /* synthetic */ int[] -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues = null;
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
    private static final int INVALID_SUBID = -1;
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
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
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
    WakeLock mWakeLock;
    final int mWakeLockTimeout;
    CatCmdMessage openChCmdMsg;

    interface BipChannel {
        void close(CatCmdMessage catCmdMessage);

        int getStatus();

        void onSessionEnd();

        boolean open(CatCmdMessage catCmdMessage);

        boolean preProcessOpen(CatCmdMessage catCmdMessage);

        void receive(CatCmdMessage catCmdMessage);

        void send(CatCmdMessage catCmdMessage);

        void setStatus(int i);
    }

    class BipNetworkCallback extends NetworkCallback {
        CatCmdMessage mCmdMsg;
        Network mCurrentNetwork = null;
        private int mNetworkType;

        BipNetworkCallback(int networkType, CatCmdMessage cmdMsg) {
            this.mNetworkType = networkType;
            this.mCmdMsg = cmdMsg;
        }

        public void onAvailable(Network network) {
            this.mCurrentNetwork = network;
            CatLog.d((Object) this, "onAvailable got Network: " + network);
            CatLog.d((Object) this, "MSG_ID_SETUP_DATA_CALL");
            Message msg = BipProxy.this.obtainMessage(10, BipProxy.this.mChannelApnInfo[this.mNetworkType - 38].bakCmdMsg);
            AsyncResult.forMessage(msg, null, null);
            msg.sendToTarget();
        }

        public void onLost(Network network) {
            CatLog.d((Object) this, "onLost Network: " + network + ", mCurrentNetwork: " + this.mCurrentNetwork);
            if (network.equals(this.mCurrentNetwork)) {
                this.mCurrentNetwork = null;
                CatCmdMessage cmdMsg = BipProxy.this.mChannelApnInfo[this.mNetworkType - 38].bakCmdMsg;
                BipProxy.this.teardownDataConnection(cmdMsg);
                BipProxy.this.checkSetStatusOrNot(cmdMsg);
            }
        }

        public void onUnavailable() {
            CatLog.d((Object) this, "onUnavailable");
            sendTerminalResponse(ResultCode.BEYOND_TERMINAL_CAPABILITY);
        }

        private void sendTerminalResponse(ResultCode rc) {
            if (this.mCmdMsg != null) {
                BipProxy.this.mStkService.sendTerminalResponse(this.mCmdMsg.mCmdDet, rc, false, 0, null);
            }
        }
    }

    static class ChannelApnInfo {
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

    private static class ConnectionSetupFailedException extends IOException {
        public ConnectionSetupFailedException(String message) {
            super(message);
            CatLog.d((Object) this, "ConnectionSetupFailedException: " + message);
        }
    }

    class DefaultBearerStateReceiver extends BroadcastReceiver {
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
                CatLog.d((Object) this, "already registered");
                return;
            }
            this.mContext.registerReceiver(this, this.mFilter);
            this.mIsRegistered = true;
        }

        public void stopListening() {
            if (this.mIsRegistered) {
                this.mContext.unregisterReceiver(this);
                this.mIsRegistered = false;
                return;
            }
            CatLog.d((Object) this, "not registered or already de-registered");
        }

        public void handleWifiDisconnectedMsg(boolean isWifiConnected) {
            if (!isWifiConnected) {
                for (int i = 0; i < 7; i++) {
                    if (BipProxy.this.mBipChannels[i] != null) {
                        CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] Status is " + (BipProxy.this.mBipChannels[i].getStatus() & 255));
                        if (5 == (BipProxy.this.mBipChannels[i].getStatus() & 255)) {
                            CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] already link droped");
                        } else if (BipProxy.this.mIsWifiConnected[i]) {
                            BipProxy.this.mBipChannels[i].setStatus(5);
                            CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] CH_STATUS_LINK_DROP");
                            BipProxy.this.cleanChannelApnInfo(i + 1);
                        } else {
                            CatLog.d((Object) this, "handleWifiDisconnectedMsg: mIsWifiConnected[" + i + "] is false");
                        }
                    } else {
                        CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] is null");
                    }
                }
            }
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                CatLog.d((Object) this, "Received broadcast: intent is null");
            } else if (intent.getAction() == null) {
                CatLog.d((Object) this, "Received broadcast: Action is null");
            } else {
                CatLog.d((Object) this, "onReceive, action: " + intent.getAction());
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    boolean isWifiConnected = networkInfo != null ? networkInfo.isConnected() : false;
                    CatLog.d((Object) this, "WifiManager.NETWORK_STATE_CHANGED_ACTION: IsWifiConnected = " + isWifiConnected);
                    handleWifiDisconnectedMsg(isWifiConnected);
                }
            }
        }
    }

    class TcpClientChannel implements BipChannel {
        CatCmdMessage catCmdMsg = null;
        ChannelSettings mChannelSettings = null;
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

        class TcpClientSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public TcpClientSendThread(CatCmdMessage cmdMsg) {
                this.cmdMsg = cmdMsg;
            }

            public void run() {
                DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d((Object) this, "SEND_DATA on channel no: " + dataSettings.channel + " Transfer data into tx buffer");
                for (int i = 0; i < dataSettings.data.length && TcpClientChannel.this.mTxPos < TcpClientChannel.this.mTxBuf.length; i++) {
                    byte[] bArr = TcpClientChannel.this.mTxBuf;
                    TcpClientChannel tcpClientChannel = TcpClientChannel.this;
                    int i2 = tcpClientChannel.mTxPos;
                    tcpClientChannel.mTxPos = i2 + 1;
                    bArr[i2] = dataSettings.data[i];
                }
                TcpClientChannel tcpClientChannel2 = TcpClientChannel.this;
                tcpClientChannel2.mTxLen += dataSettings.data.length;
                CatLog.d((Object) this, "Tx buffer now contains " + TcpClientChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == 1) {
                    TcpClientChannel.this.mTxPos = 0;
                    int len = TcpClientChannel.this.mTxLen;
                    TcpClientChannel.this.mTxLen = 0;
                    CatLog.d((Object) this, "Sent data to socket " + len + " bytes.");
                    if (TcpClientChannel.this.mSocket == null) {
                        CatLog.d((Object) this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                    try {
                        TcpClientChannel.this.mSocket.getOutputStream().write(TcpClientChannel.this.mTxBuf, 0, len);
                        CatLog.d((Object) this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e) {
                        CatLog.d((Object) this, "IOException " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                }
                int avail = 238;
                if (TcpClientChannel.this.mChannelSettings != null) {
                    avail = TcpClientChannel.this.mChannelSettings.bufSize - TcpClientChannel.this.mTxLen;
                    if (avail > 255) {
                        avail = 255;
                    }
                }
                CatLog.d((Object) this, "TR with " + avail + " bytes available in Tx Buffer on channel " + dataSettings.channel);
                BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.OK, false, 0, new SendDataResponseData(avail));
            }
        }

        class TcpClientThread extends Thread {
            TcpClientThread() {
            }

            public void run() {
                CatLog.d((Object) this, "Client thread start on channel no: " + TcpClientChannel.this.mChannelSettings.channel);
                try {
                    InetAddress addr;
                    if (TransportProtocol.TCP_CLIENT_REMOTE == TcpClientChannel.this.mChannelSettings.protocol) {
                        addr = InetAddress.getByAddress(TcpClientChannel.this.mChannelSettings.destinationAddress);
                    } else {
                        addr = InetAddress.getLocalHost();
                    }
                    TcpClientChannel.this.mSocket = new Socket();
                    CatLog.d((Object) this, "TcpClientThread bindSocket");
                    ChannelApnInfo curInfo = BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1];
                    if (!(curInfo == null || curInfo.networkCallback == null || curInfo.networkCallback.mCurrentNetwork == null)) {
                        curInfo.networkCallback.mCurrentNetwork.bindSocket(TcpClientChannel.this.mSocket);
                        TcpClientChannel.this.mSocket.connect(new InetSocketAddress(addr, TcpClientChannel.this.mChannelSettings.port));
                        CatLog.d((Object) this, "TcpClientThread mSocket.connect");
                    }
                    CatLog.d((Object) this, "Connected TCP client socket for channel " + TcpClientChannel.this.mChannelSettings.channel);
                    TcpClientChannel.this.mChannelStatus = (TcpClientChannel.this.mChannelSettings.channel << 8) + 32768;
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponse(TcpClientChannel.this.catCmdMsg.mCmdDet, TcpClientChannel.this.result, false, 0, new OpenChannelResponseData(TcpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpClientChannel.this.mChannelStatus), TcpClientChannel.this.mChannelSettings.bearerDescription));
                    } else {
                        BipProxy.this.mImmediateLinkEstablish = true;
                    }
                    while (TcpClientChannel.this.mSocket != null) {
                        try {
                            TcpClientChannel.this.mRxLen = TcpClientChannel.this.mSocket.getInputStream().read(TcpClientChannel.this.mRxBuf);
                        } catch (IOException e) {
                            CatLog.d((Object) this, "Read on No: " + TcpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                            TcpClientChannel.this.mSocket = null;
                            TcpClientChannel.this.mRxBuf = new byte[TcpClientChannel.this.mChannelSettings.bufSize];
                            TcpClientChannel.this.mTxBuf = new byte[TcpClientChannel.this.mChannelSettings.bufSize];
                            TcpClientChannel.this.mRxPos = 0;
                            TcpClientChannel.this.mRxLen = 0;
                            TcpClientChannel.this.mTxPos = 0;
                            TcpClientChannel.this.mTxLen = 0;
                            if (BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1] != null) {
                                CatLog.d((Object) this, "TcpClientThread Exception happened");
                                BipProxy.this.teardownDataConnection(TcpClientChannel.this.catCmdMsg);
                                BipProxy.this.checkSetStatusOrNot(TcpClientChannel.this.catCmdMsg);
                            }
                        }
                        synchronized (TcpClientChannel.this.token) {
                            if (TcpClientChannel.this.mRxLen > 0) {
                                CatLog.d((Object) this, "BipLog, " + TcpClientChannel.this.mRxLen + " data read.");
                                TcpClientChannel.this.mRxPos = 0;
                                int available = 255;
                                if (TcpClientChannel.this.mRxLen < 255) {
                                    available = TcpClientChannel.this.mRxLen;
                                }
                                BipProxy.this.sendDataAvailableEvent(TcpClientChannel.this.mChannelStatus, (byte) (available & 255));
                                try {
                                    TcpClientChannel.this.token.wait();
                                } catch (InterruptedException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                    CatLog.d((Object) this, "Client thread end on channel no: " + TcpClientChannel.this.mChannelSettings.channel);
                } catch (IOException e3) {
                    CatLog.d((Object) this, "OPEN_CHANNEL - Client connection failed: " + e3.getMessage());
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponse(TcpClientChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new OpenChannelResponseData(TcpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpClientChannel.this.mChannelStatus), TcpClientChannel.this.mChannelSettings.bearerDescription));
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

        TcpClientChannel() {
        }

        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << 8;
            this.catCmdMsg = cmdMsg;
            CatLog.d((Object) this, "preProcessOpen bufSize = " + this.mChannelSettings.bufSize + " mImmediateLinkEstablish = " + BipProxy.this.mImmediateLinkEstablish);
            if (this.mChannelSettings.bufSize > 16384) {
                this.result = ResultCode.PRFRMD_WITH_MODIFICATION;
                this.mChannelSettings.bufSize = 16384;
            } else {
                this.mRxBuf = new byte[this.mChannelSettings.bufSize];
                this.mTxBuf = new byte[this.mChannelSettings.bufSize];
            }
            if (!BipProxy.this.mImmediateLinkEstablish) {
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, 0, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
            }
            return true;
        }

        public boolean open(CatCmdMessage cmdMsg) {
            this.mThread = new TcpClientThread();
            this.mThread.start();
            return true;
        }

        public void close(CatCmdMessage cmdMsg) {
            if (this.mChannelSettings != null) {
                CatLog.d((Object) this, "Update channel status to closed before close socket");
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            CatLog.d((Object) this, "mSocket = " + this.mSocket);
            if (!(this.mSocket == null || (this.mSocket.isClosed() ^ 1) == 0)) {
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
            if (this.mChannelSettings == null) {
                CatLog.d((Object) this, "TcpClientChannel close BIP_ERROR");
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                return;
            }
            this.mChannelStatus = this.mChannelSettings.channel << 8;
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, 0, null);
            BipProxy.this.mStkService.sendBroadcastToOtaUI(BipProxy.this.mStkService.OTA_TYPE, true);
            if (BipProxy.this.mChannelApnInfo[this.mChannelSettings.channel - 1] != null) {
                BipProxy.this.teardownDataConnection(cmdMsg);
            }
        }

        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == 1 && BipProxy.this.setupDataConnection(BipProxy.this.openChCmdMsg)) {
                CatLog.d((Object) this, "Continue processing open channel");
                if (BipProxy.this.mBipChannels[cmdMsg.getDataSettings().channel - 1].open(BipProxy.this.openChCmdMsg)) {
                    this.mSendThread = new TcpClientSendThread(cmdMsg);
                    this.mSendThread.start();
                } else {
                    BipProxy.this.cleanupBipChannel(cmdMsg.getDataSettings().channel);
                }
                return;
            }
            this.mSendThread = new TcpClientSendThread(cmdMsg);
            this.mSendThread.start();
        }

        public void receive(CatCmdMessage cmdMsg) {
            ResultCode result = ResultCode.OK;
            CatLog.d((Object) this, "RECEIVE_DATA on channel no: " + cmdMsg.getDataSettings().channel);
            int requested = cmdMsg.getDataSettings().length;
            if (requested > 237) {
                requested = 237;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            int available = 255;
            if (this.mRxLen < 255) {
                available = this.mRxLen;
            }
            byte[] data = null;
            if (requested > 0) {
                data = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, data, 0, requested);
                this.mRxPos += requested;
            }
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, result, false, 0, new ReceiveDataResponseData(data, available));
            CatLog.d((Object) this, "Receive Data, available data is: " + available);
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
        }

        public int getStatus() {
            if (this.mChannelSettings == null) {
                this.mChannelStatus = 0;
            } else if (this.mChannelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            return this.mChannelStatus;
        }

        public void setStatus(int status) {
            if (5 == status && (this.mChannelStatus & 32768) == 32768) {
                this.mChannelStatus = (this.mChannelStatus & 32512) | status;
                BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
            }
        }

        public void onSessionEnd() {
            if (this.mThread == null || (this.mThread.isAlive() ^ 1) != 0) {
                this.mThread = new TcpClientThread();
                this.mThread.start();
            }
        }
    }

    class UdpClientChannel implements BipChannel {
        CatCmdMessage catCmdMsg = null;
        ChannelSettings mChannelSettings = null;
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

        class UdpClientSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public UdpClientSendThread(CatCmdMessage cmdMsg) {
                this.cmdMsg = cmdMsg;
            }

            public void run() {
                DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d((Object) this, "SEND_DATA on channel no: " + dataSettings.channel);
                CatLog.d((Object) this, "Transfer data into tx buffer");
                for (int i = 0; i < dataSettings.data.length && UdpClientChannel.this.mTxPos < UdpClientChannel.this.mTxBuf.length; i++) {
                    byte[] bArr = UdpClientChannel.this.mTxBuf;
                    UdpClientChannel udpClientChannel = UdpClientChannel.this;
                    int i2 = udpClientChannel.mTxPos;
                    udpClientChannel.mTxPos = i2 + 1;
                    bArr[i2] = dataSettings.data[i];
                }
                UdpClientChannel udpClientChannel2 = UdpClientChannel.this;
                udpClientChannel2.mTxLen += dataSettings.data.length;
                CatLog.d((Object) this, "Tx buffer now contains " + UdpClientChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == 1) {
                    UdpClientChannel.this.mTxPos = 0;
                    int len = UdpClientChannel.this.mTxLen;
                    UdpClientChannel.this.mTxLen = 0;
                    CatLog.d((Object) this, "Sent data to socket " + len + " bytes.");
                    if (UdpClientChannel.this.mDatagramSocket == null) {
                        CatLog.d((Object) this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                    InetAddress addr = null;
                    try {
                        addr = TransportProtocol.UDP_CLIENT_REMOTE == UdpClientChannel.this.mChannelSettings.protocol ? InetAddress.getByAddress(UdpClientChannel.this.mChannelSettings.destinationAddress) : InetAddress.getLocalHost();
                    } catch (IOException e) {
                        CatLog.d((Object) this, "OPEN_CHANNEL - UDP Client connection failed: " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                        if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                            BipProxy.this.teardownDataConnection(this.cmdMsg);
                        }
                    }
                    try {
                        UdpClientChannel.this.mDatagramSocket.send(new DatagramPacket(UdpClientChannel.this.mTxBuf, len, addr, UdpClientChannel.this.mChannelSettings.port));
                        CatLog.d((Object) this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e2) {
                        CatLog.d((Object) this, "IOException " + e2.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    } catch (IllegalArgumentException e3) {
                        CatLog.d((Object) this, "IllegalArgumentException " + e3.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new SendDataResponseData(0));
                        return;
                    }
                }
                int avail = 238;
                if (UdpClientChannel.this.mChannelSettings != null) {
                    avail = UdpClientChannel.this.mChannelSettings.bufSize - UdpClientChannel.this.mTxLen;
                    if (avail > 255) {
                        avail = 255;
                    }
                }
                CatLog.d((Object) this, "TR with " + avail + " bytes available in Tx Buffer on channel " + dataSettings.channel);
                BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.OK, false, 0, new SendDataResponseData(avail));
            }
        }

        class UdpClientThread extends Thread {
            UdpClientThread() {
            }

            /* JADX WARNING: Removed duplicated region for block: B:46:0x021e  */
            /* JADX WARNING: Removed duplicated region for block: B:21:0x010a  */
            /* JADX WARNING: Removed duplicated region for block: B:24:0x0119 A:{SYNTHETIC} */
            /* JADX WARNING: Removed duplicated region for block: B:21:0x010a  */
            /* JADX WARNING: Removed duplicated region for block: B:46:0x021e  */
            /* JADX WARNING: Removed duplicated region for block: B:24:0x0119 A:{SYNTHETIC} */
            /* JADX WARNING: Removed duplicated region for block: B:46:0x021e  */
            /* JADX WARNING: Removed duplicated region for block: B:21:0x010a  */
            /* JADX WARNING: Removed duplicated region for block: B:24:0x0119 A:{SYNTHETIC} */
            /* JADX WARNING: Removed duplicated region for block: B:21:0x010a  */
            /* JADX WARNING: Removed duplicated region for block: B:46:0x021e  */
            /* JADX WARNING: Removed duplicated region for block: B:24:0x0119 A:{SYNTHETIC} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                IOException e;
                IllegalArgumentException e2;
                try {
                    CatLog.d((Object) this, "Creating " + (TransportProtocol.UDP_CLIENT_REMOTE == UdpClientChannel.this.mChannelSettings.protocol ? "remote" : "local") + " client socket for channel " + UdpClientChannel.this.mChannelSettings.channel);
                    UdpClientChannel.this.mDatagramSocket = new DatagramSocket();
                    CatLog.d((Object) this, "UdpClientThread bindSocket");
                    ChannelApnInfo curInfo = BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1];
                    if (!(curInfo == null || curInfo.networkCallback == null || curInfo.networkCallback.mCurrentNetwork == null)) {
                        curInfo.networkCallback.mCurrentNetwork.bindSocket(UdpClientChannel.this.mDatagramSocket);
                    }
                    CatLog.d((Object) this, "Connected UDP client socket for channel " + UdpClientChannel.this.mChannelSettings.channel);
                    UdpClientChannel.this.mChannelStatus = (UdpClientChannel.this.mChannelSettings.channel << 8) + 32768;
                    BipProxy.this.mStkService.sendTerminalResponse(UdpClientChannel.this.catCmdMsg.mCmdDet, UdpClientChannel.this.result, false, 0, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                    while (UdpClientChannel.this.mDatagramSocket != null) {
                        DatagramPacket packet = null;
                        boolean success = false;
                        try {
                            CatLog.d((Object) this, "UDP Client listening on port: " + UdpClientChannel.this.mDatagramSocket.getLocalPort());
                            DatagramPacket packet2 = new DatagramPacket(UdpClientChannel.this.mRxBuf, UdpClientChannel.this.mRxBuf.length);
                            try {
                                UdpClientChannel.this.mDatagramSocket.receive(packet2);
                                success = true;
                                packet = packet2;
                            } catch (IOException e3) {
                                e = e3;
                                packet = packet2;
                                CatLog.d((Object) this, "Read on No: " + UdpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                                if (success) {
                                }
                                synchronized (UdpClientChannel.this.token) {
                                }
                            } catch (IllegalArgumentException e4) {
                                e2 = e4;
                                packet = packet2;
                                CatLog.d((Object) this, "IllegalArgumentException: " + e2.getMessage());
                                if (success) {
                                }
                                synchronized (UdpClientChannel.this.token) {
                                }
                            }
                        } catch (IOException e5) {
                            e = e5;
                            CatLog.d((Object) this, "Read on No: " + UdpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                            if (success) {
                            }
                            synchronized (UdpClientChannel.this.token) {
                            }
                        } catch (IllegalArgumentException e6) {
                            e2 = e6;
                            CatLog.d((Object) this, "IllegalArgumentException: " + e2.getMessage());
                            if (success) {
                            }
                            synchronized (UdpClientChannel.this.token) {
                            }
                        }
                        if (success) {
                            UdpClientChannel.this.mRxLen = packet.getLength();
                        } else {
                            UdpClientChannel.this.mDatagramSocket = null;
                            UdpClientChannel.this.mRxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                            UdpClientChannel.this.mTxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                            UdpClientChannel.this.mRxPos = 0;
                            UdpClientChannel.this.mRxLen = 0;
                            UdpClientChannel.this.mTxPos = 0;
                            UdpClientChannel.this.mTxLen = 0;
                            if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                                CatLog.d((Object) this, "UdpClientThread Exception happened");
                                BipProxy.this.teardownDataConnection(UdpClientChannel.this.catCmdMsg);
                                BipProxy.this.checkSetStatusOrNot(UdpClientChannel.this.catCmdMsg);
                            }
                        }
                        synchronized (UdpClientChannel.this.token) {
                            if (UdpClientChannel.this.mRxLen <= 0) {
                                CatLog.d((Object) this, "No data read. " + UdpClientChannel.this.mRxLen);
                            } else {
                                CatLog.d((Object) this, "BipLog, " + UdpClientChannel.this.mRxLen + " data read.");
                                UdpClientChannel.this.mRxPos = 0;
                                int available = 255;
                                if (UdpClientChannel.this.mRxLen < 255) {
                                    available = UdpClientChannel.this.mRxLen;
                                }
                                BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & 255));
                                try {
                                    UdpClientChannel.this.token.wait();
                                } catch (InterruptedException e7) {
                                    e7.printStackTrace();
                                }
                            }
                        }
                    }
                    CatLog.d((Object) this, "UDP Client thread end on channel no: " + UdpClientChannel.this.mChannelSettings.channel);
                } catch (IOException e8) {
                    CatLog.d((Object) this, "OPEN_CHANNEL - UDP Client connection failed: " + e8.getMessage());
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponse(UdpClientChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 0, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
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

        UdpClientChannel() {
        }

        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << 8;
            this.catCmdMsg = cmdMsg;
            CatLog.d((Object) this, "preProcessOpen bufSize = " + this.mChannelSettings.bufSize + ", mImmediateLinkEstablish = " + BipProxy.this.mImmediateLinkEstablish);
            if (this.mChannelSettings.bufSize > BipProxy.UDP_CHANNEL_BUFFER_SIZE) {
                this.result = ResultCode.PRFRMD_WITH_MODIFICATION;
                this.mChannelSettings.bufSize = BipProxy.UDP_CHANNEL_BUFFER_SIZE;
            } else if (this.mChannelSettings.bufSize > 0) {
                this.mRxBuf = new byte[this.mChannelSettings.bufSize];
                this.mTxBuf = new byte[this.mChannelSettings.bufSize];
            } else {
                this.mChannelSettings.bufSize = BipProxy.UDP_CHANNEL_BUFFER_SIZE;
            }
            if (!BipProxy.this.mImmediateLinkEstablish) {
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, 0, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
            }
            return true;
        }

        public boolean open(CatCmdMessage cmdMsg) {
            this.mThread = new UdpClientThread();
            this.mThread.start();
            return true;
        }

        public void close(CatCmdMessage cmdMsg) {
            if (this.mChannelSettings != null) {
                CatLog.d((Object) this, "Update channel status to closed before close socket");
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            CatLog.d((Object) this, "mDatagramSocket = " + this.mDatagramSocket + " mChannelSettings = " + this.mChannelSettings);
            if (!(this.mDatagramSocket == null || (this.mDatagramSocket.isClosed() ^ 1) == 0)) {
                this.mDatagramSocket.close();
            }
            this.mDatagramSocket = null;
            this.mRxPos = 0;
            this.mRxLen = 0;
            this.mTxPos = 0;
            this.mTxLen = 0;
            if (this.mChannelSettings == null) {
                CatLog.d((Object) this, "UdpClientChannel close BIP_ERROR");
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                return;
            }
            this.mChannelStatus = this.mChannelSettings.channel << 8;
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, 0, null);
            BipProxy.this.mStkService.sendBroadcastToOtaUI(BipProxy.this.mStkService.OTA_TYPE, true);
            if (BipProxy.this.mChannelApnInfo[this.mChannelSettings.channel - 1] != null) {
                CatLog.d((Object) this, "UdpClientChannel close");
                BipProxy.this.teardownDataConnection(cmdMsg);
            }
        }

        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == 1 && BipProxy.this.setupDataConnection(BipProxy.this.openChCmdMsg)) {
                CatLog.d((Object) this, "Continue processing open channel");
                if (BipProxy.this.mBipChannels[cmdMsg.getDataSettings().channel - 1].open(BipProxy.this.openChCmdMsg)) {
                    this.mSendThread = new UdpClientSendThread(cmdMsg);
                    this.mSendThread.start();
                } else {
                    BipProxy.this.cleanupBipChannel(cmdMsg.getDataSettings().channel);
                }
                return;
            }
            this.mSendThread = new UdpClientSendThread(cmdMsg);
            this.mSendThread.start();
        }

        public void receive(CatCmdMessage cmdMsg) {
            ResultCode result = ResultCode.OK;
            CatLog.d((Object) this, "RECEIVE_DATA on channel no: " + cmdMsg.getDataSettings().channel);
            int requested = cmdMsg.getDataSettings().length;
            if (requested > 237) {
                requested = 237;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            int available = 255;
            if (this.mRxLen < 255) {
                available = this.mRxLen;
            }
            byte[] data = null;
            if (requested > 0) {
                data = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, data, 0, requested);
                this.mRxPos += requested;
            }
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, result, false, 0, new ReceiveDataResponseData(data, available));
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
        }

        public int getStatus() {
            if (this.mChannelSettings == null) {
                this.mChannelStatus = 0;
            } else if (this.mChannelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << 8;
            }
            return this.mChannelStatus;
        }

        public void setStatus(int status) {
            if (5 == status && (this.mChannelStatus & 32768) == 32768) {
                this.mChannelStatus = (this.mChannelStatus & 32512) | status;
                BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
            }
        }

        public void onSessionEnd() {
            if (this.mThread == null || (this.mThread.isAlive() ^ 1) != 0) {
                this.mThread = new UdpClientThread();
                this.mThread.start();
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues() {
        if (-com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues != null) {
            return -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = 11;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = 12;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.LANGUAGE_NOTIFICATION.ordinal()] = 14;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = 15;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = 3;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CommandType.PLAY_TONE.ordinal()] = 16;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 17;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CommandType.RECEIVE_DATA.ordinal()] = 4;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CommandType.REFRESH.ordinal()] = 18;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CommandType.SELECT_ITEM.ordinal()] = 19;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CommandType.SEND_DATA.ordinal()] = 5;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CommandType.SEND_DTMF.ordinal()] = 20;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CommandType.SEND_SMS.ordinal()] = 21;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CommandType.SEND_SS.ordinal()] = 22;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CommandType.SEND_USSD.ordinal()] = 23;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CommandType.SET_POLL_INTERVALL.ordinal()] = 24;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[CommandType.SET_UP_CALL.ordinal()] = 25;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[CommandType.SET_UP_EVENT_LIST.ordinal()] = 26;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 27;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[CommandType.SET_UP_MENU.ordinal()] = 28;
        } catch (NoSuchFieldError e23) {
        }
        -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues() {
        if (-com-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues != null) {
            return -com-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues;
        }
        int[] iArr = new int[TransportProtocol.values().length];
        try {
            iArr[TransportProtocol.RESERVED.ordinal()] = 11;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TransportProtocol.TCP_CLIENT_LOCAL.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[TransportProtocol.TCP_CLIENT_REMOTE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[TransportProtocol.TCP_SERVER.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[TransportProtocol.UDP_CLIENT_LOCAL.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[TransportProtocol.UDP_CLIENT_REMOTE.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues = iArr;
        return iArr;
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
        for (int i = 0; i < this.mBipChannels.length; i++) {
            if (this.mChannelApnInfo[i] == null) {
                this.mBipChannels[i] = null;
            }
            if (this.mBipChannels[i] == null) {
                CatLog.d((Object) this, "channel index " + i + " found. new channel can be handled");
                return true;
            }
        }
        CatLog.d((Object) this, "new channel can't be handled");
        return false;
    }

    private boolean isBipOverWlanAllowed() {
        return SystemProperties.getBoolean("ro.config.bip_over_wlan", false);
    }

    private boolean isDefaultBearerDescriptionType(CatCmdMessage cmdMsg) {
        boolean z = false;
        if (cmdMsg == null || CommandType.OPEN_CHANNEL != cmdMsg.getCmdType()) {
            return false;
        }
        if (BearerType.DEFAULT_BEARER == cmdMsg.getChannelSettings().bearerDescription.type) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:9:0x0016, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isIPV6Address(CatCmdMessage cmdMsg) {
        if (cmdMsg == null || CommandType.OPEN_CHANNEL != cmdMsg.getCmdType()) {
            return false;
        }
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (newChannel == null || newChannel.destinationAddress == null || 4 == newChannel.destinationAddress.length) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:4:0x000d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isDnsServerAddressRequested(CatCmdMessage cmdMsg) {
        if (cmdMsg != null && CommandType.OPEN_CHANNEL == cmdMsg.getCmdType() && 8 == (cmdMsg.getCommandQualifier() & 8)) {
            return true;
        }
        return false;
    }

    private void updateWifiAvailableFlag(CatCmdMessage cmdMsg) {
        if (cmdMsg == null) {
            CatLog.d((Object) this, "updateWifiAvailableFlag, input param invalid!");
        } else if (!cmdMsg.getWifiConnectedFlag()) {
            CatLog.d((Object) this, "updateWifiAvailableFlag, getWifiConnectedFlag is false, just return!");
        } else if (CommandType.OPEN_CHANNEL != cmdMsg.getCmdType()) {
            CatLog.d((Object) this, "updateWifiAvailableFlag to false, cmdType is " + cmdMsg.getCmdType());
            cmdMsg.setWifiConnectedFlag(false);
        } else if (isDnsServerAddressRequested(cmdMsg)) {
            CatLog.d((Object) this, "updateWifiAvailableFlag to false, DNS server address(es) requested!");
            cmdMsg.setWifiConnectedFlag(false);
        } else if (!isBipOverWlanAllowed()) {
            CatLog.d((Object) this, "updateWifiAvailableFlag to false, isBipOverWlanAllowed is false");
            cmdMsg.setWifiConnectedFlag(false);
        } else if (!isDefaultBearerDescriptionType(cmdMsg)) {
            CatLog.d((Object) this, "updateWifiAvailableFlag to false, not default_bearer!");
            cmdMsg.setWifiConnectedFlag(false);
        } else if (isIPV6Address(cmdMsg)) {
            CatLog.d((Object) this, "updateWifiAvailableFlag to false, IPV6!");
            cmdMsg.setWifiConnectedFlag(false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x010c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleBipCommand(CatCmdMessage cmdMsg) {
        int i;
        if (cmdMsg == null) {
            CatLog.d((Object) this, "handleBipCommand null cmdMsg");
            for (i = 0; i < this.mBipChannels.length; i++) {
                if (this.mBipChannels[i] != null) {
                    CatLog.d((Object) this, "handleBipCommand handle channel " + i + " session end");
                    this.mBipChannels[i].onSessionEnd();
                }
            }
            return;
        }
        CommandType curCmdType = cmdMsg.getCmdType();
        CatLog.d((Object) this, "handleBipCommand curCmdType: " + curCmdType + ", channelSettings: " + cmdMsg.getChannelSettings() + ", cmd_qual: " + cmdMsg.getCommandQualifier() + ", dataSettings: " + cmdMsg.getDataSettings());
        updateWifiAvailableFlag(cmdMsg);
        switch (-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[curCmdType.ordinal()]) {
            case 1:
            case 4:
            case 5:
                if (cmdMsg.getDataSettings() != null) {
                    try {
                        BipChannel curChannel = this.mBipChannels[cmdMsg.getDataSettings().channel - 1];
                        if (curChannel == null) {
                            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                            CatLog.d((Object) this, "handleBipCommand, There is not open channel");
                            return;
                        } else if (CommandType.SEND_DATA == curCmdType) {
                            curChannel.send(cmdMsg);
                            return;
                        } else if (CommandType.RECEIVE_DATA == curCmdType) {
                            curChannel.receive(cmdMsg);
                            return;
                        } else if (CommandType.CLOSE_CHANNEL == curCmdType) {
                            clearWakeLock();
                            curChannel.close(cmdMsg);
                            cleanupBipChannel(cmdMsg.getDataSettings().channel);
                            return;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                        CatLog.d((Object) this, "handleBipCommand error");
                        return;
                    }
                }
                break;
            case 2:
                int[] status = new int[7];
                for (i = 0; i < 7; i++) {
                    if (this.mBipChannels[i] != null) {
                        status[i] = this.mBipChannels[i].getStatus();
                    } else {
                        status[i] = 0;
                    }
                    CatLog.d((Object) this, "get channel status = " + status[i]);
                }
                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, 0, new ChannelStatusResponseData(status));
                return;
            case 3:
                ChannelSettings channelSettings = cmdMsg.getChannelSettings();
                if (channelSettings != null) {
                    acquireWakeLock();
                    if (allChannelsClosed()) {
                        this.mDefaultBearerStateReceiver.startListening();
                    }
                    for (i = 0; i < this.mBipChannels.length; i++) {
                        if (this.mBipChannels[i] == null) {
                            CatLog.d((Object) this, "mBipChannels " + i + " is available");
                            channelSettings.channel = i + 1;
                            if (channelSettings.channel != 0) {
                                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, 1, null);
                                return;
                            }
                            switch (-getcom-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues()[channelSettings.protocol.ordinal()]) {
                                case 1:
                                case 2:
                                    this.mBipChannels[channelSettings.channel - 1] = new TcpClientChannel();
                                    break;
                                case 3:
                                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                                    return;
                                case 4:
                                case 5:
                                    this.mBipChannels[channelSettings.channel - 1] = new UdpClientChannel();
                                    break;
                                default:
                                    CatLog.d((Object) this, "invalid protocol found");
                                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                                    return;
                            }
                            if ((cmdMsg.getCommandQualifier() & 1) == 0) {
                                this.mImmediateLinkEstablish = false;
                                this.openChCmdMsg = cmdMsg;
                                this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                            } else {
                                this.mImmediateLinkEstablish = true;
                                if (setupDataConnection(cmdMsg)) {
                                    CatLog.d((Object) this, "Continue processing open channel");
                                    this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                                    if (!this.mBipChannels[channelSettings.channel - 1].open(cmdMsg)) {
                                        CatLog.d((Object) this, "open channel failed");
                                        cleanupBipChannel(channelSettings.channel);
                                    }
                                } else {
                                    CatLog.d((Object) this, "handleBipCommand :setupDataConnection returned");
                                }
                            }
                            return;
                        }
                    }
                    if (channelSettings.channel != 0) {
                    }
                }
                break;
        }
        this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
    }

    private boolean allChannelsClosed() {
        for (BipChannel channel : this.mBipChannels) {
            if (channel != null) {
                CatLog.d((Object) this, "not all Channels Closed");
                return false;
            }
        }
        CatLog.d((Object) this, "all Channels Closed");
        return true;
    }

    private void cleanupBipChannel(int channel) {
        this.mBipChannels[channel - 1] = null;
        if (allChannelsClosed()) {
            this.mDefaultBearerStateReceiver.stopListening();
        }
    }

    private void sendChannelStatusEvent(int channelStatus) {
        byte[] additionalInfo = new byte[]{(byte) -72, (byte) 2, (byte) 0, (byte) 0};
        additionalInfo[2] = (byte) ((channelStatus >> 8) & 255);
        additionalInfo[3] = (byte) (channelStatus & 255);
        CatLog.d((Object) this, "sendChannelStatusEvent channelStatus = " + channelStatus);
        this.mStkService.onEventDownload(new CatEventMessage(EventCode.CHANNEL_STATUS.value(), additionalInfo, true));
    }

    private void sendDataAvailableEvent(int channelStatus, int dataAvailable) {
        byte[] additionalInfo = new byte[]{(byte) -72, (byte) 2, (byte) 0, (byte) 0, (byte) -73, (byte) 1, (byte) 0};
        additionalInfo[2] = (byte) ((channelStatus >> 8) & 255);
        additionalInfo[3] = (byte) (channelStatus & 255);
        additionalInfo[6] = (byte) (dataAvailable & 255);
        CatLog.d((Object) this, "sendDataAvailableEvent channelStatus = " + channelStatus + " dataAvailable = " + dataAvailable);
        this.mStkService.onEventDownload(new CatEventMessage(EventCode.DATA_AVAILABLE.value(), additionalInfo, true));
    }

    private boolean checkExistingCsCallInNetworkClass2G() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        int networkType = tm.getNetworkType();
        int networkClass = TelephonyManager.getNetworkClass(networkType);
        if ((1 != networkClass && networkClass != 0) || tm.getCallState() == 0) {
            return false;
        }
        CatLog.d((Object) this, "Bearer not setup, busy on voice call, networkClass = " + networkClass + " networkType = " + networkType);
        return true;
    }

    private String getLguPlusOtaApn() {
        return SystemProperties.get("ro.config.lgu_plus_ota_apn", "ota.lguplus.co.kr");
    }

    private boolean isLguPlusOtaEnable() {
        return SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false);
    }

    private String formatDefaultApn(CatCmdMessage cmdMsg) {
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        CatLog.d((Object) this, "formatDefaultApn, mStkService.OTA_TYPE:" + this.mStkService.OTA_TYPE);
        if (!isLguPlusOtaEnable()) {
            return "default";
        }
        if (this.mStkService.OTA_TYPE != 0) {
            return "default";
        }
        if (newChannel.networkAccessName == null) {
            newChannel.networkAccessName = getLguPlusOtaApn();
        }
        if (newChannel.userLogin == null) {
            newChannel.userLogin = "";
        }
        if (newChannel.userPassword == null) {
            newChannel.userPassword = "";
        }
        String apnString = "bipapn, " + newChannel.networkAccessName + ", ," + String.valueOf(newChannel.port) + ", " + newChannel.userLogin + ", " + newChannel.userPassword + ", , , , , , ,3 , " + this.mChannelApnInfo[newChannel.channel - 1].type;
        CatLog.d((Object) this, "formatDefaultApn, apnString:" + apnString);
        return apnString;
    }

    private boolean setupDefaultDataConnection(CatCmdMessage cmdMsg) throws ConnectionSetupFailedException {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (checkExistingCsCallInNetworkClass2G()) {
            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 2, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
            throw new ConnectionSetupFailedException("Busy on voice call");
        }
        this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
        this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
        SystemProperties.set("gsm.bip.apn", formatDefaultApn(cmdMsg));
        CatLog.d((Object) this, "setupDefaultDataConnection");
        NetworkRequest request = new Builder().addTransportType(0).addCapability(getBipCapability(this.mChannelApnInfo[newChannel.channel - 1].feature)).build();
        this.mChannelApnInfo[newChannel.channel - 1].networkCallback = new BipNetworkCallback(this.mChannelApnInfo[newChannel.channel - 1].networkType, cmdMsg);
        int mSlotId = cmdMsg.getSlotId();
        CatLog.d((Object) this, "setupDefaultDataConnection  mSlotId = " + mSlotId);
        if (mSlotId > -1 && mSlotId < SIM_NUM) {
            String subid = String.valueOf(mSlotId);
            request.networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(subid));
            CatLog.d((Object) this, "setupDefaultDataConnection  mSlotId = " + mSlotId + " subid = " + subid);
        }
        cm.requestNetwork(request, this.mChannelApnInfo[newChannel.channel - 1].networkCallback);
        startDataConnectionTimer(cmdMsg);
        return false;
    }

    private boolean setupSpecificPdpConnection(CatCmdMessage cmdMsg) throws ConnectionSetupFailedException {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (newChannel.networkAccessName == null) {
            CatLog.d((Object) this, "no accessname for PS bearer req");
            return setupDefaultDataConnection(cmdMsg);
        } else if (checkExistingCsCallInNetworkClass2G()) {
            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 2, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
            throw new ConnectionSetupFailedException("Busy on voice call");
        } else {
            String apnstring;
            CatLog.d((Object) this, "Detected new data connection parameters");
            boolean userbipSetting = this.mHwCustBipProxy != null ? this.mHwCustBipProxy.kddiBipOtaEnable() : false;
            if (newChannel.userLogin == null) {
                newChannel.userLogin = userbipSetting ? "au" : "";
            }
            if (newChannel.userPassword == null) {
                newChannel.userPassword = userbipSetting ? "au" : "";
            }
            this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
            this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
            if (userbipSetting) {
                apnstring = this.mHwCustBipProxy.getApnString(newChannel, this.mChannelApnInfo[newChannel.channel - 1].type);
            } else {
                apnstring = getApnString(newChannel);
            }
            SystemProperties.set("gsm.bip.apn", apnstring);
            CatLog.d((Object) this, "setupSpecificPdpConnection");
            NetworkRequest request = new Builder().addTransportType(0).addCapability(getBipCapability(this.mChannelApnInfo[newChannel.channel - 1].feature)).build();
            this.mChannelApnInfo[newChannel.channel - 1].networkCallback = new BipNetworkCallback(this.mChannelApnInfo[newChannel.channel - 1].networkType, cmdMsg);
            cm.requestNetwork(request, this.mChannelApnInfo[newChannel.channel - 1].networkCallback);
            startDataConnectionTimer(cmdMsg);
            return false;
        }
    }

    public String getApnString(ChannelSettings newChannel) {
        StringBuilder apnstring = new StringBuilder("bipapn, ");
        apnstring.append(newChannel.networkAccessName);
        apnstring.append(", ,");
        apnstring.append(String.valueOf(newChannel.port));
        apnstring.append(", ");
        apnstring.append(newChannel.userLogin);
        apnstring.append(", ");
        apnstring.append(newChannel.userPassword);
        apnstring.append(", , , , , , ,3 , ");
        apnstring.append(this.mChannelApnInfo[newChannel.channel - 1].type);
        return apnstring.toString();
    }

    private int getBipCapability(String feature) {
        CatLog.d((Object) this, "feature: " + feature);
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP0.equals(feature)) {
            return 19;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP1.equals(feature)) {
            return 20;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP2.equals(feature)) {
            return 21;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP3.equals(feature)) {
            return 22;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP4.equals(feature)) {
            return 23;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP5.equals(feature)) {
            return 24;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP6.equals(feature)) {
            return 25;
        }
        return 19;
    }

    private int getChannelId(CatCmdMessage cmdMsg) {
        int channel = 0;
        if (cmdMsg.getCmdType() == CommandType.OPEN_CHANNEL) {
            channel = cmdMsg.getChannelSettings().channel;
        } else if (cmdMsg.getCmdType() == CommandType.CLOSE_CHANNEL || cmdMsg.getCmdType() == CommandType.RECEIVE_DATA || cmdMsg.getCmdType() == CommandType.SEND_DATA) {
            channel = cmdMsg.getDataSettings().channel;
        }
        CatLog.d((Object) this, "getChannelId:" + channel);
        return channel;
    }

    private synchronized void checkSetStatusOrNot(CatCmdMessage cmdMsg) {
        int channel = getChannelId(cmdMsg);
        int index = channel - 1;
        BipChannel tempChannel = this.mBipChannels[index];
        if (channel <= 0 || channel > 7) {
            CatLog.d((Object) this, "checkSetStatusOrNot, channel_id" + index + "is invalid, just return");
        } else if (tempChannel == null) {
            CatLog.d((Object) this, "checkSetStatusOrNot, mBipChannel[" + index + "] is null, just return");
        } else if (5 == (tempChannel.getStatus() & 255)) {
            CatLog.d((Object) this, "checkSetStatusOrNot, mBipChannel[" + index + "] already link droped, just return");
        } else {
            CatLog.d((Object) this, "checkSetStatusOrNot, mBipChannel[" + index + "] CH_STATUS_LINK_DROP");
            tempChannel.setStatus(5);
        }
    }

    private boolean setupDataConnection(CatCmdMessage cmdMsg) {
        boolean result = false;
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (newChannel.protocol == TransportProtocol.TCP_CLIENT_REMOTE || newChannel.protocol == TransportProtocol.UDP_CLIENT_REMOTE) {
            BearerDescription bd = newChannel.bearerDescription;
            CatLog.d((Object) this, "bd.type = " + bd.type + ", isWifiConnectedFlag = " + cmdMsg.getWifiConnectedFlag());
            if (cmdMsg.getWifiConnectedFlag()) {
                this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
                this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
                return true;
            }
            try {
                if (BearerType.DEFAULT_BEARER == bd.type) {
                    result = (this.mHwCustBipProxy == null || !this.mHwCustBipProxy.kddiBipOtaEnable()) ? setupDefaultDataConnection(cmdMsg) : setupSpecificPdpConnection(cmdMsg);
                } else if (BearerType.MOBILE_PS == bd.type || BearerType.MOBILE_PS_EXTENDED_QOS == bd.type || BearerType.E_UTRAN == bd.type) {
                    result = setupSpecificPdpConnection(cmdMsg);
                } else {
                    CatLog.d((Object) this, "Unsupported bearer type");
                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                }
            } catch (ConnectionSetupFailedException csfe) {
                CatLog.d((Object) this, "setupDataConnection failed: " + csfe.getMessage());
                this.mBipChannels[newChannel.channel - 1] = null;
                cleanupBipChannel(newChannel.channel);
            }
            return result;
        }
        CatLog.d((Object) this, "No data connection needed for this channel");
        return true;
    }

    private synchronized boolean teardownDataConnection(CatCmdMessage cmdMsg) {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        int channel = getChannelId(cmdMsg);
        if (channel <= 0 || channel > 7) {
            CatLog.d((Object) this, "teardownDataConnection, channel_id" + channel + "is invalid, just return");
            return false;
        }
        CatLog.d((Object) this, "teardownDataConnection channel = " + channel);
        if (this.mChannelApnInfo[channel - 1] == null) {
            CatLog.d((Object) this, "teardownDataConnection mChannelApnInfo[" + (channel - 1) + "] is null");
            return false;
        }
        if (!cmdMsg.getWifiConnectedFlag()) {
            CatLog.d((Object) this, "teardownDataConnection begin");
            if (!(this.mChannelApnInfo[channel - 1] == null || this.mChannelApnInfo[channel - 1].networkCallback == null)) {
                cm.unregisterNetworkCallback(this.mChannelApnInfo[channel - 1].networkCallback);
                this.mChannelApnInfo[channel - 1].networkCallback = null;
                CatLog.d((Object) this, "unregisterNetworkCallback");
            }
            CatLog.d((Object) this, "teardownDataConnection end");
        }
        cleanChannelApnInfo(channel);
        return true;
    }

    private void onSetupConnectionCompleted(AsyncResult ar) {
        if (ar == null) {
            CatLog.d((Object) this, "onSetupConnectionCompleted ar null");
            return;
        }
        CatCmdMessage cmdMsg = ar.userObj;
        if (ar.exception != null) {
            CatLog.d((Object) this, "Failed to setup data connection for channel: " + cmdMsg.getChannelSettings().channel);
            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS, false, 0, new OpenChannelResponseData(cmdMsg.getChannelSettings().bufSize, null, cmdMsg.getChannelSettings().bearerDescription));
            cleanupBipChannel(cmdMsg.getChannelSettings().channel);
            cleanChannelApnInfo(cmdMsg.getChannelSettings().channel);
        } else {
            CatLog.d((Object) this, "setup data connection for channel: " + cmdMsg.getChannelSettings().channel);
            if (this.mChannelApnInfo[cmdMsg.getChannelSettings().channel - 1] == null || this.mChannelApnInfo[cmdMsg.getChannelSettings().channel - 1].networkType == 0) {
                CatLog.d((Object) this, "Succeeded to setup data connection for channel - Default bearer");
            }
            CatLog.d((Object) this, "Continue processing open channel");
            this.mBipChannels[cmdMsg.getChannelSettings().channel - 1].preProcessOpen(cmdMsg);
            if (!this.mBipChannels[cmdMsg.getChannelSettings().channel - 1].open(cmdMsg)) {
                CatLog.d((Object) this, "fail to open channel");
                cleanupBipChannel(cmdMsg.getChannelSettings().channel);
            }
        }
    }

    private void onTeardownConnectionCompleted(AsyncResult ar) {
        if (ar == null) {
            CatLog.d((Object) this, "onTeardownConnectionCompleted ar null");
            return;
        }
        int channel;
        CatCmdMessage cmdMsg = ar.userObj;
        if (cmdMsg.getCmdType() == CommandType.OPEN_CHANNEL) {
            channel = cmdMsg.getChannelSettings().channel;
        } else if (cmdMsg.getCmdType() == CommandType.CLOSE_CHANNEL) {
            channel = cmdMsg.getDataSettings().channel;
        } else {
            return;
        }
        if (ar.exception != null) {
            CatLog.d((Object) this, "Failed to teardown data connection for channel " + channel + ": " + ar.exception.getMessage());
        } else {
            CatLog.d((Object) this, "Succedded to teardown data connection for channel: " + channel);
            for (int i = 0; i < 7; i++) {
                if (this.mBipChannels[i] != null) {
                    CatLog.d((Object) this, "channel " + i + " link drop");
                    this.mBipChannels[i].setStatus(5);
                }
            }
        }
        cleanupBipChannel(channel);
    }

    private void startDataConnectionTimer(CatCmdMessage cmdMsg) {
        cancelDataConnectionTimer();
        CatLog.d((Object) this, "startDataConnectionTimer.");
        sendMessageDelayed(obtainMessage(100, cmdMsg), 180000);
    }

    private void cancelDataConnectionTimer() {
        CatLog.d((Object) this, "cancelDataConnectionTimer.");
        removeMessages(100);
    }

    private void acquireWakeLock() {
        synchronized (this.mWakeLock) {
            CatLog.d((Object) this, "acquireWakeLock.");
            this.mWakeLock.acquire();
            removeMessages(99);
            sendMessageDelayed(obtainMessage(99), (long) this.mWakeLockTimeout);
        }
    }

    private void clearWakeLock() {
        synchronized (this.mWakeLock) {
            if (this.mWakeLock.isHeld()) {
                CatLog.d((Object) this, "clearWakeLock.");
                this.mWakeLock.release();
                removeMessages(99);
            }
        }
    }

    public void handleMessage(Message msg) {
        CatLog.d((Object) this, "handleMessage: " + msg.what);
        switch (msg.what) {
            case 10:
                cancelDataConnectionTimer();
                if (msg.obj != null) {
                    onSetupConnectionCompleted((AsyncResult) msg.obj);
                    return;
                }
                return;
            case 11:
                if (msg.obj != null) {
                    onTeardownConnectionCompleted((AsyncResult) msg.obj);
                    return;
                }
                return;
            case 99:
                clearWakeLock();
                return;
            case 100:
                CatCmdMessage cmdMsg = msg.obj;
                CatLog.d((Object) this, "EVENT_DC_TIMEOUT teardownDataConnection");
                teardownDataConnection(cmdMsg);
                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                return;
            default:
                throw new AssertionError("Unrecognized message: " + msg.what);
        }
    }

    public void cleanChannelApnInfo(int channel) {
        CatLog.d((Object) this, "cleanChannelApnInfo, channel: " + channel);
        this.mChannelApnInfo[channel - 1] = null;
    }
}
