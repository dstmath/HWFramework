package com.android.internal.telephony.fullnetwork;

import android.telephony.HwTelephonyManagerInner;

public class HwFullNetworkOperatorFactory {
    private static final Object LOCK = new Object();
    private static HwFullNetworkOperatorBase mOperatorBaseInstance = null;
    private static HwFullNetworkOperatorBase mOperatorCMCCInstance = null;
    private static HwFullNetworkOperatorBase mOperatorMDMCarrierCMCCInstance = null;
    private static HwFullNetworkOperatorBase mOperatorMDMCarrierCTInstance = null;

    public static HwFullNetworkOperatorBase getOperatorBase() {
        HwFullNetworkOperatorBase hwFullNetworkOperatorBase;
        synchronized (LOCK) {
            if (mOperatorBaseInstance == null) {
                if (HwFullNetworkConfigInner.IS_AIS_4G_DSDX_ENABLE) {
                    mOperatorBaseInstance = new HwFullNetworkOperatorAIS();
                } else if (HwFullNetworkConfigInner.isCMCCDsdxEnable() || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1) {
                    mOperatorBaseInstance = new HwFullNetworkOperatorCMCC();
                } else if (HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2 || HwFullNetworkConfigInner.IS_CHINA_TELECOM) {
                    mOperatorBaseInstance = new HwFullNetworkOperatorCT();
                } else if (HwFullNetworkConfigInner.IS_CMCC_CU_DSDX_ENABLE) {
                    mOperatorBaseInstance = new HwFullNetworkOperatorCU();
                } else {
                    mOperatorBaseInstance = new HwFullNetworkOperatorCommon();
                }
            }
            hwFullNetworkOperatorBase = mOperatorBaseInstance;
        }
        return hwFullNetworkOperatorBase;
    }

    public static HwFullNetworkOperatorBase getOperatorCMCC() {
        if (mOperatorCMCCInstance == null) {
            mOperatorCMCCInstance = new HwFullNetworkOperatorCMCC();
            mOperatorBaseInstance = mOperatorCMCCInstance;
        }
        return mOperatorBaseInstance;
    }

    public static HwFullNetworkOperatorBase getOperatorCMCCMDMCarrier() {
        if (mOperatorMDMCarrierCMCCInstance == null) {
            mOperatorMDMCarrierCMCCInstance = new HwFullNetworkOperatorCMCC();
        }
        mOperatorBaseInstance = mOperatorMDMCarrierCMCCInstance;
        return mOperatorBaseInstance;
    }

    public static HwFullNetworkOperatorBase getOperatorCTMDMCarrier() {
        if (mOperatorMDMCarrierCTInstance == null) {
            mOperatorMDMCarrierCTInstance = new HwFullNetworkOperatorCT();
        }
        mOperatorBaseInstance = mOperatorMDMCarrierCTInstance;
        return mOperatorBaseInstance;
    }
}
