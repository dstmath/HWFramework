package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.server.HwServiceFactory;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class DeviceNetworkPlugin extends DevicePolicyPlugin {
    private static final String ACTION_NETWORK_BLACK_LIST_ANDROID_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String ACTION_NETWORK_BLACK_LIST_CHANGED = "com.huawei.devicepolicy.NETWORK_BLACK_LIST_CHANGED";
    private static final int ADD_NETWORK_ACCESS_LIST = 0;
    private static final int BINARY = 2;
    private static final int BLACK_LIST = 1;
    private static final int CODE_SET_NETWORK_ACCESS_LIST = 1106;
    private static final int DEFAULT_SIZE = 10;
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final int DOMAIN_MAX_NUM = 200;
    private static final int EXIST_BLACK_AND_DOMAIN_NETWORK_POLICY_FLAG = 3;
    private static final int EXIST_BLACK_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 2;
    private static final int EXIST_WHITE_AND_DOMAIN_NETWORK_POLICY_FLAG = 1;
    private static final int EXIST_WHITE_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 0;
    private static final Uri[] HISTORY_URI = {Uri.parse("content://com.huawei.browser.history.provider/history"), Uri.parse("content://com.android.browser/history"), Uri.parse("content://com.android.browser.historyprovider/history")};
    private static final int IP_MAX_NUM = 200;
    private static final String ITEM_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list/network-black-domain-list-item";
    private static final String ITEM_NETWORK_BLACK_IP_LIST = "network-black-ip-list/network-black-ip-list-item";
    private static final String ITEM_NETWORK_BLACK_LIST = "network-black-list/network-black-list-item";
    private static final String ITEM_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list/network-white-domain-list-item";
    private static final String ITEM_NETWORK_WHITE_IP_LIST = "network-white-ip-list/network-white-ip-list-item";
    private static final String KEY_NETWORK_POLICY_FLAG = "network_policy";
    private static final String KEY_NETWORK_POLICY_PROPERTIES = "sys.mdm.domain_network_policy";
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int MAX_NUM = 1000;
    private static final String MDM_NETWORK_MANAGER_PERMISSION = "com.huawei.permission.sec.MDM_NETWORK_MANAGER";
    private static final int NETWORK_POLICY_NOT_SET = -1;
    private static final String POLICY_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list";
    private static final String POLICY_NETWORK_BLACK_IP_LIST = "network-black-ip-list";
    private static final String POLICY_NETWORK_BLACK_LIST = "network-black-list";
    private static final String POLICY_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list";
    private static final String POLICY_NETWORK_WHITE_IP_LIST = "network-white-ip-list";
    private static final int SET_NETWORK_ACCESS_LIST = 1;
    public static final String TAG = DeviceNetworkPlugin.class.getSimpleName();
    private static final String URL_COLUMN_NAME = "url";
    private static final int WHITE_LIST = 0;

    public DeviceNetworkPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(POLICY_NETWORK_BLACK_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_LIST, PolicyStruct.PolicyType.LIST, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(POLICY_NETWORK_WHITE_IP_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_WHITE_IP_LIST, PolicyStruct.PolicyType.LIST, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(POLICY_NETWORK_WHITE_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_WHITE_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(POLICY_NETWORK_BLACK_IP_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_IP_LIST, PolicyStruct.PolicyType.LIST, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(POLICY_NETWORK_BLACK_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[]{SettingsMDMPlugin.STATE_VALUE});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct != null) {
            return true;
        }
        HwLog.d(TAG, "policyStruct of DeviceNetworkPlugin is null");
        return false;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean checkCallingPermission(ComponentName who, String policyName) {
        char c;
        switch (policyName.hashCode()) {
            case -30728870:
                if (policyName.equals(POLICY_NETWORK_BLACK_DOMAIN_LIST)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 81510017:
                if (policyName.equals(POLICY_NETWORK_WHITE_IP_LIST)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 746015831:
                if (policyName.equals(POLICY_NETWORK_BLACK_IP_LIST)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1297508996:
                if (policyName.equals(POLICY_NETWORK_WHITE_DOMAIN_LIST)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1767633579:
                if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1 || c == 2 || c == 3 || c == 4) {
            HwLog.i(TAG, "check the calling Permission");
            this.mContext.enforceCallingOrSelfPermission(MDM_NETWORK_MANAGER_PERMISSION, "does not have network_manager MDM permission!");
            return true;
        }
        HwLog.e(TAG, "unknown policy name: " + policyName);
        return false;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        if (this.mPolicyStruct == null || policyData == null) {
            HwLog.i(TAG, "policy struct of the list of network is null");
            return false;
        }
        char c = 65535;
        switch (policyName.hashCode()) {
            case -30728870:
                if (policyName.equals(POLICY_NETWORK_BLACK_DOMAIN_LIST)) {
                    c = 4;
                    break;
                }
                break;
            case 81510017:
                if (policyName.equals(POLICY_NETWORK_WHITE_IP_LIST)) {
                    c = 1;
                    break;
                }
                break;
            case 746015831:
                if (policyName.equals(POLICY_NETWORK_BLACK_IP_LIST)) {
                    c = 2;
                    break;
                }
                break;
            case 1297508996:
                if (policyName.equals(POLICY_NETWORK_WHITE_DOMAIN_LIST)) {
                    c = 3;
                    break;
                }
                break;
            case 1767633579:
                if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                    c = 0;
                    break;
                }
                break;
        }
        if (c == 0) {
            HwLog.i(TAG, "onSetPolicy and policyName: " + policyName + " changed:" + isChanged);
            ArrayList<String> data = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
            if (data == null || data.size() == 0) {
                return false;
            }
            if (!isChanged || checkPolicySize(policyName, data, MAX_NUM)) {
                return true;
            }
            throw new IllegalArgumentException("list beyond maximum number");
        } else if (c == 1) {
            return setWhiteIpPolicyOfNetworkList(policyData, policyName, isChanged);
        } else {
            if (c == 2) {
                return setBlackIpPolicyOfNetworkList(policyData, policyName, isChanged);
            }
            if (c == 3) {
                return setWhiteDomainPolicyOfNetworkList(policyData, policyName, isChanged);
            }
            if (c == 4) {
                return setBlackDomainPolicyOfNetworkList(policyData, policyName, isChanged);
            }
            HwLog.e(TAG, "unknown policy name: " + policyName);
            return false;
        }
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        if (isChanged) {
            char c = 65535;
            if (policyName.hashCode() == 1767633579 && policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                c = 0;
            }
            if (c != 0) {
                String str = TAG;
                HwLog.e(str, "unknown policy name: " + policyName);
                return;
            }
            HwLog.i(TAG, "send broadcast when on set policy completed.");
            sendBroadcast(ACTION_NETWORK_BLACK_LIST_CHANGED);
            sendAndroidBrowserBroadcast("com.huawei.devicepolicy.action.POLICY_CHANGED");
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        char c;
        HwLog.i(TAG, " onRemovePolicy and policyName: " + policyName + " changed:" + isChanged);
        switch (policyName.hashCode()) {
            case -30728870:
                if (policyName.equals(POLICY_NETWORK_BLACK_DOMAIN_LIST)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 81510017:
                if (policyName.equals(POLICY_NETWORK_WHITE_IP_LIST)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 746015831:
                if (policyName.equals(POLICY_NETWORK_BLACK_IP_LIST)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1297508996:
                if (policyName.equals(POLICY_NETWORK_WHITE_DOMAIN_LIST)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return removeIpPolicyOfNetworkList(policyName, policyData, 0, isChanged);
        }
        if (c == 1) {
            return removeIpPolicyOfNetworkList(policyName, policyData, 1, isChanged);
        }
        if (c == 2) {
            return removeDomainPolicyOfNetworkList(policyName, policyData, 0, isChanged);
        }
        if (c == 3) {
            return removeDomainPolicyOfNetworkList(policyName, policyData, 1, isChanged);
        }
        HwLog.e(TAG, "unknown policy name: " + policyName);
        return true;
    }

    public void onRemovePolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        if (isChanged) {
            char c = 65535;
            switch (policyName.hashCode()) {
                case -30728870:
                    if (policyName.equals(POLICY_NETWORK_BLACK_DOMAIN_LIST)) {
                        c = 4;
                        break;
                    }
                    break;
                case 81510017:
                    if (policyName.equals(POLICY_NETWORK_WHITE_IP_LIST)) {
                        c = 1;
                        break;
                    }
                    break;
                case 746015831:
                    if (policyName.equals(POLICY_NETWORK_BLACK_IP_LIST)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1297508996:
                    if (policyName.equals(POLICY_NETWORK_WHITE_DOMAIN_LIST)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1767633579:
                    if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwLog.i(TAG, "send broadcast when on remove policy completed.");
                sendBroadcast(ACTION_NETWORK_BLACK_LIST_CHANGED);
                sendAndroidBrowserBroadcast("com.huawei.devicepolicy.action.POLICY_CHANGED");
            } else if (c == 1 || c == 2) {
                setIptablesAfterRemoveWhitePolicy();
            } else if (c == 3 || c == 4) {
                setIptablesAfterRemoveBlackPolicy();
            } else {
                HwLog.e(TAG, "unknown policy name: " + policyName);
            }
        }
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        int networkPolicyFlag;
        HwLog.i(TAG, "the active admin has been Removed");
        boolean isNetworkListLimitPolicy = false;
        if (removedPolicies == null) {
            HwLog.e(TAG, "removed policied list is null");
            return;
        }
        for (int i = 0; i < removedPolicies.size(); i++) {
            PolicyStruct.PolicyItem policyItem = removedPolicies.get(i);
            if (policyItem != null) {
                String policyName = policyItem.getPolicyName();
                if (!policyItem.isGlobalPolicyChanged()) {
                    continue;
                } else if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                    sendBroadcast(ACTION_NETWORK_BLACK_LIST_CHANGED);
                    sendAndroidBrowserBroadcast("com.huawei.devicepolicy.action.POLICY_CHANGED");
                } else if (isNetworkListLimitPolicy(policyName)) {
                    isNetworkListLimitPolicy = true;
                } else {
                    return;
                }
            }
        }
        if (isNetworkListLimitPolicy && (networkPolicyFlag = getNetworkPolicyFlag()) != -1) {
            if (networkPolicyFlag == 0 || networkPolicyFlag == 1) {
                setOrAddNetworkAccessList(null, 0, 1);
                setNetworkPolicyFlag(-1);
            } else if (networkPolicyFlag == 2 || networkPolicyFlag == 3) {
                setOrAddNetworkAccessList(null, 1, 1);
                setNetworkPolicyFlag(-1);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0054, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0055, code lost:
        if (r5 != null) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005b, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005f, code lost:
        throw r7;
     */
    public ArrayList<String> queryBrowsingHistory() {
        ArrayList<String> historyList = new ArrayList<>((int) DEFAULT_SIZE);
        for (Uri uri : HISTORY_URI) {
            try {
                Cursor cursor = this.mContext.getContentResolver().query(uri, new String[]{URL_COLUMN_NAME}, null, null, null);
                if (cursor == null) {
                    HwLog.e(TAG, "query browser history cursor is null ");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return historyList;
                }
                while (cursor.moveToNext()) {
                    String url = cursor.getString(cursor.getColumnIndex(URL_COLUMN_NAME));
                    if (!TextUtils.isEmpty(url)) {
                        historyList.add(url);
                    }
                }
                cursor.close();
            } catch (SQLiteException e) {
                HwLog.e(TAG, "query browser history exception");
            }
        }
        return historyList;
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setPackage("com.huawei.browser");
        this.mContext.sendBroadcast(intent, MDM_NETWORK_MANAGER_PERMISSION);
    }

    private void sendAndroidBrowserBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setPackage("com.android.browser");
        this.mContext.sendBroadcast(intent);
    }

    private boolean checkPolicySize(String policyName, ArrayList<String> policyData, int maxSize) {
        Bundle allPolicyData = this.mPolicyStruct.getPolicyItem(policyName).combineAllAttributes();
        if (allPolicyData == null) {
            return policyData.size() <= maxSize;
        }
        ArrayList<String> policies = new ArrayList<>();
        try {
            policies = allPolicyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "check policy size ArrayIndexOutOfBoundsException");
        }
        return policies == null || !policyData.removeAll(policies) || policies.size() + policyData.size() <= maxSize;
    }

    private boolean setWhiteIpPolicyOfNetworkList(Bundle policyData, String policyName, boolean isChanged) {
        if (!isChanged) {
            return true;
        }
        if (!checkPolicyData(policyData, policyName, 200)) {
            return false;
        }
        ArrayList<String> policyList = new ArrayList<>();
        try {
            policyList = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set white ip policy of networkList ArrayIndexOutOfBoundsException");
        }
        if (isValidIpAddress(policyList)) {
            int networkPolicyFlag = getNetworkPolicyFlag();
            if (existOppositeNetworkList(0, networkPolicyFlag)) {
                return false;
            }
            if (networkPolicyFlag == -1) {
                setOrAddNetworkAccessList(policyList, 0, 1);
                setNetworkPolicyFlag(0);
            } else {
                setOrAddNetworkAccessList(policyList, 0, 0);
            }
            return true;
        }
        throw new IllegalArgumentException("invalid ip address");
    }

    private boolean setBlackIpPolicyOfNetworkList(Bundle policyData, String policyName, boolean isChanged) {
        if (!isChanged) {
            return true;
        }
        if (!checkPolicyData(policyData, policyName, 200)) {
            return false;
        }
        int networkPolicyFlag = getNetworkPolicyFlag();
        ArrayList<String> policyList = new ArrayList<>();
        try {
            policyList = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set black ip policy of networkList ArrayIndexOutOfBoundsException");
        }
        if (!isValidIpAddress(policyList)) {
            throw new IllegalArgumentException("invalid ip address");
        } else if (existOppositeNetworkList(1, networkPolicyFlag)) {
            return false;
        } else {
            if (networkPolicyFlag == -1) {
                setOrAddNetworkAccessList(policyList, 1, 1);
                setNetworkPolicyFlag(2);
            } else {
                setOrAddNetworkAccessList(policyList, 1, 0);
            }
            return true;
        }
    }

    private boolean setWhiteDomainPolicyOfNetworkList(Bundle policyData, String policyName, boolean isChanged) {
        if (!isChanged) {
            return true;
        }
        if (!checkPolicyData(policyData, policyName, 200)) {
            return false;
        }
        int networkPolicyFlag = getNetworkPolicyFlag();
        ArrayList<String> policyList = new ArrayList<>();
        try {
            policyList = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set white domain policy Of networkList ArrayIndexOutOfBoundsException");
        }
        if (!isValidDomainAddress(policyList)) {
            throw new IllegalArgumentException("invalid domain name");
        } else if (existOppositeNetworkList(0, networkPolicyFlag)) {
            return false;
        } else {
            if (networkPolicyFlag == -1) {
                ArrayList<String> localhostIp = new ArrayList<>();
                localhostIp.add(LOCAL_HOST);
                setOrAddNetworkAccessList(localhostIp, 0, 1);
            }
            if (networkPolicyFlag != 1) {
                setNetworkPolicyFlag(1);
            }
            return true;
        }
    }

    private boolean setBlackDomainPolicyOfNetworkList(Bundle policyData, String policyName, boolean isChanged) {
        if (!isChanged) {
            return true;
        }
        if (!checkPolicyData(policyData, policyName, 200)) {
            return false;
        }
        int networkPolicyFlag = getNetworkPolicyFlag();
        ArrayList<String> policyList = new ArrayList<>();
        try {
            policyList = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set black domain policy of networkList ArrayIndexOutOfBoundsException");
        }
        if (!isValidDomainAddress(policyList)) {
            throw new IllegalArgumentException("invalid domain name");
        } else if (existOppositeNetworkList(1, networkPolicyFlag)) {
            return false;
        } else {
            if (networkPolicyFlag != 3) {
                setNetworkPolicyFlag(3);
            }
            return true;
        }
    }

    private boolean removeIpPolicyOfNetworkList(String policyName, Bundle policyData, int whiteOrBlack, boolean isChanged) {
        if (this.mPolicyStruct == null || policyData == null) {
            HwLog.i(TAG, "policy struct of the list of network is null");
            return false;
        } else if (!isChanged) {
            return true;
        } else {
            ArrayList<String> data = new ArrayList<>();
            try {
                data = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "remove ip policy of networkList ArrayIndexOutOfBoundsException");
            }
            if (data == null || data.size() == 0) {
                return false;
            }
            if (!isValidIpAddress(data)) {
                throw new IllegalArgumentException("invalid ip address");
            } else if (existOppositeNetworkList(whiteOrBlack, getNetworkPolicyFlag())) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean removeDomainPolicyOfNetworkList(String policyName, Bundle policyData, int whiteOrBlack, boolean isChanged) {
        if (this.mPolicyStruct == null || policyData == null) {
            HwLog.i(TAG, "policy struct of the list of network is null");
            return false;
        } else if (!isChanged) {
            return true;
        } else {
            ArrayList<String> policyList = new ArrayList<>();
            try {
                policyList = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "remove domain policy of networkList ArrayIndexOutOfBoundsException");
            }
            if (policyList == null || policyList.size() == 0) {
                return false;
            }
            if (!isValidDomainAddress(policyList)) {
                throw new IllegalArgumentException("invalid domain address");
            } else if (existOppositeNetworkList(whiteOrBlack, getNetworkPolicyFlag())) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void setIptablesAfterRemoveWhitePolicy() {
        List<String> networkWhiteIpPolicies = getPolicyArrayList(POLICY_NETWORK_WHITE_IP_LIST);
        List<String> networkWhiteDomainPolicies = getPolicyArrayList(POLICY_NETWORK_WHITE_DOMAIN_LIST);
        HwServiceFactory.getHwConnectivityManager().clearIpCacheOfDnsEvent();
        if (networkWhiteIpPolicies != null && !networkWhiteIpPolicies.isEmpty()) {
            if (networkWhiteDomainPolicies == null || networkWhiteDomainPolicies.isEmpty()) {
                setNetworkPolicyFlag(0);
            } else {
                setNetworkPolicyFlag(1);
            }
            setOrAddNetworkAccessList(networkWhiteIpPolicies, 0, 1);
        } else if (networkWhiteDomainPolicies == null || networkWhiteDomainPolicies.isEmpty()) {
            setNetworkPolicyFlag(-1);
            setOrAddNetworkAccessList(networkWhiteIpPolicies, 0, 1);
        } else {
            setNetworkPolicyFlag(1);
            ArrayList<String> localhostIp = new ArrayList<>();
            localhostIp.add(LOCAL_HOST);
            setOrAddNetworkAccessList(localhostIp, 0, 1);
        }
    }

    private void setIptablesAfterRemoveBlackPolicy() {
        List<String> networkBlackIpPolicies = getPolicyArrayList(POLICY_NETWORK_BLACK_IP_LIST);
        List<String> networkBlackDomainPolicies = getPolicyArrayList(POLICY_NETWORK_BLACK_DOMAIN_LIST);
        HwServiceFactory.getHwConnectivityManager().clearIpCacheOfDnsEvent();
        if (networkBlackIpPolicies.isEmpty()) {
            if (networkBlackDomainPolicies == null || networkBlackDomainPolicies.isEmpty()) {
                setNetworkPolicyFlag(-1);
            } else {
                setNetworkPolicyFlag(3);
            }
        } else if (networkBlackDomainPolicies == null || networkBlackDomainPolicies.isEmpty()) {
            setNetworkPolicyFlag(2);
        } else {
            setNetworkPolicyFlag(3);
        }
        setOrAddNetworkAccessList(networkBlackIpPolicies, 1, 1);
    }

    private boolean isValidIpAddress(ArrayList<String> addrList) {
        if (addrList == null || addrList.size() == 0) {
            return false;
        }
        Pattern ipv4Pattern = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)(\\/\\d{1,2})?$");
        Pattern ipv6StdPattern = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
        Pattern ipv6HexCompressedPattern = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");
        Iterator<String> it = addrList.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (!ipv4Pattern.matcher(str).matches() && !ipv6StdPattern.matcher(str).matches() && !ipv6HexCompressedPattern.matcher(str).matches()) {
                return false;
            }
        }
        HwLog.d(TAG, "isValidIpAddress input valid ");
        return true;
    }

    private boolean isValidDomainAddress(ArrayList<String> addrList) {
        if (addrList == null || addrList.size() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$");
        Iterator<String> it = addrList.iterator();
        while (it.hasNext()) {
            if (!pattern.matcher(it.next()).matches()) {
                return false;
            }
        }
        HwLog.d(TAG, " isValidDomainAddress input valid ");
        return true;
    }

    private boolean isDomainNameMatches(String hostName, String hostNamePolicy) {
        int index;
        if (TextUtils.isEmpty(hostName) || TextUtils.isEmpty(hostNamePolicy) || hostNamePolicy.length() > hostName.length() || (index = hostName.indexOf(hostNamePolicy)) != hostName.length() - hostNamePolicy.length() || (index != 0 && hostName.charAt(index - 1) != '.')) {
            return false;
        }
        return true;
    }

    private List<String> getPolicyArrayList(String policyName) {
        Bundle policyData = this.mPolicyStruct.getPolicyItem(policyName).combineAllAttributes();
        if (policyData == null || policyData.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> policies = new ArrayList<>();
        try {
            policies = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "get policy array list ArrayIndexOutOfBoundsException");
        }
        if (policies == null || policies.isEmpty()) {
            return Collections.emptyList();
        }
        return policies;
    }

    private void setOrAddNetworkAccessList(List<String> addrList, int whiteOrBlack, int setOrAdd) {
        IBinder binder = ServiceManager.getService("network_management");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        HwLog.d(TAG, " is now in func ");
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                data.writeStringList(addrList);
                data.writeInt(whiteOrBlack);
                data.writeInt(setOrAdd);
                binder.transact(CODE_SET_NETWORK_ACCESS_LIST, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                HwLog.e(TAG, "operate NetworkAccessList error");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    private void setNetworkPolicyFlag(int networkPolicyFlag) {
        long identity = Binder.clearCallingIdentity();
        String flag = null;
        if (networkPolicyFlag == -1) {
            SystemProperties.set(KEY_NETWORK_POLICY_PROPERTIES, "false");
        } else if (networkPolicyFlag == 0) {
            SystemProperties.set(KEY_NETWORK_POLICY_PROPERTIES, "false");
            flag = Integer.toBinaryString(networkPolicyFlag);
        } else if (networkPolicyFlag == 1) {
            SystemProperties.set(KEY_NETWORK_POLICY_PROPERTIES, "true");
            flag = Integer.toBinaryString(networkPolicyFlag);
        } else if (networkPolicyFlag == 2) {
            SystemProperties.set(KEY_NETWORK_POLICY_PROPERTIES, "false");
            flag = Integer.toBinaryString(networkPolicyFlag);
        } else if (networkPolicyFlag != 3) {
            HwLog.e(TAG, "networkPolicyFlag error");
            return;
        } else {
            SystemProperties.set(KEY_NETWORK_POLICY_PROPERTIES, "true");
            flag = Integer.toBinaryString(networkPolicyFlag);
        }
        try {
            Settings.System.putString(this.mContext.getContentResolver(), KEY_NETWORK_POLICY_FLAG, flag);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean checkPolicyData(Bundle policyData, String policyName, int maxSize) {
        ArrayList<String> data = new ArrayList<>();
        try {
            data = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "check policy data ArrayIndexOutOfBoundsException");
        }
        if (data == null || data.size() == 0) {
            return false;
        }
        if (checkPolicySize(policyName, data, maxSize)) {
            return true;
        }
        throw new IllegalArgumentException("list beyond maximum number");
    }

    private int getNetworkPolicyFlag() {
        String flagStr = Settings.System.getString(this.mContext.getContentResolver(), KEY_NETWORK_POLICY_FLAG);
        if (TextUtils.isEmpty(flagStr)) {
            HwLog.d(TAG, "getNetworkPolicyFlag flagStr null");
            return -1;
        }
        try {
            return Integer.parseInt(flagStr, 2);
        } catch (NumberFormatException e) {
            String str = TAG;
            HwLog.e(str, "getNetworkPolicyFlag parseInt error flagStr = " + flagStr);
            return -1;
        }
    }

    private boolean existOppositeNetworkList(int whiteOrBlack, int networkPolicyFlag) {
        if (whiteOrBlack == 0) {
            if (networkPolicyFlag == 3 || networkPolicyFlag == 2) {
                return true;
            }
            return false;
        } else if (whiteOrBlack != 1) {
            HwLog.e(TAG, "whiteOrBlack error");
            return true;
        } else if (networkPolicyFlag == 0 || networkPolicyFlag == 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNetworkListLimitPolicy(String policyName) {
        return POLICY_NETWORK_WHITE_IP_LIST.equals(policyName) || POLICY_NETWORK_WHITE_DOMAIN_LIST.equals(policyName) || POLICY_NETWORK_BLACK_IP_LIST.equals(policyName) || POLICY_NETWORK_BLACK_DOMAIN_LIST.equals(policyName);
    }
}
