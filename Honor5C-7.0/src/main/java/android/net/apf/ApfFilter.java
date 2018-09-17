package android.net.apf;

import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.apf.ApfGenerator.IllegalInstructionException;
import android.net.apf.ApfGenerator.Register;
import android.net.ip.IpManager.Callback;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.display.RampAnimator;
import com.android.server.job.controllers.JobStatus;
import java.io.FileDescriptor;
import java.io.IOException;
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
    private static int ARP_HEADER_OFFSET = 0;
    private static final byte[] ARP_IPV4_REQUEST_HEADER = null;
    private static int ARP_TARGET_IP_ADDRESS_OFFSET = 0;
    private static final boolean DBG = true;
    private static final int DHCP_CLIENT_MAC_OFFSET = 50;
    private static final int DHCP_CLIENT_PORT = 68;
    private static final byte[] ETH_BROADCAST_MAC_ADDRESS = null;
    private static final int ETH_DEST_ADDR_OFFSET = 0;
    private static final int ETH_ETHERTYPE_OFFSET = 12;
    private static final int ETH_HEADER_LEN = 14;
    private static final int FRACTION_OF_LIFETIME_TO_FILTER = 6;
    private static final int ICMP6_NEIGHBOR_ANNOUNCEMENT = 136;
    private static final int ICMP6_TYPE_OFFSET = 54;
    private static final int IPV4_DEST_ADDR_OFFSET = 30;
    private static final int IPV4_FRAGMENT_OFFSET_MASK = 8191;
    private static final int IPV4_FRAGMENT_OFFSET_OFFSET = 20;
    private static final int IPV4_PROTOCOL_OFFSET = 23;
    private static final byte[] IPV6_ALL_NODES_ADDRESS = null;
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
    private final ApfCapabilities mApfCapabilities;
    byte[] mHardwareAddress;
    @GuardedBy("this")
    private byte[] mIPv4Address;
    private final Callback mIpManagerCallback;
    @GuardedBy("this")
    private byte[] mLastInstalledProgram;
    @GuardedBy("this")
    private long mLastInstalledProgramMinLifetime;
    @GuardedBy("this")
    private long mLastTimeInstalledProgram;
    @GuardedBy("this")
    private boolean mMulticastFilter;
    private final NetworkInterface mNetworkInterface;
    @GuardedBy("this")
    private int mNumProgramUpdates;
    @GuardedBy("this")
    private ArrayList<Ra> mRas;
    ReceiveThread mReceiveThread;
    @GuardedBy("this")
    private long mUniqueCounter;

    private class Ra {
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
        private final ArrayList<Pair<Integer, Integer>> mNonLifetimes;
        private final ByteBuffer mPacket;
        private final ArrayList<Integer> mPrefixOptionOffsets;
        private final ArrayList<Integer> mRdnssOptionOffsets;
        int seenCount;

        String getLastMatchingPacket() {
            return HexDump.toHexString(this.mPacket.array(), ApfFilter.ETH_DEST_ADDR_OFFSET, this.mPacket.capacity(), false);
        }

        private String IPv6AddresstoString(int pos) {
            try {
                byte[] array = this.mPacket.array();
                if (pos < 0 || pos + ICMP6_RA_HEADER_LEN > array.length || pos + ICMP6_RA_HEADER_LEN < pos) {
                    return "???";
                }
                return ((Inet6Address) InetAddress.getByAddress(Arrays.copyOfRange(array, pos, pos + ICMP6_RA_HEADER_LEN))).getHostAddress();
            } catch (UnsupportedOperationException e) {
                return "???";
            } catch (ClassCastException e2) {
                return "???";
            }
        }

        private int uint8(byte b) {
            return b & RampAnimator.DEFAULT_MAX_BRIGHTNESS;
        }

        private int uint16(short s) {
            return 65535 & s;
        }

        private long uint32(int s) {
            return (long) (s & -1);
        }

        private void prefixOptionToString(StringBuffer sb, int offset) {
            String prefix = IPv6AddresstoString(offset + ICMP6_RA_HEADER_LEN);
            int length = uint8(this.mPacket.get(offset + ICMP6_RA_ROUTER_LIFETIME_LEN));
            long valid = (long) this.mPacket.getInt(offset + ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET);
            long preferred = (long) this.mPacket.getInt(offset + ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET);
            Object[] objArr = new Object[ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET];
            objArr[ApfFilter.ETH_DEST_ADDR_OFFSET] = prefix;
            objArr[1] = Integer.valueOf(length);
            objArr[ICMP6_RA_ROUTER_LIFETIME_LEN] = Long.valueOf(valid);
            objArr[ICMP6_PREFIX_OPTION_TYPE] = Long.valueOf(preferred);
            sb.append(String.format("%s/%d %ds/%ds ", objArr));
        }

        private void rdnssOptionToString(StringBuffer sb, int offset) {
            int optLen = uint8(this.mPacket.get(offset + 1)) * ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET;
            if (optLen >= ICMP6_ROUTE_INFO_OPTION_TYPE) {
                int numServers = (optLen - 8) / ICMP6_RA_HEADER_LEN;
                sb.append("DNS ").append(uint32(this.mPacket.getInt(offset + ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET))).append("s");
                for (int server = ApfFilter.ETH_DEST_ADDR_OFFSET; server < numServers; server++) {
                    sb.append(" ").append(IPv6AddresstoString((offset + ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET) + (server * ICMP6_RA_HEADER_LEN)));
                }
            }
        }

        public String toString() {
            try {
                StringBuffer sb = new StringBuffer();
                Object[] objArr = new Object[ICMP6_PREFIX_OPTION_TYPE];
                objArr[ApfFilter.ETH_DEST_ADDR_OFFSET] = IPv6AddresstoString(ApfFilter.IPV6_SRC_ADDR_OFFSET);
                objArr[1] = IPv6AddresstoString(ApfFilter.IPV6_DEST_ADDR_OFFSET);
                objArr[ICMP6_RA_ROUTER_LIFETIME_LEN] = Integer.valueOf(uint16(this.mPacket.getShort(ICMP6_RA_ROUTER_LIFETIME_OFFSET)));
                sb.append(String.format("RA %s -> %s %ds ", objArr));
                for (Integer intValue : this.mPrefixOptionOffsets) {
                    prefixOptionToString(sb, intValue.intValue());
                }
                for (Integer intValue2 : this.mRdnssOptionOffsets) {
                    rdnssOptionToString(sb, intValue2.intValue());
                }
                return sb.toString();
            } catch (BufferUnderflowException e) {
                return "<Malformed RA>";
            }
        }

        private int addNonLifetime(int lastNonLifetimeStart, int lifetimeOffset, int lifetimeLength) {
            lifetimeOffset += this.mPacket.position();
            this.mNonLifetimes.add(new Pair(Integer.valueOf(lastNonLifetimeStart), Integer.valueOf(lifetimeOffset - lastNonLifetimeStart)));
            return lifetimeOffset + lifetimeLength;
        }

        Ra(byte[] packet, int length) {
            this.mNonLifetimes = new ArrayList();
            this.mPrefixOptionOffsets = new ArrayList();
            this.mRdnssOptionOffsets = new ArrayList();
            this.seenCount = ApfFilter.ETH_DEST_ADDR_OFFSET;
            this.mPacket = ByteBuffer.allocate(length).put(ByteBuffer.wrap(packet, ApfFilter.ETH_DEST_ADDR_OFFSET, length));
            this.mPacket.clear();
            this.mLastSeen = ApfFilter.curTime();
            int lastNonLifetimeStart = addNonLifetime(addNonLifetime(ApfFilter.ETH_DEST_ADDR_OFFSET, ICMP6_RA_CHECKSUM_OFFSET, ICMP6_RA_ROUTER_LIFETIME_LEN), ICMP6_RA_ROUTER_LIFETIME_OFFSET, ICMP6_RA_ROUTER_LIFETIME_LEN);
            this.mPacket.position(ICMP6_RA_OPTION_OFFSET);
            while (this.mPacket.hasRemaining()) {
                int optionType = this.mPacket.get(this.mPacket.position()) & RampAnimator.DEFAULT_MAX_BRIGHTNESS;
                int optionLength = (this.mPacket.get(this.mPacket.position() + 1) & RampAnimator.DEFAULT_MAX_BRIGHTNESS) * ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET;
                switch (optionType) {
                    case ICMP6_PREFIX_OPTION_TYPE /*3*/:
                        lastNonLifetimeStart = addNonLifetime(addNonLifetime(lastNonLifetimeStart, ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET, ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET), ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET, ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET);
                        this.mPrefixOptionOffsets.add(Integer.valueOf(this.mPacket.position()));
                        break;
                    case ICMP6_ROUTE_INFO_OPTION_TYPE /*24*/:
                    case ICMP6_DNSSL_OPTION_TYPE /*31*/:
                        break;
                    case ICMP6_RDNSS_OPTION_TYPE /*25*/:
                        this.mRdnssOptionOffsets.add(Integer.valueOf(this.mPacket.position()));
                        break;
                }
                lastNonLifetimeStart = addNonLifetime(lastNonLifetimeStart, ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET, ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET);
                if (optionLength <= 0) {
                    Object[] objArr = new Object[ICMP6_RA_ROUTER_LIFETIME_LEN];
                    objArr[ApfFilter.ETH_DEST_ADDR_OFFSET] = Integer.valueOf(optionType);
                    objArr[1] = Integer.valueOf(optionLength);
                    throw new IllegalArgumentException(String.format("Invalid option length opt=%d len=%d", objArr));
                }
                this.mPacket.position(this.mPacket.position() + optionLength);
            }
            addNonLifetime(lastNonLifetimeStart, ApfFilter.ETH_DEST_ADDR_OFFSET, ApfFilter.ETH_DEST_ADDR_OFFSET);
            this.mMinLifetime = minLifetime(packet, length);
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
            return ApfFilter.DBG;
        }

        long minLifetime(byte[] packet, int length) {
            long minLifetime = JobStatus.NO_LATEST_RUNTIME;
            ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
            for (int i = ApfFilter.ETH_DEST_ADDR_OFFSET; i + 1 < this.mNonLifetimes.size(); i++) {
                int offset = ((Integer) ((Pair) this.mNonLifetimes.get(i)).first).intValue() + ((Integer) ((Pair) this.mNonLifetimes.get(i)).second).intValue();
                if (offset != ICMP6_RA_CHECKSUM_OFFSET) {
                    long val;
                    int lifetimeLength = ((Integer) ((Pair) this.mNonLifetimes.get(i + 1)).first).intValue() - offset;
                    switch (lifetimeLength) {
                        case ICMP6_RA_ROUTER_LIFETIME_LEN /*2*/:
                            val = (long) byteBuffer.getShort(offset);
                            break;
                        case ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET /*4*/:
                            val = (long) byteBuffer.getInt(offset);
                            break;
                        default:
                            throw new IllegalStateException("bogus lifetime size " + length);
                    }
                    minLifetime = Math.min(minLifetime, val & ((1 << (lifetimeLength * ICMP6_PREFIX_OPTION_PREFERRED_LIFETIME_OFFSET)) - 1));
                }
            }
            return minLifetime;
        }

        long currentLifetime() {
            return this.mMinLifetime - (ApfFilter.curTime() - this.mLastSeen);
        }

        boolean isExpired() {
            return currentLifetime() <= 0 ? ApfFilter.DBG : false;
        }

        @GuardedBy("ApfFilter.this")
        long generateFilterLocked(ApfGenerator gen) throws IllegalInstructionException {
            String nextFilterLabel = "Ra" + ApfFilter.this.getUniqueNumberLocked();
            gen.addLoadFromMemory(Register.R0, ApfFilter.ETH_HEADER_LEN);
            gen.addJumpIfR0NotEquals(this.mPacket.capacity(), nextFilterLabel);
            int filterLifetime = (int) (currentLifetime() / 6);
            gen.addLoadFromMemory(Register.R0, 15);
            gen.addJumpIfR0GreaterThan(filterLifetime, nextFilterLabel);
            for (int i = ApfFilter.ETH_DEST_ADDR_OFFSET; i < this.mNonLifetimes.size(); i++) {
                Pair<Integer, Integer> nonLifetime = (Pair) this.mNonLifetimes.get(i);
                if (((Integer) nonLifetime.second).intValue() != 0) {
                    gen.addLoadImmediate(Register.R0, ((Integer) nonLifetime.first).intValue());
                    gen.addJumpIfBytesNotEqual(Register.R0, Arrays.copyOfRange(this.mPacket.array(), ((Integer) nonLifetime.first).intValue(), ((Integer) nonLifetime.second).intValue() + ((Integer) nonLifetime.first).intValue()), nextFilterLabel);
                }
                if (i + 1 < this.mNonLifetimes.size()) {
                    Pair<Integer, Integer> nextNonLifetime = (Pair) this.mNonLifetimes.get(i + 1);
                    int offset = ((Integer) nonLifetime.first).intValue() + ((Integer) nonLifetime.second).intValue();
                    if (offset == ICMP6_RA_CHECKSUM_OFFSET) {
                        continue;
                    } else {
                        int length = ((Integer) nextNonLifetime.first).intValue() - offset;
                        switch (length) {
                            case ICMP6_RA_ROUTER_LIFETIME_LEN /*2*/:
                                gen.addLoad16(Register.R0, offset);
                                break;
                            case ICMP6_PREFIX_OPTION_VALID_LIFETIME_OFFSET /*4*/:
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
        private final byte[] mPacket;
        private final FileDescriptor mSocket;
        private volatile boolean mStopped;

        public ReceiveThread(FileDescriptor socket) {
            this.mPacket = new byte[1514];
            this.mSocket = socket;
        }

        public void halt() {
            this.mStopped = ApfFilter.DBG;
            try {
                IoBridge.closeAndSignalBlockedThreads(this.mSocket);
            } catch (IOException e) {
            }
        }

        public void run() {
            ApfFilter.this.log("begin monitoring");
            while (!this.mStopped) {
                try {
                    ApfFilter.this.processRa(this.mPacket, Os.read(this.mSocket, this.mPacket, ApfFilter.ETH_DEST_ADDR_OFFSET, this.mPacket.length));
                } catch (Exception e) {
                    if (!this.mStopped) {
                        Log.e(ApfFilter.TAG, "Read error", e);
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.apf.ApfFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.apf.ApfFilter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.apf.ApfFilter.<clinit>():void");
    }

    ApfFilter(ApfCapabilities apfCapabilities, NetworkInterface networkInterface, Callback ipManagerCallback, boolean multicastFilter) {
        this.mRas = new ArrayList();
        this.mApfCapabilities = apfCapabilities;
        this.mIpManagerCallback = ipManagerCallback;
        this.mNetworkInterface = networkInterface;
        this.mMulticastFilter = multicastFilter;
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
            Log.e(TAG, "Error starting filter", e);
        }
    }

    private static long curTime() {
        return System.currentTimeMillis() / 1000;
    }

    @GuardedBy("this")
    private void generateArpFilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        if (this.mIPv4Address != null) {
            gen.addLoadImmediate(Register.R0, ARP_HEADER_OFFSET);
            gen.addJumpIfBytesNotEqual(Register.R0, ARP_IPV4_REQUEST_HEADER, ApfGenerator.PASS_LABEL);
            gen.addLoadImmediate(Register.R0, ARP_TARGET_IP_ADDRESS_OFFSET);
            gen.addJumpIfBytesNotEqual(Register.R0, this.mIPv4Address, ApfGenerator.DROP_LABEL);
        }
        gen.addJump(ApfGenerator.PASS_LABEL);
    }

    @GuardedBy("this")
    private void generateIPv4FilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        if (this.mMulticastFilter) {
            gen.addLoad8(Register.R0, IPV4_DEST_ADDR_OFFSET);
            gen.addAnd(240);
            gen.addJumpIfR0Equals(224, ApfGenerator.DROP_LABEL);
            gen.addLoadImmediate(Register.R0, ETH_DEST_ADDR_OFFSET);
            gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
            gen.addLoad8(Register.R0, IPV4_PROTOCOL_OFFSET);
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_UDP, ApfGenerator.DROP_LABEL);
            gen.addLoad16(Register.R0, IPV6_NEXT_HEADER_OFFSET);
            gen.addJumpIfR0AnyBitsSet(IPV4_FRAGMENT_OFFSET_MASK, ApfGenerator.DROP_LABEL);
            gen.addLoadFromMemory(Register.R1, 13);
            gen.addLoad16Indexed(Register.R0, UDP_DESTINATION_PORT_OFFSET);
            gen.addJumpIfR0NotEquals(DHCP_CLIENT_PORT, ApfGenerator.DROP_LABEL);
            gen.addLoadImmediate(Register.R0, DHCP_CLIENT_MAC_OFFSET);
            gen.addAddR1();
            gen.addJumpIfBytesNotEqual(Register.R0, this.mHardwareAddress, ApfGenerator.DROP_LABEL);
        }
        gen.addJump(ApfGenerator.PASS_LABEL);
    }

    @GuardedBy("this")
    private void generateIPv6FilterLocked(ApfGenerator gen) throws IllegalInstructionException {
        gen.addLoad8(Register.R0, IPV6_NEXT_HEADER_OFFSET);
        if (this.mMulticastFilter) {
            String skipIpv6MulticastFilterLabel = "skipIPv6MulticastFilter";
            gen.addJumpIfR0Equals(OsConstants.IPPROTO_ICMPV6, skipIpv6MulticastFilterLabel);
            gen.addLoad8(Register.R0, IPV6_DEST_ADDR_OFFSET);
            gen.addJumpIfR0Equals(RampAnimator.DEFAULT_MAX_BRIGHTNESS, ApfGenerator.DROP_LABEL);
            gen.addJump(ApfGenerator.PASS_LABEL);
            gen.defineLabel(skipIpv6MulticastFilterLabel);
        } else {
            gen.addJumpIfR0NotEquals(OsConstants.IPPROTO_ICMPV6, ApfGenerator.PASS_LABEL);
        }
        String skipUnsolicitedMulticastNALabel = "skipUnsolicitedMulticastNA";
        gen.addLoad8(Register.R0, ICMP6_TYPE_OFFSET);
        gen.addJumpIfR0NotEquals(ICMP6_NEIGHBOR_ANNOUNCEMENT, skipUnsolicitedMulticastNALabel);
        gen.addLoadImmediate(Register.R0, IPV6_DEST_ADDR_OFFSET);
        gen.addJumpIfBytesNotEqual(Register.R0, IPV6_ALL_NODES_ADDRESS, skipUnsolicitedMulticastNALabel);
        gen.addJump(ApfGenerator.DROP_LABEL);
        gen.defineLabel(skipUnsolicitedMulticastNALabel);
    }

    @GuardedBy("this")
    private ApfGenerator beginProgramLocked() throws IllegalInstructionException {
        ApfGenerator gen = new ApfGenerator();
        gen.setApfVersion(this.mApfCapabilities.apfVersionSupported);
        String skipArpFiltersLabel = "skipArpFilters";
        gen.addLoad16(Register.R0, ETH_ETHERTYPE_OFFSET);
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_ARP, skipArpFiltersLabel);
        generateArpFilterLocked(gen);
        gen.defineLabel(skipArpFiltersLabel);
        String skipIPv4FiltersLabel = "skipIPv4Filters";
        gen.addJumpIfR0NotEquals(OsConstants.ETH_P_IP, skipIPv4FiltersLabel);
        generateIPv4FilterLocked(gen);
        gen.defineLabel(skipIPv4FiltersLabel);
        String ipv6FilterLabel = "IPv6Filters";
        gen.addJumpIfR0Equals(OsConstants.ETH_P_IPV6, ipv6FilterLabel);
        gen.addLoadImmediate(Register.R0, ETH_DEST_ADDR_OFFSET);
        gen.addJumpIfBytesNotEqual(Register.R0, ETH_BROADCAST_MAC_ADDRESS, ApfGenerator.PASS_LABEL);
        gen.addJump(ApfGenerator.DROP_LABEL);
        gen.defineLabel(ipv6FilterLabel);
        generateIPv6FilterLocked(gen);
        return gen;
    }

    @GuardedBy("this")
    void installNewProgramLocked() {
        purgeExpiredRasLocked();
        long programMinLifetime = JobStatus.NO_LATEST_RUNTIME;
        try {
            ApfGenerator gen = beginProgramLocked();
            ArrayList<Ra> rasToFilter = new ArrayList();
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
            this.mLastTimeInstalledProgram = curTime();
            this.mLastInstalledProgramMinLifetime = programMinLifetime;
            this.mLastInstalledProgram = program;
            this.mNumProgramUpdates++;
            this.mIpManagerCallback.installPacketFilter(program);
        } catch (IllegalInstructionException e) {
            Log.e(TAG, "Program failed to generate: ", e);
        }
    }

    @GuardedBy("this")
    private void maybeInstallNewProgramLocked() {
        if (this.mRas.size() != 0 && this.mLastTimeInstalledProgram + this.mLastInstalledProgramMinLifetime < curTime() + MAX_PROGRAM_LIFETIME_WORTH_REFRESHING) {
            installNewProgramLocked();
        }
    }

    private void hexDump(String msg, byte[] packet, int length) {
        log(msg + HexDump.toHexString(packet, ETH_DEST_ADDR_OFFSET, length, false));
    }

    @GuardedBy("this")
    private void purgeExpiredRasLocked() {
        int i = ETH_DEST_ADDR_OFFSET;
        while (i < this.mRas.size()) {
            if (((Ra) this.mRas.get(i)).isExpired()) {
                log("Expiring " + this.mRas.get(i));
                this.mRas.remove(i);
            } else {
                i++;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void processRa(byte[] packet, int length) {
        int i = ETH_DEST_ADDR_OFFSET;
        while (true) {
            Ra ra;
            if (i < this.mRas.size()) {
                ra = (Ra) this.mRas.get(i);
                if (ra.matches(packet, length)) {
                    ra.mLastSeen = curTime();
                    ra.mMinLifetime = ra.minLifetime(packet, length);
                    ra.seenCount++;
                    this.mRas.add(ETH_DEST_ADDR_OFFSET, (Ra) this.mRas.remove(i));
                    maybeInstallNewProgramLocked();
                    return;
                }
                i++;
            } else {
                purgeExpiredRasLocked();
                if (this.mRas.size() < MAX_RAS) {
                    try {
                        ra = new Ra(packet, length);
                        if (!ra.isExpired()) {
                            log("Adding " + ra);
                            this.mRas.add(ra);
                            installNewProgramLocked();
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing RA: " + e);
                        return;
                    }
                }
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ApfFilter maybeCreate(ApfCapabilities apfCapabilities, NetworkInterface networkInterface, Callback ipManagerCallback, boolean multicastFilter) {
        if (apfCapabilities == null || networkInterface == null || apfCapabilities.apfVersionSupported == 0) {
            return null;
        }
        if (apfCapabilities.maximumApfProgramSize < DumpState.DUMP_MESSAGES) {
            Log.e(TAG, "Unacceptably small APF limit: " + apfCapabilities.maximumApfProgramSize);
            return null;
        } else if (apfCapabilities.apfPacketFormat != OsConstants.ARPHRD_ETHER) {
            return null;
        } else {
            if (new ApfGenerator().setApfVersion(apfCapabilities.apfVersionSupported)) {
                return new ApfFilter(apfCapabilities, networkInterface, ipManagerCallback, multicastFilter);
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

    public synchronized void setMulticastFilter(boolean enabled) {
        if (this.mMulticastFilter != enabled) {
            this.mMulticastFilter = enabled;
            installNewProgramLocked();
        }
    }

    private static byte[] findIPv4Address(LinkProperties lp) {
        byte[] ipv4Address = null;
        for (InetAddress inetAddr : lp.getAddresses()) {
            byte[] addr = inetAddr.getAddress();
            if (addr.length == 4) {
                if (ipv4Address != null && !Arrays.equals(ipv4Address, addr)) {
                    return null;
                }
                ipv4Address = addr;
            }
        }
        return ipv4Address;
    }

    public synchronized void setLinkProperties(LinkProperties lp) {
        byte[] ipv4Address = findIPv4Address(lp);
        if (!Arrays.equals(ipv4Address, this.mIPv4Address)) {
            this.mIPv4Address = ipv4Address;
            installNewProgramLocked();
        }
    }

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
        pw.println(String.format("Last program length %d, installed %ds ago, lifetime %ds", new Object[]{Integer.valueOf(this.mLastInstalledProgram.length), Long.valueOf(curTime() - this.mLastTimeInstalledProgram), Long.valueOf(this.mLastInstalledProgramMinLifetime)}));
        pw.println("RA filters:");
        pw.increaseIndent();
        for (Ra ra : this.mRas) {
            pw.println(ra);
            pw.increaseIndent();
            pw.println(String.format("Seen: %d, last %ds ago", new Object[]{Integer.valueOf(ra.seenCount), Long.valueOf(curTime() - ra.mLastSeen)}));
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
}
