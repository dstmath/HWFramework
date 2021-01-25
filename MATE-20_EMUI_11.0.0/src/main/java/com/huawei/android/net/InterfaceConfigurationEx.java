package com.huawei.android.net;

import android.net.InterfaceConfiguration;
import android.net.LinkAddress;

public class InterfaceConfigurationEx {
    private InterfaceConfiguration mInterfaceConfiguration;

    public InterfaceConfigurationEx(InterfaceConfiguration interfaceConfiguration) {
        this.mInterfaceConfiguration = interfaceConfiguration;
    }

    public void setInterfaceDown() {
        this.mInterfaceConfiguration.setInterfaceDown();
    }

    public void clearFlag(String flag) {
        this.mInterfaceConfiguration.clearFlag(flag);
    }

    public void setInterfaceUp() {
        this.mInterfaceConfiguration.setInterfaceUp();
    }

    public void setLinkAddress(LinkAddress serverAddress) {
        this.mInterfaceConfiguration.setLinkAddress(serverAddress);
    }

    public InterfaceConfiguration getInterfaceConfiguration() {
        return this.mInterfaceConfiguration;
    }
}
