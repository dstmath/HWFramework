package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import java.util.Collections;
import java.util.List;

public class HiCoexCellularState {
    public static final int NETWORK_TYPE_LTE = 3;
    public static final int NETWORK_TYPE_NONR = 0;
    public static final int NETWORK_TYPE_NSA = 1;
    public static final int NETWORK_TYPE_SA = 2;
    private static final int NSA_STATE_END = 5;
    private static final int NSA_STATE_START = 2;
    public static final int STATE_DATA_CONNECTED = 1;
    public static final int STATE_DATA_DISCONNECTED = 2;
    public static final int STATE_IN_SERVICE = 0;
    public static final int STATE_OUT_OF_SERVICE = 1;
    public static final int STATE_POWER_OFF = 3;
    private static final String TAG = "HiCoexCellularState";
    private int mCellFreq = 0;
    private int mCellId = 0;
    private Context mContext;
    private int mCurSceneNo = 0;
    private int mDataConnectState;
    private int mDataNetworkType;
    private int mDataRegState;
    private int mNetworkType;
    private int mNsaState;
    private String mOperatorNumeric;
    private int mPreSceneNo = 0;
    private ServiceState mServiceState;
    private int mSubId = 0;
    private TelephonyManager mTelephonyManager;
    private int mVoiceRegState;

    public HiCoexCellularState(int subId, Context context) {
        this.mSubId = subId;
        this.mContext = context;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    private void updateCellInfoLte(int idx, CellInfo cellInfo) {
        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
        if (cellInfoLte == null) {
            HiCoexUtils.logE(TAG, "updateCellInfoLte CellInfoLte is null");
            return;
        }
        CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
        if (cellIdentityLte == null) {
            HiCoexUtils.logE(TAG, "updateCellInfoLte CellIdentityLte is null");
            return;
        }
        this.mCellId = cellIdentityLte.getCi();
        int cellFreq = cellIdentityLte.getEarfcn();
        HiCoexUtils.logD(TAG, "cellInfoLte idx:" + idx + ",id:" + this.mCellId + ",freq:" + cellFreq);
    }

    private void updateCellInfoNr(int idx, CellInfo cellInfo) {
        CellInfoNr cellInfoNr = (CellInfoNr) cellInfo;
        if (cellInfoNr == null) {
            HiCoexUtils.logE(TAG, "updateCellInfoNr CellInfoNr is null");
            return;
        }
        CellIdentityNr cellIdentityNr = (CellIdentityNr) cellInfoNr.getCellIdentity();
        if (cellIdentityNr == null) {
            HiCoexUtils.logE(TAG, "updateCellInfoNr CellIdentityNr is null");
            return;
        }
        this.mCellId = cellIdentityNr.getPci();
        this.mCellFreq = cellIdentityNr.getNrarfcn();
        HiCoexUtils.logD(TAG, "CellInfoNr idx:" + idx + ",id:" + this.mCellId + ",freq:" + this.mCellFreq);
    }

    private void updateCellInfo(int innerType, List<CellInfo> cellInfoList) {
        this.mCellId = 0;
        this.mCellFreq = 0;
        int hitCount = 0;
        int size = cellInfoList.size();
        HiCoexUtils.logD(TAG, "subid:" + this.mSubId + ",innertype:" + innerType + ",size:" + size);
        for (int i = 0; i < size; i++) {
            CellInfo cellInfo = cellInfoList.get(i);
            if (cellInfo == null) {
                HiCoexUtils.logD(TAG, "idx:" + i + ", isRegistered: false");
            } else if (innerType == 20 && (cellInfo instanceof CellInfoNr)) {
                hitCount++;
                updateCellInfoNr(i, cellInfo);
            } else if (innerType != 13 || !(cellInfo instanceof CellInfoLte)) {
                HiCoexUtils.logD(TAG, "idx:" + i + ", not matched, type:" + this.mNetworkType);
            } else {
                updateCellInfoLte(i, cellInfo);
            }
            if (hitCount > this.mSubId) {
                return;
            }
        }
    }

    private int getInnerNetworkType(int type) {
        if (type == 13) {
            return 13;
        }
        if (type != 20) {
            return 0;
        }
        return 20;
    }

    private void updateSceneNo() {
        this.mPreSceneNo = this.mCurSceneNo;
        this.mCurSceneNo = HiCoexCellularScore.getSceneNo(this);
        HiCoexUtils.logD(TAG, "preSceneNo:" + this.mPreSceneNo + ",curSceneNo:" + this.mCurSceneNo);
    }

    public int getSubId() {
        return this.mSubId;
    }

    public void updateServiceState(ServiceState state) {
        if (this.mTelephonyManager == null || state == null) {
            HiCoexUtils.logE(TAG, "updateServiceState TelephonyManager or ServiceState is null");
            return;
        }
        this.mVoiceRegState = state.getVoiceRegState();
        this.mDataRegState = state.getDataRegState();
        this.mNsaState = state.getNsaState();
        this.mDataNetworkType = state.getDataNetworkType();
        int i = this.mNsaState;
        if (i < 2 || i > 5) {
            this.mNetworkType = this.mTelephonyManager.getNetworkType(this.mSubId);
        } else {
            this.mNetworkType = state.getConfigRadioTechnology();
        }
        HiCoexUtils.logD(TAG, "onServiceStateChanged on mSubId:" + this.mSubId + ",voicestate:" + this.mVoiceRegState + ",datastate:" + this.mDataRegState + ",networktype:" + this.mNetworkType + ",datatype:" + this.mDataNetworkType + ",nsastate:" + this.mNsaState);
        updateSceneNo();
        if (getState() != 0) {
            HiCoexUtils.logD(TAG, "updateServiceState: is not in service");
            return;
        }
        this.mOperatorNumeric = state.getOperatorNumeric();
        List<CellInfo> cellInfoList = this.mTelephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            HiCoexUtils.logD(TAG, "updateServiceState: cellInfoList is null");
        } else {
            updateCellInfo(getInnerNetworkType(this.mNetworkType), cellInfoList);
        }
    }

