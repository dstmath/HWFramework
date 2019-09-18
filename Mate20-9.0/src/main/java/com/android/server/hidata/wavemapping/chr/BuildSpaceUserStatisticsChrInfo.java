package com.android.server.hidata.wavemapping.chr;

import android.os.Bundle;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.SpaceUserDAO;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.util.HashMap;

public class BuildSpaceUserStatisticsChrInfo {
    public static final int MSG_WAVEMAPPING_HISTORY_APPQOE_CLASSID = 909009048;
    public static final int MSG_WAVEMAPPING_HISTORY_USEREXP_CLASSID = 909009049;
    public static final int MSG_WAVEMAPPING_SPACEUSER_STATISTICS_EVENTID = 909002050;
    public static final int MSG_WAVEMAPPING_SPACE_NETWORK_GROUP_CLASSID = 909009047;
    private IMonitor.EventStream buildSpaceUserEstream;
    private IMonitor.EventStream historyAppQoEEstream;
    private IMonitor.EventStream historyUserExpEstream;
    SpaceUserDAO mspaceUserDAO = SpaceUserDAO.getInstance();
    private IMonitor.EventStream spaceNetworkGroupEstream;

    private boolean buildHistoryUserExp(String freqlocation) {
        try {
            HashMap<Integer, Bundle> userExpRec = this.mspaceUserDAO.findUserExpRecByFreqLoc(freqlocation);
            int i = 24;
            if (userExpRec.size() <= 24) {
                i = userExpRec.size();
            }
            int size = i;
            for (int i2 = 1; i2 <= size; i2++) {
                Bundle userExpValue = userExpRec.get(Integer.valueOf(i2));
                if (userExpValue != null) {
                    this.historyUserExpEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_HISTORY_USEREXP_CLASSID);
                    this.spaceNetworkGroupEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_SPACE_NETWORK_GROUP_CLASSID);
                    if (this.historyUserExpEstream != null) {
                        if (this.spaceNetworkGroupEstream != null) {
                            this.spaceNetworkGroupEstream.setParam("spcA", (short) userExpValue.getInt("spaceid"));
                            this.spaceNetworkGroupEstream.setParam("modA", userExpValue.getInt("modelVerAllap"));
                            this.spaceNetworkGroupEstream.setParam("spcM", (short) userExpValue.getInt("spaceidmain"));
                            this.spaceNetworkGroupEstream.setParam("modM", userExpValue.getInt("modelVerMainap"));
                            Bundle userPrefValue = this.mspaceUserDAO.getUerPrefTotalCountDurationByAllApSpaces(freqlocation, userExpValue.getInt("spaceid"), userExpValue.getInt("modelVerAllap"));
                            this.spaceNetworkGroupEstream.setParam("spcC", userExpValue.getInt("nwidcnt"));
                            this.spaceNetworkGroupEstream.setParam("modC", userPrefValue.getInt("totalDuration"));
                            this.spaceNetworkGroupEstream.setParam("Id", userPrefValue.getInt("totalCount"));
                            this.spaceNetworkGroupEstream.setParam("name", userExpValue.getString("networkname"));
                            this.spaceNetworkGroupEstream.setParam("freq", userExpValue.getShort("nwfreqcnt"));
                            this.spaceNetworkGroupEstream.setParam(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, (byte) userExpValue.getInt("nwType"));
                            this.spaceNetworkGroupEstream.setParam("rec", userExpValue.getShort("rec"));
                            this.historyUserExpEstream.setParam("spcInf", this.spaceNetworkGroupEstream);
                            this.historyUserExpEstream.setParam("dur", (int) (userExpValue.getLong("duration_connected") / 1000));
                            this.historyUserExpEstream.setParam(BuildBenefitStatisticsChrInfo.E909009052_DATARX_INT, (int) (userExpValue.getLong("dubaiScreenoffRx") / 1024));
                            this.historyUserExpEstream.setParam(BuildBenefitStatisticsChrInfo.E909009052_DATATX_INT, (int) (userExpValue.getLong("dubaiScreenoffTx") / 1024));
                            this.historyUserExpEstream.setParam("sig", userExpValue.getShort("avgSignal"));
                            this.historyUserExpEstream.setParam("In", userExpValue.getInt("user_pref_opt_in"));
                            this.historyUserExpEstream.setParam("Out", userExpValue.getInt("user_pref_opt_out"));
                            this.historyUserExpEstream.setParam("Stay", userExpValue.getInt("user_pref_stay"));
                            this.historyUserExpEstream.setParam("power", (int) userExpValue.getLong("dubaiScreenoffPower"));
                            this.historyUserExpEstream.setParam("pwrIdle", (int) Math.round((Math.pow(10.0d, 10.0d) * ((double) userExpValue.getLong("dubaiIdlePower"))) / ((double) userExpValue.getLong("dubaiIdleDuration"))));
                            this.buildSpaceUserEstream.fillArrayParam(BuildBenefitStatisticsChrInfo.E909002049_USR_PREF_STATISTIC_CLASS, this.historyUserExpEstream);
                            IMonitor.closeEventStream(this.spaceNetworkGroupEstream);
                            IMonitor.closeEventStream(this.historyUserExpEstream);
                        }
                    }
                    LogUtil.e("buildHistoryUserExp, open Estream failed");
                    return false;
                }
            }
            HashMap<Integer, Bundle> hashMap = userExpRec;
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
            LogUtil.e("buildHistoryUserExp,e:" + e2.getMessage());
            return false;
        }
    }

    private boolean buildHistoryAppQoE(String freqlocation) {
        try {
            HashMap<Integer, Bundle> appQoERec = this.mspaceUserDAO.findAppQoERecByFreqLoc(freqlocation);
            int i = 24;
            if (appQoERec.size() <= 24) {
                i = appQoERec.size();
            }
            int size = i;
            for (int i2 = 1; i2 <= size; i2++) {
                Bundle appQoEValue = appQoERec.get(Integer.valueOf(i2));
                if (appQoEValue != null) {
                    this.historyAppQoEEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_HISTORY_APPQOE_CLASSID);
                    this.spaceNetworkGroupEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_SPACE_NETWORK_GROUP_CLASSID);
                    if (this.historyAppQoEEstream != null) {
                        if (this.spaceNetworkGroupEstream != null) {
                            this.spaceNetworkGroupEstream.setParam("spcA", (short) appQoEValue.getInt("spaceid"));
                            this.spaceNetworkGroupEstream.setParam("modA", appQoEValue.getInt("modelVerAllap"));
                            this.spaceNetworkGroupEstream.setParam("spcM", (short) appQoEValue.getInt("spaceidmain"));
                            this.spaceNetworkGroupEstream.setParam("modM", appQoEValue.getInt("modelVerMainap"));
                            this.spaceNetworkGroupEstream.setParam("spcC", appQoEValue.getInt("nwidcnt"));
                            this.spaceNetworkGroupEstream.setParam("name", appQoEValue.getString("networkname"));
                            this.spaceNetworkGroupEstream.setParam("freq", appQoEValue.getShort("nwfreqcnt"));
                            this.spaceNetworkGroupEstream.setParam(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, (byte) appQoEValue.getInt("nwType"));
                            this.spaceNetworkGroupEstream.setParam("rec", appQoEValue.getShort("rec"));
                            this.historyAppQoEEstream.setParam("app", appQoEValue.getInt("app"));
                            this.historyAppQoEEstream.setParam("spcInf", this.spaceNetworkGroupEstream);
                            this.historyAppQoEEstream.setParam("dur", (int) (appQoEValue.getLong("duration") / 1000));
                            this.historyAppQoEEstream.setParam(BuildBenefitStatisticsChrInfo.E909009050_GOOD_INT, appQoEValue.getInt("goodcount"));
                            this.historyAppQoEEstream.setParam(BuildBenefitStatisticsChrInfo.E909009050_POOR_INT, appQoEValue.getInt("poorcount"));
                            this.buildSpaceUserEstream.fillArrayParam(BuildBenefitStatisticsChrInfo.E909002049_APP_QOE_BENEFIT_CLASS, this.historyAppQoEEstream);
                            IMonitor.closeEventStream(this.spaceNetworkGroupEstream);
                            IMonitor.closeEventStream(this.historyAppQoEEstream);
                        }
                    }
                    LogUtil.e("buildHistoryAppQoE, open Estream failed");
                    return false;
                }
            }
            HashMap<Integer, Bundle> hashMap = appQoERec;
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
            LogUtil.e("buildHistoryAppQoE,e:" + e2.getMessage());
            return false;
        }
    }

    public boolean commitCHR(String freqlocation) {
        if (freqlocation == null) {
            return false;
        }
        this.buildSpaceUserEstream = IMonitor.openEventStream(MSG_WAVEMAPPING_SPACEUSER_STATISTICS_EVENTID);
        if (this.buildSpaceUserEstream == null) {
            LogUtil.e("commitCHR, open Estream failed");
            return false;
        }
        if (freqlocation.equals(Constant.NAME_FREQLOCATION_HOME)) {
            this.buildSpaceUserEstream.setParam(BuildBenefitStatisticsChrInfo.E909002049_LOCATION_TINYINT, 0);
        } else if (freqlocation.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
            this.buildSpaceUserEstream.setParam(BuildBenefitStatisticsChrInfo.E909002049_LOCATION_TINYINT, 1);
        } else if (freqlocation.equals(Constant.NAME_FREQLOCATION_OTHER)) {
            this.buildSpaceUserEstream.setParam(BuildBenefitStatisticsChrInfo.E909002049_LOCATION_TINYINT, 2);
        }
        if (!buildHistoryAppQoE(freqlocation) || !buildHistoryUserExp(freqlocation)) {
            return false;
        }
        boolean ret = IMonitor.sendEvent(this.buildSpaceUserEstream);
        IMonitor.closeEventStream(this.buildSpaceUserEstream);
        return ret;
    }
}
