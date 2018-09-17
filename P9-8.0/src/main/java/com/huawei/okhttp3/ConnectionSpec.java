package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLSocket;
import okhttp3.CipherSuite;
import okhttp3.TlsVersion;

public final class ConnectionSpec {
    private static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[]{CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA};
    public static final ConnectionSpec CLEARTEXT = new Builder(false).build();
    public static final ConnectionSpec COMPATIBLE_TLS = new Builder(MODERN_TLS).tlsVersions(TlsVersion.TLS_1_0).supportsTlsExtensions(true).build();
    public static final ConnectionSpec MODERN_TLS = new Builder(true).cipherSuites(APPROVED_CIPHER_SUITES).tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0).supportsTlsExtensions(true).build();
    final String[] cipherSuites;
    final boolean supportsTlsExtensions;
    final boolean tls;
    final String[] tlsVersions;

    public static final class Builder {
        String[] cipherSuites;
        boolean supportsTlsExtensions;
        boolean tls;
        String[] tlsVersions;

        Builder(boolean tls) {
            this.tls = tls;
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

        public Builder cipherSuites(CipherSuite... cipherSuites) {
            if (this.tls) {
                String[] strings = new String[cipherSuites.length];
                for (int i = 0; i < cipherSuites.length; i++) {
                    strings[i] = cipherSuites[i].javaName;
                }
                return cipherSuites(strings);
            }
            throw new IllegalStateException("no cipher suites for cleartext connections");
        }

        public Builder cipherSuites(String... cipherSuites) {
            if (!this.tls) {
                throw new IllegalStateException("no cipher suites for cleartext connections");
            } else if (cipherSuites.length == 0) {
                throw new IllegalArgumentException("At least one cipher suite is required");
            } else {
                this.cipherSuites = (String[]) cipherSuites.clone();
                return this;
            }
        }

        public Builder allEnabledTlsVersions() {
            if (this.tls) {
                this.tlsVersions = null;
                return this;
            }
            throw new IllegalStateException("no TLS versions for cleartext connections");
        }

        public Builder tlsVersions(TlsVersion... tlsVersions) {
            if (this.tls) {
                String[] strings = new String[tlsVersions.length];
                for (int i = 0; i < tlsVersions.length; i++) {
                    strings[i] = tlsVersions[i].javaName;
                }
                return tlsVersions(strings);
            }
            throw new IllegalStateException("no TLS versions for cleartext connections");
        }

        public Builder tlsVersions(String... tlsVersions) {
            if (!this.tls) {
                throw new IllegalStateException("no TLS versions for cleartext connections");
            } else if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TLS version is required");
            } else {
                this.tlsVersions = (String[]) tlsVersions.clone();
                return this;
            }
        }

        public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
            if (this.tls) {
                this.supportsTlsExtensions = supportsTlsExtensions;
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

    public List<CipherSuite> cipherSuites() {
        if (this.cipherSuites == null) {
            return null;
        }
        List<CipherSuite> result = new ArrayList(this.cipherSuites.length);
        for (String cipherSuite : this.cipherSuites) {
            result.add(CipherSuite.forJavaName(cipherSuite));
        }
        return Collections.unmodifiableList(result);
    }

    public List<TlsVersion> tlsVersions() {
        if (this.tlsVersions == null) {
            return null;
        }
        List<TlsVersion> result = new ArrayList(this.tlsVersions.length);
        for (String tlsVersion : this.tlsVersions) {
            result.add(TlsVersion.forJavaName(tlsVersion));
        }
        return Collections.unmodifiableList(result);
    }

    public boolean supportsTlsExtensions() {
        return this.supportsTlsExtensions;
    }

    void apply(SSLSocket sslSocket, boolean isFallback) {
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
            cipherSuitesIntersection = (String[]) Util.intersect(String.class, this.cipherSuites, sslSocket.getEnabledCipherSuites());
        } else {
            cipherSuitesIntersection = sslSocket.getEnabledCipherSuites();
        }
        if (this.tlsVersions != null) {
            tlsVersionsIntersection = (String[]) Util.intersect(String.class, this.tlsVersions, sslSocket.getEnabledProtocols());
        } else {
            tlsVersionsIntersection = sslSocket.getEnabledProtocols();
        }
        if (isFallback && Util.indexOf(sslSocket.getSupportedCipherSuites(), "TLS_FALLBACK_SCSV") != -1) {
            cipherSuitesIntersection = Util.concat(cipherSuitesIntersection, "TLS_FALLBACK_SCSV");
        }
        return new Builder(this).cipherSuites(cipherSuitesIntersection).tlsVersions(tlsVersionsIntersection).build();
    }

    public boolean isCompatible(SSLSocket socket) {
        if (!this.tls) {
            return false;
        }
        if (this.tlsVersions != null && (nonEmptyIntersection(this.tlsVersions, socket.getEnabledProtocols()) ^ 1) != 0) {
            return false;
        }
        if (this.cipherSuites == null || (nonEmptyIntersection(this.cipherSuites, socket.getEnabledCipherSuites()) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    private static boolean nonEmptyIntersection(String[] a, String[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0) {
            return false;
        }
        for (String toFind : a) {
            if (Util.indexOf(b, toFind) != -1) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object other) {
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
        return !this.tls || (Arrays.equals(this.cipherSuites, that.cipherSuites) && Arrays.equals(this.tlsVersions, that.tlsVersions) && this.supportsTlsExtensions == that.supportsTlsExtensions);
    }

    public int hashCode() {
        if (!this.tls) {
            return 17;
        }
        return ((((Arrays.hashCode(this.cipherSuites) + 527) * 31) + Arrays.hashCode(this.tlsVersions)) * 31) + (this.supportsTlsExtensions ? 0 : 1);
    }

    public String toString() {
        if (!this.tls) {
            return "ConnectionSpec()";
        }
        return "ConnectionSpec(cipherSuites=" + (this.cipherSuites != null ? cipherSuites().toString() : "[all enabled]") + ", tlsVersions=" + (this.tlsVersions != null ? tlsVersions().toString() : "[all enabled]") + ", supportsTlsExtensions=" + this.supportsTlsExtensions + ")";
    }
}
