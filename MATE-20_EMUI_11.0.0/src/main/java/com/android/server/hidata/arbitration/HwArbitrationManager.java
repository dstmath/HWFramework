package com.android.server.hidata.arbitration;

import android.content.Context;
import android.emcom.EmcomManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SignalStrength;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.hinetwork.HwHiNetworkManager;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHiStreamManager;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.HashMap;

public class HwArbitrationManager {
    private static final boolean HiStreamEnabled = SystemProperties.getBoolean("ro.config.hw_histream.enabled", true);
    private static final int MAX_TEMPERATURE_LEVEL = 3;
    private static final String TAG = "HiData_HwArbitrationManager";
    private static HwArbitrationManager mHwArbitrationManager;
    private static int sTemperatureLevel = 0;
    private HwAPPQoEManager mAPPQoEManager = null;
    private Context mContext;
    private IHiDataCHRCallBack mHiDataCHRCallBack;
    private HwHiRadioBoost mHiRadioBoost = null;
    private HwAppTimeDetail mHwAppTimeDetail = null;
    private HwArbitrationChrImpl mHwArbitrationChrImpl;
    private HwArbitrationStateMachine mHwArbitrationStateMachine;
    private HwArbitrationCallbackImpl mHwArbitrtionCallbackImpl = null;
    private HwHiStreamManager mHwHiStreamManager = null;
    private HwWaveMappingManager mHwWaveMappingManager = null;
    private HwArbitrationStateMonitor mStateMonitor = null;

    public static HwArbitrationManager createInstance(Context context, IHiDataCHRCallBack chrcallBack) {
        if (mHwArbitrationManager == null) {
            mHwArbitrationManager = new HwArbitrationManager(context, chrcallBack);
        }
        return mHwArbitrationManager;
    }

    public static HwArbitrationManager getInstance() {
        HwArbitrationCommonUtils.logI(TAG, false, "HwArbitrationManager getInstance", new Object[0]);
        return mHwArbitrationManager;
    }

    private HwArbitrationManager(Context context, IHiDataCHRCallBack chrcallBack) {
        this.mContext = context;
        this.mHwArbitrationStateMachine = HwArbitrationStateMachine.getInstance(this.mContext);
        this.mHwArbitrtionCallbackImpl = HwArbitrationCallbackImpl.getInstance(this.mContext);
        this.mHiDataCHRCallBack = chrcallBack;
        this.mHwArbitrationChrImpl = HwArbitrationChrImpl.createInstance();
        this.mHwArbitrationChrImpl.registArbitationChrCallBack(chrcallBack);
        modelInit(context);
        this.mStateMonitor = HwArbitrationStateMonitor.createHwArbitrationStateMonitor(this.mContext, this.mHwArbitrationStateMachine.getHandler());
        this.mStateMonitor.startMonitor();
        HwArbitrationCommonUtils.logI(TAG, false, "init HwArbitration completed!", new Object[0]);
    }

    private void modelInit(Context context) {
        this.mAPPQoEManager = HwAPPQoEManager.createHwAPPQoEManager(context);
        this.mAPPQoEManager.registerAppQoECallback(this.mHwArbitrtionCallbackImpl, true);
        this.mHwWaveMappingManager = HwWaveMappingManager.getInstance(context);
        this.mHwWaveMappingManager.registerWaveMappingCallback(this.mHwArbitrtionCallbackImpl);
        if (true == HiStreamEnabled) {
            this.mHwHiStreamManager = HwHiStreamManager.createInstance(this.mContext);
            this.mHwHiStreamManager.registCHRCallback(this.mHiDataCHRCallBack);
            this.mHwHiStreamManager.registerHistreamQoeCallback(this.mHwArbitrtionCallbackImpl);
        }
        if (HwArbitrationCommonUtils.MAINLAND_REGION && !WifiProCommonUtils.isWifiProLitePropertyEnabled(this.mContext)) {
            HwHiNetworkManager.createInstance(context);
        }
        this.mHiRadioBoost = HwHiRadioBoost.createInstance(this.mContext);
        this.mHiRadioBoost.registerHiRadioCallback(this.mHwArbitrtionCallbackImpl);
        HwAppTimeDetail hwAppTimeDetail = this.mHwAppTimeDetail;
        HwAppTimeDetail.createInstance(this.mContext);
    }

    public boolean getWifiPlusFlagFromHiData() {
        HwArbitrationCallbackImpl hwArbitrationCallbackImpl = this.mHwArbitrtionCallbackImpl;
        if (hwArbitrationCallbackImpl == null) {
            return isSmartMpEnable();
        }
        return hwArbitrationCallbackImpl.getWifiPlusFlagFromHiData() || isSmartMpEnable();
    }

    private boolean isSmartMpEnable() {
        return EmcomManager.getInstance().isSmartMpEnable();
    }

    public HashMap<Integer, String> getWifiPreferenceFromHiData() {
        HashMap<Integer, String> preferList = new HashMap<>();
        if (this.mHwWaveMappingManager == null || this.mContext == null) {
            return preferList;
        }
        HwArbitrationCommonUtils.logI(TAG, false, "getWifiPreferenceFromHiData", new Object[0]);
        if (Settings.System.getInt(this.mContext.getContentResolver(), "smart_network_switching", 0) == 1) {
            return this.mHwWaveMappingManager.queryNetPreference(1);
        }
        return preferList;
    }

    public SignalStrength getSignalStrength(int subId) {
        HwArbitrationStateMonitor hwArbitrationStateMonitor = this.mStateMonitor;
        if (hwArbitrationStateMonitor != null) {
            return hwArbitrationStateMonitor.getSignalStrength(subId);
        }
        return null;
    }

    public boolean isInMpLink(int uid) {
        HwArbitrationStateMachine hwArbitrationStateMachine = this.mHwArbitrationStateMachine;
        if (hwArbitrationStateMachine != null) {
            return hwArbitrationStateMachine.isInMPLink(uid);
        }
        return false;
    }

    public boolean isHandleWifiBoostSuccessful(String packageName, boolean enable) {
        int uid = MpLinkCommonUtils.getAppUid(this.mContext, packageName);
        if (uid <= 0) {
            HwArbitrationCommonUtils.logE(TAG, false, "uid error", new Object[0]);
            return false;
        } else if (HwArbitrationCommonUtils.getActiveConnectType(this.mContext) != 800) {
            HwArbitrationCommonUtils.logE(TAG, false, "not on wifi network", new Object[0]);
            return false;
        } else {
            HwWifiBoost wifiBoost = HwWifiBoost.getInstance(this.mContext);
            if (enable) {
                wifiBoost.startStreamingBoost(uid);
                return true;
            }
            wifiBoost.stopStreamingBoost(uid);
            return true;
        }
    }

    public static boolean isExceedMaxTemperature() {
        if (sTemperatureLevel >= 3) {
            return true;
        }
        return false;
    }

    public void reportThermalDataToArbitration(int level) {
        sTemperatureLevel = level;
        if (isExceedMaxTemperature() && this.mHwArbitrationStateMachine != null) {
            HwArbitrationCommonUtils.logD(TAG, false, "Exceed max temperature.", new Object[0]);
            this.mHwArbitrationStateMachine.sendMessage(HwArbitrationDEFS.MSG_WM_HIGH_TEMPERATURE_STOP_MPLINK);
        }
    }
}
