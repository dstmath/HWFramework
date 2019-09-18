package sun.nio.ch;

import java.net.SocketOption;

class ExtendedSocketOption {
    static final SocketOption<Boolean> SO_OOBINLINE = new SocketOption<Boolean>() {
        public String name() {
            return "SO_OOBINLINE";
        }

        public Class<Boolean> type() {
            return Boolean.class;
        }

        public String toString() {
            return name();
        }
    };

    private ExtendedSocketOption() {
    }
}
