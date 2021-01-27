package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.Carrier;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.CdmaSmsAck;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaSmsSubaddress;
import android.hardware.radio.V1_0.CdmaSmsWriteArgs;
import android.hardware.radio.V1_0.DataProfileInfo;
import android.hardware.radio.V1_0.Dial;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.GsmSmsMessage;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.HardwareConfigModem;
import android.hardware.radio.V1_0.HardwareConfigSim;
import android.hardware.radio.V1_0.IRadio;
import android.hardware.radio.V1_0.IccIo;
import android.hardware.radio.V1_0.ImsSmsMessage;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.NvWriteItem;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SelectUiccSub;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SimApdu;
import android.hardware.radio.V1_0.SmsWriteArgs;
import android.hardware.radio.V1_0.UusInfo;
import android.hardware.radio.V1_1.KeepaliveRequest;
import android.hardware.radio.V1_1.RadioAccessSpecifier;
import android.hardware.radio.V1_2.LinkCapacityEstimate;
import android.hardware.radio.V1_4.CarrierRestrictionsWithPriority;
import android.hardware.radio.deprecated.V1_0.IOemHook;
import android.net.ConnectivityManager;
import android.net.KeepalivePacketData;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.service.carrier.CarrierIdentifier;
import android.telephony.AccessNetworkConstants;
import android.telephony.CarrierRestrictionRules;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.ClientRequestStats;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.ModemActivityInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyHistogram;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.cat.ComprehensionTlv;
import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.PlmnActRecord;
import com.android.internal.util.Preconditions;
import com.google.android.mms.pdu.CharacterSets;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.RILRequestEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import vendor.huawei.hardware.hisiradio.V1_0.IHisiRadio;

public class RIL extends AbstractRIL implements CommandsInterface {
    private static final String ACTION_RIL_CONNECTED = "com.huawei.intent.action.RIL_CONNECTED";
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_BLOCKING_MESSAGE_RESPONSE_TIMEOUT_MS = 2000;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final String EMPTY_ALPHA_LONG = "";
    static final String EMPTY_ALPHA_SHORT = "";
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    static final int EVENT_RADIO_PROXY_DEAD = 6;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    static final String[] HIDL_SERVICE_NAME = {"slot1", "slot2", "slot3"};
    public static final int INVALID_WAKELOCK = -1;
    static final int INVALTD_RADIO_TECH = -1;
    static final int IRADIO_GET_SERVICE_DELAY_MILLIS = (HuaweiTelephonyConfigs.isHisiPlatform() ? 1000 : 4000);
    public static final HalVersion RADIO_HAL_VERSION_1_0 = new HalVersion(1, 0);
    public static final HalVersion RADIO_HAL_VERSION_1_1 = new HalVersion(1, 1);
    public static final HalVersion RADIO_HAL_VERSION_1_2 = new HalVersion(1, 2);
    public static final HalVersion RADIO_HAL_VERSION_1_3 = new HalVersion(1, 3);
    public static final HalVersion RADIO_HAL_VERSION_1_4 = new HalVersion(1, 4);
    public static final HalVersion RADIO_HAL_VERSION_UNKNOWN = HalVersion.UNKNOWN;
    static final String RILJ_ACK_WAKELOCK_NAME = "RILJ_ACK_WL";
    static final boolean RILJ_LOGD = true;
    static final boolean RILJ_LOGV = false;
    static final String RILJ_LOG_TAG = "RILJ";
    static final String RILJ_WAKELOCK_TAG = "*telephony-radio*";
    static final int RIL_HISTOGRAM_BUCKET_COUNT = 5;
    static SparseArray<TelephonyHistogram> mRilTimeHistograms = new SparseArray<>();
    final PowerManager.WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private WorkSource mActiveWakelockWorkSource;
    private final ClientWakelockTracker mClientWakelockTracker;
    Set<Integer> mDisabledOemHookServices;
    Set<Integer> mDisabledRadioServices;
    volatile IHisiRadio mHisiRadioProxy;
    boolean mIsMobileNetworkSupported;
    Object[] mLastNITZTimeInfo;
    int mLastRadioTech;
    private TelephonyMetrics mMetrics;
    OemHookIndication mOemHookIndication;
    volatile IOemHook mOemHookProxy;
    OemHookResponse mOemHookResponse;
    final Integer mPhoneId;
    protected WorkSource mRILDefaultWorkSource;
    private RadioBugDetector mRadioBugDetector;
    RadioIndication mRadioIndication;
    volatile IRadio mRadioProxy;
    final AtomicLong mRadioProxyCookie;
    final RadioProxyDeathRecipient mRadioProxyDeathRecipient;
    RadioResponse mRadioResponse;
    private HalVersion mRadioVersion;
    @UnsupportedAppUsage
    SparseArray<RILRequest> mRequestList;
    final RilHandler mRilHandler;
    @UnsupportedAppUsage
    AtomicBoolean mTestingEmergencyCall;
    @UnsupportedAppUsage
    final PowerManager.WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;

    public static List<TelephonyHistogram> getTelephonyRILTimingHistograms() {
        List<TelephonyHistogram> list;
        synchronized (mRilTimeHistograms) {
            list = new ArrayList<>(mRilTimeHistograms.size());
            for (int i = 0; i < mRilTimeHistograms.size(); i++) {
                list.add(new TelephonyHistogram(mRilTimeHistograms.valueAt(i)));
            }
        }
        return list;
    }

