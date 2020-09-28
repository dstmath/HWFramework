package com.android.internal.telephony.uicc;

import android.app.ActivityManager;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UiccProfile extends IccCard implements IUiccProfileInner {
    protected static final boolean DBG = true;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public static final int EVENT_APP_READY = 3;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 14;
    private static final int EVENT_CARRIER_PRIVILEGES_LOADED = 13;
    private static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 101;
    private static final int EVENT_CLOSE_LOGICAL_CHANNEL_DONE = 9;
    private static final int EVENT_EID_READY = 6;
    private static final int EVENT_ICC_CHANGED = 1001;
    private static final int EVENT_ICC_LOCKED = 2;
    private static final int EVENT_ICC_RECORD_EVENTS = 7;
    private static final int EVENT_IMSI_READY = 100;
    private static final int EVENT_NETWORK_LOCKED = 5;
    private static final int EVENT_OPEN_LOGICAL_CHANNEL_DONE = 8;
    private static final int EVENT_RADIO_OFF_OR_UNAVAILABLE = 1;
    private static final int EVENT_RECORDS_LOADED = 4;
    private static final int EVENT_SIM_IO_DONE = 12;
    private static final int EVENT_TRANSMIT_APDU_BASIC_CHANNEL_DONE = 11;
    private static final int EVENT_TRANSMIT_APDU_LOGICAL_CHANNEL_DONE = 10;
    protected static final String LOG_TAG = "UiccProfile";
    private static final String OPERATOR_BRAND_OVERRIDE_PREFIX = "operator_branding_";
    private static final boolean VDBG = false;
    private RegistrantList mCarrierPrivilegeRegistrants;
    private UiccCarrierPrivilegeRules mCarrierPrivilegeRules;
    private CatService mCatService;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    private int mCdmaSubscriptionAppIndex;
    private CommandsInterface mCi;
    private Context mContext;
    private int mCurrentAppType;
    private boolean mDisposed;
    private IccCardConstants.State mExternalState;
    private int mGsmUmtsSubscriptionAppIndex;
    @VisibleForTesting
    public final Handler mHandler;
    private IHwUiccProfileEx mHwUiccProfileEx;
    private IccRecords mIccRecords;
    private int mImsSubscriptionAppIndex;
    private final Object mLock;
    private RegistrantList mNetworkLockedRegistrants;
    private RegistrantList mOperatorBrandOverrideRegistrants;
    private final int mPhoneId;
    private final ContentObserver mProvisionCompleteContentObserver;
    private final BroadcastReceiver mReceiver;
    private TelephonyManager mTelephonyManager;
    private UiccCardApplication mUiccApplication;
    private UiccCardApplication[] mUiccApplications = new UiccCardApplication[8];
    private final UiccCard mUiccCard;
    private IccCardStatus.PinState mUniversalPinState;

    public Handler getHandler() {
        return this.mHandler;
    }

    public UiccProfile(Context c, CommandsInterface ci, IccCardStatus ics, int phoneId, UiccCard uiccCard, Object lock) {
        boolean z = false;
        this.mDisposed = false;
        this.mCdmaSSM = null;
        this.mCarrierPrivilegeRegistrants = new RegistrantList();
        this.mOperatorBrandOverrideRegistrants = new RegistrantList();
        this.mNetworkLockedRegistrants = new RegistrantList();
        this.mCurrentAppType = 1;
        this.mUiccApplication = null;
        this.mIccRecords = null;
        this.mExternalState = IccCardConstants.State.UNKNOWN;
        this.mProvisionCompleteContentObserver = new ContentObserver(new Handler()) {
            /* class com.android.internal.telephony.uicc.UiccProfile.AnonymousClass1 */

            public void onChange(boolean selfChange) {
                UiccProfile.this.mContext.getContentResolver().unregisterContentObserver(this);
                for (String pkgName : UiccProfile.this.getUninstalledCarrierPackages()) {
                    InstallCarrierAppUtils.showNotification(UiccProfile.this.mContext, pkgName);
                    InstallCarrierAppUtils.registerPackageInstallReceiver(UiccProfile.this.mContext);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.uicc.UiccProfile.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && "android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                    UiccProfile.this.mHandler.sendMessage(UiccProfile.this.mHandler.obtainMessage(14));
                }
            }
        };
        this.mHandler = new Handler() {
            /* class com.android.internal.telephony.uicc.UiccProfile.AnonymousClass3 */

            public void handleMessage(Message msg) {
                if (!UiccProfile.this.mDisposed || msg.what == 8 || msg.what == 9 || msg.what == 10 || msg.what == 11 || msg.what == 12) {
                    UiccProfile uiccProfile = UiccProfile.this;
                    uiccProfile.loglocal("handleMessage: Received " + msg.what + " for phoneId " + UiccProfile.this.mPhoneId);
                    int i = msg.what;
                    if (i == 100) {
                        UiccProfile.broadcastIccStateChangedIntent(IccCardConstantsEx.INTENT_VALUE_ICC_IMSI, null, UiccProfile.this.mPhoneId);
                    } else if (i == 101) {
                        UiccProfile.this.updateActiveRecord();
                    } else if (i != 1001) {
                        switch (i) {
                            case 5:
                                UiccProfile.this.mNetworkLockedRegistrants.notifyRegistrants();
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 6:
                                UiccProfile.this.updateExternalState();
                                break;
                            case 7:
                                if (((UiccProfile) UiccProfile.this).mCurrentAppType == 1 && UiccProfile.this.mIccRecords != null && ((Integer) ((AsyncResult) msg.obj).result).intValue() == 2) {
                                    UiccProfile.this.mTelephonyManager.setSimOperatorNameForPhone(UiccProfile.this.mPhoneId, UiccProfile.this.mIccRecords.getServiceProviderName());
                                    break;
                                }
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                                AsyncResult ar = (AsyncResult) msg.obj;
                                if (ar.exception != null) {
                                    UiccProfile uiccProfile2 = UiccProfile.this;
                                    uiccProfile2.loglocal("handleMessage: Exception " + ar.exception);
                                    UiccProfile.log("handleMessage: Error in SIM access with exception" + ar.exception);
                                }
                                AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                                ((Message) ar.userObj).sendToTarget();
                                break;
                            case 13:
                                UiccProfile.this.onCarrierPrivilegesLoadedMessage();
                                UiccProfile.this.updateExternalState();
                                break;
                            case 14:
                                UiccProfile.this.handleCarrierNameOverride();
                                UiccProfile.this.handleSimCountryIsoOverride();
                                break;
                            default:
                                UiccProfile.loge("handleMessage: Unhandled message with number: " + msg.what);
                                UiccProfile.this.mHwUiccProfileEx.handleMessageExtend(msg);
                                break;
                        }
                    } else {
                        UiccProfile.this.handleIccChangedEvent(msg);
                    }
                    UiccProfile.this.mHwUiccProfileEx.handleCustMessage(msg);
                    return;
                }
                UiccProfile.loge("handleMessage: Received " + msg.what + " after dispose(); ignoring the message");
            }
        };
        log("Creating profile");
        this.mLock = lock;
        this.mUiccCard = uiccCard;
        this.mPhoneId = phoneId;
        this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(c, ci, this.mHandler, 101, null);
        this.mHwUiccProfileEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwUiccProfileEx(this);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            setCurrentAppType(phone.getPhoneType() == 1 ? true : z);
        }
        UiccCard uiccCard2 = this.mUiccCard;
        if (uiccCard2 instanceof EuiccCard) {
            ((EuiccCard) uiccCard2).registerForEidReady(this.mHandler, 6, null);
        }
        update(c, ci, ics);
        ci.registerForOffOrNotAvailable(this.mHandler, 1, null);
        resetProperties();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        c.registerReceiver(this.mReceiver, intentfilter);
        UiccController.getInstance().registerForIccChanged(this.mHandler, 1001, null);
    }

    public void dispose() {
        log("Disposing profile");
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard instanceof EuiccCard) {
            ((EuiccCard) uiccCard).unregisterForEidReady(this.mHandler);
        }
        synchronized (this.mLock) {
            unregisterAllAppEvents();
            unregisterCurrAppEvents();
            InstallCarrierAppUtils.hideAllNotifications(this.mContext);
            InstallCarrierAppUtils.unregisterPackageInstallReceiver(this.mContext);
            this.mCi.unregisterForOffOrNotAvailable(this.mHandler);
            this.mContext.unregisterReceiver(this.mReceiver);
            if (this.mCatService != null) {
                this.mCatService.dispose();
            }
            UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
            for (UiccCardApplication app : uiccCardApplicationArr) {
                if (app != null) {
                    app.dispose();
                }
            }
            this.mCatService = null;
            this.mUiccApplications = null;
            this.mCarrierPrivilegeRules = null;
            this.mContext.getContentResolver().unregisterContentObserver(this.mProvisionCompleteContentObserver);
            this.mDisposed = true;
            this.mCdmaSSM.dispose(this.mHandler);
        }
    }

    public void setVoiceRadioTech(int radioTech) {
        synchronized (this.mLock) {
            log("Setting radio tech " + ServiceState.rilRadioTechnologyToString(radioTech));
            setCurrentAppType(ServiceState.isGsm(radioTech));
            updateIccAvailability(false);
            updateActiveRecord();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateActiveRecord() {
        log("updateActiveRecord app type = " + this.mCurrentAppType + "mIccRecords = " + this.mIccRecords);
        if (this.mIccRecords != null) {
            int i = this.mCurrentAppType;
            if (i == 2) {
                if (this.mCdmaSSM.getCdmaSubscriptionSource() == 0) {
                    log("Setting Ruim Record as active");
                    this.mIccRecords.recordsRequired();
                }
            } else if (i == 1) {
                log("Setting SIM Record as active");
                this.mIccRecords.recordsRequired();
            }
        }
    }

    private void setCurrentAppType(boolean isGsm) {
        synchronized (this.mLock) {
            if (isGsm) {
                this.mCurrentAppType = 1;
            } else {
                this.mCurrentAppType = 2;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCarrierNameOverride() {
        Phone phone;
        SubscriptionController subCon = SubscriptionController.getInstance();
        int subId = subCon.getSubIdUsingPhoneId(this.mPhoneId);
        if (subId == -1) {
            loge("subId not valid for Phone " + this.mPhoneId);
            return;
        }
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader == null) {
            loge("Failed to load a Carrier Config");
            return;
        }
        PersistableBundle config = configLoader.getConfigForSubId(subId);
        boolean preferCcName = config.getBoolean("carrier_name_override_bool", false);
        String ccName = config.getString("carrier_name_string");
        String newCarrierName = null;
        String currSpn = getServiceProviderName();
        int nameSource = 1;
        if (preferCcName || (TextUtils.isEmpty(currSpn) && !TextUtils.isEmpty(ccName))) {
            newCarrierName = ccName;
            nameSource = 3;
        } else if (TextUtils.isEmpty(currSpn) && (phone = PhoneFactory.getPhone(this.mPhoneId)) != null) {
            newCarrierName = phone.getCarrierName();
        }
        if (!TextUtils.isEmpty(newCarrierName)) {
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId, newCarrierName);
            this.mOperatorBrandOverrideRegistrants.notifyRegistrants();
        }
        updateCarrierNameForSubscription(subCon, subId, nameSource);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimCountryIsoOverride() {
        SubscriptionController subCon = SubscriptionController.getInstance();
        int subId = subCon.getSubIdUsingPhoneId(this.mPhoneId);
        if (subId == -1) {
            loge("subId not valid for Phone " + this.mPhoneId);
            return;
        }
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader == null) {
            loge("Failed to load a Carrier Config");
            return;
        }
        String iso = configLoader.getConfigForSubId(subId).getString("sim_country_iso_override_string");
        if (!TextUtils.isEmpty(iso) && !iso.equals(this.mTelephonyManager.getSimCountryIsoForPhone(this.mPhoneId))) {
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId, iso);
            subCon.setCountryIso(iso, subId);
        }
    }

    private void updateCarrierNameForSubscription(SubscriptionController subCon, int subId, int nameSource) {
        SubscriptionInfo subInfo = subCon.getActiveSubscriptionInfo(subId, this.mContext.getOpPackageName());
        if (subInfo != null) {
            CharSequence oldSubName = subInfo.getDisplayName();
            String newCarrierName = this.mTelephonyManager.getSimOperatorName(subId);
            if (!TextUtils.isEmpty(newCarrierName) && !newCarrierName.equals(oldSubName)) {
                log("sim name[" + this.mPhoneId + "] = " + newCarrierName);
                subCon.setDisplayNameUsingSrc(newCarrierName, subId, nameSource);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIccChangedEvent(Message msg) {
        int index = this.mHwUiccProfileEx.getUiccIndex(this.mPhoneId, msg);
        log("handleIccChangedEvent index = " + index + ", mPhoneId = " + this.mPhoneId);
        if (index == this.mPhoneId) {
            updateIccAvailability(false);
        }
    }

    private void updateIccAvailability(boolean allAppsChanged) {
        synchronized (this.mLock) {
            IccRecords newRecords = null;
            log("mCurrentAppType = " + this.mCurrentAppType + ", mPhoneId = " + this.mPhoneId);
            if (this.mUiccCard != null) {
                UiccCardExt uiccCardExt = new UiccCardExt();
                uiccCardExt.setUiccCard(this.mUiccCard);
                this.mCurrentAppType = this.mHwUiccProfileEx.processCurrentAppType(uiccCardExt, this.mCurrentAppType, this.mPhoneId);
            }
            log("mCurrentAppType = " + this.mCurrentAppType);
            UiccCardApplication newApp = getApplication(this.mCurrentAppType);
            if (this.mUiccCard != null) {
                log("UiccCard is not null, newApp = " + newApp);
            } else {
                log("UiccCard is null, newApp = " + newApp);
            }
            if (newApp != null) {
                newRecords = newApp.getIccRecords();
            }
            if (allAppsChanged) {
                unregisterAllAppEvents();
                registerAllAppEvents();
            }
            if (!(this.mIccRecords == newRecords && this.mUiccApplication == newApp)) {
                log("Icc changed. Reregistering.");
                unregisterCurrAppEvents();
                this.mUiccApplication = newApp;
                UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
                uiccCardApplicationEx.setUiccCardApplication(newApp);
                this.mHwUiccProfileEx.setUiccApplication(uiccCardApplicationEx);
                this.mHwUiccProfileEx.queryFdn();
                this.mIccRecords = newRecords;
                registerCurrAppEvents();
                updateActiveRecord();
            }
            updateExternalState();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetProperties() {
        if (this.mCurrentAppType == 1) {
            log("update icc_operator_numeric=");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mPhoneId, PhoneConfigurationManager.SSSS);
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId, PhoneConfigurationManager.SSSS);
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId, PhoneConfigurationManager.SSSS);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void updateExternalState() {
        if (this.mUiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT) {
            int radioState = this.mCi.getRadioState();
            if (radioState == 1) {
                setExternalState(IccCardConstants.State.ABSENT);
                return;
            }
            UiccCardExt uiccCardExt = new UiccCardExt();
            uiccCardExt.setUiccCard(this.mUiccCard);
            if (this.mHwUiccProfileEx.isSimAbsent(this.mContext, uiccCardExt) || radioState == 0) {
                setExternalState(IccCardConstants.State.ABSENT);
                log("updateExternalState ABSENT");
                return;
            }
            setExternalState(IccCardConstants.State.NOT_READY);
        } else if (this.mUiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_ERROR) {
            setExternalState(IccCardConstants.State.CARD_IO_ERROR);
        } else if (this.mUiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_RESTRICTED) {
            setExternalState(IccCardConstants.State.CARD_RESTRICTED);
        } else {
            UiccCard uiccCard = this.mUiccCard;
            if (!(uiccCard instanceof EuiccCard) || ((EuiccCard) uiccCard).getEid() != null) {
                UiccCardApplication uiccCardApplication = this.mUiccApplication;
                if (uiccCardApplication == null) {
                    loge("updateExternalState: setting state to NOT_READY because mUiccApplication is null");
                    setExternalState(IccCardConstants.State.NOT_READY);
                    return;
                }
                boolean cardLocked = false;
                boolean cardLockedByNetwork = false;
                IccCardConstants.State lockedState = null;
                IccCardApplicationStatus.AppState appState = uiccCardApplication.getState();
                if (this.mUiccApplication.getPin1State() == IccCardStatus.PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
                    cardLocked = true;
                    lockedState = IccCardConstants.State.PERM_DISABLED;
                } else if (appState == IccCardApplicationStatus.AppState.APPSTATE_PIN) {
                    cardLocked = true;
                    lockedState = IccCardConstants.State.PIN_REQUIRED;
                } else if (appState == IccCardApplicationStatus.AppState.APPSTATE_PUK) {
                    cardLocked = true;
                    lockedState = IccCardConstants.State.PUK_REQUIRED;
                } else if (appState == IccCardApplicationStatus.AppState.APPSTATE_SUBSCRIPTION_PERSO) {
                    if (this.mUiccApplication.getPersoSubState() == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK) {
                        cardLocked = true;
                        lockedState = IccCardConstants.State.NETWORK_LOCKED;
                    } else if (this.mUiccApplication.getPersoSubState() == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_READY || this.mUiccApplication.getPersoSubState() == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_UNKNOWN) {
                        setExternalState(IccCardConstants.State.UNKNOWN);
                        this.mHwUiccProfileEx.custResetExternalState();
                        return;
                    } else {
                        cardLocked = true;
                        cardLockedByNetwork = true;
                    }
                }
                if (cardLocked) {
                    IccRecords iccRecords = this.mIccRecords;
                    if (iccRecords == null || (!iccRecords.getLockedRecordsLoaded() && !this.mIccRecords.getNetworkLockedRecordsLoaded())) {
                        setExternalState(IccCardConstants.State.NOT_READY);
                    } else if (cardLockedByNetwork) {
                        this.mHwUiccProfileEx.custSetExternalState(IccCardApplicationStatusEx.PersoSubStateEx.getPersoSubStateEx(this.mUiccApplication.getPersoSubState()));
                        this.mExternalState = IccCardConstants.State.NETWORK_LOCKED;
                        loge("updateExternalState: set mPhoneId=" + this.mPhoneId + " mExternalState=" + this.mExternalState);
                    } else {
                        setExternalState(lockedState);
                    }
                } else {
                    int i = AnonymousClass4.$SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppState[appState.ordinal()];
                    if (i == 1) {
                        setExternalState(IccCardConstants.State.NOT_READY);
                    } else if (i == 2) {
                        checkAndUpdateIfAnyAppToBeIgnored();
                        if (!areAllApplicationsReady()) {
                            setExternalState(IccCardConstants.State.NOT_READY);
                        } else if (!areAllRecordsLoaded() || !areCarrierPriviligeRulesLoaded()) {
                            setExternalState(IccCardConstants.State.READY);
                        } else {
                            setExternalState(IccCardConstants.State.LOADED);
                        }
                    }
                    this.mHwUiccProfileEx.custUpdateExternalState(IccCardConstantsEx.StateEx.getStateExByState(getState()));
                }
            } else {
                log("EID is not ready yet.");
            }
        }
    }

    private void registerAllAppEvents() {
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        for (UiccCardApplication app : uiccCardApplicationArr) {
            if (app != null) {
                app.registerForReady(this.mHandler, 3, null);
                IccRecords ir = app.getIccRecords();
                if (ir != null) {
                    ir.registerForRecordsLoaded(this.mHandler, 4, null);
                    ir.registerForRecordsEvents(this.mHandler, 7, null);
                }
            }
        }
    }

    private void unregisterAllAppEvents() {
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        for (UiccCardApplication app : uiccCardApplicationArr) {
            if (app != null) {
                app.unregisterForReady(this.mHandler);
                IccRecords ir = app.getIccRecords();
                if (ir != null) {
                    ir.unregisterForRecordsLoaded(this.mHandler);
                    ir.unregisterForRecordsEvents(this.mHandler);
                }
            }
        }
    }

    private void registerCurrAppEvents() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForImsiReady(this.mHandler, 100, null);
            this.mIccRecords.registerForLockedRecordsLoaded(this.mHandler, 2, null);
            this.mIccRecords.registerForNetworkLockedRecordsLoaded(this.mHandler, 5, null);
        }
        this.mHwUiccProfileEx.registerForFdnStatusChange(this.mHandler);
        this.mHwUiccProfileEx.registerUiccCardEventsExtend();
    }

    private void unregisterCurrAppEvents() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unregisterForImsiReady(this.mHandler);
            this.mIccRecords.unregisterForLockedRecordsLoaded(this.mHandler);
            this.mIccRecords.unregisterForNetworkLockedRecordsLoaded(this.mHandler);
        }
        this.mHwUiccProfileEx.unregisterForFdnStatusChange(this.mHandler);
        this.mHwUiccProfileEx.unregisterUiccCardEventsExtend();
    }

    static void broadcastIccStateChangedIntent(String value, String reason, int phoneId) {
        if (!SubscriptionManager.isValidSlotIndex(phoneId)) {
            loge("broadcastIccStateChangedIntent: phoneId=" + phoneId + " is invalid; Return!!");
            return;
        }
        HwTelephonyFactory.getHwUiccManager().broadcastIccStateChangedIntentInternal(value, reason, phoneId);
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("phoneName", "Phone");
        intent.putExtra(IccCardConstantsEx.INTENT_KEY_ICC_STATE, value);
        intent.putExtra(IccCardConstantsEx.INTENT_KEY_LOCKED_REASON, reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
        VSimUtilsInner.putVSimExtraForIccStateChanged(intent, phoneId, value);
        log("broadcastIccStateChangedIntent intent ACTION_SIM_STATE_CHANGED value=" + value + " reason=" + reason + " for phoneId=" + phoneId);
        ActivityManager.broadcastStickyIntent(intent, 51, -1);
    }

    private void setExternalState(IccCardConstants.State newState, boolean override) {
        synchronized (this.mLock) {
            if (!SubscriptionManager.isValidSlotIndex(this.mPhoneId)) {
                loge("setExternalState: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
            } else if (override || newState != this.mExternalState) {
                if (IccCardConstants.State.ABSENT == newState || IccCardConstants.State.NOT_READY == newState) {
                    this.mHwUiccProfileEx.custResetExternalState();
                }
                if (blockPinStateForDualCards(newState)) {
                    HwTelephonyFactory.getHwUiccManager().broadcastIccStateChangedIntentInternal(getIccStateIntentString(newState), getIccStateReason(newState), this.mPhoneId);
                    return;
                }
                this.mExternalState = this.mHwUiccProfileEx.modifySimStateForVsim(this.mPhoneId, IccCardConstantsEx.StateEx.getStateExByState(newState)).getValue();
                if (this.mExternalState == IccCardConstants.State.LOADED && this.mIccRecords != null) {
                    String operator = this.mIccRecords.getOperatorNumeric();
                    log("setExternalState: operator=" + operator + " mPhoneId=" + this.mPhoneId);
                    if (!TextUtils.isEmpty(operator)) {
                        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mPhoneId, operator);
                        String countryCode = operator.substring(0, 3);
                        if (countryCode != null) {
                            try {
                                this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId, MccTable.countryCodeForMcc(countryCode));
                            } catch (Exception e) {
                                loge("countryCodeForMcc error for countryCode = " + countryCode);
                            }
                        } else {
                            loge("setExternalState: state LOADED; Country code is null");
                        }
                    } else {
                        loge("setExternalState: state LOADED; Operator name is null");
                    }
                }
                log("setExternalState: set mPhoneId=" + this.mPhoneId + " mExternalState=" + this.mExternalState);
                this.mHwUiccProfileEx.processSimLockStateForCT();
                UiccController.updateInternalIccState(this.mContext, this.mExternalState, getIccStateReason(this.mExternalState), this.mPhoneId);
                broadcastIccStateChangedIntent(getIccStateIntentString(this.mExternalState), getIccStateReason(this.mExternalState), this.mPhoneId);
            } else {
                log("setExternalState: !override and newstate unchanged from " + newState);
            }
        }
    }

    public boolean blockPinStateForDualCards(IccCardConstants.State s) {
        log("blockPinStateForDualCards s = " + s + ", mPhoneId " + this.mPhoneId);
        if (s.isPinLocked()) {
            if (VSimUtilsInner.isVSimInProcess()) {
                if (VSimUtilsInner.needBlockPin(this.mPhoneId)) {
                    log("vsim block pin for phone id " + this.mPhoneId);
                    return true;
                }
                log("vsim no need block pin for phone id " + this.mPhoneId + ", just pass");
            } else if ((SystemProperties.getBoolean("persist.sys.dualcards", false) || SystemProperties.getBoolean("ro.config.full_network_support", false)) && HwTelephonyFactory.getHwUiccManager().getSwitchingSlot()) {
                log("setExternalState getWaitingSwitchBalongSlot is true, so return");
                return true;
            }
        }
        return false;
    }

    private void setExternalState(IccCardConstants.State newState) {
        setExternalState(newState, false);
    }

    public boolean getIccRecordsLoaded() {
        synchronized (this.mLock) {
            if (this.mIccRecords == null) {
                return false;
            }
            return this.mIccRecords.getRecordsLoaded();
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.uicc.UiccProfile$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State = new int[IccCardConstants.State.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppState = new int[IccCardApplicationStatus.AppState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.READY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NOT_READY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PERM_DISABLED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_RESTRICTED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.LOADED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppState[IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppState[IccCardApplicationStatus.AppState.APPSTATE_READY.ordinal()] = 2;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private String getIccStateIntentString(IccCardConstants.State state) {
        switch (AnonymousClass4.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()]) {
            case 1:
                return IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT;
            case 2:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 3:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 4:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 5:
                return IccCardConstantsEx.INTENT_VALUE_ICC_READY;
            case 6:
                return IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY;
            case 7:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 8:
                return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR;
            case 9:
                return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED;
            case 10:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOADED;
            default:
                return IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    private String getIccStateReason(IccCardConstants.State state) {
        int i = AnonymousClass4.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()];
        if (i == 2) {
            return IccCardConstantsEx.INTENT_VALUE_LOCKED_ON_PIN;
        }
        if (i == 3) {
            return IccCardConstantsEx.INTENT_VALUE_LOCKED_ON_PUK;
        }
        if (i == 4) {
            return IccCardConstantsEx.INTENT_VALUE_LOCKED_NETWORK;
        }
        if (i == 7) {
            return IccCardConstantsEx.INTENT_VALUE_ABSENT_ON_PERM_DISABLED;
        }
        if (i == 8) {
            return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR;
        }
        if (i != 9) {
            return null;
        }
        return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED;
    }

    @Override // com.android.internal.telephony.IccCard
    public IccCardConstants.State getState() {
        IccCardConstants.State state;
        CommandsInterface commandsInterface;
        if (!VSimUtilsInner.isPlatformTwoModems() || (commandsInterface = this.mCi) == null || commandsInterface.isRadioAvailable()) {
            synchronized (this.mLock) {
                state = this.mExternalState;
            }
            return state;
        }
        log("[2Cards]pending sub" + this.mPhoneId + " getState return ABSENT!");
        return IccCardConstants.State.ABSENT;
    }

    @Override // com.android.internal.telephony.IccCard
    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (this.mLock) {
            iccRecords = this.mIccRecords;
        }
        return iccRecords;
    }

    @Override // com.android.internal.telephony.IccCard
    public void registerForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mNetworkLockedRegistrants.add(r);
            if (getState() == IccCardConstants.State.NETWORK_LOCKED) {
                r.notifyRegistrant();
            }
            this.mHwUiccProfileEx.custRegisterForNetworkLocked(h, what, obj);
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void unregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
            this.mHwUiccProfileEx.custUnregisterForNetworkLocked(h);
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk(puk, newPin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin2(pin2, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk2(puk2, newPin2, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyNetworkDepersonalization(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("CommandsInterface is not set.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean getIccLockEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mUiccApplication != null && this.mUiccApplication.getIccLockEnabled();
        }
        return z;
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean getIccFdnEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mUiccApplication != null && this.mUiccApplication.getIccFdnEnabled();
        }
        return z;
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean getIccFdnAvailable() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mUiccApplication != null && this.mUiccApplication.getIccFdnAvailable();
        }
        return z;
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean getIccPin2Blocked() {
        UiccCardApplication uiccCardApplication = this.mUiccApplication;
        return uiccCardApplication != null && uiccCardApplication.getIccPin2Blocked();
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean getIccPuk2Blocked() {
        UiccCardApplication uiccCardApplication = this.mUiccApplication;
        return uiccCardApplication != null && uiccCardApplication.getIccPuk2Blocked();
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean isEmptyProfile() {
        for (UiccCardApplication app : this.mUiccApplications) {
            if (app != null) {
                return false;
            }
        }
        return true;
    }

    @Override // com.android.internal.telephony.IccCard
    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccLockEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccFdnEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccLockPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccFdnPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public String getServiceProviderName() {
        synchronized (this.mLock) {
            if (this.mIccRecords == null) {
                return null;
            }
            return this.mIccRecords.getServiceProviderName();
        }
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean hasIccCard() {
        if (this.mUiccCard.getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT) {
            return true;
        }
        loge("hasIccCard: UiccProfile is not null but UiccCard is null or card state is ABSENT");
        return false;
    }

    public void update(Context c, CommandsInterface ci, IccCardStatus ics) {
        synchronized (this.mLock) {
            this.mUniversalPinState = ics.mUniversalPinState;
            this.mGsmUmtsSubscriptionAppIndex = ics.mGsmUmtsSubscriptionAppIndex;
            this.mCdmaSubscriptionAppIndex = ics.mCdmaSubscriptionAppIndex;
            this.mImsSubscriptionAppIndex = ics.mImsSubscriptionAppIndex;
            this.mContext = c;
            this.mCi = ci;
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            log(ics.mApplications.length + " applications");
            for (int i = 0; i < this.mUiccApplications.length; i++) {
                if (this.mUiccApplications[i] == null) {
                    if (i < ics.mApplications.length) {
                        this.mUiccApplications[i] = new UiccCardApplication(this, ics.mApplications[i], this.mContext, this.mCi);
                    }
                } else if (i >= ics.mApplications.length) {
                    this.mUiccApplications[i].dispose();
                    this.mUiccApplications[i] = null;
                } else {
                    this.mUiccApplications[i].update(ics.mApplications[i], this.mContext, this.mCi);
                }
            }
            createAndUpdateCatServiceLocked();
            log("Before privilege rules: " + this.mCarrierPrivilegeRules + " : " + ics.mCardState);
            if (this.mCarrierPrivilegeRules == null && ics.mCardState == IccCardStatus.CardState.CARDSTATE_PRESENT) {
                this.mCarrierPrivilegeRules = new UiccCarrierPrivilegeRules(this, this.mHandler.obtainMessage(13));
            } else if (!(this.mCarrierPrivilegeRules == null || ics.mCardState == IccCardStatus.CardState.CARDSTATE_PRESENT)) {
                this.mCarrierPrivilegeRules = null;
                this.mContext.getContentResolver().unregisterContentObserver(this.mProvisionCompleteContentObserver);
            }
            sanitizeApplicationIndexesLocked();
            updateIccAvailability(true);
        }
    }

    private void createAndUpdateCatServiceLocked() {
        if (VSimUtilsInner.isVSimSub(this.mPhoneId)) {
            log("createAndUpdateCatService, nothing for vsim sub " + this.mPhoneId);
            return;
        }
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        if (uiccCardApplicationArr.length <= 0 || uiccCardApplicationArr[0] == null) {
            CatService catService = this.mCatService;
            if (catService != null) {
                catService.dispose();
            }
            this.mCatService = null;
            return;
        }
        CatService catService2 = this.mCatService;
        if (catService2 == null) {
            this.mCatService = CatService.getInstance(this.mCi, this.mContext, this, this.mPhoneId);
        } else {
            catService2.update(this.mCi, this.mContext, this);
        }
    }

    public CatService getCatService() {
        return this.mCatService;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        log("UiccProfile finalized");
    }

    private void sanitizeApplicationIndexesLocked() {
        this.mGsmUmtsSubscriptionAppIndex = checkIndexLocked(this.mGsmUmtsSubscriptionAppIndex, IccCardApplicationStatus.AppType.APPTYPE_SIM, IccCardApplicationStatus.AppType.APPTYPE_USIM);
        this.mCdmaSubscriptionAppIndex = checkIndexLocked(this.mCdmaSubscriptionAppIndex, IccCardApplicationStatus.AppType.APPTYPE_RUIM, IccCardApplicationStatus.AppType.APPTYPE_CSIM);
        this.mImsSubscriptionAppIndex = checkIndexLocked(this.mImsSubscriptionAppIndex, IccCardApplicationStatus.AppType.APPTYPE_ISIM, null);
    }

    private boolean isSupportedApplication(UiccCardApplication app) {
        if (app.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM || app.getType() == IccCardApplicationStatus.AppType.APPTYPE_SIM) {
            return true;
        }
        if (!UiccController.isCdmaSupported(this.mContext)) {
            return false;
        }
        if (app.getType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM || app.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
            return true;
        }
        return false;
    }

    private void checkAndUpdateIfAnyAppToBeIgnored() {
        if (this.mUiccApplications == null) {
            loge("checkAndUpdateIfAnyAppToBeIgnored return!");
            return;
        }
        boolean[] appReadyStateTracker = new boolean[(IccCardApplicationStatus.AppType.APPTYPE_ISIM.ordinal() + 1)];
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        for (UiccCardApplication app : uiccCardApplicationArr) {
            if (app != null && isSupportedApplication(app) && app.isReady()) {
                appReadyStateTracker[app.getType().ordinal()] = true;
            }
        }
        UiccCardApplication[] uiccCardApplicationArr2 = this.mUiccApplications;
        for (UiccCardApplication app2 : uiccCardApplicationArr2) {
            if (app2 != null && isSupportedApplication(app2) && !app2.isReady() && appReadyStateTracker[app2.getType().ordinal()]) {
                app2.setAppIgnoreState(true);
            }
        }
    }

    private boolean areAllApplicationsReady() {
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        if (uiccCardApplicationArr == null) {
            loge("areAllApplicationsReady return!");
            return false;
        }
        for (UiccCardApplication app : uiccCardApplicationArr) {
            if (!(app == null || !isSupportedApplication(app) || app.isReady() || app.isAppIgnored())) {
                return false;
            }
        }
        if (this.mUiccApplication != null) {
            return true;
        }
        return false;
    }

    private boolean areAllRecordsLoaded() {
        IccRecords ir;
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        if (uiccCardApplicationArr == null) {
            loge("areAllRecordsLoaded return!");
            return false;
        }
        for (UiccCardApplication app : uiccCardApplicationArr) {
            if (app != null && isSupportedApplication(app) && !app.isAppIgnored() && ((ir = app.getIccRecords()) == null || !ir.isLoaded())) {
                return false;
            }
        }
        if (this.mUiccApplication != null) {
            return true;
        }
        return false;
    }

    private int checkIndexLocked(int index, IccCardApplicationStatus.AppType expectedAppType, IccCardApplicationStatus.AppType altExpectedAppType) {
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        if (uiccCardApplicationArr == null || index >= uiccCardApplicationArr.length) {
            loge("App index " + index + " is invalid since there are no applications");
            return -1;
        } else if (index < 0) {
            return -1;
        } else {
            if (uiccCardApplicationArr[index].getType() == expectedAppType || this.mUiccApplications[index].getType() == altExpectedAppType) {
                return index;
            }
            loge("App index " + index + " is invalid since it's not " + expectedAppType + " and not " + altExpectedAppType);
            return -1;
        }
    }

    public void registerForOpertorBrandOverride(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            this.mOperatorBrandOverrideRegistrants.add(new Registrant(h, what, obj));
        }
    }

    public void registerForCarrierPrivilegeRulesLoaded(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mCarrierPrivilegeRegistrants.add(r);
            if (areCarrierPriviligeRulesLoaded()) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForCarrierPrivilegeRulesLoaded(Handler h) {
        synchronized (this.mLock) {
            this.mCarrierPrivilegeRegistrants.remove(h);
        }
    }

    public void unregisterForOperatorBrandOverride(Handler h) {
        synchronized (this.mLock) {
            this.mOperatorBrandOverrideRegistrants.remove(h);
        }
    }

    static boolean isPackageInstalled(Context context, String pkgName) {
        try {
            context.getPackageManager().getPackageInfo(pkgName, 1);
            log(pkgName + " is installed.");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            log(pkgName + " is not installed.");
            return false;
        }
    }

    private void promptInstallCarrierApp(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Intent showDialogIntent = InstallCarrierAppTrampolineActivity.get(this.mContext, pkgName);
            showDialogIntent.addFlags(268435456);
            this.mContext.startActivity(showDialogIntent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCarrierPrivilegesLoadedMessage() {
        UsageStatsManager usm = (UsageStatsManager) this.mContext.getSystemService("usagestats");
        if (usm != null) {
            usm.onCarrierPrivilegedAppsChanged();
        }
        InstallCarrierAppUtils.hideAllNotifications(this.mContext);
        InstallCarrierAppUtils.unregisterPackageInstallReceiver(this.mContext);
        synchronized (this.mLock) {
            this.mCarrierPrivilegeRegistrants.notifyRegistrants();
            boolean isProvisioned = true;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) != 1) {
                isProvisioned = false;
            }
            if (isProvisioned) {
                for (String pkgName : getUninstalledCarrierPackages()) {
                    promptInstallCarrierApp(pkgName);
                }
            } else {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mProvisionCompleteContentObserver);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Set<String> getUninstalledCarrierPackages() {
        String whitelistSetting = Settings.Global.getString(this.mContext.getContentResolver(), "carrier_app_whitelist");
        if (TextUtils.isEmpty(whitelistSetting)) {
            return Collections.emptySet();
        }
        Map<String, String> certPackageMap = parseToCertificateToPackageMap(whitelistSetting);
        if (certPackageMap.isEmpty()) {
            return Collections.emptySet();
        }
        if (this.mCarrierPrivilegeRules == null) {
            log("getUninstalledCarrierPackages mCarrierPrivilegeRules is null");
            return Collections.emptySet();
        }
        Set<String> uninstalledCarrierPackages = new ArraySet<>();
        for (UiccAccessRule accessRule : this.mCarrierPrivilegeRules.getAccessRules()) {
            String pkgName = certPackageMap.get(accessRule.getCertificateHexString().toUpperCase());
            if (!TextUtils.isEmpty(pkgName) && !isPackageInstalled(this.mContext, pkgName)) {
                uninstalledCarrierPackages.add(pkgName);
            }
        }
        return uninstalledCarrierPackages;
    }

    @VisibleForTesting
    public static Map<String, String> parseToCertificateToPackageMap(String whitelistSetting) {
        List<String> keyValuePairList = Arrays.asList(whitelistSetting.split("\\s*;\\s*"));
        if (keyValuePairList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new ArrayMap<>(keyValuePairList.size());
        for (String keyValueString : keyValuePairList) {
            String[] keyValue = keyValueString.split("\\s*:\\s*");
            if (keyValue.length == 2) {
                map.put(keyValue[0].toUpperCase(), keyValue[1]);
            } else {
                loge("Incorrect length of key-value pair in carrier app whitelist map.  Length should be exactly 2");
            }
        }
        return map;
    }

    @Override // com.android.internal.telephony.IccCard
    public boolean isApplicationOnIcc(IccCardApplicationStatus.AppType type) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mUiccApplications.length; i++) {
                if (this.mUiccApplications[i] != null && this.mUiccApplications[i].getType() == type) {
                    return true;
                }
            }
            return false;
        }
    }

    public IccCardStatus.PinState getUniversalPinState() {
        IccCardStatus.PinState pinState;
        synchronized (this.mLock) {
            pinState = this.mUniversalPinState;
        }
        return pinState;
    }

    public UiccCardApplication getApplication(int family) {
        synchronized (this.mLock) {
            int index = 8;
            if (family == 1) {
                index = this.mGsmUmtsSubscriptionAppIndex;
            } else if (family == 2) {
                index = this.mCdmaSubscriptionAppIndex;
            } else if (family == 3) {
                index = this.mImsSubscriptionAppIndex;
            }
            if (index < 0 || this.mUiccApplications == null || index >= this.mUiccApplications.length) {
                return null;
            }
            return this.mUiccApplications[index];
        }
    }

    public UiccCardApplication getApplicationIndex(int index) {
        synchronized (this.mLock) {
            if (index >= 0) {
                if (index < this.mUiccApplications.length) {
                    return this.mUiccApplications[index];
                }
            }
            return null;
        }
    }

    public UiccCardApplication getApplicationByType(int type) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mUiccApplications.length; i++) {
                if (this.mUiccApplications[i] != null && this.mUiccApplications[i].getType().ordinal() == type) {
                    return this.mUiccApplications[i];
                }
            }
            return null;
        }
    }

    public boolean resetAppWithAid(String aid, boolean reset) {
        boolean changed;
        synchronized (this.mLock) {
            changed = false;
            for (int i = 0; i < this.mUiccApplications.length; i++) {
                if (this.mUiccApplications[i] != null && (TextUtils.isEmpty(aid) || aid.equals(this.mUiccApplications[i].getAid()))) {
                    this.mUiccApplications[i].dispose();
                    this.mUiccApplications[i] = null;
                    changed = true;
                }
            }
            if (reset && TextUtils.isEmpty(aid)) {
                if (this.mCarrierPrivilegeRules != null) {
                    this.mCarrierPrivilegeRules = null;
                    this.mContext.getContentResolver().unregisterContentObserver(this.mProvisionCompleteContentObserver);
                    changed = true;
                }
                if (HuaweiTelephonyConfigs.isMTKPlatform() && this.mCatService != null) {
                    this.mCatService.dispose();
                    this.mCatService = null;
                    changed = true;
                }
            }
        }
        return changed;
    }

    public void iccOpenLogicalChannel(String aid, int p2, Message response) {
        if (Log.HWINFO) {
            loglocal("iccOpenLogicalChannel: " + aid + " , " + p2 + " by pid:" + Binder.getCallingPid() + " uid:" + Binder.getCallingUid());
        }
        this.mCi.iccOpenLogicalChannel(aid, p2, this.mHandler.obtainMessage(8, response));
    }

    public void iccCloseLogicalChannel(int channel, Message response) {
        loglocal("iccCloseLogicalChannel: " + channel);
        this.mCi.iccCloseLogicalChannel(channel, this.mHandler.obtainMessage(9, response));
    }

    public void iccTransmitApduLogicalChannel(int channel, int cla, int command, int p1, int p2, int p3, String data, Message response) {
        this.mCi.iccTransmitApduLogicalChannel(channel, cla, command, p1, p2, p3, data, this.mHandler.obtainMessage(10, response));
    }

    public void iccTransmitApduBasicChannel(int cla, int command, int p1, int p2, int p3, String data, Message response) {
        this.mCi.iccTransmitApduBasicChannel(cla, command, p1, p2, p3, data, this.mHandler.obtainMessage(11, response));
    }

    public void iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String pathID, Message response) {
        this.mCi.iccIO(command, fileID, pathID, p1, p2, p3, null, null, this.mHandler.obtainMessage(12, response));
    }

    public void sendEnvelopeWithStatus(String contents, Message response) {
        this.mCi.sendEnvelopeWithStatus(contents, response);
    }

    public int getNumApplications() {
        int count = 0;
        for (UiccCardApplication a : this.mUiccApplications) {
            if (a != null) {
                count++;
            }
        }
        return count;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public boolean areCarrierPriviligeRulesLoaded() {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        return carrierPrivilegeRules == null || carrierPrivilegeRules.areCarrierPriviligeRulesLoaded();
    }

    public boolean hasCarrierPrivilegeRules() {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        return carrierPrivilegeRules != null && carrierPrivilegeRules.hasCarrierPrivilegeRules();
    }

    public int getCarrierPrivilegeStatus(Signature signature, String packageName) {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules == null) {
            return -1;
        }
        return carrierPrivilegeRules.getCarrierPrivilegeStatus(signature, packageName);
    }

    public int getCarrierPrivilegeStatus(PackageManager packageManager, String packageName) {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules == null) {
            return -1;
        }
        return carrierPrivilegeRules.getCarrierPrivilegeStatus(packageManager, packageName);
    }

    public int getCarrierPrivilegeStatus(PackageInfo packageInfo) {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules == null) {
            return -1;
        }
        return carrierPrivilegeRules.getCarrierPrivilegeStatus(packageInfo);
    }

    public int getCarrierPrivilegeStatusForCurrentTransaction(PackageManager packageManager) {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules == null) {
            return -1;
        }
        return carrierPrivilegeRules.getCarrierPrivilegeStatusForCurrentTransaction(packageManager);
    }

    public int getCarrierPrivilegeStatusForUid(PackageManager packageManager, int uid) {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules == null) {
            return -1;
        }
        return carrierPrivilegeRules.getCarrierPrivilegeStatusForUid(packageManager, uid);
    }

    public List<String> getCertsFromCarrierPrivilegeAccessRules() {
        List<String> certs = new ArrayList<>();
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules != null) {
            for (UiccAccessRule accessRule : carrierPrivilegeRules.getAccessRules()) {
                certs.add(accessRule.getCertificateHexString());
            }
        }
        if (certs.isEmpty()) {
            return null;
        }
        return certs;
    }

    public List<String> getCarrierPackageNamesForIntent(PackageManager packageManager, Intent intent) {
        UiccCarrierPrivilegeRules carrierPrivilegeRules = getCarrierPrivilegeRules();
        if (carrierPrivilegeRules == null) {
            return null;
        }
        return carrierPrivilegeRules.getCarrierPackageNamesForIntent(packageManager, intent);
    }

    private UiccCarrierPrivilegeRules getCarrierPrivilegeRules() {
        UiccCarrierPrivilegeRules uiccCarrierPrivilegeRules;
        synchronized (this.mLock) {
            uiccCarrierPrivilegeRules = this.mCarrierPrivilegeRules;
        }
        return uiccCarrierPrivilegeRules;
    }

    public boolean setOperatorBrandOverride(String brand) {
        log("setOperatorBrandOverride: " + brand);
        log("current iccId: " + SubscriptionInfo.givePrintableIccid(getIccId()));
        String iccId = getIccId();
        if (TextUtils.isEmpty(iccId)) {
            return false;
        }
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String key = OPERATOR_BRAND_OVERRIDE_PREFIX + iccId;
        if (brand == null) {
            spEditor.remove(key).commit();
        } else {
            spEditor.putString(key, brand).commit();
        }
        this.mOperatorBrandOverrideRegistrants.notifyRegistrants();
        return true;
    }

    public String getOperatorBrandOverride() {
        String iccId = getIccId();
        if (TextUtils.isEmpty(iccId)) {
            return null;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        return sp.getString(OPERATOR_BRAND_OVERRIDE_PREFIX + iccId, null);
    }

    public String getIccId() {
        IccRecords ir;
        UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
        for (UiccCardApplication app : uiccCardApplicationArr) {
            if (!(app == null || (ir = app.getIccRecords()) == null || ir.getIccId() == null)) {
                return ir.getIccId();
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static void log(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loglocal(String msg) {
        LocalLog localLog = UiccController.sLocalLog;
        localLog.log("UiccProfile[" + this.mPhoneId + "]: " + msg);
    }

    @VisibleForTesting
    public void refresh() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(13));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IccRecords ir;
        pw.println("UiccProfile:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mCatService=" + this.mCatService);
        for (int i = 0; i < this.mCarrierPrivilegeRegistrants.size(); i++) {
            pw.println("  mCarrierPrivilegeRegistrants[" + i + "]=" + ((Registrant) this.mCarrierPrivilegeRegistrants.get(i)).getHandler());
        }
        for (int i2 = 0; i2 < this.mOperatorBrandOverrideRegistrants.size(); i2++) {
            pw.println("  mOperatorBrandOverrideRegistrants[" + i2 + "]=" + ((Registrant) this.mOperatorBrandOverrideRegistrants.get(i2)).getHandler());
        }
        pw.println(" mUniversalPinState=" + this.mUniversalPinState);
        pw.println(" mGsmUmtsSubscriptionAppIndex=" + this.mGsmUmtsSubscriptionAppIndex);
        pw.println(" mCdmaSubscriptionAppIndex=" + this.mCdmaSubscriptionAppIndex);
        pw.println(" mImsSubscriptionAppIndex=" + this.mImsSubscriptionAppIndex);
        pw.println(" mUiccApplications: length=" + this.mUiccApplications.length);
        int i3 = 0;
        while (true) {
            UiccCardApplication[] uiccCardApplicationArr = this.mUiccApplications;
            if (i3 >= uiccCardApplicationArr.length) {
                break;
            }
            if (uiccCardApplicationArr[i3] == null) {
                pw.println("  mUiccApplications[" + i3 + "]=" + ((Object) null));
            } else {
                pw.println("  mUiccApplications[" + i3 + "]=" + this.mUiccApplications[i3].getType() + " " + this.mUiccApplications[i3]);
            }
            i3++;
        }
        pw.println();
        UiccCardApplication[] uiccCardApplicationArr2 = this.mUiccApplications;
        for (UiccCardApplication app : uiccCardApplicationArr2) {
            if (app != null) {
                app.dump(fd, pw, args);
                pw.println();
            }
        }
        UiccCardApplication[] uiccCardApplicationArr3 = this.mUiccApplications;
        for (UiccCardApplication app2 : uiccCardApplicationArr3) {
            if (!(app2 == null || (ir = app2.getIccRecords()) == null)) {
                ir.dump(fd, pw, args);
                pw.println();
            }
        }
        if (this.mCarrierPrivilegeRules == null) {
            pw.println(" mCarrierPrivilegeRules: null");
        } else {
            pw.println(" mCarrierPrivilegeRules: " + this.mCarrierPrivilegeRules);
            this.mCarrierPrivilegeRules.dump(fd, pw, args);
        }
        pw.println(" mCarrierPrivilegeRegistrants: size=" + this.mCarrierPrivilegeRegistrants.size());
        for (int i4 = 0; i4 < this.mCarrierPrivilegeRegistrants.size(); i4++) {
            pw.println("  mCarrierPrivilegeRegistrants[" + i4 + "]=" + ((Registrant) this.mCarrierPrivilegeRegistrants.get(i4)).getHandler());
        }
        pw.flush();
        pw.println(" mNetworkLockedRegistrants: size=" + this.mNetworkLockedRegistrants.size());
        for (int i5 = 0; i5 < this.mNetworkLockedRegistrants.size(); i5++) {
            pw.println("  mNetworkLockedRegistrants[" + i5 + "]=" + ((Registrant) this.mNetworkLockedRegistrants.get(i5)).getHandler());
        }
        pw.println(" mCurrentAppType=" + this.mCurrentAppType);
        pw.println(" mUiccCard=" + this.mUiccCard);
        pw.println(" mUiccApplication=" + this.mUiccApplication);
        pw.println(" mIccRecords=" + this.mIccRecords);
        pw.println(" mExternalState=" + this.mExternalState);
        pw.flush();
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public int getPhoneIdHw() {
        return this.mPhoneId;
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public CommandsInterfaceEx getCiHw() {
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(this.mCi);
        return commandsInterfaceEx;
    }

    public static int getEventRadioOffOrUnavailableHw() {
        return 1;
    }

    public static int getEventAppReadyHw() {
        return 3;
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public UiccCard getUiccCardHw() {
        return this.mUiccCard;
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public IccRecordsEx getIccRecordsHw() {
        IccRecordsEx iccRecordsEx = new IccRecordsEx();
        iccRecordsEx.setIccRecords(this.mIccRecords);
        return iccRecordsEx;
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public void broadcastIccStateChangedIntentHw(String value, String reason) {
        broadcastIccStateChangedIntent(value, reason, this.mPhoneId);
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public int getGsmUmtsSubscriptionAppIndex() {
        return this.mGsmUmtsSubscriptionAppIndex;
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public int getCdmaSubscriptionAppIndex() {
        return this.mCdmaSubscriptionAppIndex;
    }

    @Override // com.android.internal.telephony.IccCard
    public void supplyDepersonalization(String pin, int type, Message onComplete) {
        this.mHwUiccProfileEx.supplyDepersonalization(pin, type, onComplete);
    }

    @Override // com.android.internal.telephony.uicc.IUiccProfileInner
    public boolean getIccCardStateHw() {
        return this.mHwUiccProfileEx.getIccCardStateHw();
    }
}
