package org.apache.http.conn.ssl;

import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

@Deprecated
public abstract class AbstractVerifier implements X509HostnameVerifier {
    private static final String[] BAD_COUNTRY_2LDS = new String[]{"ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info", "lg", "ne", "net", "or", "org"};
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    static {
        Arrays.sort(BAD_COUNTRY_2LDS);
    }

    public final void verify(String host, SSLSocket ssl) throws IOException {
        if (host == null) {
            throw new NullPointerException("host to verify is null");
        }
        verify(host, ssl.getSession().getPeerCertificates()[0]);
    }

    public final boolean verify(String host, SSLSession session) {
        try {
            verify(host, session.getPeerCertificates()[0]);
            return true;
        } catch (SSLException e) {
            return false;
        }
    }

    public final void verify(String host, X509Certificate cert) throws SSLException {
        verify(host, getCNs(cert), getDNSSubjectAlts(cert));
    }

    public final void verify(String host, String[] cns, String[] subjectAlts, boolean strictWithSubDomains) throws SSLException {
        LinkedList<String> names = new LinkedList();
        if (!(cns == null || cns.length <= 0 || cns[0] == null)) {
            names.add(cns[0]);
        }
        if (subjectAlts != null) {
            for (String subjectAlt : subjectAlts) {
                if (subjectAlt != null) {
                    names.add(subjectAlt);
                }
            }
        }
        if (names.isEmpty()) {
            throw new SSLException("Certificate for <" + host + "> doesn't contain CN or DNS subjectAlt");
        }
        StringBuffer buf = new StringBuffer();
        String hostName = host.trim().toLowerCase(Locale.ENGLISH);
        boolean match = false;
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            int doWildcard;
            String cn = ((String) it.next()).toLowerCase(Locale.ENGLISH);
            buf.append(" <");
            buf.append(cn);
            buf.append('>');
            if (it.hasNext()) {
                buf.append(" OR");
            }
            if (cn.startsWith("*.") && cn.indexOf(46, 2) != -1 && acceptableCountryWildcard(cn)) {
                doWildcard = isIPv4Address(host) ^ 1;
            } else {
                doWildcard = 0;
            }
            if (doWildcard != 0) {
                match = hostName.endsWith(cn.substring(1));
                if (match && strictWithSubDomains) {
                    if (countDots(hostName) == countDots(cn)) {
                        match = true;
                        continue;
                    } else {
                        match = false;
                        continue;
                    }
                }
            } else {
                match = hostName.equals(cn);
                continue;
            }
            if (match) {
                break;
            }
        }
        if (!match) {
            throw new SSLException("hostname in certificate didn't match: <" + host + "> !=" + buf);
        }
    }

    public static boolean acceptableCountryWildcard(String cn) {
        boolean z = true;
        int cnLen = cn.length();
        if (cnLen < 7 || cnLen > 9 || cn.charAt(cnLen - 3) != '.') {
            return true;
        }
        if (Arrays.binarySearch(BAD_COUNTRY_2LDS, cn.substring(2, cnLen - 3)) >= 0) {
            z = false;
        }
        return z;
    }

    public static String[] getCNs(X509Certificate cert) {
        List<String> cnList = new AndroidDistinguishedNameParser(cert.getSubjectX500Principal()).getAllMostSpecificFirst("cn");
        if (cnList.isEmpty()) {
            return null;
        }
        String[] cns = new String[cnList.size()];
        cnList.toArray(cns);
        return cns;
    }

    public static String[] getDNSSubjectAlts(X509Certificate cert) {
        LinkedList<String> subjectAltList = new LinkedList();
        Iterable c = null;
        try {
            c = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException cpe) {
            Logger.getLogger(AbstractVerifier.class.getName()).log(Level.FINE, "Error parsing certificate.", cpe);
        }
        if (c != null) {
            for (List<?> aC : c) {
                List<?> list = aC;
                if (((Integer) aC.get(0)).intValue() == 2) {
                    subjectAltList.add((String) aC.get(1));
                }
            }
        }
        if (subjectAltList.isEmpty()) {
            return null;
        }
        String[] subjectAlts = new String[subjectAltList.size()];
        subjectAltList.toArray(subjectAlts);
        return subjectAlts;
    }

    public static int countDots(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }

    private static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }
}
