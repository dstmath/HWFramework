package android.net.apf;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.apf.ApfGenerator.IllegalInstructionException;
import android.net.apf.ApfGenerator.Register;
import android.net.ip.IpManager.Callback;
import android.net.metrics.ApfProgramEvent;
import android.net.metrics.ApfStats;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.RaEvent.Builder;
import android.net.util.NetworkConstants;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.controllers.JobStatus;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import libcore.io.IoBridge;

public class ApfFilter {
    private static final int APF_PROGRAM_EVENT_LIFETIME_THRESHOLD = 2;
    private static final int ARP_HEADER_OFFSET = 14;
    private static final byte[] ARP_IPV4_HEADER = new byte[]{(byte) 0, (byte) 1, (byte) 8, (byte) 0, (byte) 6, (byte) 4};
    private static final int ARP_OPCODE_OFFSET = 20;
    private static final short ARP_OPCODE_REPLY = (short) 2;
    private static final short ARP_OPCODE_REQUEST = (short) 1;
    private static final int ARP_TARGET_IP_ADDRESS_OFFSET = 38;
    private static final boolean DBG = true;
    private static final int DHCP_CLIENT_MAC_OFFSET = 50;
    private static final int DHCP_CLIENT_PORT = 68;
    private static final byte[] ETH_BROADCAST_MAC_ADDRESS = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
    private static final int ETH_DEST_ADDR_OFFSET = 0;
    private static final int ETH_ETHERTYPE_OFFSET = 12;
    private static final int ETH_HEADER_LEN = 14;
    private static final int ETH_TYPE_MIN = 1536;
    private static final int FRACTION_OF_LIFETIME_TO_FILTER = 6;
    private static final int ICMP6_NEIGHBOR_ANNOUNCEMENT = 136;
    private static final int ICMP6_NEIGHBOR_SOLICITATION = 135;
    private static final int ICMP6_ROUTER_ADVERTISEMENT = 134;
    private static final int ICMP6_ROUTER_SOLICITATION = 133;
    private static final int ICMP6_TYPE_OFFSET = 54;
    private static final int IPV4_ANY_HOST_ADDRESS = 0;
    private static final int IPV4_BROADCAST_ADDRESS = -1;
    private static final int IPV4_DEST_ADDR_OFFSET = 30;
    private static final int IPV4_FRAGMENT_OFFSET_MASK = 8191;
    private static final int IPV4_FRAGMENT_OFFSET_OFFSET = 20;
    private static final int IPV4_PROTOCOL_OFFSET = 23;
    private static final byte[] IPV6_ALL_NODES_ADDRESS = new byte[]{(byte) -1, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1};
    private static final int IPV6_DEST_ADDR_OFFSET = 38;
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
    private final ApfCapabilities mApfCapabilities;
    private int mCurrentMulticastFilter = 0;
    private final boolean mDrop802_3Frames;
    byte[] mHardwareAddress;
    @GuardedBy("this")
    private byte[] mIPv4Address;
    @GuardedBy("this")
    private int mIPv4PrefixLength;
    private final Callback mIpManagerCallback;
    @GuardedBy("this")
    private ApfProgramEvent mLastInstallEvent;
    @GuardedBy("this")
    private byte[] mLastInstalledProgram;
    @GuardedBy("this")
    private long mLastInstalledProgramMinLifetime;
    @GuardedBy("this")
    private long mLastTimeInstalledProgram;
    private final IpConnectivityLog mMetricsLog;
    @GuardedBy("this")
    private boolean mMulticastFilter;
    private final NetworkInterface mNetworkInterface;
    @GuardedBy("this")
    private int mNumProgramUpdates = 0;
    @GuardedBy("this")
    private int mNumProgramUpdatesAllowingMulticast = 0;
    @GuardedBy("this")
    private ArrayList<Ra> mRas = new ArrayList();
    ReceiveThread mReceiveThread;
    @GuardedBy("this")
    private long mUniqueCounter;

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
        private final ArrayList<Pair<Integer, Integer>> mNonLifetimes = new ArrayList();
        private final ByteBuffer mPacket;
        private final ArrayList<Integer> mPrefixOptionOffsets = new ArrayList();
        private final ArrayList<Integer> mRdnssOptionOffsets = new ArrayList();
        int seenCount = 0;

