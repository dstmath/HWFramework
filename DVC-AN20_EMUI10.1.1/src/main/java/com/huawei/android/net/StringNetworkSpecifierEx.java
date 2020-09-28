package com.huawei.android.net;

import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.text.TextUtils;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public final class StringNetworkSpecifierEx {
    private StringNetworkSpecifier mStringNetworkSpecifier;

    public StringNetworkSpecifierEx(String specifier) {
        this.mStringNetworkSpecifier = new StringNetworkSpecifier(specifier);
    }

    public StringNetworkSpecifierEx(NetworkSpecifier networkSpecifier) {
        this.mStringNetworkSpecifier = (StringNetworkSpecifier) networkSpecifier;
    }

    public StringNetworkSpecifier getStringNetworkSpecifier() {
        return this.mStringNetworkSpecifier;
    }

    public boolean isSpecifierEmpty() {
        StringNetworkSpecifier stringNetworkSpecifier = this.mStringNetworkSpecifier;
        return stringNetworkSpecifier == null || TextUtils.isEmpty(stringNetworkSpecifier.toString());
    }

    public String toString() {
        StringNetworkSpecifier stringNetworkSpecifier = this.mStringNetworkSpecifier;
        if (stringNetworkSpecifier == null) {
            return null;
        }
        return stringNetworkSpecifier.toString();
    }
}
