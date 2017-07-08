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
import com.google.android.mms.pdu.PduHeaders;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
    static final int MAX_CHANNEL_NUM = 7;
    static final int MAX_LEN_OF_CHANNEL_DATA = 237;
    static final int MAX_TCP_SERVER_CHANNEL_NUM = 3;
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
    private BipChannel[] mBipChannels;
    private ChannelApnInfo[] mChannelApnInfo;
    private Context mContext;
    private DefaultBearerStateReceiver mDefaultBearerStateReceiver;
    boolean mImmediateLinkEstablish;
    protected boolean[] mIsWifiConnected;
    private ServerChannel[] mServerChannels;
    private CatService mStkService;
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
        Network mCurrentNetwork;
        private int mNetworkType;

        BipNetworkCallback(int networkType, CatCmdMessage cmdMsg) {
            this.mNetworkType = networkType;
            this.mCmdMsg = cmdMsg;
            this.mCurrentNetwork = null;
        }

        public void onAvailable(Network network) {
            this.mCurrentNetwork = network;
            CatLog.d((Object) this, "onAvailable got Network: " + network);
            CatLog.d((Object) this, "MSG_ID_SETUP_DATA_CALL");
            Message msg = BipProxy.this.obtainMessage(BipProxy.MSG_ID_SETUP_DATA_CALL, BipProxy.this.mChannelApnInfo[this.mNetworkType - 38].bakCmdMsg);
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
                BipProxy.this.mStkService.sendTerminalResponse(this.mCmdMsg.mCmdDet, rc, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
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
                case BipProxy.NO_CHANNEL_AVAILABLE /*1*/:
                    this.networkType = 38;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP0;
                    this.type = "bip0";
                    break;
                case BipProxy.CMD_QUAL_AUTO_RECONN /*2*/:
                    this.networkType = 39;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP1;
                    this.type = "bip1";
                    break;
                case BipProxy.MAX_TCP_SERVER_CHANNEL_NUM /*3*/:
                    this.networkType = 40;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP2;
                    this.type = "bip2";
                    break;
                case BipProxy.REQUESTED_BUFFER_SIZE_NOT_AVAILABLE /*4*/:
                    this.networkType = 41;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP3;
                    this.type = "bip3";
                    break;
                case BipProxy.SECURITY_ERROR /*5*/:
                    this.networkType = 42;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP4;
                    this.type = "bip4";
                    break;
                case BipProxy.REQUESTED_SIM_ME_INTERFACE_TRANSPORT_LEVEL_NOT_AVAILABLE /*6*/:
                    this.networkType = 43;
                    this.feature = AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP5;
                    this.type = "bip5";
                    break;
                case BipProxy.MAX_CHANNEL_NUM /*7*/:
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
        IntentFilter mFilter;
        boolean mIsRegistered;

        public DefaultBearerStateReceiver(Context context) {
            this.mContext = context;
            this.mFilter = new IntentFilter();
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
                for (int i = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < BipProxy.MAX_CHANNEL_NUM; i += BipProxy.NO_CHANNEL_AVAILABLE) {
                    if (BipProxy.this.mBipChannels[i] != null) {
                        CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] Status is " + (BipProxy.this.mBipChannels[i].getStatus() & PduHeaders.STORE_STATUS_ERROR_END));
                        if (BipProxy.SECURITY_ERROR == (BipProxy.this.mBipChannels[i].getStatus() & PduHeaders.STORE_STATUS_ERROR_END)) {
                            CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] already link droped");
                        } else if (BipProxy.this.mIsWifiConnected[i]) {
                            BipProxy.this.mBipChannels[i].setStatus(BipProxy.SECURITY_ERROR);
                            CatLog.d((Object) this, "handleWifiDisconnectedMsg: mBipChannels[" + i + "] CH_STATUS_LINK_DROP");
                            BipProxy.this.cleanChannelApnInfo(i + BipProxy.NO_CHANNEL_AVAILABLE);
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
                    boolean isConnected = networkInfo != null ? networkInfo.isConnected() : false;
                    CatLog.d((Object) this, "WifiManager.NETWORK_STATE_CHANGED_ACTION: IsWifiConnected = " + isConnected);
                    handleWifiDisconnectedMsg(isConnected);
                }
            }
        }
    }

    static class ServerChannel {
        ServerSocket mServerSocket;
        int port;

        ServerChannel() {
            this.port = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
        }
    }

    class TcpClientChannel implements BipChannel {
        CatCmdMessage catCmdMsg;
        ChannelSettings mChannelSettings;
        int mChannelStatus;
        byte[] mRxBuf;
        int mRxLen;
        int mRxPos;
        TcpClientSendThread mSendThread;
        Socket mSocket;
        TcpClientThread mThread;
        byte[] mTxBuf;
        int mTxLen;
        int mTxPos;
        ResultCode result;
        private Object token;

        class TcpClientSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public TcpClientSendThread(CatCmdMessage cmdMsg) {
                this.cmdMsg = cmdMsg;
            }

            public void run() {
                DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d((Object) this, "SEND_DATA on channel no: " + dataSettings.channel + " Transfer data into tx buffer");
                for (int i = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < dataSettings.data.length && TcpClientChannel.this.mTxPos < TcpClientChannel.this.mTxBuf.length; i += BipProxy.NO_CHANNEL_AVAILABLE) {
                    byte[] bArr = TcpClientChannel.this.mTxBuf;
                    TcpClientChannel tcpClientChannel = TcpClientChannel.this;
                    int i2 = tcpClientChannel.mTxPos;
                    tcpClientChannel.mTxPos = i2 + BipProxy.NO_CHANNEL_AVAILABLE;
                    bArr[i2] = dataSettings.data[i];
                }
                TcpClientChannel tcpClientChannel2 = TcpClientChannel.this;
                tcpClientChannel2.mTxLen += dataSettings.data.length;
                CatLog.d((Object) this, "Tx buffer now contains " + TcpClientChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == BipProxy.NO_CHANNEL_AVAILABLE) {
                    TcpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    int len = TcpClientChannel.this.mTxLen;
                    TcpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    CatLog.d((Object) this, "Sent data to socket " + len + " bytes.");
                    if (TcpClientChannel.this.mSocket == null) {
                        CatLog.d((Object) this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    }
                    try {
                        TcpClientChannel.this.mSocket.getOutputStream().write(TcpClientChannel.this.mTxBuf, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, len);
                        CatLog.d((Object) this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e) {
                        CatLog.d((Object) this, "IOException " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    }
                }
                int avail = 238;
                if (TcpClientChannel.this.mChannelSettings != null) {
                    avail = TcpClientChannel.this.mChannelSettings.bufSize - TcpClientChannel.this.mTxLen;
                    if (avail > PduHeaders.STORE_STATUS_ERROR_END) {
                        avail = PduHeaders.STORE_STATUS_ERROR_END;
                    }
                }
                CatLog.d((Object) this, "TR with " + avail + " bytes available in Tx Buffer on channel " + dataSettings.channel);
                BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(avail));
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
                    TcpClientChannel.this.mChannelStatus = (TcpClientChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.CHANNEL_STATUS_AVAILABLE;
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponse(TcpClientChannel.this.catCmdMsg.mCmdDet, TcpClientChannel.this.result, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(TcpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpClientChannel.this.mChannelStatus), TcpClientChannel.this.mChannelSettings.bearerDescription));
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
                            TcpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            TcpClientChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            TcpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            TcpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            if (BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1] != null) {
                                CatLog.d((Object) this, "TcpClientThread Exception happened");
                                BipProxy.this.teardownDataConnection(TcpClientChannel.this.catCmdMsg);
                                BipProxy.this.checkSetStatusOrNot(TcpClientChannel.this.catCmdMsg);
                            }
                        }
                        synchronized (TcpClientChannel.this.token) {
                            if (TcpClientChannel.this.mRxLen > 0) {
                                CatLog.d((Object) this, "BipLog, " + TcpClientChannel.this.mRxLen + " data read.");
                                TcpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                int available = PduHeaders.STORE_STATUS_ERROR_END;
                                if (TcpClientChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                    available = TcpClientChannel.this.mRxLen;
                                }
                                BipProxy.this.sendDataAvailableEvent(TcpClientChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
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
                        BipProxy.this.mStkService.sendTerminalResponse(TcpClientChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(TcpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpClientChannel.this.mChannelStatus), TcpClientChannel.this.mChannelSettings.bearerDescription));
                    } else {
                        BipProxy.this.mImmediateLinkEstablish = true;
                    }
                    if (BipProxy.this.mChannelApnInfo[TcpClientChannel.this.mChannelSettings.channel - 1] != null) {
                        BipProxy.this.teardownDataConnection(TcpClientChannel.this.catCmdMsg);
                    }
                }
            }
        }

        TcpClientChannel() {
            this.mChannelSettings = null;
            this.mChannelStatus = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mThread = null;
            this.mSendThread = null;
            this.token = new Object();
            this.mRxBuf = new byte[BipProxy.TCP_CHANNEL_BUFFER_SIZE];
            this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxBuf = new byte[BipProxy.TCP_CHANNEL_BUFFER_SIZE];
            this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.result = ResultCode.OK;
            this.catCmdMsg = null;
        }

        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            this.catCmdMsg = cmdMsg;
            CatLog.d((Object) this, "preProcessOpen bufSize = " + this.mChannelSettings.bufSize + " mImmediateLinkEstablish = " + BipProxy.this.mImmediateLinkEstablish);
            if (this.mChannelSettings.bufSize > BipProxy.TCP_CHANNEL_BUFFER_SIZE) {
                this.result = ResultCode.PRFRMD_WITH_MODIFICATION;
                this.mChannelSettings.bufSize = BipProxy.TCP_CHANNEL_BUFFER_SIZE;
            } else {
                this.mRxBuf = new byte[this.mChannelSettings.bufSize];
                this.mTxBuf = new byte[this.mChannelSettings.bufSize];
            }
            if (!BipProxy.this.mImmediateLinkEstablish) {
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
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
                this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            }
            CatLog.d((Object) this, "mSocket = " + this.mSocket);
            if (!(this.mSocket == null || this.mSocket.isClosed())) {
                try {
                    this.mSocket.close();
                } catch (IOException e) {
                }
            }
            this.mSocket = null;
            this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            if (this.mChannelSettings == null) {
                CatLog.d((Object) this, "TcpClientChannel close BIP_ERROR");
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.MAX_TCP_SERVER_CHANNEL_NUM, null);
                return;
            }
            this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
            if (BipProxy.this.mChannelApnInfo[this.mChannelSettings.channel - 1] != null) {
                BipProxy.this.teardownDataConnection(cmdMsg);
            }
        }

        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == BipProxy.NO_CHANNEL_AVAILABLE && BipProxy.this.setupDataConnection(BipProxy.this.openChCmdMsg)) {
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
            if (requested > BipProxy.MAX_LEN_OF_CHANNEL_DATA) {
                requested = BipProxy.MAX_LEN_OF_CHANNEL_DATA;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            int available = PduHeaders.STORE_STATUS_ERROR_END;
            if (this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                available = this.mRxLen;
            }
            byte[] bArr = null;
            if (requested > 0) {
                bArr = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, bArr, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, requested);
                this.mRxPos += requested;
            }
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, result, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new ReceiveDataResponseData(bArr, available));
            CatLog.d((Object) this, "Receive Data, available data is: " + available);
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
        }

        public int getStatus() {
            if (this.mChannelSettings == null) {
                this.mChannelStatus = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            } else if (this.mChannelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            }
            return this.mChannelStatus;
        }

        public void setStatus(int status) {
            if (BipProxy.SECURITY_ERROR == status && (this.mChannelStatus & BipProxy.CHANNEL_STATUS_AVAILABLE) == BipProxy.CHANNEL_STATUS_AVAILABLE) {
                this.mChannelStatus = (this.mChannelStatus & 32512) | status;
                BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
            }
        }

        public void onSessionEnd() {
            if (this.mThread == null || !this.mThread.isAlive()) {
                this.mThread = new TcpClientThread();
                this.mThread.start();
            }
        }
    }

    class TcpServerChannel implements BipChannel {
        CatCmdMessage catCmdMsg;
        int index;
        ChannelSettings mChannelSettings;
        int mChannelStatus;
        byte[] mRxBuf;
        int mRxLen;
        int mRxPos;
        TcpServerSendThread mSendThread;
        Socket mSocket;
        TcpServerThread mThread;
        byte[] mTxBuf;
        int mTxLen;
        int mTxPos;
        ResultCode result;
        private Object token;

        class TcpServerSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public TcpServerSendThread(CatCmdMessage cmdMsg) {
                this.cmdMsg = cmdMsg;
            }

            public void run() {
                DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d((Object) this, "SEND_DATA on channel no: " + dataSettings.channel);
                for (int i = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < dataSettings.data.length && TcpServerChannel.this.mTxPos < TcpServerChannel.this.mTxBuf.length; i += BipProxy.NO_CHANNEL_AVAILABLE) {
                    byte[] bArr = TcpServerChannel.this.mTxBuf;
                    TcpServerChannel tcpServerChannel = TcpServerChannel.this;
                    int i2 = tcpServerChannel.mTxPos;
                    tcpServerChannel.mTxPos = i2 + BipProxy.NO_CHANNEL_AVAILABLE;
                    bArr[i2] = dataSettings.data[i];
                }
                TcpServerChannel tcpServerChannel2 = TcpServerChannel.this;
                tcpServerChannel2.mTxLen += dataSettings.data.length;
                CatLog.d((Object) this, "Tx buffer now contains " + TcpServerChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == BipProxy.NO_CHANNEL_AVAILABLE) {
                    TcpServerChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    int len = TcpServerChannel.this.mTxLen;
                    TcpServerChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    CatLog.d((Object) this, "Sent data to socket " + len + " bytes.");
                    if (TcpServerChannel.this.mSocket == null) {
                        CatLog.d((Object) this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    }
                    try {
                        TcpServerChannel.this.mSocket.getOutputStream().write(TcpServerChannel.this.mTxBuf, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, len);
                        CatLog.d((Object) this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e) {
                        CatLog.d((Object) this, "IOException " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    }
                }
                int avail = 238;
                if (TcpServerChannel.this.mChannelSettings != null) {
                    avail = TcpServerChannel.this.mChannelSettings.bufSize - TcpServerChannel.this.mTxLen;
                    if (avail > PduHeaders.STORE_STATUS_ERROR_END) {
                        avail = PduHeaders.STORE_STATUS_ERROR_END;
                    }
                }
                BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(avail));
            }
        }

        class TcpServerThread extends Thread {
            TcpServerThread() {
            }

            public void run() {
                CatLog.d((Object) this, "Tcp Server thread start on channel no: " + TcpServerChannel.this.mChannelSettings.channel);
                int i = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                while (i < BipProxy.MAX_TCP_SERVER_CHANNEL_NUM) {
                    try {
                        if (-1 == TcpServerChannel.this.index && BipProxy.this.mServerChannels[i] == null) {
                            TcpServerChannel.this.index = i;
                            CatLog.d((Object) this, "find first null server channel index = " + TcpServerChannel.this.index);
                        }
                        if (BipProxy.this.mServerChannels[i] != null && BipProxy.this.mServerChannels[i].port == TcpServerChannel.this.mChannelSettings.port) {
                            TcpServerChannel.this.index = i;
                            CatLog.d((Object) this, "find same port server channel index = " + TcpServerChannel.this.index);
                            break;
                        }
                        i += BipProxy.NO_CHANNEL_AVAILABLE;
                    } catch (IOException e) {
                        if (BipProxy.this.mImmediateLinkEstablish) {
                            BipProxy.this.mStkService.sendTerminalResponse(TcpServerChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(TcpServerChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpServerChannel.this.mChannelStatus), TcpServerChannel.this.mChannelSettings.bearerDescription));
                        } else {
                            BipProxy.this.mImmediateLinkEstablish = true;
                        }
                        CatLog.d((Object) this, "IOException " + e.getMessage());
                        return;
                    }
                }
                CatLog.d((Object) this, "index = " + TcpServerChannel.this.index + " i = " + i);
                if (BipProxy.MAX_TCP_SERVER_CHANNEL_NUM == i && -1 == TcpServerChannel.this.index) {
                    BipProxy.this.mStkService.sendTerminalResponse(TcpServerChannel.this.catCmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(TcpServerChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpServerChannel.this.mChannelStatus), TcpServerChannel.this.mChannelSettings.bearerDescription));
                } else if (BipProxy.MAX_TCP_SERVER_CHANNEL_NUM == i) {
                    BipProxy.this.mServerChannels[TcpServerChannel.this.index] = new ServerChannel();
                    BipProxy.this.mServerChannels[TcpServerChannel.this.index].mServerSocket = new ServerSocket(TcpServerChannel.this.mChannelSettings.port);
                }
                CatLog.d((Object) this, "Open server socket on port " + TcpServerChannel.this.mChannelSettings.port + " for channel " + TcpServerChannel.this.mChannelSettings.channel);
                TcpServerChannel.this.mChannelStatus = (TcpServerChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.TCP_CHANNEL_BUFFER_SIZE;
                if (BipProxy.this.mImmediateLinkEstablish) {
                    BipProxy.this.mStkService.sendTerminalResponse(TcpServerChannel.this.catCmdMsg.mCmdDet, TcpServerChannel.this.result, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(TcpServerChannel.this.mChannelSettings.bufSize, Integer.valueOf(TcpServerChannel.this.mChannelStatus), TcpServerChannel.this.mChannelSettings.bearerDescription));
                } else {
                    BipProxy.this.mImmediateLinkEstablish = true;
                }
                if (TcpServerChannel.this.mSocket == null || TcpServerChannel.this.mSocket.isClosed()) {
                    TcpServerChannel.this.mChannelStatus = (TcpServerChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.TCP_CHANNEL_BUFFER_SIZE;
                    BipProxy.this.sendChannelStatusEvent(TcpServerChannel.this.mChannelStatus);
                    try {
                        CatLog.d((Object) this, "Wait for connection");
                        TcpServerChannel.this.mSocket = BipProxy.this.mServerChannels[TcpServerChannel.this.index].mServerSocket.accept();
                        CatLog.d((Object) this, "New connection mSocket = " + TcpServerChannel.this.mSocket);
                        if (TcpServerChannel.this.mSocket.isConnected()) {
                            TcpServerChannel.this.mChannelStatus = (TcpServerChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.CHANNEL_STATUS_AVAILABLE;
                            BipProxy.this.sendChannelStatusEvent(TcpServerChannel.this.mChannelStatus);
                        }
                    } catch (IOException e2) {
                        CatLog.d((Object) this, "IOException " + e2.getMessage());
                        return;
                    }
                }
                while (TcpServerChannel.this.mSocket != null) {
                    if (TcpServerChannel.this.mSocket.isClosed()) {
                        TcpServerChannel.this.mChannelStatus = (TcpServerChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.TCP_CHANNEL_BUFFER_SIZE;
                        BipProxy.this.sendChannelStatusEvent(TcpServerChannel.this.mChannelStatus);
                        try {
                            CatLog.d((Object) this, "Wait for connection");
                            TcpServerChannel.this.mSocket = BipProxy.this.mServerChannels[TcpServerChannel.this.index].mServerSocket.accept();
                            CatLog.d((Object) this, "New connection mSocket = " + TcpServerChannel.this.mSocket);
                            if (TcpServerChannel.this.mSocket.isConnected()) {
                                TcpServerChannel.this.mChannelStatus = (TcpServerChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.CHANNEL_STATUS_AVAILABLE;
                                BipProxy.this.sendChannelStatusEvent(TcpServerChannel.this.mChannelStatus);
                            }
                        } catch (IOException e22) {
                            CatLog.d((Object) this, "IOException " + e22.getMessage());
                            return;
                        }
                    }
                    try {
                        CatLog.d((Object) this, "Reading from input stream mSocket = " + TcpServerChannel.this.mSocket);
                        TcpServerChannel.this.mRxLen = TcpServerChannel.this.mSocket.getInputStream().read(TcpServerChannel.this.mRxBuf);
                    } catch (IOException e222) {
                        CatLog.d((Object) this, "Read on No: " + TcpServerChannel.this.mChannelSettings.channel + ", IOException " + e222.getMessage());
                        TcpServerChannel.this.mSocket = null;
                        TcpServerChannel.this.mRxBuf = new byte[TcpServerChannel.this.mChannelSettings.bufSize];
                        TcpServerChannel.this.mTxBuf = new byte[TcpServerChannel.this.mChannelSettings.bufSize];
                        TcpServerChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                        TcpServerChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                        TcpServerChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                        TcpServerChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    }
                    synchronized (TcpServerChannel.this.token) {
                        if (TcpServerChannel.this.mRxLen <= 0) {
                            CatLog.d((Object) this, "No data read. " + TcpServerChannel.this.mRxLen);
                            try {
                                TcpServerChannel.this.mSocket.close();
                            } catch (IOException e3) {
                            }
                        } else {
                            CatLog.d((Object) this, "BipLog, " + TcpServerChannel.this.mRxLen + " data read. mChannelStatus = " + TcpServerChannel.this.mChannelStatus);
                            TcpServerChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            int available = PduHeaders.STORE_STATUS_ERROR_END;
                            if (TcpServerChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                available = TcpServerChannel.this.mRxLen;
                            }
                            BipProxy.this.sendDataAvailableEvent(TcpServerChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
                            try {
                                if (TcpServerChannel.this.mRxLen > 0) {
                                    TcpServerChannel.this.token.wait();
                                }
                            } catch (InterruptedException e4) {
                                e4.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        TcpServerChannel() {
            this.mChannelSettings = null;
            this.mChannelStatus = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mThread = null;
            this.mSendThread = null;
            this.token = new Object();
            this.mRxBuf = new byte[BipProxy.TCP_CHANNEL_BUFFER_SIZE];
            this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxBuf = new byte[BipProxy.TCP_CHANNEL_BUFFER_SIZE];
            this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.result = ResultCode.OK;
            this.catCmdMsg = null;
            this.index = -1;
        }

        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            this.catCmdMsg = cmdMsg;
            CatLog.d((Object) this, "preProcessOpen bufSize = " + this.mChannelSettings.bufSize + " mImmediateLinkEstablish = " + BipProxy.this.mImmediateLinkEstablish);
            if (this.mChannelSettings.bufSize > BipProxy.TCP_CHANNEL_BUFFER_SIZE) {
                this.result = ResultCode.PRFRMD_WITH_MODIFICATION;
                this.mChannelSettings.bufSize = BipProxy.TCP_CHANNEL_BUFFER_SIZE;
            } else if (this.mChannelSettings.bufSize > 0) {
                this.mRxBuf = new byte[this.mChannelSettings.bufSize];
                this.mTxBuf = new byte[this.mChannelSettings.bufSize];
            } else {
                this.mChannelSettings.bufSize = BipProxy.TCP_CHANNEL_BUFFER_SIZE;
            }
            if (!BipProxy.this.mImmediateLinkEstablish) {
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
            }
            return true;
        }

        public boolean open(CatCmdMessage cmdMsg) {
            this.mThread = new TcpServerThread();
            this.mThread.start();
            return true;
        }

        public void close(CatCmdMessage cmdMsg) {
            CatLog.d((Object) this, "close cmd qual = " + cmdMsg.getCommandQualifier() + " mSocket = " + this.mSocket);
            if ((cmdMsg.getCommandQualifier() & BipProxy.NO_CHANNEL_AVAILABLE) == BipProxy.NO_CHANNEL_AVAILABLE) {
                if (!(this.mSocket == null || this.mSocket.isClosed())) {
                    try {
                        this.mSocket.close();
                    } catch (IOException e) {
                    }
                }
                this.mSocket = null;
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
                return;
            }
            if (!(this.mSocket == null || this.mSocket.isClosed())) {
                try {
                    this.mSocket.close();
                } catch (IOException e2) {
                }
            }
            this.mSocket = null;
            BipProxy.this.mServerChannels[this.index].mServerSocket = null;
            BipProxy.this.mServerChannels[this.index] = null;
            this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
            BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
        }

        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == BipProxy.NO_CHANNEL_AVAILABLE && BipProxy.this.setupDataConnection(BipProxy.this.openChCmdMsg)) {
                CatLog.d((Object) this, "Continue processing open channel");
                if (BipProxy.this.mBipChannels[cmdMsg.getDataSettings().channel - 1].open(BipProxy.this.openChCmdMsg)) {
                    this.mSendThread = new TcpServerSendThread(cmdMsg);
                    this.mSendThread.start();
                } else {
                    BipProxy.this.cleanupBipChannel(cmdMsg.getDataSettings().channel);
                }
                return;
            }
            this.mSendThread = new TcpServerSendThread(cmdMsg);
            this.mSendThread.start();
        }

        public void receive(CatCmdMessage cmdMsg) {
            ResultCode result = ResultCode.OK;
            CatLog.d((Object) this, "RECEIVE_DATA on channel no: " + cmdMsg.getDataSettings().channel);
            int requested = cmdMsg.getDataSettings().length;
            if (requested > BipProxy.MAX_LEN_OF_CHANNEL_DATA) {
                requested = BipProxy.MAX_LEN_OF_CHANNEL_DATA;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            int available = PduHeaders.STORE_STATUS_ERROR_END;
            if (this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                available = this.mRxLen;
            }
            byte[] bArr = null;
            if (requested > 0) {
                bArr = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, bArr, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, requested);
                this.mRxPos += requested;
            }
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, result, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new ReceiveDataResponseData(bArr, available));
        }

        public int getStatus() {
            if (this.mChannelSettings == null) {
                this.mChannelStatus = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            } else if (this.mChannelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            }
            return this.mChannelStatus;
        }

        public void setStatus(int status) {
            if (BipProxy.SECURITY_ERROR == status && (this.mChannelStatus & BipProxy.CHANNEL_STATUS_AVAILABLE) == BipProxy.CHANNEL_STATUS_AVAILABLE) {
                this.mChannelStatus = (this.mChannelStatus & 32512) | status;
                BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
            }
        }

        public void onSessionEnd() {
            if (this.mSocket != null) {
                if (!this.mSocket.isClosed()) {
                    try {
                        this.mSocket.close();
                    } catch (IOException e) {
                    }
                }
                this.mSocket = null;
            }
            if (this.mThread == null || !this.mThread.isAlive()) {
                this.mThread = new TcpServerThread();
                this.mThread.start();
            }
        }
    }

    class UdpClientChannel implements BipChannel {
        CatCmdMessage catCmdMsg;
        ChannelSettings mChannelSettings;
        int mChannelStatus;
        DatagramSocket mDatagramSocket;
        byte[] mRxBuf;
        int mRxLen;
        int mRxPos;
        UdpClientSendThread mSendThread;
        UdpClientThread mThread;
        byte[] mTxBuf;
        int mTxLen;
        int mTxPos;
        ResultCode result;
        private Object token;

        class UdpClientSendThread extends Thread {
            private CatCmdMessage cmdMsg;

            public UdpClientSendThread(CatCmdMessage cmdMsg) {
                this.cmdMsg = cmdMsg;
            }

            public void run() {
                DataSettings dataSettings = this.cmdMsg.getDataSettings();
                CatLog.d((Object) this, "SEND_DATA on channel no: " + dataSettings.channel);
                CatLog.d((Object) this, "Transfer data into tx buffer");
                for (int i = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < dataSettings.data.length && UdpClientChannel.this.mTxPos < UdpClientChannel.this.mTxBuf.length; i += BipProxy.NO_CHANNEL_AVAILABLE) {
                    byte[] bArr = UdpClientChannel.this.mTxBuf;
                    UdpClientChannel udpClientChannel = UdpClientChannel.this;
                    int i2 = udpClientChannel.mTxPos;
                    udpClientChannel.mTxPos = i2 + BipProxy.NO_CHANNEL_AVAILABLE;
                    bArr[i2] = dataSettings.data[i];
                }
                UdpClientChannel udpClientChannel2 = UdpClientChannel.this;
                udpClientChannel2.mTxLen += dataSettings.data.length;
                CatLog.d((Object) this, "Tx buffer now contains " + UdpClientChannel.this.mTxLen + " bytes.");
                if (this.cmdMsg.getCommandQualifier() == BipProxy.NO_CHANNEL_AVAILABLE) {
                    UdpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    int len = UdpClientChannel.this.mTxLen;
                    UdpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    CatLog.d((Object) this, "Sent data to socket " + len + " bytes.");
                    if (UdpClientChannel.this.mDatagramSocket == null) {
                        CatLog.d((Object) this, "Socket not available.");
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    }
                    InetAddress addr = null;
                    try {
                        addr = TransportProtocol.UDP_CLIENT_REMOTE == UdpClientChannel.this.mChannelSettings.protocol ? InetAddress.getByAddress(UdpClientChannel.this.mChannelSettings.destinationAddress) : InetAddress.getLocalHost();
                    } catch (IOException e) {
                        CatLog.d((Object) this, "OPEN_CHANNEL - UDP Client connection failed: " + e.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                        if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                            BipProxy.this.teardownDataConnection(this.cmdMsg);
                        }
                    }
                    try {
                        UdpClientChannel.this.mDatagramSocket.send(new DatagramPacket(UdpClientChannel.this.mTxBuf, len, addr, UdpClientChannel.this.mChannelSettings.port));
                        CatLog.d((Object) this, "Data on channel no: " + dataSettings.channel + " sent to socket.");
                    } catch (IOException e2) {
                        CatLog.d((Object) this, "IOException " + e2.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    } catch (IllegalArgumentException e3) {
                        CatLog.d((Object) this, "IllegalArgumentException " + e3.getMessage());
                        BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN));
                        return;
                    }
                }
                int avail = 238;
                if (UdpClientChannel.this.mChannelSettings != null) {
                    avail = UdpClientChannel.this.mChannelSettings.bufSize - UdpClientChannel.this.mTxLen;
                    if (avail > PduHeaders.STORE_STATUS_ERROR_END) {
                        avail = PduHeaders.STORE_STATUS_ERROR_END;
                    }
                }
                CatLog.d((Object) this, "TR with " + avail + " bytes available in Tx Buffer on channel " + dataSettings.channel);
                BipProxy.this.mStkService.sendTerminalResponse(this.cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new SendDataResponseData(avail));
            }
        }

        class UdpClientThread extends Thread {
            UdpClientThread() {
            }

            public void run() {
                IOException e;
                int available;
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
                    UdpClientChannel.this.mChannelStatus = (UdpClientChannel.this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED) + BipProxy.CHANNEL_STATUS_AVAILABLE;
                    BipProxy.this.mStkService.sendTerminalResponse(UdpClientChannel.this.catCmdMsg.mCmdDet, UdpClientChannel.this.result, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                    while (UdpClientChannel.this.mDatagramSocket != null) {
                        DatagramPacket datagramPacket = null;
                        boolean success = false;
                        try {
                            CatLog.d((Object) this, "UDP Client listening on port: " + UdpClientChannel.this.mDatagramSocket.getLocalPort());
                            DatagramPacket packet = new DatagramPacket(UdpClientChannel.this.mRxBuf, UdpClientChannel.this.mRxBuf.length);
                            try {
                                UdpClientChannel.this.mDatagramSocket.receive(packet);
                                success = true;
                                datagramPacket = packet;
                            } catch (IOException e3) {
                                e = e3;
                                datagramPacket = packet;
                                CatLog.d((Object) this, "Read on No: " + UdpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                                if (success) {
                                    UdpClientChannel.this.mDatagramSocket = null;
                                    UdpClientChannel.this.mRxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                    UdpClientChannel.this.mTxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                    UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    UdpClientChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    UdpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    UdpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                                        CatLog.d((Object) this, "UdpClientThread Exception happened");
                                        BipProxy.this.teardownDataConnection(UdpClientChannel.this.catCmdMsg);
                                        BipProxy.this.checkSetStatusOrNot(UdpClientChannel.this.catCmdMsg);
                                    }
                                } else {
                                    UdpClientChannel.this.mRxLen = datagramPacket.getLength();
                                }
                                synchronized (UdpClientChannel.this.token) {
                                    if (UdpClientChannel.this.mRxLen <= 0) {
                                        CatLog.d((Object) this, "BipLog, " + UdpClientChannel.this.mRxLen + " data read.");
                                        UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                        available = PduHeaders.STORE_STATUS_ERROR_END;
                                        if (UdpClientChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                            available = UdpClientChannel.this.mRxLen;
                                        }
                                        BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
                                        try {
                                            UdpClientChannel.this.token.wait();
                                        } catch (InterruptedException e4) {
                                            e4.printStackTrace();
                                        }
                                    } else {
                                        CatLog.d((Object) this, "No data read. " + UdpClientChannel.this.mRxLen);
                                    }
                                }
                            } catch (IllegalArgumentException e5) {
                                e2 = e5;
                                datagramPacket = packet;
                                CatLog.d((Object) this, "IllegalArgumentException: " + e2.getMessage());
                                if (success) {
                                    UdpClientChannel.this.mRxLen = datagramPacket.getLength();
                                } else {
                                    UdpClientChannel.this.mDatagramSocket = null;
                                    UdpClientChannel.this.mRxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                    UdpClientChannel.this.mTxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                    UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    UdpClientChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    UdpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    UdpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
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
                                        UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                        available = PduHeaders.STORE_STATUS_ERROR_END;
                                        if (UdpClientChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                            available = UdpClientChannel.this.mRxLen;
                                        }
                                        BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
                                        UdpClientChannel.this.token.wait();
                                    }
                                }
                            }
                        } catch (IOException e6) {
                            e = e6;
                            CatLog.d((Object) this, "Read on No: " + UdpClientChannel.this.mChannelSettings.channel + ", IOException " + e.getMessage());
                            if (success) {
                                UdpClientChannel.this.mDatagramSocket = null;
                                UdpClientChannel.this.mRxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                UdpClientChannel.this.mTxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                UdpClientChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                UdpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                UdpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                                    CatLog.d((Object) this, "UdpClientThread Exception happened");
                                    BipProxy.this.teardownDataConnection(UdpClientChannel.this.catCmdMsg);
                                    BipProxy.this.checkSetStatusOrNot(UdpClientChannel.this.catCmdMsg);
                                }
                            } else {
                                UdpClientChannel.this.mRxLen = datagramPacket.getLength();
                            }
                            synchronized (UdpClientChannel.this.token) {
                                if (UdpClientChannel.this.mRxLen <= 0) {
                                    CatLog.d((Object) this, "BipLog, " + UdpClientChannel.this.mRxLen + " data read.");
                                    UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    available = PduHeaders.STORE_STATUS_ERROR_END;
                                    if (UdpClientChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                        available = UdpClientChannel.this.mRxLen;
                                    }
                                    BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
                                    UdpClientChannel.this.token.wait();
                                } else {
                                    CatLog.d((Object) this, "No data read. " + UdpClientChannel.this.mRxLen);
                                }
                            }
                        } catch (IllegalArgumentException e7) {
                            e2 = e7;
                            CatLog.d((Object) this, "IllegalArgumentException: " + e2.getMessage());
                            if (success) {
                                UdpClientChannel.this.mRxLen = datagramPacket.getLength();
                            } else {
                                UdpClientChannel.this.mDatagramSocket = null;
                                UdpClientChannel.this.mRxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                UdpClientChannel.this.mTxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                                UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                UdpClientChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                UdpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                UdpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
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
                                    UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                    available = PduHeaders.STORE_STATUS_ERROR_END;
                                    if (UdpClientChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                        available = UdpClientChannel.this.mRxLen;
                                    }
                                    BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
                                    UdpClientChannel.this.token.wait();
                                }
                            }
                        }
                        if (success) {
                            UdpClientChannel.this.mRxLen = datagramPacket.getLength();
                        } else {
                            UdpClientChannel.this.mDatagramSocket = null;
                            UdpClientChannel.this.mRxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                            UdpClientChannel.this.mTxBuf = new byte[UdpClientChannel.this.mChannelSettings.bufSize];
                            UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            UdpClientChannel.this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            UdpClientChannel.this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                            UdpClientChannel.this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
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
                                UdpClientChannel.this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                                available = PduHeaders.STORE_STATUS_ERROR_END;
                                if (UdpClientChannel.this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                                    available = UdpClientChannel.this.mRxLen;
                                }
                                BipProxy.this.sendDataAvailableEvent(UdpClientChannel.this.mChannelStatus, (byte) (available & PduHeaders.STORE_STATUS_ERROR_END));
                                UdpClientChannel.this.token.wait();
                            }
                        }
                    }
                    CatLog.d((Object) this, "UDP Client thread end on channel no: " + UdpClientChannel.this.mChannelSettings.channel);
                } catch (IOException e8) {
                    CatLog.d((Object) this, "OPEN_CHANNEL - UDP Client connection failed: " + e8.getMessage());
                    if (BipProxy.this.mImmediateLinkEstablish) {
                        BipProxy.this.mStkService.sendTerminalResponse(UdpClientChannel.this.catCmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(UdpClientChannel.this.mChannelSettings.bufSize, Integer.valueOf(UdpClientChannel.this.mChannelStatus), UdpClientChannel.this.mChannelSettings.bearerDescription));
                    } else {
                        BipProxy.this.mImmediateLinkEstablish = true;
                    }
                    if (BipProxy.this.mChannelApnInfo[UdpClientChannel.this.mChannelSettings.channel - 1] != null) {
                        BipProxy.this.teardownDataConnection(UdpClientChannel.this.catCmdMsg);
                    }
                }
            }
        }

        UdpClientChannel() {
            this.mChannelSettings = null;
            this.mChannelStatus = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mThread = null;
            this.mSendThread = null;
            this.token = new Object();
            this.mRxBuf = new byte[BipProxy.UDP_CHANNEL_BUFFER_SIZE];
            this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxBuf = new byte[BipProxy.UDP_CHANNEL_BUFFER_SIZE];
            this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.result = ResultCode.OK;
            this.catCmdMsg = null;
        }

        public boolean preProcessOpen(CatCmdMessage cmdMsg) {
            this.mChannelSettings = cmdMsg.getChannelSettings();
            this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
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
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(this.mChannelSettings.bufSize, Integer.valueOf(this.mChannelStatus), this.mChannelSettings.bearerDescription));
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
                this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            }
            CatLog.d((Object) this, "mDatagramSocket = " + this.mDatagramSocket + " mChannelSettings = " + this.mChannelSettings);
            if (!(this.mDatagramSocket == null || this.mDatagramSocket.isClosed())) {
                this.mDatagramSocket.close();
            }
            this.mDatagramSocket = null;
            this.mRxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mRxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxPos = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            this.mTxLen = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            if (this.mChannelSettings == null) {
                CatLog.d((Object) this, "UdpClientChannel close BIP_ERROR");
                BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, BipProxy.MAX_TCP_SERVER_CHANNEL_NUM, null);
                return;
            }
            this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
            if (BipProxy.this.mChannelApnInfo[this.mChannelSettings.channel - 1] != null) {
                CatLog.d((Object) this, "UdpClientChannel close");
                BipProxy.this.teardownDataConnection(cmdMsg);
            }
        }

        public void send(CatCmdMessage cmdMsg) {
            if (!BipProxy.this.mImmediateLinkEstablish && cmdMsg.getCommandQualifier() == BipProxy.NO_CHANNEL_AVAILABLE && BipProxy.this.setupDataConnection(BipProxy.this.openChCmdMsg)) {
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
            if (requested > BipProxy.MAX_LEN_OF_CHANNEL_DATA) {
                requested = BipProxy.MAX_LEN_OF_CHANNEL_DATA;
            }
            if (requested > this.mRxLen) {
                requested = this.mRxLen;
                result = ResultCode.PRFRMD_WITH_MISSING_INFO;
            }
            this.mRxLen -= requested;
            int available = PduHeaders.STORE_STATUS_ERROR_END;
            if (this.mRxLen < PduHeaders.STORE_STATUS_ERROR_END) {
                available = this.mRxLen;
            }
            byte[] bArr = null;
            if (requested > 0) {
                bArr = new byte[requested];
                System.arraycopy(this.mRxBuf, this.mRxPos, bArr, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, requested);
                this.mRxPos += requested;
            }
            BipProxy.this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, result, false, BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new ReceiveDataResponseData(bArr, available));
            if (this.mRxLen == 0) {
                synchronized (this.token) {
                    this.token.notifyAll();
                }
            }
        }

        public int getStatus() {
            if (this.mChannelSettings == null) {
                this.mChannelStatus = BipProxy.NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
            } else if (this.mChannelSettings.channel == 0) {
                this.mChannelStatus = this.mChannelSettings.channel << BipProxy.DNS_SERVER_ADDRESS_REQUESTED;
            }
            return this.mChannelStatus;
        }

        public void setStatus(int status) {
            if (BipProxy.SECURITY_ERROR == status && (this.mChannelStatus & BipProxy.CHANNEL_STATUS_AVAILABLE) == BipProxy.CHANNEL_STATUS_AVAILABLE) {
                this.mChannelStatus = (this.mChannelStatus & 32512) | status;
                BipProxy.this.sendChannelStatusEvent(this.mChannelStatus);
            }
        }

        public void onSessionEnd() {
            if (this.mThread == null || !this.mThread.isAlive()) {
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
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = NO_CHANNEL_AVAILABLE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = MSG_ID_TEARDOWN_DATA_CALL;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = CMD_QUAL_AUTO_RECONN;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = MSG_ID_DATA_STATE_CHANGED;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = MSG_ID_GET_DATA_STATE;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.LANGUAGE_NOTIFICATION.ordinal()] = 14;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = MAX_TCP_SERVER_CLIENT_CHANNEL_NUM;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = MAX_TCP_SERVER_CHANNEL_NUM;
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
            iArr[CommandType.RECEIVE_DATA.ordinal()] = REQUESTED_BUFFER_SIZE_NOT_AVAILABLE;
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
            iArr[CommandType.SEND_DATA.ordinal()] = SECURITY_ERROR;
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
            iArr[TransportProtocol.RESERVED.ordinal()] = MSG_ID_TEARDOWN_DATA_CALL;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TransportProtocol.TCP_CLIENT_LOCAL.ordinal()] = NO_CHANNEL_AVAILABLE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[TransportProtocol.TCP_CLIENT_REMOTE.ordinal()] = CMD_QUAL_AUTO_RECONN;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[TransportProtocol.TCP_SERVER.ordinal()] = MAX_TCP_SERVER_CHANNEL_NUM;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[TransportProtocol.UDP_CLIENT_LOCAL.ordinal()] = REQUESTED_BUFFER_SIZE_NOT_AVAILABLE;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[TransportProtocol.UDP_CLIENT_REMOTE.ordinal()] = SECURITY_ERROR;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues = iArr;
        return iArr;
    }

    public BipProxy(CatService stkService, CommandsInterface cmdIf, Context context) {
        this.mStkService = null;
        this.mImmediateLinkEstablish = true;
        this.mBipChannels = new BipChannel[MAX_CHANNEL_NUM];
        this.mServerChannels = new ServerChannel[MAX_TCP_SERVER_CHANNEL_NUM];
        this.mChannelApnInfo = new ChannelApnInfo[MAX_CHANNEL_NUM];
        this.mIsWifiConnected = new boolean[MAX_CHANNEL_NUM];
        this.mStkService = stkService;
        this.mContext = context;
        this.mDefaultBearerStateReceiver = new DefaultBearerStateReceiver(context);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(NO_CHANNEL_AVAILABLE, "BipProxy");
        this.mWakeLock.setReferenceCounted(false);
        this.mWakeLockTimeout = DEFAULT_WAKE_LOCK_TIMEOUT;
    }

    public boolean canHandleNewChannel() {
        for (int i = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < this.mBipChannels.length; i += NO_CHANNEL_AVAILABLE) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isIPV6Address(CatCmdMessage cmdMsg) {
        if (cmdMsg == null || CommandType.OPEN_CHANNEL != cmdMsg.getCmdType()) {
            return false;
        }
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (newChannel == null || newChannel.destinationAddress == null || REQUESTED_BUFFER_SIZE_NOT_AVAILABLE == newChannel.destinationAddress.length) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isDnsServerAddressRequested(CatCmdMessage cmdMsg) {
        if (cmdMsg != null && CommandType.OPEN_CHANNEL == cmdMsg.getCmdType() && DNS_SERVER_ADDRESS_REQUESTED == (cmdMsg.getCommandQualifier() & DNS_SERVER_ADDRESS_REQUESTED)) {
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

    public void handleBipCommand(CatCmdMessage cmdMsg) {
        int i;
        if (cmdMsg == null) {
            CatLog.d((Object) this, "handleBipCommand null cmdMsg");
            for (i = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < this.mBipChannels.length; i += NO_CHANNEL_AVAILABLE) {
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
            case NO_CHANNEL_AVAILABLE /*1*/:
            case REQUESTED_BUFFER_SIZE_NOT_AVAILABLE /*4*/:
            case SECURITY_ERROR /*5*/:
                if (cmdMsg.getDataSettings() != null) {
                    try {
                        BipChannel curChannel = this.mBipChannels[cmdMsg.getDataSettings().channel - 1];
                        if (curChannel == null) {
                            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, MAX_TCP_SERVER_CHANNEL_NUM, null);
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
                        this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, MAX_TCP_SERVER_CHANNEL_NUM, null);
                        CatLog.d((Object) this, "handleBipCommand error");
                        return;
                    }
                }
                break;
            case CMD_QUAL_AUTO_RECONN /*2*/:
                int[] status = new int[MAX_CHANNEL_NUM];
                for (i = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < MAX_CHANNEL_NUM; i += NO_CHANNEL_AVAILABLE) {
                    if (this.mBipChannels[i] != null) {
                        status[i] = this.mBipChannels[i].getStatus();
                    } else {
                        status[i] = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
                    }
                    CatLog.d((Object) this, "get channel status = " + status[i]);
                }
                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.OK, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new ChannelStatusResponseData(status));
                return;
            case MAX_TCP_SERVER_CHANNEL_NUM /*3*/:
                ChannelSettings channelSettings = cmdMsg.getChannelSettings();
                if (channelSettings != null) {
                    acquireWakeLock();
                    if (allChannelsClosed()) {
                        this.mDefaultBearerStateReceiver.startListening();
                    }
                    for (i = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < this.mBipChannels.length; i += NO_CHANNEL_AVAILABLE) {
                        if (this.mBipChannels[i] == null) {
                            CatLog.d((Object) this, "mBipChannels " + i + " is available");
                            channelSettings.channel = i + NO_CHANNEL_AVAILABLE;
                            if (channelSettings.channel != 0) {
                                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, NO_CHANNEL_AVAILABLE, null);
                                return;
                            }
                            switch (-getcom-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues()[channelSettings.protocol.ordinal()]) {
                                case NO_CHANNEL_AVAILABLE /*1*/:
                                case CMD_QUAL_AUTO_RECONN /*2*/:
                                    this.mBipChannels[channelSettings.channel - 1] = new TcpClientChannel();
                                    break;
                                case MAX_TCP_SERVER_CHANNEL_NUM /*3*/:
                                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
                                    return;
                                case REQUESTED_BUFFER_SIZE_NOT_AVAILABLE /*4*/:
                                case SECURITY_ERROR /*5*/:
                                    this.mBipChannels[channelSettings.channel - 1] = new UdpClientChannel();
                                    break;
                                default:
                                    CatLog.d((Object) this, "invalid protocol found");
                                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
                                    return;
                            }
                            if ((cmdMsg.getCommandQualifier() & NO_CHANNEL_AVAILABLE) != 0) {
                                this.mImmediateLinkEstablish = false;
                                this.openChCmdMsg = cmdMsg;
                                this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                            } else {
                                this.mImmediateLinkEstablish = true;
                                if (setupDataConnection(cmdMsg)) {
                                    CatLog.d((Object) this, "handleBipCommand :setupDataConnection returned");
                                } else {
                                    CatLog.d((Object) this, "Continue processing open channel");
                                    this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                                    if (!this.mBipChannels[channelSettings.channel - 1].open(cmdMsg)) {
                                        CatLog.d((Object) this, "open channel failed");
                                        cleanupBipChannel(channelSettings.channel);
                                    }
                                }
                            }
                            return;
                        }
                    }
                    if (channelSettings.channel != 0) {
                        switch (-getcom-android-internal-telephony-cat-InterfaceTransportLevel$TransportProtocolSwitchesValues()[channelSettings.protocol.ordinal()]) {
                            case NO_CHANNEL_AVAILABLE /*1*/:
                            case CMD_QUAL_AUTO_RECONN /*2*/:
                                this.mBipChannels[channelSettings.channel - 1] = new TcpClientChannel();
                                break;
                            case MAX_TCP_SERVER_CHANNEL_NUM /*3*/:
                                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
                                return;
                            case REQUESTED_BUFFER_SIZE_NOT_AVAILABLE /*4*/:
                            case SECURITY_ERROR /*5*/:
                                this.mBipChannels[channelSettings.channel - 1] = new UdpClientChannel();
                                break;
                            default:
                                CatLog.d((Object) this, "invalid protocol found");
                                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
                                return;
                        }
                        if ((cmdMsg.getCommandQualifier() & NO_CHANNEL_AVAILABLE) != 0) {
                            this.mImmediateLinkEstablish = true;
                            if (setupDataConnection(cmdMsg)) {
                                CatLog.d((Object) this, "handleBipCommand :setupDataConnection returned");
                            } else {
                                CatLog.d((Object) this, "Continue processing open channel");
                                this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                                if (this.mBipChannels[channelSettings.channel - 1].open(cmdMsg)) {
                                    CatLog.d((Object) this, "open channel failed");
                                    cleanupBipChannel(channelSettings.channel);
                                }
                            }
                        } else {
                            this.mImmediateLinkEstablish = false;
                            this.openChCmdMsg = cmdMsg;
                            this.mBipChannels[channelSettings.channel - 1].preProcessOpen(cmdMsg);
                        }
                        return;
                    }
                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BIP_ERROR, true, NO_CHANNEL_AVAILABLE, null);
                    return;
                }
                break;
        }
        this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
    }

    private boolean allChannelsClosed() {
        BipChannel[] bipChannelArr = this.mBipChannels;
        int length = bipChannelArr.length;
        for (int i = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < length; i += NO_CHANNEL_AVAILABLE) {
            if (bipChannelArr[i] != null) {
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
        additionalInfo[CMD_QUAL_AUTO_RECONN] = (byte) ((channelStatus >> DNS_SERVER_ADDRESS_REQUESTED) & PduHeaders.STORE_STATUS_ERROR_END);
        additionalInfo[MAX_TCP_SERVER_CHANNEL_NUM] = (byte) (channelStatus & PduHeaders.STORE_STATUS_ERROR_END);
        CatLog.d((Object) this, "sendChannelStatusEvent channelStatus = " + channelStatus);
        this.mStkService.onEventDownload(new CatEventMessage(EventCode.CHANNEL_STATUS.value(), additionalInfo, true));
    }

    private void sendDataAvailableEvent(int channelStatus, int dataAvailable) {
        byte[] additionalInfo = new byte[]{(byte) -72, (byte) 2, (byte) 0, (byte) 0, (byte) -73, (byte) 1, (byte) 0};
        additionalInfo[CMD_QUAL_AUTO_RECONN] = (byte) ((channelStatus >> DNS_SERVER_ADDRESS_REQUESTED) & PduHeaders.STORE_STATUS_ERROR_END);
        additionalInfo[MAX_TCP_SERVER_CHANNEL_NUM] = (byte) (channelStatus & PduHeaders.STORE_STATUS_ERROR_END);
        additionalInfo[REQUESTED_SIM_ME_INTERFACE_TRANSPORT_LEVEL_NOT_AVAILABLE] = (byte) (dataAvailable & PduHeaders.STORE_STATUS_ERROR_END);
        CatLog.d((Object) this, "sendDataAvailableEvent channelStatus = " + channelStatus + " dataAvailable = " + dataAvailable);
        this.mStkService.onEventDownload(new CatEventMessage(EventCode.DATA_AVAILABLE.value(), additionalInfo, true));
    }

    private boolean checkExistingCsCallInNetworkClass2G() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        int networkType = tm.getNetworkType();
        int networkClass = TelephonyManager.getNetworkClass(networkType);
        if ((NO_CHANNEL_AVAILABLE != networkClass && networkClass != 0) || tm.getCallState() == 0) {
            return false;
        }
        CatLog.d((Object) this, "Bearer not setup, busy on voice call, networkClass = " + networkClass + " networkType = " + networkType);
        return true;
    }

    private boolean setupDefaultDataConnection(CatCmdMessage cmdMsg) throws ConnectionSetupFailedException {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        ChannelSettings newChannel = cmdMsg.getChannelSettings();
        if (checkExistingCsCallInNetworkClass2G()) {
            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, CMD_QUAL_AUTO_RECONN, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
            throw new ConnectionSetupFailedException("Busy on voice call");
        }
        this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
        this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
        SystemProperties.set("gsm.bip.apn", "default");
        CatLog.d((Object) this, "setupDefaultDataConnection");
        NetworkRequest request = new Builder().addTransportType(NO_SPECIFIC_CAUSE_CAN_BE_GIVEN).addCapability(getBipCapability(this.mChannelApnInfo[newChannel.channel - 1].feature)).build();
        this.mChannelApnInfo[newChannel.channel - 1].networkCallback = new BipNetworkCallback(this.mChannelApnInfo[newChannel.channel - 1].networkType, cmdMsg);
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
            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, CMD_QUAL_AUTO_RECONN, new OpenChannelResponseData(newChannel.bufSize, null, newChannel.bearerDescription));
            throw new ConnectionSetupFailedException("Busy on voice call");
        } else {
            CatLog.d((Object) this, "Detected new data connection parameters");
            if (newChannel.userLogin == null) {
                newChannel.userLogin = "";
            }
            if (newChannel.userPassword == null) {
                newChannel.userPassword = "";
            }
            this.mChannelApnInfo[newChannel.channel - 1] = new ChannelApnInfo(newChannel.channel, cmdMsg);
            this.mIsWifiConnected[newChannel.channel - 1] = cmdMsg.getWifiConnectedFlag();
            SystemProperties.set("gsm.bip.apn", "bipapn, " + newChannel.networkAccessName + ", ," + String.valueOf(newChannel.port) + ", " + newChannel.userLogin + ", " + newChannel.userPassword + ", , , , , , ,3 , " + this.mChannelApnInfo[newChannel.channel - 1].type);
            CatLog.d((Object) this, "setupSpecificPdpConnection");
            NetworkRequest request = new Builder().addTransportType(NO_SPECIFIC_CAUSE_CAN_BE_GIVEN).addCapability(getBipCapability(this.mChannelApnInfo[newChannel.channel - 1].feature)).build();
            this.mChannelApnInfo[newChannel.channel - 1].networkCallback = new BipNetworkCallback(this.mChannelApnInfo[newChannel.channel - 1].networkType, cmdMsg);
            cm.requestNetwork(request, this.mChannelApnInfo[newChannel.channel - 1].networkCallback);
            startDataConnectionTimer(cmdMsg);
            return false;
        }
    }

    private int getBipCapability(String feature) {
        CatLog.d((Object) this, "feature: " + feature);
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP0.equals(feature)) {
            return 18;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP1.equals(feature)) {
            return 19;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP2.equals(feature)) {
            return 20;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP3.equals(feature)) {
            return 21;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP4.equals(feature)) {
            return 22;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP5.equals(feature)) {
            return 23;
        }
        if (AbstractPhoneInternalInterface.FEATURE_ENABLE_BIP6.equals(feature)) {
            return 24;
        }
        return 18;
    }

    private int getChannelId(CatCmdMessage cmdMsg) {
        int channel = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN;
        if (cmdMsg.getCmdType() == CommandType.OPEN_CHANNEL) {
            channel = cmdMsg.getChannelSettings().channel;
        } else {
            if (!(cmdMsg.getCmdType() == CommandType.CLOSE_CHANNEL || cmdMsg.getCmdType() == CommandType.RECEIVE_DATA)) {
                if (cmdMsg.getCmdType() == CommandType.SEND_DATA) {
                }
            }
            channel = cmdMsg.getDataSettings().channel;
        }
        CatLog.d((Object) this, "getChannelId:" + channel);
        return channel;
    }

    private synchronized void checkSetStatusOrNot(CatCmdMessage cmdMsg) {
        int channel = getChannelId(cmdMsg);
        int index = channel - 1;
        if (channel <= 0 || channel > MAX_CHANNEL_NUM) {
            CatLog.d((Object) this, "checkSetStatusOrNot, channel_id" + index + "is invalid, just return");
        } else if (this.mBipChannels[index] == null) {
            CatLog.d((Object) this, "checkSetStatusOrNot, mBipChannel[" + index + "] is null, just return");
        } else if (SECURITY_ERROR == (this.mBipChannels[index].getStatus() & PduHeaders.STORE_STATUS_ERROR_END)) {
            CatLog.d((Object) this, "checkSetStatusOrNot, mBipChannel[" + index + "] already link droped, just return");
        } else {
            CatLog.d((Object) this, "checkSetStatusOrNot, mBipChannel[" + index + "] CH_STATUS_LINK_DROP");
            this.mBipChannels[index].setStatus(SECURITY_ERROR);
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
                    result = setupDefaultDataConnection(cmdMsg);
                } else if (BearerType.MOBILE_PS == bd.type || BearerType.MOBILE_PS_EXTENDED_QOS == bd.type || BearerType.E_UTRAN == bd.type) {
                    result = setupSpecificPdpConnection(cmdMsg);
                } else {
                    CatLog.d((Object) this, "Unsupported bearer type");
                    this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
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
        if (channel <= 0 || channel > MAX_CHANNEL_NUM) {
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
            this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, new OpenChannelResponseData(cmdMsg.getChannelSettings().bufSize, null, cmdMsg.getChannelSettings().bearerDescription));
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
            for (int i = NO_SPECIFIC_CAUSE_CAN_BE_GIVEN; i < MAX_CHANNEL_NUM; i += NO_CHANNEL_AVAILABLE) {
                if (this.mBipChannels[i] != null) {
                    CatLog.d((Object) this, "channel " + i + " link drop");
                    this.mBipChannels[i].setStatus(SECURITY_ERROR);
                }
            }
        }
        cleanupBipChannel(channel);
    }

    private void startDataConnectionTimer(CatCmdMessage cmdMsg) {
        cancelDataConnectionTimer();
        CatLog.d((Object) this, "startDataConnectionTimer.");
        sendMessageDelayed(obtainMessage(EVENT_DC_TIMEOUT, cmdMsg), 180000);
    }

    private void cancelDataConnectionTimer() {
        CatLog.d((Object) this, "cancelDataConnectionTimer.");
        removeMessages(EVENT_DC_TIMEOUT);
    }

    private void acquireWakeLock() {
        synchronized (this.mWakeLock) {
            CatLog.d((Object) this, "acquireWakeLock.");
            this.mWakeLock.acquire();
            removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
            sendMessageDelayed(obtainMessage(EVENT_WAKE_LOCK_TIMEOUT), (long) this.mWakeLockTimeout);
        }
    }

    private void clearWakeLock() {
        synchronized (this.mWakeLock) {
            if (this.mWakeLock.isHeld()) {
                CatLog.d((Object) this, "clearWakeLock.");
                this.mWakeLock.release();
                removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
            }
        }
    }

    public void handleMessage(Message msg) {
        CatLog.d((Object) this, "handleMessage: " + msg.what);
        switch (msg.what) {
            case MSG_ID_SETUP_DATA_CALL /*10*/:
                cancelDataConnectionTimer();
                if (msg.obj != null) {
                    onSetupConnectionCompleted((AsyncResult) msg.obj);
                }
            case MSG_ID_TEARDOWN_DATA_CALL /*11*/:
                if (msg.obj != null) {
                    onTeardownConnectionCompleted((AsyncResult) msg.obj);
                }
            case EVENT_WAKE_LOCK_TIMEOUT /*99*/:
                clearWakeLock();
            case EVENT_DC_TIMEOUT /*100*/:
                CatCmdMessage cmdMsg = msg.obj;
                CatLog.d((Object) this, "EVENT_DC_TIMEOUT teardownDataConnection");
                teardownDataConnection(cmdMsg);
                this.mStkService.sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, NO_SPECIFIC_CAUSE_CAN_BE_GIVEN, null);
            default:
                throw new AssertionError("Unrecognized message: " + msg.what);
        }
    }

    public void cleanChannelApnInfo(int channel) {
        CatLog.d((Object) this, "cleanChannelApnInfo, channel: " + channel);
        this.mChannelApnInfo[channel - 1] = null;
    }
}
