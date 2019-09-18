package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpaceExpInfo {
    private static final float RSSI_WEIGHT_OLD = 0.8f;
    private long data_rx = 0;
    private long data_tx = 0;
    private long dubai_idle_duration = 0;
    private long dubai_idle_power = 0;
    private long dubai_screenoff_power = 0;
    private long dubai_screenoff_rx = 0;
    private long dubai_screenoff_tx = 0;
    private long dubai_screenon_power = 0;
    private long dubai_screenon_rx = 0;
    private long dubai_screenon_tx = 0;
    private HashMap<String, Long> duration_app = new HashMap<>();
    private long duration_connected = 0;
    private String networkFreq = "";
    private String networkId = "";
    private String networkName = "";
    private int network_type = 8;
    private long power_consumption = 0;
    private HashMap<String, Integer> qoe_app_good = new HashMap<>();
    private HashMap<String, Integer> qoe_app_poor = new HashMap<>();
    private int qoe_wifipro_common = 0;
    private int qoe_wifipro_good = 0;
    private int qoe_wifipro_poor = 0;
    private int signal_value = 0;
    private StringBuilder spaceId = new StringBuilder("0");
    private StringBuilder spaceIdMainAp = new StringBuilder("0");
    private int user_pref_opt_in = 0;
    private int user_pref_opt_out = 0;
    private int user_pref_stay = 0;
    private int user_pref_total_count = 0;

    public String getNetworkName() {
        return this.networkName;
    }

    public void setNetworkName(String networkname) {
        this.networkName = networkname;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public String getNetworkFreq() {
        return this.networkFreq;
    }

    public void setNetworkFreq(String networkfreq) {
        this.networkFreq = networkfreq;
    }

    public int getSpaceID() {
        if (this.spaceId == null) {
            return 0;
        }
        int space = 0;
        if (Constant.PATTERN_STR2INT.matcher(this.spaceId).matches()) {
            try {
                space = Integer.parseInt(this.spaceId.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return space;
    }

    public int getSpaceIDMain() {
        if (this.spaceIdMainAp == null) {
            return 0;
        }
        int space = 0;
        if (Constant.PATTERN_STR2INT.matcher(this.spaceIdMainAp).matches()) {
            try {
                space = Integer.parseInt(this.spaceIdMainAp.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return space;
    }

    public int getQoEWifiProPoor() {
        return this.qoe_wifipro_poor;
    }

    public void setQoEWifiProPoor(int qoe_wifipro_poor2) {
        this.qoe_wifipro_poor = qoe_wifipro_poor2;
    }

    public int getQoEWifiProCommon() {
        return this.qoe_wifipro_common;
    }

    public void setQoEWifiProCommon(int qoe_wifipro_common2) {
        this.qoe_wifipro_common = qoe_wifipro_common2;
    }

    public int getQoEWifiProGood() {
        return this.qoe_wifipro_good;
    }

    public void setQoEWifiProGood(int qoe_wifipro_good2) {
        this.qoe_wifipro_good = qoe_wifipro_good2;
    }

    public int getSignalValue() {
        return this.signal_value;
    }

    public int getUserPrefOptIn() {
        return this.user_pref_opt_in;
    }

    public void accUserPrefOptIn() {
        this.user_pref_opt_in++;
    }

    public int getUserPrefOptOut() {
        return this.user_pref_opt_out;
    }

    public void accUserPrefOptOut() {
        this.user_pref_opt_out++;
    }

    public int getUserPrefStay() {
        return this.user_pref_stay;
    }

    public void accUserPrefStay() {
        this.user_pref_stay++;
    }

    public int getUserPrefTotalCount() {
        return this.user_pref_total_count;
    }

    public void accUserPrefTotalCount() {
        this.user_pref_total_count++;
    }

    public HashMap<String, Integer> getMapAppQoePoor() {
        return this.qoe_app_poor;
    }

    public HashMap<String, Integer> getMapAppQoeGood() {
        return this.qoe_app_good;
    }

    public HashMap<String, Long> getMapAppDuration() {
        return this.duration_app;
    }

    public int getAppQoePoor(String app) {
        if (this.qoe_app_poor.containsKey(app)) {
            return this.qoe_app_poor.get(app).intValue();
        }
        return 0;
    }

    public int getAppQoeGood(String app) {
        if (this.qoe_app_good.containsKey(app)) {
            return this.qoe_app_good.get(app).intValue();
        }
        return 0;
    }

    public long getAppDuration(String app) {
        if (this.duration_app.containsKey(app)) {
            return this.duration_app.get(app).longValue();
        }
        return 0;
    }

    public long getDuration() {
        return this.duration_connected;
    }

    public int getNetworkType() {
        return this.network_type;
    }

    public long getPowerConsumption() {
        return this.power_consumption;
    }

    public long getDubaiScreenOnTx() {
        return this.dubai_screenon_tx;
    }

    public long getDubaiScreenOnRx() {
        return this.dubai_screenon_rx;
    }

    public long getDubaiScreenOnPower() {
        return this.dubai_screenon_power;
    }

    public long getDubaiScreenOffTx() {
        return this.dubai_screenoff_tx;
    }

    public long getDubaiScreenOffRx() {
        return this.dubai_screenoff_rx;
    }

    public long getDubaiScreenOffPower() {
        return this.dubai_screenoff_power;
    }

    public long getDubaiIdleDuration() {
        return this.dubai_idle_duration;
    }

    public long getDubaiIdlePower() {
        return this.dubai_idle_power;
    }

    public void accDubaiScreenOnTraffic(long dRx, long dTx) {
        this.dubai_screenon_rx += dRx;
        this.dubai_screenon_tx += dTx;
        LogUtil.i("update Dubai ScreenOn traffic: rx=" + this.dubai_screenon_rx + ", tx=" + this.dubai_screenon_tx);
    }

    public void accDubaiScreenOffTraffic(long dRx, long dTx) {
        this.dubai_screenoff_rx += dRx;
        this.dubai_screenoff_tx += dTx;
        LogUtil.i("update Dubai ScreenOff traffic: rx=" + this.dubai_screenoff_rx + ", tx=" + this.dubai_screenoff_tx);
    }

    public void accDubaiPower(long screenon_power, long screenoff_power) {
        this.dubai_screenon_power += screenon_power;
        this.dubai_screenoff_power += screenoff_power;
        this.power_consumption = this.power_consumption + screenon_power + screenoff_power;
        LogUtil.i("update Dubai Power: power_consumption=" + this.power_consumption + ", Screen On Power=" + this.dubai_screenon_power + ", Screen Off Power=" + this.dubai_screenoff_power);
    }

    public void accDubaiIdleDuration(long duration) {
        this.dubai_idle_duration += duration;
        LogUtil.i("update Dubai idle duration: " + this.dubai_idle_duration);
    }

    public void accDuabiIdlePower(long power) {
        this.dubai_idle_power += power;
        LogUtil.i("update Dubai idle power: " + this.dubai_idle_power);
    }

    public void accPowerConsumption(long power) {
        this.power_consumption += power;
    }

    public long getDataRx() {
        return this.data_rx;
    }

    public long getDataTx() {
        return this.data_tx;
    }

    public void accDataTraffic(long dRx, long dTx) {
        this.data_rx += dRx;
        this.data_tx += dTx;
        LogUtil.i("update data traffic: acc_rx=" + this.data_rx + ", acc_tx=" + this.data_tx);
    }

    public void accQoEWifiProPoor() {
        this.qoe_wifipro_poor++;
    }

    public void accQoEWifiProCommon() {
        this.qoe_wifipro_common++;
    }

    public void accQoEWifiProGood() {
        this.qoe_wifipro_good++;
    }

    public void accAppPoor(String app) {
        if (this.qoe_app_poor.containsKey(app)) {
            this.qoe_app_poor.put(app, Integer.valueOf(this.qoe_app_poor.get(app).intValue() + 1));
            return;
        }
        this.qoe_app_poor.put(app, 1);
    }

    public void accAppGood(String app) {
        if (this.qoe_app_good.containsKey(app)) {
            this.qoe_app_good.put(app, Integer.valueOf(this.qoe_app_good.get(app).intValue() + 1));
            return;
        }
        this.qoe_app_good.put(app, 1);
    }

    private void accAppDuration(String app, long newDuration) {
        if (this.duration_app.containsKey(app)) {
            this.duration_app.put(app, Long.valueOf(this.duration_app.get(app).longValue() + newDuration));
            return;
        }
        this.duration_app.put(app, Long.valueOf(newDuration));
    }

    public int accSignalValue(int newRssi) {
        if (newRssi < 0) {
            if (this.signal_value < 0) {
                this.signal_value = Math.round((((float) this.signal_value) * 0.8f) + (((float) newRssi) * 0.19999999f));
            } else {
                this.signal_value = newRssi;
            }
        }
        return this.signal_value;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0041  */
    public void accDuration(String app, long newDuration) {
        char c;
        int hashCode = app.hashCode();
        if (hashCode != -2015525726) {
            if (hashCode == 2664213 && app.equals(Constant.USERDB_APP_NAME_WIFI)) {
                c = 0;
                switch (c) {
                    case 0:
                        LogUtil.i("update WIFI duration");
                        this.duration_connected += newDuration;
                        this.network_type = 1;
                        break;
                    case 1:
                        LogUtil.i("update MOBILE duration");
                        this.duration_connected += newDuration;
                        this.network_type = 0;
                        break;
                    default:
                        LogUtil.i("update APP duration");
                        accAppDuration(app, newDuration);
                        return;
                }
            }
        } else if (app.equals(Constant.USERDB_APP_NAME_MOBILE)) {
            c = 1;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
    }

    public void mergeAllRecords(SpaceExpInfo input) {
        this.qoe_wifipro_poor += input.getQoEWifiProPoor();
        this.qoe_wifipro_common += input.getQoEWifiProCommon();
        this.qoe_wifipro_good += input.getQoEWifiProGood();
        accSignalValue(input.getSignalValue());
        this.user_pref_opt_in += input.getUserPrefOptIn();
        this.user_pref_opt_out += input.getUserPrefOptOut();
        this.user_pref_stay += input.getUserPrefStay();
        this.user_pref_total_count += input.getUserPrefTotalCount();
        this.duration_connected += input.getDuration();
        this.data_rx += input.getDataRx();
        this.data_tx += input.getDataTx();
        this.power_consumption += input.getPowerConsumption();
        this.dubai_screenon_tx += input.getDubaiScreenOnTx();
        this.dubai_screenon_rx += input.getDubaiScreenOnRx();
        this.dubai_screenon_power += input.getDubaiScreenOnPower();
        this.dubai_screenoff_tx += input.getDubaiScreenOffTx();
        this.dubai_screenoff_rx += input.getDubaiScreenOffRx();
        this.dubai_screenoff_power += input.getDubaiScreenOffPower();
        this.dubai_idle_duration += input.getDubaiIdleDuration();
        this.dubai_idle_power += input.getDubaiIdlePower();
        if (1 == input.getNetworkType() || input.getNetworkType() == 0) {
            this.network_type = input.getNetworkType();
        }
        input.getMapAppQoePoor().forEach(new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                SpaceExpInfo.this.qoe_app_poor.merge((String) obj, (Integer) obj2, $$Lambda$SpaceExpInfo$LrkJFe4YP5gsc0rXJgTGXS3PRE.INSTANCE);
            }
        });
        input.getMapAppQoeGood().forEach(new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                SpaceExpInfo.this.qoe_app_good.merge((String) obj, (Integer) obj2, $$Lambda$SpaceExpInfo$LrkJFe4YP5gsc0rXJgTGXS3PRE.INSTANCE);
            }
        });
        input.getMapAppDuration().forEach(new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                SpaceExpInfo.this.duration_app.merge((String) obj, (Long) obj2, $$Lambda$SpaceExpInfo$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y.INSTANCE);
            }
        });
    }

    public SpaceExpInfo(StringBuilder spaceid, StringBuilder spaceid_mainap, String networkid, String networkname, String networkfreq, int qoe_wifipro_good2, int qoe_wifipro_common2, int qoe_wifipro_poor2, int signal_value2, int user_pref_opt_in2, int user_pref_opt_out2, int user_pref_stay2, int user_pref_total_count2, long power_consumption2, long duration_connected2, int nw_type) {
        this.spaceId = spaceid;
        this.spaceIdMainAp = spaceid_mainap;
        this.networkId = networkid;
        this.networkName = networkname;
        this.networkFreq = networkfreq;
        this.qoe_wifipro_good = qoe_wifipro_good2;
        this.qoe_wifipro_common = qoe_wifipro_common2;
        this.qoe_wifipro_poor = qoe_wifipro_poor2;
        this.signal_value = signal_value2;
        this.power_consumption = power_consumption2;
        this.user_pref_opt_in = user_pref_opt_in2;
        this.user_pref_opt_out = user_pref_opt_out2;
        this.user_pref_stay = user_pref_stay2;
        this.user_pref_total_count = user_pref_total_count2;
        this.duration_connected = duration_connected2;
        this.network_type = nw_type;
    }

    public SpaceExpInfo(StringBuilder spaceid, StringBuilder spaceid_mainap, String networkid, String networkname, String networkfreq, HashMap<String, Integer> app_poor, HashMap<String, Integer> app_good, HashMap<String, Long> app_duration, int qoe_wifipro_good2, int qoe_wifipro_common2, int qoe_wifipro_poor2, int signal_value2, long power_consumption2, int user_pref_opt_in2, int user_pref_opt_out2, int user_pref_stay2, int user_pref_total_count2, long duration_connected2, int nw_type, long rx, long tx, long screenofftx, long screenoffrx, long screenoffpower, long screenontx, long screenonrx, long screenonpower, long idleduration, long idlepower) {
        this.spaceId = spaceid;
        this.spaceIdMainAp = spaceid_mainap;
        this.networkId = networkid;
        this.networkName = networkname;
        this.networkFreq = networkfreq;
        this.qoe_app_poor = app_poor;
        this.qoe_app_good = app_good;
        this.duration_app = app_duration;
        this.qoe_wifipro_good = qoe_wifipro_good2;
        this.qoe_wifipro_common = qoe_wifipro_common2;
        this.qoe_wifipro_poor = qoe_wifipro_poor2;
        this.signal_value = signal_value2;
        this.power_consumption = power_consumption2;
        this.user_pref_opt_in = user_pref_opt_in2;
        this.user_pref_opt_out = user_pref_opt_out2;
        this.user_pref_stay = user_pref_stay2;
        this.user_pref_total_count = user_pref_total_count2;
        this.duration_connected = duration_connected2;
        this.network_type = nw_type;
        this.data_rx = rx;
        this.data_tx = tx;
        this.dubai_screenoff_tx = screenofftx;
        this.dubai_screenoff_rx = screenoffrx;
        this.dubai_screenoff_power = screenoffpower;
        this.dubai_screenon_tx = screenontx;
        this.dubai_screenon_rx = screenonrx;
        this.dubai_screenon_power = screenonpower;
        this.dubai_idle_duration = idleduration;
        this.dubai_idle_power = idlepower;
    }

    public SpaceExpInfo(StringBuilder spaceid, StringBuilder spaceid_mainap, String networkid, String networkname, String networkfreq, int nw_type) {
        this.spaceId = spaceid;
        this.spaceIdMainAp = spaceid_mainap;
        this.networkId = networkid;
        this.networkName = networkname;
        this.networkFreq = networkfreq;
        this.network_type = nw_type;
    }

    public String toString() {
        StringBuffer appString = new StringBuffer();
        int poorCnt = 0;
        int goodCnt = 0;
        for (Map.Entry<String, Long> entry : this.duration_app.entrySet()) {
            String app = entry.getKey();
            long duration = entry.getValue().longValue();
            if (this.qoe_app_poor.containsKey(app)) {
                poorCnt = this.qoe_app_poor.get(app).intValue();
            }
            if (this.qoe_app_good.containsKey(app)) {
                goodCnt = this.qoe_app_good.get(app).intValue();
            }
            appString.append(", ");
            appString.append(app);
            appString.append(":'");
            appString.append(Constant.USERDB_APP_NAME_DURATION);
            appString.append("'='");
            appString.append(duration);
            appString.append("' ");
            appString.append(Constant.USERDB_APP_NAME_POOR);
            appString.append("'='");
            appString.append(poorCnt);
            appString.append("' ");
            appString.append(Constant.USERDB_APP_NAME_GOOD);
            appString.append("'='");
            appString.append(goodCnt);
            appString.append("' ");
            appString.append("");
        }
        String netIdPrint = "";
        if (LogUtil.getDebug_flag()) {
            netIdPrint = this.networkId;
        }
        return "SpaceExpInfo{spaceid='" + this.spaceId.toString() + '\'' + ", spaceid_mainap='" + this.spaceIdMainAp + '\'' + ", networkname='" + this.networkName + '\'' + ", networkid='" + netIdPrint + '\'' + ", networkfreq='" + this.networkFreq + '\'' + ", qoe_wifipro_poor=" + this.qoe_wifipro_poor + ", qoe_wifipro_common=" + this.qoe_wifipro_common + ", qoe_wifipro_good=" + this.qoe_wifipro_good + ", signal_value=" + this.signal_value + ", power_consumption=" + this.power_consumption + ", data_rx=" + this.data_rx + ", data_tx=" + this.data_tx + ", user_pref_opt_in=" + this.user_pref_opt_in + ", user_pref_opt_out=" + this.user_pref_opt_out + ", user_pref_stay=" + this.user_pref_stay + ", user_pref_total_count=" + this.user_pref_total_count + ", duration=" + this.duration_connected + ", nw_type=" + this.network_type + ", dubai_screenoff_tx=" + this.dubai_screenoff_tx + ", dubai_screenoff_rx=" + this.dubai_screenoff_rx + ", dubai_screenoff_power=" + this.dubai_screenoff_power + ", dubai_screenon_tx=" + this.dubai_screenon_tx + ", dubai_screenon_rx=" + this.dubai_screenon_rx + ", dubai_screenon_power=" + this.dubai_screenon_power + ", dubai_idle_duration=" + this.dubai_idle_duration + ", dubai_idle_power=" + this.dubai_idle_power + appString.toString() + '}';
    }
}
