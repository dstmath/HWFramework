package com.android.server.devicepolicy.plugins;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final int ADD_APP_NETWORK_LIST = 0;
    private static final int ADD_NETWORK_ACCESS_LIST = 0;
    private static final int BINARY = 2;
    private static final int BLACK_LIST = 1;
    private static final int CODE_SET_APP_NETWORK_ACCESS_LIST = 1126;
    private static final int CODE_SET_NETWORK_ACCESS_LIST = 1106;
    private static final int DEFAULT_SIZE = 10;
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final int DOMAIN_MAX_LENGTH = 255;
    private static final int DOMAIN_MAX_NUM = 200;
    private static final int EXIST_BLACK_AND_DOMAIN_NETWORK_POLICY_FLAG = 3;
    private static final int EXIST_BLACK_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 2;
    private static final int EXIST_WHITE_AND_DOMAIN_NETWORK_POLICY_FLAG = 1;
    private static final int EXIST_WHITE_BUT_NOT_DOMAIN_NETWORK_POLICY_FLAG = 0;
    private static final int IP_MAX_NUM = 200;
    private static final String ITEM_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list/network-black-domain-list-item";
    private static final String ITEM_NETWORK_BLACK_IP_LIST = "network-black-ip-list/network-black-ip-list-item";
    private static final String ITEM_NETWORK_BLACK_LIST = "network-black-list/network-black-list-item";
    private static final String ITEM_NETWORK_TRUST_APP_LIST = "network-trust-app-list/network-trust-app-list-item";
    private static final String ITEM_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list/network-white-domain-list-item";
    private static final String ITEM_NETWORK_WHITE_IP_LIST = "network-white-ip-list/network-white-ip-list-item";
    private static final String KEY_NETWORK_APP_POLICY_FLAG = "network_app_policy";
    private static final String KEY_NETWORK_POLICY_FLAG = "network_policy";
    private static final String KEY_NETWORK_POLICY_PROPERTIES = "sys.mdm.domain_network_policy";
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int MAX_NUM = 1000;
    private static final String MDM_NETWORK_MANAGER_PERMISSION = "com.huawei.permission.sec.MDM_NETWORK_MANAGER";
    private static final int NETWORK_APP_POLICY_NOT_SET = 1;
    private static final int NETWORK_APP_POLICY_SET = 0;
    private static final int NETWORK_BLOCK_LIST = 1;
    private static final int NETWORK_POLICY_NOT_SET = -1;
    private static final int NETWORK_TRUST_LIST = 2;
    private static final String NODE_VALUE = "value";
    private static final String POLICY_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list";
    private static final String POLICY_NETWORK_BLACK_IP_LIST = "network-black-ip-list";
    private static final String POLICY_NETWORK_BLACK_LIST = "network-black-list";
    private static final String POLICY_NETWORK_TRUST_APP_LIST = "network-trust-app-list";
    private static final String POLICY_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list";
    private static final String POLICY_NETWORK_WHITE_IP_LIST = "network-white-ip-list";
    private static final int REMOVE_APP_NETWORK_LIST = 1;
    private static final int SET_NETWORK_ACCESS_LIST = 1;
    private static final String TAG = DeviceNetworkPlugin.class.getSimpleName();
    private static final String URL_COLUMN_NAME = "url";
    private static final int WHITE_LIST = 0;
    private RegisterAppAddOrRemoveReceiver mRegisterAppAddOrRemoveReceiver = null;

    public DeviceNetworkPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(POLICY_NETWORK_BLACK_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_NETWORK_WHITE_IP_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_WHITE_IP_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_NETWORK_WHITE_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_WHITE_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_NETWORK_BLACK_IP_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_IP_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_NETWORK_BLACK_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_DOMAIN_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_NETWORK_TRUST_APP_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_TRUST_APP_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct == null) {
            HwLog.d(TAG, "policyStruct of DeviceNetworkPlugin is null");
            return false;
        } else if (getNetworkAppPolicyFlag() != 0) {
            return true;
        } else {
            registerNetworkAppAddRecevier();
            return true;
        }
    }

    private void registerNetworkAppAddRecevier() {
        IntentFilter addOrRemoveFilter = new IntentFilter();
        if (this.mRegisterAppAddOrRemoveReceiver == null) {
            this.mRegisterAppAddOrRemoveReceiver = new RegisterAppAddOrRemoveReceiver();
        }
        addOrRemoveFilter.addAction("android.intent.action.PACKAGE_ADDED");
        addOrRemoveFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mRegisterAppAddOrRemoveReceiver, addOrRemoveFilter);
        HwLog.i(TAG, "register add package receiver");
    }

    private void unRegisterNetworkAppAddRecevier() {
        if (this.mRegisterAppAddOrRemoveReceiver != null) {
            this.mContext.unregisterReceiver(this.mRegisterAppAddOrRemoveReceiver);
            this.mRegisterAppAddOrRemoveReceiver = null;
            HwLog.i(TAG, "unregister add package receiver");
        }
    }

    private void setNetworkAppPolicyFlag(int networkAppPolicyFlag) {
        long identity = Binder.clearCallingIdentity();
        try {
            Settings.System.putString(this.mContext.getContentResolver(), KEY_NETWORK_APP_POLICY_FLAG, Integer.toBinaryString(networkAppPolicyFlag));
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private int getNetworkAppPolicyFlag() {
        String flagStr = Settings.System.getString(this.mContext.getContentResolver(), KEY_NETWORK_APP_POLICY_FLAG);
        if (TextUtils.isEmpty(flagStr)) {
            HwLog.d(TAG, "getNetworkAppPolicyFlag flagStr null");
            return 1;
        }
        try {
            return Integer.parseInt(flagStr, 2);
        } catch (NumberFormatException e) {
            String str = TAG;
            HwLog.e(str, "getNetworkAppPolicyFlag parseInt error flagStr = " + flagStr);
            return 1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean checkCallingPermission(ComponentName who, String policyName) {
        char c;
        switch (policyName.hashCode()) {
            case -1984799202:
                if (policyName.equals(POLICY_NETWORK_TRUST_APP_LIST)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
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
        if (c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5) {
            HwLog.i(TAG, "check the calling Permission");
            this.mContext.enforceCallingOrSelfPermission(MDM_NETWORK_MANAGER_PERMISSION, "does not have network_manager MDM permission!");
            return true;
        }
        HwLog.e(TAG, "unknown policy name: " + policyName);
        return false;
    }

    private boolean procNetworkBlackList(Bundle policyData, String policyName, boolean isChanged) {
        String str = TAG;
        HwLog.i(str, "onSetPolicy and policyName: " + policyName + " changed:" + isChanged);
        ArrayList<String> blackLists = null;
        try {
            blackLists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "procNetworkBlackList exception.");
        }
        if (blackLists == null || blackLists.isEmpty()) {
            return false;
        }
        if (!isChanged || checkPolicySize(policyName, blackLists, MAX_NUM)) {
            return true;
        }
        throw new IllegalArgumentException("list beyond maximum number");
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        if (policyData == null) {
            HwLog.i(TAG, "policy struct of the list of network is null");
            return false;
        }
        char c = 65535;
        switch (policyName.hashCode()) {
            case -1984799202:
                if (policyName.equals(POLICY_NETWORK_TRUST_APP_LIST)) {
                    c = 5;
                    break;
                }
                break;
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
            return procNetworkBlackList(policyData, policyName, isChanged);
        }
        if (c == 1) {
            return setWhiteIpPolicyOfNetworkList(policyData, policyName, isChanged);
        }
        if (c == 2) {
            return setBlackIpPolicyOfNetworkList(policyData, policyName, isChanged);
        }
        if (c == 3) {
            return setWhiteDomainPolicyOfNetworkList(policyData, policyName, isChanged);
        }
        if (c == 4) {
            return setBlackDomainPolicyOfNetworkList(policyData, policyName, isChanged);
        }
        if (c != 5) {
            HwLog.e(TAG, "unknown policy name: " + policyName);
            return false;
        } else if (!isChanged) {
            return true;
        } else {
            ArrayList<String> setTrustAppList = new ArrayList<>();
            try {
                setTrustAppList = policyData.getStringArrayList("value");
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "getStringArrayList get exception");
            }
            if (setTrustAppList == null || setTrustAppList.isEmpty()) {
                HwLog.e(TAG, "The list to be added is empty.");
                return false;
            }
            setOrRemoveAppNetworkAccessList(setTrustAppList, 2, 0);
            if (getNetworkAppPolicyFlag() == 1) {
                setNetworkAppPolicyFlag(0);
                registerNetworkAppAddRecevier();
            }
            return true;
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
            sendAndroidBrowserBroadcast(ACTION_NETWORK_BLACK_LIST_ANDROID_CHANGED);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        char c;
        HwLog.i(TAG, " onRemovePolicy and policyName: " + policyName + " changed:" + isChanged);
        switch (policyName.hashCode()) {
            case -1984799202:
                if (policyName.equals(POLICY_NETWORK_TRUST_APP_LIST)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
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
        if (c != 4) {
            HwLog.e(TAG, "unknown policy name: " + policyName);
            return true;
        } else if (policyData == null || !isChanged) {
            return true;
        } else {
            ArrayList<String> removetrustAppList = new ArrayList<>();
            try {
                removetrustAppList = policyData.getStringArrayList("value");
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "getStringArrayList get exception");
            }
            if (removetrustAppList == null || removetrustAppList.isEmpty()) {
                HwLog.e(TAG, "The list to be deleted is empty..");
                return false;
            }
            setOrRemoveAppNetworkAccessList(removetrustAppList, 2, 1);
            return true;
        }
    }

    public void onRemovePolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        if (isChanged) {
            char c = 65535;
            switch (policyName.hashCode()) {
                case -1984799202:
                    if (policyName.equals(POLICY_NETWORK_TRUST_APP_LIST)) {
                        c = 5;
                        break;
                    }
                    break;
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
                sendAndroidBrowserBroadcast(ACTION_NETWORK_BLACK_LIST_ANDROID_CHANGED);
            } else if (c == 1 || c == 2) {
                setIptablesAfterRemoveWhitePolicy();
            } else if (c == 3 || c == 4) {
                setIptablesAfterRemoveBlackPolicy();
            } else if (c != 5) {
                HwLog.e(TAG, "unknown policy name: " + policyName);
            } else {
                List<String> appTrustList = getPolicyArrayList(POLICY_NETWORK_TRUST_APP_LIST);
                if (appTrustList == null || appTrustList.isEmpty()) {
                    HwLog.i(TAG, "app network trust list is null, clear uid iptables");
                    setNetworkAppAndResetFlag();
                }
            }
        }
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return true;
    }

    private void dealNetworkListLimitPolicy() {
        int networkPolicyFlag = getNetworkPolicyFlag();
        if (networkPolicyFlag == -1) {
            return;
        }
        if (networkPolicyFlag == 0 || networkPolicyFlag == 1) {
            setOrAddNetworkAccessList(null, 0, 1);
            setNetworkPolicyFlag(-1);
        } else if (networkPolicyFlag == 2 || networkPolicyFlag == 3) {
            setOrAddNetworkAccessList(null, 1, 1);
            setNetworkPolicyFlag(-1);
        }
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "the active admin has been Removed");
        boolean isNetworkListLimitPolicy = false;
        if (removedPolicies == null) {
            HwLog.e(TAG, "removed policied list is null");
            return;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem policyItem = it.next();
            if (policyItem != null && policyItem.isGlobalPolicyChanged()) {
                String policyName = policyItem.getPolicyName();
                if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                    sendBroadcast(ACTION_NETWORK_BLACK_LIST_CHANGED);
                    sendAndroidBrowserBroadcast(ACTION_NETWORK_BLACK_LIST_ANDROID_CHANGED);
                } else if (isNetworkListLimitPolicy(policyName)) {
                    isNetworkListLimitPolicy = true;
                } else if (policyName.equals(POLICY_NETWORK_TRUST_APP_LIST)) {
                    setNetworkAppAndResetFlag();
                } else {
                    return;
                }
            }
        }
        if (isNetworkListLimitPolicy) {
            dealNetworkListLimitPolicy();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0046, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        if (r2 != null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004e, code lost:
        r0.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0051, code lost:
        throw r3;
     */
    private boolean getEachHistory(Uri uri, ArrayList<String> historyLists) {
        try {
            Cursor cursor = this.mContext.getContentResolver().query(uri, new String[]{URL_COLUMN_NAME}, null, null, null);
            if (cursor == null) {
                HwLog.e(TAG, "query browser history cursor is null ");
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(URL_COLUMN_NAME));
                if (!TextUtils.isEmpty(url)) {
                    historyLists.add(url);
                }
            }
            cursor.close();
            return true;
        } catch (SQLiteException e) {
            HwLog.e(TAG, "query browser history exception");
            return true;
        }
    }

    public ArrayList<String> queryBrowsingHistory() {
        ArrayList<String> historyLists = new ArrayList<>((int) DEFAULT_SIZE);
        int i = 0;
        Uri[] historyUris = {Uri.parse("content://com.huawei.browser.history.provider/history"), Uri.parse("content://com.android.browser/history"), Uri.parse("content://com.android.browser.historyprovider/history")};
        int length = historyUris.length;
        while (i < length && getEachHistory(historyUris[i], historyLists)) {
            i++;
        }
        return historyLists;
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
        if (this.mPolicyStruct == null) {
            return false;
        }
        Bundle allPolicyData = this.mPolicyStruct.getPolicyItem(policyName).combineAllAttributes();
        if (policyData.size() > maxSize) {
            return false;
        }
        List<String> policies = new ArrayList<>();
        try {
            policies = allPolicyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "check policy size ArrayIndexOutOfBoundsException");
        }
        if (policies == null || !policyData.removeAll(policies) || policies.size() + policyData.size() <= maxSize) {
            return true;
        }
        return false;
    }

    private boolean setWhiteIpPolicyOfNetworkList(Bundle policyData, String policyName, boolean isChanged) {
        if (!isChanged) {
            return true;
        }
        if (!checkPolicyData(policyData, policyName, 200)) {
            return false;
        }
        ArrayList<String> policyLists = new ArrayList<>();
        try {
            policyLists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set white ip policy of networkList ArrayIndexOutOfBoundsException");
        }
        if (isValidIpAddress(policyLists)) {
            int networkPolicyFlag = getNetworkPolicyFlag();
            if (existOppositeNetworkList(0, networkPolicyFlag)) {
                return false;
            }
            if (networkPolicyFlag == -1) {
                setOrAddNetworkAccessList(policyLists, 0, 1);
                setNetworkPolicyFlag(0);
            } else {
                setOrAddNetworkAccessList(policyLists, 0, 0);
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
        ArrayList<String> policyLists = new ArrayList<>();
        try {
            policyLists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set black ip policy of networkList ArrayIndexOutOfBoundsException");
        }
        if (!isValidIpAddress(policyLists)) {
            throw new IllegalArgumentException("invalid ip address");
        } else if (existOppositeNetworkList(1, networkPolicyFlag)) {
            return false;
        } else {
            if (networkPolicyFlag == -1) {
                setOrAddNetworkAccessList(policyLists, 1, 1);
                setNetworkPolicyFlag(2);
            } else {
                setOrAddNetworkAccessList(policyLists, 1, 0);
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
        ArrayList<String> policyLists = new ArrayList<>();
        try {
            policyLists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set white domain policy Of networkList ArrayIndexOutOfBoundsException");
        }
        if (!isValidDomainAddress(policyLists)) {
            throw new IllegalArgumentException("invalid domain name");
        } else if (existOppositeNetworkList(0, networkPolicyFlag)) {
            return false;
        } else {
            if (networkPolicyFlag == -1) {
                List<String> localhostIps = new ArrayList<>();
                localhostIps.add(LOCAL_HOST);
                setOrAddNetworkAccessList(localhostIps, 0, 1);
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
        ArrayList<String> policyLists = new ArrayList<>();
        try {
            policyLists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "set black domain policy of networkList ArrayIndexOutOfBoundsException");
        }
        if (!isValidDomainAddress(policyLists)) {
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
            ArrayList<String> policyValueLists = null;
            try {
                policyValueLists = policyData.getStringArrayList("value");
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "remove ip policy of networkList ArrayIndexOutOfBoundsException");
            }
            if (policyValueLists == null || policyValueLists.isEmpty()) {
                return false;
            }
            if (!isValidIpAddress(policyValueLists)) {
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
            ArrayList<String> policyLists = new ArrayList<>();
            try {
                policyLists = policyData.getStringArrayList("value");
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "remove domain policy of networkList ArrayIndexOutOfBoundsException");
            }
            if (policyLists == null || policyLists.isEmpty()) {
                return false;
            }
            if (!isValidDomainAddress(policyLists)) {
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
            List<String> localhostIps = new ArrayList<>();
            localhostIps.add(LOCAL_HOST);
            setOrAddNetworkAccessList(localhostIps, 0, 1);
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

    private boolean isValidIpAddress(ArrayList<String> addrLists) {
        if (addrLists == null || addrLists.isEmpty()) {
            return false;
        }
        Pattern ipv4Pattern = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)(\\/\\d{1,2})?$");
        Pattern ipv6StdPattern = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
        Pattern ipv6HexCompressedPattern = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");
        Iterator<String> it = addrLists.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (!ipv4Pattern.matcher(str).matches() && !ipv6StdPattern.matcher(str).matches() && !ipv6HexCompressedPattern.matcher(str).matches()) {
                return false;
            }
        }
        HwLog.d(TAG, "isValidIpAddress input valid ");
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0014  */
    private boolean isValidDomainAddress(ArrayList<String> addrLists) {
        if (addrLists == null || addrLists.isEmpty()) {
            return false;
        }
        Iterator<String> it = addrLists.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (TextUtils.isEmpty(str) || str.length() > DOMAIN_MAX_LENGTH) {
                return false;
            }
            while (it.hasNext()) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<String> getPolicyArrayList(String policyName) {
        Bundle policyData = this.mPolicyStruct.getPolicyItem(policyName).combineAllAttributes();
        if (policyData == null || policyData.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> policies = new ArrayList<>();
        try {
            policies = policyData.getStringArrayList("value");
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
        ArrayList<String> policyValues = null;
        try {
            policyValues = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "check policy data ArrayIndexOutOfBoundsException");
        }
        if (policyValues == null || policyValues.isEmpty()) {
            return false;
        }
        if (checkPolicySize(policyName, policyValues, maxSize)) {
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

    private void setNetworkAppAndResetFlag() {
        setOrRemoveAppNetworkAccessList(null, 2, 1);
        setNetworkAppPolicyFlag(1);
        unRegisterNetworkAppAddRecevier();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setOrRemoveAppNetworkAccessList(List<String> packageNameList, int trustOrBlock, int addorRemove) {
        IBinder binder = ServiceManager.getService("network_management");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                data.writeStringList(packageNameList);
                data.writeInt(trustOrBlock);
                data.writeInt(addorRemove);
                binder.transact(CODE_SET_APP_NETWORK_ACCESS_LIST, data, reply, 0);
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

    /* access modifiers changed from: private */
    public class RegisterAppAddOrRemoveReceiver extends BroadcastReceiver {
        private RegisterAppAddOrRemoveReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !TextUtils.equals(intent.getAction(), "android.intent.action.PACKAGE_ADDED")) {
                HwLog.e(DeviceNetworkPlugin.TAG, "intent error!");
                return;
            }
            String addOrRemovePkg = intent.getDataString();
            List<String> trustOrBlockList = DeviceNetworkPlugin.this.getPolicyArrayList(DeviceNetworkPlugin.POLICY_NETWORK_TRUST_APP_LIST);
            if (trustOrBlockList == null || trustOrBlockList.isEmpty()) {
                String str = DeviceNetworkPlugin.TAG;
                HwLog.i(str, addOrRemovePkg + " add sucess, app trust list is null");
            } else if (!TextUtils.isEmpty(addOrRemovePkg)) {
                String addOrRemovePkg2 = addOrRemovePkg.replace("package:", DeviceSettingsPlugin.EMPTY_STRING);
                String str2 = DeviceNetworkPlugin.TAG;
                HwLog.i(str2, addOrRemovePkg2 + " add sucess, app trust list" + trustOrBlockList.toString());
                if (trustOrBlockList.contains(addOrRemovePkg2)) {
                    List<String> addPkgList = new ArrayList<>();
                    addPkgList.add(addOrRemovePkg2);
                    DeviceNetworkPlugin.this.setOrRemoveAppNetworkAccessList(addPkgList, 2, 0);
                    return;
                }
                String str3 = DeviceNetworkPlugin.TAG;
                HwLog.i(str3, addOrRemovePkg2 + " add sucess, but app trust list is not contains");
            }
        }
    }
}
