package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.emcom.EmcomManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.DataFailCause;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PcoData;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Xml;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.GlobalParamsAdaptor;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwCustTelephonyProperties;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwTelephonyPropertiesInner;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustDcTrackerImpl extends HwCustDcTracker {
    private static final String APNS_OPKEY_CONFIG_FILE = "apns-opkey.xml";
    private static final String APN_OPKEY_INFO = "apnOpkeyInfo";
    private static final String APN_OPKEY_INFO_APN = "apn";
    private static final String APN_OPKEY_INFO_NUMERIC = "numeric";
    private static final String APN_OPKEY_INFO_OPKEY = "opkey";
    private static final String APN_OPKEY_INFO_USER = "user";
    private static final String APN_OPKEY_LAST_IMSI = "apn_opkey_last_imsi";
    private static final String APN_OPKEY_LIST_DOCUMENT = "apnOpkeyNodes";
    private static final String APN_OPKEY_NODE = "apnOpkeyNode";
    private static final int CAUSE_BY_DATA = 0;
    private static final int CAUSE_BY_ROAM = 1;
    private static final String CLEARCODE_2HOUR_DELAY_OVER = "clearcode2HourDelayOver";
    private static final String CUST_ADD_APEC_APN_BUILD = SystemProperties.get("ro.config.add_spec_apn_build", "");
    private static final boolean CUST_ENABLE_OTA_BIP = SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false);
    private static final String CUST_PREFERRED_APN = SystemProperties.get("ro.hwpp.preferred_apn", "").trim();
    private static final boolean CUST_RETRY_CONFIG = SystemProperties.getBoolean("ro.config.cust_retry_config", false);
    private static final int DATA_ROAMING_EXCEPTION = -1;
    private static final int DATA_ROAMING_INTERNATIONAL = 2;
    private static final int DATA_ROAMING_NATIONAL = 1;
    private static final int DATA_ROAMING_OFF = 0;
    private static final boolean DBG = true;
    private static final String DEFAULT_PCO_DATA = "-2;-2";
    private static final int DEFAULT_PCO_VALUE = -2;
    private static final int DELAY_2_HOUR = 7200000;
    private static final boolean DISABLE_SIM2_DATA = SystemProperties.getBoolean("ro.config.hw_disable_sim2_data", false);
    private static final String DOCOMO_FOTA_APN = "open-dm2.dcm-dm.ne.jp";
    private static final int EMM_CAUSE_NO_SUITABLE_CELLS_IN_TA = 15;
    private static final String ENABLE_SES_CHECK_KEY = "enable_ses_check";
    private static final boolean ENABLE_WIFI_LTE_CE = SystemProperties.getBoolean("ro.config.enable_wl_coexist", false);
    private static final boolean ESM_FLAG_ADAPTION_ENABLED = SystemProperties.getBoolean("ro.config.attach_apn_enabled", false);
    private static final int ESM_FLAG_INVALID = -1;
    private static final int EVENT_FDN_RECORDS_LOADED = 2;
    private static final int EVENT_FDN_SWITCH_CHANGED = 1;
    private static final int EVENT_LIMIT_PDP_ACT_IND = 3;
    private static final int EXPLICIT_DROP = -1;
    private static final int EXPLICIT_KEEP = 1;
    private static final Uri FDN_URL = Uri.parse("content://icc/fdn/subId/");
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final int IMPLICIT_KEEP = 0;
    private static final int IMS_PCO_TYPE = 1;
    private static final String INTENT_LIMIT_PDP_ACT_IND = "com.android.internal.telephony.limitpdpactind";
    private static final String INTENT_SET_PREF_NETWORK_TYPE = "com.android.internal.telephony.set-pref-networktype";
    private static final String INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE = "network_type";
    private static final int INTERNET_PCO_TYPE = 3;
    private static final boolean IS_ATT = ("07".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
    private static final boolean IS_CLEAR_CODE_ENABLED = SystemProperties.getBoolean("ro.config.hw_clearcode_pdp", false);
    private static final boolean IS_DOCOMO = (SystemProperties.get("ro.config.hw_opta", "0").equals("341") && SystemProperties.get("ro.config.hw_optb", "0").equals("392"));
    private static final String IS_LIMIT_PDP_ACT = "islimitpdpact";
    private static final boolean IS_MULTI_SIM_ENABLED = HwFrameworkFactory.getHwInnerTelephonyManager().isMultiSimEnabled();
    private static final boolean IS_PDN_REJ_CURE_ENABLE = SystemProperties.getBoolean("ro.config.hw_pdn_rej_data_cure", (boolean) DBG);
    private static final boolean IS_US_CHANNEL = (SystemProperties.get("ro.config.hw_opta", "0").equals("567") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    private static final boolean IS_VERIZON = (SystemProperties.get("ro.config.hw_opta", "0").equals("389") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    private static final String KOREA_MCC = "450";
    private static final String LGU_PLMN = "45006";
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final int NETWORK_MODE_LTE_GSM_WCDMA = 9;
    private static final int NETWORK_MODE_UMTS_ONLY = 2;
    private static final int OPEN_SERVICE_PDP_CREATE_WAIT_MILLIS = 60000;
    private static final int PCO_CONTENT_LENGTH = 4;
    private static final String PCO_DATA = "pco_data";
    private static final String PROP_HW_ALLOW_PDP_AUTH = "ro.config.hw_allow_pdp_auth";
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G = 10000;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G = 45000;
    private static final long PS_CLEARCODE_APN_DELAY_MILLIS_2G_3G = (SystemProperties.getLong("ro.config.clearcode_2g3g_timer", 45) * 1000);
    private static final long PS_CLEARCODE_APN_DELAY_MILLIS_4G = (SystemProperties.getLong("ro.config.clearcode_4g_timer", 10) * 1000);
    private static final long PS_CLEARCODE_LIMIT_PDP_ACT_DELAY = (SystemProperties.getLong("ro.config.clearcode_limit_timer", 1) * 1000);
    private static final String PS_CLEARCODE_PLMN = SystemProperties.get("ro.config.clearcode_plmn", "");
    private static final int RADIO_TECH_ALL = 0;
    private static final int RADIO_TECH_GU = 255;
    private static final String SES_APN_KEY = "ses_apn";
    private static final String SES_REQUEST_APN_TYPE_KEY = "ses_request_network";
    private static final String SPLIT = ";";
    private static final int SWITCH_ON = 1;
    private static final String VERIZON_ICCID = "891480";
    private static int newRac = -1;
    private static int oldRac = -1;
    private final String TAG;
    private ArrayList<ApnOpkeyInfos> apnOpkeyInfosList = null;
    private boolean hadSentBoardCastToUI = false;
    private boolean isSinglePdpAllowed = false;
    private PendingIntent mAlarmIntent;
    private boolean mBroadcastPrePostPay = DBG;
    private GsmCellLocation mCellLoc = new GsmCellLocation();
    private PhoneStateListener mCellLocationPsl = new PhoneStateListener() {
        /* class com.android.internal.telephony.dataconnection.HwCustDcTrackerImpl.AnonymousClass1 */

        /* JADX WARNING: Removed duplicated region for block: B:48:0x01d8 A[Catch:{ Exception -> 0x01f6 }] */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x01d9 A[Catch:{ Exception -> 0x01f6 }] */
        public void onCellLocationChanged(CellLocation location) {
            boolean isDisconnected;
            if (HwCustDcTrackerImpl.this.mDcTracker.getMApnContextsHw() != null) {
                try {
                    HwCustDcTrackerImpl.this.log("CLEARCODE onCellLocationChanged");
                    if (!(location instanceof GsmCellLocation)) {
                        HwCustDcTrackerImpl.this.log("CLEARCODE location not instanceof GsmCellLocation");
                        return;
                    }
                    GsmCellLocation newCellLoc = (GsmCellLocation) location;
                    int unused = HwCustDcTrackerImpl.newRac = HwServiceStateTrackerEx.getInstance(HwCustDcTrackerImpl.this.mDcTracker.mPhone.getPhoneId()).getRac();
                    int radioTech = HwCustDcTrackerImpl.this.mDcTracker.mPhone.getServiceState().getRilDataRadioTechnology();
                    HwCustDcTrackerImpl.this.log("CLEARCODE newCellLoc = " + newCellLoc + ", oldCellLoc = " + HwCustDcTrackerImpl.this.mCellLoc + " oldRac = " + HwCustDcTrackerImpl.oldRac + " newRac = " + HwCustDcTrackerImpl.newRac + " radioTech = " + radioTech + " mOldRadioTech = " + HwCustDcTrackerImpl.this.mOldRadioTech);
                    boolean isHisiPlatform = HuaweiTelephonyConfigs.isHisiPlatform();
                    boolean z = HwCustDcTrackerImpl.DBG;
                    if (isHisiPlatform && HwCustDcTrackerImpl.this.mOldRadioTech != radioTech) {
                        HwCustDcTrackerImpl.this.mOldRadioTech = radioTech;
                        HwCustDcTrackerImpl.this.log("clearcode mOldRadioTech = " + HwCustDcTrackerImpl.this.mOldRadioTech);
                        int unused2 = HwCustDcTrackerImpl.oldRac = -1;
                        HwCustDcTrackerImpl.this.resetTryTimes();
                    }
                    if (-1 == HwCustDcTrackerImpl.newRac) {
                        HwCustDcTrackerImpl.this.log("CLEARCODE not really changed");
                    } else if (HwCustDcTrackerImpl.oldRac == HwCustDcTrackerImpl.newRac || radioTech != 3) {
                        HwCustDcTrackerImpl.this.log("CLEARCODE RAC not really changed");
                    } else if (-1 == HwCustDcTrackerImpl.oldRac) {
                        int unused3 = HwCustDcTrackerImpl.oldRac = HwCustDcTrackerImpl.newRac;
                        HwCustDcTrackerImpl.this.log("CLEARCODE oldRac = -1 return");
                    } else {
                        int unused4 = HwCustDcTrackerImpl.oldRac = HwCustDcTrackerImpl.newRac;
                        HwCustDcTrackerImpl.this.mCellLoc = newCellLoc;
                        ApnContext defaultApn = (ApnContext) HwCustDcTrackerImpl.this.mDcTracker.getMApnContextsHw().get("default");
                        if (!(!HwCustDcTrackerImpl.this.mDcTracker.getDataEnabledSettingsHw().isUserDataEnabled() || defaultApn == null || defaultApn.getState() == DctConstants.State.CONNECTED)) {
                            int curPrefMode = HwTelephonyManager.getDefault().getNetworkModeFromDB(HwCustDcTrackerImpl.this.mDcTracker.mPhone.getPhoneId());
                            HwCustDcTrackerImpl.this.log("CLEARCODE onCellLocationChanged radioTech = " + radioTech + " curPrefMode" + curPrefMode);
                            if (!(curPrefMode == 9 || curPrefMode == 2)) {
                                HwCustDcTrackerImpl.this.mDcTracker.mPhone.setPreferredNetworkType(9, (Message) null);
                                HwTelephonyManager.getDefault().saveNetworkModeToDB(HwCustDcTrackerImpl.this.mDcTracker.mPhone.getPhoneId(), 9);
                                HwServiceStateTrackerEx.getInstance(HwCustDcTrackerImpl.this.mDcTracker.mPhone.getPhoneId()).setRac(-1);
                                HwCustDcTrackerImpl.this.log("CLEARCODE onCellLocationChanged try switch 3G to 4G and set newrac to -1");
                            }
                            if (defaultApn.getState() != DctConstants.State.IDLE) {
                                if (defaultApn.getState() != DctConstants.State.FAILED) {
                                    isDisconnected = false;
                                    HwCustDcTrackerImpl.this.log("CLEARCODE onCellLocationChanged try setup data again");
                                    DcTracker dcTracker = HwCustDcTrackerImpl.this.mDcTracker;
                                    if (!isDisconnected) {
                                        z = false;
                                    }
                                    dcTracker.cleanUpConnectionHw(z, defaultApn);
                                    HwCustDcTrackerImpl.this.mDcTracker.setupDataOnConnectableApnsHw("cellLocationChanged");
                                    HwCustDcTrackerImpl.this.resetTryTimes();
                                }
                            }
                            isDisconnected = true;
                            HwCustDcTrackerImpl.this.log("CLEARCODE onCellLocationChanged try setup data again");
                            DcTracker dcTracker2 = HwCustDcTrackerImpl.this.mDcTracker;
                            if (!isDisconnected) {
                            }
                            dcTracker2.cleanUpConnectionHw(z, defaultApn);
                            HwCustDcTrackerImpl.this.mDcTracker.setupDataOnConnectableApnsHw("cellLocationChanged");
                            HwCustDcTrackerImpl.this.resetTryTimes();
                        }
                    }
                } catch (Exception e) {
                    HwCustDcTrackerImpl.this.loge("Exception in CellStateHandler.handleMessage");
                }
            }
        }
    };
    private BroadcastReceiver mClearCodeBroadCastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.dataconnection.HwCustDcTrackerImpl.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != 82734660) {
                    if (hashCode == 1397869417 && action.equals(HwCustDcTrackerImpl.INTENT_LIMIT_PDP_ACT_IND)) {
                        c = 1;
                    }
                } else if (action.equals(HwCustDcTrackerImpl.INTENT_SET_PREF_NETWORK_TYPE)) {
                    c = 0;
                }
                if (c == 0) {
                    HwCustDcTrackerImpl.this.onActionIntentSetNetworkType(intent);
                } else if (c == 1) {
                    HwCustDcTrackerImpl.this.onActionIntentLimitPDPActInd(intent);
                }
            }
        }
    };
    private PendingIntent mClearCodeLimitAlarmIntent = null;
    private int mCurFailCause;
    private DcTracker mDcTracker;
    private int mDelayTime = 3000;
    private FdnAsyncQueryHandler mFdnAsyncQuery;
    private FdnChangeObserver mFdnChangeObserver;
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.dataconnection.HwCustDcTrackerImpl.AnonymousClass3 */

        public void handleMessage(Message msg) {
            HwCustDcTrackerImpl hwCustDcTrackerImpl = HwCustDcTrackerImpl.this;
            hwCustDcTrackerImpl.log("handle message " + msg);
            int i = msg.what;
            if (i == 1 || i == 2) {
                AsyncResult ar = HwCustDcTrackerImpl.this;
                ar.log("fddn msg.what = " + msg.what);
                HwCustDcTrackerImpl.this.retryDataConnectionByFdn();
            } else if (i == 3) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    HwCustDcTrackerImpl hwCustDcTrackerImpl2 = HwCustDcTrackerImpl.this;
                    hwCustDcTrackerImpl2.log("PSCLEARCODE EVENT_LIMIT_PDP_ACT_IND exception " + ar2.exception);
                    return;
                }
                HwCustDcTrackerImpl.this.onLimitPDPActInd(ar2);
            }
        }
    };
    private boolean mIsBroadcastPrePostPay = DBG;
    private boolean mIsLimitPDPAct = false;
    private boolean mIsMPDNSupportByNetwork = SystemProperties.getBoolean("persist.telephony.mpdn", (boolean) DBG);
    private boolean mIsNeedRemovePreferApn = DBG;
    private int mOldRadioTech = 0;
    private String mPLMN = "";
    private AlertDialog mPSClearCodeDialog = null;
    private Context mPhoneContext;
    private boolean mRemovePreferredApn = DBG;
    private ContentResolver mResolver;
    private String mSesApn;
    private BroadcastReceiver mSimStateChangedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.dataconnection.HwCustDcTrackerImpl.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && HwCustDcTrackerImpl.this.isValidPhoneParams()) {
                if (intent.getIntExtra("phone", -1) != HwCustDcTrackerImpl.this.mDcTracker.mPhone.getPhoneId()) {
                    HwCustDcTrackerImpl.this.log("mSimStateChangedReceiver: not current phoneId, do nothing.");
                    return;
                }
                HwCustDcTrackerImpl.this.clearApnOpkeyIfNeed(intent.getStringExtra("ss"));
            }
        }
    };
    private int mTryIndex = 0;

    public HwCustDcTrackerImpl(DcTracker dcTracker) {
        super(dcTracker);
        this.mDcTracker = dcTracker;
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        sb.append(this.mDcTracker.getTransportType() == 1 ? "C" : "I");
        String tagSuffix = sb.toString();
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            tagSuffix = tagSuffix + "-" + this.mDcTracker.mPhone.getPhoneId();
        }
        this.TAG = "HwCustDcTrackerImpl" + tagSuffix;
        this.mPhoneContext = this.mDcTracker.mPhone.getContext();
        if (HW_SIM_ACTIVATION) {
            this.mResolver = this.mDcTracker.mPhone.getContext().getContentResolver();
            Settings.Global.putString(this.mResolver, PCO_DATA, DEFAULT_PCO_DATA);
            log("setting default pco values.");
        }
        registerSimStateChangedReceiver();
        registerClearCodeBroadcastReceiver();
    }

    /* access modifiers changed from: protected */
    public boolean canKeepApn(String requestedApnType, ApnSetting apnSetting, boolean isPreferredApn) {
        int canKeepApnResult = canKeepApnForSES(apnSetting, requestedApnType);
        if (canKeepApnResult == 0) {
            int radioTech = this.mDcTracker.mPhone.getServiceState().getRilDataRadioTechnology();
            ArrayList<ApnSetting> apnList = new ArrayList<>(this.mDcTracker.getAllApnList());
            if (!isPreferredApn || !hasBetterApnByBearer(apnSetting, apnList, requestedApnType, radioTech)) {
                return DBG;
            }
            return false;
        } else if (canKeepApnResult == 1) {
            return DBG;
        } else {
            return false;
        }
    }

    public boolean apnRoamingAdjust(DcTracker dcTracker, ApnSetting apnSetting, Phone phone) {
        phone.getServiceState().getRoaming();
        phone.getServiceState().getRilDataRadioTechnology();
        String operator = dcTracker.getHwDcTrackerEx().getOperatorNumeric();
        String roamingApnStr = Settings.System.getString(phone.getContext().getContentResolver(), "hw_customized_roaming_apn");
        String str = this.TAG;
        Rlog.d(str, "[" + phone.getPhoneId() + "]apnRoamingAdjust get hw_customized_roaming_apn: " + roamingApnStr);
        if (TextUtils.isEmpty(roamingApnStr)) {
            return DBG;
        }
        String[] roamingApnList = roamingApnStr.split(SPLIT);
        if (roamingApnList.length > 0) {
            for (int i = 0; i < roamingApnList.length; i++) {
                if (!TextUtils.isEmpty(roamingApnList[i])) {
                    String[] roamingApn = roamingApnList[i].split(":");
                    if (4 == roamingApn.length) {
                        String mccmnc = roamingApn[0];
                        String carrier = roamingApn[1];
                        String apn = roamingApn[2];
                        String str2 = roamingApn[3];
                        if (operator.equals(mccmnc) && apnSetting.getEntryName().equals(carrier)) {
                            apnSetting.getApnName().equals(apn);
                        }
                    } else {
                        String mccmnc2 = this.TAG;
                        Rlog.d(mccmnc2, "[" + phone.getPhoneId() + "]apnRoamingAdjust got unsuitable configuration " + roamingApnList[i]);
                    }
                }
            }
        }
        return DBG;
    }

    private String getLguPlmn() {
        log("getLguPlmn LGU_PLMN is 45006");
        return LGU_PLMN;
    }

    public void checkPLMN(String plmn) {
        if (CUST_ENABLE_OTA_BIP) {
            log("checkPLMN plmn = " + plmn);
            String oldPLMN = this.mPLMN;
            this.mPLMN = plmn;
            if (TextUtils.isEmpty(this.mPLMN) || (this.mPLMN.equals(oldPLMN) && this.hadSentBoardCastToUI)) {
                log("checkPLMN newPLMN:" + this.mPLMN + " oldPLMN:" + oldPLMN + " hadSentBoardCastToUI:" + this.hadSentBoardCastToUI);
                return;
            }
            log("checkPLMN mIsPseudoIMSI = " + getmIsPseudoImsi());
            if (!getmIsPseudoImsi()) {
                return;
            }
            if (this.mPLMN.equals(getLguPlmn())) {
                Intent intent = new Intent();
                intent.setAction("com.android.telephony.isopencard");
                this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
                log("sendbroadcast OTA_ISOPEN_CARD_ACTION");
                this.hadSentBoardCastToUI = DBG;
            } else if (!this.mPLMN.startsWith(KOREA_MCC)) {
                Intent intent2 = new Intent();
                intent2.setAction("com.android.telephony.roamingpseudo");
                this.mDcTracker.mPhone.getContext().sendBroadcast(intent2);
                log("sendbroadcast ROAMING_PSEUDO_ACTION");
                this.hadSentBoardCastToUI = DBG;
            }
        }
    }

    public void onOtaAttachFailed(ApnContext apnContext) {
        if (CUST_ENABLE_OTA_BIP) {
            log("onOtaAttachFailed sendbroadcast OTA_OPEN_SERVICE_ACTION, but Phone.OTARESULT.NETWORKFAIL");
            apnContext.setState(DctConstants.State.FAILED);
            apnContext.setDataConnection((DataConnection) null);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.open_service_result");
            intent.putExtra("result_code", 1);
            this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
        }
    }

    public boolean getmIsPseudoImsi() {
        boolean isPseudoIMSI = false;
        if (Integer.parseInt(SystemProperties.get("gsm.sim.card.type", "-1")) == 3) {
            isPseudoIMSI = DBG;
        }
        log("getmIsPSendoIms: mIsPseudoIMSI = " + isPseudoIMSI);
        return isPseudoIMSI;
    }

    public void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
        if (CUST_ENABLE_OTA_BIP && "bip0".equals(apnContext.getApnType()) && retValue) {
            log("trySetupData: open service and setupData return true");
            this.mDcTracker.sendMessageDelayed(this.mDcTracker.obtainMessage(271146, apnContext), 60000);
        }
    }

    public void openServiceStart(UiccController uiccController) {
        UiccCard uiccCard;
        if (CUST_ENABLE_OTA_BIP) {
            if (!(uiccController == null || !getmIsPseudoImsi() || (uiccCard = uiccController.getUiccCard(0)) == null)) {
                CatService catService = uiccCard.getCatService();
                if (catService != null) {
                    catService.onOtaCommand(0);
                    log("onDataSetupComplete: Open Service!");
                } else {
                    log("onDataSetupComplete:catService is null when Open Service!");
                }
            }
            if (this.mDcTracker.hasMessages(271146)) {
                this.mDcTracker.removeMessages(271146);
            }
        }
    }

    public String getPlmn() {
        if (CUST_ENABLE_OTA_BIP) {
            return this.mPLMN;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String string) {
        Rlog.d(this.TAG, string);
    }

    public boolean isDocomoApn(ApnSetting preferredApn) {
        if (!IS_DOCOMO || (preferredApn != null && "spmode.ne.jp".equals(preferredApn.getApnName()))) {
            return false;
        }
        return DBG;
    }

    public ApnSetting getDocomoApn(ApnSetting preferredApn) {
        log("getDocomoApn end   preferredApn:" + preferredApn);
        return preferredApn;
    }

    public boolean isCanHandleType(ApnSetting apnSetting, String requestedApnType) {
        if (!IS_DOCOMO || apnSetting == null || !"fota".equalsIgnoreCase(requestedApnType) || DOCOMO_FOTA_APN.equals(apnSetting.getApnName())) {
            return DBG;
        }
        return false;
    }

    public boolean isDocomoTetheringApn(ApnSetting apnSetting, String type) {
        if (!IS_DOCOMO || apnSetting == null || !"dcmtrg.ne.jp".equals(apnSetting.getApnName())) {
            return false;
        }
        if ("default".equalsIgnoreCase(type) || "supl".equalsIgnoreCase(type)) {
            return DBG;
        }
        return false;
    }

    public void savePcoData(PcoData pcoData) {
        if (HW_SIM_ACTIVATION) {
            if (pcoData.contents == null || pcoData.contents.length != 4) {
                log("pco content illegal,not handle.");
                return;
            }
            String[] pcoValues = getPcoValueFromSetting();
            log("pco setting values is : " + Arrays.toString(pcoValues));
            String imsPcoValueSetting = pcoValues[0];
            String internetPcoValueSetting = pcoValues[1];
            boolean isNeedRefresh = DBG;
            int i = pcoData.cid;
            if (i == 1) {
                String imsPcoValue = getPcoValueFromContent(pcoData.contents);
                if (imsPcoValueSetting.equals(imsPcoValue)) {
                    isNeedRefresh = false;
                } else {
                    imsPcoValueSetting = imsPcoValue;
                }
            } else if (i != 3) {
                isNeedRefresh = false;
                log("not handle : pco data cid is : " + pcoData.cid);
            } else {
                String internetPcoValue = getPcoValueFromContent(pcoData.contents);
                if (internetPcoValueSetting.equals(internetPcoValue)) {
                    isNeedRefresh = false;
                } else {
                    internetPcoValueSetting = internetPcoValue;
                }
            }
            if (isNeedRefresh) {
                String pcoValue = imsPcoValueSetting.concat(SPLIT).concat(internetPcoValueSetting);
                Settings.Global.putString(this.mResolver, PCO_DATA, pcoValue);
                log("refresh pco setting data is : " + pcoValue);
                return;
            }
            log("not need to refresh pco data.");
        }
    }

    private String getPcoValueFromContent(byte[] contents) {
        int pcoValue = DEFAULT_PCO_VALUE;
        if (contents != null && contents.length == 4) {
            try {
                pcoValue = contents[3];
            } catch (Exception e) {
                log("Exception: transform pco value error in getPcoValueFromContent");
            }
        }
        log("content pco value is : " + pcoValue);
        return String.valueOf(pcoValue);
    }

    private String[] getPcoValueFromSetting() {
        String[] pcoValues = Settings.Global.getString(this.mResolver, PCO_DATA).split(SPLIT);
        if (pcoValues != null && pcoValues.length == 2) {
            return pcoValues;
        }
        return new String[]{String.valueOf((int) DEFAULT_PCO_VALUE), String.valueOf((int) DEFAULT_PCO_VALUE)};
    }

    private boolean isVerizonSim(Context context) {
        if (context == null) {
            return false;
        }
        return ("" + ((TelephonyManager) context.getSystemService("phone")).getSimSerialNumber()).startsWith(VERIZON_ICCID);
    }

    public boolean isRoamDisallowedByCustomization(ApnContext apnContext) {
        if (apnContext == null) {
            return false;
        }
        if ((IS_VERIZON || IS_US_CHANNEL) && isVerizonSim(this.mDcTracker.mPhone.getContext()) && "mms".equals(apnContext.getApnType()) && this.mDcTracker.mPhone.getServiceState().getDataRoaming() && !this.mDcTracker.getDataRoamingEnabled()) {
            return DBG;
        }
        return false;
    }

    public void setDataOrRoamOn(int cause) {
        ServiceStateTracker sst = this.mDcTracker.mPhone.getServiceStateTracker();
        if (sst != null && sst.returnObject() != null && sst.returnObject().isDataOffbyRoamAndData()) {
            if (cause == 0) {
                if (SystemProperties.get("gsm.isuser.setdata", "true").equals("true")) {
                    this.mDcTracker.mPhone.mCi.setMobileDataEnable(1, (Message) null);
                } else {
                    SystemProperties.set("gsm.isuser.setdata", "true");
                }
            }
            if (cause == 1) {
                this.mDcTracker.mPhone.mCi.setRoamingDataEnable(1, (Message) null);
            }
        }
    }

    public boolean isDataDisableBySim2() {
        return DISABLE_SIM2_DATA;
    }

    public boolean isDataDisable(int slotId) {
        return HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId);
    }

    public boolean addSpecifiedApnSwitch() {
        if (!TextUtils.isEmpty(CUST_ADD_APEC_APN_BUILD)) {
            return DBG;
        }
        return false;
    }

    public boolean addSpecifiedApnToWaitingApns(DcTracker dcTracker, ApnSetting preferredApn, ApnSetting apn) {
        if (!CUST_ADD_APEC_APN_BUILD.contains(dcTracker.getHwDcTrackerEx().getOperatorNumeric()) || preferredApn == null || !preferredApn.getEntryName().equals(apn.getEntryName())) {
            return false;
        }
        return DBG;
    }

    public boolean isSmartMpEnable() {
        return EmcomManager.getInstance().isSmartMpEnable();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(this.TAG, s);
    }

    private void registerSimStateChangedReceiver() {
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        if (isValidPhoneParams()) {
            this.mDcTracker.mPhone.getContext().registerReceiver(this.mSimStateChangedReceiver, filter);
        }
    }

    private void registerClearCodeBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SET_PREF_NETWORK_TYPE);
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN) && this.mDcTracker.getTransportType() == 1) {
            this.mDcTracker.mPhone.mCi.registerForLimitPDPAct(this.mHandler, 3, (Object) null);
            filter.addAction(INTENT_LIMIT_PDP_ACT_IND);
        }
        this.mPhoneContext.registerReceiver(this.mClearCodeBroadCastReceiver, filter);
    }

    public void dispose() {
        if (!isValidPhoneParams()) {
            log("Invalid phone params when dispose, do nothing.");
            return;
        }
        if (this.mSimStateChangedReceiver != null) {
            this.mDcTracker.mPhone.getContext().unregisterReceiver(this.mSimStateChangedReceiver);
            this.mSimStateChangedReceiver = null;
        }
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN) && this.mDcTracker.getTransportType() == 1) {
            this.mDcTracker.mPhone.mCi.unregisterForLimitPDPAct(this.mHandler);
        }
    }

    /* access modifiers changed from: private */
    public static class ApnOpkeyInfos {
        String apn;
        String opkey;
        String user;

        private ApnOpkeyInfos() {
            this.apn = "";
            this.user = "";
            this.opkey = "";
        }
    }

    private FileInputStream getApnOpkeyCfgFileInputStream() {
        try {
            File confFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/%s", APNS_OPKEY_CONFIG_FILE), 0);
            if (confFile != null) {
                return new FileInputStream(confFile);
            }
            log("getApnOpkeyCfgFileInputStream: apns-opkey.xml not exists.");
            return null;
        } catch (NoClassDefFoundError e) {
            loge("getApnOpkeyCfgFileInputStream: NoClassDefFoundError occurs.");
            return null;
        } catch (IOException e2) {
            loge("getApnOpkeyCfgFileInputStream: IOException occurs.");
            return null;
        } catch (Exception e3) {
            loge("getApnOpkeyCfgFileInputStream: Exception occurs.");
            return null;
        }
    }

    private void loadMatchedApnOpkeyList(String numeric) {
        if (this.apnOpkeyInfosList != null) {
            log("loadMatchedApnOpkeyList:load ApnOpkeyList only once for same SIM card, return;");
        } else if (TextUtils.isEmpty(numeric)) {
            loge("loadMatchedApnOpkeyList: numeric is null, return.");
        } else {
            FileInputStream fileIn = getApnOpkeyCfgFileInputStream();
            if (fileIn == null) {
                loge("loadMatchedApnOpkeyList: fileIn is null, retrun.");
                return;
            }
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileIn, null);
                XmlUtils.beginDocument(parser, APN_OPKEY_LIST_DOCUMENT);
                while (true) {
                    int eventType = parser.next();
                    if (1 != eventType) {
                        if (2 == eventType && APN_OPKEY_NODE.equalsIgnoreCase(parser.getName())) {
                            String apnOpkeyInfoNumeric = parser.getAttributeValue(null, APN_OPKEY_INFO_NUMERIC);
                            if (numeric.equals(apnOpkeyInfoNumeric)) {
                                log("loadMatchedApnOpkeyList: parser " + parser.getName() + " begin, numeric=" + numeric + ", apnOpkeyInfoNumeric=" + apnOpkeyInfoNumeric);
                                this.apnOpkeyInfosList = new ArrayList<>();
                                while (true) {
                                    int eventType2 = parser.next();
                                    if (1 == eventType2) {
                                        break;
                                    }
                                    log("loadMatchedApnOpkeyList: parser " + parser.getName() + ", eventType=" + eventType2);
                                    if (2 != eventType2 || !APN_OPKEY_INFO.equalsIgnoreCase(parser.getName())) {
                                        if (3 == eventType2 && APN_OPKEY_NODE.equalsIgnoreCase(parser.getName())) {
                                            log("loadMatchedApnOpkeyList: parser " + parser.getName() + " end.");
                                            break;
                                        }
                                        log("loadMatchedApnOpkeyList: skip this line node.");
                                    } else {
                                        ApnOpkeyInfos apnOpkeyInfos = new ApnOpkeyInfos();
                                        apnOpkeyInfos.apn = parser.getAttributeValue(null, APN_OPKEY_INFO_APN);
                                        apnOpkeyInfos.user = parser.getAttributeValue(null, APN_OPKEY_INFO_USER);
                                        apnOpkeyInfos.opkey = parser.getAttributeValue(null, APN_OPKEY_INFO_OPKEY);
                                        this.apnOpkeyInfosList.add(apnOpkeyInfos);
                                    }
                                }
                                log("loadMatchedApnOpkeyList: parser config xml end.");
                            }
                        }
                    }
                }
                try {
                    fileIn.close();
                } catch (IOException e) {
                    loge("loadMatchedApnOpkeyList: fileInputStream  close error");
                }
            } catch (XmlPullParserException e2) {
                loge("loadMatchedApnOpkeyList: Some errors occur when parsering apns-opkey.xml");
                fileIn.close();
            } catch (IOException e3) {
                loge("loadMatchedApnOpkeyList: Can't find apns-opkey.ml.");
                fileIn.close();
            } catch (Throwable th) {
                try {
                    fileIn.close();
                } catch (IOException e4) {
                    loge("loadMatchedApnOpkeyList: fileInputStream  close error");
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidPhoneParams() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.mPhone == null || this.mDcTracker.mPhone.getContext() == null) {
            return false;
        }
        return DBG;
    }

    public String getOpKeyByActivedApn(String activedNumeric, String activedApn, String activedUser) {
        if (TextUtils.isEmpty(activedNumeric)) {
            loge("getOpKeyByActivedApn: numeric is null, return.");
            return null;
        }
        loadMatchedApnOpkeyList(activedNumeric);
        ArrayList<ApnOpkeyInfos> arrayList = this.apnOpkeyInfosList;
        if (arrayList != null && arrayList.size() > 0) {
            int size = this.apnOpkeyInfosList.size();
            for (int i = 0; i < size; i++) {
                ApnOpkeyInfos avInfo = this.apnOpkeyInfosList.get(i);
                if (avInfo.apn != null && avInfo.apn.equals(activedApn) && avInfo.user != null && avInfo.user.equals(activedUser)) {
                    log("getOpKeyByActivedApn: return matched apn opkey = " + avInfo.opkey);
                    return avInfo.opkey;
                }
            }
        }
        return null;
    }

    public void setApnOpkeyToSettingsDB(String activedApnOpkey) {
        if (isValidPhoneParams()) {
            ContentResolver contentResolver = this.mDcTracker.mPhone.getContext().getContentResolver();
            Settings.System.putString(contentResolver, "sim_apn_opkey" + this.mDcTracker.mPhone.getPhoneId(), activedApnOpkey);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearApnOpkeyIfNeed(String simState) {
        boolean needClearApnOpkey = false;
        if ("ABSENT".equals(simState)) {
            needClearApnOpkey = DBG;
        } else if ("IMSI".equals(simState)) {
            if (!isValidPhoneParams() || this.mDcTracker.mPhone.getIccRecords() == null) {
                needClearApnOpkey = DBG;
            } else {
                String currentImsi = this.mDcTracker.mPhone.getIccRecords().getIMSI();
                HwPhoneManager hwPhoneManager = HwTelephonyFactory.getHwPhoneManager();
                Context context = this.mDcTracker.mPhone.getContext();
                String oldImsi = hwPhoneManager.decryptInfo(context, APN_OPKEY_LAST_IMSI + this.mDcTracker.mPhone.getPhoneId());
                if (currentImsi == null) {
                    log("clearApnOpkeyIfNeed: currentImsi is null, maybe error occurs, clear apn opkey.");
                    needClearApnOpkey = DBG;
                } else if (!currentImsi.equals(oldImsi)) {
                    log("clearApnOpkeyIfNeed: diffrent SIM card, clear apn opkey.");
                    needClearApnOpkey = DBG;
                    HwPhoneManager hwPhoneManager2 = HwTelephonyFactory.getHwPhoneManager();
                    Context context2 = this.mDcTracker.mPhone.getContext();
                    hwPhoneManager2.encryptInfo(context2, currentImsi, APN_OPKEY_LAST_IMSI + this.mDcTracker.mPhone.getPhoneId());
                }
            }
        }
        if (needClearApnOpkey) {
            log("clearApnOpkeyIfNeed: INTENT_VALUE_ICC_" + simState + ", clear APN opkey.");
            setApnOpkeyToSettingsDB(null);
            this.apnOpkeyInfosList = null;
        }
    }

    public ArrayList<ApnSetting> sortApnListByBearer(ArrayList<ApnSetting> list, String requestedApnType, int radioTech) {
        if (list == null || list.size() <= 1) {
            return list;
        }
        if (!"xcap".equals(requestedApnType) && !"mms".equals(requestedApnType)) {
            return list;
        }
        list.sort(new Comparator<ApnSetting>() {
            /* class com.android.internal.telephony.dataconnection.HwCustDcTrackerImpl.AnonymousClass5 */

            public int compare(ApnSetting apn1, ApnSetting apn2) {
                if (apn1.getNetworkTypeBitmask() != 0 && apn2.getNetworkTypeBitmask() == 0) {
                    return -1;
                }
                if (apn1.getNetworkTypeBitmask() != 0 || apn2.getNetworkTypeBitmask() == 0) {
                    return 0;
                }
                return 1;
            }
        });
        return list;
    }

    public boolean hasBetterApnByBearer(ApnSetting curApnSetting, ArrayList<ApnSetting> list, String requestedApnType, int radioTech) {
        if (list == null || list.size() < 1 || curApnSetting == null) {
            return false;
        }
        if (!("mms".equals(requestedApnType) || "xcap".equals(requestedApnType))) {
            return false;
        }
        int apnListSize = list.size();
        for (int i = 0; i < apnListSize; i++) {
            ApnSetting apn = list.get(i);
            if (!apn.equals(curApnSetting) && apn.canHandleType(ApnSetting.getApnTypesBitmaskFromString(requestedApnType)) && curApnSetting.getNetworkTypeBitmask() == 0 && ServiceState.bitmaskHasTech(apn.getNetworkTypeBitmask(), radioTech) && apn.getNetworkTypeBitmask() != 0) {
                log("found other better apn with non-zero bearer bitmask");
                return DBG;
            }
        }
        return false;
    }

    public void tryRestartRadioWhenPrefApnChange(ApnSetting curPreferredApn, ApnSetting oldPreferredApn) {
        int i;
        String radioOffEmmCause = getRadioOffEmmCauseCust();
        if (!TextUtils.isEmpty(radioOffEmmCause)) {
            boolean isRejCause15 = DBG;
            boolean isPreApnChanged = curPreferredApn != null && !curPreferredApn.equals(oldPreferredApn);
            if (isPreApnChanged) {
                Phone phone = this.mDcTracker.mPhone;
                ServiceStateTracker sst = phone.getServiceStateTracker();
                TelephonyManager tm = TelephonyManager.getDefault();
                if (sst == null) {
                    return;
                }
                if (tm != null) {
                    int dataRadioTech = phone.getServiceState().getRilDataRadioTechnology();
                    int nwMode = Settings.Global.getInt(this.mDcTracker.mPhone.getContext().getContentResolver(), "preferred_network_mode", 9);
                    if (!(tm.getCallState() == 0)) {
                        return;
                    }
                    if (isNetworkModeEnableLte(nwMode)) {
                        int rejCause = sst.getRejCause();
                        if (rejCause != EMM_CAUSE_NO_SUITABLE_CELLS_IN_TA) {
                            isRejCause15 = false;
                        }
                        if (!(isRejCause15 ? false : ServiceState.isLte(dataRadioTech))) {
                            boolean isResetRadio = false;
                            String[] emmCauseStr = radioOffEmmCause.split(",");
                            int length = emmCauseStr.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                }
                                String emmCause = emmCauseStr[i2];
                                try {
                                    if (!TextUtils.isEmpty(emmCause) && rejCause == Integer.parseInt(emmCause)) {
                                        isResetRadio = DBG;
                                        break;
                                    }
                                    i = length;
                                    i2++;
                                    length = i;
                                } catch (NumberFormatException e) {
                                    i = length;
                                    Rlog.e(this.TAG, "radio_off_emm_cause_reject configuration parameter error:");
                                }
                            }
                            log("onApnChanged nwMode = " + nwMode + " callState = " + tm.getCallState() + " dataRadioTech = " + dataRadioTech + " isPreApnChanged = " + isPreApnChanged + " isResetRadio = " + isResetRadio + " rejCause = " + rejCause);
                            if (isResetRadio) {
                                log("onApnChanged begin-restartRadio ");
                                sst.clearRejCause();
                                this.mDcTracker.mPhone.mCi.setRadioPower(false, (Message) null);
                            }
                        }
                    }
                }
            }
        }
    }

    public void tryClearRejCause() {
        ServiceStateTracker sst = this.mDcTracker.mPhone.getServiceStateTracker();
        if (sst != null) {
            sst.clearRejCause();
        }
    }

    private boolean isNetworkModeEnableLte(int networkMode) {
        boolean isLteOnly = networkMode == 11;
        boolean isHasLteInMultipleNetworkMode = (networkMode == 9 || networkMode == 12 || networkMode == 67 || networkMode == 68 || networkMode == 65) || (networkMode == 8 || networkMode == 10 || networkMode == 64 || networkMode == 69);
        if (isLteOnly || isHasLteInMultipleNetworkMode) {
            return DBG;
        }
        return false;
    }

    public String getRadioOffEmmCauseCust() {
        try {
            String radioOffEmmCause = (String) HwCfgFilePolicy.getValue("radio_off_emm_cause_reject", this.mDcTracker.mPhone.getPhoneId(), String.class);
            if (!TextUtils.isEmpty(radioOffEmmCause)) {
                return radioOffEmmCause;
            }
            return null;
        } catch (NoClassDefFoundError e) {
            Rlog.e(this.TAG, "read radio_off_emm_cause_reject error : NoClassDefFoundError");
            return null;
        }
    }

    private int canKeepApnForSES(ApnSetting apnSetting, String apnType) {
        if (apnSetting == null) {
            return -1;
        }
        if (!isDataAllowedForSES(apnType)) {
            return 0;
        }
        if (TextUtils.isEmpty(this.mSesApn)) {
            this.mSesApn = Settings.Global.getString(this.mPhoneContext.getContentResolver(), SES_APN_KEY);
            if (TextUtils.isEmpty(this.mSesApn)) {
                Settings.Global.putString(this.mPhoneContext.getContentResolver(), SES_REQUEST_APN_TYPE_KEY, "");
                return 0;
            }
        }
        if (this.mSesApn.equals(apnSetting.getApnName())) {
            return 1;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean isDataAllowedForSES(String apnType) {
        if (TextUtils.isEmpty(apnType)) {
            return false;
        }
        PersistableBundle pb = null;
        Object configService = this.mPhoneContext.getSystemService("carrier_config");
        if (configService instanceof CarrierConfigManager) {
            pb = ((CarrierConfigManager) configService).getConfigForSubId(this.mDcTracker.mPhone.getSubId());
        }
        if (pb == null) {
            pb = CarrierConfigManager.getDefaultConfig();
        }
        if (!pb.getBoolean(ENABLE_SES_CHECK_KEY, false) || !apnType.equals(Settings.Global.getString(this.mPhoneContext.getContentResolver(), SES_REQUEST_APN_TYPE_KEY))) {
            return false;
        }
        return DBG;
    }

    public boolean isCustCorrectApnAuthOn() {
        return SystemProperties.getBoolean(PROP_HW_ALLOW_PDP_AUTH, false);
    }

    public void custCorrectApnAuth(List<ApnSetting> allApnSettings) {
        int size = allApnSettings.size();
        for (int i = 0; i < size; i++) {
            ApnSetting apn = allApnSettings.get(i);
            String username = apn.getUser();
            String password = apn.getPassword();
            boolean hasChanged = false;
            if (username == null) {
                username = "";
                hasChanged = DBG;
            }
            if (password == null) {
                password = "";
                hasChanged = DBG;
            }
            if (hasChanged) {
                allApnSettings.set(i, ApnSetting.makeApnSetting(apn.getId(), apn.getOperatorNumeric(), apn.getEntryName(), apn.getApnName(), apn.getProxyAddressAsString(), apn.getProxyPort(), apn.getMmsc(), apn.getMmsProxyAddressAsString(), apn.getMmsProxyPort(), username, password, apn.getAuthType(), apn.getApnTypeBitmask(), apn.getProtocol(), apn.getRoamingProtocol(), apn.isEnabled(), apn.getNetworkTypeBitmask(), apn.getProfileId(), apn.isPersistent(), apn.getMaxConns(), apn.getWaitTime(), apn.getMaxConnsTime(), apn.getMtu(), apn.getMvnoType(), apn.getMvnoMatchData(), apn.getApnSetId(), apn.getCarrierId(), apn.getSkip464Xlat()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentSetNetworkType(Intent intent) {
        int networkType = intent.getIntExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, 9);
        int curPrefMode = HwTelephonyManager.getDefault().getNetworkModeFromDB(this.mDcTracker.mPhone.getPhoneId());
        log("CLEARCODE switch network type : " + networkType + " curPrefMode = " + curPrefMode);
        if (!(networkType == curPrefMode || curPrefMode == 2)) {
            this.mDcTracker.mPhone.setPreferredNetworkType(networkType, (Message) null);
            log("CLEARCODE switch network type to 4G and set newRac to -1");
            HwTelephonyManager.getDefault().saveNetworkModeToDB(this.mDcTracker.mPhone.getPhoneId(), networkType);
            HwServiceStateTrackerEx.getInstance(this.mDcTracker.mPhone.getPhoneId()).setRac(-1);
        }
        ApnContext defaultApn = (ApnContext) this.mDcTracker.getMApnContextsHw().get("default");
        boolean z = false;
        boolean isDisconnected = defaultApn.getState() == DctConstants.State.IDLE || defaultApn.getState() == DctConstants.State.FAILED;
        log("CLEARCODE 2 hours of delay is over,try setup data");
        DcTracker dcTracker = this.mDcTracker;
        if (!isDisconnected) {
            z = true;
        }
        dcTracker.cleanUpConnectionHw(z, defaultApn);
        this.mDcTracker.setupDataOnConnectableApnsHw(CLEARCODE_2HOUR_DELAY_OVER);
    }

    public void onAllApnFirstActiveFailed() {
        if (IS_MULTI_SIM_ENABLED) {
            ApnReminder.getInstance(this.mPhoneContext, this.mDcTracker.mPhone.getPhoneId()).allApnActiveFailed();
            return;
        }
        ApnReminder.getInstance(this.mPhoneContext).allApnActiveFailed();
    }

    public void onAllApnPermActiveFailed() {
        if (this.mIsBroadcastPrePostPay && GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            log("tryToActionPrePostPay.");
            GlobalParamsAdaptor.tryToActionPrePostPay();
            this.mIsBroadcastPrePostPay = false;
        }
        ApnReminder.getInstance(this.mPhoneContext).getCust().handleAllApnPermActiveFailed(this.mPhoneContext);
    }

    public boolean needRemovedPreferredApn() {
        if (!this.mIsNeedRemovePreferApn || !GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            return false;
        }
        log("Remove preferred apn.");
        this.mIsNeedRemovePreferApn = false;
        return DBG;
    }

    public void setMPDNByNetwork(String plmnNetwork) {
        if (HwTelephonyPropertiesInner.ENABLE_NEW_PDP_SCHEME) {
            log("enable new pdp scheme, don't config multipdp by network!");
            return;
        }
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.mPhone == null) {
            log("setMPDNByNetwork, mPhone is null");
            return;
        }
        Context phoneContext = this.mPhoneContext;
        if (phoneContext == null) {
            log("setMPDNByNetwork, the Context of phone is null");
            return;
        }
        String plmnsConfig = Settings.System.getString(phoneContext.getContentResolver(), "mpdn_plmn_matched_by_network");
        if (TextUtils.isEmpty(plmnsConfig)) {
            log("setMPDNByNetwork, plmnConfig is Empty");
            return;
        }
        boolean isMPDNSupport = false;
        String[] plmns = plmnsConfig.split(",");
        int length = plmns.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String plmn = plmns[i];
            if (!TextUtils.isEmpty(plmn) && plmn.equals(plmnNetwork)) {
                isMPDNSupport = DBG;
                break;
            }
            i++;
        }
        setMPDN(isMPDNSupport);
        log("setMpdnByNewNetwork done, mIsMPDNSupportByNetwork:" + isMPDNSupport);
    }

    private void setMPDN(boolean isMPDNSupport) {
        if (isMPDNSupport == this.mIsMPDNSupportByNetwork) {
            log("setMPDN, MPDN is same,Don't need change");
        } else if (!isMPDNSupport || !isRatCdmaNotEhrpd()) {
            this.mIsMPDNSupportByNetwork = isMPDNSupport;
            log("setMPDN, mIsMPDNSupportByNetwork change to " + isMPDNSupport);
        } else {
            log("setMPDN, technology is not EHRPD and ServiceState is CDMA,Can't set MPDN");
        }
    }

    private boolean isRatCdmaNotEhrpd() {
        DcTracker dcTracker = this.mDcTracker;
        if (dcTracker == null || dcTracker.mPhone == null) {
            log("isRatCdmaNotEhrpd false, don't handle mPhone==null");
            return false;
        }
        ServiceState phoneServiceState = this.mDcTracker.mPhone.getServiceState();
        if (phoneServiceState == null) {
            log("isRatCdmaNotEhrpd false, don't handle serviceState==null");
            return false;
        }
        int radioTech = phoneServiceState.getRilDataRadioTechnology();
        if (!ServiceState.isCdma(radioTech) || radioTech == 13) {
            return false;
        }
        return DBG;
    }

    public boolean shouldDisableMultiPdps(boolean onlySingleDcAllowed) {
        if (HwTelephonyPropertiesInner.ENABLE_NEW_PDP_SCHEME) {
            return shouldDisableMultiPdpsNew(onlySingleDcAllowed);
        }
        return shouldDisableMultiPdpsOld(onlySingleDcAllowed);
    }

    private boolean shouldDisableMultiPdpsNew(boolean onlySingleDcAllowed) {
        if (onlySingleDcAllowed || isRatCdmaNotEhrpd()) {
            log("shouldDisableMultiPdpsNew, only single DC is allowed for cdma rat");
            return DBG;
        }
        int phoneId = this.mDcTracker.mPhone != null ? this.mDcTracker.mPhone.getPhoneId() : 0;
        boolean isMultiPdpDisabled = SystemProperties.getBoolean("gsm.singlepdp.hplmn.matched" + phoneId, false);
        if (!IS_PDN_REJ_CURE_ENABLE || isMultiPdpDisabled) {
            return isMultiPdpDisabled;
        }
        boolean isMultiPdpDisabled2 = this.isSinglePdpAllowed;
        log("shouldDisableMultiPdpsNew, isMultiPdpDisabled:" + isMultiPdpDisabled2);
        return isMultiPdpDisabled2;
    }

    public void setSinglePdpAllow(boolean isSinglePdpAllowed2) {
        this.isSinglePdpAllowed = isSinglePdpAllowed2;
    }

    private boolean shouldDisableMultiPdpsOld(boolean onlySingleDcAllowed) {
        if (onlySingleDcAllowed) {
            return onlySingleDcAllowed;
        }
        log("shouldDisableMultiPdpsOld, mIsMPDNSupportByNetwork:" + this.mIsMPDNSupportByNetwork);
        int phoneId = this.mDcTracker.mPhone != null ? this.mDcTracker.mPhone.getPhoneId() : 0;
        if (this.mIsMPDNSupportByNetwork) {
            return false;
        }
        if (!SystemProperties.getBoolean("gsm.multipdp.plmn.matched" + phoneId, false)) {
            return DBG;
        }
        return false;
    }

    public boolean isClearCodeEnabled() {
        return IS_CLEAR_CODE_ENABLED;
    }

    public void startListenCellLocationChange() {
        ((TelephonyManager) this.mPhoneContext.getSystemService("phone")).listen(this.mCellLocationPsl, 16);
    }

    public void stopListenCellLocationChange() {
        ((TelephonyManager) this.mPhoneContext.getSystemService("phone")).listen(this.mCellLocationPsl, 0);
    }

    public void operateClearCodeProcess(ApnContext apnContext, int cause, int delay) {
        this.mDelayTime = delay;
        if (!DataFailCause.isPermanentFailure(this.mPhoneContext, cause, this.mDcTracker.mPhone.getSubId())) {
            this.mTryIndex = 0;
            log("CLEARCODE not isPermanentFailure ");
            return;
        }
        log("CLEARCODE isPermanentFailure,perhaps APN is wrong");
        boolean isClearcodeDcFailCause = cause == 33 || cause == 29;
        if (!"default".equals(apnContext.getApnType()) || !isClearcodeDcFailCause) {
            this.mTryIndex = 0;
            apnContext.markApnPermanentFailed(apnContext.getApnSetting());
            return;
        }
        this.mTryIndex++;
        log("CLEARCODE mTryIndex increase,current mTryIndex = " + this.mTryIndex);
        if (this.mTryIndex >= 3) {
            if (isLteRadioTech()) {
                this.mDcTracker.mPhone.setPreferredNetworkType(3, (Message) null);
                HwTelephonyManager.getDefault().saveNetworkModeToDB(this.mDcTracker.mPhone.getPhoneId(), 3);
                HwServiceStateTrackerEx.getInstance(this.mDcTracker.mPhone.getPhoneId()).setRac(-1);
                log("CLEARCODE mTryIndex >= 3 and is LTE,switch 4G to 3G and set newrac to -1");
            } else {
                log("CLEARCODE mTryIndex >= 3 and is 3G,show clearcode dialog");
                if (this.mPSClearCodeDialog == null) {
                    this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
                    this.mPSClearCodeDialog.show();
                }
                set2HourDelay();
            }
            this.mTryIndex = 0;
            apnContext.markApnPermanentFailed(apnContext.getApnSetting());
        }
    }

    public void resetTryTimes() {
        PendingIntent pendingIntent;
        if (isClearCodeEnabled()) {
            this.mTryIndex = 0;
            AlarmManager alarmManager = (AlarmManager) this.mPhoneContext.getSystemService("alarm");
            if (alarmManager != null && (pendingIntent = this.mAlarmIntent) != null) {
                alarmManager.cancel(pendingIntent);
                log("CLEARCODE cancel Alarm resetTryTimes");
            }
        }
    }

    public void setCurFailCause(int cause) {
        if (isClearCodeEnabled() || isPSClearCodeRplmnMatched()) {
            this.mCurFailCause = cause;
        }
    }

    public void setFirstTimeEnableData() {
        log("=PREPOSTPAY=, Data Setup Successful.");
        if (this.mBroadcastPrePostPay) {
            this.mBroadcastPrePostPay = false;
        }
    }

    private boolean isLteRadioTech() {
        if (this.mDcTracker.mPhone.getServiceState().getRilDataRadioTechnology() == 14) {
            return DBG;
        }
        return false;
    }

    private AlertDialog createPSClearCodeDiag(int cause) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mPhoneContext, this.mPhoneContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        if (cause == 29) {
            builder.setMessage(33685827);
            log("CLEARCODE clear_code_29");
        } else if (cause != 33) {
            return null;
        } else {
            builder.setMessage(33685828);
            log("CLEARCODE clear_code_33");
        }
        builder.setIcon(17301543);
        builder.setCancelable(false);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            /* class com.android.internal.telephony.dataconnection.$$Lambda$HwCustDcTrackerImpl$h8GSRrfjV4yKvZsjgprp2gnSAvE */

            public final void onClick(DialogInterface dialogInterface, int i) {
                HwCustDcTrackerImpl.this.lambda$createPSClearCodeDiag$0$HwCustDcTrackerImpl(dialogInterface, i);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    public /* synthetic */ void lambda$createPSClearCodeDiag$0$HwCustDcTrackerImpl(DialogInterface dialog, int which) {
        this.mPSClearCodeDialog = null;
    }

    private void set2HourDelay() {
        int delayTime = SystemProperties.getInt("gsm.radio.debug.cause_delay", (int) DELAY_2_HOUR);
        log("CLEARCODE dataRadioTech is 3G and mTryIndex >= 3,so set2HourDelay delayTime =" + delayTime);
        Intent intent = new Intent(INTENT_SET_PREF_NETWORK_TYPE);
        intent.putExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, 9);
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mPhoneContext, 0, intent, 134217728);
        AlarmManager alarmManager = (AlarmManager) this.mPhoneContext.getSystemService("alarm");
        if (alarmManager != null) {
            alarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delayTime), this.mAlarmIntent);
        }
    }

    public int getDelayTime() {
        int i = this.mCurFailCause;
        if (i == 33 || i == 29) {
            if (isLteRadioTech()) {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G;
            } else {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G;
            }
        }
        return this.mDelayTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLimitPDPActInd(AsyncResult ar) {
        int[] responseArray;
        PendingIntent pendingIntent;
        if (ar != null && ar.exception == null && ar.result != null && (responseArray = (int[]) ar.result) != null && responseArray.length >= 2) {
            log("PSCLEARCODE onLimitPDPActInd result flag: " + responseArray[0] + " , cause: " + responseArray[1]);
            this.mIsLimitPDPAct = responseArray[0] == 1;
            int cause = responseArray[1];
            if (this.mIsLimitPDPAct && !isLteRadioTech()) {
                showPSClearCodeDialog(cause);
            }
            AlarmManager alarmManager = (AlarmManager) this.mPhoneContext.getSystemService("alarm");
            if (!(alarmManager == null || (pendingIntent = this.mClearCodeLimitAlarmIntent) == null)) {
                alarmManager.cancel(pendingIntent);
                this.mClearCodeLimitAlarmIntent = null;
            }
            Intent intent = new Intent(INTENT_LIMIT_PDP_ACT_IND);
            intent.putExtra(IS_LIMIT_PDP_ACT, this.mIsLimitPDPAct);
            intent.addFlags(268435456);
            this.mClearCodeLimitAlarmIntent = PendingIntent.getBroadcast(this.mPhoneContext, 0, intent, 134217728);
            if (alarmManager != null) {
                alarmManager.setExact(2, SystemClock.elapsedRealtime() + PS_CLEARCODE_LIMIT_PDP_ACT_DELAY, this.mClearCodeLimitAlarmIntent);
            }
            log("PSCLEARCODE startAlarmForLimitPDPActInd: delay=" + PS_CLEARCODE_LIMIT_PDP_ACT_DELAY + " flag=" + this.mIsLimitPDPAct);
        }
    }

    private void showPSClearCodeDialog(int cause) {
        if (this.mPSClearCodeDialog == null) {
            this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
            AlertDialog alertDialog = this.mPSClearCodeDialog;
            if (alertDialog != null) {
                alertDialog.show();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentLimitPDPActInd(Intent intent) {
        if (intent != null) {
            boolean isLimitPDPAct = intent.getBooleanExtra(IS_LIMIT_PDP_ACT, false);
            log("PSCLEARCODE onActionIntentLimitPDPActInd: flag = " + isLimitPDPAct);
            if (!isLimitPDPAct) {
                this.mDcTracker.getHwDcTrackerEx().updateApnContextState();
                this.mDcTracker.setupDataOnConnectableApnsHw("limitPDPActDisabled");
            }
        }
    }

    public long updatePSClearCodeApnContext(ApnContext apnContext, long delay) {
        long delayTime = delay;
        if (apnContext == null || apnContext.getApnSetting() == null) {
            return delayTime;
        }
        int dcFailCause = this.mCurFailCause;
        if (2304 == dcFailCause) {
            log("PSCLEARCODE retry APN. new delay = -1");
            return -1;
        }
        if (33 == dcFailCause || 29 == dcFailCause) {
            if (isLteRadioTech()) {
                delayTime = PS_CLEARCODE_APN_DELAY_MILLIS_4G;
            } else {
                delayTime = PS_CLEARCODE_APN_DELAY_MILLIS_2G_3G;
            }
            apnContext.getApnSetting().setPermanentFailed(false);
        }
        log("PSCLEARCODE retry APN. new delay = " + delayTime);
        return delayTime;
    }

    public boolean isLimitPDPAct() {
        if (!this.mIsLimitPDPAct || !isPSClearCodeRplmnMatched()) {
            return false;
        }
        return DBG;
    }

    public boolean isPSClearCodeRplmnMatched() {
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || this.mDcTracker.mPhone == null || this.mDcTracker.mPhone.getServiceState() == null) {
            return false;
        }
        String operator = this.mDcTracker.mPhone.getServiceState().getOperatorNumeric();
        if (TextUtils.isEmpty(PS_CLEARCODE_PLMN) || TextUtils.isEmpty(operator)) {
            return false;
        }
        return PS_CLEARCODE_PLMN.contains(operator);
    }

    public void registerForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.registerForFdnRecordsLoaded(this.mHandler, 2, (Object) null);
        }
    }

    public void unregisterForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.unregisterForFdnRecordsLoaded(this.mHandler);
        }
    }

    public void registerForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("registerForFdn");
            UiccController.getInstance().registerForFdnStatusChange(this.mHandler, 1, (Object) null);
            this.mFdnChangeObserver = new FdnChangeObserver();
            ContentResolver cr = this.mDcTracker.mPhone.getContext().getContentResolver();
            cr.registerContentObserver(FDN_URL, DBG, this.mFdnChangeObserver);
            this.mFdnAsyncQuery = new FdnAsyncQueryHandler(cr);
        }
    }

    public void unregisterForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("unregisterForFdn");
            UiccController.getInstance().unregisterForFdnStatusChange(this.mHandler);
            if (this.mFdnChangeObserver != null) {
                this.mPhoneContext.getContentResolver().unregisterContentObserver(this.mFdnChangeObserver);
            }
        }
    }

    public boolean isPsAllowedByFdn() {
        int slotId = this.mDcTracker.mPhone.getPhoneId();
        String isFdnActivated1 = SystemProperties.get(HwCustTelephonyProperties.PROPERTY_FDN_ACTIVATED_SUB1, "false");
        String isFdnActivated2 = SystemProperties.get(HwCustTelephonyProperties.PROPERTY_FDN_ACTIVATED_SUB2, "false");
        String isPSAllowedByFdn1 = SystemProperties.get(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, "false");
        String isPSAllowedByFdn2 = SystemProperties.get(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB2, "false");
        log("fddn isPSAllowedByFdn ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2 + " ,isPSAllowedByFdn1:" + isPSAllowedByFdn1 + " ,isPSAllowedByFdn2:" + isPSAllowedByFdn2);
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            if (slotId == 0 && "true".equals(isFdnActivated1) && "false".equals(isPSAllowedByFdn1)) {
                return false;
            }
            if (slotId != 1 || !"true".equals(isFdnActivated2) || !"false".equals(isPSAllowedByFdn2)) {
                return DBG;
            }
            return false;
        }
        return DBG;
    }

    public void retryDataConnectionByFdn() {
        if (this.mDcTracker.mPhone.getSubId() != SubscriptionController.getInstance().getCurrentDds()) {
            log("fddn retryDataConnectionByFdn, not dds sub, do nothing.");
        } else if (isPsAllowedByFdn()) {
            log("fddn retryDataConnectionByFdn, FDN status change and PS is enable, try setup data.");
            this.mDcTracker.setupDataOnConnectableApnsHw("psRestrictDisabled");
        } else {
            log("fddn retryDataConnectionByFdn, PS restricted by FDN, cleaup all connections.");
            this.mDcTracker.cleanUpAllConnectionsHw("psRestrictEnabled");
        }
    }

    private class FdnChangeObserver extends ContentObserver {
        FdnChangeObserver() {
            super(HwCustDcTrackerImpl.this.mDcTracker);
        }

        public void onChange(boolean selfChange) {
            HwCustDcTrackerImpl hwCustDcTrackerImpl = HwCustDcTrackerImpl.this;
            hwCustDcTrackerImpl.log("fddn FdnChangeObserver onChange, selfChange:" + selfChange);
            HwCustDcTrackerImpl.this.asyncQueryContact();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void asyncQueryContact() {
        long slotId = (long) this.mDcTracker.mPhone.getPhoneId();
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mFdnAsyncQuery.startQuery(0, null, ContentUris.withAppendedId(FDN_URL, slotId), new String[]{"number"}, null, null, null);
        }
    }

    /* access modifiers changed from: private */
    public class FdnAsyncQueryHandler extends AsyncQueryHandler {
        FdnAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            int slotId = HwCustDcTrackerImpl.this.mDcTracker.mPhone.getPhoneId();
            boolean isFdnActivated1 = SystemProperties.getBoolean(HwCustTelephonyProperties.PROPERTY_FDN_ACTIVATED_SUB1, false);
            boolean isFdnActivated2 = SystemProperties.getBoolean(HwCustTelephonyProperties.PROPERTY_FDN_ACTIVATED_SUB2, false);
            HwCustDcTrackerImpl hwCustDcTrackerImpl = HwCustDcTrackerImpl.this;
            hwCustDcTrackerImpl.log("fddn onQueryComplete slotId:" + slotId + " ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2);
            if ((slotId == 0 && isFdnActivated1) || (slotId == 1 && isFdnActivated2)) {
                HwCustDcTrackerImpl.this.retryDataConnectionByFdn();
            }
        }
    }

    public boolean isNeedDataRoamingExpend() {
        if (this.mDcTracker.mPhone == null || this.mDcTracker.mPhone.getIccRecords() == null) {
            log("mPhone or mIccRecords is null");
            return false;
        }
        boolean dataRoamState = false;
        boolean hasHwCfgConfig = false;
        try {
            Boolean dataRoam = (Boolean) HwCfgFilePolicy.getValue("hw_data_roam_option", SubscriptionManager.getSlotIndex(this.mDcTracker.mPhone.getSubId()), Boolean.class);
            if (dataRoam != null) {
                dataRoamState = dataRoam.booleanValue();
                hasHwCfgConfig = DBG;
            }
            if (dataRoamState) {
                return DBG;
            }
            if (hasHwCfgConfig && !dataRoamState) {
                return false;
            }
            String plmnsConfig = Settings.System.getString(((DcTracker) this.mDcTracker).mPhone.getContext().getContentResolver(), "hw_data_roam_option");
            if (TextUtils.isEmpty(plmnsConfig)) {
                log("plmnConfig is Empty");
                return false;
            } else if ("ALL".equals(plmnsConfig)) {
                return DBG;
            } else {
                String mccmnc = this.mDcTracker.mPhone.getIccRecords().getOperatorNumeric();
                String[] plmns = plmnsConfig.split(",");
                for (String plmn : plmns) {
                    if (!TextUtils.isEmpty(plmn) && plmn.equals(mccmnc)) {
                        return DBG;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            loge("read data_roam_option failed");
        }
    }

    public boolean setDataRoamingScope(int scope) {
        log("dram setDataRoamingScope scope " + scope);
        if (scope < 0 || scope > 2) {
            return false;
        }
        if (getDataRoamingScope() == scope) {
            return DBG;
        }
        Settings.Global.putInt(this.mDcTracker.mPhone.getContext().getContentResolver(), this.mDcTracker.getHwDcTrackerEx().getDataRoamingSettingItem("data_roaming"), scope);
        if (this.mDcTracker.mPhone.getServiceState() == null || !this.mDcTracker.mPhone.getServiceState().getRoaming()) {
            return DBG;
        }
        log("dram setDataRoamingScope send EVENT_ROAMING_ON");
        DcTracker dcTracker = this.mDcTracker;
        dcTracker.sendMessage(dcTracker.obtainMessage(270347));
        return DBG;
    }

    public int getDataRoamingScope() {
        try {
            return Settings.Global.getInt(this.mDcTracker.mPhone.getContext().getContentResolver(), this.mDcTracker.getHwDcTrackerEx().getDataRoamingSettingItem("data_roaming"));
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public boolean getDataRoamingEnabledWithNational() {
        boolean result = DBG;
        int dataRoamingScope = getDataRoamingScope();
        if (dataRoamingScope == 0 || (1 == dataRoamingScope && true == isInternationalRoaming())) {
            result = false;
        }
        log("dram getDataRoamingEnabledWithNational result " + result + " dataRoamingScope " + dataRoamingScope);
        return result;
    }

    public boolean isInternationalRoaming() {
        if (this.mDcTracker.mPhone == null || this.mDcTracker.mPhone.getIccRecords() == null) {
            log("mPhone or mIccRecords is null");
            return false;
        } else if (this.mDcTracker.mPhone.getServiceState() == null) {
            log("dram isInternationalRoaming ServiceState is not start up");
            return false;
        } else if (!this.mDcTracker.mPhone.getServiceState().getRoaming()) {
            log("dram isInternationalRoaming Current service state is not roaming, bail ");
            return false;
        } else {
            String simNumeric = this.mDcTracker.mPhone.getIccRecords().getOperatorNumeric();
            String operatorNumeric = this.mDcTracker.mPhone.getServiceState().getOperatorNumeric();
            if (TextUtils.isEmpty(simNumeric) || TextUtils.isEmpty(operatorNumeric)) {
                log("dram isInternationalRoaming SIMNumeric or OperatorNumeric is not got!");
                return false;
            }
            log("dram isInternationalRoaming simNumeric " + simNumeric + " operatorNumeric " + operatorNumeric);
            if (simNumeric.length() <= 3 || operatorNumeric.length() <= 3 || simNumeric.substring(0, 3).equals(operatorNumeric.substring(0, 3))) {
                return false;
            }
            return DBG;
        }
    }

    public ApnSetting getCustPreferredApn(List<ApnSetting> apnSettings) {
        String str = CUST_PREFERRED_APN;
        if (str == null || "".equals(str)) {
            return null;
        }
        if (apnSettings == null || apnSettings.isEmpty()) {
            log("getCustPreferredApn mAllApnSettings == null");
            return null;
        }
        int size = apnSettings.size();
        for (int i = 0; i < size; i++) {
            ApnSetting p = apnSettings.get(i);
            if (CUST_PREFERRED_APN.equals(p.getApnName())) {
                log("getCustPreferredApn: X found apnSetting" + p);
                return p;
            }
        }
        log("getCustPreferredApn: not found apn: " + CUST_PREFERRED_APN);
        return null;
    }

    public boolean processAttDataRoamingOff() {
        boolean domesticDataEnabled = false;
        if (!IS_ATT) {
            return false;
        }
        if (Settings.Global.getInt(this.mPhoneContext.getContentResolver(), "ATT_DOMESTIC_DATA", 0) != 0) {
            domesticDataEnabled = true;
        }
        log("processAttRoamingOff domesticDataEnabled = " + domesticDataEnabled);
        this.mDcTracker.getDataEnabledSettingsHw().setUserDataEnabled(domesticDataEnabled);
        if (domesticDataEnabled) {
            this.mDcTracker.notifyOffApnsOfAvailabilityHw();
            this.mDcTracker.setupDataOnConnectableApnsHw("roamingOff");
        } else {
            this.mDcTracker.cleanUpAllConnectionsHw("roamingOff");
            this.mDcTracker.notifyOffApnsOfAvailabilityHw();
        }
        return DBG;
    }

    public boolean processAttDataRoamingOn() {
        if (!IS_ATT) {
            return false;
        }
        if (!this.mDcTracker.mPhone.getServiceState().getDataRoaming()) {
            return DBG;
        }
        boolean dataRoamingEnabled = this.mDcTracker.getDataRoamingEnabled();
        this.mDcTracker.getDataEnabledSettingsHw().setUserDataEnabled(dataRoamingEnabled);
        if (dataRoamingEnabled) {
            log("onRoamingOn: setup data on in internal roaming");
            this.mDcTracker.setupDataOnConnectableApnsHw("roamingOn");
            this.mDcTracker.notifyDataConnectionHw();
        } else {
            log("onRoamingOn: Tear down data connection on internal roaming.");
            this.mDcTracker.cleanUpAllConnectionsHw("roamingOn");
            this.mDcTracker.notifyOffApnsOfAvailabilityHw();
        }
        return DBG;
    }

    public boolean clearAndResumeNetInfoForWifiLteCoexist(int apnId, int enabled, ApnContext apnContext) {
        if (!ENABLE_WIFI_LTE_CE) {
            return DBG;
        }
        String apnType = apnContext.getApnType();
        if (!apnType.equals("default") && !"hipri".equals(apnType)) {
            return DBG;
        }
        log("enableApnType but already actived");
        if (enabled == 1) {
            if (apnContext.getState() != DctConstants.State.CONNECTED) {
                return DBG;
            }
            log("enableApnType: return APN_ALREADY_ACTIVE");
            apnContext.setEnabled((boolean) DBG);
            this.mDcTracker.startNetStatPollHw();
            this.mDcTracker.restartDataStallAlarmHw();
            this.mDcTracker.notifyDataConnectionHw();
            return DBG;
        } else if (apnContext.isDisconnected() || !this.mDcTracker.getHwDcTrackerEx().isWifiConnected()) {
            return DBG;
        } else {
            log("clearAndResumeNetInfiForWifiLteCoexist:disableApnType due to WIFI Connected");
            this.mDcTracker.stopNetStatPollHw();
            this.mDcTracker.stopDataStallAlarmHw();
            this.mDcTracker.notifyDataConnectionHw();
            apnContext.setEnabled(false);
            return false;
        }
    }

    public boolean getCustRetryConfig() {
        int slotId = this.mDcTracker.mPhone.getPhoneId();
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("cust_retry_config", slotId, Boolean.class);
        boolean valueFromProp = CUST_RETRY_CONFIG;
        log("getCustRetryConfig, slotId:" + slotId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public boolean getEsmFlagAdaptionEnabled() {
        int slotId = this.mDcTracker.mPhone.getPhoneId();
        Boolean esmFlagAdaptionEnabled = (Boolean) HwCfgFilePolicy.getValue("attach_apn_enabled", slotId, Boolean.class);
        log("getEsmFlagAdaptionEnabled, slotId:" + slotId + ", card:" + esmFlagAdaptionEnabled);
        return esmFlagAdaptionEnabled != null ? esmFlagAdaptionEnabled.booleanValue() : ESM_FLAG_ADAPTION_ENABLED;
    }

    public int getEsmFlagFromCard() {
        int slotId = this.mDcTracker.mPhone.getPhoneId();
        Integer esmFlagFromCard = (Integer) HwCfgFilePolicy.getValue("plmn_esm_flag", slotId, Integer.class);
        log("getEsmFlagFromCard, slotId:" + slotId + ", card:" + esmFlagFromCard);
        if (esmFlagFromCard != null) {
            return esmFlagFromCard.intValue();
        }
        return -1;
    }

    public boolean isApnTypeDisabled(String apnType) {
        if (TextUtils.isEmpty(apnType)) {
            return false;
        }
        for (String type : SystemProperties.get("ro.hwpp.disabled_apn_type", "").split(",")) {
            if (apnType.equals(type)) {
                return DBG;
            }
        }
        return false;
    }
}
