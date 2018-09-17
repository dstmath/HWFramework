package android.net.dhcp;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.dhcp.DhcpPacket.ParseException;
import android.net.ip.IpManager;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.HexDump;
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
    public static final String ACTION_DHCP_OFFER_INFO = "com.hw.wifipro.action.DHCP_OFFER_INFO";
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
    public static final int DHCP_FAILURE = 2;
    protected static final int DHCP_RESULTS_RECORD_SIZE = 50;
    public static final int DHCP_SUCCESS = 1;
    private static final int DHCP_TIMEOUT_MS = 18000;
    private static final boolean DO_UNICAST = false;
    public static final int EVENT_LINKADDRESS_CONFIGURED = 196617;
    private static final int FIRST_TIMEOUT_MS = 1000;
    public static final String FLAG_DHCP_OFFER_INFO = "com.hw.wifipro.FLAG_DHCP_OFFER_INFO";
    private static final int MAX_TIMEOUT_MS = 128000;
    private static final boolean MSG_DBG = false;
    private static final boolean PACKET_DBG = false;
    private static final int PRIVATE_BASE = 196708;
    private static final int PUBLIC_BASE = 196608;
    static final byte[] REQUESTED_PARAMS = null;
    private static final int SECONDS = 1000;
    private static final boolean STATE_DBG = false;
    private static final String TAG = "DhcpClient";
    private static String mDhcpError;
    private static final Class[] sMessageClasses = null;
    private static final SparseArray<String> sMessageNames = null;
    private State mConfiguringInterfaceState;
    private boolean mConnSavedAP;
    private final Context mContext;
    private final StateMachine mController;
    private int mDhcpAction;
    private State mDhcpBoundState;
    private State mDhcpHaveLeaseState;
    private State mDhcpInitRebootState;
    private State mDhcpInitState;
    private DhcpResults mDhcpLease;
    private long mDhcpLeaseExpiry;
    private State mDhcpRebindingState;
    private State mDhcpRebootingState;
    private State mDhcpRenewingState;
    private State mDhcpRequestingState;
    private DhcpResultsInfoRecord mDhcpResultsInfo;
    private State mDhcpSelectingState;
    private State mDhcpState;
    private int mDhcpofferCnt;
    private final WakeupMessage mExpiryAlarm;
    private byte[] mHwAddr;
    private NetworkInterface mIface;
    private final String mIfaceName;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private final WakeupMessage mKickAlarm;
    private DhcpResults mOffer;
    private FileDescriptor mPacketSock;
    private Inet4Address mPendingDHCPServer;
    private LinkAddress mPendingIpAddr;
    private String mPendingSSID;
    private final Random mRandom;
    private boolean mReadDBDone;
    private final WakeupMessage mRebindAlarm;
    private ReceiveThread mReceiveThread;
    private boolean mRegisteredForPreDhcpNotification;
    private final WakeupMessage mRenewAlarm;
    private State mStoppedState;
    private final WakeupMessage mTimeoutAlarm;
    private int mTransactionId;
    private long mTransactionStartMillis;
    private FileDescriptor mUdpSock;
    private State mWaitBeforeRenewalState;
    private State mWaitBeforeStartState;

    abstract class LoggingState extends State {
        LoggingState() {
        }

        public void enter() {
            DhcpClientEvent.logStateEvent(DhcpClient.this.mIfaceName, getName());
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
            return DhcpClient.STATE_DBG;
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
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpBoundState);
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
            }
        }
    }

    class DhcpBoundState extends LoggingState {
        DhcpBoundState() {
            super();
        }

        public void enter() {
            super.enter();
            if (!(DhcpClient.this.mDhcpLease.serverAddress == null || DhcpClient.this.connectUdpSock(DhcpClient.this.mDhcpLease.serverAddress))) {
                DhcpClient.this.notifyFailure();
                DhcpClient.this.transitionTo(DhcpClient.this.mStoppedState);
            }
            DhcpClient.this.scheduleLeaseTimers();
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
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
            }
        }
    }

    class DhcpHaveLeaseState extends LoggingState {
        DhcpHaveLeaseState() {
            super();
        }

        public void enter() {
            super.enter();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_EXPIRE_DHCP /*196714*/:
                    Log.d(DhcpClient.TAG, "Lease expired!");
                    DhcpClient.this.notifyFailure();
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
            }
        }

        public void exit() {
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

    abstract class PacketRetransmittingState extends LoggingState {
        protected int mTimeout;
        private int mTimer;

        protected abstract void receivePacket(DhcpPacket dhcpPacket);

        protected abstract boolean sendPacket();

        PacketRetransmittingState() {
            super();
            this.mTimeout = 0;
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
                    return DhcpClient.DBG;
                case DhcpClient.CMD_RECEIVED_PACKET /*196710*/:
                    receivePacket((DhcpPacket) message.obj);
                    return message.obj instanceof DhcpOfferPacket ? DhcpClient.STATE_DBG : DhcpClient.DBG;
                case DhcpClient.CMD_TIMEOUT /*196711*/:
                    timeout();
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
            }
        }

        public void exit() {
            DhcpClient.this.mKickAlarm.cancel();
            DhcpClient.this.mTimeoutAlarm.cancel();
        }

        protected void timeout() {
        }

        protected void tryCachedIp() {
        }

        protected void initTimer() {
            this.mTimer = DhcpClient.SECONDS;
        }

        protected int jitterTimer(int baseTimer) {
            int maxJitter = baseTimer / 10;
            return baseTimer + (DhcpClient.this.mRandom.nextInt(maxJitter * DhcpClient.DHCP_FAILURE) - maxJitter);
        }

        protected void scheduleKick() {
            DhcpClient.this.mKickAlarm.schedule(SystemClock.elapsedRealtime() + ((long) jitterTimer(this.mTimer)));
            if (this.mTimer >= H.FREEZE_TIMEOUT_VAL) {
                tryCachedIp();
            }
            this.mTimer *= DhcpClient.DHCP_FAILURE;
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

    class DhcpInitState extends PacketRetransmittingState {
        public DhcpInitState() {
            super();
        }

        public void enter() {
            super.enter();
            DhcpClient.this.startNewTransaction();
            DhcpClient.this.mDhcpofferCnt = 0;
            if (DhcpClient.this.mConnSavedAP && !((IpManager) DhcpClient.this.mController).isDhcpDiscoveryForced()) {
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
                return DhcpClient.DBG;
            }
            switch (message.what) {
                case DhcpClient.CMD_REBIND_DHCP /*196713*/:
                    DhcpClient.this.transitionTo(DhcpClient.this.mDhcpRebindingState);
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
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
                DhcpClient.this.mOffer.serverAddress = DhcpClient.this.mPendingDHCPServer;
                DhcpClient.this.mConnSavedAP = DhcpClient.STATE_DBG;
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
                        DhcpClient.this.transitionTo(DhcpClient.this.mConfiguringInterfaceState);
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

    class DhcpState extends LoggingState {
        DhcpState() {
            super();
        }

        public void enter() {
            super.enter();
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
                    return DhcpClient.DBG;
                case DhcpClient.CMD_RECEIVED_PACKET /*196710*/:
                    DhcpClient.this.calcDhcpOfferCnt((DhcpPacket) message.obj);
                    DhcpClient.this.sendDhcpOfferPacket(DhcpClient.this.mContext, (DhcpPacket) message.obj);
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
            }
        }
    }

    class ReceiveThread extends Thread {
        private final byte[] mPacket;
        private volatile boolean mStopped;

        ReceiveThread() {
            this.mPacket = new byte[1500];
            this.mStopped = DhcpClient.STATE_DBG;
        }

        public void halt() {
            this.mStopped = DhcpClient.DBG;
            DhcpClient.this.closeSockets();
        }

        public void run() {
            Log.d(DhcpClient.TAG, "Receive thread started");
            while (!this.mStopped) {
                try {
                    DhcpClient.this.sendMessage(DhcpClient.CMD_RECEIVED_PACKET, DhcpPacket.decodeFullPacket(this.mPacket, Os.read(DhcpClient.this.mPacketSock, this.mPacket, 0, this.mPacket.length), 0));
                } catch (Exception e) {
                    if (!this.mStopped) {
                        Log.e(DhcpClient.TAG, "Read error", e);
                        DhcpErrorEvent.logReceiveError(DhcpClient.this.mIfaceName);
                    }
                } catch (ParseException e2) {
                    Log.e(DhcpClient.TAG, "Can't parse packet: " + e2.getMessage());
                    DhcpErrorEvent.logParseError(DhcpClient.this.mIfaceName, e2.errorCode);
                } catch (Exception e3) {
                    String data = e3.getClass().getName();
                    EventLog.writeEvent(1397638484, new Object[]{"31850211", Integer.valueOf(-1), data});
                    Log.e(DhcpClient.TAG, "Failed to parse DHCP packet", e3);
                }
            }
            Log.d(DhcpClient.TAG, "Receive thread stopped");
        }
    }

    class StoppedState extends LoggingState {
        StoppedState() {
            super();
        }

        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case DhcpClient.CMD_START_DHCP /*196609*/:
                    DhcpClient.this.mDhcpAction = DhcpClient.CMD_START_DHCP;
                    if (DhcpClient.this.mDhcpResultsInfo != null && DhcpClient.this.mReadDBDone) {
                        try {
                            DhcpClient.this.mPendingIpAddr = new LinkAddress(DhcpClient.this.mDhcpResultsInfo.staIP);
                            DhcpClient.this.mPendingDHCPServer = (Inet4Address) InetAddress.getByName(DhcpClient.this.mDhcpResultsInfo.apDhcpServer.substring(DhcpClient.DHCP_SUCCESS));
                            DhcpClient.this.mConnSavedAP = DhcpClient.DBG;
                        } catch (Exception e) {
                            DhcpClient.this.logd("get IP&DHCPServer address Exception" + e);
                        }
                    }
                    if (DhcpClient.this.mRegisteredForPreDhcpNotification) {
                        DhcpClient.this.transitionTo(DhcpClient.this.mWaitBeforeStartState);
                    } else {
                        DhcpClient.this.transitionTo(DhcpClient.this.mDhcpInitState);
                    }
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
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
                    return DhcpClient.DBG;
                default:
                    return DhcpClient.STATE_DBG;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.dhcp.DhcpClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.dhcp.DhcpClient.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.DhcpClient.<clinit>():void");
    }

    private WakeupMessage makeWakeupMessage(String cmdName, int cmd) {
        return new WakeupMessage(this.mContext, getHandler(), DhcpClient.class.getSimpleName() + "." + this.mIfaceName + "." + cmdName, cmd);
    }

    protected DhcpClient(Context context, StateMachine controller, String iface) {
        super(TAG);
        this.mDhcpAction = CMD_START_DHCP;
        this.mConnSavedAP = STATE_DBG;
        this.mPendingDHCPServer = null;
        this.mPendingIpAddr = null;
        this.mDhcpResultsInfo = null;
        this.mPendingSSID = null;
        this.mReadDBDone = STATE_DBG;
        this.mDhcpofferCnt = 0;
        this.mStoppedState = new StoppedState();
        this.mDhcpState = new DhcpState();
        this.mDhcpInitState = new DhcpInitState();
        this.mDhcpSelectingState = new DhcpSelectingState();
        this.mDhcpRequestingState = new DhcpRequestingState();
        this.mDhcpHaveLeaseState = new DhcpHaveLeaseState();
        this.mConfiguringInterfaceState = new ConfiguringInterfaceState();
        this.mDhcpBoundState = new DhcpBoundState();
        this.mDhcpRenewingState = new DhcpRenewingState();
        this.mDhcpRebindingState = new DhcpRebindingState();
        this.mDhcpInitRebootState = new DhcpInitRebootState();
        this.mDhcpRebootingState = new DhcpRebootingState();
        this.mWaitBeforeStartState = new WaitBeforeStartState(this.mDhcpInitState);
        this.mWaitBeforeRenewalState = new WaitBeforeRenewalState(this.mDhcpRenewingState);
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
        addState(this.mConfiguringInterfaceState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpBoundState, this.mDhcpHaveLeaseState);
        addState(this.mWaitBeforeRenewalState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpRenewingState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpRebindingState, this.mDhcpHaveLeaseState);
        addState(this.mDhcpInitRebootState, this.mDhcpState);
        addState(this.mDhcpRebootingState, this.mDhcpState);
        setInitialState(this.mStoppedState);
        this.mRandom = new Random();
        this.mReadDBDone = DBG;
        if (this.mReadDBDone) {
            this.mDhcpResultsInfo = getDhcpResultsInfoRecord();
        } else {
            Log.e(TAG, "not finish read database");
            this.mDhcpResultsInfo = null;
        }
        this.mKickAlarm = makeWakeupMessage("KICK", CMD_KICK);
        this.mTimeoutAlarm = makeWakeupMessage("TIMEOUT", CMD_TIMEOUT);
        this.mRenewAlarm = makeWakeupMessage("RENEW", CMD_RENEW_DHCP);
        this.mRebindAlarm = makeWakeupMessage("REBIND", CMD_REBIND_DHCP);
        this.mExpiryAlarm = makeWakeupMessage("EXPIRY", CMD_EXPIRE_DHCP);
    }

    public void registerForPreDhcpNotification() {
        this.mRegisteredForPreDhcpNotification = DBG;
    }

    public static DhcpClient makeDhcpClient(Context context, StateMachine controller, String intf) {
        DhcpClient client = new DhcpClient(context, controller, intf);
        client.start();
        return client;
    }

    private boolean initInterface() {
        try {
            this.mIface = NetworkInterface.getByName(this.mIfaceName);
            this.mHwAddr = this.mIface.getHardwareAddress();
            this.mInterfaceBroadcastAddr = new PacketSocketAddress(this.mIface.getIndex(), DhcpPacket.ETHER_BROADCAST);
            return DBG;
        } catch (Exception e) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + this.mIfaceName, e);
            return STATE_DBG;
        }
    }

    private void startNewTransaction() {
        this.mTransactionId = this.mRandom.nextInt();
        this.mTransactionStartMillis = SystemClock.elapsedRealtime();
    }

    private boolean initSockets() {
        return initPacketSocket() ? initUdpSocket() : STATE_DBG;
    }

    private boolean initPacketSocket() {
        try {
            this.mPacketSock = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IP);
            Os.bind(this.mPacketSock, new PacketSocketAddress((short) OsConstants.ETH_P_IP, this.mIface.getIndex()));
            NetworkUtils.attachDhcpFilter(this.mPacketSock);
            return DBG;
        } catch (Exception e) {
            Log.e(TAG, "Error creating packet socket", e);
            return STATE_DBG;
        }
    }

    private boolean initUdpSocket() {
        try {
            this.mUdpSock = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_REUSEADDR, DHCP_SUCCESS);
            Os.setsockoptIfreq(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfaceName);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_BROADCAST, DHCP_SUCCESS);
            Os.setsockoptInt(this.mUdpSock, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, 0);
            Os.bind(this.mUdpSock, Inet4Address.ANY, 68);
            NetworkUtils.protectFromVpn(this.mUdpSock);
            return DBG;
        } catch (Exception e) {
            Log.e(TAG, "Error creating UDP socket", e);
            return STATE_DBG;
        }
    }

    private boolean connectUdpSock(Inet4Address to) {
        try {
            Os.connect(this.mUdpSock, to, 67);
            return DBG;
        } catch (Exception e) {
            Log.e(TAG, "Error connecting UDP socket", e);
            return STATE_DBG;
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

    private boolean transmitPacket(ByteBuffer buf, String description, int encap, Inet4Address to) {
        if (encap == 0) {
            try {
                Log.d(TAG, "Broadcasting " + description);
                Os.sendto(this.mPacketSock, buf.array(), 0, buf.limit(), 0, this.mInterfaceBroadcastAddr);
            } catch (Exception e) {
                Log.e(TAG, "Can't send packet: ", e);
                return STATE_DBG;
            }
        } else if (encap == DHCP_FAILURE && to.equals(DhcpPacket.INADDR_BROADCAST)) {
            Log.d(TAG, "Broadcasting " + description);
            Os.sendto(this.mUdpSock, buf, 0, to, 67);
        } else {
            String str = TAG;
            Object[] objArr = new Object[DHCP_FAILURE];
            objArr[0] = description;
            objArr[DHCP_SUCCESS] = Os.getpeername(this.mUdpSock);
            Log.d(str, String.format("Unicasting %s to %s", objArr));
            Os.write(this.mUdpSock, buf);
        }
        return DBG;
    }

    private boolean sendDiscoverPacket() {
        return transmitPacket(DhcpPacket.buildDiscoverPacket(0, this.mTransactionId, getSecs(), this.mHwAddr, STATE_DBG, REQUESTED_PARAMS), "DHCPDISCOVER", 0, DhcpPacket.INADDR_BROADCAST);
    }

    private boolean sendRequestPacket(Inet4Address clientAddress, Inet4Address requestedAddress, Inet4Address serverAddress, Inet4Address to) {
        int encap = DhcpPacket.INADDR_ANY.equals(clientAddress) ? 0 : DHCP_FAILURE;
        return transmitPacket(DhcpPacket.buildRequestPacket(encap, this.mTransactionId, getSecs(), clientAddress, STATE_DBG, this.mHwAddr, requestedAddress, serverAddress, REQUESTED_PARAMS, null), "DHCPREQUEST ciaddr=" + clientAddress.getHostAddress() + " request=" + "xxx.xxx.xxx.xxx" + " serverid=" + (serverAddress != null ? serverAddress.getHostAddress() : null), encap, to);
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
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, DHCP_SUCCESS, this.mDhcpAction, new DhcpResults(this.mDhcpLease));
    }

    private void notifyFailure() {
        this.mController.sendMessage(CMD_POST_DHCP_ACTION, DHCP_FAILURE, this.mDhcpAction, null);
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
            return STATE_DBG;
        }
        Log.d(TAG, "Received packet: " + packet);
        if (Arrays.equals(packet.getClientMac(), this.mHwAddr)) {
            return DBG;
        }
        Log.d(TAG, "MAC addr mismatch: got " + HexDump.toHexString(packet.getClientMac()) + ", expected " + HexDump.toHexString(packet.getClientMac()));
        return STATE_DBG;
    }

    public void setDhcpLeaseExpiry(DhcpPacket packet) {
        long j = 0;
        long leaseTimeMillis = packet.getLeaseTimeMillis();
        if (leaseTimeMillis > 0) {
            j = SystemClock.elapsedRealtime() + leaseTimeMillis;
        }
        this.mDhcpLeaseExpiry = j;
    }

    public static String getDhcpError() {
        return mDhcpError;
    }

    private void calcDhcpOfferCnt(DhcpPacket dhcpPacket) {
        if (dhcpPacket != null && (dhcpPacket instanceof DhcpOfferPacket)) {
            this.mDhcpofferCnt += DHCP_SUCCESS;
            Log.d(TAG, "Dhcpoffer count is " + this.mDhcpofferCnt);
            if (this.mDhcpofferCnt == DHCP_SUCCESS) {
                DhcpResults results = dhcpPacket.toDhcpResults();
                if (results != null) {
                    updateDhcpResultsInfoCache(results);
                }
            } else if (this.mDhcpofferCnt >= DHCP_FAILURE) {
                removeDhcpResultsInfoCache();
                logd("multi gates, not save dhcpResultsInfo");
            }
        }
    }

    private void sendDhcpOfferPacket(Context context, DhcpPacket dhcpPacket) {
        if (dhcpPacket != null && (dhcpPacket instanceof DhcpOfferPacket)) {
            Intent intent = new Intent(ACTION_DHCP_OFFER_INFO);
            String dhcpResultsStr = dhcpResults2String(dhcpPacket.toDhcpResults());
            if (dhcpResultsStr != null) {
                intent.setFlags(67108864);
                intent.putExtra(FLAG_DHCP_OFFER_INFO, dhcpResultsStr);
                context.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    private String dhcpResults2String(DhcpResults dhcpResults) {
        if (dhcpResults == null || dhcpResults.ipAddress == null || dhcpResults.ipAddress.getAddress() == null || dhcpResults.dnsServers == null) {
            return null;
        }
        StringBuilder lastDhcpResults = new StringBuilder();
        lastDhcpResults.append(String.valueOf(-1)).append("|");
        lastDhcpResults.append(dhcpResults.domains == null ? "" : dhcpResults.domains).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getAddress().getHostAddress()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getPrefixLength()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getFlags()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getScope()).append("|");
        lastDhcpResults.append(dhcpResults.gateway != null ? dhcpResults.gateway.getHostAddress() : "").append("|");
        for (InetAddress dnsServer : dhcpResults.dnsServers) {
            lastDhcpResults.append(dnsServer.getHostAddress()).append("|");
        }
        return lastDhcpResults.toString();
    }
}
