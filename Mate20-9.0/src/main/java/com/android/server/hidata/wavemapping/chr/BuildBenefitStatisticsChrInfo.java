package com.android.server.hidata.wavemapping.chr;

import android.os.Bundle;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.FastBack2LteChrDAO;
import com.android.server.hidata.wavemapping.dao.HisQoEChrDAO;
import com.android.server.hidata.wavemapping.dao.LocationDAO;

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
    public static final int MSG_WAVEMAPPING_APPQOE_BENEFIT_TABLE_CLASSID = 909009050;
    public static final int MSG_WAVEMAPPING_BENEFIT_STATISTICS_EVENTID = 909002049;
    public static final int MSG_WAVEMAPPING_FAST_BACK_TO_LTE_STATISTIC_CLASSID = 909009053;
    public static final int MSG_WAVEMAPPING_FREQ_LOC_REC_CLASSID = 909009041;
    public static final int MSG_WAVEMAPPING_LOW_POWER_STATISTIC_CLASSID = 909009052;
    public static final int MSG_WAVEMAPPING_SIGNAL_FINGERPRINT_STATISTIC_CLASSID = 909009054;
    public static final int MSG_WAVEMAPPING_USR_PREF_STATISTIC_CLASSID = 909009051;
    private FastBack2LteChrDAO mFastBack2LteChrDAO = new FastBack2LteChrDAO();
    private HisQoEChrDAO mHisQoEChrDAO = new HisQoEChrDAO();
    private LocationDAO mLocationDAO = new LocationDAO();

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b9  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x01eb  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x01f5  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x02db  */
    public boolean commitCHR(String freqlocation) {
        IMonitor.EventStream buildBenefitEstream;
        IMonitor.EventStream buildBenefitEstream2;
        IMonitor.EventStream signalFingerprintEstream;
        String str = freqlocation;
        int duration_minutes = 0;
        if (str == null) {
            return false;
        }
        IMonitor.EventStream buildBenefitEstream3 = IMonitor.openEventStream(MSG_WAVEMAPPING_BENEFIT_STATISTICS_EVENTID);
        IMonitor.EventStream freqLocRecEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_FREQ_LOC_REC_CLASSID);
        IMonitor.EventStream appQoEBenefitEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_APPQOE_BENEFIT_TABLE_CLASSID);
        IMonitor.EventStream userPrefEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_USR_PREF_STATISTIC_CLASSID);
        IMonitor.EventStream lowPowerEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_LOW_POWER_STATISTIC_CLASSID);
        IMonitor.EventStream fastBack4GEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_FAST_BACK_TO_LTE_STATISTIC_CLASSID);
        if (buildBenefitEstream3 == null || freqLocRecEstream == null || appQoEBenefitEstream == null || userPrefEstream == null || lowPowerEstream == null || fastBack4GEstream == null) {
            return false;
        }
        if (str.equals(Constant.NAME_FREQLOCATION_HOME)) {
            buildBenefitEstream3.setParam(E909002049_LOCATION_TINYINT, 0);
        } else if (str.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
            buildBenefitEstream3.setParam(E909002049_LOCATION_TINYINT, 1);
        } else if (str.equals(Constant.NAME_FREQLOCATION_OTHER)) {
            buildBenefitEstream3.setParam(E909002049_LOCATION_TINYINT, 2);
        }
        Bundle results = this.mLocationDAO.findCHRbyFreqLoc(str);
        if (str.equals(this.mLocationDAO.getFrequentLocation())) {
            long now = System.currentTimeMillis();
            long lastupdatetime = this.mLocationDAO.getlastUpdateTime();
            long benefitCHRTime = this.mLocationDAO.getBenefitCHRTime();
            long maxtime = benefitCHRTime > lastupdatetime ? benefitCHRTime : lastupdatetime;
            if (maxtime > 0 && now > maxtime) {
                buildBenefitEstream = buildBenefitEstream3;
                duration_minutes = Math.round(((float) (now - maxtime)) / 60000.0f);
                if (!results.containsKey("FREQLOCATION")) {
                    freqLocRecEstream.setParam(E909009041_FIRSTREPORT_INT, results.getInt("FIRSTREPORT"));
                    freqLocRecEstream.setParam(E909009041_ENTERY_INT, results.getInt("ENTERY"));
                    freqLocRecEstream.setParam(E909009041_LEAVE_INT, results.getInt("LEAVE"));
                    freqLocRecEstream.setParam("dur", results.getInt(Constant.USERDB_APP_NAME_DURATION) + duration_minutes);
                    freqLocRecEstream.setParam(E909009041_SPACECHANGE_INT, results.getInt("SPACECHANGE"));
                    freqLocRecEstream.setParam(E909009041_SPACELEAVE_INT, results.getInt("SPACELEAVE"));
                    buildBenefitEstream2 = buildBenefitEstream;
                    buildBenefitEstream2.setParam(E909002049_FREQ_LOC_REC_CLASS, freqLocRecEstream);
                    userPrefEstream.setParam(E909009051_TOTALSWITCH_INT, results.getInt("UPTOTALSWITCH"));
                    userPrefEstream.setParam(E909009051_AUTOSUCC_INT, results.getInt("UPAUTOSUCC"));
                    userPrefEstream.setParam(E909009051_MANUALSUCC_INT, results.getInt("UPMANUALSUCC"));
                    userPrefEstream.setParam(E909009051_AUTOFAIL_INT, results.getInt("UPAUTOFAIL"));
                    userPrefEstream.setParam(E909009051_NOSWITCHFAIL_INT, results.getInt("UPNOSWITCHFAIL"));
                    userPrefEstream.setParam("queryCnt", results.getInt("UPQRYCNT"));
                    userPrefEstream.setParam(E909009051_RESCNT_INT, results.getInt("UPRESCNT"));
                    userPrefEstream.setParam("unknownDB", results.getInt("UPUNKNOWNDB"));
                    userPrefEstream.setParam("unknownSpace", results.getInt("UPUNKNOWNSPACE"));
                    buildBenefitEstream2.setParam(E909002049_USR_PREF_STATISTIC_CLASS, userPrefEstream);
                    lowPowerEstream.setParam(E909009052_TOTALSWITCH_INT, results.getInt("LPTOTALSWITCH"));
                    lowPowerEstream.setParam(E909009052_DATARX_INT, results.getInt("LPDATARX"));
                    lowPowerEstream.setParam(E909009052_DATATX_INT, results.getInt("LPDATATX"));
                    lowPowerEstream.setParam("dur", results.getInt("LPDURATION"));
                    lowPowerEstream.setParam(E909009052_OFFSET_INT, results.getInt("LPOFFSET"));
                    lowPowerEstream.setParam(E909009052_ALREADYBEST_INT, results.getInt("LPALREADYBEST"));
                    lowPowerEstream.setParam(E909009052_NOTREACH_INT, results.getInt("LPNOTREACH"));
                    lowPowerEstream.setParam(E909009052_BACK_INT, results.getInt("LPBACK"));
                    lowPowerEstream.setParam(E909009052_UNKNOWNDB_INT, results.getInt("LPUNKNOWNDB"));
                    lowPowerEstream.setParam(E909009052_UNKNOWNSPACE_INT, results.getInt("LPUNKNOWNSPACE"));
                    buildBenefitEstream2.setParam(E909002049_LOW_POWER_STATISTIC_CLASS, lowPowerEstream);
                } else {
                    buildBenefitEstream2 = buildBenefitEstream;
                }
                if (this.mHisQoEChrDAO.getCountersByLocation(str)) {
                    appQoEBenefitEstream.setParam("queryCnt", this.mHisQoEChrDAO.getQueryCnt());
                    appQoEBenefitEstream.setParam(E909009050_GOOD_INT, this.mHisQoEChrDAO.getGoodCnt());
                    appQoEBenefitEstream.setParam(E909009050_POOR_INT, this.mHisQoEChrDAO.getPoorCnt());
                    appQoEBenefitEstream.setParam(E909009050_DATARX_INT, 0);
                    appQoEBenefitEstream.setParam(E909009050_DATATX_INT, 0);
                    appQoEBenefitEstream.setParam("unknownDB", this.mHisQoEChrDAO.getUnknownDB());
                    appQoEBenefitEstream.setParam("unknownSpace", this.mHisQoEChrDAO.getUnknownSpace());
                    buildBenefitEstream2.setParam(E909002049_APP_QOE_BENEFIT_CLASS, appQoEBenefitEstream);
                }
                this.mFastBack2LteChrDAO.getCountersByLocation(str);
                fastBack4GEstream.setParam("low", this.mFastBack2LteChrDAO.getlowRatCnt());
                fastBack4GEstream.setParam("inL", this.mFastBack2LteChrDAO.getinLteCnt());
                fastBack4GEstream.setParam("outL", this.mFastBack2LteChrDAO.getoutLteCnt());
                fastBack4GEstream.setParam("fstBak", this.mFastBack2LteChrDAO.getfastBack());
                fastBack4GEstream.setParam("sucBak", this.mFastBack2LteChrDAO.getsuccessBack());
                fastBack4GEstream.setParam("cells", this.mFastBack2LteChrDAO.getcells4G());
                fastBack4GEstream.setParam("ref", this.mFastBack2LteChrDAO.getrefCnt());
                fastBack4GEstream.setParam(E909009052_UNKNOWNDB_INT, this.mFastBack2LteChrDAO.getUnknown2DB());
                fastBack4GEstream.setParam(E909009052_UNKNOWNSPACE_INT, this.mFastBack2LteChrDAO.getUnknown2Space());
                buildBenefitEstream2.setParam(E909002049_FAST_BACK_TO_LTE_CLASS, fastBack4GEstream);
                signalFingerprintEstream = new CollectFingerChrService().getCollectFingerChrEventStreamByPlace(str);
                if (signalFingerprintEstream != null) {
                    buildBenefitEstream2.setParam(E909002049_SIGNAL_FINGERPRINT_CLASS, signalFingerprintEstream);
                }
                boolean ret = IMonitor.sendEvent(buildBenefitEstream2);
                IMonitor.closeEventStream(buildBenefitEstream2);
                IMonitor.closeEventStream(freqLocRecEstream);
                IMonitor.closeEventStream(appQoEBenefitEstream);
                IMonitor.closeEventStream(userPrefEstream);
                IMonitor.closeEventStream(lowPowerEstream);
                IMonitor.closeEventStream(fastBack4GEstream);
                if (signalFingerprintEstream != null) {
                    IMonitor.closeEventStream(signalFingerprintEstream);
                }
                this.mFastBack2LteChrDAO.resetRecord(str);
                this.mLocationDAO.resetCHRbyFreqLoc(str);
                this.mHisQoEChrDAO.resetRecord(str);
                return ret;
            }
        }
        buildBenefitEstream = buildBenefitEstream3;
        if (!results.containsKey("FREQLOCATION")) {
        }
        if (this.mHisQoEChrDAO.getCountersByLocation(str)) {
        }
        this.mFastBack2LteChrDAO.getCountersByLocation(str);
        fastBack4GEstream.setParam("low", this.mFastBack2LteChrDAO.getlowRatCnt());
        fastBack4GEstream.setParam("inL", this.mFastBack2LteChrDAO.getinLteCnt());
        fastBack4GEstream.setParam("outL", this.mFastBack2LteChrDAO.getoutLteCnt());
        fastBack4GEstream.setParam("fstBak", this.mFastBack2LteChrDAO.getfastBack());
        fastBack4GEstream.setParam("sucBak", this.mFastBack2LteChrDAO.getsuccessBack());
        fastBack4GEstream.setParam("cells", this.mFastBack2LteChrDAO.getcells4G());
        fastBack4GEstream.setParam("ref", this.mFastBack2LteChrDAO.getrefCnt());
        fastBack4GEstream.setParam(E909009052_UNKNOWNDB_INT, this.mFastBack2LteChrDAO.getUnknown2DB());
        fastBack4GEstream.setParam(E909009052_UNKNOWNSPACE_INT, this.mFastBack2LteChrDAO.getUnknown2Space());
        buildBenefitEstream2.setParam(E909002049_FAST_BACK_TO_LTE_CLASS, fastBack4GEstream);
        signalFingerprintEstream = new CollectFingerChrService().getCollectFingerChrEventStreamByPlace(str);
        if (signalFingerprintEstream != null) {
        }
        boolean ret2 = IMonitor.sendEvent(buildBenefitEstream2);
        IMonitor.closeEventStream(buildBenefitEstream2);
        IMonitor.closeEventStream(freqLocRecEstream);
        IMonitor.closeEventStream(appQoEBenefitEstream);
        IMonitor.closeEventStream(userPrefEstream);
        IMonitor.closeEventStream(lowPowerEstream);
        IMonitor.closeEventStream(fastBack4GEstream);
        if (signalFingerprintEstream != null) {
        }
        this.mFastBack2LteChrDAO.resetRecord(str);
        this.mLocationDAO.resetCHRbyFreqLoc(str);
        this.mHisQoEChrDAO.resetRecord(str);
        return ret2;
    }
}
