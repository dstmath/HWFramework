package com.huawei.android.bastet;

import java.net.InetSocketAddress;

public class EmailLoginInfo {
    private static String mAccount;
    private static InetSocketAddress mAddress;
    private static int mInterval;
    private static String mPassword;
    private static int mSecurity;

    public EmailLoginInfo(InetSocketAddress address, String account, String password, int security, int interval) {
        mAddress = address;
        mAccount = account;
        mPassword = password;
        mSecurity = security;
        mInterval = interval;
    }

    public String getHost() {
        return mAddress.getAddress().getHostAddress();
    }

    public int getPort() {
        return mAddress.getPort();
    }

    public String getAccount() {
        return mAccount;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getSecurity() {
        return mSecurity;
    }

    public int getInterval() {
        return mInterval;
    }
}