    public void updateDataConnection(int state) {
        if (state == 1 || state == 2) {
            this.mDataConnectState = state;
            updateSceneNo();
        }
    }

    public int getState() {
        int i = this.mVoiceRegState;
        if (i == 3) {
            return 3;
        }
        if (i == 0 || this.mDataRegState == 0) {
            return 0;
        }
        return 1;
    }

    public boolean hasSceneChanged() {
        return this.mPreSceneNo != this.mCurSceneNo;
    }

    public int getNetworkType() {
        if (this.mVoiceRegState != 0 && this.mDataRegState != 0) {
            return 0;
        }
        int i = this.mNetworkType;
        if (i == 13) {
            return 3;
        }
        if (i != 20) {
            return 0;
        }
        int i2 = this.mNsaState;
        if (i2 >= 2 && i2 <= 5) {
            return 1;
        }
        if (this.mNsaState == 0) {
            return 2;
        }
        return 0;
    }

    public boolean isNrNetwork() {
        int state = getNetworkType();
        return state == 1 || state == 2;
    }

    public boolean getDataConnected() {
        return this.mDataConnectState == 1;
    }

    public int getFreq() {
        return this.mCellFreq;
    }

    public List<Integer> getRecommendWiFiChannel() {
        if (getState() != 0) {
            return Collections.emptyList();
        }
        int networkType = getNetworkType();
        if (networkType != 2 && networkType != 1) {
            return Collections.emptyList();
        }
        int freq = getFreq();
        if (freq == 0 && HiCoexUtils.isCmccOperator(this.mOperatorNumeric)) {
            freq = HiCoexUtils.CELL_FREQ_NR41_DEFAULT;
        }
        return HiCoexUtils.getRecommendWiFiChannel(freq);
    }

    public List<Integer> getDeprecatedWiFiChannel() {
        if (getState() != 0) {
            HiCoexUtils.logD(TAG, "getDeprecatedWiFiChannel is not in service");
            return Collections.emptyList();
        }
        int networkType = getNetworkType();
        if (networkType == 2 || networkType == 1) {
            int nrArfcn = getFreq();
            int band = 41;
            if (nrArfcn != 0) {
                int tmpBand = HiCoexUtils.calculateNrBandByNrArfcn(nrArfcn);
                HiCoexUtils.logD(TAG, "nrArfcn:" + nrArfcn + ", tmpBand:" + tmpBand);
                if (tmpBand != -1) {
                    band = tmpBand;
                }
            }
            List<Integer> channels = HiCoexUtils.getDeprecatedWiFiChannel(band);
            if (channels != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Integer channel : channels) {
                    stringBuilder.append(channel.toString());
                    stringBuilder.append(", ");
                }
                HiCoexUtils.logD(TAG, "getDeprecatedWiFiChannel:" + stringBuilder.toString());
            }
            return channels;
        }
        HiCoexUtils.logD(TAG, "networkType:" + networkType);
        return Collections.emptyList();
    }
}
