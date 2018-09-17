package sun.security.util;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import sun.net.util.IPAddressUtil;
import sun.security.ssl.Krb5Helper;
import sun.security.x509.X500Name;

public class HostnameChecker {
    private static final int ALTNAME_DNS = 2;
    private static final int ALTNAME_IP = 7;
    private static final HostnameChecker INSTANCE_LDAP = null;
    private static final HostnameChecker INSTANCE_TLS = null;
    public static final byte TYPE_LDAP = (byte) 2;
    public static final byte TYPE_TLS = (byte) 1;
    private final byte checkType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.HostnameChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.HostnameChecker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.HostnameChecker.<clinit>():void");
    }

    private HostnameChecker(byte checkType) {
        this.checkType = checkType;
    }

    public static HostnameChecker getInstance(byte checkType) {
        if (checkType == 1) {
            return INSTANCE_TLS;
        }
        if (checkType == ALTNAME_DNS) {
            return INSTANCE_LDAP;
        }
        throw new IllegalArgumentException("Unknown check type: " + checkType);
    }

    public void match(String expectedName, X509Certificate cert) throws CertificateException {
        if (isIpAddress(expectedName)) {
            matchIP(expectedName, cert);
        } else {
            matchDNS(expectedName, cert);
        }
    }

    public static boolean match(String expectedName, Principal principal) {
        return expectedName.equalsIgnoreCase(getServerName(principal));
    }

    public static String getServerName(Principal principal) {
        return Krb5Helper.getPrincipalHostName(principal);
    }

    private static boolean isIpAddress(String name) {
        if (IPAddressUtil.isIPv4LiteralAddress(name) || IPAddressUtil.isIPv6LiteralAddress(name)) {
            return true;
        }
        return false;
    }

    private static void matchIP(String expectedIP, X509Certificate cert) throws CertificateException {
        Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
        if (subjAltNames == null) {
            throw new CertificateException("No subject alternative names present");
        }
        for (List<?> next : subjAltNames) {
            if (((Integer) next.get(0)).intValue() == ALTNAME_IP && expectedIP.equalsIgnoreCase((String) next.get(1))) {
                return;
            }
        }
        throw new CertificateException("No subject alternative names matching IP address " + expectedIP + " found");
    }

    private void matchDNS(String expectedName, X509Certificate cert) throws CertificateException {
        Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
        if (subjAltNames != null) {
            boolean foundDNS = false;
            for (List<?> next : subjAltNames) {
                if (((Integer) next.get(0)).intValue() == ALTNAME_DNS) {
                    foundDNS = true;
                    if (isMatched(expectedName, (String) next.get(1))) {
                        return;
                    }
                }
            }
            if (foundDNS) {
                throw new CertificateException("No subject alternative DNS name matching " + expectedName + " found.");
            }
        }
        DerValue derValue = getSubjectX500Name(cert).findMostSpecificAttribute(X500Name.commonName_oid);
        if (derValue != null) {
            try {
                if (isMatched(expectedName, derValue.getAsString())) {
                    return;
                }
            } catch (IOException e) {
            }
        }
        throw new CertificateException("No name matching " + expectedName + " found");
    }

    public static X500Name getSubjectX500Name(X509Certificate cert) throws CertificateParsingException {
        try {
            Principal subjectDN = cert.getSubjectDN();
            if (subjectDN instanceof X500Name) {
                return (X500Name) subjectDN;
            }
            return new X500Name(cert.getSubjectX500Principal().getEncoded());
        } catch (IOException e) {
            throw ((CertificateParsingException) new CertificateParsingException().initCause(e));
        }
    }

    private boolean isMatched(String name, String template) {
        if (this.checkType == 1) {
            return matchAllWildcards(name, template);
        }
        if (this.checkType == ALTNAME_DNS) {
            return matchLeftmostWildcard(name, template);
        }
        return false;
    }

    private static boolean matchAllWildcards(String name, String template) {
        name = name.toLowerCase();
        template = template.toLowerCase();
        StringTokenizer nameSt = new StringTokenizer(name, ".");
        StringTokenizer templateSt = new StringTokenizer(template, ".");
        if (nameSt.countTokens() != templateSt.countTokens()) {
            return false;
        }
        while (nameSt.hasMoreTokens()) {
            if (!matchWildCards(nameSt.nextToken(), templateSt.nextToken())) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchLeftmostWildcard(String name, String template) {
        name = name.toLowerCase();
        template = template.toLowerCase();
        int templateIdx = template.indexOf(".");
        int nameIdx = name.indexOf(".");
        if (templateIdx == -1) {
            templateIdx = template.length();
        }
        if (nameIdx == -1) {
            nameIdx = name.length();
        }
        if (matchWildCards(name.substring(0, nameIdx), template.substring(0, templateIdx))) {
            return template.substring(templateIdx).equals(name.substring(nameIdx));
        }
        return false;
    }

    private static boolean matchWildCards(String name, String template) {
        int wildcardIdx = template.indexOf("*");
        if (wildcardIdx == -1) {
            return name.equals(template);
        }
        boolean isBeginning = true;
        String beforeWildcard = "";
        String afterWildcard = template;
        while (wildcardIdx != -1) {
            beforeWildcard = afterWildcard.substring(0, wildcardIdx);
            afterWildcard = afterWildcard.substring(wildcardIdx + 1);
            int beforeStartIdx = name.indexOf(beforeWildcard);
            if (beforeStartIdx == -1 || (isBeginning && beforeStartIdx != 0)) {
                return false;
            }
            isBeginning = false;
            name = name.substring(beforeWildcard.length() + beforeStartIdx);
            wildcardIdx = afterWildcard.indexOf("*");
        }
        return name.endsWith(afterWildcard);
    }
}
