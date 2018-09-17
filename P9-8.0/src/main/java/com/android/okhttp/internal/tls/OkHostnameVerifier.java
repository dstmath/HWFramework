package com.android.okhttp.internal.tls;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public final class OkHostnameVerifier implements HostnameVerifier {
    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;
    public static final OkHostnameVerifier INSTANCE = new OkHostnameVerifier();
    private static final Pattern VERIFY_AS_IP_ADDRESS = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");

    private OkHostnameVerifier() {
    }

    public boolean verify(String host, SSLSession session) {
        try {
            return verify(host, (X509Certificate) session.getPeerCertificates()[0]);
        } catch (SSLException e) {
            return false;
        }
    }

    public boolean verify(String host, X509Certificate certificate) {
        if (verifyAsIpAddress(host)) {
            return verifyIpAddress(host, certificate);
        }
        return verifyHostName(host, certificate);
    }

    static boolean verifyAsIpAddress(String host) {
        return VERIFY_AS_IP_ADDRESS.matcher(host).matches();
    }

    private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
        List<String> altNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
        int size = altNames.size();
        for (int i = 0; i < size; i++) {
            if (ipAddress.equalsIgnoreCase((String) altNames.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyHostName(String hostName, X509Certificate certificate) {
        hostName = hostName.toLowerCase(Locale.US);
        boolean hasDns = false;
        List<String> altNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
        int size = altNames.size();
        for (int i = 0; i < size; i++) {
            hasDns = true;
            if (verifyHostName(hostName, (String) altNames.get(i))) {
                return true;
            }
        }
        if (!hasDns) {
            String cn = new DistinguishedNameParser(certificate.getSubjectX500Principal()).findMostSpecific("cn");
            if (cn != null) {
                return verifyHostName(hostName, cn);
            }
        }
        return false;
    }

    public static List<String> allSubjectAltNames(X509Certificate certificate) {
        List<String> altIpaNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
        List<String> altDnsNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
        List<String> result = new ArrayList(altIpaNames.size() + altDnsNames.size());
        result.addAll(altIpaNames);
        result.addAll(altDnsNames);
        return result;
    }

    private static List<String> getSubjectAltNames(X509Certificate certificate, int type) {
        List<String> result = new ArrayList();
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                return Collections.emptyList();
            }
            Iterator subjectAltName$iterator = subjectAltNames.iterator();
            while (subjectAltName$iterator.hasNext()) {
                List<?> entry = (List) subjectAltName$iterator.next();
                if (entry != null && entry.size() >= ALT_DNS_NAME) {
                    Integer altNameType = (Integer) entry.get(0);
                    if (altNameType != null && altNameType.intValue() == type) {
                        String altName = (String) entry.get(1);
                        if (altName != null) {
                            result.add(altName);
                        }
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000d, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:31:0x0094, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean verifyHostName(String hostName, String pattern) {
        if (hostName == null || hostName.length() == 0 || hostName.startsWith(".") || hostName.endsWith("..") || pattern == null || pattern.length() == 0 || pattern.startsWith(".") || pattern.endsWith("..")) {
            return false;
        }
        if (!hostName.endsWith(".")) {
            hostName = hostName + '.';
        }
        if (!pattern.endsWith(".")) {
            pattern = pattern + '.';
        }
        pattern = pattern.toLowerCase(Locale.US);
        if (!pattern.contains("*")) {
            return hostName.equals(pattern);
        }
        if (!pattern.startsWith("*.") || pattern.indexOf(42, 1) != -1 || hostName.length() < pattern.length() || "*.".equals(pattern)) {
            return false;
        }
        String suffix = pattern.substring(1);
        if (!hostName.endsWith(suffix)) {
            return false;
        }
        int suffixStartIndexInHostName = hostName.length() - suffix.length();
        return suffixStartIndexInHostName <= 0 || hostName.lastIndexOf(46, suffixStartIndexInHostName - 1) == -1;
    }
}
