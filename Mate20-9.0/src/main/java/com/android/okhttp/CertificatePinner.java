package com.android.okhttp;

import com.android.okhttp.internal.Util;
import com.android.okhttp.okio.ByteString;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLPeerUnverifiedException;

public final class CertificatePinner {
    public static final CertificatePinner DEFAULT = new Builder().build();
    private final Map<String, Set<ByteString>> hostnameToPins;

    public static final class Builder {
        /* access modifiers changed from: private */
        public final Map<String, Set<ByteString>> hostnameToPins = new LinkedHashMap();

        public Builder add(String hostname, String... pins) {
            if (hostname != null) {
                Set<ByteString> hostPins = new LinkedHashSet<>();
                Set<ByteString> previousPins = this.hostnameToPins.put(hostname, Collections.unmodifiableSet(hostPins));
                if (previousPins != null) {
                    hostPins.addAll(previousPins);
                }
                int length = pins.length;
                int i = 0;
                while (i < length) {
                    String pin = pins[i];
                    if (pin.startsWith("sha1/")) {
                        ByteString decodedPin = ByteString.decodeBase64(pin.substring("sha1/".length()));
                        if (decodedPin != null) {
                            hostPins.add(decodedPin);
                            i++;
                        } else {
                            throw new IllegalArgumentException("pins must be base64: " + pin);
                        }
                    } else {
                        throw new IllegalArgumentException("pins must start with 'sha1/': " + pin);
                    }
                }
                return this;
            }
            throw new IllegalArgumentException("hostname == null");
        }

        public CertificatePinner build() {
            return new CertificatePinner(this);
        }
    }

    private CertificatePinner(Builder builder) {
        this.hostnameToPins = Util.immutableMap(builder.hostnameToPins);
    }

    public void check(String hostname, List<Certificate> peerCertificates) throws SSLPeerUnverifiedException {
        Set<ByteString> pins = findMatchingPins(hostname);
        if (pins != null) {
            int i = 0;
            int size = peerCertificates.size();
            while (i < size) {
                if (!pins.contains(sha1((X509Certificate) peerCertificates.get(i)))) {
                    i++;
                } else {
                    return;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Certificate pinning failure!");
            StringBuilder message = sb.append("\n  Peer certificate chain:");
            int size2 = peerCertificates.size();
            for (int i2 = 0; i2 < size2; i2++) {
                X509Certificate x509Certificate = (X509Certificate) peerCertificates.get(i2);
                message.append("\n    ");
                message.append(pin(x509Certificate));
                message.append(": ");
                message.append(x509Certificate.getSubjectDN().getName());
            }
            message.append("\n  Pinned certificates for ");
            message.append(hostname);
            message.append(":");
            for (ByteString pin : pins) {
                message.append("\n    sha1/");
                message.append(pin.base64());
            }
            throw new SSLPeerUnverifiedException(message.toString());
        }
    }

    public void check(String hostname, Certificate... peerCertificates) throws SSLPeerUnverifiedException {
        check(hostname, (List<Certificate>) Arrays.asList(peerCertificates));
    }

    /* access modifiers changed from: package-private */
    public Set<ByteString> findMatchingPins(String hostname) {
        Set<ByteString> directPins = this.hostnameToPins.get(hostname);
        Set<ByteString> wildcardPins = null;
        int indexOfFirstDot = hostname.indexOf(46);
        if (indexOfFirstDot != hostname.lastIndexOf(46)) {
            Map<String, Set<ByteString>> map = this.hostnameToPins;
            wildcardPins = map.get("*." + hostname.substring(indexOfFirstDot + 1));
        }
        if (directPins == null && wildcardPins == null) {
            return null;
        }
        if (directPins != null && wildcardPins != null) {
            Set<ByteString> pins = new LinkedHashSet<>();
            pins.addAll(directPins);
            pins.addAll(wildcardPins);
            return pins;
        } else if (directPins != null) {
            return directPins;
        } else {
            return wildcardPins;
        }
    }

    public static String pin(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            return "sha1/" + sha1((X509Certificate) certificate).base64();
        }
        throw new IllegalArgumentException("Certificate pinning requires X509 certificates");
    }

    private static ByteString sha1(X509Certificate x509Certificate) {
        return Util.sha1(ByteString.of(x509Certificate.getPublicKey().getEncoded()));
    }
}
