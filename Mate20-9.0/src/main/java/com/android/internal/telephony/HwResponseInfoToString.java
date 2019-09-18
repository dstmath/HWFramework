package com.android.internal.telephony;

import android.hardware.radio.V1_0.CellIdentity;
import android.hardware.radio.V1_0.CellIdentityCdma;
import android.hardware.radio.V1_0.CellIdentityGsm;
import android.hardware.radio.V1_0.CellIdentityLte;
import android.hardware.radio.V1_0.CellIdentityTdscdma;
import android.hardware.radio.V1_0.CellIdentityWcdma;
import android.hardware.radio.V1_0.CellInfoType;
import android.hardware.radio.V1_0.DataCallFailCause;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.RegState;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.telephony.Rlog;
import java.util.ArrayList;
import vendor.huawei.hardware.hisiradio.V1_1.HwCellIdentityNr;
import vendor.huawei.hardware.hisiradio.V1_1.HwCellIdentity_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwCellInfoType_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwDataRegStateResult_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwVoiceRegStateResult_1_1;

public class HwResponseInfoToString {
    private static final String LOG_TAG = "HwResponseInfoToString";
    private static HwResponseInfoToString mInstance = new HwResponseInfoToString();

    public static HwResponseInfoToString getDefault() {
        if (mInstance != null) {
            return mInstance;
        }
        Rlog.d(LOG_TAG, "mInstance has not init.");
        return new HwResponseInfoToString();
    }

