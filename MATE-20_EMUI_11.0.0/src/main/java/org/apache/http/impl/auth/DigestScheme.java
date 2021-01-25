package org.apache.http.impl.auth;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.message.BasicHeaderValueFormatter;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

@Deprecated
public class DigestScheme extends RFC2617Scheme {
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String NC = "00000001";
    private static final int QOP_AUTH = 2;
    private static final int QOP_AUTH_INT = 1;
    private static final int QOP_MISSING = 0;
    private String cnonce;
    private boolean complete = false;
    private int qopVariant = 0;

    @Override // org.apache.http.impl.auth.AuthSchemeBase, org.apache.http.auth.AuthScheme
    public void processChallenge(Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        if (getParameter("realm") == null) {
            throw new MalformedChallengeException("missing realm in challange");
        } else if (getParameter("nonce") != null) {
            boolean unsupportedQop = false;
            String qop = getParameter("qop");
            if (qop != null) {
                StringTokenizer tok = new StringTokenizer(qop, ",");
                while (true) {
                    if (!tok.hasMoreTokens()) {
                        break;
                    }
                    String variant = tok.nextToken().trim();
                    if (variant.equals("auth")) {
                        this.qopVariant = 2;
                        break;
                    } else if (variant.equals("auth-int")) {
                        this.qopVariant = 1;
                    } else {
                        unsupportedQop = true;
                    }
                }
            }
            if (!unsupportedQop || this.qopVariant != 0) {
                this.cnonce = null;
                this.complete = true;
                return;
            }
            throw new MalformedChallengeException("None of the qop methods is supported");
        } else {
            throw new MalformedChallengeException("missing nonce in challange");
        }
    }

    @Override // org.apache.http.auth.AuthScheme
    public boolean isComplete() {
        if ("true".equalsIgnoreCase(getParameter("stale"))) {
            return false;
        }
        return this.complete;
    }

    @Override // org.apache.http.auth.AuthScheme
    public String getSchemeName() {
        return "digest";
    }

    @Override // org.apache.http.auth.AuthScheme
    public boolean isConnectionBased() {
        return false;
    }

    public void overrideParamter(String name, String value) {
        getParameters().put(name, value);
    }

    private String getCnonce() {
        if (this.cnonce == null) {
            this.cnonce = createCnonce();
        }
        return this.cnonce;
    }

