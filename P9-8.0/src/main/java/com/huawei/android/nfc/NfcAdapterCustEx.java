package com.huawei.android.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;

public class NfcAdapterCustEx {
    public static final String ACTION_CONNECTIVITY_EVENT_DETECTED = "";
    public static final String ACTION_EMVCO_MULTIPLE_CARD_DETECTED = "";
    public static final String ACTION_SWP_READER_ACTIVATED = "";
    public static final String ACTION_SWP_READER_DEACTIVATED = "";
    public static final String ACTION_SWP_READER_REQUESTED = "";
    public static final String ACTION_TRANSACTION_DETECTED = "";
    public static final String ALL_SE_ID = "com.nxp.all_se.ID";
    public static final int ALL_SE_ID_TYPE = 3;
    public static final int CARD_EMULATION_ALL_SUPPORT = 3;
    public static final int CARD_EMULATION_NO_SUPPORT = 0;
    public static final int CARD_EMULATION_SIM1_SUPPORT = 1;
    public static final int CARD_EMULATION_SIM2_SUPPORT = 2;
    public static final int CARD_EMULATION_SUB1 = 1;
    public static final int CARD_EMULATION_SUB2 = 2;
    public static final int CARD_EMULATION_UNKNOW = -1;
    public static final String EXTRA_AID = "";
    public static final String EXTRA_DATA = "";
    public static final String EXTRA_SOURCE = "";
    public static final String EXTRA_SWP_READER_TECH = "";
    public static final String SMART_MX_ID = "com.nxp.smart_mx.ID";
    public static final int SMART_MX_ID_TYPE = 1;
    public static final int SWITCH_CE_FAILED = -1;
    public static final int SWITCH_CE_SUCCESS = 0;
    public static final String SWITH_CE_SWITCH_ACTION = "com.huawei.android.nfc.SWITCH_CE_STATE";
    public static final String SWITH_CE_SWITCH_STATUS = "com.huawei.android.nfc.CE_SELECTED_STATE";
    public static final String UICC_ID = "com.nxp.uicc.ID";
    public static final int UICC_ID_TYPE = 2;

    public static boolean isTagRwEnabled(Context context) {
        return NfcAdapter.getDefaultAdapter(context).isTagRwEnabled();
    }

    public static void enableTagRw(Context context) {
        NfcAdapter.getDefaultAdapter(context).enableTagRw();
    }

    public static void disableTagRw(Context context) {
        NfcAdapter.getDefaultAdapter(context).disableTagRw();
    }

    public static int getSelectedCardEmulation(Context context) {
        return NfcAdapter.getDefaultAdapter(context).getSelectedCardEmulation();
    }

    public static void selectCardEmulation(Context context, int sub) {
        NfcAdapter.getDefaultAdapter(context).selectCardEmulation(sub);
    }

    public static int getSupportCardEmulation(Context context) {
        return NfcAdapter.getDefaultAdapter(context).getSupportCardEmulation();
    }

    public Boolean getFirmwareState() {
        return Boolean.valueOf(true);
    }

    public String getFirmwareVersion() {
        return NfcAdapter.getDefaultAdapter().getFirmwareVersion();
    }
}
