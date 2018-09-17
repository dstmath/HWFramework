package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class HwEmailMDMPlugin extends DevicePolicyPlugin {
    private static final String ALLOWING_ADDITION_BLACK_LIST_ITEM = "allowing-addition-black-list/allow-add-black-list-item";
    private static final String ALLOWING_DELETION_BLACK_LIST_ITEM = "allowing-deletion-black-list/allow-del-black-list-item";
    public static final String DEVICE_POLICY_ACTION_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String DISABLING_ADDITION_WHITE_LIST_ITEM = "disabling-addition-white-list/disable-add-white-list-item";
    private static final String DISABLING_DELETION_WHITE_LIST_ITEM = "disabling-deletion-white-list/disable-del-white-list-item";
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
    private static final String MDM_POLICY_REMOVED = "mdm_policy_removed";
    private static final String RESTRICT_DISABLE_ADD_ACCOUNT = "email-disable-add-account";
    private static final String RESTRICT_DISABLE_CLEAR_DATA = "email-disable-clear-data";
    private static final String RESTRICT_DISABLE_DELETE_ACCOUNT = "email-disable-delete-account";
    private static final String RESTRICT_DISABLE_IMAP_POP3 = "email-disable-imap-pop3";
    private static final String RESTRICT_EMAIL_FORWARDING = "email_forward";
    private static final String RESTRICT_EMAIL_FORWARDING_EMAIL_ADDRESS = "email_address";
    public static final String TAG = "HwEmailMDMPlugin";

    public HwEmailMDMPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(RESTRICT_DISABLE_DELETE_ACCOUNT, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(RESTRICT_DISABLE_ADD_ACCOUNT, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(RESTRICT_DISABLE_IMAP_POP3, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(RESTRICT_DISABLE_CLEAR_DATA, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(EMAIL_DISABLE_FORWARDING, PolicyType.CONFIGURATION, new String[]{RESTRICT_EMAIL_FORWARDING_EMAIL_ADDRESS, RESTRICT_EMAIL_FORWARDING});
        struct.addStruct(DISABLING_ADDITION_WHITE_LIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct(ALLOWING_ADDITION_BLACK_LIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct(DISABLING_DELETION_WHITE_LIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct(ALLOWING_DELETION_BLACK_LIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct(EMAIL_ACCOUNT_CONFIG, PolicyType.CONFIGURATION, new String[]{EMAIL_ACCOUNT_INCOMING_PROTOCOL, EMAIL_ACCOUNT_INCOMING_SERVER_ADDRESS, EMAIL_ACCOUNT_INCOMING_SERVER_PASSWORD, EMAIL_ACCOUNT_INCOMING_SERVER_LOGIN, EMAIL_ACCOUNT_INCOMING_SERVER_PORT, EMAIL_ACCOUNT_INCOMING_SERVER_ACCEPT_ALL_CERTIFICATES, EMAIL_ACCOUNT_INCOMING_SERVER_USE_SSL, EMAIL_ACCOUNT_INCOMING_SERVER_USE_TLS, EMAIL_ACCOUNT_INCOMING_PATH_PREFIX, EMAIL_ACCOUNT_OUTGOING_PROTOCOL, EMAIL_ACCOUNT_OUTGOING_SERVER_ADDRESS, EMAIL_ACCOUNT_OUTGOING_SERVER_PASSWORD, EMAIL_ACCOUNT_OUTGOING_SERVER_LOGIN, EMAIL_ACCOUNT_OUTGOING_SERVER_PORT, EMAIL_ACCOUNT_OUTGOING_SERVER_ACCEPT_ALL_CERTIFICATES, EMAIL_ACCOUNT_OUTGOING_SERVER_USE_SSL, EMAIL_ACCOUNT_OUTGOING_SERVER_USE_TLS, EMAIL_ACCOUNT_OUTGOING_PATH_PREFIX, EMAIL_ACCOUNT_ADDRESS, EMAIL_ACCOUNT_SENDER_NAME, EMAIL_ACCOUNT_SIGNATURE, EMAIL_ACCOUNT_IS_DEFAULT});
        struct.addStruct(EXCHANGE_ACCOUNT_CONFIG, PolicyType.CONFIGURATION, new String[]{EXCHANGE_ACCOUNT_ACCEPT_ALL_CERTIFICATES, EXCHANGE_ACCOUNT_DISPLAY_NAME, EXCHANGE_ACCOUNT_DOMAIN, EXCHANGE_ACCOUNT_USER, EXCHANGE_ACCOUNT_EMAIL_ADDRESS, EXCHANGE_ACCOUNT_IS_DEFAULT, EXCHANGE_ACCOUNT_PERIOD_CALENDAR, EXCHANGE_ACCOUNT_PROTOCOL_VERSION, EXCHANGE_ACCOUNT_SENDER_NAME, EXCHANGE_ACCOUNT_SERVER_ADDRESS, EXCHANGE_ACCOUNT_SERVER_PASSWORD, EXCHANGE_ACCOUNT_SERVER_PATH_PREFIX, EXCHANGE_ACCOUNT_SIGNATURE, EXCHANGE_ACCOUNT_SYNC_INTERVAL, EXCHANGE_ACCOUNT_SYNC_LOOKBACK, EXCHANGE_ACCOUNT_USE_SSL, EXCHANGE_ACCOUNT_USE_TSL, EXCHANGE_ACCOUNT_SCREENSHOT_DISABLED, EXCHANGE_ACCOUNT_DISPLAY_HTML_DISABLED, EXCHANGE_ACCOUNT_FORWARD_EMAIL_DISABLED});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName admin, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_EMAIL", "does not have PERMISSION_MDM_EMAIL permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName admin, String policyName, Bundle policyData, boolean globalPolicyChanged) {
        HwLog.i(TAG, "onSetPolicy PolicyName " + policyName + " globalPolicyChanged:" + globalPolicyChanged);
        Intent intent = new Intent(DEVICE_POLICY_ACTION_POLICY_CHANGED);
        intent.setClassName("com.android.email", "com.huawei.emailmdm.EmailMDMBroadcastReceiver");
        intent.putExtras(policyData);
        intent.putExtra("MDMName", admin.flattenToString());
        if ((!globalPolicyChanged && isStatePolicy(policyName)) || policyName.equals(RESTRICT_DISABLE_CLEAR_DATA)) {
            return true;
        }
        intent.putExtra("PolicyName", policyName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(Binder.getCallingUid()));
        if (policyName.equals(EMAIL_ACCOUNT_CONFIG) || policyName.equals(EXCHANGE_ACCOUNT_CONFIG) || EMAIL_DISABLE_FORWARDING.equals(policyName)) {
            return false;
        }
        return true;
    }

    private boolean isStatePolicy(String policyName) {
        if (policyName.equals(RESTRICT_DISABLE_DELETE_ACCOUNT) || policyName.equals(RESTRICT_DISABLE_ADD_ACCOUNT)) {
            return true;
        }
        return policyName.equals(RESTRICT_DISABLE_IMAP_POP3);
    }

    public boolean onRemovePolicy(ComponentName admin, String policyName, Bundle policyData, boolean globalPolicyChanged) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName admin, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved ComponentName " + admin);
        Intent intent = new Intent(DEVICE_POLICY_ACTION_POLICY_CHANGED);
        intent.setClassName("com.android.email", "com.huawei.emailmdm.EmailMDMBroadcastReceiver");
        intent.putExtra("PolicyName", MDM_POLICY_REMOVED);
        intent.putExtra("MDMName", admin.flattenToString());
        this.mContext.sendBroadcast(intent);
        return true;
    }
}
