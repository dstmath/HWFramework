package com.android.server.hidata.wavemapping.chr;

import android.os.Bundle;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.FastBack2LteChrDao;
import com.android.server.hidata.wavemapping.dao.HisQoeChrDao;
import com.android.server.hidata.wavemapping.dao.LocationDao;

public class BuildBenefitStatisticsChrInfo {
    public static final String E909002049_APP_QOE_BENEFIT_CLASS = "qoe";
    public static final String E909002049_FAST_BACK_TO_LTE_CLASS = "bak4g";
    public static final String E909002049_FREQ_LOC_REC_CLASS = "locRec";
    public static final String E909002049_LOCATION_TINYINT = "loc";
    public static final String E909002049_LOW_POWER_STATISTIC_CLASS = "pwr";
    public static final String E909002049_SIGNAL_FINGERPRINT_CLASS = "finger";
    public static final String E909002049_USR_PREF_STATISTIC_CLASS = "usr";
    public static final String E909009041_DURATION_INT = "dur";
    public static final String E909009041_ENTERY_INT = "entery";
    public static final String E909009041_FIRSTREPORT_INT = "frstRpt";
    public static final String E909009041_LEAVE_INT = "leave";
    public static final String E909009041_SPACECHANGE_INT = "spcChg";
    public static final String E909009041_SPACELEAVE_INT = "spcLeave";
    public static final String E909009050_DATARX_INT = "dataRx";
    public static final String E909009050_DATATX_INT = "dataTx";
    public static final String E909009050_GOOD_INT = "good";
    public static final String E909009050_POOR_INT = "poor";
    public static final String E909009050_QUERYCNT_INT = "queryCnt";
    public static final String E909009050_UNKNOWNDB_INT = "unknownDB";
    public static final String E909009050_UNKNOWNSPACE_INT = "unknownSpace";
    public static final String E909009051_AUTOFAIL_INT = "autoFail";
    public static final String E909009051_AUTOSUCC_INT = "autoSucc";
    public static final String E909009051_MANUALSUCC_INT = "manualSucc";
    public static final String E909009051_NOSWITCHFAIL_INT = "noSwitchFail";
    public static final String E909009051_QUERYCNT_INT = "queryCnt";
    public static final String E909009051_RESCNT_INT = "resCnt";
    public static final String E909009051_TOTALSWITCH_INT = "totalSwitch";
    public static final String E909009051_UNKNOWNDB_INT = "unknownDB";
    public static final String E909009051_UNKNOWNSPACE_INT = "unknownSpace";
    public static final String E909009052_ALREADYBEST_INT = "best";
    public static final String E909009052_BACK_INT = "bak";
    public static final String E909009052_DATARX_INT = "dRx";
    public static final String E909009052_DATATX_INT = "dTx";
    public static final String E909009052_DURATION_INT = "dur";
    public static final String E909009052_NOTREACH_INT = "noRech";
    public static final String E909009052_OFFSET_INT = "offset";
    public static final String E909009052_TOTALSWITCH_INT = "switch";
    public static final String E909009052_UNKNOWNDB_INT = "uknDB";
    public static final String E909009052_UNKNOWNSPACE_INT = "uknSpc";
    private static final int FREQUENT_LOCATION_OTHER = 2;
    private static final String KEY_BENEFIT_STATISTICS_CHR_INFO = "FREQLOCATION";
    private static final String KEY_CELL_4G = "cells";
    private static final String KEY_FAST_BACK = "fstBak";
    private static final String KEY_FL_DURATION_INT = "DURATION";
    private static final String KEY_FL_ENTERY_INT = "ENTERY";
    private static final String KEY_FL_FIRST_REPORT_INT = "FIRSTREPORT";
    private static final String KEY_FL_LEAVE_INT = "LEAVE";
    private static final String KEY_FL_SPACE_CHANGE_INT = "SPACECHANGE";
    private static final String KEY_FL_SPACE_LEAVE_INT = "SPACELEAVE";
    private static final String KEY_IN_LTE_CNT = "inL";
    private static final String KEY_LOW_RAT_CNT = "low";
    private static final String KEY_LP_ALREADY_BEST_INT = "LPALREADYBEST";
    private static final String KEY_LP_BACK_INT = "LPBACK";
    private static final String KEY_LP_DATA_RX_INT = "LPDATARX";
    private static final String KEY_LP_DATA_TX_INT = "LPDATATX";
    private static final String KEY_LP_DURATION_INT = "LPDURATION";
    private static final String KEY_LP_NOT_REACH_INT = "LPNOTREACH";
    private static final String KEY_LP_OFFSET_INT = "LPOFFSET";
    private static final String KEY_LP_TOTAL_SWITCH_INT = "LPTOTALSWITCH";
    private static final String KEY_LP_UNKNOWN_DB_INT = "LPUNKNOWNDB";
    private static final String KEY_LP_UNKNOWN_SPACE_INT = "LPUNKNOWNSPACE";
    private static final String KEY_OUT_LTE_CNT = "outL";
    private static final String KEY_REF_CNT = "ref";
    private static final String KEY_SUCCESS_BACK = "sucBak";
    private static final String KEY_UNKNOWN_DB = "uknDB";
    private static final String KEY_UNKNOWN_SPACE = "uknSpc";
    private static final String KEY_UPAUTO_FAIL_INT = "UPAUTOFAIL";
    private static final String KEY_UP_AUTO_SUCC_INT = "UPAUTOSUCC";
    private static final String KEY_UP_MANUAL_SUCC_INT = "UPMANUALSUCC";
    private static final String KEY_UP_NO_SWITCH_FAIL_INT = "UPNOSWITCHFAIL";
    private static final String KEY_UP_QUERY_CNT_INT = "UPQRYCNT";
    private static final String KEY_UP_RES_CNT_INT = "UPRESCNT";
    private static final String KEY_UP_TOTAL_SWITCH_IN = "UPTOTALSWITCH";
    private static final String KEY_UP_UNKNOWN_DB_INT = "UPUNKNOWNDB";
    private static final String KEY_UP_UNKNOWN_SPACE_INT = "UPUNKNOWNSPACE";
    public static final int MSG_WAVEMAPPING_APPQOE_BENEFIT_TABLE_CLASSID = 909009050;
    public static final int MSG_WAVEMAPPING_BENEFIT_STATISTICS_EVENTID = 909002049;
    public static final int MSG_WAVEMAPPING_FAST_BACK_TO_LTE_STATISTIC_CLASSID = 909009053;
    public static final int MSG_WAVEMAPPING_FREQ_LOC_REC_CLASSID = 909009041;
    public static final int MSG_WAVEMAPPING_LOW_POWER_STATISTIC_CLASSID = 909009052;
    public static final int MSG_WAVEMAPPING_SIGNAL_FINGERPRINT_STATISTIC_CLASSID = 909009054;
    public static final int MSG_WAVEMAPPING_USR_PREF_STATISTIC_CLASSID = 909009051;
    private FastBack2LteChrDao mFastBack2LteChrDao = new FastBack2LteChrDao();
    private HisQoeChrDao mHisQoeChrDao = new HisQoeChrDao();
    private LocationDao mLocationDao = new LocationDao();

