package com.huawei.android.app.admin;

import android.text.TextUtils;
import java.util.Locale;

public class EmailAccount {
    private static final String IMAP = "imap";
    private static final String POP = "pop3";
    public String mEmailAddress;
    public String mInComingPathPrefix;
    public String mInComingProtocol;
    public boolean mInComingServerAcceptAllCertificates;
    public String mInComingServerAddress;
    public String mInComingServerLogin;
    public String mInComingServerPassword;
    public int mInComingServerPort;
    public boolean mInComingServerUseSSL;
    public boolean mInComingServerUseTLS;
    public boolean mIsDefault;
    public String mOutGoingPathPrefix;
    public String mOutGoingProtocol;
    public boolean mOutGoingServerAcceptAllCertificates;
    public String mOutGoingServerAddress;
    public String mOutGoingServerLogin;
    public String mOutGoingServerPassword;
    public int mOutGoingServerPort;
    public boolean mOutGoingServerUseSSL;
    public boolean mOutGoingServerUseTLS;
    public String mSenderName;
    public String mSignature;

    public EmailAccount() {
        this.mInComingServerAcceptAllCertificates = true;
        this.mInComingServerUseSSL = true;
        this.mOutGoingServerAcceptAllCertificates = true;
        this.mOutGoingServerUseSSL = true;
    }

    public EmailAccount(String emailAddress, String inComingProtocol, String inComingServerAddress, int inComingServerPort, String inComingServerLogin, String inComingServerPassword, String outGoingProtocol, String outGoingServerAddress, int outGoingServerPort, String outGoingServerLogin, String outGoingServerPassword) {
        this(emailAddress, inComingProtocol, inComingServerAddress, inComingServerPort, inComingServerLogin, inComingServerPassword, true, false, true, null, outGoingProtocol, outGoingServerAddress, outGoingServerPort, outGoingServerLogin, outGoingServerPassword, true, false, true, null, null, null, false);
    }

    public EmailAccount(String emailAddress, String inComingProtocol, String inComingServerAddress, int inComingServerPort, String inComingServerLogin, String inComingServerPassword, boolean isInComingServerUseSsl, boolean isInComingServerUseTls, boolean isInComingServerAcceptAllCertificates, String inComingPathPrefix, String outGoingProtocol, String outGoingServerAddress, int outGoingServerPort, String outGoingServerLogin, String outGoingServerPassword, boolean isOutGoingServerUseSsl, boolean isOutGoingServerUseTls, boolean isOutGoingServerAcceptAllCertificates, String outGoingPathPrefix, String senderName, String signature, boolean isDefault) {
        this.mInComingServerAcceptAllCertificates = true;
        this.mInComingServerUseSSL = true;
        this.mOutGoingServerAcceptAllCertificates = true;
        this.mOutGoingServerUseSSL = true;
        if (!isAddressValid(emailAddress) || !isInComingProtocolValid(inComingProtocol)) {
            throw new IllegalArgumentException("Invalid address or protocol Parameters!");
        } else if (!isPortValid(inComingServerPort) || !isPortValid(outGoingServerPort)) {
            throw new IllegalArgumentException("Invalid port Parameters!");
        } else {
            if (isConfigValid(inComingServerAddress) && isConfigValid(inComingServerLogin)) {
                if (isConfigValid(outGoingProtocol)) {
                    this.mEmailAddress = emailAddress;
                    this.mInComingProtocol = inComingProtocol;
                    this.mInComingServerAddress = inComingServerAddress;
                    this.mInComingServerPort = inComingServerPort;
                    this.mInComingServerLogin = inComingServerLogin;
                    this.mInComingServerPassword = inComingServerPassword;
                    this.mInComingServerAcceptAllCertificates = isInComingServerAcceptAllCertificates;
                    this.mInComingServerUseSSL = isInComingServerUseSsl;
                    this.mInComingServerUseTLS = isInComingServerUseTls;
                    this.mInComingPathPrefix = inComingPathPrefix;
                    this.mOutGoingProtocol = outGoingProtocol;
                    this.mOutGoingServerAddress = outGoingServerAddress;
                    this.mOutGoingServerLogin = outGoingServerLogin;
                    this.mOutGoingServerPort = outGoingServerPort;
                    this.mOutGoingServerPassword = outGoingServerPassword;
                    this.mOutGoingServerAcceptAllCertificates = isOutGoingServerAcceptAllCertificates;
                    this.mOutGoingServerUseSSL = isOutGoingServerUseSsl;
                    this.mOutGoingServerUseTLS = isOutGoingServerUseTls;
                    this.mOutGoingPathPrefix = outGoingPathPrefix;
                    this.mSenderName = senderName;
                    this.mSignature = signature;
                    this.mIsDefault = isDefault;
                    return;
                }
            }
            throw new IllegalArgumentException("Invalid config Parameters!");
        }
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
        String protocol = inComingProtocol.toLowerCase(Locale.ROOT);
        if (protocol.equals(POP) || protocol.equals(IMAP)) {
            return true;
        }
        return false;
    }

    private boolean isPortValid(int port) {
        if (port < 0) {
            return false;
        }
        return true;
    }

    private boolean isConfigValid(String config) {
        if (TextUtils.isEmpty(config)) {
            return false;
        }
        return true;
    }
}
