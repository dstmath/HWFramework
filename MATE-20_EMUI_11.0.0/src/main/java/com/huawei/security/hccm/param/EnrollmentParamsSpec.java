package com.huawei.security.hccm.param;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private static final String DEFAULT_CONNECTION_SETTINGS = ("{" + System.lineSeparator() + "\"cmp_headers\": {" + System.lineSeparator() + "\"Method\": \"POST\"," + System.lineSeparator() + "\"Content-Type\": \"application/pkixcmp\"," + System.lineSeparator() + "\"Content-Language\": \"en-US\"," + System.lineSeparator() + "\"Connection\": \"close\"," + System.lineSeparator() + "\"DoInput\": true," + System.lineSeparator() + "\"DoOutput\": true," + System.lineSeparator() + "\"ConnectTimeout\": 15000," + System.lineSeparator() + "\"ReadTimeout\": 15000" + System.lineSeparator() + "}," + System.lineSeparator() + "\"user_settings\": {" + System.lineSeparator() + "\"Accept-Encoding\": \"UTF-8\"" + System.lineSeparator() + "}" + System.lineSeparator() + "}");
    public static final String ENROLLMENT_PROTOCOL_CMC = "ENROLLMENT_PROTOCOL_CMC";
    public static final String ENROLLMENT_PROTOCOL_CMP = "ENROLLMENT_PROTOCOL_CMP";
    public static final String ENROLLMENT_PROTOCOL_EST = "ENROLLMENT_PROTOCOL_EST";
    public static final String ENROLLMENT_PROTOCOL_SCEP = "ENROLLMENT_PROTOCOL_SCEP";
    public static final int MAX_ALIAS_LEN = 89;
    public static final int MIN_ALIAS_LEN = 1;
    private static final String TAG = "EnrollmentParamsSpec";
    private String mAlias;
    private boolean mAllowImplicitTrustAnchors;
    private List<Certificate> mClientCertificateChain;
    private JSONObject mConnectionSettings;
    private X500Name mEnrollmentCertSubject;
    private String mEnrollmentProtocol;
    private URL mEnrollmentURL;
    private Set<Certificate> mExplicitTrustAnchors;
    private Set<byte[]> mPreferredExtensions;
    private ProtocolParam<?> mProtocolParam;
    private String mSigAlgPadding;

    private EnrollmentParamsSpec(Builder builder) {
        this.mEnrollmentURL = null;
        this.mEnrollmentProtocol = null;
        this.mAllowImplicitTrustAnchors = false;
        this.mExplicitTrustAnchors = null;
        this.mClientCertificateChain = null;
        this.mPreferredExtensions = null;
        this.mConnectionSettings = null;
        this.mEnrollmentCertSubject = null;
        this.mProtocolParam = null;
        this.mSigAlgPadding = null;
        checkAliasValidation(builder.mAlias);
        this.mAlias = builder.mAlias;
        if (builder.mConnectionSettings == null) {
            try {
                this.mConnectionSettings = new JSONObject(DEFAULT_CONNECTION_SETTINGS);
            } catch (JSONException e) {
                Log.e(TAG, "Build the default connection settings failed: " + e.getMessage());
                throw new IllegalArgumentException("Build default connection settings failed: " + e.getMessage());
            }
        } else {
            this.mConnectionSettings = builder.mConnectionSettings;
        }
        this.mEnrollmentURL = builder.mEnrollmentURL;
        this.mEnrollmentProtocol = builder.mEnrollmentProtocol;
        this.mProtocolParam = builder.mProtocolParam;
        this.mEnrollmentCertSubject = builder.mEnrollmentCertSubject;
        this.mAllowImplicitTrustAnchors = builder.mAllowImplicitTrustAnchors;
        this.mSigAlgPadding = builder.mSigAlgPadding;
        if (builder.mExplicitTrustAnchors != null) {
            this.mExplicitTrustAnchors = new HashSet(builder.mExplicitTrustAnchors);
        }
        if (builder.mClientCertificateChain != null) {
            this.mClientCertificateChain = new LinkedList(builder.mClientCertificateChain);
        }
        if (builder.mPreferredExtensions != null) {
            this.mPreferredExtensions = new HashSet(builder.mPreferredExtensions);
        }
    }

    /* access modifiers changed from: private */
    public static void checkAliasValidation(@Nullable String alias) {
        if (TextUtils.isEmpty(alias)) {
            throw new IllegalArgumentException("Alias must null or empty!");
        } else if (alias.length() < 1 || alias.length() > 89) {
            throw new IllegalArgumentException("Alias length out of the boundary!");
        }
    }

    public String getSigAlgPadding() {
        return this.mSigAlgPadding;
    }

    public ProtocolParam<?> getProtocolParam() {
        return this.mProtocolParam;
    }

    public String getAlias() {
        return this.mAlias;
    }

    public URL getEnrollmentURL() {
        return this.mEnrollmentURL;
    }

    public String getEnrollmentProtocol() {
        return this.mEnrollmentProtocol;
    }

    public boolean areImplicitTrustAnchorsAllowed() {
        return this.mAllowImplicitTrustAnchors;
    }

    public Set<Certificate> getExplicitTrustAnchors() {
        Set<Certificate> set = this.mExplicitTrustAnchors;
        if (set != null) {
            return Collections.unmodifiableSet(set);
        }
        return null;
    }

    public List<Certificate> getClientCertificateChain() {
        if (this.mExplicitTrustAnchors != null) {
            return Collections.unmodifiableList(this.mClientCertificateChain);
        }
        return null;
    }

    public Set<byte[]> getPreferredExtensions() {
        Set<byte[]> set = this.mPreferredExtensions;
        if (set != null) {
            return Collections.unmodifiableSet(set);
        }
        return null;
    }

    @NonNull
    public JSONObject getConnectionSettings() {
        return this.mConnectionSettings;
    }

    public X500Name getEnrollmentCertSubject() {
        return this.mEnrollmentCertSubject;
    }

    public boolean isCommonUsedParamsNull() {
        return getAlias() == null || getEnrollmentCertSubject() == null || getEnrollmentURL() == null || getEnrollmentProtocol() == null || getProtocolParam() == null;
    }

    public static final class Builder {
        private static final String TAG = "EnrollmentParamsSpec.Builder";
        private String mAlias = null;
        private boolean mAllowImplicitTrustAnchors = false;
        private List<Certificate> mClientCertificateChain = null;
        private JSONObject mConnectionSettings = null;
        private X500Name mEnrollmentCertSubject = null;
        private String mEnrollmentProtocol = null;
        private URL mEnrollmentURL = null;
        private Set<Certificate> mExplicitTrustAnchors = null;
        private Set<byte[]> mPreferredExtensions = null;
        private ProtocolParam<?> mProtocolParam = null;
        private String mSigAlgPadding = null;

        public Builder(String alias) {
            EnrollmentParamsSpec.checkAliasValidation(alias);
            this.mAlias = alias;
        }

        public Builder(String alias, X500Name subject, URL url, String enrollmentProtocol, ProtocolParam<?> protocolParam) {
            if (alias == null) {
                throw new IllegalArgumentException("Alias must not null");
            } else if (subject == null) {
                throw new IllegalArgumentException("Subject must not null");
            } else if (url == null) {
                throw new IllegalArgumentException("Url must not null");
            } else if (enrollmentProtocol == null) {
                throw new IllegalArgumentException("EnrollmentProtocol must not null");
            } else if (protocolParam != null) {
                this.mAlias = alias;
                this.mEnrollmentCertSubject = subject;
                this.mEnrollmentURL = url;
                this.mEnrollmentProtocol = enrollmentProtocol;
                this.mProtocolParam = protocolParam;
            } else {
                throw new IllegalArgumentException("ProtocolParam must not null");
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

        public Builder setExplicitTrustAnchors(Set<Certificate> set) {
            this.mExplicitTrustAnchors = set;
            return this;
        }

        public Builder setClientCertificateChain(List<Certificate> list) {
            this.mClientCertificateChain = list;
            return this;
        }

        public Builder setPreferredExtensions(Set<byte[]> extensions) {
            this.mPreferredExtensions = extensions;
            return this;
        }

        public Builder setConnectionSettings(JSONObject settings) {
            this.mConnectionSettings = settings;
            return this;
        }

        public EnrollmentParamsSpec build() {
            return new EnrollmentParamsSpec(this);
        }
    }
}
