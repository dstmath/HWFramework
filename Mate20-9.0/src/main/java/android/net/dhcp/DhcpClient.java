package android.net.dhcp;

import android.content.Context;
import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.dhcp.DhcpPacket;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.metrics.IpConnectivityLog;
import android.net.util.InterfaceParams;
import android.net.util.NetworkConstants;
import android.os.Message;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import libcore.io.IoBridge;

public class DhcpClient extends AbsDhcpClient {
    private static final int ARP_TIMEOUT_MS = 2000;
    public static final int CMD_CLEAR_LINKADDRESS = 196615;
    public static final int CMD_CONFIGURE_LINKADDRESS = 196616;
    private static final int CMD_EXPIRE_DHCP = 196714;
    private static final int CMD_FAST_ARP_EXIT = 196715;
    private static final int CMD_FAST_ARP_NOT_EXIT = 196716;
    private static final int CMD_KICK = 196709;
    public static final int CMD_ON_QUIT = 196613;
    public static final int CMD_POST_DHCP_ACTION = 196612;
    public static final int CMD_PRE_DHCP_ACTION = 196611;
    public static final int CMD_PRE_DHCP_ACTION_COMPLETE = 196614;
    private static final int CMD_REBIND_DHCP = 196713;
    private static final int CMD_RECEIVED_PACKET = 196710;
    private static final int CMD_RENEW_DHCP = 196712;
    private static final int CMD_SLOW_ARP_EXIT = 196717;
    private static final int CMD_SLOW_ARP_NOT_EXIT = 196718;
    public static final int CMD_START_DHCP = 196609;
    public static final int CMD_STOP_DHCP = 196610;
    private static final int CMD_TIMEOUT = 196711;
    public static final int CMD_TRY_CACHED_IP = 196618;
    private static final boolean DBG = true;
    private static final int DECLINED_TIMES_MAX = 3;
    public static final int DHCP_FAILURE = 2;
    protected static final int DHCP_RESULTS_RECORD_SIZE = 50;
    public static final int DHCP_SUCCESS = 1;
    private static final int DHCP_TIMEOUT_MS = 18000;
    private static final boolean DO_UNICAST = false;
    public static final int EVENT_LINKADDRESS_CONFIGURED = 196617;
    private static final int FIRST_TIMEOUT_MS = 1000;
    private static final int MAX_TIMEOUT_MS = 128000;
    private static final boolean MSG_DBG = false;
    private static final boolean PACKET_DBG = false;
    private static final int PRIVATE_BASE = 196708;
    private static final int PUBLIC_BASE = 196608;
    static final byte[] REQUESTED_PARAMS = {1, 3, 6, UsbDescriptor.DESCRIPTORTYPE_BOS, 26, 28, 51, 58, 59, 43};
    private static final int SECONDS = 1000;
    private static final boolean STATE_DBG = false;
    private static final String TAG = "DhcpClient";
    public static String mDhcpError = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private static final Class[] sMessageClasses = {DhcpClient.class};
    /* access modifiers changed from: private */
    public static final SparseArray<String> sMessageNames = MessageUtils.findMessageNames(sMessageClasses);
    HwArpClient mArpClient;
    /* access modifiers changed from: private */
    public State mConfiguringInterfaceState = new ConfiguringInterfaceState();
    /* access modifiers changed from: private */
    public boolean mConnSavedAP = false;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final StateMachine mController;
    /* access modifiers changed from: private */
    public State mDeclineState = new DeclineState();
    /* access modifiers changed from: private */
    public int mDeclinedTimes = 0;
    /* access modifiers changed from: private */
    public int mDhcpAction = CMD_START_DHCP;
    /* access modifiers changed from: private */
    public State mDhcpBoundState = new DhcpBoundState();
    private State mDhcpHaveLeaseState = new DhcpHaveLeaseState();
    private State mDhcpInitRebootState = new DhcpInitRebootState();
    /* access modifiers changed from: private */
    public State mDhcpInitState = new DhcpInitState();
    /* access modifiers changed from: private */
    public DhcpResults mDhcpLease;
    private long mDhcpLeaseExpiry;
    /* access modifiers changed from: private */
    public int mDhcpOfferCnt = 0;
    /* access modifiers changed from: private */
    public State mDhcpRebindingState = new DhcpRebindingState();
    private State mDhcpRebootingState = new DhcpRebootingState();
    /* access modifiers changed from: private */
    public State mDhcpRenewingState = new DhcpRenewingState();
    /* access modifiers changed from: private */
    public State mDhcpRequestingState = new DhcpRequestingState();
    /* access modifiers changed from: private */
    public DhcpResultsInfoRecord mDhcpResultsInfo = null;
    private State mDhcpSelectingState = new DhcpSelectingState();
    private State mDhcpState = new DhcpState();
    /* access modifiers changed from: private */
    public final WakeupMessage mExpiryAlarm;
    /* access modifiers changed from: private */
    public State mFastArpCheckingState = new FastArpCheckingState();
    /* access modifiers changed from: private */
    public String mFastArpUuidStr = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private byte[] mHwAddr;
    private InterfaceParams mIface;
    private final String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    /* access modifiers changed from: private */
    public final WakeupMessage mKickAlarm;
    /* access modifiers changed from: private */
    public long mLastBoundExitTime;
    /* access modifiers changed from: private */
    public long mLastInitEnterTime;
    private final IpConnectivityLog mMetricsLog = new IpConnectivityLog();
    /* access modifiers changed from: private */
    public DhcpResults mOffer;
    /* access modifiers changed from: private */
    public FileDescriptor mPacketSock;
    /* access modifiers changed from: private */
    public Inet4Address mPendingDHCPServer = null;
    /* access modifiers changed from: private */
    public LinkAddress mPendingIpAddr = null;
    private String mPendingSSID = null;
    /* access modifiers changed from: private */
    public final Random mRandom;
    /* access modifiers changed from: private */
    public boolean mReadDBDone = false;
    /* access modifiers changed from: private */
    public final WakeupMessage mRebindAlarm;
    /* access modifiers changed from: private */
    public ReceiveThread mReceiveThread;
    /* access modifiers changed from: private */
    public boolean mRegisteredForPreDhcpNotification;
    /* access modifiers changed from: private */
    public final WakeupMessage mRenewAlarm;
    /* access modifiers changed from: private */
    public State mSlowArpCheckingState = new SlowArpCheckingState();
    /* access modifiers changed from: private */
    public String mSlowArpUuidStr = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    /* access modifiers changed from: private */
    public State mStoppedState = new StoppedState();
    /* access modifiers changed from: private */
    public final WakeupMessage mTimeoutAlarm;
    private int mTransactionId;
    private long mTransactionStartMillis;
    /* access modifiers changed from: private */
    public FileDescriptor mUdpSock;
    /* access modifiers changed from: private */
    public State mWaitBeforeRenewalState = new WaitBeforeRenewalState(this.mDhcpRenewingState);
    /* access modifiers changed from: private */
    public State mWaitBeforeStartState = new WaitBeforeStartState(this.mDhcpInitState);

