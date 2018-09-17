package com.android.server.connectivity;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.util.IndentingPrintWriter;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;

public class NetworkDiagnostics {
    private static final String TAG = "NetworkDiagnostics";
    private static final InetAddress TEST_DNS4 = null;
    private static final InetAddress TEST_DNS6 = null;
    private final CountDownLatch mCountDownLatch;
    private final long mDeadlineTime;
    private final String mDescription;
    private final Map<InetAddress, Measurement> mDnsUdpChecks;
    private final Map<Pair<InetAddress, InetAddress>, Measurement> mExplicitSourceIcmpChecks;
    private final Map<InetAddress, Measurement> mIcmpChecks;
    private final Integer mInterfaceIndex;
    private final LinkProperties mLinkProperties;
    private final Network mNetwork;
    private final long mStartTime;
    private final long mTimeoutMs;

    public enum DnsResponseCode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.NetworkDiagnostics.DnsResponseCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.NetworkDiagnostics.DnsResponseCode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkDiagnostics.DnsResponseCode.<clinit>():void");
        }
    }

    private class SimpleSocketCheck implements Closeable {
        protected final int mAddressFamily;
        protected FileDescriptor mFileDescriptor;
        protected final Measurement mMeasurement;
        protected SocketAddress mSocketAddress;
        protected final InetAddress mSource;
        protected final InetAddress mTarget;
        final /* synthetic */ NetworkDiagnostics this$0;

        protected SimpleSocketCheck(NetworkDiagnostics this$0, InetAddress source, InetAddress target, Measurement measurement) {
            this.this$0 = this$0;
            this.mMeasurement = measurement;
            if (target instanceof Inet6Address) {
                InetAddress targetWithScopeId = null;
                if (target.isLinkLocalAddress() && this$0.mInterfaceIndex != null) {
                    try {
                        targetWithScopeId = Inet6Address.getByAddress(null, target.getAddress(), this$0.mInterfaceIndex.intValue());
                    } catch (UnknownHostException e) {
                        this.mMeasurement.recordFailure(e.toString());
                    }
                }
                if (targetWithScopeId == null) {
                    targetWithScopeId = target;
                }
                this.mTarget = targetWithScopeId;
                this.mAddressFamily = OsConstants.AF_INET6;
            } else {
                this.mTarget = target;
                this.mAddressFamily = OsConstants.AF_INET;
            }
            this.mSource = source;
        }

        protected SimpleSocketCheck(NetworkDiagnostics this$0, InetAddress target, Measurement measurement) {
            this(this$0, null, target, measurement);
        }

        protected void setupSocket(int sockType, int protocol, long writeTimeout, long readTimeout, int dstPort) throws ErrnoException, IOException {
            this.mFileDescriptor = Os.socket(this.mAddressFamily, sockType, protocol);
            Os.setsockoptTimeval(this.mFileDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(writeTimeout));
            Os.setsockoptTimeval(this.mFileDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(readTimeout));
            this.this$0.mNetwork.bindSocket(this.mFileDescriptor);
            if (this.mSource != null) {
                Os.bind(this.mFileDescriptor, this.mSource, 0);
            }
            Os.connect(this.mFileDescriptor, this.mTarget, dstPort);
            this.mSocketAddress = Os.getsockname(this.mFileDescriptor);
        }

        protected String getSocketAddressString() {
            return String.format(this.mSocketAddress.getAddress() instanceof Inet6Address ? "[%s]:%d" : "%s:%d", new Object[]{this.mSocketAddress.getAddress().getHostAddress(), Integer.valueOf(this.mSocketAddress.getPort())});
        }

        public void close() {
            IoUtils.closeQuietly(this.mFileDescriptor);
        }
    }

    private class DnsUdpCheck extends SimpleSocketCheck implements Runnable {
        private static final int DNS_SERVER_PORT = 53;
        private static final int PACKET_BUFSIZE = 512;
        private static final int RR_TYPE_A = 1;
        private static final int RR_TYPE_AAAA = 28;
        private static final int TIMEOUT_RECV = 500;
        private static final int TIMEOUT_SEND = 100;
        private final int mQueryType;
        private final Random mRandom;
        final /* synthetic */ NetworkDiagnostics this$0;

        private String responseCodeStr(int rcode) {
            try {
                return DnsResponseCode.values()[rcode].toString();
            } catch (IndexOutOfBoundsException e) {
                return String.valueOf(rcode);
            }
        }

        public DnsUdpCheck(NetworkDiagnostics this$0, InetAddress target, Measurement measurement) {
            this.this$0 = this$0;
            super(this$0, target, measurement);
            this.mRandom = new Random();
            if (this.mAddressFamily == OsConstants.AF_INET6) {
                this.mQueryType = RR_TYPE_AAAA;
            } else {
                this.mQueryType = RR_TYPE_A;
            }
            this.mMeasurement.description = "DNS UDP dst{" + this.mTarget.getHostAddress() + "}";
        }

        public void run() {
            if (this.mMeasurement.finishTime > 0) {
                this.this$0.mCountDownLatch.countDown();
                return;
            }
            try {
                setupSocket(OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP, 100, 500, DNS_SERVER_PORT);
                Measurement measurement = this.mMeasurement;
                measurement.description += " src{" + "xxx.xxx.xxx.xxx:xxx" + "}";
                String sixRandomDigits = String.valueOf(this.mRandom.nextInt(900000) + 100000);
                measurement = this.mMeasurement;
                measurement.description += " qtype{" + this.mQueryType + "}" + " qname{" + sixRandomDigits + "-android-ds.metric.gstatic.com}";
                byte[] dnsPacket = getDnsQueryPacket(sixRandomDigits);
                int count = 0;
                this.mMeasurement.startTime = NetworkDiagnostics.now();
                while (NetworkDiagnostics.now() < this.this$0.mDeadlineTime - 1000) {
                    count += RR_TYPE_A;
                    try {
                        Os.write(this.mFileDescriptor, dnsPacket, 0, dnsPacket.length);
                        try {
                            String rcodeStr;
                            ByteBuffer reply = ByteBuffer.allocate(PACKET_BUFSIZE);
                            Os.read(this.mFileDescriptor, reply);
                            if (reply.limit() > 3) {
                                rcodeStr = " " + responseCodeStr(reply.get(3) & 15);
                            } else {
                                rcodeStr = "";
                            }
                            this.mMeasurement.recordSuccess("1/" + count + rcodeStr);
                            break;
                        } catch (ErrnoException e) {
                        }
                    } catch (Exception e2) {
                        this.mMeasurement.recordFailure(e2.toString());
                    }
                }
                if (this.mMeasurement.finishTime == 0) {
                    this.mMeasurement.recordFailure("0/" + count);
                }
                close();
            } catch (Exception e22) {
                this.mMeasurement.recordFailure(e22.toString());
            }
        }

        private byte[] getDnsQueryPacket(String sixRandomDigits) {
            byte[] rnd = sixRandomDigits.getBytes(StandardCharsets.US_ASCII);
            return new byte[]{(byte) this.mRandom.nextInt(), (byte) this.mRandom.nextInt(), (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 17, rnd[0], rnd[RR_TYPE_A], rnd[2], rnd[3], rnd[4], rnd[5], (byte) 45, (byte) 97, (byte) 110, (byte) 100, (byte) 114, (byte) 111, (byte) 105, (byte) 100, (byte) 45, (byte) 100, (byte) 115, (byte) 6, (byte) 109, (byte) 101, (byte) 116, (byte) 114, (byte) 105, (byte) 99, (byte) 7, (byte) 103, (byte) 115, (byte) 116, (byte) 97, (byte) 116, (byte) 105, (byte) 99, (byte) 3, (byte) 99, (byte) 111, (byte) 109, (byte) 0, (byte) 0, (byte) this.mQueryType, (byte) 0, (byte) 1};
        }
    }

    private class IcmpCheck extends SimpleSocketCheck implements Runnable {
        private static final int ICMPV4_ECHO_REQUEST = 8;
        private static final int ICMPV6_ECHO_REQUEST = 128;
        private static final int PACKET_BUFSIZE = 512;
        private static final int TIMEOUT_RECV = 300;
        private static final int TIMEOUT_SEND = 100;
        private final int mIcmpType;
        private final int mProtocol;
        final /* synthetic */ NetworkDiagnostics this$0;

        public IcmpCheck(NetworkDiagnostics this$0, InetAddress source, InetAddress target, Measurement measurement) {
            this.this$0 = this$0;
            super(this$0, source, target, measurement);
            if (this.mAddressFamily == OsConstants.AF_INET6) {
                this.mProtocol = OsConstants.IPPROTO_ICMPV6;
                this.mIcmpType = ICMPV6_ECHO_REQUEST;
                this.mMeasurement.description = "ICMPv6";
            } else {
                this.mProtocol = OsConstants.IPPROTO_ICMP;
                this.mIcmpType = ICMPV4_ECHO_REQUEST;
                this.mMeasurement.description = "ICMPv4";
            }
            Measurement measurement2 = this.mMeasurement;
            measurement2.description += " dst{" + this.mTarget.getHostAddress() + "}";
        }

        public IcmpCheck(NetworkDiagnostics this$0, InetAddress target, Measurement measurement) {
            this(this$0, null, target, measurement);
        }

        public void run() {
            if (this.mMeasurement.finishTime > 0) {
                this.this$0.mCountDownLatch.countDown();
                return;
            }
            try {
                setupSocket(OsConstants.SOCK_DGRAM, this.mProtocol, 100, 300, 0);
                Measurement measurement = this.mMeasurement;
                measurement.description += " src{" + "xxx.xxx.xxx.xxx:xxx" + "}";
                byte[] icmpPacket = new byte[ICMPV4_ECHO_REQUEST];
                icmpPacket[0] = (byte) this.mIcmpType;
                icmpPacket[1] = (byte) 0;
                icmpPacket[2] = (byte) 0;
                icmpPacket[3] = (byte) 0;
                icmpPacket[4] = (byte) 0;
                icmpPacket[5] = (byte) 0;
                icmpPacket[6] = (byte) 0;
                icmpPacket[7] = (byte) 0;
                int count = 0;
                this.mMeasurement.startTime = NetworkDiagnostics.now();
                while (NetworkDiagnostics.now() < this.this$0.mDeadlineTime - 400) {
                    count++;
                    icmpPacket[icmpPacket.length - 1] = (byte) count;
                    try {
                        Os.write(this.mFileDescriptor, icmpPacket, 0, icmpPacket.length);
                        try {
                            Os.read(this.mFileDescriptor, ByteBuffer.allocate(PACKET_BUFSIZE));
                            this.mMeasurement.recordSuccess("1/" + count);
                            break;
                        } catch (ErrnoException e) {
                        }
                    } catch (Exception e2) {
                        this.mMeasurement.recordFailure(e2.toString());
                    }
                }
                if (this.mMeasurement.finishTime == 0) {
                    this.mMeasurement.recordFailure("0/" + count);
                }
                close();
            } catch (Exception e22) {
                this.mMeasurement.recordFailure(e22.toString());
            }
        }
    }

    public class Measurement {
        private static final String FAILED = "FAILED";
        private static final String SUCCEEDED = "SUCCEEDED";
        String description;
        long finishTime;
        String result;
        long startTime;
        private boolean succeeded;
        final /* synthetic */ NetworkDiagnostics this$0;
        Thread thread;

        public Measurement(NetworkDiagnostics this$0) {
            this.this$0 = this$0;
            this.description = "";
            this.result = "";
        }

        public boolean checkSucceeded() {
            return this.succeeded;
        }

        void recordSuccess(String msg) {
            maybeFixupTimes();
            this.succeeded = true;
            this.result = "SUCCEEDED: " + msg;
            if (this.this$0.mCountDownLatch != null) {
                this.this$0.mCountDownLatch.countDown();
            }
        }

        void recordFailure(String msg) {
            maybeFixupTimes();
            this.succeeded = false;
            this.result = "FAILED: " + msg;
            if (this.this$0.mCountDownLatch != null) {
                this.this$0.mCountDownLatch.countDown();
            }
        }

        private void maybeFixupTimes() {
            if (this.finishTime == 0) {
                this.finishTime = NetworkDiagnostics.now();
            }
            if (this.startTime == 0) {
                this.startTime = this.finishTime;
            }
        }

        public String toString() {
            return this.description + ": " + this.result + " (" + (this.finishTime - this.startTime) + "ms)";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.NetworkDiagnostics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.NetworkDiagnostics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkDiagnostics.<clinit>():void");
    }

    private static final long now() {
        return SystemClock.elapsedRealtime();
    }

    public NetworkDiagnostics(Network network, LinkProperties lp, long timeoutMs) {
        this.mIcmpChecks = new HashMap();
        this.mExplicitSourceIcmpChecks = new HashMap();
        this.mDnsUdpChecks = new HashMap();
        this.mNetwork = network;
        this.mLinkProperties = lp;
        this.mInterfaceIndex = getInterfaceIndex(this.mLinkProperties.getInterfaceName());
        this.mTimeoutMs = timeoutMs;
        this.mStartTime = now();
        this.mDeadlineTime = this.mStartTime + this.mTimeoutMs;
        if (this.mLinkProperties.isReachable(TEST_DNS4)) {
            this.mLinkProperties.addDnsServer(TEST_DNS4);
        }
        if (this.mLinkProperties.hasGlobalIPv6Address() || this.mLinkProperties.hasIPv6DefaultRoute()) {
            this.mLinkProperties.addDnsServer(TEST_DNS6);
        }
        for (RouteInfo route : this.mLinkProperties.getRoutes()) {
            if (route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                prepareIcmpMeasurement(gateway);
                if (route.isIPv6Default()) {
                    prepareExplicitSourceIcmpMeasurements(gateway);
                }
            }
        }
        for (InetAddress nameserver : this.mLinkProperties.getDnsServers()) {
            prepareIcmpMeasurement(nameserver);
            prepareDnsMeasurement(nameserver);
        }
        this.mCountDownLatch = new CountDownLatch(totalMeasurementCount());
        startMeasurements();
        this.mDescription = "ifaces{" + TextUtils.join(",", this.mLinkProperties.getAllInterfaceNames()) + "}" + " index{" + this.mInterfaceIndex + "}" + " network{" + this.mNetwork + "}" + " nethandle{" + this.mNetwork.getNetworkHandle() + "}";
    }

    private static Integer getInterfaceIndex(String ifname) {
        try {
            return Integer.valueOf(NetworkInterface.getByName(ifname).getIndex());
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void prepareIcmpMeasurement(InetAddress target) {
        if (!this.mIcmpChecks.containsKey(target)) {
            Measurement measurement = new Measurement(this);
            measurement.thread = new Thread(new IcmpCheck(this, target, measurement));
            this.mIcmpChecks.put(target, measurement);
        }
    }

    private void prepareExplicitSourceIcmpMeasurements(InetAddress target) {
        for (LinkAddress l : this.mLinkProperties.getLinkAddresses()) {
            InetAddress source = l.getAddress();
            if ((source instanceof Inet6Address) && l.isGlobalPreferred()) {
                Pair<InetAddress, InetAddress> srcTarget = new Pair(source, target);
                if (!this.mExplicitSourceIcmpChecks.containsKey(srcTarget)) {
                    Measurement measurement = new Measurement(this);
                    measurement.thread = new Thread(new IcmpCheck(this, source, target, measurement));
                    this.mExplicitSourceIcmpChecks.put(srcTarget, measurement);
                }
            }
        }
    }

    private void prepareDnsMeasurement(InetAddress target) {
        if (!this.mDnsUdpChecks.containsKey(target)) {
            Measurement measurement = new Measurement(this);
            measurement.thread = new Thread(new DnsUdpCheck(this, target, measurement));
            this.mDnsUdpChecks.put(target, measurement);
        }
    }

    private int totalMeasurementCount() {
        return (this.mIcmpChecks.size() + this.mExplicitSourceIcmpChecks.size()) + this.mDnsUdpChecks.size();
    }

    private void startMeasurements() {
        for (Measurement measurement : this.mIcmpChecks.values()) {
            measurement.thread.start();
        }
        for (Measurement measurement2 : this.mExplicitSourceIcmpChecks.values()) {
            measurement2.thread.start();
        }
        for (Measurement measurement22 : this.mDnsUdpChecks.values()) {
            measurement22.thread.start();
        }
    }

    public void waitForMeasurements() {
        try {
            this.mCountDownLatch.await(this.mDeadlineTime - now(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
    }

    public List<Measurement> getMeasurements() {
        ArrayList<Measurement> measurements = new ArrayList(totalMeasurementCount());
        for (Entry<InetAddress, Measurement> entry : this.mIcmpChecks.entrySet()) {
            if (entry.getKey() instanceof Inet4Address) {
                measurements.add((Measurement) entry.getValue());
            }
        }
        for (Entry<Pair<InetAddress, InetAddress>, Measurement> entry2 : this.mExplicitSourceIcmpChecks.entrySet()) {
            if (((Pair) entry2.getKey()).first instanceof Inet4Address) {
                measurements.add((Measurement) entry2.getValue());
            }
        }
        for (Entry<InetAddress, Measurement> entry3 : this.mDnsUdpChecks.entrySet()) {
            if (entry3.getKey() instanceof Inet4Address) {
                measurements.add((Measurement) entry3.getValue());
            }
        }
        for (Entry<InetAddress, Measurement> entry32 : this.mIcmpChecks.entrySet()) {
            if (entry32.getKey() instanceof Inet6Address) {
                measurements.add((Measurement) entry32.getValue());
            }
        }
        for (Entry<Pair<InetAddress, InetAddress>, Measurement> entry22 : this.mExplicitSourceIcmpChecks.entrySet()) {
            if (((Pair) entry22.getKey()).first instanceof Inet6Address) {
                measurements.add((Measurement) entry22.getValue());
            }
        }
        for (Entry<InetAddress, Measurement> entry322 : this.mDnsUdpChecks.entrySet()) {
            if (entry322.getKey() instanceof Inet6Address) {
                measurements.add((Measurement) entry322.getValue());
            }
        }
        return measurements;
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("NetworkDiagnostics:" + this.mDescription);
        long unfinished = this.mCountDownLatch.getCount();
        if (unfinished > 0) {
            pw.println("WARNING: countdown wait incomplete: " + unfinished + " unfinished measurements");
        }
        pw.increaseIndent();
        for (Measurement m : getMeasurements()) {
            pw.println((m.checkSucceeded() ? "." : "F") + "  " + m.toString());
        }
        pw.decreaseIndent();
    }
}
