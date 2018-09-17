package sun.security.x509;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.ObjectIdentifier;

/* compiled from: AVA */
class AVAKeyword {
    private static final Map<String, AVAKeyword> keywordMap = new HashMap();
    private static final Map<ObjectIdentifier, AVAKeyword> oidMap = new HashMap();
    private String keyword;
    private ObjectIdentifier oid;
    private boolean rfc1779Compliant;
    private boolean rfc2253Compliant;

    private AVAKeyword(String keyword, ObjectIdentifier oid, boolean rfc1779Compliant, boolean rfc2253Compliant) {
        this.keyword = keyword;
        this.oid = oid;
        this.rfc1779Compliant = rfc1779Compliant;
        this.rfc2253Compliant = rfc2253Compliant;
        oidMap.put(oid, this);
        keywordMap.put(keyword, this);
    }

    private boolean isCompliant(int standard) {
        switch (standard) {
            case 1:
                return true;
            case 2:
                return this.rfc1779Compliant;
            case 3:
                return this.rfc2253Compliant;
            default:
                throw new IllegalArgumentException("Invalid standard " + standard);
        }
    }

    static ObjectIdentifier getOID(String keyword, int standard, Map<String, String> extraKeywordMap) throws IOException {
        keyword = keyword.toUpperCase(Locale.ENGLISH);
        if (standard != 3) {
            keyword = keyword.trim();
        } else if (keyword.startsWith(" ") || keyword.endsWith(" ")) {
            throw new IOException("Invalid leading or trailing space in keyword \"" + keyword + "\"");
        }
        String oidString = (String) extraKeywordMap.get(keyword);
        if (oidString != null) {
            return new ObjectIdentifier(oidString);
        }
        AVAKeyword ak = (AVAKeyword) keywordMap.get(keyword);
        if (ak != null && ak.isCompliant(standard)) {
            return ak.oid;
        }
        if (standard == 1 && keyword.startsWith("OID.")) {
            keyword = keyword.substring(4);
        }
        boolean number = false;
        if (keyword.length() != 0) {
            char ch = keyword.charAt(0);
            if (ch >= '0' && ch <= '9') {
                number = true;
            }
        }
        if (number) {
            return new ObjectIdentifier(keyword);
        }
        throw new IOException("Invalid keyword \"" + keyword + "\"");
    }

    static String getKeyword(ObjectIdentifier oid, int standard) {
        return getKeyword(oid, standard, Collections.emptyMap());
    }

    static String getKeyword(ObjectIdentifier oid, int standard, Map<String, String> extraOidMap) {
        String oidString = oid.toString();
        String keywordString = (String) extraOidMap.get(oidString);
        if (keywordString == null) {
            AVAKeyword ak = (AVAKeyword) oidMap.get(oid);
            if (ak != null && ak.isCompliant(standard)) {
                return ak.keyword;
            }
            if (standard == 3) {
                return oidString;
            }
            return "OID." + oidString;
        } else if (keywordString.length() == 0) {
            throw new IllegalArgumentException("keyword cannot be empty");
        } else {
            keywordString = keywordString.trim();
            char c = keywordString.charAt(0);
            if (c < 'A' || c > 'z' || (c > 'Z' && c < 'a')) {
                throw new IllegalArgumentException("keyword does not start with letter");
            }
            for (int i = 1; i < keywordString.length(); i++) {
                c = keywordString.charAt(i);
                if ((c < 'A' || c > 'z' || (c > 'Z' && c < 'a')) && ((c < '0' || c > '9') && c != '_')) {
                    throw new IllegalArgumentException("keyword character is not a letter, digit, or underscore");
                }
            }
            return keywordString;
        }
    }

    static boolean hasKeyword(ObjectIdentifier oid, int standard) {
        AVAKeyword ak = (AVAKeyword) oidMap.get(oid);
        if (ak == null) {
            return false;
        }
        return ak.isCompliant(standard);
    }

    static {
        AVAKeyword aVAKeyword = new AVAKeyword("CN", X500Name.commonName_oid, true, true);
        aVAKeyword = new AVAKeyword("C", X500Name.countryName_oid, true, true);
        aVAKeyword = new AVAKeyword("L", X500Name.localityName_oid, true, true);
        aVAKeyword = new AVAKeyword("S", X500Name.stateName_oid, false, false);
        aVAKeyword = new AVAKeyword("ST", X500Name.stateName_oid, true, true);
        aVAKeyword = new AVAKeyword("O", X500Name.orgName_oid, true, true);
        aVAKeyword = new AVAKeyword("OU", X500Name.orgUnitName_oid, true, true);
        aVAKeyword = new AVAKeyword("T", X500Name.title_oid, false, false);
        aVAKeyword = new AVAKeyword("IP", X500Name.ipAddress_oid, false, false);
        aVAKeyword = new AVAKeyword("STREET", X500Name.streetAddress_oid, true, true);
        aVAKeyword = new AVAKeyword("DC", X500Name.DOMAIN_COMPONENT_OID, false, true);
        aVAKeyword = new AVAKeyword("DNQUALIFIER", X500Name.DNQUALIFIER_OID, false, false);
        aVAKeyword = new AVAKeyword("DNQ", X500Name.DNQUALIFIER_OID, false, false);
        aVAKeyword = new AVAKeyword("SURNAME", X500Name.SURNAME_OID, false, false);
        aVAKeyword = new AVAKeyword("GIVENNAME", X500Name.GIVENNAME_OID, false, false);
        aVAKeyword = new AVAKeyword("INITIALS", X500Name.INITIALS_OID, false, false);
        aVAKeyword = new AVAKeyword("GENERATION", X500Name.GENERATIONQUALIFIER_OID, false, false);
        aVAKeyword = new AVAKeyword("EMAIL", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        aVAKeyword = new AVAKeyword("EMAILADDRESS", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        aVAKeyword = new AVAKeyword("UID", X500Name.userid_oid, false, true);
        aVAKeyword = new AVAKeyword("SERIALNUMBER", X500Name.SERIALNUMBER_OID, false, false);
    }
}
