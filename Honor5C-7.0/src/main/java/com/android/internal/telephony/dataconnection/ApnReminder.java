package com.android.internal.telephony.dataconnection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.NetworkUtils;
import android.net.Uri;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import com.android.internal.telephony.HwSubscriptionManager;
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
    private static String APN_APN = null;
    private static String APN_DISPLAY_NAME = null;
    private static final String APN_ID = "apn_id";
    private static String APN_NAME = null;
    private static String APN_ONS_NAME = null;
    private static String APN_OTHER_APNS = null;
    private static String APN_REMINDER_DOCUMENT = null;
    private static String APN_VOICEMAIL_NUMBER = null;
    private static String APN_VOICEMAIL_TAG = null;
    private static String ATTRIBUTE_PLMN = null;
    private static String ATTRIBUTE_REMIND_TYPE = null;
    private static String ATTRIBUTE_REMIND_TYPE_APN_FAILED = null;
    private static String ATTRIBUTE_REMIND_TYPE_IMSI_CHANGE = null;
    private static String ATTRIBUTE_TITLE = null;
    private static int GID1_VIRGIN_MEDIA = 0;
    private static String LAST_IMSI = null;
    private static String LOG_TAG = null;
    private static String NODE_APN = null;
    private static String NODE_APN_REMINDER = null;
    private static String PLMN_VIRGIN_MEDIA = null;
    private static final Uri PREFERAPN_NO_UPDATE_URI = null;
    private static final Uri PREFERAPN_UPDATE_URI = null;
    private static final int QUERY_TYPE_ONS_NAME = 1;
    private static final int QUERY_TYPE_VOICEMAIL_NUMBER = 2;
    private static final int QUERY_TYPE_VOICEMAIL_TAG = 3;
    private static int REMIND_TYPE_ALL_APN_FAILED;
    private static int REMIND_TYPE_IMSI_CHANGE;
    private static final Uri URL_TELEPHONY_USING_SUBID = null;
    private static String custFileName;
    private static String custFilePath;
    private static String hwCfgPolicyPath;
    private static ApnReminder instance;
    private static ApnReminder instance1;
    private static final boolean isMultiSimEnabled = false;
    private static String systemFilePath;
    boolean allApnFailed;
    boolean dialogDispalyed;
    boolean imsiChanged;
    Context mContext;
    private int mGID1;
    private HwCustApnReminder mHwCustApnReminder;
    String mImsi;
    String mPlmn;
    int mRemindType;
    private String mShowAPNMccMnc;
    int mSlotId;
    String mTitle;
    ArrayList<PopupApnConfig> myPopupApnConfigs;
    ArrayList<PopupApnSettings> myPopupApnSettings;
    boolean restoreApn;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.ApnReminder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.ApnReminder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.ApnReminder.<clinit>():void");
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
            if (slotId == QUERY_TYPE_ONS_NAME) {
                if (instance1 == null) {
                    instance1 = new ApnReminder(context, slotId);
                }
                ApnReminder apnReminder = instance1;
                return apnReminder;
            }
            if (instance == null) {
                instance = new ApnReminder(context, slotId);
            }
            apnReminder = instance;
            return apnReminder;
        }
    }

    private ApnReminder(Context context) {
        this.mGID1 = HwSubscriptionManager.SUB_INIT_STATE;
        this.restoreApn = false;
        this.allApnFailed = false;
        this.imsiChanged = false;
        this.dialogDispalyed = false;
        this.mSlotId = 0;
        this.mContext = context;
        this.mHwCustApnReminder = (HwCustApnReminder) HwCustUtils.createObj(HwCustApnReminder.class, new Object[0]);
        this.mShowAPNMccMnc = System.getString(this.mContext.getContentResolver(), "hw_show_add_apn_plmn");
    }

    private ApnReminder(Context context, int slotId) {
        this.mGID1 = HwSubscriptionManager.SUB_INIT_STATE;
        this.restoreApn = false;
        this.allApnFailed = false;
        this.imsiChanged = false;
        this.dialogDispalyed = false;
        this.mSlotId = 0;
        this.mContext = context;
        this.mSlotId = slotId;
        this.mHwCustApnReminder = (HwCustApnReminder) HwCustUtils.createObj(HwCustApnReminder.class, new Object[0]);
        this.mShowAPNMccMnc = System.getString(this.mContext.getContentResolver(), "hw_show_add_apn_plmn");
    }

    public void setPlmnAndImsi(String plmn, String imsi) {
        logd("setPlmnAndImsi plmn = " + plmn);
        if (plmn != null && imsi != null) {
            this.mPlmn = plmn;
            this.mImsi = imsi;
            if (this.mContext != null) {
                String oldImsi;
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
                    this.mHwCustApnReminder.notifyDisableAp(oldImsi);
                    this.mHwCustApnReminder.deletePreferApn(this.mContext, this.mImsi, this.mSlotId);
                }
                Editor editor = sp.edit();
                if (isMultiSimEnabled) {
                    editor.putString(LAST_IMSI + this.mSlotId, new String(Base64.encode(this.mImsi.getBytes(), 0)));
                } else {
                    editor.putString(LAST_IMSI, new String(Base64.encode(this.mImsi.getBytes(), 0)));
                }
                editor.commit();
            }
            this.myPopupApnConfigs = new ArrayList();
            if (loadApnReminder(hwCfgPolicyPath)) {
                logd("loadApnReminder from hwCfgPolicyPath success");
            } else if (loadApnReminder(custFilePath)) {
                logd("loadApnReminder from cust success");
            } else if (loadApnReminder(systemFilePath)) {
                logd("loadApnReminder from system/etc success");
            } else {
                logd("can't find apn_reminder.xml, load failed!");
            }
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
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadApnReminder(String filePath) {
        Exception e;
        Throwable th;
        File confFile = new File("/data/cust", "xml/apn_reminder.xml");
        if (hwCfgPolicyPath.equals(filePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/apn_reminder.xml", 0);
                if (cfg == null) {
                    return false;
                }
                confFile = cfg;
            } catch (NoClassDefFoundError e2) {
                Log.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return false;
            }
        }
        confFile = new File(filePath, custFileName);
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fin = new FileInputStream(confFile);
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    int nodeType;
                    int remindType;
                    confparser.setInput(fin, "UTF-8");
                    XmlUtils.beginDocument(confparser, APN_REMINDER_DOCUMENT);
                    while (true) {
                        nodeType = confparser.next();
                        if (nodeType == QUERY_TYPE_ONS_NAME) {
                            break;
                        }
                        logd("parse xml confparser.getName() = " + confparser.getName());
                        if (NODE_APN_REMINDER.equals(confparser.getName()) && nodeType == QUERY_TYPE_VOICEMAIL_NUMBER) {
                            logd(NODE_APN_REMINDER + " tag parse");
                            String nodePlmn = confparser.getAttributeValue(null, ATTRIBUTE_PLMN);
                            logd("parse plmn is " + nodePlmn);
                            if (this.mPlmn.equals(nodePlmn)) {
                                String nodeRemindType = confparser.getAttributeValue(null, ATTRIBUTE_REMIND_TYPE);
                                this.mTitle = confparser.getAttributeValue(null, ATTRIBUTE_TITLE);
                                remindType = 0;
                                if (ATTRIBUTE_REMIND_TYPE_IMSI_CHANGE.equals(nodeRemindType)) {
                                    remindType = REMIND_TYPE_IMSI_CHANGE;
                                } else if (ATTRIBUTE_REMIND_TYPE_APN_FAILED.equals(nodeRemindType)) {
                                    remindType = REMIND_TYPE_ALL_APN_FAILED;
                                }
                                logd("parse remindType is " + remindType);
                                if (remindType > 0) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            logd("skip this node");
                        }
                    }
                    this.mRemindType = remindType;
                    while (true) {
                        nodeType = confparser.next();
                        if (nodeType == QUERY_TYPE_ONS_NAME) {
                            break;
                        }
                        logd("parse confparser name " + confparser.getName());
                        if (!NODE_APN.equals(confparser.getName()) || nodeType != QUERY_TYPE_VOICEMAIL_NUMBER) {
                            if (NODE_APN_REMINDER.equals(confparser.getName()) && nodeType == QUERY_TYPE_VOICEMAIL_NUMBER) {
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
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e3) {
                        return false;
                    }
                }
                logd("ApnReminder file is successfully load from filePath:" + filePath);
                return true;
            } catch (FileNotFoundException e4) {
                fileInputStream = fin;
            } catch (Exception e5) {
                e = e5;
                fileInputStream = fin;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = fin;
            }
        } catch (FileNotFoundException e6) {
            try {
                logd("File not found: '" + confFile.getAbsolutePath() + "'");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e7) {
                        return false;
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e8) {
                        return false;
                    }
                }
                throw th;
            }
        } catch (Exception e9) {
            e = e9;
            logd("Exception while parsing '" + confFile.getAbsolutePath() + "'" + e);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e10) {
                    return false;
                }
            }
            return false;
        }
    }

    private void loadAllApn() {
        if (this.mPlmn != null) {
            String selection = ("numeric = '" + this.mPlmn + "'") + " and carrier_enabled = 1";
            logd("createAllApnList: selection=" + selection);
            Cursor cursor = this.mContext.getContentResolver().query(getCarriersUriBySlot(), null, selection, null, null);
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
            return Carriers.CONTENT_URI;
        }
        if (this.mSlotId == QUERY_TYPE_ONS_NAME) {
            return ContentUris.withAppendedId(URL_TELEPHONY_USING_SUBID, (long) this.mSlotId);
        }
        return Carriers.CONTENT_URI;
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ArrayList<ApnSetting> result = new ArrayList();
        if (cursor.moveToFirst()) {
            do {
                result.add(new ApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow(VirtualNets.NUMERIC)), cursor.getString(cursor.getColumnIndexOrThrow(NumMatchs.NAME)), cursor.getString(cursor.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("proxy"))), cursor.getString(cursor.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsproxy"))), cursor.getString(cursor.getColumnIndexOrThrow("mmsport")), cursor.getString(cursor.getColumnIndexOrThrow("user")), cursor.getString(cursor.getColumnIndexOrThrow("password")), cursor.getInt(cursor.getColumnIndexOrThrow("authtype")), parseTypes(cursor.getString(cursor.getColumnIndexOrThrow(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE))), cursor.getString(cursor.getColumnIndexOrThrow("protocol")), cursor.getString(cursor.getColumnIndexOrThrow("roaming_protocol")), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == QUERY_TYPE_ONS_NAME, cursor.getInt(cursor.getColumnIndexOrThrow("bearer")), cursor.getInt(cursor.getColumnIndexOrThrow("bearer_bitmask")), cursor.getInt(cursor.getColumnIndexOrThrow("profile_id")), cursor.getInt(cursor.getColumnIndexOrThrow("modem_cognitive")) == QUERY_TYPE_ONS_NAME, cursor.getInt(cursor.getColumnIndexOrThrow("max_conns")), cursor.getInt(cursor.getColumnIndexOrThrow("wait_time")), cursor.getInt(cursor.getColumnIndexOrThrow("max_conns_time")), cursor.getInt(cursor.getColumnIndexOrThrow("mtu")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_type")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data"))));
            } while (cursor.moveToNext());
        }
        logd("createApnList: X result=" + result);
        return result;
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        String[] result = new String[QUERY_TYPE_ONS_NAME];
        result[0] = "*";
        return result;
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
                if (PLMN_VIRGIN_MEDIA.equals(this.mPlmn) && GID1_VIRGIN_MEDIA == gid1) {
                    logd("Virgin Media simcard, do not popup dialog");
                    return;
                }
                logd("popupApnListIfNeed imsi changed");
                showAlertDialog();
            } else if (this.mRemindType == REMIND_TYPE_ALL_APN_FAILED && this.allApnFailed && this.imsiChanged) {
                logd("popupApnListIfNeed allApnFailed");
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        if (!this.dialogDispalyed) {
            this.dialogDispalyed = true;
            int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            Builder builder = new Builder(new ContextThemeWrapper(this.mContext, themeID), themeID);
            if (isMultiSimEnabled) {
                logd("showAlertDialog for double card :" + this.mSlotId);
                if (this.mSlotId == 0 && this.mTitle != null && this.mTitle.trim().length() > 0) {
                    this.mTitle += " for card1";
                }
                if (this.mSlotId == QUERY_TYPE_ONS_NAME && this.mTitle != null && this.mTitle.trim().length() > 0) {
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
            for (int i = 0; i < this.myPopupApnSettings.size(); i += QUERY_TYPE_ONS_NAME) {
                apnChoices[i] = ((PopupApnSettings) this.myPopupApnSettings.get(i)).displayName;
            }
            builder.setSingleChoiceItems(apnChoices, -1, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which >= 0 && which < ApnReminder.this.myPopupApnSettings.size()) {
                        if (ApnReminder.this.mHwCustApnReminder != null) {
                            ApnReminder.this.mHwCustApnReminder.dataRoamingSwitchForCust(((PopupApnSettings) ApnReminder.this.myPopupApnSettings.get(which)).displayName, ApnReminder.this.mContext, ApnReminder.this.mSlotId, ApnReminder.isMultiSimEnabled);
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
            alertDialog.getWindow().setType(2003);
            alertDialog.show();
        }
    }

    public boolean isShowAddAPN(String currentMccMnc) {
        if (!(TextUtils.isEmpty(this.mShowAPNMccMnc) || TextUtils.isEmpty(currentMccMnc))) {
            String[] mccmnc = this.mShowAPNMccMnc.trim().split(",");
            for (int i = 0; i < mccmnc.length; i += QUERY_TYPE_ONS_NAME) {
                if (currentMccMnc.equals(mccmnc[i])) {
                    logd("isShowAddAPN = true");
                    return true;
                }
            }
        }
        logd("isShowAddAPN = false");
        return false;
    }

    private void setSelectedApnKey(int index) {
        logd("setSelectedApnKey: delete");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (isMultiSimEnabled) {
            resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mSlotId), null, null);
        } else {
            resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
        }
        if (index >= 0 && index < this.myPopupApnSettings.size()) {
            int pos = ((PopupApnSettings) this.myPopupApnSettings.get(index)).id;
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
        this.myPopupApnSettings = new ArrayList();
        if (this.myPopupApnConfigs != null && apns != null) {
            for (PopupApnConfig apnConfig : this.myPopupApnConfigs) {
                for (ApnSetting apnSetting : apns) {
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
                                if (!(apnConfig.otherApns == null || "".equals(apnConfig.otherApns))) {
                                    setOtherApnIds(apns, popupApnSettings, apnConfig);
                                }
                                popupApnSettings.vmNumber = apnConfig.vmNumber;
                                popupApnSettings.vmTag = apnConfig.vmTag;
                                this.myPopupApnSettings.add(popupApnSettings);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            this.myPopupApnConfigs = null;
            popupApnListIfNeed();
        }
    }

    private static void logd(String msg) {
        Log.d(LOG_TAG, "[ApnReminder] " + msg);
    }

    public void setGID1(byte[] gid1) {
        if (gid1 != null && gid1.length > 0) {
            this.mGID1 = gid1[0];
            int sum = 0;
            String gid1String = IccUtils.bytesToHexString(gid1);
            if (!(gid1String == null || gid1String.length() <= QUERY_TYPE_VOICEMAIL_NUMBER || gid1String.charAt(QUERY_TYPE_VOICEMAIL_NUMBER) == 'f')) {
                int i;
                int gid1Length = 0;
                for (i = 0; i < gid1String.length(); i += QUERY_TYPE_ONS_NAME) {
                    if (gid1String.charAt(i) == 'f') {
                        gid1Length = i;
                        break;
                    }
                }
                i = 0;
                while (i < gid1Length) {
                    if (gid1String.charAt(i) >= '0' && gid1String.charAt(i) <= '9') {
                        sum = ((sum * 16) + gid1String.charAt(i)) - 48;
                    }
                    if (gid1String.charAt(i) >= 'A' && gid1String.charAt(i) <= 'F') {
                        sum = (((sum * 16) + gid1String.charAt(i)) - 65) + 10;
                    }
                    i += QUERY_TYPE_ONS_NAME;
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
        return queryValueByPreferedApn(apnId, QUERY_TYPE_ONS_NAME, plmnValue);
    }

    private void setOtherApnIds(ArrayList<ApnSetting> apns, PopupApnSettings popupApnSettings, PopupApnConfig apnConfig) {
        if (apns != null && popupApnSettings != null && apnConfig != null && apnConfig.otherApns != null && !"".equals(apnConfig.otherApns)) {
            popupApnSettings.otherApnIds = new ArrayList();
            String[] otherNameApnArray = apnConfig.otherApns.split(";");
            int length = otherNameApnArray.length;
            for (int i = 0; i < length; i += QUERY_TYPE_ONS_NAME) {
                String[] nameApn = otherNameApnArray[i].split(":");
                String name = nameApn[0];
                String apn = nameApn[QUERY_TYPE_ONS_NAME];
                for (ApnSetting apnSetting : apns) {
                    if (apnSetting.canHandleType("default") && apn != null) {
                        try {
                            if (apn.equals(apnSetting.apn) && name != null && apnSetting.carrier != null && ArrayUtils.equals(name.getBytes("UTF-8"), apnSetting.carrier.getBytes("UTF-8"), name.getBytes("UTF-8").length)) {
                                logd("find match other apn = " + apn + " name = " + name);
                                popupApnSettings.otherApnIds.add(Integer.valueOf(apnSetting.id));
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String getVoiceMailNumberByPreferedApn(int apnId, String vmNumber) {
        return queryValueByPreferedApn(apnId, QUERY_TYPE_VOICEMAIL_NUMBER, vmNumber);
    }

    public String getVoiceMailTagByPreferedApn(int apnId, String vmTag) {
        return queryValueByPreferedApn(apnId, QUERY_TYPE_VOICEMAIL_TAG, vmTag);
    }

    private String queryValueByPreferedApn(int apnId, int queryType, String queryValue) {
        if (!isPopupApnSettingsEmpty()) {
            int i = 0;
            while (i < this.myPopupApnSettings.size()) {
                if (apnId == ((PopupApnSettings) this.myPopupApnSettings.get(i)).id) {
                    queryValue = getValueByQueryType(queryType, queryValue, (PopupApnSettings) this.myPopupApnSettings.get(i));
                    logd("find matched value by apnId: " + apnId + " queryType: " + queryType);
                    break;
                }
                if (((PopupApnSettings) this.myPopupApnSettings.get(i)).otherApnIds != null && ((PopupApnSettings) this.myPopupApnSettings.get(i)).otherApnIds.size() > 0) {
                    for (int j = 0; j < ((PopupApnSettings) this.myPopupApnSettings.get(i)).otherApnIds.size(); j += QUERY_TYPE_ONS_NAME) {
                        if (apnId == ((Integer) ((PopupApnSettings) this.myPopupApnSettings.get(i)).otherApnIds.get(j)).intValue()) {
                            queryValue = getValueByQueryType(queryType, queryValue, (PopupApnSettings) this.myPopupApnSettings.get(i));
                            logd("find matched value by other apnId: " + apnId + " queryType: " + queryType);
                            return queryValue;
                        }
                    }
                    continue;
                }
                i += QUERY_TYPE_ONS_NAME;
            }
        }
        return queryValue;
    }

    private String getValueByQueryType(int queryType, String queryValue, PopupApnSettings popupApnSettings) {
        switch (queryType) {
            case QUERY_TYPE_ONS_NAME /*1*/:
                return popupApnSettings.onsName;
            case QUERY_TYPE_VOICEMAIL_NUMBER /*2*/:
                return popupApnSettings.vmNumber;
            case QUERY_TYPE_VOICEMAIL_TAG /*3*/:
                return popupApnSettings.vmTag;
            default:
                return queryValue;
        }
    }

    public HwCustApnReminder getCust() {
        return this.mHwCustApnReminder;
    }
}
