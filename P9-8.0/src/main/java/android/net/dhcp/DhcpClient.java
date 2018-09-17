package android.net.dhcp;

import android.content.Context;
import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.dhcp.DhcpPacket.ParseException;
import android.net.ip.IpManager;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.metrics.IpConnectivityLog;
import android.net.util.NetworkConstants;
import android.os.Message;
import android.os.SystemClock;
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
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import libcore.io.IoBridge;

public class DhcpClient extends AbsDhcpClient {
    private static final int ARP_TIMEOUT_MS = 2000;
    public static final int CMD_CLEAR_LINKADDRESS = 196615;
    public static final int CMD_CONFIGURE_LINKADDRESS = 196616;
    private static final int CMD_EXPIRE_DHCP = 196714;
    private static final int CMD_KICK = 196709;
    public static final int CMD_ON_QUIT = 196613;
    public static final int CMD_POST_DHCP_ACTION = 196612;
    public static final int CMD_PRE_DHCP_ACTION = 196611;
    public static final int CMD_PRE_DHCP_ACTION_COMPLETE = 196614;
    private static final int CMD_REBIND_DHCP = 196713;
    private static final int CMD_RECEIVED_PACKET = 196710;
    private static final int CMD_RENEW_DHCP = 196712;
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
    static final byte[] REQUESTED_PARAMS = new byte[]{(byte) 1, (byte) 3, (byte) 6, (byte) 15, (byte) 26, (byte) 28, (byte) 51, (byte) 58, (byte) 59, (byte) 43};
    private static final int SECONDS = 1000;
    private static final boolean STATE_DBG = false;
    private static final String TAG = "DhcpClient";
    public static String mDhcpError = "";
    private static final Class[] sMessageClasses = new Class[]{DhcpClient.class};
    private static final SparseArray<String> sMessageNames = MessageUtils.findMessageNames(sMessageClasses);
    HwArpClient mArpClient;
    private State mConfiguringInterfaceState = new ConfiguringInterfaceState();
    private boolean mConnSavedAP = false;
    private final Context mContext;
    private final StateMachine mController;
    private State mDeclineState = new DeclineState();
    private int mDeclinedTimes = 0;
    private int mDhcpAction = CMD_START_DHCP;
    private State mDhcpBoundState = new DhcpBoundState();
    private State mDhcpHaveLeaseState = new DhcpHaveLeaseState();
    private State mDhcpInitRebootState = new DhcpInitRebootState();
    private State mDhcpInitState = new DhcpInitState();
    private DhcpResults mDhcpLease;
    private long mDhcpLeaseExpiry;
    private int mDhcpOfferCnt = 0;
    private State mDhcpRebindingState = new DhcpRebindingState();
    private State mDhcpRebootingState = new DhcpRebootingState();
    private State mDhcpRenewingState = new DhcpRenewingState();
    private State mDhcpRequestingState = new DhcpRequestingState();
    private DhcpResultsInfoRecord mDhcpResultsInfo = null;
    private State mDhcpSelectingState = new DhcpSelectingState();
    private State mDhcpState = new DhcpState();
    private final WakeupMessage mExpiryAlarm;
    private State mFastArpCheckingState = new FastArpCheckingState();
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private final String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private final WakeupMessage mKickAlarm;
    private long mLastBoundExitTime;
    private long mLastInitEnterTime;
    private final IpConnectivityLog mMetricsLog = new IpConnectivityLog();
    private DhcpResults mOffer;
    private FileDescriptor mPacketSock;
    private Inet4Address mPendingDHCPServer = null;
    private LinkAddress mPendingIpAddr = null;
    private String mPendingSSID = null;
    private final Random mRandom;
    private boolean mReadDBDone = false;
    private final WakeupMessage mRebindAlarm;
    private ReceiveThread mReceiveThread;
    private boolean mRegisteredForPreDhcpNotification;
    private final WakeupMessage mRenewAlarm;
    private State mSlowArpCheckingState = new SlowArpCheckingState();
    private State mStoppedState = new StoppedState();
    private final WakeupMessage mTimeoutAlarm;
    private int mTransactionId;
    private long mTransactionStartMillis;
    private FileDescriptor mUdpSock;
    private State mWaitBeforeRenewalState = new WaitBeforeRenewalState(this.mDhcpRenewingState);
    private State mWaitBeforeStartState = new WaitBeforeStartState(this.mDhcpInitState);

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
            b.append(" ").append(messageName(message.what)).append(" ").append(message.arg1).append(" ").append(message.arg2).append(" ").append(message.obj);
            return b.toString();
        }

        public boolean processMessage(Message message) {
            return false;
        }

        public String getName() {
            return getClass().getSimpleName();
        }
    }

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
            switch (message.what) {
                case DhcpClient.EVENT_LINKADDRESS_CONFIGURED /*196617*/:
                    Log.d(DhcpClient.TAG, "process EVENT_LINKADDRESS_CONFIGURED, returning to slow arp checking");
                    DhcpClient.this.transitionTo(DhcpClient.this.mSlowArpCheckingState);
                    return true;
                default:
                    return false;
            }
        }
    }

    abstract class PacketRetransmittingState extends LoggingState {
        protected int mTimeout = 0;
        private int mTimer;

        protected abstract void receivePacket(DhcpPacket dhcpPacket);

        protected abstract boolean sendPacket();

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

        protected void timeout() {
        }

        protected void tryCachedIp() {
        }

        protected void initTimer() {
            this.mTimer = 1000;
        }

        protected int jitterTimer(int baseTimer) {
            int maxJitter = baseTimer / 10;
            return baseTimer + (DhcpClient.this.mRandom.nextInt(maxJitter * 2) - maxJitter);
        }

        protected void scheduleKick() {
            DhcpClient.this.mKickAlarm.schedule(SystemClock.elapsedRealtime() + ((long) jitterTimer(this.mTimer)));
            if (this.mTimer >= 6000) {
                tryCachedIp();
            }
            this.mTimer *= 2;
            if (this.mTimer > DhcpClient.MAX_TIMEOUT_MS) {
                this.mTimer = DhcpClient.MAX_TIMEOUT_MS;
            }
        }

        protected void maybeInitTimeout() {
            if (this.mTimeout > 0) {
                DhcpClient.this.mTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) this.mTimeout));
            }
        }
    }

    class DeclineState extends PacketRetransmittingState {
        protected boolean mRet;

        public DeclineState() {
            super();
            this.mRet = false;
            this.mTimeout = 1000;
            DhcpClient.this.mConnSavedAP = false;
        }

        protected boolean sendPacket() {
            this.mRet = DhcpClient.this.sendDeclinePacket(DhcpPacket.INADDR_ANY, (Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress(), DhcpClient.this.mDhcpLease.serverAddress, DhcpPacket.INADDR_BROADCAST);
            if (this.mRet) {
                DhcpClient dhcpClient = DhcpClient.this;
                dhcpClient.mDeclinedTimes = dhcpClient.mDeclinedTimes + 1;
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
            }
            return this.mRet;
        }

        protected void receivePacket(DhcpPacket packet) {
        }

        protected void timeout() {
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
            if (!(DhcpClient.this.mDhcpLease.serverAddress == null || (DhcpClient.this.connectUdpSock(DhcpClient.this.mDhcpLease.serverAddress) ^ 1) == 0)) {
                DhcpClient.this.notifyFailure();
                DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
            }
            DhcpClient.this.scheduleLeaseTimers();
            logTimeToBoundState();
        }

        public void exit() {
            super.exit();
            DhcpClient.this.mLastBoundExitTime = SystemClock.elapsedRealtime();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_RENEW_DHCP /*196712*/:
                    DhcpClient.this.mDhcpAction = DhcpClient.CMD_RENEW_DHCP;
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeRenewalState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRenewingState);
                    }
                    return true;
                default:
                    return false;
            }
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
            switch (message.what) {
                case DhcpClient.CMD_EXPIRE_DHCP /*196714*/:
                    Log.d(DhcpClient.TAG, "Lease expired!");
                    DhcpClient.this.notifyFailure();
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    return true;
                default:
                    return false;
            }
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
            DhcpClient.this.mLastInitEnterTime = SystemClock.elapsedRealtime();
            DhcpClient.this.mDhcpOfferCnt = 0;
            if (DhcpClient.this.mConnSavedAP && (((IpManager) DhcpClient.this.mController).isDhcpDiscoveryForced() ^ 1) != 0) {
                DhcpClient.this.logd("connect to saved AP, transitionTo mDhcpRequestingState directly");
                DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRequestingState);
            }
        }

        protected boolean sendPacket() {
            return DhcpClient.this.sendDiscoverPacket();
        }

        protected void receivePacket(DhcpPacket packet) {
            if (DhcpClient.this.isValidPacket(packet) && (packet instanceof DhcpOfferPacket)) {
                DhcpClient.this.mOffer = packet.toDhcpResults();
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

        protected void tryCachedIp() {
            DhcpClient.this.mController.sendMessage(DhcpClient.CMD_TRY_CACHED_IP);
        }
    }

    abstract class DhcpReacquiringState extends PacketRetransmittingState {
        protected String mLeaseMsg;

        protected abstract Inet4Address packetDestination();

        DhcpReacquiringState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.startNewTransaction();
        }

        protected boolean sendPacket() {
            return DhcpClient.this.sendRequestPacket((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress(), DhcpPacket.INADDR_ANY, null, packetDestination());
        }

        protected void receivePacket(DhcpPacket packet) {
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

        protected Inet4Address packetDestination() {
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
            switch (message.what) {
                case DhcpClient.CMD_REBIND_DHCP /*196713*/:
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRebindingState);
                    return true;
                default:
                    return false;
            }
        }

        protected Inet4Address packetDestination() {
            return DhcpClient.this.mDhcpLease.serverAddress != null ? DhcpClient.this.mDhcpLease.serverAddress : DhcpPacket.INADDR_BROADCAST;
        }
    }

    class DhcpRequestingState extends PacketRetransmittingState {
        public DhcpRequestingState() {
            super();
            this.mTimeout = 9000;
        }

        protected boolean sendPacket() {
            if (DhcpClient.this.mConnSavedAP) {
                DhcpClient.this.mOffer = new DhcpResults();
                DhcpClient.this.mOffer.ipAddress = DhcpClient.this.mPendingIpAddr;
                DhcpClient.this.mConnSavedAP = false;
                DhcpClient.this.mPendingIpAddr = null;
                DhcpClient.this.mPendingDHCPServer = null;
                DhcpClient.this.mDhcpResultsInfo = null;
            }
            return DhcpClient.this.sendRequestPacket(DhcpPacket.INADDR_ANY, (Inet4Address) DhcpClient.this.mOffer.ipAddress.getAddress(), DhcpClient.this.mOffer.serverAddress, DhcpPacket.INADDR_BROADCAST);
        }

        protected void receivePacket(DhcpPacket packet) {
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
                    DhcpClient.this.mOffer = null;
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                }
            }
        }

        protected void timeout() {
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
            if (DhcpClient.this.initInterface() && DhcpClient.this.initSockets()) {
                DhcpClient.this.mReceiveThread = new ReceiveThread();
                DhcpClient.this.mReceiveThread.start();
                return;
            }
            DhcpClient.mDhcpError = "dhcpclient initialize failed";
            DhcpClient.this.notifyFailure();
            DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
        }

        public void exit() {
            if (DhcpClient.this.mReceiveThread != null) {
                DhcpClient.this.mReceiveThread.halt();
                DhcpClient.this.mReceiveThread = null;
            }
            DhcpClient.this.clearDhcpState();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_STOP_DHCP /*196610*/:
                    DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
                    return true;
                case DhcpClient.CMD_RECEIVED_PACKET /*196710*/:
                    DhcpClient.this.calcDhcpOfferCnt((DhcpPacket) message.obj);
                    DhcpClient.this.sendDhcpOfferPacket(DhcpClient.this.mContext, (DhcpPacket) message.obj);
                    return true;
                default:
                    return false;
            }
        }
    }

    class FastArpCheckingState extends PacketRetransmittingState {
        public FastArpCheckingState() {
            super();
            this.mTimeout = 2000;
        }

        protected boolean sendPacket() {
            try {
                if (DhcpClient.this.mDeclinedTimes >= 3 || !DhcpClient.this.mArpClient.doFastArpTest((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress())) {
                    Log.d(DhcpClient.TAG, "Not received ARP response, returning to config interface");
                    DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
                } else {
                    Log.d(DhcpClient.TAG, "Received ARP response, returning to decline");
                    DhcpClient.this.transitionTo(DhcpClient.this.mDeclineState);
                }
            } catch (Exception e) {
                Log.e(DhcpClient.TAG, "Caught exception :", e);
            }
            return true;
        }

        protected void receivePacket(DhcpPacket packet) {
        }

        protected void timeout() {
            Log.d(DhcpClient.TAG, "Returning to config interface");
            DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
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

        /* JADX WARNING: Removed duplicated region for block: B:7:0x0030 A:{Splitter: B:4:0x000f, ExcHandler: java.io.IOException (r3_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:7:0x0030, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:9:0x0033, code:
            if (r13.mStopped == false) goto L_0x0035;
     */
        /* JADX WARNING: Missing block: B:10:0x0035, code:
            android.util.Log.e(android.net.dhcp.DhcpClient.TAG, "Read error", r3);
            android.net.dhcp.DhcpClient.-wrap12(r13.this$0, android.net.metrics.DhcpErrorEvent.RECEIVE_ERROR);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Log.d(DhcpClient.TAG, "Receive thread started");
            while (!this.mStopped) {
                try {
                    DhcpClient.this.sendMessage(DhcpClient.CMD_RECEIVED_PACKET, DhcpPacket.decodeFullPacket(this.mPacket, Os.read(DhcpClient.this.mPacketSock, this.mPacket, 0, this.mPacket.length), 0));
                } catch (Exception e) {
                } catch (ParseException e2) {
                    Log.e(DhcpClient.TAG, "Can't parse packet: " + e2.getMessage());
                    if (e2.errorCode == DhcpErrorEvent.DHCP_NO_COOKIE) {
                        String data = ParseException.class.getName();
                        EventLog.writeEvent(1397638484, new Object[]{"31850211", Integer.valueOf(-1), data});
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

        protected boolean sendPacket() {
            try {
                if (DhcpClient.this.mDeclinedTimes >= 3 || !DhcpClient.this.mArpClient.doSlowArpTest((Inet4Address) DhcpClient.this.mDhcpLease.ipAddress.getAddress())) {
                    Log.d(DhcpClient.TAG, "Not received ARP response, returning to bound state");
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                } else {
                    Log.d(DhcpClient.TAG, "Received ARP response, returning to decline");
                    DhcpClient.this.transitionTo(DhcpClient.this.mDeclineState);
                }
            } catch (Exception e) {
                Log.e(DhcpClient.TAG, "Caught exception :", e);
            }
            return true;
        }

        protected void receivePacket(DhcpPacket packet) {
        }

        protected void timeout() {
            Log.d(DhcpClient.TAG, "Returning to bound state");
            DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
        }
    }

    class StoppedState extends State {
        StoppedState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case DhcpClient.CMD_START_DHCP /*196609*/:
                    DhcpClient.this.mDhcpResultsInfo = DhcpClient.this.getDhcpResultsInfoRecord();
                    DhcpClient.this.mDhcpAction = DhcpClient.CMD_START_DHCP;
                    if (DhcpClient.this.mDhcpResultsInfo != null && DhcpClient.this.mReadDBDone) {
                        try {
                            DhcpClient.this.mPendingIpAddr = new LinkAddress(DhcpClient.this.mDhcpResultsInfo.staIP);
                            DhcpClient.this.mPendingDHCPServer = (Inet4Address) InetAddress.getByName(DhcpClient.this.mDhcpResultsInfo.apDhcpServer.substring(1));
                            DhcpClient.this.mConnSavedAP = true;
                        } catch (Exception e) {
                            DhcpClient.this.logd("get IP&DHCPServer address Exception" + e);
                        }
                    }
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeStartState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    }
                    return true;
                default:
                    return false;
            }
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
            switch (message.what) {
                case DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE /*196614*/:
                    DhcpClient.this.transitionTo(this.mOtherState);
                    return true;
                default:
                    return false;
            }
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
        super(TAG);
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

    public static DhcpClient makeDhcpClient(Context context, StateMachine controller, String intf) {
        DhcpClient client = new DhcpClient(context, controller, intf);
        client.start();
        return client;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0021 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0021, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0022, code:
            android.util.Log.e(TAG, "Can't determine ifindex or MAC address for " + r4.mIfaceName, r0);
     */
    /* JADX WARNING: Missing block: B:6:0x003f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            this.mHwAddr = this.mIface.getHardwareAddress();
            this.mInterfaceBroadcastAddr = new PacketSocketAddress(this.mIface.getIndex(), DhcpPacket.ETHER_BROADCAST);
            return true;
        } catch (Exception e) {
        }
    }

    private void startNewTransaction() {
        this.mTransactionId = this.mRandom.nextInt();
        this.mTransactionStartMillis = SystemClock.elapsedRealtime();
    }

    private boolean initSockets() {
        return initPacketSocket() ? initUdpSocket() : false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0026 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0026, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0027, code:
            android.util.Log.e(TAG, "Error creating packet socket", r1);
     */
    /* JADX WARNING: Missing block: B:6:0x0031, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initPacketSocket() {
        try {
            this.mPacketSock = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IP);
            Os.bind(this.mPacketSock, new PacketSocketAddress((short) OsConstants.ETH_P_IP, this.mIface.getIndex()));
            NetworkUtils.attachDhcpFilter(this.mPacketSock);
            return true;
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x004f A:{Splitter: B:1:0x0008, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x004f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:?, code:
            android.util.Log.e(TAG, "Error creating UDP socket", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initUdpSocket() {
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-251);
        try {
            this.mUdpSock = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_REUSEADDR, 1);
            Os.setsockoptIfreq(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfaceName);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BROADCAST, 1);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, 0);
            Os.bind(this.mUdpSock, Inet4Address.ANY, 68);
            NetworkUtils.protectFromVpn(this.mUdpSock);
            return true;
        } catch (Exception e) {
        } finally {
            TrafficStats.setThreadStatsTag(oldTag);
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0009 A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x0009, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x000a, code:
            android.util.Log.e(TAG, "Error connecting UDP socket", r0);
     */
    /* JADX WARNING: Missing block: B:6:0x0014, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean connectUdpSock(Inet4Address to) {
        try {
            Os.connect(this.mUdpSock, to, 67);
            return true;
        } catch (Exception e) {
        }
    }

    private static void closeQuietly(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException e) {
        }
    }

    private void closeSockets() {
        closeQuietly(this.mUdpSock);
        closeQuietly(this.mPacketSock);
    }

    private short getSecs() {
        return (short) ((int) ((SystemClock.elapsedRealtime() - this.mTransactionStartMillis) / 1000));
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x005e A:{Splitter: B:2:0x0005, ExcHandler: android.system.ErrnoException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x005e, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x005f, code:
            android.util.Log.e(TAG, "Can't send packet: ", r6);
     */
    /* JADX WARNING: Missing block: B:11:0x0068, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean transmitPacket(ByteBuffer buf, String description, int encap, Inet4Address to) {
        if (encap == 0) {
            try {
                Log.d(TAG, "Broadcasting " + description);
                Os.sendto(this.mPacketSock, buf.array(), 0, buf.limit(), 0, this.mInterfaceBroadcastAddr);
            } catch (Exception e) {
            }
        } else if (encap == 2 && to.equals(DhcpPacket.INADDR_BROADCAST)) {
            Log.d(TAG, "Broadcasting " + description);
            Os.sendto(this.mUdpSock, buf, 0, to, 67);
        } else {
            Log.d(TAG, String.format("Unicasting %s to %s", new Object[]{description, Os.getpeername(this.mUdpSock)}));
            Os.write(this.mUdpSock, buf);
        }
        return true;
    }

    private boolean sendDiscoverPacket() {
        return transmitPacket(DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, false, REQUESTED_PARAMS), "DHCPDISCOVER", 0, DhcpPacket.INADDR_BROADCAST);
    }

    private boolean sendRequestPacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : 2;
        return transmitPacket(DhcpPacket.buildRequestPacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null), "DHCPREQUEST ciaddr=" + clientAddress.getHostAddress() + " request=" + "xxx.xxx.xxx.xxx" + " serverid=" + (serverAddress != null ? serverAddress.getHostAddress() : null), encap, to);
    }

    private boolean sendDeclinePacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : 2;
        return transmitPacket(DhcpPacket.buildDeclinePacket(encap, this.mTransactionId, getSecs(), clientAddress, false, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null), "DHCPDECLINE clientaddr=" + clientAddress.getHostAddress() + " request=" + "xxx.xxx.xxx.xxx" + " serverid=" + (serverAddress != null ? serverAddress.getHostAddress() : null), encap, to);
    }

    private void scheduleLeaseTimers() {
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

    private void notifyFailure() {
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, 2, this.mDhcpAction, null);
        removeDhcpResultsInfoCache();
    }

    private void acceptDhcpResults(DhcpResults results, String msg) {
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

    private void clearDhcpState() {
        this.mDhcpLease = null;
        this.mDhcpLeaseExpiry = 0;
        this.mOffer = null;
    }

    public void doQuit() {
        Log.d(TAG, "doQuit");
        quit();
    }

    protected void onQuitting() {
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
        long j = 0;
        long leaseTimeMillis = packet.getLeaseTimeMillis();
        if (leaseTimeMillis > 0) {
            j = SystemClock.elapsedRealtime() + leaseTimeMillis;
        }
        this.mDhcpLeaseExpiry = j;
    }

    private void logError(int errorCode) {
        this.mMetricsLog.log(this.mIfaceName, new DhcpErrorEvent(errorCode));
    }

    private void logState(String name, int durationMs) {
        this.mMetricsLog.log(this.mIfaceName, new DhcpClientEvent(name, durationMs));
    }

    public static String getDhcpError() {
        return mDhcpError;
    }

    private void calcDhcpOfferCnt(DhcpPacket dhcpPacket) {
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
