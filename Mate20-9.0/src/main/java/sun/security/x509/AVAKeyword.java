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

    private AVAKeyword(String keyword2, ObjectIdentifier oid2, boolean rfc1779Compliant2, boolean rfc2253Compliant2) {
        this.keyword = keyword2;
        this.oid = oid2;
        this.rfc1779Compliant = rfc1779Compliant2;
        this.rfc2253Compliant = rfc2253Compliant2;
        oidMap.put(oid2, this);
        keywordMap.put(keyword2, this);
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

    static ObjectIdentifier getOID(String keyword2, int standard, Map<String, String> extraKeywordMap) throws IOException {
        String keyword3 = keyword2.toUpperCase(Locale.ENGLISH);
        if (standard != 3) {
            keyword3 = keyword3.trim();
        } else if (keyword3.startsWith(" ") || keyword3.endsWith(" ")) {
            throw new IOException("Invalid leading or trailing space in keyword \"" + keyword3 + "\"");
        }
        String oidString = extraKeywordMap.get(keyword3);
        if (oidString != null) {
            return new ObjectIdentifier(oidString);
        }
        AVAKeyword ak = keywordMap.get(keyword3);
        if (ak != null && ak.isCompliant(standard)) {
            return ak.oid;
        }
        if (standard == 1 && keyword3.startsWith("OID.")) {
            keyword3 = keyword3.substring(4);
        }
        boolean number = false;
        if (keyword3.length() != 0) {
            char ch = keyword3.charAt(0);
            if (ch >= '0' && ch <= '9') {
                number = true;
            }
        }
        if (number) {
            return new ObjectIdentifier(keyword3);
        }
        throw new IOException("Invalid keyword \"" + keyword3 + "\"");
    }

    static String getKeyword(ObjectIdentifier oid2, int standard) {
        return getKeyword(oid2, standard, Collections.emptyMap());
    }

    static String getKeyword(ObjectIdentifier oid2, int standard, Map<String, String> extraOidMap) {
        String oidString = oid2.toString();
        String keywordString = extraOidMap.get(oidString);
        if (keywordString == null) {
            AVAKeyword ak = oidMap.get(oid2);
            if (ak != null && ak.isCompliant(standard)) {
                return ak.keyword;
            }
            if (standard == 3) {
                return oidString;
            }
            return "OID." + oidString;
        } else if (keywordString.length() != 0) {
            String keywordString2 = keywordString.trim();
            char c = keywordString2.charAt(0);
            if (c < 'A' || c > 'z' || (c > 'Z' && c < 'a')) {
                throw new IllegalArgumentException("keyword does not start with letter");
            }
            for (int i = 1; i < keywordString2.length(); i++) {
                char c2 = keywordString2.charAt(i);
                if ((c2 < 'A' || c2 > 'z' || (c2 > 'Z' && c2 < 'a')) && ((c2 < '0' || c2 > '9') && c2 != '_')) {
                    throw new IllegalArgumentException("keyword character is not a letter, digit, or underscore");
                }
            }
            return keywordString2;
        } else {
            throw new IllegalArgumentException("keyword cannot be empty");
        }
    }

    static boolean hasKeyword(ObjectIdentifier oid2, int standard) {
        AVAKeyword ak = oidMap.get(oid2);
        if (ak == null) {
            return false;
        }
        return ak.isCompliant(standard);
    }

    static {
        new AVAKeyword("CN", X500Name.commonName_oid, true, true);
        new AVAKeyword("C", X500Name.countryName_oid, true, true);
        new AVAKeyword("L", X500Name.localityName_oid, true, true);
        new AVAKeyword("S", X500Name.stateName_oid, false, false);
        new AVAKeyword("ST", X500Name.stateName_oid, true, true);
        new AVAKeyword("O", X500Name.orgName_oid, true, true);
        new AVAKeyword("OU", X500Name.orgUnitName_oid, true, true);
        new AVAKeyword("T", X500Name.title_oid, false, false);
        new AVAKeyword("IP", X500Name.ipAddress_oid, false, false);
        new AVAKeyword("STREET", X500Name.streetAddress_oid, true, true);
        new AVAKeyword("DC", X500Name.DOMAIN_COMPONENT_OID, false, true);
        new AVAKeyword("DNQUALIFIER", X500Name.DNQUALIFIER_OID, false, false);
        new AVAKeyword("DNQ", X500Name.DNQUALIFIER_OID, false, false);
        new AVAKeyword("SURNAME", X500Name.SURNAME_OID, false, false);
        new AVAKeyword("GIVENNAME", X500Name.GIVENNAME_OID, false, false);
        new AVAKeyword("INITIALS", X500Name.INITIALS_OID, false, false);
        new AVAKeyword("GENERATION", X500Name.GENERATIONQUALIFIER_OID, false, false);
        new AVAKeyword("EMAIL", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new AVAKeyword("EMAILADDRESS", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new AVAKeyword("UID", X500Name.userid_oid, false, true);
        new AVAKeyword("SERIALNUMBER", X500Name.SERIALNUMBER_OID, false, false);
    }
}
