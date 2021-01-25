package com.android.server.hidata.wavemapping.chr;

import android.os.Bundle;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.SpaceUserDao;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;

public class BuildSpaceUserStatisticsChrInfo {
    private static final int DEFAULT_BYTE_SIZE = 1024;
    private static final int DEFAULT_LOCATION_CODE = 2;
    private static final int DEFAULT_SIZE = 24;
    private static final int DEFAULT_TIME_UNIT = 1000;
    private static final String KEY_APP = "app";
    private static final String KEY_APP_QOE_DURATION = "duration";
    private static final String KEY_APP_QOE_GOOD_COUNT = "goodcount";
    private static final String KEY_APP_QOE_POOR_COUNT = "poorcount";
    private static final String KEY_DURATION = "dur";
    private static final String KEY_DURATION_CONNECTED = "duration_connected";
    private static final String KEY_FREQ = "freq";
    private static final String KEY_HISTORY_DATA_RX = "dRx";
    private static final String KEY_HISTORY_DATA_TX = "dTx";
    private static final String KEY_HISTORY_GOOD = "good";
    private static final String KEY_HISTORY_OPT_IN = "In";
    private static final String KEY_HISTORY_OPT_OUT = "Out";
    private static final String KEY_HISTORY_POOR = "poor";
    private static final String KEY_HISTORY_POWER = "power";
    private static final String KEY_HISTORY_POWER_ID = "pwrIdle";
    private static final String KEY_HISTORY_SIGNAL = "sig";
    private static final String KEY_HISTORY_STAY = "Stay";
    private static final String KEY_ID = "Id";
    private static final String KEY_LOC = "loc";
    private static final String KEY_MODEL_ALL = "modA";
    private static final String KEY_MODEL_CELL = "modC";
    private static final String KEY_MODEL_MAIN = "modM";
    private static final String KEY_MODEL_VER_ALL = "modelVerAllap";
    private static final String KEY_MODEL_VER_MAIN = "modelVerMainap";
    private static final String KEY_NAME = "name";
    private static final String KEY_NETWORK_NAME = "networkname";
    private static final String KEY_NEW_FREQ_CNT = "nwfreqcnt";
    private static final String KEY_NEW_ID_CNT = "nwidcnt";
    private static final String KEY_NEW_TYPE = "nwType";
    private static final String KEY_QOE = "qoe";
    private static final String KEY_REC = "rec";
    private static final String KEY_SPACE_ALL = "spcA";
    private static final String KEY_SPACE_CELL = "spcC";
    private static final String KEY_SPACE_ID_ALL = "spaceid";
    private static final String KEY_SPACE_ID_MAIN = "spaceidmain";
    private static final String KEY_SPACE_MAIN = "spcM";
    private static final String KEY_SPACE_NETWORK = "spcInf";
    private static final String KEY_TOTAL_COUNT = "totalCount";
    private static final String KEY_TOTAL_DURATION = "totalDuration";
    private static final String KEY_TYPE = "type";
    private static final String KEY_USER = "usr";
    private static final String KEY_USER_EXP_DATA_RX = "datarx";
    private static final String KEY_USER_EXP_DATA_TX = "datarx";
    private static final String KEY_USER_EXP_OPT_IN = "user_pref_opt_in";
    private static final String KEY_USER_EXP_OPT_OUT = "user_pref_opt_out";
    private static final String KEY_USER_EXP_POWER = "powerconsumption";
    private static final String KEY_USER_EXP_SIGNAL = "avgSignal";
    private static final String KEY_USER_EXP_STAY = "user_pref_stay";
    public static final int MSG_WAVEMAPPING_HISTORY_APPQOE_CLASSID = 909009048;
    public static final int MSG_WAVEMAPPING_HISTORY_USEREXP_CLASSID = 909009049;
    public static final int MSG_WAVEMAPPING_SPACEUSER_STATISTICS_EVENTID = 909002050;
    public static final int MSG_WAVEMAPPING_SPACE_NETWORK_GROUP_CLASSID = 909009047;
    private IMonitor.EventStream buildSpaceUserEstream;
    private IMonitor.EventStream historyAppQoeEstream;
    private IMonitor.EventStream historyUserExpEstream;
    SpaceUserDao mspaceUserDao = new SpaceUserDao();
    private IMonitor.EventStream spaceNetworkGroupEstream;

