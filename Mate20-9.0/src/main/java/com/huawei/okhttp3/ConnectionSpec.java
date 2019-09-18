package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocket;

public final class ConnectionSpec {
    private static final CipherSuite[] APPROVED_CIPHER_SUITES = {CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA};
    public static final ConnectionSpec CLEARTEXT = new Builder(false).build();
    public static final ConnectionSpec COMPATIBLE_TLS = new Builder(MODERN_TLS).tlsVersions(TlsVersion.TLS_1_0).supportsTlsExtensions(true).build();
    public static final ConnectionSpec MODERN_TLS = new Builder(true).cipherSuites(APPROVED_CIPHER_SUITES).tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0).supportsTlsExtensions(true).build();
    private static final CipherSuite[] RESTRICTED_CIPHER_SUITES = {CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256};
    public static final ConnectionSpec RESTRICTED_TLS = new Builder(true).cipherSuites(RESTRICTED_CIPHER_SUITES).tlsVersions(TlsVersion.TLS_1_2).supportsTlsExtensions(true).build();
    @Nullable
    final String[] cipherSuites;
    final boolean supportsTlsExtensions;
    final boolean tls;
    @Nullable
    final String[] tlsVersions;

    public static final class Builder {
        @Nullable
        String[] cipherSuites;
        boolean supportsTlsExtensions;
        boolean tls;
        @Nullable
        String[] tlsVersions;

        Builder(boolean tls2) {
            this.tls = tls2;
        }

        public Builder(ConnectionSpec connectionSpec) {
            this.tls = connectionSpec.tls;
            this.cipherSuites = connectionSpec.cipherSuites;
            this.tlsVersions = connectionSpec.tlsVersions;
            this.supportsTlsExtensions = connectionSpec.supportsTlsExtensions;
        }

        public Builder allEnabledCipherSuites() {
            if (this.tls) {
                this.cipherSuites = null;
                return this;
            }
            throw new IllegalStateException("no cipher suites for cleartext connections");
        }

        public Builder cipherSuites(CipherSuite... cipherSuites2) {
            if (this.tls) {
                String[] strings = new String[cipherSuites2.length];
                for (int i = 0; i < cipherSuites2.length; i++) {
                    strings[i] = cipherSuites2[i].javaName;
                }
                return cipherSuites(strings);
            }
            throw new IllegalStateException("no cipher suites for cleartext connections");
        }

        public Builder cipherSuites(String... cipherSuites2) {
            if (!this.tls) {
                throw new IllegalStateException("no cipher suites for cleartext connections");
            } else if (cipherSuites2.length != 0) {
                this.cipherSuites = (String[]) cipherSuites2.clone();
                return this;
            } else {
                throw new IllegalArgumentException("At least one cipher suite is required");
            }
        }

        public Builder allEnabledTlsVersions() {
            if (this.tls) {
                this.tlsVersions = null;
                return this;
            }
            throw new IllegalStateException("no TLS versions for cleartext connections");
        }

        public Builder tlsVersions(TlsVersion... tlsVersions2) {
            if (this.tls) {
                String[] strings = new String[tlsVersions2.length];
                for (int i = 0; i < tlsVersions2.length; i++) {
                    strings[i] = tlsVersions2[i].javaName;
                }
                return tlsVersions(strings);
            }
            throw new IllegalStateException("no TLS versions for cleartext connections");
        }

        public Builder tlsVersions(String... tlsVersions2) {
            if (!this.tls) {
                throw new IllegalStateException("no TLS versions for cleartext connections");
            } else if (tlsVersions2.length != 0) {
                this.tlsVersions = (String[]) tlsVersions2.clone();
                return this;
            } else {
                throw new IllegalArgumentException("At least one TLS version is required");
            }
        }

        public Builder supportsTlsExtensions(boolean supportsTlsExtensions2) {
            if (this.tls) {
                this.supportsTlsExtensions = supportsTlsExtensions2;
                return this;
            }
            throw new IllegalStateException("no TLS extensions for cleartext connections");
        }

        public ConnectionSpec build() {
            return new ConnectionSpec(this);
        }
    }

    ConnectionSpec(Builder builder) {
        this.tls = builder.tls;
        this.cipherSuites = builder.cipherSuites;
        this.tlsVersions = builder.tlsVersions;
        this.supportsTlsExtensions = builder.supportsTlsExtensions;
    }

    public boolean isTls() {
        return this.tls;
    }

    @Nullable
    public List<CipherSuite> cipherSuites() {
        if (this.cipherSuites != null) {
            return CipherSuite.forJavaNames(this.cipherSuites);
        }
        return null;
    }

    @Nullable
    public List<TlsVersion> tlsVersions() {
        if (this.tlsVersions != null) {
            return TlsVersion.forJavaNames(this.tlsVersions);
        }
        return null;
    }

    public boolean supportsTlsExtensions() {
        return this.supportsTlsExtensions;
    }

    /* access modifiers changed from: package-private */
    public void apply(SSLSocket sslSocket, boolean isFallback) {
        ConnectionSpec specToApply = supportedSpec(sslSocket, isFallback);
        if (specToApply.tlsVersions != null) {
            sslSocket.setEnabledProtocols(specToApply.tlsVersions);
        }
        if (specToApply.cipherSuites != null) {
            sslSocket.setEnabledCipherSuites(specToApply.cipherSuites);
        }
    }

    private ConnectionSpec supportedSpec(SSLSocket sslSocket, boolean isFallback) {
        String[] cipherSuitesIntersection;
        String[] tlsVersionsIntersection;
        if (this.cipherSuites != null) {
            cipherSuitesIntersection = Util.intersect(CipherSuite.ORDER_BY_NAME, sslSocket.getEnabledCipherSuites(), this.cipherSuites);
        } else {
            cipherSuitesIntersection = sslSocket.getEnabledCipherSuites();
        }
        if (this.tlsVersions != null) {
            tlsVersionsIntersection = Util.intersect(Util.NATURAL_ORDER, sslSocket.getEnabledProtocols(), this.tlsVersions);
        } else {
            tlsVersionsIntersection = sslSocket.getEnabledProtocols();
        }
        String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        int indexOfFallbackScsv = Util.indexOf(CipherSuite.ORDER_BY_NAME, supportedCipherSuites, "TLS_FALLBACK_SCSV");
        if (isFallback && indexOfFallbackScsv != -1) {
            cipherSuitesIntersection = Util.concat(cipherSuitesIntersection, supportedCipherSuites[indexOfFallbackScsv]);
        }
        return new Builder(this).cipherSuites(cipherSuitesIntersection).tlsVersions(tlsVersionsIntersection).build();
    }

    public boolean isCompatible(SSLSocket socket) {
        if (!this.tls) {
            return false;
        }
        if (this.tlsVersions != null && !Util.nonEmptyIntersection(Util.NATURAL_ORDER, this.tlsVersions, socket.getEnabledProtocols())) {
            return false;
        }
        if (this.cipherSuites == null || Util.nonEmptyIntersection(CipherSuite.ORDER_BY_NAME, this.cipherSuites, socket.getEnabledCipherSuites())) {
            return true;
        }
        return false;
    }

    public boolean equals(@Nullable Object other) {
        if (!(other instanceof ConnectionSpec)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        ConnectionSpec that = (ConnectionSpec) other;
        if (this.tls != that.tls) {
            return false;
        }
        if (!this.tls || (Arrays.equals(this.cipherSuites, that.cipherSuites) && Arrays.equals(this.tlsVersions, that.tlsVersions) && this.supportsTlsExtensions == that.supportsTlsExtensions)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (this.tls) {
            return (31 * ((31 * ((31 * 17) + Arrays.hashCode(this.cipherSuites))) + Arrays.hashCode(this.tlsVersions))) + (this.supportsTlsExtensions ^ true ? 1 : 0);
        }
        return 17;
    }

    public String toString() {
        if (!this.tls) {
            return "ConnectionSpec()";
        }
        String cipherSuitesString = this.cipherSuites != null ? cipherSuites().toString() : "[all enabled]";
        String tlsVersionsString = this.tlsVersions != null ? tlsVersions().toString() : "[all enabled]";
        return "ConnectionSpec(cipherSuites=" + cipherSuitesString + ", tlsVersions=" + tlsVersionsString + ", supportsTlsExtensions=" + this.supportsTlsExtensions + ")";
    }
}
