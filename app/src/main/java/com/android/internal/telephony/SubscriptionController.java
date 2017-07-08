package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Telephony.GlobalMatchs;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import dalvik.system.PathClassLoader;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class SubscriptionController extends AbstractSubscriptionController {
    static final boolean DBG = true;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = false;
    static final String LOG_TAG = "SubscriptionController";
    static final int MAX_LOCAL_LOG_LINES = 500;
    static final boolean VDBG = false;
    private static int mDefaultFallbackSubId;
    private static int mDefaultPhoneId;
    private static SubscriptionController sInstance;
    protected static Phone[] sPhones;
    private static Map<Integer, Integer> sSlotIdxToSubId;
    private int[] colorArr;
    private AppOpsManager mAppOps;
    protected CallManager mCM;
    protected Context mContext;
    private ScLocalLog mLocalLog;
    protected final Object mLock;
    protected TelephonyManager mTelephonyManager;
    private Object qcRilHook;

    static class ScLocalLog {
        private LinkedList<String> mLog;

        public ScLocalLog(int maxLines) {
            this.mLog = new LinkedList();
        }

        public synchronized void log(String msg) {
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = this.mLog.listIterator(0);
            int i = 0;
            while (itr.hasNext()) {
                int i2 = i + 1;
                pw.println(Integer.toString(i) + ": " + ((String) itr.next()));
                if (i2 % 10 == 0) {
                    pw.flush();
                }
                i = i2;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SubscriptionController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SubscriptionController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.<clinit>():void");
    }

    public static SubscriptionController init(Phone phone) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionController(phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController init(Context c, CommandsInterface[] ci) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionController(c);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    protected SubscriptionController(Context c) {
        this.mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
        this.qcRilHook = null;
        this.mLock = new Object();
        init(c);
    }

    protected void init(Context c) {
        this.mContext = c;
        this.mCM = CallManager.getInstance();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        if (HwModemCapability.isCapabilitySupport(9)) {
            getQcRilHook();
        }
        logdl("[SubscriptionController] init by Context");
    }

    private boolean isSubInfoReady() {
        return sSlotIdxToSubId.size() > 0 ? DBG : IS_FAST_SWITCH_SIMSLOT;
    }

    private SubscriptionController(Phone phone) {
        this.mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
        this.qcRilHook = null;
        this.mLock = new Object();
        this.mContext = phone.getContext();
        this.mCM = CallManager.getInstance();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        if (HwModemCapability.isCapabilitySupport(9)) {
            getQcRilHook();
        }
        logdl("[SubscriptionController] init by Phone");
    }

    private boolean canReadPhoneState(String callingPackage, String message) {
        boolean z = DBG;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return DBG;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = IS_FAST_SWITCH_SIMSLOT;
            }
            return z;
        }
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void broadcastSimInfoContentChanged() {
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE"));
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
    }

    public void notifySubscriptionInfoChanged() {
        ITelephonyRegistry tr = Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifySubscriptionInfoChanged:");
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged();
    }

    private SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
        String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302555);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow(GlobalMatchs.MCC));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow(GlobalMatchs.MNC));
        String countryIso = getSubscriptionCountryIso(id);
        int status = cursor.getInt(cursor.getColumnIndexOrThrow("sub_state"));
        int nwMode = cursor.getInt(cursor.getColumnIndexOrThrow("network_mode"));
        int simProvisioningStatus = cursor.getInt(cursor.getColumnIndexOrThrow("sim_provisioning_status"));
        String line1Number = this.mTelephonyManager.getLine1Number(simSlotIndex);
        if (!(TextUtils.isEmpty(line1Number) || line1Number.equals(number))) {
            number = line1Number;
        }
        return new SubscriptionInfo(simSlotIndex, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, simProvisioningStatus, status, nwMode);
    }

    private String getSubscriptionCountryIso(int subId) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            return "";
        }
        return this.mTelephonyManager.getSimCountryIsoForPhone(phoneId);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        Throwable th;
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[]{queryKey.toString()};
        }
        List<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, null);
        if (cursor != null) {
            ArrayList<SubscriptionInfo> subList2;
            while (true) {
                ArrayList<SubscriptionInfo> subList3;
                subList2 = subList3;
                try {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList2 == null) {
                            subList3 = new ArrayList();
                        } else {
                            subList3 = subList2;
                        }
                        try {
                            subList3.add(subInfo);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        subList3 = subList2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            Object subList4 = subList2;
        } else {
            logd("Query fail");
        }
        if (cursor != null) {
            cursor.close();
        }
        return subList;
    }

    private int getUnusedColor(String callingPackage) {
        List<SubscriptionInfo> availableSubInfos = getActiveSubscriptionInfoList(callingPackage);
        this.colorArr = this.mContext.getResources().getIntArray(17235979);
        int colorIdx = 0;
        if (availableSubInfos != null) {
            int i = 0;
            while (i < this.colorArr.length) {
                int j = 0;
                while (j < availableSubInfos.size() && this.colorArr[i] != ((SubscriptionInfo) availableSubInfos.get(j)).getIconTint()) {
                    j++;
                }
                if (j == availableSubInfos.size()) {
                    return this.colorArr[i];
                }
                i++;
            }
            colorIdx = availableSubInfos.size() % this.colorArr.length;
        }
        return this.colorArr[colorIdx];
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfo")) {
            return null;
        }
        if (SubscriptionManager.isValidSubscriptionId(subId) && isSubInfoReady()) {
            long identity = Binder.clearCallingIdentity();
            try {
                List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
                if (subList != null) {
                    for (SubscriptionInfo si : subList) {
                        if (si.getSimSlotIndex() == subId) {
                            return si;
                        }
                    }
                }
                logd("[getActiveSubInfoForSubscriber]- subId=" + subId + " subList=" + subList + " subInfo=null");
                Binder.restoreCallingIdentity(identity);
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            logd("[getSubInfoUsingSubIdx]- invalid subId or not ready = " + subId);
            return null;
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForIccId")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getIccId() == iccId) {
                        logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subInfo=" + si);
                        return si;
                    }
                }
            }
            logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSimSlotIndex() == slotIdx) {
                        logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx + " subId=*");
                        return si;
                    }
                }
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx + " subId=null");
            } else {
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ subList=null");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        logd("[getAllSubInfoList]+");
        if (!canReadPhoneState(callingPackage, "getAllSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo(null, null);
            if (subList != null) {
                logd("[getAllSubInfoList]- " + subList.size() + " infos return");
            } else {
                logd("[getAllSubInfoList]- no info return");
            }
            Binder.restoreCallingIdentity(identity);
            return subList;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        List<SubscriptionInfo> list = null;
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                list = null;
                List<SubscriptionInfo> subList = getSubInfo("sim_id>=0", null);
                if (subList != null) {
                    Collections.sort(subList, new Comparator<SubscriptionInfo>() {
                        public int compare(SubscriptionInfo arg0, SubscriptionInfo arg1) {
                            int flag = arg0.getSimSlotIndex() - arg1.getSimSlotIndex();
                            if (flag == 0) {
                                return arg0.getSubscriptionId() - arg1.getSubscriptionId();
                            }
                            return flag;
                        }
                    });
                } else {
                    logdl("[getActiveSubInfoList]- no info return");
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            }
            logdl("[getActiveSubInfoList] Sub Controller not ready");
            return list;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCount(String callingPackage) {
        int i = 0;
        logd("[getActiveSubInfoCount]+");
        if (!canReadPhoneState(callingPackage, "getActiveSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (records == null) {
                logd("[getActiveSubInfoCount] records null");
                return i;
            }
            StringBuilder append = new StringBuilder().append("[getActiveSubInfoCount]- count: ");
            i = records.size();
            logd(append.append(i).toString());
            int size = records.size();
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getAllSubInfoCount(String callingPackage) {
        logd("[getAllSubInfoCount]+");
        if (!canReadPhoneState(callingPackage, "getAllSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        Cursor cursor;
        try {
            cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                logd("[getAllSubInfoCount]- " + count + " SUB(s) in DB");
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getAllSubInfoCount]- no SUB in DB");
            Binder.restoreCallingIdentity(identity);
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCountMax() {
        return this.mTelephonyManager.getSimCount();
    }

    public int addSubInfoRecord(java.lang.String r25, int r26) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int. bs: [B:2:0x0038, B:10:0x0084, B:29:0x010f]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r24 = this;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "[addSubInfoRecord]+ iccId:";
        r3 = r3.append(r4);
        r4 = android.telephony.SubscriptionInfo.givePrintableIccid(r25);
        r3 = r3.append(r4);
        r4 = " slotId:";
        r3 = r3.append(r4);
        r0 = r26;
        r3 = r3.append(r0);
        r3 = r3.toString();
        r0 = r24;
        r0.logdl(r3);
        r3 = "addSubInfoRecord";
        r0 = r24;
        r0.enforceModifyPhoneState(r3);
        r12 = android.os.Binder.clearCallingIdentity();
        if (r25 != 0) goto L_0x0045;
    L_0x0038:
        r3 = "[addSubInfoRecord]- null iccId";	 Catch:{ all -> 0x020b }
        r0 = r24;	 Catch:{ all -> 0x020b }
        r0.logdl(r3);	 Catch:{ all -> 0x020b }
        r3 = -1;
        android.os.Binder.restoreCallingIdentity(r12);
        return r3;
    L_0x0045:
        r0 = r24;	 Catch:{ all -> 0x020b }
        r3 = r0.mContext;	 Catch:{ all -> 0x020b }
        r2 = r3.getContentResolver();	 Catch:{ all -> 0x020b }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x020b }
        r4 = 3;	 Catch:{ all -> 0x020b }
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x020b }
        r5 = "_id";	 Catch:{ all -> 0x020b }
        r6 = 0;	 Catch:{ all -> 0x020b }
        r4[r6] = r5;	 Catch:{ all -> 0x020b }
        r5 = "sim_id";	 Catch:{ all -> 0x020b }
        r6 = 1;	 Catch:{ all -> 0x020b }
        r4[r6] = r5;	 Catch:{ all -> 0x020b }
        r5 = "name_source";	 Catch:{ all -> 0x020b }
        r6 = 2;	 Catch:{ all -> 0x020b }
        r4[r6] = r5;	 Catch:{ all -> 0x020b }
        r5 = "icc_id=?";	 Catch:{ all -> 0x020b }
        r6 = 1;	 Catch:{ all -> 0x020b }
        r6 = new java.lang.String[r6];	 Catch:{ all -> 0x020b }
        r7 = 0;	 Catch:{ all -> 0x020b }
        r6[r7] = r25;	 Catch:{ all -> 0x020b }
        r7 = 0;	 Catch:{ all -> 0x020b }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x020b }
        r0 = r24;	 Catch:{ all -> 0x020b }
        r3 = r0.mContext;	 Catch:{ all -> 0x020b }
        r3 = r3.getOpPackageName();	 Catch:{ all -> 0x020b }
        r0 = r24;	 Catch:{ all -> 0x020b }
        r8 = r0.getUnusedColor(r3);	 Catch:{ all -> 0x020b }
        r17 = 0;
        if (r10 == 0) goto L_0x01ab;
    L_0x0084:
        r3 = r10.moveToFirst();	 Catch:{ all -> 0x0204 }
        if (r3 == 0) goto L_0x01ab;	 Catch:{ all -> 0x0204 }
    L_0x008a:
        r3 = 0;	 Catch:{ all -> 0x0204 }
        r19 = r10.getInt(r3);	 Catch:{ all -> 0x0204 }
        r3 = 1;	 Catch:{ all -> 0x0204 }
        r16 = r10.getInt(r3);	 Catch:{ all -> 0x0204 }
        r3 = 2;	 Catch:{ all -> 0x0204 }
        r14 = r10.getInt(r3);	 Catch:{ all -> 0x0204 }
        r23 = new android.content.ContentValues;	 Catch:{ all -> 0x0204 }
        r23.<init>();	 Catch:{ all -> 0x0204 }
        r0 = r26;	 Catch:{ all -> 0x0204 }
        r1 = r16;	 Catch:{ all -> 0x0204 }
        if (r0 == r1) goto L_0x00bd;	 Catch:{ all -> 0x0204 }
    L_0x00a4:
        r3 = "sim_id";	 Catch:{ all -> 0x0204 }
        r4 = java.lang.Integer.valueOf(r26);	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r0.put(r3, r4);	 Catch:{ all -> 0x0204 }
        r3 = "network_mode";	 Catch:{ all -> 0x0204 }
        r4 = -1;	 Catch:{ all -> 0x0204 }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r0.put(r3, r4);	 Catch:{ all -> 0x0204 }
    L_0x00bd:
        r3 = 2;	 Catch:{ all -> 0x0204 }
        if (r14 == r3) goto L_0x00c2;	 Catch:{ all -> 0x0204 }
    L_0x00c0:
        r17 = 1;	 Catch:{ all -> 0x0204 }
    L_0x00c2:
        r3 = r23.size();	 Catch:{ all -> 0x0204 }
        if (r3 <= 0) goto L_0x00eb;	 Catch:{ all -> 0x0204 }
    L_0x00c8:
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x0204 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0204 }
        r4.<init>();	 Catch:{ all -> 0x0204 }
        r5 = "_id=";	 Catch:{ all -> 0x0204 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0204 }
        r0 = r19;	 Catch:{ all -> 0x0204 }
        r6 = (long) r0;	 Catch:{ all -> 0x0204 }
        r5 = java.lang.Long.toString(r6);	 Catch:{ all -> 0x0204 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0204 }
        r4 = r4.toString();	 Catch:{ all -> 0x0204 }
        r5 = 0;	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r2.update(r3, r0, r4, r5);	 Catch:{ all -> 0x0204 }
    L_0x00eb:
        r3 = "[addSubInfoRecord] Record already exists";	 Catch:{ all -> 0x0204 }
        r0 = r24;	 Catch:{ all -> 0x0204 }
        r0.logdl(r3);	 Catch:{ all -> 0x0204 }
    L_0x00f3:
        if (r10 == 0) goto L_0x00f8;
    L_0x00f5:
        r10.close();	 Catch:{ all -> 0x020b }
    L_0x00f8:
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x020b }
        r5 = "sim_id=?";	 Catch:{ all -> 0x020b }
        r4 = 1;	 Catch:{ all -> 0x020b }
        r6 = new java.lang.String[r4];	 Catch:{ all -> 0x020b }
        r4 = java.lang.String.valueOf(r26);	 Catch:{ all -> 0x020b }
        r7 = 0;	 Catch:{ all -> 0x020b }
        r6[r7] = r4;	 Catch:{ all -> 0x020b }
        r4 = 0;	 Catch:{ all -> 0x020b }
        r7 = 0;	 Catch:{ all -> 0x020b }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x020b }
        if (r10 == 0) goto L_0x0177;
    L_0x010f:
        r3 = r10.moveToFirst();	 Catch:{ all -> 0x02c6 }
        if (r3 == 0) goto L_0x0177;	 Catch:{ all -> 0x02c6 }
    L_0x0115:
        r3 = "_id";	 Catch:{ all -> 0x02c6 }
        r3 = r10.getColumnIndexOrThrow(r3);	 Catch:{ all -> 0x02c6 }
        r19 = r10.getInt(r3);	 Catch:{ all -> 0x02c6 }
        r19 = r26;	 Catch:{ all -> 0x02c6 }
        r3 = sSlotIdxToSubId;	 Catch:{ all -> 0x02c6 }
        r4 = java.lang.Integer.valueOf(r26);	 Catch:{ all -> 0x02c6 }
        r9 = r3.get(r4);	 Catch:{ all -> 0x02c6 }
        r9 = (java.lang.Integer) r9;	 Catch:{ all -> 0x02c6 }
        if (r9 == 0) goto L_0x0210;	 Catch:{ all -> 0x02c6 }
    L_0x0130:
        r3 = r9.intValue();	 Catch:{ all -> 0x02c6 }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r3);	 Catch:{ all -> 0x02c6 }
        if (r3 == 0) goto L_0x0210;	 Catch:{ all -> 0x02c6 }
    L_0x013a:
        r3 = "[addSubInfoRecord] currentSubId != null && currentSubId is valid, IGNORE";	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c6 }
    L_0x0142:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c6 }
        r3.<init>();	 Catch:{ all -> 0x02c6 }
        r4 = "[addSubInfoRecord] hashmap(";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r0 = r26;	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c6 }
        r4 = ",";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r0 = r26;	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c6 }
        r4 = ")";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r3 = r3.toString();	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c6 }
        r3 = r10.moveToNext();	 Catch:{ all -> 0x02c6 }
        if (r3 != 0) goto L_0x0115;
    L_0x0177:
        if (r10 == 0) goto L_0x017c;
    L_0x0179:
        r10.close();	 Catch:{ all -> 0x020b }
    L_0x017c:
        r0 = r24;	 Catch:{ all -> 0x020b }
        r1 = r26;	 Catch:{ all -> 0x020b }
        r21 = r0.getSubId(r1);	 Catch:{ all -> 0x020b }
        if (r21 == 0) goto L_0x018b;	 Catch:{ all -> 0x020b }
    L_0x0186:
        r0 = r21;	 Catch:{ all -> 0x020b }
        r3 = r0.length;	 Catch:{ all -> 0x020b }
        if (r3 != 0) goto L_0x02cd;	 Catch:{ all -> 0x020b }
    L_0x018b:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020b }
        r3.<init>();	 Catch:{ all -> 0x020b }
        r4 = "[addSubInfoRecord]- getSubId failed subIds == null || length == 0 subIds=";	 Catch:{ all -> 0x020b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x020b }
        r0 = r21;	 Catch:{ all -> 0x020b }
        r3 = r3.append(r0);	 Catch:{ all -> 0x020b }
        r3 = r3.toString();	 Catch:{ all -> 0x020b }
        r0 = r24;	 Catch:{ all -> 0x020b }
        r0.logdl(r3);	 Catch:{ all -> 0x020b }
        r3 = -1;
        android.os.Binder.restoreCallingIdentity(r12);
        return r3;
    L_0x01ab:
        r17 = 1;
        r23 = new android.content.ContentValues;	 Catch:{ all -> 0x0204 }
        r23.<init>();	 Catch:{ all -> 0x0204 }
        r3 = "icc_id";	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r1 = r25;	 Catch:{ all -> 0x0204 }
        r0.put(r3, r1);	 Catch:{ all -> 0x0204 }
        r3 = "color";	 Catch:{ all -> 0x0204 }
        r4 = java.lang.Integer.valueOf(r8);	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r0.put(r3, r4);	 Catch:{ all -> 0x0204 }
        r3 = "sim_id";	 Catch:{ all -> 0x0204 }
        r4 = java.lang.Integer.valueOf(r26);	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r0.put(r3, r4);	 Catch:{ all -> 0x0204 }
        r3 = "carrier_name";	 Catch:{ all -> 0x0204 }
        r4 = "";	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r0.put(r3, r4);	 Catch:{ all -> 0x0204 }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x0204 }
        r0 = r23;	 Catch:{ all -> 0x0204 }
        r22 = r2.insert(r3, r0);	 Catch:{ all -> 0x0204 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0204 }
        r3.<init>();	 Catch:{ all -> 0x0204 }
        r4 = "[addSubInfoRecord] New record created: ";	 Catch:{ all -> 0x0204 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0204 }
        r0 = r22;	 Catch:{ all -> 0x0204 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x0204 }
        r3 = r3.toString();	 Catch:{ all -> 0x0204 }
        r0 = r24;	 Catch:{ all -> 0x0204 }
        r0.logdl(r3);	 Catch:{ all -> 0x0204 }
        goto L_0x00f3;
    L_0x0204:
        r3 = move-exception;
        if (r10 == 0) goto L_0x020a;
    L_0x0207:
        r10.close();	 Catch:{ all -> 0x020b }
    L_0x020a:
        throw r3;	 Catch:{ all -> 0x020b }
    L_0x020b:
        r3 = move-exception;
        android.os.Binder.restoreCallingIdentity(r12);
        throw r3;
    L_0x0210:
        r3 = sSlotIdxToSubId;	 Catch:{ all -> 0x02c6 }
        r4 = java.lang.Integer.valueOf(r26);	 Catch:{ all -> 0x02c6 }
        r5 = java.lang.Integer.valueOf(r26);	 Catch:{ all -> 0x02c6 }
        r3.put(r4, r5);	 Catch:{ all -> 0x02c6 }
        r20 = r24.getActiveSubInfoCountMax();	 Catch:{ all -> 0x02c6 }
        r11 = r24.getDefaultSubId();	 Catch:{ all -> 0x02c6 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c6 }
        r3.<init>();	 Catch:{ all -> 0x02c6 }
        r4 = "[addSubInfoRecord] sSlotIdxToSubId.size=";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r4 = sSlotIdxToSubId;	 Catch:{ all -> 0x02c6 }
        r4 = r4.size();	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r4 = " slotId=";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r0 = r26;	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c6 }
        r4 = " subId=";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r0 = r26;	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c6 }
        r4 = " defaultSubId=";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r11);	 Catch:{ all -> 0x02c6 }
        r4 = " simCount=";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r0 = r20;	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c6 }
        r3 = r3.toString();	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c6 }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r11);	 Catch:{ all -> 0x02c6 }
        if (r3 == 0) goto L_0x0281;	 Catch:{ all -> 0x02c6 }
    L_0x027c:
        r3 = 1;	 Catch:{ all -> 0x02c6 }
        r0 = r20;	 Catch:{ all -> 0x02c6 }
        if (r0 != r3) goto L_0x0288;	 Catch:{ all -> 0x02c6 }
    L_0x0281:
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r1 = r26;	 Catch:{ all -> 0x02c6 }
        r0.setDefaultFallbackSubId(r1);	 Catch:{ all -> 0x02c6 }
    L_0x0288:
        r3 = 1;	 Catch:{ all -> 0x02c6 }
        r0 = r20;	 Catch:{ all -> 0x02c6 }
        if (r0 != r3) goto L_0x0142;	 Catch:{ all -> 0x02c6 }
    L_0x028d:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c6 }
        r3.<init>();	 Catch:{ all -> 0x02c6 }
        r4 = "[addSubInfoRecord] one sim set defaults to subId=";	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c6 }
        r0 = r26;	 Catch:{ all -> 0x02c6 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c6 }
        r3 = r3.toString();	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r1 = r26;	 Catch:{ all -> 0x02c6 }
        r0.setDefaultDataSubId(r1);	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r1 = r26;	 Catch:{ all -> 0x02c6 }
        r0.setDataSubId(r1);	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r1 = r26;	 Catch:{ all -> 0x02c6 }
        r0.setDefaultSmsSubId(r1);	 Catch:{ all -> 0x02c6 }
        r0 = r24;	 Catch:{ all -> 0x02c6 }
        r1 = r26;	 Catch:{ all -> 0x02c6 }
        r0.setDefaultVoiceSubId(r1);	 Catch:{ all -> 0x02c6 }
        goto L_0x0142;
    L_0x02c6:
        r3 = move-exception;
        if (r10 == 0) goto L_0x02cc;
    L_0x02c9:
        r10.close();	 Catch:{ all -> 0x020b }
    L_0x02cc:
        throw r3;	 Catch:{ all -> 0x020b }
    L_0x02cd:
        if (r17 == 0) goto L_0x032c;	 Catch:{ all -> 0x020b }
    L_0x02cf:
        r0 = r24;	 Catch:{ all -> 0x020b }
        r3 = r0.mTelephonyManager;	 Catch:{ all -> 0x020b }
        r4 = 0;	 Catch:{ all -> 0x020b }
        r4 = r21[r4];	 Catch:{ all -> 0x020b }
        r18 = r3.getSimOperatorName(r4);	 Catch:{ all -> 0x020b }
        r3 = android.text.TextUtils.isEmpty(r18);	 Catch:{ all -> 0x020b }
        if (r3 != 0) goto L_0x0357;	 Catch:{ all -> 0x020b }
    L_0x02e0:
        r15 = r18;	 Catch:{ all -> 0x020b }
    L_0x02e2:
        r23 = new android.content.ContentValues;	 Catch:{ all -> 0x020b }
        r23.<init>();	 Catch:{ all -> 0x020b }
        r3 = "display_name";	 Catch:{ all -> 0x020b }
        r0 = r23;	 Catch:{ all -> 0x020b }
        r0.put(r3, r15);	 Catch:{ all -> 0x020b }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x020b }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020b }
        r4.<init>();	 Catch:{ all -> 0x020b }
        r5 = "sim_id=";	 Catch:{ all -> 0x020b }
        r4 = r4.append(r5);	 Catch:{ all -> 0x020b }
        r5 = 0;	 Catch:{ all -> 0x020b }
        r5 = r21[r5];	 Catch:{ all -> 0x020b }
        r6 = (long) r5;	 Catch:{ all -> 0x020b }
        r5 = java.lang.Long.toString(r6);	 Catch:{ all -> 0x020b }
        r4 = r4.append(r5);	 Catch:{ all -> 0x020b }
        r4 = r4.toString();	 Catch:{ all -> 0x020b }
        r5 = 0;	 Catch:{ all -> 0x020b }
        r0 = r23;	 Catch:{ all -> 0x020b }
        r2.update(r3, r0, r4, r5);	 Catch:{ all -> 0x020b }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020b }
        r3.<init>();	 Catch:{ all -> 0x020b }
        r4 = "[addSubInfoRecord] sim name = ";	 Catch:{ all -> 0x020b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x020b }
        r3 = r3.append(r15);	 Catch:{ all -> 0x020b }
        r3 = r3.toString();	 Catch:{ all -> 0x020b }
        r0 = r24;	 Catch:{ all -> 0x020b }
        r0.logdl(r3);	 Catch:{ all -> 0x020b }
    L_0x032c:
        r3 = sPhones;	 Catch:{ all -> 0x020b }
        r3 = r3[r26];	 Catch:{ all -> 0x020b }
        r3.updateDataConnectionTracker();	 Catch:{ all -> 0x020b }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020b }
        r3.<init>();	 Catch:{ all -> 0x020b }
        r4 = "[addSubInfoRecord]- info size=";	 Catch:{ all -> 0x020b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x020b }
        r4 = sSlotIdxToSubId;	 Catch:{ all -> 0x020b }
        r4 = r4.size();	 Catch:{ all -> 0x020b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x020b }
        r3 = r3.toString();	 Catch:{ all -> 0x020b }
        r0 = r24;	 Catch:{ all -> 0x020b }
        r0.logdl(r3);	 Catch:{ all -> 0x020b }
        android.os.Binder.restoreCallingIdentity(r12);
        r3 = 0;
        return r3;
    L_0x0357:
        r0 = r24;	 Catch:{ all -> 0x020b }
        r3 = r0.mTelephonyManager;	 Catch:{ all -> 0x020b }
        r3 = r3.isMultiSimEnabled();	 Catch:{ all -> 0x020b }
        if (r3 == 0) goto L_0x037d;	 Catch:{ all -> 0x020b }
    L_0x0361:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x020b }
        r3.<init>();	 Catch:{ all -> 0x020b }
        r4 = "CARD ";	 Catch:{ all -> 0x020b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x020b }
        r4 = r26 + 1;	 Catch:{ all -> 0x020b }
        r4 = java.lang.Integer.toString(r4);	 Catch:{ all -> 0x020b }
        r3 = r3.append(r4);	 Catch:{ all -> 0x020b }
        r15 = r3.toString();	 Catch:{ all -> 0x020b }
        goto L_0x02e2;	 Catch:{ all -> 0x020b }
    L_0x037d:
        r15 = "CARD";	 Catch:{ all -> 0x020b }
        goto L_0x02e2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int");
    }

    public boolean setPlmnSpn(int slotId, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int[] subIds = getSubId(slotId);
            if (!(this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) == null || subIds == null)) {
                if (SubscriptionManager.isValidSubscriptionId(subIds[0])) {
                    String carrierText = "";
                    if (showPlmn) {
                        carrierText = plmn;
                        if (showSpn && !Objects.equals(spn, plmn)) {
                            carrierText = plmn + this.mContext.getString(17040659).toString() + spn;
                        }
                    } else if (showSpn) {
                        carrierText = spn;
                    }
                    int i = 0;
                    while (i < subIds.length) {
                        if (i == 0 || subIds[i] != subIds[0]) {
                            setCarrierText(carrierText, subIds[i]);
                        } else {
                            logd("skip setCarrierText for same subIds");
                        }
                        i++;
                    }
                    return DBG;
                }
            }
            logd("[setPlmnSpn] No valid subscription to store info");
            notifySubscriptionInfoChanged();
            return IS_FAST_SWITCH_SIMSLOT;
        }
    }

    private int setCarrierText(String text, int subId) {
        logd("[setCarrierText]+ text:" + text + " subId:" + subId);
        enforceModifyPhoneState("setCarrierText");
        long identity = Binder.clearCallingIdentity();
        try {
            ContentValues value = new ContentValues(1);
            value.put("carrier_name", text);
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setIconTint(int tint, int subId) {
        logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        enforceModifyPhoneState("setIconTint");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            ContentValues value = new ContentValues(1);
            value.put("color", Integer.valueOf(tint));
            logd("[setIconTint]- tint:" + tint + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayNameUsingSrc(displayName, subId, -1);
    }

    public int setDisplayNameUsingSrc(String displayName, int subId, long nameSource) {
        logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        enforceModifyPhoneState("setDisplayNameUsingSrc");
        long identity = Binder.clearCallingIdentity();
        try {
            String nameToSet;
            validateSubId(subId);
            if (displayName == null) {
                nameToSet = this.mContext.getString(17039374);
            } else {
                nameToSet = displayName;
            }
            ContentValues value = new ContentValues(1);
            value.put("display_name", nameToSet);
            if (nameSource >= 0) {
                logd("Set nameSource=" + nameSource);
                value.put("name_source", Long.valueOf(nameSource));
            }
            logd("[setDisplayName]- mDisplayName:" + nameToSet + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayNumber(String number, int subId) {
        logd("[setDisplayNumber]: subId:" + subId);
        enforceModifyPhoneState("setDisplayNumber");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            int phoneId = getPhoneId(subId);
            if (number != null && phoneId >= 0) {
                if (phoneId < this.mTelephonyManager.getPhoneCount()) {
                    ContentValues value = new ContentValues(1);
                    value.put("number", number);
                    int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
                    logd("[setDisplayNumber]- update result :" + result);
                    notifySubscriptionInfoChanged();
                    Binder.restoreCallingIdentity(identity);
                    return result;
                }
            }
            logd("[setDispalyNumber]- fail");
            return -1;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDataRoaming(int roaming, int subId) {
        logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        enforceModifyPhoneState("setDataRoaming");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            if (roaming < 0) {
                logd("[setDataRoaming]- fail");
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put("data_roaming", Integer.valueOf(roaming));
            logd("[setDataRoaming]- roaming:" + roaming + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setMccMnc(String mccMnc, int subId) {
        int mcc = 0;
        int mnc = 0;
        try {
            mcc = Integer.parseInt(mccMnc.substring(0, 3));
            mnc = Integer.parseInt(mccMnc.substring(3));
        } catch (NumberFormatException e) {
            loge("[setMccMnc] - couldn't parse mcc/mnc: " + mccMnc);
        }
        logd("[setMccMnc]+ mcc/mnc:" + mcc + "/" + mnc + " subId:" + subId);
        ContentValues value = new ContentValues(2);
        value.put(GlobalMatchs.MCC, Integer.valueOf(mcc));
        value.put(GlobalMatchs.MNC, Integer.valueOf(mnc));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
        notifySubscriptionInfoChanged();
        return result;
    }

    public int setSimProvisioningStatus(int provisioningStatus, int subId) {
        logd("[setSimProvisioningStatus]+ provisioningStatus:" + provisioningStatus + " subId:" + subId);
        enforceModifyPhoneState("setSimProvisioningStatus");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            if (provisioningStatus < 0 || provisioningStatus > 2) {
                logd("[setSimProvisioningStatus]- fail with wrong provisioningStatus");
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put("sim_provisioning_status", Integer.valueOf(provisioningStatus));
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getSlotId(int subId) {
        if (HwTelephonyFactory.getHwUiccManager().isUsingHwSubIdDesign()) {
            return getHwSlotId(subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotId]- subId invalid");
            return -1;
        } else if (sSlotIdxToSubId.size() == 0) {
            logd("[getSlotId]- size == 0, return SIM_NOT_INSERTED instead");
            return -1;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    return sim;
                }
            }
            logd("[getSlotId]- return fail");
            return -1;
        }
    }

    @Deprecated
    public int[] getSubId(int slotIdx) {
        if (HwTelephonyFactory.getHwUiccManager().isUsingHwSubIdDesign()) {
            return getHwSubId(slotIdx);
        }
        if (slotIdx == Integer.MAX_VALUE) {
            slotIdx = getSlotId(getDefaultSubId());
        }
        if (!SubscriptionManager.isValidSlotId(slotIdx)) {
            logd("[getSubId]- invalid slotIdx=" + slotIdx);
            return null;
        } else if (sSlotIdxToSubId.size() == 0) {
            return getDummySubIds(slotIdx);
        } else {
            ArrayList<Integer> subIds = new ArrayList();
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                int slot = ((Integer) entry.getKey()).intValue();
                int sub = ((Integer) entry.getValue()).intValue();
                if (slotIdx == slot) {
                    subIds.add(Integer.valueOf(sub));
                }
            }
            int numSubIds = subIds.size();
            if (numSubIds > 0) {
                int[] subIdArr = new int[numSubIds];
                for (int i = 0; i < numSubIds; i++) {
                    subIdArr[i] = ((Integer) subIds.get(i)).intValue();
                }
                return subIdArr;
            }
            logd("[getSubId]- numSubIds == 0, return DummySubIds slotIdx=" + slotIdx);
            return getDummySubIds(slotIdx);
        }
    }

    public int getPhoneId(int subId) {
        if (HwTelephonyFactory.getHwUiccManager().isUsingHwSubIdDesign()) {
            return getHwPhoneId(subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logdl("[getPhoneId] asked for default subId=" + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return -1;
        }
        if (sSlotIdxToSubId.size() == 0) {
            int phoneId = mDefaultPhoneId;
            logdl("[getPhoneId]- no sims, returning default phoneId=" + phoneId);
            return phoneId;
        }
        for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
            int sim = ((Integer) entry.getKey()).intValue();
            if (subId == ((Integer) entry.getValue()).intValue()) {
                return sim;
            }
        }
        phoneId = mDefaultPhoneId;
        logdl("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId);
        return phoneId;
    }

    private int[] getDummySubIds(int slotIdx) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] dummyValues = new int[numSubs];
        for (int i = 0; i < numSubs; i++) {
            dummyValues[i] = -2 - slotIdx;
        }
        return dummyValues;
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIdxToSubId.size();
            if (size == 0) {
                logdl("[clearSubInfo]- no simInfo size=" + size);
                return 0;
            }
            sSlotIdxToSubId.clear();
            logdl("[clearSubInfo]- clear size=" + size);
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void logvl(String msg) {
        logv(msg);
        this.mLocalLog.log(msg);
    }

    private void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    private void logdl(String msg) {
        logd(msg);
        this.mLocalLog.log(msg);
    }

    private static void slogd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logel(String msg) {
        loge(msg);
        this.mLocalLog.log(msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getDefaultSubId() {
        int subId;
        if (this.mContext.getResources().getBoolean(17956956)) {
            subId = getDefaultVoiceSubId();
        } else {
            subId = getDefaultDataSubId();
        }
        if (isActiveSubId(subId)) {
            return subId;
        }
        return mDefaultFallbackSubId;
    }

    public void setDefaultSmsSubId(int subId) {
        enforceModifyPhoneState("setDefaultSmsSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSmsSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultSmsSubId] subId=" + subId);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms", subId);
        broadcastDefaultSmsSubIdChanged(subId);
    }

    private void broadcastDefaultSmsSubIdChanged(int subId) {
        logdl("[broadcastDefaultSmsSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultSmsSubId() {
        return Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
    }

    public void setDefaultVoiceSubId(int subId) {
        enforceModifyPhoneState("setDefaultVoiceSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultVoiceSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultVoiceSubId] subId=" + subId);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_call", subId);
        broadcastDefaultVoiceSubIdChanged(subId);
    }

    private void broadcastDefaultVoiceSubIdChanged(int subId) {
        logdl("[broadcastDefaultVoiceSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultVoiceSubId() {
        return Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_call", -1);
    }

    public int getDefaultDataSubId() {
        return Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", 0);
    }

    public void setDefaultDataSubId(int subId) {
        enforceModifyPhoneState("setDefaultDataSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultDataSubId] subId=" + subId);
        ProxyController proxyController = ProxyController.getInstance();
        int len = sPhones.length;
        logdl("[setDefaultDataSubId] num phones=" + len + ", subId=" + subId);
        String flexMapSupportType = SystemProperties.get("persist.radio.flexmap_type", "nw_mode");
        boolean isQcomPlat = HwModemCapability.isCapabilitySupport(9);
        if (SubscriptionManager.isValidSubscriptionId(subId) && ((!isQcomPlat || flexMapSupportType.equals("dds")) && !IS_FAST_SWITCH_SIMSLOT)) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[len];
            boolean atLeastOneMatch = IS_FAST_SWITCH_SIMSLOT;
            for (int phoneId = 0; phoneId < len; phoneId++) {
                int raf;
                int id = sPhones[phoneId].getSubId();
                if (id == subId) {
                    raf = proxyController.getMaxRafSupported();
                    atLeastOneMatch = DBG;
                } else {
                    raf = proxyController.getMinRafSupported();
                }
                logdl("[setDefaultDataSubId] phoneId=" + phoneId + " subId=" + id + " RAF=" + raf);
                rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
            }
            if (atLeastOneMatch) {
                proxyController.setRadioCapability(rafs);
            } else {
                logdl("[setDefaultDataSubId] no valid subId's found - not updating.");
            }
        }
        updateAllDataConnectionTrackers();
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
        broadcastDefaultDataSubIdChanged(subId);
    }

    public void informDdsToQcril(int ddsPhoneId) {
        if (this.qcRilHook != null) {
            try {
                Method qcRilSendDDSInfo = this.qcRilHook.getClass().getMethod("qcRilSendDDSInfo", new Class[]{Integer.TYPE, Integer.TYPE});
                if (ddsPhoneId < 0 || ddsPhoneId >= sPhones.length) {
                    logd("informDdsToQcril dds phoneId is invalid = " + ddsPhoneId);
                    return;
                }
                for (int i = 0; i < sPhones.length; i++) {
                    logd("informDdsToQcril rild= " + i + ", ddsPhoneId=" + ddsPhoneId);
                    qcRilSendDDSInfo.invoke(this.qcRilHook, new Object[]{Integer.valueOf(ddsPhoneId), Integer.valueOf(i)});
                }
                return;
            } catch (NoSuchMethodException nsme) {
                nsme.printStackTrace();
                return;
            } catch (RuntimeException re) {
                re.printStackTrace();
                return;
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
                return;
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
                return;
            }
        }
        logd("informDdsToQcril qcRilHook is null.");
    }

    public Object getQcRilHook() {
        logd("Get QcRilHook Class");
        if (this.qcRilHook == null) {
            try {
                Object[] params = new Object[]{this.mContext};
                this.qcRilHook = new PathClassLoader("system/framework/qcrilhook.jar", ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qcrilhook.QcRilHook").getConstructor(new Class[]{Context.class}).newInstance(params);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (RuntimeException e2) {
                e2.printStackTrace();
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace();
            } catch (InstantiationException e4) {
                e4.printStackTrace();
            } catch (IllegalAccessException e5) {
                e5.printStackTrace();
            } catch (InvocationTargetException e6) {
                e6.printStackTrace();
            }
        }
        return this.qcRilHook;
    }

    private void updateAllDataConnectionTrackers() {
        int len = sPhones.length;
        logdl("[updateAllDataConnectionTrackers] sPhones.length=" + len);
        for (int phoneId = 0; phoneId < len; phoneId++) {
            logdl("[updateAllDataConnectionTrackers] phoneId=" + phoneId);
            sPhones[phoneId].updateDataConnectionTracker();
        }
    }

    private void broadcastDefaultDataSubIdChanged(int subId) {
        logdl("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setDefaultFallbackSubId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultFallbackSubId] subId=" + subId);
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            int phoneId = getPhoneId(subId);
            if (phoneId < 0 || (phoneId >= this.mTelephonyManager.getPhoneCount() && this.mTelephonyManager.getSimCount() != 1)) {
                logdl("[setDefaultFallbackSubId] not set invalid phoneId=" + phoneId + " subId=" + subId);
                return;
            }
            logdl("[setDefaultFallbackSubId] set mDefaultFallbackSubId=" + subId);
            mDefaultFallbackSubId = subId;
            MccTable.updateMccMncConfiguration(this.mContext, this.mTelephonyManager.getSimOperatorNumericForPhone(phoneId), IS_FAST_SWITCH_SIMSLOT);
            Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED");
            intent.addFlags(536870912);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, subId);
            logdl("[setDefaultFallbackSubId] broadcast default subId changed phoneId=" + phoneId + " subId=" + subId);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void clearDefaultsForInactiveSubIds() {
        enforceModifyPhoneState("clearDefaultsForInactiveSubIds");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            logdl("[clearDefaultsForInactiveSubIds] records: " + records);
            if (shouldDefaultBeCleared(records, getDefaultDataSubId())) {
                logd("[clearDefaultsForInactiveSubIds] clearing default data sub id");
                setDefaultDataSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultSmsSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default sms sub id");
                setDefaultSmsSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default voice sub id");
                setDefaultVoiceSubId(-1);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        logdl("[shouldDefaultBeCleared: subId] " + subId);
        if (records == null) {
            logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            return DBG;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSimSlotIndex();
                logdl("[shouldDefaultBeCleared] Record.id: " + id);
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return IS_FAST_SWITCH_SIMSLOT;
                }
            }
            logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            return DBG;
        } else {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return IS_FAST_SWITCH_SIMSLOT;
        }
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    public int[] getSubIdUsingSlotId(int slotId) {
        return getSubId(slotId);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<SubscriptionInfo> getSubInfoUsingSlotIdWithCheck(int slotId, boolean needCheck, String callingPackage) {
        Throwable th;
        logd("[getSubInfoUsingSlotIdWithCheck]+ slotId:" + slotId);
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIdWithCheck")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        if (slotId == Integer.MAX_VALUE) {
            try {
                slotId = getSlotId(getDefaultSubId());
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        if (SubscriptionManager.isValidSlotId(slotId)) {
            if (needCheck) {
                if (!isSubInfoReady()) {
                    logd("[getSubInfoUsingSlotIdWithCheck]- not ready");
                    Binder.restoreCallingIdentity(identity);
                    return null;
                }
            }
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotId)}, null);
            List<SubscriptionInfo> list = null;
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            ArrayList<SubscriptionInfo> subList;
                            ArrayList<SubscriptionInfo> subList2 = subList;
                            try {
                                SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                                if (subInfo != null) {
                                    if (subList2 == null) {
                                        subList = new ArrayList();
                                    } else {
                                        subList = subList2;
                                    }
                                    subList.add(subInfo);
                                } else {
                                    subList = subList2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                subList = subList2;
                            }
                        } while (cursor.moveToNext());
                    }
                } catch (Throwable th4) {
                    th = th4;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getSubInfoUsingSlotId]- null info return");
            Binder.restoreCallingIdentity(identity);
            return list;
        }
        logd("[getSubInfoUsingSlotIdWithCheck]- invalid slotId");
        Binder.restoreCallingIdentity(identity);
        return null;
    }

    private void validateSubId(int subId) {
        logd("validateSubId subId: " + subId);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            throw new RuntimeException("Invalid sub id passed as parameter");
        } else if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("Default sub id passed as parameter");
        }
    }

    public void updatePhonesAvailability(Phone[] phones) {
        sPhones = phones;
    }

    public int[] getActiveSubIdList() {
        Set<Entry<Integer, Integer>> simInfoSet = sSlotIdxToSubId.entrySet();
        int[] subIdArr = new int[simInfoSet.size()];
        int i = 0;
        for (Entry<Integer, Integer> entry : simInfoSet) {
            subIdArr[i] = ((Integer) entry.getValue()).intValue();
            i++;
        }
        return subIdArr;
    }

    public boolean isActiveSubId(int subId) {
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            return sSlotIdxToSubId.containsValue(Integer.valueOf(subId));
        }
        return IS_FAST_SWITCH_SIMSLOT;
    }

    public int getSimStateForSlotIdx(int slotIdx) {
        State simState;
        String err;
        if (slotIdx < 0) {
            simState = State.UNKNOWN;
            err = "invalid slotIdx";
        } else {
            Phone phone = PhoneFactory.getPhone(slotIdx);
            if (phone == null) {
                simState = State.UNKNOWN;
                err = "phone == null";
            } else {
                IccCard icc = phone.getIccCard();
                if (icc == null) {
                    simState = State.UNKNOWN;
                    err = "icc == null";
                } else {
                    simState = icc.getState();
                    err = "";
                }
            }
        }
        return simState.ordinal();
    }

    public void setSubscriptionProperty(int subId, String propKey, String propValue) {
        enforceModifyPhoneState("setSubscriptionProperty");
        long token = Binder.clearCallingIdentity();
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        if (propKey.equals("enable_cmas_extreme_threat_alerts") || propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog")) {
            value.put(propKey, Integer.valueOf(Integer.parseInt(propValue)));
        } else {
            logd("Invalid column name");
        }
        resolver.update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Integer.toString(subId), null);
        Binder.restoreCallingIdentity(token);
    }

    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIdWithCheck")) {
            return null;
        }
        String resultValue = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{propKey}, "sim_id=?", new String[]{subId + ""}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (!propKey.equals("enable_cmas_extreme_threat_alerts")) {
                        if (!(propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog"))) {
                            logd("Invalid column name");
                        }
                    }
                    resultValue = cursor.getInt(0) + "";
                } else {
                    logd("Valid row not present in db");
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            logd("Query failed");
        }
        if (cursor != null) {
            cursor.close();
        }
        logd("getSubscriptionProperty Query value = " + resultValue);
        return resultValue;
    }

    private static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        slogd("StackTrace - " + msg);
        StackTraceElement[] st = re.getStackTrace();
        boolean first = DBG;
        for (StackTraceElement ste : st) {
            if (first) {
                first = IS_FAST_SWITCH_SIMSLOT;
            } else {
                slogd(ste.toString());
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Requires DUMP");
        long token = Binder.clearCallingIdentity();
        try {
            pw.println("SubscriptionController:");
            pw.println(" defaultSubId=" + getDefaultSubId());
            pw.println(" defaultDataSubId=" + getDefaultDataSubId());
            pw.println(" defaultVoiceSubId=" + getDefaultVoiceSubId());
            pw.println(" defaultSmsSubId=" + getDefaultSmsSubId());
            pw.println(" defaultDataPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
            pw.println(" defaultVoicePhoneId=" + SubscriptionManager.getDefaultVoicePhoneId());
            pw.println(" defaultSmsPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultSmsPhoneId());
            pw.flush();
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                pw.println(" sSlotIdxToSubId[" + entry.getKey() + "]: subId=" + entry.getValue());
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" ActiveSubInfoList:");
                for (SubscriptionInfo entry2 : sirl) {
                    pw.println("  " + entry2.toString());
                }
            } else {
                pw.println(" ActiveSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            sirl = getAllSubInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" AllSubInfoList:");
                for (SubscriptionInfo entry22 : sirl) {
                    pw.println("  " + entry22.toString());
                }
            } else {
                pw.println(" AllSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            this.mLocalLog.dump(fd, pw, args);
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            pw.flush();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void setDefaultFallbackSubIdHw(int value) {
        setDefaultFallbackSubId(value);
    }

    public void updateAllDataConnectionTrackersHw() {
        updateAllDataConnectionTrackers();
    }
}
