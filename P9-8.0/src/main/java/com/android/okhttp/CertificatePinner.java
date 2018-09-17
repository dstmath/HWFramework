package com.android.okhttp;

import com.android.okhttp.internal.Util;
import com.android.okhttp.okio.ByteString;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
        private final Map<String, Set<ByteString>> hostnameToPins = new LinkedHashMap();

        public Builder add(String hostname, String... pins) {
            if (hostname == null) {
                throw new IllegalArgumentException("hostname == null");
            }
            Set<okio.ByteString> hostPins = new LinkedHashSet();
            Set<okio.ByteString> previousPins = (Set) this.hostnameToPins.put(hostname, Collections.unmodifiableSet(hostPins));
            if (previousPins != null) {
                hostPins.addAll(previousPins);
            }
            int i = 0;
            int length = pins.length;
            while (i < length) {
                String pin = pins[i];
                if (pin.startsWith("sha1/")) {
                    ByteString decodedPin = ByteString.decodeBase64(pin.substring("sha1/".length()));
                    if (decodedPin == null) {
                        throw new IllegalArgumentException("pins must be base64: " + pin);
                    }
                    hostPins.add(decodedPin);
                    i++;
                } else {
                    throw new IllegalArgumentException("pins must start with 'sha1/': " + pin);
                }
            }
            return this;
        }

        public CertificatePinner build() {
            return new CertificatePinner(this, null);
        }
    }

    /* synthetic */ CertificatePinner(Builder builder, CertificatePinner -this1) {
        this(builder);
    }

    private CertificatePinner(Builder builder) {
        this.hostnameToPins = Util.immutableMap(builder.hostnameToPins);
    }

    public void check(String hostname, List<Certificate> peerCertificates) throws SSLPeerUnverifiedException {
        Set<okio.ByteString> pins = findMatchingPins(hostname);
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
            StringBuilder message = new StringBuilder().append("Certificate pinning failure!").append("\n  Peer certificate chain:");
            size = peerCertificates.size();
            for (i = 0; i < size; i++) {
                X509Certificate x509Certificate = (X509Certificate) peerCertificates.get(i);
                message.append("\n    ").append(pin(x509Certificate)).append(": ").append(x509Certificate.getSubjectDN().getName());
            }
            message.append("\n  Pinned certificates for ").append(hostname).append(":");
            Iterator pin$iterator = pins.iterator();
            while (pin$iterator.hasNext()) {
                message.append("\n    sha1/").append(((ByteString) pin$iterator.next()).base64());
            }
            throw new SSLPeerUnverifiedException(message.toString());
        }
    }

    public void check(String hostname, Certificate... peerCertificates) throws SSLPeerUnverifiedException {
        check(hostname, Arrays.asList(peerCertificates));
    }

    Set<ByteString> findMatchingPins(String hostname) {
        Set<okio.ByteString> directPins = (Set) this.hostnameToPins.get(hostname);
        Object obj = null;
        int indexOfFirstDot = hostname.indexOf(46);
        if (indexOfFirstDot != hostname.lastIndexOf(46)) {
            obj = (Set) this.hostnameToPins.get("*." + hostname.substring(indexOfFirstDot + 1));
        }
        if (directPins == null && obj == null) {
            return null;
        }
        if (directPins != null && obj != null) {
            Set<okio.ByteString> pins = new LinkedHashSet();
            pins.addAll(directPins);
            pins.addAll(obj);
            return pins;
        } else if (directPins != null) {
            return directPins;
        } else {
            return obj;
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
