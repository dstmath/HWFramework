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
import android.hardware.radio.V1_4.CellIdentityNr;
import android.telephony.Rlog;
import java.util.ArrayList;
import vendor.huawei.hardware.hisiradio.V1_2.HwDataRegStateResult13;

public class HwResponseInfoToString {
    private static final String LOG_TAG = "HwResponseInfoToString";
    private static HwResponseInfoToString mInstance = new HwResponseInfoToString();

    public static HwResponseInfoToString getDefault() {
        HwResponseInfoToString hwResponseInfoToString = mInstance;
        if (hwResponseInfoToString != null) {
            return hwResponseInfoToString;
        }
        Rlog.d(LOG_TAG, "mInstance has not init.");
        return new HwResponseInfoToString();
    }

    /* JADX INFO: Multiple debug info for r0v20 java.lang.String: [D('results' java.util.ArrayList<java.lang.Object>), D('s' java.lang.String)] */
    public String retToStringEx(int req, Object ret) {
        if (req == 95 || req == 98 || req == 529 || req == 100 || req == 101) {
            return "";
        }
        if (req == 27 && (ret instanceof SetupDataCallResult)) {
            return dataCallResultToString((SetupDataCallResult) ret);
        }
        if (req == 1010 && (ret instanceof ArrayList)) {
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
            return sb.toString();
        } else if (req == 20) {
            return handleVoiceRegState(ret);
        } else {
            if (req == 21) {
                return handleDataRegState(ret);
            }
            return ret.toString();
        }
    }

    private String handleVoiceRegState(Object ret) {
        if (ret instanceof VoiceRegStateResult) {
            return voiceRegStateResultToString((VoiceRegStateResult) ret);
        }
        if (ret instanceof android.hardware.radio.V1_2.VoiceRegStateResult) {
            return voiceRegStateResultToString12((android.hardware.radio.V1_2.VoiceRegStateResult) ret);
        }
        return "****";
    }

    private String handleDataRegState(Object ret) {
        if (ret instanceof DataRegStateResult) {
            return dataRegStateResultToString((DataRegStateResult) ret);
        }
        if (ret instanceof HwDataRegStateResult13) {
            return dataRegStateResultToString((HwDataRegStateResult13) ret);
        }
        if (ret instanceof android.hardware.radio.V1_4.DataRegStateResult) {
            return dataRegStateResultToString14((android.hardware.radio.V1_4.DataRegStateResult) ret);
        }
        return "****";
    }

    private static String voiceRegStateResultToString(VoiceRegStateResult result) {
        return "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .cssSupported = " + result.cssSupported + ", .roamingIndicator = " + result.roamingIndicator + ", .systemIsInPrl = " + result.systemIsInPrl + ", .defaultRoamingIndicator = " + result.defaultRoamingIndicator + ", .reasonForDenial = " + result.reasonForDenial + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + "}";
    }

    public final String dataRegStateResultToString(DataRegStateResult result) {
        return "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .reasonDataDenied = " + result.reasonDataDenied + ", .maxDataCalls = " + result.maxDataCalls + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + "}";
    }

    private String voiceRegStateResultToString12(android.hardware.radio.V1_2.VoiceRegStateResult result) {
        return "{" + ".regState = " + RegState.toString(result.regState) + ", .rat = " + result.rat + ", .cssSupported = " + result.cssSupported + ", .roamingIndicator = " + result.roamingIndicator + ", .systemIsInPrl = " + result.systemIsInPrl + ", .defaultRoamingIndicator = " + result.defaultRoamingIndicator + ", .reasonForDenial = " + result.reasonForDenial + ", .cellIdentity = " + cellIdentityToString(result.cellIdentity) + "}";
    }

    private String dataRegStateResultToString(HwDataRegStateResult13 result) {
        return "{" + ".regState = " + RegState.toString(result.base.base.regState) + ", .rat = " + result.base.base.rat + ", .reasonDataDenied = " + result.base.base.reasonDataDenied + ", .maxDataCalls = " + result.base.base.maxDataCalls + ", .cellIdentity = " + cellIdentityToString(result.base.base.cellIdentity) + ", .vopsInfo = " + result.base.vopsInfo + ", .nrIndicators = " + result.base.nrIndicators + ", .cellIdentityNr  = " + cellIdentityNrToString(result.cellIdentityNr) + ", .nsaState = " + result.nsaState + "}";
    }

    private String dataRegStateResultToString14(android.hardware.radio.V1_4.DataRegStateResult result) {
        return "{" + ".regState = " + RegState.toString(result.base.regState) + ", .rat = " + result.base.rat + ", .reasonDataDenied = " + result.base.reasonDataDenied + ", .maxDataCalls = " + result.base.maxDataCalls + ", .cellIdentity = " + cellIdentityToString(result.base.cellIdentity) + ", .vopsInfo = " + result.vopsInfo + ", .nrIndicators = " + result.nrIndicators + "}";
    }

