package com.android.server.hidata.arbitration;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.hidata.appqoe.HwAppQoeManager;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHiStreamManager;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.HashMap;

public class HwArbitrationManager {
    private static final boolean HISTREAM_ENABLE = SystemProperties.getBoolean("ro.config.hw_histream.enabled", true);
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwArbitrationManager.class.getSimpleName());
    private static HwArbitrationManager sHwArbitrationManager;
    private HwAppQoeManager mAppQoeManager = null;
    private Context mContext;
    private HwHiRadioBoost mHiRadioBoost = null;
    private HwAppTimeDetail mHwAppTimeDetail = null;
    private HwArbitrationCallbackImpl mHwArbitrationCallbackImpl = null;
    private HwArbitrationStateMachine mHwArbitrationStateMachine;
    private HwHiStreamManager mHwHiStreamManager = null;
    private HwArbitrationStateMonitor mStateMonitor = null;

    private HwArbitrationManager(Context context) {
        this.mContext = context;
        this.mHwArbitrationStateMachine = HwArbitrationStateMachine.getInstance(this.mContext);
        this.mHwArbitrationCallbackImpl = HwArbitrationCallbackImpl.getInstance(this.mContext);
        modelInit(context);
        this.mStateMonitor = HwArbitrationStateMonitor.createHwArbitrationStateMonitor(this.mContext, this.mHwArbitrationStateMachine.getHandler());
        this.mStateMonitor.startMonitor();
        HwArbitrationCommonUtils.logI(TAG, false, "init HwArbitration completed!", new Object[0]);
    }

    public static HwArbitrationManager createInstance(Context context) {
        if (sHwArbitrationManager == null) {
            sHwArbitrationManager = new HwArbitrationManager(context);
        }
        return sHwArbitrationManager;
    }

    public static HwArbitrationManager getInstance() {
        HwArbitrationCommonUtils.logI(TAG, false, "HwArbitrationManager getInstance", new Object[0]);
        return sHwArbitrationManager;
    }

    private void modelInit(Context context) {
        this.mAppQoeManager = HwAppQoeManager.createHwAppQoeManager(context);
        this.mAppQoeManager.registerAppQoeCallback(this.mHwArbitrationCallbackImpl, true);
        if (HISTREAM_ENABLE) {
            this.mHwHiStreamManager = HwHiStreamManager.createInstance(this.mContext);
        }
        this.mHiRadioBoost = HwHiRadioBoost.createInstance(this.mContext);
        this.mHiRadioBoost.registerHiRadioCallback(this.mHwArbitrationCallbackImpl);
        HwAppTimeDetail hwAppTimeDetail = this.mHwAppTimeDetail;
        HwAppTimeDetail.createInstance(this.mContext);
    }

    public HashMap<Integer, String> getWifiPreferenceFromHiData() {
        return new HashMap<>();
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
}
