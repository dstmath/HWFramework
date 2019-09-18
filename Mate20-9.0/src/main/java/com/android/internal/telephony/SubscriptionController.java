package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SubscriptionController extends AbstractSubscriptionController {
    public static final int CLAT_SET_ERROR = 4;
    public static final int CLAT_SET_FALSE = 1;
    public static final int CLAT_SET_NULL = 3;
    public static final int CLAT_SET_TRUE = 2;
    static final boolean DBG = true;
    static final boolean DBG_CACHE = false;
    private static final int DEPRECATED_SETTING = -1;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    static final String LOG_TAG = "SubscriptionController";
    static final int MAX_LOCAL_LOG_LINES = 500;
    private static final Comparator<SubscriptionInfo> SUBSCRIPTION_INFO_COMPARATOR = $$Lambda$SubscriptionController$Nt_ojdeqo4C2mbuwymYLvwgOLGo.INSTANCE;
    static final boolean VDBG = false;
    private static int mDefaultFallbackSubId = 0;
    private static int mDefaultPhoneId = 0;
    private static SubscriptionController sInstance = null;
    protected static Phone[] sPhones;
    private static Map<Integer, Integer> sSlotIndexToSubId = new ConcurrentHashMap();
    private int[] colorArr;
    private AppOpsManager mAppOps;
    protected CallManager mCM;
    private final List<SubscriptionInfo> mCacheActiveSubInfoList = new ArrayList();
    protected Context mContext;
    private HwCustSubscriptionController mHwSc = ((HwCustSubscriptionController) HwCustUtils.createObj(HwCustSubscriptionController.class, new Object[0]));
    private long mLastISubServiceRegTime;
    private ScLocalLog mLocalLog = new ScLocalLog(500);
    protected final Object mLock = new Object();
    protected TelephonyManager mTelephonyManager;

    static class ScLocalLog {
        private LinkedList<String> mLog = new LinkedList<>();

        public ScLocalLog(int maxLines) {
        }

        public synchronized void log(String msg) {
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            int i = 0;
            Iterator<String> itr = this.mLog.listIterator(0);
            while (itr.hasNext()) {
                StringBuilder sb = new StringBuilder();
                int i2 = i + 1;
                sb.append(Integer.toString(i));
                sb.append(": ");
                sb.append(itr.next());
                pw.println(sb.toString());
                if (i2 % 10 == 0) {
                    pw.flush();
                }
                i = i2;
            }
        }
    }

    static /* synthetic */ int lambda$static$0(SubscriptionInfo arg0, SubscriptionInfo arg1) {
        int flag = arg0.getSimSlotIndex() - arg1.getSimSlotIndex();
        if (flag == 0) {
            return arg0.getSubscriptionId() - arg1.getSubscriptionId();
        }
        return flag;
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
        migrateImsSettings();
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [com.android.internal.telephony.SubscriptionController, android.os.IBinder] */
    /* access modifiers changed from: protected */
    public void init(Context c) {
        this.mContext = c;
        this.mCM = CallManager.getInstance();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
            this.mLastISubServiceRegTime = System.currentTimeMillis();
        }
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            getQcRilHook();
        }
        logdl("[SubscriptionController] init by Context");
    }

    private boolean isSubInfoReady() {
        return sSlotIndexToSubId.size() > 0;
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [com.android.internal.telephony.SubscriptionController, android.os.IBinder] */
    private SubscriptionController(Phone phone) {
        this.mContext = phone.getContext();
        this.mCM = CallManager.getInstance();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            getQcRilHook();
        }
        migrateImsSettings();
        logdl("[SubscriptionController] init by Phone");
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void broadcastSimInfoContentChanged() {
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE"));
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
    }

    public void notifySubscriptionInfoChanged() {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifySubscriptionInfoChanged:");
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged();
    }

    private SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        UiccAccessRule[] uiccAccessRuleArr;
        Cursor cursor2 = cursor;
        int i = cursor2.getInt(cursor2.getColumnIndexOrThrow(HbpcdLookup.ID));
        String iccId = cursor2.getString(cursor2.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor2.getInt(cursor2.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor2.getString(cursor2.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor2.getInt(cursor2.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor2.getInt(cursor2.getColumnIndexOrThrow("color"));
        String number = cursor2.getString(cursor2.getColumnIndexOrThrow("number"));
        int dataRoaming = cursor2.getInt(cursor2.getColumnIndexOrThrow("data_roaming"));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302762);
        int mcc = cursor2.getInt(cursor2.getColumnIndexOrThrow("mcc"));
        int mnc = cursor2.getInt(cursor2.getColumnIndexOrThrow("mnc"));
        String cardId = cursor2.getString(cursor2.getColumnIndexOrThrow("card_id"));
        String countryIso = getSubscriptionCountryIso(simSlotIndex);
        boolean z = true;
        if (cursor2.getInt(cursor2.getColumnIndexOrThrow("is_embedded")) != 1) {
            z = false;
        }
        boolean isEmbedded = z;
        if (isEmbedded) {
            uiccAccessRuleArr = UiccAccessRule.decodeRules(cursor2.getBlob(cursor2.getColumnIndexOrThrow("access_rules")));
        } else {
            uiccAccessRuleArr = null;
        }
        UiccAccessRule[] accessRules = uiccAccessRuleArr;
        int status = cursor2.getInt(cursor2.getColumnIndexOrThrow("sub_state"));
        int nwMode = cursor2.getInt(cursor2.getColumnIndexOrThrow("network_mode"));
        String line1Number = this.mTelephonyManager.getLine1Number(simSlotIndex);
        if (!TextUtils.isEmpty(line1Number) && !line1Number.equals(number)) {
            number = line1Number;
        }
        String str = line1Number;
        int i2 = simSlotIndex;
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(simSlotIndex, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules, cardId, status, nwMode);
        return subscriptionInfo;
    }

    private String getSubscriptionCountryIso(int subId) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            return "";
        }
        return this.mTelephonyManager.getSimCountryIsoForPhone(phoneId);
    }

    private List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        String[] selectionArgs = null;
        if (queryKey != null) {
            selectionArgs = new String[]{queryKey.toString()};
        }
        ArrayList<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList == null) {
                            subList = new ArrayList<>();
                        }
                        subList.add(subInfo);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
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
        this.colorArr = this.mContext.getResources().getIntArray(17236076);
        int colorIdx = 0;
        if (availableSubInfos != null) {
            int i = 0;
            while (i < this.colorArr.length) {
                int j = 0;
                while (j < availableSubInfos.size() && this.colorArr[i] != availableSubInfos.get(j).getIconTint()) {
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
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, "getActiveSubscriptionInfo")) {
            return null;
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId) || !isSubInfoReady()) {
            logd("[getSubInfoUsingSubIdx]- invalid subId or not ready = " + subId);
            return null;
        }
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
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        SubscriptionInfo si = getActiveSubscriptionInfoForIccIdInternal(iccId);
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, si != null ? si.getSubscriptionId() : -1, callingPackage, "getActiveSubscriptionInfoForIccId")) {
            return null;
        }
        return si;
    }

    private SubscriptionInfo getActiveSubscriptionInfoForIccIdInternal(String iccId) {
        if (iccId == null) {
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
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETACTIVESUBSCRIPTIONINFOFORSIMSLOTINDEX);
        }
        Phone phone = PhoneFactory.getPhone(slotIndex);
        if (phone == null) {
            loge("[getActiveSubscriptionInfoForSimSlotIndex] no phone, slotIndex=" + slotIndex);
            return null;
        } else if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, phone.getSubId(), callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        } else {
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
    }

    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        logd("[getAllSubInfoList]+");
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, callingPackage, "getAllSubInfoList")) {
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
            return subList;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        boolean canReadAllPhoneState;
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETACTIVESUBSCRIPTIONINFOLIST);
        }
        if (!isSubInfoReady()) {
            logdl("[getActiveSubInfoList] Sub Controller not ready");
            return null;
        }
        try {
            canReadAllPhoneState = TelephonyPermissions.checkReadPhoneState(this.mContext, -1, Binder.getCallingPid(), Binder.getCallingUid(), callingPackage, "getActiveSubscriptionInfoList");
        } catch (SecurityException e) {
            canReadAllPhoneState = false;
        }
        synchronized (this.mCacheActiveSubInfoList) {
            if (canReadAllPhoneState) {
                try {
                    ArrayList arrayList = new ArrayList(this.mCacheActiveSubInfoList);
                    return arrayList;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                List<SubscriptionInfo> list = (List) this.mCacheActiveSubInfoList.stream().filter(new Predicate(callingPackage) {
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final boolean test(Object obj) {
                        return SubscriptionController.lambda$getActiveSubscriptionInfoList$1(SubscriptionController.this, this.f$1, (SubscriptionInfo) obj);
                    }
                }).collect(Collectors.toList());
                return list;
            }
        }
    }

    public static /* synthetic */ boolean lambda$getActiveSubscriptionInfoList$1(SubscriptionController subscriptionController, String callingPackage, SubscriptionInfo subscriptionInfo) {
        try {
            return TelephonyPermissions.checkCallingOrSelfReadPhoneState(subscriptionController.mContext, subscriptionInfo.getSubscriptionId(), callingPackage, "getActiveSubscriptionInfoList");
        } catch (SecurityException e) {
            return false;
        }
    }

    @VisibleForTesting
    public void refreshCachedActiveSubscriptionInfoList() {
        if (isSubInfoReady()) {
            synchronized (this.mCacheActiveSubInfoList) {
                this.mCacheActiveSubInfoList.clear();
                List<SubscriptionInfo> activeSubscriptionInfoList = getSubInfo("sim_id>=0", null);
                if (activeSubscriptionInfoList != null) {
                    this.mCacheActiveSubInfoList.addAll(activeSubscriptionInfoList);
                }
            }
        }
    }

    public int getActiveSubInfoCount(String callingPackage) {
        List<SubscriptionInfo> records = getActiveSubscriptionInfoList(callingPackage);
        if (records == null) {
            return 0;
        }
        return records.size();
    }

    public int getAllSubInfoCount(String callingPackage) {
        Cursor cursor;
        logd("[getAllSubInfoCount]+");
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, callingPackage, "getAllSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
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
            throw th;
        }
    }

    public int getActiveSubInfoCountMax() {
        return this.mTelephonyManager.getSimCount();
    }

    public List<SubscriptionInfo> getAvailableSubscriptionInfoList(String callingPackage) {
        if (TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, callingPackage, "getAvailableSubscriptionInfoList")) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (!((EuiccManager) this.mContext.getSystemService("euicc")).isEnabled()) {
                    logdl("[getAvailableSubInfoList] Embedded subscriptions are disabled");
                    return null;
                }
                List<SubscriptionInfo> subList = getSubInfo("sim_id>=0 OR is_embedded=1", null);
                if (subList != null) {
                    subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
                } else {
                    logdl("[getAvailableSubInfoList]- no info return");
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new SecurityException("Need READ_PHONE_STATE to call  getAvailableSubscriptionInfoList");
        }
    }

    public List<SubscriptionInfo> getAccessibleSubscriptionInfoList(String callingPackage) {
        if (!((EuiccManager) this.mContext.getSystemService("euicc")).isEnabled()) {
            logdl("[getAccessibleSubInfoList] Embedded subscriptions are disabled");
            return null;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo("is_embedded=1", null);
            if (subList != null) {
                return (List) subList.stream().filter(new Predicate(callingPackage) {
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final boolean test(Object obj) {
                        return ((SubscriptionInfo) obj).canManageSubscription(SubscriptionController.this.mContext, this.f$1);
                    }
                }).sorted(SUBSCRIPTION_INFO_COMPARATOR).collect(Collectors.toList());
            }
            logdl("[getAccessibleSubInfoList] No info returned");
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public List<SubscriptionInfo> getSubscriptionInfoListForEmbeddedSubscriptionUpdate(String[] embeddedIccids, boolean isEuiccRemovable) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        whereClause.append("is_embedded");
        whereClause.append("=1");
        if (isEuiccRemovable) {
            whereClause.append(" AND ");
            whereClause.append("is_removable");
            whereClause.append("=1");
        }
        whereClause.append(") OR ");
        whereClause.append("icc_id");
        whereClause.append(" IN (");
        for (int i = 0; i < embeddedIccids.length; i++) {
            if (i > 0) {
                whereClause.append(",");
            }
            whereClause.append("\"");
            whereClause.append(embeddedIccids[i]);
            whereClause.append("\"");
        }
        whereClause.append(")");
        List<SubscriptionInfo> list = getSubInfo(whereClause.toString(), null);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void requestEmbeddedSubscriptionInfoListRefresh() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS", "requestEmbeddedSubscriptionInfoListRefresh");
        long token = Binder.clearCallingIdentity();
        try {
            PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(callback);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x026a, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0342, code lost:
        android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x0345, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x003c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0264, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0265, code lost:
        if (r7 != null) goto L_0x0267;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:3:0x0034, B:74:0x0184] */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x026d A[Catch:{ all -> 0x0264, all -> 0x003c }] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x027a A[Catch:{ all -> 0x0264, all -> 0x003c }] */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0294  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x033e A[SYNTHETIC, Splitter:B:128:0x033e] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0101 A[Catch:{ all -> 0x013a }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0127 A[Catch:{ all -> 0x0134 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0167 A[SYNTHETIC, Splitter:B:69:0x0167] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0184 A[SYNTHETIC, Splitter:B:74:0x0184] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x021b A[Catch:{ all -> 0x0264, all -> 0x003c }] */
    public int addSubInfoRecord(String iccId, int slotIndex) {
        Cursor cursor;
        boolean setDisplayName;
        String[] strArr;
        Cursor cursor2;
        int subId;
        String nameToSet;
        int defaultSubId;
        int i;
        String[] strArr2;
        boolean setDisplayName2;
        boolean setDisplayName3;
        String str = iccId;
        int i2 = slotIndex;
        logdl("[addSubInfoRecord]+ iccId:" + SubscriptionInfo.givePrintableIccid(iccId) + " slotIndex:" + i2);
        enforceModifyPhoneState("addSubInfoRecord");
        long identity = Binder.clearCallingIdentity();
        if (str == null) {
            logdl("[addSubInfoRecord]- null iccId");
            Binder.restoreCallingIdentity(identity);
            return -1;
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        Cursor cursor3 = resolver.query(SubscriptionManager.CONTENT_URI, new String[]{HbpcdLookup.ID, "sim_id", "name_source", "icc_id", "card_id"}, "icc_id=? OR icc_id=?", new String[]{str, IccUtils.getDecimalSubstring(iccId)}, null);
        if (cursor3 != null) {
            try {
                if (!cursor3.moveToFirst()) {
                    strArr2 = null;
                } else {
                    int subId2 = cursor3.getInt(0);
                    int oldSimInfoId = cursor3.getInt(1);
                    int nameSource = cursor3.getInt(2);
                    String oldIccId = cursor3.getString(3);
                    String oldCardId = cursor3.getString(4);
                    ContentValues value = new ContentValues();
                    if (i2 != oldSimInfoId) {
                        value.put("sim_id", Integer.valueOf(slotIndex));
                        setDisplayName2 = false;
                        try {
                            value.put("network_mode", -1);
                        } catch (Throwable th) {
                            th = th;
                            cursor = cursor3;
                        }
                    } else {
                        setDisplayName2 = false;
                    }
                    if (nameSource != 2) {
                        setDisplayName3 = true;
                    } else {
                        setDisplayName3 = setDisplayName2;
                    }
                    if (oldIccId != null) {
                        try {
                            if (oldIccId.length() < iccId.length() && oldIccId.equals(IccUtils.getDecimalSubstring(iccId))) {
                                value.put("icc_id", str);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            cursor = cursor3;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    }
                    try {
                        UiccCard card = UiccController.getInstance().getUiccCardForPhone(i2);
                        if (card != null) {
                            String cardId = card.getCardId();
                            if (cardId != null && !cardId.equals(oldCardId)) {
                                UiccCard uiccCard = card;
                                value.put("card_id", cardId);
                                if (value.size() <= 0) {
                                    Uri uri = SubscriptionManager.CONTENT_URI;
                                    StringBuilder sb = new StringBuilder();
                                    setDisplayName = setDisplayName3;
                                    try {
                                        sb.append("_id=");
                                        int i3 = oldSimInfoId;
                                        int i4 = nameSource;
                                        sb.append(Long.toString((long) subId2));
                                        String sb2 = sb.toString();
                                        strArr = null;
                                        resolver.update(uri, value, sb2, null);
                                        refreshCachedActiveSubscriptionInfoList();
                                    } catch (Throwable th3) {
                                        th = th3;
                                        cursor = cursor3;
                                        boolean z = setDisplayName;
                                        if (cursor != null) {
                                        }
                                        throw th;
                                    }
                                } else {
                                    setDisplayName = setDisplayName3;
                                    int i5 = oldSimInfoId;
                                    int i6 = nameSource;
                                    strArr = null;
                                }
                                logdl("[addSubInfoRecord] Record already exists");
                                if (cursor3 != null) {
                                    cursor3.close();
                                }
                                Cursor cursor4 = cursor3;
                                cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
                                if (cursor2 != null) {
                                    if (cursor2.moveToFirst()) {
                                        do {
                                            int i7 = cursor2.getInt(cursor2.getColumnIndexOrThrow(HbpcdLookup.ID));
                                            int subId3 = i2;
                                            Integer currentSubId = sSlotIndexToSubId.get(Integer.valueOf(slotIndex));
                                            if (currentSubId != null && currentSubId.intValue() == subId3) {
                                                if (SubscriptionManager.isValidSubscriptionId(currentSubId.intValue())) {
                                                    logdl("[addSubInfoRecord] currentSubId != null && currentSubId is valid, IGNORE");
                                                    logdl("[addSubInfoRecord] hashmap(" + i2 + "," + subId3 + ")");
                                                }
                                            }
                                            sSlotIndexToSubId.put(Integer.valueOf(slotIndex), Integer.valueOf(subId3));
                                            int subIdCountMax = getActiveSubInfoCountMax();
                                            logdl("[addSubInfoRecord] sSlotIndexToSubId.size=" + sSlotIndexToSubId.size() + " slotIndex=" + i2 + " subId=" + subId3 + " defaultSubId=" + defaultSubId + " simCount=" + subIdCountMax);
                                            if (SubscriptionManager.isValidSubscriptionId(defaultSubId)) {
                                                i = 1;
                                                if (subIdCountMax == 1) {
                                                }
                                                if (subIdCountMax == i) {
                                                    logdl("[addSubInfoRecord] one sim set defaults to subId=" + subId3);
                                                    setDefaultDataSubId(subId3);
                                                    setDataSubId(subId3);
                                                    setDefaultSmsSubId(subId3);
                                                    setDefaultVoiceSubId(subId3);
                                                }
                                                logdl("[addSubInfoRecord] hashmap(" + i2 + "," + subId3 + ")");
                                            } else {
                                                i = 1;
                                            }
                                            setDefaultFallbackSubId(subId3);
                                            if (subIdCountMax == i) {
                                            }
                                            logdl("[addSubInfoRecord] hashmap(" + i2 + "," + subId3 + ")");
                                        } while (cursor2.moveToNext() != 0);
                                    }
                                }
                                if (cursor2 != null) {
                                    cursor2.close();
                                }
                                subId = getSubIdUsingPhoneId(i2);
                                if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                                    logdl("[addSubInfoRecord]- getSubId failed invalid subId = " + subId);
                                    Binder.restoreCallingIdentity(identity);
                                    return -1;
                                }
                                if (setDisplayName) {
                                    String simCarrierName = this.mTelephonyManager.getSimOperatorName(subId);
                                    if (!TextUtils.isEmpty(simCarrierName)) {
                                        nameToSet = simCarrierName;
                                    } else if (this.mTelephonyManager.isMultiSimEnabled()) {
                                        nameToSet = "CARD " + Integer.toString(i2 + 1);
                                    } else {
                                        nameToSet = "CARD";
                                        ContentValues value2 = new ContentValues();
                                        value2.put("display_name", nameToSet);
                                        resolver.update(SubscriptionManager.CONTENT_URI, value2, "sim_id=" + Long.toString((long) subId), strArr);
                                        refreshCachedActiveSubscriptionInfoList();
                                        logdl("[addSubInfoRecord] sim name = " + nameToSet);
                                    }
                                    ContentValues value22 = new ContentValues();
                                    value22.put("display_name", nameToSet);
                                    resolver.update(SubscriptionManager.CONTENT_URI, value22, "sim_id=" + Long.toString((long) subId), strArr);
                                    refreshCachedActiveSubscriptionInfoList();
                                    logdl("[addSubInfoRecord] sim name = " + nameToSet);
                                } else if (this.mCacheActiveSubInfoList.isEmpty()) {
                                    logdl("[addSubInfoRecord] need to refresh empty cache if setDisplayName is false");
                                    refreshCachedActiveSubscriptionInfoList();
                                }
                                sPhones[i2].updateDataConnectionTracker();
                                logdl("[addSubInfoRecord]- info size=" + sSlotIndexToSubId.size());
                                Binder.restoreCallingIdentity(identity);
                                return 0;
                            }
                        }
                        if (value.size() <= 0) {
                        }
                        logdl("[addSubInfoRecord] Record already exists");
                        if (cursor3 != null) {
                        }
                        Cursor cursor42 = cursor3;
                        cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
                        if (cursor2 != null) {
                        }
                        if (cursor2 != null) {
                        }
                        subId = getSubIdUsingPhoneId(i2);
                        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        boolean z2 = setDisplayName3;
                        cursor = cursor3;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                cursor = cursor3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } else {
            strArr2 = null;
        }
        try {
            Uri uri2 = insertEmptySubInfoRecord(iccId, slotIndex);
            logdl("[addSubInfoRecord] New record created: " + uri2);
            setDisplayName = true;
            if (cursor3 != null) {
            }
            Cursor cursor422 = cursor3;
            cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
            if (cursor2 != null) {
            }
            if (cursor2 != null) {
            }
            subId = getSubIdUsingPhoneId(i2);
            if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            }
        } catch (Throwable th6) {
            th = th6;
            cursor = cursor3;
            if (cursor != null) {
            }
            throw th;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public Uri insertEmptySubInfoRecord(String iccId, int slotIndex) {
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        value.put("icc_id", iccId);
        value.put("color", Integer.valueOf(getUnusedColor(this.mContext.getOpPackageName())));
        value.put("sim_id", Integer.valueOf(slotIndex));
        value.put("carrier_name", "");
        UiccCard card = UiccController.getInstance().getUiccCardForPhone(slotIndex);
        if (card != null) {
            String cardId = card.getCardId();
            if (cardId != null) {
                value.put("card_id", cardId);
            } else {
                value.put("card_id", iccId);
            }
        } else {
            value.put("card_id", iccId);
        }
        Uri uri = resolver.insert(SubscriptionManager.CONTENT_URI, value);
        refreshCachedActiveSubscriptionInfoList();
        return uri;
    }

    public boolean setPlmnSpn(int slotIndex, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int subId = getSubIdUsingPhoneId(slotIndex);
            if (this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) != null) {
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    String carrierText = "";
                    if (showPlmn) {
                        carrierText = plmn;
                        if (showSpn && !Objects.equals(spn, plmn)) {
                            String separator = this.mContext.getString(17040305).toString();
                            carrierText = carrierText + separator + spn;
                        }
                    } else if (showSpn) {
                        carrierText = spn;
                    }
                    setCarrierText(carrierText, subId);
                    return true;
                }
            }
            logd("[setPlmnSpn] No valid subscription to store info");
            notifySubscriptionInfoChanged();
            return false;
        }
    }

    private int setCarrierText(String text, int subId) {
        logd("[setCarrierText]+ text:" + text + " subId:" + subId);
        enforceModifyPhoneState("setCarrierText");
        long identity = Binder.clearCallingIdentity();
        try {
            ContentValues value = new ContentValues(1);
            value.put("carrier_name", text);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SubscriptionManager.CONTENT_URI;
            int result = contentResolver.update(uri, value, "sim_id=" + Long.toString((long) subId), null);
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
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SubscriptionManager.CONTENT_URI;
            int result = contentResolver.update(uri, value, "sim_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
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
        String nameToSet;
        logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        enforceModifyPhoneState("setDisplayNameUsingSrc");
        long identity = Binder.clearCallingIdentity();
        try {
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
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SubscriptionManager.CONTENT_URI;
            int result = contentResolver.update(uri, value, "sim_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
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
                    ContentResolver contentResolver = this.mContext.getContentResolver();
                    Uri uri = SubscriptionManager.CONTENT_URI;
                    int result = contentResolver.update(uri, value, "sim_id=" + Long.toString((long) subId), null);
                    refreshCachedActiveSubscriptionInfoList();
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
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SubscriptionManager.CONTENT_URI;
            int result = contentResolver.update(uri, value, "sim_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
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
        refreshCachedActiveSubscriptionInfoList();
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
            for (Map.Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int sim = entry.getKey().intValue();
                if (subId == entry.getValue().intValue()) {
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
            ArrayList<Integer> subIds = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int slot = entry.getKey().intValue();
                int sub = entry.getValue().intValue();
                if (slotIndex == slot) {
                    subIds.add(Integer.valueOf(sub));
                }
            }
            int numSubIds = subIds.size();
            if (numSubIds > 0) {
                int[] subIdArr = new int[numSubIds];
                for (int i = 0; i < numSubIds; i++) {
                    subIdArr[i] = subIds.get(i).intValue();
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
        if (sSlotIndexToSubId.size() == 0) {
            int phoneId = mDefaultPhoneId;
            logdl("[getPhoneId]- no sims, returning default phoneId=" + phoneId);
            return phoneId;
        }
        for (Map.Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
            int sim = entry.getKey().intValue();
            if (subId == entry.getValue().intValue()) {
                return sim;
            }
        }
        int phoneId2 = mDefaultPhoneId;
        logdl("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId2);
        return phoneId2;
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
        if (this.mContext.getResources().getBoolean(17957068)) {
            subId = getDefaultVoiceSubId();
        } else {
            subId = getDefaultDataSubId();
        }
        if (!isActiveSubId(subId)) {
            return mDefaultFallbackSubId;
        }
        return subId;
    }

    public void setDefaultSmsSubId(int subId) {
        enforceModifyPhoneState("setDefaultSmsSubId");
        if (subId != Integer.MAX_VALUE) {
            logdl("[setDefaultSmsSubId] subId=" + subId);
            Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms", subId);
            broadcastDefaultSmsSubIdChanged(subId);
            return;
        }
        throw new RuntimeException("setDefaultSmsSubId called with DEFAULT_SUB_ID");
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
        return Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
    }

    public void setDefaultVoiceSubId(int subId) {
        enforceModifyPhoneState("setDefaultVoiceSubId");
        if (subId != Integer.MAX_VALUE) {
            logdl("[setDefaultVoiceSubId] subId=" + subId);
            Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_call", subId);
            broadcastDefaultVoiceSubIdChanged(subId);
            return;
        }
        throw new RuntimeException("setDefaultVoiceSubId called with DEFAULT_SUB_ID");
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void broadcastDefaultVoiceSubIdChanged(int subId) {
        logdl("[broadcastDefaultVoiceSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultVoiceSubId() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_call", -1);
    }

    public int getDefaultDataSubId() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", 0);
    }

    public void setDefaultDataSubId(int subId) {
        int raf;
        enforceModifyPhoneState("setDefaultDataSubId");
        if (subId != Integer.MAX_VALUE) {
            logdl("[setDefaultDataSubId] subId=" + subId);
            if (this.mHwSc != null && this.mHwSc.isBlockSetDataSub(subId)) {
                logd("[setDefaultDataSubId] ais custom version. block switch to non ais card.");
            } else if (TelephonyManager.getDefault().getSimState(subId) == 5 && getSubState(subId) == 0) {
                logd("[setDefaultDataSubId] subId(" + subId + ") is ready but inactive, not set, return.");
            } else {
                ProxyController proxyController = ProxyController.getInstance();
                int len = sPhones.length;
                logdl("[setDefaultDataSubId] num phones=" + len + ", subId=" + subId);
                if (HwTelephonyFactory.getHwDataConnectionManager().isSlaveActive()) {
                    logdl("slave in call, not allow setDefaultDataSubId");
                    return;
                }
                String flexMapSupportType = SystemProperties.get("persist.radio.flexmap_type", "nw_mode");
                boolean isHisiPlat = HuaweiTelephonyConfigs.isHisiPlatform();
                if (SubscriptionManager.isValidSubscriptionId(subId) && ((isHisiPlat || flexMapSupportType.equals("dds")) && !IS_FAST_SWITCH_SIMSLOT)) {
                    RadioAccessFamily[] rafs = new RadioAccessFamily[len];
                    boolean atLeastOneMatch = false;
                    for (int phoneId = 0; phoneId < len; phoneId++) {
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
                checkNeedSetMainSlotByPid(subId, Binder.getCallingPid());
                if (!HwTelephonyFactory.getHwUiccManager().get4GSlotInSwitchProgress()) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
                    broadcastDefaultDataSubIdChanged(subId);
                }
            }
        } else {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
    }

    private void updateAllDataConnectionTrackers() {
        int len;
        logdl("[updateAllDataConnectionTrackers] sPhones.length=" + len);
        for (Phone updateDataConnectionTracker : sPhones) {
            logdl("[updateAllDataConnectionTrackers] phoneId=" + phoneId);
            updateDataConnectionTracker.updateDataConnectionTracker();
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
        if (subId != Integer.MAX_VALUE) {
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
                return;
            }
            return;
        }
        throw new RuntimeException("setDefaultSubId called with DEFAULT_SUB_ID");
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
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        logdl("[shouldDefaultBeCleared: subId] " + subId);
        if (records == null) {
            logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            return true;
        } else if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return false;
        } else {
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
        }
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    @VisibleForTesting
    public List<SubscriptionInfo> getSubInfoUsingSlotIndexPrivileged(int slotIndex, boolean needCheck) {
        logd("[getSubInfoUsingSlotIndexPrivileged]+ slotIndex:" + slotIndex);
        if (slotIndex == Integer.MAX_VALUE) {
            slotIndex = getSlotIndex(getDefaultSubId());
        }
        ArrayList<SubscriptionInfo> subList = null;
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            logd("[getSubInfoUsingSlotIndexPrivileged]- invalid slotIndex");
            return null;
        } else if (!needCheck || isSubInfoReady()) {
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                            if (subInfo != null) {
                                if (subList == null) {
                                    subList = new ArrayList<>();
                                }
                                subList.add(subInfo);
                            }
                        } while (cursor.moveToNext());
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getSubInfoUsingSlotIndex]- null info return");
            return subList;
        } else {
            logd("[getSubInfoUsingSlotIndexPrivileged]- not ready");
            return null;
        }
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
        Set<Map.Entry<Integer, Integer>> simInfoSet = new HashSet<>(sSlotIndexToSubId.entrySet());
        int[] subIdArr = new int[simInfoSet.size()];
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : simInfoSet) {
            subIdArr[i] = entry.getValue().intValue();
            i++;
        }
        return subIdArr;
    }

    public boolean isActiveSubId(int subId) {
        return SubscriptionManager.isValidSubscriptionId(subId) && sSlotIndexToSubId.containsValue(Integer.valueOf(subId));
    }

    public int getSimStateForSlotIndex(int slotIndex) {
        IccCardConstants.State simState;
        String err;
        if (slotIndex < 0) {
            err = "invalid slotIndex";
            simState = IccCardConstants.State.UNKNOWN;
        } else {
            IccCardConstants.State simState2 = PhoneFactory.getPhone(slotIndex);
            if (simState2 == null) {
                simState = IccCardConstants.State.UNKNOWN;
                err = "phone == null";
            } else {
                IccCard icc = simState2.getIccCard();
                if (icc == null) {
                    simState = IccCardConstants.State.UNKNOWN;
                    err = "icc == null";
                } else {
                    simState = icc.getState();
                    err = "";
                }
            }
        }
        String str = err;
        return simState.ordinal();
    }

    public void setSubscriptionProperty(int subId, String propKey, String propValue) {
        enforceModifyPhoneState("setSubscriptionProperty");
        long token = Binder.clearCallingIdentity();
        ContentResolver resolver = this.mContext.getContentResolver();
        setSubscriptionPropertyIntoSettingsGlobal(subId, propKey, propValue);
        setSubscriptionPropertyIntoContentResolver(subId, propKey, propValue, resolver);
        refreshCachedActiveSubscriptionInfoList();
        Binder.restoreCallingIdentity(token);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static void setSubscriptionPropertyIntoContentResolver(int subId, String propKey, String propValue, ContentResolver resolver) {
        char c;
        ContentValues value = new ContentValues();
        switch (propKey.hashCode()) {
            case -2000412720:
                if (propKey.equals("enable_alert_vibrate")) {
                    c = 6;
                    break;
                }
            case -1950380197:
                if (propKey.equals("volte_vt_enabled")) {
                    c = 12;
                    break;
                }
            case -1555340190:
                if (propKey.equals("enable_cmas_extreme_threat_alerts")) {
                    c = 0;
                    break;
                }
            case -1433878403:
                if (propKey.equals("enable_cmas_test_alerts")) {
                    c = 10;
                    break;
                }
            case -1390801311:
                if (propKey.equals("enable_alert_speech")) {
                    c = 7;
                    break;
                }
            case -1218173306:
                if (propKey.equals("wfc_ims_enabled")) {
                    c = 14;
                    break;
                }
            case -461686719:
                if (propKey.equals("enable_emergency_alerts")) {
                    c = 3;
                    break;
                }
            case -420099376:
                if (propKey.equals("vt_ims_enabled")) {
                    c = 13;
                    break;
                }
            case -349439993:
                if (propKey.equals("alert_sound_duration")) {
                    c = 4;
                    break;
                }
            case 180938212:
                if (propKey.equals("wfc_ims_roaming_mode")) {
                    c = 16;
                    break;
                }
            case 203677434:
                if (propKey.equals("enable_cmas_amber_alerts")) {
                    c = 2;
                    break;
                }
            case 240841894:
                if (propKey.equals("show_cmas_opt_out_dialog")) {
                    c = 11;
                    break;
                }
            case 407275608:
                if (propKey.equals("enable_cmas_severe_threat_alerts")) {
                    c = 1;
                    break;
                }
            case 462555599:
                if (propKey.equals("alert_reminder_interval")) {
                    c = 5;
                    break;
                }
            case 1270593452:
                if (propKey.equals("enable_etws_test_alerts")) {
                    c = 8;
                    break;
                }
            case 1288054979:
                if (propKey.equals("enable_channel_50_alerts")) {
                    c = 9;
                    break;
                }
            case 1334635646:
                if (propKey.equals("wfc_ims_mode")) {
                    c = 15;
                    break;
                }
            case 1604840288:
                if (propKey.equals("wfc_ims_roaming_enabled")) {
                    c = 17;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                value.put(propKey, Integer.valueOf(Integer.parseInt(propValue)));
                break;
            default:
                slogd("Invalid column name");
                break;
        }
        Uri uri = SubscriptionManager.CONTENT_URI;
        resolver.update(uri, value, "sim_id=" + Integer.toString(subId), null);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0117, code lost:
        r8 = 65535;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0118, code lost:
        switch(r8) {
            case 0: goto L_0x011e;
            case 1: goto L_0x011e;
            case 2: goto L_0x011e;
            case 3: goto L_0x011e;
            case 4: goto L_0x011e;
            case 5: goto L_0x011e;
            case 6: goto L_0x011e;
            case 7: goto L_0x011e;
            case 8: goto L_0x011e;
            case 9: goto L_0x011e;
            case 10: goto L_0x011e;
            case 11: goto L_0x011e;
            case 12: goto L_0x011e;
            case 13: goto L_0x011e;
            case 14: goto L_0x011e;
            case 15: goto L_0x011e;
            case 16: goto L_0x011e;
            case 17: goto L_0x011e;
            default: goto L_0x011b;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x011e, code lost:
        r0 = r2.getInt(0) + "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0135, code lost:
        logd("Invalid column name");
     */
    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, "getSubscriptionProperty")) {
            return null;
        }
        String resultValue = getSubscriptionPropertyFromSettingsGlobal(subId, propKey);
        if (resultValue != null) {
            return resultValue;
        }
        char c = 1;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{propKey}, "sim_id=?", new String[]{subId + ""}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    switch (propKey.hashCode()) {
                        case -2000412720:
                            if (propKey.equals("enable_alert_vibrate")) {
                                c = 6;
                                break;
                            }
                        case -1950380197:
                            if (propKey.equals("volte_vt_enabled")) {
                                c = 12;
                                break;
                            }
                        case -1555340190:
                            if (propKey.equals("enable_cmas_extreme_threat_alerts")) {
                                c = 0;
                                break;
                            }
                        case -1433878403:
                            if (propKey.equals("enable_cmas_test_alerts")) {
                                c = 10;
                                break;
                            }
                        case -1390801311:
                            if (propKey.equals("enable_alert_speech")) {
                                c = 7;
                                break;
                            }
                        case -1218173306:
                            if (propKey.equals("wfc_ims_enabled")) {
                                c = 14;
                                break;
                            }
                        case -461686719:
                            if (propKey.equals("enable_emergency_alerts")) {
                                c = 3;
                                break;
                            }
                        case -420099376:
                            if (propKey.equals("vt_ims_enabled")) {
                                c = 13;
                                break;
                            }
                        case -349439993:
                            if (propKey.equals("alert_sound_duration")) {
                                c = 4;
                                break;
                            }
                        case 180938212:
                            if (propKey.equals("wfc_ims_roaming_mode")) {
                                c = 16;
                                break;
                            }
                        case 203677434:
                            if (propKey.equals("enable_cmas_amber_alerts")) {
                                c = 2;
                                break;
                            }
                        case 240841894:
                            if (propKey.equals("show_cmas_opt_out_dialog")) {
                                c = 11;
                                break;
                            }
                        case 407275608:
                            if (propKey.equals("enable_cmas_severe_threat_alerts")) {
                                break;
                            }
                        case 462555599:
                            if (propKey.equals("alert_reminder_interval")) {
                                c = 5;
                                break;
                            }
                        case 1270593452:
                            if (propKey.equals("enable_etws_test_alerts")) {
                                c = 8;
                                break;
                            }
                        case 1288054979:
                            if (propKey.equals("enable_channel_50_alerts")) {
                                c = 9;
                                break;
                            }
                        case 1334635646:
                            if (propKey.equals("wfc_ims_mode")) {
                                c = 15;
                                break;
                            }
                        case 1604840288:
                            if (propKey.equals("wfc_ims_roaming_enabled")) {
                                c = 17;
                                break;
                            }
                    }
                } else {
                    logd("Valid row not present in db");
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            logd("Query failed");
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
            pw.println(" mLastISubServiceRegTime=" + this.mLastISubServiceRegTime);
            pw.println(" defaultSubId=" + getDefaultSubId());
            pw.println(" defaultDataSubId=" + getDefaultDataSubId());
            pw.println(" defaultVoiceSubId=" + getDefaultVoiceSubId());
            pw.println(" defaultSmsSubId=" + getDefaultSmsSubId());
            pw.println(" defaultDataPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
            pw.println(" defaultVoicePhoneId=" + SubscriptionManager.getDefaultVoicePhoneId());
            pw.println(" defaultSmsPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultSmsPhoneId());
            pw.flush();
            for (Map.Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                pw.println(" sSlotIndexToSubId[" + entry.getKey() + "]: subId=" + entry.getValue());
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" ActiveSubInfoList:");
                Iterator<SubscriptionInfo> it = sirl.iterator();
                while (it.hasNext()) {
                    pw.println("  " + it.next().toString());
                }
            } else {
                pw.println(" ActiveSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl2 = getAllSubInfoList(this.mContext.getOpPackageName());
            if (sirl2 != null) {
                pw.println(" AllSubInfoList:");
                Iterator<SubscriptionInfo> it2 = sirl2.iterator();
                while (it2.hasNext()) {
                    pw.println("  " + it2.next().toString());
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

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void migrateImsSettings() {
        migrateImsSettingHelper("volte_vt_enabled", "volte_vt_enabled");
        migrateImsSettingHelper("vt_ims_enabled", "vt_ims_enabled");
        migrateImsSettingHelper("wfc_ims_enabled", "wfc_ims_enabled");
        migrateImsSettingHelper("wfc_ims_mode", "wfc_ims_mode");
        migrateImsSettingHelper("wfc_ims_roaming_mode", "wfc_ims_roaming_mode");
        migrateImsSettingHelper("wfc_ims_roaming_enabled", "wfc_ims_roaming_enabled");
    }

    private void migrateImsSettingHelper(String settingGlobal, String subscriptionProperty) {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defaultSubId = getDefaultVoiceSubId();
        try {
            int prevSetting = Settings.Global.getInt(resolver, settingGlobal);
            if (prevSetting != -1) {
                setSubscriptionPropertyIntoContentResolver(defaultSubId, subscriptionProperty, Integer.toString(prevSetting), resolver);
            }
        } catch (Settings.SettingNotFoundException e) {
        }
    }
}