    @VisibleForTesting
    public class RilHandler extends Handler {
        public RilHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 2) {
                synchronized (RIL.this.mRequestList) {
                    if (msg.arg1 == RIL.this.mWlSequenceNum && RIL.this.clearWakeLock(0)) {
                        if (RIL.this.mRadioBugDetector != null) {
                            RIL.this.mRadioBugDetector.processWakelockTimeout();
                        }
                        int count = RIL.this.mRequestList.size();
                        RIL ril = RIL.this;
                        ril.riljLog("WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                        for (int i2 = 0; i2 < count; i2++) {
                            RILRequest rr = RIL.this.mRequestList.valueAt(i2);
                            RIL ril2 = RIL.this;
                            ril2.riljLog(i2 + ": [" + rr.mSerial + "] " + RIL.requestToString(rr.mRequest));
                        }
                    }
                }
            } else if (i != 4) {
                if (i == 5) {
                    RILRequest rr2 = RIL.this.findAndRemoveRequestFromList(msg.arg1);
                    if (rr2 != null) {
                        if (rr2.mResult != null) {
                            AsyncResult.forMessage(rr2.mResult, RIL.getResponseForTimedOutRILRequest(rr2), (Throwable) null);
                            rr2.mResult.sendToTarget();
                            RIL.this.mMetrics.writeOnRilTimeoutResponse(RIL.this.mPhoneId.intValue(), rr2.mSerial, rr2.mRequest);
                        }
                        RIL.this.decrementWakeLock(rr2);
                        rr2.release();
                    }
                } else if (i == 6) {
                    RIL ril3 = RIL.this;
                    ril3.riljLog("handleMessage: EVENT_RADIO_PROXY_DEAD cookie = " + msg.obj + " mRadioProxyCookie = " + RIL.this.mRadioProxyCookie.get());
                    if (((Long) msg.obj).longValue() == RIL.this.mRadioProxyCookie.get()) {
                        RIL.this.resetProxyAndRequestList();
                    }
                }
            } else if (msg.arg1 == RIL.this.mAckWlSequenceNum) {
                RIL.this.clearWakeLock(1);
            }
        }
    }

    @VisibleForTesting
    public RadioBugDetector getRadioBugDetector() {
        if (this.mRadioBugDetector == null) {
            this.mRadioBugDetector = new RadioBugDetector(this.mContext, this.mPhoneId.intValue());
        }
        return this.mRadioBugDetector;
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        if (rr != null && rr.mRequest == 135) {
            return new ModemActivityInfo(0, 0, 0, new int[5], 0, 0);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final class RadioProxyDeathRecipient implements IHwBinder.DeathRecipient {
        RadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            RIL.this.riljLog("serviceDied");
            RIL.this.mRilHandler.sendMessage(RIL.this.mRilHandler.obtainMessage(6, Long.valueOf(cookie)));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetProxyAndRequestList() {
        this.mRadioProxy = null;
        this.mHisiRadioProxy = null;
        clearHuaweiCommonRadioProxy();
        clearHwOemHookProxy();
        clearMTKRadioProxy();
        clearQcomRadioProxy();
        this.mOemHookProxy = null;
        this.mRadioProxyCookie.incrementAndGet();
        setRadioState(2, true);
        RILRequest.resetSerial();
        clearRequestList(1, false);
        getRadioProxy(null);
        getHuaweiCommonRadioProxy(null);
        getHisiRadioProxy(null);
        resetMTKRadioProxy();
        resetQcomRadioProxy();
        getHwOemHookProxy(null);
    }

    @VisibleForTesting
    public synchronized IRadio getRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mRadioProxy != null) {
            return this.mRadioProxy;
        } else {
            Integer phoneId = Integer.valueOf(this.mPhoneId == null ? 0 : this.mPhoneId.intValue());
            try {
                if (this.mDisabledRadioServices.contains(this.mPhoneId)) {
                    riljLoge("getRadioProxy: mRadioProxy for " + HIDL_SERVICE_NAME[phoneId.intValue()] + " is disabled");
                } else {
                    try {
                        this.mRadioProxy = android.hardware.radio.V1_4.IRadio.getService(HIDL_SERVICE_NAME[phoneId.intValue()], CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext));
                        this.mRadioVersion = RADIO_HAL_VERSION_1_4;
                    } catch (NoSuchElementException e) {
                    }
                    if (this.mRadioProxy == null) {
                        try {
                            this.mRadioProxy = android.hardware.radio.V1_3.IRadio.getService(HIDL_SERVICE_NAME[phoneId.intValue()], CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext));
                            this.mRadioVersion = RADIO_HAL_VERSION_1_3;
                        } catch (NoSuchElementException e2) {
                        }
                    }
                    if (this.mRadioProxy == null) {
                        try {
                            this.mRadioProxy = android.hardware.radio.V1_2.IRadio.getService(HIDL_SERVICE_NAME[phoneId.intValue()], CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext));
                            this.mRadioVersion = RADIO_HAL_VERSION_1_2;
                        } catch (NoSuchElementException e3) {
                        }
                    }
                    if (this.mRadioProxy == null) {
                        try {
                            this.mRadioProxy = android.hardware.radio.V1_1.IRadio.getService(HIDL_SERVICE_NAME[phoneId.intValue()], CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext));
                            this.mRadioVersion = RADIO_HAL_VERSION_1_1;
                        } catch (NoSuchElementException e4) {
                        }
                    }
                    if (this.mRadioProxy == null) {
                        try {
                            this.mRadioProxy = IRadio.getService(HIDL_SERVICE_NAME[phoneId.intValue()], CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext));
                            this.mRadioVersion = RADIO_HAL_VERSION_1_0;
                        } catch (NoSuchElementException e5) {
                        }
                    }
                    if (this.mRadioProxy != null) {
                        this.mRadioProxy.linkToDeath(this.mRadioProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                        this.mRadioProxy.setResponseFunctions(this.mRadioResponse, this.mRadioIndication);
                    } else if (CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext)) {
                        this.mDisabledRadioServices.add(phoneId);
                        riljLoge("getRadioProxy: mRadioProxy for " + HIDL_SERVICE_NAME[phoneId.intValue()] + " is disabled");
                    }
                }
            } catch (RemoteException e6) {
                this.mRadioProxy = null;
                riljLoge("RadioProxy getService/setResponseFunctions failed.");
            }
            if (this.mRadioProxy == null) {
                riljLoge("getRadioProxy: mRadioProxy == null");
                if (result != null) {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
            }
            return this.mRadioProxy;
        }
    }

    @VisibleForTesting
    public synchronized IOemHook getOemHookProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mOemHookProxy != null) {
            return this.mOemHookProxy;
        } else {
            try {
                if (this.mDisabledOemHookServices.contains(this.mPhoneId)) {
                    riljLoge("getOemHookProxy: mOemHookProxy for " + HIDL_SERVICE_NAME[this.mPhoneId.intValue()] + " is disabled");
                } else {
                    this.mOemHookProxy = IOemHook.getService(HIDL_SERVICE_NAME[this.mPhoneId.intValue()], true);
                    if (this.mOemHookProxy != null) {
                        this.mOemHookProxy.setResponseFunctions(this.mOemHookResponse, this.mOemHookIndication);
                    } else {
                        this.mDisabledOemHookServices.add(this.mPhoneId);
                        riljLoge("getOemHookProxy: mOemHookProxy for " + HIDL_SERVICE_NAME[this.mPhoneId.intValue()] + " is disabled");
                    }
                }
            } catch (NoSuchElementException e) {
                this.mOemHookProxy = null;
                riljLoge("IOemHook service is not on the device HAL failed.");
            } catch (RemoteException e2) {
                this.mOemHookProxy = null;
                riljLoge("OemHookProxy getService/setResponseFunctions failed.");
            }
            if (this.mOemHookProxy == null && result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return this.mOemHookProxy;
        }
    }

    @UnsupportedAppUsage
    public RIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    @UnsupportedAppUsage
    public RIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context);
        this.mLastRadioTech = -1;
        this.mClientWakelockTracker = new ClientWakelockTracker();
        this.mRadioVersion = RADIO_HAL_VERSION_UNKNOWN;
        this.mWlSequenceNum = 0;
        this.mAckWlSequenceNum = 0;
        this.mRequestList = new SparseArray<>();
        this.mTestingEmergencyCall = new AtomicBoolean(false);
        this.mDisabledRadioServices = new HashSet();
        this.mDisabledOemHookServices = new HashSet();
        this.mMetrics = TelephonyMetrics.getInstance();
        this.mRadioBugDetector = null;
        this.mRadioProxy = null;
        this.mHisiRadioProxy = null;
        this.mOemHookProxy = null;
        this.mRadioProxyCookie = new AtomicLong(0);
        riljLog("RIL: init preferredNetworkType=" + preferredNetworkType + " cdmaSubscription=" + cdmaSubscription + ")");
        this.mContext = context;
        this.mCdmaSubscription = cdmaSubscription;
        this.mPreferredNetworkType = preferredNetworkType;
        this.mPhoneType = 0;
        this.mPhoneId = Integer.valueOf(instanceId == null ? 0 : instanceId.intValue());
        if (isRadioBugDetectionEnabled()) {
            this.mRadioBugDetector = new RadioBugDetector(context, this.mPhoneId.intValue());
        }
        this.mIsMobileNetworkSupported = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        Integer num = this.mPhoneId;
        if (num != null) {
            setHwRILReferenceInstanceId(num.intValue());
        }
        this.mRadioResponse = new RadioResponse(this);
        this.mRadioIndication = new RadioIndication(this);
        this.mOemHookResponse = new OemHookResponse(this);
        this.mOemHookIndication = new OemHookIndication(this);
        this.mRilHandler = new RilHandler();
        this.mRadioProxyDeathRecipient = new RadioProxyDeathRecipient();
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mWakeLock = pm.newWakeLock(1, RILJ_WAKELOCK_TAG);
        this.mWakeLock.setReferenceCounted(false);
        this.mAckWakeLock = pm.newWakeLock(1, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock.setReferenceCounted(false);
        this.mWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 60000);
        this.mAckWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 200);
        this.mWakeLockCount = 0;
        this.mRILDefaultWorkSource = new WorkSource(context.getApplicationInfo().uid, context.getPackageName());
        this.mActiveWakelockWorkSource = new WorkSource();
        TelephonyDevController.getInstance();
        TelephonyDevController.registerRIL(this);
        getRadioProxy(null);
        getHwOemHookProxy(null);
        riljLog("Radio HAL version: " + this.mRadioVersion);
    }

    private boolean isRadioBugDetectionEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "enable_radio_bug_detection", 0) != 0;
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void setOnNITZTime(Handler h, int what, Object obj) {
        super.setOnNITZTime(h, what, obj);
        if (this.mLastNITZTimeInfo != null) {
            this.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult((Object) null, this.mLastNITZTimeInfo, (Throwable) null));
        }
    }

    private void addRequest(RILRequest rr) {
        acquireWakeLock(rr, 0);
        synchronized (this.mRequestList) {
            rr.mStartTimeMs = SystemClock.elapsedRealtime();
            this.mRequestList.append(rr.mSerial, rr);
            checkFirstRequest();
        }
    }

    private RILRequest obtainRequest(int request, Message result, WorkSource workSource) {
        RILRequest rr = RILRequest.obtain(request, result, workSource);
        addRequest(rr);
        return rr;
    }

    private void handleRadioProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        riljLoge(caller + " failed");
        resetProxyAndRequestList();
    }

    private static String convertNullToEmptyString(String string) {
        return string != null ? string : PhoneConfigurationManager.SSSS;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIccCardStatus(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(1, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getIccCardStatus(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getIccCardStatus", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIccSlotsStatus(Message result) {
        if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setLogicalToPhysicalSlotMapping(int[] physicalSlots, Message result) {
        if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin(String pin, Message result) {
        supplyIccPinForApp(pin, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPinForApp(String pin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPinForApp(rr.mSerial, convertNullToEmptyString(pin), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "supplyIccPinForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk(String puk, String newPin, Message result) {
        supplyIccPukForApp(puk, newPin, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPukForApp(String puk, String newPin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(3, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPukForApp(rr.mSerial, convertNullToEmptyString(puk), convertNullToEmptyString(newPin), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "supplyIccPukForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin2(String pin, Message result) {
        supplyIccPin2ForApp(pin, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPin2ForApp(String pin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(4, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPin2ForApp(rr.mSerial, convertNullToEmptyString(pin), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "supplyIccPin2ForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk2(String puk2, String newPin2, Message result) {
        supplyIccPuk2ForApp(puk2, newPin2, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyIccPuk2ForApp(String puk, String newPin2, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(5, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.supplyIccPuk2ForApp(rr.mSerial, convertNullToEmptyString(puk), convertNullToEmptyString(newPin2), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "supplyIccPuk2ForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin(String oldPin, String newPin, Message result) {
        changeIccPinForApp(oldPin, newPin, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPinForApp(String oldPin, String newPin, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(6, result, this.mRILDefaultWorkSource);
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("> ");
            sb.append(requestToString(rr.mRequest));
            sb.append(" oldPin = ");
            String str = PhoneConfigurationManager.SSSS;
            sb.append(oldPin != null ? oldPin.replaceAll("\\d", CharacterSets.MIMENAME_ANY_CHARSET) : str);
            sb.append(" newPin = ");
            if (newPin != null) {
                str = newPin.replaceAll("\\d", CharacterSets.MIMENAME_ANY_CHARSET);
            }
            sb.append(str);
            sb.append(" aid = ");
            sb.append(aid);
            riljLog(sb.toString());
            try {
                radioProxy.changeIccPinForApp(rr.mSerial, convertNullToEmptyString(oldPin), convertNullToEmptyString(newPin), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "changeIccPinForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
        changeIccPin2ForApp(oldPin2, newPin2, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(7, result, this.mRILDefaultWorkSource);
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("> ");
            sb.append(requestToString(rr.mRequest));
            sb.append(" oldPin = ");
            String str = PhoneConfigurationManager.SSSS;
            sb.append(oldPin2 != null ? oldPin2.replaceAll("\\d", CharacterSets.MIMENAME_ANY_CHARSET) : str);
            sb.append(" newPin = ");
            if (newPin2 != null) {
                str = newPin2.replaceAll("\\d", CharacterSets.MIMENAME_ANY_CHARSET);
            }
            sb.append(str);
            sb.append(" aid = ");
            sb.append(aid);
            riljLog(sb.toString());
            try {
                radioProxy.changeIccPin2ForApp(rr.mSerial, convertNullToEmptyString(oldPin2), convertNullToEmptyString(newPin2), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "changeIccPin2ForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void supplyNetworkDepersonalization(String netpin, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(8, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " netpin = " + netpin);
            try {
                radioProxy.supplyNetworkDepersonalization(rr.mSerial, convertNullToEmptyString(netpin));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "supplyNetworkDepersonalization", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCurrentCalls(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(9, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCurrentCalls(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCurrentCalls", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void dial(String address, boolean isEmergencyCall, EmergencyNumber emergencyNumberInfo, boolean hasKnownUserIntentEmergency, int clirMode, Message result) {
        dial(address, isEmergencyCall, emergencyNumberInfo, hasKnownUserIntentEmergency, clirMode, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void enableModem(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (!this.mRadioVersion.less(RADIO_HAL_VERSION_1_3)) {
            android.hardware.radio.V1_3.IRadio radioProxy13 = (android.hardware.radio.V1_3.IRadio) radioProxy;
            if (radioProxy13 != null) {
                RILRequest rr = obtainRequest(146, result, this.mRILDefaultWorkSource);
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
                try {
                    radioProxy13.enableModem(rr.mSerial, enable);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "enableModem", e);
                }
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getModemStatus(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (!this.mRadioVersion.less(RADIO_HAL_VERSION_1_3)) {
            android.hardware.radio.V1_3.IRadio radioProxy13 = (android.hardware.radio.V1_3.IRadio) radioProxy;
            if (radioProxy13 != null) {
                RILRequest rr = obtainRequest(147, result, this.mRILDefaultWorkSource);
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    radioProxy13.getModemStackStatus(rr.mSerial);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "getModemStatus", e);
                }
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void dial(String address, boolean isEmergencyCall, EmergencyNumber emergencyNumberInfo, boolean hasKnownUserIntentEmergency, int clirMode, UUSInfo uusInfo, Message result) {
        if (!isEmergencyCall || !this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4) || emergencyNumberInfo == null) {
            IRadio radioProxy = getRadioProxy(result);
            if (radioProxy != null) {
                RILRequest rr = obtainRequest(10, result, this.mRILDefaultWorkSource);
                Dial dialInfo = new Dial();
                dialInfo.address = convertNullToEmptyString(address);
                dialInfo.clir = clirMode;
                if (uusInfo != null) {
                    UusInfo info = new UusInfo();
                    info.uusType = uusInfo.getType();
                    info.uusDcs = uusInfo.getDcs();
                    info.uusData = new String(uusInfo.getUserData());
                    dialInfo.uusInfo.add(info);
                }
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    radioProxy.dial(rr.mSerial, dialInfo);
                    HwTelephonyFactory.getHwChrServiceManager().reportCallException("telephony", this.mPhoneId.intValue(), 0, "AP_FLOW_SUC");
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "dial", e);
                }
            }
        } else {
            emergencyDial(address, emergencyNumberInfo, hasKnownUserIntentEmergency, clirMode, uusInfo, result);
        }
    }

    private void emergencyDial(String address, EmergencyNumber emergencyNumberInfo, boolean hasKnownUserIntentEmergency, int clirMode, UUSInfo uusInfo, Message result) {
        ArrayList arrayList;
        IRadio radioProxy = getRadioProxy(result);
        android.hardware.radio.V1_4.IRadio radioProxy14 = (android.hardware.radio.V1_4.IRadio) radioProxy;
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(205, result, this.mRILDefaultWorkSource);
            Dial dialInfo = new Dial();
            dialInfo.address = convertNullToEmptyString(address);
            dialInfo.clir = clirMode;
            if (uusInfo != null) {
                UusInfo info = new UusInfo();
                info.uusType = uusInfo.getType();
                info.uusDcs = uusInfo.getDcs();
                info.uusData = new String(uusInfo.getUserData());
                dialInfo.uusInfo.add(info);
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                int i = rr.mSerial;
                int emergencyServiceCategoryBitmaskInternalDial = emergencyNumberInfo.getEmergencyServiceCategoryBitmaskInternalDial();
                if (emergencyNumberInfo.getEmergencyUrns() != null) {
                    arrayList = new ArrayList(emergencyNumberInfo.getEmergencyUrns());
                } else {
                    arrayList = new ArrayList();
                }
                radioProxy14.emergencyDial(i, dialInfo, emergencyServiceCategoryBitmaskInternalDial, arrayList, emergencyNumberInfo.getEmergencyCallRouting(), hasKnownUserIntentEmergency, emergencyNumberInfo.getEmergencyNumberSourceBitmask() == 32);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "emergencyDial", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMSI(Message result) {
        getIMSIForApp(null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMSIForApp(String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(11, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " aid = " + aid);
            try {
                radioProxy.getImsiForApp(rr.mSerial, convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getIMSIForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void hangupConnection(int gsmIndex, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(12, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " gsmIndex = " + gsmIndex);
            try {
                radioProxy.hangup(rr.mSerial, gsmIndex);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "hangupConnection", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    @UnsupportedAppUsage
    public void hangupWaitingOrBackground(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(13, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.hangupWaitingOrBackground(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "hangupWaitingOrBackground", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    @UnsupportedAppUsage
    public void hangupForegroundResumeBackground(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(14, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.hangupForegroundResumeBackground(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "hangupForegroundResumeBackground", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void switchWaitingOrHoldingAndActive(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(15, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.switchWaitingOrHoldingAndActive(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "switchWaitingOrHoldingAndActive", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void conference(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(16, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.conference(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "conference", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void rejectCall(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(17, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.rejectCall(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "rejectCall", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getLastCallFailCause(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(18, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getLastCallFailCause(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getLastCallFailCause", e);
            }
        }
    }

    @Override // com.android.internal.telephony.AbstractRIL, com.android.internal.telephony.CommandsInterface
    public void getSignalStrength(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(19, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                try {
                    ((android.hardware.radio.V1_4.IRadio) radioProxy).getSignalStrength_1_4(rr.mSerial);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "getSignalStrength_1_4", e);
                }
            } else {
                try {
                    radioProxy.getSignalStrength(rr.mSerial);
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "getSignalStrength", e2);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getVoiceRegistrationState(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(20, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getVoiceRegistrationState(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getVoiceRegistrationState", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDataRegistrationState(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(21, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getDataRegistrationState(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getDataRegistrationState", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getOperator(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(22, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getOperator(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getOperator", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    @UnsupportedAppUsage
    public void setRadioPower(boolean on, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            if (ServiceStateTracker.ISDEMO) {
                on = false;
            }
            setShouldReportRoamingPlusInfo(on);
            RILRequest rr = obtainRequest(23, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " on = " + on);
            try {
                radioProxy.setRadioPower(rr.mSerial, on);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setRadioPower", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendDtmf(char c, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(24, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                int i = rr.mSerial;
                radioProxy.sendDtmf(i, c + PhoneConfigurationManager.SSSS);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendDtmf", e);
            }
        }
    }

    private GsmSmsMessage constructGsmSendSmsRilRequest(String smscPdu, String pdu) {
        GsmSmsMessage msg = new GsmSmsMessage();
        String str = PhoneConfigurationManager.SSSS;
        msg.smscPdu = smscPdu == null ? str : smscPdu;
        if (pdu != null) {
            str = pdu;
        }
        msg.pdu = str;
        return msg;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendSMS(String smscPdu, String pdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(25, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.sendSms(rr.mSerial, constructGsmSendSmsRilRequest(smscPdu, pdu));
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 1, 1);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendSMS", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendSMSExpectMore(String smscPdu, String pdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(26, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.sendSMSExpectMore(rr.mSerial, constructGsmSendSmsRilRequest(smscPdu, pdu));
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 1, 1);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendSMSExpectMore", e);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0040 A[RETURN] */
    private static int convertToHalMvnoType(String mvnoType) {
        char c;
        int hashCode = mvnoType.hashCode();
        if (hashCode != 102338) {
            if (hashCode != 114097) {
                if (hashCode == 3236474 && mvnoType.equals("imsi")) {
                    c = 0;
                    if (c != 0) {
                        return 1;
                    }
                    if (c == 1) {
                        return 2;
                    }
                    if (c != 2) {
                        return 0;
                    }
                    return 3;
                }
            } else if (mvnoType.equals("spn")) {
                c = 2;
                if (c != 0) {
                }
            }
        } else if (mvnoType.equals("gid")) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    private static DataProfileInfo convertToHalDataProfile10(DataProfile dp) {
        DataProfileInfo dpi = new DataProfileInfo();
        dpi.profileId = dp.getProfileId();
        dpi.apn = dp.getApn();
        dpi.protocol = ApnSetting.getProtocolStringFromInt(dp.getProtocolType());
        dpi.roamingProtocol = ApnSetting.getProtocolStringFromInt(dp.getRoamingProtocolType());
        dpi.authType = dp.getAuthType();
        dpi.user = dp.getUserName();
        dpi.password = dp.getPassword();
        dpi.type = dp.getType();
        dpi.maxConnsTime = dp.getMaxConnectionsTime();
        dpi.maxConns = dp.getMaxConnections();
        dpi.waitTime = dp.getWaitTime();
        dpi.enabled = dp.isEnabled();
        dpi.supportedApnTypesBitmap = dp.getSupportedApnTypesBitmask();
        dpi.bearerBitmap = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(dp.getBearerBitmask()) << 1;
        dpi.mtu = dp.getMtu();
        dpi.mvnoType = 0;
        dpi.mvnoMatchData = PhoneConfigurationManager.SSSS;
        return dpi;
    }

    private static android.hardware.radio.V1_4.DataProfileInfo convertToHalDataProfile14(DataProfile dp) {
        android.hardware.radio.V1_4.DataProfileInfo dpi = new android.hardware.radio.V1_4.DataProfileInfo();
        dpi.apn = dp.getApn();
        dpi.protocol = dp.getProtocolType();
        dpi.roamingProtocol = dp.getRoamingProtocolType();
        dpi.authType = dp.getAuthType();
        dpi.user = dp.getUserName();
        dpi.password = dp.getPassword();
        dpi.type = dp.getType();
        dpi.maxConnsTime = dp.getMaxConnectionsTime();
        dpi.maxConns = dp.getMaxConnections();
        dpi.waitTime = dp.getWaitTime();
        dpi.enabled = dp.isEnabled();
        dpi.supportedApnTypesBitmap = dp.getSupportedApnTypesBitmask();
        dpi.bearerBitmap = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(dp.getBearerBitmask()) << 1;
        dpi.mtu = dp.getMtu();
        dpi.persistent = dp.isPersistent();
        dpi.preferred = dp.isPreferred();
        dpi.profileId = dpi.persistent ? dp.getProfileId() : -1;
        return dpi;
    }

    private static int convertToHalResetNvType(int resetType) {
        if (resetType == 1) {
            return 0;
        }
        if (resetType == 2) {
            return 1;
        }
        if (resetType != 3) {
            return -1;
        }
        return 2;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, Message result) {
        RILRequest rr;
        Exception e;
        int dataRat;
        android.hardware.radio.V1_4.IRadio radioProxy14;
        android.hardware.radio.V1_4.DataProfileInfo dpi;
        StringBuilder sb;
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr2 = obtainRequest(27, result, this.mRILDefaultWorkSource);
            ArrayList<String> addresses = new ArrayList<>();
            ArrayList<String> dnses = new ArrayList<>();
            if (linkProperties != null) {
                for (InetAddress address : linkProperties.getAddresses()) {
                    addresses.add(address.getHostAddress());
                }
                for (InetAddress dns : linkProperties.getDnsServers()) {
                    dnses.add(dns.getHostAddress());
                }
            }
            try {
                if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                    try {
                        radioProxy14 = (android.hardware.radio.V1_4.IRadio) radioProxy;
                        try {
                            dpi = convertToHalDataProfile14(dataProfile);
                            sb = new StringBuilder();
                            sb.append((String) rr2.serialString());
                            sb.append("> ");
                            sb.append(requestToString(rr2.mRequest));
                            sb.append(",accessNetworkType=");
                        } catch (RemoteException | RuntimeException e2) {
                            e = e2;
                            rr = rr2;
                            handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                        }
                    } catch (RemoteException | RuntimeException e3) {
                        e = e3;
                        rr = rr2;
                        handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                    }
                    try {
                        sb.append(accessNetworkType);
                        sb.append(",isRoaming=");
                        sb.append(isRoaming);
                        sb.append(",allowRoaming=");
                        sb.append(allowRoaming);
                        sb.append(",");
                        sb.append(dataProfile);
                        sb.append(",addresses=");
                        sb.append(addresses);
                        sb.append(",dnses=");
                        sb.append(dnses);
                        riljLog(sb.toString());
                        try {
                            radioProxy14.setupDataCall_1_4(rr2.mSerial, accessNetworkType, dpi, allowRoaming, reason, addresses, dnses);
                        } catch (RemoteException | RuntimeException e4) {
                            e = e4;
                            rr = rr2;
                            handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                        }
                    } catch (RemoteException | RuntimeException e5) {
                        e = e5;
                        rr = rr2;
                        handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                    }
                } else {
                    try {
                        if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_2)) {
                            try {
                                android.hardware.radio.V1_2.IRadio radioProxy12 = (android.hardware.radio.V1_2.IRadio) radioProxy;
                                DataProfileInfo dpi2 = convertToHalDataProfile10(dataProfile);
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append((String) rr2.serialString());
                                sb2.append("> ");
                                sb2.append(requestToString(rr2.mRequest));
                                sb2.append(",accessNetworkType=");
                                sb2.append(AccessNetworkConstants.AccessNetworkType.toString(accessNetworkType));
                                sb2.append(",isRoaming=");
                                sb2.append(isRoaming);
                                sb2.append(",allowRoaming=");
                                sb2.append(allowRoaming);
                                sb2.append(",");
                                sb2.append(dataProfile);
                                sb2.append(",addresses=");
                                try {
                                    sb2.append(addresses);
                                    sb2.append(",dnses=");
                                    sb2.append(dnses);
                                    riljLog(sb2.toString());
                                    try {
                                        radioProxy12.setupDataCall_1_2(rr2.mSerial, accessNetworkType, dpi2, dataProfile.isPersistent(), allowRoaming, isRoaming, reason, addresses, dnses);
                                    } catch (RemoteException | RuntimeException e6) {
                                        e = e6;
                                        rr = rr2;
                                        handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                                    }
                                } catch (RemoteException | RuntimeException e7) {
                                    e = e7;
                                    rr = rr2;
                                    handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                                }
                            } catch (RemoteException | RuntimeException e8) {
                                e = e8;
                                rr = rr2;
                                handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                            }
                        } else {
                            DataProfileInfo dpi3 = convertToHalDataProfile10(dataProfile);
                            Phone phone = PhoneFactory.getPhone(this.mPhoneId.intValue());
                            if (phone != null) {
                                try {
                                    ServiceState ss = phone.getServiceState();
                                    if (ss != null) {
                                        dataRat = ss.getRilDataRadioTechnology();
                                        StringBuilder sb3 = new StringBuilder();
                                        sb3.append(rr2.serialString());
                                        sb3.append("> ");
                                        rr = rr2;
                                        try {
                                            sb3.append(requestToString(rr.mRequest));
                                            sb3.append(",dataRat=");
                                            sb3.append(dataRat);
                                            sb3.append(",isRoaming=");
                                            sb3.append(isRoaming);
                                            sb3.append(",allowRoaming=");
                                            sb3.append(allowRoaming);
                                            sb3.append(",");
                                            sb3.append(dataProfile);
                                            riljLog(sb3.toString());
                                            radioProxy.setupDataCall(rr.mSerial, dataRat, dpi3, dataProfile.isPersistent(), allowRoaming, isRoaming);
                                        } catch (RemoteException | RuntimeException e9) {
                                            e = e9;
                                        }
                                    }
                                } catch (RemoteException | RuntimeException e10) {
                                    e = e10;
                                    rr = rr2;
                                    handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                                }
                            }
                            dataRat = 0;
                            try {
                                StringBuilder sb32 = new StringBuilder();
                                sb32.append(rr2.serialString());
                                sb32.append("> ");
                                rr = rr2;
                                sb32.append(requestToString(rr.mRequest));
                                sb32.append(",dataRat=");
                                sb32.append(dataRat);
                                sb32.append(",isRoaming=");
                                sb32.append(isRoaming);
                                sb32.append(",allowRoaming=");
                                sb32.append(allowRoaming);
                                sb32.append(",");
                                sb32.append(dataProfile);
                                riljLog(sb32.toString());
                                radioProxy.setupDataCall(rr.mSerial, dataRat, dpi3, dataProfile.isPersistent(), allowRoaming, isRoaming);
                            } catch (RemoteException | RuntimeException e11) {
                                e = e11;
                                rr = rr2;
                                handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                            }
                        }
                    } catch (RemoteException | RuntimeException e12) {
                        e = e12;
                        rr = rr2;
                        handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
                    }
                }
            } catch (RemoteException | RuntimeException e13) {
                e = e13;
                rr = rr2;
                handleRadioProxyExceptionForRR(rr, "setupDataCall", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccIO(int command, int fileId, String path, int p1, int p2, int p3, String data, String pin2, Message result) {
        iccIOForApp(command, fileId, path, p1, p2, p3, data, pin2, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccIOForApp(int command, int fileId, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(28, result, this.mRILDefaultWorkSource);
            if (Build.IS_DEBUGGABLE) {
                riljLog(rr.serialString() + "> iccIO: " + requestToString(rr.mRequest) + " command = 0x" + Integer.toHexString(command) + " fileId = 0x" + Integer.toHexString(fileId) + " path = " + path + " p1 = " + p1 + " p2 = " + p2 + " p3 =  data = " + data + " aid = " + aid);
            } else {
                riljLog(rr.serialString() + "> iccIO: " + requestToString(rr.mRequest));
            }
            IccIo iccIo = new IccIo();
            iccIo.command = command;
            iccIo.fileId = fileId;
            iccIo.path = convertNullToEmptyString(path);
            iccIo.p1 = p1;
            iccIo.p2 = p2;
            iccIo.p3 = p3;
            iccIo.data = convertNullToEmptyString(data);
            iccIo.pin2 = convertNullToEmptyString(pin2);
            iccIo.aid = convertNullToEmptyString(aid);
            try {
                radioProxy.iccIOForApp(rr.mSerial, iccIo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "iccIOForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendUSSD(String ussd, Message result) {
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhoneId.intValue(), 0, "AP_FLOW_SUC");
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(29, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ussd = *******");
            try {
                radioProxy.sendUssd(rr.mSerial, convertNullToEmptyString(ussd));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendUSSD", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void cancelPendingUssd(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(30, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.cancelPendingUssd(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "cancelPendingUssd", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCLIR(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(31, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getClir(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCLIR", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCLIR(int clirMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(32, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " clirMode = " + clirMode);
            try {
                radioProxy.setClir(rr.mSerial, clirMode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCLIR", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(33, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cfreason = " + cfReason + " serviceClass = " + serviceClass);
            CallForwardInfo cfInfo = new CallForwardInfo();
            cfInfo.reason = cfReason;
            cfInfo.serviceClass = serviceClass;
            cfInfo.toa = PhoneNumberUtils.toaFromString(number);
            cfInfo.number = convertNullToEmptyString(number);
            cfInfo.timeSeconds = 0;
            try {
                radioProxy.getCallForwardStatus(rr.mSerial, cfInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCallForwardStatus", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(34, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " action = " + action + " cfReason = " + cfReason + " serviceClass = " + serviceClass + " timeSeconds = " + timeSeconds);
            CallForwardInfo cfInfo = new CallForwardInfo();
            cfInfo.status = action;
            cfInfo.reason = cfReason;
            cfInfo.serviceClass = serviceClass;
            cfInfo.toa = PhoneNumberUtils.toaFromString(number);
            cfInfo.number = convertNullToEmptyString(number);
            cfInfo.timeSeconds = timeSeconds;
            try {
                radioProxy.setCallForward(rr.mSerial, cfInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCallForward", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCallWaiting(int serviceClass, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(35, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " serviceClass = " + serviceClass);
            try {
                radioProxy.getCallWaiting(rr.mSerial, serviceClass);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCallWaiting", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCallWaiting(boolean enable, int serviceClass, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(36, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable + " serviceClass = " + serviceClass);
            try {
                radioProxy.setCallWaiting(rr.mSerial, enable, serviceClass);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCallWaiting", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(37, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success + " cause = " + cause);
            try {
                radioProxy.acknowledgeLastIncomingGsmSms(rr.mSerial, success, cause);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "acknowledgeLastIncomingGsmSms", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acceptCall(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(40, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.acceptCall(rr.mSerial);
                HwTelephonyFactory.getHwChrServiceManager().reportCallException("telephony", this.mPhoneId.intValue(), 2, "AP_FLOW_SUC");
                this.mMetrics.writeRilAnswer(this.mPhoneId.intValue(), rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "acceptCall", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deactivateDataCall(int cid, int reason, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(41, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cid = " + cid + " reason = " + reason);
            try {
                if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_2)) {
                    ((android.hardware.radio.V1_2.IRadio) radioProxy).deactivateDataCall_1_2(rr.mSerial, cid, reason);
                } else {
                    radioProxy.deactivateDataCall(rr.mSerial, cid, reason == 2);
                }
                this.mMetrics.writeRilDeactivateDataCall(this.mPhoneId.intValue(), rr.mSerial, cid, reason);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "deactivateDataCall", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryFacilityLock(String facility, String password, int serviceClass, Message result) {
        queryFacilityLockForApp(facility, password, serviceClass, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(42, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " facility = " + facility + " serviceClass = " + serviceClass + " appId = " + appId);
            try {
                radioProxy.getFacilityLockForApp(rr.mSerial, convertNullToEmptyString(facility), convertNullToEmptyString(password), serviceClass, convertNullToEmptyString(appId));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getFacilityLockForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message result) {
        setFacilityLockForApp(facility, lockState, password, serviceClass, null, result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(43, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " facility = " + facility + " lockstate = " + lockState + " serviceClass = " + serviceClass + " appId = " + appId);
            try {
                radioProxy.setFacilityLockForApp(rr.mSerial, convertNullToEmptyString(facility), lockState, convertNullToEmptyString(password), serviceClass, convertNullToEmptyString(appId));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setFacilityLockForApp", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(44, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "facility = " + facility);
            try {
                radioProxy.setBarringPassword(rr.mSerial, convertNullToEmptyString(facility), convertNullToEmptyString(oldPwd), convertNullToEmptyString(newPwd));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "changeBarringPassword", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getNetworkSelectionMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(45, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getNetworkSelectionMode(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getNetworkSelectionMode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setNetworkSelectionModeAutomatic(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(46, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setNetworkSelectionModeAutomatic(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setNetworkSelectionModeAutomatic", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setNetworkSelectionModeManual(String operatorNumeric, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(47, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " operatorNumeric = " + operatorNumeric);
            try {
                radioProxy.setNetworkSelectionModeManual(rr.mSerial, convertNullToEmptyString(operatorNumeric));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setNetworkSelectionModeManual", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getAvailableNetworks(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(48, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getAvailableNetworks(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getAvailableNetworks", e);
            }
        }
    }

    private RadioAccessSpecifier convertRadioAccessSpecifierToRadioHAL(android.telephony.RadioAccessSpecifier ras) {
        List<Integer> bands;
        RadioAccessSpecifier rasInHalFormat = new RadioAccessSpecifier();
        rasInHalFormat.radioAccessNetwork = ras.getRadioAccessNetwork();
        int radioAccessNetwork = ras.getRadioAccessNetwork();
        if (radioAccessNetwork == 1) {
            bands = rasInHalFormat.geranBands;
        } else if (radioAccessNetwork == 2) {
            bands = rasInHalFormat.utranBands;
        } else if (radioAccessNetwork != 3) {
            Log.wtf(RILJ_LOG_TAG, "radioAccessNetwork " + ras.getRadioAccessNetwork() + " not supported!");
            return null;
        } else {
            bands = rasInHalFormat.eutranBands;
        }
        if (ras.getBands() != null) {
            for (int band : ras.getBands()) {
                bands.add(Integer.valueOf(band));
            }
        }
        if (ras.getChannels() != null) {
            for (int channel : ras.getChannels()) {
                rasInHalFormat.channels.add(Integer.valueOf(channel));
            }
        }
        return rasInHalFormat;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startNetworkScan(NetworkScanRequest nsr, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            int i = 0;
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_2)) {
                android.hardware.radio.V1_2.NetworkScanRequest request = new android.hardware.radio.V1_2.NetworkScanRequest();
                request.type = nsr.getScanType();
                request.interval = nsr.getSearchPeriodicity();
                request.maxSearchTime = nsr.getMaxSearchTime();
                request.incrementalResultsPeriodicity = nsr.getIncrementalResultsPeriodicity();
                request.incrementalResults = nsr.getIncrementalResults();
                android.telephony.RadioAccessSpecifier[] specifiers = nsr.getSpecifiers();
                int length = specifiers.length;
                while (i < length) {
                    RadioAccessSpecifier rasInHalFormat = convertRadioAccessSpecifierToRadioHAL(specifiers[i]);
                    if (rasInHalFormat != null) {
                        request.specifiers.add(rasInHalFormat);
                        i++;
                    } else {
                        return;
                    }
                }
                request.mccMncs.addAll(nsr.getPlmns());
                RILRequest rr = obtainRequest(142, result, this.mRILDefaultWorkSource);
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                        ((android.hardware.radio.V1_4.IRadio) radioProxy).startNetworkScan_1_4(rr.mSerial, request);
                    } else {
                        ((android.hardware.radio.V1_2.IRadio) radioProxy).startNetworkScan_1_2(rr.mSerial, request);
                    }
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "startNetworkScan", e);
                }
            } else if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_1)) {
                android.hardware.radio.V1_1.IRadio radioProxy11 = (android.hardware.radio.V1_1.IRadio) radioProxy;
                android.hardware.radio.V1_1.NetworkScanRequest request2 = new android.hardware.radio.V1_1.NetworkScanRequest();
                request2.type = nsr.getScanType();
                request2.interval = nsr.getSearchPeriodicity();
                android.telephony.RadioAccessSpecifier[] specifiers2 = nsr.getSpecifiers();
                int length2 = specifiers2.length;
                while (i < length2) {
                    RadioAccessSpecifier rasInHalFormat2 = convertRadioAccessSpecifierToRadioHAL(specifiers2[i]);
                    if (rasInHalFormat2 != null) {
                        request2.specifiers.add(rasInHalFormat2);
                        i++;
                    } else {
                        return;
                    }
                }
                RILRequest rr2 = obtainRequest(142, result, this.mRILDefaultWorkSource);
                riljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest));
                try {
                    radioProxy11.startNetworkScan(rr2.mSerial, request2);
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr2, "startNetworkScan", e2);
                }
            } else if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
                result.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopNetworkScan(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy == null) {
            return;
        }
        if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_1)) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = (android.hardware.radio.V1_1.IRadio) radioProxy;
            RILRequest rr = obtainRequest(143, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy11.stopNetworkScan(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "stopNetworkScan", e);
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startDtmf(char c, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(49, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                int i = rr.mSerial;
                radioProxy.startDtmf(i, c + PhoneConfigurationManager.SSSS);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "startDtmf", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopDtmf(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(50, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.stopDtmf(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "stopDtmf", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void separateConnection(int gsmIndex, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(52, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " gsmIndex = " + gsmIndex);
            try {
                radioProxy.separateConnection(rr.mSerial, gsmIndex);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "separateConnection", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getBasebandVersion(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(51, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getBasebandVersion(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getBasebandVersion", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setMute(boolean enableMute, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(53, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enableMute = " + enableMute);
            try {
                radioProxy.setMute(rr.mSerial, enableMute);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setMute", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getMute(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(54, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getMute(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getMute", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCLIP(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(55, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getClip(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCLIP", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    @Deprecated
    public void getPDPContextList(Message result) {
        getDataCallList(result);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDataCallList(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(57, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getDataCallList(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getDataCallList", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    @UnsupportedAppUsage
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        vendor.huawei.hardware.radio.deprecated.V1_0.IOemHook oemHookProxy = getHwOemHookProxy(response);
        if (oemHookProxy != null) {
            RILRequest rr = obtainRequest(59, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "[" + IccUtils.bytesToHexString(data) + "]");
            try {
                oemHookProxy.sendRequestRaw(rr.mSerial, primitiveArrayToArrayList(data));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "invokeOemRilRequestRaw", e);
            }
        } else {
            riljLog("Radio Oem Hook Service is disabled for P and later devices. ");
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void invokeOemRilRequestStrings(String[] strings, Message result) {
        vendor.huawei.hardware.radio.deprecated.V1_0.IOemHook oemHookProxy = getHwOemHookProxy(result);
        if (oemHookProxy != null) {
            RILRequest rr = obtainRequest(60, result, this.mRILDefaultWorkSource);
            String logStr = PhoneConfigurationManager.SSSS;
            for (int i = 0; i < strings.length; i++) {
                logStr = logStr + strings[i] + " ";
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " strings = " + logStr);
            try {
                oemHookProxy.sendRequestStrings(rr.mSerial, new ArrayList(Arrays.asList(strings)));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "invokeOemRilRequestStrings", e);
            }
        } else {
            riljLog("Radio Oem Hook Service is disabled for P and later devices. ");
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSuppServiceNotifications(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(62, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                radioProxy.setSuppServiceNotifications(rr.mSerial, enable);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setSuppServiceNotifications", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void writeSmsToSim(int status, String smsc, String pdu, Message result) {
        int status2 = translateStatus(status);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(63, result, this.mRILDefaultWorkSource);
            SmsWriteArgs args = new SmsWriteArgs();
            args.status = status2;
            args.smsc = convertNullToEmptyString(smsc);
            args.pdu = convertNullToEmptyString(pdu);
            try {
                radioProxy.writeSmsToSim(rr.mSerial, args);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "writeSmsToSim", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deleteSmsOnSim(int index, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(64, result, this.mRILDefaultWorkSource);
            try {
                radioProxy.deleteSmsOnSim(rr.mSerial, index);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "deleteSmsOnSim", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setBandMode(int bandMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(65, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " bandMode = " + bandMode);
            try {
                radioProxy.setBandMode(rr.mSerial, bandMode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setBandMode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryAvailableBandMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(66, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getAvailableBandModes(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryAvailableBandMode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendEnvelope(String contents, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(69, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " contents = " + contents);
            try {
                radioProxy.sendEnvelope(rr.mSerial, convertNullToEmptyString(contents));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendEnvelope", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendTerminalResponse(String contents, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(70, result, this.mRILDefaultWorkSource);
            if (Log.HWINFO) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " contents = ****");
            }
            try {
                radioProxy.sendTerminalResponseToSim(rr.mSerial, convertNullToEmptyString(contents));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendTerminalResponse", e);
            }
        }
    }

    private String censoredTerminalResponse(String terminalResponse) {
        try {
            byte[] bytes = IccUtils.hexStringToBytes(terminalResponse);
            if (bytes == null) {
                return terminalResponse;
            }
            int from = 0;
            for (ComprehensionTlv ctlv : ComprehensionTlv.decodeMany(bytes, 0)) {
                if (ComprehensionTlvTag.TEXT_STRING.value() == ctlv.getTag()) {
                    terminalResponse = terminalResponse.toLowerCase().replace(IccUtils.bytesToHexString(Arrays.copyOfRange(ctlv.getRawValue(), from, ctlv.getValueIndex() + ctlv.getLength())).toLowerCase(), "********");
                }
                from = ctlv.getValueIndex() + ctlv.getLength();
            }
            return terminalResponse;
        } catch (Exception e) {
            Rlog.e(RILJ_LOG_TAG, "Could not censor the terminal response.");
            return null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendEnvelopeWithStatus(String contents, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(107, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " contents = " + contents);
            try {
                radioProxy.sendEnvelopeWithStatus(rr.mSerial, convertNullToEmptyString(contents));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendEnvelopeWithStatus", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void explicitCallTransfer(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(72, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.explicitCallTransfer(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "explicitCallTransfer", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setPreferredNetworkType(int networkType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(73, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " networkType = " + networkType);
            this.mPreferredNetworkType = networkType;
            custSetModemProperties();
            this.mMetrics.writeSetPreferredNetworkType(this.mPhoneId.intValue(), networkType);
            if (this.mRadioVersion.lessOrEqual(RADIO_HAL_VERSION_1_3)) {
                try {
                    radioProxy.setPreferredNetworkType(rr.mSerial, networkType);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "setPreferredNetworkType", e);
                }
            } else if (this.mRadioVersion.equals(RADIO_HAL_VERSION_1_4)) {
                try {
                    ((android.hardware.radio.V1_4.IRadio) radioProxy).setPreferredNetworkTypeBitmap(rr.mSerial, convertToHalRadioAccessFamily(RadioAccessFamily.getRafFromNetworkType(networkType)));
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "setPreferredNetworkTypeBitmap", e2);
                }
            }
        }
    }

    public static int convertToNetworkTypeBitMask(int raf) {
        int networkTypeRaf = 0;
        if ((65536 & raf) != 0) {
            networkTypeRaf = (int) (((long) 0) | 32768);
        }
        if ((raf & 2) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 1);
        }
        if ((raf & 4) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 2);
        }
        if ((raf & 16) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 8);
        }
        if ((raf & 32) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 8);
        }
        if ((raf & 64) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 64);
        }
        if ((raf & 128) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 16);
        }
        if ((raf & ApnSettingHelper.TYPE_IA) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 32);
        }
        if ((raf & 4096) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 2048);
        }
        if ((raf & 8192) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 8192);
        }
        if ((raf & ApnSettingHelper.TYPE_MCX) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 256);
        }
        if ((raf & ApnSettingHelper.TYPE_EMERGENCY) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 128);
        }
        if ((raf & TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_TRAT_SWAP_FAILED) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 512);
        }
        if ((32768 & raf) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 16384);
        }
        if ((raf & 8) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 4);
        }
        if ((131072 & raf) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 65536);
        }
        if ((raf & PlmnActRecord.ACCESS_TECH_EUTRAN) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 4096);
        }
        if ((524288 & raf) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 262144);
        }
        if ((1048576 & raf) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 524288);
        }
        if ((262144 & raf) != 0) {
            networkTypeRaf = (int) (((long) networkTypeRaf) | 131072);
        }
        if (networkTypeRaf == 0) {
            return 0;
        }
        return networkTypeRaf;
    }

    private static int convertToHalRadioAccessFamily(int networkTypeBitmask) {
        int raf = 0;
        if ((((long) networkTypeBitmask) & 32768) != 0) {
            raf = 0 | 65536;
        }
        if ((((long) networkTypeBitmask) & 1) != 0) {
            raf |= 2;
        }
        if ((((long) networkTypeBitmask) & 2) != 0) {
            raf |= 4;
        }
        if ((((long) networkTypeBitmask) & 8) != 0) {
            raf |= 16;
        }
        if ((((long) networkTypeBitmask) & 64) != 0) {
            raf |= 64;
        }
        if ((((long) networkTypeBitmask) & 16) != 0) {
            raf |= 128;
        }
        if ((((long) networkTypeBitmask) & 32) != 0) {
            raf |= ApnSettingHelper.TYPE_IA;
        }
        if ((((long) networkTypeBitmask) & 2048) != 0) {
            raf |= 4096;
        }
        if ((((long) networkTypeBitmask) & 8192) != 0) {
            raf |= 8192;
        }
        if ((((long) networkTypeBitmask) & 256) != 0) {
            raf |= ApnSettingHelper.TYPE_MCX;
        }
        if ((((long) networkTypeBitmask) & 128) != 0) {
            raf |= ApnSettingHelper.TYPE_EMERGENCY;
        }
        if ((((long) networkTypeBitmask) & 512) != 0) {
            raf |= TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_TRAT_SWAP_FAILED;
        }
        if ((((long) networkTypeBitmask) & 16384) != 0) {
            raf |= 32768;
        }
        if ((((long) networkTypeBitmask) & 4) != 0) {
            raf |= 8;
        }
        if ((((long) networkTypeBitmask) & 65536) != 0) {
            raf |= ApnSettingHelper.TYPE_BIP2;
        }
        if ((((long) networkTypeBitmask) & 4096) != 0) {
            raf |= PlmnActRecord.ACCESS_TECH_EUTRAN;
        }
        if ((((long) networkTypeBitmask) & 262144) != 0) {
            raf |= 524288;
        }
        if ((((long) networkTypeBitmask) & 524288) != 0) {
            raf |= ApnSettingHelper.TYPE_BIP5;
        }
        if (raf == 0) {
            return 1;
        }
        return raf;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getPreferredNetworkType(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(74, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            if (this.mRadioVersion.lessOrEqual(RADIO_HAL_VERSION_1_3)) {
                try {
                    radioProxy.getPreferredNetworkType(rr.mSerial);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "getPreferredNetworkType", e);
                }
            } else if (this.mRadioVersion.equals(RADIO_HAL_VERSION_1_4)) {
                try {
                    ((android.hardware.radio.V1_4.IRadio) radioProxy).getPreferredNetworkTypeBitmap(rr.mSerial);
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "getPreferredNetworkTypeBitmap", e2);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setLocationUpdates(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(76, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                radioProxy.setLocationUpdates(rr.mSerial, enable);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setLocationUpdates", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaSubscriptionSource(int cdmaSubscription, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(77, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cdmaSubscription = " + cdmaSubscription);
            try {
                radioProxy.setCdmaSubscriptionSource(rr.mSerial, cdmaSubscription);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCdmaSubscriptionSource", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryCdmaRoamingPreference(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(79, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCdmaRoamingPreference(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCdmaRoamingPreference", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaRoamingPreference(int cdmaRoamingType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(78, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cdmaRoamingType = " + cdmaRoamingType);
            try {
                radioProxy.setCdmaRoamingPreference(rr.mSerial, cdmaRoamingType);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCdmaRoamingPreference", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void queryTTYMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(81, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getTTYMode(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryTTYMode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setTTYMode(int ttyMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(80, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ttyMode = " + ttyMode);
            try {
                radioProxy.setTTYMode(rr.mSerial, ttyMode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setTTYMode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setPreferredVoicePrivacy(boolean enable, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(82, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                radioProxy.setPreferredVoicePrivacy(rr.mSerial, enable);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setPreferredVoicePrivacy", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getPreferredVoicePrivacy(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(83, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getPreferredVoicePrivacy(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getPreferredVoicePrivacy", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendCDMAFeatureCode(String featureCode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(84, result, this.mRILDefaultWorkSource);
            if (featureCode != null) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + featureCode.replaceAll("\\d{4}$", "****"));
            } else {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }
            try {
                radioProxy.sendCDMAFeatureCode(rr.mSerial, convertNullToEmptyString(featureCode));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendCDMAFeatureCode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(85, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " dtmfString = * on = " + on + " off = " + off);
            try {
                radioProxy.sendBurstDtmf(rr.mSerial, convertNullToEmptyString(dtmfString), on, off);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendBurstDtmf", e);
            }
        }
    }

    private void constructCdmaSendSmsRilRequest(CdmaSmsMessage msg, byte[] pdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        try {
            msg.teleserviceId = dis.readInt();
            boolean z = false;
            msg.isServicePresent = ((byte) dis.readInt()) == 1;
            msg.serviceCategory = dis.readInt();
            msg.address.digitMode = dis.read();
            msg.address.numberMode = dis.read();
            msg.address.numberType = dis.read();
            msg.address.numberPlan = dis.read();
            int addrNbrOfDigits = (byte) dis.read();
            for (int i = 0; i < addrNbrOfDigits; i++) {
                msg.address.digits.add(Byte.valueOf(dis.readByte()));
            }
            msg.subAddress.subaddressType = dis.read();
            CdmaSmsSubaddress cdmaSmsSubaddress = msg.subAddress;
            if (((byte) dis.read()) == 1) {
                z = true;
            }
            cdmaSmsSubaddress.odd = z;
            int subaddrNbrOfDigits = (byte) dis.read();
            for (int i2 = 0; i2 < subaddrNbrOfDigits; i2++) {
                msg.subAddress.digits.add(Byte.valueOf(dis.readByte()));
            }
            int bearerDataLength = dis.read();
            for (int i3 = 0; i3 < bearerDataLength; i3++) {
                msg.bearerData.add(Byte.valueOf(dis.readByte()));
            }
        } catch (IOException e) {
            riljLog("sendSmsCdma: conversion from input stream to object failed.");
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendCdmaSms(byte[] pdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(87, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            CdmaSmsMessage msg = new CdmaSmsMessage();
            constructCdmaSendSmsRilRequest(msg, pdu);
            try {
                radioProxy.sendCdmaSms(rr.mSerial, msg);
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 2, 2);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendCdmaSms", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(88, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success + " cause = " + cause);
            CdmaSmsAck msg = new CdmaSmsAck();
            msg.errorClass = !success ? 1 : 0;
            msg.smsCauseCode = cause;
            try {
                radioProxy.acknowledgeLastIncomingCdmaSms(rr.mSerial, msg);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "acknowledgeLastIncomingCdmaSms", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getGsmBroadcastConfig(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(89, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getGsmBroadcastConfig(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getGsmBroadcastConfig", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(90, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + config.length + " configs : ");
            for (SmsBroadcastConfigInfo smsBroadcastConfigInfo : config) {
                riljLog(smsBroadcastConfigInfo.toString());
            }
            ArrayList<GsmBroadcastSmsConfigInfo> configs = new ArrayList<>();
            int numOfConfig = config.length;
            for (int i = 0; i < numOfConfig; i++) {
                GsmBroadcastSmsConfigInfo info = new GsmBroadcastSmsConfigInfo();
                info.fromServiceId = config[i].getFromServiceId();
                info.toServiceId = config[i].getToServiceId();
                info.fromCodeScheme = config[i].getFromCodeScheme();
                info.toCodeScheme = config[i].getToCodeScheme();
                info.selected = config[i].isSelected();
                configs.add(info);
            }
            try {
                radioProxy.setGsmBroadcastConfig(rr.mSerial, configs);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setGsmBroadcastConfig", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setGsmBroadcastActivation(boolean activate, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(91, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " activate = " + activate);
            try {
                radioProxy.setGsmBroadcastActivation(rr.mSerial, activate);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setGsmBroadcastActivation", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCdmaBroadcastConfig(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(92, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCdmaBroadcastConfig(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCdmaBroadcastConfig", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(93, result, this.mRILDefaultWorkSource);
            ArrayList<CdmaBroadcastSmsConfigInfo> halConfigs = new ArrayList<>();
            for (CdmaSmsBroadcastConfigInfo config : configs) {
                for (int i = config.getFromServiceCategory(); i <= config.getToServiceCategory(); i++) {
                    CdmaBroadcastSmsConfigInfo info = new CdmaBroadcastSmsConfigInfo();
                    info.serviceCategory = i;
                    info.language = config.getLanguage();
                    info.selected = config.isSelected();
                    halConfigs.add(info);
                }
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + halConfigs.size() + " configs : ");
            Iterator<CdmaBroadcastSmsConfigInfo> it = halConfigs.iterator();
            while (it.hasNext()) {
                riljLog(it.next().toString());
            }
            try {
                radioProxy.setCdmaBroadcastConfig(rr.mSerial, halConfigs);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCdmaBroadcastConfig", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCdmaBroadcastActivation(boolean activate, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(94, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " activate = " + activate);
            try {
                radioProxy.setCdmaBroadcastActivation(rr.mSerial, activate);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCdmaBroadcastActivation", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCDMASubscription(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(95, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCDMASubscription(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCDMASubscription", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void writeSmsToRuim(int status, String pdu, Message result) {
        int status2 = translateStatus(status);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(96, result, this.mRILDefaultWorkSource);
            CdmaSmsWriteArgs args = new CdmaSmsWriteArgs();
            args.status = status2;
            writeContent(args.message, pdu);
            try {
                radioProxy.writeSmsToRuim(rr.mSerial, args);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "writeSmsToRuim", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void deleteSmsOnRuim(int index, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(97, result, this.mRILDefaultWorkSource);
            try {
                radioProxy.deleteSmsOnRuim(rr.mSerial, index);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "deleteSmsOnRuim", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getDeviceIdentity(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(98, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getDeviceIdentity(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getDeviceIdentity", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void exitEmergencyCallbackMode(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(99, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.exitEmergencyCallbackMode(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "exitEmergencyCallbackMode", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getSmscAddress(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(100, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSmscAddress(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getSmscAddress", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSmscAddress(String address, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(101, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " address = ***");
            try {
                radioProxy.setSmscAddress(rr.mSerial, convertNullToEmptyString(address));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setSmscAddress", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void reportSmsMemoryStatus(boolean available, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(CallFailCause.RECOVERY_ON_TIMER_EXPIRY, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " available = " + available);
            try {
                radioProxy.reportSmsMemoryStatus(rr.mSerial, available);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "reportSmsMemoryStatus", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void reportStkServiceIsRunning(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(103, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.reportStkServiceIsRunning(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "reportStkServiceIsRunning", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCdmaSubscriptionSource(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(104, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCdmaSubscriptionSource(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCdmaSubscriptionSource", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(106, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success);
            try {
                radioProxy.acknowledgeIncomingGsmSmsWithPdu(rr.mSerial, success, convertNullToEmptyString(ackPdu));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "acknowledgeIncomingGsmSmsWithPdu", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getVoiceRadioTechnology(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(108, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getVoiceRadioTechnology(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getVoiceRadioTechnology", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getCellInfoList(Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(PhoneSwitcher.EVENT_PRECISE_CALL_STATE_CHANGED, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCellInfoList(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCellInfoList", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCellInfoListRate(int rateInMillis, Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(110, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " rateInMillis = " + rateInMillis);
            try {
                radioProxy.setCellInfoListRate(rr.mSerial, rateInMillis);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCellInfoListRate", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(111, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + dataProfile);
            try {
                if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                    ((android.hardware.radio.V1_4.IRadio) radioProxy).setInitialAttachApn_1_4(rr.mSerial, convertToHalDataProfile14(dataProfile));
                } else {
                    radioProxy.setInitialAttachApn(rr.mSerial, convertToHalDataProfile10(dataProfile), dataProfile.isPersistent(), isRoaming);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setInitialAttachApn", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getImsRegistrationState(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(112, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getImsRegistrationState(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getImsRegistrationState", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendImsGsmSms(String smscPdu, String pdu, int retry, int messageRef, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(113, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ImsSmsMessage msg = new ImsSmsMessage();
            msg.tech = 1;
            msg.retry = ((byte) retry) >= 1;
            msg.messageRef = messageRef;
            msg.gsmMessage.add(constructGsmSendSmsRilRequest(smscPdu, pdu));
            try {
                radioProxy.sendImsSms(rr.mSerial, msg);
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 3, 1);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendImsGsmSms", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(113, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ImsSmsMessage msg = new ImsSmsMessage();
            msg.tech = 2;
            boolean z = true;
            if (((byte) retry) < 1) {
                z = false;
            }
            msg.retry = z;
            msg.messageRef = messageRef;
            CdmaSmsMessage cdmaMsg = new CdmaSmsMessage();
            constructCdmaSendSmsRilRequest(cdmaMsg, pdu);
            msg.cdmaMessage.add(cdmaMsg);
            try {
                radioProxy.sendImsSms(rr.mSerial, msg);
                this.mMetrics.writeRilSendSms(this.mPhoneId.intValue(), rr.mSerial, 3, 2);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendImsCdmaSms", e);
            }
        }
    }

    private SimApdu createSimApdu(int channel, int cla, int instruction, int p1, int p2, int p3, String data) {
        SimApdu msg = new SimApdu();
        msg.sessionId = channel;
        msg.cla = cla;
        msg.instruction = instruction;
        msg.p1 = p1;
        msg.p2 = p2;
        msg.p3 = p3;
        msg.data = convertNullToEmptyString(data);
        return msg;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(114, result, this.mRILDefaultWorkSource);
            if (Build.IS_DEBUGGABLE) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + String.format(" cla = 0x%02X ins = 0x%02X", Integer.valueOf(cla), Integer.valueOf(instruction)) + String.format(" p1 = 0x%02X p2 = 0x%02X p3 = 0x%02X", Integer.valueOf(p1), Integer.valueOf(p2), Integer.valueOf(p3)) + " data = " + data);
            } else {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }
            try {
                radioProxy.iccTransmitApduBasicChannel(rr.mSerial, createSimApdu(0, cla, instruction, p1, p2, p3, data));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "iccTransmitApduBasicChannel", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccOpenLogicalChannel(String aid, int p2, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(115, result, this.mRILDefaultWorkSource);
            if (Build.IS_DEBUGGABLE) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid + " p2 = " + p2);
            } else {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }
            try {
                radioProxy.iccOpenLogicalChannel(rr.mSerial, convertNullToEmptyString(aid), p2);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "iccOpenLogicalChannel", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccCloseLogicalChannel(int channel, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(116, result, this.mRILDefaultWorkSource);
            if (Log.HWINFO) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " channel = " + channel);
            }
            try {
                radioProxy.iccCloseLogicalChannel(rr.mSerial, channel);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "iccCloseLogicalChannel", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message result) {
        if (channel <= 0) {
            riljLoge("Invalid channel in iccTransmitApduLogicalChannel: " + channel);
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, new CommandException(CommandException.Error.INVALID_PARAMETER));
                result.sendToTarget();
                return;
            }
            return;
        }
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(117, result, this.mRILDefaultWorkSource);
            if (Build.IS_DEBUGGABLE) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + String.format(" channel = %d", Integer.valueOf(channel)) + String.format(" cla = 0x%02X ins = 0x%02X", Integer.valueOf(cla), Integer.valueOf(instruction)) + String.format(" p1 = 0x%02X p2 = 0x%02X p3 = 0x%02X", Integer.valueOf(p1), Integer.valueOf(p2), Integer.valueOf(p3)) + " data = " + data);
            } else {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }
            try {
                radioProxy.iccTransmitApduLogicalChannel(rr.mSerial, createSimApdu(channel, cla, instruction, p1, p2, p3, data));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "iccTransmitApduLogicalChannel", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void nvReadItem(int itemID, Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(118, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " itemId = " + itemID);
            try {
                radioProxy.nvReadItem(rr.mSerial, itemID);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "nvReadItem", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void nvWriteItem(int itemId, String itemValue, Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " itemId = " + itemId + " itemValue = " + itemValue);
            NvWriteItem item = new NvWriteItem();
            item.itemId = itemId;
            item.value = convertNullToEmptyString(itemValue);
            try {
                radioProxy.nvWriteItem(rr.mSerial, item);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "nvWriteItem", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(120, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " PreferredRoamingList = 0x" + IccUtils.bytesToHexString(preferredRoamingList));
            ArrayList<Byte> arrList = new ArrayList<>();
            for (byte b : preferredRoamingList) {
                arrList.add(Byte.valueOf(b));
            }
            try {
                radioProxy.nvWriteCdmaPrl(rr.mSerial, arrList);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "nvWriteCdmaPrl", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void nvResetConfig(int resetType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " resetType = " + resetType);
            try {
                radioProxy.nvResetConfig(rr.mSerial, convertToHalResetNvType(resetType));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "nvResetConfig", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " slot = " + slotId + " appIndex = " + appIndex + " subId = " + subId + " subStatus = " + subStatus);
            SelectUiccSub info = new SelectUiccSub();
            info.slot = slotId;
            info.appIndex = appIndex;
            info.subType = subId;
            info.actStatus = subStatus;
            try {
                radioProxy.setUiccSubscription(rr.mSerial, info);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setUiccSubscription", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void setDataAllowed(boolean allowed, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_DNS_ADDR, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " allowed = " + allowed);
            try {
                radioProxy.setDataAllowed(rr.mSerial, allowed);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setDataAllowed", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getHardwareConfig(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_PCSCF_OR_DNS_ADDRESS, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getHardwareConfig(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getHardwareConfig", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void requestIccSimAuthentication(int authContext, String data, String aid, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(125, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.requestIccSimAuthentication(rr.mSerial, authContext, convertNullToEmptyString(data), convertNullToEmptyString(aid));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "requestIccSimAuthentication", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setDataProfile(DataProfile[] dps, boolean isRoaming, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            int i = 0;
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                try {
                    android.hardware.radio.V1_4.IRadio radioProxy14 = (android.hardware.radio.V1_4.IRadio) radioProxy;
                    RILRequest rr = obtainRequest(128, result, this.mRILDefaultWorkSource);
                    ArrayList<android.hardware.radio.V1_4.DataProfileInfo> dpis = new ArrayList<>();
                    for (DataProfile dp : dps) {
                        dpis.add(convertToHalDataProfile14(dp));
                    }
                    riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with data profiles : ");
                    int length = dps.length;
                    while (i < length) {
                        riljLog(dps[i].toString());
                        i++;
                    }
                    radioProxy14.setDataProfile_1_4(rr.mSerial, dpis);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(null, "setDataProfile", e);
                }
            } else {
                ArrayList<DataProfileInfo> dpis2 = new ArrayList<>();
                for (DataProfile dp2 : dps) {
                    if (dp2.isPersistent() || HuaweiTelephonyConfigs.isMTKPlatform()) {
                        dpis2.add(convertToHalDataProfile10(dp2));
                    }
                }
                if (!dpis2.isEmpty()) {
                    RILRequest rr2 = obtainRequest(128, result, this.mRILDefaultWorkSource);
                    riljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest) + " with data profiles : ");
                    int length2 = dps.length;
                    while (i < length2) {
                        riljLog(dps[i].toString());
                        i++;
                    }
                    radioProxy.setDataProfile(rr2.mSerial, dpis2, isRoaming);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void requestShutdown(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(129, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.requestShutdown(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "requestShutdown", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void getRadioCapability(Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(130, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getRadioCapability(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getRadioCapability", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void setRadioCapability(RadioCapability rc, Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(131, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " RadioCapability = " + rc.toString());
            RadioCapability halRc = new RadioCapability();
            halRc.session = rc.getSession();
            halRc.phase = rc.getPhase();
            halRc.raf = rc.getRadioAccessFamily();
            halRc.logicalModemUuid = convertNullToEmptyString(rc.getLogicalModemUuid());
            halRc.status = rc.getStatus();
            try {
                radioProxy.setRadioCapability(rr.mSerial, halRc);
            } catch (Exception e) {
                handleRadioProxyExceptionForRR(rr, "setRadioCapability", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (!this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_2) && radioProxy != null) {
            RILRequest rr = obtainRequest(132, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " reportIntervalMs = " + reportIntervalMs + " pullMode = " + pullMode);
            try {
                radioProxy.startLceService(rr.mSerial, reportIntervalMs, pullMode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "startLceService", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void stopLceService(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (!this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_2) && radioProxy != null) {
            RILRequest rr = obtainRequest(133, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.stopLceService(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "stopLceService", e);
            }
        }
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    @Deprecated
    public void pullLceData(Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(134, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.pullLceData(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "pullLceData", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getModemActivityInfo(Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(135, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getModemActivityInfo(rr.mSerial);
                Message msg = this.mRilHandler.obtainMessage(5);
                msg.obj = null;
                msg.arg1 = rr.mSerial;
                this.mRilHandler.sendMessageDelayed(msg, 2000);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getModemActivityInfo", e);
            }
        }
    }

    @VisibleForTesting
    public static ArrayList<Carrier> createCarrierRestrictionList(List<CarrierIdentifier> carriers) {
        ArrayList<Carrier> result = new ArrayList<>();
        for (CarrierIdentifier ci : carriers) {
            Carrier c = new Carrier();
            c.mcc = convertNullToEmptyString(ci.getMcc());
            c.mnc = convertNullToEmptyString(ci.getMnc());
            int matchType = 0;
            String matchData = null;
            if (!TextUtils.isEmpty(ci.getSpn())) {
                matchType = 1;
                matchData = ci.getSpn();
            } else if (!TextUtils.isEmpty(ci.getImsi())) {
                matchType = 2;
                matchData = ci.getImsi();
            } else if (!TextUtils.isEmpty(ci.getGid1())) {
                matchType = 3;
                matchData = ci.getGid1();
            } else if (!TextUtils.isEmpty(ci.getGid2())) {
                matchType = 4;
                matchData = ci.getGid2();
            }
            c.matchType = matchType;
            c.matchData = convertNullToEmptyString(matchData);
            result.add(c);
        }
        return result;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setAllowedCarriers(CarrierRestrictionRules carrierRestrictionRules, Message result, WorkSource workSource) {
        riljLog("RIL.java - setAllowedCarriers");
        Preconditions.checkNotNull(carrierRestrictionRules, "Carrier restriction cannot be null.");
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(136, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " params: " + carrierRestrictionRules);
            int policy = 0;
            boolean supported = true;
            if (carrierRestrictionRules.getMultiSimPolicy() == 1) {
                policy = 1;
            }
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                riljLog("RIL.java - Using IRadio 1.4 or greater");
                android.hardware.radio.V1_4.IRadio radioProxy14 = (android.hardware.radio.V1_4.IRadio) radioProxy;
                CarrierRestrictionsWithPriority carrierRestrictions = new CarrierRestrictionsWithPriority();
                carrierRestrictions.allowedCarriers = createCarrierRestrictionList(carrierRestrictionRules.getAllowedCarriers());
                carrierRestrictions.excludedCarriers = createCarrierRestrictionList(carrierRestrictionRules.getExcludedCarriers());
                if (carrierRestrictionRules.getDefaultCarrierRestriction() != 0) {
                    supported = false;
                }
                carrierRestrictions.allowedCarriersPrioritized = supported;
                try {
                    radioProxy14.setAllowedCarriers_1_4(rr.mSerial, carrierRestrictions, policy);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "setAllowedCarriers_1_4", e);
                }
            } else {
                boolean isAllCarriersAllowed = carrierRestrictionRules.isAllCarriersAllowed();
                if (!(isAllCarriersAllowed || (carrierRestrictionRules.getExcludedCarriers().isEmpty() && carrierRestrictionRules.getDefaultCarrierRestriction() == 0)) || policy != 0) {
                    supported = false;
                }
                if (!supported) {
                    riljLoge("setAllowedCarriers does not support excluded list on IRadio version less than 1.4");
                    if (result != null) {
                        AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
                        result.sendToTarget();
                        return;
                    }
                    return;
                }
                riljLog("RIL.java - Using IRadio 1.3 or lower");
                CarrierRestrictions carrierRestrictions2 = new CarrierRestrictions();
                carrierRestrictions2.allowedCarriers = createCarrierRestrictionList(carrierRestrictionRules.getAllowedCarriers());
                try {
                    radioProxy.setAllowedCarriers(rr.mSerial, isAllCarriersAllowed, carrierRestrictions2);
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "setAllowedCarriers", e2);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getAllowedCarriers(Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(137, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_4)) {
                riljLog("RIL.java - Using IRadio 1.4 or greater");
                try {
                    ((android.hardware.radio.V1_4.IRadio) radioProxy).getAllowedCarriers_1_4(rr.mSerial);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "getAllowedCarriers_1_4", e);
                }
            } else {
                riljLog("RIL.java - Using IRadio 1.3 or lower");
                try {
                    radioProxy.getAllowedCarriers(rr.mSerial);
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "getAllowedCarriers", e2);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void sendDeviceState(int stateType, boolean state, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(138, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + stateType + ":" + state);
            try {
                radioProxy.sendDeviceState(rr.mSerial, stateType, state);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendDeviceState", e);
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setUnsolResponseFilter(int filter, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(139, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + filter);
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_2)) {
                try {
                    ((android.hardware.radio.V1_2.IRadio) radioProxy).setIndicationFilter_1_2(rr.mSerial, filter);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "setIndicationFilter_1_2", e);
                }
            } else {
                try {
                    radioProxy.setIndicationFilter(rr.mSerial, filter & 7);
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "setIndicationFilter", e2);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSignalStrengthReportingCriteria(int hysteresisMs, int hysteresisDb, int[] thresholdsDbm, int ran, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy == null) {
            return;
        }
        if (this.mRadioVersion.less(RADIO_HAL_VERSION_1_2)) {
            riljLoge("setSignalStrengthReportingCriteria ignored on IRadio version less than 1.2");
            return;
        }
        RILRequest rr = obtainRequest(202, result, this.mRILDefaultWorkSource);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        try {
            ((android.hardware.radio.V1_2.IRadio) radioProxy).setSignalStrengthReportingCriteria(rr.mSerial, hysteresisMs, hysteresisDb, primitiveArrayToArrayList(thresholdsDbm), convertRanToHalRan(ran));
        } catch (RemoteException | RuntimeException e) {
            handleRadioProxyExceptionForRR(rr, "setSignalStrengthReportingCriteria", e);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setLinkCapacityReportingCriteria(int hysteresisMs, int hysteresisDlKbps, int hysteresisUlKbps, int[] thresholdsDlKbps, int[] thresholdsUlKbps, int ran, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy == null) {
            return;
        }
        if (this.mRadioVersion.less(RADIO_HAL_VERSION_1_2)) {
            riljLoge("setLinkCapacityReportingCriteria ignored on IRadio version less than 1.2");
            return;
        }
        RILRequest rr = obtainRequest(203, result, this.mRILDefaultWorkSource);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        try {
            ((android.hardware.radio.V1_2.IRadio) radioProxy).setLinkCapacityReportingCriteria(rr.mSerial, hysteresisMs, hysteresisDlKbps, hysteresisUlKbps, primitiveArrayToArrayList(thresholdsDlKbps), primitiveArrayToArrayList(thresholdsUlKbps), convertRanToHalRan(ran));
        } catch (RemoteException | RuntimeException e) {
            handleRadioProxyExceptionForRR(rr, "setLinkCapacityReportingCriteria", e);
        }
    }

    private static int convertRanToHalRan(int radioAccessNetwork) {
        if (radioAccessNetwork == 1) {
            return 1;
        }
        if (radioAccessNetwork == 2) {
            return 2;
        }
        if (radioAccessNetwork == 3) {
            return 3;
        }
        if (radioAccessNetwork != 4) {
            return radioAccessNetwork != 5 ? 0 : 5;
        }
        return 4;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setSimCardPower(int state, Message result, WorkSource workSource) {
        WorkSource workSource2 = getDeafultWorkSourceIfInvalid(workSource);
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(140, result, workSource2);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + state);
            if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_1)) {
                try {
                    ((android.hardware.radio.V1_1.IRadio) radioProxy).setSimCardPower_1_1(rr.mSerial, state);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "setSimCardPower", e);
                }
            } else if (state == 0) {
                radioProxy.setSimCardPower(rr.mSerial, false);
            } else if (state == 1) {
                radioProxy.setSimCardPower(rr.mSerial, true);
            } else if (result != null) {
                try {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
                    result.sendToTarget();
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "setSimCardPower", e2);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo, Message result) {
        Preconditions.checkNotNull(imsiEncryptionInfo, "ImsiEncryptionInfo cannot be null.");
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy == null) {
            return;
        }
        if (this.mRadioVersion.greaterOrEqual(RADIO_HAL_VERSION_1_1)) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = (android.hardware.radio.V1_1.IRadio) radioProxy;
            RILRequest rr = obtainRequest(141, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                android.hardware.radio.V1_1.ImsiEncryptionInfo halImsiInfo = new android.hardware.radio.V1_1.ImsiEncryptionInfo();
                halImsiInfo.mnc = imsiEncryptionInfo.getMnc();
                halImsiInfo.mcc = imsiEncryptionInfo.getMcc();
                halImsiInfo.keyIdentifier = imsiEncryptionInfo.getKeyIdentifier();
                if (imsiEncryptionInfo.getExpirationTime() != null) {
                    halImsiInfo.expirationTime = imsiEncryptionInfo.getExpirationTime().getTime();
                }
                for (byte b : imsiEncryptionInfo.getPublicKey().getEncoded()) {
                    halImsiInfo.carrierKey.add(new Byte(b));
                }
                radioProxy11.setCarrierInfoForImsiEncryption(rr.mSerial, halImsiInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCarrierInfoForImsiEncryption", e);
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startNattKeepalive(int contextId, KeepalivePacketData packetData, int intervalMillis, Message result) {
        Preconditions.checkNotNull(packetData, "KeepaliveRequest cannot be null.");
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy == null) {
            riljLoge("Radio Proxy object is null!");
        } else if (!this.mRadioVersion.less(RADIO_HAL_VERSION_1_1)) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = (android.hardware.radio.V1_1.IRadio) radioProxy;
            RILRequest rr = obtainRequest(144, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                KeepaliveRequest req = new KeepaliveRequest();
                req.cid = contextId;
                if (packetData.dstAddress instanceof Inet4Address) {
                    req.type = 0;
                } else if (packetData.dstAddress instanceof Inet6Address) {
                    req.type = 1;
                } else {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(44));
                    result.sendToTarget();
                    return;
                }
                appendPrimitiveArrayToArrayList(packetData.srcAddress.getAddress(), req.sourceAddress);
                req.sourcePort = packetData.srcPort;
                appendPrimitiveArrayToArrayList(packetData.dstAddress.getAddress(), req.destinationAddress);
                req.destinationPort = packetData.dstPort;
                req.maxKeepaliveIntervalMillis = intervalMillis;
                radioProxy11.startKeepalive(rr.mSerial, req);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "startNattKeepalive", e);
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopNattKeepalive(int sessionHandle, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy == null) {
            Rlog.e(RILJ_LOG_TAG, "Radio Proxy object is null!");
        } else if (!this.mRadioVersion.less(RADIO_HAL_VERSION_1_1)) {
            android.hardware.radio.V1_1.IRadio radioProxy11 = (android.hardware.radio.V1_1.IRadio) radioProxy;
            RILRequest rr = obtainRequest(145, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy11.stopKeepalive(rr.mSerial, sessionHandle);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "stopNattKeepalive", e);
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMEI(Message result) {
        throw new RuntimeException("getIMEI not expected to be called");
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getIMEISV(Message result) {
        throw new RuntimeException("getIMEISV not expected to be called");
    }

    @Override // com.android.internal.telephony.CommandsInterface
    @Deprecated
    public void getLastPdpFailCause(Message result) {
        throw new RuntimeException("getLastPdpFailCause not expected to be called");
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getLastDataCallFailCause(Message result) {
        throw new RuntimeException("getLastDataCallFailCause not expected to be called");
    }

    private int translateStatus(int status) {
        int i = status & 7;
        if (i == 1) {
            return 1;
        }
        if (i == 3) {
            return 0;
        }
        if (i == 5) {
            return 3;
        }
        if (i != 7) {
            return 1;
        }
        return 2;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void resetRadio(Message result) {
        throw new RuntimeException("resetRadio not expected to be called");
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void handleCallSetupRequestFromSim(boolean accept, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(71, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.handleStkCallSetupRequestFromSim(rr.mSerial, accept);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getAllowedCarriers", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void processIndication(int indicationType) {
        if (indicationType == 1) {
            sendAck();
            riljLog("Unsol response received; Sending ack to ril.cpp");
        }
    }

    /* access modifiers changed from: package-private */
    public void processRequestAck(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
        }
        if (rr == null) {
            Rlog.w(RILJ_LOG_TAG, "processRequestAck: Unexpected solicited ack response! serial: " + serial);
            return;
        }
        decrementWakeLock(rr);
        riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
    }

    @VisibleForTesting
    public RILRequest processResponse(RadioResponseInfo responseInfo) {
        RILRequest rr;
        int serial = responseInfo.serial;
        int error = responseInfo.error;
        int type = responseInfo.type;
        if (type == 1) {
            synchronized (this.mRequestList) {
                rr = this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(RILJ_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                RadioBugDetector radioBugDetector = this.mRadioBugDetector;
                if (radioBugDetector != null) {
                    radioBugDetector.detectRadioBug(rr.mRequest, error);
                }
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
            return rr;
        }
        RILRequest rr2 = findAndRemoveRequestFromList(serial);
        if (rr2 == null) {
            Rlog.e(RILJ_LOG_TAG, "processResponse: Unexpected response! serial: " + serial + " error: " + error);
            return null;
        }
        addToRilHistogram(rr2);
        RadioBugDetector radioBugDetector2 = this.mRadioBugDetector;
        if (radioBugDetector2 != null) {
            radioBugDetector2.detectRadioBug(rr2.mRequest, error);
        }
        if (type == 2) {
            sendAck();
            riljLog("Response received for " + rr2.serialString() + " " + requestToString(rr2.mRequest) + " Sending ack to ril.cpp");
        }
        int i = rr2.mRequest;
        if (i == 3 || i == 5) {
            if (this.mIccStatusChangedRegistrants != null) {
                riljLog("ON enter sim puk fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                this.mIccStatusChangedRegistrants.notifyRegistrants();
            }
        } else if (i == 129) {
            setRadioState(2, false);
        }
        if (error != 0) {
            int i2 = rr2.mRequest;
            if ((i2 == 2 || i2 == 4 || i2 == 43 || i2 == 6 || i2 == 7) && this.mIccStatusChangedRegistrants != null) {
                riljLog("ON some errors fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                this.mIccStatusChangedRegistrants.notifyRegistrants();
            }
        } else if (rr2.mRequest == 14 && this.mTestingEmergencyCall.getAndSet(false) && this.mEmergencyCallbackModeRegistrant != null) {
            riljLog("testing emergency call, notify ECM Registrants");
            this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
        }
        return rr2;
    }

    @VisibleForTesting
    public void processResponseDone(RILRequest rr, RadioResponseInfo responseInfo, Object ret) {
        if (responseInfo.error == 0) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret));
        } else {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " error " + responseInfo.error);
            rr.onError(responseInfo.error, ret);
        }
        this.mMetrics.writeOnRilSolicitedResponse(this.mPhoneId.intValue(), rr.mSerial, responseInfo.error, rr.mRequest, ret);
        if (responseInfo.type == 0) {
            decrementWakeLock(rr);
        }
        rr.release();
    }

    private void sendAck() {
        RILRequest rr = RILRequest.obtain(800, null, this.mRILDefaultWorkSource);
        acquireWakeLock(rr, 1);
        IRadio radioProxy = getRadioProxy(null);
        if (radioProxy != null) {
            try {
                radioProxy.responseAcknowledgement();
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendAck", e);
                riljLoge("sendAck failed.");
            }
        } else {
            Rlog.e(RILJ_LOG_TAG, "Error trying to send ack, radioProxy = null");
        }
        rr.release();
    }

    private WorkSource getDeafultWorkSourceIfInvalid(WorkSource workSource) {
        if (workSource == null) {
            return this.mRILDefaultWorkSource;
        }
        return workSource;
    }

    @UnsupportedAppUsage
    private void acquireWakeLock(RILRequest rr, int wakeLockType) {
        synchronized (rr) {
            if (rr.mWakeLockType != -1) {
                riljLog("Failed to aquire wakelock for " + rr.serialString());
                return;
            }
            if (wakeLockType == 0) {
                synchronized (this.mWakeLock) {
                    this.mWakeLock.acquire();
                    this.mWakeLockCount++;
                    this.mWlSequenceNum++;
                    if (!this.mClientWakelockTracker.isClientActive(rr.getWorkSourceClientId())) {
                        this.mActiveWakelockWorkSource.add(rr.mWorkSource);
                        this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                    }
                    this.mClientWakelockTracker.startTracking(rr.mClientId, rr.mRequest, rr.mSerial, this.mWakeLockCount);
                    Message msg = this.mRilHandler.obtainMessage(2);
                    msg.arg1 = this.mWlSequenceNum;
                    this.mRilHandler.sendMessageDelayed(msg, (long) this.mWakeLockTimeout);
                }
            } else if (wakeLockType != 1) {
                Rlog.w(RILJ_LOG_TAG, "Acquiring Invalid Wakelock type " + wakeLockType);
                return;
            } else {
                synchronized (this.mAckWakeLock) {
                    this.mAckWakeLock.acquire();
                    this.mAckWlSequenceNum++;
                    Message msg2 = this.mRilHandler.obtainMessage(4);
                    msg2.arg1 = this.mAckWlSequenceNum;
                    this.mRilHandler.sendMessageDelayed(msg2, (long) this.mAckWakeLockTimeout);
                }
            }
            rr.mWakeLockType = wakeLockType;
        }
    }

    @VisibleForTesting
    public PowerManager.WakeLock getWakeLock(int wakeLockType) {
        return wakeLockType == 0 ? this.mWakeLock : this.mAckWakeLock;
    }

    @VisibleForTesting
    public RilHandler getRilHandler() {
        return this.mRilHandler;
    }

    @VisibleForTesting
    public SparseArray<RILRequest> getRilRequestList() {
        return this.mRequestList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void decrementWakeLock(RILRequest rr) {
        synchronized (rr) {
            int i = rr.mWakeLockType;
            if (i != -1) {
                if (i == 0) {
                    synchronized (this.mWakeLock) {
                        this.mClientWakelockTracker.stopTracking(rr.mClientId, rr.mRequest, rr.mSerial, this.mWakeLockCount > 1 ? this.mWakeLockCount - 1 : 0);
                        if (!this.mClientWakelockTracker.isClientActive(rr.getWorkSourceClientId())) {
                            this.mActiveWakelockWorkSource.remove(rr.mWorkSource);
                            this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                        }
                        if (this.mWakeLockCount > 1) {
                            this.mWakeLockCount--;
                        } else {
                            this.mWakeLockCount = 0;
                            this.mWakeLock.release();
                        }
                    }
                } else if (i != 1) {
                    Rlog.w(RILJ_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
                }
            }
            rr.mWakeLockType = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean clearWakeLock(int wakeLockType) {
        if (wakeLockType == 0) {
            synchronized (this.mWakeLock) {
                if (this.mWakeLockCount == 0 && !this.mWakeLock.isHeld()) {
                    return false;
                }
                riljLog("NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
                this.mWakeLockCount = 0;
                this.mWakeLock.release();
                this.mClientWakelockTracker.stopTrackingAll();
                this.mActiveWakelockWorkSource = new WorkSource();
                return true;
            }
        }
        synchronized (this.mAckWakeLock) {
            if (!this.mAckWakeLock.isHeld()) {
                return false;
            }
            this.mAckWakeLock.release();
            return true;
        }
    }

    @UnsupportedAppUsage
    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                riljLog("clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = this.mRequestList.valueAt(i);
                if (loggable) {
                    riljLog(i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                decrementWakeLock(rr);
                rr.release();
            }
            this.mRequestList.clear();
            HwTelephonyFactory.getHwTelephonyChrManager().stopRilRequestBlockTimer(this.mPhoneId.intValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
                checkFirstRequest();
            }
        }
        return rr;
    }

    private void addToRilHistogram(RILRequest rr) {
        int totalTime = (int) (SystemClock.elapsedRealtime() - rr.mStartTimeMs);
        synchronized (mRilTimeHistograms) {
            TelephonyHistogram entry = mRilTimeHistograms.get(rr.mRequest);
            if (entry == null) {
                entry = new TelephonyHistogram(1, rr.mRequest, 5);
                mRilTimeHistograms.put(rr.mRequest, entry);
            }
            entry.addTimeTaken(totalTime);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public RadioCapability makeStaticRadioCapability() {
        int raf = 0;
        String rafString = this.mContext.getResources().getString(17039877);
        if (!TextUtils.isEmpty(rafString)) {
            raf = RadioAccessFamily.rafTypeFromString(rafString);
        }
        Integer num = this.mPhoneId;
        RadioCapability rc = new RadioCapability(num != null ? num.intValue() : 0, 0, 0, raf, PhoneConfigurationManager.SSSS, 1);
        riljLog("Faking RIL_REQUEST_GET_RADIO_CAPABILITY response using " + raf);
        return rc;
    }

    /* JADX INFO: Multiple debug info for r0v11 java.lang.String: [D('hwcfgs' java.util.ArrayList<com.android.internal.telephony.HardwareConfig>), D('s' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v14 java.lang.String: [D('cinfo' com.android.internal.telephony.CallForwardInfo[]), D('s' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v17 java.lang.String: [D('cells' java.util.ArrayList<android.telephony.NeighboringCellInfo>), D('s' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v20 java.lang.String: [D('calls' java.util.ArrayList<com.android.internal.telephony.DriverCall>), D('s' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v23 java.lang.String: [D('strings' java.lang.String[]), D('s' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v26 java.lang.String: [D('intArray' int[]), D('s' java.lang.String)] */
    @UnsupportedAppUsage
    static String retToString(int req, Object ret) {
        int i;
        if (ret == null || req == 11 || req == 95 || req == 98 || req == 115 || req == 117 || req == 529 || req == 38 || req == 39) {
            return PhoneConfigurationManager.SSSS;
        }
        if (ret instanceof int[]) {
            int[] intArray = (int[]) ret;
            int length = intArray.length;
            StringBuilder sb = new StringBuilder("{");
            if (length > 0) {
                sb.append(intArray[0]);
                for (int i2 = 0 + 1; i2 < length; i2++) {
                    sb.append(", ");
                    sb.append(intArray[i2]);
                }
            }
            sb.append("}");
            return sb.toString();
        } else if (ret instanceof String[]) {
            String[] strings = (String[]) ret;
            int length2 = strings.length;
            StringBuilder sb2 = new StringBuilder("{");
            if (length2 > 0) {
                if (req == 98) {
                    i = 0 + 1;
                    sb2.append(Rlog.pii(RILJ_LOG_TAG, strings[0]));
                } else {
                    i = 0 + 1;
                    sb2.append(strings[0]);
                }
                while (i < length2) {
                    sb2.append(", ");
                    sb2.append(strings[i]);
                    i++;
                }
            }
            sb2.append("}");
            return sb2.toString();
        } else if (req == 9) {
            StringBuilder sb3 = new StringBuilder("{");
            Iterator<DriverCall> it = ((ArrayList) ret).iterator();
            while (it.hasNext()) {
                sb3.append("[");
                sb3.append(it.next());
                sb3.append("] ");
            }
            sb3.append("}");
            return sb3.toString();
        } else if (req == 75) {
            StringBuilder sb4 = new StringBuilder("{");
            Iterator<NeighboringCellInfo> it2 = ((ArrayList) ret).iterator();
            while (it2.hasNext()) {
                sb4.append("[");
                sb4.append(it2.next());
                sb4.append("] ");
            }
            sb4.append("}");
            return sb4.toString();
        } else if (req == 33) {
            StringBuilder sb5 = new StringBuilder("{");
            for (CallForwardInfo callForwardInfo : (CallForwardInfo[]) ret) {
                sb5.append("[");
                sb5.append(callForwardInfo);
                sb5.append("] ");
            }
            sb5.append("}");
            return sb5.toString();
        } else if (req != 124) {
            return HwTelephonyFactory.getHwTelephonyBaseManager().retToStringEx(req, ret);
        } else {
            StringBuilder sb6 = new StringBuilder(" ");
            Iterator<HardwareConfig> it3 = ((ArrayList) ret).iterator();
            while (it3.hasNext()) {
                sb6.append("[");
                sb6.append(it3.next());
                sb6.append("] ");
            }
            return sb6.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeMetricsCallRing(char[] response) {
        this.mMetrics.writeRilCallRing(this.mPhoneId.intValue(), response);
    }

    /* access modifiers changed from: package-private */
    public void writeMetricsSrvcc(int state) {
        this.mMetrics.writeRilSrvcc(this.mPhoneId.intValue(), state);
    }

    /* access modifiers changed from: package-private */
    public void writeMetricsModemRestartEvent(String reason) {
        this.mMetrics.writeModemRestartEvent(this.mPhoneId.intValue(), reason);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void notifyRegistrantsRilConnectionChanged(int rilVer) {
        this.mRilVersion = rilVer;
        if (this.mRilConnectedRegistrants != null) {
            this.mRilConnectedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new Integer(rilVer), (Throwable) null));
        }
        this.mContext.sendBroadcast(new Intent(ACTION_RIL_CONNECTED));
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void notifyRegistrantsCdmaInfoRec(CdmaInformationRecords infoRec) {
        if (infoRec.record instanceof CdmaInformationRecords.CdmaDisplayInfoRec) {
            if (this.mDisplayInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mDisplayInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
            }
        } else if (infoRec.record instanceof CdmaInformationRecords.CdmaSignalInfoRec) {
            if (this.mSignalInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mSignalInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
            }
        } else if (infoRec.record instanceof CdmaInformationRecords.CdmaNumberInfoRec) {
            if (this.mNumberInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mNumberInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
            }
        } else if (infoRec.record instanceof CdmaInformationRecords.CdmaRedirectingNumberInfoRec) {
            if (this.mRedirNumInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mRedirNumInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
            }
        } else if (infoRec.record instanceof CdmaInformationRecords.CdmaLineControlInfoRec) {
            if (this.mLineControlInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mLineControlInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
            }
        } else if (infoRec.record instanceof CdmaInformationRecords.CdmaT53ClirInfoRec) {
            if (this.mT53ClirInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mT53ClirInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
            }
        } else if ((infoRec.record instanceof CdmaInformationRecords.CdmaT53AudioControlInfoRec) && this.mT53AudCntrlInfoRegistrants != null) {
            unsljLogRet(1027, infoRec.record);
            this.mT53AudCntrlInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, infoRec.record, (Throwable) null));
        }
    }

    @UnsupportedAppUsage
    static String requestToString(int request) {
        if (request == 205) {
            return "EMERGENCY_DIAL";
        }
        if (request == 800) {
            return "RIL_RESPONSE_ACKNOWLEDGEMENT";
        }
        switch (request) {
            case 1:
                return "GET_SIM_STATUS";
            case 2:
                return "ENTER_SIM_PIN";
            case 3:
                return "ENTER_SIM_PUK";
            case 4:
                return "ENTER_SIM_PIN2";
            case 5:
                return "ENTER_SIM_PUK2";
            case 6:
                return "CHANGE_SIM_PIN";
            case 7:
                return "CHANGE_SIM_PIN2";
            case 8:
                return "ENTER_NETWORK_DEPERSONALIZATION";
            case 9:
                return "GET_CURRENT_CALLS";
            case 10:
                return "DIAL";
            case 11:
                return "GET_IMSI";
            case 12:
                return "HANGUP";
            case 13:
                return "HANGUP_WAITING_OR_BACKGROUND";
            case 14:
                return "HANGUP_FOREGROUND_RESUME_BACKGROUND";
            case 15:
                return "REQUEST_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
            case 16:
                return "CONFERENCE";
            case 17:
                return "UDUB";
            case 18:
                return "LAST_CALL_FAIL_CAUSE";
            case 19:
                return "SIGNAL_STRENGTH";
            case 20:
                return "VOICE_REGISTRATION_STATE";
            case 21:
                return "DATA_REGISTRATION_STATE";
            case 22:
                return "OPERATOR";
            case 23:
                return "RADIO_POWER";
            case TelephonyProto.RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                return "DTMF";
            case 25:
                return "SEND_SMS";
            case 26:
                return "SEND_SMS_EXPECT_MORE";
            case 27:
                return "SETUP_DATA_CALL";
            case 28:
                return "SIM_IO";
            case 29:
                return "SEND_USSD";
            case 30:
                return "CANCEL_USSD";
            case 31:
                return "GET_CLIR";
            case 32:
                return "SET_CLIR";
            case 33:
                return "QUERY_CALL_FORWARD_STATUS";
            case 34:
                return "SET_CALL_FORWARD";
            case 35:
                return "QUERY_CALL_WAITING";
            case 36:
                return "SET_CALL_WAITING";
            case 37:
                return "SMS_ACKNOWLEDGE";
            case 38:
                return "GET_IMEI";
            case 39:
                return "GET_IMEISV";
            case 40:
                return "ANSWER";
            case 41:
                return "DEACTIVATE_DATA_CALL";
            case 42:
                return "QUERY_FACILITY_LOCK";
            case 43:
                return "SET_FACILITY_LOCK";
            case 44:
                return "CHANGE_BARRING_PASSWORD";
            case 45:
                return "QUERY_NETWORK_SELECTION_MODE";
            case 46:
                return "SET_NETWORK_SELECTION_AUTOMATIC";
            case 47:
                return "SET_NETWORK_SELECTION_MANUAL";
            case 48:
                return "QUERY_AVAILABLE_NETWORKS ";
            case 49:
                return "DTMF_START";
            case 50:
                return "DTMF_STOP";
            case 51:
                return "BASEBAND_VERSION";
            case 52:
                return "SEPARATE_CONNECTION";
            case 53:
                return "SET_MUTE";
            case 54:
                return "GET_MUTE";
            case 55:
                return "QUERY_CLIP";
            case 56:
                return "LAST_DATA_CALL_FAIL_CAUSE";
            case 57:
                return "DATA_CALL_LIST";
            case 58:
                return "RESET_RADIO";
            case 59:
                return "OEM_HOOK_RAW";
            case 60:
                return "OEM_HOOK_STRINGS";
            case TelephonyProto.RilErrno.RIL_E_NETWORK_NOT_READY /* 61 */:
                return "SCREEN_STATE";
            case 62:
                return "SET_SUPP_SVC_NOTIFICATION";
            case 63:
                return "WRITE_SMS_TO_SIM";
            case 64:
                return "DELETE_SMS_ON_SIM";
            case 65:
                return "SET_BAND_MODE";
            case 66:
                return "QUERY_AVAILABLE_BAND_MODE";
            case TelephonyProto.RilErrno.RIL_E_INVALID_RESPONSE /* 67 */:
                return "REQUEST_STK_GET_PROFILE";
            case CallFailCause.ACM_LIMIT_EXCEEDED /* 68 */:
                return "REQUEST_STK_SET_PROFILE";
            case CallFailCause.REQUESTED_FACILITY_NOT_IMPLEMENTED /* 69 */:
                return "REQUEST_STK_SEND_ENVELOPE_COMMAND";
            case CallFailCause.ONLY_RESTRICTED_DIGITAL_INFO_BC_AVAILABLE /* 70 */:
                return "REQUEST_STK_SEND_TERMINAL_RESPONSE";
            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_25 /* 71 */:
                return "REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM";
            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                return "REQUEST_EXPLICIT_CALL_TRANSFER";
            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_41 /* 73 */:
                return "REQUEST_SET_PREFERRED_NETWORK_TYPE";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /* 74 */:
                return "REQUEST_GET_PREFERRED_NETWORK_TYPE";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_26 /* 75 */:
                return "REQUEST_GET_NEIGHBORING_CELL_IDS";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_41 /* 76 */:
                return "REQUEST_SET_LOCATION_UPDATES";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_25 /* 77 */:
                return "RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_26 /* 78 */:
                return "RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE";
            case 79:
                return "RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE";
            case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /* 80 */:
                return "RIL_REQUEST_SET_TTY_MODE";
            case 81:
                return "RIL_REQUEST_QUERY_TTY_MODE";
            case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /* 82 */:
                return "RIL_REQUEST_CDMA_SET_PREFERRED_VOICE_PRIVACY_MODE";
            case 83:
                return "RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE";
            case 84:
                return "RIL_REQUEST_CDMA_FLASH";
            case 85:
                return "RIL_REQUEST_CDMA_BURST_DTMF";
            case 86:
                return "RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY";
            case CallFailCause.USER_NOT_MEMBER_OF_CUG /* 87 */:
                return "RIL_REQUEST_CDMA_SEND_SMS";
            case CallFailCause.INCOMPATIBLE_DESTINATION /* 88 */:
                return "RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE";
            case 89:
                return "RIL_REQUEST_GSM_GET_BROADCAST_CONFIG";
            case 90:
                return "RIL_REQUEST_GSM_SET_BROADCAST_CONFIG";
            case CallFailCause.INVALID_TRANSIT_NETWORK_SELECTION /* 91 */:
                return "RIL_REQUEST_GSM_BROADCAST_ACTIVATION";
            case 92:
                return "RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG";
            case 93:
                return "RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG";
            case 94:
                return "RIL_REQUEST_CDMA_BROADCAST_ACTIVATION";
            case 95:
                return "RIL_REQUEST_CDMA_SUBSCRIPTION";
            case 96:
                return "RIL_REQUEST_CDMA_WRITE_SMS_TO_RUIM";
            case 97:
                return "RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM";
            case 98:
                return "RIL_REQUEST_DEVICE_IDENTITY";
            case 99:
                return "REQUEST_EXIT_EMERGENCY_CALLBACK_MODE";
            case 100:
                return "RIL_REQUEST_GET_SMSC_ADDRESS";
            case 101:
                return "RIL_REQUEST_SET_SMSC_ADDRESS";
            case CallFailCause.RECOVERY_ON_TIMER_EXPIRY /* 102 */:
                return "RIL_REQUEST_REPORT_SMS_MEMORY_STATUS";
            case 103:
                return "RIL_REQUEST_REPORT_STK_SERVICE_IS_RUNNING";
            case 104:
                return "RIL_REQUEST_CDMA_GET_SUBSCRIPTION_SOURCE";
            case 105:
                return "RIL_REQUEST_ISIM_AUTHENTICATION";
            case 106:
                return "RIL_REQUEST_ACKNOWLEDGE_INCOMING_GSM_SMS_WITH_PDU";
            case 107:
                return "RIL_REQUEST_STK_SEND_ENVELOPE_WITH_STATUS";
            case 108:
                return "RIL_REQUEST_VOICE_RADIO_TECH";
            case PhoneSwitcher.EVENT_PRECISE_CALL_STATE_CHANGED /* 109 */:
                return "RIL_REQUEST_GET_CELL_INFO_LIST";
            case 110:
                return "RIL_REQUEST_SET_CELL_INFO_LIST_RATE";
            case 111:
                return "RIL_REQUEST_SET_INITIAL_ATTACH_APN";
            case 112:
                return "RIL_REQUEST_IMS_REGISTRATION_STATE";
            case 113:
                return "RIL_REQUEST_IMS_SEND_SMS";
            case 114:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_BASIC";
            case 115:
                return "RIL_REQUEST_SIM_OPEN_CHANNEL";
            case 116:
                return "RIL_REQUEST_SIM_CLOSE_CHANNEL";
            case 117:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_CHANNEL";
            case 118:
                return "RIL_REQUEST_NV_READ_ITEM";
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH /* 119 */:
                return "RIL_REQUEST_NV_WRITE_ITEM";
            case 120:
                return "RIL_REQUEST_NV_WRITE_CDMA_PRL";
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /* 121 */:
                return "RIL_REQUEST_NV_RESET_CONFIG";
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /* 122 */:
                return "RIL_REQUEST_SET_UICC_SUBSCRIPTION";
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_DNS_ADDR /* 123 */:
                return "RIL_REQUEST_ALLOW_DATA";
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_PCSCF_OR_DNS_ADDRESS /* 124 */:
                return "GET_HARDWARE_CONFIG";
            case 125:
                return "RIL_REQUEST_SIM_AUTHENTICATION";
            default:
                switch (request) {
                    case 128:
                        return "RIL_REQUEST_SET_DATA_PROFILE";
                    case 129:
                        return "RIL_REQUEST_SHUTDOWN";
                    case 130:
                        return "RIL_REQUEST_GET_RADIO_CAPABILITY";
                    case 131:
                        return "RIL_REQUEST_SET_RADIO_CAPABILITY";
                    case 132:
                        return "RIL_REQUEST_START_LCE";
                    case 133:
                        return "RIL_REQUEST_STOP_LCE";
                    case 134:
                        return "RIL_REQUEST_PULL_LCEDATA";
                    case 135:
                        return "RIL_REQUEST_GET_ACTIVITY_INFO";
                    case 136:
                        return "RIL_REQUEST_SET_ALLOWED_CARRIERS";
                    case 137:
                        return "RIL_REQUEST_GET_ALLOWED_CARRIERS";
                    case 138:
                        return "RIL_REQUEST_SEND_DEVICE_STATE";
                    case 139:
                        return "RIL_REQUEST_SET_UNSOLICITED_RESPONSE_FILTER";
                    case 140:
                        return "RIL_REQUEST_SET_SIM_CARD_POWER";
                    case 141:
                        return "RIL_REQUEST_SET_CARRIER_INFO_IMSI_ENCRYPTION";
                    case 142:
                        return "RIL_REQUEST_START_NETWORK_SCAN";
                    case 143:
                        return "RIL_REQUEST_STOP_NETWORK_SCAN";
                    case 144:
                        return "RIL_REQUEST_START_KEEPALIVE";
                    case 145:
                        return "RIL_REQUEST_STOP_KEEPALIVE";
                    case 146:
                        return "RIL_REQUEST_ENABLE_MODEM";
                    case 147:
                        return "RIL_REQUEST_GET_MODEM_STATUS";
                    default:
                        switch (request) {
                            case 200:
                                return "RIL_REQUEST_GET_SLOT_STATUS";
                            case 201:
                                return "RIL_REQUEST_SET_LOGICAL_TO_PHYSICAL_SLOT_MAPPING";
                            case 202:
                                return "RIL_REQUEST_SET_SIGNAL_STRENGTH_REPORTING_CRITERIA";
                            case 203:
                                return "RIL_REQUEST_SET_LINK_CAPACITY_REPORTING_CRITERIA";
                            default:
                                return HwTelephonyFactory.getHwTelephonyBaseManager().requestToStringEx(request);
                        }
                }
        }
    }

    @UnsupportedAppUsage
    static String responseToString(int request) {
        if (request == 1100) {
            return "RIL_UNSOL_ICC_SLOT_STATUS";
        }
        if (request == 1101) {
            return "RIL_UNSOL_PHYSICAL_CHANNEL_CONFIG";
        }
        switch (request) {
            case 1000:
                return "UNSOL_RESPONSE_RADIO_STATE_CHANGED";
            case 1001:
                return "UNSOL_RESPONSE_CALL_STATE_CHANGED";
            case 1002:
                return "UNSOL_RESPONSE_NETWORK_STATE_CHANGED";
            case 1003:
                return "UNSOL_RESPONSE_NEW_SMS";
            case 1004:
                return "UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT";
            case 1005:
                return "UNSOL_RESPONSE_NEW_SMS_ON_SIM";
            case 1006:
                return "UNSOL_ON_USSD";
            case CallFailCause.CDMA_PREEMPTED /* 1007 */:
                return "UNSOL_ON_USSD_REQUEST";
            case CallFailCause.CDMA_NOT_EMERGENCY /* 1008 */:
                return "UNSOL_NITZ_TIME_RECEIVED";
            case CallFailCause.CDMA_ACCESS_BLOCKED /* 1009 */:
                return "UNSOL_SIGNAL_STRENGTH";
            case 1010:
                return "UNSOL_DATA_CALL_LIST_CHANGED";
            case 1011:
                return "UNSOL_SUPP_SVC_NOTIFICATION";
            case 1012:
                return "UNSOL_STK_SESSION_END";
            case 1013:
                return "UNSOL_STK_PROACTIVE_COMMAND";
            case 1014:
                return "UNSOL_STK_EVENT_NOTIFY";
            case CharacterSets.UTF_16 /* 1015 */:
                return "UNSOL_STK_CALL_SETUP";
            case 1016:
                return "UNSOL_SIM_SMS_STORAGE_FULL";
            case 1017:
                return "UNSOL_SIM_REFRESH";
            case 1018:
                return "UNSOL_CALL_RING";
            case 1019:
                return "UNSOL_RESPONSE_SIM_STATUS_CHANGED";
            case 1020:
                return "UNSOL_RESPONSE_CDMA_NEW_SMS";
            case 1021:
                return "UNSOL_RESPONSE_NEW_BROADCAST_SMS";
            case 1022:
                return "UNSOL_CDMA_RUIM_SMS_STORAGE_FULL";
            case 1023:
                return "UNSOL_RESTRICTED_STATE_CHANGED";
            case ApnSettingHelper.TYPE_MCX /* 1024 */:
                return "UNSOL_ENTER_EMERGENCY_CALLBACK_MODE";
            case 1025:
                return "UNSOL_CDMA_CALL_WAITING";
            case 1026:
                return "UNSOL_CDMA_OTA_PROVISION_STATUS";
            case 1027:
                return "UNSOL_CDMA_INFO_REC";
            case 1028:
                return "UNSOL_OEM_HOOK_RAW";
            case 1029:
                return "UNSOL_RINGBACK_TONE";
            case 1030:
                return "UNSOL_RESEND_INCALL_MUTE";
            case 1031:
                return "CDMA_SUBSCRIPTION_SOURCE_CHANGED";
            case 1032:
                return "UNSOL_CDMA_PRL_CHANGED";
            case 1033:
                return "UNSOL_EXIT_EMERGENCY_CALLBACK_MODE";
            case 1034:
                return "UNSOL_RIL_CONNECTED";
            case 1035:
                return "UNSOL_VOICE_RADIO_TECH_CHANGED";
            case 1036:
                return "UNSOL_CELL_INFO_LIST";
            case 1037:
                return "UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED";
            case 1038:
                return "RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED";
            case 1039:
                return "UNSOL_SRVCC_STATE_NOTIFY";
            case 1040:
                return "RIL_UNSOL_HARDWARE_CONFIG_CHANGED";
            default:
                switch (request) {
                    case 1042:
                        return "RIL_UNSOL_RADIO_CAPABILITY";
                    case 1043:
                        return "UNSOL_ON_SS";
                    case 1044:
                        return "UNSOL_STK_CC_ALPHA_NOTIFY";
                    case 1045:
                        return "UNSOL_LCE_INFO_RECV";
                    case 1046:
                        return "UNSOL_PCO_DATA";
                    case 1047:
                        return "UNSOL_MODEM_RESTART";
                    case 1048:
                        return "RIL_UNSOL_CARRIER_INFO_IMSI_ENCRYPTION";
                    case 1049:
                        return "RIL_UNSOL_NETWORK_SCAN_RESULT";
                    case 1050:
                        return "RIL_UNSOL_KEEPALIVE_STATUS";
                    default:
                        return HwTelephonyFactory.getHwTelephonyBaseManager().responseToStringEx(request);
                }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void riljLog(String msg) {
        Rlog.i(RILJ_LOG_TAG, msg + " [SUB" + this.mPhoneId + "]");
    }

    /* access modifiers changed from: package-private */
    public void riljLoge(String msg) {
        Rlog.e(RILJ_LOG_TAG, msg + " [SUB" + this.mPhoneId + "]");
    }

    /* access modifiers changed from: package-private */
    public void riljLoge(String msg, Exception e) {
        Rlog.e(RILJ_LOG_TAG, msg + " [SUB" + this.mPhoneId + "]", e);
    }

    /* access modifiers changed from: package-private */
    public void riljLogv(String msg) {
        Rlog.v(RILJ_LOG_TAG, msg + " [SUB" + this.mPhoneId + "]");
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void unsljLog(int response) {
        riljLog("[UNSL]< " + responseToString(response));
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void unsljLogMore(int response, String more) {
        riljLog("[UNSL]< " + responseToString(response) + " " + more);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void unsljLogvRet(int response, Object ret) {
        riljLogv("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setPhoneType(int phoneType) {
        riljLog("setPhoneType=" + phoneType + " old value=" + this.mPhoneType);
        this.mPhoneType = phoneType;
    }

    @Override // com.android.internal.telephony.BaseCommands, com.android.internal.telephony.CommandsInterface
    public void testingEmergencyCall() {
        riljLog("testingEmergencyCall");
        this.mTestingEmergencyCall.set(true);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RIL: " + this);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mWakeLockTimeout=" + this.mWakeLockTimeout);
        synchronized (this.mRequestList) {
            synchronized (this.mWakeLock) {
                pw.println(" mWakeLockCount=" + this.mWakeLockCount);
            }
            int count = this.mRequestList.size();
            pw.println(" mRequestList count=" + count);
            for (int i = 0; i < count; i++) {
                RILRequest rr = this.mRequestList.valueAt(i);
                pw.println("  [" + rr.mSerial + "] " + requestToString(rr.mRequest));
            }
        }
        pw.println(" mLastNITZTimeInfo=" + Arrays.toString(this.mLastNITZTimeInfo));
        pw.println(" mTestingEmergencyCall=" + this.mTestingEmergencyCall.get());
        this.mClientWakelockTracker.dumpClientRequestTracker(pw);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public List<ClientRequestStats> getClientRequestStats() {
        return this.mClientWakelockTracker.getClientRequestStats();
    }

    public static void appendPrimitiveArrayToArrayList(byte[] src, ArrayList<Byte> dst) {
        for (byte b : src) {
            dst.add(Byte.valueOf(b));
        }
    }

    public static ArrayList<Byte> primitiveArrayToArrayList(byte[] arr) {
        ArrayList<Byte> arrayList = new ArrayList<>(arr.length);
        for (byte b : arr) {
            arrayList.add(Byte.valueOf(b));
        }
        return arrayList;
    }

    public static ArrayList<Integer> primitiveArrayToArrayList(int[] arr) {
        ArrayList<Integer> arrayList = new ArrayList<>(arr.length);
        for (int i : arr) {
            arrayList.add(Integer.valueOf(i));
        }
        return arrayList;
    }

    public static byte[] arrayListToPrimitiveArray(ArrayList<Byte> bytes) {
        byte[] ret = new byte[bytes.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bytes.get(i).byteValue();
        }
        return ret;
    }

    static ArrayList<HardwareConfig> convertHalHwConfigList(ArrayList<HardwareConfig> hwListRil, RIL ril) {
        HardwareConfig hw;
        ArrayList<HardwareConfig> response = new ArrayList<>(hwListRil.size());
        Iterator<HardwareConfig> it = hwListRil.iterator();
        while (it.hasNext()) {
            HardwareConfig hwRil = it.next();
            int type = hwRil.type;
            if (type == 0) {
                hw = new HardwareConfig(type);
                HardwareConfigModem hwModem = (HardwareConfigModem) hwRil.modem.get(0);
                hw.assignModem(hwRil.uuid, hwRil.state, hwModem.rilModel, hwModem.rat, hwModem.maxVoice, hwModem.maxData, hwModem.maxStandby);
            } else if (type == 1) {
                hw = new HardwareConfig(type);
                hw.assignSim(hwRil.uuid, hwRil.state, ((HardwareConfigSim) hwRil.sim.get(0)).modemUuid);
            } else {
                throw new RuntimeException("RIL_REQUEST_GET_HARDWARE_CONFIG invalid hardward type:" + type);
            }
            response.add(hw);
        }
        return response;
    }

    static RadioCapability convertHalRadioCapability(RadioCapability rcRil, RIL ril) {
        int session = rcRil.session;
        int phase = rcRil.phase;
        int rat = convertToNetworkTypeBitMask(rcRil.raf);
        String logicModemUuid = rcRil.logicalModemUuid;
        int status = rcRil.status;
        ril.riljLog("convertHalRadioCapability: session=" + session + ", phase=" + phase + ", rat=" + rat + ", logicModemUuid=" + logicModemUuid + ", status=" + status + ", rcRil.raf=" + rcRil.raf);
        return new RadioCapability(ril.mPhoneId.intValue(), session, phase, rat, logicModemUuid, status);
    }

    static LinkCapacityEstimate convertHalLceData(LceDataInfo halData, RIL ril) {
        int i;
        int i2 = halData.lastHopCapacityKbps;
        int unsignedInt = Byte.toUnsignedInt(halData.confidenceLevel);
        if (halData.lceSuspended) {
            i = 1;
        } else {
            i = 0;
        }
        LinkCapacityEstimate lce = new LinkCapacityEstimate(i2, unsignedInt, i);
        ril.riljLog("LCE capacity information received:" + lce);
        return lce;
    }

    static LinkCapacityEstimate convertHalLceData(LinkCapacityEstimate halData, RIL ril) {
        LinkCapacityEstimate lce = new LinkCapacityEstimate(halData.downlinkCapacityKbps, halData.uplinkCapacityKbps);
        ril.riljLog("LCE capacity information received:" + lce);
        return lce;
    }

    static void writeToParcelForGsm(Parcel p, int lac, int cid, int arfcn, int bsic, String mcc, String mnc, String al, String as, int ss, int ber, int ta) {
        p.writeInt(1);
        p.writeString(mcc);
        p.writeString(mnc);
        p.writeString(al);
        p.writeString(as);
        p.writeInt(lac);
        p.writeInt(cid);
        p.writeInt(arfcn);
        p.writeInt(bsic);
        p.writeInt(ss);
        p.writeInt(ber);
        p.writeInt(ta);
    }

    static void writeToParcelForCdma(Parcel p, int ni, int si, int bsi, int lon, int lat, String al, String as, int dbm, int ecio, int eDbm, int eEcio, int eSnr) {
        new CellIdentityCdma(ni, si, bsi, lon, lat, al, as).writeToParcel(p, 0);
        new CellSignalStrengthCdma(dbm, ecio, eDbm, eEcio, eSnr).writeToParcel(p, 0);
    }

    static void writeToParcelForLte(Parcel p, int ci, int pci, int tac, int earfcn, int bandwidth, String mcc, String mnc, String al, String as, int ss, int rsrp, int rsrq, int rssnr, int cqi, int ta, boolean isEndcAvailable) {
        p.writeInt(3);
        p.writeString(mcc);
        p.writeString(mnc);
        p.writeString(al);
        p.writeString(as);
        p.writeInt(ci);
        p.writeInt(pci);
        p.writeInt(tac);
        p.writeInt(earfcn);
        p.writeInt(bandwidth);
        p.writeInt(ss);
        p.writeInt(rsrp);
        p.writeInt(rsrq);
        p.writeInt(rssnr);
        p.writeInt(cqi);
        p.writeInt(ta);
        p.writeBoolean(isEndcAvailable);
    }

    static void writeToParcelForWcdma(Parcel p, int lac, int cid, int psc, int uarfcn, String mcc, String mnc, String al, String as, int ss, int ber, int rscp, int ecno) {
        p.writeInt(4);
        p.writeString(mcc);
        p.writeString(mnc);
        p.writeString(al);
        p.writeString(as);
        p.writeInt(lac);
        p.writeInt(cid);
        p.writeInt(psc);
        p.writeInt(uarfcn);
        p.writeInt(ss);
        p.writeInt(ber);
        p.writeInt(rscp);
        p.writeInt(ecno);
    }

    private static void writeToParcelForTdscdma(Parcel p, int lac, int cid, int cpid, int uarfcn, String mcc, String mnc, String al, String as, int ss, int ber, int rscp) {
        p.writeInt(5);
        p.writeString(mcc);
        p.writeString(mnc);
        p.writeString(al);
        p.writeString(as);
        p.writeInt(lac);
        p.writeInt(cid);
        p.writeInt(cpid);
        p.writeInt(uarfcn);
        p.writeInt(ss);
        p.writeInt(ber);
        p.writeInt(rscp);
    }

    @VisibleForTesting
    public static ArrayList<CellInfo> convertHalCellInfoList(ArrayList<android.hardware.radio.V1_0.CellInfo> records) {
        ArrayList<CellInfo> response = new ArrayList<>(records.size());
        long nanotime = SystemClock.elapsedRealtimeNanos();
        Iterator<android.hardware.radio.V1_0.CellInfo> it = records.iterator();
        while (it.hasNext()) {
            android.hardware.radio.V1_0.CellInfo record = it.next();
            record.timeStamp = nanotime;
            response.add(CellInfo.create(record));
        }
        return response;
    }

    @VisibleForTesting
    public static ArrayList<CellInfo> convertHalCellInfoList_1_2(ArrayList<android.hardware.radio.V1_2.CellInfo> records) {
        ArrayList<CellInfo> response = new ArrayList<>(records.size());
        long nanotime = SystemClock.elapsedRealtimeNanos();
        Iterator<android.hardware.radio.V1_2.CellInfo> it = records.iterator();
        while (it.hasNext()) {
            android.hardware.radio.V1_2.CellInfo record = it.next();
            record.timeStamp = nanotime;
            response.add(CellInfo.create(record));
        }
        return response;
    }

    @VisibleForTesting
    public static ArrayList<CellInfo> convertHalCellInfoList_1_4(ArrayList<android.hardware.radio.V1_4.CellInfo> records) {
        ArrayList<CellInfo> response = new ArrayList<>(records.size());
        long nanotime = SystemClock.elapsedRealtimeNanos();
        Iterator<android.hardware.radio.V1_4.CellInfo> it = records.iterator();
        while (it.hasNext()) {
            response.add(CellInfo.create(it.next(), nanotime));
        }
        return response;
    }

    /* JADX INFO: Multiple debug info for r0v27 int: [D('result' android.hardware.radio.V1_4.SetupDataCallResult), D('mtu' int)] */
    /* JADX INFO: Multiple debug info for r0v30 int: [D('result' android.hardware.radio.V1_0.SetupDataCallResult), D('mtu' int)] */
    @VisibleForTesting
    public static DataCallResponse convertDataCallResult(Object dcResult) {
        int active;
        int cid;
        int suggestedRetryTime;
        int cause;
        String ifname;
        int mtu;
        int cause2;
        String[] addresses;
        LinkAddress la;
        if (dcResult == null) {
            return null;
        }
        String[] addresses2 = null;
        String[] dnses = null;
        String[] gateways = null;
        String[] pcscfs = null;
        if (dcResult instanceof SetupDataCallResult) {
            SetupDataCallResult result = (SetupDataCallResult) dcResult;
            int cause3 = result.status;
            int suggestedRetryTime2 = result.suggestedRetryTime;
            int cid2 = result.cid;
            int active2 = result.active;
            int protocolType = ApnSetting.getProtocolIntFromString(result.type);
            String ifname2 = result.ifname;
            if (!TextUtils.isEmpty(result.addresses)) {
                addresses2 = result.addresses.split("\\s+");
            }
            if (!TextUtils.isEmpty(result.dnses)) {
                dnses = result.dnses.split("\\s+");
            }
            if (!TextUtils.isEmpty(result.gateways)) {
                gateways = result.gateways.split("\\s+");
            }
            if (!TextUtils.isEmpty(result.pcscf)) {
                pcscfs = result.pcscf.split("\\s+");
            }
            mtu = result.mtu;
            cause = cause3;
            suggestedRetryTime = suggestedRetryTime2;
            cid = cid2;
            active = active2;
            cause2 = protocolType;
            ifname = ifname2;
        } else if (dcResult instanceof android.hardware.radio.V1_4.SetupDataCallResult) {
            android.hardware.radio.V1_4.SetupDataCallResult result2 = (android.hardware.radio.V1_4.SetupDataCallResult) dcResult;
            int cause4 = result2.cause;
            int suggestedRetryTime3 = result2.suggestedRetryTime;
            int cid3 = result2.cid;
            int active3 = result2.active;
            int protocolType2 = result2.type;
            String ifname3 = result2.ifname;
            addresses2 = (String[]) result2.addresses.stream().toArray($$Lambda$RIL$zYsQZAc3z9bM5fCaq_J0dn5kjjo.INSTANCE);
            dnses = (String[]) result2.dnses.stream().toArray($$Lambda$RIL$Ir4pOMTf7R0Jtw4O3F7JgMVtXO4.INSTANCE);
            gateways = (String[]) result2.gateways.stream().toArray($$Lambda$RIL$803u4JiCud_JSoDndvAhT13ZZqU.INSTANCE);
            pcscfs = (String[]) result2.pcscf.stream().toArray($$Lambda$RIL$ZGWeCQ9boMO1_J1_yQ82l_jKNc.INSTANCE);
            mtu = result2.mtu;
            cause = cause4;
            suggestedRetryTime = suggestedRetryTime3;
            cid = cid3;
            active = active3;
            cause2 = protocolType2;
            ifname = ifname3;
        } else {
            Rlog.e(RILJ_LOG_TAG, "Unsupported SetupDataCallResult " + dcResult);
            return null;
        }
        List<LinkAddress> laList = new ArrayList<>();
        if (addresses2 != null) {
            for (String address : addresses2) {
                String address2 = address.trim();
                if (!address2.isEmpty()) {
                    try {
                        if (address2.split("/").length == 2) {
                            la = new LinkAddress(address2);
                        } else {
                            InetAddress ia = NetworkUtils.numericToInetAddress(address2);
                            la = new LinkAddress(ia, ia instanceof Inet4Address ? 32 : 64);
                        }
                        laList.add(la);
                    } catch (IllegalArgumentException e) {
                        Rlog.e(RILJ_LOG_TAG, "Unknown address. ");
                    }
                }
            }
        }
        List<InetAddress> dnsList = new ArrayList<>();
        if (dnses != null) {
            for (String dns : dnses) {
                String dns2 = dns.trim();
                try {
                    dnsList.add(NetworkUtils.numericToInetAddress(dns2));
                } catch (IllegalArgumentException e2) {
                    Rlog.e(RILJ_LOG_TAG, "Unknown dns: " + dns2, e2);
                }
            }
        }
        List<InetAddress> gatewayList = new ArrayList<>();
        if (gateways != null) {
            for (String gateway : gateways) {
                String gateway2 = gateway.trim();
                try {
                    gatewayList.add(NetworkUtils.numericToInetAddress(gateway2));
                } catch (IllegalArgumentException e3) {
                    Rlog.e(RILJ_LOG_TAG, "Unknown gateway: " + gateway2, e3);
                }
            }
        }
        List<InetAddress> pcscfList = new ArrayList<>();
        if (pcscfs != null) {
            int length = pcscfs.length;
            int i = 0;
            while (i < length) {
                String pcscf = pcscfs[i].trim();
                try {
                    pcscfList.add(NetworkUtils.numericToInetAddress(pcscf));
                    addresses = addresses2;
                } catch (IllegalArgumentException e4) {
                    StringBuilder sb = new StringBuilder();
                    addresses = addresses2;
                    sb.append("Unknown pcscf: ");
                    sb.append(pcscf);
                    Rlog.e(RILJ_LOG_TAG, sb.toString(), e4);
                }
                i++;
                addresses2 = addresses;
            }
        }
        return new DataCallResponse(cause, suggestedRetryTime, cid, active, cause2, ifname, laList, dnsList, gatewayList, pcscfList, mtu);
    }

    static /* synthetic */ String[] lambda$convertDataCallResult$0(int x$0) {
        return new String[x$0];
    }

    static /* synthetic */ String[] lambda$convertDataCallResult$1(int x$0) {
        return new String[x$0];
    }

    static /* synthetic */ String[] lambda$convertDataCallResult$2(int x$0) {
        return new String[x$0];
    }

    static /* synthetic */ String[] lambda$convertDataCallResult$3(int x$0) {
        return new String[x$0];
    }

    @VisibleForTesting
    public static ArrayList<DataCallResponse> convertDataCallResultList(List<? extends Object> dataCallResultList) {
        ArrayList<DataCallResponse> response = new ArrayList<>(dataCallResultList.size());
        for (Object obj : dataCallResultList) {
            response.add(convertDataCallResult(obj));
        }
        return response;
    }

    public HalVersion getHalVersion() {
        return this.mRadioVersion;
    }

    public void addRequestEx(RILRequest rr) {
        addRequest(rr);
    }

    public void handleRadioProxyExceptionForRREx(String caller, Exception e, RILRequest rr) {
        handleRadioProxyExceptionForRR(rr, caller, e);
    }

    public int getLastRadioTech() {
        return this.mLastRadioTech;
    }

    public Context getContext() {
        return this.mContext;
    }

    private RadioResponseInfo convertToOriginalRadioResponseInfo(Object responseInfo) {
        RadioResponseInfo radioResponseInfo = new RadioResponseInfo();
        if (responseInfo != null) {
            if (responseInfo instanceof vendor.huawei.hardware.hisiradio.V1_0.RadioResponseInfo) {
                radioResponseInfo.serial = ((vendor.huawei.hardware.hisiradio.V1_0.RadioResponseInfo) responseInfo).serial;
                radioResponseInfo.error = ((vendor.huawei.hardware.hisiradio.V1_0.RadioResponseInfo) responseInfo).error;
                radioResponseInfo.type = ((vendor.huawei.hardware.hisiradio.V1_0.RadioResponseInfo) responseInfo).type;
            } else if (responseInfo instanceof vendor.huawei.hardware.radio.V2_0.RadioResponseInfo) {
                radioResponseInfo.serial = ((vendor.huawei.hardware.radio.V2_0.RadioResponseInfo) responseInfo).serial;
                radioResponseInfo.error = ((vendor.huawei.hardware.radio.V2_0.RadioResponseInfo) responseInfo).error;
                radioResponseInfo.type = ((vendor.huawei.hardware.radio.V2_0.RadioResponseInfo) responseInfo).type;
            } else if (responseInfo instanceof vendor.huawei.hardware.qcomradio.V1_0.RadioResponseInfo) {
                radioResponseInfo.serial = ((vendor.huawei.hardware.qcomradio.V1_0.RadioResponseInfo) responseInfo).serial;
                radioResponseInfo.error = ((vendor.huawei.hardware.qcomradio.V1_0.RadioResponseInfo) responseInfo).error;
                radioResponseInfo.type = ((vendor.huawei.hardware.qcomradio.V1_0.RadioResponseInfo) responseInfo).type;
            }
        }
        return radioResponseInfo;
    }

    public RILRequest processResponseEx(Object responseInfo) {
        return processResponse(convertToOriginalRadioResponseInfo(responseInfo));
    }

    public void processResponseDoneEx(RILRequest rr, Object responseInfo, Object ret) {
        processResponseDone(rr, convertToOriginalRadioResponseInfo(responseInfo), ret);
    }

    /* access modifiers changed from: protected */
    public WorkSource getmRILDefaultWorkSourceHw() {
        return this.mRILDefaultWorkSource;
    }

    static void writeToParcelForGsmHw(Parcel p, int lac, int cid, int arfcn, int bsic, String mcc, String mnc, String al, String as, int ss, int ber, int ta) {
        writeToParcelForGsm(p, lac, cid, arfcn, bsic, mcc, mnc, al, as, ss, ber, ta);
    }

    static void writeToParcelForCdmaHw(Parcel p, int ni, int si, int bsi, int lon, int lat, String al, String as, int dbm, int ecio, int eDbm, int eEcio, int eSnr) {
        writeToParcelForCdma(p, ni, si, bsi, lon, lat, al, as, dbm, ecio, eDbm, eEcio, eSnr);
    }

    static void writeToParcelForLteHw(Parcel p, int ci, int pci, int tac, int earfcn, int bandwidth, String mcc, String mnc, String al, String as, int ss, int rsrp, int rsrq, int rssnr, int cqi, int ta, boolean isEndcAvailable) {
        writeToParcelForLte(p, ci, pci, tac, earfcn, bandwidth, mcc, mnc, al, as, ss, rsrp, rsrq, rssnr, cqi, ta, isEndcAvailable);
    }

    static void writeToParcelForWcdmaHw(Parcel p, int lac, int cid, int psc, int uarfcn, String mcc, String mnc, String al, String as, int ss, int ber, int rscp, int ecno) {
        writeToParcelForWcdma(p, lac, cid, psc, uarfcn, mcc, mnc, al, as, ss, ber, rscp, ecno);
    }

    /* access modifiers changed from: protected */
    public WorkSource getMRILDefaultWorkSource() {
        return this.mRILDefaultWorkSource;
    }

    /* access modifiers changed from: protected */
    public WorkSource getMActiveWakelockWorkSource() {
        return this.mActiveWakelockWorkSource;
    }

    private void checkFirstRequest() {
        int size = this.mRequestList.size();
        RILRequestEx firstRequest = null;
        if (size > 0) {
            firstRequest = RILRequestEx.from(this.mRequestList.valueAt(0));
        }
        HwTelephonyFactory.getHwTelephonyChrManager().checkFirstRequest(size, firstRequest, this.mPhoneId.intValue());
    }
}
