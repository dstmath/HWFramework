package com.android.okhttp;

import com.android.okhttp.internal.Util;
import com.squareup.okhttp.CipherSuite;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLSocket;

public final class ConnectionSpec {
    private static final CipherSuite[] APPROVED_CIPHER_SUITES = null;
    public static final ConnectionSpec CLEARTEXT = null;
    public static final ConnectionSpec COMPATIBLE_TLS = null;
    public static final ConnectionSpec MODERN_TLS = null;
    private final String[] cipherSuites;
    final boolean supportsTlsExtensions;
    final boolean tls;
    private final String[] tlsVersions;

    public static final class Builder {
        private String[] cipherSuites;
        private boolean supportsTlsExtensions;
        private boolean tls;
        private String[] tlsVersions;

        Builder(boolean tls) {
            this.tls = tls;
        }

        public Builder(ConnectionSpec connectionSpec) {
            this.tls = connectionSpec.tls;
            this.cipherSuites = connectionSpec.cipherSuites;
            this.tlsVersions = connectionSpec.tlsVersions;
            this.supportsTlsExtensions = connectionSpec.supportsTlsExtensions;
        }

        public Builder cipherSuites(CipherSuite... cipherSuites) {
            if (this.tls) {
                String[] strings = new String[cipherSuites.length];
                for (int i = 0; i < cipherSuites.length; i++) {
                    strings[i] = cipherSuites[i].javaName;
                }
                this.cipherSuites = strings;
                return this;
            }
            throw new IllegalStateException("no cipher suites for cleartext connections");
        }

        public Builder cipherSuites(String... cipherSuites) {
            if (this.tls) {
                if (cipherSuites == null) {
                    this.cipherSuites = null;
                } else {
                    this.cipherSuites = (String[]) cipherSuites.clone();
                }
                return this;
            }
            throw new IllegalStateException("no cipher suites for cleartext connections");
        }

        public Builder tlsVersions(TlsVersion... tlsVersions) {
            if (!this.tls) {
                throw new IllegalStateException("no TLS versions for cleartext connections");
            } else if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TlsVersion is required");
            } else {
                String[] strings = new String[tlsVersions.length];
                for (int i = 0; i < tlsVersions.length; i++) {
                    strings[i] = tlsVersions[i].javaName;
                }
                this.tlsVersions = strings;
                return this;
            }
        }

        public Builder tlsVersions(String... tlsVersions) {
            if (this.tls) {
                if (tlsVersions == null) {
                    this.tlsVersions = null;
                } else {
                    this.tlsVersions = (String[]) tlsVersions.clone();
                }
                return this;
            }
            throw new IllegalStateException("no TLS versions for cleartext connections");
        }

        public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
            if (this.tls) {
                this.supportsTlsExtensions = supportsTlsExtensions;
                return this;
            }
            throw new IllegalStateException("no TLS extensions for cleartext connections");
        }

        public ConnectionSpec build() {
            return new ConnectionSpec();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.ConnectionSpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.ConnectionSpec.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.ConnectionSpec.<clinit>():void");
    }

    private ConnectionSpec(Builder builder) {
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
        Object[] result = new CipherSuite[this.cipherSuites.length];
        for (int i = 0; i < this.cipherSuites.length; i++) {
            result[i] = CipherSuite.forJavaName(this.cipherSuites[i]);
        }
        return Util.immutableList(result);
    }

    public List<TlsVersion> tlsVersions() {
        Object[] result = new TlsVersion[this.tlsVersions.length];
        for (int i = 0; i < this.tlsVersions.length; i++) {
            result[i] = TlsVersion.forJavaName(this.tlsVersions[i]);
        }
        return Util.immutableList(result);
    }

    public boolean supportsTlsExtensions() {
        return this.supportsTlsExtensions;
    }

    void apply(SSLSocket sslSocket, boolean isFallback) {
        ConnectionSpec specToApply = supportedSpec(sslSocket, isFallback);
        sslSocket.setEnabledProtocols(specToApply.tlsVersions);
        String[] cipherSuitesToEnable = specToApply.cipherSuites;
        if (cipherSuitesToEnable != null) {
            sslSocket.setEnabledCipherSuites(cipherSuitesToEnable);
        }
    }

    private ConnectionSpec supportedSpec(SSLSocket sslSocket, boolean isFallback) {
        String[] strArr = null;
        if (this.cipherSuites != null) {
            strArr = (String[]) Util.intersect(String.class, this.cipherSuites, sslSocket.getEnabledCipherSuites());
        }
        if (isFallback) {
            String fallbackScsv = "TLS_FALLBACK_SCSV";
            if (Arrays.asList(sslSocket.getSupportedCipherSuites()).contains("TLS_FALLBACK_SCSV")) {
                String[] oldEnabledCipherSuites;
                if (strArr != null) {
                    oldEnabledCipherSuites = strArr;
                } else {
                    oldEnabledCipherSuites = sslSocket.getEnabledCipherSuites();
                }
                String[] newEnabledCipherSuites = new String[(oldEnabledCipherSuites.length + 1)];
                System.arraycopy(oldEnabledCipherSuites, 0, newEnabledCipherSuites, 0, oldEnabledCipherSuites.length);
                newEnabledCipherSuites[newEnabledCipherSuites.length - 1] = "TLS_FALLBACK_SCSV";
                strArr = newEnabledCipherSuites;
            }
        }
        return new Builder(this).cipherSuites(strArr).tlsVersions((String[]) Util.intersect(String.class, this.tlsVersions, sslSocket.getEnabledProtocols())).build();
    }

    public boolean isCompatible(SSLSocket socket) {
        if (!this.tls) {
            return false;
        }
        if (!nonEmptyIntersection(this.tlsVersions, socket.getEnabledProtocols())) {
            return false;
        }
        boolean nonEmptyIntersection = this.cipherSuites == null ? socket.getEnabledCipherSuites().length > 0 : nonEmptyIntersection(this.cipherSuites, socket.getEnabledCipherSuites());
        return nonEmptyIntersection;
    }

    private static boolean nonEmptyIntersection(String[] a, String[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0) {
            return false;
        }
        for (String toFind : a) {
            if (contains(b, toFind)) {
                return true;
            }
        }
        return false;
    }

    private static <T> boolean contains(T[] array, T value) {
        for (T arrayValue : array) {
            if (Util.equal(value, arrayValue)) {
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
        List<CipherSuite> cipherSuites = cipherSuites();
        return "ConnectionSpec(cipherSuites=" + (cipherSuites == null ? "[use default]" : cipherSuites.toString()) + ", tlsVersions=" + tlsVersions() + ", supportsTlsExtensions=" + this.supportsTlsExtensions + ")";
    }
}
