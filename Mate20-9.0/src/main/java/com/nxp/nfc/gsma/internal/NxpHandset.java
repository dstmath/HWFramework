package com.nxp.nfc.gsma.internal;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.util.Log;
import com.nxp.nfc.NxpConstants;
import com.nxp.nfc.NxpNfcAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NxpHandset {
    private static final int BATTERY_LOW_MODE = 144;
    private static final int BATTERY_OPERATIONAL_MODE = 146;
    private static final int BATTERY_POWER_OFF_MODE = 145;
    private static final int FELICA = 32;
    private static final int HCI_SWP = 0;
    private static final int MIFARE_CLASSIC = 33;
    private static final int MIFARE_DESFIRE = 34;
    private static final int MULTIPLE_ACTIVE_CEE = 1;
    private static final int NFC_FORUM_TYPE3 = 35;
    public static final int OMAPI = 80;
    private final int GSMA_NFCHST;
    private String TAG;
    private Context mContext;
    private NfcAdapter mNfcAdapter;
    private INxpNfcController mNfcControllerService;
    private NxpNfcAdapter mNxpNfcAdapter;

    public NxpHandset() {
        this.TAG = "NxpHandset";
        this.mNfcAdapter = null;
        this.mNxpNfcAdapter = null;
        this.mNfcControllerService = null;
        this.GSMA_NFCHST = 9000;
        this.mContext = getContext();
        this.mNfcAdapter = NfcAdapter.getNfcAdapter(this.mContext);
        if (this.mNfcAdapter != null) {
            this.mNxpNfcAdapter = NxpNfcAdapter.getNxpNfcAdapter(this.mNfcAdapter);
        }
        if (this.mNxpNfcAdapter != null) {
            this.mNfcControllerService = this.mNxpNfcAdapter.getNxpNfcControllerInterface();
        }
    }

    private Context getContext() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, null);
        } catch (Exception e) {
            try {
                return (Context) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication", new Class[0]).invoke(null, null);
            } catch (Exception e2) {
                throw new RuntimeException("Failed to get application instance");
            }
        }
    }

    public int getNxpVersion() {
        return 9000;
    }

    public boolean getNxpProperty(int feature) throws IllegalArgumentException {
        Log.e("NxpHandset", "getNxpProperty:feature=0x" + feature);
        if (feature == 0 || feature == 1 || feature == FELICA || feature == MIFARE_CLASSIC || feature == MIFARE_DESFIRE || feature == NFC_FORUM_TYPE3 || feature == BATTERY_LOW_MODE || feature == 80 || feature == BATTERY_POWER_OFF_MODE) {
            switch (feature) {
                case 0:
                case 1:
                case FELICA /*32*/:
                case MIFARE_CLASSIC /*33*/:
                case MIFARE_DESFIRE /*34*/:
                case NFC_FORUM_TYPE3 /*35*/:
                case OMAPI /*80*/:
                case BATTERY_LOW_MODE /*144*/:
                    return true;
                case BATTERY_POWER_OFF_MODE /*145*/:
                    return false;
                default:
                    return false;
            }
        } else {
            throw new IllegalArgumentException("Feature is inappropriate argument");
        }
    }

    public List<String> getAvailableSecureElements(int batteryLevel) {
        String[] secureElemArray;
        String pkg = this.mContext.getPackageName();
        List<String> secureElementList = new ArrayList<>(3);
        switch (batteryLevel) {
            case BATTERY_LOW_MODE /*144*/:
            case BATTERY_POWER_OFF_MODE /*145*/:
            case BATTERY_OPERATIONAL_MODE /*146*/:
                try {
                    secureElemArray = this.mNxpNfcAdapter.getActiveSecureElementList(pkg);
                } catch (IOException e) {
                    secureElemArray = null;
                }
                if (secureElemArray == null || secureElemArray.length <= 0) {
                    return Collections.emptyList();
                }
                Collections.addAll(secureElementList, secureElemArray);
                for (int i = 0; i < secureElementList.size(); i++) {
                    if (secureElementList.get(i).equals(NxpConstants.UICC_ID)) {
                        secureElementList.set(i, NxpConstants.UICC_ID);
                    } else if (secureElementList.get(i).equals(NxpConstants.UICC2_ID)) {
                        secureElementList.set(i, "SIM2");
                    } else if (secureElementList.get(i).equals(NxpConstants.SMART_MX_ID)) {
                        secureElementList.set(i, "eSE");
                    }
                }
                return secureElementList;
            default:
                throw new IllegalArgumentException("Wrong value for batteryLevel");
        }
    }

    public void enableMultiEvt_transactionReception() {
        String pkg = this.mContext.getPackageName();
        boolean isEnabled = false;
        String str = this.TAG;
        Log.d(str, "pkg " + pkg);
        try {
            isEnabled = this.mNfcControllerService.enableMultiEvt_NxptransactionReception(pkg, NxpConstants.UICC_ID);
        } catch (RemoteException e) {
            Log.e(this.TAG, "Exception:commitOffHostService failed", e);
        }
        if (!isEnabled) {
            throw new SecurityException("Application is not allowed to use this API");
        }
    }
}
