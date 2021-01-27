package android.net.http;

import java.util.Locale;

public class HttpAuthHeader {
    private static final String ALGORITHM_TOKEN = "algorithm";
    public static final int BASIC = 1;
    public static final String BASIC_TOKEN = "Basic";
    public static final int DIGEST = 2;
    public static final String DIGEST_TOKEN = "Digest";
    private static final String NONCE_TOKEN = "nonce";
    private static final String OPAQUE_TOKEN = "opaque";
    private static final String QOP_TOKEN = "qop";
    private static final String REALM_TOKEN = "realm";
    private static final String STALE_TOKEN = "stale";
    public static final int UNKNOWN = 0;
    private String mAlgorithm;
    private boolean mIsProxy;
    private String mNonce;
    private String mOpaque;
    private String mPassword;
    private String mQop;
    private String mRealm;
    private int mScheme;
    private boolean mStale;
    private String mUsername;

    public HttpAuthHeader(String header) {
        if (header != null) {
            parseHeader(header);
        }
    }

    public boolean isProxy() {
        return this.mIsProxy;
    }

    public void setProxy() {
        this.mIsProxy = true;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public boolean isBasic() {
        return this.mScheme == 1;
    }

    public boolean isDigest() {
        return this.mScheme == 2;
    }

    public int getScheme() {
        return this.mScheme;
    }

    public boolean getStale() {
        return this.mStale;
    }

    public String getRealm() {
        return this.mRealm;
    }

    public String getNonce() {
        return this.mNonce;
    }

    public String getOpaque() {
        return this.mOpaque;
    }

    public String getQop() {
        return this.mQop;
    }

    public String getAlgorithm() {
        return this.mAlgorithm;
    }

    public boolean isSupportedScheme() {
        if (this.mRealm != null) {
            int i = this.mScheme;
            if (i == 1) {
                return true;
            }
            if (i != 2 || !this.mAlgorithm.equals("md5")) {
                return false;
            }
            String str = this.mQop;
            if (str == null || str.equals("auth")) {
                return true;
            }
            return false;
        }
        return false;
    }

    private void parseHeader(String header) {
        String parameters;
        if (header != null && (parameters = parseScheme(header)) != null && this.mScheme != 0) {
            parseParameters(parameters);
        }
    }

    private String parseScheme(String header) {
        int i;
        if (header == null || (i = header.indexOf(32)) < 0) {
            return null;
        }
        String scheme = header.substring(0, i).trim();
        if (scheme.equalsIgnoreCase("Digest")) {
            this.mScheme = 2;
            this.mAlgorithm = "md5";
        } else if (scheme.equalsIgnoreCase("Basic")) {
            this.mScheme = 1;
        }
        return header.substring(i + 1);
    }

    private void parseParameters(String parameters) {
        int i;
        if (parameters != null) {
            do {
                i = parameters.indexOf(44);
                if (i < 0) {
                    parseParameter(parameters);
                    continue;
                } else {
                    parseParameter(parameters.substring(0, i));
                    parameters = parameters.substring(i + 1);
                    continue;
                }
            } while (i >= 0);
        }
    }

    private void parseParameter(String parameter) {
        int i;
        if (parameter != null && (i = parameter.indexOf(61)) >= 0) {
            String token = parameter.substring(0, i).trim();
            String value = trimDoubleQuotesIfAny(parameter.substring(i + 1).trim());
            if (token.equalsIgnoreCase(REALM_TOKEN)) {
                this.mRealm = value;
            } else if (this.mScheme == 2) {
                parseParameter(token, value);
            }
        }
    }

    private void parseParameter(String token, String value) {
        if (token != null && value != null) {
            if (token.equalsIgnoreCase(NONCE_TOKEN)) {
                this.mNonce = value;
            } else if (token.equalsIgnoreCase(STALE_TOKEN)) {
                parseStale(value);
            } else if (token.equalsIgnoreCase(OPAQUE_TOKEN)) {
                this.mOpaque = value;
            } else if (token.equalsIgnoreCase(QOP_TOKEN)) {
                this.mQop = value.toLowerCase(Locale.ROOT);
            } else if (token.equalsIgnoreCase(ALGORITHM_TOKEN)) {
                this.mAlgorithm = value.toLowerCase(Locale.ROOT);
            }
        }
    }

    private void parseStale(String value) {
        if (value != null && value.equalsIgnoreCase("true")) {
            this.mStale = true;
        }
    }

    private static String trimDoubleQuotesIfAny(String value) {
        int len;
        if (value == null || (len = value.length()) <= 2 || value.charAt(0) != '\"' || value.charAt(len - 1) != '\"') {
            return value;
        }
        return value.substring(1, len - 1);
    }
}
