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
    public static final CertificatePinner DEFAULT = null;
    private final Map<String, Set<ByteString>> hostnameToPins;

    public static final class Builder {
        private final Map<String, Set<ByteString>> hostnameToPins;

        public Builder() {
            this.hostnameToPins = new LinkedHashMap();
        }

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
            return new CertificatePinner();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.CertificatePinner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.CertificatePinner.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.CertificatePinner.<clinit>():void");
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
