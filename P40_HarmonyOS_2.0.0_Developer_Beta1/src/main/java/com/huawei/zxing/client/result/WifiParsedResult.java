package com.huawei.zxing.client.result;

public final class WifiParsedResult extends ParsedResult {
    private final boolean hidden;
    private final String networkEncryption;
    private final String password;
    private final String ssid;

    public WifiParsedResult(String networkEncryption2, String ssid2, String password2) {
        this(networkEncryption2, ssid2, password2, false);
    }

    public WifiParsedResult(String networkEncryption2, String ssid2, String password2, boolean hidden2) {
        super(ParsedResultType.WIFI);
        this.ssid = ssid2;
        this.networkEncryption = networkEncryption2;
        this.password = password2;
        this.hidden = hidden2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public String getNetworkEncryption() {
        return this.networkEncryption;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    @Override // com.huawei.zxing.client.result.ParsedResult
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(80);
        maybeAppend(this.ssid, result);
        maybeAppend(this.networkEncryption, result);
        maybeAppend(this.password, result);
        maybeAppend(Boolean.toString(this.hidden), result);
        return result.toString();
    }
}
