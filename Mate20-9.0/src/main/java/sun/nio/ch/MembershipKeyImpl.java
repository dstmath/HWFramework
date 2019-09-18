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

        Type4(MulticastChannel ch, InetAddress group, NetworkInterface interf, InetAddress source, int groupAddress2, int interfAddress2, int sourceAddress2) {
            super(ch, group, interf, source);
            this.groupAddress = groupAddress2;
            this.interfAddress = interfAddress2;
            this.sourceAddress = sourceAddress2;
        }

        /* access modifiers changed from: package-private */
        public int groupAddress() {
            return this.groupAddress;
        }

        /* access modifiers changed from: package-private */
        public int interfaceAddress() {
            return this.interfAddress;
        }

        /* access modifiers changed from: package-private */
        public int source() {
            return this.sourceAddress;
        }
    }

    static class Type6 extends MembershipKeyImpl {
        private final byte[] groupAddress;
        private final int index;
        private final byte[] sourceAddress;

        Type6(MulticastChannel ch, InetAddress group, NetworkInterface interf, InetAddress source, byte[] groupAddress2, int index2, byte[] sourceAddress2) {
            super(ch, group, interf, source);
            this.groupAddress = groupAddress2;
            this.index = index2;
            this.sourceAddress = sourceAddress2;
        }

        /* access modifiers changed from: package-private */
        public byte[] groupAddress() {
            return this.groupAddress;
        }

        /* access modifiers changed from: package-private */
        public int index() {
            return this.index;
        }

        /* access modifiers changed from: package-private */
        public byte[] source() {
            return this.sourceAddress;
        }
    }

    private MembershipKeyImpl(MulticastChannel ch2, InetAddress group2, NetworkInterface interf2, InetAddress source2) {
        this.valid = true;
        this.stateLock = new Object();
        this.ch = ch2;
        this.group = group2;
        this.interf = interf2;
        this.source = source2;
    }

    public boolean isValid() {
        return this.valid;
    }

    /* access modifiers changed from: package-private */
    public void invalidate() {
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
        if (this.source == null) {
            synchronized (this.stateLock) {
                if (this.blockedSet != null && this.blockedSet.contains(toBlock)) {
                    return this;
                }
                ((DatagramChannelImpl) this.ch).block(this, toBlock);
                if (this.blockedSet == null) {
                    this.blockedSet = new HashSet<>();
                }
                this.blockedSet.add(toBlock);
                return this;
            }
        }
        throw new IllegalStateException("key is source-specific");
    }

    public MembershipKey unblock(InetAddress toUnblock) {
        synchronized (this.stateLock) {
            if (this.blockedSet == null || !this.blockedSet.contains(toUnblock)) {
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
        return sb.toString();
    }
}
