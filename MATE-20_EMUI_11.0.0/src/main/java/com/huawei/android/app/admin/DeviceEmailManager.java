package com.huawei.android.app.admin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Locale;

public class DeviceEmailManager {
    private static final String ACCOUNT_ADDED_BY_MDM = "accountAddedByMdm";
    private static final String ACCOUNT_WHERE = "emailAddress=?";
    private static final String ALIAS_PATH = "/alias/";
    private static final String ALLOWING_ADDITION_BLACK_LIST = "allowing-addition-black-list";
    private static final String ALLOWING_DELETION_BLACK_LIST = "allowing-deletion-black-list";
    private static final String AUTHORITY_EMAIL_MDM_ACCOUNT_PROVIDER = "com.android.email.huawei.mdm.provider";
    public static final int DISABLE_SYNC_VALUE = 0;
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
    private static final String EMAIL_ADDRESS = "email_address";
    private static final String EMAIL_DISABLE_FORWARDING = "email-disable-forwarding";
    public static final int ENABLE_SYNC_VALUE = 1;
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
    private static final String EXCHANGE_ACCOUNT_SYNC_CALENDAR = "exchange_account_sync_calendar";
    private static final String EXCHANGE_ACCOUNT_SYNC_CONTACTS = "exchange_account_sync_contacts";
    private static final String EXCHANGE_ACCOUNT_SYNC_INTERVAL = "exchange_account_sync_interval";
    private static final String EXCHANGE_ACCOUNT_SYNC_LOOKBACK = "exchange_account_sync_lookback";
    private static final String EXCHANGE_ACCOUNT_USER = "exchange_account_user";
    private static final String EXCHANGE_ACCOUNT_USE_SSL = "exchange_account_use_ssl";
    private static final String EXCHANGE_ACCOUNT_USE_TSL = "exchange_account_use_tsl";
    private static final String EXCHANGE_FORCE_SMIME_ENCRYPTION = "exchange_force_smime_encryption";
    private static final String EXCHANGE_FORCE_SMIME_SIGN = "exchange_force_smime_sign";
    public static final int FORCE_SYNC_VALUE = 2;
    private static final String KEY_EMAIL_ADDRESS = "key_email_address";
    private static final String RESTRICT_DISABLE_ADD_ACCOUNT = "email-disable-add-account";
    private static final String RESTRICT_DISABLE_CLEAR_DATA = "email-disable-clear-data";
    private static final String RESTRICT_DISABLE_DELETE_ACCOUNT = "email-disable-delete-account";
    private static final String RESTRICT_DISABLE_IMAP_POP3 = "email-disable-imap-pop3";
    private static final String SMIME_ALIAS = "force_alias";
    private static final String SMIME_FORCE_REQUIRED = "force_required";
    private static final String SMIME_PATH = "/smime/";
    private static final String SMIME_TYPE = "smime_type";
    public static final int SMIME_TYPE_ENCRYPTION = 2;
    public static final int SMIME_TYPE_SIGN = 1;
    public static final int SYNC_TYPE_CALENDAR = 0;
    public static final int SYNC_TYPE_CONTACTS = 1;
    private static final String SYNC_VALUE = "syncValue";
    private static final String TAG = "DeviceEmailManager";
    private static final String URI_ACCOUNT_ADDED_PATH = "/accountAdded";
    private static final String URI_CONTENT_HEADER = "content://";
    private static final String URI_SYCN_CALENDAR_PATH = "/syncCalendar";
    private static final String URI_SYCN_CONTACTS_PATH = "/syncContacts";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setAccountDeletionDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.removePolicy(admin, DISABLING_DELETION_WHITE_LIST, null) && this.mDpm.removePolicy(admin, ALLOWING_DELETION_BLACK_LIST, null) && this.mDpm.setPolicy(admin, RESTRICT_DISABLE_DELETE_ACCOUNT, bundle);
    }

    public boolean isAccountDeletionDisabled(ComponentName admin) {
        return isAccountDeletionDisabled(admin, UserHandle.myUserId());
    }

    public boolean isAccountDeletionDisabled(ComponentName admin, int userId) {
        Bundle bundle = this.mDpm.getPolicy(admin, RESTRICT_DISABLE_DELETE_ACCOUNT, userId);
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
        return bundle.getBoolean("value", false) || isAccountDeletionDisabled(null);
    }

    public boolean isAppDataClearanceDisabled(Context context) {
        Bundle bundle = this.mDpm.getPolicy(null, RESTRICT_DISABLE_CLEAR_DATA);
        if (bundle == null) {
            return isAccountDataDeletionDisabled(context);
        }
        return bundle.getBoolean("value", false) || isAccountDataDeletionDisabled(context);
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
        if (disable) {
            ArrayList<String> trustList = getAccountsTrustListDisablingDeletion(null);
            for (Account account : emailAccounts) {
                if (!isInRuleList(account.name, trustList)) {
                    Log.d(TAG, "MDM6->isClearAppDtaDisabled->email in trust");
                    return true;
                }
            }
            for (Account account2 : exchangeAccounts) {
                if (!isInRuleList(account2.name, trustList)) {
                    Log.d(TAG, "MDM6->isClearAppDtaDisabled->eas in trust");
                    return true;
                }
            }
        } else {
            ArrayList<String> blockList = getAccountsBlockListAllowingDeletion(null);
            for (Account account3 : emailAccounts) {
                if (isInRuleList(account3.name, blockList)) {
                    Log.d(TAG, "MDM6->isClearAppDtaDisabled->email in block");
                    return true;
                }
            }
            for (Account account4 : exchangeAccounts) {
                if (isInRuleList(account4.name, blockList)) {
                    Log.d(TAG, "MDM6->isClearAppDtaDisabled->eas in block");
                    return true;
                }
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
        return this.mDpm.removePolicy(admin, DISABLING_ADDITION_WHITE_LIST, null) && this.mDpm.removePolicy(admin, ALLOWING_ADDITION_BLACK_LIST, null) && this.mDpm.setPolicy(admin, RESTRICT_DISABLE_ADD_ACCOUNT, bundle);
    }

    public boolean isAccountAdditionDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, RESTRICT_DISABLE_ADD_ACCOUNT);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value", false);
    }

    public void configEmailAccount(ComponentName admin, EmailAccount emailAccount) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        Bundle emailAccountConfig = new Bundle();
        if (emailAccount != null) {
            emailAccountConfig.putString(EMAIL_ACCOUNT_ADDRESS, emailAccount.mEmailAddress);
            emailAccountConfig.putString(EMAIL_ACCOUNT_SIGNATURE, emailAccount.mSignature);
            emailAccountConfig.putString(EMAIL_ACCOUNT_SENDER_NAME, emailAccount.mSenderName);
            String str6 = "1";
            emailAccountConfig.putString(EMAIL_ACCOUNT_IS_DEFAULT, emailAccount.mIsDefault ? str6 : "0");
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_PROTOCOL, emailAccount.mInComingProtocol);
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_ADDRESS, emailAccount.mInComingServerAddress);
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_PASSWORD, emailAccount.mInComingServerPassword);
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_LOGIN, emailAccount.mInComingServerLogin);
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_PORT, String.valueOf(emailAccount.mInComingServerPort));
            if (emailAccount.mInComingServerAcceptAllCertificates) {
                str = str6;
            } else {
                str = "0";
            }
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_ACCEPT_ALL_CERTIFICATES, str);
            if (emailAccount.mInComingServerUseSSL) {
                str2 = str6;
            } else {
                str2 = "0";
            }
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_USE_SSL, str2);
            if (emailAccount.mInComingServerUseTLS) {
                str3 = str6;
            } else {
                str3 = "0";
            }
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_SERVER_USE_TLS, str3);
            emailAccountConfig.putString(EMAIL_ACCOUNT_INCOMING_PATH_PREFIX, emailAccount.mInComingPathPrefix);
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_PROTOCOL, emailAccount.mOutGoingProtocol);
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_ADDRESS, emailAccount.mOutGoingServerAddress);
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_PASSWORD, emailAccount.mOutGoingServerPassword);
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_LOGIN, emailAccount.mOutGoingServerLogin);
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_PORT, String.valueOf(emailAccount.mOutGoingServerPort));
            if (emailAccount.mOutGoingServerAcceptAllCertificates) {
                str4 = str6;
            } else {
                str4 = "0";
            }
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_ACCEPT_ALL_CERTIFICATES, str4);
            if (emailAccount.mOutGoingServerUseSSL) {
                str5 = str6;
            } else {
                str5 = "0";
            }
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_USE_SSL, str5);
            if (!emailAccount.mOutGoingServerUseTLS) {
                str6 = "0";
            }
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_SERVER_USE_TLS, str6);
            emailAccountConfig.putString(EMAIL_ACCOUNT_OUTGOING_PATH_PREFIX, emailAccount.mOutGoingPathPrefix);
            this.mDpm.setPolicy(admin, EMAIL_ACCOUNT_CONFIG, emailAccountConfig);
            setAppDataClearanceDisabled(admin, true);
            return;
        }
        throw new IllegalArgumentException("Error: EmailAccount is null!");
    }

    public void configExchangeAccount(ComponentName admin, ExchangeAccount exchangeAccount) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        Bundle exchangeAccountConfig = new Bundle();
        if (exchangeAccount != null) {
            String str6 = "1";
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_ACCEPT_ALL_CERTIFICATES, exchangeAccount.mAcceptAllCertificates ? str6 : "0");
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_DISPLAY_NAME, exchangeAccount.mDisplayName);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_DOMAIN, exchangeAccount.mEasDomain);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_USER, exchangeAccount.mEasUser);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_EMAIL_ADDRESS, exchangeAccount.mEmailAddress);
            if (exchangeAccount.mIsDefault) {
                str = str6;
            } else {
                str = "0";
            }
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_IS_DEFAULT, str);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_PERIOD_CALENDAR, String.valueOf(exchangeAccount.mCalendarPeriod));
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_PROTOCOL_VERSION, exchangeAccount.mProtocolVersion);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SENDER_NAME, exchangeAccount.mSenderName);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SERVER_ADDRESS, exchangeAccount.mServerAddress);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SERVER_PASSWORD, exchangeAccount.mServerPassword);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SERVER_PATH_PREFIX, exchangeAccount.mServerPathPrefix);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SIGNATURE, exchangeAccount.mSignature);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SYNC_INTERVAL, String.valueOf(exchangeAccount.mSyncInterval));
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SYNC_LOOKBACK, String.valueOf(exchangeAccount.mSyncLookback));
            if (exchangeAccount.mUseSSL) {
                str2 = str6;
            } else {
                str2 = "0";
            }
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_USE_SSL, str2);
            if (exchangeAccount.mUseTLS) {
                str3 = str6;
            } else {
                str3 = "0";
            }
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_USE_TSL, str3);
            if (exchangeAccount.mScreenShotDisabled) {
                str4 = str6;
            } else {
                str4 = "0";
            }
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SCREENSHOT_DISABLED, str4);
            if (exchangeAccount.mDisplayHtmlDisabled) {
                str5 = str6;
            } else {
                str5 = "0";
            }
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_DISPLAY_HTML_DISABLED, str5);
            if (!exchangeAccount.mForwardEmailDisabled) {
                str6 = "0";
            }
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_FORWARD_EMAIL_DISABLED, str6);
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SYNC_CALENDAR, String.valueOf(exchangeAccount.mSyncCalendar));
            exchangeAccountConfig.putString(EXCHANGE_ACCOUNT_SYNC_CONTACTS, String.valueOf(exchangeAccount.mSyncContacts));
            this.mDpm.setPolicy(admin, EXCHANGE_ACCOUNT_CONFIG, exchangeAccountConfig);
            setAppDataClearanceDisabled(admin, true);
            return;
        }
        throw new IllegalArgumentException("Error: ExchangeAccount is null!");
    }

    public void configExchangeMailProvider(ComponentName admin, HwMailProvider para) {
        if (para == null) {
            Log.w(TAG, "configExchangeMailProvider para is null");
            return;
        }
        Bundle paraex = new Bundle();
        paraex.putString("id", para.getId());
        paraex.putString("label", para.getLabel());
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
            return new HwMailProvider(paraex.getString("id"), paraex.getString("label"), paraex.getString("domain"), paraex.getString("incominguri"), paraex.getString("incomingusername"), paraex.getString("incomingfield"), paraex.getString("outgoinguri"), paraex.getString("outgoingusername"));
        }
        return null;
    }

    public boolean setEmailForwardingDisabled(ComponentName admin, String emailAddress, boolean disabled) {
        if (isAddressValid(emailAddress)) {
            Bundle bundle = new Bundle();
            bundle.putString(EMAIL_ADDRESS, emailAddress);
            bundle.putString("email_forward", String.valueOf(disabled));
            setAppDataClearanceDisabled(admin, true);
            return this.mDpm.setPolicy(admin, EMAIL_DISABLE_FORWARDING, bundle);
        }
        throw new IllegalArgumentException("Invalid Parameters!");
    }

    private boolean isAddressValid(String emailAddress) {
        if (!TextUtils.isEmpty(emailAddress) && emailAddress.indexOf("@") != -1) {
            return true;
        }
        return false;
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
        if (!setAccountAdditionDisabled(admin, true) || !this.mDpm.setPolicy(admin, DISABLING_ADDITION_WHITE_LIST, bundle)) {
            return false;
        }
        return true;
    }

    private boolean setAccountsToBlackListAllowingAddition(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (!setAccountAdditionDisabled(admin, false) || !this.mDpm.setPolicy(admin, ALLOWING_ADDITION_BLACK_LIST, bundle)) {
            return false;
        }
        return true;
    }

    @Deprecated
    public ArrayList<String> getAccountsWhiteListDisablingAddition(ComponentName admin) {
        return getAccountsTrustListDisablingAddition(admin);
    }

    @Deprecated
    public ArrayList<String> getAccountsBlackListAllowingAddition(ComponentName admin) {
        return getAccountsBlockListAllowingAddition(admin);
    }

    public ArrayList<String> getAccountsBlockListAllowingAddition(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, ALLOWING_ADDITION_BLACK_LIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getAccountsBlockListAllowingAddition exception.");
            return null;
        }
    }

    public ArrayList<String> getAccountsTrustListDisablingAddition(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLING_ADDITION_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getAccountsTrustListDisablingAddition exception.");
            return null;
        }
    }

    public boolean isAccountAdditionDisabled(ComponentName admin, String emailAddress) {
        if (TextUtils.isEmpty(emailAddress)) {
            return false;
        }
        if (isAccountAdditionDisabled(admin)) {
            boolean matched = isInRuleList(emailAddress, getAccountsTrustListDisablingAddition(admin));
            Log.d(TAG, "MDM6->isAccountAdditionDisabled->matched in trust: " + matched);
            return !matched;
        }
        boolean matched2 = isInRuleList(emailAddress, getAccountsBlockListAllowingAddition(admin));
        Log.d(TAG, "MDM6->isAccountAdditionDisabled->matched in block: " + matched2);
        return matched2;
    }

    private boolean setAccountsToWhiteListDisablingDeletion(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (!setAccountDeletionDisabled(admin, true) || !this.mDpm.setPolicy(admin, DISABLING_DELETION_WHITE_LIST, bundle)) {
            return false;
        }
        return true;
    }

    private boolean setAccountsToBlackListAllowingDeletion(ComponentName admin, ArrayList<String> accounts) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", accounts);
        if (!setAccountDeletionDisabled(admin, false) || !this.mDpm.setPolicy(admin, ALLOWING_DELETION_BLACK_LIST, bundle)) {
            return false;
        }
        return true;
    }

    @Deprecated
    public ArrayList<String> getAccountsWhiteListDisablingDeletion(ComponentName admin) {
        return getAccountsTrustListDisablingDeletion(admin);
    }

    @Deprecated
    public ArrayList<String> getAccountsWhiteListDisablingDeletion(ComponentName admin, int userId) {
        return getAccountsTrustListDisablingDeletion(admin, userId);
    }

    public ArrayList<String> getAccountsTrustListDisablingDeletion(ComponentName admin) {
        return getAccountsTrustListDisablingDeletion(admin, UserHandle.myUserId());
    }

    public ArrayList<String> getAccountsTrustListDisablingDeletion(ComponentName admin, int userId) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLING_DELETION_WHITE_LIST, userId);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getAccountsTrustListDisablingDeletion exception.");
            return null;
        }
    }

    @Deprecated
    public ArrayList<String> getAccountsBlackListAllowingDeletion(ComponentName admin) {
        return getAccountsBlockListAllowingDeletion(admin);
    }

    @Deprecated
    public ArrayList<String> getAccountsBlackListAllowingDeletion(ComponentName admin, int userId) {
        return getAccountsBlockListAllowingDeletion(admin, userId);
    }

    public ArrayList<String> getAccountsBlockListAllowingDeletion(ComponentName admin) {
        return getAccountsBlockListAllowingDeletion(admin, UserHandle.myUserId());
    }

    public ArrayList<String> getAccountsBlockListAllowingDeletion(ComponentName admin, int userId) {
        Bundle bundle = this.mDpm.getPolicy(admin, ALLOWING_DELETION_BLACK_LIST, userId);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getAccountsBlockListAllowingDeletion exception.");
            return null;
        }
    }

    public boolean isAccountDeletionDisabled(ComponentName admin, String emailAddress) {
        return isAccountDeletionDisabled(admin, emailAddress, UserHandle.myUserId());
    }

    public boolean isAccountDeletionDisabled(ComponentName admin, String emailAddress, int userId) {
        if (TextUtils.isEmpty(emailAddress)) {
            return false;
        }
        if (isAccountDeletionDisabled(admin, userId)) {
            boolean matched = isInRuleList(emailAddress, getAccountsTrustListDisablingDeletion(admin, userId));
            Log.d(TAG, "MDM6->isAccountDeletionAllowed->matched in trust: " + matched);
            return !matched;
        }
        boolean matched2 = isInRuleList(emailAddress, getAccountsBlockListAllowingDeletion(admin, userId));
        Log.d(TAG, "MDM6->isAccountDeletionAllowed->matched in block: " + matched2);
        return matched2;
    }

    private boolean wildCardMatch(String text, String pattern) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(pattern)) {
            return false;
        }
        String text2 = text.toLowerCase(Locale.US);
        String pattern2 = pattern.toLowerCase(Locale.US);
        String[] cards = pattern2.split("\\*");
        if (!(pattern2.endsWith("*") || cards.length <= 1 || text2.endsWith(cards[cards.length - 1]))) {
            return false;
        }
        boolean isFirstPart = true;
        String text3 = text2;
        for (String card : cards) {
            int idx = text3.indexOf(card);
            if (isFirstPart) {
                if (!text3.startsWith(card)) {
                    return false;
                }
                isFirstPart = false;
            }
            if (idx == -1) {
                return false;
            }
            text3 = text3.substring(card.length() + idx);
        }
        return true;
    }

    public boolean isInRuleList(String emailAddress, ArrayList<String> rules) {
        if (TextUtils.isEmpty(emailAddress) || rules == null || rules.isEmpty()) {
            return false;
        }
        int size = rules.size();
        for (int i = 0; i < size; i++) {
            if (wildCardMatch(emailAddress, rules.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void setAccountSync(Context context, String emailAddress, int syncType, int syncValue, int userId) {
        if (context == null || TextUtils.isEmpty(emailAddress) || syncType < 0 || syncType > 1 || syncValue < 0 || syncValue > 2) {
            Log.w(TAG, "MDM6->setAccountSync->invalid param!");
            return;
        }
        Uri.Builder builder = getUriBySyncType(syncType, userId).buildUpon().appendQueryParameter(KEY_EMAIL_ADDRESS, emailAddress);
        ContentValues values = new ContentValues();
        values.put(SYNC_VALUE, Integer.valueOf(syncValue));
        context.getContentResolver().update(builder.build(), values, null, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        if (r7 != null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0063, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008b, code lost:
        if (0 != 0) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008e, code lost:
        return r2;
     */
    public boolean isAccountForceSync(Context context, String emailAddress, int syncType, int userId) {
        Throwable th;
        NumberFormatException ex;
        boolean result = false;
        if (context != null && !TextUtils.isEmpty(emailAddress) && syncType >= 0) {
            boolean z = true;
            if (syncType <= 1) {
                Cursor cursor = null;
                try {
                    try {
                        cursor = context.getContentResolver().query(getUriBySyncType(syncType, userId).buildUpon().appendQueryParameter(KEY_EMAIL_ADDRESS, emailAddress).build(), new String[]{SYNC_VALUE}, null, null, null);
                        if (cursor != null && cursor.moveToNext()) {
                            if (Integer.valueOf(cursor.getString(0)).intValue() != 2) {
                                z = false;
                            }
                            result = z;
                        }
                    } catch (NumberFormatException e) {
                        ex = e;
                        try {
                            Log.w(TAG, "MDM6->isAccountForceSync->NumberFormatException :" + ex.getMessage());
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                } catch (NumberFormatException e2) {
                    ex = e2;
                    Log.w(TAG, "MDM6->isAccountForceSync->NumberFormatException :" + ex.getMessage());
                } catch (Throwable th3) {
                    th = th3;
                    if (0 != 0) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
        Log.w(TAG, "MDM6->isAccountForceSync->invalid param!");
        return false;
    }

    private Uri getUriBySyncType(int syncType, int userId) {
        StringBuffer buffer = new StringBuffer(URI_CONTENT_HEADER);
        buffer.append(userId);
        buffer.append('@');
        buffer.append(AUTHORITY_EMAIL_MDM_ACCOUNT_PROVIDER);
        if (syncType == 0) {
            buffer.append(URI_SYCN_CALENDAR_PATH);
        } else if (syncType == 1) {
            buffer.append(URI_SYCN_CONTACTS_PATH);
        }
        return Uri.parse(buffer.toString());
    }

    public boolean isAccountAddedByMDM(Context context, String emailAddress, int userId) {
        if (context != null) {
            if (!TextUtils.isEmpty(emailAddress)) {
                ContentResolver resolver = context.getContentResolver();
                StringBuffer buffer = new StringBuffer(URI_CONTENT_HEADER);
                buffer.append(userId);
                buffer.append('@');
                buffer.append(AUTHORITY_EMAIL_MDM_ACCOUNT_PROVIDER);
                buffer.append(URI_ACCOUNT_ADDED_PATH);
                Cursor cursor = null;
                boolean result = false;
                try {
                    cursor = resolver.query(Uri.parse(buffer.toString()).buildUpon().appendQueryParameter(KEY_EMAIL_ADDRESS, emailAddress).build(), new String[]{ACCOUNT_ADDED_BY_MDM}, null, null, null);
                    if (cursor != null && cursor.moveToNext()) {
                        result = Boolean.valueOf(cursor.getString(0)).booleanValue();
                    }
                    return result;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        Log.w(TAG, "MDM6->isAccountForceSync->invalid param!");
        return false;
    }

    public boolean setForceSMIMECertificateAlias(ComponentName admin, String emailAddress, String alias, int type) {
        return setForceSMIMEPolicy(admin, emailAddress, alias, type, -1);
    }

    public boolean isForceSMIMECertificateAlias(Context context, String emailAddress, String alias, int type) {
        if (context == null || TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(alias) || (type != 1 && type != 2)) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(getUri(ALIAS_PATH, type), null, ACCOUNT_WHERE, new String[]{emailAddress}, null);
            if (cursor == null || !cursor.moveToNext()) {
                return false;
            }
            boolean equals = TextUtils.equals(alias, cursor.getString(0));
            cursor.close();
            return equals;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean setForceSMIMEMessages(ComponentName admin, String emailAddress, boolean force, int type) {
        return setForceSMIMEPolicy(admin, emailAddress, DeviceSettingsManager.EMPTY_STRING, type, force ? 1 : 0);
    }

    public boolean isForceSMIMEMessages(Context context, String emailAddress, int type) {
        if (context == null || TextUtils.isEmpty(emailAddress) || (type != 1 && type != 2)) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(getUri(SMIME_PATH, type), null, ACCOUNT_WHERE, new String[]{emailAddress}, null);
            if (cursor == null || !cursor.moveToNext()) {
                return false;
            }
            boolean equals = Boolean.TRUE.toString().equals(cursor.getString(0));
            cursor.close();
            return equals;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean setForceRequiredSMIMEAndCertificateAlias(ComponentName admin, String emailAddress, String alias, boolean force, int type) {
        return setForceSMIMEPolicy(admin, emailAddress, alias, type, force ? 1 : 0);
    }

    private boolean setForceSMIMEPolicy(ComponentName admin, String emailAddress, String alias, int type, int required) {
        String policy = getSmimePolicy(type);
        if (!isAddressValid(emailAddress) || alias == null || policy == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putString(EMAIL_ADDRESS, emailAddress);
        bundle.putString(SMIME_ALIAS, alias);
        bundle.putInt(SMIME_FORCE_REQUIRED, required);
        return this.mDpm.setPolicy(admin, policy, bundle);
    }

    private String getSmimePolicy(int type) {
        if (type == 1) {
            return EXCHANGE_FORCE_SMIME_SIGN;
        }
        if (type == 2) {
            return EXCHANGE_FORCE_SMIME_ENCRYPTION;
        }
        return null;
    }

    private Uri getUri(String path, int type) {
        StringBuffer buffer = new StringBuffer(URI_CONTENT_HEADER);
        buffer.append(AUTHORITY_EMAIL_MDM_ACCOUNT_PROVIDER);
        buffer.append(path);
        buffer.append(type);
        return Uri.parse(buffer.toString());
    }
}