    @Override // org.apache.http.auth.AuthScheme
    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        } else if (request != null) {
            getParameters().put("methodname", request.getRequestLine().getMethod());
            getParameters().put("uri", request.getRequestLine().getUri());
            if (getParameter("charset") == null) {
                getParameters().put("charset", AuthParams.getCredentialCharset(request.getParams()));
            }
            return createDigestHeader(credentials, createDigest(credentials));
        } else {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
    }

    private static MessageDigest createMessageDigest(String digAlg) throws UnsupportedDigestAlgorithmException {
        try {
            return MessageDigest.getInstance(digAlg);
        } catch (Exception e) {
            throw new UnsupportedDigestAlgorithmException("Unsupported algorithm in HTTP Digest authentication: " + digAlg);
        }
    }

    private String createDigest(Credentials credentials) throws AuthenticationException {
        String qopOption;
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String method = getParameter("methodname");
        String algorithm = getParameter("algorithm");
        if (uri == null) {
            throw new IllegalStateException("URI may not be null");
        } else if (realm == null) {
            throw new IllegalStateException("Realm may not be null");
        } else if (nonce != null) {
            if (algorithm == null) {
                algorithm = "MD5";
            }
            String charset = getParameter("charset");
            if (charset == null) {
                charset = "ISO-8859-1";
            }
            if (this.qopVariant != 1) {
                MessageDigest md5Helper = createMessageDigest("MD5");
                String uname = credentials.getUserPrincipal().getName();
                String pwd = credentials.getPassword();
                StringBuilder tmp = new StringBuilder(uname.length() + realm.length() + pwd.length() + 2);
                tmp.append(uname);
                tmp.append(':');
                tmp.append(realm);
                tmp.append(':');
                tmp.append(pwd);
                String a1 = tmp.toString();
                if (algorithm.equalsIgnoreCase("MD5-sess")) {
                    String cnonce2 = getCnonce();
                    String tmp2 = encode(md5Helper.digest(EncodingUtils.getBytes(a1, charset)));
                    StringBuilder tmp3 = new StringBuilder(tmp2.length() + nonce.length() + cnonce2.length() + 2);
                    tmp3.append(tmp2);
                    tmp3.append(':');
                    tmp3.append(nonce);
                    tmp3.append(':');
                    tmp3.append(cnonce2);
                    a1 = tmp3.toString();
                } else if (!algorithm.equalsIgnoreCase("MD5")) {
                    throw new AuthenticationException("Unhandled algorithm " + algorithm + " requested");
                }
                String md5a1 = encode(md5Helper.digest(EncodingUtils.getBytes(a1, charset)));
                String a2 = null;
                if (this.qopVariant != 1) {
                    a2 = method + ':' + uri;
                }
                String md5a2 = encode(md5Helper.digest(EncodingUtils.getAsciiBytes(a2)));
                if (this.qopVariant == 0) {
                    StringBuilder tmp22 = new StringBuilder(md5a1.length() + nonce.length() + md5a2.length());
                    tmp22.append(md5a1);
                    tmp22.append(':');
                    tmp22.append(nonce);
                    tmp22.append(':');
                    tmp22.append(md5a2);
                    qopOption = tmp22.toString();
                } else {
                    String qopOption2 = getQopVariantString();
                    String cnonce3 = getCnonce();
                    StringBuilder tmp23 = new StringBuilder(md5a1.length() + nonce.length() + NC.length() + cnonce3.length() + qopOption2.length() + md5a2.length() + 5);
                    tmp23.append(md5a1);
                    tmp23.append(':');
                    tmp23.append(nonce);
                    tmp23.append(':');
                    tmp23.append(NC);
                    tmp23.append(':');
                    tmp23.append(cnonce3);
                    tmp23.append(':');
                    tmp23.append(qopOption2);
                    tmp23.append(':');
                    tmp23.append(md5a2);
                    qopOption = tmp23.toString();
                }
                return encode(md5Helper.digest(EncodingUtils.getAsciiBytes(qopOption)));
            }
            throw new AuthenticationException("Unsupported qop in HTTP Digest authentication");
        } else {
            throw new IllegalStateException("Nonce may not be null");
        }
    }

    private Header createDigestHeader(Credentials credentials, String digest) throws AuthenticationException {
        CharArrayBuffer buffer;
        CharArrayBuffer buffer2 = new CharArrayBuffer(128);
        if (isProxy()) {
            buffer2.append(AUTH.PROXY_AUTH_RESP);
        } else {
            buffer2.append(AUTH.WWW_AUTH_RESP);
        }
        buffer2.append(": Digest ");
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String opaque = getParameter("opaque");
        String algorithm = getParameter("algorithm");
        String uname = credentials.getUserPrincipal().getName();
        List<BasicNameValuePair> params = new ArrayList<>(20);
        CharArrayBuffer buffer3 = buffer2;
        params.add(new BasicNameValuePair("username", uname));
        params.add(new BasicNameValuePair("realm", realm));
        params.add(new BasicNameValuePair("nonce", nonce));
        params.add(new BasicNameValuePair("uri", uri));
        params.add(new BasicNameValuePair("response", digest));
        if (this.qopVariant != 0) {
            params.add(new BasicNameValuePair("qop", getQopVariantString()));
            params.add(new BasicNameValuePair("nc", NC));
            params.add(new BasicNameValuePair("cnonce", getCnonce()));
        }
        if (algorithm != null) {
            params.add(new BasicNameValuePair("algorithm", algorithm));
        }
        if (opaque != null) {
            params.add(new BasicNameValuePair("opaque", opaque));
        }
        int i = 0;
        while (i < params.size()) {
            BasicNameValuePair param = params.get(i);
            if (i > 0) {
                buffer = buffer3;
                buffer.append(", ");
            } else {
                buffer = buffer3;
            }
            BasicHeaderValueFormatter.DEFAULT.formatNameValuePair(buffer, param, !("nc".equals(param.getName()) || "qop".equals(param.getName())));
            i++;
            buffer3 = buffer;
        }
        return new BufferedHeader(buffer3);
    }

    private String getQopVariantString() {
        if (this.qopVariant == 1) {
            return "auth-int";
        }
        return "auth";
    }

    private static String encode(byte[] binaryData) {
        if (binaryData.length != 16) {
            return null;
        }
        char[] buffer = new char[32];
        for (int i = 0; i < 16; i++) {
            char[] cArr = HEXADECIMAL;
            buffer[i * 2] = cArr[(binaryData[i] & 240) >> 4];
            buffer[(i * 2) + 1] = cArr[binaryData[i] & 15];
        }
        return new String(buffer);
    }

    public static String createCnonce() {
        return encode(createMessageDigest("MD5").digest(EncodingUtils.getAsciiBytes(Long.toString(System.currentTimeMillis()))));
    }
}
