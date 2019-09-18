package gov.nist.javax.sip.clientauthutils;

import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestAlgorithm {
    private static final char[] toHex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String calculateResponse(String algorithm, String hashUserNameRealmPasswd, String nonce_value, String nc_value, String cnonce_value, String method, String digest_uri_value, String entity_body, String qop_value, StackLogger stackLogger) {
        String A2;
        if (stackLogger.isLoggingEnabled()) {
            stackLogger.logDebug("trying to authenticate using : " + algorithm + ", " + hashUserNameRealmPasswd + ", " + nonce_value + ", " + nc_value + ", " + cnonce_value + ", " + method + ", " + digest_uri_value + ", " + entity_body + ", " + qop_value);
        }
        if (hashUserNameRealmPasswd == null || method == null || digest_uri_value == null || nonce_value == null) {
            throw new NullPointerException("Null parameter to MessageDigestAlgorithm.calculateResponse()");
        } else if (cnonce_value == null || cnonce_value.length() == 0) {
            throw new NullPointerException("cnonce_value may not be absent for MD5-Sess algorithm.");
        } else {
            if (qop_value == null || qop_value.trim().length() == 0 || qop_value.trim().equalsIgnoreCase("auth")) {
                A2 = method + Separators.COLON + digest_uri_value;
            } else {
                if (entity_body == null) {
                    entity_body = "";
                }
                A2 = method + Separators.COLON + digest_uri_value + Separators.COLON + H(entity_body);
            }
            if (cnonce_value == null || qop_value == null || nc_value == null || (!qop_value.equalsIgnoreCase("auth") && !qop_value.equalsIgnoreCase("auth-int"))) {
                return KD(hashUserNameRealmPasswd, nonce_value + Separators.COLON + H(A2));
            }
            return KD(hashUserNameRealmPasswd, nonce_value + Separators.COLON + nc_value + Separators.COLON + cnonce_value + Separators.COLON + qop_value + Separators.COLON + H(A2));
        }
    }

    static String calculateResponse(String algorithm, String username_value, String realm_value, String passwd, String nonce_value, String nc_value, String cnonce_value, String method, String digest_uri_value, String entity_body, String qop_value, StackLogger stackLogger) {
        String A1;
        String A2;
        String str = algorithm;
        String str2 = username_value;
        String str3 = realm_value;
        String str4 = passwd;
        String str5 = nonce_value;
        String str6 = nc_value;
        String str7 = cnonce_value;
        String str8 = method;
        String str9 = digest_uri_value;
        String entity_body2 = entity_body;
        String str10 = qop_value;
        if (stackLogger.isLoggingEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("trying to authenticate using : ");
            sb.append(str);
            sb.append(", ");
            sb.append(str2);
            sb.append(", ");
            sb.append(str3);
            sb.append(", ");
            sb.append(str4 != null && passwd.trim().length() > 0);
            sb.append(", ");
            sb.append(str5);
            sb.append(", ");
            sb.append(str6);
            sb.append(", ");
            sb.append(str7);
            sb.append(", ");
            sb.append(str8);
            sb.append(", ");
            sb.append(str9);
            sb.append(", ");
            sb.append(entity_body2);
            sb.append(", ");
            sb.append(str10);
            stackLogger.logDebug(sb.toString());
        } else {
            StackLogger stackLogger2 = stackLogger;
        }
        if (str2 == null || str3 == null || str4 == null || str8 == null || str9 == null || str5 == null) {
            throw new NullPointerException("Null parameter to MessageDigestAlgorithm.calculateResponse()");
        }
        if (str == null || algorithm.trim().length() == 0 || algorithm.trim().equalsIgnoreCase("MD5")) {
            A1 = str2 + Separators.COLON + str3 + Separators.COLON + str4;
        } else if (str7 == null || cnonce_value.length() == 0) {
            throw new NullPointerException("cnonce_value may not be absent for MD5-Sess algorithm.");
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(H(str2 + Separators.COLON + str3 + Separators.COLON + str4));
            sb2.append(Separators.COLON);
            sb2.append(str5);
            sb2.append(Separators.COLON);
            sb2.append(str7);
            A1 = sb2.toString();
        }
        if (str10 == null || qop_value.trim().length() == 0 || qop_value.trim().equalsIgnoreCase("auth")) {
            A2 = str8 + Separators.COLON + str9;
        } else {
            if (entity_body2 == null) {
                entity_body2 = "";
            }
            A2 = str8 + Separators.COLON + str9 + Separators.COLON + H(entity_body2);
        }
        if (str7 == null || str10 == null || str6 == null || (!str10.equalsIgnoreCase("auth") && !str10.equalsIgnoreCase("auth-int"))) {
            return KD(H(A1), str5 + Separators.COLON + H(A2));
        }
        return KD(H(A1), str5 + Separators.COLON + str6 + Separators.COLON + str7 + Separators.COLON + str10 + Separators.COLON + H(A2));
    }

    private static String H(String data) {
        try {
            return toHexString(MessageDigest.getInstance("MD5").digest(data.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Failed to instantiate an MD5 algorithm", ex);
        }
    }

    private static String KD(String secret, String data) {
        return H(secret + Separators.COLON + data);
    }

    private static String toHexString(byte[] b) {
        int pos = 0;
        char[] c = new char[(b.length * 2)];
        for (int i = 0; i < b.length; i++) {
            int pos2 = pos + 1;
            c[pos] = toHex[(b[i] >> 4) & 15];
            pos = pos2 + 1;
            c[pos2] = toHex[b[i] & 15];
        }
        return new String(c);
    }
}
