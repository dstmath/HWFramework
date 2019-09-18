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
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarrierIdentifier extends Handler {
    private static final int CARRIER_ID_DB_UPDATE_EVENT = 6;
    /* access modifiers changed from: private */
    public static final Uri CONTENT_URL_PREFER_APN = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "preferapn");
    private static final boolean DBG = true;
    private static final int ICC_CHANGED_EVENT = 4;
    private static final String LOG_TAG = CarrierIdentifier.class.getSimpleName();
    private static final String OPERATOR_BRAND_OVERRIDE_PREFIX = "operator_branding_";
    private static final int PREFER_APN_UPDATE_EVENT = 5;
    private static final int SIM_ABSENT_EVENT = 2;
    private static final int SIM_LOAD_EVENT = 1;
    private static final int SPN_OVERRIDE_EVENT = 3;
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private int mCarrierId = -1;
    private final LocalLog mCarrierIdLocalLog = new LocalLog(20);
    private List<CarrierMatchingRule> mCarrierMatchingRulesOnMccMnc = new ArrayList();
    private String mCarrierName;
    private final ContentObserver mContentObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange, Uri uri) {
            if (CarrierIdentifier.CONTENT_URL_PREFER_APN.equals(uri.getLastPathSegment())) {
                CarrierIdentifier.logd("onChange URI: " + uri);
                CarrierIdentifier.this.sendEmptyMessage(5);
            } else if (Telephony.CarrierId.All.CONTENT_URI.equals(uri)) {
                CarrierIdentifier.logd("onChange URI: " + uri);
                CarrierIdentifier.this.sendEmptyMessage(6);
            }
        }
    };
    private Context mContext;
    private IccRecords mIccRecords;
    private final SubscriptionsChangedListener mOnSubscriptionsChangedListener = new SubscriptionsChangedListener();
    /* access modifiers changed from: private */
    public Phone mPhone;
    private String mPreferApn;
    private String mSpn = "";
    private final TelephonyManager mTelephonyMgr;
    private UiccProfile mUiccProfile;

    private static class CarrierMatchingRule {
        private static final int SCORE_APN = 1;
        private static final int SCORE_GID1 = 16;
        private static final int SCORE_GID2 = 8;
        private static final int SCORE_ICCID_PREFIX = 32;
        private static final int SCORE_IMSI_PREFIX = 64;
        private static final int SCORE_INVALID = -1;
        private static final int SCORE_MCCMNC = 128;
        private static final int SCORE_PLMN = 4;
        private static final int SCORE_SPN = 2;
        private String mApn;
        /* access modifiers changed from: private */
        public int mCid;
        /* access modifiers changed from: private */
        public String mGid1;
        private String mGid2;
        private String mIccidPrefix;
        private String mImsiPrefixPattern;
        /* access modifiers changed from: private */
        public String mMccMnc;
        /* access modifiers changed from: private */
        public String mName;
        private String mPlmn;
        /* access modifiers changed from: private */
        public int mScore = 0;
        private String mSpn;

        CarrierMatchingRule(String mccmnc, String imsiPrefixPattern, String iccidPrefix, String gid1, String gid2, String plmn, String spn, String apn, int cid, String name) {
            this.mMccMnc = mccmnc;
            this.mImsiPrefixPattern = imsiPrefixPattern;
            this.mIccidPrefix = iccidPrefix;
            this.mGid1 = gid1;
            this.mGid2 = gid2;
            this.mPlmn = plmn;
            this.mSpn = spn;
            this.mApn = apn;
            this.mCid = cid;
            this.mName = name;
        }

        public void match(CarrierMatchingRule subscriptionRule) {
            this.mScore = 0;
            if (this.mMccMnc != null) {
                if (!CarrierIdentifier.equals(subscriptionRule.mMccMnc, this.mMccMnc, false)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 128;
            }
            if (this.mImsiPrefixPattern != null) {
                if (!imsiPrefixMatch(subscriptionRule.mImsiPrefixPattern, this.mImsiPrefixPattern)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 64;
            }
            if (this.mIccidPrefix != null) {
                if (!iccidPrefixMatch(subscriptionRule.mIccidPrefix, this.mIccidPrefix)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 32;
            }
            if (this.mGid1 != null) {
                if (!CarrierIdentifier.equals(subscriptionRule.mGid1, this.mGid1, true)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 16;
            }
            if (this.mGid2 != null) {
                if (!CarrierIdentifier.equals(subscriptionRule.mGid2, this.mGid2, true)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 8;
            }
            if (this.mPlmn != null) {
                if (!CarrierIdentifier.equals(subscriptionRule.mPlmn, this.mPlmn, true)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 4;
            }
            if (this.mSpn != null) {
                if (!CarrierIdentifier.equals(subscriptionRule.mSpn, this.mSpn, true)) {
                    this.mScore = -1;
                    return;
                }
                this.mScore += 2;
            }
            if (this.mApn != null) {
                if (!CarrierIdentifier.equals(subscriptionRule.mApn, this.mApn, true)) {
                    this.mScore = -1;
                    return;
                }
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
                if (prefixXPattern.charAt(i) != 'x' && prefixXPattern.charAt(i) != 'X' && prefixXPattern.charAt(i) != imsi.charAt(i)) {
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

        public String toString() {
            return "[CarrierMatchingRule] - mccmnc: " + this.mMccMnc + " gid1: " + this.mGid1 + " gid2: " + this.mGid2 + " plmn: " + this.mPlmn + " imsi_prefix: " + this.mImsiPrefixPattern + " iccid_prefix" + this.mIccidPrefix + " spn: " + this.mSpn + " apn: " + this.mApn + " name: " + this.mName + " cid: " + this.mCid + " score: " + this.mScore;
        }
    }

    private class SubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        final AtomicInteger mPreviousSubId;

        private SubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            int subId = CarrierIdentifier.this.mPhone.getSubId();
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                CarrierIdentifier.logd("SubscriptionListener.onSubscriptionInfoChanged subId: " + this.mPreviousSubId);
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    CarrierIdentifier.this.sendEmptyMessage(1);
                } else {
                    CarrierIdentifier.this.sendEmptyMessage(2);
                }
            }
        }
    }

    public CarrierIdentifier(Phone phone) {
        logd("Creating CarrierIdentifier[" + phone.getPhoneId() + "]");
        this.mContext = phone.getContext();
        this.mPhone = phone;
        this.mTelephonyMgr = TelephonyManager.from(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(CONTENT_URL_PREFER_APN, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Telephony.CarrierId.All.CONTENT_URI, false, this.mContentObserver);
        SubscriptionManager.from(this.mContext).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        UiccController.getInstance().registerForIccChanged(this, 4, null);
    }

    public void handleMessage(Message msg) {
        if (VDBG) {
            logd("handleMessage: " + msg.what);
        }
        switch (msg.what) {
            case 1:
            case 6:
                this.mSpn = this.mTelephonyMgr.getSimOperatorNameForPhone(this.mPhone.getPhoneId());
                this.mPreferApn = getPreferApn();
                loadCarrierMatchingRulesOnMccMnc();
                return;
            case 2:
                this.mCarrierMatchingRulesOnMccMnc.clear();
                this.mSpn = null;
                this.mPreferApn = null;
                updateCarrierIdAndName(-1, null);
                return;
            case 3:
                String spn = this.mTelephonyMgr.getSimOperatorNameForPhone(this.mPhone.getPhoneId());
                if (!equals(this.mSpn, spn, true)) {
                    logd("[updateSpn] from:" + this.mSpn + " to:" + spn);
                    this.mSpn = spn;
                    matchCarrier();
                    return;
                }
                return;
            case 4:
                IccRecords newIccRecords = UiccController.getInstance().getIccRecords(this.mPhone.getPhoneId(), 1);
                if (this.mIccRecords != newIccRecords) {
                    if (this.mIccRecords != null) {
                        logd("Removing stale icc objects.");
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                        this.mIccRecords.unregisterForRecordsOverride(this);
                        this.mIccRecords = null;
                    }
                    if (newIccRecords != null) {
                        logd("new Icc object");
                        newIccRecords.registerForRecordsLoaded(this, 1, null);
                        newIccRecords.registerForRecordsOverride(this, 1, null);
                        this.mIccRecords = newIccRecords;
                    }
                }
                UiccProfile uiccProfile = UiccController.getInstance().getUiccProfileForPhone(this.mPhone.getPhoneId());
                if (this.mUiccProfile != uiccProfile) {
                    if (this.mUiccProfile != null) {
                        logd("unregister operatorBrandOverride");
                        this.mUiccProfile.unregisterForOperatorBrandOverride(this);
                        this.mUiccProfile = null;
                    }
                    if (uiccProfile != null) {
                        logd("register operatorBrandOverride");
                        uiccProfile.registerForOpertorBrandOverride(this, 3, null);
                        this.mUiccProfile = uiccProfile;
                        return;
                    }
                    return;
                }
                return;
            case 5:
                String preferApn = getPreferApn();
                if (!equals(this.mPreferApn, preferApn, true)) {
                    logd("[updatePreferApn] from:" + this.mPreferApn + " to:" + preferApn);
                    this.mPreferApn = preferApn;
                    matchCarrier();
                    return;
                }
                return;
            default:
                loge("invalid msg: " + msg.what);
                return;
        }
    }

    private void loadCarrierMatchingRulesOnMccMnc() {
        Cursor cursor;
        try {
            String mccmnc = this.mTelephonyMgr.getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
            cursor = this.mContext.getContentResolver().query(Telephony.CarrierId.All.CONTENT_URI, null, "mccmnc=?", new String[]{mccmnc}, null);
            if (cursor != null) {
                if (VDBG) {
                    logd("[loadCarrierMatchingRules]- " + cursor.getCount() + " Records(s) in DB mccmnc: " + mccmnc);
                }
                this.mCarrierMatchingRulesOnMccMnc.clear();
                while (cursor.moveToNext()) {
                    this.mCarrierMatchingRulesOnMccMnc.add(makeCarrierMatchingRule(cursor));
                }
                matchCarrier();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            loge("[loadCarrierMatchingRules]- ex: " + ex);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0095, code lost:
        if (r0 == null) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x009e, code lost:
        if (r0 != null) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00a0, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00a4, code lost:
        return null;
     */
    private String getPreferApn() {
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
                    if (cursor != null) {
                        cursor.close();
                    }
                    return apn;
                }
            } catch (Exception ex) {
                loge("[getPreferApn]- exception: " + ex);
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private void updateCarrierIdAndName(int cid, String name) {
        boolean update = false;
        if (!equals(name, this.mCarrierName, true)) {
            logd("[updateCarrierName] from:" + this.mCarrierName + " to:" + name);
            this.mCarrierName = name;
            update = true;
        }
        if (cid != this.mCarrierId) {
            logd("[updateCarrierId] from:" + this.mCarrierId + " to:" + cid);
            this.mCarrierId = cid;
            update = true;
        }
        if (update) {
            LocalLog localLog = this.mCarrierIdLocalLog;
            localLog.log("[updateCarrierIdAndName] cid:" + this.mCarrierId + " name:" + this.mCarrierName);
            Intent intent = new Intent("android.telephony.action.SUBSCRIPTION_CARRIER_IDENTITY_CHANGED");
            intent.putExtra("android.telephony.extra.CARRIER_ID", this.mCarrierId);
            intent.putExtra("android.telephony.extra.CARRIER_NAME", this.mCarrierName);
            intent.putExtra("android.telephony.extra.SUBSCRIPTION_ID", this.mPhone.getSubId());
            this.mContext.sendBroadcast(intent);
            ContentValues cv = new ContentValues();
            cv.put("carrier_id", Integer.valueOf(this.mCarrierId));
            cv.put("carrier_name", this.mCarrierName);
            this.mContext.getContentResolver().update(Uri.withAppendedPath(Telephony.CarrierId.CONTENT_URI, Integer.toString(this.mPhone.getSubId())), cv, null, null);
        }
    }

    private CarrierMatchingRule makeCarrierMatchingRule(Cursor cursor) {
        CarrierMatchingRule carrierMatchingRule = new CarrierMatchingRule(cursor.getString(cursor.getColumnIndexOrThrow("mccmnc")), cursor.getString(cursor.getColumnIndexOrThrow("imsi_prefix_xpattern")), cursor.getString(cursor.getColumnIndexOrThrow("iccid_prefix")), cursor.getString(cursor.getColumnIndexOrThrow("gid1")), cursor.getString(cursor.getColumnIndexOrThrow("gid2")), cursor.getString(cursor.getColumnIndexOrThrow("plmn")), cursor.getString(cursor.getColumnIndexOrThrow("spn")), cursor.getString(cursor.getColumnIndexOrThrow("apn")), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_id")), cursor.getString(cursor.getColumnIndexOrThrow("carrier_name")));
        return carrierMatchingRule;
    }

    private void matchCarrier() {
        if (!SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
            logd("[matchCarrier]skip before sim records loaded");
            return;
        }
        String mccmnc = this.mTelephonyMgr.getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        String iccid = this.mPhone.getIccSerialNumber();
        String gid1 = this.mPhone.getGroupIdLevel1();
        String gid2 = this.mPhone.getGroupIdLevel2();
        String imsi = this.mPhone.getSubscriberId();
        String plmn = this.mPhone.getPlmn();
        String spn = this.mSpn;
        String apn = this.mPreferApn;
        if (VDBG) {
            logd("[matchCarrier] mnnmnc:" + mccmnc + " gid1: " + gid1 + " gid2: " + gid2 + " imsi: " + Rlog.pii(LOG_TAG, imsi) + " iccid: " + Rlog.pii(LOG_TAG, iccid) + " plmn: " + plmn + " spn: " + spn + " apn: " + apn);
        }
        String str = spn;
        String str2 = plmn;
        String str3 = imsi;
        CarrierMatchingRule subscriptionRule = new CarrierMatchingRule(mccmnc, imsi, iccid, gid1, gid2, plmn, spn, apn, -1, null);
        int maxScore = -1;
        CarrierMatchingRule maxRule = null;
        for (CarrierMatchingRule rule : this.mCarrierMatchingRulesOnMccMnc) {
            rule.match(subscriptionRule);
            if (rule.mScore > maxScore) {
                maxScore = rule.mScore;
                maxRule = rule;
            }
        }
        String unknownMccmncToLog = null;
        if (maxScore == -1) {
            logd("[matchCarrier - no match] cid: -1 name: " + null);
            updateCarrierIdAndName(-1, null);
        } else {
            logd("[matchCarrier] cid: " + maxRule.mCid + " name: " + maxRule.mName);
            updateCarrierIdAndName(maxRule.mCid, maxRule.mName);
        }
        String unknownGid1ToLog = ((maxScore & 16) != 0 || TextUtils.isEmpty(subscriptionRule.mGid1)) ? null : subscriptionRule.mGid1;
        if ((maxScore == -1 || (maxScore & 16) == 0) && !TextUtils.isEmpty(subscriptionRule.mMccMnc)) {
            unknownMccmncToLog = subscriptionRule.mMccMnc;
        }
        TelephonyMetrics.getInstance().writeCarrierIdMatchingEvent(this.mPhone.getPhoneId(), getCarrierListVersion(), this.mCarrierId, unknownMccmncToLog, unknownGid1ToLog);
    }

    public int getCarrierListVersion() {
        Cursor cursor = this.mContext.getContentResolver().query(Uri.withAppendedPath(Telephony.CarrierId.All.CONTENT_URI, "get_version"), null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getCarrierId() {
        return this.mCarrierId;
    }

    public String getCarrierName() {
        return this.mCarrierName;
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
        ipw.println("mCarrierIdLocalLogs:");
        ipw.increaseIndent();
        this.mCarrierIdLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println("mCarrierId: " + this.mCarrierId);
        ipw.println("mCarrierName: " + this.mCarrierName);
        ipw.println("version: " + getCarrierListVersion());
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