    public boolean commitChr(String freqLocation) {
        if (freqLocation == null) {
            return false;
        }
        IMonitor.EventStream buildBenefitEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_BENEFIT_STATISTICS_EVENTID);
        IMonitor.EventStream freqLocRecEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_FREQ_LOC_REC_CLASSID);
        IMonitor.EventStream appQoeBenefitEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_APPQOE_BENEFIT_TABLE_CLASSID);
        IMonitor.EventStream userPrefEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_USR_PREF_STATISTIC_CLASSID);
        IMonitor.EventStream lowPowerEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_LOW_POWER_STATISTIC_CLASSID);
        IMonitor.EventStream fastBack4gEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_FAST_BACK_TO_LTE_STATISTIC_CLASSID);
        if (buildBenefitEstream == null || freqLocRecEstream == null || appQoeBenefitEstream == null || userPrefEstream == null || lowPowerEstream == null || fastBack4gEstream == null) {
            return false;
        }
        setLocationParam(buildBenefitEstream, freqLocation);
        Bundle results = this.mLocationDao.findChrByFreqLoc(freqLocation);
        if (results != null && results.containsKey(KEY_BENEFIT_STATISTICS_CHR_INFO)) {
            setFreqLocRecParam(freqLocRecEstream, buildBenefitEstream, results, freqLocation);
            setUserPreferenceParam(userPrefEstream, buildBenefitEstream, results);
            setLowPowerParam(lowPowerEstream, buildBenefitEstream, results);
        }
        if (this.mHisQoeChrDao.getCountersByLocation(freqLocation)) {
            setAppQoeBenefitParam(appQoeBenefitEstream, buildBenefitEstream);
        }
        this.mFastBack2LteChrDao.getCountersByLocation(freqLocation);
        setBackTo4gParam(fastBack4gEstream, buildBenefitEstream);
        IMonitor.EventStream signalFingerprintEstream = new CollectFingerChrService().getCollectFingerChrEventStreamByPlace(freqLocation);
        setFingerprintParam(signalFingerprintEstream, buildBenefitEstream);
        boolean isRet = IMonitor.sendEvent(buildBenefitEstream);
        IMonitor.closeEventStream(buildBenefitEstream);
        IMonitor.closeEventStream(freqLocRecEstream);
        IMonitor.closeEventStream(appQoeBenefitEstream);
        IMonitor.closeEventStream(userPrefEstream);
        IMonitor.closeEventStream(lowPowerEstream);
        IMonitor.closeEventStream(fastBack4gEstream);
        closeFingerprintEstream(signalFingerprintEstream);
        this.mFastBack2LteChrDao.resetRecord(freqLocation);
        this.mLocationDao.resetChrByFreqLoc(freqLocation);
        this.mHisQoeChrDao.resetRecord(freqLocation);
        return isRet;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0047  */
    private void setLocationParam(IMonitor.EventStream buildBenefitEstream, String freqLocation) {
        char c;
        int hashCode = freqLocation.hashCode();
        if (hashCode != -1966460228) {
            if (hashCode != 2223327) {
                if (hashCode == 75532016 && freqLocation.equals(Constant.NAME_FREQLOCATION_OTHER)) {
                    c = 2;
                    if (c != 0) {
                        buildBenefitEstream.setParam(E909002049_LOCATION_TINYINT, 0);
                        return;
                    } else if (c == 1) {
                        buildBenefitEstream.setParam(E909002049_LOCATION_TINYINT, 1);
                        return;
                    } else if (c == 2) {
                        buildBenefitEstream.setParam(E909002049_LOCATION_TINYINT, 2);
                        return;
                    } else {
                        return;
                    }
                }
            } else if (freqLocation.equals(Constant.NAME_FREQLOCATION_HOME)) {
                c = 0;
                if (c != 0) {
                }
            }
        } else if (freqLocation.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    private void setFreqLocRecParam(IMonitor.EventStream freqLocRecEstream, IMonitor.EventStream buildBenefitEstream, Bundle results, String freqLocation) {
        if (results != null) {
            int durationMinutes = 0;
            if (freqLocation.equals(this.mLocationDao.getFrequentLocation())) {
                long now = System.currentTimeMillis();
                long lastUpdateTime = this.mLocationDao.getLastUpdateTime();
                long benefitChrTime = this.mLocationDao.getBenefitChrTime();
                long maxTime = benefitChrTime > lastUpdateTime ? benefitChrTime : lastUpdateTime;
                if (maxTime > 0 && now > maxTime) {
                    durationMinutes = Math.round(((float) (now - maxTime)) / 60000.0f);
                }
            }
            freqLocRecEstream.setParam(E909009041_FIRSTREPORT_INT, results.getInt(KEY_FL_FIRST_REPORT_INT));
            freqLocRecEstream.setParam(E909009041_ENTERY_INT, results.getInt(KEY_FL_ENTERY_INT));
            freqLocRecEstream.setParam(E909009041_LEAVE_INT, results.getInt(KEY_FL_LEAVE_INT));
            freqLocRecEstream.setParam("dur", results.getInt("DURATION") + durationMinutes);
            freqLocRecEstream.setParam(E909009041_SPACECHANGE_INT, results.getInt(KEY_FL_SPACE_CHANGE_INT));
            freqLocRecEstream.setParam(E909009041_SPACELEAVE_INT, results.getInt(KEY_FL_SPACE_LEAVE_INT));
            buildBenefitEstream.setParam(E909002049_FREQ_LOC_REC_CLASS, freqLocRecEstream);
        }
    }

    private void setUserPreferenceParam(IMonitor.EventStream userPrefEstream, IMonitor.EventStream buildBenefitEstream, Bundle results) {
        if (results != null) {
            userPrefEstream.setParam(E909009051_TOTALSWITCH_INT, results.getInt(KEY_UP_TOTAL_SWITCH_IN));
            userPrefEstream.setParam(E909009051_AUTOSUCC_INT, results.getInt(KEY_UP_AUTO_SUCC_INT));
            userPrefEstream.setParam(E909009051_MANUALSUCC_INT, results.getInt(KEY_UP_MANUAL_SUCC_INT));
            userPrefEstream.setParam(E909009051_AUTOFAIL_INT, results.getInt(KEY_UPAUTO_FAIL_INT));
            userPrefEstream.setParam(E909009051_NOSWITCHFAIL_INT, results.getInt(KEY_UP_NO_SWITCH_FAIL_INT));
            userPrefEstream.setParam("queryCnt", results.getInt(KEY_UP_QUERY_CNT_INT));
            userPrefEstream.setParam(E909009051_RESCNT_INT, results.getInt(KEY_UP_RES_CNT_INT));
            userPrefEstream.setParam("unknownDB", results.getInt(KEY_UP_UNKNOWN_DB_INT));
            userPrefEstream.setParam("unknownSpace", results.getInt(KEY_UP_UNKNOWN_SPACE_INT));
            buildBenefitEstream.setParam(E909002049_USR_PREF_STATISTIC_CLASS, userPrefEstream);
        }
    }

    private void setLowPowerParam(IMonitor.EventStream lowPowerEstream, IMonitor.EventStream buildBenefitEstream, Bundle results) {
        if (results != null) {
            lowPowerEstream.setParam(E909009052_TOTALSWITCH_INT, results.getInt(KEY_LP_TOTAL_SWITCH_INT));
            lowPowerEstream.setParam(E909009052_DATARX_INT, results.getInt(KEY_LP_DATA_RX_INT));
            lowPowerEstream.setParam(E909009052_DATATX_INT, results.getInt(KEY_LP_DATA_TX_INT));
            lowPowerEstream.setParam("dur", results.getInt(KEY_LP_DURATION_INT));
            lowPowerEstream.setParam(E909009052_OFFSET_INT, results.getInt(KEY_LP_OFFSET_INT));
            lowPowerEstream.setParam(E909009052_ALREADYBEST_INT, results.getInt(KEY_LP_ALREADY_BEST_INT));
            lowPowerEstream.setParam(E909009052_NOTREACH_INT, results.getInt(KEY_LP_NOT_REACH_INT));
            lowPowerEstream.setParam(E909009052_BACK_INT, results.getInt(KEY_LP_BACK_INT));
            lowPowerEstream.setParam("uknDB", results.getInt(KEY_LP_UNKNOWN_DB_INT));
            lowPowerEstream.setParam("uknSpc", results.getInt(KEY_LP_UNKNOWN_SPACE_INT));
            buildBenefitEstream.setParam(E909002049_LOW_POWER_STATISTIC_CLASS, lowPowerEstream);
        }
    }

    private void setAppQoeBenefitParam(IMonitor.EventStream appQoeBenefitEstream, IMonitor.EventStream buildBenefitEstream) {
        appQoeBenefitEstream.setParam("queryCnt", this.mHisQoeChrDao.getQueryCnt());
        appQoeBenefitEstream.setParam(E909009050_GOOD_INT, this.mHisQoeChrDao.getGoodCnt());
        appQoeBenefitEstream.setParam(E909009050_POOR_INT, this.mHisQoeChrDao.getPoorCnt());
        appQoeBenefitEstream.setParam(E909009050_DATARX_INT, 0);
        appQoeBenefitEstream.setParam(E909009050_DATATX_INT, 0);
        appQoeBenefitEstream.setParam("unknownDB", this.mHisQoeChrDao.getUnknownDb());
        appQoeBenefitEstream.setParam("unknownSpace", this.mHisQoeChrDao.getUnknownSpace());
        buildBenefitEstream.setParam(E909002049_APP_QOE_BENEFIT_CLASS, appQoeBenefitEstream);
    }

    private void setBackTo4gParam(IMonitor.EventStream fastBack4gEstream, IMonitor.EventStream buildBenefitEstream) {
        fastBack4gEstream.setParam(KEY_LOW_RAT_CNT, this.mFastBack2LteChrDao.getLowRatCnt());
        fastBack4gEstream.setParam(KEY_IN_LTE_CNT, this.mFastBack2LteChrDao.getInLteCnt());
        fastBack4gEstream.setParam(KEY_OUT_LTE_CNT, this.mFastBack2LteChrDao.getOutLteCnt());
        fastBack4gEstream.setParam(KEY_FAST_BACK, this.mFastBack2LteChrDao.getFastBack());
        fastBack4gEstream.setParam(KEY_SUCCESS_BACK, this.mFastBack2LteChrDao.getSuccessBack());
        fastBack4gEstream.setParam(KEY_CELL_4G, this.mFastBack2LteChrDao.getCells4G());
        fastBack4gEstream.setParam(KEY_REF_CNT, this.mFastBack2LteChrDao.getRefCnt());
        fastBack4gEstream.setParam("uknDB", this.mFastBack2LteChrDao.getUnknown2Db());
        fastBack4gEstream.setParam("uknSpc", this.mFastBack2LteChrDao.getUnknown2Space());
        buildBenefitEstream.setParam(E909002049_FAST_BACK_TO_LTE_CLASS, fastBack4gEstream);
    }

    private void setFingerprintParam(IMonitor.EventStream signalFingerprintEstream, IMonitor.EventStream buildBenefitEstream) {
        if (signalFingerprintEstream != null) {
            buildBenefitEstream.setParam(E909002049_SIGNAL_FINGERPRINT_CLASS, signalFingerprintEstream);
        }
    }

    private void closeFingerprintEstream(IMonitor.EventStream signalFingerprintEstream) {
        if (signalFingerprintEstream != null) {
            IMonitor.closeEventStream(signalFingerprintEstream);
        }
    }
}
