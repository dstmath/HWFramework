package com.android.server.intellicom.networkslice.css;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.intellicom.common.IntellicomUtils;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.android.server.intellicom.networkslice.model.OsAppId;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.PhoneStateListenerEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import huawei.android.net.slice.INetworkSliceStateListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HwNetworkSliceSettingsObserver {
    private static final int AIRPLANE_MODE_OFF = 0;
    private static final int AIRPLANE_MODE_ON = 1;
    private static final int APN_ENABLED = 1;
    private static final String APN_SUBID_PATH = "filtered/subId/";
    private static final boolean DBG = true;
    private static final Uri GLOBAL_AIRPLANE_MODE_URI = Settings.Global.getUriFor("airplane_mode_on");
    private static final Uri GLOBAL_MOBILE_DATA_URI = Settings.Global.getUriFor("mobile_data");
    private static final int MOBILE_DATA_DISABLED = 0;
    private static final int MOBILE_DATA_ENABLED = 1;
    private static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    private static final int QUERY_TOKEN = 0;
    private static final String SEPARATOR_FOR_NORMAL_DATA = ",";
    private static final String SETTINGS_SYSTEM_APPID = "5g_slice_appId";
    private static final String SETTINGS_SYSTEM_CCT = "5g_slice_cct";
    private static final String SETTINGS_SYSTEM_DNN = "5g_slice_dnn";
    private static final String SETTINGS_SYSTEM_FQDN = "5g_slice_fqdn";
    private static final String SETTINGS_SYSTEM_SWITCH_DUAL_CARD_SLOT = "switch_dual_card_slots";
    private static final String SETTING_SYSTEM_VPN_ON = "wifipro_network_vpn_state";
    private static final Uri SYSTEM_APPID_URI = Settings.System.getUriFor(SETTINGS_SYSTEM_APPID);
    private static final Uri SYSTEM_CCT_URI = Settings.System.getUriFor(SETTINGS_SYSTEM_CCT);
    private static final Uri SYSTEM_DNN_URI = Settings.System.getUriFor(SETTINGS_SYSTEM_DNN);
    private static final Uri SYSTEM_FQDN_URI = Settings.System.getUriFor(SETTINGS_SYSTEM_FQDN);
    private static final Uri SYSTEM_SWITCH_CARD_URI = Settings.System.getUriFor(SETTINGS_SYSTEM_SWITCH_DUAL_CARD_SLOT);
    private static final Uri SYSTEM_VPN_URI = Settings.System.getUriFor("wifipro_network_vpn_state");
    private static final String TAG = "HwNetworkSliceSettingsObserver";
    private static final String URSP_MANUAL_MATCH_CONFIG = "ursp_manual_match_config";
    private static final int VPN_OFF = 0;
    private static final int VPN_ON = 1;
    private List<ApnObject> mApnObjects;
    private ContentObserver mApnObserver;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private boolean mIsAirplaneModeOn;
    private boolean mIsDefaultDataOnMainCard;
    private boolean mIsMobileDataEnabled;
    private boolean mIsNr;
    private boolean mIsNrSa;
    private boolean mIsVpnOn;
    private Looper mLooper;
    private int mMainSlotId;
    private PhoneStateListener mPhoneStateListener;
    private QueryHandler mQueryHandler;
    private ContentObserver mSettingsObserver;
    private final RemoteCallbackList<INetworkSliceStateListener> mStateListeners;
    private List<String> mWhiteListForCct;
    private final List<String> mWhiteListForCooperativeApp;
    private List<String> mWhiteListForDnn;
    private List<String> mWhiteListForFqdn;
    private List<OsAppId> mWhiteListForOsAppId;
    private ContentObserver mWhiteListObserver;

    private HwNetworkSliceSettingsObserver() {
        this.mWhiteListForOsAppId = new ArrayList();
        this.mWhiteListForDnn = new ArrayList();
        this.mWhiteListForFqdn = new ArrayList();
        this.mApnObjects = new ArrayList();
        this.mWhiteListForCct = new ArrayList();
        this.mWhiteListForCooperativeApp = new ArrayList();
        this.mStateListeners = new RemoteCallbackList<>();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    HwNetworkSliceSettingsObserver.this.log("intent or intent.getAction is null.");
                    return;
                }
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != -229777127) {
                    if (hashCode == -25388475 && action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
                        c = 1;
                    }
                } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED)) {
                    c = 0;
                }
                if (c == 0) {
                    int slotId = intent.getIntExtra("phone", -1000);
                    String simState = intent.getStringExtra("ss");
                    HwNetworkSliceSettingsObserver.this.log("Receive ACTION_SIM_STATE_CHANGED, slotId=" + slotId + ", simState=" + simState);
                    if ("LOADED".equals(simState) && slotId == HwNetworkSliceSettingsObserver.this.mMainSlotId) {
                        HwNetworkSliceSettingsObserver.this.registerApnContentObserver();
                    }
                } else if (c != 1) {
                    HwNetworkSliceSettingsObserver.this.log("BroadcastReceiver error: " + action);
                } else {
                    HwNetworkSliceSettingsObserver.this.log("Receive ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
                    HwNetworkSliceSettingsObserver.this.handleDefaultDataSubscriptionChanged(intent);
                }
            }
        };
    }

    public void initWhitelist(Context context, Looper looper) {
        if (Stream.of(context, looper).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            log("context or looper is null, fail to initWhitelist HwNetworkSliceSettingsObserver");
            return;
        }
        this.mContext = context;
        this.mLooper = looper;
        this.mWhiteListObserver = new ContentObserver(new Handler(this.mLooper)) {
            /* class com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange, Uri uri) {
                if (HwNetworkSliceSettingsObserver.SYSTEM_APPID_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.log("mAppIdObserver_onChange");
                    HwNetworkSliceSettingsObserver.this.readAppIdWhiteList();
                } else if (HwNetworkSliceSettingsObserver.SYSTEM_DNN_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.log("mDnnObserver_onChange");
                    HwNetworkSliceSettingsObserver.this.readDnnWhiteList();
                } else if (HwNetworkSliceSettingsObserver.SYSTEM_FQDN_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.log("mFqdnObserver_onChange");
                    HwNetworkSliceSettingsObserver.this.readFqdnWhiteList();
                } else if (HwNetworkSliceSettingsObserver.SYSTEM_CCT_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.log("mCctObserver_onChange");
                    HwNetworkSliceSettingsObserver.this.readCctWhiteList();
                } else {
                    HwNetworkSliceSettingsObserver.this.log("no match onChange");
                }
            }
        };
        readAppIdWhiteList();
        readDnnWhiteList();
        readFqdnWhiteList();
        readCctWhiteList();
        ContentResolver cr = this.mContext.getContentResolver();
        cr.registerContentObserver(Settings.System.getUriFor(SETTINGS_SYSTEM_APPID), true, this.mWhiteListObserver);
        cr.registerContentObserver(Settings.System.getUriFor(SETTINGS_SYSTEM_DNN), true, this.mWhiteListObserver);
        cr.registerContentObserver(Settings.System.getUriFor(SETTINGS_SYSTEM_FQDN), true, this.mWhiteListObserver);
        cr.registerContentObserver(Settings.System.getUriFor(SETTINGS_SYSTEM_CCT), true, this.mWhiteListObserver);
        log("finish whitelist init.");
    }

    public void init(Context context, Looper looper) {
        if (Stream.of(context, looper).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            log("context or looper is null, fail to init HwNetworkSliceSettingsObserver");
            return;
        }
        this.mContext = context;
        this.mLooper = looper;
        initSettingsObserver(looper);
        registerBroadcast();
        initPhoneStateListener(context, looper);
        initCooperativeAppWhiteList(context);
    }

    public boolean registerListener(INetworkSliceStateListener networkSliceStateListener) {
        boolean result;
        if (networkSliceStateListener == null) {
            return false;
        }
        notifyNetworkSliceStateChanged(networkSliceStateListener);
        synchronized (this.mStateListeners) {
            result = this.mStateListeners.register(networkSliceStateListener);
        }
        log("registerListener:" + networkSliceStateListener + " result=" + result);
        return result;
    }

    public boolean unregisterListener(INetworkSliceStateListener networkSliceStateListener) {
        boolean result;
        if (networkSliceStateListener == null) {
            return false;
        }
        synchronized (this.mStateListeners) {
            result = this.mStateListeners.unregister(networkSliceStateListener);
        }
        log("unregisterListener:" + networkSliceStateListener + " result=" + result);
        return result;
    }

    public boolean isNeedToRequestSliceForFqdn(String fqdn) {
        if (!this.mWhiteListForFqdn.contains(fqdn)) {
            return false;
        }
        log("fqdn in whitelist");
        return true;
    }

    public boolean isNeedToRequestSliceForFqdnAuto(String fqdn, int uid) {
        if (isCooperativeApp(uid)) {
            return false;
        }
        return isNeedToRequestSliceForFqdn(fqdn);
    }

    public boolean isNeedToRequestSliceForDnn(String dnn) {
        if (!this.mWhiteListForDnn.contains(dnn)) {
            return false;
        }
        log("dnn in whitelist dnn=" + dnn);
        return true;
    }

    public boolean isNeedToRequestSliceForDnnAuto(String dnn, int uid) {
        if (isCooperativeApp(uid)) {
            return false;
        }
        return isNeedToRequestSliceForDnn(dnn);
    }

    public boolean isNeedToRequestSliceForAppIdAuto(String appId) {
        if (isCooperativeApp(appId)) {
            return false;
        }
        return isNeedToRequestSliceForAppId(appId);
    }

    public boolean isNeedToRequestSliceForAppId(String appId) {
        if (this.mWhiteListForOsAppId.contains(OsAppId.create(HwNetworkSliceManager.OS_ID + appId))) {
            return true;
        }
        return false;
    }

    public boolean isNeedToRequestSliceForCctAuto(String cct, int uid) {
        if (isCooperativeApp(uid)) {
            return false;
        }
        return isNeedToRequestSliceForCct(cct);
    }

    public boolean isNeedToRequestSliceForCct(String cct) {
        if (this.mWhiteListForCct.contains(cct)) {
            return true;
        }
        return false;
    }

    public static HwNetworkSliceSettingsObserver getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public boolean isAirplaneModeOn() {
        return this.mIsAirplaneModeOn;
    }

    public boolean isMobileDataEnabled() {
        return this.mIsMobileDataEnabled;
    }

    public int getMainSlotId() {
        return this.mMainSlotId;
    }

    public boolean isVpnOn() {
        return this.mIsVpnOn;
    }

    public List<ApnObject> getApnObjects() {
        return this.mApnObjects;
    }

    public boolean isNrSa() {
        return this.mIsNr && this.mIsNrSa;
    }

    public boolean isDefaultDataOnMainCard() {
        return this.mIsDefaultDataOnMainCard;
    }

    public void notifyNetworkSliceStateChanged() {
        int retCode = getSliceEnvironmentCode();
        synchronized (this.mStateListeners) {
            int listnenerNum = this.mStateListeners.beginBroadcast();
            for (int i = 0; i < listnenerNum; i++) {
                try {
                    log("notifyNetworkSliceStateChanged retCode = " + retCode);
                    this.mStateListeners.getBroadcastItem(i).onNetworkSliceStateChanged(retCode);
                } catch (RemoteException e) {
                    log("notifyNetworkSliceStateChanged RemoteException occurs");
                }
            }
            this.mStateListeners.finishBroadcast();
        }
    }

    public boolean isCooperativeApp(int uid) {
        Context context = this.mContext;
        if (Stream.of(context, context.getPackageManager()).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            return false;
        }
        return isCooperativeApp(this.mContext.getPackageManager().getNameForUid(uid));
    }

    public boolean isCooperativeApp(String packageName) {
        return this.mWhiteListForCooperativeApp.contains(packageName);
    }

    private void notifyNetworkSliceStateChanged(INetworkSliceStateListener networkSliceStateListener) {
        if (networkSliceStateListener != null) {
            try {
                int retCode = getSliceEnvironmentCode();
                log("notifyNetworkSliceStateChanged retCode = " + retCode);
                networkSliceStateListener.onNetworkSliceStateChanged(retCode);
            } catch (RemoteException e) {
                log("onNetworkSliceStateChanged RemoteException occurs.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerApnContentObserver() {
        Uri apnUri;
        if (Stream.of(this.mContext, this.mLooper).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            log("registerApnContentObserver context or looper is null");
            return;
        }
        queryApnName();
        ContentResolver cr = this.mContext.getContentResolver();
        ContentObserver contentObserver = this.mApnObserver;
        if (contentObserver != null) {
            cr.unregisterContentObserver(contentObserver);
        }
        int subId = IntellicomUtils.getSubId(this.mMainSlotId);
        if (subId == -1) {
            log("subId invalid, registerApnContentObserver fail");
            return;
        }
        String subscriptionId = Long.toString((long) subId);
        if (IntellicomUtils.isMultiSimEnabled()) {
            apnUri = Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, subscriptionId);
        } else {
            Uri uri = Telephony.Carriers.SIM_APN_URI;
            apnUri = Uri.withAppendedPath(uri, APN_SUBID_PATH + subscriptionId);
        }
        this.mApnObserver = new ContentObserver(new Handler(this.mLooper)) {
            /* class com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwNetworkSliceSettingsObserver.this.queryApnName();
            }
        };
        cr.registerContentObserver(apnUri, true, this.mApnObserver);
    }

    private int getSliceEnvironmentCode() {
        int code = 0;
        if (!isMobileDataEnabled()) {
            code = 0 | 2;
        }
        if (!isDefaultDataOnMainCard()) {
            code |= 4;
        }
        if (!isNrSa()) {
            code |= 1;
        }
        if (HwNetworkSliceManager.getInstance().isUpToToplimit()) {
            return code | 8;
        }
        return code;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readApnName(Cursor cursor) {
        if (cursor == null) {
            log("Cursor is null apn query failed.");
            return;
        }
        List<ApnObject> apnObjects = new ArrayList<>();
        while (cursor.moveToNext()) {
            try {
                if (cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == 1) {
                    apnObjects.add(new ApnObject(cursor.getString(cursor.getColumnIndexOrThrow("apn")), ApnSettingHelper.getApnTypesBitmaskFromString(cursor.getString(cursor.getColumnIndexOrThrow("type")))));
                }
            } catch (IllegalArgumentException e) {
                log("readApnNames occurs IllegalArgumentException");
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
        }
        cursor.close();
        this.mApnObjects = apnObjects;
        log("readApnName mApn=" + this.mApnObjects);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryApnName() {
        if (this.mQueryHandler == null) {
            this.mQueryHandler = new QueryHandler(this.mContext.getContentResolver());
            log("mQueryHandler is null, new an fresh object.");
        }
        String operator = IntellicomUtils.getOperator(this.mMainSlotId);
        log("queryApnName operator = " + operator + "mMainSlotId = " + this.mMainSlotId);
        if (!TextUtils.isEmpty(operator)) {
            String selection = "numeric = '" + operator + "'";
            String subscriptionId = Long.toString((long) IntellicomUtils.getSubId(this.mMainSlotId));
            if (IntellicomUtils.isMultiSimEnabled()) {
                this.mQueryHandler.startQuery(0, null, Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, subscriptionId), null, selection, null, "_id");
                return;
            }
            this.mQueryHandler.startQuery(0, null, Uri.withAppendedPath(Telephony.Carriers.SIM_APN_URI, APN_SUBID_PATH + subscriptionId), null, null, null, "_id");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readAppIdWhiteList() {
        String appIds = Settings.System.getString(this.mContext.getContentResolver(), SETTINGS_SYSTEM_APPID);
        if (appIds != null) {
            this.mWhiteListForOsAppId = parseWhiteListOsAppId(parseWhiteListData(appIds));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readVpnSwitch() {
        boolean z = false;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "wifipro_network_vpn_state", 0) == 1) {
            z = true;
        }
        this.mIsVpnOn = z;
        log("mIsVpnOn = " + this.mIsVpnOn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readMainSlotId() {
        this.mMainSlotId = Settings.System.getInt(this.mContext.getContentResolver(), SETTINGS_SYSTEM_SWITCH_DUAL_CARD_SLOT, -1);
        log("mMainSlotId = " + this.mMainSlotId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readAirplaneMode() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            z = true;
        }
        this.mIsAirplaneModeOn = z;
        log("mIsAirplaneModeOn = " + this.mIsAirplaneModeOn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readMobileData() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) == 1) {
            z = true;
        }
        this.mIsMobileDataEnabled = z;
        log("mIsMobileDataEnabled = " + this.mIsMobileDataEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readDnnWhiteList() {
        String dnns = Settings.System.getString(this.mContext.getContentResolver(), SETTINGS_SYSTEM_DNN);
        if (dnns != null) {
            this.mWhiteListForDnn = parseWhiteListData(dnns);
            log("mWhiteListForDnn = " + this.mWhiteListForDnn);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readCctWhiteList() {
        String ccts = Settings.System.getString(this.mContext.getContentResolver(), SETTINGS_SYSTEM_CCT);
        if (ccts != null) {
            this.mWhiteListForCct = parseWhiteListData(ccts);
            log("mWhiteListForCct = " + this.mWhiteListForCct);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readFqdnWhiteList() {
        String fqdns = Settings.System.getString(this.mContext.getContentResolver(), SETTINGS_SYSTEM_FQDN);
        if (fqdns != null) {
            this.mWhiteListForFqdn = parseWhiteListData(fqdns);
        }
    }

    private List<String> parseWhiteListData(String whiteListData) {
        List<String> tempList = new ArrayList<>();
        if (whiteListData == null) {
            return tempList;
        }
        Collections.addAll(tempList, whiteListData.split(","));
        return tempList;
    }

    private List<OsAppId> parseWhiteListOsAppId(List<String> osAppIds) {
        List<OsAppId> temp = new ArrayList<>();
        if (osAppIds == null || osAppIds.size() == 0) {
            return temp;
        }
        return (List) osAppIds.stream().map($$Lambda$HwNetworkSliceSettingsObserver$qzv9F90HVXuudhHKNMx6tK08oVo.INSTANCE).filter($$Lambda$HwNetworkSliceSettingsObserver$Lioft6RZaSiJKPaB73WTiJolKKg.INSTANCE).collect(Collectors.toList());
    }

    static /* synthetic */ boolean lambda$parseWhiteListOsAppId$1(OsAppId osAppId) {
        return osAppId != null;
    }

    private void initCooperativeAppWhiteList(Context context) {
        if (context == null) {
            log("initSignedApk context is null");
            return;
        }
        String cooperativePackageNames = Settings.System.getString(context.getContentResolver(), URSP_MANUAL_MATCH_CONFIG);
        if (TextUtils.isEmpty(cooperativePackageNames)) {
            log("initCooperativeAppWhiteList packageNames is empty");
            return;
        }
        String[] packageNames = cooperativePackageNames.split(",");
        if (packageNames == null || packageNames.length == 0) {
            log("initCooperativeAppWhiteList packageNames is empty");
        } else {
            this.mWhiteListForCooperativeApp.addAll((Collection) Arrays.stream(packageNames).collect(Collectors.toList()));
        }
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    private void initSettingsObserver(Looper looper) {
        if (looper == null) {
            log("initSettingsObserver looper is null");
            return;
        }
        this.mSettingsObserver = new ContentObserver(new Handler(looper)) {
            /* class com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri) {
                if (HwNetworkSliceSettingsObserver.GLOBAL_MOBILE_DATA_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.readMobileData();
                    if (HwNetworkSliceSettingsObserver.this.mIsMobileDataEnabled) {
                        HwNetworkSliceManager.getInstance().restoreSliceEnvironment();
                    }
                    HwNetworkSliceSettingsObserver.this.notifyNetworkSliceStateChanged();
                    HwNetworkSliceSettingsObserver hwNetworkSliceSettingsObserver = HwNetworkSliceSettingsObserver.this;
                    hwNetworkSliceSettingsObserver.log("mMobileDataObserver_onChange, MOBILE_DATA_ENABLE = " + HwNetworkSliceSettingsObserver.this.mIsMobileDataEnabled);
                } else if (HwNetworkSliceSettingsObserver.SYSTEM_SWITCH_CARD_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.readMainSlotId();
                    HwNetworkSliceSettingsObserver hwNetworkSliceSettingsObserver2 = HwNetworkSliceSettingsObserver.this;
                    hwNetworkSliceSettingsObserver2.log("mSwitchDualCardSlotsObserver_onChange, MainSlotId = " + HwNetworkSliceSettingsObserver.this.mMainSlotId);
                } else if (HwNetworkSliceSettingsObserver.GLOBAL_AIRPLANE_MODE_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.readAirplaneMode();
                    if (!HwNetworkSliceSettingsObserver.this.mIsAirplaneModeOn) {
                        HwNetworkSliceManager.getInstance().restoreSliceEnvironment();
                    }
                    HwNetworkSliceSettingsObserver hwNetworkSliceSettingsObserver3 = HwNetworkSliceSettingsObserver.this;
                    hwNetworkSliceSettingsObserver3.log("mAirplaneModeObserver_onChange, AIRPLANE_MODE_ON = " + HwNetworkSliceSettingsObserver.this.mIsAirplaneModeOn);
                } else if (HwNetworkSliceSettingsObserver.SYSTEM_VPN_URI.equals(uri)) {
                    HwNetworkSliceSettingsObserver.this.readVpnSwitch();
                    HwNetworkSliceSettingsObserver hwNetworkSliceSettingsObserver4 = HwNetworkSliceSettingsObserver.this;
                    hwNetworkSliceSettingsObserver4.log("mVpnObserver_onChange, VPN_ON = " + HwNetworkSliceSettingsObserver.this.mIsVpnOn);
                    if (!HwNetworkSliceSettingsObserver.this.mIsVpnOn) {
                        HwNetworkSliceManager.getInstance().restoreSliceEnvironment();
                    }
                } else {
                    HwNetworkSliceSettingsObserver.this.log("no match onChange");
                }
            }
        };
        readAirplaneMode();
        readMobileData();
        readMainSlotId();
        readVpnSwitch();
        ContentResolver cr = this.mContext.getContentResolver();
        cr.registerContentObserver(GLOBAL_MOBILE_DATA_URI, true, this.mSettingsObserver);
        cr.registerContentObserver(GLOBAL_AIRPLANE_MODE_URI, true, this.mSettingsObserver);
        cr.registerContentObserver(SYSTEM_SWITCH_CARD_URI, true, this.mSettingsObserver);
        cr.registerContentObserver(SYSTEM_VPN_URI, true, this.mSettingsObserver);
    }

    /* JADX WARN: Type inference failed for: r0v4, types: [com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver$5, android.telephony.PhoneStateListener] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void initPhoneStateListener(Context context, Looper looper) {
        if (Stream.of(context, looper).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            log("initPhoneStateListener context or looper is null");
            return;
        }
        this.mPhoneStateListener = new PhoneStateListenerEx(looper) {
            /* class com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver.AnonymousClass5 */

            public void onServiceStateChanged(ServiceState serviceState) {
                HwNetworkSliceSettingsObserver.this.log("onServiceStateChanged");
                HwNetworkSliceSettingsObserver.this.updateNrState(serviceState);
            }
        };
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm == null) {
            log("can not initialize HwNetworkSliceManager");
        } else {
            tm.listen(this.mPhoneStateListener, 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNrState(ServiceState state) {
        if (state == null) {
            log("updateNrState failed, invalid state");
            return;
        }
        boolean isNr = false;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            isNr = tm.getNetworkType() == 20;
        }
        boolean isNrSa = true ^ HwTelephonyManager.getDefault().isNsaState(getMainSlotId());
        log("updateNrState mIsNr=" + this.mIsNr + ", isNr=" + isNr + ", mIsNrSa=" + this.mIsNrSa + ", isNrSa=" + isNrSa + ", MainSlotId=" + getMainSlotId());
        if (this.mIsNr != isNr || this.mIsNrSa != isNrSa) {
            this.mIsNr = isNr;
            this.mIsNrSa = isNrSa;
            if (this.mIsNr && this.mIsNrSa) {
                HwNetworkSliceManager.getInstance().restoreSliceEnvironment();
            }
            notifyNetworkSliceStateChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDefaultDataSubscriptionChanged(Intent intent) {
        boolean z = false;
        if (!Stream.of(intent, this.mContext).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")) != null) {
                log("handleDefaultDataSubscriptionChanged MainSlotId=" + getMainSlotId() + ", DefaultDataPhoneId=" + SubscriptionManagerEx.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()));
                if (getMainSlotId() == SubscriptionManagerEx.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId())) {
                    z = true;
                }
                this.mIsDefaultDataOnMainCard = z;
            }
            if (this.mIsDefaultDataOnMainCard) {
                HwNetworkSliceManager.getInstance().restoreSliceEnvironment();
                HwNetworkSliceManager.getInstance().bindAllProccessToNetwork();
            } else {
                HwNetworkSliceManager.getInstance().unbindAllProccessToNetwork();
            }
            notifyNetworkSliceStateChanged();
        }
    }

    public static class ApnObject {
        String apnName;
        int apnTypesBitmask;

        ApnObject(String apnName2, int apnTypesBitmask2) {
            this.apnName = apnName2;
            this.apnTypesBitmask = apnTypesBitmask2;
        }

        public String getApnName() {
            return this.apnName;
        }

        public int getApnTypesBitmask() {
            return this.apnTypesBitmask;
        }

        public String toString() {
            return "ApnObject{apnName='" + this.apnName + "', apnTypesBitmask=" + this.apnTypesBitmask + '}';
        }
    }

    /* access modifiers changed from: private */
    public static class SingletonInstance {
        private static final HwNetworkSliceSettingsObserver INSTANCE = new HwNetworkSliceSettingsObserver();

        private SingletonInstance() {
        }
    }

    /* access modifiers changed from: private */
    public final class QueryHandler extends AsyncQueryHandler {
        QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        @Override // android.content.AsyncQueryHandler
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            HwNetworkSliceSettingsObserver.this.readApnName(cursor);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Log.i(TAG, msg);
    }

    private void logd(String msg) {
        Log.d(TAG, msg);
    }
}
