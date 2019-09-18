package com.android.internal.telephony.dataconnection;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.NetworkUtils;
import android.net.Uri;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class ApnReminder {
    private static String APN_APN = "apn";
    private static String APN_DISPLAY_NAME = "displayName";
    private static final String APN_ID = "apn_id";
    private static String APN_NAME = HwTelephony.NumMatchs.NAME;
    private static String APN_ONS_NAME = "onsName";
    private static String APN_OTHER_APNS = "otherApns";
    private static String APN_REMINDER_DOCUMENT = "apnReminderList";
    private static String APN_VOICEMAIL_NUMBER = "vmNumber";
    private static String APN_VOICEMAIL_TAG = "vmTag";
    private static String ATTRIBUTE_PLMN = "plmn";
    private static String ATTRIBUTE_REMIND_TYPE = "remindType";
    private static String ATTRIBUTE_REMIND_TYPE_APN_FAILED = "apnFailed";
    private static String ATTRIBUTE_REMIND_TYPE_IMSI_CHANGE = "imsiChange";
    private static String ATTRIBUTE_TITLE = "title";
    private static int GID1_VIRGIN_MEDIA = 40;
    private static String LAST_IMSI = "apn_reminder_last_imsi";
    private static String LOG_TAG = "GSM";
    private static String NODE_APN = "apnSetting";
    private static String NODE_APN_REMINDER = "apnReminder";
    private static String PLMN_VIRGIN_MEDIA = "23430";
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final Uri PREFERAPN_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static final int QUERY_TYPE_ONS_NAME = 1;
    private static final int QUERY_TYPE_VOICEMAIL_NUMBER = 2;
    private static final int QUERY_TYPE_VOICEMAIL_TAG = 3;
    private static int REMIND_TYPE_ALL_APN_FAILED = 2;
    private static int REMIND_TYPE_IMSI_CHANGE = 1;
    private static final Uri URL_TELEPHONY_USING_SUBID = Uri.parse("content://telephony/carriers/subId");
    private static String custFileName = "apn_reminder.xml";
    private static String custFilePath = "/data/cust/xml/";
    private static String hwCfgPolicyPath = "hwCfgPolicyPath";
    private static ApnReminder instance = null;
    private static ApnReminder instance1 = null;
    /* access modifiers changed from: private */
    public static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static String systemFilePath = "/system/etc/";
    boolean allApnFailed = false;
    boolean dialogDispalyed = false;
    boolean imsiChanged = false;
    Context mContext;
    private int mGID1 = HwSubscriptionManager.SUB_INIT_STATE;
    /* access modifiers changed from: private */
    public HwCustApnReminder mHwCustApnReminder;
    String mImsi;
    String mPlmn;
    int mRemindType;
    private String mShowAPNMccMnc;
    int mSlotId = 0;
    String mTitle;
    ArrayList<PopupApnConfig> myPopupApnConfigs;
    ArrayList<PopupApnSettings> myPopupApnSettings;
    boolean restoreApn = false;

    static class PopupApnConfig {
        String apn;
        String displayName;
        String name;
        String onsName;
        String otherApns;
        String vmNumber;
        String vmTag;

        PopupApnConfig() {
        }
    }

    static class PopupApnSettings {
        String displayName;
        int id;
        String onsName;
        ArrayList<Integer> otherApnIds;
        String vmNumber;
        String vmTag;

        PopupApnSettings() {
        }
    }

    public static synchronized ApnReminder getInstance(Context context) {
        ApnReminder apnReminder;
        synchronized (ApnReminder.class) {
            if (instance == null) {
                instance = new ApnReminder(context);
            }
            apnReminder = instance;
        }
        return apnReminder;
    }

    public static synchronized ApnReminder getInstance(Context context, int slotId) {
        synchronized (ApnReminder.class) {
            if (slotId == 1) {
                if (instance1 == null) {
                    instance1 = new ApnReminder(context, slotId);
                }
                ApnReminder apnReminder = instance1;
                return apnReminder;
            }
            if (instance == null) {
                instance = new ApnReminder(context, slotId);
            }
            ApnReminder apnReminder2 = instance;
            return apnReminder2;
        }
    }

    private ApnReminder(Context context) {
        this.mContext = context;
        this.mHwCustApnReminder = (HwCustApnReminder) HwCustUtils.createObj(HwCustApnReminder.class, new Object[0]);
        this.mShowAPNMccMnc = Settings.System.getString(this.mContext.getContentResolver(), "hw_show_add_apn_plmn");
    }

    private ApnReminder(Context context, int slotId) {
        this.mContext = context;
        this.mSlotId = slotId;
        this.mHwCustApnReminder = (HwCustApnReminder) HwCustUtils.createObj(HwCustApnReminder.class, new Object[0]);
        this.mShowAPNMccMnc = Settings.System.getString(this.mContext.getContentResolver(), "hw_show_add_apn_plmn");
    }

    private void callForLog() {
        if (loadApnReminder(hwCfgPolicyPath)) {
            logd("loadApnReminder from hwCfgPolicyPath success");
        } else if (loadApnReminder(custFilePath)) {
            logd("loadApnReminder from cust success");
        } else if (loadApnReminder(systemFilePath)) {
            logd("loadApnReminder from system/etc success");
        } else {
            logd("can't find apn_reminder.xml, load failed!");
        }
    }

    private void loadAllApnIfNeeded() {
        if (this.mRemindType > 0) {
            if (this.imsiChanged || this.restoreApn) {
                logd("imsiChanged delete preference apn");
                ContentResolver resolver = this.mContext.getContentResolver();
                if (isMultiSimEnabled) {
                    resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mSlotId), null, null);
                } else {
                    resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
                }
            }
            loadAllApn();
        }
    }

    public void setPlmnAndImsi(String plmn, String imsi) {
        String oldImsi;
        logd("setPlmnAndImsi plmn = " + plmn);
        if (plmn != null && imsi != null) {
            this.mPlmn = plmn;
            this.mImsi = imsi;
            if (this.mContext != null) {
                SharedPreferences sp = this.mContext.getSharedPreferences("ApnReminderImsi", 0);
                if (isMultiSimEnabled) {
                    oldImsi = sp.getString(LAST_IMSI + this.mSlotId, null);
                } else {
                    oldImsi = sp.getString(LAST_IMSI, null);
                }
                logd("setPlmnAndImsi for card" + this.mSlotId);
                if (oldImsi != null) {
                    oldImsi = new String(Base64.decode(oldImsi, 0));
                }
                if (this.mImsi.trim().length() <= 0 || this.mImsi.equals(oldImsi)) {
                    this.imsiChanged = false;
                    logd("setPlmnAndImsi not imsiChanged");
                } else {
                    this.imsiChanged = true;
                    this.dialogDispalyed = false;
                    this.allApnFailed = false;
                    this.restoreApn = false;
                    logd("setPlmnAndImsi imsiChanged");
                    this.mHwCustApnReminder.notifyDisableAp(this.mContext, oldImsi);
                    this.mHwCustApnReminder.deletePreferApn(this.mContext, this.mImsi, this.mSlotId);
                }
                SharedPreferences.Editor editor = sp.edit();
                if (isMultiSimEnabled) {
                    editor.putString(LAST_IMSI + this.mSlotId, new String(Base64.encode(this.mImsi.getBytes(), 0)));
                } else {
                    editor.putString(LAST_IMSI, new String(Base64.encode(this.mImsi.getBytes(), 0)));
                }
                editor.commit();
            }
            this.myPopupApnConfigs = new ArrayList<>();
            callForLog();
            loadAllApnIfNeeded();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    private boolean loadApnReminder(String filePath) {
        File confFile;
        String nodePlmn;
        new File("/data/cust", "xml/apn_reminder.xml");
        if (hwCfgPolicyPath.equals(filePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/apn_reminder.xml", 0);
                if (cfg == null) {
                    return false;
                }
                confFile = cfg;
            } catch (NoClassDefFoundError e) {
                Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return false;
            }
        } else {
            confFile = new File(filePath, custFileName);
        }
        FileInputStream fin = null;
        try {
            FileInputStream fin2 = new FileInputStream(confFile);
            XmlPullParser confparser = Xml.newPullParser();
            if (confparser != null) {
                confparser.setInput(fin2, "UTF-8");
                XmlUtils.beginDocument(confparser, APN_REMINDER_DOCUMENT);
                while (true) {
                    int next = confparser.next();
                    int nodeType = next;
                    if (next != 1) {
                        logd("parse xml confparser.getName() = " + confparser.getName());
                        if (NODE_APN_REMINDER.equals(confparser.getName())) {
                            if (nodeType == 2) {
                                logd(NODE_APN_REMINDER + " tag parse");
                                logd("parse plmn is " + nodePlmn);
                                if (this.mPlmn.equals(nodePlmn)) {
                                    String nodeRemindType = confparser.getAttributeValue(null, ATTRIBUTE_REMIND_TYPE);
                                    this.mTitle = confparser.getAttributeValue(null, ATTRIBUTE_TITLE);
                                    int remindType = 0;
                                    if (ATTRIBUTE_REMIND_TYPE_IMSI_CHANGE.equals(nodeRemindType)) {
                                        remindType = REMIND_TYPE_IMSI_CHANGE;
                                    } else if (ATTRIBUTE_REMIND_TYPE_APN_FAILED.equals(nodeRemindType)) {
                                        remindType = REMIND_TYPE_ALL_APN_FAILED;
                                    }
                                    logd("parse remindType is " + remindType);
                                    if (remindType > 0) {
                                        this.mRemindType = remindType;
                                        while (true) {
                                            int next2 = confparser.next();
                                            int nodeType2 = next2;
                                            if (next2 != 1) {
                                                logd("parse confparser name " + confparser.getName());
                                                if (!NODE_APN.equals(confparser.getName()) || nodeType2 != 2) {
                                                    if (NODE_APN_REMINDER.equals(confparser.getName()) && nodeType2 == 2) {
                                                        logd(NODE_APN_REMINDER + " node end");
                                                        break;
                                                    }
                                                    logd("skip this node");
                                                } else {
                                                    PopupApnConfig popupApnConfig = new PopupApnConfig();
                                                    popupApnConfig.vmTag = confparser.getAttributeValue(null, APN_VOICEMAIL_TAG);
                                                    popupApnConfig.vmNumber = confparser.getAttributeValue(null, APN_VOICEMAIL_NUMBER);
                                                    popupApnConfig.otherApns = confparser.getAttributeValue(null, APN_OTHER_APNS);
                                                    popupApnConfig.onsName = confparser.getAttributeValue(null, APN_ONS_NAME);
                                                    popupApnConfig.displayName = confparser.getAttributeValue(null, APN_DISPLAY_NAME);
                                                    popupApnConfig.apn = confparser.getAttributeValue(null, APN_APN);
                                                    popupApnConfig.name = confparser.getAttributeValue(null, APN_NAME);
                                                    this.myPopupApnConfigs.add(popupApnConfig);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        logd("skip this node");
                    }
                }
            }
            try {
                fin2.close();
                logd("ApnReminder file is successfully load from filePath:" + filePath);
                return true;
            } catch (IOException e2) {
            }
            return false;
        } catch (FileNotFoundException e3) {
            logd("File not found: '" + confFile.getAbsolutePath() + "'");
            if (fin != null) {
                fin.close();
            }
            return false;
        } catch (Exception e4) {
            logd("Exception while parsing '" + confFile.getAbsolutePath() + "'" + e4);
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e5) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (fin != null) {
                fin.close();
            }
            throw th;
        }
    }

    private void loadAllApn() {
        if (this.mPlmn != null) {
            logd("createAllApnList: selection=" + selection);
            Cursor cursor = this.mContext.getContentResolver().query(getCarriersUriBySlot(), null, "numeric = ?" + " and carrier_enabled = 1", new String[]{this.mPlmn}, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    setAllApn(createApnList(cursor));
                }
                cursor.close();
            }
        }
    }

    private Uri getCarriersUriBySlot() {
        logd("getCarriersUriBySlot, mSlotId is: " + this.mSlotId);
        if (this.mSlotId == 0) {
            return Telephony.Carriers.CONTENT_URI;
        }
        if (this.mSlotId == 1) {
            return ContentUris.withAppendedId(URL_TELEPHONY_USING_SUBID, (long) this.mSlotId);
        }
        return Telephony.Carriers.CONTENT_URI;
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        Cursor cursor2 = cursor;
        ArrayList<ApnSetting> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                ApnSetting apn = new ApnSetting(cursor2.getInt(cursor2.getColumnIndexOrThrow("_id")), cursor2.getString(cursor2.getColumnIndexOrThrow("numeric")), cursor2.getString(cursor2.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME)), cursor2.getString(cursor2.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor2.getString(cursor2.getColumnIndexOrThrow("proxy"))), cursor2.getString(cursor2.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor2.getString(cursor2.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor2.getString(cursor2.getColumnIndexOrThrow("mmsproxy"))), cursor2.getString(cursor2.getColumnIndexOrThrow("mmsport")), cursor2.getString(cursor2.getColumnIndexOrThrow("user")), cursor2.getString(cursor2.getColumnIndexOrThrow("password")), cursor2.getInt(cursor2.getColumnIndexOrThrow("authtype")), parseTypes(cursor2.getString(cursor2.getColumnIndexOrThrow(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE))), cursor2.getString(cursor2.getColumnIndexOrThrow("protocol")), cursor2.getString(cursor2.getColumnIndexOrThrow("roaming_protocol")), cursor2.getInt(cursor2.getColumnIndexOrThrow("carrier_enabled")) == 1, cursor2.getInt(cursor2.getColumnIndexOrThrow("bearer")), cursor2.getInt(cursor2.getColumnIndexOrThrow("bearer_bitmask")), cursor2.getInt(cursor2.getColumnIndexOrThrow("profile_id")), cursor2.getInt(cursor2.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor2.getInt(cursor2.getColumnIndexOrThrow("max_conns")), cursor2.getInt(cursor2.getColumnIndexOrThrow("wait_time")), cursor2.getInt(cursor2.getColumnIndexOrThrow("max_conns_time")), cursor2.getInt(cursor2.getColumnIndexOrThrow("mtu")), cursor2.getString(cursor2.getColumnIndexOrThrow("mvno_type")), cursor2.getString(cursor2.getColumnIndexOrThrow("mvno_match_data")));
                result.add(apn);
            } while (cursor.moveToNext());
        }
        logd("createApnList: X result=" + result);
        return result;
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        return new String[]{"*"};
    }

    private void popupApnListIfNeed() {
        if (this.mImsi == null && this.mPlmn == null) {
            logd("popupApnListIfNeed imsi or plmn not loaded");
        } else if (this.myPopupApnSettings == null || this.myPopupApnSettings.size() == 0) {
            logd("popupApnListIfNeed no myPopupApnSettings content");
        } else {
            logd("popupApnListIfNeed mRemindType = " + this.mRemindType);
            if ((this.mRemindType == REMIND_TYPE_IMSI_CHANGE && this.imsiChanged) || (this.mRemindType == REMIND_TYPE_IMSI_CHANGE && this.restoreApn)) {
                int gid1 = getGID1();
                logd("popupApnListIfNeed get gid1 = " + gid1 + " mPlmn =" + this.mPlmn);
                if (!PLMN_VIRGIN_MEDIA.equals(this.mPlmn) || GID1_VIRGIN_MEDIA != gid1) {
                    logd("popupApnListIfNeed imsi changed");
                    showAlertDialog();
                    return;
                }
                logd("Virgin Media simcard, do not popup dialog");
            } else if (this.mRemindType == REMIND_TYPE_ALL_APN_FAILED && this.allApnFailed && this.imsiChanged) {
                logd("popupApnListIfNeed allApnFailed");
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        if (!this.dialogDispalyed) {
            boolean isSetCardTwo = true;
            this.dialogDispalyed = true;
            int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this.mContext, themeID), themeID);
            if (isMultiSimEnabled) {
                logd("showAlertDialog for double card :" + this.mSlotId);
                if (this.mSlotId == 0 && this.mTitle != null && this.mTitle.trim().length() > 0) {
                    this.mTitle += " for card1";
                }
                if (this.mSlotId != 1 || this.mTitle == null || this.mTitle.trim().length() <= 0) {
                    isSetCardTwo = false;
                }
                if (isSetCardTwo) {
                    this.mTitle += " for card2";
                }
                builder.setTitle(this.mTitle);
            } else {
                logd("showAlertDialog for single card");
                if (this.mTitle == null || this.mTitle.trim().length() <= 0) {
                    builder.setTitle("choose your apn");
                } else {
                    builder.setTitle(this.mTitle);
                }
            }
            builder.setCancelable(false);
            String[] apnChoices = new String[this.myPopupApnSettings.size()];
            int myApnSettingSize = this.myPopupApnSettings.size();
            for (int i = 0; i < myApnSettingSize; i++) {
                apnChoices[i] = this.myPopupApnSettings.get(i).displayName;
            }
            builder.setSingleChoiceItems(apnChoices, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which >= 0 && which < ApnReminder.this.myPopupApnSettings.size()) {
                        if (ApnReminder.this.mHwCustApnReminder != null) {
                            ApnReminder.this.mHwCustApnReminder.dataRoamingSwitchForCust(ApnReminder.this.myPopupApnSettings.get(which).displayName, ApnReminder.this.mContext, ApnReminder.this.mSlotId, ApnReminder.isMultiSimEnabled);
                        }
                        ApnReminder.this.setSelectedApnKey(which);
                        dialog.dismiss();
                    }
                }
            });
            if (isShowAddAPN(this.mPlmn) && this.mHwCustApnReminder != null) {
                this.mHwCustApnReminder.showNewAddAPN(this.mContext, builder);
            }
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setType(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_MODE_DONE);
            alertDialog.show();
        }
    }

    public boolean isShowAddAPN(String currentMccMnc) {
        if (!TextUtils.isEmpty(this.mShowAPNMccMnc) && !TextUtils.isEmpty(currentMccMnc)) {
            String[] mccmnc = this.mShowAPNMccMnc.trim().split(",");
            for (String equals : mccmnc) {
                if (currentMccMnc.equals(equals)) {
                    logd("isShowAddAPN = true");
                    return true;
                }
            }
        }
        logd("isShowAddAPN = false");
        return false;
    }

    /* access modifiers changed from: private */
    public void setSelectedApnKey(int index) {
        logd("setSelectedApnKey: delete");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (isMultiSimEnabled) {
            resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mSlotId), null, null);
        } else {
            resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
        }
        if (index >= 0 && index < this.myPopupApnSettings.size()) {
            int pos = this.myPopupApnSettings.get(index).id;
            if (pos >= 0) {
                logd("setPreferredApn: update");
                ContentValues values = new ContentValues();
                values.put(APN_ID, Integer.valueOf(pos));
                if (isMultiSimEnabled) {
                    resolver.update(ContentUris.withAppendedId(PREFERAPN_UPDATE_URI, (long) this.mSlotId), values, null, null);
                } else {
                    resolver.update(PREFERAPN_UPDATE_URI, values, null, null);
                }
            }
            this.mContext.sendBroadcast(new Intent("android.intent.action.refreshapn"));
        }
    }

    public void allApnActiveFailed() {
        logd("allApnActiveFailed");
        this.allApnFailed = true;
        popupApnListIfNeed();
    }

    private void setAllApn(ArrayList<ApnSetting> apns) {
        logd("setAllApn myPopupApnConfigs = " + this.myPopupApnConfigs);
        this.myPopupApnSettings = new ArrayList<>();
        if (this.myPopupApnConfigs != null && apns != null) {
            int config_list_size = this.myPopupApnConfigs.size();
            for (int i = 0; i < config_list_size; i++) {
                PopupApnConfig apnConfig = this.myPopupApnConfigs.get(i);
                int j = 0;
                int apn_list_size = apns.size();
                while (true) {
                    if (j >= apn_list_size) {
                        break;
                    }
                    ApnSetting apnSetting = apns.get(j);
                    if (apnSetting.canHandleType("default")) {
                        logd("apnConfig.apn = " + apnConfig.apn);
                        logd("apnConfig.name = " + apnConfig.name);
                        logd("apnSetting.apn = " + apnSetting.apn);
                        logd("apnSetting.carrier = " + apnSetting.carrier);
                        logd("apnConfig.displayName = " + apnConfig.displayName);
                        logd("apnConfig.onsName = " + apnConfig.onsName);
                        logd("apnConfig.otherApns = " + apnConfig.otherApns);
                        logd("apnConfig.vmTag = " + apnConfig.vmTag);
                        try {
                            if (!(apnConfig.apn == null || !apnConfig.apn.equals(apnSetting.apn) || apnConfig.name == null || apnSetting.carrier == null || !ArrayUtils.equals(apnConfig.name.getBytes("UTF-8"), apnSetting.carrier.getBytes("UTF-8"), apnConfig.name.getBytes("UTF-8").length) || apnConfig.displayName == null)) {
                                logd("find match popupApnSettings displayName = " + apnConfig.displayName);
                                PopupApnSettings popupApnSettings = new PopupApnSettings();
                                popupApnSettings.id = apnSetting.id;
                                popupApnSettings.displayName = apnConfig.displayName;
                                popupApnSettings.onsName = apnConfig.onsName;
                                if (apnConfig.otherApns != null && !"".equals(apnConfig.otherApns)) {
                                    setOtherApnIds(apns, popupApnSettings, apnConfig);
                                }
                                popupApnSettings.vmNumber = apnConfig.vmNumber;
                                popupApnSettings.vmTag = apnConfig.vmTag;
                                this.myPopupApnSettings.add(popupApnSettings);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    j++;
                }
            }
            this.myPopupApnConfigs = null;
            popupApnListIfNeed();
        }
    }

    private static void logd(String msg) {
        String str = LOG_TAG;
        Log.d(str, "[ApnReminder] " + msg);
    }

    public void setGID1(byte[] gid1) {
        if (gid1 != null && gid1.length > 0) {
            this.mGID1 = gid1[0];
            int sum = 0;
            String gid1String = IccUtils.bytesToHexString(gid1);
            if (!(gid1String == null || gid1String.length() <= 2 || gid1String.charAt(2) == 'f')) {
                int gid1Length = 0;
                int gid1StringLength = gid1String.length();
                int i = 0;
                while (true) {
                    if (i >= gid1StringLength) {
                        break;
                    } else if (gid1String.charAt(i) == 'f') {
                        gid1Length = i;
                        break;
                    } else {
                        i++;
                    }
                }
                for (int i2 = 0; i2 < gid1Length; i2++) {
                    if (gid1String.charAt(i2) >= '0' && gid1String.charAt(i2) <= '9') {
                        sum = ((sum * 16) + gid1String.charAt(i2)) - 48;
                    }
                    if (gid1String.charAt(i2) >= 'A' && gid1String.charAt(i2) <= 'F') {
                        sum = (((sum * 16) + gid1String.charAt(i2)) - 65) + 10;
                    }
                }
                this.mGID1 = sum;
            }
            logd("setGID1 mGID1 = " + this.mGID1);
        }
    }

    private int getGID1() {
        return this.mGID1;
    }

    public void restoreApn(String plmn, String imsi) {
        logd("restoreApn");
        this.restoreApn = true;
        this.dialogDispalyed = false;
        setPlmnAndImsi(plmn, imsi);
    }

    public boolean isPopupApnSettingsEmpty() {
        if (this.myPopupApnSettings == null || this.myPopupApnSettings.size() == 0) {
            return true;
        }
        return false;
    }

    public String getOnsNameByPreferedApn(int apnId, String plmnValue) {
        return queryValueByPreferedApn(apnId, 1, plmnValue);
    }

    private void setOtherApnIds(ArrayList<ApnSetting> apns, PopupApnSettings popupApnSettings, PopupApnConfig apnConfig) {
        ArrayList<ApnSetting> arrayList = apns;
        PopupApnSettings popupApnSettings2 = popupApnSettings;
        PopupApnConfig popupApnConfig = apnConfig;
        if (arrayList != null && popupApnSettings2 != null && popupApnConfig != null && popupApnConfig.otherApns != null && !"".equals(popupApnConfig.otherApns)) {
            popupApnSettings2.otherApnIds = new ArrayList<>();
            String[] otherNameApnArray = popupApnConfig.otherApns.split(";");
            int length = otherNameApnArray.length;
            char c = 0;
            int i = 0;
            while (i < length) {
                String[] nameApn = otherNameApnArray[i].split(":");
                String name = nameApn[c];
                String apn = nameApn[1];
                int list_size = apns.size();
                int i2 = 0;
                while (true) {
                    if (i2 >= list_size) {
                        break;
                    }
                    ApnSetting apnSetting = arrayList.get(i2);
                    if (apnSetting.canHandleType("default") && apn != null) {
                        try {
                            if (apn.equals(apnSetting.apn) && name != null && apnSetting.carrier != null && ArrayUtils.equals(name.getBytes("UTF-8"), apnSetting.carrier.getBytes("UTF-8"), name.getBytes("UTF-8").length)) {
                                logd("find match other apn = " + apn + " name = " + name);
                                popupApnSettings2.otherApnIds.add(Integer.valueOf(apnSetting.id));
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    i2++;
                }
                i++;
                c = 0;
            }
        }
    }

    public String getVoiceMailNumberByPreferedApn(int apnId, String vmNumber) {
        return queryValueByPreferedApn(apnId, 2, vmNumber);
    }

    public String getVoiceMailTagByPreferedApn(int apnId, String vmTag) {
        return queryValueByPreferedApn(apnId, 3, vmTag);
    }

    private String queryValueByPreferedApn(int apnId, int queryType, String queryValue) {
        if (!isPopupApnSettingsEmpty()) {
            int myApnSettingSize = this.myPopupApnSettings.size();
            int i = 0;
            while (true) {
                if (i >= myApnSettingSize) {
                    break;
                } else if (apnId == this.myPopupApnSettings.get(i).id) {
                    queryValue = getValueByQueryType(queryType, queryValue, this.myPopupApnSettings.get(i));
                    logd("find matched value by apnId: " + apnId + " queryType: " + queryType);
                    break;
                } else {
                    if (this.myPopupApnSettings.get(i).otherApnIds != null && this.myPopupApnSettings.get(i).otherApnIds.size() > 0) {
                        int myApnSettingOtherApnIdSize = this.myPopupApnSettings.get(i).otherApnIds.size();
                        for (int j = 0; j < myApnSettingOtherApnIdSize; j++) {
                            if (apnId == this.myPopupApnSettings.get(i).otherApnIds.get(j).intValue()) {
                                String queryValue2 = getValueByQueryType(queryType, queryValue, this.myPopupApnSettings.get(i));
                                logd("find matched value by other apnId: " + apnId + " queryType: " + queryType);
                                return queryValue2;
                            }
                        }
                        continue;
                    }
                    i++;
                }
            }
        }
        return queryValue;
    }

    private String getValueByQueryType(int queryType, String queryValue, PopupApnSettings popupApnSettings) {
        switch (queryType) {
            case 1:
                return popupApnSettings.onsName;
            case 2:
                return popupApnSettings.vmNumber;
            case 3:
                return popupApnSettings.vmTag;
            default:
                return queryValue;
        }
    }

    public HwCustApnReminder getCust() {
        return this.mHwCustApnReminder;
    }
}
