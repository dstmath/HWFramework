package gov.nist.javax.sip.clientauthutils;

import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestAlgorithm {
    private static final char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String calculateResponse(String algorithm, String hashUserNameRealmPasswd, String nonce_value, String nc_value, String cnonce_value, String method, String digest_uri_value, String entity_body, String qop_value, StackLogger stackLogger) {
        if (stackLogger.isLoggingEnabled()) {
            stackLogger.logDebug("trying to authenticate using : " + algorithm + ", " + hashUserNameRealmPasswd + ", " + nonce_value + ", " + nc_value + ", " + cnonce_value + ", " + method + ", " + digest_uri_value + ", " + entity_body + ", " + qop_value);
        }
        if (hashUserNameRealmPasswd == null || method == null || digest_uri_value == null || nonce_value == null) {
            throw new NullPointerException("Null parameter to MessageDigestAlgorithm.calculateResponse()");
        } else if (cnonce_value == null || cnonce_value.length() == 0) {
            throw new NullPointerException("cnonce_value may not be absent for MD5-Sess algorithm.");
        } else {
            String A2;
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

    /* JADX WARNING: Missing block: B:34:0x0125, code:
            if (r17.equalsIgnoreCase("auth-int") != false) goto L_0x0127;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static String calculateResponse(String algorithm, String username_value, String realm_value, String passwd, String nonce_value, String nc_value, String cnonce_value, String method, String digest_uri_value, String entity_body, String qop_value, StackLogger stackLogger) {
        if (stackLogger.isLoggingEnabled()) {
            StringBuilder append = new StringBuilder().append("trying to authenticate using : ").append(algorithm).append(", ").append(username_value).append(", ").append(realm_value).append(", ");
            boolean z = passwd != null && passwd.trim().length() > 0;
            stackLogger.logDebug(append.append(z).append(", ").append(nonce_value).append(", ").append(nc_value).append(", ").append(cnonce_value).append(", ").append(method).append(", ").append(digest_uri_value).append(", ").append(entity_body).append(", ").append(qop_value).toString());
        }
        if (username_value == null || realm_value == null || passwd == null || method == null || digest_uri_value == null || nonce_value == null) {
            throw new NullPointerException("Null parameter to MessageDigestAlgorithm.calculateResponse()");
        }
        String A1;
        String A2;
        if (algorithm == null || algorithm.trim().length() == 0 || algorithm.trim().equalsIgnoreCase("MD5")) {
            A1 = username_value + Separators.COLON + realm_value + Separators.COLON + passwd;
        } else if (cnonce_value == null || cnonce_value.length() == 0) {
            throw new NullPointerException("cnonce_value may not be absent for MD5-Sess algorithm.");
        } else {
            A1 = H(username_value + Separators.COLON + realm_value + Separators.COLON + passwd) + Separators.COLON + nonce_value + Separators.COLON + cnonce_value;
        }
        if (qop_value == null || qop_value.trim().length() == 0 || qop_value.trim().equalsIgnoreCase("auth")) {
            A2 = method + Separators.COLON + digest_uri_value;
        } else {
            if (entity_body == null) {
                entity_body = "";
            }
            A2 = method + Separators.COLON + digest_uri_value + Separators.COLON + H(entity_body);
        }
        if (!(cnonce_value == null || qop_value == null || nc_value == null)) {
            if (!qop_value.equalsIgnoreCase("auth")) {
            }
            return KD(H(A1), nonce_value + Separators.COLON + nc_value + Separators.COLON + cnonce_value + Separators.COLON + qop_value + Separators.COLON + H(A2));
        }
        return KD(H(A1), nonce_value + Separators.COLON + H(A2));
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
            int i2 = pos + 1;
            c[pos] = toHex[(b[i] >> 4) & 15];
            pos = i2 + 1;
            c[i2] = toHex[b[i] & 15];
        }
        return new String(c);
    }
}
