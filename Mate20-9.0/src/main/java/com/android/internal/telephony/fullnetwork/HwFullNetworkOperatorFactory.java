package com.android.internal.telephony.fullnetwork;

public class HwFullNetworkOperatorFactory {
    private static final Object mLock = new Object();
    protected static HwFullNetworkOperatorBase mOperatorBaseInstance = null;

    public static HwFullNetworkOperatorBase getOperatorBase() {
        HwFullNetworkOperatorBase hwFullNetworkOperatorBase;
        synchronized (mLock) {
            if (mOperatorBaseInstance == null) {
                if (HwFullNetworkConfig.IS_AIS_4G_DSDX_ENABLE) {
                    mOperatorBaseInstance = new HwFullNetworkOperatorAIS();
                } else if (HwFullNetworkConfig.IS_CMCC_4G_DSDX_ENABLE) {
                    mOperatorBaseInstance = new HwFullNetworkOperatorCMCC();
                } else {
                    if (!HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) {
                        if (!HwFullNetworkConfig.IS_CHINA_TELECOM) {
                            if (HwFullNetworkConfig.IS_CMCC_CU_DSDX_ENABLE) {
                                mOperatorBaseInstance = new HwFullNetworkOperatorCU();
                            } else {
                                mOperatorBaseInstance = new HwFullNetworkOperatorCommon();
                            }
                        }
                    }
                    mOperatorBaseInstance = new HwFullNetworkOperatorCT();
                }
            }
            hwFullNetworkOperatorBase = mOperatorBaseInstance;
        }
        return hwFullNetworkOperatorBase;
    }
}