    private static String cellIdentityToString(CellIdentity cellIdentity) {
        return "{" + ".cellInfoType = " + CellInfoType.toString(cellIdentity.cellInfoType) + ", .cellIdentityGsm = " + processCellIdentityGsmArray(cellIdentity.cellIdentityGsm) + ", .cellIdentityWcdma = " + processCellIdentityWcdmaArray(cellIdentity.cellIdentityWcdma) + ", .cellIdentityCdma = " + processCellIdentityCdmaArray(cellIdentity.cellIdentityCdma) + ", .cellIdentityLte = " + processCellIdentityLteArray(cellIdentity.cellIdentityLte) + ", .cellIdentityTdscdma = " + processCellIdentityTdscdmaArray(cellIdentity.cellIdentityTdscdma) + "}";
    }

    private static String cellIdentityToString(android.hardware.radio.V1_2.CellIdentity cellIdentity) {
        return "{" + ".cellInfoType = " + CellInfoType.toString(cellIdentity.cellInfoType) + ", .cellIdentityGsm = " + processCellIdentityGsmArray12(cellIdentity.cellIdentityGsm) + ", .cellIdentityWcdma = " + processCellIdentityWcdmaArray12(cellIdentity.cellIdentityWcdma) + ", .cellIdentityCdma = " + processCellIdentityCdmaArray12(cellIdentity.cellIdentityCdma) + ", .cellIdentityLte = " + processCellIdentityLteArray12(cellIdentity.cellIdentityLte) + ", .cellIdentityTdscdma = " + processCellIdentityTdscdmaArray12(cellIdentity.cellIdentityTdscdma) + "}";
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

    private static String processCellIdentityGsmArray12(ArrayList<android.hardware.radio.V1_2.CellIdentityGsm> cellIdentityGsm) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityGsm.size();
        for (int i = 0; i < size; i++) {
            android.hardware.radio.V1_2.CellIdentityGsm gsmCellIdentity = cellIdentityGsm.get(i);
            if (gsmCellIdentity != null) {
                if (gsmCellIdentity.base != null) {
                    builder.append(cellIdentityGsmToString(gsmCellIdentity.base));
                }
                builder.append(", .operatorNames = ");
                builder.append(gsmCellIdentity.operatorNames);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityGsmToString(CellIdentityGsm cellIdentityGsm) {
        return "{" + ".mcc = " + cellIdentityGsm.mcc + ", .mnc = " + cellIdentityGsm.mnc + ", .lac = " + getPrivateData(Integer.valueOf(cellIdentityGsm.lac)) + ", .cid = " + getPrivateData(Integer.valueOf(cellIdentityGsm.cid)) + ", .arfcn = " + cellIdentityGsm.arfcn + ", .bsic = " + ((int) cellIdentityGsm.bsic) + "}";
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

    private static String processCellIdentityWcdmaArray12(ArrayList<android.hardware.radio.V1_2.CellIdentityWcdma> cellIdentityWcdma) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityWcdma.size();
        for (int i = 0; i < size; i++) {
            android.hardware.radio.V1_2.CellIdentityWcdma wcdmaCellIdentity = cellIdentityWcdma.get(i);
            if (wcdmaCellIdentity != null) {
                if (wcdmaCellIdentity.base != null) {
                    builder.append(cellIdentityWcdmaToString(wcdmaCellIdentity.base));
                }
                builder.append(", .operatorNames = ");
                builder.append(wcdmaCellIdentity.operatorNames);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityWcdmaToString(CellIdentityWcdma cellIdentityWcdma) {
        return "{" + ".mcc = " + cellIdentityWcdma.mcc + ", .mnc = " + cellIdentityWcdma.mnc + ", .lac = " + getPrivateData(Integer.valueOf(cellIdentityWcdma.lac)) + ", .cid = " + getPrivateData(Integer.valueOf(cellIdentityWcdma.cid)) + ", .psc = " + cellIdentityWcdma.psc + ", .uarfcn = " + cellIdentityWcdma.uarfcn + "}";
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

    private static String processCellIdentityCdmaArray12(ArrayList<android.hardware.radio.V1_2.CellIdentityCdma> cellIdentityCdma) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityCdma.size();
        for (int i = 0; i < size; i++) {
            android.hardware.radio.V1_2.CellIdentityCdma cdmaCellIdentity = cellIdentityCdma.get(i);
            if (cdmaCellIdentity != null) {
                if (cdmaCellIdentity.base != null) {
                    builder.append(cellIdentityCdmaToString(cdmaCellIdentity.base));
                }
                builder.append(", .operatorNames = ");
                builder.append(cdmaCellIdentity.operatorNames);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private static String cellIdentityCdmaToString(CellIdentityCdma cellIdentityCdma) {
        return ".networkId = " + cellIdentityCdma.networkId + ", .systemId = " + cellIdentityCdma.systemId + ", .baseStationId = " + getPrivateData(Integer.valueOf(cellIdentityCdma.baseStationId)) + ", .longitude = " + getPrivateData(Integer.valueOf(cellIdentityCdma.longitude)) + ", .latitude = " + getPrivateData(Integer.valueOf(cellIdentityCdma.latitude));
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

    private static String processCellIdentityLteArray12(ArrayList<android.hardware.radio.V1_2.CellIdentityLte> cellIdentityLte) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityLte.size();
        for (int i = 0; i < size; i++) {
            android.hardware.radio.V1_2.CellIdentityLte lteCellIdentity = cellIdentityLte.get(i);
            if (lteCellIdentity != null) {
                if (lteCellIdentity.base != null) {
                    builder.append(cellIdentityLteToString(lteCellIdentity.base));
                }
                builder.append(", .operatorNames = ");
                builder.append(lteCellIdentity.operatorNames);
                builder.append(", .bandwidth = ");
                builder.append(lteCellIdentity.bandwidth);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityLteToString(CellIdentityLte cellIdentityLte) {
        return "{" + ".mcc = " + cellIdentityLte.mcc + ", .mnc = " + cellIdentityLte.mnc + ", .ci = " + getPrivateData(Integer.valueOf(cellIdentityLte.ci)) + ", .pci = " + cellIdentityLte.pci + ", .tac = " + getPrivateData(Integer.valueOf(cellIdentityLte.tac)) + ", .earfcn = " + cellIdentityLte.earfcn + "}";
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

    private static String processCellIdentityTdscdmaArray12(ArrayList<android.hardware.radio.V1_2.CellIdentityTdscdma> cellIdentityTdscdma) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int size = cellIdentityTdscdma.size();
        for (int i = 0; i < size; i++) {
            android.hardware.radio.V1_2.CellIdentityTdscdma tdscdmaCellIdentity = cellIdentityTdscdma.get(i);
            if (tdscdmaCellIdentity != null) {
                if (tdscdmaCellIdentity.base != null) {
                    builder.append(cellIdentityTdscdmaToString(tdscdmaCellIdentity.base));
                }
                builder.append(", .uarfcn = ");
                builder.append(tdscdmaCellIdentity.uarfcn);
                builder.append(", .operatorNames = ");
                builder.append(tdscdmaCellIdentity.operatorNames);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String cellIdentityTdscdmaToString(CellIdentityTdscdma cellIdentityTdscdma) {
        return "{" + ".mcc = " + cellIdentityTdscdma.mcc + ", .mnc = " + cellIdentityTdscdma.mnc + ", .lac = " + getPrivateData(Integer.valueOf(cellIdentityTdscdma.lac)) + ", .cid = " + getPrivateData(Integer.valueOf(cellIdentityTdscdma.cid)) + ", .cpid = " + cellIdentityTdscdma.cpid + "}";
    }

    private static String cellIdentityNrToString(CellIdentityNr cellIdentityNr) {
        return "{" + ".mcc = " + cellIdentityNr.mcc + ", .mnc = " + cellIdentityNr.mnc + ", .nci = " + getPrivateData(Long.valueOf(cellIdentityNr.nci)) + ", .pci = " + getPrivateData(Integer.valueOf(cellIdentityNr.pci)) + ", .tac = " + getPrivateData(Integer.valueOf(cellIdentityNr.tac)) + ", .nrarfcn = " + cellIdentityNr.nrarfcn + ", .operatorNames = " + cellIdentityNr.operatorNames + "}";
    }

    private static String dataCallResultToString(SetupDataCallResult result) {
        return "" + "{" + ".status = " + DataCallFailCause.toString(result.status) + ", .suggestedRetryTime = " + result.suggestedRetryTime + ", .cid = " + result.cid + ", .active = " + result.active + ", .type = " + result.type + ", .ifname = " + result.ifname + ", .addresses = *" + ", .dnses = " + result.dnses + ", .gateways = *" + ", .pcscf = " + result.pcscf + ", .mtu = " + result.mtu + "}";
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private static String getPrivateData(Object content) {
        int value;
        if (content == null) {
            return "";
        }
        String strContent = String.valueOf(content);
        if ((content instanceof Integer) && ((value = ((Integer) content).intValue()) == -1 || value == Integer.MAX_VALUE)) {
            return "INVALID";
        }
        int strLength = strContent.length();
        if (strLength <= 0) {
            return "######";
        }
        if (strLength <= 1) {
            return "*";
        }
        if (strLength <= 4) {
            return getStarByNumber(strLength - 1) + strContent.charAt(strLength - 1);
        }
        return getStarByNumber(strLength / 2) + strContent.substring(strLength / 2);
    }

    private static String getStarByNumber(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append("*");
        }
        return result.toString();
    }
}