        String getLastMatchingPacket() {
            return HexDump.toHexString(this.mPacket.array(), 0, this.mPacket.capacity(), false);
        }

        /* JADX WARNING: Removed duplicated region for block: B:11:0x0026 A:{Splitter: B:0:0x0000, ExcHandler: java.lang.ClassCastException (e java.lang.ClassCastException)} */
        /* JADX WARNING: Missing block: B:13:0x002a, code:
            return "???";
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String IPv6AddresstoString(int pos) {
            try {
                byte[] array = this.mPacket.array();
                if (pos < 0 || pos + 16 > array.length || pos + 16 < pos) {
                    return "???";
                }
                return ((Inet6Address) InetAddress.getByAddress(Arrays.copyOfRange(array, pos, pos + 16))).getHostAddress();
            } catch (UnsupportedOperationException e) {
                return "???";
            } catch (ClassCastException e2) {
            }
        }

        private void prefixOptionToString(StringBuffer sb, int offset) {
            String prefix = IPv6AddresstoString(offset + 16);
            int length = ApfFilter.getUint8(this.mPacket, offset + 2);
            long valid = ApfFilter.getUint32(this.mPacket, offset + 4);
            long preferred = ApfFilter.getUint32(this.mPacket, offset + 8);
            sb.append(String.format("%s/%d %ds/%ds ", new Object[]{prefix, Integer.valueOf(length), Long.valueOf(valid), Long.valueOf(preferred)}));
        }

        private void rdnssOptionToString(StringBuffer sb, int offset) {
            int optLen = ApfFilter.getUint8(this.mPacket, offset + 1) * 8;
            if (optLen >= 24) {
                int numServers = (optLen - 8) / 16;
                sb.append("DNS ").append(ApfFilter.getUint32(this.mPacket, offset + 4)).append("s");
                for (int server = 0; server < numServers; server++) {
                    sb.append(" ").append(IPv6AddresstoString((offset + 8) + (server * 16)));
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:6:0x004d A:{Splitter: B:0:0x0000, ExcHandler: java.nio.BufferUnderflowException (e java.nio.BufferUnderflowException)} */
        /* JADX WARNING: Missing block: B:8:0x0051, code:
            return "<Malformed RA>";
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String toString() {
            try {
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("RA %s -> %s %ds ", new Object[]{IPv6AddresstoString(22), IPv6AddresstoString(38), Integer.valueOf(ApfFilter.getUint16(this.mPacket, 60))}));
                for (Integer intValue : this.mPrefixOptionOffsets) {
                    prefixOptionToString(sb, intValue.intValue());
                }
                for (Integer intValue2 : this.mRdnssOptionOffsets) {
                    rdnssOptionToString(sb, intValue2.intValue());
                }
                return sb.toString();
            } catch (BufferUnderflowException e) {
            }
        }

        private int addNonLifetime(int lastNonLifetimeStart, int lifetimeOffset, int lifetimeLength) {
            lifetimeOffset += this.mPacket.position();
            this.mNonLifetimes.add(new Pair(Integer.valueOf(lastNonLifetimeStart), Integer.valueOf(lifetimeOffset - lastNonLifetimeStart)));
            return lifetimeOffset + lifetimeLength;
        }

        private int addNonLifetimeU32(int lastNonLifetimeStart) {
            return addNonLifetime(lastNonLifetimeStart, 4, 4);
        }

        Ra(byte[] packet, int length) throws InvalidRaException {
            if (length < 70) {
                throw new InvalidRaException("Not an ICMP6 router advertisement");
            }
            this.mPacket = ByteBuffer.wrap(Arrays.copyOf(packet, length));
            this.mLastSeen = ApfFilter.this.currentTimeSeconds();
            if (ApfFilter.getUint16(this.mPacket, 12) == OsConstants.ETH_P_IPV6 && ApfFilter.getUint8(this.mPacket, 20) == OsConstants.IPPROTO_ICMPV6 && ApfFilter.getUint8(this.mPacket, 54) == 134) {
                Builder builder = new Builder();
                int lastNonLifetimeStart = addNonLifetime(addNonLifetime(0, 56, 2), 60, 2);
                builder.updateRouterLifetime((long) ApfFilter.getUint16(this.mPacket, 60));
                this.mPacket.position(70);
                while (this.mPacket.hasRemaining()) {
                    int position = this.mPacket.position();
                    int optionLength = ApfFilter.getUint8(this.mPacket, position + 1) * 8;
                    switch (ApfFilter.getUint8(this.mPacket, position)) {
                        case 3:
                            lastNonLifetimeStart = addNonLifetime(lastNonLifetimeStart, 4, 4);
                            builder.updatePrefixValidLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            lastNonLifetimeStart = addNonLifetime(lastNonLifetimeStart, 8, 4);
                            builder.updatePrefixPreferredLifetime(ApfFilter.getUint32(this.mPacket, position + 8));
                            this.mPrefixOptionOffsets.add(Integer.valueOf(position));
                            break;
                        case 24:
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateRouteInfoLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            break;
                        case 25:
                            this.mRdnssOptionOffsets.add(Integer.valueOf(position));
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateRdnssLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            break;
                        case 31:
                            lastNonLifetimeStart = addNonLifetimeU32(lastNonLifetimeStart);
                            builder.updateDnsslLifetime(ApfFilter.getUint32(this.mPacket, position + 4));
                            break;
                    }
                    if (optionLength <= 0) {
                        throw new InvalidRaException(String.format("Invalid option length opt=%d len=%d", new Object[]{Integer.valueOf(optionType), Integer.valueOf(optionLength)}));
                    }
                    this.mPacket.position(position + optionLength);
                }
                addNonLifetime(lastNonLifetimeStart, 0, 0);
                this.mMinLifetime = minLifetime(packet, length);
                ApfFilter.this.mMetricsLog.log(builder.build());
                return;
            }
            throw new InvalidRaException("Not an ICMP6 router advertisement");
        }

        boolean matches(byte[] packet, int length) {
            if (length != this.mPacket.capacity()) {
                return false;
            }
            byte[] referencePacket = this.mPacket.array();
            for (Pair<Integer, Integer> nonLifetime : this.mNonLifetimes) {
                int i = ((Integer) nonLifetime.first).intValue();
                while (true) {
                    if (i < ((Integer) nonLifetime.second).intValue() + ((Integer) nonLifetime.first).intValue()) {
                        if (packet[i] != referencePacket[i]) {
                            return false;
                        }
                        i++;
                    }
                }
            }
            return true;
        }

        long minLifetime(byte[] packet, int length) {
            long minLifetime = JobStatus.NO_LATEST_RUNTIME;
            ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
            for (int i = 0; i + 1 < this.mNonLifetimes.size(); i++) {
                int offset = ((Integer) ((Pair) this.mNonLifetimes.get(i)).first).intValue() + ((Integer) ((Pair) this.mNonLifetimes.get(i)).second).intValue();
                if (offset != 56) {
                    long optionLifetime;
                    int lifetimeLength = ((Integer) ((Pair) this.mNonLifetimes.get(i + 1)).first).intValue() - offset;
                    switch (lifetimeLength) {
                        case 2:
                            optionLifetime = (long) ApfFilter.getUint16(byteBuffer, offset);
                            break;
                        case 4:
                            optionLifetime = ApfFilter.getUint32(byteBuffer, offset);
                            break;
                        default:
                            throw new IllegalStateException("bogus lifetime size " + lifetimeLength);
                    }
                    minLifetime = Math.min(minLifetime, optionLifetime);
                }
            }
            return minLifetime;
        }

        long currentLifetime() {
            return this.mMinLifetime - (ApfFilter.this.currentTimeSeconds() - this.mLastSeen);
        }

        boolean isExpired() {
            return currentLifetime() <= 0;
        }

        @GuardedBy("ApfFilter.this")
        long generateFilterLocked(ApfGenerator gen) throws IllegalInstructionException {
            String nextFilterLabel = "Ra" + ApfFilter.this.getUniqueNumberLocked();
            gen.addLoadFromMemory(Register.R0, 14);
            gen.addJumpIfR0NotEquals(this.mPacket.capacity(), nextFilterLabel);
            int filterLifetime = (int) (currentLifetime() / 6);
            gen.addLoadFromMemory(Register.R0, 15);
            gen.addJumpIfR0GreaterThan(filterLifetime, nextFilterLabel);
            for (int i = 0; i < this.mNonLifetimes.size(); i++) {
                Pair<Integer, Integer> nonLifetime = (Pair) this.mNonLifetimes.get(i);
                if (((Integer) nonLifetime.second).intValue() != 0) {
                    gen.addLoadImmediate(Register.R0, ((Integer) nonLifetime.first).intValue());
                    gen.addJumpIfBytesNotEqual(Register.R0, Arrays.copyOfRange(this.mPacket.array(), ((Integer) nonLifetime.first).intValue(), ((Integer) nonLifetime.second).intValue() + ((Integer) nonLifetime.first).intValue()), nextFilterLabel);
                }
                if (i + 1 < this.mNonLifetimes.size()) {
                    Pair<Integer, Integer> nextNonLifetime = (Pair) this.mNonLifetimes.get(i + 1);
                    int offset = ((Integer) nonLifetime.first).intValue() + ((Integer) nonLifetime.second).intValue();
                    if (offset == 56) {
                        continue;
                    } else {
                        int length = ((Integer) nextNonLifetime.first).intValue() - offset;
                        switch (length) {
                            case 2:
                                gen.addLoad16(Register.R0, offset);
                                break;
                            case 4:
                                gen.addLoad32(Register.R0, offset);
                                break;
                            default:
                                throw new IllegalStateException("bogus lifetime size " + length);
                        }
                        gen.addJumpIfR0LessThan(filterLifetime, nextFilterLabel);
                    }
                }
            }
            gen.addJump(ApfGenerator.DROP_LABEL);
            gen.defineLabel(nextFilterLabel);
            return (long) filterLifetime;
        }
    }

    class ReceiveThread extends Thread {
        private static final /* synthetic */ int[] -android-net-apf-ApfFilter$ProcessRaResultSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$apf$ApfFilter$ProcessRaResult;
        private final byte[] mPacket = new byte[1514];
        private final FileDescriptor mSocket;
        private final long mStart = SystemClock.elapsedRealtime();
        private final ApfStats mStats = new ApfStats();
        private volatile boolean mStopped;

