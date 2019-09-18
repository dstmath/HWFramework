package com.huawei.android.bastet;

public class ExchangeHttpHeader {
    private String mEncoding;
    private String mHostName;
    private String mPolicyKey;
    private String mUserAgent;
    private String mVersion;

    public ExchangeHttpHeader(String version, String user, String encoding, String policy, String host) {
        this.mVersion = version;
        this.mUserAgent = user;
        this.mEncoding = encoding;
        this.mPolicyKey = policy;
        this.mHostName = host;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public String getUserAgent() {
        return this.mUserAgent;
    }

    public String getEncoding() {
        return this.mEncoding;
    }

    public String getPolicyKey() {
        return this.mPolicyKey;
    }

    public String getHostName() {
        return this.mHostName;
    }
}
