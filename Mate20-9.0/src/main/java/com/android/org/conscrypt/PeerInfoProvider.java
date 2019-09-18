package com.android.org.conscrypt;

abstract class PeerInfoProvider {
    private static final PeerInfoProvider NULL_PEER_INFO_PROVIDER = new PeerInfoProvider() {
        /* access modifiers changed from: package-private */
        public String getHostname() {
            return null;
        }

        public String getHostnameOrIP() {
            return null;
        }

        public int getPort() {
            return -1;
        }
    };

    /* access modifiers changed from: package-private */
    public abstract String getHostname();

    /* access modifiers changed from: package-private */
    public abstract String getHostnameOrIP();

    /* access modifiers changed from: package-private */
    public abstract int getPort();

    PeerInfoProvider() {
    }

    static PeerInfoProvider nullProvider() {
        return NULL_PEER_INFO_PROVIDER;
    }

    static PeerInfoProvider forHostAndPort(final String host, final int port) {
        return new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return host;
            }

            public String getHostnameOrIP() {
                return host;
            }

            public int getPort() {
                return port;
            }
        };
    }
}
