package com.android.server.net;

import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwNetworkPolicyManagerService extends NetworkPolicyManagerService {
    private static final String ATTR_OPPACKAGENAME = "packagename";
    private static final String ATTR_POLICY = "policy";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_VERSION = "version";
    private static final int HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE = 201;
    private static final String HW_CONNECTIVITY_ACTION = "huawei.net.conn.HW_CONNECTIVITY_CHANGE";
    private static final int HW_RULE_ALL_ACCESS = 0;
    private static final int HW_RULE_MOBILE_RESTRICT = 1;
    private static final int HW_RULE_WIFI_RESTRICT = 2;
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    static final String TAG = "HwNetworkPolicy";
    private static final String TAG_APP_POLICY = "app-policy";
    private static final String TAG_NETWORK_POLICY = "network-policy";
    private static final String TAG_POLICY_LIST = "policy-list";
    private static final String TAG_UID_POLICY = "uid-policy";
    private static final int TRANSCODE_IS_RESTRICTED_BY_HAWARE = 1003;
    private static final int TRANSCODE_UPDATE_HAWARE_RULE = 1002;
    private static final String VERIZON_ICCID_PREFIX = "891480";
    private static final int VERSION_INIT = 1;
    private static final int VERSION_LATEST = 1;
    final Context mContext;
    private HawarePolicy mHawarePolicy;
    final AtomicFile mHwPolicyFile;
    final SparseIntArray mHwUidPolicy = new SparseIntArray();
    final SparseArray<String> mHwUidPolicyWriters = new SparseArray<>();
    final SparseIntArray mHwUidRules = new SparseIntArray();
    private boolean mIsRoaming = false;
    private final Object mRulesLock = new Object();
    private BroadcastReceiver netReceiver = new IntenterBoradCastReceiver();

    public HwNetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkManagementService networkManagement) {
        super(context, activityManager, networkManagement);
        this.mContext = context;
        this.mHwPolicyFile = new AtomicFile(new File(getHwSystemDir(), "hwnetpolicy.xml"));
        initRegisterReceiver();
        this.mHawarePolicy = new HawarePolicy();
        LocalServices.addService(HwNetworkPolicyManagerInternal.class, new HwNetworkPolicyManagerInternalImpl());
    }

    static File getHwSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Slog.i(TAG, "onTransact, code = " + code);
        if (code == 200) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            setHwUidPolicy(data);
            return true;
        }
        if (code == 201) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            addHwUidPolicy(data);
        } else if (code == 202) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            removeHwUidPolicy(data);
            return true;
        } else if (code == 203) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            getHwUidPolicy(data, reply);
            return true;
        } else if (code == 204) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            getHwUidsWithPolicy(data, reply);
            return true;
        } else if (code == 205) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            forceUpdatePolicyLocked(data);
            return true;
        } else if (code == 1002) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            boolean isSuccess = this.mHawarePolicy.setHawareRule(data.readInt(), data.createIntArray(), data.createIntArray());
            reply.writeNoException();
            reply.writeBoolean(isSuccess);
            return true;
        } else if (code == 1003) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            boolean isRestrcted = this.mHawarePolicy.isRestrictedByHaware(data.readInt(), data.readInt());
            reply.writeNoException();
            reply.writeBoolean(isRestrcted);
            return true;
        }
        return HwNetworkPolicyManagerService.super.onTransact(code, data, reply, flags);
    }

    public void setHwUidPolicy(Parcel data) {
        setHwUidPolicy(data.readInt(), data.readInt(), data.readString());
    }

    public void setHwUidPolicy(int uid, int policy, String opPackageName) {
        synchronized (this.mRulesLock) {
            if (this.mHwUidPolicy.get(uid, 0) != policy) {
                setHwUidPolicyUncheckedLocked(uid, policy, opPackageName, true);
            }
        }
    }

    public void setHwUidPolicyUncheckedLocked(int uid, int policy, String opPackageName, boolean persist) {
        this.mHwUidPolicy.put(uid, policy);
        this.mHwUidPolicyWriters.put(uid, opPackageName);
        updateHwRuleForRestrictLocked(uid);
        if (persist) {
            writeHwPolicyLocked();
        }
    }

    public void addHwUidPolicy(Parcel data) {
        addHwUidPolicy(data.readInt(), data.readInt(), data.readString());
    }

    public void addHwUidPolicy(int uid, int policy, String opPackageName) {
        synchronized (this.mRulesLock) {
            int oldPolicy = this.mHwUidPolicy.get(uid, 0);
            int policy2 = policy | oldPolicy;
            if (oldPolicy != policy2) {
                setHwUidPolicyUncheckedLocked(uid, policy2, opPackageName, true);
            }
        }
    }

    public void removeHwUidPolicy(Parcel data) {
        removeHwUidPolicy(data.readInt(), data.readInt(), data.readString());
    }

    public void removeHwUidPolicy(int uid, int policy, String opPackageName) {
        synchronized (this.mRulesLock) {
            int oldPolicy = this.mHwUidPolicy.get(uid, 0);
            int policy2 = oldPolicy & (~policy);
            if (oldPolicy != policy2) {
                setHwUidPolicyUncheckedLocked(uid, policy2, opPackageName, true);
            }
        }
    }

    public void getHwUidPolicy(Parcel data, Parcel reply) {
        reply.writeInt(getHwUidPolicy(data.readInt()));
    }

    public int getHwUidPolicy(int uid) {
        int i;
        synchronized (this.mRulesLock) {
            i = this.mHwUidPolicy.get(uid, 0);
        }
        return i;
    }

    public void getHwUidsWithPolicy(Parcel data, Parcel reply) {
        int[] uids = getHwUidsWithPolicy(data.readInt());
        reply.writeInt(uids.length);
        reply.writeIntArray(uids);
    }

    public int[] getHwUidsWithPolicy(int policy) {
        int[] uids = new int[0];
        synchronized (this.mRulesLock) {
            for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
                int uid = this.mHwUidPolicy.keyAt(i);
                if ((this.mHwUidPolicy.valueAt(i) & policy) != 0) {
                    uids = ArrayUtils.appendInt(uids, uid);
                }
            }
        }
        return uids;
    }

    /* access modifiers changed from: package-private */
    public void updateHwRuleForRestrictLocked(int uid) {
        int newPolicy;
        int newPolicy2;
        int uidPolicy = this.mHwUidPolicy.get(uid, 0);
        boolean ruleRestrict = true;
        boolean isMobileRestrict = (uidPolicy & 1) != 0;
        boolean isWifiRestrict = (uidPolicy & 2) != 0;
        boolean isRoamingRestrict = (uidPolicy & 4) != 0;
        if (this.mIsRoaming) {
            if (isMobileRestrict || isRoamingRestrict) {
                newPolicy = 0 | 1;
            } else {
                newPolicy = 0 & -2;
            }
        } else if (isMobileRestrict) {
            newPolicy = 0 | 1;
        } else {
            newPolicy = 0 & -2;
        }
        if (isWifiRestrict) {
            newPolicy2 = newPolicy | 2;
        } else {
            newPolicy2 = newPolicy & -3;
        }
        int oldPolicy = this.mHwUidRules.get(uid, 0);
        if (newPolicy2 != oldPolicy) {
            if (newPolicy2 == 0) {
                this.mHwUidRules.delete(uid);
            } else {
                this.mHwUidRules.put(uid, newPolicy2);
            }
            IBinder networkManager = ServiceManager.getService("network_management");
            if (networkManager != null) {
                if ((newPolicy2 & 1) != (oldPolicy & 1)) {
                    setHwNetworkRestrict(uid, (newPolicy2 & 1) != 0, true, networkManager);
                }
                if ((newPolicy2 & 2) != (oldPolicy & 2)) {
                    if ((newPolicy2 & 2) == 0) {
                        ruleRestrict = false;
                    }
                    setHwNetworkRestrict(uid, ruleRestrict, false, networkManager);
                }
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0016: APUT  
      (r3v1 'args' java.lang.String[] A[D('args' java.lang.String[])])
      (0 ??[int, short, byte, char])
      (r4v0 java.lang.String)
     */
    private void setHwNetworkRestrict(int uid, boolean isRestrict, boolean isMobileNetwork, IBinder networkManager) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String[] args = new String[3];
        args[0] = isMobileNetwork ? "mobile" : "wifi";
        args[1] = isRestrict ? "block" : "allow";
        args[2] = String.valueOf(uid);
        try {
            data.writeString("firewall");
            data.writeArray(args);
            networkManager.transact(201, data, reply, 0);
            data.recycle();
            if (reply == null) {
                return;
            }
        } catch (Exception e) {
            Log.e(TAG_NETWORK_POLICY, "setHwNetworkRestrict failed");
            if (data != null) {
                data.recycle();
            }
            if (reply == null) {
                return;
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            throw th;
        }
        reply.recycle();
    }

    public void writeHwPolicyLocked() {
        Slog.i(TAG, "writeHwPolicyLocked");
        try {
            FileOutputStream fos = this.mHwPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, "version", 1);
            for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
                int uid = this.mHwUidPolicy.keyAt(i);
                int policy = this.mHwUidPolicy.valueAt(i);
                String opPackageName = this.mHwUidPolicyWriters.get(uid);
                if (policy != 0) {
                    out.startTag(null, TAG_UID_POLICY);
                    XmlUtils.writeIntAttribute(out, "uid", uid);
                    XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy);
                    XmlUtils.writeStringAttribute(out, ATTR_OPPACKAGENAME, opPackageName);
                    out.endTag(null, TAG_UID_POLICY);
                }
            }
            out.endTag(null, TAG_POLICY_LIST);
            out.endDocument();
            this.mHwPolicyFile.finishWrite(fos);
        } catch (IOException e) {
            if (0 != 0) {
                this.mHwPolicyFile.failWrite(null);
            }
        }
    }

    public void factoryReset(String subscriber) {
        HwNetworkPolicyManagerService.super.factoryReset(subscriber);
        for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
            setUidPolicy(this.mHwUidPolicy.keyAt(i), 0);
        }
    }

    /* access modifiers changed from: protected */
    public void readPolicyAL() {
        HwNetworkPolicyManagerService.super.readPolicyAL();
        Slog.i(TAG, "readHwPolicyLocked");
        this.mHwUidPolicy.clear();
        FileInputStream fis = null;
        try {
            fis = this.mHwPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(fis, StandardCharsets.UTF_8.name());
            while (true) {
                int type = in.next();
                if (type == 1) {
                    break;
                }
                String tag = in.getName();
                if (type == 2 && TAG_UID_POLICY.equals(tag)) {
                    setHwUidPolicyUncheckedLocked(XmlUtils.readIntAttribute(in, "uid"), XmlUtils.readIntAttribute(in, ATTR_POLICY), XmlUtils.readStringAttribute(in, ATTR_OPPACKAGENAME), false);
                }
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "problem reading hw network policy, file not found");
        } catch (IOException e2) {
            Slog.e(TAG, "problem reading hw network policy" + e2);
        } catch (XmlPullParserException e3) {
            Slog.e(TAG, "problem reading hw network policy" + e3);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    public void forceUpdatePolicyLocked(Parcel data) {
        synchronized (this.mRulesLock) {
            boolean z = true;
            if (data.readInt() != 1) {
                z = false;
            }
            this.mIsRoaming = z;
            for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
                updateHwRuleForRestrictLocked(this.mHwUidPolicy.keyAt(i));
            }
        }
    }

    private void initRegisterReceiver() {
        Log.i(TAG_NETWORK_POLICY, "initRegisterReceiver " + this.mContext);
        if (this.mContext != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HW_CONNECTIVITY_ACTION);
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            this.mContext.registerReceiver(this.netReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateInterfaceWhiteList(Context context, String itfName) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService("connectivity");
        Set<String> need2UpdateIntfList = new HashSet<>();
        Set<String> allIntfList = new HashSet<>();
        Network[] networks = connManager.getAllNetworks();
        for (int i = 0; i < networks.length; i++) {
            NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(networks[i]);
            LinkProperties linkProperties = connManager.getLinkProperties(networks[i]);
            NetworkInfo networkInfo = connManager.getNetworkInfo(networks[i]);
            if (networkInfo != null && networkInfo.isConnected()) {
                String intfName = linkProperties.getInterfaceName();
                allIntfList.add(intfName);
                if (networkCapabilities.hasCapability(11)) {
                    need2UpdateIntfList.add(intfName);
                }
            }
        }
        if (itfName != null) {
            allIntfList.add(itfName);
        }
        Log.d(TAG_NETWORK_POLICY, "allIntfList " + allIntfList);
        Log.d(TAG_NETWORK_POLICY, "need2UpdateIntfList " + need2UpdateIntfList);
        for (String intfName2 : allIntfList) {
            setMeteredInterface(intfName2, true);
        }
        for (String intfName3 : need2UpdateIntfList) {
            setMeteredInterface(intfName3, false);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x002c: APUT  
      (r5v1 'args' java.lang.String[] A[D('args' java.lang.String[])])
      (1 ??[boolean, int, float, short, byte, char])
      (r8v0 java.lang.String)
     */
    private void setMeteredInterface(String itfName, boolean isMetered) {
        if (itfName != null) {
            IBinder networkManager = ServiceManager.getService("network_management");
            if (networkManager == null) {
                Log.e(TAG_NETWORK_POLICY, "setIfWhitelist networkManager is null");
                return;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String[] args = new String[2];
            args[0] = itfName;
            args[1] = !isMetered ? "enable_ifwhitelist" : "disable_ifwhitelist";
            try {
                data.writeString("ifwhitelist");
                data.writeArray(args);
                networkManager.transact(201, data, reply, 0);
                Slog.i(TAG_NETWORK_POLICY, "" + reply);
                data.recycle();
                if (reply == null) {
                    return;
                }
            } catch (Exception e) {
                Slog.e(TAG, "setIfWhitelist--> fail");
                if (data != null) {
                    data.recycle();
                }
                if (reply == null) {
                    return;
                }
            } catch (Throwable th) {
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
                throw th;
            }
            reply.recycle();
        }
    }

    private class IntenterBoradCastReceiver extends BroadcastReceiver {
        private IntenterBoradCastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwNetworkPolicyManagerService.this.isMatchedOperator() && intent != null) {
                String action = intent.getAction();
                Log.d(HwNetworkPolicyManagerService.TAG_NETWORK_POLICY, "onReceive " + action);
                if (HwNetworkPolicyManagerService.HW_CONNECTIVITY_ACTION.equals(action)) {
                    HwNetworkPolicyManagerService.this.updateInterfaceWhiteList(context, intent.getStringExtra("intfName"));
                }
                if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    HwNetworkPolicyManagerService.this.updateInterfaceWhiteList(context, null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMatchedOperator() {
        String iccid = "" + ((TelephonyManager) this.mContext.getSystemService("phone")).getSimSerialNumber();
        if (!HW_SIM_ACTIVATION || !iccid.startsWith(VERIZON_ICCID_PREFIX)) {
            return false;
        }
        return true;
    }

    private class HwNetworkPolicyManagerInternalImpl extends HwNetworkPolicyManagerInternal {
        private HwNetworkPolicyManagerInternalImpl() {
        }

        public boolean isRestrictedByHaware(int uid, int pid) {
            if (HwNetworkPolicyManagerService.this.mHawarePolicy != null) {
                return HwNetworkPolicyManagerService.this.mHawarePolicy.isRestrictedByHaware(uid, pid);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class HawarePolicy {
        private static final int ALLOW = 1;
        private static final boolean DEBUG = Log.isLoggable(HwNetworkPolicyManagerService.TAG, 3);
        private static final int DENY = 2;
        private static final int DISABLE = 1;
        private static final int ENABLE = 0;
        private static final int ENABLE_POWER_SAVE_CHANIN = 6;
        private static final int OP_CHAIN = 0;
        private static final int REPLACE_PID_BLACKLIST_RULE = 4;
        private static final int REPLACE_UID_BLACKLIST_RULE = 2;
        private static final int SET_PID_BLACKLIST_RULE = 3;
        private static final int SET_POWER_SAVE_RULE = 7;
        private static final int SET_UID_BLACKLIST_RULE = 1;
        private static final int UPDATE_GMS_UIDS = 5;
        private boolean mEnablePowerSave;
        private Object mLock;
        private HashSet<Integer> mPidBlackList;
        private HashSet<Integer> mUidBlackList;
        private HashSet<Integer> mUidWhiteList;

        private HawarePolicy() {
            this.mUidBlackList = new HashSet<>();
            this.mPidBlackList = new HashSet<>();
            this.mUidWhiteList = new HashSet<>();
            this.mEnablePowerSave = false;
            this.mLock = new Object();
        }

        /* access modifiers changed from: package-private */
        public boolean isRestrictedByHaware(int uid, int pid) {
            synchronized (this.mLock) {
                if (this.mUidBlackList.contains(Integer.valueOf(uid))) {
                    if (DEBUG) {
                        Log.d(HwNetworkPolicyManagerService.TAG, "restrict by uid black, uid : " + uid + " pid : " + pid);
                    }
                    return true;
                } else if (this.mPidBlackList.contains(Integer.valueOf(pid))) {
                    if (DEBUG) {
                        Log.d(HwNetworkPolicyManagerService.TAG, "restrict by pid black, uid : " + uid + " pid : " + pid);
                    }
                    return true;
                } else if (!this.mEnablePowerSave || uid < 10000 || this.mUidWhiteList.contains(Integer.valueOf(uid))) {
                    return false;
                } else {
                    if (DEBUG) {
                        Log.d(HwNetworkPolicyManagerService.TAG, "restrict by uid white, uid : " + uid + " pid : " + pid);
                    }
                    return true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean setHawareRule(int cmd, int[] keys, int[] values) {
            if (Binder.getCallingUid() != 1000) {
                Log.e(HwNetworkPolicyManagerService.TAG, "setHAwareRule permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            }
            synchronized (this.mLock) {
                if (DEBUG) {
                    Log.d(HwNetworkPolicyManagerService.TAG, "setHAwareRule cmd : " + cmd + " keys : " + Arrays.toString(keys) + " values : " + Arrays.toString(values));
                }
                updateRecord(cmd, keys, values);
            }
            return true;
        }

        private void updateRecord(int cmd, int[] keys, int[] values) {
            if (keys != null && values != null) {
                boolean z = true;
                if (cmd == 1) {
                    updateRecord(keys, values, this.mUidBlackList, true, false);
                } else if (cmd == 2) {
                    updateRecord(keys, values, this.mUidBlackList, true, true);
                } else if (cmd == 3) {
                    updateRecord(keys, values, this.mPidBlackList, true, false);
                } else if (cmd == 4) {
                    updateRecord(keys, values, this.mPidBlackList, true, true);
                } else if (cmd != 6) {
                    if (cmd == 7) {
                        updateRecord(keys, values, this.mUidWhiteList, false, false);
                    }
                } else if (keys.length > 0) {
                    if (keys[0] != 0) {
                        z = false;
                    }
                    this.mEnablePowerSave = z;
                }
            }
        }

        private void updateRecord(int[] keys, int[] values, HashSet<Integer> list, boolean isBlackList, boolean isReplace) {
            int keySize;
            if (!(list == null || keys == null || values == null || (keySize = keys.length) != values.length)) {
                if (isReplace) {
                    list.clear();
                }
                for (int i = 0; i != keySize; i++) {
                    int key = keys[i];
                    if (values[i] == 2) {
                        if (isBlackList) {
                            list.add(Integer.valueOf(key));
                        } else {
                            list.remove(Integer.valueOf(key));
                        }
                    } else if (isBlackList) {
                        list.remove(Integer.valueOf(key));
                    } else {
                        list.add(Integer.valueOf(key));
                    }
                }
            }
        }
    }
}
