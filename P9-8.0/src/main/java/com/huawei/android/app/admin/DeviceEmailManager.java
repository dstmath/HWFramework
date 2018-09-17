package com.huawei.android.app.admin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.internal.telephony.PhoneConstantsEx;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Locale;

public class DeviceEmailManager {
    private static final String ALLOWING_ADDITION_BLACK_LIST = "allowing-addition-black-list";
    private static final String ALLOWING_DELETION_BLACK_LIST = "allowing-deletion-black-list";
    private static final String DISABLING_ADDITION_WHITE_LIST = "disabling-addition-white-list";
    private static final String DISABLING_DELETION_WHITE_LIST = "disabling-deletion-white-list";
    private static final String EMAIL_ACCOUNT_ADDRESS = "email_account_address";
    private static final String EMAIL_ACCOUNT_CONFIG = "email_account_config";
    private static final String EMAIL_ACCOUNT_INCOMING_PATH_PREFIX = "email_account_incoming_path_prefix";
    private static final String EMAIL_ACCOUNT_INCOMING_PROTOCOL = "email_account_incoming_protocol";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_ACCEPT_ALL_CERTIFICATES = "email_account_incoming_server_accept_all_certificates";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_ADDRESS = "email_account_incoming_server_address";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_LOGIN = "email_account_incoming_server_login";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_PASSWORD = "email_account_incoming_server_password";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_PORT = "email_account_incoming_server_port";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_USE_SSL = "email_account_incoming_server_use_ssl";
    private static final String EMAIL_ACCOUNT_INCOMING_SERVER_USE_TLS = "email_account_incoming_server_use_tls";
    private static final String EMAIL_ACCOUNT_IS_DEFAULT = "email_account_is_default";
    private static final String EMAIL_ACCOUNT_OUTGOING_PATH_PREFIX = "email_account_outgoing_path_prefix";
    private static final String EMAIL_ACCOUNT_OUTGOING_PROTOCOL = "email_account_outgoing_protocol";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_ACCEPT_ALL_CERTIFICATES = "email_account_outgoing_server_accept_all_certificates";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_ADDRESS = "email_account_outgoing_server_address";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_LOGIN = "email_account_outgoing_server_login";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_PASSWORD = "email_account_outgoing_server_password";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_PORT = "email_account_outgoing_server_port";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_USE_SSL = "email_account_outgoing_server_use_ssl";
    private static final String EMAIL_ACCOUNT_OUTGOING_SERVER_USE_TLS = "email_account_outgoing_server_use_tls";
    private static final String EMAIL_ACCOUNT_SENDER_NAME = "email_account_sender_name";
    private static final String EMAIL_ACCOUNT_SIGNATURE = "email_account_signature";
    private static final String EMAIL_DISABLE_FORWARDING = "email-disable-forwarding";
    private static final String EXCHANGE_ACCOUNT_ACCEPT_ALL_CERTIFICATES = "exchange_account_accept_all_certificates";
    private static final String EXCHANGE_ACCOUNT_CONFIG = "exchange_account_config";
    private static final String EXCHANGE_ACCOUNT_DISPLAY_HTML_DISABLED = "exchange_account_display_html_disabled";
    private static final String EXCHANGE_ACCOUNT_DISPLAY_NAME = "exchange_account_display_name";
    private static final String EXCHANGE_ACCOUNT_DOMAIN = "exchange_account_domain";
    private static final String EXCHANGE_ACCOUNT_EMAIL_ADDRESS = "exchange_account_email_address";
    private static final String EXCHANGE_ACCOUNT_FORWARD_EMAIL_DISABLED = "exchange_account_forward_email_disabled";
    private static final String EXCHANGE_ACCOUNT_IS_DEFAULT = "exchange_account_is_default";
    private static final String EXCHANGE_ACCOUNT_PERIOD_CALENDAR = "exchange_account_period_calendar";
    private static final String EXCHANGE_ACCOUNT_PROTOCOL_VERSION = "exchange_account_protocol_version";
    private static final String EXCHANGE_ACCOUNT_SCREENSHOT_DISABLED = "exchange_account_screenshot_disabled";
    private static final String EXCHANGE_ACCOUNT_SENDER_NAME = "exchange_account_sender_name";
    private static final String EXCHANGE_ACCOUNT_SERVER_ADDRESS = "exchange_account_server_address";
    private static final String EXCHANGE_ACCOUNT_SERVER_PASSWORD = "exchange_account_server_password";
    private static final String EXCHANGE_ACCOUNT_SERVER_PATH_PREFIX = "exchange_account_server_path_prefix";
    private static final String EXCHANGE_ACCOUNT_SIGNATURE = "exchange_account_signature";
    private static final String EXCHANGE_ACCOUNT_SYNC_INTERVAL = "exchange_account_sync_interval";
    private static final String EXCHANGE_ACCOUNT_SYNC_LOOKBACK = "exchange_account_sync_lookback";
    private static final String EXCHANGE_ACCOUNT_USER = "exchange_account_user";
    private static final String EXCHANGE_ACCOUNT_USE_SSL = "exchange_account_use_ssl";
    private static final String EXCHANGE_ACCOUNT_USE_TSL = "exchange_account_use_tsl";
    private static final String RESTRICT_DISABLE_ADD_ACCOUNT = "email-disable-add-account";
    private static final String RESTRICT_DISABLE_CLEAR_DATA = "email-disable-clear-data";
    private static final String RESTRICT_DISABLE_DELETE_ACCOUNT = "email-disable-delete-account";
    private static final String RESTRICT_DISABLE_IMAP_POP3 = "email-disable-imap-pop3";
    private static final String TAG = "DeviceEmailManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setAccountDeletionDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        if (this.mDpm.removePolicy(admin, DISABLING_DELETION_WHITE_LIST, null) && this.mDpm.removePolicy(admin, ALLOWING_DELETION_BLACK_LIST, null)) {
            return this.mDpm.setPolicy(admin, RESTRICT_DISABLE_DELETE_ACCOUNT, bundle);
        }
        return false;
    }

    public boolean isAccountDeletionDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, RESTRICT_DISABLE_DELETE_ACCOUNT);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value", false);
    }

    public boolean isAppDataClearanceDisabled() {
        Bundle bundle = this.mDpm.getPolicy(null, RESTRICT_DISABLE_CLEAR_DATA);
        if (bundle == null) {
            return isAccountDeletionDisabled(null);
        }
        return !bundle.getBoolean("value", false) ? isAccountDeletionDisabled(null) : true;
    }

    public boolean isAppDataClearanceDisabled(Context context) {
        Bundle bundle = this.mDpm.getPolicy(null, RESTRICT_DISABLE_CLEAR_DATA);
        if (bundle == null) {
            return isAccountDataDeletionDisabled(context);
        }
        return !bundle.getBoolean("value", false) ? isAccountDataDeletionDisabled(context) : true;
    }

    private boolean isAccountDataDeletionDisabled(Context context) {
        if (context == null) {
            Log.d(TAG, "MDM6->isClearAppDtaDisabled->context is null ");
            return false;
        }
        boolean disable = isAccountDeletionDisabled(null);
        AccountManager am = AccountManager.get(context);
        if (am == null) {
            Log.e(TAG, "AccountManager is null in function isAccountDataDeletionDisabled ");
            return false;
        }
        Account[] emailAccounts = am.getAccountsByType("com.android.email");
        Account[] exchangeAccounts = am.getAccountsByType("com.android.exchange");
        int i;
        if (disable) {
            ArrayList<String> whiteList = getAccountsWhiteListDisablingDeletion(null);
            i = 0;
            while (i < emailLen) {
                if (isInRuleList(emailAccounts[i].name, whiteList)) {
                    i++;
                } else {
                    Log.d(TAG, "MDM6->isClearAppDtaDisabled->email in white");
                    return true;
                }
            }
            i = 0;
            while (i < easLen) {
                if (isInRuleList(exchangeAccounts[i].name, whiteList)) {
                    i++;
                } else {
                    Log.d(TAG, "MDM6->isClearAppDtaDisabled->eas in white");
                    return true;
                }
            }
        }
        ArrayList<String> blackList = getAccountsBlackListAllowingDeletion(null);
        for (Account account : emailAccounts) {
            if (isInRuleList(account.name, blackList)) {
                Log.d(TAG, "MDM6->isClearAppDtaDisabled->email in black");
                return true;
            }
        }
        for (Account account2 : exchangeAccounts) {
            if (isInRuleList(account2.name, blackList)) {
                Log.d(TAG, "MDM6->isClearAppDtaDisabled->eas in black");
                return true;
            }
        }
        return false;
    }

    public boolean setAppDataClearanceDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, RESTRICT_DISABLE_CLEAR_DATA, bundle);
    }

    public boolean setPop3ImapDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, RESTRICT_DISABLE_IMAP_POP3, bundle);
    }

    public boolean isPop3ImapDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, RESTRICT_DISABLE_IMAP_POP3);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value", false);
    }

    public boolean setAccountAdditionDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        if (this.mDpm.removePolicy(admin, DISABLING_ADDITION_WHITE_LIST, null) && this.mDpm.removePolicy(admin, ALLOWING_ADDITION_BLACK_LIST, null)) {
            return this.mDpm.setPolicy(admin, RESTRICT_DISABLE_ADD_ACCOUNT, bundle);
        }
        return false;
    }

    public boolean isAccountAdditionDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, RESTRICT_DISABLE_ADD_ACCOUNT);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value", false);
    }

    public void configEmailAccount(ComponentName admin, EmailAccount emailAccount) {
        Bundle emailAccountConfig = new Bundle();
        if (emailAccount == null) {
            throw new IllegalArgumentException("Error: EmailAccount is null!");
        }
        emailAccountConfig.putString(EMAIL_ACCOUNT_ADDRESS, emailAccount.mEmailAddress);
        emailAccountConfig.putString(EMAIL_ACCOUNT_SIGNATURE, emailAccount.mSignature);
        emailAccountConfig.putString(EMAIL_ACCOUNT_SENDER_NAME, emailAccount.mSenderName);
        emailAccountConfig.putString(EMAIL_ACCOUNT_IS_DEFAULT, emailAccount.mIsDefault ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_PROTOCOL, emailAccount.mInComingProtocol);
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_ADDRESS, emailAccount.mInComingServerAddress);
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_PASSWORD, emailAccount.mInComingServerPassword);
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_LOGIN, emailAccount.mInComingServerLogin);
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_PORT, String.valueOf(emailAccount.mInComingServerPort));
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_ACCEPT_ALL_CERTIFICATES, emailAccount.mInComingServerAcceptAllCertificates ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_USE_SSL, emailAccount.mInComingServerUseSSL ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_USE_TLS, emailAccount.mInComingServerUseTLS ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_PATH_PREFIX, emailAccount.mInComingPathPrefix);
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_PROTOCOL, emailAccount.mOutGoingProtocol);
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_ADDRESS, emailAccount.mOutGoingServerAddress);
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_PASSWORD, emailAccount.mOutGoingServerPassword);
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_LOGIN, emailAccount.mOutGoingServerLogin);
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_PORT, String.valueOf(emailAccount.mOutGoingServerPort));
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_ACCEPT_ALL_CERTIFICATES, emailAccount.mOutGoingServerAcceptAllCertificates ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_USE_SSL, emailAccount.mOutGoingServerUseSSL ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_USE_TLS, emailAccount.mOutGoingServerUseTLS ? "1" : "0");
        emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_PATH_PREFIX, emailAccount.mOutGoingPathPrefix);
        this.mDpm.setPolicy(admin, EMAIL_ACCOUNT_CONFIG, emailAccountConfig);
        setAppDataClearanceDisabled(admin, true);
    }

    public void configExchangeAccount(ComponentName admin, ExchangeAccount exchangeAccount) {
        Bundle exchangeAccountConfig = new Bundle();
        if (exchangeAccount == null) {
            throw new IllegalArgumentException("Error: ExchangeAccount is null!");
        }
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_ACCEPT_ALL_CERTIFICATES, exchangeAccount.mAcceptAllCertificates ? "1" : "0");
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_DISPLAY_NAME, exchangeAccount.mDisplayName);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_DOMAIN, exchangeAccount.mEasDomain);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_USER, exchangeAccount.mEasUser);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_EMAIL_ADDRESS, exchangeAccount.mEmailAddress);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_IS_DEFAULT, exchangeAccount.mIsDefault ? "1" : "0");
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_PERIOD_CALENDAR, String.valueOf(exchangeAccount.mCalendarPeriod));
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_PROTOCOL_VERSION, exchangeAccount.mProtocolVersion);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SENDER_NAME, exchangeAccount.mSenderName);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SERVER_ADDRESS, exchangeAccount.mServerAddress);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SERVER_PASSWORD, exchangeAccount.mServerPassword);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SERVER_PATH_PREFIX, exchangeAccount.mServerPathPrefix);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SIGNATURE, exchangeAccount.mSignature);
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SYNC_INTERVAL, String.valueOf(exchangeAccount.mSyncInterval));
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SYNC_LOOKBACK, String.valueOf(exchangeAccount.mSyncLookback));
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_USE_SSL, exchangeAccount.mUseSSL ? "1" : "0");
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_USE_TSL, exchangeAccount.mUseTLS ? "1" : "0");
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SCREENSHOT_DISABLED, exchangeAccount.mScreenShotDisabled ? "1" : "0");
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_DISPLAY_HTML_DISABLED, exchangeAccount.mDisplayHtmlDisabled ? "1" : "0");
        exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_FORWARD_EMAIL_DISABLED, exchangeAccount.mForwardEmailDisabled ? "1" : "0");
        this.mDpm.setPolicy(admin, EXCHANGE_ACCOUNT_CONFIG, exchangeAccountConfig);
        setAppDataClearanceDisabled(admin, true);
    }

    public void configExchangeMailProvider(ComponentName admin, HwMailProvider para) {
        if (para == null) {
            Log.w(TAG, "configExchangeMailProvider para is null");
            return;
        }
        Bundle paraex = new Bundle();
        paraex.putString("id", para.getId());
        paraex.putString(StreamItemsColumns.RES_LABEL, para.getLabel());
        paraex.putString("domain", para.getDomain());
        paraex.putString("incominguri", para.getIncominguri());
        paraex.putString("incomingusername", para.getIncomingusername());
        paraex.putString("incomingfield", para.getIncomingfield());
        paraex.putString("outgoinguri", para.getOutgoinguri());
        paraex.putString("outgoingusername", para.getOutgoingusername());
        this.mDpm.configExchangeMailProvider(admin, paraex);
    }

    public HwMailProvider getMailProviderForDomain(ComponentName admin, String domain) {
        Bundle paraex = this.mDpm.getMailProviderForDomain(admin, domain);
        if (paraex != null) {
            return new HwMailProvider(paraex.getString("id"), paraex.getString(StreamItemsColumns.RES_LABEL), paraex.getString("domain"), paraex.getString("incominguri"), paraex.getString("incomingusername"), paraex.getString("incomingfield"), paraex.getString("outgoinguri"), paraex.getString("outgoingusername"));
        }
        return null;
    }

    public boolean setEmailForwardingDisabled(ComponentName admin, String emailAddress, boolean disabled) {
        if (isAddressValid(emailAddress)) {
            Bundle bundle = new Bundle();
            bundle.putString("email_address", emailAddress);
            bundle.putString("email_forward", String.valueOf(disabled));
            setAppDataClearanceDisabled(admin, true);
            return this.mDpm.setPolicy(admin, EMAIL_DISABLE_FORWARDING, bundle);
        }
        throw new IllegalArgumentException("Invalid Parameters!");
    }

    private boolean isAddressValid(String emailAddress) {
        if (TextUtils.isEmpty(emailAddress) || emailAddress.indexOf("@") == -1) {
            return false;
        }
        return true;
    }

    public boolean setExceptionListForAccountAddition(ComponentName admin, boolean disabled, ArrayList<String> list) {
        if (disabled) {
            return setAccountsToWhiteListDisablingAddition(admin, list);
        }
        return setAccountsToBlackListAllowingAddition(admin, list);
    }

    public boolean setExceptionListForAccountDeletion(ComponentName admin, boolean disabled, ArrayList<String> list) {
        if (disabled) {
            return setAccountsToWhiteListDisablingDeletion(admin, list);
        }
        return setAccountsToBlackListAllowingDeletion(admin, list);
    }

    private boolean setAccountsToWhiteListDisablingAddition(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (setAccountAdditionDisabled(admin, true)) {
            return this.mDpm.setPolicy(admin, DISABLING_ADDITION_WHITE_LIST, bundle);
        }
        return false;
    }

    private boolean setAccountsToBlackListAllowingAddition(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (setAccountAdditionDisabled(admin, false)) {
            return this.mDpm.setPolicy(admin, ALLOWING_ADDITION_BLACK_LIST, bundle);
        }
        return false;
    }

    public ArrayList<String> getAccountsWhiteListDisablingAddition(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLING_ADDITION_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public ArrayList<String> getAccountsBlackListAllowingAddition(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, ALLOWING_ADDITION_BLACK_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean isAccountAdditionDisabled(ComponentName admin, String emailAddress) {
        if (TextUtils.isEmpty(emailAddress)) {
            return false;
        }
        boolean matched;
        if (isAccountAdditionDisabled(admin)) {
            matched = isInRuleList(emailAddress, getAccountsWhiteListDisablingAddition(admin));
            Log.d(TAG, "MDM6->isAccountAdditionDisabled->matched in white: " + matched);
            return matched ^ 1;
        }
        matched = isInRuleList(emailAddress, getAccountsBlackListAllowingAddition(admin));
        Log.d(TAG, "MDM6->isAccountAdditionDisabled->matched in black: " + matched);
        return matched;
    }

    private boolean setAccountsToWhiteListDisablingDeletion(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (setAccountDeletionDisabled(admin, true)) {
            return this.mDpm.setPolicy(admin, DISABLING_DELETION_WHITE_LIST, bundle);
        }
        return false;
    }

    private boolean setAccountsToBlackListAllowingDeletion(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (setAccountDeletionDisabled(admin, false)) {
            return this.mDpm.setPolicy(admin, ALLOWING_DELETION_BLACK_LIST, bundle);
        }
        return false;
    }

    public ArrayList<String> getAccountsWhiteListDisablingDeletion(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLING_DELETION_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public ArrayList<String> getAccountsBlackListAllowingDeletion(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, ALLOWING_DELETION_BLACK_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean isAccountDeletionDisabled(ComponentName admin, String emailAddress) {
        if (TextUtils.isEmpty(emailAddress)) {
            return false;
        }
        boolean matched;
        if (isAccountDeletionDisabled(admin)) {
            matched = isInRuleList(emailAddress, getAccountsWhiteListDisablingDeletion(admin));
            Log.d(TAG, "MDM6->isAccountDeletionAllowed->matched in white: " + matched);
            return matched ^ 1;
        }
        matched = isInRuleList(emailAddress, getAccountsBlackListAllowingDeletion(admin));
        Log.d(TAG, "MDM6->isAccountDeletionAllowed->matched in black: " + matched);
        return matched;
    }

    private boolean wildCardMatch(String text, String pattern) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(pattern)) {
            return false;
        }
        text = text.toLowerCase(Locale.US);
        pattern = pattern.toLowerCase(Locale.US);
        String[] cards = pattern.split("\\*");
        if (!pattern.endsWith(PhoneConstantsEx.APN_TYPE_ALL) && cards.length > 1 && (text.endsWith(cards[cards.length - 1]) ^ 1) != 0) {
            return false;
        }
        boolean isFirstPart = true;
        for (String card : cards) {
            int idx = text.indexOf(card);
            if (isFirstPart) {
                if (!text.startsWith(card)) {
                    return false;
                }
                isFirstPart = false;
            }
            if (idx == -1) {
                return false;
            }
            text = text.substring(card.length() + idx);
        }
        return true;
    }

    public boolean isInRuleList(String emailAddress, ArrayList<String> rules) {
        if (TextUtils.isEmpty(emailAddress) || rules == null || rules.isEmpty()) {
            return false;
        }
        int size = rules.size();
        for (int i = 0; i < size; i++) {
            if (wildCardMatch(emailAddress, (String) rules.get(i))) {
                return true;
            }
        }
        return false;
    }
}
