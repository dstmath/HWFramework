package android.net.apf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.apf.ApfGenerator;
import android.net.ip.IpClient;
import android.net.metrics.ApfProgramEvent;
import android.net.metrics.ApfStats;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.RaEvent;
import android.net.util.InterfaceParams;
import android.net.util.NetworkConstants;
import android.os.PowerManager;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.BitUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import libcore.io.IoBridge;

public class ApfFilter {
    private static final int APF_MAX_ETH_TYPE_BLACK_LIST_LEN = 20;
    private static final int APF_PROGRAM_EVENT_LIFETIME_THRESHOLD = 2;
    private static final int ARP_HEADER_OFFSET = 14;
    private static final byte[] ARP_IPV4_HEADER = {0, 1, 8, 0, 6, 4};
    private static final int ARP_OPCODE_OFFSET = 20;
    private static final short ARP_OPCODE_REPLY = 2;
    private static final short ARP_OPCODE_REQUEST = 1;
    private static final int ARP_TARGET_IP_ADDRESS_OFFSET = 38;
    private static final boolean DBG = true;
    private static final int DHCP_CLIENT_MAC_OFFSET = 50;
    private static final int DHCP_CLIENT_PORT = 68;
    private static final byte[] ETH_BROADCAST_MAC_ADDRESS = {-1, -1, -1, -1, -1, -1};
    private static final int ETH_DEST_ADDR_OFFSET = 0;
    private static final int ETH_ETHERTYPE_OFFSET = 12;
    private static final int ETH_HEADER_LEN = 14;
    private static final int ETH_TYPE_MAX = 65535;
    private static final int ETH_TYPE_MIN = 1536;
    private static final int FRACTION_OF_LIFETIME_TO_FILTER = 6;
    private static final int ICMP6_TYPE_OFFSET = 54;
    private static final int IPV4_ANY_HOST_ADDRESS = 0;
    private static final int IPV4_BROADCAST_ADDRESS = -1;
    private static final int IPV4_DEST_ADDR_OFFSET = 30;
    private static final int IPV4_FRAGMENT_OFFSET_MASK = 8191;
    private static final int IPV4_FRAGMENT_OFFSET_OFFSET = 20;
    private static final int IPV4_PROTOCOL_OFFSET = 23;
    private static final byte[] IPV6_ALL_NODES_ADDRESS = {-1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
    private static final int IPV6_DEST_ADDR_OFFSET = 38;
    private static final int IPV6_FLOW_LABEL_LEN = 3;
    private static final int IPV6_FLOW_LABEL_OFFSET = 15;
    private static final int IPV6_HEADER_LEN = 40;
    private static final int IPV6_NEXT_HEADER_OFFSET = 20;
    private static final int IPV6_SRC_ADDR_OFFSET = 22;
    private static final long MAX_PROGRAM_LIFETIME_WORTH_REFRESHING = 30;
    private static final int MAX_RAS = 10;
    private static final String TAG = "ApfFilter";
    private static final int UDP_DESTINATION_PORT_OFFSET = 16;
    private static final int UDP_HEADER_LEN = 8;
    private static final boolean VDBG = false;
    private final int DEFAULT_FILTER = 0;
    private final int SCREEN_CHANGE_FILTER = 1;
    /* access modifiers changed from: private */
    public final ApfCapabilities mApfCapabilities;
    private final Context mContext;
    /* access modifiers changed from: private */
    public final String mCountAndDropLabel;
    private final String mCountAndPassLabel;
    private int mCurrentMulticastFilter = 0;
    @GuardedBy("this")
    private byte[] mDataSnapshot;
    private final BroadcastReceiver mDeviceIdleReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                ApfFilter.this.setDozeMode(((PowerManager) context.getSystemService("power")).isDeviceIdleMode());
            }
        }
    };
    private final boolean mDrop802_3Frames;
    private final int[] mEthTypeBlackList;
    @VisibleForTesting
    byte[] mHardwareAddress;
    @GuardedBy("this")
    private byte[] mIPv4Address;
    @GuardedBy("this")
    private int mIPv4PrefixLength;
    @GuardedBy("this")
    private boolean mInDozeMode;
    private final InterfaceParams mInterfaceParams;
    private final IpClient.Callback mIpClientCallback;
    @GuardedBy("this")
    private ApfProgramEvent mLastInstallEvent;
    @GuardedBy("this")
    private byte[] mLastInstalledProgram;
    @GuardedBy("this")
    private long mLastInstalledProgramMinLifetime;
    @GuardedBy("this")
    private long mLastTimeInstalledProgram;
    /* access modifiers changed from: private */
    public final IpConnectivityLog mMetricsLog;
    @GuardedBy("this")
    private boolean mMulticastFilter;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public int mNumProgramUpdates = 0;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public int mNumProgramUpdatesAllowingMulticast = 0;
    @GuardedBy("this")
    private ArrayList<Ra> mRas = new ArrayList<>();
    @VisibleForTesting
    ReceiveThread mReceiveThread;
    @GuardedBy("this")
    private long mUniqueCounter;

    public static class ApfConfiguration {
        public ApfCapabilities apfCapabilities;
        public int[] ethTypeBlackList;
        public boolean ieee802_3Filter;
        public boolean multicastFilter;
    }

    @VisibleForTesting
    private enum Counter {
        RESERVED_OOB,
        TOTAL_PACKETS,
        PASSED_ARP,
        PASSED_DHCP,
        PASSED_IPV4,
        PASSED_IPV6_NON_ICMP,
        PASSED_IPV4_UNICAST,
        PASSED_IPV6_ICMP,
        PASSED_IPV6_UNICAST_NON_ICMP,
        PASSED_ARP_NON_IPV4,
        PASSED_ARP_UNKNOWN,
        PASSED_ARP_UNICAST_REPLY,
        PASSED_NON_IP_UNICAST,
        DROPPED_ETH_BROADCAST,
        DROPPED_RA,
        DROPPED_GARP_REPLY,
        DROPPED_ARP_OTHER_HOST,
        DROPPED_IPV4_L2_BROADCAST,
        DROPPED_IPV4_BROADCAST_ADDR,
        DROPPED_IPV4_BROADCAST_NET,
        DROPPED_IPV4_MULTICAST,
        DROPPED_IPV6_ROUTER_SOLICITATION,
        DROPPED_IPV6_MULTICAST_NA,
        DROPPED_IPV6_MULTICAST,
        DROPPED_IPV6_MULTICAST_PING,
        DROPPED_IPV6_NON_ICMP_MULTICAST,
        DROPPED_802_3_FRAME,
        DROPPED_ETHERTYPE_BLACKLISTED;

        public int offset() {
            return (-ordinal()) * 4;
        }

        public static int totalSize() {
            return (((Counter[]) Counter.class.getEnumConstants()).length - 1) * 4;
        }
    }

    public static class InvalidRaException extends Exception {
        public InvalidRaException(String m) {
            super(m);
        }
    }

    private enum ProcessRaResult {
        MATCH,
        DROPPED,
        PARSE_ERROR,
        ZERO_LIFETIME,
        UPDATE_NEW_RA,
        UPDATE_EXPIRY
    }

    @VisibleForTesting
    class Ra {
        private static final int ICMP6_4_BYTE_LIFETIME_LEN = 4;
        private static final int ICMP6_4_BYTE_LIFETIME_OFFSET = 4;
        private static final int ICMP6_DNSSL_OPTION_TYPE = 31;
        private static final int ICMP6_PREFIX_OPTION_LEN = 32;
        private static final int ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_LEN = 4;
        private static final int ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET = 8;
        private static final int ICMP6_PREFIX_OPTION_TYPE = 3;
        private static final int ICMP6_PREFIX_OPTION_VALID_LIFETIME_LEN = 4;
        private static final int ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET = 4;
        private static final int ICMP6_RA_CHECKSUM_LEN = 2;
        private static final int ICMP6_RA_CHECKSUM_OFFSET = 56;
        private static final int ICMP6_RA_HEADER_LEN = 16;
        private static final int ICMP6_RA_OPTION_OFFSET = 70;
        private static final int ICMP6_RA_ROUTER_LIFETIME_LEN = 2;
        private static final int ICMP6_RA_ROUTER_LIFETIME_OFFSET = 60;
        private static final int ICMP6_RDNSS_OPTION_TYPE = 25;
        private static final int ICMP6_ROUTE_INFO_OPTION_TYPE = 24;
        long mLastSeen;
        long mMinLifetime;
        private final ArrayList<Pair<Integer, Integer>> mNonLifetimes = new ArrayList<>();
        private final ByteBuffer mPacket;
        private final ArrayList<Integer> mPrefixOptionOffsets = new ArrayList<>();
        private final ArrayList<Integer> mRdnssOptionOffsets = new ArrayList<>();
        int seenCount = 0;

        /* access modifiers changed from: package-private */
        public String getLastMatchingPacket() {
            return HexDump.toHexString(this.mPacket.array(), 0, this.mPacket.capacity(), false);
        }

        private String IPv6AddresstoString(int pos) {
            try {
                byte[] array = this.mPacket.array();
                if (pos >= 0 && pos + 16 <= array.length) {
                    if (pos + 16 >= pos) {
                        return ((Inet6Address) InetAddress.getByAddress(Arrays.copyOfRange(array, pos, pos + 16))).getHostAddress();
                    }
                }
                return "???";
            } catch (UnsupportedOperationException e) {
                return "???";
            } catch (ClassCastException | UnknownHostException e2) {
                return "???";
            }
        }

        private void prefixOptionToString(StringBuffer sb, int offset) {
            sb.append(String.format("%s/%d %ds/%ds ", new Object[]{IPv6AddresstoString(offset + 16), Integer.valueOf(BitUtils.getUint8(this.mPacket, offset + 2)), Long.valueOf(BitUtils.getUint32(this.mPacket, offset + 4)), Long.valueOf(BitUtils.getUint32(this.mPacket, offset + 8))}));
        }

        private void rdnssOptionToString(StringBuffer sb, int offset) {
            int optLen = BitUtils.getUint8(this.mPacket, offset + 1) * 8;
            if (optLen >= 24) {
                long lifetime = BitUtils.getUint32(this.mPacket, offset + 4);
                int numServers = (optLen - 8) / 16;
                sb.append("DNS ");
                sb.append(lifetime);
                sb.append("s");
                for (int server = 0; server < numServers; server++) {
                    sb.append(" ");
                    sb.append(IPv6AddresstoString(offset + 8 + (16 * server)));
                }
            }
        }

        public String toString() {
            try {
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("RA %s -> %s %ds ", new Object[]{IPv6AddresstoString(22), IPv6AddresstoString(38), Integer.valueOf(BitUtils.getUint16(this.mPacket, 60))}));
                Iterator<Integer> it = this.mPrefixOptionOffsets.iterator();
                while (it.hasNext()) {
                    prefixOptionToString(sb, it.next().intValue());
                }
                Iterator<Integer> it2 = this.mRdnssOptionOffsets.iterator();
                while (it2.hasNext()) {
                    rdnssOptionToString(sb, it2.next().intValue());
                }
                return sb.toString();
            } catch (IndexOutOfBoundsException | BufferUnderflowException e) {
                return "<Malformed RA>";
            }
        }

        private int addNonLifetime(int lastNonLifetimeStart, int lifetimeOffset, int lifetimeLength) {
            int lifetimeOffset2 = lifetimeOffset + this.mPacket.position();
            this.mNonLifetimes.add(new Pair(Integer.valueOf(lastNonLifetimeStart), Integer.valueOf(lifetimeOffset2 - lastNonLifetimeStart)));
            return lifetimeOffset2 + lifetimeLength;
        }

        private int addNonLifetimeU32(int lastNonLifetimeStart) {
            return addNonLifetime(lastNonLifetimeStart, 4, 4);
        }

        Ra(byte[] packet, int length) throws InvalidRaException {
            if (length >= 70) {
                this.mPacket = ByteBuffer.wrap(Arrays.copyOf(packet, length));
                this.mLastSeen = ApfFilter.this.currentTimeSeconds();
                if (BitUtils.getUint16(this.mPacket, 12) == OsConstants.ETH_P_IPV6 && BitUtils.getUint8(this.mPacket, 20) == OsConstants.IPPROTO_ICMPV6 && BitUtils.getUint8(this.mPacket, 54) == 134) {
                    RaEvent.Builder builder = new RaEvent.Builder();
                    int lastNonLifetimeStart = addNonLifetime(addNonLifetime(addNonLifetime(0, 15, 3), 56, 2), 60, 2);
                    builder.updateRouterLifetime((long) BitUtils.getUint16(this.mPacket, 60));
                    this.mPacket.position(70);
                    while (this.mPacket.hasRemaining()) {
                        int position = this.mPacket.position();
                        int optionType = BitUtils.getUint8(this.mPacket, position);
                        int optionLength = BitUtils.getUint8(this.mPacket, position + 1) * 8;
                        if (optionType == 3) {
                            int lastNonLifetimeStart2 = addNonLifetime(lastNonLifetimeStart, 4, 4);
                            builder.updatePrefixValidLifetime(BitUtils.getUint32(this.mPacket, position + 4));
                            lastNonLifetimeStart = addNonLifetime(lastNonLifetimeStart2, 8, 4);
                            builder.updatePrefixPreferredLifetime(BitUtils.getUint32(this.mPacket, position + 8));
                            this.mPrefixOptionOffsets.add(Integer.valueOf(position));
                        } else if (optionType != 31) {
                            switch (optionType) {
                                case 24:
                                    lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                                    builder.updateRouteInfoLifetime(BitUtils.getUint32(this.mPacket, position + 4));
                                    break;
                                case 25:
                                    this.mRdnssOptionOffsets.add(Integer.valueOf(position));
                                    lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                                    builder.updateRdnssLifetime(BitUtils.getUint32(this.mPacket, position + 4));
                                    break;
                            }
                        } else {
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateDnsslLifetime(BitUtils.getUint32(this.mPacket, position + 4));
                        }
                        if (optionLength > 0) {
                            this.mPacket.position(position + optionLength);
                        } else {
                            throw new InvalidRaException(String.format("Invalid option length opt=%d len=%d", new Object[]{Integer.valueOf(optionType), Integer.valueOf(optionLength)}));
                        }
                    }
                    addNonLifetime(lastNonLifetimeStart, 0, 0);
                    this.mMinLifetime = minLifetime(packet, length);
                    ApfFilter.this.mMetricsLog.log(builder.build());
                    return;
                }
                throw new InvalidRaException("Not an ICMP6 router advertisement");
            }
            throw new InvalidRaException("Not an ICMP6 router advertisement");
        }

        /* access modifiers changed from: package-private */
        public boolean matches(byte[] packet, int length) {
            if (length != this.mPacket.capacity()) {
                return false;
            }
            byte[] referencePacket = this.mPacket.array();
            Iterator<Pair<Integer, Integer>> it = this.mNonLifetimes.iterator();
            while (it.hasNext()) {
                Pair<Integer, Integer> nonLifetime = it.next();
                int i = ((Integer) nonLifetime.first).intValue();
                while (true) {
                    if (i < ((Integer) nonLifetime.first).intValue() + ((Integer) nonLifetime.second).intValue()) {
                        if (packet[i] != referencePacket[i]) {
                            return false;
                        }
                        i++;
                    }
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public long minLifetime(byte[] packet, int length) {
            long optionLifetime;
            long minLifetime = JobStatus.NO_LATEST_RUNTIME;
            ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
            for (int i = 0; i + 1 < this.mNonLifetimes.size(); i++) {
                int offset = ((Integer) this.mNonLifetimes.get(i).first).intValue() + ((Integer) this.mNonLifetimes.get(i).second).intValue();
                if (!(offset == 15 || offset == 56)) {
                    int lifetimeLength = ((Integer) this.mNonLifetimes.get(i + 1).first).intValue() - offset;
                    if (lifetimeLength == 2) {
                        optionLifetime = (long) BitUtils.getUint16(byteBuffer, offset);
                    } else if (lifetimeLength == 4) {
                        optionLifetime = BitUtils.getUint32(byteBuffer, offset);
                    } else {
                        throw new IllegalStateException("bogus lifetime size " + lifetimeLength);
                    }
                    minLifetime = Math.min(minLifetime, optionLifetime);
                }
            }
            return minLifetime;
        }

        /* access modifiers changed from: package-private */
        public long currentLifetime() {
            return this.mMinLifetime - (ApfFilter.this.currentTimeSeconds() - this.mLastSeen);
        }

        /* access modifiers changed from: package-private */
        public boolean isExpired() {
            return currentLifetime() <= 0;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("ApfFilter.this")
        public long generateFilterLocked(ApfGenerator gen) throws ApfGenerator.IllegalInstructionException {
            String nextFilterLabel = "Ra" + ApfFilter.this.getUniqueNumberLocked();
            gen.addLoadFromMemory(ApfGenerator.Register.R0, 14);
            gen.addJumpIfR0NotEquals(this.mPacket.capacity(), nextFilterLabel);
            int filterLifetime = (int) (currentLifetime() / 6);
            gen.addLoadFromMemory(ApfGenerator.Register.R0, 15);
            gen.addJumpIfR0GreaterThan(filterLifetime, nextFilterLabel);
            for (int i = 0; i < this.mNonLifetimes.size(); i++) {
                Pair<Integer, Integer> nonLifetime = this.mNonLifetimes.get(i);
                if (((Integer) nonLifetime.second).intValue() != 0) {
                    gen.addLoadImmediate(ApfGenerator.Register.R0, ((Integer) nonLifetime.first).intValue());
                    gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, Arrays.copyOfRange(this.mPacket.array(), ((Integer) nonLifetime.first).intValue(), ((Integer) nonLifetime.first).intValue() + ((Integer) nonLifetime.second).intValue()), nextFilterLabel);
                }
                if (i + 1 < this.mNonLifetimes.size()) {
                    Pair<Integer, Integer> nextNonLifetime = this.mNonLifetimes.get(i + 1);
                    int offset = ((Integer) nonLifetime.first).intValue() + ((Integer) nonLifetime.second).intValue();
                    if (!(offset == 15 || offset == 56)) {
                        int length = ((Integer) nextNonLifetime.first).intValue() - offset;
                        if (length == 2) {
                            gen.addLoad16(ApfGenerator.Register.R0, offset);
                        } else if (length == 4) {
                            gen.addLoad32(ApfGenerator.Register.R0, offset);
                        } else {
                            throw new IllegalStateException("bogus lifetime size " + length);
                        }
                        gen.addJumpIfR0LessThan(filterLifetime, nextFilterLabel);
                    }
                }
            }
            ApfFilter.this.maybeSetCounter(gen, Counter.DROPPED_RA);
            gen.addJump(ApfFilter.this.mCountAndDropLabel);
            gen.defineLabel(nextFilterLabel);
            return (long) filterLifetime;
        }
    }

    @VisibleForTesting
    class ReceiveThread extends Thread {
        private final byte[] mPacket = new byte[1514];
        private final FileDescriptor mSocket;
        private final long mStart = SystemClock.elapsedRealtime();
        private final ApfStats mStats = new ApfStats();
        private volatile boolean mStopped;

        public ReceiveThread(FileDescriptor socket) {
            this.mSocket = socket;
        }

        public void halt() {
            this.mStopped = true;
            try {
                IoBridge.closeAndSignalBlockedThreads(this.mSocket);
            } catch (IOException e) {
            }
        }

        public void run() {
            ApfFilter.this.log("begin monitoring");
            while (!this.mStopped) {
                try {
                    updateStats(ApfFilter.this.processRa(this.mPacket, Os.read(this.mSocket, this.mPacket, 0, this.mPacket.length)));
                } catch (ErrnoException | IOException e) {
                    if (!this.mStopped) {
                        Log.e(ApfFilter.TAG, "Read error", e);
                    }
                }
            }
            logStats();
        }

        private void updateStats(ProcessRaResult result) {
            this.mStats.receivedRas++;
            switch (result) {
                case MATCH:
                    this.mStats.matchingRas++;
                    return;
                case DROPPED:
                    this.mStats.droppedRas++;
                    return;
                case PARSE_ERROR:
                    this.mStats.parseErrors++;
                    return;
                case ZERO_LIFETIME:
                    this.mStats.zeroLifetimeRas++;
                    return;
                case UPDATE_EXPIRY:
                    this.mStats.matchingRas++;
                    this.mStats.programUpdates++;
                    return;
                case UPDATE_NEW_RA:
                    this.mStats.programUpdates++;
                    return;
                default:
                    return;
            }
        }

        private void logStats() {
            long nowMs = SystemClock.elapsedRealtime();
            synchronized (this) {
                this.mStats.durationMs = nowMs - this.mStart;
                this.mStats.maxProgramSize = ApfFilter.this.mApfCapabilities.maximumApfProgramSize;
                this.mStats.programUpdatesAll = ApfFilter.this.mNumProgramUpdates;
                this.mStats.programUpdatesAllowingMulticast = ApfFilter.this.mNumProgramUpdatesAllowingMulticast;
                ApfFilter.this.mMetricsLog.log(this.mStats);
                ApfFilter.this.logApfProgramEventLocked(nowMs / 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    public void maybeSetCounter(ApfGenerator gen, Counter c) {
        if (this.mApfCapabilities.hasDataAccess()) {
            gen.addLoadImmediate(ApfGenerator.Register.R1, c.offset());
        }
    }

    @VisibleForTesting
    ApfFilter(Context context, ApfConfiguration config, InterfaceParams ifParams, IpClient.Callback ipClientCallback, IpConnectivityLog log) {
        this.mApfCapabilities = config.apfCapabilities;
        this.mIpClientCallback = ipClientCallback;
        this.mInterfaceParams = ifParams;
        this.mMulticastFilter = config.multicastFilter;
        this.mDrop802_3Frames = config.ieee802_3Filter;
        this.mContext = context;
        if (this.mApfCapabilities.hasDataAccess()) {
            this.mCountAndPassLabel = "countAndPass";
            this.mCountAndDropLabel = "countAndDrop";
        } else {
            this.mCountAndPassLabel = ApfGenerator.PASS_LABEL;
            this.mCountAndDropLabel = ApfGenerator.DROP_LABEL;
        }
        this.mEthTypeBlackList = filterEthTypeBlackList(config.ethTypeBlackList);
        this.mMetricsLog = log;
        maybeStartFilter();
        this.mContext.registerReceiver(this.mDeviceIdleReceiver, new IntentFilter("android.os.action.DEVICE_IDLE_MODE_CHANGED"));
    }

    public synchronized void setDataSnapshot(byte[] data) {
        this.mDataSnapshot = data;
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        Log.d(TAG, "(" + this.mInterfaceParams.name + "): " + s);
    }

    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long getUniqueNumberLocked() {
        long j = this.mUniqueCounter;
        this.mUniqueCounter = 1 + j;
        return j;
    }

    @GuardedBy("this")
    private static int[] filterEthTypeBlackList(int[] ethTypeBlackList) {
        ArrayList<Integer> bl = new ArrayList<>();
        int length = ethTypeBlackList.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            int p = ethTypeBlackList[i];
            if (p >= 1536 && p <= 65535 && !bl.contains(Integer.valueOf(p))) {
                if (bl.size() == 20) {
                    Log.w(TAG, "Passed EthType Black List size too large (" + bl.size() + ") using top " + 20 + " protocols");
                    break;
                }
                bl.add(Integer.valueOf(p));
            }
            i++;
        }
        return bl.stream().mapToInt($$Lambda$ApfFilter$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void maybeStartFilter() {
        try {
            this.mHardwareAddress = this.mInterfaceParams.macAddr.toByteArray();
            synchronized (this) {
                if (this.mApfCapabilities.hasDataAccess()) {
                    this.mIpClientCallback.installPacketFilter(new byte[this.mApfCapabilities.maximumApfProgramSize]);
                }
                installNewProgramLocked();
            }
            FileDescriptor socket = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IPV6);
            Os.bind(socket, new PacketSocketAddress((short) OsConstants.ETH_P_IPV6, this.mInterfaceParams.index));
            NetworkUtils.attachRaFilter(socket, this.mApfCapabilities.apfPacketFormat);
            this.mReceiveThread = new ReceiveThread(socket);
            this.mReceiveThread.start();
        } catch (ErrnoException | SocketException e) {
            Log.e(TAG, "Error starting filter", e);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long currentTimeSeconds() {
        return SystemClock.elapsedRealtime() / 1000;
    }

    @GuardedBy("this")
    private void generateArpFilterLocked(ApfGenerator gen) throws ApfGenerator.IllegalInstructionException {
        gen.addLoadImmediate(ApfGenerator.Register.R0, 14);
        maybeSetCounter(gen, Counter.PASSED_ARP_NON_IPV4);
        gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, ARP_IPV4_HEADER, this.mCountAndPassLabel);
        gen.addLoad16(ApfGenerator.Register.R0, 20);
        gen.addJumpIfR0Equals(1, "checkTargetIPv4");
        maybeSetCounter(gen, Counter.PASSED_ARP_UNKNOWN);
        gen.addJumpIfR0NotEquals(2, this.mCountAndPassLabel);
        gen.addLoadImmediate(ApfGenerator.Register.R0, 0);
        maybeSetCounter(gen, Counter.PASSED_ARP_UNICAST_REPLY);
        gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, ETH_BROADCAST_MAC_ADDRESS, this.mCountAndPassLabel);
        gen.defineLabel("checkTargetIPv4");
        if (this.mIPv4Address == null) {
            gen.addLoad32(ApfGenerator.Register.R0, 38);
            maybeSetCounter(gen, Counter.DROPPED_GARP_REPLY);
            gen.addJumpIfR0Equals(0, this.mCountAndDropLabel);
        } else {
            gen.addLoadImmediate(ApfGenerator.Register.R0, 38);
            maybeSetCounter(gen, Counter.DROPPED_ARP_OTHER_HOST);
            gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, this.mIPv4Address, this.mCountAndDropLabel);
        }
        maybeSetCounter(gen, Counter.PASSED_ARP);
        gen.addJump(this.mCountAndPassLabel);
    }

    @GuardedBy("this")
    private void generateIPv4FilterLocked(ApfGenerator gen) throws ApfGenerator.IllegalInstructionException {
        if (this.mMulticastFilter) {
            gen.addLoad8(ApfGenerator.Register.R0, 23);
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_UDP, "skip_dhcp_v4_filter");
            gen.addLoad16(ApfGenerator.Register.R0, 20);
            gen.addJumpIfR0AnyBitsSet(8191, "skip_dhcp_v4_filter");
            gen.addLoadFromMemory(ApfGenerator.Register.R1, 13);
            gen.addLoad16Indexed(ApfGenerator.Register.R0, 16);
            gen.addJumpIfR0NotEquals(68, "skip_dhcp_v4_filter");
            gen.addLoadImmediate(ApfGenerator.Register.R0, 50);
            gen.addAddR1();
            gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, this.mHardwareAddress, "skip_dhcp_v4_filter");
            maybeSetCounter(gen, Counter.PASSED_DHCP);
            gen.addJump(this.mCountAndPassLabel);
            gen.defineLabel("skip_dhcp_v4_filter");
            gen.addLoad8(ApfGenerator.Register.R0, 30);
            gen.addAnd(240);
            maybeSetCounter(gen, Counter.DROPPED_IPV4_MULTICAST);
            gen.addJumpIfR0Equals(UsbDescriptor.CLASSID_WIRELESS, this.mCountAndDropLabel);
            maybeSetCounter(gen, Counter.DROPPED_IPV4_BROADCAST_ADDR);
            gen.addLoad32(ApfGenerator.Register.R0, 30);
            gen.addJumpIfR0Equals(-1, this.mCountAndDropLabel);
            if (this.mIPv4Address != null && this.mIPv4PrefixLength < 31) {
                maybeSetCounter(gen, Counter.DROPPED_IPV4_BROADCAST_NET);
                gen.addJumpIfR0Equals(ipv4BroadcastAddress(this.mIPv4Address, this.mIPv4PrefixLength), this.mCountAndDropLabel);
            }
            maybeSetCounter(gen, Counter.PASSED_IPV4_UNICAST);
            gen.addLoadImmediate(ApfGenerator.Register.R0, 0);
            gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, ETH_BROADCAST_MAC_ADDRESS, this.mCountAndPassLabel);
            maybeSetCounter(gen, Counter.DROPPED_IPV4_L2_BROADCAST);
            gen.addJump(this.mCountAndDropLabel);
        }
        maybeSetCounter(gen, Counter.PASSED_IPV4);
        gen.addJump(this.mCountAndPassLabel);
    }

    @GuardedBy("this")
    private void generateIPv6FilterLocked(ApfGenerator gen) throws ApfGenerator.IllegalInstructionException {
        gen.addLoad8(ApfGenerator.Register.R0, 20);
        if (this.mMulticastFilter) {
            if (this.mInDozeMode) {
                gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_ICMPV6, "dropAllIPv6Multicast");
                gen.addLoad8(ApfGenerator.Register.R0, 54);
                gen.addJumpIfR0NotEquals(128, "skipIPv6MulticastFilter");
            } else {
                gen.addJumpIfR0Equals(OsConstants.IPPROTO_ICMPV6, "skipIPv6MulticastFilter");
            }
            gen.defineLabel("dropAllIPv6Multicast");
            maybeSetCounter(gen, Counter.DROPPED_IPV6_NON_ICMP_MULTICAST);
            gen.addLoad8(ApfGenerator.Register.R0, 38);
            gen.addJumpIfR0Equals(255, this.mCountAndDropLabel);
            maybeSetCounter(gen, Counter.PASSED_IPV6_UNICAST_NON_ICMP);
            gen.addJump(this.mCountAndPassLabel);
            gen.defineLabel("skipIPv6MulticastFilter");
        } else {
            maybeSetCounter(gen, Counter.PASSED_IPV6_NON_ICMP);
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_ICMPV6, this.mCountAndPassLabel);
        }
        gen.addLoad8(ApfGenerator.Register.R0, 54);
        maybeSetCounter(gen, Counter.DROPPED_IPV6_ROUTER_SOLICITATION);
        gen.addJumpIfR0Equals(NetworkConstants.ICMPV6_ROUTER_SOLICITATION, this.mCountAndDropLabel);
        gen.addJumpIfR0NotEquals(NetworkConstants.ICMPV6_NEIGHBOR_ADVERTISEMENT, "skipUnsolicitedMulticastNA");
        gen.addLoadImmediate(ApfGenerator.Register.R0, 38);
        gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, IPV6_ALL_NODES_ADDRESS, "skipUnsolicitedMulticastNA");
        maybeSetCounter(gen, Counter.DROPPED_IPV6_MULTICAST_NA);
        gen.addJump(this.mCountAndDropLabel);
        gen.defineLabel("skipUnsolicitedMulticastNA");
    }

    @GuardedBy("this")
    private ApfGenerator emitPrologueLocked() throws ApfGenerator.IllegalInstructionException {
        ApfGenerator gen = new ApfGenerator(this.mApfCapabilities.apfVersionSupported);
        if (this.mApfCapabilities.hasDataAccess()) {
            maybeSetCounter(gen, Counter.TOTAL_PACKETS);
            gen.addLoadData(ApfGenerator.Register.R0, 0);
            gen.addAdd(1);
            gen.addStoreData(ApfGenerator.Register.R0, 0);
        }
        gen.addLoad16(ApfGenerator.Register.R0, 12);
        if (this.mDrop802_3Frames) {
            maybeSetCounter(gen, Counter.DROPPED_802_3_FRAME);
            gen.addJumpIfR0LessThan(1536, this.mCountAndDropLabel);
        }
        maybeSetCounter(gen, Counter.DROPPED_ETHERTYPE_BLACKLISTED);
        for (int p : this.mEthTypeBlackList) {
            gen.addJumpIfR0Equals(p, this.mCountAndDropLabel);
        }
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_ARP, "skipArpFilters");
        generateArpFilterLocked(gen);
        gen.defineLabel("skipArpFilters");
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_IP, "skipIPv4Filters");
        generateIPv4FilterLocked(gen);
        gen.defineLabel("skipIPv4Filters");
        gen.addJumpIfR0Equals(OsConstants.ETH_P_IPV6, "IPv6Filters");
        gen.addLoadImmediate(ApfGenerator.Register.R0, 0);
        maybeSetCounter(gen, Counter.PASSED_NON_IP_UNICAST);
        gen.addJumpIfBytesNotEqual(ApfGenerator.Register.R0, ETH_BROADCAST_MAC_ADDRESS, this.mCountAndPassLabel);
        maybeSetCounter(gen, Counter.DROPPED_ETH_BROADCAST);
        gen.addJump(this.mCountAndDropLabel);
        gen.defineLabel("IPv6Filters");
        generateIPv6FilterLocked(gen);
        return gen;
    }

    @GuardedBy("this")
    private void emitEpilogue(ApfGenerator gen) throws ApfGenerator.IllegalInstructionException {
        if (this.mApfCapabilities.hasDataAccess()) {
            maybeSetCounter(gen, Counter.PASSED_IPV6_ICMP);
            gen.defineLabel(this.mCountAndPassLabel);
            gen.addLoadData(ApfGenerator.Register.R0, 0);
            gen.addAdd(1);
            gen.addStoreData(ApfGenerator.Register.R0, 0);
            gen.addJump(ApfGenerator.PASS_LABEL);
            gen.defineLabel(this.mCountAndDropLabel);
            gen.addLoadData(ApfGenerator.Register.R0, 0);
            gen.addAdd(1);
            gen.addStoreData(ApfGenerator.Register.R0, 0);
            gen.addJump(ApfGenerator.DROP_LABEL);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("this")
    @VisibleForTesting
    public void installNewProgramLocked() {
        purgeExpiredRasLocked();
        ArrayList<Ra> rasToFilter = new ArrayList<>();
        long programMinLifetime = JobStatus.NO_LATEST_RUNTIME;
        long maximumApfProgramSize = (long) this.mApfCapabilities.maximumApfProgramSize;
        if (this.mApfCapabilities.hasDataAccess()) {
            maximumApfProgramSize -= (long) Counter.totalSize();
        }
        try {
            ApfGenerator gen = emitPrologueLocked();
            emitEpilogue(gen);
            if (((long) gen.programLengthOverEstimate()) > maximumApfProgramSize) {
                Log.e(TAG, "Program exceeds maximum size " + maximumApfProgramSize);
                return;
            }
            Iterator<Ra> it = this.mRas.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Ra ra = it.next();
                ra.generateFilterLocked(gen);
                if (((long) gen.programLengthOverEstimate()) > maximumApfProgramSize) {
                    break;
                }
                rasToFilter.add(ra);
            }
            ApfGenerator gen2 = emitPrologueLocked();
            Iterator<Ra> it2 = rasToFilter.iterator();
            while (it2.hasNext()) {
                programMinLifetime = Math.min(programMinLifetime, it2.next().generateFilterLocked(gen2));
            }
            emitEpilogue(gen2);
            byte[] program = gen2.generate();
            boolean z = false;
            this.mCurrentMulticastFilter = 0;
            Log.d(TAG, "get multicast filter program size=" + program.length + ", program=" + Arrays.toString(program));
            long now = currentTimeSeconds();
            this.mLastTimeInstalledProgram = now;
            this.mLastInstalledProgramMinLifetime = programMinLifetime;
            this.mLastInstalledProgram = program;
            this.mNumProgramUpdates = this.mNumProgramUpdates + 1;
            this.mIpClientCallback.installPacketFilter(program);
            logApfProgramEventLocked(now);
            this.mLastInstallEvent = new ApfProgramEvent();
            this.mLastInstallEvent.lifetime = programMinLifetime;
            this.mLastInstallEvent.filteredRas = rasToFilter.size();
            this.mLastInstallEvent.currentRas = this.mRas.size();
            this.mLastInstallEvent.programLength = program.length;
            ApfProgramEvent apfProgramEvent = this.mLastInstallEvent;
            if (this.mIPv4Address != null) {
                z = true;
            }
            apfProgramEvent.flags = ApfProgramEvent.flagsFor(z, this.mMulticastFilter);
        } catch (ApfGenerator.IllegalInstructionException | IllegalStateException e) {
            Log.e(TAG, "Failed to generate APF program.", e);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("this")
    public void logApfProgramEventLocked(long now) {
        if (this.mLastInstallEvent != null) {
            ApfProgramEvent ev = this.mLastInstallEvent;
            this.mLastInstallEvent = null;
            ev.actualLifetime = now - this.mLastTimeInstalledProgram;
            if (ev.actualLifetime >= 2) {
                this.mMetricsLog.log(ev);
            }
        }
    }

    private boolean shouldInstallnewProgram() {
        return this.mLastTimeInstalledProgram + this.mLastInstalledProgramMinLifetime < currentTimeSeconds() + MAX_PROGRAM_LIFETIME_WORTH_REFRESHING;
    }

    private void hexDump(String msg, byte[] packet, int length) {
        log(msg + HexDump.toHexString(packet, 0, length, false));
    }

    @GuardedBy("this")
    private void purgeExpiredRasLocked() {
        int i = 0;
        while (i < this.mRas.size()) {
            if (this.mRas.get(i).isExpired()) {
                log("Expiring " + this.mRas.get(i));
                this.mRas.remove(i);
            } else {
                i++;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public synchronized ProcessRaResult processRa(byte[] packet, int length) {
        for (int i = 0; i < this.mRas.size(); i++) {
            Ra ra = this.mRas.get(i);
            if (ra.matches(packet, length)) {
                ra.mLastSeen = currentTimeSeconds();
                ra.mMinLifetime = ra.minLifetime(packet, length);
                ra.seenCount++;
                this.mRas.add(0, this.mRas.remove(i));
                if (shouldInstallnewProgram()) {
                    installNewProgramLocked();
                    return ProcessRaResult.UPDATE_EXPIRY;
                }
                return ProcessRaResult.MATCH;
            }
        }
        purgeExpiredRasLocked();
        if (this.mRas.size() >= 10) {
            return ProcessRaResult.DROPPED;
        }
        try {
            Ra ra2 = new Ra(packet, length);
            if (ra2.isExpired()) {
                return ProcessRaResult.ZERO_LIFETIME;
            }
            log("Adding " + ra2);
            this.mRas.add(ra2);
            installNewProgramLocked();
            return ProcessRaResult.UPDATE_NEW_RA;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RA", e);
            return ProcessRaResult.PARSE_ERROR;
        }
    }

    public static ApfFilter maybeCreate(Context context, ApfConfiguration config, InterfaceParams ifParams, IpClient.Callback ipClientCallback) {
        if (context == null || config == null || ifParams == null) {
            return null;
        }
        ApfCapabilities apfCapabilities = config.apfCapabilities;
        if (apfCapabilities == null || apfCapabilities.apfVersionSupported == 0) {
            return null;
        }
        if (apfCapabilities.maximumApfProgramSize < 512) {
            Log.e(TAG, "Unacceptably small APF limit: " + apfCapabilities.maximumApfProgramSize);
            return null;
        } else if (apfCapabilities.apfPacketFormat != OsConstants.ARPHRD_ETHER) {
            return null;
        } else {
            if (!ApfGenerator.supportsVersion(apfCapabilities.apfVersionSupported)) {
                Log.e(TAG, "Unsupported APF version: " + apfCapabilities.apfVersionSupported);
                return null;
            }
            ApfFilter apfFilter = new ApfFilter(context, config, ifParams, ipClientCallback, new IpConnectivityLog());
            return apfFilter;
        }
    }

    public synchronized void shutdown() {
        if (this.mReceiveThread != null) {
            log("shutting down");
            this.mReceiveThread.halt();
            this.mReceiveThread = null;
        }
        this.mRas.clear();
        this.mContext.unregisterReceiver(this.mDeviceIdleReceiver);
    }

    public synchronized void setMulticastFilter(boolean isEnabled) {
        Log.d(TAG, "setMulticastFilter: mMulticastFilter=" + this.mMulticastFilter + ", isEnabled=" + isEnabled);
        if (this.mCurrentMulticastFilter != 0 || this.mMulticastFilter != isEnabled) {
            this.mMulticastFilter = isEnabled;
            if (!isEnabled) {
                this.mNumProgramUpdatesAllowingMulticast++;
            }
            installNewProgramLocked();
        }
    }

    public synchronized void setScreenOffMulticastFilter(boolean isEnabled) {
        Log.d(TAG, "setScreenOffMulticastFilter:" + isEnabled + ", CurrentMulticastFilter=" + this.mCurrentMulticastFilter + ", mMulticastFilter=" + this.mMulticastFilter);
        if (this.mCurrentMulticastFilter != 1 || this.mMulticastFilter != isEnabled) {
            this.mCurrentMulticastFilter = 1;
            setMulticastFilter(isEnabled);
        }
    }

    @VisibleForTesting
    public synchronized void setDozeMode(boolean isEnabled) {
        if (this.mInDozeMode != isEnabled) {
            this.mInDozeMode = isEnabled;
            installNewProgramLocked();
        }
    }

    private static LinkAddress findIPv4LinkAddress(LinkProperties lp) {
        LinkAddress ipv4Address = null;
        for (LinkAddress address : lp.getLinkAddresses()) {
            if (address.getAddress() instanceof Inet4Address) {
                if (ipv4Address != null && !ipv4Address.isSameAddressAs(address)) {
                    return null;
                }
                ipv4Address = address;
            }
        }
        return ipv4Address;
    }

    public synchronized void setLinkProperties(LinkProperties lp) {
        LinkAddress ipv4Address = findIPv4LinkAddress(lp);
        byte[] addr = ipv4Address != null ? ipv4Address.getAddress().getAddress() : null;
        int prefix = ipv4Address != null ? ipv4Address.getPrefixLength() : 0;
        if (prefix != this.mIPv4PrefixLength || !Arrays.equals(addr, this.mIPv4Address)) {
            this.mIPv4Address = addr;
            this.mIPv4PrefixLength = prefix;
            installNewProgramLocked();
        }
    }

    public static long counterValue(byte[] data, Counter counter) throws ArrayIndexOutOfBoundsException {
        int offset = counter.offset();
        if (offset < 0) {
            offset += data.length;
        }
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | ((long) (data[offset] & 255));
            offset++;
        }
        return value;
    }

    public synchronized void dump(IndentingPrintWriter pw) {
        pw.println("Capabilities: " + this.mApfCapabilities);
        StringBuilder sb = new StringBuilder();
        sb.append("Receive thread: ");
        sb.append(this.mReceiveThread != null ? "RUNNING" : "STOPPED");
        pw.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Multicast: ");
        sb2.append(this.mMulticastFilter ? "DROP" : "ALLOW");
        pw.println(sb2.toString());
        try {
            pw.println("IPv4 address: " + InetAddress.getByAddress(this.mIPv4Address).getHostAddress());
        } catch (NullPointerException | UnknownHostException e) {
        }
        if (this.mLastTimeInstalledProgram == 0) {
            pw.println("No program installed.");
            return;
        }
        pw.println("Program updates: " + this.mNumProgramUpdates);
        pw.println(String.format("Last program length %d, installed %ds ago, lifetime %ds", new Object[]{Integer.valueOf(this.mLastInstalledProgram.length), Long.valueOf(currentTimeSeconds() - this.mLastTimeInstalledProgram), Long.valueOf(this.mLastInstalledProgramMinLifetime)}));
        pw.println("RA filters:");
        pw.increaseIndent();
        Iterator<Ra> it = this.mRas.iterator();
        while (it.hasNext()) {
            Ra ra = it.next();
            pw.println(ra);
            pw.increaseIndent();
            pw.println(String.format("Seen: %d, last %ds ago", new Object[]{Integer.valueOf(ra.seenCount), Long.valueOf(currentTimeSeconds() - ra.mLastSeen)}));
            pw.println("Last match:");
            pw.increaseIndent();
            pw.println(ra.getLastMatchingPacket());
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
        pw.println("Last program:");
        pw.increaseIndent();
        pw.println(HexDump.toHexString(this.mLastInstalledProgram, false));
        pw.decreaseIndent();
        pw.println("APF packet counters: ");
        pw.increaseIndent();
        if (!this.mApfCapabilities.hasDataAccess()) {
            pw.println("APF counters not supported");
        } else if (this.mDataSnapshot == null) {
            pw.println("No last snapshot.");
        } else {
            try {
                Counter[] counters = (Counter[]) Counter.class.getEnumConstants();
                for (Counter c : Arrays.asList(counters).subList(1, counters.length)) {
                    long value = counterValue(this.mDataSnapshot, c);
                    if (value != 0) {
                        pw.println(c.toString() + ": " + value);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e2) {
                pw.println("Uh-oh: " + e2);
            }
        }
        pw.decreaseIndent();
    }

    @VisibleForTesting
    public static int ipv4BroadcastAddress(byte[] addrBytes, int prefixLength) {
        return BitUtils.bytesToBEInt(addrBytes) | ((int) (BitUtils.uint32(-1) >>> prefixLength));
    }
}