        private static /* synthetic */ int[] -getandroid-net-apf-ApfFilter$ProcessRaResultSwitchesValues() {
            if (-android-net-apf-ApfFilter$ProcessRaResultSwitchesValues != null) {
                return -android-net-apf-ApfFilter$ProcessRaResultSwitchesValues;
            }
            int[] iArr = new int[ProcessRaResult.values().length];
            try {
                iArr[ProcessRaResult.DROPPED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ProcessRaResult.MATCH.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ProcessRaResult.PARSE_ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ProcessRaResult.UPDATE_EXPIRY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ProcessRaResult.UPDATE_NEW_RA.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ProcessRaResult.ZERO_LIFETIME.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            -android-net-apf-ApfFilter$ProcessRaResultSwitchesValues = iArr;
            return iArr;
        }

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

        /* JADX WARNING: Removed duplicated region for block: B:6:0x0024 A:{Splitter: B:3:0x000c, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:6:0x0024, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:8:0x0027, code:
            if (r6.mStopped == false) goto L_0x0029;
     */
        /* JADX WARNING: Missing block: B:9:0x0029, code:
            android.util.Log.e(android.net.apf.ApfFilter.TAG, "Read error", r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            ApfFilter.this.log("begin monitoring");
            while (!this.mStopped) {
                try {
                    updateStats(ApfFilter.this.processRa(this.mPacket, Os.read(this.mSocket, this.mPacket, 0, this.mPacket.length)));
                } catch (Exception e) {
                }
            }
            logStats();
        }

        private void updateStats(ProcessRaResult result) {
            ApfStats apfStats = this.mStats;
            apfStats.receivedRas++;
            switch (-getandroid-net-apf-ApfFilter$ProcessRaResultSwitchesValues()[result.ordinal()]) {
                case 1:
                    apfStats = this.mStats;
                    apfStats.droppedRas++;
                    return;
                case 2:
                    apfStats = this.mStats;
                    apfStats.matchingRas++;
                    return;
                case 3:
                    apfStats = this.mStats;
                    apfStats.parseErrors++;
                    return;
                case 4:
                    apfStats = this.mStats;
                    apfStats.matchingRas++;
                    apfStats = this.mStats;
                    apfStats.programUpdates++;
                    return;
                case 5:
                    apfStats = this.mStats;
                    apfStats.programUpdates++;
                    return;
                case 6:
                    apfStats = this.mStats;
                    apfStats.zeroLifetimeRas++;
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

    ApfFilter(ApfCapabilities apfCapabilities, NetworkInterface networkInterface, Callback ipManagerCallback, boolean multicastFilter, boolean ieee802_3Filter, IpConnectivityLog log) {
        this.mApfCapabilities = apfCapabilities;
        this.mIpManagerCallback = ipManagerCallback;
        this.mNetworkInterface = networkInterface;
        this.mMulticastFilter = multicastFilter;
        this.mDrop802_3Frames = ieee802_3Filter;
        this.mMetricsLog = log;
        maybeStartFilter();
    }

    private void log(String s) {
        Log.d(TAG, "(" + this.mNetworkInterface.getName() + "): " + s);
    }

    @GuardedBy("this")
    private long getUniqueNumberLocked() {
        long j = this.mUniqueCounter;
        this.mUniqueCounter = 1 + j;
        return j;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f A:{Splitter: B:0:0x0000, ExcHandler: java.net.SocketException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x003f, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0040, code:
            android.util.Log.e(TAG, "Error starting filter", r1);
     */
    /* JADX WARNING: Missing block: B:16:0x0049, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void maybeStartFilter() {
        try {
            this.mHardwareAddress = this.mNetworkInterface.getHardwareAddress();
            synchronized (this) {
                installNewProgramLocked();
            }
            FileDescriptor socket = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, OsConstants.ETH_P_IPV6);
            Os.bind(socket, new PacketSocketAddress((short) OsConstants.ETH_P_IPV6, this.mNetworkInterface.getIndex()));
            NetworkUtils.attachRaFilter(socket, this.mApfCapabilities.apfPacketFormat);
            this.mReceiveThread = new ReceiveThread(socket);
            this.mReceiveThread.start();
        } catch (Exception e) {
        }
    }

    protected long currentTimeSeconds() {
        return SystemClock.elapsedRealtime() / 1000;
    }

    @GuardedBy("this")
    private void generateArpFilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        String checkTargetIPv4 = "checkTargetIPv4";
        gen.addLoadImmediate(Register.R0, 14);
        gen.addJumpIfBytesNotEqual(Register.R0, ARP_IPV4_HEADER, ApfGenerator.PASS_LABEL);
        gen.addLoad16(Register.R0, 20);
        gen.addJumpIfR0Equals(1, "checkTargetIPv4");
        gen.addJumpIfR0NotEquals(2, ApfGenerator.PASS_LABEL);
        gen.addLoadImmediate(Register.R0, 0);
        gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
        gen.defineLabel("checkTargetIPv4");
        if (this.mIPv4Address == null) {
            gen.addLoad32(Register.R0, 38);
            gen.addJumpIfR0Equals(0, ApfGenerator.DROP_LABEL);
        } else {
            gen.addLoadImmediate(Register.R0, 38);
            gen.addJumpIfBytesNotEqual(Register.R0, this.mIPv4Address, ApfGenerator.DROP_LABEL);
        }
        gen.addJump(ApfGenerator.PASS_LABEL);
    }

    @GuardedBy("this")
    private void generateIPv4FilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        if (this.mMulticastFilter) {
            String skipDhcpv4Filter = "skip_dhcp_v4_filter";
            gen.addLoad8(Register.R0, 23);
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_UDP, "skip_dhcp_v4_filter");
            gen.addLoad16(Register.R0, 20);
            gen.addJumpIfR0AnyBitsSet(8191, "skip_dhcp_v4_filter");
            gen.addLoadFromMemory(Register.R1, 13);
            gen.addLoad16Indexed(Register.R0, 16);
            gen.addJumpIfR0NotEquals(68, "skip_dhcp_v4_filter");
            gen.addLoadImmediate(Register.R0, 50);
            gen.addAddR1();
            gen.addJumpIfBytesNotEqual(Register.R0, this.mHardwareAddress, "skip_dhcp_v4_filter");
            gen.addJump(ApfGenerator.PASS_LABEL);
            gen.defineLabel("skip_dhcp_v4_filter");
            gen.addLoad8(Register.R0, 30);
            gen.addAnd(240);
            gen.addJumpIfR0Equals(224, ApfGenerator.DROP_LABEL);
            gen.addLoad32(Register.R0, 30);
            gen.addJumpIfR0Equals(-1, ApfGenerator.DROP_LABEL);
            if (this.mIPv4Address != null && this.mIPv4PrefixLength < 31) {
                gen.addJumpIfR0Equals(ipv4BroadcastAddress(this.mIPv4Address, this.mIPv4PrefixLength), ApfGenerator.DROP_LABEL);
            }
            gen.addLoadImmediate(Register.R0, 0);
            gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
            gen.addJump(ApfGenerator.DROP_LABEL);
        }
        gen.addJump(ApfGenerator.PASS_LABEL);
    }

    @GuardedBy("this")
    private void generateIPv6FilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        gen.addLoad8(Register.R0, 20);
        if (this.mMulticastFilter) {
            String skipIpv6MulticastFilterLabel = "skipIPv6MulticastFilter";
            gen.addJumpIfR0Equals(OsConstants.IPPROTO_ICMPV6, skipIpv6MulticastFilterLabel);
            gen.addLoad8(Register.R0, 38);
            gen.addJumpIfR0Equals(255, ApfGenerator.DROP_LABEL);
            gen.addJump(ApfGenerator.PASS_LABEL);
            gen.defineLabel(skipIpv6MulticastFilterLabel);
        } else {
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_ICMPV6, ApfGenerator.PASS_LABEL);
        }
        String skipUnsolicitedMulticastNALabel = "skipUnsolicitedMulticastNA";
        gen.addLoad8(Register.R0, 54);
        gen.addJumpIfR0Equals(133, ApfGenerator.DROP_LABEL);
        gen.addJumpIfR0NotEquals(136, skipUnsolicitedMulticastNALabel);
        gen.addLoadImmediate(Register.R0, 38);
        gen.addJumpIfBytesNotEqual(Register.R0, IPV6_ALL_NODES_ADDRESS, skipUnsolicitedMulticastNALabel);
        gen.addJump(ApfGenerator.DROP_LABEL);
        gen.defineLabel(skipUnsolicitedMulticastNALabel);
    }

    @GuardedBy("this")
    private ApfGenerator beginProgramLocked() throws IllegalInstructionException {
        ApfGenerator gen = new ApfGenerator();
        gen.setApfVersion(this.mApfCapabilities.apfVersionSupported);
        gen.addLoad16(Register.R0, 12);
        if (this.mDrop802_3Frames) {
            gen.addJumpIfR0LessThan(ETH_TYPE_MIN, ApfGenerator.DROP_LABEL);
        }
        String skipArpFiltersLabel = "skipArpFilters";
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_ARP, skipArpFiltersLabel);
        generateArpFilterLocked(gen);
        gen.defineLabel(skipArpFiltersLabel);
        String skipIPv4FiltersLabel = "skipIPv4Filters";
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_IP, skipIPv4FiltersLabel);
        generateIPv4FilterLocked(gen);
        gen.defineLabel(skipIPv4FiltersLabel);
        String ipv6FilterLabel = "IPv6Filters";
        gen.addJumpIfR0Equals(OsConstants.ETH_P_IPV6, ipv6FilterLabel);
        gen.addLoadImmediate(Register.R0, 0);
        gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
        gen.addJump(ApfGenerator.DROP_LABEL);
        gen.defineLabel(ipv6FilterLabel);
        generateIPv6FilterLocked(gen);
        return gen;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0052 A:{Splitter: B:1:0x000e, ExcHandler: android.net.apf.ApfGenerator.IllegalInstructionException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:13:0x0052, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:0x0053, code:
            android.util.Log.e(TAG, "Failed to generate APF program.", r0);
     */
    /* JADX WARNING: Missing block: B:15:0x005c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @GuardedBy("this")
    void installNewProgramLocked() {
        boolean z = false;
        purgeExpiredRasLocked();
        ArrayList<Ra> rasToFilter = new ArrayList();
        long programMinLifetime = JobStatus.NO_LATEST_RUNTIME;
        try {
            ApfGenerator gen = beginProgramLocked();
            for (Ra ra : this.mRas) {
                ra.generateFilterLocked(gen);
                if (gen.programLengthOverEstimate() > this.mApfCapabilities.maximumApfProgramSize) {
                    break;
                }
                rasToFilter.add(ra);
            }
            gen = beginProgramLocked();
            for (Ra ra2 : rasToFilter) {
                programMinLifetime = Math.min(programMinLifetime, ra2.generateFilterLocked(gen));
            }
            byte[] program = gen.generate();
            this.mCurrentMulticastFilter = 0;
            Log.d(TAG, "get multicast filter program=" + program);
            long now = currentTimeSeconds();
            this.mLastTimeInstalledProgram = now;
            this.mLastInstalledProgramMinLifetime = programMinLifetime;
            this.mLastInstalledProgram = program;
            this.mNumProgramUpdates++;
            this.mIpManagerCallback.installPacketFilter(program);
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
        } catch (Exception e) {
        }
    }

    private void logApfProgramEventLocked(long now) {
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
            if (((Ra) this.mRas.get(i)).isExpired()) {
                log("Expiring " + this.mRas.get(i));
                this.mRas.remove(i);
            } else {
                i++;
            }
        }
    }

    synchronized ProcessRaResult processRa(byte[] packet, int length) {
        Ra ra;
        for (int i = 0; i < this.mRas.size(); i++) {
            ra = (Ra) this.mRas.get(i);
            if (ra.matches(packet, length)) {
                ra.mLastSeen = currentTimeSeconds();
                ra.mMinLifetime = ra.minLifetime(packet, length);
                ra.seenCount++;
                this.mRas.add(0, (Ra) this.mRas.remove(i));
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
            ra = new Ra(packet, length);
            if (ra.isExpired()) {
                return ProcessRaResult.ZERO_LIFETIME;
            }
            log("Adding " + ra);
            this.mRas.add(ra);
            installNewProgramLocked();
            return ProcessRaResult.UPDATE_NEW_RA;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RA", e);
            return ProcessRaResult.PARSE_ERROR;
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ApfFilter maybeCreate(ApfCapabilities apfCapabilities, NetworkInterface networkInterface, Callback ipManagerCallback, boolean multicastFilter, boolean ieee802_3Filter) {
        if (apfCapabilities == null || networkInterface == null || apfCapabilities.apfVersionSupported == 0) {
            return null;
        }
        if (apfCapabilities.maximumApfProgramSize < 512) {
            Log.e(TAG, "Unacceptably small APF limit: " + apfCapabilities.maximumApfProgramSize);
            return null;
        } else if (apfCapabilities.apfPacketFormat != OsConstants.ARPHRD_ETHER) {
            return null;
        } else {
            if (new ApfGenerator().setApfVersion(apfCapabilities.apfVersionSupported)) {
                return new ApfFilter(apfCapabilities, networkInterface, ipManagerCallback, multicastFilter, ieee802_3Filter, new IpConnectivityLog());
            }
            Log.e(TAG, "Unsupported APF version: " + apfCapabilities.apfVersionSupported);
            return null;
        }
    }

    public synchronized void shutdown() {
        if (this.mReceiveThread != null) {
            log("shutting down");
            this.mReceiveThread.halt();
            this.mReceiveThread = null;
        }
        this.mRas.clear();
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

    private static LinkAddress findIPv4LinkAddress(LinkProperties lp) {
        LinkAddress ipv4Address = null;
        for (LinkAddress address : lp.getLinkAddresses()) {
            if (address.getAddress() instanceof Inet4Address) {
                if (ipv4Address != null && (ipv4Address.isSameAddressAs(address) ^ 1) != 0) {
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

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0150 A:{Splitter: B:9:0x0056, ExcHandler: java.net.UnknownHostException (e java.net.UnknownHostException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void dump(IndentingPrintWriter pw) {
        pw.println("Capabilities: " + this.mApfCapabilities);
        pw.println("Receive thread: " + (this.mReceiveThread != null ? "RUNNING" : "STOPPED"));
        pw.println("Multicast: " + (this.mMulticastFilter ? "DROP" : "ALLOW"));
        try {
            pw.println("IPv4 address: " + InetAddress.getByAddress(this.mIPv4Address).getHostAddress());
        } catch (UnknownHostException e) {
        }
        if (this.mLastTimeInstalledProgram == 0) {
            pw.println("No program installed.");
            return;
        }
        pw.println("Program updates: " + this.mNumProgramUpdates);
        pw.println(String.format("Last program length %d, installed %ds ago, lifetime %ds", new Object[]{Integer.valueOf(this.mLastInstalledProgram.length), Long.valueOf(currentTimeSeconds() - this.mLastTimeInstalledProgram), Long.valueOf(this.mLastInstalledProgramMinLifetime)}));
        pw.println("RA filters:");
        pw.increaseIndent();
        for (Ra ra : this.mRas) {
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
    }

    private static int uint8(byte b) {
        return b & 255;
    }

    private static int uint16(short s) {
        return NetworkConstants.ARP_HWTYPE_RESERVED_HI & s;
    }

    private static long uint32(int i) {
        return ((long) i) & 4294967295L;
    }

    private static int getUint8(ByteBuffer buffer, int position) {
        return uint8(buffer.get(position));
    }

    private static int getUint16(ByteBuffer buffer, int position) {
        return uint16(buffer.getShort(position));
    }

    private static long getUint32(ByteBuffer buffer, int position) {
        return uint32(buffer.getInt(position));
    }

    public static int ipv4BroadcastAddress(byte[] addrBytes, int prefixLength) {
        return bytesToInt(addrBytes) | ((int) (uint32(-1) >>> prefixLength));
    }

    public static int bytesToInt(byte[] addrBytes) {
        return (((uint8(addrBytes[0]) << 24) + (uint8(addrBytes[1]) << 16)) + (uint8(addrBytes[2]) << 8)) + uint8(addrBytes[3]);
    }
}
