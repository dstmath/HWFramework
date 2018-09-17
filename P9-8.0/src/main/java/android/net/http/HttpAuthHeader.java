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
        boolean z = true;
        if (this.mRealm != null) {
            if (this.mScheme == 1) {
                return true;
            }
            if (this.mScheme == 2) {
                if (!this.mAlgorithm.equals("md5")) {
                    z = false;
                } else if (this.mQop != null) {
                    z = this.mQop.equals("auth");
                }
                return z;
            }
        }
        return false;
    }

    private void parseHeader(String header) {
        if (header != null) {
            String parameters = parseScheme(header);
            if (parameters != null && this.mScheme != 0) {
                parseParameters(parameters);
            }
        }
    }

    private String parseScheme(String header) {
        if (header != null) {
            int i = header.indexOf(32);
            if (i >= 0) {
                String scheme = header.substring(0, i).trim();
                if (scheme.equalsIgnoreCase("Digest")) {
                    this.mScheme = 2;
                    this.mAlgorithm = "md5";
                } else if (scheme.equalsIgnoreCase("Basic")) {
                    this.mScheme = 1;
                }
                return header.substring(i + 1);
            }
        }
        return null;
    }

    private void parseParameters(String parameters) {
        if (parameters != null) {
            int i;
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
        if (parameter != null) {
            int i = parameter.indexOf(61);
            if (i >= 0) {
                String token = parameter.substring(0, i).trim();
                String value = trimDoubleQuotesIfAny(parameter.substring(i + 1).trim());
                if (token.equalsIgnoreCase(REALM_TOKEN)) {
                    this.mRealm = value;
                } else if (this.mScheme == 2) {
                    parseParameter(token, value);
                }
            }
        }
    }

    private void parseParameter(String token, String value) {
        if (!(token == null || value == null)) {
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
        if (value != null) {
            int len = value.length();
            if (len > 2 && value.charAt(0) == '\"' && value.charAt(len - 1) == '\"') {
                return value.substring(1, len - 1);
            }
        }
        return value;
    }
}
