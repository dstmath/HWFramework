package com.hwawei.application.hwpay;

import android.support.annotation.NonNull;

public class TokenInfo {
    public static final String TOKEN_TYPE_ACCESS_TOKEN = "AccessToken";
    private String mCredential;
    private String mType;

    public TokenInfo(@NonNull String type, @NonNull String hash) {
        this.mType = type;
        this.mCredential = hash;
    }

    public String getType() {
        return this.mType;
    }

    public String getCredential() {
        return this.mCredential;
    }

    public String getToken() {
        return this.mType + " " + this.mCredential;
    }
}
