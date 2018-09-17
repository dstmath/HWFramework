package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.MembershipKey;
import java.nio.channels.MulticastChannel;
import java.util.HashSet;

class MembershipKeyImpl extends MembershipKey {
    private HashSet<InetAddress> blockedSet;
    private final MulticastChannel ch;
    private final InetAddress group;
    private final NetworkInterface interf;
    private final InetAddress source;
    private Object stateLock;
    private volatile boolean valid;

    static class Type4 extends MembershipKeyImpl {
        private final int groupAddress;
        private final int interfAddress;
        private final int sourceAddress;

        Type4(MulticastChannel ch, InetAddress group, NetworkInterface interf, InetAddress source, int groupAddress, int interfAddress, int sourceAddress) {
            super(ch, group, interf, source, null);
            this.groupAddress = groupAddress;
            this.interfAddress = interfAddress;
            this.sourceAddress = sourceAddress;
        }

        int groupAddress() {
            return this.groupAddress;
        }

        int interfaceAddress() {
            return this.interfAddress;
        }

        int source() {
            return this.sourceAddress;
        }
    }

    static class Type6 extends MembershipKeyImpl {
        private final byte[] groupAddress;
        private final int index;
        private final byte[] sourceAddress;

        Type6(MulticastChannel ch, InetAddress group, NetworkInterface interf, InetAddress source, byte[] groupAddress, int index, byte[] sourceAddress) {
            super(ch, group, interf, source, null);
            this.groupAddress = groupAddress;
            this.index = index;
            this.sourceAddress = sourceAddress;
        }

        byte[] groupAddress() {
            return this.groupAddress;
        }

        int index() {
            return this.index;
        }

        byte[] source() {
            return this.sourceAddress;
        }
    }

    /* synthetic */ MembershipKeyImpl(MulticastChannel ch, InetAddress group, NetworkInterface interf, InetAddress source, MembershipKeyImpl -this4) {
        this(ch, group, interf, source);
    }

    private MembershipKeyImpl(MulticastChannel ch, InetAddress group, NetworkInterface interf, InetAddress source) {
        this.valid = true;
        this.stateLock = new Object();
        this.ch = ch;
        this.group = group;
        this.interf = interf;
        this.source = source;
    }

    public boolean isValid() {
        return this.valid;
    }

    void invalidate() {
        this.valid = false;
    }

    public void drop() {
        ((DatagramChannelImpl) this.ch).drop(this);
    }

    public MulticastChannel channel() {
        return this.ch;
    }

    public InetAddress group() {
        return this.group;
    }

    public NetworkInterface networkInterface() {
        return this.interf;
    }

    public InetAddress sourceAddress() {
        return this.source;
    }

    public MembershipKey block(InetAddress toBlock) throws IOException {
        if (this.source != null) {
            throw new IllegalStateException("key is source-specific");
        }
        synchronized (this.stateLock) {
            if (this.blockedSet == null || !this.blockedSet.contains(toBlock)) {
                ((DatagramChannelImpl) this.ch).block(this, toBlock);
                if (this.blockedSet == null) {
                    this.blockedSet = new HashSet();
                }
                this.blockedSet.-java_util_stream_DistinctOps$1-mthref-1(toBlock);
                return this;
            }
            return this;
        }
    }

    public MembershipKey unblock(InetAddress toUnblock) {
        synchronized (this.stateLock) {
            if (this.blockedSet == null || (this.blockedSet.contains(toUnblock) ^ 1) != 0) {
                throw new IllegalStateException("not blocked");
            }
            ((DatagramChannelImpl) this.ch).unblock(this, toUnblock);
            this.blockedSet.remove(toUnblock);
        }
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append('<');
        sb.append(this.group.getHostAddress());
        sb.append(',');
        sb.append(this.interf.getName());
        if (this.source != null) {
            sb.append(',');
            sb.append(this.source.getHostAddress());
        }
        sb.append('>');
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}
