package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.service.carrier.CarrierIdentifier;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.IndentingPrintWriter;
import com.huawei.internal.telephony.IccCardConstantsEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CarrierResolver extends Handler {
    private static final int CARRIER_ID_DB_UPDATE_EVENT = 4;
    private static final Uri CONTENT_URL_PREFER_APN = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "preferapn");
    private static final boolean DBG = true;
    private static final int ICC_CHANGED_EVENT = 2;
    private static final String LOG_TAG = CarrierResolver.class.getSimpleName();
    private static final int PREFER_APN_UPDATE_EVENT = 3;
    private static final int SIM_LOAD_EVENT = 1;
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private int mCarrierId = -1;
    private final LocalLog mCarrierIdLocalLog = new LocalLog(10);
    private List<CarrierMatchingRule> mCarrierMatchingRulesOnMccMnc = new ArrayList();
    private String mCarrierName;
    private final ContentObserver mContentObserver = new ContentObserver(this) {
        /* class com.android.internal.telephony.CarrierResolver.AnonymousClass1 */

        public void onChange(boolean selfChange, Uri uri) {
            if (CarrierResolver.CONTENT_URL_PREFER_APN.equals(uri.getLastPathSegment())) {
                CarrierResolver.logd("onChange URI: " + uri);
                CarrierResolver.this.sendEmptyMessage(3);
            } else if (Telephony.CarrierId.All.CONTENT_URI.equals(uri)) {
                CarrierResolver.logd("onChange URI: " + uri);
                CarrierResolver.this.sendEmptyMessage(4);
            }
        }
    };
    private Context mContext;
    private IccRecords mIccRecords;
    private int mMnoCarrierId = -1;
    private Phone mPhone;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.internal.telephony.CarrierResolver.AnonymousClass2 */

        public void onCallStateChanged(int state, String ignored) {
        }
    };
    private String mPreferApn;
    private int mSpecificCarrierId = -1;
    private String mSpecificCarrierName;
    private String mSpn = PhoneConfigurationManager.SSSS;
    private final TelephonyManager mTelephonyMgr;
    private String mTestOverrideApn;
    private String mTestOverrideCarrierPriviledgeRule;

    public CarrierResolver(Phone phone) {
        logd("Creating CarrierResolver[" + phone.getPhoneId() + "]");
        this.mContext = phone.getContext();
        this.mPhone = phone;
        this.mTelephonyMgr = TelephonyManager.from(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(CONTENT_URL_PREFER_APN, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Telephony.CarrierId.All.CONTENT_URI, false, this.mContentObserver);
        UiccController.getInstance().registerForIccChanged(this, 2, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004b A[ADDED_TO_REGION] */
    public void resolveSubscriptionCarrierId(String simState) {
        char c;
        logd("[resolveSubscriptionCarrierId] simState: " + simState);
        int hashCode = simState.hashCode();
        if (hashCode != -2044189691) {
            if (hashCode != -1830845986) {
                if (hashCode == 1924388665 && simState.equals(IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT)) {
                    c = 0;
                    if (c != 0 || c == 1) {
                        handleSimAbsent();
                    } else if (c == 2) {
                        handleSimLoaded();
                        return;
                    } else {
                        return;
                    }
                }
            } else if (simState.equals(IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR)) {
                c = 1;
                if (c != 0) {
                }
                handleSimAbsent();
            }
        } else if (simState.equals(IccCardConstantsEx.INTENT_VALUE_ICC_LOADED)) {
            c = 2;
            if (c != 0) {
            }
            handleSimAbsent();
        }
        c = 65535;
        if (c != 0) {
        }
        handleSimAbsent();
    }

    private void handleSimLoaded() {
        String str;
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            if (iccRecords.getServiceProviderName() == null) {
                str = PhoneConfigurationManager.SSSS;
            } else {
                str = this.mIccRecords.getServiceProviderName();
            }
            this.mSpn = str;
        } else {
            loge("mIccRecords is null on SIM_LOAD_EVENT, could not get SPN");
        }
        this.mPreferApn = getPreferApn();
        loadCarrierMatchingRulesOnMccMnc();
    }

    private void handleSimAbsent() {
        this.mCarrierMatchingRulesOnMccMnc.clear();
        this.mSpn = null;
        this.mPreferApn = null;
        updateCarrierIdAndName(-1, null, -1, null, -1);
    }

    public void handleMessage(Message msg) {
        logd("handleMessage: " + msg.what);
        int i = msg.what;
        if (i == 1) {
            handleSimLoaded();
        } else if (i == 2) {
            IccRecords newIccRecords = UiccController.getInstance().getIccRecords(this.mPhone.getPhoneId(), 1);
            IccRecords iccRecords = this.mIccRecords;
            if (iccRecords != newIccRecords) {
                if (iccRecords != null) {
                    logd("Removing stale icc objects.");
                    this.mIccRecords.unregisterForRecordsOverride(this);
                    this.mIccRecords = null;
                }
                if (newIccRecords != null) {
                    logd("new Icc object");
                    newIccRecords.registerForRecordsOverride(this, 1, null);
                    this.mIccRecords = newIccRecords;
                }
            }
        } else if (i == 3) {
            String preferApn = getPreferApn();
            if (!equals(this.mPreferApn, preferApn, true)) {
                logd("[updatePreferApn] from:" + this.mPreferApn + " to:" + preferApn);
                this.mPreferApn = preferApn;
                matchSubscriptionCarrier();
            }
        } else if (i != 4) {
            loge("invalid msg: " + msg.what);
        } else {
            loadCarrierMatchingRulesOnMccMnc();
        }
    }

    private void loadCarrierMatchingRulesOnMccMnc() {
        try {
            String mccmnc = this.mTelephonyMgr.getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
            Cursor cursor = this.mContext.getContentResolver().query(Telephony.CarrierId.All.CONTENT_URI, null, "mccmnc=?", new String[]{mccmnc}, null);
            if (cursor != null) {
                try {
                    if (VDBG) {
                        logd("[loadCarrierMatchingRules]- " + cursor.getCount() + " Records(s) in DB mccmnc: " + mccmnc);
                    }
                    this.mCarrierMatchingRulesOnMccMnc.clear();
                    while (cursor.moveToNext()) {
                        this.mCarrierMatchingRulesOnMccMnc.add(makeCarrierMatchingRule(cursor));
                    }
                    matchSubscriptionCarrier();
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            loge("[loadCarrierMatchingRules]- ex: " + ex);
        }
    }

    private String getCarrierNameFromId(int cid) {
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = Telephony.CarrierId.All.CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, "carrier_id=?", new String[]{cid + PhoneConfigurationManager.SSSS}, null);
            if (cursor != null) {
                try {
                    if (VDBG) {
                        logd("[getCarrierNameFromId]- " + cursor.getCount() + " Records(s) in DB cid: " + cid);
                    }
                    if (cursor.moveToNext()) {
                        return cursor.getString(cursor.getColumnIndex("carrier_name"));
                    }
                } finally {
                    cursor.close();
                }
            }
            if (cursor == null) {
                return null;
            }
            cursor.close();
            return null;
        } catch (Exception ex) {
            loge("[getCarrierNameFromId]- ex: " + ex);
            return null;
        }
    }

    private static List<CarrierMatchingRule> getCarrierMatchingRulesFromMccMnc(Context context, String mccmnc) {
        List<CarrierMatchingRule> rules = new ArrayList<>();
        try {
            Cursor cursor = context.getContentResolver().query(Telephony.CarrierId.All.CONTENT_URI, null, "mccmnc=?", new String[]{mccmnc}, null);
            if (cursor != null) {
                try {
                    if (VDBG) {
                        logd("[loadCarrierMatchingRules]- " + cursor.getCount() + " Records(s) in DB mccmnc: " + mccmnc);
                    }
                    rules.clear();
                    while (cursor.moveToNext()) {
                        rules.add(makeCarrierMatchingRule(cursor));
                    }
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            loge("[loadCarrierMatchingRules]- ex: " + ex);
        }
        return rules;
    }

    private String getPreferApn() {
        if (!TextUtils.isEmpty(this.mTestOverrideApn)) {
            logd("[getPreferApn]- " + this.mTestOverrideApn + " test override");
            return this.mTestOverrideApn;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri uri = Telephony.Carriers.CONTENT_URI;
        Cursor cursor = contentResolver.query(Uri.withAppendedPath(uri, "preferapn/subId/" + this.mPhone.getSubId()), new String[]{"apn"}, null, null, null);
        if (cursor != null) {
            try {
                if (VDBG) {
                    logd("[getPreferApn]- " + cursor.getCount() + " Records(s) in DB");
                }
                if (cursor.moveToNext()) {
                    String apn = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
                    logd("[getPreferApn]- " + apn);
                    cursor.close();
                    return apn;
                }
            } catch (Exception ex) {
                loge("[getPreferApn]- exception: " + ex);
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0050, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0055, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0056, code lost:
        r0.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0059, code lost:
        throw r2;
     */
    private boolean isPreferApnUserEdited(String preferApn) {
        try {
            boolean z = true;
            Cursor cursor = this.mContext.getContentResolver().query(Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "preferapn/subId/" + this.mPhone.getSubId()), new String[]{"edited"}, "apn=?", new String[]{preferApn}, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    if (cursor.getInt(cursor.getColumnIndexOrThrow("edited")) != 1) {
                        z = false;
                    }
                    cursor.close();
                    return z;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            loge("[isPreferApnUserEdited]- exception: " + ex);
        }
        return false;
    }

    public void setTestOverrideApn(String apn) {
        logd("[setTestOverrideApn]: " + apn);
        this.mTestOverrideApn = apn;
    }

    public void setTestOverrideCarrierPriviledgeRule(String rule) {
        logd("[setTestOverrideCarrierPriviledgeRule]: " + rule);
        this.mTestOverrideCarrierPriviledgeRule = rule;
    }

    private void updateCarrierIdAndName(int cid, String name, int specificCarrierId, String specificCarrierName, int mnoCid) {
        boolean update = false;
        if (specificCarrierId != this.mSpecificCarrierId) {
            logd("[updateSpecificCarrierId] from:" + this.mSpecificCarrierId + " to:" + specificCarrierId);
            this.mSpecificCarrierId = specificCarrierId;
            update = true;
        }
        if (specificCarrierName != this.mSpecificCarrierName) {
            logd("[updateSpecificCarrierName] from:" + this.mSpecificCarrierName + " to:" + specificCarrierName);
            this.mSpecificCarrierName = specificCarrierName;
            update = true;
        }
        if (update) {
            LocalLog localLog = this.mCarrierIdLocalLog;
            localLog.log("[updateSpecificCarrierIdAndName] cid:" + this.mSpecificCarrierId + " name:" + this.mSpecificCarrierName);
            Intent intent = new Intent("android.telephony.action.SUBSCRIPTION_SPECIFIC_CARRIER_IDENTITY_CHANGED");
            intent.putExtra("android.telephony.extra.SPECIFIC_CARRIER_ID", this.mSpecificCarrierId);
            intent.putExtra("android.telephony.extra.SPECIFIC_CARRIER_NAME", this.mSpecificCarrierName);
            intent.putExtra("android.telephony.extra.SUBSCRIPTION_ID", this.mPhone.getSubId());
            this.mContext.sendBroadcast(intent);
            ContentValues cv = new ContentValues();
            cv.put("specific_carrier_id", Integer.valueOf(this.mSpecificCarrierId));
            cv.put("specific_carrier_id_name", this.mSpecificCarrierName);
            this.mContext.getContentResolver().update(Telephony.CarrierId.getSpecificCarrierIdUriForSubscriptionId(this.mPhone.getSubId()), cv, null, null);
        }
        boolean update2 = false;
        if (!equals(name, this.mCarrierName, true)) {
            logd("[updateCarrierName] from:" + this.mCarrierName + " to:" + name);
            this.mCarrierName = name;
            update2 = true;
        }
        if (cid != this.mCarrierId) {
            logd("[updateCarrierId] from:" + this.mCarrierId + " to:" + cid);
            this.mCarrierId = cid;
            update2 = true;
        }
        if (mnoCid != this.mMnoCarrierId) {
            logd("[updateMnoCarrierId] from:" + this.mMnoCarrierId + " to:" + mnoCid);
            this.mMnoCarrierId = mnoCid;
            update2 = true;
        }
        if (update2) {
            LocalLog localLog2 = this.mCarrierIdLocalLog;
            localLog2.log("[updateCarrierIdAndName] cid:" + this.mCarrierId + " name:" + this.mCarrierName + " mnoCid:" + this.mMnoCarrierId);
            Intent intent2 = new Intent("android.telephony.action.SUBSCRIPTION_CARRIER_IDENTITY_CHANGED");
            intent2.putExtra("android.telephony.extra.CARRIER_ID", this.mCarrierId);
            intent2.putExtra("android.telephony.extra.CARRIER_NAME", this.mCarrierName);
            intent2.putExtra("android.telephony.extra.SUBSCRIPTION_ID", this.mPhone.getSubId());
            this.mContext.sendBroadcast(intent2);
            ContentValues cv2 = new ContentValues();
            cv2.put("carrier_id", Integer.valueOf(this.mCarrierId));
            cv2.put("carrier_name", this.mCarrierName);
            this.mContext.getContentResolver().update(Telephony.CarrierId.getUriForSubscriptionId(this.mPhone.getSubId()), cv2, null, null);
        }
        if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
            SubscriptionController.getInstance().setCarrierId(this.mCarrierId, this.mPhone.getSubId());
        }
    }

    private static CarrierMatchingRule makeCarrierMatchingRule(Cursor cursor) {
        ArrayList arrayList;
        String certs = cursor.getString(cursor.getColumnIndexOrThrow("privilege_access_rule"));
        String string = cursor.getString(cursor.getColumnIndexOrThrow("mccmnc"));
        String string2 = cursor.getString(cursor.getColumnIndexOrThrow("imsi_prefix_xpattern"));
        String string3 = cursor.getString(cursor.getColumnIndexOrThrow("iccid_prefix"));
        String string4 = cursor.getString(cursor.getColumnIndexOrThrow("gid1"));
        String string5 = cursor.getString(cursor.getColumnIndexOrThrow("gid2"));
        String string6 = cursor.getString(cursor.getColumnIndexOrThrow("plmn"));
        String string7 = cursor.getString(cursor.getColumnIndexOrThrow("spn"));
        String string8 = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
        if (TextUtils.isEmpty(certs)) {
            arrayList = null;
        } else {
            arrayList = new ArrayList(Arrays.asList(certs));
        }
        return new CarrierMatchingRule(string, string2, string3, string4, string5, string6, string7, string8, arrayList, cursor.getInt(cursor.getColumnIndexOrThrow("carrier_id")), cursor.getString(cursor.getColumnIndexOrThrow("carrier_name")), cursor.getInt(cursor.getColumnIndexOrThrow("parent_carrier_id")));
    }

    public static class CarrierMatchingRule {
        private static final int SCORE_APN = 1;
        private static final int SCORE_GID1 = 32;
        private static final int SCORE_GID2 = 16;
        private static final int SCORE_ICCID_PREFIX = 64;
        private static final int SCORE_IMSI_PREFIX = 128;
        private static final int SCORE_INVALID = -1;
        private static final int SCORE_MCCMNC = 256;
        private static final int SCORE_PLMN = 8;
        private static final int SCORE_PRIVILEGE_ACCESS_RULE = 4;
        private static final int SCORE_SPN = 2;
        public final String apn;
        public final String gid1;
        public final String gid2;
        public final String iccidPrefix;
        public final String imsiPrefixPattern;
        private int mCid;
        private String mName;
        private int mParentCid;
        private int mScore;
        public final String mccMnc;
        public final String plmn;
        public final List<String> privilegeAccessRule;
        public final String spn;

        @VisibleForTesting
        public CarrierMatchingRule(String mccmnc, String imsiPrefixPattern2, String iccidPrefix2, String gid12, String gid22, String plmn2, String spn2, String apn2, List<String> privilegeAccessRule2, int cid, String name, int parentCid) {
            this.mScore = 0;
            this.mccMnc = mccmnc;
            this.imsiPrefixPattern = imsiPrefixPattern2;
            this.iccidPrefix = iccidPrefix2;
            this.gid1 = gid12;
            this.gid2 = gid22;
            this.plmn = plmn2;
            this.spn = spn2;
            this.apn = apn2;
            this.privilegeAccessRule = privilegeAccessRule2;
            this.mCid = cid;
            this.mName = name;
            this.mParentCid = parentCid;
        }

        private CarrierMatchingRule(CarrierMatchingRule rule) {
            this.mScore = 0;
            this.mccMnc = rule.mccMnc;
            this.imsiPrefixPattern = rule.imsiPrefixPattern;
            this.iccidPrefix = rule.iccidPrefix;
            this.gid1 = rule.gid1;
            this.gid2 = rule.gid2;
            this.plmn = rule.plmn;
            this.spn = rule.spn;
            this.apn = rule.apn;
            this.privilegeAccessRule = rule.privilegeAccessRule;
            this.mCid = rule.mCid;
            this.mName = rule.mName;
            this.mParentCid = rule.mParentCid;
        }

        public void match(CarrierMatchingRule subscriptionRule) {
            this.mScore = 0;
            String str = this.mccMnc;
            if (str != null) {
                if (!CarrierResolver.equals(subscriptionRule.mccMnc, str, false)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += SCORE_MCCMNC;
            }
            String str2 = this.imsiPrefixPattern;
            if (str2 != null) {
                if (!imsiPrefixMatch(subscriptionRule.imsiPrefixPattern, str2)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 128;
            }
            String str3 = this.iccidPrefix;
            if (str3 != null) {
                if (!iccidPrefixMatch(subscriptionRule.iccidPrefix, str3)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 64;
            }
            String str4 = this.gid1;
            if (str4 != null) {
                if (!gidMatch(subscriptionRule.gid1, str4)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 32;
            }
            String str5 = this.gid2;
            if (str5 != null) {
                if (!gidMatch(subscriptionRule.gid2, str5)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 16;
            }
            String str6 = this.plmn;
            if (str6 != null) {
                if (!CarrierResolver.equals(subscriptionRule.plmn, str6, true)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 8;
            }
            String str7 = this.spn;
            if (str7 != null) {
                if (!CarrierResolver.equals(subscriptionRule.spn, str7, true)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 2;
            }
            List<String> list = this.privilegeAccessRule;
            if (list != null && !list.isEmpty()) {
                if (!carrierPrivilegeRulesMatch(subscriptionRule.privilegeAccessRule, this.privilegeAccessRule)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 4;
            }
            String str8 = this.apn;
            if (str8 == null) {
                return;
            }
            if (!CarrierResolver.equals(subscriptionRule.apn, str8, true)) {
                this.mScore = -1;
            } else {
                this.mScore++;
            }
        }

        private boolean imsiPrefixMatch(String imsi, String prefixXPattern) {
            if (TextUtils.isEmpty(prefixXPattern)) {
                return true;
            }
            if (TextUtils.isEmpty(imsi) || imsi.length() < prefixXPattern.length()) {
                return false;
            }
            for (int i = 0; i < prefixXPattern.length(); i++) {
                if (!(prefixXPattern.charAt(i) == 'x' || prefixXPattern.charAt(i) == 'X' || prefixXPattern.charAt(i) == imsi.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        private boolean iccidPrefixMatch(String iccid, String prefix) {
            if (iccid == null || prefix == null) {
                return false;
            }
            return iccid.startsWith(prefix);
        }

        private boolean gidMatch(String gidFromSim, String gid) {
            return gidFromSim != null && gidFromSim.toLowerCase().startsWith(gid.toLowerCase());
        }

        private boolean carrierPrivilegeRulesMatch(List<String> certsFromSubscription, List<String> certs) {
            if (certsFromSubscription == null || certsFromSubscription.isEmpty()) {
                return false;
            }
            for (String cert : certs) {
                Iterator<String> it = certsFromSubscription.iterator();
                while (true) {
                    if (it.hasNext()) {
                        String certFromSubscription = it.next();
                        if (!TextUtils.isEmpty(cert) && cert.equalsIgnoreCase(certFromSubscription)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public String toString() {
            return "[CarrierMatchingRule] - mccmnc: " + this.mccMnc + " gid1: " + this.gid1 + " gid2: " + this.gid2 + " plmn: " + this.plmn + " imsi_prefix: " + this.imsiPrefixPattern + " iccid_prefix" + this.iccidPrefix + " spn: " + this.spn + " privilege_access_rule: " + this.privilegeAccessRule + " apn: " + this.apn + " name: " + this.mName + " cid: " + this.mCid + " score: " + this.mScore;
        }
    }

    private CarrierMatchingRule getSubscriptionMatchingRule() {
        List<String> accessRules;
        String mccmnc = this.mTelephonyMgr.getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        String iccid = this.mPhone.getIccSerialNumber();
        String gid1 = this.mPhone.getGroupIdLevel1();
        String gid2 = this.mPhone.getGroupIdLevel2();
        String imsi = this.mPhone.getSubscriberId();
        String plmn = this.mPhone.getPlmn();
        String spn = this.mSpn;
        String apn = this.mPreferApn;
        if (!TextUtils.isEmpty(this.mTestOverrideCarrierPriviledgeRule)) {
            accessRules = new ArrayList<>(Arrays.asList(this.mTestOverrideCarrierPriviledgeRule));
        } else {
            accessRules = this.mTelephonyMgr.createForSubscriptionId(this.mPhone.getSubId()).getCertsFromCarrierPrivilegeAccessRules();
        }
        if (VDBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("[matchSubscriptionCarrier] mnnmnc:");
            sb.append(mccmnc);
            sb.append(" gid1: ");
            sb.append(gid1);
            sb.append(" gid2: ");
            sb.append(gid2);
            sb.append(" imsi: ");
            sb.append(Rlog.pii(LOG_TAG, imsi));
            sb.append(" iccid: ");
            sb.append(Rlog.pii(LOG_TAG, iccid));
            sb.append(" plmn: ");
            sb.append(plmn);
            sb.append(" spn: ");
            sb.append(spn);
            sb.append(" apn: ");
            sb.append(apn);
            sb.append(" accessRules: ");
            sb.append(accessRules != null ? accessRules : null);
            logd(sb.toString());
        }
        return new CarrierMatchingRule(mccmnc, imsi, iccid, gid1, gid2, plmn, spn, apn, accessRules, -1, null, -1);
    }

    private void matchSubscriptionCarrier() {
        String imsiPrefix;
        if (!SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
            logd("[matchSubscriptionCarrier]skip before sim records loaded");
            return;
        }
        CarrierMatchingRule subscriptionRule = getSubscriptionMatchingRule();
        int maxScore = -1;
        CarrierMatchingRule maxRule = null;
        CarrierMatchingRule maxRuleParent = null;
        CarrierMatchingRule mnoRule = null;
        for (CarrierMatchingRule rule : this.mCarrierMatchingRulesOnMccMnc) {
            rule.match(subscriptionRule);
            if (rule.mScore > maxScore) {
                maxScore = rule.mScore;
                maxRule = rule;
                maxRuleParent = rule;
            } else if (maxScore > -1 && rule.mScore == maxScore && maxRule != null) {
                if (rule.mParentCid == maxRule.mCid) {
                    maxRule = rule;
                } else if (maxRule.mParentCid == rule.mCid) {
                    maxRuleParent = rule;
                }
            }
            if (rule.mScore == 256) {
                mnoRule = rule;
            }
        }
        String apn = null;
        if (maxScore == -1) {
            logd("[matchSubscriptionCarrier - no match] cid: -1 name: " + ((Object) null));
            updateCarrierIdAndName(-1, null, -1, null, -1);
        } else if (!(maxRule == null || maxRuleParent == null)) {
            if (maxRule == maxRuleParent && maxRule.mParentCid != -1) {
                CarrierMatchingRule maxRuleParent2 = new CarrierMatchingRule(maxRule);
                maxRuleParent2.mCid = maxRuleParent2.mParentCid;
                maxRuleParent2.mName = getCarrierNameFromId(maxRuleParent2.mCid);
                maxRuleParent = maxRuleParent2;
            }
            logd("[matchSubscriptionCarrier] specific cid: " + maxRule.mCid + " specific name: " + maxRule.mName + " cid: " + maxRuleParent.mCid + " name: " + maxRuleParent.mName);
            updateCarrierIdAndName(maxRuleParent.mCid, maxRuleParent.mName, maxRule.mCid, maxRule.mName, mnoRule == null ? maxRule.mCid : mnoRule.mCid);
        }
        String unknownGid1ToLog = ((maxScore & 32) != 0 || TextUtils.isEmpty(subscriptionRule.gid1)) ? null : subscriptionRule.gid1;
        String unknownMccmncToLog = ((maxScore == -1 || (maxScore & 32) == 0) && !TextUtils.isEmpty(subscriptionRule.mccMnc)) ? subscriptionRule.mccMnc : null;
        if (subscriptionRule.apn != null && !isPreferApnUserEdited(subscriptionRule.apn)) {
            apn = subscriptionRule.apn;
        }
        String iccidPrefix = (subscriptionRule.iccidPrefix == null || subscriptionRule.iccidPrefix.length() < 7) ? subscriptionRule.iccidPrefix : subscriptionRule.iccidPrefix.substring(0, 7);
        if (subscriptionRule.imsiPrefixPattern == null || subscriptionRule.imsiPrefixPattern.length() < 8) {
            imsiPrefix = subscriptionRule.imsiPrefixPattern;
        } else {
            imsiPrefix = subscriptionRule.imsiPrefixPattern.substring(0, 8);
        }
        TelephonyMetrics.getInstance().writeCarrierIdMatchingEvent(this.mPhone.getPhoneId(), getCarrierListVersion(), this.mCarrierId, unknownMccmncToLog, unknownGid1ToLog, new CarrierMatchingRule(subscriptionRule.mccMnc, imsiPrefix, iccidPrefix, subscriptionRule.gid1, subscriptionRule.gid2, subscriptionRule.plmn, subscriptionRule.spn, apn, subscriptionRule.privilegeAccessRule, -1, null, -1));
    }

    public int getCarrierListVersion() {
        Cursor cursor = this.mContext.getContentResolver().query(Uri.withAppendedPath(Telephony.CarrierId.All.CONTENT_URI, "get_version"), null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getCarrierId() {
        return this.mCarrierId;
    }

    public int getSpecificCarrierId() {
        return this.mSpecificCarrierId;
    }

    public String getCarrierName() {
        return this.mCarrierName;
    }

    public String getSpecificCarrierName() {
        return this.mSpecificCarrierName;
    }

    public int getMnoCarrierId() {
        return this.mMnoCarrierId;
    }

    public static int getCarrierIdFromIdentifier(Context context, CarrierIdentifier carrierIdentifier) {
        String mccmnc = carrierIdentifier.getMcc() + carrierIdentifier.getMnc();
        String gid1 = carrierIdentifier.getGid1();
        String gid2 = carrierIdentifier.getGid2();
        String imsi = carrierIdentifier.getImsi();
        String spn = carrierIdentifier.getSpn();
        if (VDBG) {
            logd("[getCarrierIdFromIdentifier] mnnmnc:" + mccmnc + " gid1: " + gid1 + " gid2: " + gid2 + " imsi: " + Rlog.pii(LOG_TAG, imsi) + " spn: " + spn);
        }
        CarrierMatchingRule targetRule = new CarrierMatchingRule(mccmnc, imsi, null, gid1, gid2, null, spn, null, null, -1, null, -1);
        int carrierId = -1;
        int maxScore = -1;
        for (CarrierMatchingRule rule : getCarrierMatchingRulesFromMccMnc(context, targetRule.mccMnc)) {
            rule.match(targetRule);
            if (rule.mScore > maxScore) {
                maxScore = rule.mScore;
                carrierId = rule.mCid;
            }
        }
        return carrierId;
    }

    public static List<Integer> getCarrierIdsFromApnQuery(Context context, String mccmnc, String mvnoCase, String mvnoData) {
        String selection = "mccmnc=" + mccmnc;
        if ("spn".equals(mvnoCase) && mvnoData != null) {
            selection = selection + " AND spn='" + mvnoData + "'";
        } else if ("imsi".equals(mvnoCase) && mvnoData != null) {
            selection = selection + " AND imsi_prefix_xpattern='" + mvnoData + "'";
        } else if ("gid1".equals(mvnoCase) && mvnoData != null) {
            selection = selection + " AND gid1='" + mvnoData + "'";
        } else if (!"gid2".equals(mvnoCase) || mvnoData == null) {
            logd("mvno case empty or other invalid values");
        } else {
            selection = selection + " AND gid2='" + mvnoData + "'";
        }
        List<Integer> ids = new ArrayList<>();
        try {
            Cursor cursor = context.getContentResolver().query(Telephony.CarrierId.All.CONTENT_URI, null, selection, null, null);
            if (cursor != null) {
                try {
                    if (VDBG) {
                        logd("[getCarrierIdsFromApnQuery]- " + cursor.getCount() + " Records(s) in DB");
                    }
                    while (cursor.moveToNext()) {
                        int cid = cursor.getInt(cursor.getColumnIndex("carrier_id"));
                        if (!ids.contains(Integer.valueOf(cid))) {
                            ids.add(Integer.valueOf(cid));
                        }
                    }
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            loge("[getCarrierIdsFromApnQuery]- ex: " + ex);
        }
        logd(selection + " " + ids);
        return ids;
    }

    public static int getCarrierIdFromMccMnc(Context context, String mccmnc) {
        try {
            Cursor cursor = context.getContentResolver().query(Telephony.CarrierId.All.CONTENT_URI, null, "mccmnc=? AND gid1 is NULL AND gid2 is NULL AND imsi_prefix_xpattern is NULL AND spn is NULL AND iccid_prefix is NULL AND plmn is NULL AND privilege_access_rule is NULL AND apn is NULL", new String[]{mccmnc}, null);
            if (cursor != null) {
                try {
                    if (VDBG) {
                        logd("[getCarrierIdFromMccMnc]- " + cursor.getCount() + " Records(s) in DB mccmnc: " + mccmnc);
                    }
                    if (cursor.moveToNext()) {
                        return cursor.getInt(cursor.getColumnIndex("carrier_id"));
                    }
                } finally {
                    cursor.close();
                }
            }
            if (cursor == null) {
                return -1;
            }
            cursor.close();
            return -1;
        } catch (Exception ex) {
            loge("[getCarrierIdFromMccMnc]- ex: " + ex);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public static boolean equals(String a, String b, boolean ignoreCase) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return ignoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
    }

    /* access modifiers changed from: private */
    public static void logd(String str) {
        Rlog.d(LOG_TAG, str);
    }

    private static void loge(String str) {
        Rlog.e(LOG_TAG, str);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.println("mCarrierResolverLocalLogs:");
        ipw.increaseIndent();
        this.mCarrierIdLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println("mCarrierId: " + this.mCarrierId);
        ipw.println("mSpecificCarrierId: " + this.mSpecificCarrierId);
        ipw.println("mMnoCarrierId: " + this.mMnoCarrierId);
        ipw.println("mCarrierName: " + this.mCarrierName);
        ipw.println("mSpecificCarrierName: " + this.mSpecificCarrierName);
        ipw.println("carrier_list_version: " + getCarrierListVersion());
        ipw.println("mCarrierMatchingRules on mccmnc: " + this.mTelephonyMgr.getSimOperatorNumericForPhone(this.mPhone.getPhoneId()));
        ipw.increaseIndent();
        for (CarrierMatchingRule rule : this.mCarrierMatchingRulesOnMccMnc) {
            ipw.println(rule.toString());
        }
        ipw.decreaseIndent();
        ipw.println("mSpn: " + this.mSpn);
        ipw.println("mPreferApn: " + this.mPreferApn);
        ipw.flush();
    }
}