    private boolean buildHistoryUserExp(String freqLocation) {
        try {
            HashMap<Integer, Bundle> userExpRec = this.mspaceUserDao.findUserExpRecByFreqLoc(freqLocation);
            int size = 24;
            if (userExpRec.size() <= 24) {
                size = userExpRec.size();
            }
            for (int i = 1; i <= size; i++) {
                Bundle userExpValue = userExpRec.get(Integer.valueOf(i));
                if (userExpValue != null) {
                    this.historyUserExpEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_HISTORY_USEREXP_CLASSID);
                    this.spaceNetworkGroupEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_SPACE_NETWORK_GROUP_CLASSID);
                    if (this.historyUserExpEstream != null) {
                        if (this.spaceNetworkGroupEstream != null) {
                            this.spaceNetworkGroupEstream.setParam(KEY_SPACE_ALL, (short) userExpValue.getInt(KEY_SPACE_ID_ALL));
                            this.spaceNetworkGroupEstream.setParam(KEY_MODEL_ALL, userExpValue.getInt(KEY_MODEL_VER_ALL));
                            this.spaceNetworkGroupEstream.setParam(KEY_SPACE_MAIN, (short) userExpValue.getInt(KEY_SPACE_ID_MAIN));
                            this.spaceNetworkGroupEstream.setParam(KEY_MODEL_MAIN, userExpValue.getInt(KEY_MODEL_VER_MAIN));
                            Bundle userPrefValue = this.mspaceUserDao.getUerPrefTotalCountDurationByAllApSpaces(freqLocation, userExpValue.getInt(KEY_SPACE_ID_ALL), userExpValue.getInt(KEY_MODEL_VER_ALL));
                            this.spaceNetworkGroupEstream.setParam(KEY_SPACE_CELL, userExpValue.getInt(KEY_NEW_ID_CNT));
                            if (userPrefValue != null) {
                                this.spaceNetworkGroupEstream.setParam(KEY_MODEL_CELL, userPrefValue.getInt(KEY_TOTAL_DURATION));
                                this.spaceNetworkGroupEstream.setParam(KEY_ID, userPrefValue.getInt(KEY_TOTAL_COUNT));
                            }
                            this.spaceNetworkGroupEstream.setParam(KEY_NAME, userExpValue.getString(KEY_NETWORK_NAME));
                            this.spaceNetworkGroupEstream.setParam(KEY_FREQ, userExpValue.getShort(KEY_NEW_FREQ_CNT));
                            this.spaceNetworkGroupEstream.setParam("type", (byte) userExpValue.getInt(KEY_NEW_TYPE));
                            this.spaceNetworkGroupEstream.setParam(KEY_REC, userExpValue.getShort(KEY_REC));
                            setHistoryUserExpParam(this.historyUserExpEstream, userExpValue);
                            IMonitor.closeEventStream(this.spaceNetworkGroupEstream);
                            IMonitor.closeEventStream(this.historyUserExpEstream);
                        }
                    }
                    LogUtil.e(false, "buildHistoryUserExp, open Estream failed", new Object[0]);
                    return false;
                }
            }
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
            LogUtil.e(false, "buildHistoryUserExp failed by Exception", new Object[0]);
            return false;
        }
    }

    private void setHistoryUserExpParam(IMonitor.EventStream historyUserExpEstream2, Bundle userExpValue) {
        if (userExpValue != null) {
            historyUserExpEstream2.setParam(KEY_SPACE_NETWORK, this.spaceNetworkGroupEstream);
            historyUserExpEstream2.setParam("dur", (int) (userExpValue.getLong(KEY_DURATION_CONNECTED) / 1000));
            historyUserExpEstream2.setParam("dRx", (int) (userExpValue.getLong("datarx") / 1024));
            historyUserExpEstream2.setParam("dTx", (int) (userExpValue.getLong("datarx") / 1024));
            historyUserExpEstream2.setParam(KEY_HISTORY_SIGNAL, userExpValue.getShort(KEY_USER_EXP_SIGNAL));
            historyUserExpEstream2.setParam(KEY_HISTORY_OPT_IN, userExpValue.getInt(KEY_USER_EXP_OPT_IN));
            historyUserExpEstream2.setParam(KEY_HISTORY_OPT_OUT, userExpValue.getInt(KEY_USER_EXP_OPT_OUT));
            historyUserExpEstream2.setParam(KEY_HISTORY_STAY, userExpValue.getInt(KEY_USER_EXP_STAY));
            historyUserExpEstream2.setParam(KEY_HISTORY_POWER, (int) userExpValue.getLong(KEY_USER_EXP_POWER));
            historyUserExpEstream2.setParam(KEY_HISTORY_POWER_ID, 0);
            this.buildSpaceUserEstream.fillArrayParam("usr", historyUserExpEstream2);
        }
    }

    private boolean buildHistoryAppQoE(String freqLocation) {
        try {
            HashMap<Integer, Bundle> appQoeRec = this.mspaceUserDao.findAppQoeRecByFreqLoc(freqLocation);
            int size = 24;
            if (appQoeRec.size() <= 24) {
                size = appQoeRec.size();
            }
            for (int i = 1; i <= size; i++) {
                Bundle appQoeValue = appQoeRec.get(Integer.valueOf(i));
                if (appQoeValue != null) {
                    this.historyAppQoeEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_HISTORY_APPQOE_CLASSID);
                    this.spaceNetworkGroupEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_SPACE_NETWORK_GROUP_CLASSID);
                    if (this.historyAppQoeEstream != null) {
                        if (this.spaceNetworkGroupEstream != null) {
                            this.spaceNetworkGroupEstream.setParam(KEY_SPACE_ALL, (short) appQoeValue.getInt(KEY_SPACE_ID_ALL));
                            this.spaceNetworkGroupEstream.setParam(KEY_MODEL_ALL, appQoeValue.getInt(KEY_MODEL_VER_ALL));
                            this.spaceNetworkGroupEstream.setParam(KEY_SPACE_MAIN, (short) appQoeValue.getInt(KEY_SPACE_ID_MAIN));
                            this.spaceNetworkGroupEstream.setParam(KEY_MODEL_MAIN, appQoeValue.getInt(KEY_MODEL_VER_MAIN));
                            this.spaceNetworkGroupEstream.setParam(KEY_SPACE_CELL, appQoeValue.getInt(KEY_NEW_ID_CNT));
                            this.spaceNetworkGroupEstream.setParam(KEY_NAME, appQoeValue.getString(KEY_NETWORK_NAME));
                            this.spaceNetworkGroupEstream.setParam(KEY_FREQ, appQoeValue.getShort(KEY_NEW_FREQ_CNT));
                            this.spaceNetworkGroupEstream.setParam("type", (byte) appQoeValue.getInt(KEY_NEW_TYPE));
                            this.spaceNetworkGroupEstream.setParam(KEY_REC, appQoeValue.getShort(KEY_REC));
                            this.historyAppQoeEstream.setParam(KEY_APP, appQoeValue.getInt(KEY_APP));
                            this.historyAppQoeEstream.setParam(KEY_SPACE_NETWORK, this.spaceNetworkGroupEstream);
                            this.historyAppQoeEstream.setParam("dur", (int) (appQoeValue.getLong(KEY_APP_QOE_DURATION) / 1000));
                            this.historyAppQoeEstream.setParam("good", appQoeValue.getInt(KEY_APP_QOE_GOOD_COUNT));
                            this.historyAppQoeEstream.setParam("poor", appQoeValue.getInt(KEY_APP_QOE_POOR_COUNT));
                            this.buildSpaceUserEstream.fillArrayParam("qoe", this.historyAppQoeEstream);
                            IMonitor.closeEventStream(this.spaceNetworkGroupEstream);
                            IMonitor.closeEventStream(this.historyAppQoeEstream);
                        }
                    }
                    LogUtil.e(false, "buildHistoryAppQoE, open Estream failed", new Object[0]);
                    return false;
                }
            }
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
            LogUtil.e(false, "buildHistoryAppQoE failed by Exception", new Object[0]);
            return false;
        }
    }

    public boolean commitChr(String freqLocation) {
        if (freqLocation == null) {
            return false;
        }
        this.buildSpaceUserEstream = IMonitor.openEventStream((int) MSG_WAVEMAPPING_SPACEUSER_STATISTICS_EVENTID);
        if (this.buildSpaceUserEstream == null) {
            LogUtil.e(false, "commitChr, open Estream failed", new Object[0]);
            return false;
        }
        if (freqLocation.equals(Constant.NAME_FREQLOCATION_HOME)) {
            this.buildSpaceUserEstream.setParam("loc", 0);
        } else if (freqLocation.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
            this.buildSpaceUserEstream.setParam("loc", 1);
        } else if (freqLocation.equals(Constant.NAME_FREQLOCATION_OTHER)) {
            this.buildSpaceUserEstream.setParam("loc", 2);
        }
        if (!buildHistoryAppQoE(freqLocation) || !buildHistoryUserExp(freqLocation)) {
            return false;
        }
        boolean isRet = IMonitor.sendEvent(this.buildSpaceUserEstream);
        IMonitor.closeEventStream(this.buildSpaceUserEstream);
        return isRet;
    }
}
