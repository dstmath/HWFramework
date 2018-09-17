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
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionController extends AbstractSubscriptionController {
    public static final int CLAT_SET_ERROR = 4;
    public static final int CLAT_SET_FALSE = 1;
    public static final int CLAT_SET_NULL = 3;
    public static final int CLAT_SET_TRUE = 2;
    static final boolean DBG = true;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    static final String LOG_TAG = "SubscriptionController";
    static final int MAX_LOCAL_LOG_LINES = 500;
    static final boolean VDBG = false;
    private static int mDefaultFallbackSubId = 0;
    private static int mDefaultPhoneId = 0;
    private static SubscriptionController sInstance = null;
    protected static Phone[] sPhones;
    private static Map<Integer, Integer> sSlotIndexToSubId = new ConcurrentHashMap();
    private int[] colorArr;
    private AppOpsManager mAppOps;
    protected CallManager mCM;
    protected Context mContext;
    private ScLocalLog mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
    protected final Object mLock = new Object();
    protected TelephonyManager mTelephonyManager;
    private Object qcRilHook = null;

    static class ScLocalLog {
        private LinkedList<String> mLog = new LinkedList();

        public ScLocalLog(int maxLines) {
        }

        public synchronized void log(String msg) {
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = this.mLog.listIterator(0);
            int i = 0;
            while (true) {
                int i2 = i;
                if (itr.hasNext()) {
                    i = i2 + 1;
                    pw.println(Integer.toString(i2) + ": " + ((String) itr.next()));
                    if (i % 10 == 0) {
                        pw.flush();
                    }
                }
            }
        }
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
        return sSlotIndexToSubId.size() > 0;
    }

    private SubscriptionController(Phone phone) {
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
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
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
        this.mContext.sendBroadcast(new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
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
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302619);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow("mcc"));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow("mnc"));
        String countryIso = getSubscriptionCountryIso(id);
        int status = cursor.getInt(cursor.getColumnIndexOrThrow("sub_state"));
        int nwMode = cursor.getInt(cursor.getColumnIndexOrThrow("network_mode"));
        String line1Number = this.mTelephonyManager.getLine1Number(simSlotIndex);
        if (!(TextUtils.isEmpty(line1Number) || (line1Number.equals(number) ^ 1) == 0)) {
            number = line1Number;
        }
        return new SubscriptionInfo(simSlotIndex, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, status, nwMode);
    }

    private String getSubscriptionCountryIso(int subId) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            return "";
        }
        return this.mTelephonyManager.getSimCountryIsoForPhone(phoneId);
    }

    private List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        Throwable th;
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[]{queryKey.toString()};
        }
        List<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, null);
        if (cursor != null) {
            while (true) {
                ArrayList<SubscriptionInfo> subList2;
                ArrayList<SubscriptionInfo> subList3 = subList2;
                try {
                    if (!cursor.moveToNext()) {
                        Object subList4 = subList3;
                        break;
                    }
                    SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList3 == null) {
                            subList2 = new ArrayList();
                        } else {
                            subList2 = subList3;
                        }
                        try {
                            subList2.add(subInfo);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        subList2 = subList3;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
        logd("Query fail");
        if (cursor != null) {
            cursor.close();
        }
        return subList4;
    }

    private int getUnusedColor(String callingPackage) {
        List<SubscriptionInfo> availableSubInfos = getActiveSubscriptionInfoList(callingPackage);
        this.colorArr = this.mContext.getResources().getIntArray(17236068);
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
        if (SubscriptionManager.isValidSubscriptionId(subId) && (isSubInfoReady() ^ 1) == 0) {
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
                logd("[getActiveSubInfoForSubscriber] subInfo=null");
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
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForIccId") || iccId == null) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (iccId.equals(si.getIccId())) {
                        logd("[getActiveSubInfoUsingIccId]+ iccId=" + SubscriptionInfo.givePrintableIccid(iccId) + " subInfo=" + si);
                        return si;
                    }
                }
            }
            logd("[getActiveSubInfoUsingIccId]+ iccId=" + SubscriptionInfo.givePrintableIccid(iccId) + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIndex, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSimSlotIndex() == slotIndex) {
                        logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=*");
                        return si;
                    }
                }
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=null");
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

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int, dom blocks: [B:2:0x0038, B:10:0x0084, B:19:0x00ff]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x00e5 A:{SYNTHETIC, Splitter: B:15:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00ff A:{SYNTHETIC, Splitter: B:19:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0217 A:{SYNTHETIC, Splitter: B:37:0x0217} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x02d0 A:{Catch:{ all -> 0x02c9, all -> 0x02b3, all -> 0x02ba }} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0228 A:{Catch:{ all -> 0x02c9, all -> 0x02b3, all -> 0x02ba }} */
    public int addSubInfoRecord(java.lang.String r24, int r25) {
        /*
        r23 = this;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "[addSubInfoRecord]+ iccId:";
        r3 = r3.append(r4);
        r4 = android.telephony.SubscriptionInfo.givePrintableIccid(r24);
        r3 = r3.append(r4);
        r4 = " slotIndex:";
        r3 = r3.append(r4);
        r0 = r25;
        r3 = r3.append(r0);
        r3 = r3.toString();
        r0 = r23;
        r0.logdl(r3);
        r3 = "addSubInfoRecord";
        r0 = r23;
        r0.enforceModifyPhoneState(r3);
        r12 = android.os.Binder.clearCallingIdentity();
        if (r24 != 0) goto L_0x0045;
    L_0x0038:
        r3 = "[addSubInfoRecord]- null iccId";	 Catch:{ all -> 0x02ba }
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r0.logdl(r3);	 Catch:{ all -> 0x02ba }
        r3 = -1;
        android.os.Binder.restoreCallingIdentity(r12);
        return r3;
    L_0x0045:
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r3 = r0.mContext;	 Catch:{ all -> 0x02ba }
        r2 = r3.getContentResolver();	 Catch:{ all -> 0x02ba }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02ba }
        r4 = 3;	 Catch:{ all -> 0x02ba }
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x02ba }
        r5 = "_id";	 Catch:{ all -> 0x02ba }
        r6 = 0;	 Catch:{ all -> 0x02ba }
        r4[r6] = r5;	 Catch:{ all -> 0x02ba }
        r5 = "sim_id";	 Catch:{ all -> 0x02ba }
        r6 = 1;	 Catch:{ all -> 0x02ba }
        r4[r6] = r5;	 Catch:{ all -> 0x02ba }
        r5 = "name_source";	 Catch:{ all -> 0x02ba }
        r6 = 2;	 Catch:{ all -> 0x02ba }
        r4[r6] = r5;	 Catch:{ all -> 0x02ba }
        r5 = "icc_id=?";	 Catch:{ all -> 0x02ba }
        r6 = 1;	 Catch:{ all -> 0x02ba }
        r6 = new java.lang.String[r6];	 Catch:{ all -> 0x02ba }
        r7 = 0;	 Catch:{ all -> 0x02ba }
        r6[r7] = r24;	 Catch:{ all -> 0x02ba }
        r7 = 0;	 Catch:{ all -> 0x02ba }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x02ba }
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r3 = r0.mContext;	 Catch:{ all -> 0x02ba }
        r3 = r3.getOpPackageName();	 Catch:{ all -> 0x02ba }
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r8 = r0.getUnusedColor(r3);	 Catch:{ all -> 0x02ba }
        r17 = 0;
        if (r10 == 0) goto L_0x008c;
    L_0x0084:
        r3 = r10.moveToFirst();	 Catch:{ all -> 0x02b3 }
        r3 = r3 ^ 1;	 Catch:{ all -> 0x02b3 }
        if (r3 == 0) goto L_0x0248;	 Catch:{ all -> 0x02b3 }
    L_0x008c:
        r17 = 1;	 Catch:{ all -> 0x02b3 }
        r22 = new android.content.ContentValues;	 Catch:{ all -> 0x02b3 }
        r22.<init>();	 Catch:{ all -> 0x02b3 }
        r3 = "icc_id";	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r1 = r24;	 Catch:{ all -> 0x02b3 }
        r0.put(r3, r1);	 Catch:{ all -> 0x02b3 }
        r3 = "color";	 Catch:{ all -> 0x02b3 }
        r4 = java.lang.Integer.valueOf(r8);	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r0.put(r3, r4);	 Catch:{ all -> 0x02b3 }
        r3 = "sim_id";	 Catch:{ all -> 0x02b3 }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r0.put(r3, r4);	 Catch:{ all -> 0x02b3 }
        r3 = "carrier_name";	 Catch:{ all -> 0x02b3 }
        r4 = "";	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r0.put(r3, r4);	 Catch:{ all -> 0x02b3 }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r21 = r2.insert(r3, r0);	 Catch:{ all -> 0x02b3 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02b3 }
        r3.<init>();	 Catch:{ all -> 0x02b3 }
        r4 = "[addSubInfoRecord] New record created: ";	 Catch:{ all -> 0x02b3 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02b3 }
        r0 = r21;	 Catch:{ all -> 0x02b3 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02b3 }
        r3 = r3.toString();	 Catch:{ all -> 0x02b3 }
        r0 = r23;	 Catch:{ all -> 0x02b3 }
        r0.logdl(r3);	 Catch:{ all -> 0x02b3 }
    L_0x00e3:
        if (r10 == 0) goto L_0x00e8;
    L_0x00e5:
        r10.close();	 Catch:{ all -> 0x02ba }
    L_0x00e8:
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02ba }
        r5 = "sim_id=?";	 Catch:{ all -> 0x02ba }
        r4 = 1;	 Catch:{ all -> 0x02ba }
        r6 = new java.lang.String[r4];	 Catch:{ all -> 0x02ba }
        r4 = java.lang.String.valueOf(r25);	 Catch:{ all -> 0x02ba }
        r7 = 0;	 Catch:{ all -> 0x02ba }
        r6[r7] = r4;	 Catch:{ all -> 0x02ba }
        r4 = 0;	 Catch:{ all -> 0x02ba }
        r7 = 0;	 Catch:{ all -> 0x02ba }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x02ba }
        if (r10 == 0) goto L_0x0215;
    L_0x00ff:
        r3 = r10.moveToFirst();	 Catch:{ all -> 0x02c9 }
        if (r3 == 0) goto L_0x0215;	 Catch:{ all -> 0x02c9 }
    L_0x0105:
        r3 = "_id";	 Catch:{ all -> 0x02c9 }
        r3 = r10.getColumnIndexOrThrow(r3);	 Catch:{ all -> 0x02c9 }
        r19 = r10.getInt(r3);	 Catch:{ all -> 0x02c9 }
        r19 = r25;	 Catch:{ all -> 0x02c9 }
        r3 = sSlotIndexToSubId;	 Catch:{ all -> 0x02c9 }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x02c9 }
        r9 = r3.get(r4);	 Catch:{ all -> 0x02c9 }
        r9 = (java.lang.Integer) r9;	 Catch:{ all -> 0x02c9 }
        if (r9 == 0) goto L_0x012c;	 Catch:{ all -> 0x02c9 }
    L_0x0120:
        r3 = r9.intValue();	 Catch:{ all -> 0x02c9 }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r3);	 Catch:{ all -> 0x02c9 }
        r3 = r3 ^ 1;	 Catch:{ all -> 0x02c9 }
        if (r3 == 0) goto L_0x02bf;	 Catch:{ all -> 0x02c9 }
    L_0x012c:
        r3 = sSlotIndexToSubId;	 Catch:{ all -> 0x02c9 }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x02c9 }
        r5 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x02c9 }
        r3.put(r4, r5);	 Catch:{ all -> 0x02c9 }
        r20 = r23.getActiveSubInfoCountMax();	 Catch:{ all -> 0x02c9 }
        r11 = r23.getDefaultSubId();	 Catch:{ all -> 0x02c9 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c9 }
        r3.<init>();	 Catch:{ all -> 0x02c9 }
        r4 = "[addSubInfoRecord] sSlotIndexToSubId.size=";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r4 = sSlotIndexToSubId;	 Catch:{ all -> 0x02c9 }
        r4 = r4.size();	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r4 = " slotIndex=";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r0 = r25;	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c9 }
        r4 = " subId=";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r0 = r25;	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c9 }
        r4 = " defaultSubId=";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r11);	 Catch:{ all -> 0x02c9 }
        r4 = " simCount=";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r0 = r20;	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c9 }
        r3 = r3.toString();	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c9 }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r11);	 Catch:{ all -> 0x02c9 }
        if (r3 == 0) goto L_0x019d;	 Catch:{ all -> 0x02c9 }
    L_0x0198:
        r3 = 1;	 Catch:{ all -> 0x02c9 }
        r0 = r20;	 Catch:{ all -> 0x02c9 }
        if (r0 != r3) goto L_0x01a4;	 Catch:{ all -> 0x02c9 }
    L_0x019d:
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r1 = r25;	 Catch:{ all -> 0x02c9 }
        r0.setDefaultFallbackSubId(r1);	 Catch:{ all -> 0x02c9 }
    L_0x01a4:
        r3 = 1;	 Catch:{ all -> 0x02c9 }
        r0 = r20;	 Catch:{ all -> 0x02c9 }
        if (r0 != r3) goto L_0x01e0;	 Catch:{ all -> 0x02c9 }
    L_0x01a9:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c9 }
        r3.<init>();	 Catch:{ all -> 0x02c9 }
        r4 = "[addSubInfoRecord] one sim set defaults to subId=";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r0 = r25;	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c9 }
        r3 = r3.toString();	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r1 = r25;	 Catch:{ all -> 0x02c9 }
        r0.setDefaultDataSubId(r1);	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r1 = r25;	 Catch:{ all -> 0x02c9 }
        r0.setDataSubId(r1);	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r1 = r25;	 Catch:{ all -> 0x02c9 }
        r0.setDefaultSmsSubId(r1);	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r1 = r25;	 Catch:{ all -> 0x02c9 }
        r0.setDefaultVoiceSubId(r1);	 Catch:{ all -> 0x02c9 }
    L_0x01e0:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02c9 }
        r3.<init>();	 Catch:{ all -> 0x02c9 }
        r4 = "[addSubInfoRecord] hashmap(";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r0 = r25;	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c9 }
        r4 = ",";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r0 = r25;	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02c9 }
        r4 = ")";	 Catch:{ all -> 0x02c9 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02c9 }
        r3 = r3.toString();	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c9 }
        r3 = r10.moveToNext();	 Catch:{ all -> 0x02c9 }
        if (r3 != 0) goto L_0x0105;
    L_0x0215:
        if (r10 == 0) goto L_0x021a;
    L_0x0217:
        r10.close();	 Catch:{ all -> 0x02ba }
    L_0x021a:
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r1 = r25;	 Catch:{ all -> 0x02ba }
        r19 = r0.getSubIdUsingPhoneId(r1);	 Catch:{ all -> 0x02ba }
        r3 = android.telephony.SubscriptionManager.isValidSubscriptionId(r19);	 Catch:{ all -> 0x02ba }
        if (r3 != 0) goto L_0x02d0;	 Catch:{ all -> 0x02ba }
    L_0x0228:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02ba }
        r3.<init>();	 Catch:{ all -> 0x02ba }
        r4 = "[addSubInfoRecord]- getSubId failed invalid subId = ";	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02ba }
        r0 = r19;	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02ba }
        r3 = r3.toString();	 Catch:{ all -> 0x02ba }
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r0.logdl(r3);	 Catch:{ all -> 0x02ba }
        r3 = -1;
        android.os.Binder.restoreCallingIdentity(r12);
        return r3;
    L_0x0248:
        r3 = 0;
        r19 = r10.getInt(r3);	 Catch:{ all -> 0x02b3 }
        r3 = 1;	 Catch:{ all -> 0x02b3 }
        r16 = r10.getInt(r3);	 Catch:{ all -> 0x02b3 }
        r3 = 2;	 Catch:{ all -> 0x02b3 }
        r14 = r10.getInt(r3);	 Catch:{ all -> 0x02b3 }
        r22 = new android.content.ContentValues;	 Catch:{ all -> 0x02b3 }
        r22.<init>();	 Catch:{ all -> 0x02b3 }
        r0 = r25;	 Catch:{ all -> 0x02b3 }
        r1 = r16;	 Catch:{ all -> 0x02b3 }
        if (r0 == r1) goto L_0x027b;	 Catch:{ all -> 0x02b3 }
    L_0x0262:
        r3 = "sim_id";	 Catch:{ all -> 0x02b3 }
        r4 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r0.put(r3, r4);	 Catch:{ all -> 0x02b3 }
        r3 = "network_mode";	 Catch:{ all -> 0x02b3 }
        r4 = -1;	 Catch:{ all -> 0x02b3 }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r0.put(r3, r4);	 Catch:{ all -> 0x02b3 }
    L_0x027b:
        r3 = 2;	 Catch:{ all -> 0x02b3 }
        if (r14 == r3) goto L_0x0280;	 Catch:{ all -> 0x02b3 }
    L_0x027e:
        r17 = 1;	 Catch:{ all -> 0x02b3 }
    L_0x0280:
        r3 = r22.size();	 Catch:{ all -> 0x02b3 }
        if (r3 <= 0) goto L_0x02a9;	 Catch:{ all -> 0x02b3 }
    L_0x0286:
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02b3 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02b3 }
        r4.<init>();	 Catch:{ all -> 0x02b3 }
        r5 = "_id=";	 Catch:{ all -> 0x02b3 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x02b3 }
        r0 = r19;	 Catch:{ all -> 0x02b3 }
        r6 = (long) r0;	 Catch:{ all -> 0x02b3 }
        r5 = java.lang.Long.toString(r6);	 Catch:{ all -> 0x02b3 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x02b3 }
        r4 = r4.toString();	 Catch:{ all -> 0x02b3 }
        r5 = 0;	 Catch:{ all -> 0x02b3 }
        r0 = r22;	 Catch:{ all -> 0x02b3 }
        r2.update(r3, r0, r4, r5);	 Catch:{ all -> 0x02b3 }
    L_0x02a9:
        r3 = "[addSubInfoRecord] Record already exists";	 Catch:{ all -> 0x02b3 }
        r0 = r23;	 Catch:{ all -> 0x02b3 }
        r0.logdl(r3);	 Catch:{ all -> 0x02b3 }
        goto L_0x00e3;
    L_0x02b3:
        r3 = move-exception;
        if (r10 == 0) goto L_0x02b9;
    L_0x02b6:
        r10.close();	 Catch:{ all -> 0x02ba }
    L_0x02b9:
        throw r3;	 Catch:{ all -> 0x02ba }
    L_0x02ba:
        r3 = move-exception;
        android.os.Binder.restoreCallingIdentity(r12);
        throw r3;
    L_0x02bf:
        r3 = "[addSubInfoRecord] currentSubId != null && currentSubId is valid, IGNORE";	 Catch:{ all -> 0x02c9 }
        r0 = r23;	 Catch:{ all -> 0x02c9 }
        r0.logdl(r3);	 Catch:{ all -> 0x02c9 }
        goto L_0x01e0;
    L_0x02c9:
        r3 = move-exception;
        if (r10 == 0) goto L_0x02cf;
    L_0x02cc:
        r10.close();	 Catch:{ all -> 0x02ba }
    L_0x02cf:
        throw r3;	 Catch:{ all -> 0x02ba }
    L_0x02d0:
        if (r17 == 0) goto L_0x032d;	 Catch:{ all -> 0x02ba }
    L_0x02d2:
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r3 = r0.mTelephonyManager;	 Catch:{ all -> 0x02ba }
        r0 = r19;	 Catch:{ all -> 0x02ba }
        r18 = r3.getSimOperatorName(r0);	 Catch:{ all -> 0x02ba }
        r3 = android.text.TextUtils.isEmpty(r18);	 Catch:{ all -> 0x02ba }
        if (r3 != 0) goto L_0x0358;	 Catch:{ all -> 0x02ba }
    L_0x02e2:
        r15 = r18;	 Catch:{ all -> 0x02ba }
    L_0x02e4:
        r22 = new android.content.ContentValues;	 Catch:{ all -> 0x02ba }
        r22.<init>();	 Catch:{ all -> 0x02ba }
        r3 = "display_name";	 Catch:{ all -> 0x02ba }
        r0 = r22;	 Catch:{ all -> 0x02ba }
        r0.put(r3, r15);	 Catch:{ all -> 0x02ba }
        r3 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02ba }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02ba }
        r4.<init>();	 Catch:{ all -> 0x02ba }
        r5 = "sim_id=";	 Catch:{ all -> 0x02ba }
        r4 = r4.append(r5);	 Catch:{ all -> 0x02ba }
        r0 = r19;	 Catch:{ all -> 0x02ba }
        r6 = (long) r0;	 Catch:{ all -> 0x02ba }
        r5 = java.lang.Long.toString(r6);	 Catch:{ all -> 0x02ba }
        r4 = r4.append(r5);	 Catch:{ all -> 0x02ba }
        r4 = r4.toString();	 Catch:{ all -> 0x02ba }
        r5 = 0;	 Catch:{ all -> 0x02ba }
        r0 = r22;	 Catch:{ all -> 0x02ba }
        r2.update(r3, r0, r4, r5);	 Catch:{ all -> 0x02ba }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02ba }
        r3.<init>();	 Catch:{ all -> 0x02ba }
        r4 = "[addSubInfoRecord] sim name = ";	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r15);	 Catch:{ all -> 0x02ba }
        r3 = r3.toString();	 Catch:{ all -> 0x02ba }
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r0.logdl(r3);	 Catch:{ all -> 0x02ba }
    L_0x032d:
        r3 = sPhones;	 Catch:{ all -> 0x02ba }
        r3 = r3[r25];	 Catch:{ all -> 0x02ba }
        r3.updateDataConnectionTracker();	 Catch:{ all -> 0x02ba }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02ba }
        r3.<init>();	 Catch:{ all -> 0x02ba }
        r4 = "[addSubInfoRecord]- info size=";	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02ba }
        r4 = sSlotIndexToSubId;	 Catch:{ all -> 0x02ba }
        r4 = r4.size();	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02ba }
        r3 = r3.toString();	 Catch:{ all -> 0x02ba }
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r0.logdl(r3);	 Catch:{ all -> 0x02ba }
        android.os.Binder.restoreCallingIdentity(r12);
        r3 = 0;
        return r3;
    L_0x0358:
        r0 = r23;	 Catch:{ all -> 0x02ba }
        r3 = r0.mTelephonyManager;	 Catch:{ all -> 0x02ba }
        r3 = r3.isMultiSimEnabled();	 Catch:{ all -> 0x02ba }
        if (r3 == 0) goto L_0x037e;	 Catch:{ all -> 0x02ba }
    L_0x0362:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02ba }
        r3.<init>();	 Catch:{ all -> 0x02ba }
        r4 = "CARD ";	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02ba }
        r4 = r25 + 1;	 Catch:{ all -> 0x02ba }
        r4 = java.lang.Integer.toString(r4);	 Catch:{ all -> 0x02ba }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02ba }
        r15 = r3.toString();	 Catch:{ all -> 0x02ba }
        goto L_0x02e4;	 Catch:{ all -> 0x02ba }
    L_0x037e:
        r15 = "CARD";	 Catch:{ all -> 0x02ba }
        goto L_0x02e4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int");
    }

    public boolean setPlmnSpn(int slotIndex, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int subId = getSubIdUsingPhoneId(slotIndex);
            if (this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) == null || (SubscriptionManager.isValidSubscriptionId(subId) ^ 1) != 0) {
                logd("[setPlmnSpn] No valid subscription to store info");
                notifySubscriptionInfoChanged();
                return false;
            }
            String carrierText = "";
            if (showPlmn) {
                carrierText = plmn;
                if (showSpn && !Objects.equals(spn, plmn)) {
                    carrierText = plmn + this.mContext.getString(17040233).toString() + spn;
                }
            } else if (showSpn) {
                carrierText = spn;
            }
            setCarrierText(carrierText, subId);
            return true;
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
        value.put("mcc", Integer.valueOf(mcc));
        value.put("mnc", Integer.valueOf(mnc));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Long.toString((long) subId), null);
        notifySubscriptionInfoChanged();
        return result;
    }

    public int getSlotIndex(int subId) {
        if (HwTelephonyFactory.getHwUiccManager().isUsingHwSubIdDesign()) {
            return getHwSlotId(subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotIndex]- subId invalid");
            return -1;
        } else if (sSlotIndexToSubId.size() == 0) {
            logd("[getSlotIndex]- size == 0, return SIM_NOT_INSERTED instead");
            return -1;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    return sim;
                }
            }
            logd("[getSlotIndex]- return fail");
            return -1;
        }
    }

    @Deprecated
    public int[] getSubId(int slotIndex) {
        if (HwTelephonyFactory.getHwUiccManager().isUsingHwSubIdDesign()) {
            return getHwSubId(slotIndex);
        }
        if (slotIndex == Integer.MAX_VALUE) {
            slotIndex = getSlotIndex(getDefaultSubId());
        }
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            logd("[getSubId]- invalid slotIndex=" + slotIndex);
            return null;
        } else if (sSlotIndexToSubId.size() == 0) {
            return getDummySubIds(slotIndex);
        } else {
            ArrayList<Integer> subIds = new ArrayList();
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int slot = ((Integer) entry.getKey()).intValue();
                int sub = ((Integer) entry.getValue()).intValue();
                if (slotIndex == slot) {
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
            logd("[getSubId]- numSubIds == 0, return DummySubIds slotIndex=" + slotIndex);
            return getDummySubIds(slotIndex);
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
        int phoneId;
        if (sSlotIndexToSubId.size() == 0) {
            phoneId = mDefaultPhoneId;
            logdl("[getPhoneId]- no sims, returning default phoneId=" + phoneId);
            return phoneId;
        }
        for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
            int sim = ((Integer) entry.getKey()).intValue();
            if (subId == ((Integer) entry.getValue()).intValue()) {
                return sim;
            }
        }
        phoneId = mDefaultPhoneId;
        logdl("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId);
        return phoneId;
    }

    private int[] getDummySubIds(int slotIndex) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] dummyValues = new int[numSubs];
        for (int i = 0; i < numSubs; i++) {
            dummyValues[i] = -2 - slotIndex;
        }
        return dummyValues;
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIndexToSubId.size();
            if (size == 0) {
                logdl("[clearSubInfo]- no simInfo size=" + size);
                return 0;
            }
            sSlotIndexToSubId.clear();
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
        if (this.mContext.getResources().getBoolean(17957047)) {
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
        Intent intent = new Intent("android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
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
        intent.addFlags(553648128);
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
        if (HwTelephonyFactory.getHwDataConnectionManager().isSlaveActive()) {
            logdl("slave in call, not allow setDefaultDataSubId");
            return;
        }
        String flexMapSupportType = SystemProperties.get("persist.radio.flexmap_type", "nw_mode");
        boolean isQcomPlat = HwModemCapability.isCapabilitySupport(9);
        if (SubscriptionManager.isValidSubscriptionId(subId) && ((!isQcomPlat || flexMapSupportType.equals("dds")) && (IS_FAST_SWITCH_SIMSLOT ^ 1) != 0)) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[len];
            boolean atLeastOneMatch = false;
            for (int phoneId = 0; phoneId < len; phoneId++) {
                int raf;
                int id = sPhones[phoneId].getSubId();
                if (id == subId) {
                    raf = proxyController.getMaxRafSupported();
                    atLeastOneMatch = true;
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
        if (4 == updateClatForMobile(subId)) {
            logd("set clat is error.");
        }
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
        intent.addFlags(553648128);
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
            MccTable.updateMccMncConfiguration(this.mContext, this.mTelephonyManager.getSimOperatorNumericForPhone(phoneId), false);
            Intent intent = new Intent("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED");
            intent.addFlags(553648128);
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
            return true;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSimSlotIndex();
                logdl("[shouldDefaultBeCleared] Record.id: " + id);
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return false;
                }
            }
            logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            return true;
        } else {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return false;
        }
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    public List<SubscriptionInfo> getSubInfoUsingSlotIndexWithCheck(int slotIndex, boolean needCheck, String callingPackage) {
        Throwable th;
        logd("[getSubInfoUsingSlotIndexWithCheck]+ slotIndex:" + slotIndex);
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIndexWithCheck")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        if (slotIndex == Integer.MAX_VALUE) {
            try {
                slotIndex = getSlotIndex(getDefaultSubId());
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            if (needCheck) {
                if ((isSubInfoReady() ^ 1) != 0) {
                    logd("[getSubInfoUsingSlotIndexWithCheck]- not ready");
                    Binder.restoreCallingIdentity(identity);
                    return null;
                }
            }
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
            List<SubscriptionInfo> subList = null;
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            ArrayList<SubscriptionInfo> subList2;
                            ArrayList<SubscriptionInfo> subList3 = subList2;
                            try {
                                SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                                if (subInfo != null) {
                                    if (subList3 == null) {
                                        subList2 = new ArrayList();
                                    } else {
                                        subList2 = subList3;
                                    }
                                    subList2.add(subInfo);
                                } else {
                                    subList2 = subList3;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                subList2 = subList3;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
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
            logd("[getSubInfoUsingSlotIndex]- null info return");
            Binder.restoreCallingIdentity(identity);
            return subList;
        }
        logd("[getSubInfoUsingSlotIndexWithCheck]- invalid slotIndex");
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
        Set<Entry<Integer, Integer>> simInfoSet = sSlotIndexToSubId.entrySet();
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
            return sSlotIndexToSubId.containsValue(Integer.valueOf(subId));
        }
        return false;
    }

    public int getSimStateForSlotIndex(int slotIndex) {
        State simState;
        String err;
        if (slotIndex < 0) {
            simState = State.UNKNOWN;
            err = "invalid slotIndex";
        } else {
            Phone phone = PhoneFactory.getPhone(slotIndex);
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
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIndexWithCheck")) {
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
        boolean first = true;
        for (StackTraceElement ste : re.getStackTrace()) {
            if (first) {
                first = false;
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
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                pw.println(" sSlotIndexToSubId[" + entry.getKey() + "]: subId=" + entry.getValue());
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
