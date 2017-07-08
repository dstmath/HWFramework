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
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
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
    final SparseIntArray mHwUidPolicy;
    final SparseIntArray mHwUidRules;
    private boolean mIsRoaming;
    final Object mRulesLock;
    private int sIncreaseCmdCount;

    public void removeHwUidPolicy(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.net.HwNetworkPolicyManagerService.removeHwUidPolicy(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.HwNetworkPolicyManagerService.removeHwUidPolicy(int, int):void");
    }

    public HwNetworkPolicyManagerService(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
        super(context, activityManager, powerManager, networkStats, networkManagement);
        this.mRulesLock = new Object();
        this.mHwUidPolicy = new SparseIntArray();
        this.mHwUidRules = new SparseIntArray();
        this.sIncreaseCmdCount = HW_RULE_ALL_ACCESS;
        this.mIsRoaming = false;
        this.mContext = context;
        this.mHwPolicyFile = new AtomicFile(new File(getHwSystemDir(), "hwnetpolicy.xml"));
    }

    static File getHwSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Slog.i(TAG, "onTransact, code = " + code);
        if (code == WifiProCommonUtils.HTTP_REACHALBE_HOME) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            setHwUidPolicy(data);
            return true;
        }
        if (code == HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            addHwUidPolicy(data);
        } else if (code == GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            removeHwUidPolicy(data);
            return true;
        } else if (code == GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            getHwUidPolicy(data, reply);
            return true;
        } else if (code == WifiProCommonUtils.HTTP_REACHALBE_GOOLE) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            getHwUidsWithPolicy(data, reply);
            return true;
        } else if (code == GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            forceUpdatePolicyLocked(data);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    public void setHwUidPolicy(Parcel data) {
        setHwUidPolicy(data.readInt(), data.readInt());
    }

    public void setHwUidPolicy(int uid, int policy) {
        synchronized (this.mRulesLock) {
            if (this.mHwUidPolicy.get(uid, HW_RULE_ALL_ACCESS) != policy) {
                setHwUidPolicyUncheckedLocked(uid, policy, true);
            }
        }
    }

    public void setHwUidPolicyUncheckedLocked(int uid, int policy, boolean persist) {
        this.mHwUidPolicy.put(uid, policy);
        updateHwRuleForRestrictLocked(uid);
        if (persist) {
            writeHwPolicyLocked();
        }
    }

    public void addHwUidPolicy(Parcel data) {
        addHwUidPolicy(data.readInt(), data.readInt());
    }

    public void addHwUidPolicy(int uid, int policy) {
        synchronized (this.mRulesLock) {
            int oldPolicy = this.mHwUidPolicy.get(uid, HW_RULE_ALL_ACCESS);
            policy |= oldPolicy;
            if (oldPolicy != policy) {
                setHwUidPolicyUncheckedLocked(uid, policy, true);
            }
        }
    }

    public void removeHwUidPolicy(Parcel data) {
        removeHwUidPolicy(data.readInt(), data.readInt());
    }

    public void getHwUidPolicy(Parcel data, Parcel reply) {
        reply.writeInt(getHwUidPolicy(data.readInt()));
    }

    public int getHwUidPolicy(int uid) {
        int i;
        synchronized (this.mRulesLock) {
            i = this.mHwUidPolicy.get(uid, HW_RULE_ALL_ACCESS);
        }
        return i;
    }

    public void getHwUidsWithPolicy(Parcel data, Parcel reply) {
        int[] uids = getHwUidsWithPolicy(data.readInt());
        reply.writeInt(uids.length);
        reply.writeIntArray(uids);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int[] getHwUidsWithPolicy(int policy) {
        int[] uids = new int[HW_RULE_ALL_ACCESS];
        synchronized (this.mRulesLock) {
            int i = HW_RULE_ALL_ACCESS;
            while (true) {
                if (i < this.mHwUidPolicy.size()) {
                    int uid = this.mHwUidPolicy.keyAt(i);
                    if ((this.mHwUidPolicy.valueAt(i) & policy) != 0) {
                        uids = ArrayUtils.appendInt(uids, uid);
                    }
                    i += VERSION_LATEST;
                }
            }
        }
        return uids;
    }

    void updateHwRuleForRestrictLocked(int uid) {
        int newPolicy;
        int uidPolicy = this.mHwUidPolicy.get(uid, HW_RULE_ALL_ACCESS);
        boolean isMobileRestrict = (uidPolicy & VERSION_LATEST) != 0;
        boolean isWifiRestrict = (uidPolicy & HW_RULE_WIFI_RESTRICT) != 0;
        boolean isRoamingRestrict = (uidPolicy & 4) != 0;
        if (this.mIsRoaming) {
            if (isMobileRestrict || isRoamingRestrict) {
                newPolicy = VERSION_LATEST;
            } else {
                newPolicy = HW_RULE_ALL_ACCESS;
            }
        } else if (isMobileRestrict) {
            newPolicy = VERSION_LATEST;
        } else {
            newPolicy = HW_RULE_ALL_ACCESS;
        }
        if (isWifiRestrict) {
            newPolicy |= HW_RULE_WIFI_RESTRICT;
        } else {
            newPolicy &= -3;
        }
        int oldPolicy = this.mHwUidRules.get(uid, HW_RULE_ALL_ACCESS);
        if (newPolicy != oldPolicy) {
            if (newPolicy == 0) {
                this.mHwUidRules.delete(uid);
            } else {
                this.mHwUidRules.put(uid, newPolicy);
            }
            IBinder networkManager = ServiceManager.getService("network_management");
            if (networkManager != null) {
                if ((newPolicy & VERSION_LATEST) != (oldPolicy & VERSION_LATEST)) {
                    setHwNetworkRestrict(uid, (newPolicy & VERSION_LATEST) != 0, true, networkManager);
                }
                if ((newPolicy & HW_RULE_WIFI_RESTRICT) != (oldPolicy & HW_RULE_WIFI_RESTRICT)) {
                    boolean ruleRestrict;
                    if ((newPolicy & HW_RULE_WIFI_RESTRICT) != 0) {
                        ruleRestrict = true;
                    } else {
                        ruleRestrict = false;
                    }
                    setHwNetworkRestrict(uid, ruleRestrict, false, networkManager);
                }
            }
        }
    }

    private void setHwNetworkRestrict(int uid, boolean isRestrict, boolean isMobileNetwork, IBinder networkManager) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String cmd = "bandwidth";
        String[] args = new String[5];
        args[HW_RULE_ALL_ACCESS] = "firewall";
        args[VERSION_LATEST] = isRestrict ? "block" : "allow";
        args[HW_RULE_WIFI_RESTRICT] = isMobileNetwork ? "mobile" : GnssConnectivityLogManager.SUBSYS_WIFI;
        args[3] = String.valueOf(uid);
        args[4] = String.valueOf(this.sIncreaseCmdCount);
        try {
            data.writeString(cmd);
            data.writeArray(args);
            networkManager.transact(HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE, data, reply, HW_RULE_ALL_ACCESS);
            this.sIncreaseCmdCount += VERSION_LATEST;
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
        }
    }

    public void writeHwPolicyLocked() {
        Slog.i(TAG, "writeHwPolicyLocked");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mHwPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, VERSION_LATEST);
            for (int i = HW_RULE_ALL_ACCESS; i < this.mHwUidPolicy.size(); i += VERSION_LATEST) {
                int uid = this.mHwUidPolicy.keyAt(i);
                int policy = this.mHwUidPolicy.valueAt(i);
                if (policy != 0) {
                    out.startTag(null, TAG_UID_POLICY);
                    XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                    XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy);
                    out.endTag(null, TAG_UID_POLICY);
                }
            }
            out.endTag(null, TAG_POLICY_LIST);
            out.endDocument();
            this.mHwPolicyFile.finishWrite(fileOutputStream);
        } catch (IOException e) {
            if (fileOutputStream != null) {
                this.mHwPolicyFile.failWrite(fileOutputStream);
            }
        }
    }

    public void factoryReset(String subscriber) {
        super.factoryReset(subscriber);
        for (int i = HW_RULE_ALL_ACCESS; i < this.mHwUidPolicy.size(); i += VERSION_LATEST) {
            setUidPolicy(this.mHwUidPolicy.keyAt(i), HW_RULE_ALL_ACCESS);
        }
    }

    protected void readPolicyLocked() {
        super.readPolicyLocked();
        Slog.i(TAG, "readHwPolicyLocked");
        this.mHwUidPolicy.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mHwPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            while (true) {
                int type = in.next();
                if (type == VERSION_LATEST) {
                    break;
                }
                String tag = in.getName();
                if (type == HW_RULE_WIFI_RESTRICT && TAG_UID_POLICY.equals(tag)) {
                    setHwUidPolicyUncheckedLocked(XmlUtils.readIntAttribute(in, ATTR_UID), XmlUtils.readIntAttribute(in, ATTR_POLICY), false);
                }
            }
        } catch (FileNotFoundException e) {
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
            if (data.readInt() != VERSION_LATEST) {
                z = false;
            }
            this.mIsRoaming = z;
            for (int i = HW_RULE_ALL_ACCESS; i < this.mHwUidPolicy.size(); i += VERSION_LATEST) {
                updateHwRuleForRestrictLocked(this.mHwUidPolicy.keyAt(i));
            }
        }
    }
}
