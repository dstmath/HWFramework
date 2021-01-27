package com.huawei.android.content.pm;

public class IExtServiceProviderEx {
    private IExtServiceProvider mExtServiceProvider;

    public IExtServiceProvider getExtServiceProvider() {
        return this.mExtServiceProvider;
    }

    public void setExtServiceProvider(IExtServiceProvider extServiceProvider) {
        this.mExtServiceProvider = extServiceProvider;
    }
}
