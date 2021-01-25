package com.android.server.wifi;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class CellularLinkLayerStatsCollector {
    private static final boolean DBG = false;
    private static final String TAG = "CellStatsCollector";
    private int mCachedDefaultDataSubId = -1;
    private TelephonyManager mCachedDefaultDataTelephonyManager = null;
    private Context mContext;
    private int mLastDataNetworkType = 0;
    private CellInfo mLastPrimaryCellInfo = null;
    private SubscriptionManager mSubManager = null;

    public CellularLinkLayerStatsCollector(Context context) {
        this.mContext = context;
    }

    public CellularLinkLayerStats update() {
        CellularLinkLayerStats cellStats = new CellularLinkLayerStats();
        retrieveDefaultDataTelephonyManager();
        TelephonyManager telephonyManager = this.mCachedDefaultDataTelephonyManager;
        if (telephonyManager == null) {
            return cellStats;
        }
        SignalStrength signalStrength = telephonyManager.getSignalStrength();
        List<CellSignalStrength> cssList = null;
        if (signalStrength != null) {
            cssList = signalStrength.getCellSignalStrengths();
        }
        if (this.mCachedDefaultDataTelephonyManager.getDataNetworkType() == 0 || cssList == null || cssList.size() == 0) {
            this.mLastPrimaryCellInfo = null;
            this.mLastDataNetworkType = 0;
            return cellStats;
        }
        CellSignalStrength primaryCss = cssList.get(0);
        cellStats.setSignalStrengthDbm(primaryCss.getDbm());
        updateSignalStrengthDbAndNetworkTypeOfCellStats(primaryCss, cellStats);
        int networkType = cellStats.getDataNetworkType();
        CellInfo primaryCellInfo = getPrimaryCellInfo(this.mCachedDefaultDataTelephonyManager, networkType);
        cellStats.setIsSameRegisteredCell(getIsSameRegisteredCell(primaryCellInfo, networkType));
        this.mLastPrimaryCellInfo = primaryCellInfo;
        this.mLastDataNetworkType = networkType;
        return cellStats;
    }

    private void retrieveDefaultDataTelephonyManager() {
        if (initSubManager()) {
            SubscriptionManager subscriptionManager = this.mSubManager;
            int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (defaultDataSubId == -1) {
                this.mCachedDefaultDataTelephonyManager = null;
            } else if (defaultDataSubId != this.mCachedDefaultDataSubId || this.mCachedDefaultDataTelephonyManager == null) {
                this.mCachedDefaultDataSubId = defaultDataSubId;
                TelephonyManager defaultSubTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
                SubscriptionManager subscriptionManager2 = this.mSubManager;
                if (defaultDataSubId == SubscriptionManager.getDefaultSubscriptionId()) {
                    this.mCachedDefaultDataTelephonyManager = defaultSubTelephonyManager;
                } else {
                    this.mCachedDefaultDataTelephonyManager = defaultSubTelephonyManager.createForSubscriptionId(defaultDataSubId);
                }
            }
        }
    }

    private boolean initSubManager() {
        if (this.mSubManager == null) {
            this.mSubManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
        }
        return this.mSubManager != null;
    }

    private void updateSignalStrengthDbAndNetworkTypeOfCellStats(CellSignalStrength primaryCss, CellularLinkLayerStats cellStats) {
        if (primaryCss instanceof CellSignalStrengthLte) {
            cellStats.setSignalStrengthDb(((CellSignalStrengthLte) primaryCss).getRsrq());
            cellStats.setDataNetworkType(13);
        } else if (primaryCss instanceof CellSignalStrengthCdma) {
            CellSignalStrengthCdma cssCdma = (CellSignalStrengthCdma) primaryCss;
            int evdoSnr = cssCdma.getEvdoSnr();
            int cdmaEcio = cssCdma.getCdmaEcio();
            if (evdoSnr != Integer.MAX_VALUE) {
                cellStats.setSignalStrengthDb(evdoSnr);
                cellStats.setDataNetworkType(5);
                return;
            }
            cellStats.setSignalStrengthDb(cdmaEcio);
            cellStats.setDataNetworkType(4);
        } else if (primaryCss instanceof CellSignalStrengthTdscdma) {
            cellStats.setDataNetworkType(17);
        } else if (primaryCss instanceof CellSignalStrengthWcdma) {
            cellStats.setSignalStrengthDb(((CellSignalStrengthWcdma) primaryCss).getEcNo());
            cellStats.setDataNetworkType(3);
        } else if (primaryCss instanceof CellSignalStrengthGsm) {
            cellStats.setDataNetworkType(16);
        } else if (primaryCss instanceof CellSignalStrengthNr) {
            cellStats.setSignalStrengthDb(((CellSignalStrengthNr) primaryCss).getCsiSinr());
            cellStats.setDataNetworkType(20);
        } else {
            Log.e(TAG, "invalid CellSignalStrength");
        }
    }

    private CellInfo getPrimaryCellInfo(TelephonyManager defaultDataTelephonyManager, int networkType) {
        List<CellInfo> cellInfoList = getRegisteredCellInfo(defaultDataTelephonyManager);
        int cilSize = cellInfoList.size();
        CellInfo primaryCellInfo = null;
        for (int i = 0; i < cilSize; i++) {
            CellInfo cellInfo = cellInfoList.get(i);
            if (((cellInfo instanceof CellInfoTdscdma) && networkType == 17) || (((cellInfo instanceof CellInfoCdma) && (networkType == 4 || networkType == 5)) || (((cellInfo instanceof CellInfoLte) && networkType == 13) || (((cellInfo instanceof CellInfoWcdma) && networkType == 3) || (((cellInfo instanceof CellInfoGsm) && networkType == 16) || ((cellInfo instanceof CellInfoNr) && networkType == 20)))))) {
                primaryCellInfo = cellInfo;
            }
        }
        return primaryCellInfo;
    }

    private boolean getIsSameRegisteredCell(CellInfo primaryCellInfo, int networkType) {
        boolean isSameRegisteredCell;
        if (primaryCellInfo != null && this.mLastPrimaryCellInfo != null) {
            isSameRegisteredCell = primaryCellInfo.getCellIdentity().equals(this.mLastPrimaryCellInfo.getCellIdentity());
        } else if (primaryCellInfo == null && this.mLastPrimaryCellInfo == null) {
            isSameRegisteredCell = true;
        } else {
            isSameRegisteredCell = false;
        }
        int i = this.mLastDataNetworkType;
        if (i == 0 || i != networkType) {
            return false;
        }
        return isSameRegisteredCell;
    }

    private List<CellInfo> getRegisteredCellInfo(TelephonyManager defaultDataTelephonyManager) {
        List<CellInfo> allList = defaultDataTelephonyManager.getAllCellInfo();
        List<CellInfo> cellInfoList = new ArrayList<>();
        if (allList == null) {
            Log.e(TAG, "allList is null");
            return cellInfoList;
        }
        for (CellInfo ci : allList) {
            if (ci.isRegistered()) {
                cellInfoList.add(ci);
            }
        }
        return cellInfoList;
    }
}
