package com.android.server.net;

import android.app.IActivityManager;
import android.content.Context;
import android.net.INetworkStatsService;
import android.os.Environment;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.rms.iaware.dev.SceneInfo;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private static final int HW_RULE_ALL_ACCESS = 0;
    private static final int HW_RULE_MOBILE_RESTRICT = 1;
    private static final int HW_RULE_WIFI_RESTRICT = 2;
    static final String TAG = "HwNetworkPolicy";
    private static final String TAG_APP_POLICY = "app-policy";
    private static final String TAG_NETWORK_POLICY = "network-policy";
    private static final String TAG_POLICY_LIST = "policy-list";
    private static final String TAG_UID_POLICY = "uid-policy";
    private static final int VERSION_INIT = 1;
    private static final int VERSION_LATEST = 1;
    final Context mContext;
    final AtomicFile mHwPolicyFile;
    final SparseIntArray mHwUidPolicy = new SparseIntArray();
    final SparseArray<String> mHwUidPolicyWriters = new SparseArray();
    final SparseIntArray mHwUidRules = new SparseIntArray();
    private boolean mIsRoaming = false;
    final Object mRulesLock = new Object();
    private int sIncreaseCmdCount = 0;

    public HwNetworkPolicyManagerService(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
        super(context, activityManager, networkStats, networkManagement);
        this.mContext = context;
        this.mHwPolicyFile = new AtomicFile(new File(getHwSystemDir(), "hwnetpolicy.xml"));
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
        if (code == HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE) {
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
        } else if (code == WifiProCommonUtils.HTTP_REACHALBE_GOOLE) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            getHwUidsWithPolicy(data, reply);
            return true;
        } else if (code == 205) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            forceUpdatePolicyLocked(data);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
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
            policy |= oldPolicy;
            if (oldPolicy != policy) {
                setHwUidPolicyUncheckedLocked(uid, policy, opPackageName, true);
            }
        }
    }

    public void removeHwUidPolicy(Parcel data) {
        removeHwUidPolicy(data.readInt(), data.readInt(), data.readString());
    }

    public void removeHwUidPolicy(int uid, int policy, String opPackageName) {
        synchronized (this.mRulesLock) {
            int oldPolicy = this.mHwUidPolicy.get(uid, 0);
            policy = oldPolicy & (~policy);
            if (oldPolicy != policy) {
                setHwUidPolicyUncheckedLocked(uid, policy, opPackageName, true);
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

    void updateHwRuleForRestrictLocked(int uid) {
        int newPolicy;
        int uidPolicy = this.mHwUidPolicy.get(uid, 0);
        boolean isMobileRestrict = (uidPolicy & 1) != 0;
        boolean isWifiRestrict = (uidPolicy & 2) != 0;
        boolean isRoamingRestrict = (uidPolicy & 4) != 0;
        if (this.mIsRoaming) {
            if (isMobileRestrict || (isRoamingRestrict ^ 1) == 0) {
                newPolicy = 1;
            } else {
                newPolicy = 0;
            }
        } else if (isMobileRestrict) {
            newPolicy = 1;
        } else {
            newPolicy = 0;
        }
        if (isWifiRestrict) {
            newPolicy |= 2;
        } else {
            newPolicy &= -3;
        }
        int oldPolicy = this.mHwUidRules.get(uid, 0);
        if (newPolicy != oldPolicy) {
            if (newPolicy == 0) {
                this.mHwUidRules.delete(uid);
            } else {
                this.mHwUidRules.put(uid, newPolicy);
            }
            IBinder networkManager = ServiceManager.getService("network_management");
            if (networkManager != null) {
                if ((newPolicy & 1) != (oldPolicy & 1)) {
                    setHwNetworkRestrict(uid, (newPolicy & 1) != 0, true, networkManager);
                }
                if ((newPolicy & 2) != (oldPolicy & 2)) {
                    setHwNetworkRestrict(uid, (newPolicy & 2) != 0, false, networkManager);
                }
            }
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setHwNetworkRestrict(int uid, boolean isRestrict, boolean isMobileNetwork, IBinder networkManager) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String cmd = "bandwidth";
        String[] args = new String[5];
        args[0] = "firewall";
        args[1] = isRestrict ? "block" : SceneInfo.ITEM_RULE_ALLOW;
        args[2] = isMobileNetwork ? "mobile" : "wifi";
        args[3] = String.valueOf(uid);
        args[4] = String.valueOf(this.sIncreaseCmdCount);
        try {
            data.writeString(cmd);
            data.writeArray(args);
            networkManager.transact(HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE, data, reply, 0);
            this.sIncreaseCmdCount++;
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
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
    }

    public void writeHwPolicyLocked() {
        Slog.i(TAG, "writeHwPolicyLocked");
        FileOutputStream fos = null;
        try {
            fos = this.mHwPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, 1);
            for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
                int uid = this.mHwUidPolicy.keyAt(i);
                int policy = this.mHwUidPolicy.valueAt(i);
                String opPackageName = (String) this.mHwUidPolicyWriters.get(uid);
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
            if (fos != null) {
                this.mHwPolicyFile.failWrite(fos);
            }
        }
    }

    public void factoryReset(String subscriber) {
        super.factoryReset(subscriber);
        for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
            setUidPolicy(this.mHwUidPolicy.keyAt(i), 0);
        }
    }

    protected void readPolicyAL() {
        super.readPolicyAL();
        Slog.i(TAG, "readHwPolicyLocked");
        this.mHwUidPolicy.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mHwPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            loop0:
            while (true) {
                int type = in.next();
                if (type == 1) {
                    break loop0;
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
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public void forceUpdatePolicyLocked(Parcel data) {
        boolean z = true;
        synchronized (this.mRulesLock) {
            if (data.readInt() != 1) {
                z = false;
            }
            this.mIsRoaming = z;
            for (int i = 0; i < this.mHwUidPolicy.size(); i++) {
                updateHwRuleForRestrictLocked(this.mHwUidPolicy.keyAt(i));
            }
        }
    }
}
