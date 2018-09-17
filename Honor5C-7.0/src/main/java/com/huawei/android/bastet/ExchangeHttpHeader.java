package com.huawei.android.bastet;

public class ExchangeHttpHeader {
    private static String mEncoding;
    private static String mHostName;
    private static String mPolicyKey;
    private static String mUserAgent;
    private static String mVersion;

    public ExchangeHttpHeader(String version, String user, String encoding, String policy, String host) {
        mVersion = version;
        mUserAgent = user;
        mEncoding = encoding;
        mPolicyKey = policy;
        mHostName = host;
    }

    public String getVersion() {
        return mVersion;
    }

    public String getUserAgent() {
        return mUserAgent;
    }

    public String getEncoding() {
        return mEncoding;
    }

    public String getPolicyKey() {
        return mPolicyKey;
    }

    public String getHostName() {
        return mHostName;
    }
}