    public String retToStringEx(int req, Object ret) {
        String s;
        if (!(req == 95 || req == 98 || req == 529)) {
            switch (req) {
                case 100:
                case 101:
                    break;
                default:
                    if (req == 27 && (ret instanceof SetupDataCallResult)) {
                        s = dataCallResultToString((SetupDataCallResult) ret);
                    } else if (req == 1010 && (ret instanceof ArrayList)) {
                        ArrayList<Object> results = (ArrayList) ret;
                        int size = results.size();
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < size; i++) {
                            Object o = results.get(i);
                            if (o instanceof SetupDataCallResult) {
                                sb.append(dataCallResultToString((SetupDataCallResult) o));
                            } else {
                                sb.append(o.toString());
                            }
                        }
                        sb.append("]");
                        s = sb.toString();
                    } else if (req == 20) {
                        if (ret instanceof VoiceRegStateResult) {
                            s = voiceRegStateResultToString((VoiceRegStateResult) ret);
                        } else if (ret instanceof HwVoiceRegStateResult_1_1) {
                            s = voiceRegStateResultToString((HwVoiceRegStateResult_1_1) ret);
                        } else {
                            s = ret.toString();
                        }
                    } else if (req != 21) {
                        s = ret.toString();
                    } else if (ret instanceof DataRegStateResult) {
                        s = dataRegStateResultToString((DataRegStateResult) ret);
                    } else if (ret instanceof HwDataRegStateResult_1_1) {
                        s = dataRegStateResultToString((HwDataRegStateResult_1_1) ret);
                    } else {
                        s = ret.toString();
                    }
                    return s;
            }
        }
        return "";
    }

    private static String voiceRegStateResultToString(VoiceRegStateResult result) {
        return "" + "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .cssSupported = " + result.cssSupported + ", .roamingIndicator = " + result.roamingIndicator + ", .systemIsInPrl = " + result.systemIsInPrl + ", .defaultRoamingIndicator = " + result.defaultRoamingIndicator + ", .reasonForDenial = " + result.reasonForDenial + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + "}";
    }

    public final String dataRegStateResultToString(DataRegStateResult result) {
        return "" + "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .reasonDataDenied = " + result.reasonDataDenied + ", .maxDataCalls = " + result.maxDataCalls + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + "}";
    }

    private static String voiceRegStateResultToString(HwVoiceRegStateResult_1_1 result) {
        return "" + "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .cssSupported = " + result.cssSupported + ", .roamingIndicator = " + result.roamingIndicator + ", .systemIsInPrl = " + result.systemIsInPrl + ", .defaultRoamingIndicator = " + result.defaultRoamingIndicator + ", .reasonForDenial = " + result.reasonForDenial + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + ", .nsaState = " + result.nsaState + "}";
    }

    public final String dataRegStateResultToString(HwDataRegStateResult_1_1 result) {
        return "" + "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .reasonDataDenied = " + result.reasonDataDenied + ", .maxDataCalls = " + result.maxDataCalls + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + ", .nsaState = " + result.nsaState + "}";
    }

    private static String cellIdentityToString(CellIdentity cellIdentity) {
        return "{" + ".cellInfoType = " + CellInfoType.toString(cellIdentity.cellInfoType) + ", .cellIdentityGsm = " + processCellIdentityGsmArray(cellIdentity.cellIdentityGsm) + ", .cellIdentityWcdma = " + processCellIdentityWcdmaArray(cellIdentity.cellIdentityWcdma) + ", .cellIdentityCdma = " + processCellIdentityCdmaArray(cellIdentity.cellIdentityCdma) + ", .cellIdentityLte = " + processCellIdentityLteArray(cellIdentity.cellIdentityLte) + ", .cellIdentityTdscdma = " + processCellIdentityTdscdmaArray(cellIdentity.cellIdentityTdscdma) + "}";
    }

    private static String cellIdentityToString(HwCellIdentity_1_1 cellIdentity) {
        return "{" + ".HwCellIdentity_1_1 = " + HwCellInfoType_1_1.toString(cellIdentity.cellInfoType) + ", .cellIdentityGsm = " + processCellIdentityGsmArray(cellIdentity.cellIdentityGsm) + ", .cellIdentityWcdma = " + processCellIdentityWcdmaArray(cellIdentity.cellIdentityWcdma) + ", .cellIdentityCdma = " + processCellIdentityCdmaArray(cellIdentity.cellIdentityCdma) + ", .cellIdentityLte = " + processCellIdentityLteArray(cellIdentity.cellIdentityLte) + ", .cellIdentityTdscdma = " + processCellIdentityTdscdmaArray(cellIdentity.cellIdentityTdscdma) + ", .cellIdentityNr = " + processCellIdentityNrArray(cellIdentity.cellIdentityNr) + "}";
    }

    private static String processCellIdentityGsmArray(ArrayList<CellIdentityGsm> cellIdentityGsm) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityGsm.size();
        for (int i = 0; i < size; i++) {
            CellIdentityGsm gsmCellIdentity = cellIdentityGsm.get(i);
            if (gsmCellIdentity != null && (gsmCellIdentity instanceof CellIdentityGsm)) {
                builder.append(cellIdentityGsmToString(gsmCellIdentity));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityGsmToString(CellIdentityGsm cellIdentityGsm) {
        return "{" + ".mcc = " + cellIdentityGsm.mcc + ", .mnc = " + cellIdentityGsm.mnc + ", .lac = " + "***" + ", .cid = " + "***" + ", .arfcn = " + cellIdentityGsm.arfcn + ", .bsic = " + cellIdentityGsm.bsic + "}";
    }

    private static String processCellIdentityWcdmaArray(ArrayList<CellIdentityWcdma> cellIdentityWcdma) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityWcdma.size();
        for (int i = 0; i < size; i++) {
            CellIdentityWcdma wcdmaCellIdentity = cellIdentityWcdma.get(i);
            if (wcdmaCellIdentity != null && (wcdmaCellIdentity instanceof CellIdentityWcdma)) {
                builder.append(cellIdentityWcdmaToString(wcdmaCellIdentity));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityWcdmaToString(CellIdentityWcdma cellIdentityWcdma) {
        return "{" + ".mcc = " + cellIdentityWcdma.mcc + ", .mnc = " + cellIdentityWcdma.mnc + ", .lac = " + "***" + ", .cid = " + "***" + ", .psc = " + cellIdentityWcdma.psc + ", .uarfcn = " + cellIdentityWcdma.uarfcn + "}";
    }

    private static String processCellIdentityCdmaArray(ArrayList<CellIdentityCdma> cellIdentityCdma) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityCdma.size();
        for (int i = 0; i < size; i++) {
            CellIdentityCdma cdmaCellIdentity = cellIdentityCdma.get(i);
            if (cdmaCellIdentity != null && (cdmaCellIdentity instanceof CellIdentityCdma)) {
                builder.append(cellIdentityCdmaToString(cdmaCellIdentity));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private static String cellIdentityCdmaToString(CellIdentityCdma cellIdentityCdma) {
        return ".networkId = " + cellIdentityCdma.networkId + ", .systemId = " + cellIdentityCdma.systemId + ", .baseStationId = " + "***" + ", .longitude = " + "***" + ", .latitude = " + "***";
    }

    private static String processCellIdentityLteArray(ArrayList<CellIdentityLte> cellIdentityLte) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityLte.size();
        for (int i = 0; i < size; i++) {
            CellIdentityLte lteCellIdentity = cellIdentityLte.get(i);
            if (lteCellIdentity != null && (lteCellIdentity instanceof CellIdentityLte)) {
                builder.append(cellIdentityLteToString(lteCellIdentity));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityLteToString(CellIdentityLte cellIdentityLte) {
        return "{" + ".mcc = " + cellIdentityLte.mcc + ", .mnc = " + cellIdentityLte.mnc + ", .ci = " + "***" + ", .pci = " + cellIdentityLte.pci + ", .tac = " + "***" + ", .earfcn = " + cellIdentityLte.earfcn + "}";
    }

    private static String processCellIdentityTdscdmaArray(ArrayList<CellIdentityTdscdma> cellIdentityTdscdma) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityTdscdma.size();
        for (int i = 0; i < size; i++) {
            CellIdentityTdscdma tdscdmaCellIdentity = cellIdentityTdscdma.get(i);
            if (tdscdmaCellIdentity != null && (tdscdmaCellIdentity instanceof CellIdentityTdscdma)) {
                builder.append(cellIdentityTdscdmaToString(tdscdmaCellIdentity));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityTdscdmaToString(CellIdentityTdscdma cellIdentityTdscdma) {
        return "{" + ".mcc = " + cellIdentityTdscdma.mcc + ", .mnc = " + cellIdentityTdscdma.mnc + ", .lac = " + "***" + ", .cid = " + "***" + ", .cpid = " + cellIdentityTdscdma.cpid + "}";
    }

    private static String processCellIdentityNrArray(ArrayList<HwCellIdentityNr> cellIdentityNr) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityNr.size();
        for (int i = 0; i < size; i++) {
            HwCellIdentityNr nrCellIdentity = cellIdentityNr.get(i);
            if (nrCellIdentity != null && (nrCellIdentity instanceof HwCellIdentityNr)) {
                builder.append(cellIdentityNrToString(nrCellIdentity));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityNrToString(HwCellIdentityNr cellIdentityNr) {
        return "{" + ".mcc = " + cellIdentityNr.mcc + ", .mnc = " + cellIdentityNr.mnc + ", .ci = " + "***" + ", .pci = " + cellIdentityNr.pci + ", .tac = " + "***" + ", .earfcn = " + cellIdentityNr.earfcn + "}";
    }

    private static String dataCallResultToString(SetupDataCallResult result) {
        return "" + "{" + ".status = " + DataCallFailCause.toString(result.status) + ", .suggestedRetryTime = " + result.suggestedRetryTime + ", .cid = " + result.cid + ", .active = " + result.active + ", .type = " + result.type + ", .ifname = " + result.ifname + ", .addresses = *" + ", .dnses = " + result.dnses + ", .gateways = *" + ", .pcscf = " + result.pcscf + ", .mtu = " + result.mtu + "}";
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
