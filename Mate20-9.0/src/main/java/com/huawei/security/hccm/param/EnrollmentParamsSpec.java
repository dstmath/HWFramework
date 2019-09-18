package com.huawei.security.hccm.param;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bouncycastle.asn1.x500.X500Name;
import org.json.JSONException;
import org.json.JSONObject;

public final class EnrollmentParamsSpec {
    public static final String CSR_PKCS10 = "PKCS10";
    private static final String DEFUALT_CONNECTION_SETTINGS = "{\n\"cmp_headers\": {\n\"Method\": \"POST\",\n\"Content-Type\": \"application/pkixcmp\",\n\"Content-Language\": \"en-US\",\n\"Connection\": \"close\",\n\"DoInput\": true,\n\"DoOutput\": true,\n\"ConnectTimeout\": 15000,\n\"ReadTimeout\": 15000\n},\n\"user_settings\": {\n\"Accept-Encoding\": \"UTF-8\"\n}\n}";
    public static final String ENROLLMENT_PROTOCOL_CMC = "ENROLLMENT_PROTOCOL_CMC";
    public static final String ENROLLMENT_PROTOCOL_CMP = "ENROLLMENT_PROTOCOL_CMP";
    public static final String ENROLLMENT_PROTOCOL_EST = "ENROLLMENT_PROTOCOL_EST";
    public static final String ENROLLMENT_PROTOCOL_SCEP = "ENROLLMENT_PROTOCOL_SCEP";
    public static final int MAX_ALIAS_LEN = 89;
    public static final int MIN_ALIAS_LEN = 1;
    private static final String TAG = "EnrollmentParamsSpec";
    private String mAlias;
    private boolean mAllowImplicitTrustAnchors;
    private CredentialSpec mAuthCredentialSpec;
    private List<Certificate> mClientCertificateChain;
    private JSONObject mConnectionSettings;
    private X500Name mEnrollmentCertSubject;
    private String mEnrollmentProtocol;
    private URL mEnrollmentURL;
    private Set<Certificate> mExplicitTrustAnchors;
    private Set<byte[]> mPreferredExtensions;
    private ProtocolParam mProtocolParam;
    private String mSigAlgPadding;
    private CredentialSpec mUserCredentialSpec;

    public static final class Builder {
        private static final String TAG = "EnrollmentParamsSpec.Builder";
        private String mAlias = null;
        private boolean mAllowImplicitTrustAnchors = false;
        private CredentialSpec mAuthCredentialSpec = null;
        private List<Certificate> mClientCertificateChain = null;
        private JSONObject mConnectionSettings = null;
        private X500Name mEnrollmentCertSubject = null;
        private String mEnrollmentProtocol = null;
        private URL mEnrollmentURL = null;
        private Set<Certificate> mExplicitTrustAnchors = null;
        private Set<byte[]> mPreferredExtensions = null;
        private ProtocolParam mProtocolParam = null;
        private String mSigAlgPadding = null;
        private CredentialSpec mUserCredentialSpec = null;
        String msg;

        public Builder(@NonNull String alias) {
            if (alias == null) {
                throw new NullPointerException("alias == null");
            } else if (alias.length() < 1 || alias.length() > 89) {
                this.msg = "alias length out of the boundary";
                throw new IllegalArgumentException(this.msg);
            } else {
                this.mAlias = alias;
            }
        }

        public Builder(@NonNull String alias, @NonNull X500Name subject, @NonNull URL url, @NonNull String enrollmentProtocol, @NonNull ProtocolParam protocolParam) {
            if (alias == null) {
                throw new NullPointerException("alias == null");
            } else if (alias.length() < 1 || alias.length() > 89) {
                this.msg = "alias length out of the boundary";
                throw new IllegalArgumentException(this.msg);
            } else if (url == null) {
                throw new NullPointerException("enrollmentURL == null");
            } else if (enrollmentProtocol == null) {
                throw new NullPointerException("enrollmentProtocol == null");
            } else if (subject == null) {
                throw new NullPointerException("subject == null");
            } else if (protocolParam != null) {
                this.mAlias = alias;
                this.mEnrollmentCertSubject = subject;
                this.mEnrollmentURL = url;
                this.mEnrollmentProtocol = enrollmentProtocol;
                this.mProtocolParam = protocolParam;
            } else {
                throw new NullPointerException("protocolParam == null");
            }
        }

        public Builder setSigAlgPadding(String padding) {
            this.mSigAlgPadding = padding;
            return this;
        }

        public Builder setAllowImplicitTrustAnchors(boolean allow) {
            this.mAllowImplicitTrustAnchors = allow;
            return this;
        }

        public Builder setExplicitTrustAnchors(@NonNull Set<Certificate> set) {
            if (set != null) {
                this.mExplicitTrustAnchors = new HashSet(set);
                return this;
            }
            throw new NullPointerException("explicittrustanchors == null");
        }

        public Builder setClientCertificateChain(@NonNull List<Certificate> list) {
            if (list != null) {
                this.mClientCertificateChain = new LinkedList(list);
                return this;
            }
            throw new NullPointerException("clientcertificatechain == null");
        }

        public Builder setUserCredential(@NonNull CredentialSpec credentialSpec) {
            if (credentialSpec != null) {
                this.mUserCredentialSpec = credentialSpec;
                return this;
            }
            throw new NullPointerException("usercredentialSpec == null");
        }

        public Builder setAuthCredential(@NonNull CredentialSpec credentialSpec) {
            if (credentialSpec != null) {
                this.mAuthCredentialSpec = credentialSpec;
                return this;
            }
            throw new NullPointerException("authcredentialspec == null");
        }

