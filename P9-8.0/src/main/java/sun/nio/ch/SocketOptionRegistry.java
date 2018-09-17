package sun.nio.ch;

import java.net.ProtocolFamily;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.util.HashMap;
import java.util.Map;

class SocketOptionRegistry {

    private static class LazyInitialization {
        static final Map<RegistryKey, OptionKey> options = options();

        private LazyInitialization() {
        }

        private static Map<RegistryKey, OptionKey> options() {
            Map<RegistryKey, OptionKey> map = new HashMap();
            map.put(new RegistryKey(StandardSocketOptions.SO_BROADCAST, Net.UNSPEC), new OptionKey(1, 6));
            map.put(new RegistryKey(StandardSocketOptions.SO_KEEPALIVE, Net.UNSPEC), new OptionKey(1, 9));
            map.put(new RegistryKey(StandardSocketOptions.SO_LINGER, Net.UNSPEC), new OptionKey(1, 13));
            map.put(new RegistryKey(StandardSocketOptions.SO_SNDBUF, Net.UNSPEC), new OptionKey(1, 7));
            map.put(new RegistryKey(StandardSocketOptions.SO_RCVBUF, Net.UNSPEC), new OptionKey(1, 8));
            map.put(new RegistryKey(StandardSocketOptions.SO_REUSEADDR, Net.UNSPEC), new OptionKey(1, 2));
            map.put(new RegistryKey(StandardSocketOptions.TCP_NODELAY, Net.UNSPEC), new OptionKey(6, 1));
            map.put(new RegistryKey(StandardSocketOptions.IP_TOS, StandardProtocolFamily.INET), new OptionKey(0, 1));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_IF, StandardProtocolFamily.INET), new OptionKey(0, 32));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_TTL, StandardProtocolFamily.INET), new OptionKey(0, 33));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_LOOP, StandardProtocolFamily.INET), new OptionKey(0, 34));
            map.put(new RegistryKey(StandardSocketOptions.IP_TOS, StandardProtocolFamily.INET6), new OptionKey(41, 67));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_IF, StandardProtocolFamily.INET6), new OptionKey(41, 17));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_TTL, StandardProtocolFamily.INET6), new OptionKey(41, 18));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_LOOP, StandardProtocolFamily.INET6), new OptionKey(41, 19));
            map.put(new RegistryKey(ExtendedSocketOption.SO_OOBINLINE, Net.UNSPEC), new OptionKey(1, 10));
            return map;
        }
    }

    private static class RegistryKey {
        private final ProtocolFamily family;
        private final SocketOption<?> name;

        RegistryKey(SocketOption<?> name, ProtocolFamily family) {
            this.name = name;
            this.family = family;
        }

        public int hashCode() {
            return this.name.hashCode() + this.family.hashCode();
        }

        public boolean equals(Object ob) {
            if (ob == null || !(ob instanceof RegistryKey)) {
                return false;
            }
            RegistryKey other = (RegistryKey) ob;
            if (this.name == other.name && this.family == other.family) {
                return true;
            }
            return false;
        }
    }

    private SocketOptionRegistry() {
    }

    public static OptionKey findOption(SocketOption<?> name, ProtocolFamily family) {
        return (OptionKey) LazyInitialization.options.get(new RegistryKey(name, family));
    }
}