    class ConfiguringInterfaceState extends LoggingState {
        ConfiguringInterfaceState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_CONFIGURE_LINKADDRESS, DhcpClient.this.mDhcpLease.ipAddress);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            if (message.what != 196617) {
                return false;
            }
            Log.d(DhcpClient.TAG, "process EVENT_LINKADDRESS_CONFIGURED, returning to slow arp checking");
            DhcpClient.this.transitionTo(DhcpClient.this.mSlowArpCheckingState);
            return true;
        }
    }

    class DeclineState extends PacketRetransmittingState {
        protected boolean mRet = false;

        public DeclineState() {
            super();
            this.mTimeout = 1000;
            boolean unused = DhcpClient.this.mConnSavedAP = false;
        }

        /* access modifiers changed from: protected */
        public boolean sendPacket() {
            this.mRet = DhcpClient.this.sendDeclinePacket(DhcpPacket.INADDR_ANY, (Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress(), DhcpClient.this.mDhcpLease.serverAddress, DhcpPacket.INADDR_BROADCAST);
            if (this.mRet) {
                int unused = DhcpClient.this.mDeclinedTimes = DhcpClient.this.mDeclinedTimes + 1;
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            }
            return this.mRet;
        }

        /* access modifiers changed from: protected */
        public void receivePacket(DhcpPacket packet) {
        }

        /* access modifiers changed from: protected */
        public void timeout() {
            Log.d(DhcpClient.TAG, "After sending ARP unresponse for a while, go to config IP");
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
        }
    }

    class DhcpBoundState extends LoggingState {
        DhcpBoundState() {
            super();
        }

        public void enter() {
            super.enter();
            if (DhcpClient.this.mDhcpLease.serverAddress != null && !DhcpClient.this.connectUdpSock(DhcpClient.this.mDhcpLease.serverAddress)) {
                DhcpClient.this.notifyFailure();
                DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
            }
            DhcpClient.this.scheduleLeaseTimers();
            logTimeToBoundState();
        }

        public void exit() {
            super.exit();
            long unused = DhcpClient.this.mLastBoundExitTime = SystemClock.elapsedRealtime();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            if (message.what != DhcpClient.CMD_RENEW_DHCP) {
                return false;
            }
            int unused = DhcpClient.this.mDhcpAction = DhcpClient.CMD_RENEW_DHCP;
            if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeRenewalState);
            } else {
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRenewingState);
            }
            return true;
        }

        private void logTimeToBoundState() {
            long now = SystemClock.elapsedRealtime();
            if (DhcpClient.this.mLastBoundExitTime > DhcpClient.this.mLastInitEnterTime) {
                DhcpClient.this.logState("RenewingBoundState", (int) (now - DhcpClient.this.mLastBoundExitTime));
            } else {
                DhcpClient.this.logState("InitialBoundState", (int) (now - DhcpClient.this.mLastInitEnterTime));
            }
        }
    }

    class DhcpHaveLeaseState extends LoggingState {
        DhcpHaveLeaseState() {
            super();
        }

        public boolean processMessage(Message message) {
            if (message.what != DhcpClient.CMD_EXPIRE_DHCP) {
                return false;
            }
            Log.d(DhcpClient.TAG, "Lease expired!");
            DhcpClient.this.notifyFailure();
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            return true;
        }

        public void exit() {
            Log.d(DhcpClient.TAG, "DhcpHaveLeaseState exit!");
            DhcpClient.this.mRenewAlarm.cancel();
            DhcpClient.this.mRebindAlarm.cancel();
            DhcpClient.this.mExpiryAlarm.cancel();
            DhcpClient.this.clearDhcpState();
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_CLEAR_LINKADDRESS);
        }
    }

    class DhcpInitRebootState extends LoggingState {
        DhcpInitRebootState() {
            super();
        }
    }

    class DhcpInitState extends PacketRetransmittingState {
        public DhcpInitState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.startNewTransaction();
            long unused = DhcpClient.this.mLastInitEnterTime = SystemClock.elapsedRealtime();
            int unused2 = DhcpClient.this.mDhcpOfferCnt = 0;
            if (DhcpClient.this.mConnSavedAP && !DhcpClient.this.mController.isDhcpDiscoveryForced()) {
                DhcpClient.this.logd("connect to saved AP, transitionTo mDhcpRequestingState directly");
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
            }
        }

        /* access modifiers changed from: protected */
        public boolean sendPacket() {
            return DhcpClient.this.sendDiscoverPacket();
        }

        /* access modifiers changed from: protected */
        public void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet) && (packet instanceof DhcpOfferPacket)) {
                DhcpResults unused = DhcpClient.this.mOffer = packet.toDhcpResults();
                if (DhcpClient.this.isInvalidIpAddr(DhcpClient.this.mOffer)) {
                    DhcpClient.this.notifyInvalidDhcpOfferRcvd(DhcpClient.this.mContext, DhcpClient.this.mOffer);
                    return;
                }
                if (DhcpClient.this.mOffer != null) {
                    Log.d(DhcpClient.TAG, "Got pending lease: " + DhcpClient.this.mOffer);
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void tryCachedIp() {
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_TRY_CACHED_IP);
        }
    }

    abstract class DhcpReacquiringState extends PacketRetransmittingState {
        protected String mLeaseMsg;

        /* access modifiers changed from: protected */
        public abstract Inet4Address packetDestination();

        DhcpReacquiringState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.startNewTransaction();
        }

        /* access modifiers changed from: protected */
        public boolean sendPacket() {
            return DhcpClient.this.sendRequestPacket((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress(), DhcpPacket.INADDR_ANY, null, packetDestination());
        }

        /* access modifiers changed from: protected */
        public void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet)) {
                if (packet instanceof DhcpAckPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    if (results != null) {
                        if (!DhcpClient.this.mDhcpLease.ipAddress.equals(results.ipAddress)) {
                            Log.d(DhcpClient.TAG, "Renewed lease not for our current IP address!");
                            DhcpClient.this.notifyFailure();
                            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                        }
                        DhcpClient.this.setDhcpLeaseExpiry(packet);
                        DhcpClient.this.acceptDhcpResults(results, this.mLeaseMsg);
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                    }
                } else if (packet instanceof DhcpNakPacket) {
                    Log.d(DhcpClient.TAG, "Received NAK, returning to INIT");
                    DhcpClient.this.notifyFailure();
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
            }
        }
    }

    class DhcpRebindingState extends DhcpReacquiringState {
        public DhcpRebindingState() {
            super();
            this.mLeaseMsg = "Rebound";
        }

        public void enter() {
            super.enter();
            DhcpClient.closeQuietly(DhcpClient.this.mUdpSock);
            if (!DhcpClient.this.initUdpSocket()) {
                Log.e(DhcpClient.TAG, "Failed to recreate UDP socket");
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            }
        }

        /* access modifiers changed from: protected */
        public Inet4Address packetDestination() {
            return DhcpPacket.INADDR_BROADCAST;
        }
    }

    class DhcpRebootingState extends LoggingState {
        DhcpRebootingState() {
            super();
        }
    }

    class DhcpRenewingState extends DhcpReacquiringState {
        public DhcpRenewingState() {
            super();
            this.mLeaseMsg = "Renewed";
        }

        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            if (message.what != DhcpClient.CMD_REBIND_DHCP) {
                return false;
            }
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRebindingState);
            return true;
        }

        /* access modifiers changed from: protected */
        public Inet4Address packetDestination() {
            return DhcpClient.this.mDhcpLease.serverAddress != null ? DhcpClient.this.mDhcpLease.serverAddress : DhcpPacket.INADDR_BROADCAST;
        }
    }

    class DhcpRequestingState extends PacketRetransmittingState {
        public DhcpRequestingState() {
            super();
            this.mTimeout = 9000;
        }

        /* access modifiers changed from: protected */
        public boolean sendPacket() {
            if (DhcpClient.this.mConnSavedAP) {
                DhcpResults unused = DhcpClient.this.mOffer = new DhcpResults();
                DhcpClient.this.mOffer.ipAddress = DhcpClient.this.mPendingIpAddr;
                boolean unused2 = DhcpClient.this.mConnSavedAP = false;
                LinkAddress unused3 = DhcpClient.this.mPendingIpAddr = null;
                Inet4Address unused4 = DhcpClient.this.mPendingDHCPServer = null;
                DhcpResultsInfoRecord unused5 = DhcpClient.this.mDhcpResultsInfo = null;
            }
            return DhcpClient.this.sendRequestPacket(DhcpPacket.INADDR_ANY, (Inet4Address) DhcpClient.this.mOffer.ipAddress.getAddress(), DhcpClient.this.mOffer.serverAddress, DhcpPacket.INADDR_BROADCAST);
        }

        /* access modifiers changed from: protected */
        public void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet)) {
                if (packet instanceof DhcpAckPacket) {
                    DhcpResults results = packet.toDhcpResults();
                    if (results != null) {
                        DhcpClient.this.setDhcpLeaseExpiry(packet);
                        DhcpClient.this.acceptDhcpResults(results, "Confirmed");
                        Log.d(DhcpClient.TAG, "Received ACK, returning to fast ARP checking");
                        DhcpClient.this.transitionTo(DhcpClient.this.mFastArpCheckingState);
                    }
                } else if (packet instanceof DhcpNakPacket) {
                    Log.d(DhcpClient.TAG, "Received NAK, returning to INIT");
                    DhcpResults unused = DhcpClient.this.mOffer = null;
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void timeout() {
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
        }
    }

    class DhcpSelectingState extends LoggingState {
        DhcpSelectingState() {
            super();
        }
    }

    class DhcpState extends State {
        DhcpState() {
        }

        public void enter() {
            DhcpClient.this.clearDhcpState();
            if (!DhcpClient.this.initInterface() || !DhcpClient.this.initSockets()) {
                DhcpClient.mDhcpError = "dhcpclient initialize failed";
                DhcpClient.this.notifyFailure();
                DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
                return;
            }
            ReceiveThread unused = DhcpClient.this.mReceiveThread = new ReceiveThread();
            DhcpClient.this.mReceiveThread.start();
        }

        public void exit() {
            if (DhcpClient.this.mReceiveThread != null) {
                DhcpClient.this.mReceiveThread.halt();
                ReceiveThread unused = DhcpClient.this.mReceiveThread = null;
            }
            DhcpClient.this.clearDhcpState();
        }

        public boolean processMessage(Message message) {
            DhcpClient.super.processMessage(message);
            int i = message.what;
            if (i == 196610) {
                DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
                return true;
            } else if (i != DhcpClient.CMD_RECEIVED_PACKET) {
                return false;
            } else {
                DhcpClient.this.calcDhcpOfferCnt((DhcpPacket) message.obj);
                DhcpClient.this.sendDhcpOfferPacket(DhcpClient.this.mContext, (DhcpPacket) message.obj);
                return true;
            }
        }
    }

    class FastArpCheckingState extends PacketRetransmittingState {
        public FastArpCheckingState() {
            super();
            this.mTimeout = 2000;
        }

        public void enter() {
            maybeInitTimeout();
            sendPacket();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != DhcpClient.CMD_TIMEOUT) {
                switch (i) {
                    case DhcpClient.CMD_FAST_ARP_EXIT /*196715*/:
                        if (!DhcpClient.this.mFastArpUuidStr.equals(message.obj)) {
                            Log.d(DhcpClient.TAG, "FAST_ARP_EXIT FastArpThreadUuid Error! ArpUuid: " + DhcpClient.this.mFastArpUuidStr + "ArpUuidThread: " + message.obj);
                            return true;
                        }
                        DhcpClient.this.transitionTo(DhcpClient.this.mDeclineState);
                        return true;
                    case DhcpClient.CMD_FAST_ARP_NOT_EXIT /*196716*/:
                        if (!DhcpClient.this.mFastArpUuidStr.equals(message.obj)) {
                            Log.d(DhcpClient.TAG, "FAST_ARP_NOT_EXIT FastArpThreadUuid Error! ArpUuid: " + DhcpClient.this.mFastArpUuidStr + "ArpUuidThread: " + message.obj);
                            return true;
                        }
                        DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                        return true;
                    default:
                        return false;
                }
            } else {
                timeout();
                return true;
            }
        }

        /* access modifiers changed from: protected */
        public boolean sendPacket() {
            String unused = DhcpClient.this.mFastArpUuidStr = UUID.randomUUID().toString();
            if (DhcpClient.this.mDeclinedTimes >= 3) {
                DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                return true;
            }
            new Thread(new Runnable() {
                final String fastArpThreadUuidStr = DhcpClient.this.mFastArpUuidStr;

                public void run() {
                    try {
                        if (DhcpClient.this.mArpClient.doFastArpTest((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress())) {
                            Log.d(DhcpClient.TAG, "Received ARP response, send fast arp exit message");
                            DhcpClient.this.sendMessage(DhcpClient.CMD_FAST_ARP_EXIT, this.fastArpThreadUuidStr);
                            return;
                        }
                        Log.d(DhcpClient.TAG, "Not received ARP response, send fast arp not exit message");
                        DhcpClient.this.sendMessage(DhcpClient.CMD_FAST_ARP_NOT_EXIT, this.fastArpThreadUuidStr);
                    } catch (Exception e) {
                        Log.e(DhcpClient.TAG, "Failed to sendFastArpPacket" + e.toString());
                    }
                }
            }).start();
            return true;
        }

        /* access modifiers changed from: protected */
        public void receivePacket(DhcpPacket packet) {
        }

        /* access modifiers changed from: protected */
        public void timeout() {
            Log.d(DhcpClient.TAG, "Returning to config interface");
            DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
        }

        public void exit() {
            DhcpClient.this.mTimeoutAlarm.cancel();
        }
    }

    abstract class LoggingState extends State {
        private long mEnterTimeMs;

        LoggingState() {
        }

        public void enter() {
            this.mEnterTimeMs = SystemClock.elapsedRealtime();
        }

        public void exit() {
            DhcpClient.this.logState(getName(), (int) (SystemClock.elapsedRealtime() - this.mEnterTimeMs));
        }

        private String messageName(int what) {
            return (String) DhcpClient.sMessageNames.get(what, Integer.toString(what));
        }

        private String messageToString(Message message) {
            long now = SystemClock.uptimeMillis();
            StringBuilder b = new StringBuilder(" ");
            TimeUtils.formatDuration(message.getWhen() - now, b);
            b.append(" ");
            b.append(messageName(message.what));
            b.append(" ");
            b.append(message.arg1);
            b.append(" ");
            b.append(message.arg2);
            b.append(" ");
            b.append(message.obj);
            return b.toString();
        }

        public boolean processMessage(Message message) {
            return false;
        }

        public String getName() {
            return getClass().getSimpleName();
        }
    }

    abstract class PacketRetransmittingState extends LoggingState {
        protected int mTimeout = 0;
        private int mTimer;

        /* access modifiers changed from: protected */
        public abstract void receivePacket(DhcpPacket dhcpPacket);

        /* access modifiers changed from: protected */
        public abstract boolean sendPacket();

        PacketRetransmittingState() {
            super();
        }

        public void enter() {
            super.enter();
            initTimer();
            maybeInitTimeout();
            DhcpClient.this.sendMessage(DhcpClient.CMD_KICK);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_KICK /*196709*/:
                    sendPacket();
                    scheduleKick();
                    return true;
                case DhcpClient.CMD_RECEIVED_PACKET /*196710*/:
                    receivePacket((DhcpPacket) message.obj);
                    return !(message.obj instanceof DhcpOfferPacket);
                case DhcpClient.CMD_TIMEOUT /*196711*/:
                    timeout();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            super.exit();
            DhcpClient.this.mKickAlarm.cancel();
            DhcpClient.this.mTimeoutAlarm.cancel();
        }

        /* access modifiers changed from: protected */
        public void timeout() {
        }

        /* access modifiers changed from: protected */
        public void tryCachedIp() {
        }

        /* access modifiers changed from: protected */
        public void initTimer() {
            this.mTimer = 1000;
        }

        /* access modifiers changed from: protected */
        public int jitterTimer(int baseTimer) {
            int maxJitter = baseTimer / 10;
            return baseTimer + (DhcpClient.this.mRandom.nextInt(2 * maxJitter) - maxJitter);
        }

        /* access modifiers changed from: protected */
        public void scheduleKick() {
            DhcpClient.this.mKickAlarm.schedule(SystemClock.elapsedRealtime() + ((long) jitterTimer(this.mTimer)));
            if (this.mTimer >= 6000) {
                tryCachedIp();
            }
            this.mTimer *= 2;
            if (this.mTimer > DhcpClient.MAX_TIMEOUT_MS) {
                this.mTimer = DhcpClient.MAX_TIMEOUT_MS;
            }
        }

        /* access modifiers changed from: protected */
        public void maybeInitTimeout() {
            if (this.mTimeout > 0) {
                DhcpClient.this.mTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) this.mTimeout));
            }
        }
    }

    class ReceiveThread extends Thread {
        private final byte[] mPacket = new byte[NetworkConstants.ETHER_MTU];
        private volatile boolean mStopped = false;

        ReceiveThread() {
        }

        public void halt() {
            this.mStopped = true;
            DhcpClient.this.closeSockets();
        }

        public void run() {
            Log.d(DhcpClient.TAG, "Receive thread started");
            while (!this.mStopped) {
                try {
                    DhcpClient.this.sendMessage(DhcpClient.CMD_RECEIVED_PACKET, DhcpPacket.decodeFullPacket(this.mPacket, Os.read(DhcpClient.this.mPacketSock, this.mPacket, 0, this.mPacket.length), 0));
                } catch (ErrnoException | IOException e) {
                    if (!this.mStopped) {
                        Log.e(DhcpClient.TAG, "Read error", e);
                        DhcpClient.this.logError(DhcpErrorEvent.RECEIVE_ERROR);
                    }
                } catch (DhcpPacket.ParseException e2) {
                    Log.e(DhcpClient.TAG, "Can't parse packet: " + e2.getMessage());
                    if (e2.errorCode == DhcpErrorEvent.DHCP_NO_COOKIE) {
                        EventLog.writeEvent(1397638484, new Object[]{"31850211", -1, DhcpPacket.ParseException.class.getName()});
                    }
                    DhcpClient.this.logError(e2.errorCode);
                } catch (Exception e3) {
                    Log.e(DhcpClient.TAG, "Failed to parse DHCP packet", e3);
                }
            }
            Log.d(DhcpClient.TAG, "Receive thread stopped");
        }
    }

    class SlowArpCheckingState extends PacketRetransmittingState {
        public SlowArpCheckingState() {
            super();
            this.mTimeout = 4000;
        }

        public void enter() {
            maybeInitTimeout();
            sendPacket();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != DhcpClient.CMD_TIMEOUT) {
                switch (i) {
                    case DhcpClient.CMD_SLOW_ARP_EXIT /*196717*/:
                        if (!DhcpClient.this.mSlowArpUuidStr.equals(message.obj)) {
                            Log.d(DhcpClient.TAG, "SLOW_ARP_EXIT SlowArpThreadUuid Error! ArpUuid: " + DhcpClient.this.mSlowArpUuidStr + "ArpUuidThread: " + message.obj);
                            return true;
                        }
                        DhcpClient.this.transitionTo(DhcpClient.this.mDeclineState);
                        return true;
                    case DhcpClient.CMD_SLOW_ARP_NOT_EXIT /*196718*/:
                        if (!DhcpClient.this.mSlowArpUuidStr.equals(message.obj)) {
                            Log.d(DhcpClient.TAG, "SLOW_ARP_NOT_EXIT SlowArpThreadUuid Error! ArpUuid: " + DhcpClient.this.mSlowArpUuidStr + "ArpUuidThread: " + message.obj);
                            return true;
                        }
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                        return true;
                    default:
                        return false;
                }
            } else {
                timeout();
                return true;
            }
        }

        /* access modifiers changed from: protected */
        public boolean sendPacket() {
            String unused = DhcpClient.this.mSlowArpUuidStr = UUID.randomUUID().toString();
            if (DhcpClient.this.mDeclinedTimes >= 3) {
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                return true;
            }
            new Thread(new Runnable() {
                final String slowArpThreadUuidStr = DhcpClient.this.mSlowArpUuidStr;

                public void run() {
                    try {
                        if (DhcpClient.this.mArpClient.doSlowArpTest((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress())) {
                            Log.d(DhcpClient.TAG, "Received ARP response, send slow arp exit message");
                            DhcpClient.this.sendMessage(DhcpClient.CMD_SLOW_ARP_EXIT, this.slowArpThreadUuidStr);
                            return;
                        }
                        Log.d(DhcpClient.TAG, "Not received ARP response, send slow arp not exit message");
                        DhcpClient.this.sendMessage(DhcpClient.CMD_SLOW_ARP_NOT_EXIT, this.slowArpThreadUuidStr);
                    } catch (Exception e) {
                        Log.e(DhcpClient.TAG, "Failed to sendSlowArpPacket" + e.toString());
                    }
                }
            }).start();
            return true;
        }

        /* access modifiers changed from: protected */
        public void receivePacket(DhcpPacket packet) {
        }

        /* access modifiers changed from: protected */
        public void timeout() {
            Log.d(DhcpClient.TAG, "Returning to bound state");
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
        }

        public void exit() {
            DhcpClient.this.mTimeoutAlarm.cancel();
        }
    }

    class StoppedState extends State {
        StoppedState() {
        }

        public boolean processMessage(Message message) {
            if (message.what != 196609) {
                return false;
            }
            DhcpResultsInfoRecord unused = DhcpClient.this.mDhcpResultsInfo = DhcpClient.this.getDhcpResultsInfoRecord();
            int unused2 = DhcpClient.this.mDhcpAction = DhcpClient.CMD_START_DHCP;
            if (DhcpClient.this.mDhcpResultsInfo != null && DhcpClient.this.mReadDBDone) {
                try {
                    LinkAddress unused3 = DhcpClient.this.mPendingIpAddr = new LinkAddress(DhcpClient.this.mDhcpResultsInfo.staIP);
                    Inet4Address unused4 = DhcpClient.this.mPendingDHCPServer = (Inet4Address) InetAddress.getByName(DhcpClient.this.mDhcpResultsInfo.apDhcpServer.substring(1));
                    boolean unused5 = DhcpClient.this.mConnSavedAP = true;
                } catch (Exception e) {
                    DhcpClient dhcpClient = DhcpClient.this;
                    dhcpClient.logd("get IP&DHCPServer address Exception" + e);
                }
            }
            if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeStartState);
            } else {
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            }
            return true;
        }
    }

    abstract class WaitBeforeOtherState extends LoggingState {
        protected State mOtherState;

        WaitBeforeOtherState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION);
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            if (message.what != 196614) {
                return false;
            }
            DhcpClient.this.transitionTo(this.mOtherState);
            return true;
        }
    }

    class WaitBeforeRenewalState extends WaitBeforeOtherState {
        public WaitBeforeRenewalState(State otherState) {
            super();
            this.mOtherState = otherState;
        }
    }

    class WaitBeforeStartState extends WaitBeforeOtherState {
        public WaitBeforeStartState(State otherState) {
            super();
            this.mOtherState = otherState;
        }
    }

    private WakeupMessage makeWakeupMessage(String cmdName, int cmd) {
        return new WakeupMessage(this.mContext, getHandler(), DhcpClient.class.getSimpleName() + "." + this.mIfaceName + "." + cmdName, cmd);
    }

    protected DhcpClient(Context context, StateMachine controller, String iface) {
        super(TAG, controller.getHandler());
        this.mContext = context;
        this.mController = controller;
        this.mIfaceName = iface;
        addState(this.mStoppedState);
        addState(this.mDhcpState);
        addState(this.mDhcpInitState, this.mDhcpState);
        addState(this.mWaitBeforeStartState, this.mDhcpState);
        addState(this.mDhcpSelectingState, this.mDhcpState);
        addState(this.mDhcpRequestingState, this.mDhcpState);
        addState(this.mDhcpHaveLeaseState, this.mDhcpState);
        addState(this.mFastArpCheckingState, this.mDhcpState);
        addState(this.mDeclineState, this.mDhcpState);
        addState(this.mConfiguringInterfaceState, this.mDhcpState);
        addState(this.mSlowArpCheckingState, this.mDhcpState);
        addState(this.mDhcpBoundState, this.mDhcpHaveLeaseState);
        addState(this.mWaitBeforeRenewalState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpRenewingState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpRebindingState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpInitRebootState, this.mDhcpState);
        addState(this.mDhcpRebootingState, this.mDhcpState);
        setInitialState(this.mStoppedState);
        this.mRandom = new Random();
        this.mReadDBDone = true;
        this.mKickAlarm = makeWakeupMessage("KICK", CMD_KICK);
        this.mTimeoutAlarm = makeWakeupMessage("TIMEOUT", CMD_TIMEOUT);
        this.mRenewAlarm = makeWakeupMessage("RENEW", CMD_RENEW_DHCP);
        this.mRebindAlarm = makeWakeupMessage("REBIND", CMD_REBIND_DHCP);
        this.mExpiryAlarm = makeWakeupMessage("EXPIRY", CMD_EXPIRE_DHCP);
        this.mArpClient = new HwArpClient(this.mContext);
    }

    public void registerForPreDhcpNotification() {
        this.mRegisteredForPreDhcpNotification = true;
    }

    public static DhcpClient makeDhcpClient(Context context, StateMachine controller, InterfaceParams ifParams) {
        DhcpClient client = new DhcpClient(context, controller, ifParams.name);
        client.mIface = ifParams;
        client.start();
        return client;
    }

    /* access modifiers changed from: private */
    public boolean initInterface() {
        if (this.mIface == null) {
            this.mIface = InterfaceParams.getByName(this.mIfaceName);
        }
        if (this.mIface == null) {
            Log.e(TAG, "Can't determine InterfaceParams for " + this.mIfaceName);
            return false;
        }
        this.mHwAddr = this.mIface.macAddr.toByteArray();
        this.mInterfaceBroadcastAddr = new PacketSocketAddress(this.mIface.index, DhcpPacket.ETHER_BROADCAST);
        return true;
    }

    /* access modifiers changed from: private */
    public void startNewTransaction() {
        this.mTransactionId = this.mRandom.nextInt();
        this.mTransactionStartMillis = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public boolean initSockets() {
        return initPacketSocket() && initUdpSocket();
    }

    private boolean initPacketSocket() {
        try {
            this.mPacketSock = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IP);
            Os.bind(this.mPacketSock, new PacketSocketAddress((short) OsConstants.ETH_P_IP, this.mIface.index));
            NetworkUtils.attachDhcpFilter(this.mPacketSock);
            return true;
        } catch (ErrnoException | SocketException e) {
            Log.e(TAG, "Error creating packet socket", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean initUdpSocket() {
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-192);
        try {
            this.mUdpSock = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_REUSEADDR, 1);
            Os.setsockoptIfreq(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfaceName);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BROADCAST, 1);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, 0);
            Os.bind(this.mUdpSock, Inet4Address.ANY, 68);
            NetworkUtils.protectFromVpn(this.mUdpSock);
            return true;
        } catch (ErrnoException | SocketException e) {
            Log.e(TAG, "Error creating UDP socket", e);
            return false;
        } finally {
            TrafficStats.setThreadStatsTag(oldTag);
        }
    }

    /* access modifiers changed from: private */
    public boolean connectUdpSock(Inet4Address to) {
        try {
            Os.connect(this.mUdpSock, to, 67);
            return true;
        } catch (ErrnoException | SocketException e) {
            Log.e(TAG, "Error connecting UDP socket", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static void closeQuietly(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: private */
    public void closeSockets() {
        closeQuietly(this.mUdpSock);
        closeQuietly(this.mPacketSock);
    }

    private short getSecs() {
        return (short) ((int) ((SystemClock.elapsedRealtime() - this.mTransactionStartMillis) / 1000));
    }

    private boolean transmitPacket(ByteBuffer buf, String description, int encap, Inet4Address to) {
        if (encap == 0) {
            try {
                Log.d(TAG, "Broadcasting " + description);
                Os.sendto(this.mPacketSock, buf.array(), 0, buf.limit(), 0, this.mInterfaceBroadcastAddr);
            } catch (ErrnoException | IOException e) {
                Log.e(TAG, "Can't send packet: ", e);
                return false;
            }
        } else if (encap != 2 || !to.equals(DhcpPacket.INADDR_BROADCAST)) {
            Log.d(TAG, String.format("Unicasting %s to %s", new Object[]{description, Os.getpeername(this.mUdpSock)}));
            Os.write(this.mUdpSock, buf);
        } else {
            Log.d(TAG, "Broadcasting " + description);
            Os.sendto(this.mUdpSock, buf, 0, to, 67);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean sendDiscoverPacket() {
        return transmitPacket(DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, REQUESTED_PARAMS), "DHCPDISCOVER", 0, DhcpPacket.INADDR_BROADCAST);
    }

    /* access modifiers changed from: private */
    public boolean sendRequestPacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : 2;
        ByteBuffer packet = DhcpPacket.buildRequestPacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null);
        if (serverAddress != null) {
            String hostAddress = serverAddress.getHostAddress();
        }
        return transmitPacket(packet, "DHCPREQUEST ciaddr=" + "xxx.xxx.xxx.xxx" + " request=" + "xxx.xxx.xxx.xxx" + " serverid=" + "xxx.xxx.xxx.xxx", encap, to);
    }

    /* access modifiers changed from: private */
    public boolean sendDeclinePacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : 2;
        ByteBuffer packet = DhcpPacket.buildDeclinePacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null);
        if (serverAddress != null) {
            String hostAddress = serverAddress.getHostAddress();
        }
        return transmitPacket(packet, "DHCPDECLINE clientaddr=" + "xxx.xxx.xxx.xxx" + " request=" + "xxx.xxx.xxx.xxx" + " serverid=" + "xxx.xxx.xxx.xxx", encap, to);
    }

    /* access modifiers changed from: private */
    public void scheduleLeaseTimers() {
        if (this.mDhcpLeaseExpiry == 0) {
            Log.d(TAG, "Infinite lease, no timer scheduling needed");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long remainingDelay = this.mDhcpLeaseExpiry - now;
        long renewDelay = remainingDelay / 2;
        long rebindDelay = (7 * remainingDelay) / 8;
        this.mRenewAlarm.schedule(now + renewDelay);
        this.mRebindAlarm.schedule(now + rebindDelay);
        this.mExpiryAlarm.schedule(now + remainingDelay);
        Log.d(TAG, "Scheduling renewal in " + (renewDelay / 1000) + "s");
        Log.d(TAG, "Scheduling rebind in " + (rebindDelay / 1000) + "s");
        Log.d(TAG, "Scheduling expiry in " + (remainingDelay / 1000) + "s");
    }

    private void notifySuccess() {
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, 1, this.mDhcpAction, new DhcpResults(this.mDhcpLease));
    }

    /* access modifiers changed from: private */
    public void notifyFailure() {
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, 2, this.mDhcpAction, null);
        removeDhcpResultsInfoCache();
    }

    /* access modifiers changed from: private */
    public void acceptDhcpResults(DhcpResults results, String msg) {
        this.mDhcpLease = results;
        try {
            if (this.mDhcpLease.dnsServers.size() == 0) {
                Log.d(TAG, "Add default dns");
                this.mDhcpLease.addDns("8.8.8.8");
                this.mDhcpLease.addDns("8.8.4.4");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        this.mOffer = null;
        Log.d(TAG, msg + " lease: " + this.mDhcpLease);
        notifySuccess();
    }

    /* access modifiers changed from: private */
    public void clearDhcpState() {
        this.mDhcpLease = null;
        this.mDhcpLeaseExpiry = 0;
        this.mOffer = null;
    }

    public void doQuit() {
        Log.d(TAG, "doQuit");
        quit();
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        Log.d(TAG, "onQuitting");
        this.mController.sendMessage(CMD_ON_QUIT);
    }

    public boolean isValidPacket(DhcpPacket packet) {
        if (packet.getTransactionId() != this.mTransactionId) {
            return false;
        }
        Log.d(TAG, "Received packet: " + packet);
        if (Arrays.equals(packet.getClientMac(), this.mHwAddr)) {
            return true;
        }
        Log.d(TAG, "MAC addr mismatch: got " + HexDump.toHexString(packet.getClientMac()) + ", expected " + HexDump.toHexString(packet.getClientMac()));
        return false;
    }

    public void setDhcpLeaseExpiry(DhcpPacket packet) {
        long leaseTimeMillis = packet.getLeaseTimeMillis();
        long j = 0;
        if (leaseTimeMillis > 0) {
            j = SystemClock.elapsedRealtime() + leaseTimeMillis;
        }
        this.mDhcpLeaseExpiry = j;
    }

    /* access modifiers changed from: private */
    public void logError(int errorCode) {
        this.mMetricsLog.log(this.mIfaceName, new DhcpErrorEvent(errorCode));
    }

    /* access modifiers changed from: private */
    public void logState(String name, int durationMs) {
        this.mMetricsLog.log(this.mIfaceName, new DhcpClientEvent(name, durationMs));
    }

    public static String getDhcpError() {
        return mDhcpError;
    }

    /* access modifiers changed from: private */
    public void calcDhcpOfferCnt(DhcpPacket dhcpPacket) {
        if (dhcpPacket != null && (dhcpPacket instanceof DhcpOfferPacket)) {
            this.mDhcpOfferCnt++;
            Log.d(TAG, "Dhcpoffer count is " + this.mDhcpOfferCnt);
            if (this.mDhcpOfferCnt == 1) {
                DhcpResults results = dhcpPacket.toDhcpResults();
                if (results != null) {
                    updateDhcpResultsInfoCache(results);
                }
            } else if (this.mDhcpOfferCnt >= 2) {
                removeDhcpResultsInfoCache();
                logd("multi gates, not save dhcpResultsInfo");
            }
        }
    }
}