        public Builder setPreferredExtensions(@NonNull Set<byte[]> extensions) {
            if (extensions != null) {
                this.mPreferredExtensions = new HashSet();
                this.mPreferredExtensions.addAll(extensions);
                return this;
            }
            throw new NullPointerException("preferredextensions == null");
        }

        public Builder setConnectionSettings(@NonNull JSONObject settings) {
            if (settings != null) {
                this.mConnectionSettings = settings;
                return this;
            }
            throw new NullPointerException("connectionsettings == null");
        }

        public EnrollmentParamsSpec build() {
            EnrollmentParamsSpec enrollmentParamsSpec = new EnrollmentParamsSpec(this.mAlias, this.mEnrollmentCertSubject, this.mEnrollmentURL, this.mConnectionSettings, this.mEnrollmentProtocol, this.mProtocolParam, this.mAllowImplicitTrustAnchors, this.mExplicitTrustAnchors, this.mClientCertificateChain, this.mUserCredentialSpec, this.mAuthCredentialSpec, this.mPreferredExtensions, this.mSigAlgPadding);
            return enrollmentParamsSpec;
        }
    }

    private EnrollmentParamsSpec(String alias, X500Name subject, URL enrollmentURL, JSONObject connectionSettings, String enrollmentProtocol, ProtocolParam protocolParam, boolean allowImplicitTrustAnchors, Set<Certificate> explicitTrustAnchors, List<Certificate> clientCertificateChain, CredentialSpec userCredentialSpec, CredentialSpec authCredentialSpec, Set<byte[]> extensions, String padding) {
        JSONObject jSONObject = connectionSettings;
        Set<Certificate> set = explicitTrustAnchors;
        List<Certificate> list = clientCertificateChain;
        Set<byte[]> set2 = extensions;
        this.mEnrollmentURL = null;
        this.mEnrollmentProtocol = null;
        this.mAllowImplicitTrustAnchors = false;
        this.mExplicitTrustAnchors = null;
        this.mClientCertificateChain = null;
        this.mUserCredentialSpec = null;
        this.mAuthCredentialSpec = null;
        this.mPreferredExtensions = null;
        this.mConnectionSettings = null;
        this.mEnrollmentCertSubject = null;
        this.mProtocolParam = null;
        this.mSigAlgPadding = null;
        if (!TextUtils.isEmpty(alias)) {
            if (jSONObject == null) {
                try {
                    this.mConnectionSettings = new JSONObject(DEFUALT_CONNECTION_SETTINGS);
                } catch (JSONException e) {
                    Log.e(TAG, "build the default connection settings failed");
                    throw new NullPointerException("connectionSettings == null");
                }
            }
            this.mAlias = alias;
            this.mEnrollmentURL = enrollmentURL;
            this.mEnrollmentProtocol = enrollmentProtocol;
            this.mAllowImplicitTrustAnchors = allowImplicitTrustAnchors;
            if (set != null) {
                this.mExplicitTrustAnchors = new HashSet(set);
            }
            if (list != null) {
                this.mClientCertificateChain = new LinkedList(list);
            }
            this.mUserCredentialSpec = userCredentialSpec;
            this.mAuthCredentialSpec = authCredentialSpec;
            if (set2 != null) {
                this.mPreferredExtensions = new HashSet(set2);
            }
            this.mConnectionSettings = jSONObject;
            this.mEnrollmentCertSubject = subject;
            this.mProtocolParam = protocolParam;
            this.mSigAlgPadding = padding;
            return;
        }
        String str = alias;
        X500Name x500Name = subject;
        URL url = enrollmentURL;
        String str2 = enrollmentProtocol;
        ProtocolParam protocolParam2 = protocolParam;
        boolean z = allowImplicitTrustAnchors;
        CredentialSpec credentialSpec = userCredentialSpec;
        CredentialSpec credentialSpec2 = authCredentialSpec;
        String str3 = padding;
        throw new IllegalArgumentException("enrollment cert alias must not be empty");
    }

    public String getSigAlgPadding() {
        return this.mSigAlgPadding;
    }

    public ProtocolParam getProtocolParam() {
        return this.mProtocolParam;
    }

    @NonNull
    public String getAlias() {
        return this.mAlias;
    }

    @NonNull
    public URL getEnrollmentURL() {
        return this.mEnrollmentURL;
    }

    @NonNull
    public String getEnrollmentProtocol() {
        return this.mEnrollmentProtocol;
    }

    public boolean areImplicitTrustAnchorsAllowed() {
        return this.mAllowImplicitTrustAnchors;
    }

    public Set<Certificate> getExplicitTrustAnchors() {
        if (this.mExplicitTrustAnchors != null) {
            return Collections.unmodifiableSet(this.mExplicitTrustAnchors);
        }
        return null;
    }

    public List<Certificate> getClientCertificateChain() {
        if (this.mExplicitTrustAnchors != null) {
            return Collections.unmodifiableList(this.mClientCertificateChain);
        }
        return null;
    }

    public CredentialSpec getUserCredential() {
        return this.mUserCredentialSpec;
    }

    public CredentialSpec getAuthCredential() {
        return this.mAuthCredentialSpec;
    }

    public Set<byte[]> getPreferredExtensions() {
        if (this.mPreferredExtensions != null) {
            return Collections.unmodifiableSet(this.mPreferredExtensions);
        }
        return null;
    }

    public JSONObject getConnectionSettings() {
        return this.mConnectionSettings;
    }

    public X500Name getEnrollmentCertSubject() {
        return this.mEnrollmentCertSubject;
    }
}
