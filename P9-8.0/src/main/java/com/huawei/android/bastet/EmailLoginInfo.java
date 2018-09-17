package com.huawei.android.bastet;

import java.net.InetSocketAddress;

public class EmailLoginInfo {
    private String mAccount;
    private InetSocketAddress mAddress;
    private int mInterval;
    private String mPassword;
    private int mSecurity;

    public EmailLoginInfo(InetSocketAddress address, String account, String password, int security, int interval) {
        this.mAddress = address;
        this.mAccount = account;
        this.mPassword = password;
        this.mSecurity = security;
        this.mInterval = interval;
    }

    public String getHost() {
        return this.mAddress.getAddress().getHostAddress();
    }

    public int getPort() {
        return this.mAddress.getPort();
    }

    public String getAccount() {
        return this.mAccount;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public int getSecurity() {
        return this.mSecurity;
    }

    public int getInterval() {
        return this.mInterval;
    }
}
