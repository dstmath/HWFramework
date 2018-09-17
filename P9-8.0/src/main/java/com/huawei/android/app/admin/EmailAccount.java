package com.huawei.android.app.admin;

import android.text.TextUtils;

public class EmailAccount {
    private static final String IMAP = "imap";
    private static final String POP = "pop3";
    public String mEmailAddress;
    public String mInComingPathPrefix;
    public String mInComingProtocol;
    public boolean mInComingServerAcceptAllCertificates = true;
    public String mInComingServerAddress;
    public String mInComingServerLogin;
    public String mInComingServerPassword;
    public int mInComingServerPort;
    public boolean mInComingServerUseSSL = true;
    public boolean mInComingServerUseTLS;
    public boolean mIsDefault;
    public String mOutGoingPathPrefix;
    public String mOutGoingProtocol;
    public boolean mOutGoingServerAcceptAllCertificates = true;
    public String mOutGoingServerAddress;
    public String mOutGoingServerLogin;
    public String mOutGoingServerPassword;
    public int mOutGoingServerPort;
    public boolean mOutGoingServerUseSSL = true;
    public boolean mOutGoingServerUseTLS;
    public String mSenderName;
    public String mSignature;

    public EmailAccount(String emailAddress, String inComingProtocol, String inComingServerAddress, int inComingServerPort, String inComingServerLogin, String inComingServerPassword, String outGoingProtocol, String outGoingServerAddress, int outGoingServerPort, String outGoingServerLogin, String outGoingServerPassword) {
        if (!isAddressValid(emailAddress) || (isInComingProtocolValid(inComingProtocol) ^ 1) != 0 || TextUtils.isEmpty(inComingServerAddress) || inComingServerPort < 0 || TextUtils.isEmpty(inComingServerLogin) || TextUtils.isEmpty(outGoingProtocol) || outGoingServerPort < 0) {
            throw new IllegalArgumentException("Invalid Parameters!");
        }
        this.mEmailAddress = emailAddress;
        this.mInComingProtocol = inComingProtocol;
        this.mInComingServerAddress = inComingServerAddress;
        this.mInComingServerPort = inComingServerPort;
        this.mInComingServerLogin = inComingServerLogin;
        this.mInComingServerPassword = inComingServerPassword;
        this.mOutGoingProtocol = outGoingProtocol;
        this.mOutGoingServerAddress = outGoingServerAddress;
        this.mOutGoingServerLogin = outGoingServerLogin;
        this.mOutGoingServerPort = outGoingServerPort;
        this.mOutGoingServerPassword = outGoingServerPassword;
    }

    public EmailAccount(String emailAddress, String inComingProtocol, String inComingServerAddress, int inComingServerPort, String inComingServerLogin, String inComingServerPassword, boolean inComingServerUseSSL, boolean inComingServerUseTLS, boolean inComingServerAcceptAllCertificates, String inComingPathPrefix, String outGoingProtocol, String outGoingServerAddress, int outGoingServerPort, String outGoingServerLogin, String outGoingServerPassword, boolean outGoingServerUseSSL, boolean outGoingServerUseTLS, boolean outGoingServerAcceptAllCertificates, String outGoingPathPrefix, String senderName, String signature, boolean isDefault) {
        if (!isAddressValid(emailAddress) || (isInComingProtocolValid(inComingProtocol) ^ 1) != 0 || TextUtils.isEmpty(inComingServerAddress) || inComingServerPort < 0 || TextUtils.isEmpty(inComingServerLogin) || TextUtils.isEmpty(outGoingProtocol) || outGoingServerPort < 0) {
            throw new IllegalArgumentException("Invalid Parameters!");
        }
        this.mEmailAddress = emailAddress;
        this.mInComingProtocol = inComingProtocol;
        this.mInComingServerAddress = inComingServerAddress;
        this.mInComingServerPort = inComingServerPort;
        this.mInComingServerLogin = inComingServerLogin;
        this.mInComingServerPassword = inComingServerPassword;
        this.mInComingServerAcceptAllCertificates = inComingServerAcceptAllCertificates;
        this.mInComingServerUseSSL = inComingServerUseSSL;
        this.mInComingServerUseTLS = inComingServerUseTLS;
        this.mInComingPathPrefix = inComingPathPrefix;
        this.mOutGoingProtocol = outGoingProtocol;
        this.mOutGoingServerAddress = outGoingServerAddress;
        this.mOutGoingServerLogin = outGoingServerLogin;
        this.mOutGoingServerPort = outGoingServerPort;
        this.mOutGoingServerPassword = outGoingServerPassword;
        this.mOutGoingServerAcceptAllCertificates = outGoingServerAcceptAllCertificates;
        this.mOutGoingServerUseSSL = outGoingServerUseSSL;
        this.mOutGoingServerUseTLS = outGoingServerUseTLS;
        this.mOutGoingPathPrefix = outGoingPathPrefix;
        this.mSenderName = senderName;
        this.mSignature = signature;
        this.mIsDefault = isDefault;
    }

    private boolean isAddressValid(String emailAddress) {
        if (emailAddress == null || emailAddress.indexOf("@") == -1) {
            return false;
        }
        return true;
    }

    private boolean isInComingProtocolValid(String inComingProtocol) {
        if (inComingProtocol == null) {
            return false;
        }
        String protocol = inComingProtocol.toLowerCase();
        if (protocol.equals(POP) || protocol.equals(IMAP)) {
            return true;
        }
        return false;
    }
}
