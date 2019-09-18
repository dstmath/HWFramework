package sun.nio.ch;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.MembershipKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class MembershipRegistry {
    private Map<InetAddress, List<MembershipKeyImpl>> groups = null;

    MembershipRegistry() {
    }

    /* access modifiers changed from: package-private */
    public MembershipKey checkMembership(InetAddress group, NetworkInterface interf, InetAddress source) {
        if (this.groups != null) {
            List<MembershipKeyImpl> keys = this.groups.get(group);
            if (keys != null) {
                for (MembershipKeyImpl key : keys) {
                    if (key.networkInterface().equals(interf)) {
                        if (source == null) {
                            if (key.sourceAddress() == null) {
                                return key;
                            }
                            throw new IllegalStateException("Already a member to receive all packets");
                        } else if (key.sourceAddress() == null) {
                            throw new IllegalStateException("Already have source-specific membership");
                        } else if (source.equals(key.sourceAddress())) {
                            return key;
                        }
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void add(MembershipKeyImpl key) {
        List<MembershipKeyImpl> keys;
        InetAddress group = key.group();
        if (this.groups == null) {
            this.groups = new HashMap();
            keys = null;
        } else {
            keys = this.groups.get(group);
        }
        if (keys == null) {
            keys = new LinkedList<>();
            this.groups.put(group, keys);
        }
        keys.add(key);
    }

    /* access modifiers changed from: package-private */
    public void remove(MembershipKeyImpl key) {
        InetAddress group = key.group();
        List<MembershipKeyImpl> keys = this.groups.get(group);
        if (keys != null) {
            Iterator<MembershipKeyImpl> i = keys.iterator();
            while (true) {
                if (i.hasNext()) {
                    if (i.next() == key) {
                        i.remove();
                        break;
                    }
                } else {
                    break;
                }
            }
            if (keys.isEmpty()) {
                this.groups.remove(group);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateAll() {
        if (this.groups != null) {
            for (InetAddress group : this.groups.keySet()) {
                for (MembershipKeyImpl key : this.groups.get(group)) {
                    key.invalidate();
                }
            }
        }
    }
}
