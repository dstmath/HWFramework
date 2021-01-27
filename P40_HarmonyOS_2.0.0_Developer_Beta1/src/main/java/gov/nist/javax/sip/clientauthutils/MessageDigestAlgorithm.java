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
            if (qop_value == null || nc_value == null || (!qop_value.equalsIgnoreCase("auth") && !qop_value.equalsIgnoreCase("auth-int"))) {
                return KD(hashUserNameRealmPasswd, nonce_value + Separators.COLON + H(A2));
            }
            return KD(hashUserNameRealmPasswd, nonce_value + Separators.COLON + nc_value + Separators.COLON + cnonce_value + Separators.COLON + qop_value + Separators.COLON + H(A2));
        }
    }

    static String calculateResponse(String algorithm, String username_value, String realm_value, String passwd, String nonce_value, String nc_value, String cnonce_value, String method, String digest_uri_value, String entity_body, String qop_value, StackLogger stackLogger) {
        String A1;
        String A2;
        String entity_body2 = entity_body;
        if (stackLogger.isLoggingEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("trying to authenticate using : ");
            sb.append(algorithm);
            sb.append(", ");
            sb.append(username_value);
            sb.append(", ");
            sb.append(realm_value);
            sb.append(", ");
            sb.append(passwd != null && passwd.trim().length() > 0);
            sb.append(", ");
            sb.append(nonce_value);
            sb.append(", ");
            sb.append(nc_value);
            sb.append(", ");
            sb.append(cnonce_value);
            sb.append(", ");
            sb.append(method);
            sb.append(", ");
            sb.append(digest_uri_value);
            sb.append(", ");
            sb.append(entity_body2);
            sb.append(", ");
            sb.append(qop_value);
            stackLogger.logDebug(sb.toString());
        }
        if (username_value == null || realm_value == null || passwd == null || method == null || digest_uri_value == null || nonce_value == null) {
            throw new NullPointerException("Null parameter to MessageDigestAlgorithm.calculateResponse()");
        }
        if (algorithm == null || algorithm.trim().length() == 0 || algorithm.trim().equalsIgnoreCase("MD5")) {
            A1 = username_value + Separators.COLON + realm_value + Separators.COLON + passwd;
        } else if (cnonce_value == null || cnonce_value.length() == 0) {
            throw new NullPointerException("cnonce_value may not be absent for MD5-Sess algorithm.");
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(H(username_value + Separators.COLON + realm_value + Separators.COLON + passwd));
            sb2.append(Separators.COLON);
            sb2.append(nonce_value);
            sb2.append(Separators.COLON);
            sb2.append(cnonce_value);
            A1 = sb2.toString();
        }
        if (qop_value == null || qop_value.trim().length() == 0 || qop_value.trim().equalsIgnoreCase("auth")) {
            A2 = method + Separators.COLON + digest_uri_value;
        } else {
            if (entity_body2 == null) {
                entity_body2 = "";
            }
            A2 = method + Separators.COLON + digest_uri_value + Separators.COLON + H(entity_body2);
        }
        if (cnonce_value == null || qop_value == null || nc_value == null || (!qop_value.equalsIgnoreCase("auth") && !qop_value.equalsIgnoreCase("auth-int"))) {
            return KD(H(A1), nonce_value + Separators.COLON + H(A2));
        }
        return KD(H(A1), nonce_value + Separators.COLON + nc_value + Separators.COLON + cnonce_value + Separators.COLON + qop_value + Separators.COLON + H(A2));
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
            char[] cArr = toHex;
            c[pos] = cArr[(b[i] >> 4) & 15];
            pos = pos2 + 1;
            c[pos2] = cArr[b[i] & 15];
        }
        return new String(c);
    }
}
